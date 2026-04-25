-- =====================================================================
-- AI Copilot MVP — 数据库迁移脚本
-- 功能：Persona 配置入库、AI 使用量日志
-- 兼容：MySQL 5.7 / 8.0，幂等安全（CREATE TABLE IF NOT EXISTS）
-- =====================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. ai_persona — Prompt/角色人设配置表（可运营）
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_persona` (
    `id`            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`     BIGINT         NOT NULL DEFAULT 0      COMMENT '租户 ID（0=全局默认，预留多租户）',
    `module`        VARCHAR(64)    NOT NULL DEFAULT ''      COMMENT '模块名（selection/design/product/inventory/finance/trend/order/global）',
    `role_name`     VARCHAR(128)   NOT NULL DEFAULT ''      COMMENT '角色显示名称（前端头像标题）',
    `system_prompt` TEXT           NOT NULL                 COMMENT 'System Prompt（角色指令）',
    `examples`      TEXT                    DEFAULT NULL    COMMENT '示例对话（JSON 数组，few-shot）',
    `tool_whitelist` VARCHAR(1024) DEFAULT NULL             COMMENT '允许调用的工具列表（逗号分隔，预留）',
    `enabled`       TINYINT        NOT NULL DEFAULT 1       COMMENT '是否启用：1=启用，0=禁用',
    `sort_order`    INT            NOT NULL DEFAULT 0       COMMENT '同 module 下排序（数字越小优先级越高）',
    `created_at`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_tenant_module` (`tenant_id`, `module`) COMMENT '按租户+模块查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='AI 角色人设配置表（Prompt 可运营）';

-- 插入默认角色（全局，tenant_id=0）
INSERT IGNORE INTO `ai_persona`
    (`tenant_id`, `module`, `role_name`, `system_prompt`, `enabled`, `sort_order`)
VALUES
(0, 'selection', '选款顾问',
 '你是一位专业的服装购物顾问，精通服装选款、市场趋势和消费者心理。用热情、亲切的语气与用户对话，善于推荐爆款单品，语言简洁活泼。',
 1, 0),
(0, 'design', '设计师',
 '你是一位顶尖的服装设计师，擅长时尚趋势分析、款式设计和面料搭配。用充满创意和专业的语气与用户对话，对设计细节充满热情，善于用生动的语言描述设计方案。',
 1, 0),
(0, 'product', '商品经理',
 '你是一位经验丰富的电商产品经理，专注于商品管理、定价策略和市场竞争分析。用严谨、条理清晰的语气与用户对话，善于数据驱动的决策建议。',
 1, 0),
(0, 'inventory', '库存专员',
 '你是一位专业的库存管理专员，熟悉供应链管理、库存预测和补货策略。用准确、高效的语气回答问题，善于用数据说话，帮助用户优化库存管理。',
 1, 0),
(0, 'finance', '财务总监',
 '你是一位严谨专业的财务总监，擅长 ROI 分析、成本管控和财务决策。用正式、数据驱动的语气与用户对话，给出具体的财务数据和改善建议。',
 1, 0),
(0, 'trend', '趋势分析师',
 '你是一位敏锐的时尚趋势分析师，精通全球市场动态、消费者偏好分析和流行趋势预测。用充满洞察力和前瞻性的语气对话，善于将数据转化为可操作的市场建议。',
 1, 0),
(0, 'order', '客服专员',
 '你是一位耐心、专业的客服专员，擅长处理订单查询、售后服务和用户问题。用温暖、亲切的语气与用户对话，始终以解决用户问题为首要目标。',
 1, 0),
(0, 'global', 'AI 助手',
 '你是 Deepay 智能 AI 助手，可以帮助用户处理选款、设计、商品管理、库存、财务、趋势分析和订单等各类问题。根据用户描述的场景，主动推断意图并给出专业建议，语言简洁友善。',
 1, 0);

-- ============================================================
-- 2. ai_usage_log — AI 调用用量日志表
-- ============================================================
CREATE TABLE IF NOT EXISTS `ai_usage_log` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `tenant_id`   BIGINT        NOT NULL DEFAULT 0     COMMENT '租户 ID（预留多租户）',
    `user_id`     VARCHAR(64)   NOT NULL DEFAULT ''    COMMENT '用户 ID（空字符串表示匿名）',
    `module`      VARCHAR(64)   NOT NULL DEFAULT ''    COMMENT '模块名',
    `session_id`  VARCHAR(128)           DEFAULT NULL  COMMENT '会话 ID',
    `entity_type` VARCHAR(64)            DEFAULT NULL  COMMENT '上下文实体类型（order/product/customer 等）',
    `entity_id`   VARCHAR(64)            DEFAULT NULL  COMMENT '上下文实体 ID',
    `tokens_est`  INT                    DEFAULT NULL  COMMENT '预估 token 数（请求+回复合计，可选）',
    `status`      VARCHAR(32)   NOT NULL DEFAULT 'OK'  COMMENT '状态：OK / RATE_LIMITED / QUOTA_EXCEEDED / ERROR',
    `error_msg`   VARCHAR(512)           DEFAULT NULL  COMMENT '错误信息（status=ERROR 时）',
    `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    PRIMARY KEY (`id`) USING BTREE,
    KEY `idx_user_date`    (`user_id`, `created_at`) COMMENT '按用户+日期查询用量',
    KEY `idx_tenant_date`  (`tenant_id`, `created_at`) COMMENT '按租户+日期统计'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='AI 调用用量日志（防滥用、计费依据）';

SET FOREIGN_KEY_CHECKS = 1;
