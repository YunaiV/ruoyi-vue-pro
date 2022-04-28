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

 Date: 05/02/2022 00:50:30
*/
SET
FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;

-- ----------------------------
-- Table structure for product_category
-- ----------------------------
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category`
(
    `id`          bigint        NOT NULL AUTO_INCREMENT COMMENT '分类编号',
    `pid`         bigint        NOT NULL COMMENT '父分类编号',
    `name`        varchar(255)  NOT NULL COMMENT '分类名称',
    `icon`        varchar(100)  NOT NULL DEFAULT '#' COMMENT '分类图标',
    `banner_url`  varchar(255)  NOT NULL COMMENT '分类图片',
    `sort`        int                    DEFAULT '0' COMMENT '分类排序',
    `description` varchar(1024)          DEFAULT NULL COMMENT '分类描述',
    `status`      tinyint       NOT NULL COMMENT '开启状态',
    `creator`     varchar(64)            DEFAULT '' COMMENT '创建者',
    `create_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`     varchar(64)            DEFAULT '' COMMENT '更新者',
    `update_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     bit(1)        NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`   bigint        NOT NULL DEFAULT '0' COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='商品分类';

-- ----------------------------
-- Table structure for product_brand
-- ----------------------------
DROP TABLE IF EXISTS `product_brand`;
CREATE TABLE `product_brand`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT '品牌编号',
    `category_id` bigint       NOT NULL COMMENT '分类编号',
    `name`        varchar(255) NOT NULL COMMENT '品牌名称',
    `banner_url`  varchar(255) NOT NULL COMMENT '品牌图片',
    `sort`        int                   DEFAULT '0' COMMENT '品牌排序',
    `description` varchar(1024)         DEFAULT NULL COMMENT '品牌描述',
    `status`      tinyint      NOT NULL COMMENT '状态',
    `creator`     varchar(64)           DEFAULT '' COMMENT '创建者',
    `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`     varchar(64)           DEFAULT '' COMMENT '更新者',
    `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`   bigint       NOT NULL DEFAULT '0' COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='品牌';

-- TODO 父级菜单的 id 处理
INSERT INTO `system_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES (2000, '商城', '', 1, 1, 0, '/mall', 'merchant', NULL, 0);
INSERT INTO `system_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES (2001, '商品管理', '', 1, 1, 2000, 'product', 'dict', NULL, 0);
-- 商品分类 菜单 SQL
INSERT INTO `system_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES ('商品分类管理', '', 2, 0, 2001, 'category', '', 'mall/product/category/index', 0);
-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();
-- 按钮 SQL
INSERT INTO `system_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES ('商品分类查询', 'product:category:query', 3, 1, @parentId, '', '', '', 0);
INSERT INTO `system_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES ('商品分类创建', 'product:category:create', 3, 2, @parentId, '', '', '', 0);
INSERT INTO `system_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES ('商品分类更新', 'product:category:update', 3, 3, @parentId, '', '', '', 0);
INSERT INTO `system_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES ('商品分类删除', 'product:category:delete', 3, 4, @parentId, '', '', '', 0);
INSERT INTO `system_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES ('商品分类导出', 'product:category:export', 3, 5, @parentId, '', '', '', 0);
-- 品牌管理 菜单 SQL
INSERT INTO `system_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES ('品牌管理', '', 2, 0, 2001, 'brand', '', 'mall/product/brand/index', 0);
-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();
-- 按钮 SQL
INSERT INTO `system_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES ('品牌查询', 'product:brand:query', 3, 1, @parentId, '', '', '', 0);
INSERT INTO `system_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES ('品牌创建', 'product:brand:create', 3, 2, @parentId, '', '', '', 0);
INSERT INTO `system_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES ('品牌更新', 'product:brand:update', 3, 3, @parentId, '', '', '', 0);
INSERT INTO `system_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES ('品牌删除', 'product:brand:delete', 3, 4, @parentId, '', '', '', 0);
INSERT INTO `system_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES ('品牌导出', 'product:brand:export', 3, 5, @parentId, '', '', '', 0);


-- ----------------------------
-- Table structure for market_activity
-- ----------------------------
DROP TABLE IF EXISTS `market_activity`;
CREATE TABLE `market_activity` (
   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动编号',
   `title` varchar(50) NOT NULL DEFAULT '' COMMENT '活动标题',
   `activity_type` tinyint(4) NOT NULL COMMENT '活动类型',
   `status` tinyint(4) NOT NULL DEFAULT '-1' COMMENT '活动状态',
   `start_time` datetime NOT NULL COMMENT '开始时间',
   `end_time` datetime NOT NULL COMMENT '结束时间',
   `invalid_time` datetime DEFAULT NULL COMMENT '失效时间',
   `delete_time` datetime DEFAULT NULL COMMENT '删除时间',
   `time_limited_discount` varchar(2000) DEFAULT NULL COMMENT '限制折扣字符串，使用 JSON 序列化成字符串存储',
   `full_privilege` varchar(2000) DEFAULT NULL COMMENT '限制折扣字符串，使用 JSON 序列化成字符串存储',
   `creator`     varchar(64)           DEFAULT '' COMMENT '创建者',
   `create_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `updater`     varchar(64)           DEFAULT '' COMMENT '更新者',
   `update_time` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   `deleted`     bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
   `tenant_id`   bigint       NOT NULL DEFAULT '0' COMMENT '租户编号',
   PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='促销活动';