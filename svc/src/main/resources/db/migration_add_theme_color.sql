-- =============================================
-- 用户表添加主题颜色字段
-- 执行方式：在 MySQL 中手动执行此 SQL
-- =============================================

ALTER TABLE `User`
    ADD COLUMN `ThemeColor` VARCHAR(16) DEFAULT NULL COMMENT '用户自定义主题颜色（hex格式，如 #7C3AED）'
    AFTER `Avatar`;

-- 已有用户默认为 null，前端会使用默认紫色 #7C3AED
