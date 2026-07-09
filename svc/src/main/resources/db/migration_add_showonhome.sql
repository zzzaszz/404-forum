-- =============================================
-- 板块表添加首页快捷显示字段
-- =============================================

ALTER TABLE `Board`
    ADD COLUMN `ShowOnHome` INT DEFAULT 0 COMMENT '是否在首页快捷入口显示：1显示 0不显示'
    AFTER `Status`;
