/*
 ruoyi-vue-pro - AI 模块数据库增量升级脚本

 Project: yudao-module-ai
 Database: MySQL 5.7 / 8.0+
 Version: 2025.10-SNAPSHOT
 Date: 2025-03-15

 说明:
   本脚本用于已部署环境的增量升级,添加缺失的表和字段
   - 新增 ai_workflow 表
   - ai_chat_message 表添加 3 个字段
   - ai_chat_role 表添加 3 个字段
   - ai_music 表添加 2 个字段

 使用方法:
   mysql -u root -p ruoyi-vue-pro < ai-upgrade-2025-03-15.sql

 注意事项:
   1. 请在执行前备份数据库
   2. 确认当前环境已有 AI 模块的基础表
   3. 脚本幂等性设计,可重复执行
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. 新增 ai_workflow 表(AI 工作流表)
-- ============================================================

CREATE TABLE IF NOT EXISTS `ai_workflow` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工作流名称',
  `code` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '工作流标识',
  `graph` text COLLATE utf8mb4_unicode_ci COMMENT '工作流模型 JSON 数据',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `status` int NOT NULL DEFAULT '0' COMMENT '状态(0-开启 1-关闭)',
  `creator` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`,`deleted`) USING BTREE COMMENT '工作流标识唯一索引',
  KEY `idx_name` (`name`) USING BTREE COMMENT '工作流名称索引',
  KEY `idx_status` (`status`) USING BTREE COMMENT '状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 工作流表';

-- ============================================================
-- 2. ai_chat_message 表添加字段
-- ============================================================

-- 添加推理内容字段
SET @col_exists = (SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_chat_message'
      AND COLUMN_NAME = 'reasoning_content');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `ai_chat_message` ADD COLUMN `reasoning_content` text COLLATE utf8mb4_unicode_ci COMMENT ''推理内容(用于存储模型的推理过程)'' AFTER `content`',
    'SELECT ''Column reasoning_content already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加联网搜索字段
SET @col_exists = (SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_chat_message'
      AND COLUMN_NAME = 'web_search_pages');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `ai_chat_message` ADD COLUMN `web_search_pages` varchar(8192) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT ''联网搜索的网页内容数组(JSON 格式)'' AFTER `segment_ids`',
    'SELECT ''Column web_search_pages already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加附件URL字段
SET @col_exists = (SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_chat_message'
      AND COLUMN_NAME = 'attachment_urls');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `ai_chat_message` ADD COLUMN `attachment_urls` varchar(2048) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT ''附件 URL 数组(逗号分隔)'' AFTER `web_search_pages`',
    'SELECT ''Column attachment_urls already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 3. ai_chat_role 表添加字段
-- ============================================================

-- 添加知识库关联字段
SET @col_exists = (SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_chat_role'
      AND COLUMN_NAME = 'knowledge_ids');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `ai_chat_role` ADD COLUMN `knowledge_ids` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT ''引用的知识库编号列表(逗号分隔)'' AFTER `model_id`',
    'SELECT ''Column knowledge_ids already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加工具关联字段
SET @col_exists = (SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_chat_role'
      AND COLUMN_NAME = 'tool_ids');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `ai_chat_role` ADD COLUMN `tool_ids` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT ''引用的工具编号列表(逗号分隔)'' AFTER `knowledge_ids`',
    'SELECT ''Column tool_ids already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加MCP客户端关联字段
SET @col_exists = (SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_chat_role'
      AND COLUMN_NAME = 'mcp_client_names');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `ai_chat_role` ADD COLUMN `mcp_client_names` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT ''引用的 MCP Client 名字列表(逗号分隔)'' AFTER `tool_ids`',
    'SELECT ''Column mcp_client_names already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 4. ai_music 表添加字段
-- ============================================================

-- 添加音乐标签字段
SET @col_exists = (SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_music'
      AND COLUMN_NAME = 'tags');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `ai_music` ADD COLUMN `tags` varchar(1024) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT ''音乐风格标签(JSON 数组格式)'' AFTER `model`',
    'SELECT ''Column tags already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加音乐时长字段
SET @col_exists = (SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_music'
      AND COLUMN_NAME = 'duration');

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE `ai_music` ADD COLUMN `duration` double DEFAULT NULL COMMENT ''音乐时长(秒)'' AFTER `tags`',
    'SELECT ''Column duration already exists'' AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ============================================================
-- 升级完成
-- ============================================================

SET FOREIGN_KEY_CHECKS = 1;

-- 验证升级结果
SELECT 'AI 模块数据库增量升级完成!' AS message;
SELECT TABLE_NAME, TABLE_COMMENT
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME LIKE 'ai_%'
ORDER BY TABLE_NAME;
