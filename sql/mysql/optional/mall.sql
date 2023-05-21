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
                                    `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '活动标题',
                                    `activity_type` tinyint NOT NULL COMMENT '活动类型',
                                    `status` tinyint NOT NULL DEFAULT -1 COMMENT '活动状态',
                                    `start_time` datetime NOT NULL COMMENT '开始时间',
                                    `end_time` datetime NOT NULL COMMENT '结束时间',
                                    `invalid_time` datetime NULL DEFAULT NULL COMMENT '失效时间',
                                    `delete_time` datetime NULL DEFAULT NULL COMMENT '删除时间',
                                    `time_limited_discount` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限制折扣字符串，使用 JSON 序列化成字符串存储',
                                    `full_privilege` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '限制折扣字符串，使用 JSON 序列化成字符串存储',
                                    `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
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
                                  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT 'Banner标题',
                                  `pic_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片URL',
                                  `status` tinyint NOT NULL DEFAULT -1 COMMENT '活动状态',
                                  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '跳转地址',
                                  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
                                  `sort` tinyint NULL DEFAULT NULL COMMENT '排序',
                                  `memo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
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
                                   `name` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '收件人名称',
                                   `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '手机号',
                                   `area_id` bigint NOT NULL COMMENT '地区编码',
                                   `post_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '邮编',
                                   `detail_address` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '收件详细地址',
                                   `defaulted` bit(1) NOT NULL COMMENT '是否默认',
                                   `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                   `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   INDEX `idx_userId`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '用户收件地址';

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
CREATE TABLE `product_property` (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                    `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '规格名称',
                                    `status` tinyint DEFAULT NULL COMMENT '状态： 0 开启 ，1 禁用',
                                    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
                                    `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人',
                                    `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                    `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
                                    PRIMARY KEY (`id`) USING BTREE,
                                    KEY `idx_name` (`name`(32)) USING BTREE COMMENT '规格名称索引'
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='规格名称';

-- ----------------------------
-- Records of product_property
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for product_property_value
-- ----------------------------
DROP TABLE IF EXISTS `product_property_value`;
CREATE TABLE `product_property_value` (
                                          `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                          `property_id` bigint DEFAULT NULL COMMENT '规格键id',
                                          `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '规格值名字',
                                          `status` tinyint DEFAULT NULL COMMENT '状态： 1 开启 ，2 禁用',
                                          `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                          `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
                                          `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人',
                                          `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                          `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                          `remark` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
                                          PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='规格值';

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
                               `properties` varchar(512) DEFAULT NULL COMMENT '属性数组，JSON 格式 [{propertId: , valueId: }, {propertId: , valueId: }]',
                               `price` int NOT NULL DEFAULT '-1' COMMENT '商品价格，单位：分',
                               `market_price` int DEFAULT NULL COMMENT '市场价，单位：分',
                               `cost_price` int NOT NULL DEFAULT '-1' COMMENT '成本价，单位： 分',
                               `bar_code` varchar(64)  DEFAULT NULL COMMENT 'SKU 的条形码',
                               `pic_url` varchar(256)  NOT NULL COMMENT '图片地址',
                               `stock` int DEFAULT NULL COMMENT '库存',
                               `weight` double DEFAULT NULL COMMENT '商品重量，单位：kg 千克',
                               `volume` double DEFAULT NULL COMMENT '商品体积，单位：m^3 平米',
                               `sub_commission_first_price` int DEFAULT NULL COMMENT '一级分销的佣金，单位：分',
                               `sub_commission_second_price` int DEFAULT NULL COMMENT '二级分销的佣金，单位：分',
                               `sales_count` int DEFAULT NULL COMMENT '商品销量',
                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
                               `updater` double(64,0) DEFAULT NULL COMMENT '更新人',
                               `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                               `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
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
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品 SPU 编号，自增',
                               `name` varchar(128)  NOT NULL COMMENT '商品名称', 
                               `keyword` varchar(256)  DEFAULT NULL COMMENT '关键字',
                               `introduction` varchar(256)  COMMENT '商品简介',
                               `description` text  COMMENT '商品详情', 
                               `bar_code` varchar(64) DEFAULT NULL COMMENT '条形码',
                               `category_id` bigint NOT NULL COMMENT '商品分类编号', 
                               `brand_id` int DEFAULT NULL COMMENT '商品品牌编号',
                               `pic_url` varchar(256)  NOT NULL COMMENT '商品封面图',
                               `slider_pic_urls` varchar(2000)  DEFAULT '' COMMENT '商品轮播图地址\n 数组，以逗号分隔\n 最多上传15张',
                               `video_url` varchar(256)  DEFAULT NULL COMMENT '商品视频', 
                               `unit` tinyint NOT NULL COMMENT  '单位',
                               `sort` int NOT NULL DEFAULT '0' COMMENT '排序字段',
                               `status` tinyint NOT NULL COMMENT '商品状态: 0 上架（开启） 1 下架（禁用）-1 回收',
                               `spec_type` bit(1) DEFAULT NULL COMMENT '规格类型：0 单规格 1 多规格',
                               `price` int NOT NULL DEFAULT '-1' COMMENT '商品价格，单位使用：分',
                               `market_price` int DEFAULT NULL COMMENT '市场价，单位使用：分',
                               `cost_price` int NOT NULL DEFAULT '-1' COMMENT '成本价，单位： 分',
                               `stock` int NOT NULL DEFAULT '0' COMMENT '库存',
                               `delivery_template_id` bigint NOT NULL COMMENT '物流配置模板编号',
                               `recommend_hot` bit(1) DEFAULT NULL COMMENT '是否热卖推荐: 0 默认 1 热卖',
                               `recommend_benefit` bit(1) DEFAULT NULL COMMENT '是否优惠推荐: 0 默认 1 优选',
                               `recommend_best` bit(1) DEFAULT NULL COMMENT '是否精品推荐: 0 默认 1 精品',
                               `recommend_new` bit(1) DEFAULT NULL COMMENT '是否新品推荐: 0 默认 1 新品',
                               `recommend_good` bit(1) DEFAULT NULL COMMENT '是否优品推荐',
                               `give_integral` int NOT NULL COMMENT '赠送积分',
                               `give_coupon_template_ids` varchar(512) DEFAULT '' COMMENT '赠送的优惠劵编号的数组',
                               `sub_commission_type` bit(1) DEFAULT NULL COMMENT '分销类型',
                               `activity_orders` varchar(16) NOT NULL DEFAULT '' COMMENT '活动显示排序0=默认, 1=秒杀，2=砍价，3=拼团',
                               `sales_count` int DEFAULT '0' COMMENT '商品销量',
                               `virtual_sales_count` int DEFAULT '0' COMMENT '虚拟销量',
                               `browse_count` int DEFAULT '0' COMMENT '商品点击量',
                               `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `creator` varchar(64)  DEFAULT NULL COMMENT '创建人',
                               `updater` varchar(64)  DEFAULT NULL COMMENT '更新人',
                               `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除', 
                               `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                               PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='商品spu';

-- ----------------------------
-- Table structure for product_favorite
-- ----------------------------
DROP TABLE IF EXISTS `product_favorite`;
CREATE TABLE `product_favorite` (
   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号，自增',
   `spu_id` bigint NOT NULL COMMENT '商品 SPU 编号',
   `user_id` bigint NOT NULL COMMENT '用户id',
   `type` int(10) NOT NULL DEFAULT 1 COMMENT '类型1:收藏 2:点赞',
   `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
   `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='商品收藏表';

-- Table structure for trade_delivery_express_template
-- ----------------------------
DROP TABLE IF EXISTS `trade_delivery_express_template`;
CREATE TABLE `trade_delivery_express_template` (
   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号，自增',
   `name` varchar(64)  NOT NULL COMMENT '模板名称',
   `charge_mode` tinyint NOT NULL DEFAULT 1 COMMENT '配送计费方式 1:按件 2:按重量 3:按体积',
   `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
   `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
   `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='快递运费模板';

-- ----------------------------
-- Table structure for trade_delivery_express_template_free
-- ----------------------------
DROP TABLE IF EXISTS `trade_delivery_express_template_free`;
CREATE TABLE `trade_delivery_express_template_free` (
   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号，自增',
   `template_id` bigint NOT NULL COMMENT '配送模板编号, 对应delivery_template表id',
   `area_id` int NOT NULL  COMMENT '包邮区域id',
   `free_price` int NOT NULL  COMMENT '包邮金额(单位分) 订单总金额>包邮金额才免运费',
   `free_count` int NOT NULL DEFAULT 0 COMMENT '包邮件数,订单总件数>包邮件数才免运费',
   `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
   `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='快递运费模板包邮配置';

-- ----------------------------
-- Table structure for trade_delivery_express_template_charge
-- ----------------------------
DROP TABLE IF EXISTS `trade_delivery_express_template_charge`;
CREATE TABLE `trade_delivery_express_template_charge` (
   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号，自增',
   `template_id` bigint NOT NULL COMMENT '配送模板编号, 对应delivery_template表id',
   `area_id` int NOT NULL  COMMENT '配送区域id 1:适用于全国',
   `charge_mode` tinyint NOT NULL  COMMENT '配送计费方式 1:按件 2:按重量 3:按体积',
   `start_count` double NOT NULL  COMMENT '首件数量(件数,重量，或体积)',
   `start_price`  int NOT NULL  COMMENT '起步价(单位分)',
   `extra_count` double NOT NULL  COMMENT '续件数量(件,重量，或体积)',
   `extra_price`  int NOT NULL  COMMENT '额外价(单位分)',
   `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
   `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='快递运费模板计费配置';

-- ----------------------------
-- Table structure for trade_delivery_pick_up_store
-- ----------------------------
DROP TABLE IF EXISTS `trade_delivery_pick_up_store`;
CREATE TABLE `trade_delivery_pick_up_store` (
   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号，自增',
   `name` varchar(64)  NOT NULL COMMENT '门店名称',
   `introduction` varchar(256)  COMMENT '门店简介',
   `phone` varchar(16) NOT NULL COMMENT '门店手机',
   `area_id` int NOT NULL  COMMENT '区域id',
   `detail_address` varchar(256) NOT NULL COMMENT '门店详细地址',
   `logo` varchar(256) NOT NULL COMMENT '门店logo',
   `opening_time` time NOT NULL  COMMENT '营业开始时间',
   `closing_time` time NOT NULL  COMMENT '营业结束时间',
   `latitude`  varchar(128)  NOT NULL  COMMENT '纬度',
   `longitude` varchar(128) NOT NULL  COMMENT '经度',
   `status` tinyint NOT NULL DEFAULT 0 COMMENT '门店状态（0正常 1停用）',
   `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
   `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='自提门店';

-- ----------------------------
-- Table structure for trade_delivery_pick_up_store_staff
-- ----------------------------
DROP TABLE IF EXISTS `trade_delivery_pick_up_store_staff`;
CREATE TABLE `trade_delivery_pick_up_store_staff` (
   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号，自增',
    store_id bigint NOT NULL  COMMENT '自提门店编号',
   `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
   `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
   `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='自提门店店员';

-- ----------------------------
-- Table structure for trade_delivery_pick_up_store_staff
-- ----------------------------
DROP TABLE IF EXISTS `trade_delivery_pick_up_store_staff`;
CREATE TABLE `trade_delivery_pick_up_store_staff` (
   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号，自增',
   `admin_user_id` bigint  NOT NULL COMMENT '管理员用户id',
    store_id bigint NOT NULL  COMMENT '自提门店编号',
   `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
   `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
   `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='自提门店店员';


-- ----------------------------
-- Table structure for trade_delivery_express
-- ----------------------------
DROP TABLE IF EXISTS `trade_delivery_express`;
CREATE TABLE `trade_delivery_express` (
   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号，自增',
   `code` varchar(64) NOT NULL  COMMENT '快递公司编号',
   `name` varchar(64) NOT NULL  COMMENT '快递公司名称',
   `logo` varchar(256) COMMENT '快递公司logo',
   `sort` int NOT NULL DEFAULT 0 COMMENT '排序',
   `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0正常 1停用）',
   `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
   `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='快递公司';

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
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2025, 'Banner管理', '', 2, 1, 2000, 'brand', '', 'mall/market/banner/index', 0, b'1', b'1', '', '2022-08-01 14:56:14', '', '2022-08-01 14:56:14', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2026, 'Banner查询', 'market:banner:query', 3, 1, 2025, '', '', '', 0, b'1', b'1', '', '2022-08-01 14:56:14', '', '2022-08-01 14:56:14', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2027, 'Banner创建', 'market:banner:create', 3, 2, 2025, '', '', '', 0, b'1', b'1', '', '2022-08-01 14:56:14', '', '2022-08-01 14:56:14', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2028, 'Banner更新', 'market:banner:update', 3, 3, 2025, '', '', '', 0, b'1', b'1', '', '2022-08-01 14:56:14', '', '2022-08-01 14:56:14', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu` (`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`, `visible`, `keep_alive`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2029, 'Banner删除', 'market:banner:delete', 3, 4, 2025, '', '', '', 0, b'1', b'1', '', '2022-08-01 14:56:14', '', '2022-08-01 14:56:14', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu`(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2164, '配送管理', '', 1, 0, 2072, 'delivery', '', '', '', 0, b'1', b'1', b'1', '1', '2023-05-18 09:18:02', '1', '2023-05-18 09:48:48', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu`(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2165, '快递发货', '', 1, 0, 2164, 'express', '', '', '', 0, b'1', b'1', b'1', '1', '2023-05-18 09:22:06', '1', '2023-05-18 09:22:06', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu`(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2166, '门店自提', '', 1, 1, 2164, 'pick-up-store', '', '', '', 0, b'1', b'1', b'1', '1', '2023-05-18 09:23:14', '1', '2023-05-18 09:23:14', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu`(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2167, '快递公司', '', 2, 0, 2165, 'express', '', 'mall/trade/delivery/express/index', 'Express', 0, b'1', b'1', b'1', '1', '2023-05-18 09:27:21', '1', '2023-05-18 22:11:14', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu`(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2168, '快递公司查询', 'trade:delivery:express:query', 3, 1, 2167, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2023-05-18 09:37:53', '', '2023-05-18 09:37:53', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu`(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2169, '快递公司创建', 'trade:delivery:express:create', 3, 2, 2167, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2023-05-18 09:37:53', '', '2023-05-18 09:37:53', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu`(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2170, '快递公司更新', 'trade:delivery:express:update', 3, 3, 2167, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2023-05-18 09:37:53', '', '2023-05-18 09:37:53', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu`(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2171, '快递公司删除', 'trade:delivery:express:delete', 3, 4, 2167, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2023-05-18 09:37:53', '', '2023-05-18 09:37:53', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu`(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2172, '快递公司导出', 'trade:delivery:express:export', 3, 5, 2167, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2023-05-18 09:37:53', '', '2023-05-18 09:37:53', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu`(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2178, '快递运费模板导出', 'trade:delivery:express-template:export', 3, 5, 2173, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2023-05-20 06:49:53', '', '2023-05-20 06:49:53', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu`(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2177, '快递运费模板删除', 'trade:delivery:express-template:delete', 3, 4, 2173, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2023-05-20 06:49:53', '', '2023-05-20 06:49:53', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu`(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2176, '快递运费模板更新', 'trade:delivery:express-template:update', 3, 3, 2173, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2023-05-20 06:49:53', '', '2023-05-20 06:49:53', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu`(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2175, '快递运费模板创建', 'trade:delivery:express-template:create', 3, 2, 2173, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2023-05-20 06:49:53', '', '2023-05-20 06:49:53', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu`(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2174, '快递运费模板查询', 'trade:delivery:express-template:query', 3, 1, 2173, '', '', '', NULL, 0, b'1', b'1', b'1', '', '2023-05-20 06:49:53', '', '2023-05-20 06:49:53', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_menu`(`id`, `name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `component_name`, `status`, `visible`, `keep_alive`, `always_show`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (2173, '运费模版', 'trade:delivery:express-template:query', 2, 1, 2165, 'express-template', '', 'mall/trade/delivery/expressTemplate/index', 'ExpressTemplate', 0, b'1', b'1', b'1', '1', '2023-05-20 06:48:10', '1', '2023-05-20 06:48:29', b'0');

INSERT INTO `system_dict_data`(`sort`,`label`,`value`,`dict_type`,`status`,`color_type`,`css_class`,`remark`,`creator`,`create_time`,`updater`,`update_time`,`deleted`) VALUES
                                                                                                                                                                            (1,'打',2,'product_unit',0,'','','',1, NOW(),1, NOW(),0),
                                                                                                                                                                            (1,'盒',3,'product_unit',0,'','','',1, NOW(),1, NOW(),0),
                                                                                                                                                                            (1,'袋',4,'product_unit',0,'','','',1, NOW(),1, NOW(),0),
                                                                                                                                                                            (1,'箱',5,'product_unit',0,'','','',1, NOW(),1, NOW(),0),
                                                                                                                                                                            (1,'套',6,'product_unit',0,'','','',1, NOW(),1, NOW(),0),
                                                                                                                                                                            (1,'包',7,'product_unit',0,'','','',1, NOW(),1, NOW(),0),
                                                                                                                                                                            (1,'双',8,'product_unit',0,'','','',1, NOW(),1, NOW(),0),
                                                                                                                                                                            (1,'卷',9,'product_unit',0,'','','',1, NOW(),1, NOW(),0),
                                                                                                                                                                            (1,'张',10,'product_unit',0,'','','',1, NOW(),1, NOW(),0),
                                                                                                                                                                            (1,'克',11,'product_unit',0,'','','',1, NOW(),1, NOW(),0),
                                                                                                                                                                            (1,'千克',12,'product_unit',0,'','','',1, NOW(),1, NOW(),0),
                                                                                                                                                                            (1,'毫克',13,'product_unit',0,'','','',1, NOW(),1, NOW(),0),
                                                                                                                                                                            (1,'微克',14,'product_unit',0,'','','',1, NOW(),1, NOW(),0),
                                                                                                                                                                            (1,'吨',15,'product_unit',0,'','','',1, NOW(),1, NOW(),0),
                                                                                                                                                                            (1,'升',16,'product_unit',0,'','','',1, NOW(),1, NOW(),0),
                                                                                                                                                                            (1,'毫升',17,'product_unit',0,'','','',1, NOW(),1, NOW(),0),
                                                                                                                                                                            (1, '平方米', 18, 'product_unit', 0, '', '', '',  1, NOW(), 1, NOW(), 0),
                                                                                                                                                                            (1, '平方千米', 19, 'product_unit', 0, '', '', '',  1, NOW(), 1, NOW(), 0),
                                                                                                                                                                            (1, '平方英里', 20, 'product_unit', 0, '', '', '',  1, NOW(), 1, NOW(), 0),
                                                                                                                                                                            (1, '平方码', 21, 'product_unit', 0, '', '', '',  1, NOW(), 1, NOW(), 0),
                                                                                                                                                                            (1, '平方英尺', 22, 'product_unit', 0, '', '', '',  1, NOW(), 1, NOW(), 0),
                                                                                                                                                                            (1, '立方米', 23, 'product_unit', 0, '', '', '',  1, NOW(), 1, NOW(), 0),
                                                                                                                                                                            (1, '立方厘米', 24, 'product_unit', 0, '', '', '',  1, NOW(), 1, NOW(), 0),
                                                                                                                                                                            (1, '立方英寸', 25, 'product_unit', 0, '', '', '',  1, NOW(), 1, NOW(), 0),
                                                                                                                                                                            (1, '米', 26, 'product_unit', 0, '', '', '',  1, NOW(), 1, NOW(), 0),
                                                                                                                                                                            (1, '千米', 27, 'product_unit', 0, '', '', '',  1, NOW(), 1, NOW(), 0),
                                                                                                                                                                            (1, '厘米', 28, 'product_unit', 0, '', '', '', 1, NOW(), 1, NOW(), 0),
                                                                                                                                                                            (1, '毫米', 29, 'product_unit', 0, '', '', '', 1, NOW(), 1, NOW(), 0),
                                                                                                                                                                            (1, '英寸', 30, 'product_unit', 0, '', '', '', 1, NOW(), 1, NOW(), 0),
                                                                                                                                                                            (1, '英尺', 31, 'product_unit', 0, '', '', '', 1, NOW(), 1, NOW(), 0),
                                                                                                                                                                            (1, '码', 32, 'product_unit', 0, '', '', '', 1, NOW(), 1, NOW(), 0),
                                                                                                                                                                            (1,'个',1,'product_unit',0,'','','',1, NOW(),1, NOW(),0);
-- ----------------------------
-- 数字字典，快递计费方式
-- ----------------------------
INSERT INTO `ruoyi-vue-pro`.`system_dict_type`(`id`, `name`, `type`, `status`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`, `deleted_time`) VALUES (169, '快递计费方式', 'trade_delivery_express_charge_mode', 0, '用于商城交易模块配送管理', '1', '2023-05-21 22:45:03', '1', '2023-05-21 22:45:03', b'0', '1970-01-01 00:00:00');

INSERT INTO `ruoyi-vue-pro`.`system_dict_data`(`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1237, 2, '按体积', '3', 'trade_delivery_express_charge_mode', 0, '', '', '', '1', '2023-05-21 22:47:18', '1', '2023-05-21 22:47:18', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_dict_data`(`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1236, 1, '按重量', '2', 'trade_delivery_express_charge_mode', 0, '', '', '', '1', '2023-05-21 22:46:58', '1', '2023-05-21 22:46:58', b'0');
INSERT INTO `ruoyi-vue-pro`.`system_dict_data`(`id`, `sort`, `label`, `value`, `dict_type`, `status`, `color_type`, `css_class`, `remark`, `creator`, `create_time`, `updater`, `update_time`, `deleted`) VALUES (1235, 0, '按件', '1', 'trade_delivery_express_charge_mode', 0, '', '', '', '1', '2023-05-21 22:46:40', '1', '2023-05-21 22:46:40', b'0');
