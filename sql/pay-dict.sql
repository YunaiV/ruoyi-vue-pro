
-- 商户状态 status
INSERT INTO `sys_dict_type` ( `name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES ( '商户状态', 'pay_merchant_status', 0, '商户的启用于停用状态', '1', '2021-11-03 11:29:04', '1', '2021-11-03 11:29:04', b'0');

INSERT INTO `sys_dict_data` ( `sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES ( 1, '启用', '0', 'pay_merchant_status', 0, '商户启用', '1', '2021-11-03 11:30:52', '1', '2021-11-03 11:31:15', b'0');
INSERT INTO `sys_dict_data` ( `sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES ( 2, '停用', '1', 'pay_merchant_status', 0, '商户停用', '1', '2021-11-03 11:31:05', '1', '2021-11-03 11:31:05', b'0');

-- 支付应用状态 status
INSERT INTO `sys_dict_type` ( `name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES ('支付应用状态', 'pay_app_status', 0, '支付应用的启停状态', '1', '2021-11-06 19:41:50', '1', '2021-11-06 19:41:50', b'0');

INSERT INTO `sys_dict_data` ( `sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES ( 1, '开启', '0', 'pay_app_status', 0, NULL, '1', '2021-11-06 19:42:10', '1', '2021-11-06 19:42:10', b'0');
INSERT INTO `sys_dict_data` ( `sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES ( 2, '关闭', '1', 'pay_app_status', 0, NULL, '1', '2021-11-06 19:42:17', '1', '2021-11-06 19:42:17', b'0');

-- 支付渠道状态 status
INSERT INTO `sys_dict_type` ( `name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES ( '支付渠道状态', 'pay_channel_status', 0, '支付渠道的启停状态', '1', '2021-11-08 16:47:21', '1', '2021-11-08 16:47:21', b'0');

INSERT INTO `sys_dict_data` ( `sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES ( 1, '开启', '0', 'pay_channel_status', 0, '开启', '1', '2021-11-08 16:47:45', '1', '2021-11-08 16:47:45', b'0');
INSERT INTO `sys_dict_data` ( `sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES ( 2, '关闭', '1', 'pay_channel_status', 0, '关闭', '1', '2021-11-08 16:47:52', '1', '2021-11-08 16:47:52', b'0');

-- 微信渠道版本控制
INSERT INTO `sys_dict_type` (`name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES ('支付渠道微信版本', 'pay_channel_wechat_version', 0, '支付渠道微信版本', '1', '2021-11-08 17:00:26', '1', '2021-11-08 17:00:26', b'0');

INSERT INTO `sys_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1, 'v2', 'v2', 'pay_channel_wechat_version', 0, 'v2版本', '1', '2021-11-08 17:00:58', '1', '2021-11-08 17:00:58', b'0');
INSERT INTO `sys_dict_data` (`sort`, `label`, `value`, `dict_type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2, 'v3', 'v3', 'pay_channel_wechat_version', 0, 'v3版本', '1', '2021-11-08 17:01:07', '1', '2021-11-08 17:01:07', b'0');
