/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1
 Source Server Type    : MySQL
 Source Server Version : 80026
 Source Host           : localhost:3306
 Source Schema         : ruoyi-vue-pro

 Target Server Type    : MySQL
 Target Server Version : 80026
 File Encoding         : 65001

 Date: 23/10/2021 17:46:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for pay_app
-- ----------------------------
DROP TABLE IF EXISTS `pay_app`;
CREATE TABLE `pay_app` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '应用编号',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '应用名',
  `status` tinyint NOT NULL COMMENT '开启状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `pay_notify_url` varchar(1024) NOT NULL COMMENT '支付结果的回调地址',
  `notify_notify_url` varchar(1024) NOT NULL COMMENT '退款结果的回调地址',
  `merchant_id` bigint NOT NULL COMMENT '商户编号',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付应用信息';

-- ----------------------------
-- Records of pay_app
-- ----------------------------
BEGIN;
INSERT INTO `pay_app` VALUES (6, '芋道', 0, '我是一个公众号', 'http://127.0.0.1', 'http://127.0.0.1', 1, '', '2021-10-23 08:49:25', '', '2021-10-23 08:49:25', b'0');
COMMIT;

-- ----------------------------
-- Table structure for pay_channel
-- ----------------------------
DROP TABLE IF EXISTS `pay_channel`;
CREATE TABLE `pay_channel` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商户编号',
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '渠道编码',
  `status` tinyint NOT NULL COMMENT '开启状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `fee_rate` double NOT NULL DEFAULT '0' COMMENT '渠道费率，单位：百分比',
  `merchant_id` bigint NOT NULL COMMENT '商户编号',
  `app_id` bigint NOT NULL COMMENT '应用编号',
  `config` varchar(4096) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付渠道配置',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付渠道\n';

-- ----------------------------
-- Records of pay_channel
-- ----------------------------
BEGIN;
INSERT INTO `pay_channel` VALUES (9, 'wx_pub', 0, NULL, 1, 1, 6, '{\"@class\":\"cn.iocoder.yudao.framework.pay.core.client.impl.wx.WXPayClientConfig\",\"appId\":\"wx041349c6f39b268b\",\"mchId\":\"1545083881\",\"apiVersion\":\"v2\",\"mchKey\":\"0alL64UDQdlCwiKZ73ib7ypaIjMns06p\",\"privateKeyContent\":\"-----BEGIN PRIVATE KEY-----\\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC5q2hYE3loOQoH\\nl/2kh/epuj17W8VpV5vBl7ysJWAbBXux6mlq4gKTHD0QUQdiKtDEUm/bKC9Bi6VU\\nuklM5Y8oCaCbhjklHRbET8jsgd9phSNGviHclYRLsQRO8oXnN89kN0y7DYKm0hYd\\nmaiS12Z3v8VaImSTr4HVeHlC/z3S6mdwSr263stKt931YTcbTj/QFH7znsv9Na0u\\nX6LaMBEEAsJctWdm8Ndrd1tGh9Fzf0DA5VRXsJR3kkWspy+IwiDTPV/FDKOU9NJC\\nSxMmDePerTfkoZ2s1rltqBK0ykDJrXtxR+hTzEsKZ/KpNi8tyYpfNZsviHIlUsLP\\nFJ5UvUhpAgMBAAECggEAd90NltazqTIxpGdeCwrwOzWNnYbIclJprlhMKIJUgf1P\\nNrPTbHoOGXTAgzkcYCat8iAaMEzH/TOu/3zn92m3uqxEcEL9v1UBLqknWHAbkB6w\\ngGocqDAqYUcdNe5hvbyM+fCta5C0SQgV2PQrHOlMMICwYpkTfzhtxCdreXIYMoGg\\nJEIRkZWgrm/N7LTtNgizznuUjy6OURWjXaWKPcs3b3j6G1gLj9Vp++z4y0u51nqM\\n4R6fcvl8M6BjlcC8zo6DbOvCW8cXtuXsnru+2TPrUnsGeybJok4fEQsfW1BvpvPo\\nief38rYJn4OWxIrHcpWrhNtXtgRPeiMGFfIsEQDmVQKBgQDzXK6Nn3Nr3TFfGVTy\\n8QYrzOuY2NqzH8nnsLL6qn3HoKxTv+PcFKOTPsi6f4hIYCzBP0esRrPv0ffMu9oQ\\nJvFtCJvMmcKGtp0Q5hcj0y/XkbC3AWuahJtBv8lhKXVnQXSL0j3+ombljw4/8yN0\\nAzgBz+j/skQQgZ3sN5h+DHGtgwKBgQDDT784/2pd4m86c/uBmrwYfqu6MJo0eHJh\\n1XPtE+u8pOHyNTFk77rKobKDqN5VlrF0uEmBc/08LKhyxJ3vh/zAbcmqT1Mq778y\\nAKKUtVmvcaVDrvSQHsnhj0zt4SHGmmU34U2b9hV+nocq5VszX6/jp//HJi9bs3We\\ndAzfFCmaowKBgC1MmDVGc+6lCraf+X8LPFHU4Bnga70h8qxM6NPd/nG1R76DHn/t\\n25DiA+0rJgwK0unZxJadxoqic9TJNssA5Lmd+5o3GM2Imm311mLVwbcHqHQ4MHZf\\nrqKrd2m9lNv2hCIurVmDk1Gxsj5XHMdQfhFgSQengCHubp30r07vNA3PAoGAUEAE\\nIjdQTSMs8KeXP7mEb8wcY3R05/pVhT1fVJpK0kgtTofss7yM05V88/v+3sv8Pik6\\niqZN9tuimwWOn00Q3UA/DGtrkMjRlooMQ24AW8YmUZkhg9YivTtUMKnAZwopbLx2\\nVw7V5iDdCRMUVheK/c+ZmQpnixZBzcmBQGfYcGECgYBjEq3Mem+Aw6pXOu6+0FwH\\n9y6Xi4HhBkq0OOZZuXFtWVry7YrD3pBgzWVAZJqJCkyPKKZzCzwdbFd3u0lYBs35\\nzYgx7ug4hR+wfI980a3vxjcWGOqnOUUnUJ7ucIa+KDgnYV/bBy4jqpVreXmWAJXl\\nfyjG3eLWBrtrsI9YX6zeAA==\\n-----END PRIVATE KEY-----\\n\",\"privateCertContent\":\"-----BEGIN CERTIFICATE-----\\nMIID6TCCAtGgAwIBAgIUNkEHq6aQcF80NSYqWS58ybsJzI4wDQYJKoZIhvcNAQEL\\nBQAwXjELMAkGA1UEBhMCQ04xEzARBgNVBAoTClRlbnBheS5jb20xHTAbBgNVBAsT\\nFFRlbnBheS5jb20gQ0EgQ2VudGVyMRswGQYDVQQDExJUZW5wYXkuY29tIFJvb3Qg\\nQ0EwHhcNMjExMDIxMDU0NTQxWhcNMjYxMDIwMDU0NTQxWjB7MRMwEQYDVQQDDAox\\nNTQ1MDgzODgxMRswGQYDVQQKDBLlvq7kv6HllYbmiLfns7vnu58xJzAlBgNVBAsM\\nHuWOhuWfjuWMuuWkp+adjuWwp+aXpeeUqOWTgeW6lzELMAkGA1UEBgwCQ04xETAP\\nBgNVBAcMCFNoZW5aaGVuMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA\\nuatoWBN5aDkKB5f9pIf3qbo9e1vFaVebwZe8rCVgGwV7seppauICkxw9EFEHYirQ\\nxFJv2ygvQYulVLpJTOWPKAmgm4Y5JR0WxE/I7IHfaYUjRr4h3JWES7EETvKF5zfP\\nZDdMuw2CptIWHZmoktdmd7/FWiJkk6+B1Xh5Qv890upncEq9ut7LSrfd9WE3G04/\\n0BR+857L/TWtLl+i2jARBALCXLVnZvDXa3dbRofRc39AwOVUV7CUd5JFrKcviMIg\\n0z1fxQyjlPTSQksTJg3j3q035KGdrNa5bagStMpAya17cUfoU8xLCmfyqTYvLcmK\\nXzWbL4hyJVLCzxSeVL1IaQIDAQABo4GBMH8wCQYDVR0TBAIwADALBgNVHQ8EBAMC\\nBPAwZQYDVR0fBF4wXDBaoFigVoZUaHR0cDovL2V2Y2EuaXRydXMuY29tLmNuL3B1\\nYmxpYy9pdHJ1c2NybD9DQT0xQkQ0MjIwRTUwREJDMDRCMDZBRDM5NzU0OTg0NkMw\\nMUMzRThFQkQyMA0GCSqGSIb3DQEBCwUAA4IBAQBe7XgncAY/1PLbCsnMsYt11k3V\\n2cdNZ+yuCxhlOEKk3nHE6WCTL6zL0qWlTKKpnw1rE/+4OS76Tg72wWXcHfHDAOgt\\n9icp62cKx1WO3QweeZpSvLDmtdLgKKrqeIWh+rL8+ZhuAOxSkaRwcsMTWDaLeDOi\\n0pGeqvfG8WNhPxkkaSI8xbiTK641Yg9WT/Q4yfHS7Q6wg1dj9YQdo0dvVB0S2Nir\\nX9IK6PUaHDnQeFKDmKgLkDGLaKaiijEvC91wMEE6qB8b0eNhciaxq2YhGHcFmSRP\\nWUyc5CmBadt7wIOH5Z3bfuwWGxqxKjNw/baM/d+nk7hlDr01YL9c0g16B9MW\\n-----END CERTIFICATE-----\\n\",\"apiV3Key\":\"joerVi8y5DJ3o4ttA0o1uH47Xz1u2Ase\"}', NULL, '2021-10-23 17:12:10', NULL, '2021-10-23 17:12:10', b'0');
COMMIT;

-- ----------------------------
-- Table structure for pay_merchant
-- ----------------------------
DROP TABLE IF EXISTS `pay_merchant`;
CREATE TABLE `pay_merchant` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商户编号',
  `no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商户号',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商户全称',
  `short_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商户简称',
  `status` tinyint NOT NULL COMMENT '开启状态',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付商户信息';

-- ----------------------------
-- Records of pay_merchant
-- ----------------------------
BEGIN;
INSERT INTO `pay_merchant` VALUES (1, 'M233666999', '芋道源码', '芋艿', 0, '我是备注', '', '2021-10-23 08:31:14', '', '2021-10-23 08:44:04', b'0');
COMMIT;

-- ----------------------------
-- Table structure for pay_order
-- ----------------------------
DROP TABLE IF EXISTS `pay_order`;
CREATE TABLE `pay_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付订单编号',
  `merchant_id` bigint NOT NULL COMMENT '商户编号',
  `app_id` bigint NOT NULL COMMENT '应用编号',
  `channel_id` bigint NOT NULL COMMENT '渠道编号',
  `channel_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '渠道编码',
  `merchant_order_id` varchar(64) NOT NULL COMMENT '商户订单编号',
  `subject` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品标题',
  `body` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品描述',
  `amount` bigint NOT NULL COMMENT '支付金额，单位：分',
  `channel_fee_rate` double NOT NULL DEFAULT '0' COMMENT '渠道手续费，单位：百分比',
  `channel_fee_amount` bigint NOT NULL DEFAULT '0' COMMENT '渠道手续金额，单位：分',
  `status` tinyint NOT NULL COMMENT '支付状态',
  `notify_status` tinyint NOT NULL COMMENT '通知商户支付结果的回调状态',
  `user_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户 IP',
  `expire_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '订单失效时间',
  `success_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '订单支付成功时间',
  `success_extension_id` bigint DEFAULT NULL COMMENT '支付成功的订单拓展单编号',
  `channel_extras` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付渠道的额外参数',
  `notify_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '异步通知地址',
  `refund_status` tinyint NOT NULL COMMENT '退款状态',
  `refund_times` tinyint NOT NULL COMMENT '退款次数',
  `refund_amount` bigint NOT NULL COMMENT '退款总金额，单位：分',
  `channel_user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '渠道用户编号',
  `channel_order_no` varchar(64) DEFAULT NULL COMMENT '渠道订单号',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付订单\n';

-- ----------------------------
-- Records of pay_order
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for pay_order_extension
-- ----------------------------
DROP TABLE IF EXISTS `pay_order_extension`;
CREATE TABLE `pay_order_extension` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '支付订单编号',
  `no` varchar(64) NOT NULL COMMENT '支付订单号',
  `order_id` bigint NOT NULL COMMENT '支付订单编号',
  `channel_id` bigint NOT NULL COMMENT '渠道编号',
  `channel_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '渠道编码',
  `channel_callback_data` varchar(1024) NOT NULL COMMENT '支付渠道异步通知的内容',
  `user_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户 IP',
  `status` tinyint NOT NULL COMMENT '支付状态',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付订单\n';

-- ----------------------------
-- Records of pay_order_extension
-- ----------------------------
BEGIN;
INSERT INTO `pay_order_extension` VALUES (9, '', 1, 0, '', '2021-10-23 09:27:37', '', 0, NULL, '2021-10-23 17:12:10', NULL, '2021-10-23 17:12:10', b'0');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
