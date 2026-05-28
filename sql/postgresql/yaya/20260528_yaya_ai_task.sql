CREATE SEQUENCE IF NOT EXISTS yaya_recording_seq;

CREATE TABLE IF NOT EXISTS yaya_recording (
  id int8 NOT NULL,
  member_user_id int8 NOT NULL,
  topic_id int8 NULL,
  question_id int8 NULL,
  attempt_id int8 NULL,
  storage_path text NOT NULL,
  mime_type varchar(120) NOT NULL,
  size_bytes int8 NOT NULL DEFAULT 0,
  duration_seconds numeric(10,2) NULL,
  checksum varchar(128) NOT NULL DEFAULT '',
  status varchar(40) NOT NULL DEFAULT 'stored',
  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_recording PRIMARY KEY (id)
);

CREATE SEQUENCE IF NOT EXISTS yaya_transcript_seq;

CREATE TABLE IF NOT EXISTS yaya_transcript (
  id int8 NOT NULL,
  recording_id int8 NOT NULL,
  source varchar(80) NOT NULL DEFAULT 'manual',
  text text NOT NULL DEFAULT '',
  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_transcript PRIMARY KEY (id)
);

CREATE SEQUENCE IF NOT EXISTS yaya_ai_task_seq;

CREATE TABLE IF NOT EXISTS yaya_ai_task (
  id int8 NOT NULL,
  member_user_id int8 NULL,
  recording_id int8 NULL,
  topic_id int8 NULL,
  task_key varchar(64) NOT NULL,
  task_type varchar(40) NOT NULL DEFAULT 'evaluation',
  status varchar(40) NOT NULL DEFAULT 'PENDING',
  request_payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  response_payload jsonb NOT NULL DEFAULT '{}'::jsonb,
  result_payload jsonb NULL,
  error_payload jsonb NULL,
  accepted_at timestamp NULL,
  completed_at timestamp NULL,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_ai_task PRIMARY KEY (id),
  CONSTRAINT uk_yaya_ai_task_key UNIQUE (task_key)
);

ALTER TABLE yaya_ai_task ADD COLUMN IF NOT EXISTS member_user_id int8 NULL;
ALTER TABLE yaya_ai_task ADD COLUMN IF NOT EXISTS recording_id int8 NULL;
ALTER TABLE yaya_ai_task ADD COLUMN IF NOT EXISTS topic_id int8 NULL;
ALTER TABLE yaya_ai_task ALTER COLUMN task_type SET DEFAULT 'evaluation';
ALTER TABLE yaya_ai_task ADD COLUMN IF NOT EXISTS response_payload jsonb NOT NULL DEFAULT '{}'::jsonb;
ALTER TABLE yaya_ai_task ADD COLUMN IF NOT EXISTS accepted_at timestamp NULL;
ALTER TABLE yaya_ai_task ADD COLUMN IF NOT EXISTS completed_at timestamp NULL;

CREATE SEQUENCE IF NOT EXISTS yaya_evaluation_seq;

CREATE TABLE IF NOT EXISTS yaya_evaluation (
  id int8 NOT NULL,
  member_user_id int8 NOT NULL,
  recording_id int8 NULL,
  topic_id int8 NULL,
  ai_task_id int8 NULL,
  status varchar(40) NOT NULL DEFAULT 'PENDING',
  provider varchar(80) NOT NULL DEFAULT '',
  model varchar(120) NOT NULL DEFAULT '',
  score_overall numeric(4,1) NULL,
  scores jsonb NOT NULL DEFAULT '{}'::jsonb,
  report jsonb NULL,
  text_route_scores jsonb NULL,
  audio_route_scores jsonb NULL,
  pron_route_scores jsonb NULL,
  fluency_metrics jsonb NULL,
  band_lower numeric(4,1) NULL,
  band_upper numeric(4,1) NULL,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_evaluation PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_yaya_recording_user ON yaya_recording (member_user_id, create_time);
CREATE INDEX IF NOT EXISTS idx_yaya_recording_topic ON yaya_recording (topic_id, create_time);
CREATE INDEX IF NOT EXISTS idx_yaya_transcript_recording ON yaya_transcript (recording_id);
CREATE INDEX IF NOT EXISTS idx_yaya_ai_task_status ON yaya_ai_task (status, create_time);
CREATE INDEX IF NOT EXISTS idx_yaya_evaluation_user ON yaya_evaluation (member_user_id, create_time);
CREATE INDEX IF NOT EXISTS idx_yaya_evaluation_task ON yaya_evaluation (ai_task_id);
