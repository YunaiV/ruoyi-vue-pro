/*
 ERP 模块数据库初始化脚本 (PostgreSQL)
 
 Source: erp-2024-05-03.sql (MySQL)
 Target: PostgreSQL 12+
 Generated: 2025-11-13
 
 Features:
 - 33 tables for ERP system (Product, Stock, Purchase, Sale, Finance)
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


-- ========== 产品管理 ==========

-- ----------------------------
-- Table: erp_product
-- ----------------------------
DROP TABLE IF EXISTS erp_product CASCADE;
CREATE TABLE erp_product (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  bar_code VARCHAR(50) NOT NULL,
  category_id BIGINT NOT NULL,
  unit_id BIGINT NOT NULL,
  status SMALLINT NOT NULL,
  standard VARCHAR(100),
  remark VARCHAR(500),
  expiry_day INTEGER,
  weight NUMERIC(24, 6),
  purchase_price NUMERIC(24, 6),
  sale_price NUMERIC(24, 6),
  min_price NUMERIC(24, 6),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_product IS 'ERP 产品表';
COMMENT ON COLUMN erp_product.id IS '产品编号';
COMMENT ON COLUMN erp_product.name IS '产品名称';
COMMENT ON COLUMN erp_product.bar_code IS '产品条码';
COMMENT ON COLUMN erp_product.category_id IS '产品分类编号';
COMMENT ON COLUMN erp_product.unit_id IS '单位编号';
COMMENT ON COLUMN erp_product.status IS '产品状态';
COMMENT ON COLUMN erp_product.standard IS '产品规格';
COMMENT ON COLUMN erp_product.remark IS '产品备注';
COMMENT ON COLUMN erp_product.expiry_day IS '保质期天数';
COMMENT ON COLUMN erp_product.weight IS '基础重量（kg）';
COMMENT ON COLUMN erp_product.purchase_price IS '采购价格，单位：元';
COMMENT ON COLUMN erp_product.sale_price IS '销售价格，单位：元';
COMMENT ON COLUMN erp_product.min_price IS '最低价格，单位：元';
COMMENT ON COLUMN erp_product.creator IS '创建者';
COMMENT ON COLUMN erp_product.create_time IS '创建时间';
COMMENT ON COLUMN erp_product.updater IS '更新者';
COMMENT ON COLUMN erp_product.update_time IS '更新时间';
COMMENT ON COLUMN erp_product.deleted IS '是否删除';
COMMENT ON COLUMN erp_product.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_product_update_time
BEFORE UPDATE ON erp_product
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_product_category
-- ----------------------------
DROP TABLE IF EXISTS erp_product_category CASCADE;
CREATE TABLE erp_product_category (
  id BIGSERIAL PRIMARY KEY,
  parent_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  code VARCHAR(100) NOT NULL,
  sort INTEGER DEFAULT 0,
  status SMALLINT NOT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_product_category IS 'ERP 产品分类';
COMMENT ON COLUMN erp_product_category.id IS '分类编号';
COMMENT ON COLUMN erp_product_category.parent_id IS '父分类编号';
COMMENT ON COLUMN erp_product_category.name IS '分类名称';
COMMENT ON COLUMN erp_product_category.code IS '分类编码';
COMMENT ON COLUMN erp_product_category.sort IS '分类排序';
COMMENT ON COLUMN erp_product_category.status IS '开启状态';
COMMENT ON COLUMN erp_product_category.creator IS '创建者';
COMMENT ON COLUMN erp_product_category.create_time IS '创建时间';
COMMENT ON COLUMN erp_product_category.updater IS '更新者';
COMMENT ON COLUMN erp_product_category.update_time IS '更新时间';
COMMENT ON COLUMN erp_product_category.deleted IS '是否删除';
COMMENT ON COLUMN erp_product_category.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_product_category_update_time
BEFORE UPDATE ON erp_product_category
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_product_unit
-- ----------------------------
DROP TABLE IF EXISTS erp_product_unit CASCADE;
CREATE TABLE erp_product_unit (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  status SMALLINT NOT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_product_unit IS 'ERP 产品单位表';
COMMENT ON COLUMN erp_product_unit.id IS '单位编号';
COMMENT ON COLUMN erp_product_unit.name IS '单位名字';
COMMENT ON COLUMN erp_product_unit.status IS '单位状态';
COMMENT ON COLUMN erp_product_unit.creator IS '创建者';
COMMENT ON COLUMN erp_product_unit.create_time IS '创建时间';
COMMENT ON COLUMN erp_product_unit.updater IS '更新者';
COMMENT ON COLUMN erp_product_unit.update_time IS '更新时间';
COMMENT ON COLUMN erp_product_unit.deleted IS '是否删除';
COMMENT ON COLUMN erp_product_unit.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_product_unit_update_time
BEFORE UPDATE ON erp_product_unit
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ========== 财务管理 ==========

-- ----------------------------
-- Table: erp_account
-- ----------------------------
DROP TABLE IF EXISTS erp_account CASCADE;
CREATE TABLE erp_account (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  no VARCHAR(50),
  remark VARCHAR(255),
  status SMALLINT NOT NULL,
  sort INTEGER NOT NULL,
  default_status BOOLEAN DEFAULT FALSE,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_account IS 'ERP 结算账户';
COMMENT ON COLUMN erp_account.id IS '结算账户编号';
COMMENT ON COLUMN erp_account.name IS '账户名称';
COMMENT ON COLUMN erp_account.no IS '账户编码';
COMMENT ON COLUMN erp_account.remark IS '备注';
COMMENT ON COLUMN erp_account.status IS '开启状态';
COMMENT ON COLUMN erp_account.sort IS '排序';
COMMENT ON COLUMN erp_account.default_status IS '是否默认';
COMMENT ON COLUMN erp_account.creator IS '创建者';
COMMENT ON COLUMN erp_account.create_time IS '创建时间';
COMMENT ON COLUMN erp_account.updater IS '更新者';
COMMENT ON COLUMN erp_account.update_time IS '更新时间';
COMMENT ON COLUMN erp_account.deleted IS '是否删除';
COMMENT ON COLUMN erp_account.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_account_update_time
BEFORE UPDATE ON erp_account
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_finance_payment
-- ----------------------------
DROP TABLE IF EXISTS erp_finance_payment CASCADE;
CREATE TABLE erp_finance_payment (
  id BIGSERIAL PRIMARY KEY,
  no VARCHAR(255) NOT NULL,
  status SMALLINT NOT NULL,
  payment_time TIMESTAMP NOT NULL,
  finance_user_id BIGINT,
  supplier_id BIGINT NOT NULL,
  account_id BIGINT NOT NULL,
  total_price NUMERIC(24, 6) NOT NULL,
  discount_price NUMERIC(24, 6) NOT NULL,
  payment_price NUMERIC(24, 6) NOT NULL,
  remark VARCHAR(1024),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_finance_payment IS 'ERP 付款单表';
COMMENT ON COLUMN erp_finance_payment.id IS '编号';
COMMENT ON COLUMN erp_finance_payment.no IS '付款单号';
COMMENT ON COLUMN erp_finance_payment.status IS '状态';
COMMENT ON COLUMN erp_finance_payment.payment_time IS '付款时间';
COMMENT ON COLUMN erp_finance_payment.finance_user_id IS '财务人员编号';
COMMENT ON COLUMN erp_finance_payment.supplier_id IS '供应商编号';
COMMENT ON COLUMN erp_finance_payment.account_id IS '付款账户编号';
COMMENT ON COLUMN erp_finance_payment.total_price IS '合计价格，单位：元';
COMMENT ON COLUMN erp_finance_payment.discount_price IS '优惠金额，单位：元';
COMMENT ON COLUMN erp_finance_payment.payment_price IS '实付金额，单位：分';
COMMENT ON COLUMN erp_finance_payment.remark IS '备注';
COMMENT ON COLUMN erp_finance_payment.creator IS '创建者';
COMMENT ON COLUMN erp_finance_payment.create_time IS '创建时间';
COMMENT ON COLUMN erp_finance_payment.updater IS '更新者';
COMMENT ON COLUMN erp_finance_payment.update_time IS '更新时间';
COMMENT ON COLUMN erp_finance_payment.deleted IS '是否删除';
COMMENT ON COLUMN erp_finance_payment.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_finance_payment_update_time
BEFORE UPDATE ON erp_finance_payment
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_finance_payment_item
-- ----------------------------
DROP TABLE IF EXISTS erp_finance_payment_item CASCADE;
CREATE TABLE erp_finance_payment_item (
  id BIGSERIAL PRIMARY KEY,
  payment_id BIGINT NOT NULL,
  biz_type SMALLINT NOT NULL,
  biz_id BIGINT NOT NULL,
  biz_no VARCHAR(255) NOT NULL,
  total_price NUMERIC(24, 6) NOT NULL,
  paid_price NUMERIC(24, 6) NOT NULL,
  payment_price NUMERIC(24, 6) NOT NULL,
  remark VARCHAR(1024),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_finance_payment_item IS 'ERP 付款项表';
COMMENT ON COLUMN erp_finance_payment_item.id IS '编号';
COMMENT ON COLUMN erp_finance_payment_item.payment_id IS '付款单编号';
COMMENT ON COLUMN erp_finance_payment_item.biz_type IS '业务类型';
COMMENT ON COLUMN erp_finance_payment_item.biz_id IS '业务编号';
COMMENT ON COLUMN erp_finance_payment_item.biz_no IS '业务单号';
COMMENT ON COLUMN erp_finance_payment_item.total_price IS '应付欠款，单位：分';
COMMENT ON COLUMN erp_finance_payment_item.paid_price IS '已付欠款，单位：分';
COMMENT ON COLUMN erp_finance_payment_item.payment_price IS '本次付款，单位：分';
COMMENT ON COLUMN erp_finance_payment_item.remark IS '备注';
COMMENT ON COLUMN erp_finance_payment_item.creator IS '创建者';
COMMENT ON COLUMN erp_finance_payment_item.create_time IS '创建时间';
COMMENT ON COLUMN erp_finance_payment_item.updater IS '更新者';
COMMENT ON COLUMN erp_finance_payment_item.update_time IS '更新时间';
COMMENT ON COLUMN erp_finance_payment_item.deleted IS '是否删除';
COMMENT ON COLUMN erp_finance_payment_item.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_finance_payment_item_update_time
BEFORE UPDATE ON erp_finance_payment_item
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_finance_receipt
-- ----------------------------
DROP TABLE IF EXISTS erp_finance_receipt CASCADE;
CREATE TABLE erp_finance_receipt (
  id BIGSERIAL PRIMARY KEY,
  no VARCHAR(255) NOT NULL,
  status SMALLINT NOT NULL,
  receipt_time TIMESTAMP NOT NULL,
  finance_user_id BIGINT,
  customer_id BIGINT NOT NULL,
  account_id BIGINT NOT NULL,
  total_price NUMERIC(24, 6) NOT NULL,
  discount_price NUMERIC(24, 6) NOT NULL,
  receipt_price NUMERIC(24, 6) NOT NULL,
  remark VARCHAR(1024),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_finance_receipt IS 'ERP 收款单表';
COMMENT ON COLUMN erp_finance_receipt.id IS '编号';
COMMENT ON COLUMN erp_finance_receipt.no IS '收款单号';
COMMENT ON COLUMN erp_finance_receipt.status IS '状态';
COMMENT ON COLUMN erp_finance_receipt.receipt_time IS '收款时间';
COMMENT ON COLUMN erp_finance_receipt.finance_user_id IS '财务人员编号';
COMMENT ON COLUMN erp_finance_receipt.customer_id IS '客户编号';
COMMENT ON COLUMN erp_finance_receipt.account_id IS '收款账户编号';
COMMENT ON COLUMN erp_finance_receipt.total_price IS '合计价格，单位：元';
COMMENT ON COLUMN erp_finance_receipt.discount_price IS '优惠金额，单位：元';
COMMENT ON COLUMN erp_finance_receipt.receipt_price IS '实收金额，单位：分';
COMMENT ON COLUMN erp_finance_receipt.remark IS '备注';
COMMENT ON COLUMN erp_finance_receipt.creator IS '创建者';
COMMENT ON COLUMN erp_finance_receipt.create_time IS '创建时间';
COMMENT ON COLUMN erp_finance_receipt.updater IS '更新者';
COMMENT ON COLUMN erp_finance_receipt.update_time IS '更新时间';
COMMENT ON COLUMN erp_finance_receipt.deleted IS '是否删除';
COMMENT ON COLUMN erp_finance_receipt.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_finance_receipt_update_time
BEFORE UPDATE ON erp_finance_receipt
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_finance_receipt_item
-- ----------------------------
DROP TABLE IF EXISTS erp_finance_receipt_item CASCADE;
CREATE TABLE erp_finance_receipt_item (
  id BIGSERIAL PRIMARY KEY,
  receipt_id BIGINT NOT NULL,
  biz_type SMALLINT NOT NULL,
  biz_id BIGINT NOT NULL,
  biz_no VARCHAR(255) NOT NULL,
  total_price NUMERIC(24, 6) NOT NULL,
  receipted_price NUMERIC(24, 6) NOT NULL,
  receipt_price NUMERIC(24, 6) NOT NULL,
  remark VARCHAR(1024),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_finance_receipt_item IS 'ERP 收款项表';
COMMENT ON COLUMN erp_finance_receipt_item.id IS '编号';
COMMENT ON COLUMN erp_finance_receipt_item.receipt_id IS '收款单编号';
COMMENT ON COLUMN erp_finance_receipt_item.biz_type IS '业务类型';
COMMENT ON COLUMN erp_finance_receipt_item.biz_id IS '业务编号';
COMMENT ON COLUMN erp_finance_receipt_item.biz_no IS '业务单号';
COMMENT ON COLUMN erp_finance_receipt_item.total_price IS '应收金额，单位：分';
COMMENT ON COLUMN erp_finance_receipt_item.receipted_price IS '已收金额，单位：分';
COMMENT ON COLUMN erp_finance_receipt_item.receipt_price IS '本次收款，单位：分';
COMMENT ON COLUMN erp_finance_receipt_item.remark IS '备注';
COMMENT ON COLUMN erp_finance_receipt_item.creator IS '创建者';
COMMENT ON COLUMN erp_finance_receipt_item.create_time IS '创建时间';
COMMENT ON COLUMN erp_finance_receipt_item.updater IS '更新者';
COMMENT ON COLUMN erp_finance_receipt_item.update_time IS '更新时间';
COMMENT ON COLUMN erp_finance_receipt_item.deleted IS '是否删除';
COMMENT ON COLUMN erp_finance_receipt_item.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_finance_receipt_item_update_time
BEFORE UPDATE ON erp_finance_receipt_item
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ========== 库存管理 ==========

-- ----------------------------
-- Table: erp_stock
-- ----------------------------
DROP TABLE IF EXISTS erp_stock CASCADE;
CREATE TABLE erp_stock (
  id BIGSERIAL PRIMARY KEY,
  product_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  count NUMERIC(24, 6) NOT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_stock IS 'ERP 产品库存表';
COMMENT ON COLUMN erp_stock.id IS '编号';
COMMENT ON COLUMN erp_stock.product_id IS '产品编号';
COMMENT ON COLUMN erp_stock.warehouse_id IS '仓库编号';
COMMENT ON COLUMN erp_stock.count IS '库存数量';
COMMENT ON COLUMN erp_stock.creator IS '创建者';
COMMENT ON COLUMN erp_stock.create_time IS '创建时间';
COMMENT ON COLUMN erp_stock.updater IS '更新者';
COMMENT ON COLUMN erp_stock.update_time IS '更新时间';
COMMENT ON COLUMN erp_stock.deleted IS '是否删除';
COMMENT ON COLUMN erp_stock.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_stock_update_time
BEFORE UPDATE ON erp_stock
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_stock_check
-- ----------------------------
DROP TABLE IF EXISTS erp_stock_check CASCADE;
CREATE TABLE erp_stock_check (
  id BIGSERIAL PRIMARY KEY,
  no VARCHAR(255) NOT NULL,
  check_time TIMESTAMP NOT NULL,
  total_count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6) NOT NULL,
  status SMALLINT NOT NULL,
  remark VARCHAR(255),
  file_url VARCHAR(255),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_stock_check IS 'ERP 库存盘点单表';
COMMENT ON COLUMN erp_stock_check.id IS '盘点编号';
COMMENT ON COLUMN erp_stock_check.no IS '盘点单号';
COMMENT ON COLUMN erp_stock_check.check_time IS '盘点时间';
COMMENT ON COLUMN erp_stock_check.total_count IS '合计数量';
COMMENT ON COLUMN erp_stock_check.total_price IS '合计金额，单位：元';
COMMENT ON COLUMN erp_stock_check.status IS '状态';
COMMENT ON COLUMN erp_stock_check.remark IS '备注';
COMMENT ON COLUMN erp_stock_check.file_url IS '附件 URL';
COMMENT ON COLUMN erp_stock_check.creator IS '创建者';
COMMENT ON COLUMN erp_stock_check.create_time IS '创建时间';
COMMENT ON COLUMN erp_stock_check.updater IS '更新者';
COMMENT ON COLUMN erp_stock_check.update_time IS '更新时间';
COMMENT ON COLUMN erp_stock_check.deleted IS '是否删除';
COMMENT ON COLUMN erp_stock_check.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_stock_check_update_time
BEFORE UPDATE ON erp_stock_check
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_stock_check_item
-- ----------------------------
DROP TABLE IF EXISTS erp_stock_check_item CASCADE;
CREATE TABLE erp_stock_check_item (
  id BIGSERIAL PRIMARY KEY,
  check_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_unit_id BIGINT NOT NULL,
  product_price NUMERIC(24, 6),
  stock_count NUMERIC(24, 6) NOT NULL,
  actual_count NUMERIC(24, 6) NOT NULL,
  count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6),
  remark VARCHAR(255),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_stock_check_item IS 'ERP 库存盘点项表';
COMMENT ON COLUMN erp_stock_check_item.id IS '盘点项编号';
COMMENT ON COLUMN erp_stock_check_item.check_id IS '盘点编号';
COMMENT ON COLUMN erp_stock_check_item.warehouse_id IS '仓库编号';
COMMENT ON COLUMN erp_stock_check_item.product_id IS '产品编号';
COMMENT ON COLUMN erp_stock_check_item.product_unit_id IS '产品单位编号';
COMMENT ON COLUMN erp_stock_check_item.product_price IS '产品单价';
COMMENT ON COLUMN erp_stock_check_item.stock_count IS '账面数量（当前库存）';
COMMENT ON COLUMN erp_stock_check_item.actual_count IS '实际数量（实际库存）';
COMMENT ON COLUMN erp_stock_check_item.count IS '盈亏数量';
COMMENT ON COLUMN erp_stock_check_item.total_price IS '合计金额，单位：元';
COMMENT ON COLUMN erp_stock_check_item.remark IS '备注';
COMMENT ON COLUMN erp_stock_check_item.creator IS '创建者';
COMMENT ON COLUMN erp_stock_check_item.create_time IS '创建时间';
COMMENT ON COLUMN erp_stock_check_item.updater IS '更新者';
COMMENT ON COLUMN erp_stock_check_item.update_time IS '更新时间';
COMMENT ON COLUMN erp_stock_check_item.deleted IS '是否删除';
COMMENT ON COLUMN erp_stock_check_item.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_stock_check_item_update_time
BEFORE UPDATE ON erp_stock_check_item
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_stock_in
-- ----------------------------
DROP TABLE IF EXISTS erp_stock_in CASCADE;
CREATE TABLE erp_stock_in (
  id BIGSERIAL PRIMARY KEY,
  no VARCHAR(255) NOT NULL,
  supplier_id BIGINT,
  in_time TIMESTAMP NOT NULL,
  total_count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6) NOT NULL,
  status SMALLINT NOT NULL,
  remark VARCHAR(255),
  file_url VARCHAR(255),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_stock_in IS 'ERP 其它入库单表';
COMMENT ON COLUMN erp_stock_in.id IS '入库编号';
COMMENT ON COLUMN erp_stock_in.no IS '入库单号';
COMMENT ON COLUMN erp_stock_in.supplier_id IS '供应商编号';
COMMENT ON COLUMN erp_stock_in.in_time IS '入库时间';
COMMENT ON COLUMN erp_stock_in.total_count IS '合计数量';
COMMENT ON COLUMN erp_stock_in.total_price IS '合计金额，单位：元';
COMMENT ON COLUMN erp_stock_in.status IS '状态';
COMMENT ON COLUMN erp_stock_in.remark IS '备注';
COMMENT ON COLUMN erp_stock_in.file_url IS '附件 URL';
COMMENT ON COLUMN erp_stock_in.creator IS '创建者';
COMMENT ON COLUMN erp_stock_in.create_time IS '创建时间';
COMMENT ON COLUMN erp_stock_in.updater IS '更新者';
COMMENT ON COLUMN erp_stock_in.update_time IS '更新时间';
COMMENT ON COLUMN erp_stock_in.deleted IS '是否删除';
COMMENT ON COLUMN erp_stock_in.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_stock_in_update_time
BEFORE UPDATE ON erp_stock_in
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_stock_in_item
-- ----------------------------
DROP TABLE IF EXISTS erp_stock_in_item CASCADE;
CREATE TABLE erp_stock_in_item (
  id BIGSERIAL PRIMARY KEY,
  in_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_unit_id BIGINT NOT NULL,
  product_price NUMERIC(24, 6),
  count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6),
  remark VARCHAR(255),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_stock_in_item IS 'ERP 其它入库单项表';
COMMENT ON COLUMN erp_stock_in_item.id IS '入库项编号';
COMMENT ON COLUMN erp_stock_in_item.in_id IS '入库编号';
COMMENT ON COLUMN erp_stock_in_item.warehouse_id IS '仓库编号';
COMMENT ON COLUMN erp_stock_in_item.product_id IS '产品编号';
COMMENT ON COLUMN erp_stock_in_item.product_unit_id IS '产品单位编号';
COMMENT ON COLUMN erp_stock_in_item.product_price IS '产品单价';
COMMENT ON COLUMN erp_stock_in_item.count IS '产品数量';
COMMENT ON COLUMN erp_stock_in_item.total_price IS '合计金额，单位：元';
COMMENT ON COLUMN erp_stock_in_item.remark IS '备注';
COMMENT ON COLUMN erp_stock_in_item.creator IS '创建者';
COMMENT ON COLUMN erp_stock_in_item.create_time IS '创建时间';
COMMENT ON COLUMN erp_stock_in_item.updater IS '更新者';
COMMENT ON COLUMN erp_stock_in_item.update_time IS '更新时间';
COMMENT ON COLUMN erp_stock_in_item.deleted IS '是否删除';
COMMENT ON COLUMN erp_stock_in_item.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_stock_in_item_update_time
BEFORE UPDATE ON erp_stock_in_item
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_stock_move
-- ----------------------------
DROP TABLE IF EXISTS erp_stock_move CASCADE;
CREATE TABLE erp_stock_move (
  id BIGSERIAL PRIMARY KEY,
  no VARCHAR(255) NOT NULL,
  move_time TIMESTAMP NOT NULL,
  total_count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6) NOT NULL,
  status SMALLINT NOT NULL,
  remark VARCHAR(255),
  file_url VARCHAR(255),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_stock_move IS 'ERP 库存调拨单表';
COMMENT ON COLUMN erp_stock_move.id IS '调拨编号';
COMMENT ON COLUMN erp_stock_move.no IS '调拨单号';
COMMENT ON COLUMN erp_stock_move.move_time IS '调拨时间';
COMMENT ON COLUMN erp_stock_move.total_count IS '合计数量';
COMMENT ON COLUMN erp_stock_move.total_price IS '合计金额，单位：元';
COMMENT ON COLUMN erp_stock_move.status IS '状态';
COMMENT ON COLUMN erp_stock_move.remark IS '备注';
COMMENT ON COLUMN erp_stock_move.file_url IS '附件 URL';
COMMENT ON COLUMN erp_stock_move.creator IS '创建者';
COMMENT ON COLUMN erp_stock_move.create_time IS '创建时间';
COMMENT ON COLUMN erp_stock_move.updater IS '更新者';
COMMENT ON COLUMN erp_stock_move.update_time IS '更新时间';
COMMENT ON COLUMN erp_stock_move.deleted IS '是否删除';
COMMENT ON COLUMN erp_stock_move.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_stock_move_update_time
BEFORE UPDATE ON erp_stock_move
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_stock_move_item
-- ----------------------------
DROP TABLE IF EXISTS erp_stock_move_item CASCADE;
CREATE TABLE erp_stock_move_item (
  id BIGSERIAL PRIMARY KEY,
  move_id BIGINT NOT NULL,
  from_warehouse_id BIGINT NOT NULL,
  to_warehouse_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_unit_id BIGINT NOT NULL,
  product_price NUMERIC(24, 6),
  count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6),
  remark VARCHAR(255),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_stock_move_item IS 'ERP 库存调拨项表';
COMMENT ON COLUMN erp_stock_move_item.id IS '调拨项编号';
COMMENT ON COLUMN erp_stock_move_item.move_id IS '调拨编号';
COMMENT ON COLUMN erp_stock_move_item.from_warehouse_id IS '调出仓库编号';
COMMENT ON COLUMN erp_stock_move_item.to_warehouse_id IS '调入仓库编号';
COMMENT ON COLUMN erp_stock_move_item.product_id IS '产品编号';
COMMENT ON COLUMN erp_stock_move_item.product_unit_id IS '产品单位编号';
COMMENT ON COLUMN erp_stock_move_item.product_price IS '产品单价';
COMMENT ON COLUMN erp_stock_move_item.count IS '产品数量';
COMMENT ON COLUMN erp_stock_move_item.total_price IS '合计金额，单位：元';
COMMENT ON COLUMN erp_stock_move_item.remark IS '备注';
COMMENT ON COLUMN erp_stock_move_item.creator IS '创建者';
COMMENT ON COLUMN erp_stock_move_item.create_time IS '创建时间';
COMMENT ON COLUMN erp_stock_move_item.updater IS '更新者';
COMMENT ON COLUMN erp_stock_move_item.update_time IS '更新时间';
COMMENT ON COLUMN erp_stock_move_item.deleted IS '是否删除';
COMMENT ON COLUMN erp_stock_move_item.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_stock_move_item_update_time
BEFORE UPDATE ON erp_stock_move_item
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_stock_out
-- ----------------------------
DROP TABLE IF EXISTS erp_stock_out CASCADE;
CREATE TABLE erp_stock_out (
  id BIGSERIAL PRIMARY KEY,
  no VARCHAR(255) NOT NULL,
  customer_id BIGINT,
  out_time TIMESTAMP NOT NULL,
  total_count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6) NOT NULL,
  status SMALLINT NOT NULL,
  remark VARCHAR(255),
  file_url VARCHAR(255),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_stock_out IS 'ERP 其它入库单表';
COMMENT ON COLUMN erp_stock_out.id IS '出库编号';
COMMENT ON COLUMN erp_stock_out.no IS '出库单号';
COMMENT ON COLUMN erp_stock_out.customer_id IS '客户编号';
COMMENT ON COLUMN erp_stock_out.out_time IS '出库时间';
COMMENT ON COLUMN erp_stock_out.total_count IS '合计数量';
COMMENT ON COLUMN erp_stock_out.total_price IS '合计金额，单位：元';
COMMENT ON COLUMN erp_stock_out.status IS '状态';
COMMENT ON COLUMN erp_stock_out.remark IS '备注';
COMMENT ON COLUMN erp_stock_out.file_url IS '附件 URL';
COMMENT ON COLUMN erp_stock_out.creator IS '创建者';
COMMENT ON COLUMN erp_stock_out.create_time IS '创建时间';
COMMENT ON COLUMN erp_stock_out.updater IS '更新者';
COMMENT ON COLUMN erp_stock_out.update_time IS '更新时间';
COMMENT ON COLUMN erp_stock_out.deleted IS '是否删除';
COMMENT ON COLUMN erp_stock_out.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_stock_out_update_time
BEFORE UPDATE ON erp_stock_out
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_stock_out_item
-- ----------------------------
DROP TABLE IF EXISTS erp_stock_out_item CASCADE;
CREATE TABLE erp_stock_out_item (
  id BIGSERIAL PRIMARY KEY,
  out_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_unit_id BIGINT NOT NULL,
  product_price NUMERIC(24, 6),
  count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6),
  remark VARCHAR(255),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_stock_out_item IS 'ERP 其它出库单项表';
COMMENT ON COLUMN erp_stock_out_item.id IS '出库项编号';
COMMENT ON COLUMN erp_stock_out_item.out_id IS '出库编号';
COMMENT ON COLUMN erp_stock_out_item.warehouse_id IS '仓库编号';
COMMENT ON COLUMN erp_stock_out_item.product_id IS '产品编号';
COMMENT ON COLUMN erp_stock_out_item.product_unit_id IS '产品单位编号';
COMMENT ON COLUMN erp_stock_out_item.product_price IS '产品单价';
COMMENT ON COLUMN erp_stock_out_item.count IS '产品数量';
COMMENT ON COLUMN erp_stock_out_item.total_price IS '合计金额，单位：元';
COMMENT ON COLUMN erp_stock_out_item.remark IS '备注';
COMMENT ON COLUMN erp_stock_out_item.creator IS '创建者';
COMMENT ON COLUMN erp_stock_out_item.create_time IS '创建时间';
COMMENT ON COLUMN erp_stock_out_item.updater IS '更新者';
COMMENT ON COLUMN erp_stock_out_item.update_time IS '更新时间';
COMMENT ON COLUMN erp_stock_out_item.deleted IS '是否删除';
COMMENT ON COLUMN erp_stock_out_item.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_stock_out_item_update_time
BEFORE UPDATE ON erp_stock_out_item
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_stock_record
-- ----------------------------
DROP TABLE IF EXISTS erp_stock_record CASCADE;
CREATE TABLE erp_stock_record (
  id BIGSERIAL PRIMARY KEY,
  product_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  count NUMERIC(24, 6) NOT NULL,
  total_count NUMERIC(24, 6) NOT NULL,
  biz_type SMALLINT NOT NULL,
  biz_id BIGINT NOT NULL,
  biz_item_id BIGINT NOT NULL,
  biz_no VARCHAR(255) NOT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_stock_record IS 'ERP 产品库存明细表';
COMMENT ON COLUMN erp_stock_record.id IS '编号';
COMMENT ON COLUMN erp_stock_record.product_id IS '产品编号';
COMMENT ON COLUMN erp_stock_record.warehouse_id IS '仓库编号';
COMMENT ON COLUMN erp_stock_record.count IS '出入库数量';
COMMENT ON COLUMN erp_stock_record.total_count IS '总库存量';
COMMENT ON COLUMN erp_stock_record.biz_type IS '业务类型';
COMMENT ON COLUMN erp_stock_record.biz_id IS '业务编号';
COMMENT ON COLUMN erp_stock_record.biz_item_id IS '业务项编号';
COMMENT ON COLUMN erp_stock_record.biz_no IS '业务单号';
COMMENT ON COLUMN erp_stock_record.creator IS '创建者';
COMMENT ON COLUMN erp_stock_record.create_time IS '创建时间';
COMMENT ON COLUMN erp_stock_record.updater IS '更新者';
COMMENT ON COLUMN erp_stock_record.update_time IS '更新时间';
COMMENT ON COLUMN erp_stock_record.deleted IS '是否删除';
COMMENT ON COLUMN erp_stock_record.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_stock_record_update_time
BEFORE UPDATE ON erp_stock_record
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_warehouse
-- ----------------------------
DROP TABLE IF EXISTS erp_warehouse CASCADE;
CREATE TABLE erp_warehouse (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(20) NOT NULL,
  address VARCHAR(50),
  sort BIGINT NOT NULL,
  remark VARCHAR(100),
  principal VARCHAR(20),
  warehouse_price NUMERIC(24, 6),
  truckage_price NUMERIC(24, 6),
  status SMALLINT NOT NULL,
  default_status BOOLEAN DEFAULT FALSE,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_warehouse IS 'ERP 仓库表';
COMMENT ON COLUMN erp_warehouse.id IS '仓库编号';
COMMENT ON COLUMN erp_warehouse.name IS '仓库名称';
COMMENT ON COLUMN erp_warehouse.address IS '仓库地址';
COMMENT ON COLUMN erp_warehouse.sort IS '排序';
COMMENT ON COLUMN erp_warehouse.remark IS '备注';
COMMENT ON COLUMN erp_warehouse.principal IS '负责人';
COMMENT ON COLUMN erp_warehouse.warehouse_price IS '仓储费，单位：元';
COMMENT ON COLUMN erp_warehouse.truckage_price IS '搬运费，单位：元';
COMMENT ON COLUMN erp_warehouse.status IS '开启状态';
COMMENT ON COLUMN erp_warehouse.default_status IS '是否默认';
COMMENT ON COLUMN erp_warehouse.creator IS '创建者';
COMMENT ON COLUMN erp_warehouse.create_time IS '创建时间';
COMMENT ON COLUMN erp_warehouse.updater IS '更新者';
COMMENT ON COLUMN erp_warehouse.update_time IS '更新时间';
COMMENT ON COLUMN erp_warehouse.deleted IS '是否删除';
COMMENT ON COLUMN erp_warehouse.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_warehouse_update_time
BEFORE UPDATE ON erp_warehouse
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ========== 采购管理 ==========

-- ----------------------------
-- Table: erp_purchase_in
-- ----------------------------
DROP TABLE IF EXISTS erp_purchase_in CASCADE;
CREATE TABLE erp_purchase_in (
  id BIGSERIAL PRIMARY KEY,
  no VARCHAR(255) NOT NULL,
  status SMALLINT NOT NULL,
  supplier_id BIGINT NOT NULL,
  account_id BIGINT NOT NULL,
  in_time TIMESTAMP NOT NULL,
  order_id BIGINT NOT NULL,
  order_no VARCHAR(255) NOT NULL,
  total_count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6) NOT NULL,
  payment_price NUMERIC(24, 6) NOT NULL DEFAULT 0.000000,
  total_product_price NUMERIC(24, 6) NOT NULL,
  total_tax_price NUMERIC(24, 6) NOT NULL,
  discount_percent NUMERIC(24, 6) NOT NULL,
  discount_price NUMERIC(24, 6) NOT NULL,
  other_price NUMERIC(24, 6) NOT NULL DEFAULT 0.000000,
  file_url VARCHAR(512),
  remark VARCHAR(1024),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_purchase_in IS 'ERP 采购入库表';
COMMENT ON COLUMN erp_purchase_in.id IS '编号';
COMMENT ON COLUMN erp_purchase_in.no IS '采购入库编号';
COMMENT ON COLUMN erp_purchase_in.status IS '采购状态';
COMMENT ON COLUMN erp_purchase_in.supplier_id IS '供应商编号';
COMMENT ON COLUMN erp_purchase_in.account_id IS '结算账户编号';
COMMENT ON COLUMN erp_purchase_in.in_time IS '入库时间';
COMMENT ON COLUMN erp_purchase_in.order_id IS '采购订单编号';
COMMENT ON COLUMN erp_purchase_in.order_no IS '采购订单号';
COMMENT ON COLUMN erp_purchase_in.total_count IS '合计数量';
COMMENT ON COLUMN erp_purchase_in.total_price IS '合计价格，单位：元';
COMMENT ON COLUMN erp_purchase_in.payment_price IS '已付款金额，单位：元';
COMMENT ON COLUMN erp_purchase_in.total_product_price IS '合计产品价格，单位：元';
COMMENT ON COLUMN erp_purchase_in.total_tax_price IS '合计税额，单位：元';
COMMENT ON COLUMN erp_purchase_in.discount_percent IS '优惠率，百分比';
COMMENT ON COLUMN erp_purchase_in.discount_price IS '优惠金额，单位：元';
COMMENT ON COLUMN erp_purchase_in.other_price IS '其它金额，单位：元';
COMMENT ON COLUMN erp_purchase_in.file_url IS '附件地址';
COMMENT ON COLUMN erp_purchase_in.remark IS '备注';
COMMENT ON COLUMN erp_purchase_in.creator IS '创建者';
COMMENT ON COLUMN erp_purchase_in.create_time IS '创建时间';
COMMENT ON COLUMN erp_purchase_in.updater IS '更新者';
COMMENT ON COLUMN erp_purchase_in.update_time IS '更新时间';
COMMENT ON COLUMN erp_purchase_in.deleted IS '是否删除';
COMMENT ON COLUMN erp_purchase_in.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_purchase_in_update_time
BEFORE UPDATE ON erp_purchase_in
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_purchase_in_items
-- ----------------------------
DROP TABLE IF EXISTS erp_purchase_in_items CASCADE;
CREATE TABLE erp_purchase_in_items (
  id BIGSERIAL PRIMARY KEY,
  in_id BIGINT NOT NULL,
  order_item_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_unit_id BIGINT NOT NULL,
  product_price NUMERIC(24, 6) NOT NULL,
  count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6) NOT NULL,
  tax_percent NUMERIC(24, 6),
  tax_price NUMERIC(24, 6),
  remark VARCHAR(1024),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_purchase_in_items IS 'ERP 销售入库项表';
COMMENT ON COLUMN erp_purchase_in_items.id IS '编号';
COMMENT ON COLUMN erp_purchase_in_items.in_id IS '采购入库编号';
COMMENT ON COLUMN erp_purchase_in_items.order_item_id IS '采购订单项编号';
COMMENT ON COLUMN erp_purchase_in_items.warehouse_id IS '仓库编号';
COMMENT ON COLUMN erp_purchase_in_items.product_id IS '产品编号';
COMMENT ON COLUMN erp_purchase_in_items.product_unit_id IS '产品单位单位';
COMMENT ON COLUMN erp_purchase_in_items.product_price IS '产品单价';
COMMENT ON COLUMN erp_purchase_in_items.count IS '数量';
COMMENT ON COLUMN erp_purchase_in_items.total_price IS '总价';
COMMENT ON COLUMN erp_purchase_in_items.tax_percent IS '税率，百分比';
COMMENT ON COLUMN erp_purchase_in_items.tax_price IS '税额，单位：元';
COMMENT ON COLUMN erp_purchase_in_items.remark IS '备注';
COMMENT ON COLUMN erp_purchase_in_items.creator IS '创建者';
COMMENT ON COLUMN erp_purchase_in_items.create_time IS '创建时间';
COMMENT ON COLUMN erp_purchase_in_items.updater IS '更新者';
COMMENT ON COLUMN erp_purchase_in_items.update_time IS '更新时间';
COMMENT ON COLUMN erp_purchase_in_items.deleted IS '是否删除';
COMMENT ON COLUMN erp_purchase_in_items.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_purchase_in_items_update_time
BEFORE UPDATE ON erp_purchase_in_items
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_purchase_order
-- ----------------------------
DROP TABLE IF EXISTS erp_purchase_order CASCADE;
CREATE TABLE erp_purchase_order (
  id BIGSERIAL PRIMARY KEY,
  no VARCHAR(255) NOT NULL,
  status SMALLINT NOT NULL,
  supplier_id BIGINT NOT NULL,
  account_id BIGINT,
  order_time TIMESTAMP NOT NULL,
  total_count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6) NOT NULL,
  total_product_price NUMERIC(24, 6) NOT NULL,
  total_tax_price NUMERIC(24, 6) NOT NULL,
  discount_percent NUMERIC(24, 6) NOT NULL,
  discount_price NUMERIC(24, 6) NOT NULL,
  deposit_price NUMERIC(24, 6) NOT NULL DEFAULT 0.000000,
  file_url VARCHAR(512),
  remark VARCHAR(1024),
  in_count NUMERIC(24, 6) NOT NULL DEFAULT 0.000000,
  return_count NUMERIC(24, 6) NOT NULL DEFAULT 0.000000,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_purchase_order IS 'ERP 采购订单表';
COMMENT ON COLUMN erp_purchase_order.id IS '编号';
COMMENT ON COLUMN erp_purchase_order.no IS '采购单编号';
COMMENT ON COLUMN erp_purchase_order.status IS '采购状态';
COMMENT ON COLUMN erp_purchase_order.supplier_id IS '供应商编号';
COMMENT ON COLUMN erp_purchase_order.account_id IS '结算账户编号';
COMMENT ON COLUMN erp_purchase_order.order_time IS '采购时间';
COMMENT ON COLUMN erp_purchase_order.total_count IS '合计数量';
COMMENT ON COLUMN erp_purchase_order.total_price IS '合计价格，单位：元';
COMMENT ON COLUMN erp_purchase_order.total_product_price IS '合计产品价格，单位：元';
COMMENT ON COLUMN erp_purchase_order.total_tax_price IS '合计税额，单位：元';
COMMENT ON COLUMN erp_purchase_order.discount_percent IS '优惠率，百分比';
COMMENT ON COLUMN erp_purchase_order.discount_price IS '优惠金额，单位：元';
COMMENT ON COLUMN erp_purchase_order.deposit_price IS '定金金额，单位：元';
COMMENT ON COLUMN erp_purchase_order.file_url IS '附件地址';
COMMENT ON COLUMN erp_purchase_order.remark IS '备注';
COMMENT ON COLUMN erp_purchase_order.in_count IS '采购入库数量';
COMMENT ON COLUMN erp_purchase_order.return_count IS '采购退货数量';
COMMENT ON COLUMN erp_purchase_order.creator IS '创建者';
COMMENT ON COLUMN erp_purchase_order.create_time IS '创建时间';
COMMENT ON COLUMN erp_purchase_order.updater IS '更新者';
COMMENT ON COLUMN erp_purchase_order.update_time IS '更新时间';
COMMENT ON COLUMN erp_purchase_order.deleted IS '是否删除';
COMMENT ON COLUMN erp_purchase_order.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_purchase_order_update_time
BEFORE UPDATE ON erp_purchase_order
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_purchase_order_items
-- ----------------------------
DROP TABLE IF EXISTS erp_purchase_order_items CASCADE;
CREATE TABLE erp_purchase_order_items (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_unit_id BIGINT NOT NULL,
  product_price NUMERIC(24, 6) NOT NULL,
  count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6) NOT NULL,
  tax_percent NUMERIC(24, 6),
  tax_price NUMERIC(24, 6),
  remark VARCHAR(1024),
  in_count NUMERIC(24, 6) NOT NULL DEFAULT 0.000000,
  return_count NUMERIC(24, 6) NOT NULL DEFAULT 0.000000,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_purchase_order_items IS 'ERP 采购订单项表';
COMMENT ON COLUMN erp_purchase_order_items.id IS '编号';
COMMENT ON COLUMN erp_purchase_order_items.order_id IS '采购订单编号';
COMMENT ON COLUMN erp_purchase_order_items.product_id IS '产品编号';
COMMENT ON COLUMN erp_purchase_order_items.product_unit_id IS '产品单位单位';
COMMENT ON COLUMN erp_purchase_order_items.product_price IS '产品单价';
COMMENT ON COLUMN erp_purchase_order_items.count IS '数量';
COMMENT ON COLUMN erp_purchase_order_items.total_price IS '总价';
COMMENT ON COLUMN erp_purchase_order_items.tax_percent IS '税率，百分比';
COMMENT ON COLUMN erp_purchase_order_items.tax_price IS '税额，单位：元';
COMMENT ON COLUMN erp_purchase_order_items.remark IS '备注';
COMMENT ON COLUMN erp_purchase_order_items.in_count IS '采购入库数量';
COMMENT ON COLUMN erp_purchase_order_items.return_count IS '采购退货数量';
COMMENT ON COLUMN erp_purchase_order_items.creator IS '创建者';
COMMENT ON COLUMN erp_purchase_order_items.create_time IS '创建时间';
COMMENT ON COLUMN erp_purchase_order_items.updater IS '更新者';
COMMENT ON COLUMN erp_purchase_order_items.update_time IS '更新时间';
COMMENT ON COLUMN erp_purchase_order_items.deleted IS '是否删除';
COMMENT ON COLUMN erp_purchase_order_items.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_purchase_order_items_update_time
BEFORE UPDATE ON erp_purchase_order_items
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_purchase_return
-- ----------------------------
DROP TABLE IF EXISTS erp_purchase_return CASCADE;
CREATE TABLE erp_purchase_return (
  id BIGSERIAL PRIMARY KEY,
  no VARCHAR(255) NOT NULL,
  status SMALLINT NOT NULL,
  supplier_id BIGINT NOT NULL,
  account_id BIGINT NOT NULL,
  return_time TIMESTAMP NOT NULL,
  order_id BIGINT NOT NULL,
  order_no VARCHAR(255) NOT NULL,
  total_count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6) NOT NULL,
  refund_price NUMERIC(24, 6) NOT NULL DEFAULT 0.000000,
  total_product_price NUMERIC(24, 6) NOT NULL,
  total_tax_price NUMERIC(24, 6) NOT NULL,
  discount_percent NUMERIC(24, 6) NOT NULL,
  discount_price NUMERIC(24, 6) NOT NULL,
  other_price NUMERIC(24, 6) NOT NULL DEFAULT 0.000000,
  file_url VARCHAR(512),
  remark VARCHAR(1024),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_purchase_return IS 'ERP 采购退货表';
COMMENT ON COLUMN erp_purchase_return.id IS '编号';
COMMENT ON COLUMN erp_purchase_return.no IS '采购退货编号';
COMMENT ON COLUMN erp_purchase_return.status IS '退货状态';
COMMENT ON COLUMN erp_purchase_return.supplier_id IS '供应商编号';
COMMENT ON COLUMN erp_purchase_return.account_id IS '结算账户编号';
COMMENT ON COLUMN erp_purchase_return.return_time IS '退货时间';
COMMENT ON COLUMN erp_purchase_return.order_id IS '采购订单编号';
COMMENT ON COLUMN erp_purchase_return.order_no IS '采购订单号';
COMMENT ON COLUMN erp_purchase_return.total_count IS '合计数量';
COMMENT ON COLUMN erp_purchase_return.total_price IS '合计价格，单位：元';
COMMENT ON COLUMN erp_purchase_return.refund_price IS '已退款金额，单位：元';
COMMENT ON COLUMN erp_purchase_return.total_product_price IS '合计产品价格，单位：元';
COMMENT ON COLUMN erp_purchase_return.total_tax_price IS '合计税额，单位：元';
COMMENT ON COLUMN erp_purchase_return.discount_percent IS '优惠率，百分比';
COMMENT ON COLUMN erp_purchase_return.discount_price IS '优惠金额，单位：元';
COMMENT ON COLUMN erp_purchase_return.other_price IS '其它金额，单位：元';
COMMENT ON COLUMN erp_purchase_return.file_url IS '附件地址';
COMMENT ON COLUMN erp_purchase_return.remark IS '备注';
COMMENT ON COLUMN erp_purchase_return.creator IS '创建者';
COMMENT ON COLUMN erp_purchase_return.create_time IS '创建时间';
COMMENT ON COLUMN erp_purchase_return.updater IS '更新者';
COMMENT ON COLUMN erp_purchase_return.update_time IS '更新时间';
COMMENT ON COLUMN erp_purchase_return.deleted IS '是否删除';
COMMENT ON COLUMN erp_purchase_return.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_purchase_return_update_time
BEFORE UPDATE ON erp_purchase_return
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_purchase_return_items
-- ----------------------------
DROP TABLE IF EXISTS erp_purchase_return_items CASCADE;
CREATE TABLE erp_purchase_return_items (
  id BIGSERIAL PRIMARY KEY,
  return_id BIGINT NOT NULL,
  order_item_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_unit_id BIGINT NOT NULL,
  product_price NUMERIC(24, 6) NOT NULL,
  count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6) NOT NULL,
  tax_percent NUMERIC(24, 6),
  tax_price NUMERIC(24, 6),
  remark VARCHAR(1024),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_purchase_return_items IS 'ERP 采购退货项表';
COMMENT ON COLUMN erp_purchase_return_items.id IS '编号';
COMMENT ON COLUMN erp_purchase_return_items.return_id IS '采购退货编号';
COMMENT ON COLUMN erp_purchase_return_items.order_item_id IS '采购订单项编号';
COMMENT ON COLUMN erp_purchase_return_items.warehouse_id IS '仓库编号';
COMMENT ON COLUMN erp_purchase_return_items.product_id IS '产品编号';
COMMENT ON COLUMN erp_purchase_return_items.product_unit_id IS '产品单位单位';
COMMENT ON COLUMN erp_purchase_return_items.product_price IS '产品单价';
COMMENT ON COLUMN erp_purchase_return_items.count IS '数量';
COMMENT ON COLUMN erp_purchase_return_items.total_price IS '总价';
COMMENT ON COLUMN erp_purchase_return_items.tax_percent IS '税率，百分比';
COMMENT ON COLUMN erp_purchase_return_items.tax_price IS '税额，单位：元';
COMMENT ON COLUMN erp_purchase_return_items.remark IS '备注';
COMMENT ON COLUMN erp_purchase_return_items.creator IS '创建者';
COMMENT ON COLUMN erp_purchase_return_items.create_time IS '创建时间';
COMMENT ON COLUMN erp_purchase_return_items.updater IS '更新者';
COMMENT ON COLUMN erp_purchase_return_items.update_time IS '更新时间';
COMMENT ON COLUMN erp_purchase_return_items.deleted IS '是否删除';
COMMENT ON COLUMN erp_purchase_return_items.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_purchase_return_items_update_time
BEFORE UPDATE ON erp_purchase_return_items
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_supplier
-- ----------------------------
DROP TABLE IF EXISTS erp_supplier CASCADE;
CREATE TABLE erp_supplier (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  contact VARCHAR(100),
  mobile VARCHAR(30),
  telephone VARCHAR(30),
  email VARCHAR(50),
  fax VARCHAR(30),
  remark VARCHAR(255),
  status SMALLINT NOT NULL,
  sort INTEGER NOT NULL,
  tax_no VARCHAR(50),
  tax_percent NUMERIC(24, 6),
  bank_name VARCHAR(50),
  bank_account VARCHAR(50),
  bank_address VARCHAR(50),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_supplier IS 'ERP 供应商表';
COMMENT ON COLUMN erp_supplier.id IS '供应商编号';
COMMENT ON COLUMN erp_supplier.name IS '供应商名称';
COMMENT ON COLUMN erp_supplier.contact IS '联系人';
COMMENT ON COLUMN erp_supplier.mobile IS '手机号码';
COMMENT ON COLUMN erp_supplier.telephone IS '联系电话';
COMMENT ON COLUMN erp_supplier.email IS '电子邮箱';
COMMENT ON COLUMN erp_supplier.fax IS '传真';
COMMENT ON COLUMN erp_supplier.remark IS '备注';
COMMENT ON COLUMN erp_supplier.status IS '开启状态';
COMMENT ON COLUMN erp_supplier.sort IS '排序';
COMMENT ON COLUMN erp_supplier.tax_no IS '纳税人识别号';
COMMENT ON COLUMN erp_supplier.tax_percent IS '税率';
COMMENT ON COLUMN erp_supplier.bank_name IS '开户行';
COMMENT ON COLUMN erp_supplier.bank_account IS '开户账号';
COMMENT ON COLUMN erp_supplier.bank_address IS '开户地址';
COMMENT ON COLUMN erp_supplier.creator IS '创建者';
COMMENT ON COLUMN erp_supplier.create_time IS '创建时间';
COMMENT ON COLUMN erp_supplier.updater IS '更新者';
COMMENT ON COLUMN erp_supplier.update_time IS '更新时间';
COMMENT ON COLUMN erp_supplier.deleted IS '是否删除';
COMMENT ON COLUMN erp_supplier.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_supplier_update_time
BEFORE UPDATE ON erp_supplier
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ========== 销售管理 ==========

-- ----------------------------
-- Table: erp_customer
-- ----------------------------
DROP TABLE IF EXISTS erp_customer CASCADE;
CREATE TABLE erp_customer (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  contact VARCHAR(100),
  mobile VARCHAR(30),
  telephone VARCHAR(30),
  email VARCHAR(50),
  fax VARCHAR(30),
  remark VARCHAR(255),
  status SMALLINT NOT NULL,
  sort INTEGER NOT NULL,
  tax_no VARCHAR(50),
  tax_percent NUMERIC(24, 6),
  bank_name VARCHAR(50),
  bank_account VARCHAR(50),
  bank_address VARCHAR(50),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_customer IS 'ERP 客户表';
COMMENT ON COLUMN erp_customer.id IS '客户编号';
COMMENT ON COLUMN erp_customer.name IS '客户名称';
COMMENT ON COLUMN erp_customer.contact IS '联系人';
COMMENT ON COLUMN erp_customer.mobile IS '手机号码';
COMMENT ON COLUMN erp_customer.telephone IS '联系电话';
COMMENT ON COLUMN erp_customer.email IS '电子邮箱';
COMMENT ON COLUMN erp_customer.fax IS '传真';
COMMENT ON COLUMN erp_customer.remark IS '备注';
COMMENT ON COLUMN erp_customer.status IS '开启状态';
COMMENT ON COLUMN erp_customer.sort IS '排序';
COMMENT ON COLUMN erp_customer.tax_no IS '纳税人识别号';
COMMENT ON COLUMN erp_customer.tax_percent IS '税率';
COMMENT ON COLUMN erp_customer.bank_name IS '开户行';
COMMENT ON COLUMN erp_customer.bank_account IS '开户账号';
COMMENT ON COLUMN erp_customer.bank_address IS '开户地址';
COMMENT ON COLUMN erp_customer.creator IS '创建者';
COMMENT ON COLUMN erp_customer.create_time IS '创建时间';
COMMENT ON COLUMN erp_customer.updater IS '更新者';
COMMENT ON COLUMN erp_customer.update_time IS '更新时间';
COMMENT ON COLUMN erp_customer.deleted IS '是否删除';
COMMENT ON COLUMN erp_customer.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_customer_update_time
BEFORE UPDATE ON erp_customer
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_sale_order
-- ----------------------------
DROP TABLE IF EXISTS erp_sale_order CASCADE;
CREATE TABLE erp_sale_order (
  id BIGSERIAL PRIMARY KEY,
  no VARCHAR(255) NOT NULL,
  status SMALLINT NOT NULL,
  customer_id BIGINT NOT NULL,
  account_id BIGINT,
  sale_user_id BIGINT,
  order_time TIMESTAMP NOT NULL,
  total_count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6) NOT NULL,
  total_product_price NUMERIC(24, 6) NOT NULL,
  total_tax_price NUMERIC(24, 6) NOT NULL,
  discount_percent NUMERIC(24, 6) NOT NULL,
  discount_price NUMERIC(24, 6) NOT NULL,
  deposit_price NUMERIC(24, 6) NOT NULL DEFAULT 0.000000,
  file_url VARCHAR(512),
  remark VARCHAR(1024),
  out_count NUMERIC(24, 6) NOT NULL DEFAULT 0.000000,
  return_count NUMERIC(24, 6) NOT NULL DEFAULT 0.000000,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_sale_order IS 'ERP 销售订单表';
COMMENT ON COLUMN erp_sale_order.id IS '编号';
COMMENT ON COLUMN erp_sale_order.no IS '销售单编号';
COMMENT ON COLUMN erp_sale_order.status IS '销售状态';
COMMENT ON COLUMN erp_sale_order.customer_id IS '客户编号';
COMMENT ON COLUMN erp_sale_order.account_id IS '结算账户编号';
COMMENT ON COLUMN erp_sale_order.sale_user_id IS '销售用户编号';
COMMENT ON COLUMN erp_sale_order.order_time IS '下单时间';
COMMENT ON COLUMN erp_sale_order.total_count IS '合计数量';
COMMENT ON COLUMN erp_sale_order.total_price IS '合计价格，单位：元';
COMMENT ON COLUMN erp_sale_order.total_product_price IS '合计产品价格，单位：元';
COMMENT ON COLUMN erp_sale_order.total_tax_price IS '合计税额，单位：元';
COMMENT ON COLUMN erp_sale_order.discount_percent IS '优惠率，百分比';
COMMENT ON COLUMN erp_sale_order.discount_price IS '优惠金额，单位：元';
COMMENT ON COLUMN erp_sale_order.deposit_price IS '定金金额，单位：元';
COMMENT ON COLUMN erp_sale_order.file_url IS '附件地址';
COMMENT ON COLUMN erp_sale_order.remark IS '备注';
COMMENT ON COLUMN erp_sale_order.out_count IS '销售出库数量';
COMMENT ON COLUMN erp_sale_order.return_count IS '销售退货数量';
COMMENT ON COLUMN erp_sale_order.creator IS '创建者';
COMMENT ON COLUMN erp_sale_order.create_time IS '创建时间';
COMMENT ON COLUMN erp_sale_order.updater IS '更新者';
COMMENT ON COLUMN erp_sale_order.update_time IS '更新时间';
COMMENT ON COLUMN erp_sale_order.deleted IS '是否删除';
COMMENT ON COLUMN erp_sale_order.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_sale_order_update_time
BEFORE UPDATE ON erp_sale_order
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_sale_order_items
-- ----------------------------
DROP TABLE IF EXISTS erp_sale_order_items CASCADE;
CREATE TABLE erp_sale_order_items (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_unit_id BIGINT NOT NULL,
  product_price NUMERIC(24, 6),
  count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6),
  tax_percent NUMERIC(24, 6),
  tax_price NUMERIC(24, 6),
  remark VARCHAR(1024),
  out_count NUMERIC(24, 6) NOT NULL DEFAULT 0.000000,
  return_count NUMERIC(24, 6) NOT NULL DEFAULT 0.000000,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_sale_order_items IS 'ERP 销售订单项表';
COMMENT ON COLUMN erp_sale_order_items.id IS '编号';
COMMENT ON COLUMN erp_sale_order_items.order_id IS '销售订单编号';
COMMENT ON COLUMN erp_sale_order_items.product_id IS '产品编号';
COMMENT ON COLUMN erp_sale_order_items.product_unit_id IS '产品单位单位';
COMMENT ON COLUMN erp_sale_order_items.product_price IS '产品单价';
COMMENT ON COLUMN erp_sale_order_items.count IS '数量';
COMMENT ON COLUMN erp_sale_order_items.total_price IS '总价';
COMMENT ON COLUMN erp_sale_order_items.tax_percent IS '税率，百分比';
COMMENT ON COLUMN erp_sale_order_items.tax_price IS '税额，单位：元';
COMMENT ON COLUMN erp_sale_order_items.remark IS '备注';
COMMENT ON COLUMN erp_sale_order_items.out_count IS '销售出库数量';
COMMENT ON COLUMN erp_sale_order_items.return_count IS '销售退货数量';
COMMENT ON COLUMN erp_sale_order_items.creator IS '创建者';
COMMENT ON COLUMN erp_sale_order_items.create_time IS '创建时间';
COMMENT ON COLUMN erp_sale_order_items.updater IS '更新者';
COMMENT ON COLUMN erp_sale_order_items.update_time IS '更新时间';
COMMENT ON COLUMN erp_sale_order_items.deleted IS '是否删除';
COMMENT ON COLUMN erp_sale_order_items.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_sale_order_items_update_time
BEFORE UPDATE ON erp_sale_order_items
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_sale_out
-- ----------------------------
DROP TABLE IF EXISTS erp_sale_out CASCADE;
CREATE TABLE erp_sale_out (
  id BIGSERIAL PRIMARY KEY,
  no VARCHAR(255) NOT NULL,
  status SMALLINT NOT NULL,
  customer_id BIGINT NOT NULL,
  account_id BIGINT NOT NULL,
  sale_user_id BIGINT,
  out_time TIMESTAMP NOT NULL,
  order_id BIGINT NOT NULL,
  order_no VARCHAR(255) NOT NULL,
  total_count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6) NOT NULL,
  receipt_price NUMERIC(24, 6) NOT NULL DEFAULT 0.000000,
  total_product_price NUMERIC(24, 6) NOT NULL,
  total_tax_price NUMERIC(24, 6) NOT NULL,
  discount_percent NUMERIC(24, 6) NOT NULL,
  discount_price NUMERIC(24, 6) NOT NULL,
  other_price NUMERIC(24, 6) NOT NULL DEFAULT 0.000000,
  file_url VARCHAR(512),
  remark VARCHAR(1024),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_sale_out IS 'ERP 销售出库表';
COMMENT ON COLUMN erp_sale_out.id IS '编号';
COMMENT ON COLUMN erp_sale_out.no IS '销售出库编号';
COMMENT ON COLUMN erp_sale_out.status IS '出库状态';
COMMENT ON COLUMN erp_sale_out.customer_id IS '客户编号';
COMMENT ON COLUMN erp_sale_out.account_id IS '结算账户编号';
COMMENT ON COLUMN erp_sale_out.sale_user_id IS '销售用户编号';
COMMENT ON COLUMN erp_sale_out.out_time IS '出库时间';
COMMENT ON COLUMN erp_sale_out.order_id IS '销售订单编号';
COMMENT ON COLUMN erp_sale_out.order_no IS '销售订单号';
COMMENT ON COLUMN erp_sale_out.total_count IS '合计数量';
COMMENT ON COLUMN erp_sale_out.total_price IS '合计价格，单位：元';
COMMENT ON COLUMN erp_sale_out.receipt_price IS '已收款金额，单位：元';
COMMENT ON COLUMN erp_sale_out.total_product_price IS '合计产品价格，单位：元';
COMMENT ON COLUMN erp_sale_out.total_tax_price IS '合计税额，单位：元';
COMMENT ON COLUMN erp_sale_out.discount_percent IS '优惠率，百分比';
COMMENT ON COLUMN erp_sale_out.discount_price IS '优惠金额，单位：元';
COMMENT ON COLUMN erp_sale_out.other_price IS '其它金额，单位：元';
COMMENT ON COLUMN erp_sale_out.file_url IS '附件地址';
COMMENT ON COLUMN erp_sale_out.remark IS '备注';
COMMENT ON COLUMN erp_sale_out.creator IS '创建者';
COMMENT ON COLUMN erp_sale_out.create_time IS '创建时间';
COMMENT ON COLUMN erp_sale_out.updater IS '更新者';
COMMENT ON COLUMN erp_sale_out.update_time IS '更新时间';
COMMENT ON COLUMN erp_sale_out.deleted IS '是否删除';
COMMENT ON COLUMN erp_sale_out.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_sale_out_update_time
BEFORE UPDATE ON erp_sale_out
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_sale_out_items
-- ----------------------------
DROP TABLE IF EXISTS erp_sale_out_items CASCADE;
CREATE TABLE erp_sale_out_items (
  id BIGSERIAL PRIMARY KEY,
  out_id BIGINT NOT NULL,
  order_item_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_unit_id BIGINT NOT NULL,
  product_price NUMERIC(24, 6) NOT NULL,
  count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6) NOT NULL,
  tax_percent NUMERIC(24, 6),
  tax_price NUMERIC(24, 6),
  remark VARCHAR(1024),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_sale_out_items IS 'ERP 销售出库项表';
COMMENT ON COLUMN erp_sale_out_items.id IS '编号';
COMMENT ON COLUMN erp_sale_out_items.out_id IS '销售出库编号';
COMMENT ON COLUMN erp_sale_out_items.order_item_id IS '销售订单项编号';
COMMENT ON COLUMN erp_sale_out_items.warehouse_id IS '仓库编号';
COMMENT ON COLUMN erp_sale_out_items.product_id IS '产品编号';
COMMENT ON COLUMN erp_sale_out_items.product_unit_id IS '产品单位单位';
COMMENT ON COLUMN erp_sale_out_items.product_price IS '产品单价';
COMMENT ON COLUMN erp_sale_out_items.count IS '数量';
COMMENT ON COLUMN erp_sale_out_items.total_price IS '总价';
COMMENT ON COLUMN erp_sale_out_items.tax_percent IS '税率，百分比';
COMMENT ON COLUMN erp_sale_out_items.tax_price IS '税额，单位：元';
COMMENT ON COLUMN erp_sale_out_items.remark IS '备注';
COMMENT ON COLUMN erp_sale_out_items.creator IS '创建者';
COMMENT ON COLUMN erp_sale_out_items.create_time IS '创建时间';
COMMENT ON COLUMN erp_sale_out_items.updater IS '更新者';
COMMENT ON COLUMN erp_sale_out_items.update_time IS '更新时间';
COMMENT ON COLUMN erp_sale_out_items.deleted IS '是否删除';
COMMENT ON COLUMN erp_sale_out_items.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_sale_out_items_update_time
BEFORE UPDATE ON erp_sale_out_items
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_sale_return
-- ----------------------------
DROP TABLE IF EXISTS erp_sale_return CASCADE;
CREATE TABLE erp_sale_return (
  id BIGSERIAL PRIMARY KEY,
  no VARCHAR(255) NOT NULL,
  status SMALLINT NOT NULL,
  customer_id BIGINT NOT NULL,
  account_id BIGINT NOT NULL,
  sale_user_id BIGINT,
  return_time TIMESTAMP NOT NULL,
  order_id BIGINT NOT NULL,
  order_no VARCHAR(255) NOT NULL,
  total_count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6) NOT NULL,
  refund_price NUMERIC(24, 6) NOT NULL DEFAULT 0.000000,
  total_product_price NUMERIC(24, 6) NOT NULL,
  total_tax_price NUMERIC(24, 6) NOT NULL,
  discount_percent NUMERIC(24, 6) NOT NULL,
  discount_price NUMERIC(24, 6) NOT NULL,
  other_price NUMERIC(24, 6) NOT NULL DEFAULT 0.000000,
  file_url VARCHAR(512),
  remark VARCHAR(1024),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_sale_return IS 'ERP 销售退货表';
COMMENT ON COLUMN erp_sale_return.id IS '编号';
COMMENT ON COLUMN erp_sale_return.no IS '销售退货编号';
COMMENT ON COLUMN erp_sale_return.status IS '退货状态';
COMMENT ON COLUMN erp_sale_return.customer_id IS '客户编号';
COMMENT ON COLUMN erp_sale_return.account_id IS '结算账户编号';
COMMENT ON COLUMN erp_sale_return.sale_user_id IS '销售用户编号';
COMMENT ON COLUMN erp_sale_return.return_time IS '退货时间';
COMMENT ON COLUMN erp_sale_return.order_id IS '销售订单编号';
COMMENT ON COLUMN erp_sale_return.order_no IS '销售订单号';
COMMENT ON COLUMN erp_sale_return.total_count IS '合计数量';
COMMENT ON COLUMN erp_sale_return.total_price IS '合计价格，单位：元';
COMMENT ON COLUMN erp_sale_return.refund_price IS '已退款金额，单位：元';
COMMENT ON COLUMN erp_sale_return.total_product_price IS '合计产品价格，单位：元';
COMMENT ON COLUMN erp_sale_return.total_tax_price IS '合计税额，单位：元';
COMMENT ON COLUMN erp_sale_return.discount_percent IS '优惠率，百分比';
COMMENT ON COLUMN erp_sale_return.discount_price IS '优惠金额，单位：元';
COMMENT ON COLUMN erp_sale_return.other_price IS '其它金额，单位：元';
COMMENT ON COLUMN erp_sale_return.file_url IS '附件地址';
COMMENT ON COLUMN erp_sale_return.remark IS '备注';
COMMENT ON COLUMN erp_sale_return.creator IS '创建者';
COMMENT ON COLUMN erp_sale_return.create_time IS '创建时间';
COMMENT ON COLUMN erp_sale_return.updater IS '更新者';
COMMENT ON COLUMN erp_sale_return.update_time IS '更新时间';
COMMENT ON COLUMN erp_sale_return.deleted IS '是否删除';
COMMENT ON COLUMN erp_sale_return.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_sale_return_update_time
BEFORE UPDATE ON erp_sale_return
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- Table: erp_sale_return_items
-- ----------------------------
DROP TABLE IF EXISTS erp_sale_return_items CASCADE;
CREATE TABLE erp_sale_return_items (
  id BIGSERIAL PRIMARY KEY,
  return_id BIGINT NOT NULL,
  order_item_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_unit_id BIGINT NOT NULL,
  product_price NUMERIC(24, 6) NOT NULL,
  count NUMERIC(24, 6) NOT NULL,
  total_price NUMERIC(24, 6) NOT NULL,
  tax_percent NUMERIC(24, 6),
  tax_price NUMERIC(24, 6),
  remark VARCHAR(1024),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

COMMENT ON TABLE erp_sale_return_items IS 'ERP 销售退货项表';
COMMENT ON COLUMN erp_sale_return_items.id IS '编号';
COMMENT ON COLUMN erp_sale_return_items.return_id IS '销售退货编号';
COMMENT ON COLUMN erp_sale_return_items.order_item_id IS '销售订单项编号';
COMMENT ON COLUMN erp_sale_return_items.warehouse_id IS '仓库编号';
COMMENT ON COLUMN erp_sale_return_items.product_id IS '产品编号';
COMMENT ON COLUMN erp_sale_return_items.product_unit_id IS '产品单位单位';
COMMENT ON COLUMN erp_sale_return_items.product_price IS '产品单价';
COMMENT ON COLUMN erp_sale_return_items.count IS '数量';
COMMENT ON COLUMN erp_sale_return_items.total_price IS '总价';
COMMENT ON COLUMN erp_sale_return_items.tax_percent IS '税率，百分比';
COMMENT ON COLUMN erp_sale_return_items.tax_price IS '税额，单位：元';
COMMENT ON COLUMN erp_sale_return_items.remark IS '备注';
COMMENT ON COLUMN erp_sale_return_items.creator IS '创建者';
COMMENT ON COLUMN erp_sale_return_items.create_time IS '创建时间';
COMMENT ON COLUMN erp_sale_return_items.updater IS '更新者';
COMMENT ON COLUMN erp_sale_return_items.update_time IS '更新时间';
COMMENT ON COLUMN erp_sale_return_items.deleted IS '是否删除';
COMMENT ON COLUMN erp_sale_return_items.tenant_id IS '租户编号';

CREATE TRIGGER update_erp_sale_return_items_update_time
BEFORE UPDATE ON erp_sale_return_items
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();
