-- =====================================================================
-- Deepay 一衣一链 MVP —— 第3步：商品展示页字段补充
-- 执行环境：MySQL 5.7 / 8.0
-- =====================================================================

ALTER TABLE `deepay_style_chain`
    ADD COLUMN `title`       VARCHAR(255)   DEFAULT NULL COMMENT '商品标题',
    ADD COLUMN `description` TEXT           DEFAULT NULL COMMENT '商品描述',
    ADD COLUMN `price`       DECIMAL(10, 2) DEFAULT NULL COMMENT '商品价格（欧元）';
