-- ============================================================
-- Deepay Master Migration — 全量建表脚本（Phase 3 → Phase 9）
-- 执行顺序：按文件内 section 顺序依次执行
-- 所有表：CREATE TABLE IF NOT EXISTS，InnoDB，utf8mb4
-- ============================================================

-- ============================================================
-- SECTION 1: v3 核心表（deepay_style_chain 补字段 + 新建表）
-- ============================================================

-- deepay_style_chain（设计链）—— 存量补字段
ALTER TABLE deepay_style_chain
    ADD COLUMN IF NOT EXISTS keyword         VARCHAR(256)  DEFAULT NULL COMMENT '用户输入关键词',
    ADD COLUMN IF NOT EXISTS selected_image  VARCHAR(512)  DEFAULT NULL COMMENT 'AI 决策选中的图片',
    ADD COLUMN IF NOT EXISTS pattern_file    VARCHAR(512)  DEFAULT NULL COMMENT '打版文件路径',
    ADD COLUMN IF NOT EXISTS decision_reason VARCHAR(1024) DEFAULT NULL COMMENT 'AI 决策原因（可解释）';

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

CREATE TABLE IF NOT EXISTS deepay_inventory (
    id           BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    chain_code   VARCHAR(64) NOT NULL                COMMENT '关联链码',
    stock        INT         NOT NULL DEFAULT 0      COMMENT '可用库存',
    locked_stock INT         NOT NULL DEFAULT 0      COMMENT '锁定库存（下单未支付）',
    created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_chain_code (chain_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Deepay 库存表';

CREATE TABLE IF NOT EXISTS deepay_metrics (
    id         BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    chain_code VARCHAR(64)   NOT NULL                COMMENT '关联链码',
    sold_count INT           NOT NULL DEFAULT 0      COMMENT '销量快照',
    price      DECIMAL(10,2)          DEFAULT NULL   COMMENT '价格快照',
    category   VARCHAR(256)           DEFAULT NULL   COMMENT '分类 / 复盘 action',
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_chain_code (chain_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Deepay 销售指标快照表';

CREATE TABLE IF NOT EXISTS deepay_order (
    id         BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    payment_id VARCHAR(128)  NOT NULL                COMMENT '唯一支付 ID',
    chain_code VARCHAR(64)   NOT NULL                COMMENT '关联链码',
    status     VARCHAR(32)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING / PAID / CANCELLED',
    amount     DECIMAL(10,2) NOT NULL DEFAULT 0      COMMENT '实收金额',
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at    DATETIME               DEFAULT NULL   COMMENT '支付时间',
    UNIQUE KEY uk_payment_id (payment_id),
    KEY idx_chain_code (chain_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Deepay 订单表';

CREATE TABLE IF NOT EXISTS deepay_design_version (
    id         BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    chain_code VARCHAR(64)            DEFAULT NULL   COMMENT '关联链码',
    image_url  VARCHAR(512)  NOT NULL                COMMENT '设计图 URL',
    version    INT           NOT NULL DEFAULT 1      COMMENT '版本号',
    selected   TINYINT(1)    NOT NULL DEFAULT 0      COMMENT '是否被选中',
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_chain_code (chain_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Deepay 设计版本历史表';

CREATE TABLE IF NOT EXISTS deepay_production (
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    chain_code   VARCHAR(64)  NOT NULL                COMMENT '关联链码',
    pattern_file VARCHAR(512)          DEFAULT NULL   COMMENT '版型文件路径',
    tech_pack    VARCHAR(512)          DEFAULT NULL   COMMENT '技术包下载地址',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_chain_code (chain_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Deepay 生产记录表';

ALTER TABLE deepay_product
    ADD COLUMN IF NOT EXISTS description VARCHAR(2048) DEFAULT NULL COMMENT '商品描述';

-- ============================================================
-- SECTION 2: v4-v5（Phase 3/4/5 — 稳定性 + 利润 + B2B）
-- ============================================================

ALTER TABLE deepay_order
    ADD COLUMN IF NOT EXISTS user_id   BIGINT         NULL COMMENT '下单用户 ID' AFTER chain_code,
    ADD COLUMN IF NOT EXISTS paid_at   DATETIME       NULL COMMENT '实际支付时间' AFTER status;

ALTER TABLE deepay_order
    ADD UNIQUE INDEX IF NOT EXISTS uk_chain_code_user_id (chain_code, user_id);

ALTER TABLE deepay_product
    ADD COLUMN IF NOT EXISTS cost_price    DECIMAL(10,2)  NULL COMMENT '生产成本（元）' AFTER stock,
    ADD COLUMN IF NOT EXISTS cdn_image_url VARCHAR(512)   NULL COMMENT 'CDN 图片地址'  AFTER cost_price;

ALTER TABLE deepay_metrics
    ADD COLUMN IF NOT EXISTS view_count        INT            DEFAULT 0   COMMENT '浏览次数'       AFTER sold_count,
    ADD COLUMN IF NOT EXISTS pay_count         INT            DEFAULT 0   COMMENT '成功支付次数'    AFTER view_count,
    ADD COLUMN IF NOT EXISTS conversion_rate   DECIMAL(6,4)   DEFAULT 0   COMMENT '转化率'         AFTER pay_count,
    ADD COLUMN IF NOT EXISTS cost_price        DECIMAL(10,2)  NULL        COMMENT '成本快照'        AFTER conversion_rate,
    ADD COLUMN IF NOT EXISTS profit            DECIMAL(10,2)  NULL        COMMENT '单笔利润'        AFTER cost_price,
    ADD COLUMN IF NOT EXISTS roi               DECIMAL(10,4)  NULL        COMMENT 'ROI'             AFTER profit;

ALTER TABLE deepay_style_chain
    ADD COLUMN IF NOT EXISTS context_snapshot LONGTEXT NULL COMMENT '决策快照 JSON';

CREATE TABLE IF NOT EXISTS deepay_log (
    id          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    chain_code  VARCHAR(32)    NULL                   COMMENT '关联链码',
    action      VARCHAR(32)    NOT NULL               COMMENT 'CREATE/PUBLISH/PAY/REPRICE/STOP/REDESIGN/REFUND/RESTOCK/RETRY',
    before_val  TEXT           NULL                   COMMENT '操作前状态',
    after_val   TEXT           NULL                   COMMENT '操作后状态',
    time        DATETIME       NOT NULL               COMMENT '操作时间',
    PRIMARY KEY (id),
    INDEX idx_chain_code (chain_code),
    INDEX idx_action     (action),
    INDEX idx_time       (time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计日志';

CREATE TABLE IF NOT EXISTS deepay_retry_task (
    id              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    chain_code      VARCHAR(32)    NULL                   COMMENT '关联链码',
    task_type       VARCHAR(32)    NOT NULL               COMMENT 'AI_DESIGN/PAYMENT/PATTERN/PUBLISH',
    status          VARCHAR(16)    NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/RETRYING/DONE/FAILED',
    error_msg       VARCHAR(2048)  NULL                   COMMENT '失败原因摘要',
    retry_count     INT            NOT NULL DEFAULT 0     COMMENT '已重试次数',
    max_retry       INT            NOT NULL DEFAULT 3     COMMENT '最大重试次数',
    next_retry_at   DATETIME       NULL                   COMMENT '下次重试时间',
    created_at      DATETIME       NOT NULL               COMMENT '创建时间',
    updated_at      DATETIME       NOT NULL               COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_status_next_retry (status, next_retry_at),
    INDEX idx_chain_code (chain_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='失败任务重试队列';

CREATE TABLE IF NOT EXISTS deepay_client (
    id                  BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    name                VARCHAR(128)   NOT NULL               COMMENT '客户名称',
    level               CHAR(1)        NOT NULL DEFAULT 'B'   COMMENT '等级：A/B/C',
    total_order_amount  DECIMAL(14,2)  NOT NULL DEFAULT 0     COMMENT '历史累计下单金额',
    contact             VARCHAR(128)   NULL                   COMMENT '联系方式',
    remark              VARCHAR(512)   NULL                   COMMENT '备注',
    created_at          DATETIME       NOT NULL               COMMENT '创建时间',
    updated_at          DATETIME       NOT NULL               COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='B2B 客户（分层：A/B/C）';

CREATE TABLE IF NOT EXISTS deepay_demand_forecast (
    id                       BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    chain_code               VARCHAR(32)    NULL                   COMMENT '关联链码',
    category                 VARCHAR(64)    NULL                   COMMENT '品类关键词',
    predicted_sales          INT            NOT NULL               COMMENT '预测销量（件）',
    confidence               DECIMAL(5,2)   NULL                   COMMENT '置信度 0~1',
    forecast_days            INT            NOT NULL DEFAULT 7     COMMENT '预测周期（天）',
    season_factor            DECIMAL(5,2)   NULL                   COMMENT '季节因子',
    suggested_production_qty INT            NULL                   COMMENT '建议生产量',
    created_at               DATETIME       NOT NULL               COMMENT '预测时间',
    PRIMARY KEY (id),
    INDEX idx_chain_code (chain_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Deepay 需求预测表';

-- ============================================================
-- SECTION 3: v6（Phase 6 — 客户画像 + 用户记忆）
-- ============================================================

CREATE TABLE IF NOT EXISTS deepay_user_profile (
    id                  BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id             VARCHAR(64)    NOT NULL               COMMENT '外部用户 ID',
    category            VARCHAR(64)    NULL                   COMMENT '主营品类',
    style_preference    VARCHAR(64)    NULL                   COMMENT '主要风格标签',
    style_weights_json  TEXT           NULL                   COMMENT '风格权重 JSON',
    gender              VARCHAR(16)    NULL                   COMMENT '目标性别：MALE/FEMALE/UNISEX',
    age_group           VARCHAR(32)    NULL                   COMMENT '年龄段：YOUNG/MIDDLE/ELDER',
    price_range         VARCHAR(32)    NULL                   COMMENT '价格带：LOW/MID/HIGH',
    market              VARCHAR(32)    NULL                   COMMENT '目标市场：CN/EU/US/ME',
    confidence          DECIMAL(5,2)   NOT NULL DEFAULT 0.00  COMMENT '画像置信度（0~1）',
    created_at          DATETIME       NOT NULL               COMMENT '创建时间',
    updated_at          DATETIME       NOT NULL               COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_user_id (user_id),
    INDEX idx_category  (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='终端用户画像';

CREATE TABLE IF NOT EXISTS deepay_customer_profile (
    id                  BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    customer_id         BIGINT         NOT NULL               COMMENT 'B2B 客户 ID',
    category_level1     VARCHAR(64)    NULL                   COMMENT '一级品类',
    category_level2     VARCHAR(64)    NULL                   COMMENT '二级品类',
    style_weights       TEXT           NULL                   COMMENT '风格权重 JSON',
    price_level         VARCHAR(16)    NULL                   COMMENT '价格带：LOW/MID/HIGH',
    market              VARCHAR(16)    NULL                   COMMENT '目标市场：CN/EU/US/ME',
    target_age          VARCHAR(16)    NULL                   COMMENT '年龄段：YOUNG/MIDDLE/ELDER',
    gender              VARCHAR(16)    NULL                   COMMENT '目标性别：MALE/FEMALE/UNISEX',
    confidence_score    DECIMAL(5,2)   NOT NULL DEFAULT 0.00  COMMENT '画像置信度',
    created_at          DATETIME       NOT NULL               COMMENT '创建时间',
    updated_at          DATETIME       NOT NULL               COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_customer_id (customer_id),
    INDEX idx_category_level1 (category_level1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='B2B 客户画像';

ALTER TABLE deepay_product
    ADD COLUMN IF NOT EXISTS category VARCHAR(64) NULL COMMENT '商品品类' AFTER cdn_image_url;

ALTER TABLE deepay_product
    ADD COLUMN IF NOT EXISTS style VARCHAR(64) NULL COMMENT '风格标签' AFTER category;

CREATE INDEX IF NOT EXISTS idx_product_category ON deepay_product (category);

ALTER TABLE deepay_metrics
    ADD COLUMN IF NOT EXISTS style VARCHAR(32) NULL COMMENT '风格标签' AFTER category;

CREATE TABLE IF NOT EXISTS deepay_user_selection (
    id              BIGINT       NOT NULL AUTO_INCREMENT    COMMENT '主键',
    user_id         VARCHAR(64)  NOT NULL                   COMMENT '外部用户 ID',
    chain_code      VARCHAR(64)  NULL                       COMMENT '关联链码',
    selected_image  VARCHAR(512) NULL                       COMMENT '用户选中的设计图 CDN 地址',
    category        VARCHAR(64)  NULL                       COMMENT '本次选择品类',
    style           VARCHAR(64)  NULL                       COMMENT '本次选择风格标签',
    market          VARCHAR(16)  NULL                       COMMENT '本次目标市场',
    score           INT          NULL                       COMMENT '设计图评分',
    created_at      DATETIME     NOT NULL                   COMMENT '选择时间',
    PRIMARY KEY (id),
    INDEX idx_user_id  (user_id),
    INDEX idx_category (category),
    INDEX idx_user_category (user_id, category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户选图记录';

-- ============================================================
-- SECTION 4: v8（Phase 8 — 爆款复制引擎）
-- ============================================================

CREATE TABLE IF NOT EXISTS deepay_hot_product (
    id              BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    chain_code      VARCHAR(64)     NOT NULL               COMMENT '原始商品链码',
    category        VARCHAR(64)     NULL                   COMMENT '品类',
    style           VARCHAR(64)     NULL                   COMMENT '风格标签',
    image_url       VARCHAR(512)    NULL                   COMMENT '原款主图 CDN 地址',
    sold_count      INT             NOT NULL DEFAULT 0     COMMENT '累计销量',
    hot_level       VARCHAR(16)     NOT NULL DEFAULT 'HOT' COMMENT 'HOT 或 SUPER_HOT',
    score           DOUBLE          NULL                   COMMENT '爆款评分',
    created_at      DATETIME        NOT NULL               COMMENT '记录写入时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_chain_code (chain_code),
    INDEX idx_category  (category),
    INDEX idx_hot_level (hot_level),
    INDEX idx_sold_count (sold_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='爆款识别结果表';

CREATE TABLE IF NOT EXISTS deepay_variant (
    id                  BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键',
    parent_chain_code   VARCHAR(64)     NOT NULL               COMMENT '爆款父链码',
    variant_code        VARCHAR(64)     NOT NULL               COMMENT '变体唯一编码',
    category            VARCHAR(64)     NULL                   COMMENT '品类',
    color               VARCHAR(32)     NULL                   COMMENT '颜色变体',
    fabric              VARCHAR(64)     NULL                   COMMENT '面料变体',
    fit                 VARCHAR(32)     NULL                   COMMENT '版型变体',
    style               VARCHAR(64)     NULL                   COMMENT '风格标签',
    image_url           VARCHAR(512)    NULL                   COMMENT '变体设计图 CDN 地址',
    design_prompt       VARCHAR(1024)   NULL                   COMMENT '生成本变体所用的 Prompt',
    created_at          DATETIME        NOT NULL               COMMENT '变体创建时间',
    PRIMARY KEY (id),
    UNIQUE INDEX uk_variant_code (variant_code),
    INDEX idx_parent    (parent_chain_code),
    INDEX idx_category  (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='爆款变体表';

-- ============================================================
-- SECTION 5: v9（Phase 9 — 外部趋势款选品库）
-- ============================================================

CREATE TABLE IF NOT EXISTS deepay_trend_item (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    image_url   VARCHAR(512)    NULL                     COMMENT '款式图片 URL',
    brand       VARCHAR(64)     NULL                     COMMENT '品牌名称',
    category    VARCHAR(64)     NULL                     COMMENT '品类',
    style       VARCHAR(64)     NULL                     COMMENT '风格标签',
    price       DECIMAL(10,2)   NULL                     COMMENT '参考售价',
    source      VARCHAR(32)     NULL                     COMMENT '数据来源（1688 / tiktok / shein / brand）',
    heat_score  INT             NOT NULL DEFAULT 0       COMMENT '热度分值',
    created_at  DATETIME        NOT NULL                 COMMENT '记录创建时间',
    PRIMARY KEY (id),
    INDEX idx_category   (category),
    INDEX idx_source     (source),
    INDEX idx_heat_score (heat_score),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外部趋势款选品表';

-- ============================================================
-- SECTION 6: v10（Phase 9 — 设计图评分）
-- ============================================================

CREATE TABLE IF NOT EXISTS deepay_design_image (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    url         VARCHAR(500)    NULL                     COMMENT '图片CDN地址',
    category    VARCHAR(64)     NULL                     COMMENT '品类',
    style       VARCHAR(64)     NULL                     COMMENT '风格',
    score       DOUBLE          NOT NULL DEFAULT 0       COMMENT '综合分',
    trend_score DOUBLE          NOT NULL DEFAULT 0       COMMENT '趋势分',
    match_score DOUBLE          NOT NULL DEFAULT 0       COMMENT '客户匹配分',
    created_at  DATETIME        NOT NULL                 COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_category (category),
    INDEX idx_score    (score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设计图评分结果表';

-- ============================================================
-- SECTION 7: v11（Phase 9 — 选款参考池）
-- ============================================================

CREATE TABLE IF NOT EXISTS deepay_selection_pool (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    image_url   VARCHAR(512)    NULL                     COMMENT '款式图片URL',
    category    VARCHAR(64)     NULL                     COMMENT '品类',
    style       VARCHAR(64)     NULL                     COMMENT '风格标签',
    source      VARCHAR(32)     NULL                     COMMENT '来源(1688/tiktok/shein/brand)',
    score       DOUBLE          NOT NULL DEFAULT 0       COMMENT '热度评分',
    created_at  DATETIME        NOT NULL                 COMMENT '创建时间',
    PRIMARY KEY (id),
    INDEX idx_category (category),
    INDEX idx_score    (score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选款参考池';

-- ============================================================
-- SECTION 8: v12（Phase 9 — 用户反馈学习）
-- ============================================================

CREATE TABLE IF NOT EXISTS deepay_feedback (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    image_id    BIGINT          NULL                     COMMENT '关联deepay_design_image.id',
    image_url   VARCHAR(512)    NULL                     COMMENT '图片URL',
    user_id     VARCHAR(64)     NULL                     COMMENT '用户ID',
    selected    TINYINT(1)      NOT NULL DEFAULT 0       COMMENT '是否被选中(1=是/0=否)',
    created_at  DATETIME        NOT NULL                 COMMENT '记录时间',
    PRIMARY KEY (id),
    INDEX idx_image_id (image_id),
    INDEX idx_user_id  (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户反馈学习表';
