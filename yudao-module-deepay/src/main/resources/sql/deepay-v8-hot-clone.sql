-- ============================================================
-- Deepay Phase 8 — 爆款复制引擎（Hot Clone Engine）
-- 文件：deepay-v8-hot-clone.sql
-- ============================================================

-- ============================================================
-- 1. 爆款表 deepay_hot_product
--    识别规则：sold_count >= 10 → HOT；>= 50 → SUPER_HOT
--    HotCloneAgent 扫描 deepay_product + deepay_metrics 后写入
-- ============================================================
CREATE TABLE IF NOT EXISTS deepay_hot_product (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    chain_code      VARCHAR(64)     NOT NULL               COMMENT '原始商品链码（关联 deepay_product）',
    category        VARCHAR(64)     NULL                   COMMENT '品类（外套/内裤/连衣裙等）',
    style           VARCHAR(64)     NULL                   COMMENT '风格标签（SEXY/MINIMAL/CASUAL等）',
    image_url       VARCHAR(512)    NULL                   COMMENT '原款主图 CDN 地址',
    sold_count      INT             NOT NULL DEFAULT 0     COMMENT '累计销量（识别爆款依据）',
    hot_level       VARCHAR(16)     NOT NULL DEFAULT 'HOT' COMMENT 'HOT（>=10）或 SUPER_HOT（>=50）',
    score           DOUBLE          NULL                   COMMENT '爆款评分（sold_count / 50.0，0~1+）',
    created_at      DATETIME        NOT NULL               COMMENT '记录写入时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_chain_code (chain_code),
    INDEX idx_category  (category),
    INDEX idx_hot_level (hot_level),
    INDEX idx_sold_count (sold_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='爆款识别结果表（HotCloneAgent 写入，DeepayHotCloneScheduler 定时扫描）';

-- ============================================================
-- 2. 变体表 deepay_variant
--    每条记录代表一个由爆款衍生出的变体款式
--    variant_code 格式：{parent_chain_code}-V{001}
-- ============================================================
CREATE TABLE IF NOT EXISTS deepay_variant (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    parent_chain_code   VARCHAR(64)     NOT NULL               COMMENT '爆款父链码（关联 deepay_hot_product.chain_code）',
    variant_code        VARCHAR(64)     NOT NULL               COMMENT '变体唯一编码（父链码-V序号）',
    category            VARCHAR(64)     NULL                   COMMENT '品类（与父款相同）',
    color               VARCHAR(32)     NULL                   COMMENT '颜色变体（黑/白/灰/米白等）',
    fabric              VARCHAR(64)     NULL                   COMMENT '面料变体（棉/牛仔/针织/羊毛等）',
    fit                 VARCHAR(32)     NULL                   COMMENT '版型变体（宽松/修身/直筒等）',
    style               VARCHAR(64)     NULL                   COMMENT '风格标签（与父款相同）',
    image_url           VARCHAR(512)    NULL                   COMMENT '变体设计图 CDN 地址（FluxService 生成）',
    design_prompt       VARCHAR(1024)   NULL                   COMMENT '生成本变体所用的 Prompt',
    created_at          DATETIME        NOT NULL               COMMENT '变体创建时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_variant_code (variant_code),
    INDEX idx_parent    (parent_chain_code),
    INDEX idx_category  (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='爆款变体表（HotCloneAgent 生成，每款爆款最多衍生 15 个变体款）';
