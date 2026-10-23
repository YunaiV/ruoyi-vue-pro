-- ----------------------------
-- Table structure for ai_model_call_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_model_call_log`;
CREATE TABLE `ai_model_call_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户编号',
  `platform` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '平台标识，例如 OPENAI/DEEP_SEEK',
  `model_id` bigint NULL DEFAULT NULL COMMENT '模型编号，关联 ai_model.id，可为空',
  `model` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型标识，冗余自 ai_model.model',
  `api_key_id` bigint NULL DEFAULT NULL COMMENT 'API 密钥编号，关联 ai_api_key.id，可为空',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务类型：CHAT_MESSAGE/WRITE/MIND_MAP/KNOWLEDGE_EMBED/KNOWLEDGE_RERANK/WORKFLOW_RUN 等',
  `biz_id` bigint NOT NULL COMMENT '业务主键，例如 ai_chat_message.id/ai_write.id 等',
  `conversation_id` bigint NULL DEFAULT NULL COMMENT '会话编号，聊天场景填 ai_chat_conversation.id',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '链路追踪编号',
  `request_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '请求时间',
  `response_time` datetime NULL DEFAULT NULL COMMENT '响应时间',
  `duration_ms` int NULL DEFAULT NULL COMMENT '耗时，单位：毫秒',
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态：SUCCESS/FAIL/CANCEL',
  `error_message` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '错误信息',
  `request_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '厂商请求编号，用于对账',
  `prompt_tokens` int NULL DEFAULT NULL COMMENT '输入 Token 数（总量，含缓存命中部分）',
  `completion_tokens` int NULL DEFAULT NULL COMMENT '输出 Token 数（总量，含推理/思考部分）',
  `total_tokens` int NULL DEFAULT NULL COMMENT '总 Token 数',
  `cached_tokens` int NULL DEFAULT NULL COMMENT '缓存命中 Token 数（输入中命中上下文缓存的部分，厂商返回时记录）',
  `reasoning_tokens` int NULL DEFAULT NULL COMMENT '推理/思考 Token 数（输出中用于推理的部分，如 DeepSeek-R1/OpenAI o1 等思考模型）',
  `token_source` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Token 来源：PROVIDER/ESTIMATED/NONE',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '币种，首期固定为 CNY',
  `price_in_per_1m` bigint NOT NULL DEFAULT 0 COMMENT '输入单价快照：微元/100万 tokens（缓存未命中）',
  `price_cached_per_1m` bigint NOT NULL DEFAULT 0 COMMENT '缓存命中输入单价快照：微元/100万 tokens，0 表示不区分',
  `price_out_per_1m` bigint NOT NULL DEFAULT 0 COMMENT '输出单价快照：微元/100万 tokens（标准输出）',
  `price_reasoning_per_1m` bigint NOT NULL DEFAULT 0 COMMENT '推理/思考输出单价快照：微元/100万 tokens，0 表示不区分',
  `cost_amount` bigint NOT NULL DEFAULT 0 COMMENT '本次调用费用，单位：微元（1元=1,000,000微元）',
  `blocked` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否被预算拦截：0 否；1 是',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_model_call_tenant_req_time` (`tenant_id`, `request_time`) USING BTREE,
  KEY `idx_model_call_tenant_user_time` (`tenant_id`, `user_id`, `request_time`) USING BTREE,
  KEY `idx_model_call_tenant_model_time` (`tenant_id`, `model_id`, `request_time`) USING BTREE,
  KEY `idx_model_call_tenant_biz` (`tenant_id`, `biz_type`, `biz_id`) USING BTREE,
  KEY `idx_model_call_tenant_conv_time` (`tenant_id`, `conversation_id`, `request_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI 模型调用日志表';

-- ----------------------------
-- Table structure for ai_model_pricing
-- ----------------------------
DROP TABLE IF EXISTS `ai_model_pricing`;
CREATE TABLE `ai_model_pricing` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `model_id` bigint NOT NULL COMMENT '模型编号，关联 ai_model.id',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '币种，首期固定 CNY',
  `price_in_per_1m` bigint NOT NULL DEFAULT 0 COMMENT '输入单价：微元/100万 tokens（缓存未命中）',
  `price_cached_per_1m` bigint NOT NULL DEFAULT 0 COMMENT '缓存命中输入单价：微元/100万 tokens，0 表示与输入同价（不区分）',
  `price_out_per_1m` bigint NOT NULL DEFAULT 0 COMMENT '输出单价：微元/100万 tokens（标准输出）',
  `price_reasoning_per_1m` bigint NOT NULL DEFAULT 0 COMMENT '推理/思考输出单价：微元/100万 tokens，0 表示与输出同价（不区分）',
  `strategy_type` varchar(32) NOT NULL DEFAULT 'DEFAULT' COMMENT '计费策略类型',
  `strategy_config` text NULL COMMENT '策略扩展配置（JSON）',
  `status` tinyint NOT NULL COMMENT '状态',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_model_pricing_tenant_model_status` (`tenant_id`, `model_id`, `status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI 模型计费配置表';

-- ----------------------------
-- Table structure for ai_budget_config
-- ----------------------------
DROP TABLE IF EXISTS `ai_budget_config`;
CREATE TABLE `ai_budget_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` bigint NOT NULL DEFAULT 0 COMMENT '用户编号，0 表示租户级预算',
  `period_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '周期类型：MONTHLY/DAILY',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '币种，首期固定 CNY',
  `budget_amount` bigint NOT NULL DEFAULT 0 COMMENT '预算金额，单位：微元',
  `alert_thresholds` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '告警阈值配置，例如 JSON [80,90,100]',
  `status` tinyint NOT NULL COMMENT '状态',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_budget_cfg_tenant_user_period` (`tenant_id`, `user_id`, `period_type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI 预算配置表';

-- ----------------------------
-- Table structure for ai_budget_usage
-- ----------------------------
DROP TABLE IF EXISTS `ai_budget_usage`;
CREATE TABLE `ai_budget_usage` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` bigint NOT NULL DEFAULT 0 COMMENT '用户编号，0 表示租户级汇总用量',
  `period_start_time` datetime NOT NULL COMMENT '周期开始时间，例如当月 1 号 00:00',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '币种，首期固定 CNY',
  `used_amount` bigint NOT NULL DEFAULT 0 COMMENT '已用金额，单位：微元',
  `version` int NOT NULL DEFAULT 0 COMMENT '版本号，用于乐观锁/并发控制',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_budget_usage_tenant_user_period` (`tenant_id`, `user_id`, `period_start_time`, `currency`) USING BTREE,
  UNIQUE KEY `uk_user_period_tenant` (`user_id`, `period_start_time`, `tenant_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI 预算用量表';

-- ----------------------------
-- Table structure for ai_budget_log
-- ----------------------------
DROP TABLE IF EXISTS `ai_budget_log`;
CREATE TABLE `ai_budget_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` bigint NOT NULL DEFAULT 0 COMMENT '用户编号，0 表示租户级事件',
  `event_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '事件类型：THRESHOLD_ALERT/OVER_LIMIT_BLOCK/MANUAL_ADJUST 等',
  `period_start_time` datetime NOT NULL COMMENT '周期开始时间',
  `currency` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CNY' COMMENT '币种，首期固定 CNY',
  `budget_amount` bigint NOT NULL DEFAULT 0 COMMENT '该周期预算金额，单位：微元',
  `used_amount` bigint NOT NULL DEFAULT 0 COMMENT '事件发生时的已用金额，单位：微元',
  `delta_amount` bigint NULL DEFAULT NULL COMMENT '本次事件涉及的金额变化，单位：微元，可为空',
  `request_biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '触发事件的业务类型，例如 CHAT_MESSAGE',
  `request_biz_id` bigint NULL DEFAULT NULL COMMENT '触发事件的业务主键，例如 ai_model_call_log.id',
  `message` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述信息，例如“达到80%阈值触发告警”',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_budget_log_tenant_user_time` (`tenant_id`, `user_id`, `period_start_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI 预算事件日志表';

-- ----------------------------
-- 菜单 SQL（system_menu）
-- 父菜单：AI 大模型（id=2758），控制台（id=2760）
-- ID 从 5050 开始，避免与现有菜单冲突
-- ----------------------------

-- =============================================
-- 一级目录：AI 计费管理（挂在 AI 大模型下）
-- =============================================
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (5050, '计费管理', '', 1, 200, 2758, 'billing', 'ep:money', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- =============================================
-- 模型计费配置
-- =============================================
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (5051, '模型计费配置', '', 2, 1, 5050, 'model-pricing', 'ep:price-tag', 'ai/billing/modelPricing/index', 'AiModelPricing', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (5052, '计费配置查询', 'ai:model-pricing:query', 3, 1, 5051, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (5053, '计费配置创建', 'ai:model-pricing:create', 3, 2, 5051, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (5054, '计费配置更新', 'ai:model-pricing:update', 3, 3, 5051, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (5055, '计费配置删除', 'ai:model-pricing:delete', 3, 4, 5051, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- =============================================
-- 调用日志
-- =============================================
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (5056, '调用日志', '', 2, 2, 5050, 'model-call-log', 'ep:document', 'ai/billing/modelCallLog/index', 'AiModelCallLog', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (5057, '调用日志查询', 'ai:model-call-log:query', 3, 1, 5056, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (5058, '调用日志导出', 'ai:model-call-log:export', 3, 5, 5056, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- =============================================
-- 预算配置
-- =============================================
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (5059, '预算配置', '', 2, 3, 5050, 'budget-config', 'ep:wallet', 'ai/billing/budgetConfig/index', 'AiBudgetConfig', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (5060, '预算配置查询', 'ai:budget-config:query', 3, 1, 5059, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (5061, '预算配置创建', 'ai:budget-config:create', 3, 2, 5059, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (5062, '预算配置更新', 'ai:budget-config:update', 3, 3, 5059, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (5063, '预算配置删除', 'ai:budget-config:delete', 3, 4, 5059, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- =============================================
-- 预算使用情况
-- =============================================
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (5064, '预算使用情况', '', 2, 4, 5050, 'budget-usage', 'ep:data-analysis', 'ai/billing/budgetUsage/index', 'AiBudgetUsage', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (5065, '预算使用查询', 'ai:budget-usage:query', 3, 1, 5064, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');

-- =============================================
-- 预算事件日志
-- =============================================
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (5066, '预算事件日志', '', 2, 5, 5050, 'budget-log', 'ep:bell', 'ai/billing/budgetLog/index', 'AiBudgetLog', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`)
VALUES (5067, '预算事件查询', 'ai:budget-log:query', 3, 1, 5066, '', '', '', '', 0, b'1', b'1', b'1', '1', NOW(), '1', NOW(), b'0');