CREATE SEQUENCE IF NOT EXISTS yaya_favorite_seq;

CREATE TABLE IF NOT EXISTS yaya_favorite (
  id int8 NOT NULL,
  member_user_id int8 NOT NULL,
  topic_id int8 NOT NULL,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_favorite PRIMARY KEY (id),
  CONSTRAINT uk_yaya_favorite_user_topic UNIQUE (member_user_id, topic_id)
);

CREATE SEQUENCE IF NOT EXISTS yaya_user_topic_state_seq;

CREATE TABLE IF NOT EXISTS yaya_user_topic_state (
  id int8 NOT NULL,
  member_user_id int8 NOT NULL,
  topic_id int8 NOT NULL,
  practiced boolean NOT NULL DEFAULT false,
  attempt_count int4 NOT NULL DEFAULT 0,
  last_attempt_id int8 NULL,
  last_practiced_at timestamp NULL,
  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_user_topic_state PRIMARY KEY (id),
  CONSTRAINT uk_yaya_user_topic_state UNIQUE (member_user_id, topic_id)
);

CREATE SEQUENCE IF NOT EXISTS yaya_practice_attempt_seq;

CREATE TABLE IF NOT EXISTS yaya_practice_attempt (
  id int8 NOT NULL,
  member_user_id int8 NOT NULL,
  topic_id int8 NOT NULL,
  status varchar(40) NOT NULL DEFAULT 'submitted',
  answer_text text NOT NULL DEFAULT '',
  duration_seconds int4 NULL,
  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_practice_attempt PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_yaya_favorite_member ON yaya_favorite (member_user_id);
CREATE INDEX IF NOT EXISTS idx_yaya_topic_state_member ON yaya_user_topic_state (member_user_id);
CREATE INDEX IF NOT EXISTS idx_yaya_attempt_user_topic ON yaya_practice_attempt (member_user_id, topic_id, create_time);
