/*
 PostgreSQL Schema for CRM Module
 Converted from MySQL crm-2024-09-30.sql
 Date: 2025-11-13
*/

-- 创建序列
CREATE SEQUENCE IF NOT EXISTS crm_business_seq START WITH 15;
CREATE SEQUENCE IF NOT EXISTS crm_business_product_seq START WITH 37;
CREATE SEQUENCE IF NOT EXISTS crm_business_status_seq START WITH 12;
CREATE SEQUENCE IF NOT EXISTS crm_business_status_type_seq START WITH 7;
CREATE SEQUENCE IF NOT EXISTS crm_clue_seq START WITH 9;
CREATE SEQUENCE IF NOT EXISTS crm_contact_seq START WITH 18;
CREATE SEQUENCE IF NOT EXISTS crm_contact_business_seq START WITH 37;
CREATE SEQUENCE IF NOT EXISTS crm_contract_seq START WITH 14;
CREATE SEQUENCE IF NOT EXISTS crm_contract_config_seq START WITH 2;
CREATE SEQUENCE IF NOT EXISTS crm_contract_product_seq START WITH 49;
CREATE SEQUENCE IF NOT EXISTS crm_customer_seq START WITH 18;
CREATE SEQUENCE IF NOT EXISTS crm_customer_limit_config_seq START WITH 3;
CREATE SEQUENCE IF NOT EXISTS crm_customer_pool_config_seq START WITH 2;
CREATE SEQUENCE IF NOT EXISTS crm_follow_up_record_seq START WITH 34;
CREATE SEQUENCE IF NOT EXISTS crm_permission_seq START WITH 119;
CREATE SEQUENCE IF NOT EXISTS crm_product_seq START WITH 7;
CREATE SEQUENCE IF NOT EXISTS crm_product_category_seq START WITH 7;
CREATE SEQUENCE IF NOT EXISTS crm_receivable_seq START WITH 31;
CREATE SEQUENCE IF NOT EXISTS crm_receivable_plan_seq START WITH 9;

-- ----------------------------
-- Table structure for crm_business
-- ----------------------------
DROP TABLE IF EXISTS crm_business;
CREATE TABLE crm_business (
  id BIGINT NOT NULL DEFAULT nextval('crm_business_seq'),
  name VARCHAR(100) NOT NULL,
  customer_id BIGINT NOT NULL,
  follow_up_status BOOLEAN DEFAULT FALSE,
  contact_last_time TIMESTAMP DEFAULT NULL,
  contact_next_time TIMESTAMP DEFAULT NULL,
  owner_user_id BIGINT DEFAULT NULL,
  status_type_id BIGINT DEFAULT NULL,
  status_id BIGINT DEFAULT NULL,
  end_status SMALLINT DEFAULT NULL,
  deal_time TIMESTAMP DEFAULT NULL,
  total_product_price NUMERIC(24, 6) DEFAULT NULL,
  discount_percent NUMERIC(24, 6) DEFAULT NULL,
  total_price NUMERIC(24, 6) DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  creator VARCHAR(64) NOT NULL DEFAULT '',
  updater VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  end_remark VARCHAR(500) DEFAULT NULL,
  deleted BOOLEAN DEFAULT FALSE,
  tenant_id BIGINT DEFAULT 0,
  PRIMARY KEY (id)
);

COMMENT ON TABLE crm_business IS 'CRM 商机表';
COMMENT ON COLUMN crm_business.id IS '编号';
COMMENT ON COLUMN crm_business.name IS '商机名称';
COMMENT ON COLUMN crm_business.customer_id IS '客户编号';
COMMENT ON COLUMN crm_business.follow_up_status IS '跟进状态';
COMMENT ON COLUMN crm_business.contact_last_time IS '最后跟进时间';
COMMENT ON COLUMN crm_business.contact_next_time IS '下次联系时间';
COMMENT ON COLUMN crm_business.owner_user_id IS '负责人的用户编号';
COMMENT ON COLUMN crm_business.status_type_id IS '商机状态类型编号';
COMMENT ON COLUMN crm_business.status_id IS '商机状态编号';
COMMENT ON COLUMN crm_business.end_status IS '结束状态：1-赢单 2-输单3-无效';
COMMENT ON COLUMN crm_business.deal_time IS '预计成交日期';
COMMENT ON COLUMN crm_business.total_product_price IS '产品总金额，单位：元';
COMMENT ON COLUMN crm_business.discount_percent IS '整单折扣，百分比';
COMMENT ON COLUMN crm_business.total_price IS '商机总金额，单位：元';
COMMENT ON COLUMN crm_business.remark IS '备注';
COMMENT ON COLUMN crm_business.creator IS '创建人';
COMMENT ON COLUMN crm_business.updater IS '更新人';
COMMENT ON COLUMN crm_business.create_time IS '创建时间';
COMMENT ON COLUMN crm_business.update_time IS '更新时间';
COMMENT ON COLUMN crm_business.end_remark IS '结束时的备注';
COMMENT ON COLUMN crm_business.deleted IS '逻辑删除';
COMMENT ON COLUMN crm_business.tenant_id IS '租户ID';

-- ----------------------------
-- Table structure for crm_business_product
-- ----------------------------
DROP TABLE IF EXISTS crm_business_product;
CREATE TABLE crm_business_product (
  id BIGINT NOT NULL DEFAULT nextval('crm_business_product_seq'),
  business_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_price NUMERIC(24, 6) NOT NULL,
  business_price NUMERIC(24, 6) NOT NULL,
  count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6) NOT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  PRIMARY KEY (id)
);

COMMENT ON TABLE crm_business_product IS 'CRM 商机产品关联表';
COMMENT ON COLUMN crm_business_product.id IS '主键';
COMMENT ON COLUMN crm_business_product.business_id IS '商机编号';
COMMENT ON COLUMN crm_business_product.product_id IS '产品编号';
COMMENT ON COLUMN crm_business_product.product_price IS '产品单价';
COMMENT ON COLUMN crm_business_product.business_price IS '商机价格';
COMMENT ON COLUMN crm_business_product.count IS '数量';
COMMENT ON COLUMN crm_business_product.total_price IS '总计价格';
COMMENT ON COLUMN crm_business_product.creator IS '创建者';
COMMENT ON COLUMN crm_business_product.create_time IS '创建时间';
COMMENT ON COLUMN crm_business_product.updater IS '更新者';
COMMENT ON COLUMN crm_business_product.update_time IS '更新时间';
COMMENT ON COLUMN crm_business_product.deleted IS '是否删除';
COMMENT ON COLUMN crm_business_product.tenant_id IS '租户编号';

-- ----------------------------
-- Table structure for crm_business_status
-- ----------------------------
DROP TABLE IF EXISTS crm_business_status;
CREATE TABLE crm_business_status (
  id BIGINT NOT NULL DEFAULT nextval('crm_business_status_seq'),
  type_id BIGINT NOT NULL,
  name VARCHAR(100) NOT NULL,
  percent INTEGER NOT NULL,
  sort INTEGER NOT NULL DEFAULT 1,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id)
);

COMMENT ON TABLE crm_business_status IS 'CRM 商机状态表';
COMMENT ON COLUMN crm_business_status.id IS '主键';
COMMENT ON COLUMN crm_business_status.type_id IS '状态类型编号';
COMMENT ON COLUMN crm_business_status.name IS '状态类型名';
COMMENT ON COLUMN crm_business_status.percent IS '赢单率';
COMMENT ON COLUMN crm_business_status.sort IS '排序';
COMMENT ON COLUMN crm_business_status.creator IS '创建者';
COMMENT ON COLUMN crm_business_status.create_time IS '创建时间';
COMMENT ON COLUMN crm_business_status.updater IS '更新者';
COMMENT ON COLUMN crm_business_status.update_time IS '更新时间';
COMMENT ON COLUMN crm_business_status.tenant_id IS '租户编号';
COMMENT ON COLUMN crm_business_status.deleted IS '是否删除';

-- ----------------------------
-- Table structure for crm_business_status_type
-- ----------------------------
DROP TABLE IF EXISTS crm_business_status_type;
CREATE TABLE crm_business_status_type (
  id BIGINT NOT NULL DEFAULT nextval('crm_business_status_type_seq'),
  name VARCHAR(100) NOT NULL,
  dept_ids VARCHAR(255) NOT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id)
);

COMMENT ON TABLE crm_business_status_type IS 'CRM 商机状态组表';
COMMENT ON COLUMN crm_business_status_type.id IS '主键';
COMMENT ON COLUMN crm_business_status_type.name IS '状态组名';
COMMENT ON COLUMN crm_business_status_type.dept_ids IS '使用的部门编号';
COMMENT ON COLUMN crm_business_status_type.creator IS '创建者';
COMMENT ON COLUMN crm_business_status_type.create_time IS '创建时间';
COMMENT ON COLUMN crm_business_status_type.updater IS '更新者';
COMMENT ON COLUMN crm_business_status_type.update_time IS '更新时间';
COMMENT ON COLUMN crm_business_status_type.tenant_id IS '租户编号';
COMMENT ON COLUMN crm_business_status_type.deleted IS '是否删除';

-- ----------------------------
-- Table structure for crm_clue
-- ----------------------------
DROP TABLE IF EXISTS crm_clue;
CREATE TABLE crm_clue (
  id BIGINT NOT NULL DEFAULT nextval('crm_clue_seq'),
  name VARCHAR(128) NOT NULL DEFAULT '',
  follow_up_status BOOLEAN DEFAULT FALSE,
  contact_last_time TIMESTAMP DEFAULT NULL,
  contact_last_content VARCHAR(255) DEFAULT NULL,
  contact_next_time TIMESTAMP DEFAULT NULL,
  owner_user_id BIGINT NOT NULL,
  transform_status BOOLEAN DEFAULT FALSE,
  customer_id BIGINT DEFAULT NULL,
  mobile VARCHAR(20) DEFAULT NULL,
  telephone VARCHAR(20) DEFAULT NULL,
  qq VARCHAR(20) DEFAULT NULL,
  wechat VARCHAR(255) DEFAULT NULL,
  email VARCHAR(255) DEFAULT NULL,
  area_id BIGINT DEFAULT NULL,
  detail_address VARCHAR(255) DEFAULT NULL,
  industry_id INTEGER DEFAULT NULL,
  level INTEGER DEFAULT NULL,
  source INTEGER DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

COMMENT ON TABLE crm_clue IS 'CRM 线索表';
COMMENT ON COLUMN crm_clue.id IS '编号，主键自增';
COMMENT ON COLUMN crm_clue.name IS '线索名称';
COMMENT ON COLUMN crm_clue.follow_up_status IS '跟进状态';
COMMENT ON COLUMN crm_clue.contact_last_time IS '最后跟进时间';
COMMENT ON COLUMN crm_clue.contact_last_content IS '最后跟进内容';
COMMENT ON COLUMN crm_clue.contact_next_time IS '下次联系时间';
COMMENT ON COLUMN crm_clue.owner_user_id IS '负责人的用户编号';
COMMENT ON COLUMN crm_clue.transform_status IS '转化状态';
COMMENT ON COLUMN crm_clue.customer_id IS '客户编号';
COMMENT ON COLUMN crm_clue.mobile IS '手机号';
COMMENT ON COLUMN crm_clue.telephone IS '电话';
COMMENT ON COLUMN crm_clue.qq IS 'QQ';
COMMENT ON COLUMN crm_clue.wechat IS '微信';
COMMENT ON COLUMN crm_clue.email IS '邮箱';
COMMENT ON COLUMN crm_clue.area_id IS '地区编号';
COMMENT ON COLUMN crm_clue.detail_address IS '详细地址';
COMMENT ON COLUMN crm_clue.industry_id IS '所属行业';
COMMENT ON COLUMN crm_clue.level IS '客户等级';
COMMENT ON COLUMN crm_clue.source IS '客户来源';
COMMENT ON COLUMN crm_clue.remark IS '备注';
COMMENT ON COLUMN crm_clue.creator IS '创建者';
COMMENT ON COLUMN crm_clue.create_time IS '创建时间';
COMMENT ON COLUMN crm_clue.updater IS '更新者';
COMMENT ON COLUMN crm_clue.update_time IS '更新时间';
COMMENT ON COLUMN crm_clue.deleted IS '是否删除';
COMMENT ON COLUMN crm_clue.tenant_id IS '租户编号';

-- ----------------------------
-- Table structure for crm_contact
-- ----------------------------
DROP TABLE IF EXISTS crm_contact;
CREATE TABLE crm_contact (
  id BIGINT NOT NULL DEFAULT nextval('crm_contact_seq'),
  name VARCHAR(128) DEFAULT NULL,
  customer_id BIGINT DEFAULT NULL,
  contact_last_time TIMESTAMP DEFAULT NULL,
  contact_last_content VARCHAR(255) DEFAULT NULL,
  contact_next_time TIMESTAMP DEFAULT NULL,
  owner_user_id VARCHAR(256) DEFAULT NULL,
  mobile VARCHAR(16) DEFAULT NULL,
  telephone VARCHAR(16) DEFAULT NULL,
  email VARCHAR(128) DEFAULT NULL,
  qq INTEGER DEFAULT NULL,
  wechat VARCHAR(128) DEFAULT NULL,
  area_id BIGINT DEFAULT NULL,
  detail_address VARCHAR(256) DEFAULT NULL,
  sex INTEGER DEFAULT NULL,
  master BOOLEAN DEFAULT NULL,
  parent_id BIGINT DEFAULT NULL,
  post VARCHAR(32) DEFAULT NULL,
  remark VARCHAR(512) DEFAULT NULL,
  creator VARCHAR(64) DEFAULT NULL,
  create_time TIMESTAMP DEFAULT NULL,
  updater VARCHAR(64) DEFAULT NULL,
  update_time TIMESTAMP DEFAULT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT DEFAULT NULL,
  PRIMARY KEY (id)
);

COMMENT ON TABLE crm_contact IS 'CRM 联系人';
COMMENT ON COLUMN crm_contact.id IS '主键';
COMMENT ON COLUMN crm_contact.name IS '联系人名称';
COMMENT ON COLUMN crm_contact.customer_id IS '客户编号';
COMMENT ON COLUMN crm_contact.contact_last_time IS '最后跟进时间';
COMMENT ON COLUMN crm_contact.contact_last_content IS '最后跟进内容';
COMMENT ON COLUMN crm_contact.contact_next_time IS '下次联系时间';
COMMENT ON COLUMN crm_contact.owner_user_id IS '负责人用户编号';
COMMENT ON COLUMN crm_contact.mobile IS '手机号';
COMMENT ON COLUMN crm_contact.telephone IS '电话';
COMMENT ON COLUMN crm_contact.email IS '电子邮箱';
COMMENT ON COLUMN crm_contact.qq IS 'QQ';
COMMENT ON COLUMN crm_contact.wechat IS '微信';
COMMENT ON COLUMN crm_contact.area_id IS '地区';
COMMENT ON COLUMN crm_contact.detail_address IS '地址';
COMMENT ON COLUMN crm_contact.sex IS '性别';
COMMENT ON COLUMN crm_contact.master IS '是否关键决策人';
COMMENT ON COLUMN crm_contact.parent_id IS '直系上属';
COMMENT ON COLUMN crm_contact.post IS '职务';
COMMENT ON COLUMN crm_contact.remark IS '备注';
COMMENT ON COLUMN crm_contact.creator IS '创建人';
COMMENT ON COLUMN crm_contact.create_time IS '创建时间';
COMMENT ON COLUMN crm_contact.updater IS '更新人';
COMMENT ON COLUMN crm_contact.update_time IS '更新时间';
COMMENT ON COLUMN crm_contact.deleted IS '是否删除';
COMMENT ON COLUMN crm_contact.tenant_id IS '租户编号';

-- ----------------------------
-- Table structure for crm_contact_business
-- ----------------------------
DROP TABLE IF EXISTS crm_contact_business;
CREATE TABLE crm_contact_business (
  id BIGINT NOT NULL DEFAULT nextval('crm_contact_business_seq'),
  contact_id BIGINT DEFAULT NULL,
  business_id BIGINT DEFAULT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

COMMENT ON TABLE crm_contact_business IS 'CRM 联系人商机关联表';
COMMENT ON COLUMN crm_contact_business.id IS '主键';
COMMENT ON COLUMN crm_contact_business.contact_id IS '联系人id';
COMMENT ON COLUMN crm_contact_business.business_id IS '商机id';
COMMENT ON COLUMN crm_contact_business.creator IS '创建者';
COMMENT ON COLUMN crm_contact_business.create_time IS '创建时间';
COMMENT ON COLUMN crm_contact_business.updater IS '更新者';
COMMENT ON COLUMN crm_contact_business.update_time IS '更新时间';
COMMENT ON COLUMN crm_contact_business.deleted IS '是否删除';
COMMENT ON COLUMN crm_contact_business.tenant_id IS '租户编号';

-- ----------------------------
-- Table structure for crm_contract
-- ----------------------------
DROP TABLE IF EXISTS crm_contract;
CREATE TABLE crm_contract (
  id BIGINT NOT NULL DEFAULT nextval('crm_contract_seq'),
  name VARCHAR(128) NOT NULL,
  no VARCHAR(128) NOT NULL,
  customer_id BIGINT NOT NULL,
  business_id BIGINT DEFAULT NULL,
  contact_last_time TIMESTAMP DEFAULT NULL,
  owner_user_id BIGINT DEFAULT NULL,
  process_instance_id VARCHAR(64) DEFAULT NULL,
  audit_status SMALLINT NOT NULL DEFAULT 0,
  order_date TIMESTAMP DEFAULT NULL,
  start_time TIMESTAMP DEFAULT NULL,
  end_time TIMESTAMP DEFAULT NULL,
  total_product_price NUMERIC(24, 6) DEFAULT NULL,
  discount_percent NUMERIC(24, 6) DEFAULT NULL,
  total_price NUMERIC(10, 2) DEFAULT NULL,
  sign_contact_id BIGINT DEFAULT NULL,
  sign_user_id BIGINT DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

COMMENT ON TABLE crm_contract IS 'CRM 合同表';
COMMENT ON COLUMN crm_contract.id IS '编号，主键自增';
COMMENT ON COLUMN crm_contract.name IS '合同名称';
COMMENT ON COLUMN crm_contract.no IS '合同编号';
COMMENT ON COLUMN crm_contract.customer_id IS '客户编号';
COMMENT ON COLUMN crm_contract.business_id IS '商机编号';
COMMENT ON COLUMN crm_contract.contact_last_time IS '最后跟进时间';
COMMENT ON COLUMN crm_contract.owner_user_id IS '负责人的用户编号';
COMMENT ON COLUMN crm_contract.process_instance_id IS '工作流编号';
COMMENT ON COLUMN crm_contract.audit_status IS '审批状态';
COMMENT ON COLUMN crm_contract.order_date IS '下单日期';
COMMENT ON COLUMN crm_contract.start_time IS '开始时间';
COMMENT ON COLUMN crm_contract.end_time IS '结束时间';
COMMENT ON COLUMN crm_contract.total_product_price IS '产品总金额';
COMMENT ON COLUMN crm_contract.discount_percent IS '整单折扣';
COMMENT ON COLUMN crm_contract.total_price IS '合同总金额';
COMMENT ON COLUMN crm_contract.sign_contact_id IS '联系人编号';
COMMENT ON COLUMN crm_contract.sign_user_id IS '公司签约人';
COMMENT ON COLUMN crm_contract.remark IS '备注';
COMMENT ON COLUMN crm_contract.creator IS '创建者';
COMMENT ON COLUMN crm_contract.create_time IS '创建时间';
COMMENT ON COLUMN crm_contract.updater IS '更新者';
COMMENT ON COLUMN crm_contract.update_time IS '更新时间';
COMMENT ON COLUMN crm_contract.deleted IS '是否删除';
COMMENT ON COLUMN crm_contract.tenant_id IS '租户编号';

-- ----------------------------
-- Table structure for crm_contract_config
-- ----------------------------
DROP TABLE IF EXISTS crm_contract_config;
CREATE TABLE crm_contract_config (
  id BIGINT NOT NULL DEFAULT nextval('crm_contract_config_seq'),
  notify_enabled SMALLINT DEFAULT NULL,
  notify_days INTEGER DEFAULT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

COMMENT ON TABLE crm_contract_config IS 'CRM 合同配置表';
COMMENT ON COLUMN crm_contract_config.id IS '编号';
COMMENT ON COLUMN crm_contract_config.notify_enabled IS '是否开启提前提醒';
COMMENT ON COLUMN crm_contract_config.notify_days IS '提前提醒天数';
COMMENT ON COLUMN crm_contract_config.creator IS '创建者';
COMMENT ON COLUMN crm_contract_config.create_time IS '创建时间';
COMMENT ON COLUMN crm_contract_config.updater IS '更新者';
COMMENT ON COLUMN crm_contract_config.update_time IS '更新时间';
COMMENT ON COLUMN crm_contract_config.deleted IS '是否删除';
COMMENT ON COLUMN crm_contract_config.tenant_id IS '租户编号';

-- ----------------------------
-- Table structure for crm_contract_product
-- ----------------------------
DROP TABLE IF EXISTS crm_contract_product;
CREATE TABLE crm_contract_product (
  id BIGINT NOT NULL DEFAULT nextval('crm_contract_product_seq'),
  contract_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_price NUMERIC(24, 6) NOT NULL,
  contract_price NUMERIC(24, 6) NOT NULL,
  count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6) NOT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 1,
  PRIMARY KEY (id)
);

COMMENT ON TABLE crm_contract_product IS 'CRM 合同产品关联表';
COMMENT ON COLUMN crm_contract_product.id IS '主键';
COMMENT ON COLUMN crm_contract_product.contract_id IS '合同编号';
COMMENT ON COLUMN crm_contract_product.product_id IS '产品编号';
COMMENT ON COLUMN crm_contract_product.product_price IS '产品单价';
COMMENT ON COLUMN crm_contract_product.contract_price IS '合同价格';
COMMENT ON COLUMN crm_contract_product.count IS '数量';
COMMENT ON COLUMN crm_contract_product.total_price IS '总计价格';
COMMENT ON COLUMN crm_contract_product.creator IS '创建者';
COMMENT ON COLUMN crm_contract_product.create_time IS '创建时间';
COMMENT ON COLUMN crm_contract_product.updater IS '更新者';
COMMENT ON COLUMN crm_contract_product.update_time IS '更新时间';
COMMENT ON COLUMN crm_contract_product.deleted IS '是否删除';
COMMENT ON COLUMN crm_contract_product.tenant_id IS '租户编号';

-- ----------------------------
-- Table structure for crm_customer
-- ----------------------------
DROP TABLE IF EXISTS crm_customer;
CREATE TABLE crm_customer (
  id BIGINT NOT NULL DEFAULT nextval('crm_customer_seq'),
  name VARCHAR(255) DEFAULT NULL,
  follow_up_status SMALLINT NOT NULL DEFAULT 0,
  contact_last_time TIMESTAMP DEFAULT NULL,
  contact_last_content VARCHAR(255) DEFAULT NULL,
  contact_next_time TIMESTAMP DEFAULT NULL,
  owner_user_id BIGINT DEFAULT NULL,
  owner_time TIMESTAMP NOT NULL,
  lock_status BOOLEAN NOT NULL DEFAULT FALSE,
  deal_status BOOLEAN NOT NULL DEFAULT FALSE,
  mobile VARCHAR(20) DEFAULT NULL,
  telephone VARCHAR(20) DEFAULT NULL,
  qq VARCHAR(20) DEFAULT NULL,
  wechat VARCHAR(255) DEFAULT NULL,
  email VARCHAR(255) DEFAULT NULL,
  area_id BIGINT DEFAULT NULL,
  detail_address VARCHAR(255) DEFAULT NULL,
  industry_id INTEGER DEFAULT NULL,
  level INTEGER DEFAULT NULL,
  source INTEGER DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE INDEX idx_owner_user_id ON crm_customer(owner_user_id);

COMMENT ON TABLE crm_customer IS 'CRM 客户表';
COMMENT ON COLUMN crm_customer.id IS '编号，主键自增';
COMMENT ON COLUMN crm_customer.name IS '客户名称';
COMMENT ON COLUMN crm_customer.follow_up_status IS '跟进状态';
COMMENT ON COLUMN crm_customer.contact_last_time IS '最后跟进时间';
COMMENT ON COLUMN crm_customer.contact_last_content IS '最后跟进内容';
COMMENT ON COLUMN crm_customer.contact_next_time IS '下次联系时间';
COMMENT ON COLUMN crm_customer.owner_user_id IS '负责人的用户编号';
COMMENT ON COLUMN crm_customer.owner_time IS '成为负责人的时间';
COMMENT ON COLUMN crm_customer.lock_status IS '锁定状态';
COMMENT ON COLUMN crm_customer.deal_status IS '成交状态';
COMMENT ON COLUMN crm_customer.mobile IS '手机';
COMMENT ON COLUMN crm_customer.telephone IS '电话';
COMMENT ON COLUMN crm_customer.qq IS 'QQ';
COMMENT ON COLUMN crm_customer.wechat IS '微信';
COMMENT ON COLUMN crm_customer.email IS '邮箱';
COMMENT ON COLUMN crm_customer.area_id IS '地区编号';
COMMENT ON COLUMN crm_customer.detail_address IS '详细地址';
COMMENT ON COLUMN crm_customer.industry_id IS '所属行业';
COMMENT ON COLUMN crm_customer.level IS '客户等级';
COMMENT ON COLUMN crm_customer.source IS '客户来源';
COMMENT ON COLUMN crm_customer.remark IS '备注';
COMMENT ON COLUMN crm_customer.creator IS '创建者';
COMMENT ON COLUMN crm_customer.create_time IS '创建时间';
COMMENT ON COLUMN crm_customer.updater IS '更新者';
COMMENT ON COLUMN crm_customer.update_time IS '更新时间';
COMMENT ON COLUMN crm_customer.deleted IS '是否删除';
COMMENT ON COLUMN crm_customer.tenant_id IS '租户编号';

-- ----------------------------
-- Table structure for crm_customer_limit_config
-- ----------------------------
DROP TABLE IF EXISTS crm_customer_limit_config;
CREATE TABLE crm_customer_limit_config (
  id BIGINT NOT NULL DEFAULT nextval('crm_customer_limit_config_seq'),
  type INTEGER NOT NULL,
  user_ids VARCHAR(2048) DEFAULT '',
  dept_ids VARCHAR(2048) DEFAULT '',
  max_count INTEGER NOT NULL,
  deal_count_enabled SMALLINT DEFAULT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

COMMENT ON TABLE crm_customer_limit_config IS 'CRM 客户限制配置表';
COMMENT ON COLUMN crm_customer_limit_config.id IS '编号';
COMMENT ON COLUMN crm_customer_limit_config.type IS '规则类型 1: 拥有客户数限制，2:锁定客户数限制';
COMMENT ON COLUMN crm_customer_limit_config.user_ids IS '规则适用人群';
COMMENT ON COLUMN crm_customer_limit_config.dept_ids IS '规则适用部门';
COMMENT ON COLUMN crm_customer_limit_config.max_count IS '数量上限';
COMMENT ON COLUMN crm_customer_limit_config.deal_count_enabled IS '成交客户是否占有拥有客户数(当 type = 1 时)';
COMMENT ON COLUMN crm_customer_limit_config.creator IS '创建者';
COMMENT ON COLUMN crm_customer_limit_config.create_time IS '创建时间';
COMMENT ON COLUMN crm_customer_limit_config.updater IS '更新者';
COMMENT ON COLUMN crm_customer_limit_config.update_time IS '更新时间';
COMMENT ON COLUMN crm_customer_limit_config.deleted IS '是否删除';
COMMENT ON COLUMN crm_customer_limit_config.tenant_id IS '租户编号';

-- ----------------------------
-- Table structure for crm_customer_pool_config
-- ----------------------------
DROP TABLE IF EXISTS crm_customer_pool_config;
CREATE TABLE crm_customer_pool_config (
  id BIGINT NOT NULL DEFAULT nextval('crm_customer_pool_config_seq'),
  enabled SMALLINT NOT NULL,
  contact_expire_days INTEGER DEFAULT NULL,
  deal_expire_days INTEGER DEFAULT NULL,
  notify_enabled SMALLINT DEFAULT NULL,
  notify_days INTEGER DEFAULT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

COMMENT ON TABLE crm_customer_pool_config IS 'CRM 客户公海配置表';
COMMENT ON COLUMN crm_customer_pool_config.id IS '编号';
COMMENT ON COLUMN crm_customer_pool_config.enabled IS '是否启用客户公海';
COMMENT ON COLUMN crm_customer_pool_config.contact_expire_days IS '未跟进放入公海天数';
COMMENT ON COLUMN crm_customer_pool_config.deal_expire_days IS '未成交放入公海天数';
COMMENT ON COLUMN crm_customer_pool_config.notify_enabled IS '是否开启提前提醒';
COMMENT ON COLUMN crm_customer_pool_config.notify_days IS '提前提醒天数';
COMMENT ON COLUMN crm_customer_pool_config.creator IS '创建者';
COMMENT ON COLUMN crm_customer_pool_config.create_time IS '创建时间';
COMMENT ON COLUMN crm_customer_pool_config.updater IS '更新者';
COMMENT ON COLUMN crm_customer_pool_config.update_time IS '更新时间';
COMMENT ON COLUMN crm_customer_pool_config.deleted IS '是否删除';
COMMENT ON COLUMN crm_customer_pool_config.tenant_id IS '租户编号';

-- ----------------------------
-- Table structure for crm_follow_up_record
-- ----------------------------
DROP TABLE IF EXISTS crm_follow_up_record;
CREATE TABLE crm_follow_up_record (
  id BIGINT NOT NULL DEFAULT nextval('crm_follow_up_record_seq'),
  biz_type INTEGER DEFAULT NULL,
  biz_id BIGINT DEFAULT NULL,
  type INTEGER DEFAULT NULL,
  content VARCHAR(512) DEFAULT '',
  next_time TIMESTAMP DEFAULT NULL,
  pic_urls VARCHAR(1024) DEFAULT NULL,
  file_urls VARCHAR(1024) DEFAULT NULL,
  business_ids VARCHAR(255) DEFAULT '',
  contact_ids VARCHAR(255) DEFAULT '',
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

COMMENT ON TABLE crm_follow_up_record IS 'CRM 跟进记录';
COMMENT ON COLUMN crm_follow_up_record.id IS '编号';
COMMENT ON COLUMN crm_follow_up_record.biz_type IS '数据类型';
COMMENT ON COLUMN crm_follow_up_record.biz_id IS '数据编号';
COMMENT ON COLUMN crm_follow_up_record.type IS '跟进类型';
COMMENT ON COLUMN crm_follow_up_record.content IS '跟进内容';
COMMENT ON COLUMN crm_follow_up_record.next_time IS '下次联系时间';
COMMENT ON COLUMN crm_follow_up_record.pic_urls IS '图片';
COMMENT ON COLUMN crm_follow_up_record.file_urls IS '附件';
COMMENT ON COLUMN crm_follow_up_record.business_ids IS '关联的商机编号数组';
COMMENT ON COLUMN crm_follow_up_record.contact_ids IS '关联的联系人编号数组';
COMMENT ON COLUMN crm_follow_up_record.creator IS '创建者';
COMMENT ON COLUMN crm_follow_up_record.create_time IS '创建时间';
COMMENT ON COLUMN crm_follow_up_record.updater IS '更新者';
COMMENT ON COLUMN crm_follow_up_record.update_time IS '更新时间';
COMMENT ON COLUMN crm_follow_up_record.deleted IS '是否删除';
COMMENT ON COLUMN crm_follow_up_record.tenant_id IS '租户编号';

-- ----------------------------
-- Table structure for crm_permission
-- ----------------------------
DROP TABLE IF EXISTS crm_permission;
CREATE TABLE crm_permission (
  id BIGINT NOT NULL DEFAULT nextval('crm_permission_seq'),
  biz_type SMALLINT NOT NULL DEFAULT 100,
  biz_id BIGINT NOT NULL DEFAULT 0,
  user_id BIGINT NOT NULL DEFAULT 0,
  level INTEGER NOT NULL DEFAULT 0,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

COMMENT ON TABLE crm_permission IS 'CRM 数据权限表';
COMMENT ON COLUMN crm_permission.id IS '编号';
COMMENT ON COLUMN crm_permission.biz_type IS '数据类型';
COMMENT ON COLUMN crm_permission.biz_id IS '数据编号';
COMMENT ON COLUMN crm_permission.user_id IS '用户编号';
COMMENT ON COLUMN crm_permission.level IS '会员等级';
COMMENT ON COLUMN crm_permission.creator IS '创建者';
COMMENT ON COLUMN crm_permission.create_time IS '创建时间';
COMMENT ON COLUMN crm_permission.updater IS '更新者';
COMMENT ON COLUMN crm_permission.update_time IS '更新时间';
COMMENT ON COLUMN crm_permission.deleted IS '是否删除';
COMMENT ON COLUMN crm_permission.tenant_id IS '租户编号';

-- ----------------------------
-- Table structure for crm_product
-- ----------------------------
DROP TABLE IF EXISTS crm_product;
CREATE TABLE crm_product (
  id BIGINT NOT NULL DEFAULT nextval('crm_product_seq'),
  name VARCHAR(100) NOT NULL,
  no VARCHAR(20) NOT NULL,
  unit SMALLINT DEFAULT NULL,
  price NUMERIC(24, 6) DEFAULT 0.000000,
  status SMALLINT NOT NULL DEFAULT 1,
  category_id BIGINT NOT NULL,
  description VARCHAR(100) DEFAULT NULL,
  owner_user_id BIGINT NOT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id)
);

COMMENT ON TABLE crm_product IS 'CRM 产品表';
COMMENT ON COLUMN crm_product.id IS '产品编号';
COMMENT ON COLUMN crm_product.name IS '产品名称';
COMMENT ON COLUMN crm_product.no IS '产品编码';
COMMENT ON COLUMN crm_product.unit IS '单位';
COMMENT ON COLUMN crm_product.price IS '价格，单位：元';
COMMENT ON COLUMN crm_product.status IS '状态';
COMMENT ON COLUMN crm_product.category_id IS '产品分类编号';
COMMENT ON COLUMN crm_product.description IS '产品描述';
COMMENT ON COLUMN crm_product.owner_user_id IS '负责人的用户编号';
COMMENT ON COLUMN crm_product.creator IS '创建者';
COMMENT ON COLUMN crm_product.create_time IS '创建时间';
COMMENT ON COLUMN crm_product.updater IS '更新者';
COMMENT ON COLUMN crm_product.update_time IS '更新时间';
COMMENT ON COLUMN crm_product.tenant_id IS '租户编号';
COMMENT ON COLUMN crm_product.deleted IS '是否删除';

-- ----------------------------
-- Table structure for crm_product_category
-- ----------------------------
DROP TABLE IF EXISTS crm_product_category;
CREATE TABLE crm_product_category (
  id BIGINT NOT NULL DEFAULT nextval('crm_product_category_seq'),
  name VARCHAR(100) NOT NULL,
  parent_id BIGINT NOT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

COMMENT ON TABLE crm_product_category IS 'CRM 产品分类表';
COMMENT ON COLUMN crm_product_category.id IS '分类编号';
COMMENT ON COLUMN crm_product_category.name IS '分类名称';
COMMENT ON COLUMN crm_product_category.parent_id IS '父级编号';
COMMENT ON COLUMN crm_product_category.creator IS '创建者';
COMMENT ON COLUMN crm_product_category.create_time IS '创建时间';
COMMENT ON COLUMN crm_product_category.updater IS '更新者';
COMMENT ON COLUMN crm_product_category.update_time IS '更新时间';
COMMENT ON COLUMN crm_product_category.deleted IS '是否删除';
COMMENT ON COLUMN crm_product_category.tenant_id IS '租户编号';

-- ----------------------------
-- Table structure for crm_receivable
-- ----------------------------
DROP TABLE IF EXISTS crm_receivable;
CREATE TABLE crm_receivable (
  id BIGINT NOT NULL DEFAULT nextval('crm_receivable_seq'),
  no VARCHAR(100) NOT NULL,
  plan_id BIGINT DEFAULT NULL,
  customer_id BIGINT NOT NULL,
  contract_id BIGINT NOT NULL,
  owner_user_id BIGINT DEFAULT NULL,
  audit_status SMALLINT NOT NULL,
  process_instance_id VARCHAR(64) DEFAULT NULL,
  return_time TIMESTAMP DEFAULT NULL,
  return_type INTEGER DEFAULT NULL,
  price NUMERIC(24, 6) NOT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

COMMENT ON TABLE crm_receivable IS 'CRM 回款表';
COMMENT ON COLUMN crm_receivable.id IS 'ID';
COMMENT ON COLUMN crm_receivable.no IS '回款编号';
COMMENT ON COLUMN crm_receivable.plan_id IS '回款计划ID';
COMMENT ON COLUMN crm_receivable.customer_id IS '客户ID';
COMMENT ON COLUMN crm_receivable.contract_id IS '合同ID';
COMMENT ON COLUMN crm_receivable.owner_user_id IS '负责人的用户编号';
COMMENT ON COLUMN crm_receivable.audit_status IS '审批状态';
COMMENT ON COLUMN crm_receivable.process_instance_id IS '工作流编号';
COMMENT ON COLUMN crm_receivable.return_time IS '回款日期';
COMMENT ON COLUMN crm_receivable.return_type IS '回款方式';
COMMENT ON COLUMN crm_receivable.price IS '回款金额';
COMMENT ON COLUMN crm_receivable.remark IS '备注';
COMMENT ON COLUMN crm_receivable.creator IS '创建者';
COMMENT ON COLUMN crm_receivable.create_time IS '创建时间';
COMMENT ON COLUMN crm_receivable.updater IS '更新者';
COMMENT ON COLUMN crm_receivable.update_time IS '更新时间';
COMMENT ON COLUMN crm_receivable.deleted IS '是否删除';
COMMENT ON COLUMN crm_receivable.tenant_id IS '租户编号';

-- ----------------------------
-- Table structure for crm_receivable_plan
-- ----------------------------
DROP TABLE IF EXISTS crm_receivable_plan;
CREATE TABLE crm_receivable_plan (
  id BIGINT NOT NULL DEFAULT nextval('crm_receivable_plan_seq'),
  period BIGINT NOT NULL,
  customer_id BIGINT NOT NULL,
  contract_id BIGINT NOT NULL,
  owner_user_id BIGINT DEFAULT NULL,
  receivable_id BIGINT DEFAULT NULL,
  return_time TIMESTAMP DEFAULT NULL,
  return_type SMALLINT DEFAULT NULL,
  price NUMERIC(24, 6) NOT NULL,
  remind_days BIGINT DEFAULT NULL,
  remind_time TIMESTAMP DEFAULT NULL,
  remark VARCHAR(500) DEFAULT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

COMMENT ON TABLE crm_receivable_plan IS 'CRM 回款计划表';
COMMENT ON COLUMN crm_receivable_plan.id IS 'ID';
COMMENT ON COLUMN crm_receivable_plan.period IS '期数';
COMMENT ON COLUMN crm_receivable_plan.customer_id IS '客户编号';
COMMENT ON COLUMN crm_receivable_plan.contract_id IS '合同编号';
COMMENT ON COLUMN crm_receivable_plan.owner_user_id IS '负责人编号';
COMMENT ON COLUMN crm_receivable_plan.receivable_id IS '回款编号';
COMMENT ON COLUMN crm_receivable_plan.return_time IS '计划回款日期';
COMMENT ON COLUMN crm_receivable_plan.return_type IS '计划还款方式';
COMMENT ON COLUMN crm_receivable_plan.price IS '计划回款金额';
COMMENT ON COLUMN crm_receivable_plan.remind_days IS '提前几天提醒';
COMMENT ON COLUMN crm_receivable_plan.remind_time IS '提醒日期';
COMMENT ON COLUMN crm_receivable_plan.remark IS '备注';
COMMENT ON COLUMN crm_receivable_plan.creator IS '创建者';
COMMENT ON COLUMN crm_receivable_plan.create_time IS '创建时间';
COMMENT ON COLUMN crm_receivable_plan.updater IS '更新者';
COMMENT ON COLUMN crm_receivable_plan.update_time IS '更新时间';
COMMENT ON COLUMN crm_receivable_plan.deleted IS '是否删除';
COMMENT ON COLUMN crm_receivable_plan.tenant_id IS '租户编号';
