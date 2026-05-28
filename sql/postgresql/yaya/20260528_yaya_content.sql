CREATE TABLE IF NOT EXISTS yaya_content_module (
  id int8 NOT NULL,
  module_key varchar(80) NOT NULL,
  name varchar(120) NOT NULL,
  description text NOT NULL DEFAULT '',
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_content_module PRIMARY KEY (id),
  CONSTRAINT uk_yaya_content_module_key UNIQUE (module_key)
);

CREATE TABLE IF NOT EXISTS yaya_content_season (
  id int8 NOT NULL,
  season_key varchar(40) NOT NULL,
  name varchar(120) NOT NULL,
  active int2 NOT NULL DEFAULT 1,
  defaulted int2 NOT NULL DEFAULT 0,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_content_season PRIMARY KEY (id),
  CONSTRAINT uk_yaya_content_season_key UNIQUE (season_key)
);

CREATE TABLE IF NOT EXISTS yaya_import_batch (
  id int8 NOT NULL,
  season_key varchar(40) NOT NULL,
  source_label varchar(120) NOT NULL,
  status varchar(40) NOT NULL,
  summary jsonb NOT NULL DEFAULT '{}'::jsonb,
  created_by varchar(120) NOT NULL DEFAULT 'local-admin',
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_import_batch PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS yaya_source_document (
  id int8 NOT NULL,
  import_batch_id int8 NOT NULL,
  part int2 NOT NULL,
  path text NOT NULL,
  checksum varchar(128) NOT NULL,
  parser_version varchar(80) NOT NULL DEFAULT '26q1-csv-v1',
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_source_document PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS yaya_source_topic_snapshot (
  id int8 NOT NULL,
  import_batch_id int8 NOT NULL,
  source_document_id int8 NOT NULL,
  part int2 NOT NULL,
  source_row int4 NOT NULL,
  source_hash varchar(128) NOT NULL,
  raw_data jsonb NOT NULL DEFAULT '{}'::jsonb,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_source_topic_snapshot PRIMARY KEY (id),
  CONSTRAINT uk_yaya_source_topic_hash UNIQUE (source_hash)
);

CREATE TABLE IF NOT EXISTS yaya_practice_topic (
  id int8 NOT NULL,
  legacy_uuid varchar(64) NULL,
  season_id int8 NOT NULL,
  source_snapshot_id int8 NULL,
  part int2 NOT NULL,
  stable_key varchar(180) NOT NULL,
  topic_no int4 NULL,
  title_en text NOT NULL DEFAULT '',
  title_zh text NOT NULL DEFAULT '',
  topic_type varchar(80) NOT NULL DEFAULT '',
  category varchar(120) NOT NULL DEFAULT '',
  prompt_en text NOT NULL DEFAULT '',
  prompt_zh text NOT NULL DEFAULT '',
  display_order int4 NOT NULL DEFAULT 0,
  review_status varchar(40) NOT NULL DEFAULT 'draft',
  publish_status varchar(40) NOT NULL DEFAULT 'draft',
  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_practice_topic PRIMARY KEY (id),
  CONSTRAINT uk_yaya_topic_stable_key UNIQUE (season_id, part, stable_key)
);

CREATE TABLE IF NOT EXISTS yaya_practice_question (
  id int8 NOT NULL,
  legacy_uuid varchar(64) NULL,
  topic_id int8 NOT NULL,
  question_role varchar(60) NOT NULL DEFAULT 'question',
  prompt_en text NOT NULL DEFAULT '',
  prompt_zh text NOT NULL DEFAULT '',
  cue_bullets jsonb NOT NULL DEFAULT '[]'::jsonb,
  display_order int4 NOT NULL DEFAULT 0,
  prepare_seconds int4 NULL,
  answer_seconds int4 NULL,
  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_practice_question PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS yaya_topic_relation (
  id int8 NOT NULL,
  source_topic_id int8 NOT NULL,
  target_topic_id int8 NOT NULL,
  relation_type varchar(80) NOT NULL DEFAULT 'part2_part3',
  confidence varchar(40) NOT NULL DEFAULT 'manual',
  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_topic_relation PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS yaya_tag (
  id int8 NOT NULL,
  tag_key varchar(80) NOT NULL,
  label varchar(120) NOT NULL,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_tag PRIMARY KEY (id),
  CONSTRAINT uk_yaya_tag_key UNIQUE (tag_key)
);

CREATE TABLE IF NOT EXISTS yaya_topic_tag (
  id int8 NOT NULL,
  topic_id int8 NOT NULL,
  tag_id int8 NOT NULL,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_topic_tag PRIMARY KEY (id),
  CONSTRAINT uk_yaya_topic_tag UNIQUE (topic_id, tag_id)
);

CREATE INDEX IF NOT EXISTS idx_yaya_source_document_batch ON yaya_source_document (import_batch_id, part);
CREATE INDEX IF NOT EXISTS idx_yaya_source_snapshot_document ON yaya_source_topic_snapshot (source_document_id, source_row);
CREATE INDEX IF NOT EXISTS idx_yaya_topic_season_part ON yaya_practice_topic (season_id, part, publish_status);
CREATE INDEX IF NOT EXISTS idx_yaya_topic_snapshot ON yaya_practice_topic (source_snapshot_id);
CREATE INDEX IF NOT EXISTS idx_yaya_question_topic ON yaya_practice_question (topic_id, display_order);
CREATE INDEX IF NOT EXISTS idx_yaya_topic_relation_source ON yaya_topic_relation (source_topic_id, relation_type);
CREATE INDEX IF NOT EXISTS idx_yaya_topic_relation_target ON yaya_topic_relation (target_topic_id, relation_type);
