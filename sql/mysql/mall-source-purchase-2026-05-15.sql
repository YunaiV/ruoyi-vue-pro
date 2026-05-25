-- 商城代发采购字段：商品来源、订单采购状态与源站下单信息

ALTER TABLE product_spu
    ADD COLUMN source_platform varchar(32) DEFAULT NULL COMMENT '来源平台' AFTER source_link,
    ADD COLUMN source_product_id varchar(128) DEFAULT NULL COMMENT '源商品编号' AFTER source_platform,
    ADD COLUMN source_shop_name varchar(255) DEFAULT NULL COMMENT '源店铺名称' AFTER source_product_id;

ALTER TABLE product_sku
    ADD COLUMN source_sku_id varchar(128) DEFAULT NULL COMMENT '源 SKU 编号' AFTER sales_count,
    ADD COLUMN source_sku_name varchar(255) DEFAULT NULL COMMENT '源 SKU 规格名称' AFTER source_sku_id,
    ADD COLUMN source_sku_url varchar(512) DEFAULT NULL COMMENT '源 SKU 链接' AFTER source_sku_name,
    ADD COLUMN source_price int DEFAULT NULL COMMENT '源采购价，单位：分' AFTER source_sku_url;

ALTER TABLE trade_order
    ADD COLUMN purchase_status tinyint NOT NULL DEFAULT 0 COMMENT '采购状态：0 未采购，10 已拣货，20 已下单，30 已同步物流' AFTER comment_status,
    ADD COLUMN source_platform varchar(32) DEFAULT NULL COMMENT '来源平台' AFTER purchase_status,
    ADD COLUMN source_order_no varchar(128) DEFAULT NULL COMMENT '源站订单号' AFTER source_platform,
    ADD COLUMN source_order_time datetime DEFAULT NULL COMMENT '源站下单时间' AFTER source_order_no,
    ADD COLUMN purchase_remark varchar(512) DEFAULT NULL COMMENT '采购备注' AFTER source_order_time;

ALTER TABLE trade_order_item
    ADD COLUMN source_link varchar(512) DEFAULT NULL COMMENT '源商品链接' AFTER comment_status,
    ADD COLUMN source_platform varchar(32) DEFAULT NULL COMMENT '来源平台' AFTER source_link,
    ADD COLUMN source_product_id varchar(128) DEFAULT NULL COMMENT '源商品编号' AFTER source_platform,
    ADD COLUMN source_sku_id varchar(128) DEFAULT NULL COMMENT '源 SKU 编号' AFTER source_product_id,
    ADD COLUMN source_sku_name varchar(255) DEFAULT NULL COMMENT '源 SKU 规格名称' AFTER source_sku_id,
    ADD COLUMN source_sku_url varchar(512) DEFAULT NULL COMMENT '源 SKU 链接' AFTER source_sku_name,
    ADD COLUMN source_price int DEFAULT NULL COMMENT '源采购价，单位：分' AFTER source_sku_url;

INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted)
VALUES ('交易订单采购状态', 'trade_order_purchase_status', 0, '交易订单采购状态', '1', NOW(), '1', NOW(), b'0');

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark, creator, create_time, updater, update_time, deleted)
VALUES
    (0, '未采购', '0', 'trade_order_purchase_status', 0, 'info', '', '交易订单采购状态 - 未采购', '1', NOW(), '1', NOW(), b'0'),
    (10, '已拣货', '10', 'trade_order_purchase_status', 0, 'warning', '', '交易订单采购状态 - 已拣货', '1', NOW(), '1', NOW(), b'0'),
    (20, '已下单', '20', 'trade_order_purchase_status', 0, 'primary', '', '交易订单采购状态 - 已下单', '1', NOW(), '1', NOW(), b'0'),
    (30, '已同步物流', '30', 'trade_order_purchase_status', 0, 'success', '', '交易订单采购状态 - 已同步物流', '1', NOW(), '1', NOW(), b'0');
