/*
 * 芋道 AI 模块 - PostgreSQL 数据库升级脚本
 *
 * Project: ruoyi-vue-pro
 * Module: yudao-module-ai
 * Database: PostgreSQL 12+
 * Version: 2025.12
 * Date: 2025-12-01
 *
 * 修复内容:
 * 移除 AI 模块所有表的 tenant_id 字段
 * 原因：AI 模块实体类继承自 BaseDO，不支持多租户，SQL 中的 tenant_id 字段与代码不一致
 *
 * 影响的表:
 * - ai_api_key
 * - ai_model
 * - ai_tool
 * - ai_workflow
 * - ai_chat_role
 * - ai_knowledge
 * - ai_chat_conversation
 * - ai_knowledge_document
 * - ai_chat_message
 * - ai_knowledge_segment
 * - ai_image
 * - ai_music
 * - ai_write
 * - ai_mind_map
 */

-- ----------------------------
-- 移除 ai_api_key 表的 tenant_id 字段
-- ----------------------------
ALTER TABLE ai_api_key DROP COLUMN IF EXISTS tenant_id;

-- ----------------------------
-- 移除 ai_model 表的 tenant_id 字段
-- ----------------------------
ALTER TABLE ai_model DROP COLUMN IF EXISTS tenant_id;

-- ----------------------------
-- 移除 ai_tool 表的 tenant_id 字段
-- ----------------------------
ALTER TABLE ai_tool DROP COLUMN IF EXISTS tenant_id;

-- ----------------------------
-- 移除 ai_workflow 表的 tenant_id 字段
-- ----------------------------
ALTER TABLE ai_workflow DROP COLUMN IF EXISTS tenant_id;

-- ----------------------------
-- 移除 ai_chat_role 表的 tenant_id 字段
-- ----------------------------
ALTER TABLE ai_chat_role DROP COLUMN IF EXISTS tenant_id;

-- ----------------------------
-- 移除 ai_knowledge 表的 tenant_id 字段
-- ----------------------------
ALTER TABLE ai_knowledge DROP COLUMN IF EXISTS tenant_id;

-- ----------------------------
-- 移除 ai_chat_conversation 表的 tenant_id 字段
-- ----------------------------
ALTER TABLE ai_chat_conversation DROP COLUMN IF EXISTS tenant_id;

-- ----------------------------
-- 移除 ai_knowledge_document 表的 tenant_id 字段
-- ----------------------------
ALTER TABLE ai_knowledge_document DROP COLUMN IF EXISTS tenant_id;

-- ----------------------------
-- 移除 ai_chat_message 表的 tenant_id 字段
-- ----------------------------
ALTER TABLE ai_chat_message DROP COLUMN IF EXISTS tenant_id;

-- ----------------------------
-- 移除 ai_knowledge_segment 表的 tenant_id 字段
-- ----------------------------
ALTER TABLE ai_knowledge_segment DROP COLUMN IF EXISTS tenant_id;

-- ----------------------------
-- 移除 ai_image 表的 tenant_id 字段
-- ----------------------------
ALTER TABLE ai_image DROP COLUMN IF EXISTS tenant_id;

-- ----------------------------
-- 移除 ai_music 表的 tenant_id 字段
-- ----------------------------
ALTER TABLE ai_music DROP COLUMN IF EXISTS tenant_id;

-- ----------------------------
-- 移除 ai_write 表的 tenant_id 字段
-- ----------------------------
ALTER TABLE ai_write DROP COLUMN IF EXISTS tenant_id;

-- ----------------------------
-- 移除 ai_mind_map 表的 tenant_id 字段
-- ----------------------------
ALTER TABLE ai_mind_map DROP COLUMN IF EXISTS tenant_id;

SELECT 'AI 模块 PostgreSQL tenant_id 字段移除完成！' AS message;
