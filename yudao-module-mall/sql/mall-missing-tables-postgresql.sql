-- PostgreSQL script for missing Mall tables
-- Generated manually based on MySQL definitions

-- product_spu table
DROP TABLE IF EXISTS product_spu;
CREATE TABLE product_spu (
    id int8 NOT NULL,
    name varchar(128) NOT NULL,
    keyword varchar(256) NULL DEFAULT NULL,
    introduction varchar(256) NULL DEFAULT NULL,
    description text NULL,
    category_id int8 NOT NULL,
    brand_id int4 NULL DEFAULT NULL,
    pic_url varchar(256) NOT NULL,
    slider_pic_urls varchar(2000) NULL DEFAULT '',
    sort int4 NOT NULL DEFAULT 0,
    status int2 NOT NULL,
    spec_type bool NULL DEFAULT NULL,
    price int4 NOT NULL DEFAULT -1,
    market_price int4 NULL DEFAULT NULL,
    cost_price int4 NOT NULL DEFAULT -1,
    stock int4 NOT NULL DEFAULT 0,
    delivery_types varchar(32) NOT NULL DEFAULT '',
    delivery_template_id int8 NULL DEFAULT NULL,
    give_integral int4 NOT NULL DEFAULT 0,
    sub_commission_type bool NULL DEFAULT NULL,
    sales_count int4 NULL DEFAULT 0,
    virtual_sales_count int4 NULL DEFAULT 0,
    browse_count int4 NULL DEFAULT 0,
    create_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    creator varchar(64) NULL DEFAULT NULL,
    updater varchar(64) NULL DEFAULT NULL,
    deleted bool NOT NULL DEFAULT false,
    tenant_id int8 NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE product_spu IS '商品spu';
CREATE INDEX idx_product_spu_category_id ON product_spu(category_id);
CREATE INDEX idx_product_spu_brand_id ON product_spu(brand_id);
CREATE SEQUENCE IF NOT EXISTS product_spu_seq START WITH 700;

-- trade_order table
DROP TABLE IF EXISTS trade_order;
CREATE TABLE trade_order (
    id int8 NOT NULL,
    no varchar(32) NOT NULL,
    type int4 NOT NULL DEFAULT 0,
    terminal int4 NOT NULL,
    user_id int8 NOT NULL,
    user_ip varchar(30) NOT NULL DEFAULT '',
    user_remark varchar(200) NULL DEFAULT NULL,
    status int4 NOT NULL,
    product_count int4 NOT NULL,
    cancel_type int4 NULL DEFAULT NULL,
    remark varchar(200) NULL DEFAULT NULL,
    pay_status bool NOT NULL DEFAULT false,
    pay_time timestamp NULL DEFAULT NULL,
    finish_time timestamp NULL DEFAULT NULL,
    cancel_time timestamp NULL DEFAULT NULL,
    original_price int4 NOT NULL DEFAULT 0,
    order_price int4 NOT NULL DEFAULT 0,
    discount_price int4 NOT NULL DEFAULT 0,
    delivery_price int4 NOT NULL DEFAULT 0,
    adjust_price int4 NOT NULL DEFAULT 0,
    pay_price int4 NOT NULL DEFAULT 0,
    pay_order_id int8 NULL DEFAULT NULL,
    pay_channel_code varchar(16) NULL DEFAULT NULL,
    delivery_type int4 NOT NULL,
    delivery_template_id int8 NULL DEFAULT NULL,
    logistics_id int8 NULL DEFAULT NULL,
    logistics_no varchar(64) NULL DEFAULT NULL,
    delivery_time timestamp NULL DEFAULT NULL,
    receive_time timestamp NULL DEFAULT NULL,
    receiver_name varchar(20) NOT NULL,
    receiver_mobile varchar(20) NOT NULL,
    receiver_area_id int4 NOT NULL,
    receiver_detail_address varchar(255) NOT NULL,
    pick_up_store_id int8 NULL DEFAULT NULL,
    pick_up_verify_code varchar(64) NULL DEFAULT NULL,
    after_sale_status int4 NOT NULL DEFAULT 0,
    refund_price int4 NOT NULL DEFAULT 0,
    coupon_id int8 NULL DEFAULT NULL,
    coupon_price int4 NOT NULL DEFAULT 0,
    point_price int4 NOT NULL DEFAULT 0,
    use_point int4 NOT NULL DEFAULT 0,
    give_point int4 NOT NULL DEFAULT 0,
    refund_point int4 NOT NULL DEFAULT 0,
    vip_price int4 NOT NULL DEFAULT 0,
    seckill_activity_id int8 NULL DEFAULT NULL,
    bargain_activity_id int8 NULL DEFAULT NULL,
    bargain_record_id int8 NULL DEFAULT NULL,
    combination_activity_id int8 NULL DEFAULT NULL,
    combination_head_id int8 NULL DEFAULT NULL,
    combination_record_id int8 NULL DEFAULT NULL,
    brokerage_user_id int8 NULL DEFAULT NULL,
    creator varchar(64) NULL DEFAULT '',
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater varchar(64) NULL DEFAULT '',
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted bool NOT NULL DEFAULT false,
    tenant_id int8 NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE trade_order IS '交易订单表';
CREATE UNIQUE INDEX uk_trade_order_no ON trade_order(no);
CREATE INDEX idx_trade_order_user_id ON trade_order(user_id);
CREATE SEQUENCE IF NOT EXISTS trade_order_seq START WITH 200;

-- trade_order_item table
DROP TABLE IF EXISTS trade_order_item;
CREATE TABLE trade_order_item (
    id int8 NOT NULL,
    user_id int8 NOT NULL,
    order_id int8 NOT NULL,
    cart_id int8 NULL DEFAULT NULL,
    spu_id int8 NOT NULL,
    spu_name varchar(255) NOT NULL,
    sku_id int8 NOT NULL,
    properties jsonb NULL,
    pic_url varchar(200) NULL DEFAULT NULL,
    count int4 NOT NULL,
    comment_status bool NOT NULL DEFAULT false,
    price int4 NOT NULL DEFAULT 0,
    discount_price int4 NOT NULL DEFAULT 0,
    delivery_price int4 NOT NULL DEFAULT 0,
    adjust_price int4 NOT NULL DEFAULT 0,
    pay_price int4 NOT NULL DEFAULT 0,
    original_price int4 NOT NULL DEFAULT 0,
    original_unit_price int4 NOT NULL DEFAULT 0,
    coupon_price int4 NOT NULL DEFAULT 0,
    point_price int4 NOT NULL DEFAULT 0,
    use_point int4 NOT NULL DEFAULT 0,
    give_point int4 NOT NULL DEFAULT 0,
    vip_price int4 NOT NULL DEFAULT 0,
    after_sale_id int8 NULL DEFAULT NULL,
    after_sale_status int4 NOT NULL DEFAULT 0,
    creator varchar(64) NULL DEFAULT '',
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater varchar(64) NULL DEFAULT '',
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted bool NOT NULL DEFAULT false,
    tenant_id int8 NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE trade_order_item IS '交易订单明细表';
CREATE INDEX idx_trade_order_item_order_id ON trade_order_item(order_id);
CREATE INDEX idx_trade_order_item_user_id ON trade_order_item(user_id);
CREATE SEQUENCE IF NOT EXISTS trade_order_item_seq START WITH 200;

-- trade_after_sale table
DROP TABLE IF EXISTS trade_after_sale;
CREATE TABLE trade_after_sale (
    id int8 NOT NULL,
    no varchar(32) NOT NULL,
    user_id int8 NOT NULL,
    order_id int8 NOT NULL,
    order_no varchar(32) NOT NULL,
    order_item_id int8 NOT NULL,
    spu_id int8 NOT NULL,
    spu_name varchar(255) NOT NULL,
    sku_id int8 NOT NULL,
    properties jsonb NULL,
    pic_url varchar(200) NULL DEFAULT NULL,
    count int4 NOT NULL,
    audit_time timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    audit_user_id int8 NULL DEFAULT NULL,
    audit_reason varchar(255) NULL DEFAULT NULL,
    type int4 NOT NULL,
    way int4 NOT NULL,
    status int4 NOT NULL,
    apply_reason varchar(255) NOT NULL,
    apply_description varchar(255) NULL DEFAULT NULL,
    apply_pic_urls varchar(1000) NULL DEFAULT NULL,
    pay_refund_id int8 NULL DEFAULT NULL,
    refund_price int4 NOT NULL DEFAULT 0,
    refund_point int4 NOT NULL DEFAULT 0,
    refund_time timestamp NULL DEFAULT NULL,
    logistics_id int8 NULL DEFAULT NULL,
    logistics_no varchar(64) NULL DEFAULT NULL,
    logistics_time timestamp NULL DEFAULT NULL,
    receive_time timestamp NULL DEFAULT NULL,
    receive_reason varchar(255) NULL DEFAULT NULL,
    creator varchar(64) NULL DEFAULT '',
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater varchar(64) NULL DEFAULT '',
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted bool NOT NULL DEFAULT false,
    tenant_id int8 NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE trade_after_sale IS '交易售后表';
CREATE UNIQUE INDEX uk_trade_after_sale_no ON trade_after_sale(no);
CREATE INDEX idx_trade_after_sale_order_id ON trade_after_sale(order_id);
CREATE INDEX idx_trade_after_sale_user_id ON trade_after_sale(user_id);
CREATE SEQUENCE IF NOT EXISTS trade_after_sale_seq START WITH 100;

-- trade_cart table
DROP TABLE IF EXISTS trade_cart;
CREATE TABLE trade_cart (
    id int8 NOT NULL,
    user_id int8 NOT NULL,
    spu_id int8 NOT NULL,
    sku_id int8 NOT NULL,
    count int4 NOT NULL,
    selected bool NOT NULL DEFAULT true,
    creator varchar(64) NULL DEFAULT '',
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater varchar(64) NULL DEFAULT '',
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted bool NOT NULL DEFAULT false,
    tenant_id int8 NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE trade_cart IS '购物车';
CREATE INDEX idx_trade_cart_user_id ON trade_cart(user_id);
CREATE SEQUENCE IF NOT EXISTS trade_cart_seq START WITH 100;

-- promotion_coupon table
DROP TABLE IF EXISTS promotion_coupon;
CREATE TABLE promotion_coupon (
    id int8 NOT NULL,
    template_id int8 NOT NULL,
    name varchar(64) NOT NULL,
    take_type int4 NOT NULL,
    use_price int4 NOT NULL DEFAULT 0,
    valid_start_time timestamp NOT NULL,
    valid_end_time timestamp NOT NULL,
    discount_type int4 NOT NULL,
    discount_percent int4 NULL DEFAULT NULL,
    discount_price int4 NULL DEFAULT NULL,
    discount_limit_price int4 NULL DEFAULT NULL,
    user_id int8 NOT NULL,
    use_order_id int8 NULL DEFAULT NULL,
    status int4 NOT NULL,
    use_time timestamp NULL DEFAULT NULL,
    creator varchar(64) NULL DEFAULT '',
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater varchar(64) NULL DEFAULT '',
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted bool NOT NULL DEFAULT false,
    tenant_id int8 NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE promotion_coupon IS '优惠券';
CREATE INDEX idx_promotion_coupon_user_id ON promotion_coupon(user_id);
CREATE INDEX idx_promotion_coupon_template_id ON promotion_coupon(template_id);
CREATE SEQUENCE IF NOT EXISTS promotion_coupon_seq START WITH 100;

-- promotion_coupon_template table
DROP TABLE IF EXISTS promotion_coupon_template;
CREATE TABLE promotion_coupon_template (
    id int8 NOT NULL,
    name varchar(64) NOT NULL,
    status int4 NOT NULL,
    total_count int4 NOT NULL DEFAULT -1,
    take_limit_count int4 NOT NULL DEFAULT 1,
    take_type int4 NOT NULL,
    use_price int4 NOT NULL DEFAULT 0,
    product_scope int4 NOT NULL,
    product_scope_values varchar(500) NULL DEFAULT NULL,
    validity_type int4 NOT NULL,
    valid_start_time timestamp NULL DEFAULT NULL,
    valid_end_time timestamp NULL DEFAULT NULL,
    fixed_start_term int4 NULL DEFAULT NULL,
    fixed_end_term int4 NULL DEFAULT NULL,
    discount_type int4 NOT NULL,
    discount_percent int4 NULL DEFAULT NULL,
    discount_price int4 NULL DEFAULT NULL,
    discount_limit_price int4 NULL DEFAULT NULL,
    take_count int4 NOT NULL DEFAULT 0,
    use_count int4 NOT NULL DEFAULT 0,
    creator varchar(64) NULL DEFAULT '',
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater varchar(64) NULL DEFAULT '',
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted bool NOT NULL DEFAULT false,
    tenant_id int8 NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE promotion_coupon_template IS '优惠券模板';
CREATE SEQUENCE IF NOT EXISTS promotion_coupon_template_seq START WITH 10;

-- promotion_seckill_activity table
DROP TABLE IF EXISTS promotion_seckill_activity;
CREATE TABLE promotion_seckill_activity (
    id int8 NOT NULL,
    spu_id int8 NOT NULL,
    name varchar(64) NOT NULL,
    status int4 NOT NULL DEFAULT -1,
    remark varchar(255) NULL DEFAULT NULL,
    start_time timestamp NOT NULL,
    end_time timestamp NOT NULL,
    sort int4 NOT NULL DEFAULT 0,
    config_ids varchar(100) NOT NULL,
    order_count int4 NOT NULL DEFAULT 0,
    user_count int4 NOT NULL DEFAULT 0,
    total_price int4 NOT NULL DEFAULT 0,
    total_limit_count int4 NULL DEFAULT NULL,
    single_limit_count int4 NULL DEFAULT NULL,
    stock int4 NULL DEFAULT NULL,
    total_stock int4 NULL DEFAULT NULL,
    creator varchar(64) NULL DEFAULT '',
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater varchar(64) NULL DEFAULT '',
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted bool NOT NULL DEFAULT false,
    tenant_id int8 NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE promotion_seckill_activity IS '秒杀活动';
CREATE SEQUENCE IF NOT EXISTS promotion_seckill_activity_seq START WITH 10;

-- promotion_combination_activity table
DROP TABLE IF EXISTS promotion_combination_activity;
CREATE TABLE promotion_combination_activity (
    id int8 NOT NULL,
    name varchar(64) NOT NULL,
    spu_id int8 NOT NULL,
    total_limit_count int4 NOT NULL,
    single_limit_count int4 NOT NULL,
    start_time timestamp NOT NULL,
    end_time timestamp NOT NULL,
    user_size int4 NOT NULL,
    virtual_group int4 NOT NULL DEFAULT 0,
    status int4 NOT NULL DEFAULT 0,
    limit_duration int4 NOT NULL,
    order_count int4 NOT NULL DEFAULT 0,
    user_count int4 NOT NULL DEFAULT 0,
    virtual_group_count int4 NOT NULL DEFAULT 0,
    total_price int4 NOT NULL DEFAULT 0,
    creator varchar(64) NULL DEFAULT '',
    create_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater varchar(64) NULL DEFAULT '',
    update_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted bool NOT NULL DEFAULT false,
    tenant_id int8 NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
COMMENT ON TABLE promotion_combination_activity IS '拼团活动';
CREATE SEQUENCE IF NOT EXISTS promotion_combination_activity_seq START WITH 10;
