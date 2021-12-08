-- 商户状态 status
INSERT INTO `sys_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`,
                             `deleted`)
VALUES ('商户状态', 'pay_merchant_status', 0, '商户的启用于停用状态', '1', '2021-11-03 11:29:04', '1', '2021-11-03 11:29:04', b'0');

INSERT INTO `sys_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                             `updater`, `update_time`, `deleted`)
VALUES (1, '启用', '0', 'pay_merchant_status', 0, '商户启用', '1', '2021-11-03 11:30:52', '1', '2021-11-03 11:31:15', b'0');
INSERT INTO `sys_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                             `updater`, `update_time`, `deleted`)
VALUES (2, '停用', '1', 'pay_merchant_status', 0, '商户停用', '1', '2021-11-03 11:31:05', '1', '2021-11-03 11:31:05', b'0');

-- 支付应用状态 status
INSERT INTO `sys_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`,
                             `deleted`)
VALUES ('支付应用状态', 'pay_app_status', 0, '支付应用的启停状态', '1', '2021-11-06 19:41:50', '1', '2021-11-06 19:41:50', b'0');

INSERT INTO `sys_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                             `updater`, `update_time`, `deleted`)
VALUES (1, '开启', '0', 'pay_app_status', 0, NULL, '1', '2021-11-06 19:42:10', '1', '2021-11-06 19:42:10', b'0');
INSERT INTO `sys_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                             `updater`, `update_time`, `deleted`)
VALUES (2, '关闭', '1', 'pay_app_status', 0, NULL, '1', '2021-11-06 19:42:17', '1', '2021-11-06 19:42:17', b'0');

-- 支付渠道状态 status
INSERT INTO `sys_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`,
                             `deleted`)
VALUES ('支付渠道状态', 'pay_channel_status', 0, '支付渠道的启停状态', '1', '2021-11-08 16:47:21', '1', '2021-11-08 16:47:21', b'0');

INSERT INTO `sys_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                             `updater`, `update_time`, `deleted`)
VALUES (1, '开启', '0', 'pay_channel_status', 0, '开启', '1', '2021-11-08 16:47:45', '1', '2021-11-08 16:47:45', b'0');
INSERT INTO `sys_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                             `updater`, `update_time`, `deleted`)
VALUES (2, '关闭', '1', 'pay_channel_status', 0, '关闭', '1', '2021-11-08 16:47:52', '1', '2021-11-08 16:47:52', b'0');

-- 微信渠道版本控制
INSERT INTO `sys_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`,
                             `deleted`)
VALUES ('支付渠道微信版本', 'pay_channel_wechat_version', 0, '支付渠道微信版本', '1', '2021-11-08 17:00:26', '1', '2021-11-08 17:00:26',
        b'0');

INSERT INTO `sys_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                             `updater`, `update_time`, `deleted`)
VALUES (1, 'v2', 'v2', 'pay_channel_wechat_version', 0, 'v2版本', '1', '2021-11-08 17:00:58', '1', '2021-11-08 17:00:58',
        b'0');
INSERT INTO `sys_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                             `updater`, `update_time`, `deleted`)
VALUES (2, 'v3', 'v3', 'pay_channel_wechat_version', 0, 'v3版本', '1', '2021-11-08 17:01:07', '1', '2021-11-08 17:01:07',
        b'0');

-- 支付渠道支付宝算法类型
INSERT INTO `sys_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`,
                             `deleted`)
VALUES ('支付渠道支付宝算法类型', 'pay_channel_alipay_sign_type', 0, '支付渠道支付宝算法类型', '1', '2021-11-18 15:39:09', '1',
        '2021-11-18 15:39:09', b'0');
INSERT INTO `sys_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                             `updater`, `update_time`, `deleted`)
VALUES (1, 'RSA2', 'RSA2', 'pay_channel_alipay_sign_type', 0, 'RSA2', '1', '2021-11-18 15:39:29', '1',
        '2021-11-18 15:39:29', b'0');


-- 支付渠道支付宝公钥类型
INSERT INTO `sys_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`,
                             `deleted`)
VALUES ('支付渠道支付宝公钥类型', 'pay_channel_alipay_mode', 0, '支付渠道支付宝公钥类型', '1', '2021-11-18 15:44:28', '1',
        '2021-11-18 15:44:28', b'0');
INSERT INTO `sys_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                             `updater`, `update_time`, `deleted`)
VALUES (1, '公钥模式', '1', 'pay_channel_alipay_mode', 0, '公钥模式：privateKey + alipayPublicKey', '1', '2021-11-18 15:45:23',
        '1', '2021-11-18 15:45:23', b'0');
INSERT INTO `sys_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                             `updater`, `update_time`, `deleted`)
VALUES (2, '证书模式', '2', 'pay_channel_alipay_mode', 0, '证书模式：appCertContent + alipayPublicCertContent + rootCertContent',
        '1', '2021-11-18 15:45:40', '1', '2021-11-18 15:45:40', b'0');


-- 支付宝网关地址
INSERT INTO `sys_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`,
                             `deleted`)
VALUES ('支付宝网关地址', 'pay_channel_alipay_server_type', 0, '支付宝网关地址', '1', '2021-11-18 16:58:55', '1',
        '2021-11-18 17:01:34', b'0');
INSERT INTO `sys_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                             `updater`, `update_time`, `deleted`)
VALUES (1, '线上', 'https://openapi.alipay.com/gateway.do', 'pay_channel_alipay_server_type', 0, '网关地址 - 线上', '1',
        '2021-11-18 16:59:32', '1', '2021-11-21 17:37:29', b'0');
INSERT INTO `sys_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                             `updater`, `update_time`, `deleted`)
VALUES (2, '沙箱', 'https://openapi.alipaydev.com/gateway.do', 'pay_channel_alipay_server_type', 0, '网关地址 - 沙箱', '1',
        '2021-11-18 16:59:48', '1', '2021-11-21 17:37:39', b'0');

-- 支付渠道编码类型
INSERT INTO `sys_dict_type`(`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`,
                            `deleted`)
VALUES ('支付渠道编码类型', 'pay_channel_code_type', 0, '支付渠道的编码', '1', '2021-12-03 10:35:08', '1', '2021-12-03 10:35:08',
        b'0');
INSERT INTO `sys_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                            `updater`, `update_time`, `deleted`)
VALUES (1, '微信 JSAPI 支付', 'wx_pub', 'pay_channel_code_type', 0, '微信 JSAPI（公众号） 支付', '1', '2021-12-03 10:40:24', '1',
        '2021-12-04 16:41:00', b'0');
INSERT INTO `sys_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                            `updater`, `update_time`, `deleted`)
VALUES (2, '微信小程序支付', 'wx_lite', 'pay_channel_code_type', 0, '微信小程序支付', '1', '2021-12-03 10:41:06', '1',
        '2021-12-03 10:41:06', b'0');
INSERT INTO `sys_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                            `updater`, `update_time`, `deleted`)
VALUES (3, '微信 App 支付', 'wx_app', 'pay_channel_code_type', 0, '微信 App 支付', '1', '2021-12-03 10:41:20', '1',
        '2021-12-03 10:41:20', b'0');
INSERT INTO `sys_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                            `updater`, `update_time`, `deleted`)
VALUES (4, '支付宝 PC 网站支付', 'alipay_pc', 'pay_channel_code_type', 0, '支付宝 PC 网站支付', '1', '2021-12-03 10:42:09', '1',
        '2021-12-03 10:42:09', b'0');
INSERT INTO `sys_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                            `updater`, `update_time`, `deleted`)
VALUES (5, '支付宝 Wap 网站支付', 'alipay_wap', 'pay_channel_code_type', 0, '支付宝 Wap 网站支付', '1', '2021-12-03 10:42:26', '1',
        '2021-12-03 10:42:26', b'0');
INSERT INTO `sys_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                            `updater`, `update_time`, `deleted`)
VALUES (6, '支付宝App 支付', 'alipay_app', 'pay_channel_code_type', 0, '支付宝App 支付', '1', '2021-12-03 10:42:55', '1',
        '2021-12-03 10:42:55', b'0');
INSERT INTO `sys_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                            `updater`, `update_time`, `deleted`)
VALUES (7, '支付宝扫码支付', 'alipay_qr', 'pay_channel_code_type', 0, '支付宝扫码支付', '1', '2021-12-03 10:43:10', '1',
        '2021-12-03 10:43:10', b'0');

-- 支付订单回调状态
INSERT INTO `sys_dict_type`(`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`,
                            `deleted`)
VALUES ('支付订单回调状态', 'pay_order_notify_status', 0, '支付订单回调状态', '1', '2021-12-03 10:53:29', '1', '2021-12-03 10:53:29',
        b'0');
INSERT INTO `sys_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                            `updater`, `update_time`, `deleted`)
VALUES (1, '通知成功', '10', 'pay_order_notify_status', 0, '通知成功', '1', '2021-12-03 11:02:41', '1', '2021-12-03 11:02:41',
        b'0');
INSERT INTO `sys_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                            `updater`, `update_time`, `deleted`)
VALUES (2, '通知失败', '20', 'pay_order_notify_status', 0, '通知失败', '1', '2021-12-03 11:02:59', '1', '2021-12-03 11:02:59',
        b'0');
INSERT INTO `sys_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                            `updater`, `update_time`, `deleted`)
VALUES (3, '未通知', '0', 'pay_order_notify_status', 0, '未通知', '1', '2021-12-03 11:03:10', '1', '2021-12-03 11:03:10',
        b'0');

-- 支付订单状态
INSERT INTO `sys_dict_type`(`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`,
                            `deleted`)
VALUES ('支付订单状态', 'pay_order_status', 0, '支付订单状态', '1', '2021-12-03 11:17:50', '1', '2021-12-03 11:17:50', b'0');
INSERT INTO `sys_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                            `updater`, `update_time`, `deleted`)
VALUES (1, '支付成功', '10', 'pay_order_status', 0, '支付成功', '1', '2021-12-03 11:18:29', '1', '2021-12-03 11:28:32', b'0');
INSERT INTO `sys_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                            `updater`, `update_time`, `deleted`)
VALUES (2, '支付关闭', '20', 'pay_order_status', 0, '支付关闭', '1', '2021-12-03 11:18:42', '1', '2021-12-03 11:28:34', b'0');
INSERT INTO `sys_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                            `updater`, `update_time`, `deleted`)
VALUES (3, '未支付', '0', 'pay_order_status', 0, '未支付', '1', '2021-12-03 11:18:18', '1', '2021-12-03 11:28:36', b'0');

-- 支付订单退款状态
INSERT INTO `sys_dict_type`(`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`,
                            `deleted`)
VALUES ('支付订单退款状态', 'pay_order_refund_status', 0, '支付订单退款状态', '1', '2021-12-03 11:27:31', '1', '2021-12-03 11:27:31',
        b'0');
INSERT INTO `sys_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                            `updater`, `update_time`, `deleted`)
VALUES (1, '未退款', '0', 'pay_order_refund_status', 0, '未退款', '1', '2021-12-03 11:30:35', '1', '2021-12-03 11:34:05',
        b'0');
INSERT INTO `sys_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                            `updater`, `update_time`, `deleted`)
VALUES (2, '部分退款', '10', 'pay_order_refund_status', 0, '部分退款', '1', '2021-12-03 11:30:44', '1', '2021-12-03 11:34:10',
        b'0');
INSERT INTO `sys_dict_data`(`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`,
                            `updater`, `update_time`, `deleted`)
VALUES (3, '全部退款', '20', 'pay_order_refund_status', 0, '全部退款', '1', '2021-12-03 11:30:52', '1', '2021-12-03 11:34:14',
        b'0');