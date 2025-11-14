/*
 MP模块数据库初始化脚本 (PostgreSQL)

 Source: mp-2024-05-29.sql (MySQL)
 Target: PostgreSQL 12+
 Generated: 2025-11-14

 Features:
 - 7 tables for WeChat Official Account module
 - Multi-tenant support (tenant_id)
 - Soft delete (deleted flag)
 - Audit fields (creator, create_time, updater, update_time)
 - Auto-update trigger for update_time field
*/

-- ========== 创建触发器函数（用于 update_time 自动更新）==========
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ========== 创建序列 ==========
CREATE SEQUENCE IF NOT EXISTS mp_account_seq START WITH 6;
CREATE SEQUENCE IF NOT EXISTS mp_auto_reply_seq START WITH 55;
CREATE SEQUENCE IF NOT EXISTS mp_material_seq START WITH 98;
CREATE SEQUENCE IF NOT EXISTS mp_menu_seq START WITH 169;
CREATE SEQUENCE IF NOT EXISTS mp_message_seq START WITH 414;
CREATE SEQUENCE IF NOT EXISTS mp_tag_seq START WITH 14;
CREATE SEQUENCE IF NOT EXISTS mp_user_seq START WITH 55;

-- ========== 公众号账号管理 ==========

-- ----------------------------
-- Table: mp_account
-- ----------------------------
DROP TABLE IF EXISTS mp_account CASCADE;
CREATE TABLE mp_account (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(100),
  account VARCHAR(100),
  app_id VARCHAR(100),
  app_secret VARCHAR(100),
  token VARCHAR(100),
  aes_key VARCHAR(300),
  qr_code_url VARCHAR(200),
  remark VARCHAR(255),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE mp_account IS '公众号账号表';
COMMENT ON COLUMN mp_account.id IS '编号';
COMMENT ON COLUMN mp_account.name IS '公众号名称';
COMMENT ON COLUMN mp_account.account IS '公众号账号';
COMMENT ON COLUMN mp_account.app_id IS '公众号appid';
COMMENT ON COLUMN mp_account.app_secret IS '公众号密钥';
COMMENT ON COLUMN mp_account.token IS '公众号token';
COMMENT ON COLUMN mp_account.aes_key IS '消息加解密密钥';
COMMENT ON COLUMN mp_account.qr_code_url IS '二维码图片URL';
COMMENT ON COLUMN mp_account.remark IS '备注';
COMMENT ON COLUMN mp_account.creator IS '创建者';
COMMENT ON COLUMN mp_account.create_time IS '创建时间';
COMMENT ON COLUMN mp_account.updater IS '更新者';
COMMENT ON COLUMN mp_account.update_time IS '更新时间';
COMMENT ON COLUMN mp_account.deleted IS '是否删除';
COMMENT ON COLUMN mp_account.tenant_id IS '租户编号';

CREATE TRIGGER update_mp_account_update_time
BEFORE UPDATE ON mp_account
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ========== 公众号粉丝管理 ==========

-- ----------------------------
-- Table: mp_user
-- ----------------------------
DROP TABLE IF EXISTS mp_user CASCADE;
CREATE TABLE mp_user (
  id BIGSERIAL PRIMARY KEY,
  openid VARCHAR(100) NOT NULL,
  union_id VARCHAR(100),
  subscribe_status SMALLINT NOT NULL,
  subscribe_time TIMESTAMP NOT NULL,
  nickname VARCHAR(64),
  head_image_url VARCHAR(1024),
  unsubscribe_time TIMESTAMP,
  language VARCHAR(30),
  country VARCHAR(30),
  province VARCHAR(30),
  city VARCHAR(30),
  remark VARCHAR(128),
  tag_ids VARCHAR(255),
  account_id BIGINT NOT NULL,
  app_id VARCHAR(128) NOT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE mp_user IS '公众号粉丝表';
COMMENT ON COLUMN mp_user.id IS '编号';
COMMENT ON COLUMN mp_user.openid IS '用户标识';
COMMENT ON COLUMN mp_user.union_id IS '微信生态唯一标识';
COMMENT ON COLUMN mp_user.subscribe_status IS '关注状态';
COMMENT ON COLUMN mp_user.subscribe_time IS '关注时间';
COMMENT ON COLUMN mp_user.nickname IS '昵称';
COMMENT ON COLUMN mp_user.head_image_url IS '头像地址';
COMMENT ON COLUMN mp_user.unsubscribe_time IS '取消关注时间';
COMMENT ON COLUMN mp_user.language IS '语言';
COMMENT ON COLUMN mp_user.country IS '国家';
COMMENT ON COLUMN mp_user.province IS '省份';
COMMENT ON COLUMN mp_user.city IS '城市';
COMMENT ON COLUMN mp_user.remark IS '备注';
COMMENT ON COLUMN mp_user.tag_ids IS '标签编号数组';
COMMENT ON COLUMN mp_user.account_id IS '公众号账号的编号';
COMMENT ON COLUMN mp_user.app_id IS '微信公众号 appid';
COMMENT ON COLUMN mp_user.creator IS '创建者';
COMMENT ON COLUMN mp_user.create_time IS '创建时间';
COMMENT ON COLUMN mp_user.updater IS '更新者';
COMMENT ON COLUMN mp_user.update_time IS '更新时间';
COMMENT ON COLUMN mp_user.deleted IS '是否删除';
COMMENT ON COLUMN mp_user.tenant_id IS '租户编号';

CREATE TRIGGER update_mp_user_update_time
BEFORE UPDATE ON mp_user
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ========== 公众号标签管理 ==========

-- ----------------------------
-- Table: mp_tag
-- ----------------------------
DROP TABLE IF EXISTS mp_tag CASCADE;
CREATE TABLE mp_tag (
  id BIGSERIAL PRIMARY KEY,
  tag_id BIGINT,
  name VARCHAR(32),
  count INTEGER DEFAULT 0,
  account_id BIGINT NOT NULL,
  app_id VARCHAR(128) NOT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE mp_tag IS '公众号标签表';
COMMENT ON COLUMN mp_tag.id IS '主键';
COMMENT ON COLUMN mp_tag.tag_id IS '公众号标签 id';
COMMENT ON COLUMN mp_tag.name IS '标签名称';
COMMENT ON COLUMN mp_tag.count IS '此标签下粉丝数';
COMMENT ON COLUMN mp_tag.account_id IS '公众号账号的编号';
COMMENT ON COLUMN mp_tag.app_id IS '公众号 appId';
COMMENT ON COLUMN mp_tag.creator IS '创建者';
COMMENT ON COLUMN mp_tag.create_time IS '创建时间';
COMMENT ON COLUMN mp_tag.updater IS '更新者';
COMMENT ON COLUMN mp_tag.update_time IS '更新时间';
COMMENT ON COLUMN mp_tag.deleted IS '是否删除';
COMMENT ON COLUMN mp_tag.tenant_id IS '租户编号';

CREATE TRIGGER update_mp_tag_update_time
BEFORE UPDATE ON mp_tag
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ========== 公众号菜单管理 ==========

-- ----------------------------
-- Table: mp_menu
-- ----------------------------
DROP TABLE IF EXISTS mp_menu CASCADE;
CREATE TABLE mp_menu (
  id BIGSERIAL PRIMARY KEY,
  account_id BIGINT NOT NULL,
  app_id VARCHAR(128) NOT NULL,
  name VARCHAR(255),
  menu_key VARCHAR(255),
  parent_id BIGINT,
  type VARCHAR(32) NOT NULL DEFAULT '',
  url VARCHAR(500),
  mini_program_app_id VARCHAR(32),
  mini_program_page_path VARCHAR(200),
  article_id VARCHAR(200),
  reply_message_type VARCHAR(32),
  reply_content VARCHAR(1024),
  reply_media_id VARCHAR(128),
  reply_media_url VARCHAR(1024),
  reply_title VARCHAR(128),
  reply_description VARCHAR(256),
  reply_thumb_media_id VARCHAR(128),
  reply_thumb_media_url VARCHAR(1024),
  reply_articles VARCHAR(1024),
  reply_music_url VARCHAR(1024),
  reply_hq_music_url VARCHAR(1024),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE mp_menu IS '公众号菜单表';
COMMENT ON COLUMN mp_menu.id IS '主键';
COMMENT ON COLUMN mp_menu.account_id IS '公众号账号的编号';
COMMENT ON COLUMN mp_menu.app_id IS '公众号 appId';
COMMENT ON COLUMN mp_menu.name IS '菜单名称';
COMMENT ON COLUMN mp_menu.menu_key IS '菜单标识';
COMMENT ON COLUMN mp_menu.parent_id IS '父菜单编号';
COMMENT ON COLUMN mp_menu.type IS '按钮类型';
COMMENT ON COLUMN mp_menu.url IS '网页链接';
COMMENT ON COLUMN mp_menu.mini_program_app_id IS '小程序appid';
COMMENT ON COLUMN mp_menu.mini_program_page_path IS '小程序页面路径';
COMMENT ON COLUMN mp_menu.article_id IS '跳转图文的媒体编号';
COMMENT ON COLUMN mp_menu.reply_message_type IS '消息类型';
COMMENT ON COLUMN mp_menu.reply_content IS '回复的消息内容';
COMMENT ON COLUMN mp_menu.reply_media_id IS '回复的媒体文件 id';
COMMENT ON COLUMN mp_menu.reply_media_url IS '回复的媒体文件 URL';
COMMENT ON COLUMN mp_menu.reply_title IS '回复的标题';
COMMENT ON COLUMN mp_menu.reply_description IS '回复的描述';
COMMENT ON COLUMN mp_menu.reply_thumb_media_id IS '回复的缩略图的媒体 id';
COMMENT ON COLUMN mp_menu.reply_thumb_media_url IS '回复的缩略图的媒体 URL';
COMMENT ON COLUMN mp_menu.reply_articles IS '回复的图文消息数组';
COMMENT ON COLUMN mp_menu.reply_music_url IS '回复的音乐链接';
COMMENT ON COLUMN mp_menu.reply_hq_music_url IS '回复的高质量音乐链接';
COMMENT ON COLUMN mp_menu.creator IS '创建者';
COMMENT ON COLUMN mp_menu.create_time IS '创建时间';
COMMENT ON COLUMN mp_menu.updater IS '更新者';
COMMENT ON COLUMN mp_menu.update_time IS '更新时间';
COMMENT ON COLUMN mp_menu.deleted IS '是否删除';
COMMENT ON COLUMN mp_menu.tenant_id IS '租户编号';

CREATE TRIGGER update_mp_menu_update_time
BEFORE UPDATE ON mp_menu
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ========== 公众号消息管理 ==========

-- ----------------------------
-- Table: mp_message
-- ----------------------------
DROP TABLE IF EXISTS mp_message CASCADE;
CREATE TABLE mp_message (
  id BIGSERIAL PRIMARY KEY,
  msg_id BIGINT,
  account_id BIGINT NOT NULL,
  app_id VARCHAR(128) NOT NULL,
  user_id BIGINT NOT NULL,
  openid VARCHAR(100) NOT NULL,
  type VARCHAR(32) NOT NULL,
  send_from SMALLINT NOT NULL,
  content VARCHAR(1024),
  media_id VARCHAR(128),
  media_url VARCHAR(1024),
  recognition VARCHAR(1024),
  format VARCHAR(16),
  title VARCHAR(128),
  description VARCHAR(256),
  thumb_media_id VARCHAR(128),
  thumb_media_url VARCHAR(1024),
  url VARCHAR(500),
  location_x DOUBLE PRECISION,
  location_y DOUBLE PRECISION,
  scale DOUBLE PRECISION,
  label VARCHAR(128),
  articles VARCHAR(1024),
  music_url VARCHAR(1024),
  hq_music_url VARCHAR(1024),
  event VARCHAR(64),
  event_key VARCHAR(64),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE mp_message IS '公众号消息表 ';
COMMENT ON COLUMN mp_message.id IS '主键';
COMMENT ON COLUMN mp_message.msg_id IS '微信公众号消息id';
COMMENT ON COLUMN mp_message.account_id IS '公众号账号的编号';
COMMENT ON COLUMN mp_message.app_id IS '公众号 appId';
COMMENT ON COLUMN mp_message.user_id IS '公众号粉丝的编号';
COMMENT ON COLUMN mp_message.openid IS '公众号粉丝标志';
COMMENT ON COLUMN mp_message.type IS '消息类型';
COMMENT ON COLUMN mp_message.send_from IS '消息来源';
COMMENT ON COLUMN mp_message.content IS '消息内容';
COMMENT ON COLUMN mp_message.media_id IS '媒体文件 id';
COMMENT ON COLUMN mp_message.media_url IS '媒体文件 URL';
COMMENT ON COLUMN mp_message.recognition IS '语音识别后文本';
COMMENT ON COLUMN mp_message.format IS '语音格式';
COMMENT ON COLUMN mp_message.title IS '标题';
COMMENT ON COLUMN mp_message.description IS '描述';
COMMENT ON COLUMN mp_message.thumb_media_id IS '缩略图的媒体 id';
COMMENT ON COLUMN mp_message.thumb_media_url IS '缩略图的媒体 URL';
COMMENT ON COLUMN mp_message.url IS '点击图文消息跳转链接';
COMMENT ON COLUMN mp_message.location_x IS '地理位置维度';
COMMENT ON COLUMN mp_message.location_y IS '地理位置经度';
COMMENT ON COLUMN mp_message.scale IS '地图缩放大小';
COMMENT ON COLUMN mp_message.label IS '详细地址';
COMMENT ON COLUMN mp_message.articles IS '图文消息数组';
COMMENT ON COLUMN mp_message.music_url IS '音乐链接';
COMMENT ON COLUMN mp_message.hq_music_url IS '高质量音乐链接';
COMMENT ON COLUMN mp_message.event IS '事件类型';
COMMENT ON COLUMN mp_message.event_key IS '事件 Key';
COMMENT ON COLUMN mp_message.creator IS '创建者';
COMMENT ON COLUMN mp_message.create_time IS '创建时间';
COMMENT ON COLUMN mp_message.updater IS '更新者';
COMMENT ON COLUMN mp_message.update_time IS '更新时间';
COMMENT ON COLUMN mp_message.deleted IS '是否删除';
COMMENT ON COLUMN mp_message.tenant_id IS '租户编号';

CREATE TRIGGER update_mp_message_update_time
BEFORE UPDATE ON mp_message
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ========== 公众号自动回复管理 ==========

-- ----------------------------
-- Table: mp_auto_reply
-- ----------------------------
DROP TABLE IF EXISTS mp_auto_reply CASCADE;
CREATE TABLE mp_auto_reply (
  id BIGSERIAL PRIMARY KEY,
  account_id BIGINT NOT NULL,
  app_id VARCHAR(128) NOT NULL,
  type SMALLINT NOT NULL,
  request_keyword VARCHAR(255),
  request_match SMALLINT,
  request_message_type VARCHAR(32),
  response_message_type VARCHAR(32) NOT NULL,
  response_content VARCHAR(1024),
  response_media_id VARCHAR(128),
  response_media_url VARCHAR(1024),
  response_title VARCHAR(128),
  response_description VARCHAR(256),
  response_thumb_media_id VARCHAR(128),
  response_thumb_media_url VARCHAR(1024),
  response_articles VARCHAR(1024),
  response_music_url VARCHAR(1024),
  response_hq_music_url VARCHAR(1024),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE mp_auto_reply IS '公众号消息自动回复表';
COMMENT ON COLUMN mp_auto_reply.id IS '主键';
COMMENT ON COLUMN mp_auto_reply.account_id IS '公众号账号的编号';
COMMENT ON COLUMN mp_auto_reply.app_id IS '公众号 appId';
COMMENT ON COLUMN mp_auto_reply.type IS '回复类型';
COMMENT ON COLUMN mp_auto_reply.request_keyword IS '请求的关键字';
COMMENT ON COLUMN mp_auto_reply.request_match IS '请求的关键字的匹配';
COMMENT ON COLUMN mp_auto_reply.request_message_type IS '请求的消息类型';
COMMENT ON COLUMN mp_auto_reply.response_message_type IS '回复的消息类型';
COMMENT ON COLUMN mp_auto_reply.response_content IS '回复的消息内容';
COMMENT ON COLUMN mp_auto_reply.response_media_id IS '回复的媒体文件 id';
COMMENT ON COLUMN mp_auto_reply.response_media_url IS '回复的媒体文件 URL';
COMMENT ON COLUMN mp_auto_reply.response_title IS '回复的标题';
COMMENT ON COLUMN mp_auto_reply.response_description IS '回复的描述';
COMMENT ON COLUMN mp_auto_reply.response_thumb_media_id IS '回复的缩略图的媒体 id';
COMMENT ON COLUMN mp_auto_reply.response_thumb_media_url IS '回复的缩略图的媒体 URL';
COMMENT ON COLUMN mp_auto_reply.response_articles IS '回复的图文消息数组';
COMMENT ON COLUMN mp_auto_reply.response_music_url IS '回复的音乐链接';
COMMENT ON COLUMN mp_auto_reply.response_hq_music_url IS '回复的高质量音乐链接';
COMMENT ON COLUMN mp_auto_reply.creator IS '创建者';
COMMENT ON COLUMN mp_auto_reply.create_time IS '创建时间';
COMMENT ON COLUMN mp_auto_reply.updater IS '更新者';
COMMENT ON COLUMN mp_auto_reply.update_time IS '更新时间';
COMMENT ON COLUMN mp_auto_reply.deleted IS '是否删除';
COMMENT ON COLUMN mp_auto_reply.tenant_id IS '租户编号';

CREATE TRIGGER update_mp_auto_reply_update_time
BEFORE UPDATE ON mp_auto_reply
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ========== 公众号素材管理 ==========

-- ----------------------------
-- Table: mp_material
-- ----------------------------
DROP TABLE IF EXISTS mp_material CASCADE;
CREATE TABLE mp_material (
  id BIGSERIAL PRIMARY KEY,
  account_id BIGINT NOT NULL,
  app_id VARCHAR(128) NOT NULL,
  media_id VARCHAR(128) NOT NULL,
  type VARCHAR(32) NOT NULL,
  permanent BOOLEAN NOT NULL DEFAULT FALSE,
  url VARCHAR(1024),
  name VARCHAR(255),
  mp_url VARCHAR(1024),
  title VARCHAR(255),
  introduction VARCHAR(255),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE mp_material IS '公众号素材表';
COMMENT ON COLUMN mp_material.id IS '主键';
COMMENT ON COLUMN mp_material.account_id IS '公众号账号的编号';
COMMENT ON COLUMN mp_material.app_id IS '公众号 appId';
COMMENT ON COLUMN mp_material.media_id IS '公众号素材 id';
COMMENT ON COLUMN mp_material.type IS '文件类型';
COMMENT ON COLUMN mp_material.permanent IS '是否永久';
COMMENT ON COLUMN mp_material.url IS '文件服务器的 URL';
COMMENT ON COLUMN mp_material.name IS '名字';
COMMENT ON COLUMN mp_material.mp_url IS '公众号文件 URL';
COMMENT ON COLUMN mp_material.title IS '视频素材的标题';
COMMENT ON COLUMN mp_material.introduction IS '视频素材的描述';
COMMENT ON COLUMN mp_material.creator IS '创建者';
COMMENT ON COLUMN mp_material.create_time IS '创建时间';
COMMENT ON COLUMN mp_material.updater IS '更新者';
COMMENT ON COLUMN mp_material.update_time IS '更新时间';
COMMENT ON COLUMN mp_material.deleted IS '是否删除';
COMMENT ON COLUMN mp_material.tenant_id IS '租户编号';

CREATE TRIGGER update_mp_material_update_time
BEFORE UPDATE ON mp_material
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();
