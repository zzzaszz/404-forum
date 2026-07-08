# FileServiceImpl MinIO 文件上传实现分析

> 源文件：`inks-service-utils-master/inks-service-std-utils/.../service/impl/FileServiceImpl.java`
> 分析范围：MinIO 相关的文件上传、分片上传、代理访问、配置管理与鉴权机制。
> 本文件是一个多后端（MinIO / 阿里云 OSS / FAS）统一文件服务，MinIO 是其中一种存储后端。

---

## 一、整体定位

`FileServiceImpl` 实现了 `FileService` 接口，对外提供统一的文件上传/下载/代理能力，内部按租户（tenantId）路由到不同 OSS 后端：

```
                  ┌──────────────────────────┐
   调用方 ───────► │   FileServiceImpl        │
                  │  (统一入口 + 鉴权 + 路由) │
                  └───────────┬──────────────┘
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
        OssMinioService  OssAliyunService  OssFasService
        (MinioClient)    (阿里云 OSS)      (FAS HTTP)
```

MinIO 相关代码散落在两类位置：

1. **统一抽象层**：通过 `OssService` 接口委托给 `ossMinioService`（`getCurrentOssService()` 按租户 ossType 选择）。
2. **直接使用 `MinioClient`**：分片上传、代理访问、流式传输、配置测试等场景直接操作 `io.minio.*` API。

---

## 二、依赖与配置

### 2.1 关键依赖

- `io.minio.*`：MinIO Java SDK（`MinioClient`、`PutObjectArgs`、`GetObjectArgs`、`StatObjectArgs`、`BucketExistsArgs`、
  `MakeBucketArgs`）。
- `com.aliyun.oss.*`：阿里云 OSS（对比参考）。
- `cn.hutool.core.util.IdUtil`：生成 `fastSimpleUUID` 作为文件名。
- `org.apache.commons.io.IOUtils`：流拷贝。
- `thumbnailator`：图片缩略图。
- `DynamicOSSClientFactory` / `OSSConfigManager`：按租户动态获取 MinIO 客户端与配置。

### 2.2 配置项

| 配置键                            | 默认值                                       | 说明         |
|--------------------------------|-------------------------------------------|------------|
| `inks.oss.fastfail`            | true                                      | OSS 快速失败开关 |
| `inks.oss.urlprefix`           | 空                                         | 默认 URL 前缀  |
| `file.upload.temp-dir`         | `${java.io.tmpdir}/allfile-upload-chunks` | 分片临时目录     |
| `file.upload.expiration-hours` | 24                                        | 分片过期清理小时数  |

### 2.3 MinIO 客户端获取

客户端**按租户隔离**，通过工厂缓存：

```java
// 当前登录用户租户
private MinioClient getCurrentMinioClient() {
    String tenantId = tokenService.getLoginUser().getTenantid();
    return dynamicOSSClientFactory.getMinioClient(tenantId);
}

// 指定租户
private MinioClient getMinioClient(String tenantId) {
    return dynamicOSSClientFactory.getMinioClient(tenantId);
}
```

> 客户端不直接 `new`，而是走 `DynamicOSSClientFactory`，便于配置变更后刷新缓存（`refreshOssConfig` →
`dynamicOSSClientFactory.refreshClientCache(tid)`）。

---

## 三、MinIO 上传的几种方式

### 3.1 通用上传 `upload(MultipartFile, dir, filename, auth, module, tid)`

流程：

1. `getCurrentOssService()` 按租户 ossType 取 `OssService` 实现（MinIO 时为 `ossMinioService`）。
2. `buildObjectName(tid, dir, filename, originalFilename)` 生成对象路径。
3. `verifyAccess(objectName, auth, ...)` 鉴权（目录权限）。
4. 委托 `service.upload(file, objectName, module)` 实际上传。

> 实际的 `MinioClient.putObject` 调用在 `OssMinioServiceImpl` 中，本类不直接操作。

### 3.2 自定义路径上传 `uploadByPathCustom(...)`（直接操作 MinioClient）

这是本类中**直接使用 MinIO SDK** 的上传路径，支持传入自定义的 endpoint/ak/sk/bucket：

```java
MinioClient minioClient = MinioClient.builder()
        .endpoint(ossendpoint)
        .credentials(ossaccesskey, osssecretkey)
        .build();
// 1. 桶存在性检查，不存在则创建
boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(ossbucket).build());
if(!bucketExists){
        minioClient.

makeBucket(MakeBucketArgs.builder().

bucket(ossbucket).

build());
        }
// 2. 流式上传
        minioClient.

putObject(PutObjectArgs.builder()
        .

bucket(ossbucket).

object(objectPath)
        .

stream(inputStream, file.length(), -1)   // -1 表示分片大小由 SDK 决定
        .

contentType(Files.probeContentType(file.toPath()))
        .

build());
// 3. 返回拼装的访问 URL
        return ossendpoint +"/"+ossbucket +"/"+objectPath;
```

要点：

- `putObject` 用 `stream(InputStream, size, partSize)` 重载，`partSize=-1` 让 SDK 自动选分片大小（适合中小文件）。
- `contentType` 用 `Files.probeContentType` 推断。
- 返回 URL 为 `endpoint/bucket/objectPath` 直连形式（非代理）。

### 3.3 流上传 `uploadByStream(InputStream, dir, filename, contentType, size, module, tid)`

- size 为空时 `IOUtils.toByteArray` 全量读入内存补全 size（**大文件慎用**，会 OOM）。
- 委托 `service.uploadStream(inputStream, bucketName, objectName, size, contentType, module)`。

### 3.4 文本/HTML 上传 `uploadByText` / `uploadByHtml`

将字符串按 UTF-8 转字节流，强制 `text/plain` / `text/html`，走 `uploadStream`。

### 3.5 图片上传 `upload2Image`

- 上传原图。
- 若原图 > 30KB，用 `Thumbnails.of(...).size(200,200).outputQuality(0.7)` 生成缩略图再上传，返回 `originUrl` + `thumbUrl`。
- ≤30KB 时缩略图直接复用原图 URL。

### 3.6 Base64 上传 `uploadByBase64`

解析 `data:image/png;base64,xxxx` 形式（截取逗号后内容），按扩展名推断 contentType，走 `uploadStream`。

---

## 四、大文件分片上传（核心，直接操作 MinioClient）

这是本类最复杂的部分，采用**「本地分片暂存 + 合并后整体 putObject」**方案，而非 MinIO 原生 multipart upload。

### 4.1 状态存储（内存 + 本地磁盘）

```java
// uploadId → 分片元信息
ConcurrentHashMap<String, ChunkInfo> CHUNK_INFO_MAP
// uploadId → 最后访问时间（用于过期清理）
ConcurrentHashMap<String, Long> CHUNK_LAST_ACCESS_TIME
// uploadId → 合并进度
Map<String, MergeProgress> MERGE_PROGRESS_MAP
// uploadId → 合并任务 Future
Map<String, Future<?>> ACTIVE_MERGE_TASKS
```

> ⚠️ 状态存内存：**单机方案，多实例部署会失效**（分片落在不同节点无法合并）。

### 4.2 三阶段流程

#### 阶段1：初始化 `initMultipartUpload(originalFileName, fileSize, chunkSize, existingUploadId, tid)`

1. 懒加载初始化 MinIO（`initMinioForSharding`）：检查/创建桶。
2. 若 `existingUploadId` 存在且文件名/大小匹配 → **断点续传**：校验已上传分片文件是否仍在磁盘，返回 `resumed=true` +
   `uploadedCount`。
3. 否则新建 `uploadId`（UUID），创建临时目录 `tempDir/{uploadId}`，计算 `totalChunks = ceil(fileSize/chunkSize)`，初始化
   `ChunkInfo`。
4. 返回 `uploadId / chunkSize / totalChunks / uploadedChunks / chunkHashes`。

#### 阶段2：分片上传 `uploadChunk(MultipartFile, uploadId, chunkNumber, clientChunkHash)`

1. 校验 uploadId 与 chunkNumber 范围。
2. `file.transferTo(chunkFile)` 将分片落到本地 `chunk_{n}` 文件。
3. 标记 `uploadedChunks[n]=true`，记录 `chunkHashes[n]`。
4. 校验文件非空，统计已上传数，返回 `isCompleted = (uploadedCount == totalChunks)`。

#### 阶段3：合并并上传 `completeMultipartUpload(uploadId, dir, clientFilename, tid)`

**异步执行**（提交到 `chunkMergeExecutor` 固定线程池 4 线程）：

1. **CHECKING**：校验所有分片均已上传，缺片则标记 ERROR。
2. **MERGING**：按顺序读取 `chunk_0..chunk_n`，8KB 缓冲写入合并文件 `mergedFile`，实时更新进度 20%→70%。
3. **UPLOADING_TO_MINIO**：`minioClient.putObject` 整体上传合并文件到最终 objectName，进度 70%→90%。
4. **COMPLETED**：进度 100%。

```java
minioClient.putObject(PutObjectArgs.builder()
        .

bucket(configManager.getBucket())
        .

object(chunkInfo.getObjectName())
        .

contentType(contentType)
        .

stream(is, mergedFile.length(), -1)
        .

build());
```

返回时即响应「任务已启动」，合并通过 `getMergeProgress` 轮询。

### 4.3 进度查询 `getMergeProgress(uploadId)`

返回 `status / percent / current / total / errorMessage`，COMPLETED 时附带 `objectName / fileName / fileSize / url`（
`/File/proxy/{objectName}`）。

### 4.4 取消 `cancelMultipartUpload(uploadId)`

- 取消正在运行的合并 Future。
- 清理所有内存 map 与本地分片临时目录。

### 4.5 过期清理 `cleanupExpiredChunkUploads`

`@Scheduled(fixedRate = 1h)`：扫描 `CHUNK_LAST_ACCESS_TIME`，超过 `expiration-hours`（默认 24h）未访问的 uploadId 调用
`cancelMultipartUpload` 清理。

### 4.6 分片方案评价

| 维度   | 现状                          | 评价                                       |
|------|-----------------------------|------------------------------------------|
| 合并方式 | 本地合并后整体 putObject           | 非 MinIO 原生 multipart，大文件需本地双倍磁盘空间（分片+合并） |
| 状态存储 | JVM 内存                      | 单机方案，**不支持多实例**，重启丢任务                    |
| 续传   | 支持（按 uploadId + 文件名 + 大小匹配） | 较完善                                      |
| 并发   | 固定 4 线程池                    | 合并是 IO 密集，4 线程偏少                         |
| 原子性  | 合并失败时已上传分片保留，可重试            | 可接受                                      |
| 进度反馈 | 内存 map 轮询                   | 简单有效，但无 SSE/WebSocket 推送                 |

> **改进建议**：改用 MinIO 原生 `composeObject` / multipart upload API，分片直接上传到 MinIO，合并由服务端完成，免去本地磁盘与双倍空间；状态改存
> Redis 以支持多实例。

---

## 五、MinIO 文件代理访问与下载

### 5.1 代理下载 `proxyFile(sec, request, response)`

路径 `/proxy/**` 提取 objectName，按 `busSecurity` 开关分两条鉴权路径，最终 `doProxy` 分发：

MinIO 分支：

```java
MinioClient minioClient = getMinioClient(tenantId);
ObjectStat stat = minioClient.statObject(StatObjectArgs.builder()
        .bucket(bucketName).object(objectName).build());
filesize =stat.

length();

contenttype =stat.

contentType();

fileoriname =

getUserMetaFromStat(stat, "orifilename");  // 读取 x-amz-meta-orifilename

fileoriname =URLDecoder.

decode(fileoriname, "UTF-8");

boolean isDownload = shouldForceDownload(filename, contenttype);

setContentDispositionAuto(response, isDownload, filename);
try(
InputStream is = minioClient.getObject(GetObjectArgs.builder()
        .bucket(bucketName).object(objectName).build())){
        response.

setContentType(contenttype);
    response.

setContentLengthLong(filesize);
    IOUtils.

copy(is, response.getOutputStream());
        }
```

要点：

- 先 `statObject` 取元数据（大小、类型、自定义 meta），再 `getObject` 取流。
- `shouldForceDownload` 按扩展名/MIME 决定 `Content-Disposition` 是 `inline`（浏览器预览）还是 `attachment`（下载）。
    - INLINE 扩展名：jpg/jpeg/png/gif/bmp/webp/svg/ico
    - INLINE MIME：image/ video/ audio/ text/plain application/pdf
- 下载行为会写文件日志 `UtsFilelogPojo`（optype=1）。

### 5.2 文本代理 `proxyByText`

以 UTF-8 Reader 拷贝到 `response.getWriter()`，`Content-Type: text/plain`。

### 5.3 Base64 代理 `proxyByBase64`

读取对象字节流，Base64 编码返回字符串。

### 5.4 流式传输 `streamFile`

路径 `/stream/**`，直接 `getObject` 流拷贝，**未支持 Range 断点续传**（视频拖拽会受限）。

### 5.5 取字节 `getObjectBytes`

`service.getObject` 取流后 `IOUtils.toByteArray` 全量读入（**大文件 OOM 风险**）。

---

## 六、鉴权机制 `verifyAccess`（4 关校验）

```
第一关：公开目录  ── publicmark=1 且路径前缀匹配 ──► 放行（无需登录）
   第二关：用户识别  ── preloadedUser > auth > sec > 请求Token ──► 失败则 403
      第三关：规则匹配  ── dirname 是 objectName 前缀 ──► 无匹配则放行
         第四关：黑白名单 ── 黑名单命中拒绝 / 白名单不命中拒绝 ──► 否则放行
```

### 已记录的隐患（源码注释问题 1-8）

1. **规则缓存 Key 硬编码 `DEFAULT_TENANT`**：多租户不隔离，A 租户的公开目录配置会影响 B 租户。
2. **无规则时 fail-open**：`rules==null` → 默认放行，与安全最佳实践（fail-closed）相反。
3. **公开目录前缀匹配无 `/` 边界**：`"pub"` 会命中 `"pub-other/..."`（前缀污染）。
4. **`findFirst` 非确定性**：多条规则匹配时不保证取最长/最精确那条，应最长前缀匹配。
5. **黑白名单 split 无 trim**：`"userA, userB"` 中带空格的 `userB` 匹配失败。
6. **公开目录子目录无法单独限制**：公开规则优先级高于更精确的黑白名单规则。
7. **鉴权失败错误信息不区分**：auth/sec/请求Token 任一失败均 403，调试困难（有意为之）。
8. **`setStatus(403)` 可能流已提交**：缺少 `response.isCommitted()` 检查。

---

## 七、对象名构建 `buildObjectName`

```java
// 规则：{tenantId}/{dir}/{yyyyMM}/{filename}
String fullDir = cleanedDir.isEmpty()
                ? tid + "/" + yearMonth
                : tid + "/" + cleanedDir + "/" + yearMonth;
return fullDir +"/"+filenameToUse;
```

文件名选择优先级：

1. `clientProvidedFilename` 非空 → 直接用。
2. `isOriginalFilename(tid)` 为真 → 用原始文件名。
3. 否则 → `UUID + 原扩展名`（防冲突、防泄露原名）。
4. 都没有 → 纯 UUID。

> 第一段恒为 `tenantId`，故 `extractTenantIdFromPath` 直接取路径第一段即可反推租户。

---

## 八、配置测试 `testOssConfig`

针对 MinIO：

1. 参数完整性校验（bucket/endpoint/ak/sk 非空）。
2. 构建临时 `MinioClient`，`bucketExists` 探活。
3. 可选 `testupload=true`：内存生成 100×100 红色 PNG，`putObject` 到 `pic/{yyyyMM}/test_{ts}.png`，返回可访问 URL。

> 测试客户端为临时构建、未复用缓存，且未 shutdown（MinIO SDK 无显式 shutdown，可接受）。

---

## 九、生命周期与线程安全

- `@PostConstruct initService`：创建分片临时目录、初始化 4 线程合并池。OSS 配置**懒加载**（首次使用才初始化）。
- `@PreDestroy destroyService`：关闭线程池。
- `initMinioForSharding()`：`synchronized` 双重检查，确保桶只创建一次。
- 状态 map 均为 `ConcurrentHashMap`，但**跨实例不共享**。

---

## 十、404-forum 项目落地方案

> 本章基于本项目实际环境，给出可直接参照实现的 MinIO 图片上传方案。**以下代码为文档内参考片段，不在本次改动中创建实际文件**
> （按需求只完善文档）。

### 10.1 项目现状盘点

| 项           | 现状                                                             | 说明                        |
|-------------|----------------------------------------------------------------|---------------------------|
| MinIO 依赖    | ✅ `svc/pom.xml` 已引入 `io.minio:minio`（版本由父 POM 管理）              | 无需再加依赖                    |
| MinIO 配置    | ❌ `application-dev.yml` 无 minio 配置                             | 需新增 endpoint/ak/sk/bucket |
| 数据库列        | ✅ `Post.Images` 为 `json` 列                                     | 存图片 URL/对象名 JSON 数组       |
| Pojo/Mapper | ✅ `PostPojo.images`（String）、`PostMapper.xml` 已支持 insert/update | 直接可用                      |
| 业务层         | ⚠️ `ForumServiceImpl.createTopic` 当前 `setImages("")`           | 需接收前端传入的图片列表              |
| 控制器         | ✅ `ForumController @RequestMapping("/forum")`，统一 `R<T>`        | 上传接口挂此处                   |
| 鉴权          | ✅ `JwtFilter` + `UserContext`，`AdminInterceptor`               | 上传需登录态                    |
| 前端代理        | ✅ Vite `/api → localhost:10610`                                | 上传走 `/api/forum/upload`   |
| 前端组件        | ✅ Element Plus `el-upload` 可用                                  | 配合 `mapTopic` 现有头像机制      |

### 10.2 配置（新增到 `svc/src/main/resources/application-dev.yml`）

```yaml
# MinIO 文件存储
minio:
  endpoint: http://192.168.99.240:9000        # MinIO 服务地址
  access-key: minioadmin                       # 访问密钥
  secret-key: minioadmin                       # 秘密密钥
  bucket: 404-forum                            # 桶名
  url-prefix: /api/forum/file                  # 访问前缀（走代理）
```

> `url-prefix` 指向本服务代理接口，避免暴露 MinIO 直连地址；若图片允许公开直读，也可配为 MinIO endpoint。

### 10.3 客户端配置类（参考片段）

新建 `svc/.../config/MinioConfig.java`：

```java

@Configuration
@ConfigurationProperties(prefix = "minio")
@Data
public class MinioConfig {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String urlPrefix;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
```

> 单例客户端，无需多租户工厂（论坛无多租户）。桶初始化在首次上传时按需创建（参考原实现 `initMinioForSharding` 的双重检查）。

### 10.4 文件服务（参考片段）

新建 `svc/.../service/MinioFileService.java` + impl，只保留论坛需要的三个能力：

```java
public interface MinioFileService {
    /** 上传图片，返回 objectName（非完整 URL） */
    String uploadImage(MultipartFile file, String boardCode) throws Exception;

    /** 代理读取对象到 response（支持 inline 预览） */
    void proxyObject(String objectName, HttpServletResponse response) throws Exception;

    /** 删除对象 */
    void deleteObject(String objectName) throws Exception;
}
```

实现要点（借鉴原实现）：

- `uploadImage`：
  ```java
  // 1. 校验：非空、是图片、≤10MB（MAX_IMAGE_SIZE）
  // 2. 对象名：forum/{boardCode}/{yyyyMM}/{uuid}.{ext}
  String objectName = String.format("forum/%s/%s/%s.%s",
          boardCode, yyyyMM, IdUtil.fastSimpleUUID(), ext);
  // 3. 桶不存在则建（双重检查，synchronized）
  // 4. putObject 流式上传，contentType 用 inferContentType
  minioClient.putObject(PutObjectArgs.builder()
          .bucket(bucket).object(objectName)
          .stream(file.getInputStream(), file.getSize(), -1)
          .contentType(file.getContentType())
          .build());
  return objectName;
  ```
- `proxyObject`：`statObject` 取大小/类型 → `shouldForceDownload` 决定 inline/attachment → `getObject` 流拷贝到
  `response.getOutputStream()`。
- `deleteObject`：`removeObject`，捕获 not-found 视为成功（参考原 `isNotFound`）。

### 10.5 上传接口设计（挂 `ForumController`）

| 接口   | 方法   | 路径                   | 鉴权        | 说明                                                                   |
|------|------|----------------------|-----------|----------------------------------------------------------------------|
| 上传图片 | POST | `/forum/upload`      | 登录        | `multipart/form-data`，参数 `file` + `boardCode`，返回 `{objectName, url}` |
| 图片代理 | GET  | `/forum/file/**`     | 公开（或 sec） | 代理读取，inline 预览                                                       |
| 删除图片 | POST | `/forum/file/delete` | 登录        | 入参 `{objectName}`                                                    |

返回示例（上传）：

```json
{
  "code": 200,
  "data": {
    "objectName": "forum/tech/202606/abc123.png",
    "url": "/api/forum/file/forum/tech/202606/abc123.png"
  }
}
```

> 上传接口需登录（`UserContext.getUserId()` 非空），代理接口可公开读（帖子图片需匿名可见）。`/forum/file/**` 加入 `JwtFilter`
> 放行或允许无 token 访问。

### 10.6 帖子图片存储约定

`Post.Images`（JSON 列）存**对象名数组**，而非完整 URL：

```json
[
  "forum/tech/202606/abc123.png",
  "forum/tech/202606/def456.jpg"
]
```

- 发帖时：前端先调 `/forum/upload` 逐张上传拿到 objectName，再随 `createTopic` 提交 `images` 数组。
- `ForumServiceImpl.createTopic` 改为接收 `images`（List<String>），用 `JSON.toJSONString` 存入 `PostPojo.images`，替代当前
  `setImages("")`。
- 展示时：`mapTopic` 解析 `images` JSON 为数组，每项拼 `urlPrefix + objectName` 渲染。

### 10.7 前端对接

`web/src/api/index.js` 新增：

```js
export function uploadImage(file, boardCode) {
    const form = new FormData()
    form.append('file', file)
    form.append('boardCode', boardCode)
    return request({
        url: '/forum/upload',
        method: 'post',
        data: form,
        headers: {'Content-Type': 'multipart/form-data'}
    })
}
```

发帖页（`TopicCategory.vue` / 新建 `CreatePost.vue`）用 `el-upload`：

```vue

<el-upload
    :http-request="handleUpload"
    list-type="picture-card"
    :limit="9"
    accept="image/*"
>
  <el-icon>
    <Plus/>
  </el-icon>
</el-upload>
```

```js
async function handleUpload({file}) {
    const res = await uploadImage(file, boardCode.value)
    imageList.value.push(res.data.objectName) // 收集 objectName
}

// 提交发帖
createTopic({boardCode, title, content, images: imageList.value})
```

详情页 `TopicDetail.vue` 解析 `topic.images` 渲染 `<el-image>`。

### 10.8 对象名与安全规则

- **对象名**：`forum/{boardCode}/{yyyyMM}/{uuid}.{ext}` —— 借鉴原 `buildObjectName`，去掉租户段，用 boardCode 分类，uuid
  防冲突与原名泄露。
- **上传校验**：类型限 image/*、大小 ≤10MB、扩展名白名单（jpg/jpeg/png/gif/webp）。
- **访问控制**：帖子图片匿名可见 → 代理接口公开；上传/删除需登录。
- **删除联动**：删帖（`PostController.delete`）时一并删除关联图片对象，避免孤儿文件。

### 10.9 不引入的能力（明确裁剪）

| 原实现能力                 | 是否引入 | 原因                 |
|-----------------------|------|--------------------|
| 多租户动态配置工厂             | ❌    | 论坛无多租户             |
| 内存态分片上传               | ❌    | 单机限制，论坛图片 <10MB 无需 |
| `verifyAccess` 目录规则   | ❌    | 改用简单的登录态 + 板块校验    |
| `getObjectBytes` 全量读入 | ❌    | OOM 风险，改流式代理       |
| Base64/文本/HTML 上传     | ❌    | 论坛暂不需要             |
| 配置热刷新 / 测试接口          | ⬜    | 可选，二期再考虑           |

### 10.10 借鉴点速查表（保留）

| 模式                                                       | 借鉴价值     | 简化建议                              |
|----------------------------------------------------------|----------|-----------------------------------|
| `MinioClient.builder().endpoint().credentials().build()` | 客户端构建    | 直接单例 Bean                         |
| `putObject(stream, size, -1)`                            | 流式上传     | 帖子图片整体上传即可                        |
| `buildObjectName` 路径规则                                   | 路径分类     | `{boardCode}/{yyyyMM}/{uuid}.ext` |
| `shouldForceDownload`                                    | 预览 vs 下载 | 图片 inline，其他 attachment           |
| 代理访问 `/file/**`                                          | 隐藏存储     | 论坛图片走代理公开读                        |
| `statObject` 取 meta                                      | 大小/类型    | 代理下载时参考                           |

---

## 十一、实施路线图（按文件粒度）

> 本路线图列出落地方案涉及的文件与改动点，供后续按文档实施时对照。**当前仅文档，不创建代码。**

| 阶段       | 文件                                                 | 改动                                    | 依赖  |
|----------|----------------------------------------------------|---------------------------------------|-----|
| 1 配置     | `svc/src/main/resources/application-dev.yml`       | 新增 `minio:` 配置块                       | 无   |
| 2 客户端    | `svc/.../config/MinioConfig.java`（新建）              | `MinioClient` Bean + 配置属性             | 阶段1 |
| 3 服务     | `svc/.../service/MinioFileService.java` + impl（新建） | upload/proxy/delete                   | 阶段2 |
| 4 接口     | `svc/.../controller/ForumController.java`          | 增 `/upload`、`/file/**`、`/file/delete` | 阶段3 |
| 5 鉴权     | `svc/.../filter/JwtFilter.java`                    | 放行 `/forum/file/**`                   | 阶段4 |
| 6 业务     | `svc/.../service/impl/ForumServiceImpl.java`       | `createTopic` 接收 `images`，存 JSON      | 阶段4 |
| 7 DTO    | `ForumService.createTopic` 签名                      | 增加 `List<String> images` 参数           | 阶段6 |
| 8 前端 API | `web/src/api/index.js`                             | 新增 `uploadImage`                      | 阶段4 |
| 9 前端发帖   | `web/src/views/TopicCategory.vue`                  | `el-upload` + 提交 images               | 阶段8 |
| 10 前端展示  | `web/src/views/TopicDetail.vue`                    | 解析 images 渲染                          | 阶段6 |
| 11 删帖联动  | `PostServiceImpl.delete`                           | 删帖时删图片对象                              | 阶段3 |
| 12 文档    | 本文档 / `03-后端API接口文档.md`                            | 补上传接口说明                               | 阶段4 |

### 验收标准

- 登录用户发帖可上传 ≤9 张图片，每张 ≤10MB。
- 帖子详情正常展示图片，匿名用户可查看。
- 删帖后关联图片从 MinIO 删除。
- 未登录上传返回「请先登录」。

---

## 十二、关键问题汇总（原实现）

| #  | 问题                            | 位置                               | 风险          |
|----|-------------------------------|----------------------------------|-------------|
| 1  | 分片状态存内存                       | `CHUNK_INFO_MAP` 等               | 多实例失效、重启丢任务 |
| 2  | 合并需本地双倍磁盘                     | `completeMultipartUpload`        | 大文件占满磁盘     |
| 3  | 未用 MinIO 原生 multipart         | 分片上传整体设计                         | 效率与可扩展性差    |
| 4  | `uploadByStream` size 为空时全量读入 | `IOUtils.toByteArray`            | 大文件 OOM     |
| 5  | `getObjectBytes` 全量读入         | 末尾                               | 大文件 OOM     |
| 6  | `streamFile` 不支持 Range        | `streamMinioObject`              | 视频拖拽受限      |
| 7  | 鉴权 fail-open                  | `verifyAccess` 第三关               | 无规则=全放行     |
| 8  | 规则缓存多租户不隔离                    | `DIR_RULE_LIST + DEFAULT_TENANT` | 跨租户泄露       |
| 9  | 公开目录前缀无边界                     | `objectName.startsWith(dirname)` | 前缀污染        |
| 10 | `findFirst` 非最长匹配             | 规则匹配                             | 取到较宽规则      |
| 11 | 黑白名单 split 无 trim             | 第四关                              | 带空格配置失效     |
| 12 | 合并线程池固定 4                     | `initService`                    | 高并发瓶颈       |
| 13 | `testOssConfig` 临时客户端未复用      | 测试方法                             | 资源浪费（轻微）    |

> 本项目落地时（第十章）已规避上述 1/4/5/7/8/9/10/11 项。

---

## 十三、总结

`FileServiceImpl` 是一个功能完整但复杂度高的企业级文件服务：

- **优点**：统一抽象多 OSS 后端、支持断点续传与进度反馈、代理访问隐藏存储细节、配置可热刷新与测试、文件名防冲突、按租户隔离。
- **核心短板**：分片上传为单机内存态方案、多处全量读入有 OOM 风险、鉴权 fail-open 与多租户隔离缺陷、未利用 MinIO 原生
  multipart 能力。

对于 404-forum 这类轻量论坛，建议只借鉴其 **MinIO 客户端构建 + putObject 流式上传 + 对象名规则 + 代理/直链访问**
这几条，分片与多租户动态配置可不必引入。

**本项目落地结论**：第十章已给出贴合 404-forum 的完整方案——复用已引入的 `minio` 依赖与 `Post.Images` JSON 列，新增
`MinioConfig` + `MinioFileService` + `ForumController` 上传/代理/删除三接口，对象名采用
`forum/{boardCode}/{yyyyMM}/{uuid}.{ext}`，前端用 `el-upload` 上传、发帖提交 objectName 数组。第十一章给出 12
步文件粒度路线图与验收标准，可直接照此实施。
