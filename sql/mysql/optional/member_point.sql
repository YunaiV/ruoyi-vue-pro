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

 Date: 28/06/2023 22:40:52
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
                                       `trade_give_point` bigint DEFAULT NULL COMMENT '1元赠送多少分',
                                       `creator` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
                                       `create_time` datetime DEFAULT NULL COMMENT '创建时间',
                                       `updater` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
                                       `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '变更时间',
                                       `tenant_id` varchar(255) DEFAULT NULL COMMENT '租户id',
                                       `deleted` bit(1) DEFAULT b'0' COMMENT '是否被删除 0 未删除 1已删除',
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员积分配置表';

-- ----------------------------
-- Records of member_point_config
-- ----------------------------
BEGIN;
INSERT INTO `member_point_config` (`id`, `trade_deduct_enable`, `trade_deduct_unit_price`, `trade_deduct_max_price`, `trade_give_point`, `creator`, `create_time`, `updater`, `update_time`, `tenant_id`, `deleted`) VALUES (1, b'1', 0, 10000, 1, '1', '2023-06-10 10:57:22', '1', '2023-06-10 03:06:58', '1', b'1');
INSERT INTO `member_point_config` (`id`, `trade_deduct_enable`, `trade_deduct_unit_price`, `trade_deduct_max_price`, `trade_give_point`, `creator`, `create_time`, `updater`, `update_time`, `tenant_id`, `deleted`) VALUES (2, b'1', 32, 10003, 1212, '1', '2023-06-10 11:07:12', '1', '2023-06-28 21:50:34', '1', b'0');
INSERT INTO `member_point_config` (`id`, `trade_deduct_enable`, `trade_deduct_unit_price`, `trade_deduct_max_price`, `trade_give_point`, `creator`, `create_time`, `updater`, `update_time`, `tenant_id`, `deleted`) VALUES (3, b'1', 12, 12, 12, '1', '2023-06-10 16:09:26', '1', '2023-06-10 08:10:53', '1', b'1');
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


INSERT INTO `system_dict_type` (`id`, `name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `deleted_time`) VALUES (170, '积分业务类型', 'member_point_biz_type', 0, '', '1', '2023-06-10 12:15:00', '1', '2023-06-28 13:48:20', b'0', '1970-01-01 00:00:00');
INSERT INTO `system_dict_type` (`id`, `name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `deleted_time`) VALUES (171, '积分订单状态', 'member_point_status', 0, '', '1', '2023-06-10 12:16:27', '1', '2023-06-28 13:48:17', b'0', '1970-01-01 00:00:00');
INSERT INTO `system_dict_type` (`id`, `name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `deleted_time`) VALUES (169, '是否抵扣积分', 'trade_deduct_enable', 0, NULL, '1', '2023-06-10 00:34:12', '1', '2023-06-10 04:14:20', b'1', '2023-06-10 12:14:20');

INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1235, 1, '购物', '1', 'member_point_biz_type', 0, '', '', '', '1', '2023-06-10 12:15:27', '1', '2023-06-28 13:48:28', b'0');
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1236, 2, '签到', '2', 'member_point_biz_type', 0, '', '', '', '1', '2023-06-10 12:15:48', '1', '2023-06-28 13:48:31', b'0');
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1237, 1, '订单创建', '1', 'member_point_status', 0, '', '', '', '1', '2023-06-10 12:16:42', '1', '2023-06-28 13:48:34', b'0');
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1238, 2, '冻结期', '2', 'member_point_status', 0, '', '', '', '1', '2023-06-10 12:16:58', '1', '2023-06-28 13:48:36', b'0');
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1239, 3, '完成', '3', 'member_point_status', 0, '', '', '', '1', '2023-06-10 12:17:07', '1', '2023-06-28 13:48:38', b'0');
INSERT INTO `system_dict_data` (`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1240, 4, '失效(订单退款)', '4', 'member_point_status', 0, '', '', '', '1', '2023-06-10 12:17:21', '1', '2023-06-28 13:48:42', b'0');


INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2162, '会员中心', '', 1, 55, 0, '/member', 'date-range', NULL, NULL, 0, b'1', b'1', b'1', '1', '2023-06-10 00:42:03', '1', '2023-06-28 21:52:34', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2175, '积分配置', '', 2, 0, 2199, 'config', '', 'member/point/config/index', 'PointConfig', 0, b'1', b'1', b'1', '', '2023-06-10 02:07:44', '1', '2023-06-27 22:50:59', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2176, '积分设置查询', 'point:config:query', 3, 1, 2175, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2023-06-10 02:07:44', '', '2023-06-10 02:07:44', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2177, '积分设置创建', 'point:config:save', 3, 2, 2175, '', '', '', '', 0, b'1', b'1', b'1', '', '2023-06-10 02:07:44', '1', '2023-06-27 20:32:31', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2181, '签到配置', '', 2, 2, 2200, 'sign-in-config', '', 'member/signin/config/index', 'SignInConfig', 0, b'1', b'1', b'1', '', '2023-06-10 03:26:12', '1', '2023-06-27 22:51:45', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2182, '积分签到规则查询', 'point:sign-in-config:query', 3, 1, 2181, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2023-06-10 03:26:12', '', '2023-06-10 03:26:12', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2183, '积分签到规则创建', 'point:sign-in-config:create', 3, 2, 2181, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2023-06-10 03:26:12', '', '2023-06-10 03:26:12', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2184, '积分签到规则更新', 'point:sign-in-config:update', 3, 3, 2181, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2023-06-10 03:26:12', '', '2023-06-10 03:26:12', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2185, '积分签到规则删除', 'point:sign-in-config:delete', 3, 4, 2181, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2023-06-10 03:26:12', '', '2023-06-10 03:26:12', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2186, '积分签到规则导出', 'point:sign-in-config:export', 3, 5, 2181, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2023-06-10 03:26:12', '', '2023-06-10 03:26:12', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2187, '积分记录', '', 2, 1, 2199, 'record', '', 'member/point/record/index', 'PointRecord', 0, b'1', b'1', b'1', '', '2023-06-10 04:18:50', '1', '2023-06-27 22:51:07', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2188, '用户积分记录查询', 'point:record:query', 3, 1, 2187, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2023-06-10 04:18:50', '', '2023-06-10 04:18:50', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2192, '用户积分记录导出', 'point:record:export', 3, 5, 2187, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2023-06-10 04:18:50', '', '2023-06-10 04:18:50', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2193, '签到记录', '', 2, 3, 2200, 'sign-in-record', '', 'member/signin/record/index', 'SignInRecord', 0, b'1', b'1', b'1', '', '2023-06-10 04:48:22', '1', '2023-06-27 22:51:54', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2194, '用户签到积分查询', 'point:sign-in-record:query', 3, 1, 2193, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2023-06-10 04:48:22', '', '2023-06-10 04:48:22', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2197, '用户签到积分删除', 'point:sign-in-record:delete', 3, 4, 2193, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2023-06-10 04:48:22', '', '2023-06-10 04:48:22', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2198, '用户签到积分导出', 'point:sign-in-record:export', 3, 5, 2193, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2023-06-10 04:48:22', '', '2023-06-10 04:48:22', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2199, '会员积分', '', 1, 1, 2162, 'point', '', '', '', 0, b'1', b'1', b'1', '1', '2023-06-27 22:48:51', '1', '2023-06-27 22:48:51', b'0');
INSERT INTO `system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2200, '会员签到', '', 1, 2, 2162, 'signin', '', '', '', 0, b'1', b'1', b'1', '1', '2023-06-27 22:49:53', '1', '2023-06-27 22:49:53', b'0');

SET FOREIGN_KEY_CHECKS = 1;
