-- ----------------------------
-- 转账单表
-- ----------------------------
DROP TABLE IF EXISTS `pay_transfer`;
CREATE TABLE `pay_transfer`
(
    `id`                   bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `no`                   varchar(64)  NOT NULL COMMENT '转账单号',
    `app_id`               bigint       NOT NULL COMMENT '应用编号',
    `channel_id`           bigint       NOT NULL  COMMENT '转账渠道编号',
    `channel_code`         varchar(32)  NOT NULL  COMMENT '转账渠道编码',
    `merchant_transfer_id` varchar(64)  NOT NULL COMMENT '商户转账单编号',
    `type`                 int          NOT NULL COMMENT '类型',
    `status`               tinyint      NOT NULL COMMENT '转账状态',
    `success_time`         datetime     NULL COMMENT '转账成功时间',
    `price`                int          NOT NULL COMMENT '转账金额，单位：分',
    `subject`              varchar(512) NOT NULL COMMENT '转账标题',
    `user_name`            varchar(64)  NULL COMMENT '收款人姓名',
    `alipay_logon_id`      varchar(64)  NULL COMMENT '支付宝登录号',
    `openid`               varchar(64)   NULL COMMENT '微信 openId',
    `notify_url`           varchar(1024) NOT NULL COMMENT '异步通知商户地址',
    `user_ip`              varchar(50)   NOT NULL COMMENT '用户 IP',
    `channel_extras`       varchar(512) NULL DEFAULT NULL COMMENT '渠道的额外参数',
    `channel_transfer_no`  varchar(64)  NULL DEFAULT NULL COMMENT '渠道转账单号',
    `channel_error_code`   varchar(128) NULL DEFAULT NULL COMMENT '调用渠道的错误码',
    `channel_error_msg`    varchar(256) NULL DEFAULT NULL COMMENT '调用渠道的错误提示',
    `channel_notify_data`  varchar(4096) NULL DEFAULT NULL COMMENT '渠道的同步/异步通知的内容',
    `creator`              varchar(64)  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
    `create_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`              varchar(64)  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
    `update_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`              bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`            bigint       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='转账单表';

-- ----------------------------
-- Table structure for pay_demo_transfer
-- ----------------------------
DROP TABLE IF EXISTS `pay_demo_transfer`;
CREATE TABLE `pay_demo_transfer`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单编号',
  `app_id`  bigint NOT NULL COMMENT '应用编号',
  `type`  int NOT NULL COMMENT '转账类型',
  `price` int NOT NULL COMMENT '转账金额，单位：分',
  `user_name`            varchar(64)  NULL COMMENT '收款人姓名',
  `alipay_logon_id`      varchar(64)  NULL COMMENT '支付宝登录号',
  `openid`               varchar(64)  NULL COMMENT '微信 openId',
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
) ENGINE = InnoDB  COMMENT = '示例业务转账订单';


-- ALTER TABLE `pay_channel`
--   MODIFY COLUMN `config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '支付渠道配置' AFTER `app_id`;

-- ----------------------------
-- 充值套餐表
-- ----------------------------
DROP TABLE IF EXISTS `pay_wallet_recharge_package`;
CREATE TABLE `pay_wallet_recharge_package`
(
    `id`                   bigint       NOT NULL AUTO_INCREMENT COMMENT '编号',
    `name`                 varchar(64)  NOT NULL COMMENT '套餐名',
    `pay_price`            int          NOT NULL COMMENT '支付金额',
    `bonus_price`          int          NOT NULL COMMENT '赠送金额',
    `status`               tinyint      NOT NULL COMMENT '状态',
    `creator`              varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT '' COMMENT '创建者',
    `create_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`              varchar(64)  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
    `update_time`          datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`              bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`            bigint       NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='充值套餐表';

-- ----------------------------
-- Table structure for pay_wallet_recharge
-- ----------------------------
DROP TABLE IF EXISTS `pay_wallet_recharge`;
CREATE TABLE `pay_wallet_recharge`  (
    `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '编号',
    `wallet_id` bigint(0) NOT NULL COMMENT '会员钱包 id',
    `total_price` int(0) NOT NULL COMMENT '用户实际到账余额，例如充 100 送 20，则该值是 120',
    `pay_price` int(0) NOT NULL COMMENT '实际支付金额',
    `bonus_price` int(0) NOT NULL COMMENT '钱包赠送金额',
    `package_id` bigint(0) DEFAULT NULL COMMENT '充值套餐编号',
    `pay_status` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否已支付：[0:未支付 1:已经支付过]',
    `pay_order_id` bigint(0) DEFAULT NULL COMMENT '支付订单编号',
    `pay_channel_code` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '支付成功的支付渠道',
    `pay_time` datetime(0) DEFAULT NULL COMMENT '订单支付时间',
    `pay_refund_id` bigint(0) DEFAULT NULL COMMENT '支付退款单编号',
    `refund_total_price` int(0) NOT NULL DEFAULT 0 COMMENT '退款金额，包含赠送金额',
    `refund_pay_price` int(0) NOT NULL DEFAULT 0 COMMENT '退款支付金额',
    `refund_bonus_price` int(0) NOT NULL DEFAULT 0 COMMENT '退款钱包赠送金额',
    `refund_time` datetime(0) DEFAULT NULL COMMENT '退款时间',
    `refund_status` int(0) NOT NULL DEFAULT 0 COMMENT '退款状态',
    `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
    `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
    `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint(0) NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员钱包充值' ROW_FORMAT = Dynamic;

-- 钱包充值套餐,钱包余额菜单脚本

INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '钱包管理', '', 1, 5, 1117,
           'wallet', 'ep:caret-right', '', 0, ''
       );
SELECT @parentId1 := LAST_INSERT_ID();

INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '充值套餐', '', 2, 2, @parentId1,
           'wallet-recharge-package', 'fa:leaf', 'pay/wallet/rechargePackage/index', 0, 'WalletRechargePackage'
       );
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '钱包充值套餐查询', 'pay:wallet-recharge-package:query', 3, 1, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '钱包充值套餐创建', 'pay:wallet-recharge-package:create', 3, 2, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '钱包充值套餐更新', 'pay:wallet-recharge-package:update', 3, 3, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '钱包充值套餐删除', 'pay:wallet-recharge-package:delete', 3, 4, @parentId,
           '', '', '', 0
       );

INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '钱包余额', '', 2, 1, @parentId1,
           'wallet-balance', 'fa:leaf', 'pay/wallet/balance/index', 0, 'WalletBalance'
       );

SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '钱包余额查询', 'pay:wallet:query', 3, 1, @parentId,
           '', '', '', 0
       );

-- 支付实战和转账实战数据库脚本

update  system_menu set deleted = 1  where id = 2161;

INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '接入示例', '', 1, 99, 1117,
           'demo', 'ep:caret-right', '', 0, ''
       );

SELECT @parentId1 := LAST_INSERT_ID();

INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '支付实战', '', 2, 1, @parentId1,
           'demo-order', 'fa:leaf', 'pay/demo/order/index', 0, NULL
       );

INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '转账实战', '', 2, 1, @parentId1,
           'demo-transfer', 'fa:leaf', 'pay/demo/transfer/index', 0, NULL
       );

-- 转账状态和转账类型数据字典
INSERT INTO `system_dict_type`(`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `deleted_time`) VALUES ('支付转账类型', 'pay_transfer_type', 0, '', '1', '2023-10-28 16:27:18', '1', '2023-10-28 16:27:18', b'0', '1970-01-01 00:00:00');
INSERT INTO `system_dict_type`(`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `deleted_time`) VALUES ('转账订单状态', 'pay_transfer_status', 0, '', '1', '2023-10-28 16:18:32', '1', '2023-10-28 16:18:32', b'0', '1970-01-01 00:00:00');

INSERT INTO `system_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (4, '钱包余额', '4', 'pay_transfer_type', 0, 'info', '', '', '1', '2023-10-28 16:28:37', '1', '2023-10-28 16:28:37', b'0');
INSERT INTO `system_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (3, '银行卡', '3', 'pay_transfer_type', 0, 'default', '', '', '1', '2023-10-28 16:28:21', '1', '2023-10-28 16:28:21', b'0');
INSERT INTO `system_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2, '微信余额', '2', 'pay_transfer_type', 0, 'info', '', '', '1', '2023-10-28 16:28:07', '1', '2023-10-28 16:28:07', b'0');
INSERT INTO `system_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1, '支付宝余额', '1', 'pay_transfer_type', 0, 'default', '', '', '1', '2023-10-28 16:27:44', '1', '2023-10-28 16:27:44', b'0');
INSERT INTO `system_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (4, '转账失败', '30', 'pay_transfer_status', 0, 'warning', '', '', '1', '2023-10-28 16:24:16', '1', '2023-10-28 16:24:16', b'0');
INSERT INTO `system_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (3, '转账成功', '20', 'pay_transfer_status', 0, 'success', '', '', '1', '2023-10-28 16:23:50', '1', '2023-10-28 16:23:50', b'0');
INSERT INTO `system_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2, '转账进行中', '10', 'pay_transfer_status', 0, 'info', '', '', '1', '2023-10-28 16:23:12', '1', '2023-10-28 16:23:12', b'0');
INSERT INTO `system_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1, '等待转账', '0', 'pay_transfer_status', 0, 'default', '', '', '1', '2023-10-28 16:21:43', '1', '2023-10-28 16:23:22', b'0');

-- 转账订单菜单脚本

INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '转账订单', '', 2, 3, 1117,
           'transfer', 'ep:credit-card', 'pay/transfer/index', 0, 'PayTransfer'
       );

-- 转账通知脚本

ALTER TABLE `pay_app`
    ADD COLUMN `transfer_notify_url` varchar(1024) NOT NULL COMMENT '转账结果的回调地址' AFTER `refund_notify_url`;
ALTER TABLE  `pay_notify_task`
    MODIFY COLUMN `merchant_order_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '商户订单编号' AFTER `status`,
    ADD COLUMN `merchant_transfer_id` varchar(64) COMMENT '商户转账单编号' AFTER `merchant_order_id`;