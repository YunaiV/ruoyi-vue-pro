-- MP module PostgreSQL create SQL (generated from yudao-module-mp DO classes)

CREATE SEQUENCE IF NOT EXISTS mp_account_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE TABLE IF NOT EXISTS "mp_account" (
    "id" bigint NOT NULL DEFAULT nextval('mp_account_seq'),
    "name" varchar(255) NOT NULL DEFAULT '',
    "account" varchar(255) NOT NULL DEFAULT '',
    "app_id" varchar(255) NOT NULL DEFAULT '',
    "app_secret" varchar(255) NOT NULL DEFAULT '',
    "token" varchar(255) NOT NULL DEFAULT '',
    "aes_key" varchar(255) DEFAULT NULL,
    "qr_code_url" varchar(1024) DEFAULT NULL,
    "remark" varchar(1024) DEFAULT NULL,
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" boolean NOT NULL DEFAULT FALSE,
    "tenant_id" bigint NOT NULL DEFAULT 0,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "mp_account" IS '公众号账号';

CREATE SEQUENCE IF NOT EXISTS mp_material_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE TABLE IF NOT EXISTS "mp_material" (
    "id" bigint NOT NULL DEFAULT nextval('mp_material_seq'),
    "account_id" bigint NOT NULL,
    "app_id" varchar(255) NOT NULL DEFAULT '',
    "media_id" varchar(255) DEFAULT NULL,
    "type" varchar(32) NOT NULL DEFAULT '',
    "permanent" boolean NOT NULL DEFAULT FALSE,
    "url" varchar(1024) DEFAULT NULL,
    "name" varchar(255) DEFAULT NULL,
    "mp_url" varchar(1024) DEFAULT NULL,
    "title" varchar(255) DEFAULT NULL,
    "introduction" text DEFAULT NULL,
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" boolean NOT NULL DEFAULT FALSE,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "mp_material" IS '公众号素材';

CREATE SEQUENCE IF NOT EXISTS mp_menu_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE TABLE IF NOT EXISTS "mp_menu" (
    "id" bigint NOT NULL DEFAULT nextval('mp_menu_seq'),
    "account_id" bigint NOT NULL,
    "app_id" varchar(255) NOT NULL DEFAULT '',
    "name" varchar(255) NOT NULL DEFAULT '',
    "menu_key" varchar(255) DEFAULT NULL,
    "parent_id" bigint NOT NULL DEFAULT 0,
    "type" varchar(64) DEFAULT NULL,
    "url" varchar(1024) DEFAULT NULL,
    "mini_program_app_id" varchar(255) DEFAULT NULL,
    "mini_program_page_path" varchar(1024) DEFAULT NULL,
    "article_id" varchar(255) DEFAULT NULL,
    "reply_message_type" varchar(64) DEFAULT NULL,
    "reply_content" text DEFAULT NULL,
    "reply_media_id" varchar(255) DEFAULT NULL,
    "reply_media_url" varchar(1024) DEFAULT NULL,
    "reply_title" varchar(255) DEFAULT NULL,
    "reply_description" text DEFAULT NULL,
    "reply_thumb_media_id" varchar(255) DEFAULT NULL,
    "reply_thumb_media_url" varchar(1024) DEFAULT NULL,
    "reply_articles" text DEFAULT NULL,
    "reply_music_url" varchar(1024) DEFAULT NULL,
    "reply_hq_music_url" varchar(1024) DEFAULT NULL,
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" boolean NOT NULL DEFAULT FALSE,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "mp_menu" IS '公众号菜单';

CREATE SEQUENCE IF NOT EXISTS mp_auto_reply_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE TABLE IF NOT EXISTS "mp_auto_reply" (
    "id" bigint NOT NULL DEFAULT nextval('mp_auto_reply_seq'),
    "account_id" bigint NOT NULL,
    "app_id" varchar(255) NOT NULL DEFAULT '',
    "type" integer NOT NULL,
    "request_keyword" varchar(255) DEFAULT NULL,
    "request_match" integer DEFAULT NULL,
    "request_message_type" varchar(64) DEFAULT NULL,
    "response_message_type" varchar(64) NOT NULL DEFAULT '',
    "response_content" text DEFAULT NULL,
    "response_media_id" varchar(255) DEFAULT NULL,
    "response_media_url" varchar(1024) DEFAULT NULL,
    "response_title" varchar(255) DEFAULT NULL,
    "response_description" text DEFAULT NULL,
    "response_thumb_media_id" varchar(255) DEFAULT NULL,
    "response_thumb_media_url" varchar(1024) DEFAULT NULL,
    "response_articles" text DEFAULT NULL,
    "response_music_url" varchar(1024) DEFAULT NULL,
    "response_hq_music_url" varchar(1024) DEFAULT NULL,
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" boolean NOT NULL DEFAULT FALSE,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "mp_auto_reply" IS '公众号消息自动回复';

CREATE SEQUENCE IF NOT EXISTS mp_message_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE TABLE IF NOT EXISTS "mp_message" (
    "id" bigint NOT NULL DEFAULT nextval('mp_message_seq'),
    "msg_id" bigint DEFAULT NULL,
    "account_id" bigint NOT NULL,
    "app_id" varchar(255) NOT NULL DEFAULT '',
    "user_id" bigint DEFAULT NULL,
    "openid" varchar(255) DEFAULT NULL,
    "type" varchar(64) NOT NULL DEFAULT '',
    "send_from" integer NOT NULL,
    "content" text DEFAULT NULL,
    "media_id" varchar(255) DEFAULT NULL,
    "media_url" varchar(1024) DEFAULT NULL,
    "recognition" text DEFAULT NULL,
    "format" varchar(64) DEFAULT NULL,
    "title" varchar(255) DEFAULT NULL,
    "description" text DEFAULT NULL,
    "thumb_media_id" varchar(255) DEFAULT NULL,
    "thumb_media_url" varchar(1024) DEFAULT NULL,
    "url" varchar(1024) DEFAULT NULL,
    "location_x" double precision DEFAULT NULL,
    "location_y" double precision DEFAULT NULL,
    "scale" double precision DEFAULT NULL,
    "label" varchar(1024) DEFAULT NULL,
    "articles" text DEFAULT NULL,
    "music_url" varchar(1024) DEFAULT NULL,
    "hq_music_url" varchar(1024) DEFAULT NULL,
    "event" varchar(64) DEFAULT NULL,
    "event_key" varchar(255) DEFAULT NULL,
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" boolean NOT NULL DEFAULT FALSE,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "mp_message" IS '公众号消息';

CREATE SEQUENCE IF NOT EXISTS mp_message_template_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE TABLE IF NOT EXISTS "mp_message_template" (
    "id" bigint NOT NULL DEFAULT nextval('mp_message_template_seq'),
    "account_id" bigint NOT NULL,
    "app_id" varchar(255) NOT NULL DEFAULT '',
    "template_id" varchar(255) NOT NULL DEFAULT '',
    "title" varchar(255) NOT NULL DEFAULT '',
    "content" text DEFAULT NULL,
    "example" text DEFAULT NULL,
    "primary_industry" varchar(255) DEFAULT NULL,
    "deputy_industry" varchar(255) DEFAULT NULL,
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" boolean NOT NULL DEFAULT FALSE,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "mp_message_template" IS '公众号模版消息';

CREATE SEQUENCE IF NOT EXISTS mp_tag_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE TABLE IF NOT EXISTS "mp_tag" (
    "id" bigint NOT NULL DEFAULT nextval('mp_tag_seq'),
    "tag_id" bigint NOT NULL,
    "name" varchar(255) NOT NULL DEFAULT '',
    "count" integer NOT NULL DEFAULT 0,
    "account_id" bigint NOT NULL,
    "app_id" varchar(255) NOT NULL DEFAULT '',
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" boolean NOT NULL DEFAULT FALSE,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "mp_tag" IS '公众号标签';

CREATE SEQUENCE IF NOT EXISTS mp_user_seq START WITH 1 INCREMENT BY 1 NO MINVALUE NO MAXVALUE CACHE 1;
CREATE TABLE IF NOT EXISTS "mp_user" (
    "id" bigint NOT NULL DEFAULT nextval('mp_user_seq'),
    "openid" varchar(255) NOT NULL DEFAULT '',
    "union_id" varchar(255) DEFAULT NULL,
    "subscribe_status" integer NOT NULL DEFAULT 0,
    "subscribe_time" timestamp DEFAULT NULL,
    "unsubscribe_time" timestamp DEFAULT NULL,
    "nickname" varchar(255) DEFAULT NULL,
    "head_image_url" varchar(1024) DEFAULT NULL,
    "language" varchar(64) DEFAULT NULL,
    "country" varchar(255) DEFAULT NULL,
    "province" varchar(255) DEFAULT NULL,
    "city" varchar(255) DEFAULT NULL,
    "remark" varchar(1024) DEFAULT NULL,
    "tag_ids" varchar(1024) DEFAULT NULL,
    "account_id" bigint NOT NULL,
    "app_id" varchar(255) NOT NULL DEFAULT '',
    "creator" varchar(64) DEFAULT '',
    "create_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updater" varchar(64) DEFAULT '',
    "update_time" timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted" boolean NOT NULL DEFAULT FALSE,
    PRIMARY KEY ("id")
);
COMMENT ON TABLE "mp_user" IS '微信公众号粉丝';
