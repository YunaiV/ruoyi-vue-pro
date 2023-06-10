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

 Date: 10/06/2023 20:13:57
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for member_point_config
-- ----------------------------
DROP TABLE IF EXISTS `member_point_config`;
CREATE TABLE `member_point_config` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `trade_deduct_enable` int DEFAULT NULL COMMENT '1 开启积分抵扣\n0 关闭积分抵扣',
  `trade_deduct_unit_price` decimal(10,2) DEFAULT NULL COMMENT '积分抵扣，抵扣最低为分 以0.01表示 1积分抵扣0.01元(单位：元)',
  `trade_deduct_max_price` bigint DEFAULT NULL COMMENT '积分抵扣最大值',
  `trade_give_point` bigint DEFAULT NULL COMMENT '1元赠送多少分',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '变更时间',
  `tenant_id` varchar(255) DEFAULT NULL COMMENT '租户id',
  `deleted` int DEFAULT '0' COMMENT '是否被删除 0 未删除 1已删除',
  `creator` varchar(255) DEFAULT NULL COMMENT '创建人',
  `updater` varchar(255) DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分设置';

-- ----------------------------
-- Records of member_point_config
-- ----------------------------
BEGIN;
INSERT INTO `member_point_config` (`id`, `trade_deduct_enable`, `trade_deduct_unit_price`, `trade_deduct_max_price`, `trade_give_point`, `create_time`, `update_time`, `tenant_id`, `deleted`, `creator`, `updater`) VALUES (1, 1, 0.01, 10000, 1, '2023-06-10 10:57:22', '2023-06-10 03:06:58', '1', 1, '1', '1');
INSERT INTO `member_point_config` (`id`, `trade_deduct_enable`, `trade_deduct_unit_price`, `trade_deduct_max_price`, `trade_give_point`, `create_time`, `update_time`, `tenant_id`, `deleted`, `creator`, `updater`) VALUES (2, 1, 0.01, 10000, 10, '2023-06-10 11:07:12', '2023-06-10 11:07:12', '1', 0, '1', '1');
INSERT INTO `member_point_config` (`id`, `trade_deduct_enable`, `trade_deduct_unit_price`, `trade_deduct_max_price`, `trade_give_point`, `create_time`, `update_time`, `tenant_id`, `deleted`, `creator`, `updater`) VALUES (3, 1, 12.00, 12, 12, '2023-06-10 16:09:26', '2023-06-10 08:10:53', '1', 1, '1', '1');
COMMIT;

-- ----------------------------
-- Table structure for member_point_record
-- ----------------------------
DROP TABLE IF EXISTS `member_point_record`;
CREATE TABLE `member_point_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `biz_id` varchar(255) DEFAULT NULL COMMENT '业务编码',
  `biz_type` varchar(255) DEFAULT NULL COMMENT '业务类型',
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '1增加 0扣减',
  `title` varchar(255) DEFAULT NULL COMMENT '积分标题',
  `description` varchar(5000) DEFAULT NULL COMMENT '积分描述',
  `point` int DEFAULT NULL COMMENT '积分',
  `total_point` int NOT NULL COMMENT '变动后的积分',
  `status` int DEFAULT NULL COMMENT '状态：1-订单创建，2-冻结期，3-完成，4-失效（订单退款）\n',
  `user_id` int DEFAULT NULL COMMENT '用户id',
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户积分记录';

-- ----------------------------
-- Records of member_point_record
-- ----------------------------
BEGIN;
INSERT INTO `member_point_record` (`id`, `biz_id`, `biz_type`, `type`, `title`, `description`, `point`, `total_point`, `status`, `user_id`, `freezing_time`, `thawing_time`, `create_time`, `tenant_id`, `deleted`, `creator`, `updater`, `update_time`) VALUES (1, '1', '1', '1', '12', NULL, 212, 12, 1, 12, '2023-06-13 00:00:00', '2023-06-20 00:00:00', '2023-06-10 12:38:48', '1', 1, '1', '1', '2023-06-10 04:42:24');
INSERT INTO `member_point_record` (`id`, `biz_id`, `biz_type`, `type`, `title`, `description`, `point`, `total_point`, `status`, `user_id`, `freezing_time`, `thawing_time`, `create_time`, `tenant_id`, `deleted`, `creator`, `updater`, `update_time`) VALUES (2, '12', '1', '0', NULL, NULL, 1212, 12, 2, 12, '2023-06-28 00:00:00', NULL, '2023-06-10 12:42:48', '1', 0, '1', '1', '2023-06-10 12:43:04');
INSERT INTO `member_point_record` (`id`, `biz_id`, `biz_type`, `type`, `title`, `description`, `point`, `total_point`, `status`, `user_id`, `freezing_time`, `thawing_time`, `create_time`, `tenant_id`, `deleted`, `creator`, `updater`, `update_time`) VALUES (3, '12', '1', '1', '12', NULL, 12, 12, 1, 12, '2023-06-27 00:00:00', '2023-06-23 00:00:00', '2023-06-10 20:06:48', '1', 0, '1', '1', '2023-06-10 20:06:48');
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
  `deleted` int DEFAULT '0' COMMENT '是否删除',
  `creator` varchar(255) DEFAULT NULL COMMENT '创建人',
  `updater` varchar(255) DEFAULT NULL COMMENT '变更人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分签到规则';

-- ----------------------------
-- Records of member_sign_in_config
-- ----------------------------
BEGIN;
INSERT INTO `member_sign_in_config` (`id`, `day`, `point`, `create_time`, `update_time`, `tenant_id`, `deleted`, `creator`, `updater`) VALUES (1, 1, 10, '2023-06-10 11:34:43', '2023-06-10 11:34:43', '1', 0, '1', '1');
INSERT INTO `member_sign_in_config` (`id`, `day`, `point`, `create_time`, `update_time`, `tenant_id`, `deleted`, `creator`, `updater`) VALUES (2, 2, 20, '2023-06-10 11:34:59', '2023-06-10 03:55:35', '1', 1, '1', '1');
INSERT INTO `member_sign_in_config` (`id`, `day`, `point`, `create_time`, `update_time`, `tenant_id`, `deleted`, `creator`, `updater`) VALUES (3, 7, 1001, '2023-06-10 17:47:45', '2023-06-10 19:54:37', '1', 0, '1', '1');
INSERT INTO `member_sign_in_config` (`id`, `day`, `point`, `create_time`, `update_time`, `tenant_id`, `deleted`, `creator`, `updater`) VALUES (4, 6, 12121, '2023-06-10 17:47:55', '2023-06-10 19:48:37', '1', 0, '1', '1');
INSERT INTO `member_sign_in_config` (`id`, `day`, `point`, `create_time`, `update_time`, `tenant_id`, `deleted`, `creator`, `updater`) VALUES (5, 2, 12, '2023-06-10 19:54:52', '2023-06-10 19:54:52', '1', 0, '1', '1');
COMMIT;

-- ----------------------------
-- Table structure for member_sign_in_record
-- ----------------------------
DROP TABLE IF EXISTS `member_sign_in_record`;
CREATE TABLE `member_sign_in_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '签到自增id',
  `user_id` int DEFAULT NULL COMMENT '签到用户',
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
INSERT INTO `member_sign_in_record` (`id`, `user_id`, `day`, `point`, `create_time`, `update_time`, `tenant_id`, `deleted`, `creator`, `updater`) VALUES (1, 121, 1, 123, '2023-06-10 12:58:18', '2023-06-10 04:59:00', '1', 1, '1', '1');
INSERT INTO `member_sign_in_record` (`id`, `user_id`, `day`, `point`, `create_time`, `update_time`, `tenant_id`, `deleted`, `creator`, `updater`) VALUES (2, 12, 12, 12, '2023-06-10 19:56:39', '2023-06-10 11:56:45', '1', 1, '1', '1');
INSERT INTO `member_sign_in_record` (`id`, `user_id`, `day`, `point`, `create_time`, `update_time`, `tenant_id`, `deleted`, `creator`, `updater`) VALUES (3, 12, 12, 1212, '2023-06-10 20:01:17', '2023-06-10 12:01:23', '1', 1, '1', '1');
INSERT INTO `member_sign_in_record` (`id`, `user_id`, `day`, `point`, `create_time`, `update_time`, `tenant_id`, `deleted`, `creator`, `updater`) VALUES (4, 12, 12, 1212, '2023-06-10 20:01:27', '2023-06-10 20:01:27', '1', 0, '1', '1');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;


INSERT INTO `mall`.`system_dict_type` ( `name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `deleted_time`) VALUES ( '积分业务类型', 'point_biz_type', 0, '', '1', '2023-06-10 12:15:00', '1', '2023-06-10 04:25:07', b'0', '1970-01-01 00:00:00');
INSERT INTO `mall`.`system_dict_type` ( `name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `deleted_time`) VALUES ( '积分订单状态', 'point_status', 0, '', '1', '2023-06-10 12:16:27', '1', '2023-06-10 12:16:27', b'0', '1970-01-01 00:00:00');

INSERT INTO `mall`.`system_dict_data` ( `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES ( 1, '购物', '1', 'point_biz_type', 0, '', '', '', '1', '2023-06-10 12:15:27', '1', '2023-06-10 04:25:15', b'0');
INSERT INTO `mall`.`system_dict_data` ( `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES ( 2, '签到', '2', 'point_biz_type', 0, '', '', '', '1', '2023-06-10 12:15:48', '1', '2023-06-10 04:25:18', b'0');
INSERT INTO `mall`.`system_dict_data` ( `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES ( 1, '订单创建', '1', 'point_status', 0, '', '', '', '1', '2023-06-10 12:16:42', '1', '2023-06-10 12:16:42', b'0');
INSERT INTO `mall`.`system_dict_data` ( `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES ( 2, '冻结期', '2', 'point_status', 0, '', '', '', '1', '2023-06-10 12:16:58', '1', '2023-06-10 12:16:58', b'0');
INSERT INTO `mall`.`system_dict_data` ( `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES ( 3, '完成', '3', 'point_status', 0, '', '', '', '1', '2023-06-10 12:17:07', '1', '2023-06-10 12:17:07', b'0');
INSERT INTO `mall`.`system_dict_data` ( `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES ( 4, '失效(订单退款)', '4', 'point_status', 0, '', '', '', '1', '2023-06-10 12:17:21', '1', '2023-06-10 12:17:21', b'0');
