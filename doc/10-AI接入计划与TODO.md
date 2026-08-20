# 10 · AI 接入计划与 TODO

> 参考方案：`doc/匿名聊天系统接入AI方案.html`
> 进度状态：✅ 已完成 / 🚧 进行中 / ⬜ 待办
> 核心思想：**AI 是系统里的一个新"聊天参与者"**。后端负责聊天业务，AI 只负责生成回复；密钥、限流、上下文管理全部收口在后端，前端不直接调用 AI。

## 一、现状盘点

| 已有能力 | 说明 | 与 AI 接入的关系 |
|---|---|---|
| `/ws/chat` 聊天室 | 多房间广播，**消息不落库**，仅内存广播 | AI 可作为"聊天参与者"进入房间，但需要消息持久化支撑 |
| `/ws/notify` 通知 | 按 userId 推送私信等 | AI 回复可通过通知通道推送给用户 |
| 私信 `/forum/message/send` | 已落库 `Notification` | AI 会话可复用该表做私聊式对话 |
| 关注 `/chat/follow*` | **仅存 Redis，未持久化**（优化点） | 与 AI 无关，但属于必须修的持久化问题 |
| 权限体系 | `User.Role` + `AdminInterceptor` | AI 角色创建权限点 `ai:agent:*` 待扩展 |

## 二、目标架构

```
前端 Vue（打字机效果）
  └─ HTTP / SSE
Spring Boot（本服务）
  ├─ AiChatController   /api/ai/chat/*      # 会话管理 + 发送消息
  ├─ AiAgentController  /api/ai/agent/*     # AI 角色管理
  ├─ AiService          # 业务编排：上下文 + 调模型 + 落库
  ├─ AiConversationService  # 会话持久化（MySQL）
  ├─ AiChatMessageService   # 消息持久化（MySQL）
  ├─ AiAgentService         # 角色持久化（MySQL）
  └─ AiProvider         # 模型抽象层（OpenAI 兼容）
       ├─ DeepSeekProvider
       └─ ...（预留 OpenAI / Qwen）
```

**数据库新增 3 张表**（`ai_conversation` / `ai_chat_message` / `ai_agent`），详见 `migration_ai_chat.sql`。

## 三、分阶段任务

### Phase A · MVP 基础闭环（当前阶段）

- [x] 新增 3 张 AI 表 + 关注表 `user_follow` 迁移脚本
- [x] Entity / Mapper / Service / Controller 骨架
- [x] `AiProvider` 抽象接口 + `DeepSeekProvider`（OpenAI 兼容，一次性返回）
- [x] `AiChatController`：创建会话 / 发送消息 / 历史记录
- [x] `AiAgentController`：角色列表 / 创建 / 编辑 / 删除
- [x] 前端：AI 聊天页 `AiChat.vue` + 角色管理页 `AiAgentManage.vue`
- [x] `application.yml` 增加 `ai` 配置（api-key 走环境变量）

### Phase B · 上下文与体验（✅ 已完成）

- [x] 发送消息时组装最近 N 条历史（`System + History + 当前问题`）
- [x] Redis 热上下文 `ai:conversation:{conversationId}`（最近 50 条，Miss 回填 MySQL）
- [x] **流式输出（SSE）**：`POST /api/ai/chat/stream`，SseEmitter 逐段推送，前端 fetch 流式拼字
  - 本项目无 `spring-boot-starter-webflux`，采用 `HttpURLConnection` 逐行解析 `data:` 增量，无新增依赖
- [x] 消息过长截断（2000 字）、上下文只取最近 20 条
- [ ] 历史摘要：会话拉长后滚动生成 Conversation Summary（进阶项，暂缓）

### Phase C · AI 角色深化（✅ 已完成）

- [x] 角色自建 API Key（`CryptoService` AES/GCM 加密落库 `api_key_enc` + 接口脱敏 + 调用时解密）
- [x] 限流：用户维度 `ai:chat:rate` Redis 每分钟计数（20 次/分）
- [x] 角色聊天会话绑定：`ai_conversation.agent_id` 加载对应 systemPrompt 与自带 Key
- [ ] 角色 Key 健康状态（连续失败自动停用角色）—— 预留 `keyError` 标记，未做自动停用
- [ ] 权限点 `ai:agent:create/edit/delete/use` + 后台分配 —— 现有系统为 `User.Role` 简单角色体系，引入 RBAC 权限点属较大改造，暂缓

### Phase D · 聊天室与 AI 融合

- [x] 聊天室消息持久化：`/ws/chat` 聊天消息落库 `RoomMessage` 表 + `/chat/history` 历史接口 + 前端进入/切换房间加载历史
- [ ] AI 机器人进入房间：AI 作为 `senderType=AI` 参与者回复房间消息
- [ ] 私信式 AI 对话：复用 `Notification` 或独立会话

## 四、代码结构（已创建骨架）

```
svc/src/main/java/zzz/service/forum/
├── domain/
│   ├── AiConversationEntity.java
│   ├── AiChatMessageEntity.java
│   ├── AiAgentEntity.java
│   └── UserFollowEntity.java            # 关注持久化
├── domain/dto/
│   ├── AiCreateConversationRequest.java
│   ├── AiSendRequest.java
│   └── AiAgentRequest.java
├── mapper/  (+ resources/mapper/*.xml)
│   ├── AiConversationMapper.java
│   ├── AiChatMessageMapper.java
│   ├── AiAgentMapper.java
│   └── UserFollowMapper.java
├── service/
│   ├── AiConversationService.java  (+ impl)
│   ├── AiChatMessageService.java   (+ impl)
│   ├── AiAgentService.java         (+ impl)
│   └── AiService.java / AiServiceImpl.java
├── ai/
│   ├── AiProvider.java              # 模型抽象
│   ├── AiRequest.java / AiResponse.java / AiMessage.java
│   └── DeepSeekProvider.java        # OpenAI 兼容实现
└── controller/
    ├── AiChatController.java
    └── AiAgentController.java

web/src/
├── api/index.js                     # + AI 接口函数
├── views/AiChat.vue                 # AI 聊天页
├── views/AiAgentManage.vue          # 角色管理页
└── router/index.js                  # + 路由
```

## 五、其他优化点（查看页面发现）

| 优化点 | 现状 | 建议 |
|---|---|---|
| **关注/订阅未持久化** | `ChatController` 关注列表仅存 Redis `follow:{uid}`，重启/换 Redis 丢失；`followers` 用 `scanKeys` 全量扫 | 新增 `user_follow` 表，Redis 做读缓存，MySQL 持久化（本次已实现） |
| **聊天室消息不落库** | `/ws/chat` 消息仅内存广播，刷新即失 | 已落库 `RoomMessage` 表 + `/chat/history` 历史接口 + 前端加载（本次已实现） |
| **`followers` 扫描全量 key** | 用 `scanKeysWithPrefix` 遍历所有关注列表 | 改为 SQL 反向查询 `user_follow where follow_user_id = ?`（本次已实现） |
| **房间、在线人数仅内存** | `ChatRoomManager` 单机 ConcurrentHashMap，重启丢失 | 多实例需 Redis 发布订阅 + 房间状态入 Redis |
| **AI Key 安全** | （新增） | AES/GCM 加密落库 + 脱敏返回 + 调用时解密（本次已实现） |
| **限流缺失** | 发消息/关注无频控 | AI 消息已按用户 Redis 计数限流（20 次/分，本次已实现） |

## 变更记录

- 2026-08-20：参考 AI 方案文档，建立接入计划与 TODO；创建后端骨架（3 张 AI 表 + Entity/Mapper/Service/Controller/Provider）、前端 AI 聊天页与角色管理页；修复关注关系持久化（新增 `user_follow` 表，双写）。
- 2026-08-20：填充 TODO 代码——SSE 流式输出（`AiProvider.chatStream` + `SseEmitter` + 前端 fetch 流式拼字）、Redis 热上下文 `ai:conversation:{id}`、消息截断与用户维度限流、`CryptoService`(AES/GCM) 角色 Key 加解密、聊天室消息持久化（`RoomMessage` 表 + `/chat/history` + 前端加载历史）；`application.yml` 密钥走环境变量 `DEEPSEEK_API_KEY` / `AES_KEY`。完成集成自测。
- 2026-08-20：AI 接口路径按项目约定修正（后端 mapping 去 `/api` 前缀：`/ai/chat`、`/ai/agent`），修复前端经 vite 代理转发 `/ai/chat/stream` 404 问题。
- 2026-08-20：对话前校验 API Key——角色未填 Key 且系统 `DEEPSEEK_API_KEY` 未配置时，一次性返回与 SSE 流式均明确提示"未配置 API Key"，避免静默空回复。
