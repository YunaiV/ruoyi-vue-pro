-- ============================================================
-- Deepay Phase 6 — AI 选品系统：客户画像记忆 + 趋势过滤 + 风格引擎
-- 文件：deepay-v6-profile.sql
-- ============================================================

-- ============================================================
-- 1. 用户画像表（终端用户，含商家/设计师）
--    user_id VARCHAR(64)，兼容外部系统 UID（微信 openid、平台账号等）
-- ============================================================
CREATE TABLE IF NOT EXISTS deepay_user_profile (
    id                  BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id             VARCHAR(64)    NOT NULL               COMMENT '外部用户 ID（微信openid/平台UID等）',
    category            VARCHAR(64)    NULL                   COMMENT '主营品类（上衣/外套/内裤/裤子/连衣裙等）',
    style_preference    VARCHAR(64)    NULL                   COMMENT '主要风格标签（SEXY/CASUAL/SPORT/MINIMAL/LUXURY）',
    style_weights_json  TEXT           NULL                   COMMENT '风格权重 JSON，如 {"SEXY":0.8,"MINIMAL":0.3}',
    gender              VARCHAR(16)    NULL                   COMMENT '目标性别：MALE/FEMALE/UNISEX',
    age_group           VARCHAR(32)    NULL                   COMMENT '年龄段：YOUNG/MIDDLE/ELDER',
    price_range         VARCHAR(32)    NULL                   COMMENT '价格带：LOW/MID/HIGH',
    market              VARCHAR(32)    NULL                   COMMENT '目标市场：CN/EU/US/ME',
    confidence          DECIMAL(5,2)   NOT NULL DEFAULT 0.00  COMMENT '画像置信度（0~1），≥0.6 跳过问卷',
    created_at          DATETIME       NOT NULL               COMMENT '创建时间',
    updated_at          DATETIME       NOT NULL               COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_user_id (user_id),
    INDEX idx_category  (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='终端用户画像（MemoryAgent 读写，实现"记住用户偏好"）';

-- ============================================================
-- 2. B2B 客户画像表（由 CustomerProfileAgent 读写）
-- ============================================================
CREATE TABLE IF NOT EXISTS deepay_customer_profile (
    id                  BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    customer_id         BIGINT         NOT NULL               COMMENT 'B2B 客户 ID（关联 deepay_client.id）',
    category_level1     VARCHAR(64)    NULL                   COMMENT '一级品类',
    category_level2     VARCHAR(64)    NULL                   COMMENT '二级品类',
    style_weights       TEXT           NULL                   COMMENT '风格权重 JSON',
    price_level         VARCHAR(16)    NULL                   COMMENT '价格带：LOW/MID/HIGH',
    market              VARCHAR(16)    NULL                   COMMENT '目标市场：CN/EU/US/ME',
    target_age          VARCHAR(16)    NULL                   COMMENT '年龄段：YOUNG/MIDDLE/ELDER',
    gender              VARCHAR(16)    NULL                   COMMENT '目标性别：MALE/FEMALE/UNISEX',
    confidence_score    DECIMAL(5,2)   NOT NULL DEFAULT 0.00  COMMENT '画像置信度（0~1）',
    created_at          DATETIME       NOT NULL               COMMENT '创建时间',
    updated_at          DATETIME       NOT NULL               COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_customer_id (customer_id),
    INDEX idx_category_level1 (category_level1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='B2B 客户画像（CustomerProfileAgent 读写）';

-- ============================================================
-- 3. deepay_product：增加 category 字段
--    TrendAgent 用 WHERE p.category = ? 实现强品类过滤
-- ============================================================
ALTER TABLE deepay_product
    ADD COLUMN IF NOT EXISTS category VARCHAR(64) NULL
        COMMENT '商品品类（外套/内裤/裤子/上衣/连衣裙），TrendAgent 过滤依据'
        AFTER cdn_image_url;

-- 若数据库不支持 IF NOT EXISTS（MySQL < 8.0），使用如下替代：
-- ALTER TABLE deepay_product ADD COLUMN category VARCHAR(64) NULL COMMENT '商品品类' AFTER cdn_image_url;

CREATE INDEX IF NOT EXISTS idx_product_category ON deepay_product (category);

-- ============================================================
-- 4. deepay_metrics：增加 style 字段
--    TrendAgent 风格加权排序依据；AnalyticsAgent 落库时写入
-- ============================================================
ALTER TABLE deepay_metrics
    ADD COLUMN IF NOT EXISTS style VARCHAR(32) NULL
        COMMENT '风格标签（SEXY/CASUAL/SPORT/MINIMAL/LUXURY），AnalyticsAgent 写入'
        AFTER category;

-- ============================================================
-- 5. 用户选择记录表（"越用越准"核心）
-- ============================================================
CREATE TABLE IF NOT EXISTS deepay_user_selection (
    id              BIGINT       NOT NULL AUTO_INCREMENT    COMMENT '主键',
    user_id         VARCHAR(64)  NOT NULL                   COMMENT '外部用户 ID（与 deepay_user_profile.user_id 对应）',
    chain_code      VARCHAR(64)  NULL                       COMMENT '关联链码，可追溯完整生产链路',
    selected_image  VARCHAR(512) NULL                       COMMENT '用户选中的设计图 CDN 地址',
    category        VARCHAR(64)  NULL                       COMMENT '本次选择品类（外套/内裤等）',
    style           VARCHAR(64)  NULL                       COMMENT '本次选择风格标签（SEXY/MINIMAL等）',
    market          VARCHAR(16)  NULL                       COMMENT '本次目标市场（CN/EU/US/ME）',
    score           INT          NULL                       COMMENT '设计图评分（JudgeAgent 打分）',
    created_at      DATETIME     NOT NULL                   COMMENT '选择时间',
    PRIMARY KEY (id),
    INDEX idx_user_id  (user_id),
    INDEX idx_category (category),
    INDEX idx_user_category (user_id, category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COMMENT='用户选图记录（SelectionAgent 写入，TrendAgent 下次优先返回同类目款式）';

-- ============================================================
-- 6. 历史数据回填（可选，生产环境谨慎执行）
--    将 deepay_metrics.category 复制到 deepay_product.category（按 chain_code 关联）
-- ============================================================
-- UPDATE deepay_product p
--     JOIN deepay_metrics m ON p.chain_code = m.chain_code
-- SET p.category = m.category
-- WHERE p.category IS NULL AND m.category IS NOT NULL;
