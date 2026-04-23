-- =====================================================================
-- Deepay Selection V2 — 选款引导层增强（数据库迁移脚本）
-- 变更内容：
--   1. deepay_trend_pool  — 新增 brand 字段（来源品牌名）
--   2. deepay_customer_profile — 新增 purpose 字段（批发/零售）
-- 执行环境：MySQL 5.7 / 8.0
-- =====================================================================

-- ----------------------------
-- 1. deepay_trend_pool — 新增 brand 字段
-- ----------------------------
ALTER TABLE `deepay_trend_pool`
    ADD COLUMN IF NOT EXISTS `brand` VARCHAR(64) DEFAULT NULL
        COMMENT '来源品牌（ZARA / H&M / Nike / Adidas / Gucci / Prada / Balenciaga / SHEIN / TikTok…）'
        AFTER `image_url`;

-- ----------------------------
-- 2. deepay_customer_profile — 新增 purpose 字段
-- ----------------------------
ALTER TABLE `deepay_customer_profile`
    ADD COLUMN IF NOT EXISTS `purpose` VARCHAR(20) DEFAULT NULL
        COMMENT '用途：WHOLESALE（批发）/ RETAIL（零售）'
        AFTER `gender`;

-- ----------------------------
-- 3. 更新种子数据 brand（如表中有旧数据，按 source 批量回填）
-- ----------------------------
UPDATE `deepay_trend_pool` SET `brand` = 'SHEIN'       WHERE `source` = 'shein'   AND `brand` IS NULL;
UPDATE `deepay_trend_pool` SET `brand` = 'TikTok'      WHERE `source` = 'tiktok'  AND `brand` IS NULL;
UPDATE `deepay_trend_pool` SET `brand` = '1688'        WHERE `source` = '1688'    AND `brand` IS NULL;
UPDATE `deepay_trend_pool` SET `brand` = '秀场'         WHERE `source` = 'runway'  AND `brand` IS NULL;
UPDATE `deepay_trend_pool` SET `brand` = '品牌参考'      WHERE `source` = 'brand'   AND `brand` IS NULL;
