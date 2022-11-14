/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1 MySQL
 Source Server Type    : MySQL
 Source Server Version : 80026
 Source Host           : localhost:3306
 Source Schema         : ruoyi-vue-pro

 Target Server Type    : MySQL
 Target Server Version : 80026
 File Encoding         : 65001

 Date: 01/08/2022 23:01:36
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for market_activity
-- ----------------------------
DROP TABLE IF EXISTS `market_activity`;
CREATE TABLE `market_activity`  (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动编号',
                                    `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '活动标题',
                                    `activity_type` tinyint NOT NULL COMMENT '活动类型',
                                    `status` tinyint NOT NULL DEFAULT -1 COMMENT '活动状态',
                                    `start_time` datetime NOT NULL COMMENT '开始时间',
                                    `end_time` datetime NOT NULL COMMENT '结束时间',
                                    `invalid_time` datetime NULL DEFAULT NULL COMMENT '失效时间',
                                    `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
                                    `time_limited_discount` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '限制折扣字符串，使用 JSON 序列化成字符串存储',
                                    `full_privilege` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '限制折扣字符串，使用 JSON 序列化成字符串存储',
                                    `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
                                    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
                                    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '促销活动';

-- ----------------------------
-- Records of market_activity
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for market_banner
-- ----------------------------
DROP TABLE IF EXISTS `market_banner`;
CREATE TABLE `market_banner`  (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'Banner编号',
                                  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT 'Banner标题',
                                  `pic_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '图片URL',
                                  `status` tinyint NOT NULL DEFAULT -1 COMMENT '活动状态',
                                  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '跳转地址',
                                  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
                                  `sort` tinyint NULL DEFAULT NULL COMMENT '排序',
                                  `memo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
                                  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Banner管理';

-- ----------------------------
-- Records of market_banner
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for member_address
-- ----------------------------
DROP TABLE IF EXISTS `member_address`;
CREATE TABLE `member_address`  (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收件地址编号',
                                   `user_id` bigint NOT NULL COMMENT '用户编号',
                                   `name` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收件人名称',
                                   `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
                                   `area_id` bigint NOT NULL COMMENT '地区编码',
                                   `post_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮编',
                                   `detail_address` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收件详细地址',
                                   `defaulted` bit(1) NOT NULL COMMENT '是否默认',
                                   `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                   `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   INDEX `idx_userId`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户收件地址';

-- ----------------------------
-- Records of member_address
-- ----------------------------
BEGIN;
INSERT INTO `member_address` (`id`, `user_id`, `name`, `mobile`, `area_id`, `post_code`, `detail_address`, `defaulted`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES (21, 1, 'yunai', '15601691300', 610632, '200000', '芋道源码 233 号 666 室', b'1', '1', '2022-08-01 22:46:35', '1', '2022-08-01 22:46:35', b'0', 1);
COMMIT;

-- ----------------------------
-- Table structure for product_brand
-- ----------------------------
DROP TABLE IF EXISTS `product_brand`;
CREATE TABLE `product_brand`  (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '品牌编号',
                                  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '品牌名称',
                                  `pic_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '品牌图片',
                                  `sort` int NULL DEFAULT 0 COMMENT '品牌排序',
                                  `description` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '品牌描述',
                                  `status` tinyint NOT NULL COMMENT '状态',
                                  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
                                  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品品牌';

-- ----------------------------
-- Records of product_brand
-- ----------------------------
BEGIN;
INSERT INTO `product_brand` (`id`, `name`, `pic_url`, `sort`, `description`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES (1, '苹果', 'http://test.yudao.iocoder.cn/e3726713fa56db5717c78c011762fcc7a251db12735c3581470638b8e1fa17e2.jpeg', 0, '是上市', 0, '1', '2022-07-30 22:12:18', '1', '2022-07-30 22:13:55', b'0', 1);
COMMIT;

-- ----------------------------
-- Table structure for product_category
-- ----------------------------
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category`  (
                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类编号',
                                     `parent_id` bigint NOT NULL COMMENT '父分类编号',
                                     `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
                                     `pic_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类图片',
                                     `sort` int NULL DEFAULT 0 COMMENT '分类排序',
                                     `description` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类描述',
                                     `status` tinyint NOT NULL COMMENT '开启状态',
                                     `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
                                     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
                                     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                     `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
                                     PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品分类';

-- ----------------------------
-- Records of product_category
-- ----------------------------
BEGIN;
INSERT INTO `product_category` (`id`, `parent_id`, `name`, `pic_url`, `sort`, `description`, `status`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `tenant_id`) VALUES (1, 0, '电脑办公', 'http://test.yudao.iocoder.cn/122d548e1b3cd5dec72fe8075c6977a70f9cc13541a684ab3685f1b5df42f6bd.jpeg', 1, '1234', 0, '1', '2022-07-30 16:36:35', '1', '2022-07-30 20:27:16', b'0', 1), (2, 1, '笔记本', 'http://test.yudao.iocoder.cn/72713ac7b947600a019a18786ed0e6562e8692e253dbd35110a0a85c2469bbec.jpg', 1, '<p>测试一下</p>', 0, '1', '2022-07-30 16:38:09', '1', '2022-07-30 16:38:09', b'0', 1), (3, 1, '游戏本', 'http://test.yudao.iocoder.cn/287c50dd9f5f575f57329a0c57b2095be6d1aeba83867b905fe549f54a296feb.jpg', 2, '<p>测试一下</p>', 0, '1', '2022-07-30 16:39:09', '1', '2022-07-30 20:26:59', b'0', 1), (4, 0, '手机', 'http://test.yudao.iocoder.cn/e1b63900c78dbb661b3e383960cee5cfea7e1dd2fb22cff2e317ff025faaf8b2.jpeg', 2, '<p>123</p>', 0, '1', '2022-07-30 16:40:00', '1', '2022-07-30 16:40:09', b'0', 1), (5, 4, '5G手机', 'http://test.yudao.iocoder.cn/3af6557ac7def6423f046f5b2e920b644793420b466959aaa996a2e19068bbde.jpeg', 1, '<p><br></p>', 0, '1', '2022-07-30 16:43:00', '1', '2022-07-30 16:43:00', b'0', 1), (6, 4, '游戏手机', 'http://test.yudao.iocoder.cn/964fe9ccd1710d64ede261dc36d231918a017641986c15293c367f9f66d94d05.jpeg', 2, NULL, 0, '1', '2022-07-30 16:43:44', '1', '2022-07-30 16:43:44', b'0', 1), (7, 5, '厉害的 5G 手机', 'http://test.yudao.iocoder.cn/b287122f277838e8de368769b96217918605743bc45f3a29bda3cc7359dc66e1.png', 0, '123', 0, '1', '2022-07-30 20:38:09', '1', '2022-07-30 20:38:09', b'0', 1);
COMMIT;

-- ----------------------------
-- Table structure for product_property
-- ----------------------------
DROP TABLE IF EXISTS `product_property`;
CREATE TABLE `product_property`  (
                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                     `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '规格名称',
                                     `status` tinyint NULL DEFAULT NULL COMMENT '状态： 0 开启 ，1 禁用',
                                     `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
                                     `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
                                     `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
                                     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                     PRIMARY KEY (`id`) USING BTREE,
                                     INDEX `idx_name`(`name`(32) ASC) USING BTREE COMMENT '规格名称索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '规格名称';

-- ----------------------------
-- Records of product_property
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for product_property_value
-- ----------------------------
DROP TABLE IF EXISTS `product_property_value`;
CREATE TABLE `product_property_value`  (
                                           `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                           `property_id` bigint NULL DEFAULT NULL COMMENT '规格键id',
                                           `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '规格值名字',
                                           `status` tinyint NULL DEFAULT NULL COMMENT '状态： 1 开启 ，2 禁用',
                                           `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                           `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                           `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
                                           `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
                                           `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
                                           `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                           PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '规格值';

-- ----------------------------
-- Records of product_property_value
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for product_sku
-- ----------------------------
DROP TABLE IF EXISTS `product_sku`;
CREATE TABLE `product_sku` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                               `spu_id` bigint NOT NULL COMMENT 'spu编号',
                               `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                               `name` varchar(128)  DEFAULT NULL COMMENT '商品 SKU 名字',
                               `properties` varchar(128)  DEFAULT NULL COMMENT '规格值数组-json格式， [{propertId: , valueId: }, {propertId: , valueId: }]',
                               `price` int NOT NULL DEFAULT '-1' COMMENT '销售价格，单位：分',
                               `market_price` int DEFAULT NULL COMMENT '市场价',
                               `cost_price` int NOT NULL DEFAULT '-1' COMMENT '成本价，单位： 分',
                               `pic_url` varchar(128)  NOT NULL COMMENT '图片地址',
                               `stock` int DEFAULT NULL COMMENT '库存',
                               `warn_stock` int DEFAULT NULL COMMENT '预警库存',
                               `volume` double DEFAULT NULL COMMENT '商品体积',
                               `weight` double DEFAULT NULL COMMENT '商品重量',
                               `bar_code` varchar(64)  DEFAULT NULL COMMENT '条形码',
                               `status` tinyint DEFAULT NULL COMMENT '状态： 0-正常 1-禁用',
                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                               `updater` double(64,0) DEFAULT NULL COMMENT '更新人',
`deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='商品sku';

-- ----------------------------
-- Records of product_sku
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for product_spu
-- ----------------------------
DROP TABLE IF EXISTS `product_spu`;
CREATE TABLE `product_spu` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                               `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                               `brand_id` bigint DEFAULT NULL COMMENT '商品品牌编号',
                               `category_id` bigint NOT NULL COMMENT '分类id',
                               `spec_type` int NOT NULL COMMENT '规格类型：0 单规格 1 多规格',
                               `code` varchar(128)  DEFAULT NULL COMMENT '商品编码',
                               `name` varchar(128)  NOT NULL COMMENT '商品名称',
                               `sell_point` varchar(128)  DEFAULT NULL COMMENT '卖点',
                               `description` text  COMMENT '描述',
                               `pic_urls` varchar(1024)  DEFAULT '' COMMENT '商品轮播图地址\n 数组，以逗号分隔\n 最多上传15张',
                               `video_url` varchar(128)  DEFAULT NULL COMMENT '商品视频',
                               `market_price` int DEFAULT NULL COMMENT '市场价，单位使用：分',
                               `min_price` int DEFAULT NULL COMMENT '最小价格，单位使用：分',
                               `max_price` int DEFAULT NULL COMMENT '最大价格，单位使用：分',
                               `total_stock` int NOT NULL DEFAULT '0' COMMENT '总库存',
                               `show_stock` int DEFAULT '0' COMMENT '是否展示库存',
                               `sales_count` int DEFAULT '0' COMMENT '商品销量',
                               `virtual_sales_count` int DEFAULT '0' COMMENT '虚拟销量',
                               `click_count` int DEFAULT '0' COMMENT '商品点击量',
                               `status` bit(1) DEFAULT NULL COMMENT '上下架状态： 0 上架（开启） 1 下架（禁用）-1 回收',
                               `sort` int NOT NULL DEFAULT '0' COMMENT '排序字段',
                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `creator` varchar(64)  DEFAULT NULL COMMENT '创建人',
                               `updater` varchar(64)  DEFAULT NULL COMMENT '更新人',
                               `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                               PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='商品spu';

-- ----------------------------
-- Records of product_spu
-- ----------------------------
BEGIN;
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2000, '商品中心', '', 1, 60, 0, '/product', 'merchant', NULL, 0, b'1', b'1', '', '2022-07-29 15:53:53', '1', '2022-07-30 22:26:19', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2002, '商品分类', '', 2, 2, 2000, 'category', 'dict', 'mall/product/category/index', 0, b'1', b'1', '', '2022-07-29 15:53:53', '1', '2022-07-30 22:23:37', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2003, '分类查询', 'product:category:query', 3, 1, 2002, '', '', '', 0, b'1', b'1', '', '2022-07-29 15:53:53', '', '2022-07-29 15:53:53', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2004, '分类创建', 'product:category:create', 3, 2, 2002, '', '', '', 0, b'1', b'1', '', '2022-07-29 15:53:53', '', '2022-07-29 15:53:53', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2005, '分类更新', 'product:category:update', 3, 3, 2002, '', '', '', 0, b'1', b'1', '', '2022-07-29 15:53:53', '', '2022-07-29 15:53:53', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2006, '分类删除', 'product:category:delete', 3, 4, 2002, '', '', '', 0, b'1', b'1', '', '2022-07-29 15:53:53', '', '2022-07-29 15:53:53', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2007, '分类导出', 'product:category:export', 3, 5, 2002, '', '', '', 0, b'1', b'1', '', '2022-07-29 15:53:53', '', '2022-07-30 13:52:13', b'1');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2008, '商品品牌', '', 2, 1, 2000, 'brand', 'dashboard', 'mall/product/brand/index', 0, b'1', b'1', '', '2022-07-30 13:52:44', '1', '2022-07-30 22:23:43', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2009, '品牌查询', 'product:brand:query', 3, 1, 2008, '', '', '', 0, b'1', b'1', '', '2022-07-30 13:52:44', '', '2022-07-30 13:52:44', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2010, '品牌创建', 'product:brand:create', 3, 2, 2008, '', '', '', 0, b'1', b'1', '', '2022-07-30 13:52:44', '', '2022-07-30 13:52:44', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2011, '品牌更新', 'product:brand:update', 3, 3, 2008, '', '', '', 0, b'1', b'1', '', '2022-07-30 13:52:44', '', '2022-07-30 13:52:44', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2012, '品牌删除', 'product:brand:delete', 3, 4, 2008, '', '', '', 0, b'1', b'1', '', '2022-07-30 13:52:44', '', '2022-07-30 13:52:44', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2013, '品牌导出', 'product:brand:export', 3, 5, 2008, '', '', '', 0, b'1', b'1', '', '2022-07-30 13:52:44', '', '2022-07-30 14:15:00', b'1');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2014, '商品管理', '', 2, 0, 2000, 'spu', 'link', 'mall/product/spu/index', 0, b'1', b'1', '', '2022-07-30 14:22:58', '1', '2022-07-30 22:26:35', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2015, '商品查询', 'product:spu:query', 3, 1, 2014, '', '', '', 0, b'1', b'1', '', '2022-07-30 14:22:58', '', '2022-07-30 14:22:58', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2016, '商品创建', 'product:spu:create', 3, 2, 2014, '', '', '', 0, b'1', b'1', '', '2022-07-30 14:22:58', '', '2022-07-30 14:22:58', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2017, '商品更新', 'product:spu:update', 3, 3, 2014, '', '', '', 0, b'1', b'1', '', '2022-07-30 14:22:58', '', '2022-07-30 14:22:58', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2018, '商品删除', 'product:spu:delete', 3, 4, 2014, '', '', '', 0, b'1', b'1', '', '2022-07-30 14:22:58', '', '2022-07-30 14:22:58', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2019, '规格管理', '', 2, 3, 2000, 'property', '', 'mall/product/property/index', 0, b'1', b'1', '', '2022-08-01 14:55:35', '', '2022-08-01 14:55:35', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2020, '规格查询', 'product:property:query', 3, 1, 2019, '', '', '', 0, b'1', b'1', '', '2022-08-01 14:55:35', '', '2022-08-01 14:55:35', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2021, '规格创建', 'product:property:create', 3, 2, 2019, '', '', '', 0, b'1', b'1', '', '2022-08-01 14:55:35', '', '2022-08-01 14:55:35', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2022, '规格更新', 'product:property:update', 3, 3, 2019, '', '', '', 0, b'1', b'1', '', '2022-08-01 14:55:35', '', '2022-08-01 14:55:35', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2023, '规格删除', 'product:property:delete', 3, 4, 2019, '', '', '', 0, b'1', b'1', '', '2022-08-01 14:55:35', '', '2022-08-01 14:55:35', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2024, '规格导出', 'product:property:export', 3, 5, 2019, '', '', '', 0, b'1', b'1', '', '2022-08-01 14:55:35', '', '2022-08-01 14:55:35', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2025, 'Banner管理', '', 2, 1, 2000, 'banner', '', 'mall/market/banner/index', 0, b'1', b'1', '', '2022-08-01 14:56:14', '', '2022-08-01 14:56:14', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2026, 'Banner查询', 'market:banner:query', 3, 1, 2025, '', '', '', 0, b'1', b'1', '', '2022-08-01 14:56:14', '', '2022-08-01 14:56:14', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2027, 'Banner创建', 'market:banner:create', 3, 2, 2025, '', '', '', 0, b'1', b'1', '', '2022-08-01 14:56:14', '', '2022-08-01 14:56:14', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2028, 'Banner更新', 'market:banner:update', 3, 3, 2025, '', '', '', 0, b'1', b'1', '', '2022-08-01 14:56:14', '', '2022-08-01 14:56:14', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2029, 'Banner删除', 'market:banner:delete', 3, 4, 2025, '', '', '', 0, b'1', b'1', '', '2022-08-01 14:56:14', '', '2022-08-01 14:56:14', b'0');

