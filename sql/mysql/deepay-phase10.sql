-- =====================================================================
-- Deepay Phase 10 — 商品化 + 收款闭环（数据库迁移脚本）
-- 货币标准：EUR（欧元），金额 BigDecimal，禁止 double/float
-- 执行环境：MySQL 5.7 / 8.0
-- =====================================================================

-- ----------------------------
-- 1. deepay_product 新增字段
-- ----------------------------
ALTER TABLE `deepay_product`
    ADD COLUMN IF NOT EXISTS `description` TEXT
        COMMENT '商品卖点描述（面料+版型+人群，由 ProductFinalizeAgent 生成）'
        AFTER `title`,
    ADD COLUMN IF NOT EXISTS `currency` VARCHAR(10) NOT NULL DEFAULT 'EUR'
        COMMENT '货币代码（ISO 4217），全系统统一 EUR'
        AFTER `price`,
    ADD COLUMN IF NOT EXISTS `channel` VARCHAR(64) DEFAULT NULL
        COMMENT '发布渠道（逗号分隔：H5 / H5,1688 / H5,Shopify），PublishChannelAgent 写入'
        AFTER `category`;

-- ----------------------------
-- 2. deepay_order 新增字段
-- ----------------------------
ALTER TABLE `deepay_order`
    ADD COLUMN IF NOT EXISTS `currency` VARCHAR(10) NOT NULL DEFAULT 'EUR'
        COMMENT '货币代码（ISO 4217），全系统统一 EUR'
        AFTER `amount`;

-- ----------------------------
-- 3. deepay_payment_log（支付日志，每次回调写一条）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `deepay_payment_log`
(
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `payment_id`   VARCHAR(128)  NOT NULL DEFAULT '' COMMENT '支付渠道订单号（Jeepay payOrderId / 我方 outTradeNo）',
    `order_id`     VARCHAR(32)            DEFAULT NULL COMMENT '关联 deepay_order.id',
    `status`       VARCHAR(32)   NOT NULL DEFAULT '' COMMENT '处理状态：PAID/IDEMPOTENT/SIGN_FAIL/AMOUNT_MISMATCH/ORDER_NOT_FOUND/REFUND',
    `amount`       DECIMAL(12,2)          DEFAULT NULL COMMENT '实收金额（EUR）',
    `callback_raw` TEXT                   COMMENT '回调原始报文（截取前2000字符）',
    `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_payment_id` (`payment_id`) USING BTREE,
    INDEX `idx_status`     (`status`) USING BTREE,
    INDEX `idx_created_at` (`created_at` DESC) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = 'Deepay 支付回调日志（Phase 10）';

-- ----------------------------
-- 4. deepay_design_generated（Phase 9，如不存在则创建）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `deepay_design_generated`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `chain_code` VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '链码',
    `image_url`  VARCHAR(512) NOT NULL DEFAULT '' COMMENT '生成图 URL',
    `prompt`     VARCHAR(500)          DEFAULT NULL COMMENT '生成 Prompt',
    `version`    INT          NOT NULL DEFAULT 1 COMMENT '版本号',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_chain_code` (`chain_code`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = 'Deepay 原创设计生成记录（Phase 9）';

-- ----------------------------
-- 5. deepay_design_variant（Phase 9）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `deepay_design_variant`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `chain_code` VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '链码',
    `image_url`  VARCHAR(512) NOT NULL DEFAULT '' COMMENT '变体图 URL',
    `style_tag`  VARCHAR(32)           DEFAULT NULL COMMENT '变体风格标签（原版微调/风格强化/简化版）',
    `score`      INT          NOT NULL DEFAULT 0 COMMENT '综合得分（0~100）',
    `selected`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否选中（1=是）',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_chain_code` (`chain_code`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = 'Deepay 设计变体记录（Phase 9）';

-- ----------------------------
-- 6. deepay_design_cost（Phase 9，如不存在则创建）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `deepay_design_cost`
(
    `id`           BIGINT        NOT NULL AUTO_INCREMENT,
    `chain_code`   VARCHAR(64)   NOT NULL DEFAULT '' COMMENT '链码',
    `fabric_cost`  DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '面料成本（EUR）',
    `labor_cost`   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '人工成本（EUR）',
    `total_cost`   DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '总成本（EUR）',
    `suggest_price`DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '建议售价（EUR）',
    `margin`       DECIMAL(8,4)           DEFAULT NULL COMMENT '毛利率（0~1）',
    `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_chain_code` (`chain_code`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COMMENT = 'Deepay 成本估算记录（Phase 9）';

-- =====================================================================
-- 备注：
--   所有金额字段类型 DECIMAL(12,2)，货币 EUR（欧元）
--   Jeepay 传入金额为"分"（× 100）：€19.99 → 1999
--   应用层全部使用 java.math.BigDecimal，禁止 double / float
-- =====================================================================
