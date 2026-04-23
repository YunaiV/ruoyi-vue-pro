-- ============================================================
-- Deepay 数据库迁移脚本：Phase 3 稳定性 + Phase 4 利润驱动 + Phase 5 B2B 供应链
-- 文件：deepay-v4-v5.sql
-- ============================================================

-- ============================================================
-- Phase 3 / Block 1-4：现有表结构变更
-- ============================================================

-- deepay_order：增加 user_id（订单唯一性）、paid_at（幂等时间戳）
ALTER TABLE deepay_order
    ADD COLUMN user_id   BIGINT         NULL COMMENT '下单用户 ID（B2B 场景）' AFTER chain_code,
    ADD COLUMN paid_at   DATETIME       NULL COMMENT '实际支付时间'              AFTER status;

-- 唯一索引：防止同一用户对同一商品重复下单
ALTER TABLE deepay_order
    ADD UNIQUE INDEX uk_chain_code_user_id (chain_code, user_id);

-- deepay_product：增加成本价、CDN 图片地址
ALTER TABLE deepay_product
    ADD COLUMN cost_price    DECIMAL(10,2)  NULL COMMENT '生产成本（元），用于利润计算'  AFTER stock,
    ADD COLUMN cdn_image_url VARCHAR(512)   NULL COMMENT 'CDN 图片地址'                 AFTER cost_price;

-- deepay_metrics：Phase 4 利润字段 + Phase 3 行为分析字段
ALTER TABLE deepay_metrics
    ADD COLUMN view_count        INT            DEFAULT 0    COMMENT '商品详情页浏览次数'   AFTER sold_count,
    ADD COLUMN pay_count         INT            DEFAULT 0    COMMENT '成功支付次数'          AFTER view_count,
    ADD COLUMN conversion_rate   DECIMAL(6,4)   DEFAULT 0    COMMENT '转化率=pay/view'       AFTER pay_count,
    ADD COLUMN cost_price        DECIMAL(10,2)  NULL         COMMENT '成本快照'              AFTER conversion_rate,
    ADD COLUMN profit            DECIMAL(10,2)  NULL         COMMENT '单笔利润=price-cost'   AFTER cost_price,
    ADD COLUMN roi               DECIMAL(10,4)  NULL         COMMENT 'ROI=profit/cost'        AFTER profit;

-- deepay_style_chain：增加 context_snapshot（AI 决策回溯）
ALTER TABLE deepay_style_chain
    ADD COLUMN context_snapshot  LONGTEXT       NULL COMMENT '决策快照 JSON，每个 Agent 执行后更新';

-- ============================================================
-- Phase 3 / Block 5：审计日志表
-- ============================================================
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计日志（不可变，永久保留）';

-- ============================================================
-- Phase 3 / Block 6：失败重试任务表
-- ============================================================
CREATE TABLE IF NOT EXISTS deepay_retry_task (
    id              BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    chain_code      VARCHAR(32)    NULL                   COMMENT '关联链码',
    task_type       VARCHAR(32)    NOT NULL               COMMENT 'AI_DESIGN/PAYMENT/PATTERN/PUBLISH',
    status          VARCHAR(16)    NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/RETRYING/DONE/FAILED',
    error_msg       VARCHAR(2048)  NULL                   COMMENT '失败原因摘要',
    retry_count     INT            NOT NULL DEFAULT 0     COMMENT '已重试次数',
    max_retry       INT            NOT NULL DEFAULT 3     COMMENT '最大重试次数',
    next_retry_at   DATETIME       NULL                   COMMENT '下次重试时间（退避策略）',
    created_at      DATETIME       NOT NULL               COMMENT '创建时间',
    updated_at      DATETIME       NOT NULL               COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_status_next_retry (status, next_retry_at),
    INDEX idx_chain_code (chain_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='失败任务重试队列';

-- ============================================================
-- Phase 5 / B2B 供应链：客户表
-- ============================================================
CREATE TABLE IF NOT EXISTS deepay_client (
    id                  BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    name                VARCHAR(128)   NOT NULL               COMMENT '客户名称',
    level               CHAR(1)        NOT NULL DEFAULT 'B'   COMMENT '等级：A大客户/B标准/C限量',
    total_order_amount  DECIMAL(14,2)  NOT NULL DEFAULT 0     COMMENT '历史累计下单金额（元）',
    contact             VARCHAR(128)   NULL                   COMMENT '联系方式',
    remark              VARCHAR(512)   NULL                   COMMENT '备注',
    created_at          DATETIME       NOT NULL               COMMENT '创建时间',
    updated_at          DATETIME       NOT NULL               COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='B2B 客户（分层：A/B/C）';

-- ============================================================
-- Phase 5 / B2B 供应链：需求预测表
-- ============================================================
CREATE TABLE IF NOT EXISTS deepay_demand_forecast (
    id                      BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    chain_code              VARCHAR(32)    NULL                   COMMENT '关联链码',
    category                VARCHAR(64)    NULL                   COMMENT '品类关键词',
    predicted_sales         INT            NOT NULL               COMMENT '预测销量（件）',
    confidence              DECIMAL(5,2)   NULL                   COMMENT '置信度 0~1',
    forecast_days           INT            NOT NULL DEFAULT 7     COMMENT '预测周期（天）',
    season_factor           DECIMAL(5,2)   NULL                   COMMENT '季节因子',
    suggested_production_qty INT           NULL                   COMMENT '建议生产量',
    created_at              DATETIME       NOT NULL               COMMENT '预测时间',
    PRIMARY KEY (id),
    INDEX idx_chain_code (chain_code),
    INDEX idx_category   (category),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求预测（DemandAgent 输出）';

-- ============================================================
-- Phase 5 / B2B 供应链：生产计划表
-- ============================================================
CREATE TABLE IF NOT EXISTS deepay_production_plan (
    id                  BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    chain_code          VARCHAR(32)    NULL                   COMMENT '关联链码',
    planned_quantity    INT            NOT NULL               COMMENT '计划生产量',
    forecast_demand     INT            NULL                   COMMENT '来自 DemandAgent 的预测需求',
    current_stock       INT            NULL                   COMMENT '当前库存快照',
    in_transit_stock    INT            NULL                   COMMENT '在途库存快照',
    status              VARCHAR(16)    NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/IN_PRODUCTION/COMPLETED/CANCELLED',
    factory_id          BIGINT         NULL                   COMMENT '关联工厂 ID（可选）',
    created_at          DATETIME       NOT NULL               COMMENT '创建时间',
    updated_at          DATETIME       NOT NULL               COMMENT '更新时间',
    PRIMARY KEY (id),
    INDEX idx_chain_code (chain_code),
    INDEX idx_status     (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='生产计划（ProductionPlanner 输出）';
