-- =====================================================================
-- Deepay 第三阶段 —— 支付 + 成交闭环
-- 执行环境：MySQL 5.7 / 8.0
-- =====================================================================

-- ----------------------------
-- Table: deepay_product（商品表，Task 2 补充）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `deepay_product`
(
    `id`         BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chain_code` VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '关联样式链码',
    `title`      VARCHAR(256)   NOT NULL DEFAULT '' COMMENT '商品标题',
    `description` TEXT                   COMMENT '商品描述',
    `cover_image` VARCHAR(512)  NOT NULL DEFAULT '' COMMENT '商品封面图URL',
    `price`      DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '销售价格（元）',
    `status`     VARCHAR(32)    NOT NULL DEFAULT 'SELLING' COMMENT '状态：SELLING / SOLD',
    `created_at` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_chain_code` (`chain_code`) COMMENT '链码唯一索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = 'Deepay 商品表';

-- ----------------------------
-- Table: deepay_order（订单表）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `deepay_order`
(
    `id`         BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chain_code` VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '关联商品链码',
    `amount`     DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '支付金额（元）',
    `status`     VARCHAR(32)    NOT NULL DEFAULT 'INIT' COMMENT '状态：INIT / PAID',
    `created_at` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_chain_code` (`chain_code`) COMMENT '链码索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = 'Deepay 订单表';

-- ----------------------------
-- Table: deepay_metrics（复盘指标表）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `deepay_metrics`
(
    `id`         BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chain_code` VARCHAR(64)    NOT NULL DEFAULT '' COMMENT '关联商品链码',
    `price`      DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '成交价格（元）',
    `sold`       TINYINT        NOT NULL DEFAULT 0 COMMENT '是否已售：0=未售，1=已售',
    `created_at` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_chain_code` (`chain_code`) COMMENT '链码索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = 'Deepay 复盘指标表';
