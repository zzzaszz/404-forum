# 03 · 后端 API 接口文档（ForumController）

> 本文档为前后端联调的**契约依据**。所有论坛 C 端业务接口挂在 `/forum` 下，统一返回体 `R<T> { code, msg, data }`，成功
`code=200`。
> 完整建表脚本见 `doc/atest.sql`，字段说明见 `02-数据库设计.md`。

## 一、通用约定

- Base URL（开发）：`http://localhost:10610`
- 前端通过 Vite 代理 `/api → localhost:10610`（去掉 `/api` 前缀），故前端调用形如 `/forum/topics`。
- 鉴权：`/api/auth/login`、`/api/auth/register`、`/api/auth/refresh`、`/api/auth/logout` 由 `JwtFilter` 放行；其余接口建议携带
  `Authorization: <accessToken>`。匿名浏览接口（列表/详情/热门/搜索/统计/在线）可不携带；写操作（发帖/评论/点赞/收藏/举报/发私信）需登录态，从
  `UserContext` 取 `userId`。
- 登录/注册详见 `06-登录注册文档.md`（账号密码，密码 SHA-256+盐哈希）。
- 时间字段后端按 `yyyy-MM-dd HH:mm:ss` 序列化（Jackson 配置）。

## 二、话题相关

### 2.1 话题列表（最新 / 按板块 / 搜索）

`GET /forum/topics`

| 参数        | 类型     | 必填 | 说明                   |
|-----------|--------|----|----------------------|
| boardCode | string | 否  | 板块编码，过滤指定板块；不传则全部    |
| sort      | string | 否  | `latest`(默认) / `hot` |
| keyword   | string | 否  | 标题/内容模糊搜索            |
| pageNum   | int    | 否  | 默认 1                 |
| pageSize  | int    | 否  | 默认 10                |

响应 `data`：

```json
{
  "list": [
    {
      "id": 329905409110446081,
      "boardId": 329905409110446082,
      "boardCode": "tech",
      "boardName": "技术交流",
      "title": "Vue3 Composition API 最佳实践",
      "content": "...",
      "author": "匿名#D45",
      "avatar": "🦊",
      "color": "#3B82F6",
      "views": 340,
      "comments": 23,
      "likes": 12,
      "heat": 890.0,
      "createTime": "2026-06-30 14:30:00"
    }
  ],
  "total": 128,
  "pageNum": 1,
  "pageSize": 10
}
```

### 2.2 热门话题榜

`GET /forum/hot?limit=10`

响应 `data`：数组，元素 `{ id, title, author, heat, comments, views, boardCode }`。

### 2.3 话题详情

`GET /forum/topic/{id}`

- 返回完整 `PostPojo`（含 `boardName`），并**自增浏览量 +1**。
- 字段：
  `id, boardId, boardName, title, content, anonymousName(author), anonymousAvatar(avatar), anonymousColor(color), viewCount, replyCount, likeCount, favoriteCount, hotScore, isTop, isEssence, createdAt`。

### 2.4 发布话题

`POST /forum/topic/create`

请求体：

```json
{ "boardCode": "tech", "title": "标题", "content": "正文" }
```

逻辑：从 `UserContext` 取 `userId`；随机生成匿名昵称/头像/颜色；生成雪花 ID；初始计数 0、状态 1（正常）、`hotScore` 按 0；写入
`Post`，板块 `PostCount+1`。返回新建的 `PostPojo`。

## 三、评论相关

### 3.1 评论列表

`GET /forum/topic/{id}/comments?pageNum=1&pageSize=50`

响应 `data`：`{ list: CommentPojo[], total }`，按 `CreatedAt` 升序。CommentPojo 字段：
`id, postId, anonymousName(author), anonymousColor(color), content, likeCount, replyCount, createdAt`。

### 3.2 发表评论

`POST /forum/comment/create`

请求体：

```json
{ "postId": 329905409110446081, "content": "回复内容", "parentId": 0, "rootId": 0 }
```

逻辑：生成匿名身份 + 雪花 ID；写入 `Comment`；帖子 `ReplyCount+1`；返回 `CommentPojo`。

## 四、互动相关

### 4.1 点赞（切换）

`POST /forum/like`

请求体：`{ "targetType": 1, "targetId": 329905409110446081 }`（targetType：1 帖子 2 评论）

逻辑：查 `LikeRecord` 是否存在；存在则删除并计数 -1（取消赞），不存在则新增并计数 +1（点赞）。响应 `data`：
`{ liked: true, count: 13 }`。

### 4.2 收藏（切换）

`POST /forum/favorite`

请求体：`{ "postId": 329905409110446081 }`

逻辑：同点赞，操作 `FavoriteRecord` 与 `Post.FavoriteCount`。响应 `data`：`{ favorited: true, count: 5 }`。

### 4.3 举报

`POST /forum/report`

请求体：`{ "targetType": 1, "targetId": 329905409110446081, "reason": "垃圾广告", "description": "" }`

逻辑：写入 `Report`（状态 0 待处理），目标 `ReportCount+1`。响应 `data`：`{ id }`。

## 五、元数据与统计

### 5.1 板块导航

`GET /forum/boards`

响应 `data`：`BoardPojo[]`（`status=1` 启用，按 `SortOrder` 降序）。前端用于侧边栏与发帖分类下拉。

### 5.2 统计（Dashboard）

`GET /forum/stats`

响应 `data`：`{ onlineCount, activeTopics, totalTopics, totalUsers, totalComments }`。

### 5.3 在线用户

`GET /forum/online`

响应 `data`：`{ count, users: [{ id, nickname }] }`。（基于 Redis 在线集合，无数据时返回最近活跃用户占位。）

## 六、私信 / 通知

### 6.1 我的私信列表

`GET /forum/messages`

响应 `data`：`{ list: NotificationPojo[], unread }`。查询当前用户的 `Notification`，按时间倒序。

### 6.2 发送私信

`POST /forum/message/send`

请求体：`{ "toUserId": 329905409110446081, "content": "在吗？" }`

逻辑：向目标用户写入一条 `Notification`（type=1 私信，sourceUserId=当前用户）。响应 `data`：`{ id }`。

## 七、搜索

### 7.1 搜索话题

`GET /forum/search?keyword=&pageNum=1&pageSize=10`

响应 `data`：与 2.1 列表结构一致（`Title` 或 `ContentText` like 关键字）。

## 八、错误码

| code | 含义                                      |
|------|-----------------------------------------|
| 200  | 成功                                      |
| 500  | 业务异常（`msg` 为错误描述）                       |
| 401  | 未登录 / token 失效（由 JwtFilter 返回 HTTP 401） |

> 写操作若 `UserContext.getUserId()` 为 null，服务端返回 `R.fail("请先登录")`。
