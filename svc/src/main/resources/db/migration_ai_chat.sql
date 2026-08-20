-- =====================================================
-- AI 聊天接入 + 关注持久化 迁移脚本
-- 参考：doc/10-AI接入计划与TODO.md
-- =====================================================

-- ----------------------------
-- Table structure for AiConversation  （AI 会话表）
-- ----------------------------
DROP TABLE IF EXISTS `AiConversation`;
CREATE TABLE `AiConversation`
(
    `Id`          bigint                                                 NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `UserId`      bigint                                                 NOT NULL COMMENT '发起用户ID',
    `AgentId`     bigint                                                 NULL DEFAULT NULL COMMENT '关联AI角色ID（可空=通用AI）',
    `Title`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL DEFAULT '' COMMENT '会话标题',
    `Status`      tinyint                                                NULL DEFAULT 1 COMMENT '状态 0关闭 1进行中',
    `CreatedAt`   datetime                                               NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `UpdatedAt`   datetime                                               NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`Id`) USING BTREE,
    INDEX `IndexUserId` (`UserId`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = 'AI会话表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for AiChatMessage  （AI 消息表，senderType=USER/AI/SYSTEM）
-- ----------------------------
DROP TABLE IF EXISTS `AiChatMessage`;
CREATE TABLE `AiChatMessage`
(
    `Id`             bigint                                                 NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `ConversationId` bigint                                                 NOT NULL COMMENT '会话ID',
    `SenderType`     varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NOT NULL COMMENT '发送者类型 USER/AI/SYSTEM',
    `SenderId`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL DEFAULT NULL COMMENT '发送者ID（用户ID或AI编码）',
    `Content`        text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin         NULL COMMENT '消息内容',
    `CreateTime`     datetime                                               NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`Id`) USING BTREE,
    INDEX `IndexConversationId` (`ConversationId`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = 'AI消息表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for AiAgent  （AI 角色表）
-- ----------------------------
DROP TABLE IF EXISTS `AiAgent`;
CREATE TABLE `AiAgent`
(
    `Id`            bigint                                                 NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `UserId`        bigint                                                 NULL DEFAULT NULL COMMENT '创建者ID（null=系统预置角色）',
    `Name`          varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NOT NULL COMMENT '角色名称',
    `Code`          varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL DEFAULT NULL COMMENT '角色编码',
    `Model`         varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL DEFAULT NULL COMMENT '模型标识（留空=系统默认）',
    `SystemPrompt`  text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin         NULL COMMENT '系统提示词',
    `Temperature`   decimal(3, 2)                                          NULL DEFAULT 0.70 COMMENT '采样温度',
    `MaxTokens`     int                                                    NULL DEFAULT 2048 COMMENT '最大输出token',
    `Enabled`       tinyint                                                NULL DEFAULT 1 COMMENT '是否启用 1启用 0停用',
    `IsSystem`      tinyint                                                NULL DEFAULT 0 COMMENT '1系统预置 0用户自建',
    `Avatar`        varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '头像URL或emoji',
    `Provider`      varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL DEFAULT NULL COMMENT '厂商标识 deepseek/openai/qwen（留空=系统默认）',
    `ApiBaseUrl`    varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '厂商base-url（留空=系统默认）',
    `ApiKeyEnc`     varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '用户自带API Key（AES加密）',
    `CreatedAt`     datetime                                               NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `UpdatedAt`     datetime                                               NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`Id`) USING BTREE,
    INDEX `IndexUserId` (`UserId`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = 'AI角色表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for RoomMessage  （聊天室消息持久化表）
-- ----------------------------
DROP TABLE IF EXISTS `RoomMessage`;
CREATE TABLE `RoomMessage`
(
    `Id`          bigint                                                 NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `RoomId`      int                                                    NOT NULL COMMENT '房间号',
    `UserId`      bigint                                                 NULL DEFAULT NULL COMMENT '发送用户ID（游客为空）',
    `Nickname`    varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL DEFAULT '' COMMENT '发送者昵称',
    `Content`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '消息内容',
    `CreateTime`  datetime                                               NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`Id`) USING BTREE,
    INDEX `IndexRoomId` (`RoomId`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '聊天室消息表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for UserFollow  （关注关系持久化表）
-- ----------------------------
DROP TABLE IF EXISTS `UserFollow`;
CREATE TABLE `UserFollow`
(
    `Id`             bigint                                                 NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `UserId`         bigint                                                 NOT NULL COMMENT '关注者ID',
    `FollowUserId`   bigint                                                 NOT NULL COMMENT '被关注者ID',
    `Nickname`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL DEFAULT '' COMMENT '被关注者昵称（冗余，便于列表展示）',
    `CreatedAt`      datetime                                               NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`Id`) USING BTREE,
    UNIQUE INDEX `UkUserFollow` (`UserId`, `FollowUserId`) USING BTREE,
    INDEX `IndexFollowUserId` (`FollowUserId`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '用户关注关系表'
  ROW_FORMAT = Dynamic;
