/*
 * 芋道 AI 模块 - PostgreSQL 数据库初始化脚本
 *
 * Project: ruoyi-vue-pro
 * Module: yudao-module-ai
 * Database: PostgreSQL 12+
 * Version: 2025.10-SNAPSHOT
 * Date: 2025-03-15
 *
 * 修复内容:
 * 1. 新增 ai_workflow 表(工作流表)
 * 2. ai_chat_message 表新增字段: reasoning_content, web_search_pages, attachment_urls
 * 3. ai_chat_role 表新增字段: knowledge_ids, tool_ids, mcp_client_names
 * 4. ai_music 表新增字段: tags, duration
 * 5. 数据类型转换为 PostgreSQL 标准类型
 * 6. 规范所有表的索引和字段注释
 *
 * 注意事项:
 * 1. 必须先执行序列创建脚本: postgresql/ai-sequences.sql
 * 2. PostgreSQL 默认使用 UTF-8 字符集,无需额外配置
 * 3. 注释使用 COMMENT ON 语法
 */

-- ============================================================
-- Table: ai_api_key - AI API 密钥表
-- ============================================================

DROP TABLE IF EXISTS ai_api_key CASCADE;

CREATE TABLE ai_api_key (
  id BIGINT NOT NULL DEFAULT nextval('ai_api_key_seq'),
  name VARCHAR(255) NOT NULL,
  api_key VARCHAR(1024) NOT NULL,
  platform VARCHAR(255) NOT NULL,
  url VARCHAR(255) NULL DEFAULT NULL,
  status INT NOT NULL,
  creator VARCHAR(64) NULL DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) NULL DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE INDEX idx_api_key_platform ON ai_api_key (platform);
CREATE INDEX idx_api_key_status ON ai_api_key (status);

COMMENT ON TABLE ai_api_key IS 'AI API 密钥表';
COMMENT ON COLUMN ai_api_key.id IS '编号';
COMMENT ON COLUMN ai_api_key.name IS '名称';
COMMENT ON COLUMN ai_api_key.api_key IS '密钥';
COMMENT ON COLUMN ai_api_key.platform IS '平台';
COMMENT ON COLUMN ai_api_key.url IS '自定义 API 地址';
COMMENT ON COLUMN ai_api_key.status IS '状态';
COMMENT ON COLUMN ai_api_key.creator IS '创建者';
COMMENT ON COLUMN ai_api_key.create_time IS '创建时间';
COMMENT ON COLUMN ai_api_key.updater IS '更新者';
COMMENT ON COLUMN ai_api_key.update_time IS '更新时间';
COMMENT ON COLUMN ai_api_key.deleted IS '是否删除';
COMMENT ON COLUMN ai_api_key.tenant_id IS '租户编号';

-- ============================================================
-- Table: ai_model - AI 模型表
-- ============================================================

DROP TABLE IF EXISTS ai_model CASCADE;

CREATE TABLE ai_model (
  id BIGINT NOT NULL DEFAULT nextval('ai_model_seq'),
  key_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  model VARCHAR(255) NOT NULL,
  platform VARCHAR(255) NOT NULL,
  type INT NOT NULL,
  sort INT NOT NULL,
  status INT NOT NULL,
  temperature DOUBLE PRECISION NULL DEFAULT NULL,
  max_tokens INT NULL DEFAULT NULL,
  max_contexts INT NULL DEFAULT NULL,
  creator VARCHAR(64) NULL DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) NULL DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE INDEX idx_model_key_id ON ai_model (key_id);
CREATE INDEX idx_model_platform ON ai_model (platform);
CREATE INDEX idx_model_type ON ai_model (type);
CREATE INDEX idx_model_status_sort ON ai_model (status, sort);

COMMENT ON TABLE ai_model IS 'AI 模型表';
COMMENT ON COLUMN ai_model.id IS '编号';
COMMENT ON COLUMN ai_model.key_id IS 'API 秘钥编号';
COMMENT ON COLUMN ai_model.name IS '模型名称';
COMMENT ON COLUMN ai_model.model IS '模型标志';
COMMENT ON COLUMN ai_model.platform IS '平台';
COMMENT ON COLUMN ai_model.type IS '类型';
COMMENT ON COLUMN ai_model.sort IS '排序值';
COMMENT ON COLUMN ai_model.status IS '状态';
COMMENT ON COLUMN ai_model.temperature IS '温度参数';
COMMENT ON COLUMN ai_model.max_tokens IS '单条回复的最大 Token 数量';
COMMENT ON COLUMN ai_model.max_contexts IS '上下文的最大 Message 数量';
COMMENT ON COLUMN ai_model.creator IS '创建者';
COMMENT ON COLUMN ai_model.create_time IS '创建时间';
COMMENT ON COLUMN ai_model.updater IS '更新者';
COMMENT ON COLUMN ai_model.update_time IS '更新时间';
COMMENT ON COLUMN ai_model.deleted IS '是否删除';
COMMENT ON COLUMN ai_model.tenant_id IS '租户编号';

-- ============================================================
-- Table: ai_tool - AI 工具表
-- ============================================================

DROP TABLE IF EXISTS ai_tool CASCADE;

CREATE TABLE ai_tool (
  id BIGINT NOT NULL DEFAULT nextval('ai_tool_seq'),
  name VARCHAR(255) NOT NULL,
  description VARCHAR(1024) NULL DEFAULT NULL,
  status INT NOT NULL,
  creator VARCHAR(64) NULL DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) NULL DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE INDEX idx_tool_name ON ai_tool (name);
CREATE INDEX idx_tool_status ON ai_tool (status);

COMMENT ON TABLE ai_tool IS 'AI 工具表';
COMMENT ON COLUMN ai_tool.id IS '工具编号';
COMMENT ON COLUMN ai_tool.name IS '工具名称';
COMMENT ON COLUMN ai_tool.description IS '工具描述';
COMMENT ON COLUMN ai_tool.status IS '状态';
COMMENT ON COLUMN ai_tool.creator IS '创建者';
COMMENT ON COLUMN ai_tool.create_time IS '创建时间';
COMMENT ON COLUMN ai_tool.updater IS '更新者';
COMMENT ON COLUMN ai_tool.update_time IS '更新时间';
COMMENT ON COLUMN ai_tool.deleted IS '是否删除';
COMMENT ON COLUMN ai_tool.tenant_id IS '租户编号';

-- ============================================================
-- Table: ai_workflow - AI 工作流表
-- ============================================================

DROP TABLE IF EXISTS ai_workflow CASCADE;

CREATE TABLE ai_workflow (
  id BIGINT NOT NULL DEFAULT nextval('ai_workflow_seq'),
  name VARCHAR(255) NOT NULL,
  code VARCHAR(255) NOT NULL,
  graph TEXT NULL,
  remark VARCHAR(1024) NULL DEFAULT NULL,
  status INT NOT NULL,
  creator VARCHAR(64) NULL DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) NULL DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uk_workflow_code ON ai_workflow (code, deleted);
CREATE INDEX idx_workflow_name ON ai_workflow (name);
CREATE INDEX idx_workflow_status ON ai_workflow (status);

COMMENT ON TABLE ai_workflow IS 'AI 工作流表';
COMMENT ON COLUMN ai_workflow.id IS '编号';
COMMENT ON COLUMN ai_workflow.name IS '工作流名称';
COMMENT ON COLUMN ai_workflow.code IS '工作流标识';
COMMENT ON COLUMN ai_workflow.graph IS '工作流模型 JSON 数据';
COMMENT ON COLUMN ai_workflow.remark IS '备注';
COMMENT ON COLUMN ai_workflow.status IS '状态';
COMMENT ON COLUMN ai_workflow.creator IS '创建者';
COMMENT ON COLUMN ai_workflow.create_time IS '创建时间';
COMMENT ON COLUMN ai_workflow.updater IS '更新者';
COMMENT ON COLUMN ai_workflow.update_time IS '更新时间';
COMMENT ON COLUMN ai_workflow.deleted IS '是否删除';
COMMENT ON COLUMN ai_workflow.tenant_id IS '租户编号';

-- ============================================================
-- Table: ai_chat_role - AI 聊天角色表
-- ============================================================

DROP TABLE IF EXISTS ai_chat_role CASCADE;

CREATE TABLE ai_chat_role (
  id BIGINT NOT NULL DEFAULT nextval('ai_chat_role_seq'),
  name VARCHAR(255) NOT NULL,
  avatar VARCHAR(512) NULL DEFAULT NULL,
  category VARCHAR(64) NULL DEFAULT NULL,
  description VARCHAR(1024) NULL DEFAULT NULL,
  system_message VARCHAR(4096) NULL DEFAULT NULL,
  user_id BIGINT NULL DEFAULT NULL,
  model_id BIGINT NULL DEFAULT NULL,
  knowledge_ids VARCHAR(1024) NULL DEFAULT NULL,
  tool_ids VARCHAR(1024) NULL DEFAULT NULL,
  mcp_client_names VARCHAR(1024) NULL DEFAULT NULL,
  public_status BOOLEAN NOT NULL DEFAULT FALSE,
  sort INT NOT NULL DEFAULT 0,
  status INT NOT NULL,
  creator VARCHAR(64) NULL DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) NULL DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE INDEX idx_chat_role_user_id ON ai_chat_role (user_id);
CREATE INDEX idx_chat_role_model_id ON ai_chat_role (model_id);
CREATE INDEX idx_chat_role_category ON ai_chat_role (category);
CREATE INDEX idx_chat_role_public_status ON ai_chat_role (public_status);
CREATE INDEX idx_chat_role_status_sort ON ai_chat_role (status, sort);

COMMENT ON TABLE ai_chat_role IS 'AI 聊天角色表';
COMMENT ON COLUMN ai_chat_role.id IS '编号';
COMMENT ON COLUMN ai_chat_role.name IS '角色名称';
COMMENT ON COLUMN ai_chat_role.avatar IS '角色头像';
COMMENT ON COLUMN ai_chat_role.category IS '角色分类';
COMMENT ON COLUMN ai_chat_role.description IS '角色描述';
COMMENT ON COLUMN ai_chat_role.system_message IS '角色设定';
COMMENT ON COLUMN ai_chat_role.user_id IS '用户编号';
COMMENT ON COLUMN ai_chat_role.model_id IS '模型编号';
COMMENT ON COLUMN ai_chat_role.knowledge_ids IS '引用的知识库编号列表';
COMMENT ON COLUMN ai_chat_role.tool_ids IS '引用的工具编号列表';
COMMENT ON COLUMN ai_chat_role.mcp_client_names IS '引用的 MCP Client 名字列表';
COMMENT ON COLUMN ai_chat_role.public_status IS '是否公开';
COMMENT ON COLUMN ai_chat_role.sort IS '排序值';
COMMENT ON COLUMN ai_chat_role.status IS '状态';
COMMENT ON COLUMN ai_chat_role.creator IS '创建者';
COMMENT ON COLUMN ai_chat_role.create_time IS '创建时间';
COMMENT ON COLUMN ai_chat_role.updater IS '更新者';
COMMENT ON COLUMN ai_chat_role.update_time IS '更新时间';
COMMENT ON COLUMN ai_chat_role.deleted IS '是否删除';
COMMENT ON COLUMN ai_chat_role.tenant_id IS '租户编号';

-- ============================================================
-- Table: ai_knowledge - AI 知识库表
-- ============================================================

DROP TABLE IF EXISTS ai_knowledge CASCADE;

CREATE TABLE ai_knowledge (
  id BIGINT NOT NULL DEFAULT nextval('ai_knowledge_seq'),
  name VARCHAR(255) NOT NULL,
  description VARCHAR(1024) NULL DEFAULT NULL,
  embedding_model_id BIGINT NOT NULL,
  embedding_model VARCHAR(255) NOT NULL,
  top_k INT NOT NULL DEFAULT 10,
  similarity_threshold DOUBLE PRECISION NOT NULL DEFAULT 0.7,
  status INT NOT NULL,
  creator VARCHAR(64) NULL DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) NULL DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE INDEX idx_knowledge_embedding_model_id ON ai_knowledge (embedding_model_id);
CREATE INDEX idx_knowledge_status ON ai_knowledge (status);

COMMENT ON TABLE ai_knowledge IS 'AI 知识库表';
COMMENT ON COLUMN ai_knowledge.id IS '编号';
COMMENT ON COLUMN ai_knowledge.name IS '知识库名称';
COMMENT ON COLUMN ai_knowledge.description IS '知识库描述';
COMMENT ON COLUMN ai_knowledge.embedding_model_id IS '向量模型编号';
COMMENT ON COLUMN ai_knowledge.embedding_model IS '模型标识';
COMMENT ON COLUMN ai_knowledge.top_k IS 'topK';
COMMENT ON COLUMN ai_knowledge.similarity_threshold IS '相似度阈值';
COMMENT ON COLUMN ai_knowledge.status IS '状态';
COMMENT ON COLUMN ai_knowledge.creator IS '创建者';
COMMENT ON COLUMN ai_knowledge.create_time IS '创建时间';
COMMENT ON COLUMN ai_knowledge.updater IS '更新者';
COMMENT ON COLUMN ai_knowledge.update_time IS '更新时间';
COMMENT ON COLUMN ai_knowledge.deleted IS '是否删除';
COMMENT ON COLUMN ai_knowledge.tenant_id IS '租户编号';

-- ============================================================
-- Table: ai_chat_conversation - AI 聊天对话表
-- ============================================================

DROP TABLE IF EXISTS ai_chat_conversation CASCADE;

CREATE TABLE ai_chat_conversation (
  id BIGINT NOT NULL DEFAULT nextval('ai_chat_conversation_seq'),
  user_id BIGINT NOT NULL,
  role_id BIGINT NULL DEFAULT NULL,
  title VARCHAR(256) NOT NULL,
  model_id BIGINT NOT NULL,
  model VARCHAR(32) NOT NULL,
  pinned BOOLEAN NOT NULL DEFAULT FALSE,
  pinned_time TIMESTAMP NULL DEFAULT NULL,
  system_message VARCHAR(1024) NULL DEFAULT NULL,
  temperature DOUBLE PRECISION NOT NULL,
  max_tokens INT NOT NULL,
  max_contexts INT NOT NULL,
  creator VARCHAR(64) NULL DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) NULL DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE INDEX idx_conversation_user_id ON ai_chat_conversation (user_id);
CREATE INDEX idx_conversation_role_id ON ai_chat_conversation (role_id);
CREATE INDEX idx_conversation_model_id ON ai_chat_conversation (model_id);
CREATE INDEX idx_conversation_pinned ON ai_chat_conversation (pinned, pinned_time);

COMMENT ON TABLE ai_chat_conversation IS 'AI 聊天对话表';
COMMENT ON COLUMN ai_chat_conversation.id IS '对话编号';
COMMENT ON COLUMN ai_chat_conversation.user_id IS '用户编号';
COMMENT ON COLUMN ai_chat_conversation.role_id IS '聊天角色';
COMMENT ON COLUMN ai_chat_conversation.title IS '对话标题';
COMMENT ON COLUMN ai_chat_conversation.model_id IS '模型编号';
COMMENT ON COLUMN ai_chat_conversation.model IS '模型标识';
COMMENT ON COLUMN ai_chat_conversation.pinned IS '是否置顶';
COMMENT ON COLUMN ai_chat_conversation.pinned_time IS '置顶时间';
COMMENT ON COLUMN ai_chat_conversation.system_message IS '角色设定';
COMMENT ON COLUMN ai_chat_conversation.temperature IS '温度参数';
COMMENT ON COLUMN ai_chat_conversation.max_tokens IS '单条回复的最大 Token 数量';
COMMENT ON COLUMN ai_chat_conversation.max_contexts IS '上下文的最大 Message 数量';
COMMENT ON COLUMN ai_chat_conversation.creator IS '创建者';
COMMENT ON COLUMN ai_chat_conversation.create_time IS '创建时间';
COMMENT ON COLUMN ai_chat_conversation.updater IS '更新者';
COMMENT ON COLUMN ai_chat_conversation.update_time IS '更新时间';
COMMENT ON COLUMN ai_chat_conversation.deleted IS '是否删除';
COMMENT ON COLUMN ai_chat_conversation.tenant_id IS '租户编号';

-- ============================================================
-- Table: ai_knowledge_document - AI 知识库-文档表
-- ============================================================

DROP TABLE IF EXISTS ai_knowledge_document CASCADE;

CREATE TABLE ai_knowledge_document (
  id BIGINT NOT NULL DEFAULT nextval('ai_knowledge_document_seq'),
  knowledge_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  url VARCHAR(512) NULL DEFAULT NULL,
  content TEXT NULL,
  content_length INT NOT NULL DEFAULT 0,
  tokens INT NOT NULL DEFAULT 0,
  segment_max_tokens INT NOT NULL DEFAULT 500,
  retrieval_count INT NOT NULL DEFAULT 0,
  status INT NOT NULL,
  creator VARCHAR(64) NULL DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) NULL DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE INDEX idx_document_knowledge_id ON ai_knowledge_document (knowledge_id);
CREATE INDEX idx_document_status ON ai_knowledge_document (status);

COMMENT ON TABLE ai_knowledge_document IS 'AI 知识库-文档表';
COMMENT ON COLUMN ai_knowledge_document.id IS '编号';
COMMENT ON COLUMN ai_knowledge_document.knowledge_id IS '知识库编号';
COMMENT ON COLUMN ai_knowledge_document.name IS '文档名称';
COMMENT ON COLUMN ai_knowledge_document.url IS '文件 URL';
COMMENT ON COLUMN ai_knowledge_document.content IS '内容';
COMMENT ON COLUMN ai_knowledge_document.content_length IS '文档长度';
COMMENT ON COLUMN ai_knowledge_document.tokens IS '文档 token 数量';
COMMENT ON COLUMN ai_knowledge_document.segment_max_tokens IS '分片最大 Token 数';
COMMENT ON COLUMN ai_knowledge_document.retrieval_count IS '召回次数';
COMMENT ON COLUMN ai_knowledge_document.status IS '状态';
COMMENT ON COLUMN ai_knowledge_document.creator IS '创建者';
COMMENT ON COLUMN ai_knowledge_document.create_time IS '创建时间';
COMMENT ON COLUMN ai_knowledge_document.updater IS '更新者';
COMMENT ON COLUMN ai_knowledge_document.update_time IS '更新时间';
COMMENT ON COLUMN ai_knowledge_document.deleted IS '是否删除';
COMMENT ON COLUMN ai_knowledge_document.tenant_id IS '租户编号';

-- ============================================================
-- Table: ai_chat_message - AI 聊天消息表
-- ============================================================

DROP TABLE IF EXISTS ai_chat_message CASCADE;

CREATE TABLE ai_chat_message (
  id BIGINT NOT NULL DEFAULT nextval('ai_chat_message_seq'),
  conversation_id BIGINT NOT NULL,
  reply_id BIGINT NULL DEFAULT NULL,
  user_id BIGINT NOT NULL,
  role_id BIGINT NULL DEFAULT NULL,
  type VARCHAR(16) NOT NULL,
  model VARCHAR(32) NOT NULL,
  model_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  reasoning_content TEXT NULL,
  use_context BOOLEAN NOT NULL DEFAULT FALSE,
  segment_ids VARCHAR(2048) NULL DEFAULT NULL,
  web_search_pages VARCHAR(8192) NULL DEFAULT NULL,
  attachment_urls VARCHAR(2048) NULL DEFAULT NULL,
  creator VARCHAR(64) NULL DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) NULL DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE INDEX idx_message_conversation_id ON ai_chat_message (conversation_id);
CREATE INDEX idx_message_user_id ON ai_chat_message (user_id);
CREATE INDEX idx_message_role_id ON ai_chat_message (role_id);
CREATE INDEX idx_message_model_id ON ai_chat_message (model_id);
CREATE INDEX idx_message_reply_id ON ai_chat_message (reply_id);

COMMENT ON TABLE ai_chat_message IS 'AI 聊天消息表';
COMMENT ON COLUMN ai_chat_message.id IS '消息编号';
COMMENT ON COLUMN ai_chat_message.conversation_id IS '对话编号';
COMMENT ON COLUMN ai_chat_message.reply_id IS '回复编号';
COMMENT ON COLUMN ai_chat_message.user_id IS '用户编号';
COMMENT ON COLUMN ai_chat_message.role_id IS '角色编号';
COMMENT ON COLUMN ai_chat_message.type IS '消息类型';
COMMENT ON COLUMN ai_chat_message.model IS '模型标识';
COMMENT ON COLUMN ai_chat_message.model_id IS '模型编号';
COMMENT ON COLUMN ai_chat_message.content IS '消息内容';
COMMENT ON COLUMN ai_chat_message.reasoning_content IS '推理内容';
COMMENT ON COLUMN ai_chat_message.use_context IS '是否携带上下文';
COMMENT ON COLUMN ai_chat_message.segment_ids IS '知识库段落编号数组';
COMMENT ON COLUMN ai_chat_message.web_search_pages IS '联网搜索的网页内容数组';
COMMENT ON COLUMN ai_chat_message.attachment_urls IS '附件 URL 数组';
COMMENT ON COLUMN ai_chat_message.creator IS '创建者';
COMMENT ON COLUMN ai_chat_message.create_time IS '创建时间';
COMMENT ON COLUMN ai_chat_message.updater IS '更新者';
COMMENT ON COLUMN ai_chat_message.update_time IS '更新时间';
COMMENT ON COLUMN ai_chat_message.deleted IS '是否删除';
COMMENT ON COLUMN ai_chat_message.tenant_id IS '租户编号';

-- ============================================================
-- Table: ai_knowledge_segment - AI 知识库-文档分段表
-- ============================================================

DROP TABLE IF EXISTS ai_knowledge_segment CASCADE;

CREATE TABLE ai_knowledge_segment (
  id BIGINT NOT NULL DEFAULT nextval('ai_knowledge_segment_seq'),
  knowledge_id BIGINT NOT NULL,
  document_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  content_length INT NOT NULL DEFAULT 0,
  vector_id VARCHAR(255) NULL DEFAULT '',
  tokens INT NOT NULL DEFAULT 0,
  retrieval_count INT NOT NULL DEFAULT 0,
  status INT NOT NULL,
  creator VARCHAR(64) NULL DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) NULL DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE INDEX idx_segment_knowledge_id ON ai_knowledge_segment (knowledge_id);
CREATE INDEX idx_segment_document_id ON ai_knowledge_segment (document_id);
CREATE INDEX idx_segment_vector_id ON ai_knowledge_segment (vector_id);
CREATE INDEX idx_segment_status ON ai_knowledge_segment (status);

COMMENT ON TABLE ai_knowledge_segment IS 'AI 知识库-文档分段表';
COMMENT ON COLUMN ai_knowledge_segment.id IS '编号';
COMMENT ON COLUMN ai_knowledge_segment.knowledge_id IS '知识库编号';
COMMENT ON COLUMN ai_knowledge_segment.document_id IS '文档编号';
COMMENT ON COLUMN ai_knowledge_segment.content IS '切片内容';
COMMENT ON COLUMN ai_knowledge_segment.content_length IS '切片内容长度';
COMMENT ON COLUMN ai_knowledge_segment.vector_id IS '向量库的编号';
COMMENT ON COLUMN ai_knowledge_segment.tokens IS 'token 数量';
COMMENT ON COLUMN ai_knowledge_segment.retrieval_count IS '召回次数';
COMMENT ON COLUMN ai_knowledge_segment.status IS '状态';
COMMENT ON COLUMN ai_knowledge_segment.creator IS '创建者';
COMMENT ON COLUMN ai_knowledge_segment.create_time IS '创建时间';
COMMENT ON COLUMN ai_knowledge_segment.updater IS '更新者';
COMMENT ON COLUMN ai_knowledge_segment.update_time IS '更新时间';
COMMENT ON COLUMN ai_knowledge_segment.deleted IS '是否删除';
COMMENT ON COLUMN ai_knowledge_segment.tenant_id IS '租户编号';

-- ============================================================
-- Table: ai_image - AI 绘画表
-- ============================================================

DROP TABLE IF EXISTS ai_image CASCADE;

CREATE TABLE ai_image (
  id BIGINT NOT NULL DEFAULT nextval('ai_image_seq'),
  user_id BIGINT NOT NULL,
  prompt VARCHAR(2048) NOT NULL,
  platform VARCHAR(255) NOT NULL,
  model_id BIGINT NOT NULL,
  model VARCHAR(255) NOT NULL,
  width INT NULL DEFAULT NULL,
  height INT NULL DEFAULT NULL,
  status INT NOT NULL,
  finish_time TIMESTAMP NULL DEFAULT NULL,
  error_message VARCHAR(2048) NULL DEFAULT NULL,
  pic_url VARCHAR(512) NULL DEFAULT NULL,
  public_status BOOLEAN NOT NULL DEFAULT FALSE,
  options VARCHAR(4096) NULL DEFAULT NULL,
  buttons VARCHAR(2048) NULL DEFAULT NULL,
  task_id VARCHAR(255) NULL DEFAULT NULL,
  creator VARCHAR(64) NULL DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) NULL DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE INDEX idx_image_user_id ON ai_image (user_id);
CREATE INDEX idx_image_platform ON ai_image (platform);
CREATE INDEX idx_image_model_id ON ai_image (model_id);
CREATE INDEX idx_image_status ON ai_image (status);
CREATE INDEX idx_image_task_id ON ai_image (task_id);

COMMENT ON TABLE ai_image IS 'AI 绘画表';
COMMENT ON COLUMN ai_image.id IS '编号';
COMMENT ON COLUMN ai_image.user_id IS '用户编号';
COMMENT ON COLUMN ai_image.prompt IS '提示词';
COMMENT ON COLUMN ai_image.platform IS '平台';
COMMENT ON COLUMN ai_image.model_id IS '模型编号';
COMMENT ON COLUMN ai_image.model IS '模型标识';
COMMENT ON COLUMN ai_image.width IS '图片宽度';
COMMENT ON COLUMN ai_image.height IS '图片高度';
COMMENT ON COLUMN ai_image.status IS '生成状态';
COMMENT ON COLUMN ai_image.finish_time IS '完成时间';
COMMENT ON COLUMN ai_image.error_message IS '绘画错误信息';
COMMENT ON COLUMN ai_image.pic_url IS '图片地址';
COMMENT ON COLUMN ai_image.public_status IS '是否公开';
COMMENT ON COLUMN ai_image.options IS '绘制参数';
COMMENT ON COLUMN ai_image.buttons IS 'mj buttons 按钮';
COMMENT ON COLUMN ai_image.task_id IS '任务编号';
COMMENT ON COLUMN ai_image.creator IS '创建者';
COMMENT ON COLUMN ai_image.create_time IS '创建时间';
COMMENT ON COLUMN ai_image.updater IS '更新者';
COMMENT ON COLUMN ai_image.update_time IS '更新时间';
COMMENT ON COLUMN ai_image.deleted IS '是否删除';
COMMENT ON COLUMN ai_image.tenant_id IS '租户编号';

-- ============================================================
-- Table: ai_music - AI 音乐表
-- ============================================================

DROP TABLE IF EXISTS ai_music CASCADE;

CREATE TABLE ai_music (
  id BIGINT NOT NULL DEFAULT nextval('ai_music_seq'),
  user_id BIGINT NOT NULL,
  title VARCHAR(255) NULL DEFAULT NULL,
  lyric TEXT NULL,
  image_url VARCHAR(512) NULL DEFAULT NULL,
  audio_url VARCHAR(512) NULL DEFAULT NULL,
  video_url VARCHAR(512) NULL DEFAULT NULL,
  status INT NOT NULL,
  generate_mode INT NOT NULL,
  description VARCHAR(2048) NULL DEFAULT NULL,
  platform VARCHAR(255) NOT NULL,
  model VARCHAR(255) NOT NULL,
  tags VARCHAR(1024) NULL DEFAULT NULL,
  duration DOUBLE PRECISION NULL DEFAULT NULL,
  public_status BOOLEAN NOT NULL DEFAULT FALSE,
  task_id VARCHAR(255) NULL DEFAULT NULL,
  error_message VARCHAR(2048) NULL DEFAULT NULL,
  creator VARCHAR(64) NULL DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) NULL DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE INDEX idx_music_user_id ON ai_music (user_id);
CREATE INDEX idx_music_platform ON ai_music (platform);
CREATE INDEX idx_music_status ON ai_music (status);
CREATE INDEX idx_music_task_id ON ai_music (task_id);

COMMENT ON TABLE ai_music IS 'AI 音乐表';
COMMENT ON COLUMN ai_music.id IS '编号';
COMMENT ON COLUMN ai_music.user_id IS '用户编号';
COMMENT ON COLUMN ai_music.title IS '音乐名称';
COMMENT ON COLUMN ai_music.lyric IS '歌词';
COMMENT ON COLUMN ai_music.image_url IS '图片地址';
COMMENT ON COLUMN ai_music.audio_url IS '音频地址';
COMMENT ON COLUMN ai_music.video_url IS '视频地址';
COMMENT ON COLUMN ai_music.status IS '音乐状态';
COMMENT ON COLUMN ai_music.generate_mode IS '生成模式';
COMMENT ON COLUMN ai_music.description IS '描述词';
COMMENT ON COLUMN ai_music.platform IS '平台';
COMMENT ON COLUMN ai_music.model IS '模型';
COMMENT ON COLUMN ai_music.tags IS '音乐风格标签';
COMMENT ON COLUMN ai_music.duration IS '音乐时长';
COMMENT ON COLUMN ai_music.public_status IS '是否公开';
COMMENT ON COLUMN ai_music.task_id IS '任务编号';
COMMENT ON COLUMN ai_music.error_message IS '错误信息';
COMMENT ON COLUMN ai_music.creator IS '创建者';
COMMENT ON COLUMN ai_music.create_time IS '创建时间';
COMMENT ON COLUMN ai_music.updater IS '更新者';
COMMENT ON COLUMN ai_music.update_time IS '更新时间';
COMMENT ON COLUMN ai_music.deleted IS '是否删除';
COMMENT ON COLUMN ai_music.tenant_id IS '租户编号';

-- ============================================================
-- Table: ai_write - AI 写作表
-- ============================================================

DROP TABLE IF EXISTS ai_write CASCADE;

CREATE TABLE ai_write (
  id BIGINT NOT NULL DEFAULT nextval('ai_write_seq'),
  user_id BIGINT NOT NULL,
  type INT NOT NULL,
  platform VARCHAR(255) NOT NULL,
  model_id BIGINT NOT NULL,
  model VARCHAR(255) NOT NULL,
  prompt VARCHAR(2048) NOT NULL,
  generated_content TEXT NULL,
  original_content TEXT NULL,
  length INT NULL DEFAULT NULL,
  format INT NULL DEFAULT NULL,
  tone INT NULL DEFAULT NULL,
  language INT NULL DEFAULT NULL,
  error_message VARCHAR(2048) NULL DEFAULT NULL,
  creator VARCHAR(64) NULL DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) NULL DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE INDEX idx_write_user_id ON ai_write (user_id);
CREATE INDEX idx_write_type ON ai_write (type);
CREATE INDEX idx_write_platform ON ai_write (platform);
CREATE INDEX idx_write_model_id ON ai_write (model_id);

COMMENT ON TABLE ai_write IS 'AI 写作表';
COMMENT ON COLUMN ai_write.id IS '编号';
COMMENT ON COLUMN ai_write.user_id IS '用户编号';
COMMENT ON COLUMN ai_write.type IS '写作类型';
COMMENT ON COLUMN ai_write.platform IS '平台';
COMMENT ON COLUMN ai_write.model_id IS '模型编号';
COMMENT ON COLUMN ai_write.model IS '模型';
COMMENT ON COLUMN ai_write.prompt IS '生成内容提示';
COMMENT ON COLUMN ai_write.generated_content IS '生成的内容';
COMMENT ON COLUMN ai_write.original_content IS '原文';
COMMENT ON COLUMN ai_write.length IS '长度提示词';
COMMENT ON COLUMN ai_write.format IS '格式提示词';
COMMENT ON COLUMN ai_write.tone IS '语气提示词';
COMMENT ON COLUMN ai_write.language IS '语言提示词';
COMMENT ON COLUMN ai_write.error_message IS '错误信息';
COMMENT ON COLUMN ai_write.creator IS '创建者';
COMMENT ON COLUMN ai_write.create_time IS '创建时间';
COMMENT ON COLUMN ai_write.updater IS '更新者';
COMMENT ON COLUMN ai_write.update_time IS '更新时间';
COMMENT ON COLUMN ai_write.deleted IS '是否删除';
COMMENT ON COLUMN ai_write.tenant_id IS '租户编号';

-- ============================================================
-- Table: ai_mind_map - AI 思维导图表
-- ============================================================

DROP TABLE IF EXISTS ai_mind_map CASCADE;

CREATE TABLE ai_mind_map (
  id BIGINT NOT NULL DEFAULT nextval('ai_mind_map_seq'),
  user_id BIGINT NOT NULL,
  platform VARCHAR(255) NOT NULL,
  model_id BIGINT NOT NULL,
  model VARCHAR(255) NOT NULL,
  prompt VARCHAR(2048) NOT NULL,
  generated_content TEXT NULL,
  error_message VARCHAR(2048) NULL DEFAULT NULL,
  creator VARCHAR(64) NULL DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) NULL DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE INDEX idx_mind_map_user_id ON ai_mind_map (user_id);
CREATE INDEX idx_mind_map_platform ON ai_mind_map (platform);
CREATE INDEX idx_mind_map_model_id ON ai_mind_map (model_id);

COMMENT ON TABLE ai_mind_map IS 'AI 思维导图表';
COMMENT ON COLUMN ai_mind_map.id IS '编号';
COMMENT ON COLUMN ai_mind_map.user_id IS '用户编号';
COMMENT ON COLUMN ai_mind_map.platform IS '平台';
COMMENT ON COLUMN ai_mind_map.model_id IS '模型编号';
COMMENT ON COLUMN ai_mind_map.model IS '模型';
COMMENT ON COLUMN ai_mind_map.prompt IS '生成内容提示';
COMMENT ON COLUMN ai_mind_map.generated_content IS '生成的内容';
COMMENT ON COLUMN ai_mind_map.error_message IS '错误信息';
COMMENT ON COLUMN ai_mind_map.creator IS '创建者';
COMMENT ON COLUMN ai_mind_map.create_time IS '创建时间';
COMMENT ON COLUMN ai_mind_map.updater IS '更新者';
COMMENT ON COLUMN ai_mind_map.update_time IS '更新时间';
COMMENT ON COLUMN ai_mind_map.deleted IS '是否删除';
COMMENT ON COLUMN ai_mind_map.tenant_id IS '租户编号';

-- ============================================================
-- 完成提示
-- ============================================================

SELECT 'AI 模块 PostgreSQL 建表脚本执行完成！共创建 14 张表。' AS message;
