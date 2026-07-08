# 05 · 开发计划与 TODO

> 进度状态：✅ 已完成 / 🚧 进行中 / ⬜ 待办

## Phase 0 · 基础框架（✅ 已完成）

- ✅ Spring Boot 项目搭建、MyBatis + PageHelper 配置
- ✅ 数据库建表（`doc/atest.sql`，14 张表）
- ✅ 雪花 ID 生成器、CORS、Redis、Knife4j
- ✅ JWT 鉴权（access/refresh 双 token）+ `JwtFilter` + `UserContext`
- ✅ 登录/刷新/登出接口 `/api/auth/*`
- ✅ 账号密码登录 + 注册（替代 deviceId 隐式登录，`PasswordUtils` SHA-256+盐，`/api/auth/register`）

## Phase 1 · 通用 CRUD（✅ 已完成）

- ✅ Board / Post / Comment / LikeRecord / FavoriteRecord / Report / Notification / Tag / PostTag / ContentAudit /
  AnonymousIdentity / UserBehavior / UserDevice / UserToken 通用五接口

## Phase 2 · 论坛业务聚合接口（✅ 已完成）

- ✅ `ForumMapper` + XML：最新/热门/详情/按板块/搜索/评论/通知/统计聚合查询
- ✅ `ForumService` + impl：发帖匿名身份生成、计数维护、点赞/收藏切换、举报、私信、搜索
- ✅ `ForumController` (`/forum`)：话题、评论、互动、元数据、私信、搜索全量接口
- ✅ 接口文档 `doc/03-后端API接口文档.md`

## Phase 3 · 前端对接（✅ 已完成）

- ✅ `api/index.js` 论坛接口函数对齐
- ✅ Dashboard / TopicCategory / TopicDetail / HotTopics / Search / Messages 接入真实接口
- ✅ 字段映射、加载态、空态、发帖/评论提交
- ✅ 前端对接文档 `doc/04-前端对接文档.md`

## Phase 4 · 待办（⬜）

### 内容安全

- ⬜ 敏感词过滤：加载 `sensitive-words.txt`（DFA/Trie），发帖/评论拦截
- ⬜ `ContentAudit` 审核流程接入：机器审核 + 人工复核
- ⬜ 管理后台内容审核页（帖子审核 / 举报处理）

### 风控

- ⬜ Redis 滑动窗口限流：单 IP/用户 发帖、评论频率上限
- ⬜ `@RateLimit` 注解 + 拦截器
- ⬜ IP/设备黑名单（`UserBehavior` / `UserDevice` 埋点联动）

### 性能与缓存

- ⬜ 板块帖子缓存 `board:posts:{boardId}`（ZSET 热度）
- ⬜ 帖子详情缓存 `post:detail:{id}`（5min）
- ⬜ 热度分 `HotScore` 定时计算任务（`@Scheduled`）

### 功能补全

- ⬜ 楼中楼评论（`RootId` / `ParentId` 嵌套回复渲染）
- ⬜ 图片上传（本地 `uploads/` → OSS）
- ⬜ 个人中心 `Profile.vue`：我的发帖 / 收藏 / 浏览历史（基于 `UserBehavior`）
- ⬜ 实时聊天 `ChatRoom.vue`（WebSocket，目前 mock）

### 端侧

- ⬜ Flutter 移动端（首页 / 板块 / 详情 / 发帖 / 回复）

### 工程化

- ⬜ 后端单元测试 / 接口测试
- ⬜ Docker + Nginx 部署脚本
- ⬜ CI/CD

## 变更记录

- 2026-06-30：新增 `doc/` 文档；新增 `ForumController`/`ForumService`/`ForumMapper` 论坛业务接口；前端核心视图由 mock
  改为真实接口对接。
- 2026-06-30：登录改造为账号密码。`User` 表新增 `Username`/`PasswordHash`（迁移脚本 `migration-account-password.sql`）；新增
  `PasswordUtils`、`RegisterRequest`、`/api/auth/register`；`LoginRequest` 改为 `username/password`；`AuthController` 改用
  `R` 返回以便前端正确展示错误；前端 `Login.vue` 改为账号密码表单 + 登录/注册切换。
- 2026-06-30：修复通用 CRUD 分页默认排序报错。各控制器默认 `orderBy` 误写为 `*.CreateDate`（实际列名 `CreatedAt`），改为
  `*.CreatedAt`；`Tag`/`PostTag` 无日期列，改用 `Id`。同步修正 12 张表 Mapper XML 的 DateRange 子句 `CreateDate`→
  `CreatedAt`。
- 2026-06-30：新增角色权限与管理后台。`User` 表加 `Role`（迁移脚本 `migration-role.sql`，0普通1管理员）；`LoginResponse` 返回
  role；新增 `AdminInterceptor` 拦截 `/user/**` 校验管理员（403）；新增 `UserController` 用户管理（分页/新增/删除/重置密码/封禁解封/编辑）；前端路由
  `requiresAdmin` 守卫 + 侧边栏「管理」分组仅管理员可见 + 新增 `UserManage.vue`；板块管理移入管理分组。详见
  `08-权限管理文档.md`。
