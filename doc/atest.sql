/*
 Navicat Premium Dump SQL

 Source Server         : 240
 Source Server Type    : MySQL
 Source Server Version : 80100 (8.1.0)
 Source Host           : 192.168.99.240:53308
 Source Schema         : atest

 Target Server Type    : MySQL
 Target Server Version : 80100 (8.1.0)
 File Encoding         : 65001

 Date: 30/06/2026 16:58:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for AnonymousIdentity
-- ----------------------------
DROP TABLE IF EXISTS `AnonymousIdentity`;
CREATE TABLE `AnonymousIdentity`
(
    `Id`              bigint                                                 NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `UserId`          bigint                                                 NOT NULL COMMENT '真实用户ID',
    `PostId`          bigint                                                 NOT NULL COMMENT '帖子ID（匿名作用域）',
    `AnonymousName`   varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NOT NULL COMMENT '匿名昵称（如：吃瓜猫）',
    `AnonymousAvatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '匿名头像',
    `AnonymousColor`  varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL DEFAULT '' COMMENT '匿名颜色（UI标识）',
    `CreatedAt`       datetime                                               NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`Id`) USING BTREE,
    UNIQUE INDEX `UkUserPost` (`UserId` ASC, `PostId` ASC) USING BTREE,
    INDEX `IndexPostId` (`PostId` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '匿名身份映射表（同帖匿名一致，不同帖匿名不同）'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of AnonymousIdentity
-- ----------------------------

-- ----------------------------
-- Table structure for Board
-- ----------------------------
DROP TABLE IF EXISTS `Board`;
CREATE TABLE `Board`
(
    `Id`          bigint                                                 NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `Name`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NOT NULL COMMENT '板块名称',
    `Code`        varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NOT NULL COMMENT '板块编码（唯一标识）',
    `Icon`        varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '板块图标URL',
    `Description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '板块简介',
    `SortOrder`   int                                                    NULL DEFAULT 0 COMMENT '排序值（越大越靠前）',
    `PostCount`   int                                                    NULL DEFAULT 0 COMMENT '帖子数量',
    `FollowCount` int                                                    NULL DEFAULT 0 COMMENT '关注人数',
    `Status`      tinyint                                                NULL DEFAULT 1 COMMENT '状态：1正常 0隐藏',
    `CreatedAt`   datetime                                               NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `UpdatedAt`   datetime                                               NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`Id`) USING BTREE,
    UNIQUE INDEX `UkCode` (`Code` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 329905409110446106
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '社区板块表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of Board
-- ----------------------------
INSERT INTO `Board`
VALUES (329905409110446101, '随便聊聊', 'random', '💬', '无主题闲聊，畅所欲言', 50, 0, 0, 1, '2026-06-30 07:45:16',
        '2026-06-30 07:45:16');
INSERT INTO `Board`
VALUES (329905409110446102, '技术交流', 'tech', '💻', '分享技术心得与经验', 40, 0, 0, 1, '2026-06-30 07:45:16',
        '2026-06-30 07:45:16');
INSERT INTO `Board`
VALUES (329905409110446103, '游戏讨论', 'game', '🎮', '游戏资讯与交流', 30, 0, 0, 1, '2026-06-30 07:45:16',
        '2026-06-30 07:45:16');
INSERT INTO `Board`
VALUES (329905409110446104, '生活分享', 'life', '🌿', '分享生活中的点滴', 20, 0, 0, 1, '2026-06-30 07:45:16',
        '2026-06-30 07:45:16');
INSERT INTO `Board`
VALUES (329905409110446105, '求助问答', 'help', '🆘', '提出问题，寻求帮助', 10, 0, 0, 1, '2026-06-30 07:45:16',
        '2026-06-30 07:45:16');

-- ----------------------------
-- Table structure for Comment
-- ----------------------------
DROP TABLE IF EXISTS `Comment`;
CREATE TABLE `Comment`
(
    `Id`                  bigint                                                NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `PostId`              bigint                                                NOT NULL COMMENT '帖子ID',
    `UserId`              bigint                                                NOT NULL COMMENT '真实用户ID',
    `RootId`              bigint                                                NULL DEFAULT 0 COMMENT '根评论ID（楼中楼结构）',
    `ParentId`            bigint                                                NULL DEFAULT 0 COMMENT '父评论ID',
    `ReplyUserId`         bigint                                                NULL DEFAULT 0 COMMENT '被回复用户ID',
    `AnonymousIdentityId` bigint                                                NULL DEFAULT NULL COMMENT '匿名身份ID',
    `AnonymousName`       varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '匿名昵称',
    `Content`             text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin        NOT NULL COMMENT '评论内容',
    `LikeCount`           bigint                                                NULL DEFAULT 0 COMMENT '点赞数',
    `ReplyCount`          bigint                                                NULL DEFAULT 0 COMMENT '回复数',
    `Status`              tinyint                                               NULL DEFAULT 1 COMMENT '状态：1正常 2删除 3屏蔽',
    `CreatedAt`           datetime                                              NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`Id`) USING BTREE,
    INDEX `IndexPostId` (`PostId` ASC) USING BTREE,
    INDEX `IndexRootId` (`RootId` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '评论表（支持楼中楼结构）'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of Comment
-- ----------------------------

-- ----------------------------
-- Table structure for ContentAudit
-- ----------------------------
DROP TABLE IF EXISTS `ContentAudit`;
CREATE TABLE `ContentAudit`
(
    `Id`         bigint                                                 NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `TargetType` tinyint                                                NOT NULL COMMENT '目标类型',
    `TargetId`   bigint                                                 NOT NULL COMMENT '目标ID',
    `AuditType`  tinyint                                                NOT NULL COMMENT '审核类型',
    `RiskLevel`  tinyint                                                NULL DEFAULT 0 COMMENT '风险等级',
    `Result`     tinyint                                                NULL DEFAULT 0 COMMENT '审核结果：0待审 1通过 2拒绝',
    `HitWords`   json                                                   NULL COMMENT '命中敏感词',
    `Remark`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '备注',
    `CreatedAt`  datetime                                               NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`Id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '内容审核表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ContentAudit
-- ----------------------------

-- ----------------------------
-- Table structure for Dm_ToolTrack
-- ----------------------------
DROP TABLE IF EXISTS `Dm_ToolTrack`;
CREATE TABLE `Dm_ToolTrack`
(
    `id`           varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT 'id',
    `TrackNo`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '跟踪单号',
    `TrackDate`    datetime                                                NOT NULL COMMENT '跟踪日期',
    `TrackType`    varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '跟踪类型(分发,回收,维护,盘点)',
    `SceneType`    varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '场景类型(工厂,学校)',
    `BatchNo`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NULL     DEFAULT NULL COMMENT '批次号(便于批量操作管理)',
    `StartTime`    datetime                                                NULL     DEFAULT NULL COMMENT '操作开始时间',
    `EndTime`      datetime                                                NULL     DEFAULT NULL COMMENT '操作结束时间',
    `OrgUnitid`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '小组id',
    `OrgUnitName`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NOT NULL COMMENT '小组名称',
    `Operatorid`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '操作员id',
    `OperatorName` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '操作员姓名',
    `TotalCount`   int                                                     NOT NULL DEFAULT 0 COMMENT '计划数量',
    `ActualCount`  int                                                     NOT NULL DEFAULT 0 COMMENT '实际数量',
    `Status`       varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '单据状态(待处理,处理中,已完成,已取消)',
    `Priority`     int                                                     NOT NULL DEFAULT 1 COMMENT '优先级(1:普通,2:重要,3:紧急)',
    `Summary`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '摘要说明',
    `RowNum`       int                                                     NOT NULL DEFAULT 0 COMMENT '行号',
    `Remark`       varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '备注',
    `CreateBy`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '创建人',
    `CreateByid`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '创建人id',
    `CreateDate`   datetime                                                NOT NULL COMMENT '创建时间',
    `Lister`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '制表人',
    `Listerid`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '制表人id',
    `ModifyDate`   datetime                                                NOT NULL COMMENT '修改时间',
    `Custom1`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段1',
    `Custom2`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段2',
    `Custom3`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段3',
    `Custom4`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段4',
    `Custom5`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段5',
    `Custom6`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段6',
    `Custom7`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段7',
    `Custom8`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段8',
    `Custom9`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段9',
    `Custom10`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段10',
    `Tenantid`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '租户id',
    `TenantName`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '租户名称',
    `Revision`     int                                                     NOT NULL COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '工具跟踪主表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of Dm_ToolTrack
-- ----------------------------

-- ----------------------------
-- Table structure for Dm_ToolTrackAsset
-- ----------------------------
DROP TABLE IF EXISTS `Dm_ToolTrackAsset`;
CREATE TABLE `Dm_ToolTrackAsset`
(
    `id`            varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT 'id',
    `AssetCode`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '资产编码',
    `AssetName`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NOT NULL COMMENT '资产名称',
    `Categoryid`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '分类id',
    `CategoryName`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NOT NULL COMMENT '分类名称',
    `QrCode`        varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NOT NULL COMMENT '二维码内容',
    `QrCodeUrl`     varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '二维码图片URL',
    `SceneType`     varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '场景类型(工厂,学校)',
    `OrgUnitid`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NULL     DEFAULT NULL COMMENT '当前归属小组id(可空:在库/公共)',
    `OrgUnitName`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '当前归属小组名称',
    `Holderid`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NULL     DEFAULT NULL COMMENT '当前持有人id',
    `HolderName`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NULL     DEFAULT NULL COMMENT '当前持有人姓名',
    `Status`        varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '资产状态(可用/已分配/丢失/损坏/维护中/已退役)',
    `LastDistDate`  datetime                                                NULL     DEFAULT NULL COMMENT '最后分发时间',
    `LastCollDate`  datetime                                                NULL     DEFAULT NULL COMMENT '最后回收时间',
    `LastMainDate`  datetime                                                NULL     DEFAULT NULL COMMENT '最后维护时间',
    `NextMainDate`  datetime                                                NULL     DEFAULT NULL COMMENT '下次维护时间',
    `UsageCount`    int                                                     NOT NULL DEFAULT 0 COMMENT '使用次数',
    `Specification` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '规格说明',
    `PurchaseDate`  datetime                                                NULL     DEFAULT NULL COMMENT '采购日期',
    `PurchasePrice` decimal(18, 2)                                          NULL     DEFAULT NULL COMMENT '采购价格',
    `WarrantyDate`  datetime                                                NULL     DEFAULT NULL COMMENT '保修到期日期',
    `ImageUrl`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '图片URL',
    `EnabledMark`   int                                                     NOT NULL DEFAULT 1 COMMENT '有效标记',
    `RowNum`        int                                                     NOT NULL DEFAULT 0 COMMENT '行号序列',
    `Remark`        varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '备注',
    `CreateBy`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '创建人',
    `CreateByid`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '创建人id',
    `CreateDate`    datetime                                                NOT NULL COMMENT '创建时间',
    `Lister`        varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '制表人',
    `Listerid`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '制表人id',
    `ModifyDate`    datetime                                                NOT NULL COMMENT '修改时间',
    `Custom1`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段1',
    `Custom2`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段2',
    `Custom3`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段3',
    `Custom4`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段4',
    `Custom5`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段5',
    `Custom6`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段6',
    `Custom7`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段7',
    `Custom8`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段8',
    `Custom9`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段9',
    `Custom10`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段10',
    `Tenantid`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '租户id',
    `TenantName`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '租户名称',
    `Revision`      int                                                     NOT NULL COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '工具资产表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of Dm_ToolTrackAsset
-- ----------------------------

-- ----------------------------
-- Table structure for Dm_ToolTrackCategory
-- ----------------------------
DROP TABLE IF EXISTS `Dm_ToolTrackCategory`;
CREATE TABLE `Dm_ToolTrackCategory`
(
    `id`           varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT 'id',
    `Parentid`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '父级分类id',
    `Ancestors`    varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '祖先分类链',
    `CategoryCode` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '分类编码',
    `CategoryName` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NOT NULL COMMENT '分类名称',
    `SceneType`    varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '场景类型(工厂,学校)',
    `EnabledMark`  int                                                     NOT NULL DEFAULT 1 COMMENT '有效标记',
    `RowNum`       int                                                     NOT NULL DEFAULT 0 COMMENT '行号序列',
    `Remark`       varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '备注',
    `CreateBy`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '创建人',
    `CreateByid`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '创建人id',
    `CreateDate`   datetime                                                NOT NULL COMMENT '创建时间',
    `Lister`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '制表人',
    `Listerid`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '制表人id',
    `ModifyDate`   datetime                                                NOT NULL COMMENT '修改时间',
    `Custom1`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段1',
    `Custom2`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段2',
    `Custom3`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段3',
    `Custom4`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段4',
    `Custom5`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段5',
    `Custom6`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段6',
    `Custom7`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段7',
    `Custom8`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段8',
    `Custom9`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段9',
    `Custom10`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段10',
    `Tenantid`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '租户id',
    `TenantName`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '租户名称',
    `Revision`     int                                                     NOT NULL COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '工具分类表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of Dm_ToolTrackCategory
-- ----------------------------

-- ----------------------------
-- Table structure for Dm_ToolTrackGroup
-- ----------------------------
DROP TABLE IF EXISTS `Dm_ToolTrackGroup`;
CREATE TABLE `Dm_ToolTrackGroup`
(
    `id`           varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT 'id',
    `Parentid`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '父级小组id',
    `Ancestors`    varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '祖先小组链(用,分隔)',
    `OrgUnitCode`  varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '小组编码',
    `OrgUnitName`  varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NOT NULL COMMENT '小组名称',
    `OrgUnitType`  varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '小组类型(生产线,班级,车间,年级)',
    `SceneType`    varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '场景类型(工厂,学校)',
    `MaxCapacity`  int                                                     NOT NULL DEFAULT 0 COMMENT '最大容量(成员数上限)',
    `CurrentCount` int                                                     NOT NULL DEFAULT 0 COMMENT '当前成员数量',
    `Leader`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NULL     DEFAULT NULL COMMENT '负责人姓名',
    `LeaderPhone`  varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NULL     DEFAULT NULL COMMENT '负责人电话',
    `Location`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '位置',
    `WorkSchedule` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '工作/课程时间安排',
    `EnabledMark`  int                                                     NOT NULL DEFAULT 1 COMMENT '有效标记',
    `RowNum`       int                                                     NOT NULL DEFAULT 0 COMMENT '行号序列',
    `Remark`       varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '备注',
    `CreateBy`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '创建人',
    `CreateByid`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '创建人id',
    `CreateDate`   datetime                                                NOT NULL COMMENT '创建时间',
    `Lister`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '制表人',
    `Listerid`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '制表人id',
    `ModifyDate`   datetime                                                NOT NULL COMMENT '修改时间',
    `Custom1`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段1',
    `Custom2`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段2',
    `Custom3`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段3',
    `Custom4`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段4',
    `Custom5`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段5',
    `Custom6`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段6',
    `Custom7`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段7',
    `Custom8`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段8',
    `Custom9`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段9',
    `Custom10`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段10',
    `Tenantid`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '租户id',
    `TenantName`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '租户名称',
    `Revision`     int                                                     NOT NULL COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '小组表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of Dm_ToolTrackGroup
-- ----------------------------

-- ----------------------------
-- Table structure for Dm_ToolTrackItem
-- ----------------------------
DROP TABLE IF EXISTS `Dm_ToolTrackItem`;
CREATE TABLE `Dm_ToolTrackItem`
(
    `id`            varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT 'id',
    `Pid`           varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT 'Pid',
    `Assetid`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '资产id',
    `AssetCode`     varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '资产编码',
    `FromHolderid`  varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NULL     DEFAULT NULL COMMENT '来源持有人id(回收/转移用)',
    `FromOrgUnitid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NULL     DEFAULT NULL COMMENT '来源小组id',
    `ToHolderid`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NULL     DEFAULT NULL COMMENT '去向持有人id(分发/转移用)',
    `ToOrgUnitid`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NULL     DEFAULT NULL COMMENT '去向小组id',
    `FlowTime`      datetime                                                NOT NULL COMMENT '业务发生时间(扫码时间)',
    `Qty`           int                                                     NOT NULL DEFAULT 1 COMMENT '数量',
    `ResultStatus`  varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '完成后资产状态',
    `RowNum`        int                                                     NOT NULL DEFAULT 0 COMMENT '行号',
    `Remark`        varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '备注',
    `Custom1`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段1',
    `Custom2`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段2',
    `Custom3`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段3',
    `Custom4`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段4',
    `Custom5`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段5',
    `Custom6`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段6',
    `Custom7`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段7',
    `Custom8`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段8',
    `Custom9`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段9',
    `Custom10`      varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段10',
    `Tenantid`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '租户id',
    `Revision`      int                                                     NOT NULL COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '工具跟踪流水表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of Dm_ToolTrackItem
-- ----------------------------

-- ----------------------------
-- Table structure for Dm_ToolTrackMember
-- ----------------------------
DROP TABLE IF EXISTS `Dm_ToolTrackMember`;
CREATE TABLE `Dm_ToolTrackMember`
(
    `id`          varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT 'id',
    `OrgUnitid`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '所属小组id',
    `MemberCode`  varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '成员编码',
    `MemberName`  varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '成员姓名',
    `MemberType`  varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '成员类型(员工,学生,教师)',
    `Phone`       varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NULL     DEFAULT NULL COMMENT '电话',
    `Email`       varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '邮箱',
    `Position`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NULL     DEFAULT NULL COMMENT '职位/年级',
    `SkillLevel`  varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NULL     DEFAULT NULL COMMENT '技能等级',
    `Status`      varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL DEFAULT 'ACTIVE' COMMENT '状态(ACTIVE在职,INACTIVE离职)',
    `JoinDate`    datetime                                                NULL     DEFAULT NULL COMMENT '加入日期',
    `Avatar`      varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '头像URL',
    `EnabledMark` int                                                     NOT NULL DEFAULT 1 COMMENT '有效标记',
    `RowNum`      int                                                     NOT NULL DEFAULT 0 COMMENT '行号序列',
    `Remark`      varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT NULL COMMENT '备注',
    `CreateBy`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '创建人',
    `CreateByid`  varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '创建人id',
    `CreateDate`  datetime                                                NOT NULL COMMENT '创建时间',
    `Lister`      varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '制表人',
    `Listerid`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '制表人id',
    `ModifyDate`  datetime                                                NOT NULL COMMENT '修改时间',
    `Custom1`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段1',
    `Custom2`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段2',
    `Custom3`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段3',
    `Custom4`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段4',
    `Custom5`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段5',
    `Custom6`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段6',
    `Custom7`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段7',
    `Custom8`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段8',
    `Custom9`     varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段9',
    `Custom10`    varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT NULL COMMENT '自定义字段10',
    `Tenantid`    varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '租户id',
    `TenantName`  varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '租户名称',
    `Revision`    int                                                     NOT NULL COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '成员表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of Dm_ToolTrackMember
-- ----------------------------

-- ----------------------------
-- Table structure for FavoriteRecord
-- ----------------------------
DROP TABLE IF EXISTS `FavoriteRecord`;
CREATE TABLE `FavoriteRecord`
(
    `Id`        bigint   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `UserId`    bigint   NOT NULL COMMENT '用户ID',
    `PostId`    bigint   NOT NULL COMMENT '帖子ID',
    `CreatedAt` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`Id`) USING BTREE,
    INDEX `IndexUserId` (`UserId` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '收藏记录表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of FavoriteRecord
-- ----------------------------

-- ----------------------------
-- Table structure for LikeRecord
-- ----------------------------
DROP TABLE IF EXISTS `LikeRecord`;
CREATE TABLE `LikeRecord`
(
    `Id`         bigint   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `UserId`     bigint   NOT NULL COMMENT '用户ID',
    `TargetType` tinyint  NOT NULL COMMENT '目标类型：1帖子 2评论',
    `TargetId`   bigint   NOT NULL COMMENT '目标ID',
    `CreatedAt`  datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`Id`) USING BTREE,
    UNIQUE INDEX `UkUserTarget` (`UserId` ASC, `TargetType` ASC, `TargetId` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '点赞记录表（防重复点赞）'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of LikeRecord
-- ----------------------------

-- ----------------------------
-- Table structure for Notification
-- ----------------------------
DROP TABLE IF EXISTS `Notification`;
CREATE TABLE `Notification`
(
    `Id`           bigint                                                 NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `UserId`       bigint                                                 NOT NULL COMMENT '接收用户ID',
    `Type`         tinyint                                                NOT NULL COMMENT '通知类型',
    `SourceUserId` bigint                                                 NULL DEFAULT 0 COMMENT '触发用户ID',
    `TargetId`     bigint                                                 NULL DEFAULT 0 COMMENT '关联目标ID',
    `Content`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '通知内容',
    `IsRead`       tinyint                                                NULL DEFAULT 0 COMMENT '是否已读',
    `CreatedAt`    datetime                                               NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`Id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '通知表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of Notification
-- ----------------------------

-- ----------------------------
-- Table structure for Post
-- ----------------------------
DROP TABLE IF EXISTS `Post`;
CREATE TABLE `Post`
(
    `Id`                  bigint                                                 NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `BoardId`             bigint                                                 NOT NULL COMMENT '板块ID',
    `UserId`              bigint                                                 NOT NULL COMMENT '真实用户ID',
    `AnonymousIdentityId` bigint                                                 NULL DEFAULT NULL COMMENT '匿名身份ID',
    `AnonymousName`       varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NOT NULL COMMENT '匿名昵称',
    `AnonymousAvatar`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '匿名头像',
    `AnonymousColor`      varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL DEFAULT '' COMMENT '匿名标识颜色',
    `Title`               varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '帖子标题',
    `Content`             mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin   NOT NULL COMMENT '帖子内容（富文本）',
    `ContentText`         text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin         NULL COMMENT '纯文本内容（用于搜索）',
    `Images`              json                                                   NULL COMMENT '图片列表（JSON数组）',
    `ViewCount`           bigint                                                 NULL DEFAULT 0 COMMENT '浏览量',
    `ReplyCount`          bigint                                                 NULL DEFAULT 0 COMMENT '评论数',
    `LikeCount`           bigint                                                 NULL DEFAULT 0 COMMENT '点赞数',
    `FavoriteCount`       bigint                                                 NULL DEFAULT 0 COMMENT '收藏数',
    `ShareCount`          bigint                                                 NULL DEFAULT 0 COMMENT '分享数',
    `ReportCount`         bigint                                                 NULL DEFAULT 0 COMMENT '举报数',
    `HotScore`            double                                                 NULL DEFAULT 0 COMMENT '热度分（用于推荐排序）',
    `IsTop`               tinyint                                                NULL DEFAULT 0 COMMENT '是否置顶',
    `IsEssence`           tinyint                                                NULL DEFAULT 0 COMMENT '是否精华',
    `Status`              tinyint                                                NULL DEFAULT 1 COMMENT '状态：1正常 2审核中 3删除',
    `IpHash`              varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL DEFAULT '' COMMENT 'IP哈希（风控）',
    `DeviceHash`          varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL DEFAULT '' COMMENT '设备哈希（风控）',
    `CreatedAt`           datetime                                               NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `UpdatedAt`           datetime                                               NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `Deleted`             tinyint                                                NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`Id`) USING BTREE,
    INDEX `IndexBoardId` (`BoardId` ASC) USING BTREE,
    INDEX `IndexHotScore` (`HotScore` ASC) USING BTREE,
    INDEX `IndexCreatedAt` (`CreatedAt` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '帖子表（社区核心内容）'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of Post
-- ----------------------------

-- ----------------------------
-- Table structure for PostTag
-- ----------------------------
DROP TABLE IF EXISTS `PostTag`;
CREATE TABLE `PostTag`
(
    `Id`     bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `PostId` bigint NOT NULL COMMENT '帖子ID',
    `TagId`  bigint NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`Id`) USING BTREE,
    INDEX `IndexPostId` (`PostId` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '帖子标签关联表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of PostTag
-- ----------------------------

-- ----------------------------
-- Table structure for Report
-- ----------------------------
DROP TABLE IF EXISTS `Report`;
CREATE TABLE `Report`
(
    `Id`           bigint                                                 NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `ReportUserId` bigint                                                 NOT NULL COMMENT '举报人ID',
    `TargetType`   tinyint                                                NOT NULL COMMENT '目标类型',
    `TargetId`     bigint                                                 NOT NULL COMMENT '目标ID',
    `Reason`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '举报原因',
    `Description`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '补充说明',
    `Status`       tinyint                                                NULL DEFAULT 0 COMMENT '处理状态：0未处理 1已处理',
    `CreatedAt`    datetime                                               NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`Id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '举报记录表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of Report
-- ----------------------------

-- ----------------------------
-- Table structure for Tag
-- ----------------------------
DROP TABLE IF EXISTS `Tag`;
CREATE TABLE `Tag`
(
    `Id`        bigint                                                NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `Name`      varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '标签名称',
    `PostCount` int                                                   NULL DEFAULT 0 COMMENT '关联帖子数量',
    PRIMARY KEY (`Id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '标签表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of Tag
-- ----------------------------

-- ----------------------------
-- Table structure for User
-- ----------------------------
DROP TABLE IF EXISTS `User`;
CREATE TABLE `User`
(
    `Id`           bigint                                                 NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `DeviceId`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NOT NULL COMMENT '设备唯一标识，用于匿名注册用户绑定',
    `Username`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT '' COMMENT '登录账号（唯一）',
    `PasswordHash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT '' COMMENT '密码哈希（salt:hash）',
    `Nickname`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT '' COMMENT '用户昵称（系统生成）',
    `Avatar`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL     DEFAULT '' COMMENT '用户头像URL',
    `Status`       tinyint                                                NULL     DEFAULT 1 COMMENT '账号状态：1正常 2封禁 3注销',
    `Role`         tinyint                                                NOT NULL DEFAULT 0 COMMENT '角色：0普通用户 1管理员',
    `LastLoginIp`  varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL     DEFAULT '' COMMENT '最后登录IP地址',
    `LastLoginAt`  datetime                                               NULL     DEFAULT NULL COMMENT '最后登录时间',
    `CreatedAt`    datetime                                               NULL     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `UpdatedAt`    datetime                                               NULL     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `Deleted`      tinyint                                                NULL     DEFAULT 0 COMMENT '逻辑删除标记：0未删除 1已删除',
    PRIMARY KEY (`Id`) USING BTREE,
    UNIQUE INDEX `UkDeviceId` (`DeviceId` ASC) USING BTREE,
    UNIQUE INDEX `UkUsername` (`Username` ASC) USING BTREE,
    INDEX `IndexRole` (`Role` ASC) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 347313076330172417
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '用户表（匿名系统真实身份主体）'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of User
-- ----------------------------
INSERT INTO `User`
VALUES (329905409110446080, '123', 'cao', 'j1op32Oh7H9e4lX/OCHl0A==:oAAz+6pu+jEOc2VpJx5HDpLqLD6crqEMkQuLMnRzSI4=',
        '熬夜企鹅46080', '', 2, 0, '0:0:0:0:0:0:0:1', '2026-06-30 16:54:41', '2026-05-13 06:59:29',
        '2026-06-30 08:54:40', 0);
INSERT INTO `User`
VALUES (347313076330172416, 'demo', 'demo', 'j1op32Oh7H9e4lX/OCHl0A==:oAAz+6pu+jEOc2VpJx5HDpLqLD6crqEMkQuLMnRzSI4=',
        'nick', '', 1, 1, '0:0:0:0:0:0:0:1', '2026-06-30 16:53:10', '2026-06-30 07:51:22', '2026-06-30 08:53:02', 0);

-- ----------------------------
-- Table structure for UserBehavior
-- ----------------------------
DROP TABLE IF EXISTS `UserBehavior`;
CREATE TABLE `UserBehavior`
(
    `Id`           bigint   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `UserId`       bigint   NOT NULL COMMENT '用户ID',
    `BehaviorType` tinyint  NOT NULL COMMENT '行为类型：1浏览 2点赞 3收藏 4评论',
    `TargetType`   tinyint  NOT NULL COMMENT '目标类型',
    `TargetId`     bigint   NOT NULL COMMENT '目标ID',
    `Duration`     int      NULL DEFAULT 0 COMMENT '停留时长（秒）',
    `CreatedAt`    datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`Id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '用户行为表（推荐系统基础）'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of UserBehavior
-- ----------------------------

-- ----------------------------
-- Table structure for UserDevice
-- ----------------------------
DROP TABLE IF EXISTS `UserDevice`;
CREATE TABLE `UserDevice`
(
    `Id`                bigint                                                 NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `UserId`            bigint                                                 NOT NULL COMMENT '用户ID（关联User表）',
    `DeviceId`          varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NOT NULL COMMENT '设备唯一ID',
    `DeviceFingerprint` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '设备指纹（风控识别）',
    `Os`                varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL DEFAULT '' COMMENT '操作系统类型',
    `Browser`           varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL DEFAULT '' COMMENT '浏览器信息',
    `Ip`                varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL DEFAULT '' COMMENT '登录IP地址',
    `UserAgent`         varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT 'UA信息',
    `CreatedAt`         datetime                                               NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`Id`) USING BTREE,
    INDEX `IndexUserId` (`UserId` ASC) USING BTREE,
    INDEX `IndexDeviceId` (`DeviceId` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '用户设备记录表（风控与设备追踪）'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of UserDevice
-- ----------------------------

-- ----------------------------
-- Table structure for UserToken
-- ----------------------------
DROP TABLE IF EXISTS `UserToken`;
CREATE TABLE `UserToken`
(
    `Id`           bigint                                                 NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `UserId`       bigint                                                 NOT NULL COMMENT '用户ID',
    `AccessToken`  varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT 'JWT访问令牌',
    `RefreshToken` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '刷新令牌（用于续期）',
    `ExpireAt`     datetime                                               NULL DEFAULT NULL COMMENT 'AccessToken过期时间',
    `DeviceId`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL DEFAULT '' COMMENT '设备ID',
    `Ip`           varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL DEFAULT '' COMMENT '登录IP',
    `Status`       tinyint                                                NULL DEFAULT 1 COMMENT '状态：1有效 0失效',
    `CreatedAt`    datetime                                               NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`Id`) USING BTREE,
    UNIQUE INDEX `UkAccessToken` (`AccessToken` ASC) USING BTREE,
    INDEX `IndexUserId` (`UserId` ASC) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin COMMENT = '用户Token表（JWT会话管理）'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of UserToken
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
