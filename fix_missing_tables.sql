-- 修复缺失的 PostgreSQL 表
-- trade_brokerage_user, trade_delivery_express, trade_delivery_express_template,
-- trade_delivery_pick_up_store, trade_statistics 等

-- ----------------------------
-- Table structure for trade_brokerage_user
-- ----------------------------
DROP TABLE IF EXISTS trade_brokerage_user;
CREATE TABLE trade_brokerage_user (
    id int8 NOT NULL,
  bind_user_id int8 NULL DEFAULT NULL,
  bind_user_time timestamp NULL DEFAULT NULL,
  brokerage_enabled bool NOT NULL DEFAULT '1',
  brokerage_time timestamp NULL DEFAULT NULL,
  brokerage_price int4 NOT NULL DEFAULT 0,
  frozen_price int4 NOT NULL DEFAULT 0,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted boolean NOT NULL DEFAULT FALSE,
  tenant_id int8 NOT NULL DEFAULT 0
);

ALTER TABLE trade_brokerage_user ADD CONSTRAINT pk_trade_brokerage_user PRIMARY KEY (id);

COMMENT ON COLUMN trade_brokerage_user.id IS '用户编号';
COMMENT ON COLUMN trade_brokerage_user.bind_user_id IS '推广员编号';
COMMENT ON COLUMN trade_brokerage_user.bind_user_time IS '推广员绑定时间';
COMMENT ON COLUMN trade_brokerage_user.brokerage_enabled IS '是否成为推广员';
COMMENT ON COLUMN trade_brokerage_user.brokerage_time IS '成为分销员时间';
COMMENT ON COLUMN trade_brokerage_user.brokerage_price IS '可用佣金';
COMMENT ON COLUMN trade_brokerage_user.frozen_price IS '冻结佣金';
COMMENT ON COLUMN trade_brokerage_user.creator IS '创建者';
COMMENT ON COLUMN trade_brokerage_user.create_time IS '创建时间';
COMMENT ON COLUMN trade_brokerage_user.updater IS '更新者';
COMMENT ON COLUMN trade_brokerage_user.update_time IS '更新时间';
COMMENT ON COLUMN trade_brokerage_user.deleted IS '是否删除';
COMMENT ON COLUMN trade_brokerage_user.tenant_id IS '租户编号';
COMMENT ON TABLE trade_brokerage_user IS '分销用户';

DROP SEQUENCE IF EXISTS trade_brokerage_user_seq;
CREATE SEQUENCE trade_brokerage_user_seq START 300;

-- ----------------------------
-- Table structure for trade_delivery_express
-- ----------------------------
DROP TABLE IF EXISTS trade_delivery_express;
CREATE TABLE trade_delivery_express (
    id int8 NOT NULL,
  code varchar(64) NOT NULL,
  name varchar(64) NOT NULL,
  logo varchar(256) NULL DEFAULT NULL,
  sort int4 NOT NULL DEFAULT 0,
  status int2 NOT NULL DEFAULT 0,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted boolean NOT NULL DEFAULT FALSE,
  tenant_id int8 NOT NULL DEFAULT 0
);

ALTER TABLE trade_delivery_express ADD CONSTRAINT pk_trade_delivery_express PRIMARY KEY (id);

COMMENT ON COLUMN trade_delivery_express.id IS '编号';
COMMENT ON COLUMN trade_delivery_express.code IS '快递公司编码';
COMMENT ON COLUMN trade_delivery_express.name IS '快递公司名称';
COMMENT ON COLUMN trade_delivery_express.logo IS '快递公司 logo';
COMMENT ON COLUMN trade_delivery_express.sort IS '排序';
COMMENT ON COLUMN trade_delivery_express.status IS '状态';
COMMENT ON COLUMN trade_delivery_express.creator IS '创建者';
COMMENT ON COLUMN trade_delivery_express.create_time IS '创建时间';
COMMENT ON COLUMN trade_delivery_express.updater IS '更新者';
COMMENT ON COLUMN trade_delivery_express.update_time IS '更新时间';
COMMENT ON COLUMN trade_delivery_express.deleted IS '是否删除';
COMMENT ON COLUMN trade_delivery_express.tenant_id IS '租户编号';
COMMENT ON TABLE trade_delivery_express IS '快递公司';

-- 插入初始数据
INSERT INTO trade_delivery_express (id, code, name, logo, sort, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (1, 'STO', '申通快递', '', 1, 0, '1', CURRENT_TIMESTAMP, '1', CURRENT_TIMESTAMP, false, 1);
INSERT INTO trade_delivery_express (id, code, name, logo, sort, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (2, 'SF', '顺丰速运', '', 2, 0, '1', CURRENT_TIMESTAMP, '1', CURRENT_TIMESTAMP, false, 1);
INSERT INTO trade_delivery_express (id, code, name, logo, sort, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (3, 'ZTO', '中通快递', '', 3, 0, '1', CURRENT_TIMESTAMP, '1', CURRENT_TIMESTAMP, false, 1);
INSERT INTO trade_delivery_express (id, code, name, logo, sort, status, creator, create_time, updater, update_time, deleted, tenant_id) VALUES (4, 'YD', '韵达快递', '', 4, 0, '1', CURRENT_TIMESTAMP, '1', CURRENT_TIMESTAMP, false, 1);

DROP SEQUENCE IF EXISTS trade_delivery_express_seq;
CREATE SEQUENCE trade_delivery_express_seq START 10;

-- ----------------------------
-- Table structure for trade_delivery_express_template
-- ----------------------------
DROP TABLE IF EXISTS trade_delivery_express_template;
CREATE TABLE trade_delivery_express_template (
    id int8 NOT NULL,
  name varchar(64) NOT NULL,
  charge_mode int2 NOT NULL,
  sort int4 NOT NULL DEFAULT 0,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted boolean NOT NULL DEFAULT FALSE,
  tenant_id int8 NOT NULL DEFAULT 0
);

ALTER TABLE trade_delivery_express_template ADD CONSTRAINT pk_trade_delivery_express_template PRIMARY KEY (id);

COMMENT ON COLUMN trade_delivery_express_template.id IS '编号';
COMMENT ON COLUMN trade_delivery_express_template.name IS '模板名称';
COMMENT ON COLUMN trade_delivery_express_template.charge_mode IS '配送计费方式';
COMMENT ON COLUMN trade_delivery_express_template.sort IS '排序';
COMMENT ON COLUMN trade_delivery_express_template.creator IS '创建者';
COMMENT ON COLUMN trade_delivery_express_template.create_time IS '创建时间';
COMMENT ON COLUMN trade_delivery_express_template.updater IS '更新者';
COMMENT ON COLUMN trade_delivery_express_template.update_time IS '更新时间';
COMMENT ON COLUMN trade_delivery_express_template.deleted IS '是否删除';
COMMENT ON COLUMN trade_delivery_express_template.tenant_id IS '租户编号';
COMMENT ON TABLE trade_delivery_express_template IS '快递运费模板';

DROP SEQUENCE IF EXISTS trade_delivery_express_template_seq;
CREATE SEQUENCE trade_delivery_express_template_seq START 10;

-- ----------------------------
-- Table structure for trade_delivery_express_template_charge
-- ----------------------------
DROP TABLE IF EXISTS trade_delivery_express_template_charge;
CREATE TABLE trade_delivery_express_template_charge (
    id int8 NOT NULL,
  template_id int8 NOT NULL,
  area_ids text NOT NULL,
  start_count float8 NOT NULL DEFAULT 0,
  start_price int4 NOT NULL DEFAULT 0,
  extra_count float8 NOT NULL DEFAULT 0,
  extra_price int4 NOT NULL DEFAULT 0,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted boolean NOT NULL DEFAULT FALSE,
  tenant_id int8 NOT NULL DEFAULT 0
);

ALTER TABLE trade_delivery_express_template_charge ADD CONSTRAINT pk_trade_delivery_express_template_charge PRIMARY KEY (id);

COMMENT ON TABLE trade_delivery_express_template_charge IS '快递运费模板计费配置';

DROP SEQUENCE IF EXISTS trade_delivery_express_template_charge_seq;
CREATE SEQUENCE trade_delivery_express_template_charge_seq START 10;

-- ----------------------------
-- Table structure for trade_delivery_express_template_free
-- ----------------------------
DROP TABLE IF EXISTS trade_delivery_express_template_free;
CREATE TABLE trade_delivery_express_template_free (
    id int8 NOT NULL,
  template_id int8 NOT NULL,
  area_ids text NOT NULL,
  free_count int4 NOT NULL DEFAULT 0,
  free_price int4 NOT NULL DEFAULT 0,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted boolean NOT NULL DEFAULT FALSE,
  tenant_id int8 NOT NULL DEFAULT 0
);

ALTER TABLE trade_delivery_express_template_free ADD CONSTRAINT pk_trade_delivery_express_template_free PRIMARY KEY (id);

COMMENT ON TABLE trade_delivery_express_template_free IS '快递运费模板包邮配置';

DROP SEQUENCE IF EXISTS trade_delivery_express_template_free_seq;
CREATE SEQUENCE trade_delivery_express_template_free_seq START 10;

-- ----------------------------
-- Table structure for trade_delivery_pick_up_store
-- ----------------------------
DROP TABLE IF EXISTS trade_delivery_pick_up_store;
CREATE TABLE trade_delivery_pick_up_store (
    id int8 NOT NULL,
  name varchar(64) NOT NULL,
  introduction varchar(256) NULL DEFAULT NULL,
  phone varchar(16) NOT NULL,
  area_id int4 NOT NULL,
  detail_address varchar(256) NOT NULL,
  logo varchar(256) NOT NULL,
  opening_time date NOT NULL,
  closing_time date NOT NULL,
  latitude float8 NOT NULL,
  longitude float8 NOT NULL,
  verify_user_ids varchar(256) NULL DEFAULT NULL,
  status int2 NOT NULL DEFAULT 0,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted boolean NOT NULL DEFAULT FALSE,
  tenant_id int8 NOT NULL DEFAULT 0
);

ALTER TABLE trade_delivery_pick_up_store ADD CONSTRAINT pk_trade_delivery_pick_up_store PRIMARY KEY (id);

COMMENT ON COLUMN trade_delivery_pick_up_store.id IS '编号';
COMMENT ON COLUMN trade_delivery_pick_up_store.name IS '门店名称';
COMMENT ON COLUMN trade_delivery_pick_up_store.introduction IS '门店简介';
COMMENT ON COLUMN trade_delivery_pick_up_store.phone IS '门店手机';
COMMENT ON COLUMN trade_delivery_pick_up_store.area_id IS '区域编号';
COMMENT ON COLUMN trade_delivery_pick_up_store.detail_address IS '门店详细地址';
COMMENT ON COLUMN trade_delivery_pick_up_store.logo IS '门店 logo';
COMMENT ON COLUMN trade_delivery_pick_up_store.opening_time IS '营业开始时间';
COMMENT ON COLUMN trade_delivery_pick_up_store.closing_time IS '营业结束时间';
COMMENT ON COLUMN trade_delivery_pick_up_store.latitude IS '纬度';
COMMENT ON COLUMN trade_delivery_pick_up_store.longitude IS '经度';
COMMENT ON COLUMN trade_delivery_pick_up_store.verify_user_ids IS '核销用户编号数组';
COMMENT ON COLUMN trade_delivery_pick_up_store.status IS '门店状态';
COMMENT ON COLUMN trade_delivery_pick_up_store.creator IS '创建者';
COMMENT ON COLUMN trade_delivery_pick_up_store.create_time IS '创建时间';
COMMENT ON COLUMN trade_delivery_pick_up_store.updater IS '更新者';
COMMENT ON COLUMN trade_delivery_pick_up_store.update_time IS '更新时间';
COMMENT ON COLUMN trade_delivery_pick_up_store.deleted IS '是否删除';
COMMENT ON COLUMN trade_delivery_pick_up_store.tenant_id IS '租户编号';
COMMENT ON TABLE trade_delivery_pick_up_store IS '自提门店';

DROP SEQUENCE IF EXISTS trade_delivery_pick_up_store_seq;
CREATE SEQUENCE trade_delivery_pick_up_store_seq START 10;

-- ----------------------------
-- Table structure for trade_statistics
-- ----------------------------
DROP TABLE IF EXISTS trade_statistics;
CREATE TABLE trade_statistics (
    id int8 NOT NULL,
  time timestamp NOT NULL,
  order_create_count int4 NOT NULL DEFAULT 0,
  order_pay_count int4 NOT NULL DEFAULT 0,
  order_pay_price int4 NOT NULL DEFAULT 0,
  after_sale_count int4 NOT NULL DEFAULT 0,
  after_sale_refund_price int4 NOT NULL DEFAULT 0,
  brokerage_settlement_price int4 NOT NULL DEFAULT 0,
  wallet_pay_price int4 NOT NULL DEFAULT 0,
  recharge_pay_count int4 NOT NULL DEFAULT 0,
  recharge_pay_price int4 NOT NULL DEFAULT 0,
  recharge_refund_count int4 NOT NULL DEFAULT 0,
  recharge_refund_price int4 NOT NULL DEFAULT 0,
  creator varchar(64) NULL DEFAULT '',
  create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater varchar(64) NULL DEFAULT '',
  update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted boolean NOT NULL DEFAULT FALSE,
  tenant_id int8 NOT NULL DEFAULT 0
);

ALTER TABLE trade_statistics ADD CONSTRAINT pk_trade_statistics PRIMARY KEY (id);

CREATE INDEX idx_trade_statistics_01 ON trade_statistics (time);

COMMENT ON COLUMN trade_statistics.id IS '编号，主键自增';
COMMENT ON COLUMN trade_statistics.time IS '统计日期';
COMMENT ON COLUMN trade_statistics.order_create_count IS '创建订单数';
COMMENT ON COLUMN trade_statistics.order_pay_count IS '支付订单商品数';
COMMENT ON COLUMN trade_statistics.order_pay_price IS '总支付金额，单位：分';
COMMENT ON COLUMN trade_statistics.after_sale_count IS '退款订单数';
COMMENT ON COLUMN trade_statistics.after_sale_refund_price IS '总退款金额，单位：分';
COMMENT ON COLUMN trade_statistics.brokerage_settlement_price IS '佣金金额（已结算），单位：分';
COMMENT ON COLUMN trade_statistics.wallet_pay_price IS '总支付金额（余额），单位：分';
COMMENT ON COLUMN trade_statistics.recharge_pay_count IS '充值订单数';
COMMENT ON COLUMN trade_statistics.recharge_pay_price IS '充值金额，单位：分';
COMMENT ON COLUMN trade_statistics.recharge_refund_count IS '充值退款订单数';
COMMENT ON COLUMN trade_statistics.recharge_refund_price IS '充值退款金额，单位：分';
COMMENT ON COLUMN trade_statistics.creator IS '创建者';
COMMENT ON COLUMN trade_statistics.create_time IS '创建时间';
COMMENT ON COLUMN trade_statistics.updater IS '更新者';
COMMENT ON COLUMN trade_statistics.update_time IS '更新时间';
COMMENT ON COLUMN trade_statistics.deleted IS '是否删除';
COMMENT ON COLUMN trade_statistics.tenant_id IS '租户编号';
COMMENT ON TABLE trade_statistics IS '交易统计表';

DROP SEQUENCE IF EXISTS trade_statistics_seq;
CREATE SEQUENCE trade_statistics_seq START 200;
