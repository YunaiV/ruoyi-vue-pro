CREATE SEQUENCE IF NOT EXISTS pay_app_seq;
CREATE SEQUENCE IF NOT EXISTS pay_order_seq;
CREATE SEQUENCE IF NOT EXISTS yaya_member_order_seq;

CREATE TABLE IF NOT EXISTS pay_app (
  id int8 NOT NULL,
  app_key varchar(64) NOT NULL,
  name varchar(64) NOT NULL,
  status int2 NOT NULL,
  remark varchar(255) NULL,
  order_notify_url varchar(1024) NOT NULL,
  refund_notify_url varchar(1024) NOT NULL,
  transfer_notify_url varchar(1024) NULL,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_pay_app PRIMARY KEY (id)
);

ALTER TABLE pay_app ADD COLUMN IF NOT EXISTS tenant_id int8 NOT NULL DEFAULT 0;
DROP INDEX IF EXISTS uk_pay_app_app_key;
CREATE UNIQUE INDEX IF NOT EXISTS uk_pay_app_tenant_app_key
  ON pay_app (tenant_id, app_key)
  WHERE deleted = 0;

UPDATE pay_app
SET tenant_id = 1
WHERE app_key = 'yaya' AND tenant_id = 0 AND deleted = 0;

SELECT setval(
  'pay_app_seq',
  GREATEST((SELECT COALESCE(MAX(id), 0) FROM pay_app), 1),
  (SELECT COALESCE(MAX(id), 0) FROM pay_app) > 0
);

INSERT INTO pay_app (
  id, app_key, name, status, remark, order_notify_url, refund_notify_url, transfer_notify_url,
  creator, create_time, updater, update_time, deleted, tenant_id
)
SELECT
  nextval('pay_app_seq'), 'yaya', 'Yaya Membership', 0, 'Yaya member order app',
  'http://127.0.0.1:48080/app-api/yaya/pay/notify/order',
  'http://127.0.0.1:48080/app-api/yaya/pay/notify/refund',
  'http://127.0.0.1:48080/app-api/yaya/pay/notify/transfer',
  '1', CURRENT_TIMESTAMP, '1', CURRENT_TIMESTAMP, 0, 1
WHERE NOT EXISTS (
  SELECT 1 FROM pay_app WHERE app_key = 'yaya' AND tenant_id = 1 AND deleted = 0
);

SELECT setval(
  'pay_app_seq',
  GREATEST((SELECT COALESCE(MAX(id), 0) FROM pay_app), 1),
  (SELECT COALESCE(MAX(id), 0) FROM pay_app) > 0
);

CREATE TABLE IF NOT EXISTS pay_order (
  id int8 NOT NULL,
  app_id int8 NOT NULL,
  channel_id int8 NULL,
  channel_code varchar(32) NULL,
  user_id int8 NULL,
  user_type int2 NULL,
  merchant_order_id varchar(64) NOT NULL,
  subject varchar(32) NOT NULL,
  body varchar(128) NOT NULL,
  notify_url varchar(1024) NOT NULL,
  price int4 NOT NULL,
  channel_fee_rate float8 NULL DEFAULT 0,
  channel_fee_price int4 NULL DEFAULT 0,
  status int2 NOT NULL,
  user_ip varchar(50) NOT NULL,
  expire_time timestamp NOT NULL,
  success_time timestamp NULL,
  extension_id int8 NULL,
  no varchar(64) NULL,
  refund_price int4 NOT NULL DEFAULT 0,
  channel_user_id varchar(255) NULL,
  channel_order_no varchar(64) NULL,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_pay_order PRIMARY KEY (id)
);

ALTER TABLE pay_order ADD COLUMN IF NOT EXISTS tenant_id int8 NOT NULL DEFAULT 0;
CREATE UNIQUE INDEX IF NOT EXISTS uk_pay_order_app_merchant
  ON pay_order (app_id, merchant_order_id)
  WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_pay_order_status_expire
  ON pay_order (status, expire_time);
CREATE INDEX IF NOT EXISTS idx_pay_order_app_status
  ON pay_order (app_id, status);

SELECT setval(
  'pay_order_seq',
  GREATEST((SELECT COALESCE(MAX(id), 0) FROM pay_order), 1),
  (SELECT COALESCE(MAX(id), 0) FROM pay_order) > 0
);

CREATE TABLE IF NOT EXISTS yaya_member_order (
  id int8 NOT NULL,
  member_user_id int8 NOT NULL,
  plan_id int8 NOT NULL,
  plan_key varchar(80) NOT NULL,
  price_cent int8 NOT NULL DEFAULT 0,
  currency varchar(12) NOT NULL DEFAULT 'CNY',
  status varchar(40) NOT NULL,
  pay_order_id int8 NULL,
  channel_code varchar(64) NULL,
  pay_channel_code varchar(64) NULL,
  paid_at timestamp NULL,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted int2 NOT NULL DEFAULT 0,
  tenant_id int8 NOT NULL DEFAULT 0,
  CONSTRAINT pk_yaya_member_order PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_yaya_member_order_pay_order
  ON yaya_member_order (pay_order_id)
  WHERE pay_order_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_yaya_member_order_user_status
  ON yaya_member_order (member_user_id, status);
CREATE INDEX IF NOT EXISTS idx_yaya_member_order_plan
  ON yaya_member_order (plan_id);

SELECT setval(
  'yaya_member_order_seq',
  GREATEST((SELECT COALESCE(MAX(id), 0) FROM yaya_member_order), 1),
  (SELECT COALESCE(MAX(id), 0) FROM yaya_member_order) > 0
);
