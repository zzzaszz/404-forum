# 404-Forum 匿名社区 · 文档总索引

> 本目录为项目文档目录（合并自原 `doc/` 早期分析草稿与原 `docs/` 正式文档）。

## 项目简介

一个支持多板块分类的匿名发帖 / 回复社区平台。用户登录后可自由选择板块发布匿名帖子，其他用户可对帖子进行匿名回复、点赞、收藏、举报。包含
Web 端（Vue 3 + Element Plus）与后端服务（Spring Boot + MyBatis + MySQL + Redis）。

## 目录结构

```
doc/
├── README.md                       # 本文件（文档索引）
├── 01-架构与项目结构.md              # 技术栈、目录结构、模块划分
├── 02-数据库设计.md                  # 全部建表方案（与 atest.sql 对齐）
├── 03-后端API接口文档.md             # ForumController 等接口契约（前后端对接依据）
├── 04-前端对接文档.md                # 前端 API 封装、视图与接口对应关系
├── 05-开发计划与TODO.md              # 已完成 / 待办事项清单
├── 06-登录注册文档.md                # 账号密码登录/注册设计（含迁移脚本）
├── 07-图片资源需求清单.md            # 所需图片：尺寸/格式/命名/存放路径
├── 08-权限管理文档.md                # 角色区分、管理员拦截、用户管理
├── 09-FileServiceImpl-MinIO上传实现分析.md  # MinIO 文件上传/分片/代理/鉴权分析
├── seed.sql                         # 默认板块种子数据
├── migration-account-password.sql   # User 表账号密码字段迁移
├── migration-role.sql               # User 表角色字段迁移
├── atest.sql                        # 建表脚本（14 张表）
├── 开发文档.md                       # 早期开发设计总览
├── DB相关.md                         # 早期建表分析
├── 登录相关.md                       # 早期登录设计分析
├── 匿名社区后端相关分析.md            # 早期后端技术分析
└── index.html                       # 早期前端 Demo
```

## 阅读顺序

1. 新人入门 → `01-架构与项目结构.md`
2. 建库 → `02-数据库设计.md`（执行 `doc/atest.sql` → `migration-account-password.sql` → `seed.sql`）
3. 前后端联调 → `03-后端API接口文档.md` + `04-前端对接文档.md`
4. 登录注册 → `06-登录注册文档.md`
5. 权限管理 → `08-权限管理文档.md`
6. 推进任务 → `05-开发计划与TODO.md`

## 当前阶段

- ✅ 基础框架搭建（登录鉴权、通用 CRUD、雪花 ID、JWT 过滤器、Redis）
- ✅ 各实体通用 CRUD（Board / Post / Comment / Like / Favorite / Report / Notification 等）
- ✅ 论坛业务聚合接口 `ForumController`（最新话题、热门、详情、发帖、评论、点赞、收藏、举报、统计、私信、搜索）
- ✅ 账号密码登录 / 注册（替代原 deviceId 隐式登录，密码 SHA-256+盐哈希存储）
- ✅ 角色权限（普通用户/管理员），管理员可见板块管理 + 用户管理（增删改、重置密码、封禁/解封）
- ✅ 前端核心视图对接真实后端接口（Dashboard / 板块 / 详情 / 热门 / 搜索 / 私信）
- ⬜ 敏感词过滤、限流防刷、内容审核后台、Flutter 端（见 TODO）

## 约定

- 后端统一返回体：`R<T> { code, msg, data }`，成功 `code=200`。
- 路由前缀：鉴权 `/api/auth`，论坛业务 `/forum`，通用 CRUD `/{entity}`。
- 前端开发环境通过 Vite 代理 `/api → http://localhost:10610`（去掉 `/api` 前缀）。
- 匿名身份由后端在发帖 / 评论时生成（随机昵称 + 颜色 + emoji 头像）。
