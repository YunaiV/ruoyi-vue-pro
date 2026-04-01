/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1 MySQL
 Source Server Type    : MySQL
 Source Server Version : 80200 (8.2.0)
 Source Host           : 127.0.0.1:3306
 Source Schema         : ruoyi-vue-pro

 Target Server Type    : MySQL
 Target Server Version : 80200 (8.2.0)
 File Encoding         : 65001

 Date: 18/01/2024 19:47:09
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for member_address
-- ----------------------------
DROP TABLE IF EXISTS `member_address`;
CREATE TABLE `member_address`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收件地址编号',
  `user_id` bigint NOT NULL COMMENT '用户编号',
  `name` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '收件人名称',
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '手机号',
  `area_id` bigint NOT NULL COMMENT '地区编码',
  `detail_address` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '收件详细地址',
  `default_status` bit(1) NOT NULL COMMENT '是否默认',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_userId`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 32 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户收件地址';

-- ----------------------------
-- Records of member_address
-- ----------------------------
BEGIN;
INSERT INTO `member_address` (`id`, `user_id`, `name`, `mobile`, `area_id`, `detail_address`, `default_status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES (21, 247, 'yunai', '15601691300', 140302, '芋道源码 233 号 666 室', b'0', '1', '2022-08-01 22:46:35', '247', '2023-12-16 19:39:30', b'0', 1), (23, 247, '芋艿', '15601691300', 120103, '13232312444', b'0', '247', '2023-06-26 19:47:40', '247', '2023-12-16 20:27:18', b'0', 1), (24, 248, '芋头', '15601691234', 110101, '灌灌灌灌灌', b'1', '248', '2023-10-06 10:08:24', '248', '2023-10-06 10:08:24', b'0', 1), (25, 247, '测试1', '15601691300', 120101, '3213213444', b'1', '247', '2023-12-16 19:39:30', '247', '2023-12-16 20:33:05', b'0', 1), (28, 247, 'hhh', '15601691203', 230102, '32132132312', b'0', '247', '2023-12-16 20:33:20', '247', '2023-12-16 20:33:20', b'0', 1), (29, 283, '阿斗', '15601691388', 110101, '9999', b'0', '283', '2024-01-17 14:41:55', '283', '2024-01-17 14:41:55', b'0', 1), (30, 284, '阿巴', '15601691300', 110101, 'abc', b'0', '284', '2024-01-17 15:22:42', '284', '2024-01-17 15:22:42', b'0', 1), (31, 285, '阿巴阿巴', '15601692332', 130102, '3333', b'1', '285', '2024-01-17 15:45:00', '285', '2024-01-17 15:45:00', b'0', 1);
COMMIT;

-- ----------------------------
-- Table structure for member_config
-- ----------------------------
DROP TABLE IF EXISTS `member_config`;
CREATE TABLE `member_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `point_trade_deduct_enable` bit(1) NOT NULL COMMENT '是否开启积分抵扣',
  `point_trade_deduct_unit_price` int NOT NULL COMMENT '积分抵扣(单位：分)',
  `point_trade_deduct_max_price` int NULL DEFAULT NULL COMMENT '积分抵扣最大值',
  `point_trade_give_point` bigint NULL DEFAULT NULL COMMENT '1 元赠送多少分',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员配置表';

-- ----------------------------
-- Records of member_config
-- ----------------------------
BEGIN;
INSERT INTO `member_config` (`id`, `point_trade_deduct_enable`, `point_trade_deduct_unit_price`, `point_trade_deduct_max_price`, `point_trade_give_point`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES (5, b'1', 100, 2, 3, '1', '2023-08-20 09:54:42', '1', '2023-10-01 23:44:01', b'0', 1);
COMMIT;

-- ----------------------------
-- Table structure for member_experience_record
-- ----------------------------
DROP TABLE IF EXISTS `member_experience_record`;
CREATE TABLE `member_experience_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` bigint NOT NULL DEFAULT 0 COMMENT '用户编号',
  `biz_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '业务编号',
  `biz_type` tinyint NOT NULL DEFAULT 0 COMMENT '业务类型',
  `title` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '标题',
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '描述',
  `experience` int NOT NULL DEFAULT 0 COMMENT '经验',
  `total_experience` int NOT NULL DEFAULT 0 COMMENT '变更后的经验',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE COMMENT '会员经验记录-用户编号',
  INDEX `idx_user_biz_type`(`user_id` ASC, `biz_type` ASC) USING BTREE COMMENT '会员经验记录-用户业务类型'
) ENGINE = InnoDB AUTO_INCREMENT = 63 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员经验记录';

-- ----------------------------
-- Records of member_experience_record
-- ----------------------------
BEGIN;
INSERT INTO `member_experience_record` (`id`, `user_id`, `biz_id`, `biz_type`, `title`, `description`, `experience`, `total_experience`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES (1, 247, '0', 0, '管理员调整', '管理员调整获得100经验', 100, 100, '1', '2023-08-22 21:52:44', '1', '2023-08-22 21:52:44', b'0', 1), (2, 247, '0', 0, '管理员调整', '管理员调整获得100经验', -50, 100, '1', '2023-08-22 21:52:44', '1', '2023-08-22 21:52:44', b'0', 1), (3, 247, '78', 2, '下单奖励', '下单获得 27 经验', 27, 127, NULL, '2023-08-30 18:46:52', NULL, '2023-08-30 18:46:52', b'0', 1), (4, 247, 'null', 3, '退单扣除', '退单获得 -6 经验', -6, 121, NULL, '2023-08-31 19:56:21', NULL, '2023-08-31 19:56:21', b'0', 1), (5, 247, '80', 2, '下单奖励', '下单获得 699906 经验', 699906, 700027, NULL, '2023-08-31 23:43:29', NULL, '2023-08-31 23:43:29', b'0', 1), (6, 247, '81', 2, '下单奖励', '下单获得 2799606 经验', 2799606, 3499633, NULL, '2023-08-31 23:46:17', NULL, '2023-08-31 23:46:17', b'0', 1), (7, 247, '94', 2, '下单奖励', '下单获得 559920 经验', 559920, 4059553, NULL, '2023-09-30 22:07:42', NULL, '2023-09-30 22:07:42', b'0', 1), (8, 247, '95', 2, '下单奖励', '下单获得 384945 经验', 384945, 4444498, NULL, '2023-10-01 21:36:13', NULL, '2023-10-01 21:36:13', b'0', 1), (9, 247, '96', 2, '下单奖励', '下单获得 699900 经验', 699900, 5144398, NULL, '2023-10-02 09:40:21', NULL, '2023-10-02 09:40:21', b'0', 1), (10, 247, '97', 2, '下单奖励', '下单获得 384945 经验', 384945, 5529343, NULL, '2023-10-02 10:21:12', NULL, '2023-10-02 10:21:12', b'0', 1), (11, 247, '11', 3, '退单扣除', '退单获得 -699900 经验', -699900, 4829443, NULL, '2023-10-02 15:19:37', NULL, '2023-10-02 15:19:37', b'0', 1), (12, 247, '98', 2, '下单奖励', '下单获得 384945 经验', 384945, 5214388, NULL, '2023-10-02 15:38:39', NULL, '2023-10-02 15:38:39', b'0', 1), (13, 247, '102', 2, '下单奖励', '下单获得 201 经验', 201, 5214589, NULL, '2023-10-05 23:05:29', NULL, '2023-10-05 23:05:29', b'0', 1), (14, 247, '107', 2, '下单奖励', '下单获得 201 经验', 201, 5214790, NULL, '2023-10-05 23:23:00', NULL, '2023-10-05 23:23:00', b'0', 1), (15, 248, '108', 2, '下单奖励', '下单获得 275 经验', 275, 275, NULL, '2023-10-06 10:13:44', NULL, '2023-10-06 10:13:44', b'0', 1), (16, 248, '118', 2, '下单奖励', '下单获得 203 经验', 203, 478, NULL, '2023-10-07 06:58:52', NULL, '2023-10-07 06:58:52', b'0', 1), (17, 248, '119', 2, '下单奖励', '下单获得 700100 经验', 700100, 700578, NULL, '2023-10-10 23:02:36', NULL, '2023-10-10 23:02:36', b'0', 1), (21, 248, '15', 3, '退单扣除', '退单获得 -700100 经验', -700100, 478, '1', '2023-10-10 23:11:19', '1', '2023-10-10 23:11:19', b'0', 1), (22, 248, '119', 3, '退单扣除', '退单获得 -700100 经验', -700100, 0, '1', '2023-10-10 23:11:23', '1', '2023-10-10 23:11:23', b'0', 1), (23, 248, '120', 2, '下单奖励', '下单获得 700100 经验', 700100, 700100, NULL, '2023-10-10 23:16:09', NULL, '2023-10-10 23:16:09', b'0', 1), (24, 248, '16', 3, '退单扣除', '退单获得 -700100 经验', -700100, 0, '1', '2023-10-10 23:20:10', '1', '2023-10-10 23:20:10', b'0', 1), (25, 248, '120', 3, '退单扣除', '退单获得 -700100 经验', -700100, 0, '1', '2023-10-10 23:20:16', '1', '2023-10-10 23:20:16', b'0', 1), (26, 248, '121', 2, '下单奖励', '下单获得 700100 经验', 700100, 700100, NULL, '2023-10-10 23:23:30', NULL, '2023-10-10 23:23:30', b'0', 1), (27, 248, '17', 3, '退单扣除', '退单获得 -700100 经验', -700100, 0, '1', '2023-10-10 23:23:50', '1', '2023-10-10 23:23:50', b'0', 1), (28, 248, '121', 3, '退单扣除', '退单获得 -700100 经验', -700100, 0, '1', '2023-10-10 23:23:55', '1', '2023-10-10 23:23:55', b'0', 1), (29, 248, '122', 2, '下单奖励', '下单获得 700100 经验', 700100, 700100, NULL, '2023-10-10 23:28:07', NULL, '2023-10-10 23:28:07', b'0', 1), (30, 248, '18', 3, '退单扣除', '退单获得 -700100 经验', -700100, 0, '1', '2023-10-10 23:29:55', '1', '2023-10-10 23:29:55', b'0', 1), (31, 248, '123', 2, '下单奖励', '下单获得 300 经验', 300, 300, NULL, '2023-10-10 23:44:45', NULL, '2023-10-10 23:44:45', b'0', 1), (32, 248, '124', 2, '下单奖励', '下单获得 560120 经验', 560120, 560420, NULL, '2023-10-11 07:03:46', NULL, '2023-10-11 07:03:46', b'0', 1), (33, 248, '19', 3, '退单扣除', '退单获得 -300 经验', -300, 560120, '1', '2023-10-11 07:04:28', '1', '2023-10-11 07:04:28', b'0', 1), (34, 248, '125', 11, '下单奖励', '下单获得 700100 经验', 700100, 700100, NULL, '2023-10-11 07:33:29', NULL, '2023-10-11 07:33:29', b'0', 1), (37, 248, '127', 11, '下单奖励', '下单获得 385145 经验', 385145, 385145, NULL, '2023-10-11 07:36:44', NULL, '2023-10-11 07:36:44', b'0', 1), (40, 248, '120', 13, '下单奖励（单个取消）', '退款订单获得 -385145 经验', -385145, 0, '1', '2023-10-11 07:39:26', '1', '2023-10-11 07:39:26', b'0', 1), (41, 248, '131', 11, '下单奖励', '下单获得 700100 经验', 700100, 700100, NULL, '2023-10-24 12:33:22', NULL, '2023-10-24 12:33:22', b'0', 1), (42, 247, '136', 11, '下单奖励', '下单获得 255 经验', 255, 5215045, NULL, '2023-12-12 20:49:44', NULL, '2023-12-12 20:49:44', b'0', 1), (43, 247, '137', 11, '下单奖励', '下单获得 385145 经验', 385145, 5600190, NULL, '2023-12-12 20:51:12', NULL, '2023-12-12 20:51:12', b'0', 1), (44, 247, '139', 11, '下单奖励', '下单获得 206 经验', 206, 5600396, NULL, '2023-12-16 00:01:26', NULL, '2023-12-16 00:01:26', b'0', 1), (45, 247, '142', 11, '下单奖励', '下单获得 100 经验', 100, 5600496, NULL, '2023-12-24 11:32:18', NULL, '2023-12-24 11:32:18', b'0', 1), (46, 247, '143', 11, '下单奖励', '下单获得 101 经验', 101, 5600597, NULL, '2023-12-24 13:08:57', NULL, '2023-12-24 13:08:57', b'0', 1), (47, 247, '151', 11, '下单奖励', '下单获得 385047 经验', 385047, 5985644, NULL, '2024-01-04 23:40:52', NULL, '2024-01-04 23:40:52', b'0', 1), (48, 247, '152', 11, '下单奖励', '下单获得 700000 经验', 700000, 6685644, NULL, '2024-01-13 16:47:16', NULL, '2024-01-13 16:47:16', b'0', 1), (49, 247, '153', 11, '下单奖励', '下单获得 103 经验', 103, 6685747, NULL, '2024-01-13 16:47:44', NULL, '2024-01-13 16:47:44', b'0', 1), (50, 247, '154', 11, '下单奖励', '下单获得 103 经验', 103, 6685850, NULL, '2024-01-13 17:07:58', NULL, '2024-01-13 17:07:58', b'0', 1), (51, 247, '155', 11, '下单奖励', '下单获得 300 经验', 300, 6686150, NULL, '2024-01-13 17:10:33', NULL, '2024-01-13 17:10:33', b'0', 1), (52, 247, '156', 11, '下单奖励', '下单获得 300 经验', 300, 6686450, NULL, '2024-01-13 17:12:13', NULL, '2024-01-13 17:12:13', b'0', 1), (53, 247, '157', 11, '下单奖励', '下单获得 300 经验', 300, 6686750, NULL, '2024-01-13 17:13:45', NULL, '2024-01-13 17:13:45', b'0', 1), (54, 247, '158', 11, '下单奖励', '下单获得 300 经验', 300, 6687050, NULL, '2024-01-13 17:16:49', NULL, '2024-01-13 17:16:49', b'0', 1), (55, 247, '159', 11, '下单奖励', '下单获得 300 经验', 300, 6687350, NULL, '2024-01-13 19:16:38', NULL, '2024-01-13 19:16:38', b'0', 1), (56, 247, '160', 11, '下单奖励', '下单获得 200 经验', 200, 6687550, NULL, '2024-01-13 20:23:10', NULL, '2024-01-13 20:23:10', b'0', 1), (57, 247, '161', 11, '下单奖励', '下单获得 386013 经验', 386013, 7073563, NULL, '2024-01-16 08:14:07', NULL, '2024-01-16 08:14:07', b'0', 1), (58, 247, '155', 13, '下单奖励（单个退款）', '退款订单获得 -384945 经验', -384945, 6688618, '1', '2024-01-16 20:27:23', '1', '2024-01-16 20:27:23', b'0', 1), (59, 283, '162', 11, '下单奖励', '下单获得 300 经验', 300, 300, NULL, '2024-01-17 14:42:00', NULL, '2024-01-17 14:42:00', b'0', 1), (60, 284, '163', 11, '下单奖励', '下单获得 300 经验', 300, 300, NULL, '2024-01-17 15:24:09', NULL, '2024-01-17 15:24:09', b'0', 1), (61, 285, '164', 11, '下单奖励', '下单获得 300 经验', 300, 300, NULL, '2024-01-17 15:45:36', NULL, '2024-01-17 15:45:36', b'0', 1), (62, 247, '165', 11, '下单奖励', '下单获得 300 经验', 300, 6688918, NULL, '2024-01-17 19:35:34', NULL, '2024-01-17 19:35:34', b'0', 1);
COMMIT;

-- ----------------------------
-- Table structure for member_group
-- ----------------------------
DROP TABLE IF EXISTS `member_group`;
CREATE TABLE `member_group`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '名称',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '备注',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户分组';

-- ----------------------------
-- Records of member_group
-- ----------------------------
BEGIN;
INSERT INTO `member_group` (`id`, `name`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES (1, '哈哈哈', 0, '你猜', '1', '2023-08-22 21:58:13', '1', '2023-08-22 21:58:13', b'0', 1);
COMMIT;

-- ----------------------------
-- Table structure for member_level
-- ----------------------------
DROP TABLE IF EXISTS `member_level`;
CREATE TABLE `member_level`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '等级名称',
  `level` int NOT NULL DEFAULT 0 COMMENT '等级',
  `experience` int NOT NULL DEFAULT 0 COMMENT '升级经验',
  `discount_percent` tinyint NOT NULL DEFAULT 100 COMMENT '享受折扣',
  `icon` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '等级图标',
  `background_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '等级背景图',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员等级';

-- ----------------------------
-- Records of member_level
-- ----------------------------
BEGIN;
INSERT INTO `member_level` (`id`, `name`, `level`, `experience`, `discount_percent`, `icon`, `background_url`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES (1, '青铜', 1, 100, 80, 'http://127.0.0.1:48080/admin-api/infra/file/4/get/59a1d52ebd38cc843fb5fafc30bce85f15d6ac22c8227dc3fe775d064f71ca6e.png', 'http://127.0.0.1:48080/admin-api/infra/file/4/get/179e45b487daad722fa8bccf382fa35a80caa0a6e6633ca4da961bfbefd9d68a.jpg', 0, '1', '2023-08-22 21:45:03', '1', '2023-08-22 21:45:12', b'0', 1), (2, '白银', 2, 500, 75, 'http://127.0.0.1:48080/admin-api/infra/file/4/get/439a9e188bc2342d79803b2ff1a4872a98a9545a108013f9c0550b7fc604f3d2.png', '', 0, '1', '2023-08-28 20:36:55', '1', '2023-08-28 20:36:55', b'0', 1), (3, '黄金', 3, 50000, 55, '', '', 0, '1', '2023-08-28 20:37:05', '1', '2023-08-28 20:37:05', b'0', 1);
COMMIT;

-- ----------------------------
-- Table structure for member_level_record
-- ----------------------------
DROP TABLE IF EXISTS `member_level_record`;
CREATE TABLE `member_level_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` bigint NOT NULL DEFAULT 0 COMMENT '用户编号',
  `level_id` bigint NOT NULL DEFAULT 0 COMMENT '等级编号',
  `level` int NOT NULL DEFAULT 0 COMMENT '会员等级',
  `discount_percent` tinyint NOT NULL DEFAULT 100 COMMENT '享受折扣',
  `experience` int NOT NULL DEFAULT 0 COMMENT '升级经验',
  `user_experience` int NOT NULL DEFAULT 0 COMMENT '会员此时的经验',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '备注',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '描述',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE COMMENT '会员等级记录-用户编号'
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员等级记录';

-- ----------------------------
-- Records of member_level_record
-- ----------------------------
BEGIN;
INSERT INTO `member_level_record` (`id`, `user_id`, `level_id`, `level`, `discount_percent`, `experience`, `user_experience`, `remark`, `description`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES (1, 247, 1, 1, 80, 100, 100, '321321312', '管理员调整为：青铜', '1', '2023-08-22 21:52:44', '1', '2023-08-22 21:52:44', b'0', 1), (2, 247, 1, 1, 80, 100, 121, '', '', NULL, '2023-08-31 19:56:21', NULL, '2023-08-31 19:56:21', b'0', 1), (3, 247, 3, 3, 55, 50000, 700027, '', '', NULL, '2023-08-31 23:43:29', NULL, '2023-08-31 23:43:29', b'0', 1), (4, 247, 3, 3, 55, 50000, 4059553, '', '', NULL, '2023-09-30 22:07:42', NULL, '2023-09-30 22:07:42', b'0', 1), (5, 247, 3, 3, 55, 50000, 5144398, '', '', NULL, '2023-10-02 09:40:21', NULL, '2023-10-02 09:40:21', b'0', 1), (6, 247, 3, 3, 55, 50000, 4829443, '', '', NULL, '2023-10-02 15:19:37', NULL, '2023-10-02 15:19:37', b'0', 1), (7, 247, 3, 3, 55, 50000, 5214589, '', '', NULL, '2023-10-05 23:05:29', NULL, '2023-10-05 23:05:29', b'0', 1), (8, 248, 1, 1, 80, 100, 275, '', '', NULL, '2023-10-06 10:13:44', NULL, '2023-10-06 10:13:44', b'0', 1), (9, 248, 3, 3, 55, 50000, 700578, '', '', NULL, '2023-10-10 23:02:36', NULL, '2023-10-10 23:02:36', b'0', 1), (13, 248, 1, 1, 80, 100, 478, '', '', '1', '2023-10-10 23:11:19', '1', '2023-10-10 23:11:19', b'0', 1), (14, 248, 3, 3, 55, 50000, 700100, '', '', NULL, '2023-10-10 23:16:09', NULL, '2023-10-10 23:16:09', b'0', 1), (15, 248, 3, 3, 55, 50000, 700100, '', '', NULL, '2023-10-10 23:23:30', NULL, '2023-10-10 23:23:30', b'0', 1), (16, 248, 3, 3, 55, 50000, 700100, '', '', NULL, '2023-10-10 23:28:07', NULL, '2023-10-10 23:28:07', b'0', 1), (17, 248, 1, 1, 80, 100, 300, '', '', NULL, '2023-10-10 23:44:45', NULL, '2023-10-10 23:44:45', b'0', 1), (18, 248, 3, 3, 55, 50000, 560420, '', '', NULL, '2023-10-11 07:03:46', NULL, '2023-10-11 07:03:46', b'0', 1), (19, 248, 3, 3, 55, 50000, 700100, '', '', NULL, '2023-10-11 07:33:29', NULL, '2023-10-11 07:33:29', b'0', 1), (20, 248, 3, 3, 55, 50000, 700100, '', '', NULL, '2023-10-24 12:33:22', NULL, '2023-10-24 12:33:22', b'0', 1), (21, 247, 3, 3, 55, 50000, 5215045, '', '', NULL, '2023-12-12 20:49:44', NULL, '2023-12-12 20:49:44', b'0', 1), (22, 247, 3, 3, 55, 50000, 5600396, '', '', NULL, '2023-12-16 00:01:26', NULL, '2023-12-16 00:01:26', b'0', 1), (23, 247, 3, 3, 55, 50000, 5600597, '', '', NULL, '2023-12-24 13:08:57', NULL, '2023-12-24 13:08:57', b'0', 1), (24, 247, 3, 3, 55, 50000, 6685644, '', '', NULL, '2024-01-13 16:47:16', NULL, '2024-01-13 16:47:16', b'0', 1), (25, 247, 3, 3, 55, 50000, 6685850, '', '', NULL, '2024-01-13 17:07:58', NULL, '2024-01-13 17:07:58', b'0', 1), (26, 247, 3, 3, 55, 50000, 6686450, '', '', NULL, '2024-01-13 17:12:13', NULL, '2024-01-13 17:12:13', b'0', 1), (27, 247, 3, 3, 55, 50000, 6687050, '', '', NULL, '2024-01-13 17:16:49', NULL, '2024-01-13 17:16:49', b'0', 1), (28, 247, 3, 3, 55, 50000, 6687550, '', '', NULL, '2024-01-13 20:23:10', NULL, '2024-01-13 20:23:10', b'0', 1), (29, 247, 3, 3, 55, 50000, 6688618, '', '', '1', '2024-01-16 20:27:23', '1', '2024-01-16 20:27:23', b'0', 1), (30, 283, 1, 1, 80, 100, 300, '', '', NULL, '2024-01-17 14:42:00', NULL, '2024-01-17 14:42:00', b'0', 1), (31, 284, 1, 1, 80, 100, 300, '', '', NULL, '2024-01-17 15:24:09', NULL, '2024-01-17 15:24:09', b'0', 1), (32, 285, 1, 1, 80, 100, 300, '', '', NULL, '2024-01-17 15:45:36', NULL, '2024-01-17 15:45:36', b'0', 1);
COMMIT;

-- ----------------------------
-- Table structure for member_point_record
-- ----------------------------
DROP TABLE IF EXISTS `member_point_record`;
CREATE TABLE `member_point_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `user_id` bigint NOT NULL COMMENT '用户编号',
  `biz_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务编码',
  `biz_type` tinyint NOT NULL COMMENT '业务类型',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '积分标题',
  `description` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '积分描述',
  `point` int NOT NULL COMMENT '积分',
  `total_point` int NOT NULL COMMENT '变动后的积分',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `index_userId`(`user_id` ASC) USING BTREE,
  INDEX `index_title`(`title` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 89 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户积分记录';

-- ----------------------------
-- Records of member_point_record
-- ----------------------------
BEGIN;
INSERT INTO `member_point_record` (`id`, `user_id`, `biz_id`, `biz_type`, `title`, `description`, `point`, `total_point`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES (1, 247, '1', 1, '12', NULL, 33, 12, '', '2023-07-02 14:50:23', '', '2023-08-20 11:03:01', b'0', 1), (2, 247, '12', 1, '123', NULL, 22, 130, '', '2023-07-02 14:50:23', '', '2023-08-20 11:03:00', b'0', 1), (3, 247, '12', 1, '12', NULL, -12, 12, '', '2023-07-02 14:50:55', '', '2023-08-21 14:19:29', b'0', 1), (4, 247, '78', 10, '订单消费', '下单获得 81 积分', 81, 91, NULL, '2023-08-30 18:46:52', NULL, '2023-08-30 18:46:52', b'0', 1), (5, 247, 'null', 11, '订单取消', '退单获得 -18 积分', -18, 73, NULL, '2023-08-31 19:56:21', NULL, '2023-08-31 19:56:21', b'0', 1), (6, 247, '80', 10, '订单消费', '下单获得 2099718 积分', 2099718, 2099791, NULL, '2023-08-31 23:43:29', NULL, '2023-08-31 23:43:29', b'0', 1), (7, 247, '81', 10, '订单消费', '下单获得 8398818 积分', 8398818, 10498609, NULL, '2023-08-31 23:46:17', NULL, '2023-08-31 23:46:17', b'0', 1), (8, 247, '85', 12, '订单使用', '下单使用 -30 积分', -30, 10498579, '247', '2023-09-20 17:11:36', '247', '2023-09-20 17:11:36', b'0', 1), (9, 247, '86', 12, '订单使用', '下单使用 -30 积分', -30, 10498549, '247', '2023-09-20 19:32:59', '247', '2023-09-20 19:32:59', b'0', 1), (10, 247, '87', 12, '订单使用', '下单使用 -30 积分', -30, 10498519, '247', '2023-09-20 23:11:21', '247', '2023-09-20 23:11:21', b'0', 1), (11, 247, '88', 12, '订单使用', '下单使用 -30 积分', -30, 10498489, '247', '2023-09-20 23:20:21', '247', '2023-09-20 23:20:21', b'0', 1), (12, 247, '89', 12, '订单使用', '下单使用 -30 积分', -30, 10498459, '247', '2023-09-23 23:51:40', '247', '2023-09-23 23:51:40', b'0', 1), (13, 247, '90', 12, '订单使用', '下单使用 -30 积分', -30, 10498429, '247', '2023-09-23 23:52:34', '247', '2023-09-23 23:52:34', b'0', 1), (14, 247, '91', 12, '订单使用', '下单使用 -30 积分', -30, 10498399, '247', '2023-09-23 23:54:18', '247', '2023-09-23 23:54:18', b'0', 1), (15, 247, '92', 12, '订单使用', '下单使用 -30 积分', -30, 10498369, '247', '2023-09-23 23:55:33', '247', '2023-09-23 23:55:33', b'0', 1), (16, 247, '93', 12, '订单使用', '下单使用 -30 积分', -30, 10498339, '247', '2023-09-23 23:56:53', '247', '2023-09-23 23:56:53', b'0', 1), (17, 247, '85', 11, '订单取消', '订单取消，退还 10 积分', 10, 10498349, NULL, '2023-09-25 23:54:47', NULL, '2023-09-25 23:54:47', b'0', 1), (18, 247, '86', 11, '订单取消', '订单取消，退还 10 积分', 10, 10498359, NULL, '2023-09-25 23:54:48', NULL, '2023-09-25 23:54:48', b'0', 1), (19, 247, '87', 11, '订单取消', '订单取消，退还 10 积分', 10, 10498369, NULL, '2023-09-25 23:54:48', NULL, '2023-09-25 23:54:48', b'0', 1), (20, 247, '88', 11, '订单取消', '订单取消，退还 10 积分', 10, 10498379, NULL, '2023-09-25 23:54:48', NULL, '2023-09-25 23:54:48', b'0', 1), (21, 247, '89', 11, '订单取消', '订单取消，退还 10 积分', 10, 10498389, NULL, '2023-09-25 23:54:48', NULL, '2023-09-25 23:54:48', b'0', 1), (22, 247, '90', 11, '订单取消', '订单取消，退还 10 积分', 10, 10498399, NULL, '2023-09-25 23:54:48', NULL, '2023-09-25 23:54:48', b'0', 1), (23, 247, '91', 11, '订单取消', '订单取消，退还 10 积分', 10, 10498409, NULL, '2023-09-25 23:54:48', NULL, '2023-09-25 23:54:48', b'0', 1), (24, 247, '92', 11, '订单取消', '订单取消，退还 10 积分', 10, 10498419, NULL, '2023-09-25 23:54:48', NULL, '2023-09-25 23:54:48', b'0', 1), (25, 247, '93', 11, '订单取消', '订单取消，退还 10 积分', 10, 10498429, NULL, '2023-09-25 23:54:48', NULL, '2023-09-25 23:54:48', b'0', 1), (26, 247, '94', 10, '订单奖励', '下单获得 16797 积分', 16797, 10515226, NULL, '2023-09-30 22:07:42', NULL, '2023-09-30 22:07:42', b'0', 1), (27, 247, '95', 10, '订单奖励', '下单获得 11548 积分', 11548, 10526774, NULL, '2023-10-01 21:36:13', NULL, '2023-10-01 21:36:13', b'0', 1), (28, 247, '1', 2, '管理员修改', '管理员修改 112 积分', 112, 10526886, '1', '2023-10-01 22:44:05', '1', '2023-10-01 22:44:05', b'0', 1), (29, 247, '96', 10, '订单奖励', '下单获得 20997 积分', 20997, 10547883, NULL, '2023-10-02 09:40:20', NULL, '2023-10-02 09:40:20', b'0', 1), (30, 247, '97', 10, '订单奖励', '下单获得 11548 积分', 11548, 10559431, NULL, '2023-10-02 10:21:12', NULL, '2023-10-02 10:21:12', b'0', 1), (31, 247, '98', 10, '订单奖励', '下单获得 11548 积分', 11548, 10570979, NULL, '2023-10-02 15:38:39', NULL, '2023-10-02 15:38:39', b'0', 1), (32, 247, '102', 10, '订单奖励', '下单获得 6 积分', 6, 10570985, NULL, '2023-10-05 23:05:29', NULL, '2023-10-05 23:05:29', b'0', 1), (33, 247, '107', 10, '订单奖励', '下单获得 6 积分', 6, 10570991, NULL, '2023-10-05 23:23:00', NULL, '2023-10-05 23:23:00', b'0', 1), (34, 248, '108', 10, '订单奖励', '下单获得 8 积分', 8, 8, NULL, '2023-10-06 10:13:44', NULL, '2023-10-06 10:13:44', b'0', 1), (35, 248, '118', 10, '订单奖励', '下单获得 6 积分', 6, 14, NULL, '2023-10-07 06:58:51', NULL, '2023-10-07 06:58:51', b'0', 1), (36, 248, '119', 10, '订单奖励', '下单获得 21003 积分', 21003, 21017, NULL, '2023-10-10 23:02:35', NULL, '2023-10-10 23:02:35', b'0', 1), (40, 248, '15', 14, '订单退款', '订单退款，扣除赠送的 -21003 积分', -21003, 2080697, '1', '2023-10-10 23:11:19', '1', '2023-10-10 23:11:19', b'0', 1), (41, 248, '119', 11, '订单取消', '订单取消，退还 -21003 积分', -21003, 2059694, '1', '2023-10-10 23:11:23', '1', '2023-10-10 23:11:23', b'0', 1), (42, 248, '120', 10, '订单奖励', '下单获得 21003 积分', 21003, 2080697, NULL, '2023-10-10 23:16:09', NULL, '2023-10-10 23:16:09', b'0', 1), (43, 248, '16', 14, '订单退款', '订单退款，扣除赠送的 -21003 积分', -21003, 2059694, '1', '2023-10-10 23:20:10', '1', '2023-10-10 23:20:10', b'0', 1), (44, 248, '120', 11, '订单取消', '订单取消，退还 -21003 积分', -21003, 2038691, '1', '2023-10-10 23:20:16', '1', '2023-10-10 23:20:16', b'0', 1), (45, 248, '121', 10, '订单奖励', '下单获得 21003 积分', 21003, 2059694, NULL, '2023-10-10 23:23:30', NULL, '2023-10-10 23:23:30', b'0', 1), (46, 248, '17', 14, '订单退款', '订单退款，扣除赠送的 -21003 积分', -21003, 2038691, '1', '2023-10-10 23:23:50', '1', '2023-10-10 23:23:50', b'0', 1), (47, 248, '121', 11, '订单取消', '订单取消，退还 -21003 积分', -21003, 2017688, '1', '2023-10-10 23:23:55', '1', '2023-10-10 23:23:55', b'0', 1), (48, 248, '122', 10, '订单奖励', '下单获得 21003 积分', 21003, 2038691, NULL, '2023-10-10 23:28:07', NULL, '2023-10-10 23:28:07', b'0', 1), (49, 248, '18', 14, '订单退款', '订单退款，扣除赠送的 -21003 积分', -21003, 2017688, '1', '2023-10-10 23:29:55', '1', '2023-10-10 23:29:55', b'0', 1), (50, 248, '123', 10, '订单奖励', '下单获得 9 积分', 9, 2017697, NULL, '2023-10-10 23:44:45', NULL, '2023-10-10 23:44:45', b'0', 1), (51, 248, '124', 10, '订单奖励', '下单获得 16803 积分', 16803, 2034500, NULL, '2023-10-11 07:03:46', NULL, '2023-10-11 07:03:46', b'0', 1), (52, 248, '19', 14, '订单退款', '订单退款，扣除赠送的 -9 积分', -9, 2034491, '1', '2023-10-11 07:04:28', '1', '2023-10-11 07:04:28', b'0', 1), (53, 248, '125', 21, '订单积分奖励', '下单获得 21003 积分', 21003, 21003, NULL, '2023-10-11 07:33:29', NULL, '2023-10-11 07:33:29', b'0', 1), (56, 248, '127', 21, '订单积分奖励', '下单获得 11554 积分', 11554, 11554, NULL, '2023-10-11 07:36:44', NULL, '2023-10-11 07:36:44', b'0', 1), (59, 248, '120', 23, '订单积分奖励（单个退款）', '订单退款，扣除赠送的 -11554 积分', -11554, 0, '1', '2023-10-11 07:39:26', '1', '2023-10-11 07:39:26', b'0', 1), (60, 248, '131', 21, '订单积分奖励', '下单获得 21003 积分', 21003, 21003, NULL, '2023-10-24 12:33:22', NULL, '2023-10-24 12:33:22', b'0', 1), (61, 247, '136', 21, '订单积分奖励', '下单获得 7 积分', 7, 10570998, NULL, '2023-12-12 20:49:44', NULL, '2023-12-12 20:49:44', b'0', 1), (62, 247, '137', 21, '订单积分奖励', '下单获得 11554 积分', 11554, 10582552, NULL, '2023-12-12 20:51:12', NULL, '2023-12-12 20:51:12', b'0', 1), (63, 247, '139', 21, '订单积分奖励', '下单获得 6 积分', 6, 10582558, NULL, '2023-12-16 00:01:26', NULL, '2023-12-16 00:01:26', b'0', 1), (64, 247, '142', 21, '订单积分奖励', '下单获得 3 积分', 3, 10582561, NULL, '2023-12-24 11:32:18', NULL, '2023-12-24 11:32:18', b'0', 1), (65, 247, '143', 21, '订单积分奖励', '下单获得 3 积分', 3, 10582564, NULL, '2023-12-24 13:08:57', NULL, '2023-12-24 13:08:57', b'0', 1), (66, 247, '5', 1, '签到', '签到获得 10 积分', 10, 10582574, '247', '2023-12-27 23:56:31', '247', '2023-12-27 23:56:31', b'0', 1), (67, 247, '6', 1, '签到', '签到获得 3 积分', 3, 10582577, '247', '2023-12-28 00:07:31', '247', '2023-12-28 00:07:31', b'0', 1), (68, 247, '7', 1, '签到', '签到获得 10 积分', 10, 121, '247', '2024-01-04 13:02:21', '247', '2024-01-04 13:02:21', b'0', 1), (69, 247, '8', 1, '签到', '签到获得 3 积分', 3, 124, '247', '2024-01-04 13:03:21', '247', '2024-01-04 13:03:21', b'0', 1), (70, 247, '9', 1, '签到', '签到获得 3 积分', 3, 127, '247', '2024-01-04 13:03:32', '247', '2024-01-04 13:03:32', b'0', 1), (71, 247, '10', 1, '签到', '签到获得 3 积分', 3, 130, '247', '2024-01-04 13:03:45', '247', '2024-01-04 13:03:45', b'0', 1), (72, 247, '11', 1, '签到', '签到获得 5 积分', 5, 135, '247', '2024-01-04 13:04:11', '247', '2024-01-04 13:04:11', b'0', 1), (73, 247, '151', 21, '订单积分奖励', '下单获得 11551 积分', 11551, 11686, NULL, '2024-01-04 23:40:52', NULL, '2024-01-04 23:40:52', b'0', 1), (74, 247, '152', 21, '订单积分奖励', '下单获得 21000 积分', 21000, 32686, NULL, '2024-01-13 16:47:16', NULL, '2024-01-13 16:47:16', b'0', 1), (75, 247, '153', 21, '订单积分奖励', '下单获得 3 积分', 3, 32689, NULL, '2024-01-13 16:47:44', NULL, '2024-01-13 16:47:44', b'0', 1), (76, 247, '154', 21, '订单积分奖励', '下单获得 3 积分', 3, 32692, NULL, '2024-01-13 17:07:58', NULL, '2024-01-13 17:07:58', b'0', 1), (77, 247, '155', 21, '订单积分奖励', '下单获得 9 积分', 9, 32701, NULL, '2024-01-13 17:10:33', NULL, '2024-01-13 17:10:33', b'0', 1), (78, 247, '156', 21, '订单积分奖励', '下单获得 9 积分', 9, 32710, NULL, '2024-01-13 17:12:13', NULL, '2024-01-13 17:12:13', b'0', 1), (79, 247, '157', 21, '订单积分奖励', '下单获得 9 积分', 9, 32719, NULL, '2024-01-13 17:13:45', NULL, '2024-01-13 17:13:45', b'0', 1), (80, 247, '158', 21, '订单积分奖励', '下单获得 9 积分', 9, 32728, NULL, '2024-01-13 17:16:49', NULL, '2024-01-13 17:16:49', b'0', 1), (81, 247, '159', 21, '订单积分奖励', '下单获得 9 积分', 9, 32737, NULL, '2024-01-13 19:16:38', NULL, '2024-01-13 19:16:38', b'0', 1), (82, 247, '160', 21, '订单积分奖励', '下单获得 6 积分', 6, 32743, NULL, '2024-01-13 20:23:10', NULL, '2024-01-13 20:23:10', b'0', 1), (83, 247, '161', 21, '订单积分奖励', '下单获得 11580 积分', 11580, 44323, NULL, '2024-01-16 08:14:07', NULL, '2024-01-16 08:14:07', b'0', 1), (84, 247, '155', 23, '订单积分奖励（单个退款）', '订单退款，扣除赠送的 -11547 积分', -11547, 32776, '1', '2024-01-16 20:27:23', '1', '2024-01-16 20:27:23', b'0', 1), (85, 283, '162', 21, '订单积分奖励', '下单获得 9 积分', 9, 9, NULL, '2024-01-17 14:42:00', NULL, '2024-01-17 14:42:00', b'0', 1), (86, 284, '163', 21, '订单积分奖励', '下单获得 9 积分', 9, 9, NULL, '2024-01-17 15:24:09', NULL, '2024-01-17 15:24:09', b'0', 1), (87, 285, '164', 21, '订单积分奖励', '下单获得 9 积分', 9, 9, NULL, '2024-01-17 15:45:36', NULL, '2024-01-17 15:45:36', b'0', 1), (88, 247, '165', 21, '订单积分奖励', '下单获得 9 积分', 9, 32785, NULL, '2024-01-17 19:35:34', NULL, '2024-01-17 19:35:34', b'0', 1);
COMMIT;

-- ----------------------------
-- Table structure for member_sign_in_config
-- ----------------------------
DROP TABLE IF EXISTS `member_sign_in_config`;
CREATE TABLE `member_sign_in_config`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '编号',
  `day` int NOT NULL COMMENT '第几天',
  `point` int NOT NULL COMMENT '奖励积分',
  `experience` int NOT NULL DEFAULT 0 COMMENT '奖励经验',
  `status` tinyint NOT NULL COMMENT '状态',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '签到规则';

-- ----------------------------
-- Records of member_sign_in_config
-- ----------------------------
BEGIN;
INSERT INTO `member_sign_in_config` (`id`, `day`, `point`, `experience`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES (1, 1, 10, 0, 0, '', '2023-08-20 01:38:56', '', '2023-08-20 01:38:56', b'0', 0), (2, 2, 20, 0, 0, '', '2023-08-20 01:38:56', '', '2023-08-20 01:38:56', b'0', 0), (3, 7, 1001, 0, 0, '', '2023-08-20 01:38:56', '', '2023-08-20 01:38:56', b'0', 0), (4, 6, 12121, 0, 0, '', '2023-08-20 01:38:56', '', '2023-08-20 01:38:56', b'0', 0), (5, 2, 12, 0, 0, '', '2023-08-20 01:38:56', '', '2023-08-20 01:38:56', b'0', 0), (6, 1, 10, 0, 0, '1', '2023-08-20 19:20:42', '1', '2023-08-20 19:20:56', b'0', 1), (7, 7, 22, 0, 0, '1', '2023-08-20 19:20:48', '1', '2023-08-20 19:20:48', b'0', 1), (8, 2, 3, 0, 0, '1', '2023-08-21 20:22:44', '1', '2023-08-21 20:22:44', b'0', 1), (9, 3, 4, 0, 0, '1', '2023-08-21 20:22:48', '1', '2023-08-21 20:22:48', b'0', 1), (10, 4, 5, 0, 0, '1', '2023-08-21 20:22:51', '1', '2023-08-21 20:22:51', b'0', 1), (11, 5, 6, 0, 0, '1', '2023-08-21 20:22:56', '1', '2023-08-21 20:22:56', b'0', 1), (12, 6, 7, 0, 0, '1', '2023-08-21 20:22:59', '1', '2023-08-21 20:22:59', b'0', 1);
COMMIT;

-- ----------------------------
-- Table structure for member_sign_in_record
-- ----------------------------
DROP TABLE IF EXISTS `member_sign_in_record`;
CREATE TABLE `member_sign_in_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '签到自增id',
  `user_id` int NULL DEFAULT NULL COMMENT '签到用户',
  `day` int NULL DEFAULT NULL COMMENT '第几天签到',
  `point` int NOT NULL DEFAULT 0 COMMENT '签到的分数',
  `experience` int NOT NULL DEFAULT 0 COMMENT '奖励经验',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '签到记录';

-- ----------------------------
-- Records of member_sign_in_record
-- ----------------------------
BEGIN;
INSERT INTO `member_sign_in_record` (`id`, `user_id`, `day`, `point`, `experience`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES (1, 247, 1, 123, 0, '', '2023-09-24 01:23:47', '', '2023-09-24 01:23:47', b'0', 0), (2, 247, 12, 12, 0, '', '2023-09-24 01:23:47', '', '2023-09-24 01:23:47', b'0', 0), (3, 247, 12, 1212, 0, '', '2023-09-24 01:23:47', '', '2023-09-24 01:23:47', b'0', 0), (4, 247, 12, 1212, 0, '', '2023-09-24 01:23:47', '', '2023-09-24 01:23:47', b'0', 0), (5, 247, 1, 10, 0, '247', '2023-12-27 23:56:31', '247', '2023-12-27 23:56:31', b'0', 1), (6, 247, 2, 3, 0, '247', '2023-12-28 00:07:31', '247', '2023-12-28 00:07:31', b'0', 1), (7, 247, 3, 10, 0, '247', '2024-01-03 13:02:21', '247', '2023-12-24 18:18:36', b'0', 1), (8, 247, 2, 3, 0, '247', '2024-01-02 13:03:21', '247', '2023-12-24 18:18:02', b'0', 1), (9, 247, 1, 3, 0, '247', '2024-01-01 13:03:32', '247', '2023-12-24 18:18:31', b'0', 1), (11, 247, 4, 5, 0, '247', '2024-01-04 13:04:11', '247', '2024-01-04 13:04:11', b'0', 1);
COMMIT;

-- ----------------------------
-- Table structure for member_tag
-- ----------------------------
DROP TABLE IF EXISTS `member_tag`;
CREATE TABLE `member_tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '标签名称',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员标签';

-- ----------------------------
-- Records of member_tag
-- ----------------------------
BEGIN;
INSERT INTO `member_tag` (`id`, `name`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES (1, '绿色', '1', '2023-08-20 09:21:12', '1', '2023-08-20 09:21:12', b'0', 1), (2, '黄色', '1', '2023-08-20 09:21:27', '1', '2023-08-20 09:21:27', b'0', 1);
COMMIT;

-- ----------------------------
-- Table structure for member_user
-- ----------------------------
DROP TABLE IF EXISTS `member_user`;
CREATE TABLE `member_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `mobile` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '密码',
  `status` tinyint NOT NULL COMMENT '状态',
  `register_ip` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '注册 IP',
  `register_terminal` tinyint NULL DEFAULT NULL COMMENT '注册终端',
  `login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `nickname` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '用户昵称',
  `avatar` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '头像',
  `name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '真实名字',
  `sex` tinyint NULL DEFAULT 0 COMMENT '用户性别',
  `area_id` bigint NULL DEFAULT NULL COMMENT '所在地',
  `birthday` datetime NULL DEFAULT NULL COMMENT '出生日期',
  `mark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '会员备注',
  `point` int NOT NULL DEFAULT 0 COMMENT '积分',
  `tag_ids` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户标签编号列表，以逗号分隔',
  `level_id` bigint NULL DEFAULT NULL COMMENT '等级编号',
  `experience` int NOT NULL DEFAULT 0 COMMENT '经验',
  `group_id` bigint NULL DEFAULT NULL COMMENT '用户分组编号',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 286 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会员用户';

-- ----------------------------
-- Records of member_user
-- ----------------------------
BEGIN;
INSERT INTO `member_user` (`id`, `mobile`, `password`, `status`, `register_ip`, `register_terminal`, `login_ip`, `login_date`, `nickname`, `avatar`, `name`, `sex`, `area_id`, `birthday`, `mark`, `point`, `tag_ids`, `level_id`, `experience`, `group_id`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES (247, '15601691399', '$2a$04$7BixfD4z0Xs/pOlILmlmSeEJ0Cy3aKVcpGIi3.xkYWwR5I8EIV/qy', 0, '127.0.0.1', NULL, '127.0.0.1', '2024-01-17 19:27:05', '啦啦啦', 'http://127.0.0.1:48080/admin-api/infra/file/4/get/f341ff4f7838c29a4978421d0de39ac5d9e18016af124e7ef94a29cb4e8e5a15.jpeg', '啦啦啦', 2, 130102, '2023-08-28 00:00:00', '备注3213123', 32785, '1,2', 0, 6688918, 1, NULL, '2023-10-15 11:23:08', NULL, '2024-01-17 19:35:34', b'0', 1), (248, '15601691499', '$2a$04$uhOkX4y5VkktR0B2KXAQmuAOYbVB3UiJ4smmD6HoIqiu/71kG0pXy', 0, '127.0.0.1', NULL, '127.0.0.1', '2023-12-24 22:38:41', '土豆233', '', '', 0, NULL, NULL, NULL, 21003, NULL, 3, 700100, NULL, '247', '2023-10-14 18:21:04', '247', '2023-12-22 13:54:33', b'0', 1), (249, '15601691388', '$2a$04$bGr1w7v1mz7PSpLYtrgFie/1KU8ytH/7k74naQJx574KXA2UzoB.6', 0, '127.0.0.1', 30, '127.0.0.1', '2024-01-13 17:15:46', '土豆233', '', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, '2023-10-30 12:49:03', '247', '2024-01-13 17:15:46', b'0', 1), (250, '15601691301', '$2a$04$FAGPqc9ZByK3xf1Z4bWpW./kuUicI82FroqtKjuRP04uWgGXfioyC', 0, '127.0.0.1', 20, '127.0.0.1', '2023-12-28 22:58:49', '土豆233', '', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, '247', '2023-12-17 19:22:26', NULL, '2023-12-28 22:58:49', b'0', 1), (251, '15601691302', '$2a$04$iFjWz8/d7CibBCdxJX/dD.Zz1fys9hc9SNH/Hpcajp5OUUNE0Vfoq', 0, '127.0.0.1', 20, '127.0.0.1', '2024-01-13 16:54:30', '土豆233', '', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, '247', '2023-12-17 19:22:57', '247', '2024-01-13 16:54:30', b'0', 1), (252, NULL, '$2a$04$EcI3tqOw53N5yFRkVB2gkO6eblGjF7wBAZggM/AMZIxiKfT9A6.4C', 0, '127.0.0.1', 20, '', NULL, '土豆233', 'http://127.0.0.1:48080/admin-api/infra/file/4/get/49009b73d392657c85bed2e82e22b09720e7c68466b8565c8b2f6c00e03b705e.jpg', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, '247', '2023-12-22 00:10:06', '247', '2023-12-22 00:10:06', b'0', 1), (253, NULL, '$2a$04$S0/ctJznIyzvYWuUDd1zZusYFM8KZqvRK.q9KKXn9Z8DLY23UVMDG', 0, '127.0.0.1', 20, '', NULL, '土豆233', 'http://127.0.0.1:48080/admin-api/infra/file/4/get/49009b73d392657c85bed2e82e22b09720e7c68466b8565c8b2f6c00e03b705e.jpg', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, '247', '2023-12-22 00:10:34', '247', '2023-12-22 00:10:34', b'0', 1), (254, NULL, '$2a$04$ZWMBB2sVjG5V39xVisdZbeCSNIuLdgCJNLTRSH698QfZwF0s403nC', 0, '127.0.0.1', 20, '', NULL, '土豆233', 'http://127.0.0.1:48080/admin-api/infra/file/4/get/49009b73d392657c85bed2e82e22b09720e7c68466b8565c8b2f6c00e03b705e.jpg', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, '247', '2023-12-22 00:11:10', '247', '2023-12-22 00:11:10', b'0', 1), (255, NULL, '$2a$04$Rm/ssffAJ3od9b.IDiSgYeyyY6FHWMrDPFywrL5eWZcmWrtUgUkQG', 0, '127.0.0.1', 20, '', NULL, '土豆233', 'http://127.0.0.1:48080/admin-api/infra/file/4/get/49009b73d392657c85bed2e82e22b09720e7c68466b8565c8b2f6c00e03b705e.jpg', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, '247', '2023-12-22 00:11:22', '247', '2023-12-22 00:11:22', b'0', 1), (256, NULL, '$2a$04$UA8Y87jYk3m1roGHGZkXLOPqCHLpmYjd7xFVSVSZ5m5fuRMz5GIgW', 0, '127.0.0.1', 20, '127.0.0.1', '2023-12-22 00:14:02', '土豆233', 'http://127.0.0.1:48080/admin-api/infra/file/4/get/49009b73d392657c85bed2e82e22b09720e7c68466b8565c8b2f6c00e03b705e.jpg', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, '247', '2023-12-22 00:14:02', '247', '2023-12-22 00:14:02', b'0', 1), (257, NULL, '$2a$04$ZEJ4s8x68UzAlKdRfnByCOYozK5IchWJNBK56MvmHfR6T0rdk30sa', 0, '127.0.0.1', 20, '127.0.0.1', '2023-12-22 00:16:38', '土豆233', 'http://127.0.0.1:48080/admin-api/infra/file/4/get/49009b73d392657c85bed2e82e22b09720e7c68466b8565c8b2f6c00e03b705e.jpg', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, '247', '2023-12-22 00:16:38', '247', '2023-12-22 00:16:38', b'0', 1), (258, NULL, '$2a$04$6AKifvXpNxBBDnspkzRc.OKgZIlg4TxPvHcRvvnqmVpjSsQQ8mgtW', 0, '127.0.0.1', 20, '127.0.0.1', '2023-12-22 00:17:10', '土豆233', 'http://127.0.0.1:48080/admin-api/infra/file/4/get/49009b73d392657c85bed2e82e22b09720e7c68466b8565c8b2f6c00e03b705e.jpg', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, '247', '2023-12-22 00:17:10', '247', '2023-12-22 00:17:10', b'0', 1), (259, NULL, '$2a$04$TV0itDCiIU2HVHadk7sxyOumKYTG2tkkePlL.AYogA49ronHWDbGy', 0, '127.0.0.1', 20, '127.0.0.1', '2023-12-22 00:17:38', '土豆233', 'http://127.0.0.1:48080/admin-api/infra/file/4/get/49009b73d392657c85bed2e82e22b09720e7c68466b8565c8b2f6c00e03b705e.jpg', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, '247', '2023-12-22 00:17:38', '247', '2023-12-22 00:17:38', b'0', 1), (260, NULL, '$2a$04$anryZQ1C2EB73TqatKmfn.6Z3sdJkcdJ.QJECcnLc9c3L8qH3cJdi', 0, '127.0.0.1', 20, '127.0.0.1', '2023-12-22 00:18:39', '土豆233', 'http://127.0.0.1:48080/admin-api/infra/file/4/get/49009b73d392657c85bed2e82e22b09720e7c68466b8565c8b2f6c00e03b705e.jpg', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, '247', '2023-12-22 00:18:39', '247', '2023-12-22 00:18:39', b'0', 1), (261, NULL, '$2a$04$SNcGXnXveFnMLRw.CararucpycSGYYxAji.fNrnlesDouvEvvooCe', 0, '127.0.0.1', 20, '127.0.0.1', '2023-12-22 00:19:03', '土豆233', 'http://127.0.0.1:48080/admin-api/infra/file/4/get/49009b73d392657c85bed2e82e22b09720e7c68466b8565c8b2f6c00e03b705e.jpg', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, '247', '2023-12-22 00:19:03', '247', '2023-12-22 00:19:03', b'0', 1), (262, NULL, '$2a$04$24JHBKIQlB4NzW0pp5chGOPIIr2HVkp.1Fk9vWZ00y5qvMOZ2aeky', 0, '127.0.0.1', 20, '127.0.0.1', '2023-12-23 09:51:46', '土豆233', 'http://127.0.0.1:48080/admin-api/infra/file/4/get/49009b73d392657c85bed2e82e22b09720e7c68466b8565c8b2f6c00e03b705e.jpg', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, '247', '2023-12-23 09:51:46', '247', '2023-12-23 09:51:46', b'0', 1), (273, NULL, '$2a$04$eEAnFbJw5/t90hBI25MqGerMxEJp0sKT7lduLDk3o0AzOP0eK9jpi', 0, '127.0.0.1', 20, '127.0.0.1', '2023-12-23 19:29:40', '用户315213', '', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, '247', '2023-12-23 17:39:53', '247', '2023-12-23 19:29:40', b'0', 1), (274, NULL, '$2a$04$oJqlMrtCGLxNlauBw98rG.YygC8CH2E1A6PNIA9F5.AbzAtBdMZym', 0, '127.0.0.1', 20, '127.0.0.1', '2023-12-23 21:23:19', '芋艿（一种食材）', 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTJsb6yF8nF3I1MtFk8KFcYyFHunpicwZLOxCLFy0WfibgH9Lt4LjKOfKGd5NV7BRwcByCHkj7n3xf7w/132', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, '247', '2023-12-23 21:23:19', '247', '2023-12-23 21:23:19', b'0', 1), (275, NULL, '$2a$04$iJ0Pnshm0/FlyT3w6Ggmeu/1FVpHzuPpC/rsi6pluNHyB2BYdQi3y', 0, '127.0.0.1', 20, '127.0.0.1', '2023-12-23 21:39:58', '芋艿（一种食材）', 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTJsb6yF8nF3I1MtFk8KFcYyFHunpicwZLOxCLFy0WfibgH9Lt4LjKOfKGd5NV7BRwcByCHkj7n3xf7w/132', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, '247', '2023-12-23 21:39:58', '247', '2023-12-23 21:39:58', b'0', 1), (276, NULL, '$2a$04$yZIxDQxy6Ivzg64D8rpW4.p.TtCAFjZpzlkH25EW7yoBqOj4/lk8e', 0, '127.0.0.1', 20, '127.0.0.1', '2023-12-23 22:27:10', '芋艿（一种食材）', 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTJsb6yF8nF3I1MtFk8KFcYyFHunpicwZLOxCLFy0WfibgH9Lt4LjKOfKGd5NV7BRwcByCHkj7n3xf7w/132', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, '247', '2023-12-23 22:27:10', '247', '2023-12-23 22:27:10', b'0', 1), (277, NULL, '$2a$04$N3SkOzxjWAmDXoDOrYqnXe/4Cs/7JwL3B3qR/yS2OMbJhIkeqIq46', 0, '127.0.0.1', 20, '114.84.176.64', '2023-12-24 10:50:12', '芋艿（一种食材）', 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTKMezSxtOImrC9lbhwHiazYwck3xwrEcO7VJfG6WQo260whaeVNoByE5RreiaGsGfOMlIiaDhSaA991w/132', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, '247', '2023-12-24 10:15:38', '247', '2023-12-24 10:50:12', b'0', 1), (278, NULL, '$2a$04$jQD8S2yqATWhhLfnytZJ6eRUU2OnnpTuk6mD2obv0GsKuttgApWTG', 0, '114.84.176.64', 20, '114.84.176.64', '2023-12-24 11:48:52', '用户531758', '', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, '247', '2023-12-24 11:48:52', '247', '2023-12-24 11:48:52', b'0', 1), (279, NULL, '$2a$04$oPpxJc6hscS06.PN2HdnFuboaYikUh1iJ8.O5CeBHYmexINBMSKG6', 0, '114.84.176.64', 20, '114.84.176.64', '2023-12-24 13:08:03', '用户351537', '', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, '247', '2023-12-24 13:08:03', '247', '2023-12-24 13:08:03', b'0', 1), (280, NULL, '$2a$04$i1/ib1XW08U78Z6Jt5H27e4i3lWJJVS2Z7eJFkBa/nrYNlTZRnky2', 0, '114.84.176.64', 20, '114.84.176.64', '2023-12-24 20:10:34', '芋艿（一种食材）', 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTKMezSxtOImrC9lbhwHiazYwck3xwrEcO7VJfG6WQo260whaeVNoByE5RreiaGsGfOMlIiaDhSaA991w/132', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, '247', '2023-12-24 20:10:34', '247', '2023-12-24 20:10:34', b'0', 1), (281, '15601691305', '$2a$04$3pwBg7WrEUPL17i1yVZH4uKFxyt/5ptzZLnqvSrP/RC2/L7kE5MIe', 0, '127.0.0.1', 20, '127.0.0.1', '2023-12-28 22:51:07', '用户6822470', '', '', 1, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, '2023-12-28 22:51:07', '281', '2023-12-28 22:51:33', b'0', 1), (282, '15601691382', '$2a$04$fiRnifjyMsVVnXHatJiDwOdIUbjw5OoLcerWRg9p7J/VPty5c4TsC', 0, '127.0.0.1', 20, '127.0.0.1', '2023-12-28 23:19:09', '芋艿（一种食材）', 'https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTJsb6yF8nF3I1MtFk8KFcYyFHunpicwZLOxCLFy0WfibgH9Lt4LjKOfKGd5NV7BRwcByCHkj7n3xf7w/132', '', 0, NULL, NULL, NULL, 0, NULL, NULL, 0, NULL, NULL, '2023-12-28 23:19:09', '282', '2023-12-28 23:19:28', b'0', 1), (283, '18818260278', '$2a$04$B8NUowhn9Cj6vAW8rVOR4e3YDMaLirhdH7s/rB0XabxB9E6i/yXb6', 0, '127.0.0.1', 20, '127.0.0.1', '2024-01-17 14:40:23', '用户991818', 'http://test.yudao.iocoder.cn/51b284275ebabaec210a1dc6f92cfc7a63ff8509db8298bc49d77cd0f299fe7b.jpg', '', 0, NULL, NULL, NULL, 9, NULL, 1, 300, NULL, NULL, '2024-01-17 14:40:23', NULL, '2024-01-17 14:42:00', b'0', 1), (284, '15601691310', '$2a$04$wxq7x2Ae0Vqu5kXyHWlIVeNq6oqRiWGZ3RJrnzS3HCKHTFvhPmOg6', 0, '127.0.0.1', 20, '127.0.0.1', '2024-01-17 15:16:12', '我是一个团长', 'http://test.yudao.iocoder.cn/a00353eb8aec04053fde8887802f856625b8210b66e948dffd9da21bdda28ef2.png', '', 0, NULL, NULL, NULL, 9, NULL, 1, 300, NULL, NULL, '2024-01-17 15:16:12', NULL, '2024-01-17 15:24:09', b'0', 1), (285, '15601691323', '$2a$04$xTYFGI/RQ2P0uvR0N9xFXuLwubffpAt6RMvyg0S2zsaoJtIuKVkBK', 0, '127.0.0.1', 20, '127.0.0.1', '2024-01-17 15:41:01', '我是噶团员', 'http://test.yudao.iocoder.cn/524fb8e6aa7060ff1b3a4cf32805b906fc68f748cfab389c0efd5b5336c6a401.jpg', '', 0, NULL, NULL, NULL, 9, NULL, 1, 300, NULL, NULL, '2024-01-17 15:41:01', NULL, '2024-01-17 15:45:36', b'0', 1);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
