ALTER TABLE trade_order ADD COLUMN use_point int NOT NULL DEFAULT 0 COMMENT '使用的积分' AFTER point_price;
ALTER TABLE trade_order ADD COLUMN refund_point int NOT NULL DEFAULT 0 COMMENT '退还的使用积分' AFTER use_point;
ALTER TABLE trade_order ADD COLUMN give_point int NOT NULL DEFAULT 0 COMMENT '赠送的积分' AFTER refund_point;

ALTER TABLE trade_order_item ADD COLUMN use_point int NOT NULL DEFAULT 0 COMMENT '使用的积分' AFTER point_price;
ALTER TABLE trade_order_item ADD COLUMN give_point int NOT NULL DEFAULT 0 COMMENT '赠送的积分' AFTER use_point;
