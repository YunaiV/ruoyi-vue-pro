CREATE TABLE IF NOT EXISTS yaya_legacy_user_map (
  id int8 NOT NULL,
  legacy_uuid varchar(64) NOT NULL,
  legacy_external_id varchar(120) NOT NULL,
  member_user_id int8 NOT NULL,
  migration_status varchar(40) NOT NULL DEFAULT 'mapped',
  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_legacy_user_map PRIMARY KEY (id),
  CONSTRAINT uk_yaya_legacy_uuid UNIQUE (legacy_uuid),
  CONSTRAINT uk_yaya_legacy_external_id UNIQUE (legacy_external_id)
);

CREATE TABLE IF NOT EXISTS yaya_member_plan (
  id int8 NOT NULL,
  plan_key varchar(80) NOT NULL,
  name varchar(120) NOT NULL,
  description text NOT NULL DEFAULT '',
  price_cent int8 NOT NULL DEFAULT 0,
  currency varchar(12) NOT NULL DEFAULT 'CNY',
  duration_days int4 NOT NULL DEFAULT 30,
  active int2 NOT NULL DEFAULT 1,
  benefits jsonb NOT NULL DEFAULT '{}'::jsonb,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_member_plan PRIMARY KEY (id),
  CONSTRAINT uk_yaya_member_plan_key UNIQUE (plan_key)
);

CREATE TABLE IF NOT EXISTS yaya_member_entitlement (
  id int8 NOT NULL,
  member_user_id int8 NOT NULL,
  plan_id int8 NOT NULL,
  source_type varchar(40) NOT NULL,
  source_id int8 NULL,
  status varchar(40) NOT NULL DEFAULT 'active',
  starts_at timestamp NOT NULL,
  ends_at timestamp NOT NULL,
  idempotency_key varchar(128) NOT NULL,
  metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_member_entitlement PRIMARY KEY (id),
  CONSTRAINT uk_yaya_entitlement_idempotency UNIQUE (idempotency_key)
);

CREATE INDEX IF NOT EXISTS idx_yaya_legacy_user_member ON yaya_legacy_user_map (member_user_id);
CREATE INDEX IF NOT EXISTS idx_yaya_entitlement_user_status ON yaya_member_entitlement (member_user_id, status, ends_at);
CREATE INDEX IF NOT EXISTS idx_yaya_entitlement_plan ON yaya_member_entitlement (plan_id);
