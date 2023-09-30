-- ----------------------------
-- 转账单表
-- ----------------------------
DROP TABLE IF EXISTS `pay_transfer`;
CREATE TABLE `pay_transfer`
(
    `id`                   bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `type`                 int          NOT NULL COMMENT '类型',
    `app_id`               bigint       NOT NULL COMMENT '应用编号',
    `merchant_order_id`    varchar(64)  NOT NULL COMMENT '商户订单编号',
    `price`                int          NOT NULL COMMENT '转账金额，单位：分',
    `title`                varchar(512) NOT NULL COMMENT '转账标题',
    `payee_info`           varchar(512) NOT NULL COMMENT '收款人信息，不同类型和渠道不同',
    `status`               tinyint      NOT NULL COMMENT '转账状态',
    `success_time`         datetime     NULL COMMENT '转账成功时间',
    `extension_id`         bigint       NULL  COMMENT '转账渠道编号',
    `no`                   varchar(64)  NULL COMMENT '转账单号',
    `channel_id`           bigint       NULL  COMMENT '转账渠道编号',
    `channel_code`         varchar(32)  NULL  COMMENT '转账渠道编码',
    `creator`              varchar(64)  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
    `create_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`              varchar(64)  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
    `update_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`              bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`            bigint       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='转账单表';

-- ----------------------------
-- 转账扩展单
-- ----------------------------
DROP TABLE IF EXISTS `pay_transfer_extension`;
CREATE TABLE `pay_transfer_extension`
(
    `id`                   bigint        NOT NULL AUTO_INCREMENT COMMENT '编号',
    `no`                   varchar(64)   NOT NULL COMMENT '转账单号',
    `transfer_id`          bigint        NOT NULL COMMENT '转账单编号',
    `channel_id`           bigint        NOT NULL COMMENT '转账渠道编号',
    `channel_code`         varchar(32)   NOT NULL COMMENT '转账渠道编码',
    `channel_extras`       varchar(512)  NULL DEFAULT NULL COMMENT '支付渠道的额外参数',
    `status`               tinyint       NOT NULL COMMENT '转账状态',
    `channel_notify_data`  varchar(4096) NULL DEFAULT NULL COMMENT '支付渠道异步通知的内容',
    `creator`              varchar(64)   NULL DEFAULT '' COMMENT '创建者',
    `create_time`          datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`              varchar(64)   NULL DEFAULT '' COMMENT '更新者',
    `update_time`          datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`              bit(1)        NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`            bigint        NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='转账拓展单表';

-- ----------------------------
-- Table structure for pay_demo_transfer
-- ----------------------------
DROP TABLE IF EXISTS `pay_demo_transfer`;
CREATE TABLE `pay_demo_transfer`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单编号',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户编号',
  `price` int NOT NULL COMMENT '转账金额，单位：分',
  `type`  int NOT NULL COMMENT '转账类型',
  `payee_info` varchar(512) NOT NULL COMMENT '收款人信息，不同类型和渠道不同',
  `transfer_status` tinyint      NOT NULL DEFAULT 0 COMMENT '转账状态',
  `pay_transfer_id` bigint NULL DEFAULT NULL COMMENT '转账订单编号',
  `pay_channel_code` varchar(16)  NULL DEFAULT NULL COMMENT '转账支付成功渠道',
  `transfer_time` datetime NULL DEFAULT NULL COMMENT '转账支付时间',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
   PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB  COMMENT = '示例业务转账订单\n';


ALTER TABLE `pay_channel`
    MODIFY COLUMN `config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '支付渠道配置' AFTER `app_id`;

