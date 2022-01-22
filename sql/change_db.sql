ALTER TABLE `ruoyi-vue-pro`.`pay_order_extension`
CHANGE COLUMN `channel_notify_data` `channel_notify_data` VARCHAR(2048) CHARACTER SET 'utf8mb4' NULL DEFAULT NULL COMMENT '支付渠道异步通知的内容' ;

ALTER TABLE `ruoyi-vue-pro`.`pay_refund`
CHANGE COLUMN `req_no` `req_no` VARCHAR(64) NULL COMMENT '退款单请求号' ;

ALTER TABLE `ruoyi-vue-pro`.`pay_refund`
DROP COLUMN `req_no`;
