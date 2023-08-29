ALTER TABLE `ruoyi-vue-pro`.`trade_after_sale_log`
    ADD COLUMN `before_status` int NOT NULL COMMENT '售前状态' AFTER `id`,
    ADD COLUMN `after_status` int NOT NULL COMMENT '售后状态' AFTER `before_status`;
