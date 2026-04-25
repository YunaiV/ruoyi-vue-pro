-- ============================================================
-- Deepay AI Copilot MVP — 新增表迁移脚本
-- 版本：V2 — AI Persona / Rate Limit / Long-term Memory
-- 执行环境：MySQL 5.7 / 8.0
-- 执行方式：直接执行或通过 Flyway/Liquibase 管理
-- 所有表幂等安全（IF NOT EXISTS）
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. deepay_ai_persona — AI 角色人设（Prompt 可运营配置）
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_ai_persona` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`      BIGINT        NOT NULL DEFAULT 0      COMMENT '租户 ID（0=全局默认）',
    `module`         VARCHAR(32)   NOT NULL                COMMENT '板块：selection/design/product/inventory/finance/trend/order/sales',
    `role_name`      VARCHAR(64)   NOT NULL DEFAULT ''     COMMENT '角色名称（UI 展示用）',
    `system_prompt`  TEXT                   DEFAULT NULL   COMMENT '注入 LLM 的 system prompt（覆盖代码默认值）',
    `examples`       TEXT                   DEFAULT NULL   COMMENT '示例问答 JSON 数组（few-shot），格式：[{"q":"...","a":"..."}]',
    `tool_whitelist` VARCHAR(512)           DEFAULT NULL   COMMENT '允许使用的工具列表（逗号分隔）',
    `enabled`        TINYINT(1)    NOT NULL DEFAULT 1      COMMENT '是否启用（0=禁用，1=启用）',
    `creator`        VARCHAR(64)   NOT NULL DEFAULT ''     COMMENT '创建人',
    `updater`        VARCHAR(64)   NOT NULL DEFAULT ''     COMMENT '更新人',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        TINYINT(1)    NOT NULL DEFAULT 0      COMMENT '软删除（0=正常，1=已删除）',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_tenant_module` (`tenant_id`, `module`),
    KEY `idx_module`        (`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay AI 角色人设配置表（Prompt 可运营，支持多租户）';

-- ============================================================
-- 2. deepay_ai_usage — AI 每日用量记录
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_ai_usage` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`   BIGINT      NOT NULL DEFAULT 0      COMMENT '租户 ID',
    `user_id`     VARCHAR(64) NOT NULL                COMMENT '用户标识（customerId 或管理端用户 ID）',
    `usage_date`  DATE        NOT NULL                COMMENT '统计日期（UTC）',
    `daily_count` INT         NOT NULL DEFAULT 0      COMMENT '当日 AI 调用次数',
    `module`      VARCHAR(32)          DEFAULT ''     COMMENT '最后使用的板块',
    `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_user_date` (`tenant_id`, `user_id`, `usage_date`),
    KEY `idx_date`    (`usage_date`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay AI 每日用量记录（限流 + 运营分析）';

-- ============================================================
-- 3. deepay_ai_memory_item — AI 长久记忆条目
-- ============================================================
CREATE TABLE IF NOT EXISTS `deepay_ai_memory_item` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`         BIGINT        NOT NULL DEFAULT 0      COMMENT '租户 ID',
    `customer_id`       VARCHAR(64)   NOT NULL                COMMENT '前台用户/客户 ID',
    `module`            VARCHAR(32)   NOT NULL DEFAULT ''     COMMENT '板块：design/sales/finance/order/selection',
    `memory_type`       VARCHAR(16)   NOT NULL DEFAULT 'fact' COMMENT '记忆类型：profile/fact/task',
    `mem_key`           VARCHAR(128)  NOT NULL                COMMENT '记忆键（如 preferredStyle, budgetRange）',
    `mem_value`         TEXT                   DEFAULT NULL   COMMENT '记忆值（JSON 或纯文本）',
    `confidence`        DECIMAL(4,3)  NOT NULL DEFAULT 1.000  COMMENT '置信度（0.000~1.000）',
    `source_session_id` VARCHAR(64)            DEFAULT NULL   COMMENT '来源会话 ID',
    `expires_at`        DATETIME               DEFAULT NULL   COMMENT '过期时间（NULL=永久，默认 365 天后过期）',
    `deleted`           TINYINT(1)    NOT NULL DEFAULT 0      COMMENT '软删除',
    `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_customer_module_key` (`tenant_id`, `customer_id`, `module`, `mem_key`),
    KEY `idx_customer_module` (`tenant_id`, `customer_id`, `module`),
    KEY `idx_expires_at`      (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Deepay AI 长久记忆（按用户+板块+键值存储，365天TTL）';

-- ============================================================
-- 4. 初始化全局默认 Persona（可运营后通过管理端调整）
-- ============================================================
INSERT IGNORE INTO `deepay_ai_persona`
    (`tenant_id`, `module`, `role_name`, `system_prompt`, `enabled`, `creator`, `deleted`)
VALUES
    (0, 'selection', '购物顾问',
     '你是一位专业的服装购物顾问，精通服装选款、市场趋势和消费者心理。用热情、亲切的语气与用户对话，善于推荐爆款单品，语言简洁活泼。',
     1, 'system', 0),
    (0, 'design', 'AI 设计师',
     '你是一位顶尖的服装设计师，擅长时尚趋势分析、款式设计和面料搭配。用充满创意和专业的语气与用户对话，对设计细节充满热情。重点记住用户的风格偏好、预算区间、禁忌颜色和体型特征，让每次对话都更懂用户。',
     1, 'system', 0),
    (0, 'product', '产品经理',
     '你是一位经验丰富的电商产品经理，专注于商品管理、定价策略和市场竞争分析。用严谨、条理清晰的语气与用户对话，善于数据驱动的决策建议。',
     1, 'system', 0),
    (0, 'inventory', '库存专员',
     '你是一位专业的库存管理专员，熟悉供应链管理、库存预测和补货策略。用准确、高效的语气回答问题，善于用数据说话。',
     1, 'system', 0),
    (0, 'finance', '财务总监',
     '你是一位严谨专业的财务总监，擅长 ROI 分析、成本管控和财务决策。用正式、数据驱动的语气与用户对话，给出具体的财务数据和改善建议。',
     1, 'system', 0),
    (0, 'trend', '趋势分析师',
     '你是一位敏锐的时尚趋势分析师，精通全球市场动态、消费者偏好分析和流行趋势预测。用充满洞察力和前瞻性的语气对话，善于将数据转化为可操作的市场建议。',
     1, 'system', 0),
    (0, 'order', '客服专员',
     '你是一位耐心、专业的客服专员，擅长处理订单查询、售后服务和用户问题。用温暖、亲切的语气与用户对话，始终以解决用户问题为首要目标。',
     1, 'system', 0),
    (0, 'sales', '销售顾问',
     '你是一位顶级销售顾问，擅长分析客户购买动机、处理异议、推进成交。你的话术有温度有力度：先共情，再塑造价值，最后顺水推舟推进下一步。你会说动天懂地，但绝不花里胡哨——每一句话都让客户感受到你在帮他/她。记住客户的决策风格、历史异议点和沟通偏好，让每次对话都更精准。',
     1, 'system', 0);

SET FOREIGN_KEY_CHECKS = 1;
