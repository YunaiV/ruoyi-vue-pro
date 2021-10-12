/*
  支付相关表结构
 Date: 12/10/2021 14:55:36
*/
-- ----------------------------
-- Table structure for tb_trade_merchant
-- ----------------------------
DROP TABLE IF EXISTS `tb_trade_merchant`;
CREATE TABLE `tb_trade_merchant` (
                                     `id` int(11) NOT NULL COMMENT '主键',
                                     `merchant_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户号',
                                     `name` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商户名称',
                                     `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户编号',
                                     `user_name` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户姓名',
                                     `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '支付应用Id',
                                     `app_secret` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '支付应用密钥',
                                     `app_mchid` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '支付应用直连商户的商户号',
                                     `pay_way_code` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '支付方式',
                                     `pay_way_name` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付方式名称',
                                     `pay_type_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '支付类型编码',
                                     `pay_type_name` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付类型名称',
                                     `rsa_private_key` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'RSA私钥',
                                     `rsa_public_key` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'RAS公钥',
                                     `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                     `update_time` datetime DEFAULT NULL COMMENT '修改时间',
                                     PRIMARY KEY (`id`),
                                     UNIQUE KEY `uk_merchant_id` (`merchant_id`) USING BTREE COMMENT '商户号唯一索引',
                                     KEY `idx_user_id` (`user_id`) USING BTREE COMMENT '用户编号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商户信息';

-- ----------------------------
-- Table structure for tb_trade_payment
-- ----------------------------
DROP TABLE IF EXISTS `tb_trade_payment`;
CREATE TABLE `tb_trade_payment` (
                                    `id` int(11) NOT NULL COMMENT '主键',
                                    `payment_id` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付单号',
                                    `order_id` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户订单号',
                                    `status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '状态',
                                    `trade_type` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '交易类型（消费、充值）',
                                    `product_description` varchar(300) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品说明',
                                    `merchant_id` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户号',
                                    `merchant_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商户名称',
                                    `bank_ prepay_id` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '预支付单：银行/银联/微信/支付宝',
                                    `bank_transaction_id` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付订单号: 银行/银联/微信/支付宝',
                                    `pay_way_code` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付方式（通道）编号',
                                    `pay_type_code` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付类型编码',
                                    `pay_amount` bigint(20) NOT NULL COMMENT '(已成功)支付金额,单位为：分',
                                    `payer_user_id` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '付款方编号',
                                    `payer_open_id` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '付款方第三方平台认证Id',
                                    `payer_bank_type` varchar(16) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '付款方银行类型',
                                    `payer_bank_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '付款方银行名称',
                                    `receiver_bank_no` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '收款方银行账号',
                                    `receiver_bank_name` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '收款方银行名称',
                                    `receiver_bank_type` varchar(30) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '收款方银行账户类型',
                                    `notify_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '银行异步通知URL',
                                    `fund_into_type` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '资金流入类型',
                                    `refund_status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '退款状态',
                                    `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
                                    `refund_amount` bigint(20) DEFAULT NULL COMMENT '退款金额',
                                    `refund_num` int(11) DEFAULT NULL COMMENT '退款次数',
                                    `create_time` datetime DEFAULT NULL COMMENT '支付单创建时间',
                                    `update_time` datetime DEFAULT NULL COMMENT '编辑时间',
                                    `complete_time` datetime DEFAULT NULL COMMENT '支付完成时间',
                                    `cancel_time` datetime DEFAULT NULL COMMENT '订单撤销时间',
                                    `expire_time` datetime DEFAULT NULL COMMENT '支付单失效时间',
                                    `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付备注',
                                    `operator_user_id` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人用户编号',
                                    `operator_user_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '操作人名称',
                                    `operator_time` datetime DEFAULT NULL COMMENT '操作时间',
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `uk_payment_id` (`payment_id`) USING BTREE COMMENT '支付单号唯一索引',
                                    KEY `idx_order_id` (`order_id`) USING BTREE COMMENT '订单编号',
                                    KEY `idx_merchant_id` (`merchant_id`) USING BTREE COMMENT '商户号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支付单';

-- ----------------------------
-- Table structure for tb_trade_refund_record
-- ----------------------------
DROP TABLE IF EXISTS `tb_trade_refund_record`;
CREATE TABLE `tb_trade_refund_record` (
                                          `id` int(11) NOT NULL COMMENT '主键',
                                          `refund_id` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '退款单号',
                                          `payment_id` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '支付单号',
                                          `order_id` varchar(32) COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单号',
                                          `bank_transaction_id` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '银行支付订单号',
                                          `merchant_id` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '商户号',
                                          `merchant_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商户名称',
                                          `bank_refund_id` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '银行支付退款单号',
                                          `status` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '退款状态',
                                          `product_description` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品描述',
                                          `trade_type` int(11) DEFAULT NULL COMMENT '交易类型',
                                          `refund_amount` bigint(20) NOT NULL COMMENT '退款金额',
                                          `apply_user_id` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '申请退款人的id',
                                          `apply_user_name` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '申请退款人的名称',
                                          `apply_user_open_id` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '申请退款人第三方平台id',
                                          `apply_user_logo` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '申请退款人的logo',
                                          `notify_url` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '退款通知url',
                                          `refund_way_code` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '退款方式（通道）编码',
                                          `refund_way_name` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '退款方式（通道）名称',
                                          `user_received_account` varchar(128) COLLATE utf8mb4_general_ci NOT NULL COMMENT '退款入账账户',
                                          `create_time` datetime DEFAULT NULL COMMENT '创建时间/退款请求时间',
                                          `refund_time` datetime DEFAULT NULL COMMENT '成功退款时间',
                                          `complete_time` datetime DEFAULT NULL COMMENT '退款完成时间',
                                          `refund_reason` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '退款原因',
                                          `remark` varchar(3000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
                                          `bank_return_message` varchar(2000) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付平台返回的信息',
                                          PRIMARY KEY (`id`),
                                          UNIQUE KEY `uk_refund_id` (`refund_id`) USING BTREE COMMENT '退款单号唯一索引',
                                          KEY `idx_payment_id` (`payment_id`) USING BTREE COMMENT '付款单',
                                          KEY `idx_order_id` (`order_id`) USING BTREE COMMENT '订单编号'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='退款记录';