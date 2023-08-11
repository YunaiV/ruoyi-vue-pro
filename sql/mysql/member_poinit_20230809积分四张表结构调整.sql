/*
 Navicat Premium Data Transfer

 Source Server         : docer-master-root(3308)
 Source Server Type    : MySQL
 Source Server Version : 80030
 Source Host           : 10.211.55.5:3308
 Source Schema         : mall

 Target Server Type    : MySQL
 Target Server Version : 80030
 File Encoding         : 65001

 Date: 09/08/2023 22:41:36
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for member_point_config
-- ----------------------------
DROP TABLE IF EXISTS `member_point_config`;
CREATE TABLE `member_point_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `trade_deduct_enable` bit(1) DEFAULT NULL COMMENT '1 开启积分抵扣\n0 关闭积分抵扣',
  `trade_deduct_unit_price` int DEFAULT NULL COMMENT '积分抵扣(单位：分)',
  `trade_deduct_max_price` int DEFAULT NULL COMMENT '积分抵扣最大值',
  `trade_give_point` int DEFAULT NULL COMMENT '1元赠送多少分',
  `creator` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `updater` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '变更时间',
  `tenant_id` varchar(255) DEFAULT NULL COMMENT '租户id',
  `deleted` bit(1) DEFAULT b'0' COMMENT '是否被删除 0 未删除 1已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员积分配置表';

-- ----------------------------
-- Records of member_point_config
-- ----------------------------
BEGIN;
INSERT INTO `member_point_config` (`id`, `trade_deduct_enable`, `trade_deduct_unit_price`, `trade_deduct_max_price`, `trade_give_point`, `creator`, `create_time`, `updater`, `update_time`, `tenant_id`, `deleted`) VALUES (5, b'1', 12504, 1234, 1234, '1', '2023-08-09 22:27:42', '1', '2023-08-09 22:32:55', '1', b'0');
COMMIT;

-- ----------------------------
-- Table structure for member_point_record
-- ----------------------------
DROP TABLE IF EXISTS `member_point_record`;
CREATE TABLE `member_point_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `biz_id` varchar(255) DEFAULT NULL COMMENT '业务编码',
  `biz_type` varchar(255) DEFAULT NULL COMMENT '业务类型',
  `title` varchar(255) DEFAULT NULL COMMENT '积分标题',
  `description` varchar(5000) DEFAULT NULL COMMENT '积分描述',
  `point` int DEFAULT NULL COMMENT '积分',
  `total_point` int NOT NULL COMMENT '变动后的积分',
  `status` int DEFAULT NULL COMMENT '状态：1-订单创建，2-冻结期，3-完成，4-失效（订单退款）\n',
  `user_id` bigint DEFAULT NULL COMMENT '用户id',
  `freezing_time` datetime DEFAULT NULL COMMENT '冻结时间',
  `thawing_time` datetime DEFAULT NULL COMMENT '解冻时间',
  `create_time` datetime DEFAULT NULL COMMENT '发生时间',
  `tenant_id` varchar(255) DEFAULT NULL COMMENT '租户',
  `deleted` int DEFAULT '0' COMMENT '是否删除',
  `creator` varchar(255) DEFAULT NULL COMMENT '创建用户',
  `updater` varchar(255) DEFAULT NULL COMMENT '更新用户',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `index_userId` (`user_id`),
  KEY `index_title` (`title`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户积分记录';

-- ----------------------------
-- Records of member_point_record
-- ----------------------------
BEGIN;
INSERT INTO `member_point_record` (`id`, `biz_id`, `biz_type`, `title`, `description`, `point`, `total_point`, `status`, `user_id`, `freezing_time`, `thawing_time`, `create_time`, `tenant_id`, `deleted`, `creator`, `updater`, `update_time`) VALUES (1, '1', '1', '12', NULL, 212, 12, 1, 247, '2023-06-13 00:00:00', '2023-06-20 00:00:00', '2023-06-10 12:38:48', '1', 1, '1', '1', '2023-08-09 12:30:36');
INSERT INTO `member_point_record` (`id`, `biz_id`, `biz_type`, `title`, `description`, `point`, `total_point`, `status`, `user_id`, `freezing_time`, `thawing_time`, `create_time`, `tenant_id`, `deleted`, `creator`, `updater`, `update_time`) VALUES (2, '12', '1', '这是一个测试', '我是描述', 1212, 12, 2, 247, '2023-06-28 00:00:00', NULL, '2023-06-10 12:42:48', '1', 0, '1', '1', '2023-08-09 12:31:45');
INSERT INTO `member_point_record` (`id`, `biz_id`, `biz_type`, `title`, `description`, `point`, `total_point`, `status`, `user_id`, `freezing_time`, `thawing_time`, `create_time`, `tenant_id`, `deleted`, `creator`, `updater`, `update_time`) VALUES (3, '12', '1', '12', '我是一个描述', 12, 12, 1, 248, '2023-06-27 00:00:00', '2023-06-23 00:00:00', '2023-06-10 20:06:48', '1', 0, '1', '1', '2023-08-09 12:31:41');
INSERT INTO `member_point_record` (`id`, `biz_id`, `biz_type`, `title`, `description`, `point`, `total_point`, `status`, `user_id`, `freezing_time`, `thawing_time`, `create_time`, `tenant_id`, `deleted`, `creator`, `updater`, `update_time`) VALUES (4, '12', '1', '描述2', '我是一个描述', -12, 12, 1, 248, '2023-08-09 11:21:00', '2023-09-13 00:00:00', '2023-06-10 20:06:48', '1', 0, '1', '1', '2023-08-09 12:57:28');
COMMIT;

-- ----------------------------
-- Table structure for member_sign_in_config
-- ----------------------------
DROP TABLE IF EXISTS `member_sign_in_config`;
CREATE TABLE `member_sign_in_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '规则自增主键',
  `day` int DEFAULT NULL COMMENT '签到第x天',
  `point` int DEFAULT NULL COMMENT '签到天数对应分数',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '变更时间',
  `tenant_id` varchar(255) DEFAULT NULL COMMENT '租户id',
  `is_enable` tinyint(1) DEFAULT NULL COMMENT '是否启用 1启用，0未启动',
  `deleted` int DEFAULT '0' COMMENT '是否删除',
  `creator` varchar(255) DEFAULT NULL COMMENT '创建人',
  `updater` varchar(255) DEFAULT NULL COMMENT '变更人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分签到规则';

-- ----------------------------
-- Records of member_sign_in_config
-- ----------------------------
BEGIN;
INSERT INTO `member_sign_in_config` (`id`, `day`, `point`, `create_time`, `update_time`, `tenant_id`, `is_enable`, `deleted`, `creator`, `updater`) VALUES (1, 4, 10, '2023-06-10 11:34:43', '2023-08-08 15:10:03', '1', 1, 0, '1', '1');
INSERT INTO `member_sign_in_config` (`id`, `day`, `point`, `create_time`, `update_time`, `tenant_id`, `is_enable`, `deleted`, `creator`, `updater`) VALUES (2, 2, 20, '2023-06-10 11:34:59', '2023-08-08 13:39:54', '1', 1, 1, '1', '1');
INSERT INTO `member_sign_in_config` (`id`, `day`, `point`, `create_time`, `update_time`, `tenant_id`, `is_enable`, `deleted`, `creator`, `updater`) VALUES (3, 7, 1001, '2023-06-10 17:47:45', '2023-08-08 15:09:55', '1', 0, 0, '1', '1');
INSERT INTO `member_sign_in_config` (`id`, `day`, `point`, `create_time`, `update_time`, `tenant_id`, `is_enable`, `deleted`, `creator`, `updater`) VALUES (4, 6, 12121, '2023-06-10 17:47:55', '2023-08-08 15:09:47', '1', 0, 0, '1', '1');
INSERT INTO `member_sign_in_config` (`id`, `day`, `point`, `create_time`, `update_time`, `tenant_id`, `is_enable`, `deleted`, `creator`, `updater`) VALUES (5, 2, 12, '2023-06-10 19:54:52', '2023-08-08 15:10:03', '1', 1, 0, '1', '1');
COMMIT;

-- ----------------------------
-- Table structure for member_sign_in_record
-- ----------------------------
DROP TABLE IF EXISTS `member_sign_in_record`;
CREATE TABLE `member_sign_in_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '签到自增id',
  `user_id` bigint DEFAULT NULL COMMENT '签到用户',
  `day` int DEFAULT NULL COMMENT '第几天签到',
  `point` int DEFAULT NULL COMMENT '签到的分数',
  `create_time` datetime DEFAULT NULL COMMENT '签到时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '变更时间',
  `tenant_id` varchar(255) DEFAULT NULL COMMENT '租户id',
  `deleted` int DEFAULT '0' COMMENT '是否删除',
  `creator` varchar(255) DEFAULT NULL COMMENT '创建人',
  `updater` varchar(255) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户签到积分';

-- ----------------------------
-- Records of member_sign_in_record
-- ----------------------------
BEGIN;
INSERT INTO `member_sign_in_record` (`id`, `user_id`, `day`, `point`, `create_time`, `update_time`, `tenant_id`, `deleted`, `creator`, `updater`) VALUES (1, 247, 1, 123, '2023-06-10 12:58:18', '2023-08-09 08:51:31', '1', 1, '1', '1');
INSERT INTO `member_sign_in_record` (`id`, `user_id`, `day`, `point`, `create_time`, `update_time`, `tenant_id`, `deleted`, `creator`, `updater`) VALUES (2, 247, 12, 12, '2023-06-10 19:56:39', '2023-08-09 08:51:32', '1', 1, '1', '1');
INSERT INTO `member_sign_in_record` (`id`, `user_id`, `day`, `point`, `create_time`, `update_time`, `tenant_id`, `deleted`, `creator`, `updater`) VALUES (3, 1, 12, 1212, '2023-06-10 20:01:17', '2023-08-09 08:05:07', '1', 1, '1', '1');
INSERT INTO `member_sign_in_record` (`id`, `user_id`, `day`, `point`, `create_time`, `update_time`, `tenant_id`, `deleted`, `creator`, `updater`) VALUES (4, 247, 12, 1212, '2023-06-10 20:01:27', '2023-08-09 08:51:34', '1', 0, '1', '1');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
