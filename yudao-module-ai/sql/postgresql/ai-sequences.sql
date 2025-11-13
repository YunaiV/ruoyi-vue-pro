/*
 ruoyi-vue-pro - AI 模块 PostgreSQL 序列定义

 Project: yudao-module-ai
 Database: PostgreSQL
 Version: 2025.10-SNAPSHOT
 Date: 2025-03-15

 说明:
   本脚本用于 PostgreSQL 数据库,定义所有 AI 模块表的序列(Sequence)
   必须在创建表之前执行此脚本

 使用方法:
   psql -U postgres -d ruoyi-vue-pro -f ai-sequences.sql
*/

-- ============================================================
-- AI 模块序列定义
-- ============================================================

-- 1. API 密钥表序列
CREATE SEQUENCE IF NOT EXISTS ai_api_key_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 2. 模型表序列
CREATE SEQUENCE IF NOT EXISTS ai_model_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 3. 工具表序列
CREATE SEQUENCE IF NOT EXISTS ai_tool_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 4. 工作流表序列
CREATE SEQUENCE IF NOT EXISTS ai_workflow_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 5. 聊天角色表序列
CREATE SEQUENCE IF NOT EXISTS ai_chat_role_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 6. 知识库表序列
CREATE SEQUENCE IF NOT EXISTS ai_knowledge_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 7. 对话表序列
CREATE SEQUENCE IF NOT EXISTS ai_chat_conversation_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 8. 知识库文档表序列
CREATE SEQUENCE IF NOT EXISTS ai_knowledge_document_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 9. 聊天消息表序列
CREATE SEQUENCE IF NOT EXISTS ai_chat_message_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 10. 文档分段表序列
CREATE SEQUENCE IF NOT EXISTS ai_knowledge_segment_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 11. 绘画表序列
CREATE SEQUENCE IF NOT EXISTS ai_image_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 12. 音乐表序列
CREATE SEQUENCE IF NOT EXISTS ai_music_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 13. 写作表序列
CREATE SEQUENCE IF NOT EXISTS ai_write_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- 14. 思维导图表序列
CREATE SEQUENCE IF NOT EXISTS ai_mind_map_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- ============================================================
-- 验证序列创建
-- ============================================================

SELECT
    sequence_name,
    start_value,
    increment_by
FROM information_schema.sequences
WHERE sequence_schema = 'public'
  AND sequence_name LIKE 'ai_%_seq'
ORDER BY sequence_name;

-- 输出完成信息
SELECT 'AI 模块序列创建完成! 共创建 14 个序列。' AS message;
