-- Deepay 全量数据库脚本（最终版）
-- 执行顺序：1 → 2 → 3 → 4 → 5 → 6 → 7 → 8

-- ============================================================
-- 1. deepay_style_chain（设计链）—— 存量补字段
-- ============================================================
ALTER TABLE deepay_style_chain
    ADD COLUMN IF NOT EXISTS keyword         VARCHAR(256)  DEFAULT NULL COMMENT '用户输入关键词',
    ADD COLUMN IF NOT EXISTS selected_image  VARCHAR(512)  DEFAULT NULL COMMENT 'AI 决策选中的图片',
    ADD COLUMN IF NOT EXISTS pattern_file    VARCHAR(512)  DEFAULT NULL COMMENT '打版文件路径',
    ADD COLUMN IF NOT EXISTS decision_reason VARCHAR(1024) DEFAULT NULL COMMENT 'AI 决策原因（可解释）';

-- ============================================================
-- 2. deepay_product（商品表）—— 全新创建
-- ============================================================
CREATE TABLE IF NOT EXISTS deepay_product (
    id          BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    chain_code  VARCHAR(64)   NOT NULL                   COMMENT '关联链码',
    title       VARCHAR(512)  NOT NULL DEFAULT ''        COMMENT '商品标题',
    description VARCHAR(2048)          DEFAULT NULL      COMMENT '商品描述',
    price       DECIMAL(10,2) NOT NULL DEFAULT 0         COMMENT '售价（元）',
    status      VARCHAR(32)   NOT NULL DEFAULT 'DRAFT'   COMMENT 'DRAFT / SELLING / STOPPED / REDESIGNING',
    sold_count  INT           NOT NULL DEFAULT 0         COMMENT '销量',
    stock       INT           NOT NULL DEFAULT 0         COMMENT '可用库存',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_chain_code (chain_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Deepay 商品表';

-- ============================================================
-- 3. deepay_inventory（库存表）—— 全新创建
-- ============================================================
CREATE TABLE IF NOT EXISTS deepay_inventory (
    id           BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    chain_code   VARCHAR(64) NOT NULL                COMMENT '关联链码',
    stock        INT         NOT NULL DEFAULT 0      COMMENT '可用库存',
    locked_stock INT         NOT NULL DEFAULT 0      COMMENT '锁定库存（下单未支付）',
    created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_chain_code (chain_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Deepay 库存表';

-- ============================================================
-- 4. deepay_metrics（指标快照表）—— 全新创建
-- ============================================================
CREATE TABLE IF NOT EXISTS deepay_metrics (
    id         BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    chain_code VARCHAR(64)   NOT NULL                COMMENT '关联链码',
    sold_count INT           NOT NULL DEFAULT 0      COMMENT '销量快照',
    price      DECIMAL(10,2)          DEFAULT NULL   COMMENT '价格快照',
    category   VARCHAR(256)           DEFAULT NULL   COMMENT '分类 / 复盘 action（REVIEW:BOOST|...）',
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_chain_code (chain_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Deepay 销售指标快照表';

-- ============================================================
-- 5. deepay_order（订单表）—— 全新创建
-- ============================================================
CREATE TABLE IF NOT EXISTS deepay_order (
    id         BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    payment_id VARCHAR(128)  NOT NULL                COMMENT '唯一支付 ID（PAY-{chainCode}-{ts}）',
    chain_code VARCHAR(64)   NOT NULL                COMMENT '关联链码',
    status     VARCHAR(32)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING / PAID / CANCELLED',
    amount     DECIMAL(10,2) NOT NULL DEFAULT 0      COMMENT '实收金额',
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at    DATETIME               DEFAULT NULL   COMMENT '支付时间',
    UNIQUE KEY uk_payment_id (payment_id),
    KEY idx_chain_code (chain_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Deepay 订单表';

-- ============================================================
-- 6. deepay_design_version（设计版本表）—— 全新创建（可追溯）
-- ============================================================
CREATE TABLE IF NOT EXISTS deepay_design_version (
    id         BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    chain_code VARCHAR(64)            DEFAULT NULL   COMMENT '关联链码（JudgeAgent 运行时可能尚无 chainCode）',
    image_url  VARCHAR(512)  NOT NULL                COMMENT '设计图 URL',
    version    INT           NOT NULL DEFAULT 1      COMMENT '版本号（每次 REDESIGN +1）',
    selected   TINYINT(1)    NOT NULL DEFAULT 0      COMMENT '是否被 AIDecisionAgent 选中',
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_chain_code (chain_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Deepay 设计版本历史表';

-- ============================================================
-- 7. deepay_production（生产记录表）—— 全新创建（PatternAgent 落库）
-- ============================================================
CREATE TABLE IF NOT EXISTS deepay_production (
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    chain_code   VARCHAR(64)  NOT NULL                COMMENT '关联链码',
    pattern_file VARCHAR(512)          DEFAULT NULL   COMMENT '版型文件路径（.dxf）',
    tech_pack    VARCHAR(512)          DEFAULT NULL   COMMENT '技术包下载地址（PDF）',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_chain_code (chain_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Deepay 生产记录表';

-- ============================================================
-- 8. deepay_product 补 description 字段（若表已存在旧版本）
-- ============================================================
ALTER TABLE deepay_product
    ADD COLUMN IF NOT EXISTS description VARCHAR(2048) DEFAULT NULL COMMENT '商品描述',
    MODIFY COLUMN IF EXISTS status VARCHAR(32) NOT NULL DEFAULT 'DRAFT'
        COMMENT 'DRAFT / SELLING / STOPPED / REDESIGNING';

