# ✅ 一、用户与认证体系

------

## User（用户表）

```
CREATE TABLE User (
  Id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  DeviceId VARCHAR(64) NOT NULL COMMENT '设备唯一标识，用于匿名注册用户绑定',
  Nickname VARCHAR(64) DEFAULT '' COMMENT '用户昵称（系统生成）',
  Avatar VARCHAR(255) DEFAULT '' COMMENT '用户头像URL',

  Status TINYINT DEFAULT 1 COMMENT '账号状态：1正常 2封禁 3注销',

  LastLoginIp VARCHAR(64) DEFAULT '' COMMENT '最后登录IP地址',
  LastLoginAt DATETIME DEFAULT NULL COMMENT '最后登录时间',

  CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UpdatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  Deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记：0未删除 1已删除',

  PRIMARY KEY (Id),
  UNIQUE KEY UkDeviceId (DeviceId)
) COMMENT='用户表（匿名系统真实身份主体）';
```

------

## UserDevice（设备表）

```
CREATE TABLE UserDevice (
  Id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  UserId BIGINT NOT NULL COMMENT '用户ID（关联User表）',
  DeviceId VARCHAR(64) NOT NULL COMMENT '设备唯一ID',
  DeviceFingerprint VARCHAR(128) DEFAULT '' COMMENT '设备指纹（风控识别）',

  Os VARCHAR(32) DEFAULT '' COMMENT '操作系统类型',
  Browser VARCHAR(32) DEFAULT '' COMMENT '浏览器信息',
  Ip VARCHAR(64) DEFAULT '' COMMENT '登录IP地址',
  UserAgent VARCHAR(512) DEFAULT '' COMMENT 'UA信息',

  CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  PRIMARY KEY (Id),
  KEY IndexUserId (UserId),
  KEY IndexDeviceId (DeviceId)
) COMMENT='用户设备记录表（风控与设备追踪）';
```

------

## UserToken（JWT/RefreshToken）

```
CREATE TABLE UserToken (
  Id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  UserId BIGINT NOT NULL COMMENT '用户ID',
  AccessToken VARCHAR(512) NOT NULL COMMENT 'JWT访问令牌',
  RefreshToken VARCHAR(512) DEFAULT '' COMMENT '刷新令牌（用于续期）',

  ExpireAt DATETIME DEFAULT NULL COMMENT 'AccessToken过期时间',

  DeviceId VARCHAR(64) DEFAULT '' COMMENT '设备ID',
  Ip VARCHAR(64) DEFAULT '' COMMENT '登录IP',

  Status TINYINT DEFAULT 1 COMMENT '状态：1有效 0失效',

  CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  PRIMARY KEY (Id),
  KEY IndexUserId (UserId),
  UNIQUE KEY UkAccessToken (AccessToken)
) COMMENT='用户Token表（JWT会话管理）';
```

------

# ✅ 二、匿名体系（核心）

------

## AnonymousIdentity（匿名身份映射）

```
CREATE TABLE AnonymousIdentity (
  Id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  UserId BIGINT NOT NULL COMMENT '真实用户ID',
  PostId BIGINT NOT NULL COMMENT '帖子ID（匿名作用域）',

  AnonymousName VARCHAR(32) NOT NULL COMMENT '匿名昵称（如：吃瓜猫）',
  AnonymousAvatar VARCHAR(255) DEFAULT '' COMMENT '匿名头像',
  AnonymousColor VARCHAR(16) DEFAULT '' COMMENT '匿名颜色（UI标识）',

  CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  PRIMARY KEY (Id),
  UNIQUE KEY UkUserPost (UserId, PostId),
  KEY IndexPostId (PostId)
) COMMENT='匿名身份映射表（同帖匿名一致，不同帖匿名不同）';
```

------

# ✅ 三、社区内容体系

------

## Board（板块）

```
CREATE TABLE Board (
  Id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  Name VARCHAR(64) NOT NULL COMMENT '板块名称',
  Code VARCHAR(32) NOT NULL COMMENT '板块编码（唯一标识）',
  Icon VARCHAR(255) DEFAULT '' COMMENT '板块图标URL',
  Description VARCHAR(255) DEFAULT '' COMMENT '板块简介',

  SortOrder INT DEFAULT 0 COMMENT '排序值（越大越靠前）',

  PostCount INT DEFAULT 0 COMMENT '帖子数量',
  FollowCount INT DEFAULT 0 COMMENT '关注人数',

  Status TINYINT DEFAULT 1 COMMENT '状态：1正常 0隐藏',

  CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UpdatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  PRIMARY KEY (Id),
  UNIQUE KEY UkCode (Code)
) COMMENT='社区板块表';
```

------

## Post（帖子）

```
CREATE TABLE Post (
  Id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  BoardId BIGINT NOT NULL COMMENT '板块ID',
  UserId BIGINT NOT NULL COMMENT '真实用户ID',
  AnonymousIdentityId BIGINT DEFAULT NULL COMMENT '匿名身份ID',

  AnonymousName VARCHAR(32) NOT NULL COMMENT '匿名昵称',
  AnonymousAvatar VARCHAR(255) DEFAULT '' COMMENT '匿名头像',
  AnonymousColor VARCHAR(16) DEFAULT '' COMMENT '匿名标识颜色',

  Title VARCHAR(255) DEFAULT '' COMMENT '帖子标题',
  Content MEDIUMTEXT NOT NULL COMMENT '帖子内容（富文本）',
  ContentText TEXT COMMENT '纯文本内容（用于搜索）',

  Images JSON DEFAULT NULL COMMENT '图片列表（JSON数组）',

  ViewCount BIGINT DEFAULT 0 COMMENT '浏览量',
  ReplyCount BIGINT DEFAULT 0 COMMENT '评论数',
  LikeCount BIGINT DEFAULT 0 COMMENT '点赞数',
  FavoriteCount BIGINT DEFAULT 0 COMMENT '收藏数',
  ShareCount BIGINT DEFAULT 0 COMMENT '分享数',
  ReportCount BIGINT DEFAULT 0 COMMENT '举报数',

  HotScore DOUBLE DEFAULT 0 COMMENT '热度分（用于推荐排序）',

  IsTop TINYINT DEFAULT 0 COMMENT '是否置顶',
  IsEssence TINYINT DEFAULT 0 COMMENT '是否精华',

  Status TINYINT DEFAULT 1 COMMENT '状态：1正常 2审核中 3删除',

  IpHash VARCHAR(64) DEFAULT '' COMMENT 'IP哈希（风控）',
  DeviceHash VARCHAR(64) DEFAULT '' COMMENT '设备哈希（风控）',

  CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UpdatedAt DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

  Deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',

  PRIMARY KEY (Id),
  KEY IndexBoardId (BoardId),
  KEY IndexHotScore (HotScore),
  KEY IndexCreatedAt (CreatedAt)
) COMMENT='帖子表（社区核心内容）';
```

------

## Comment（评论）

```
CREATE TABLE Comment (
  Id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  PostId BIGINT NOT NULL COMMENT '帖子ID',
  UserId BIGINT NOT NULL COMMENT '真实用户ID',

  RootId BIGINT DEFAULT 0 COMMENT '根评论ID（楼中楼结构）',
  ParentId BIGINT DEFAULT 0 COMMENT '父评论ID',
  ReplyUserId BIGINT DEFAULT 0 COMMENT '被回复用户ID',

  AnonymousIdentityId BIGINT DEFAULT NULL COMMENT '匿名身份ID',
  AnonymousName VARCHAR(32) NOT NULL COMMENT '匿名昵称',

  Content TEXT NOT NULL COMMENT '评论内容',

  LikeCount BIGINT DEFAULT 0 COMMENT '点赞数',
  ReplyCount BIGINT DEFAULT 0 COMMENT '回复数',

  Status TINYINT DEFAULT 1 COMMENT '状态：1正常 2删除 3屏蔽',

  CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  PRIMARY KEY (Id),
  KEY IndexPostId (PostId),
  KEY IndexRootId (RootId)
) COMMENT='评论表（支持楼中楼结构）';
```

------

# ✅ 四、互动体系

------

## LikeRecord（点赞记录）

```
CREATE TABLE LikeRecord (
  Id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  UserId BIGINT NOT NULL COMMENT '用户ID',
  TargetType TINYINT NOT NULL COMMENT '目标类型：1帖子 2评论',
  TargetId BIGINT NOT NULL COMMENT '目标ID',

  CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  PRIMARY KEY (Id),
  UNIQUE KEY UkUserTarget (UserId, TargetType, TargetId)
) COMMENT='点赞记录表（防重复点赞）';
```

------

## FavoriteRecord（收藏）

```
CREATE TABLE FavoriteRecord (
  Id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  UserId BIGINT NOT NULL COMMENT '用户ID',
  PostId BIGINT NOT NULL COMMENT '帖子ID',

  CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  PRIMARY KEY (Id),
  KEY IndexUserId (UserId)
) COMMENT='收藏记录表';
```

------

# ✅ 五、标签体系

------

## Tag

```
CREATE TABLE Tag (
  Id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  Name VARCHAR(64) NOT NULL COMMENT '标签名称',
  PostCount INT DEFAULT 0 COMMENT '关联帖子数量',

  PRIMARY KEY (Id)
) COMMENT='标签表';
```

------

## PostTag

```
CREATE TABLE PostTag (
  Id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  PostId BIGINT NOT NULL COMMENT '帖子ID',
  TagId BIGINT NOT NULL COMMENT '标签ID',

  PRIMARY KEY (Id),
  KEY IndexPostId (PostId)
) COMMENT='帖子标签关联表';
```

------

# ✅ 六、举报 & 通知

------

## Report

```
CREATE TABLE Report (
  Id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  ReportUserId BIGINT NOT NULL COMMENT '举报人ID',
  TargetType TINYINT NOT NULL COMMENT '目标类型',
  TargetId BIGINT NOT NULL COMMENT '目标ID',

  Reason VARCHAR(255) NOT NULL COMMENT '举报原因',
  Description VARCHAR(500) DEFAULT '' COMMENT '补充说明',

  Status TINYINT DEFAULT 0 COMMENT '处理状态：0未处理 1已处理',

  CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  PRIMARY KEY (Id)
) COMMENT='举报记录表';
```

------

## Notification

```
CREATE TABLE Notification (
  Id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  UserId BIGINT NOT NULL COMMENT '接收用户ID',
  Type TINYINT NOT NULL COMMENT '通知类型',

  SourceUserId BIGINT DEFAULT 0 COMMENT '触发用户ID',
  TargetId BIGINT DEFAULT 0 COMMENT '关联目标ID',

  Content VARCHAR(255) DEFAULT '' COMMENT '通知内容',
  IsRead TINYINT DEFAULT 0 COMMENT '是否已读',

  CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  PRIMARY KEY (Id)
) COMMENT='通知表';
```

------

# ✅ 七、内容审核 & 行为分析

------

## ContentAudit

```
CREATE TABLE ContentAudit (
  Id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  TargetType TINYINT NOT NULL COMMENT '目标类型',
  TargetId BIGINT NOT NULL COMMENT '目标ID',

  AuditType TINYINT NOT NULL COMMENT '审核类型',
  RiskLevel TINYINT DEFAULT 0 COMMENT '风险等级',

  Result TINYINT DEFAULT 0 COMMENT '审核结果：0待审 1通过 2拒绝',

  HitWords JSON DEFAULT NULL COMMENT '命中敏感词',

  Remark VARCHAR(255) DEFAULT '' COMMENT '备注',

  CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  PRIMARY KEY (Id)
) COMMENT='内容审核表';
```

------

## UserBehavior

```
CREATE TABLE UserBehavior (
  Id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',

  UserId BIGINT NOT NULL COMMENT '用户ID',
  BehaviorType TINYINT NOT NULL COMMENT '行为类型：1浏览 2点赞 3收藏 4评论',

  TargetType TINYINT NOT NULL COMMENT '目标类型',
  TargetId BIGINT NOT NULL COMMENT '目标ID',

  Duration INT DEFAULT 0 COMMENT '停留时长（秒）',

  CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

  PRIMARY KEY (Id)
) COMMENT='用户行为表（推荐系统基础）';
```

------

### 