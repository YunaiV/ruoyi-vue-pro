-- =====================================================================
-- Deepay 第三阶段 —— 库存与货物智能管理
-- 执行环境：MySQL 5.7 / 8.0
-- =====================================================================

CREATE TABLE IF NOT EXISTS `deepay_inventory`
(
    `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chain_code`   VARCHAR(64) NOT NULL DEFAULT '' COMMENT '商品链码，关联 deepay_style_chain.chain_code',
    `stock`        INT         NOT NULL DEFAULT 0 COMMENT '当前可用库存',
    `locked_stock` INT         NOT NULL DEFAULT 0 COMMENT '锁定库存（已下单未支付）',
    `status`       VARCHAR(16) NOT NULL DEFAULT 'NORMAL' COMMENT '库存状态：NORMAL / LOW / OUT',
    `updated_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_chain_code` (`chain_code`) COMMENT '链码唯一索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = 'Deepay 库存管理表';
