-- 增加字段
ALTER TABLE trade_order
    ADD COLUMN brokerage_user_id bigint NULL COMMENT '推广人编号' AFTER comment_status;
