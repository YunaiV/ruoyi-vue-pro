-- =====================================================================
-- Deepay FX + 多货币 — 数据库迁移脚本（接 deepay-phase10.sql 之后执行）
-- 货币标准：EUR 基准，全部 BigDecimal，禁止 double/float
-- 执行环境：MySQL 5.7 / 8.0
-- =====================================================================

-- ----------------------------
-- 1. deepay_fx_rate（汇率表，以 EUR 为基准）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `deepay_fx_rate`
(
    `currency`   VARCHAR(10)    NOT NULL COMMENT '货币代码（ISO 4217），主键',
    `rate`       DECIMAL(12, 6) NOT NULL COMMENT '相对 EUR 的汇率（1 EUR = rate 目标货币）',
    `updated_at` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`currency`) USING BTREE,
    INDEX `idx_updated_at` (`updated_at` DESC) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = 'Deepay FX 汇率表（EUR基准，FxRateScheduler 每小时更新）';

-- 预置默认汇率（避免第一次启动时无数据）
INSERT INTO `deepay_fx_rate` (`currency`, `rate`, `updated_at`)
VALUES ('EUR', 1.000000, NOW()),
       ('USD', 1.080000, NOW()),
       ('CNY', 7.800000, NOW()),
       ('GBP', 0.860000, NOW()),
       ('AED', 3.970000, NOW()),
       ('JPY', 163.000000, NOW()),
       ('KRW', 1450.000000, NOW()),
       ('SGD', 1.460000, NOW()),
       ('HKD', 8.440000, NOW()),
       ('AUD', 1.650000, NOW()),
       ('CAD', 1.470000, NOW()),
       ('CHF', 0.950000, NOW()),
       ('SEK', 11.30000, NOW()),
       ('NOK', 11.60000, NOW()),
       ('DKK', 7.460000, NOW()),
       ('PLN', 4.230000, NOW()),
       ('INR', 90.000000, NOW()),
       ('BRL', 5.450000, NOW()),
       ('MXN', 18.200000, NOW()),
       ('SAR', 4.050000, NOW()),
       ('QAR', 3.930000, NOW()),
       ('THB', 38.500000, NOW()),
       ('MYR', 5.070000, NOW()),
       ('IDR', 17300.000000, NOW()),
       ('VND', 27300.000000, NOW()),
       ('PHP', 61.200000, NOW()),
       ('TWD', 34.800000, NOW())
AS new_val
ON DUPLICATE KEY UPDATE `rate` = new_val.`rate`, `updated_at` = new_val.`updated_at`;

-- ----------------------------
-- 2. deepay_product — 新增 base_price（EUR 基准价）
-- ----------------------------
ALTER TABLE `deepay_product`
    ADD COLUMN IF NOT EXISTS `base_price` DECIMAL(10, 2) DEFAULT NULL
        COMMENT '基准价（EUR），AI 定价系统唯一输出，Analytics 用此计算利润'
        AFTER `price`;

-- 初始化 base_price = price（迁移存量数据）
UPDATE `deepay_product` SET `base_price` = `price`
WHERE `base_price` IS NULL AND `price` IS NOT NULL;

-- ----------------------------
-- 3. deepay_order — 新增 base_amount + display_amount
-- ----------------------------
ALTER TABLE `deepay_order`
    ADD COLUMN IF NOT EXISTS `base_amount` DECIMAL(10, 2) DEFAULT NULL
        COMMENT '基准金额（EUR），真实结算用，Analytics 统一用此字段'
        AFTER `amount`,
    ADD COLUMN IF NOT EXISTS `display_amount` DECIMAL(10, 2) DEFAULT NULL
        COMMENT '展示金额（用户付款货币），Jeepay 支付金额 = display_amount × 100'
        AFTER `base_amount`;

-- 初始化存量数据
UPDATE `deepay_order`
SET `base_amount` = `amount`, `display_amount` = `amount`
WHERE `base_amount` IS NULL AND `amount` IS NOT NULL;

-- =====================================================================
-- 验收 SQL（执行后应看到数据）：
--   SELECT currency, rate FROM deepay_fx_rate ORDER BY currency;
--   → EUR:1.00 / USD:1.08 / CNY:7.80 / GBP:0.86 / AED:3.97 …
-- =====================================================================
