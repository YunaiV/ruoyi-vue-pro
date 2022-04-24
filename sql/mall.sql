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
    `icon`        varchar(100)           DEFAULT '#' COMMENT '分类图标',
    `banner_url`  varchar(255)  NOT NULL COMMENT '分类图片',
    `sort`        int           NOT NULL DEFAULT '0' COMMENT '分类排序',
    `description` varchar(1024) NOT NULL COMMENT '分类描述',
    `status`      tinyint       NOT NULL COMMENT '开启状态',
    `creator`     varchar(64)            DEFAULT '' COMMENT '创建者',
    `create_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`     varchar(64)            DEFAULT '' COMMENT '更新者',
    `update_time` datetime      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     bit(1)        NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`   bigint        NOT NULL DEFAULT '0' COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB COMMENT='商品分类';

-- TODO 父级菜单的 id 处理
INSERT INTO `system_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES (2000, '商城', '', 1, 1, 0, '/mall', 'merchant', NULL, 0);
INSERT INTO `system_menu` (`id`, `name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES (2001, '商品管理', '', 1, 1, 2000, 'product', 'dict', NULL, 0);
-- 菜单 SQL
INSERT INTO `system_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES ('商品分类管理', '', 2, 0, 2001, 'category', '', 'mall/product/category/index', 0);
-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();
-- 按钮 SQL
INSERT INTO `system_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES ('商品分类查询', 'product:category:query', 3, 1, @parentId, '', '', '', 0);
INSERT INTO `system_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES ('商品分类创建', 'product:category:create', 3, 2, @parentId, '', '', '', 0);
INSERT INTO `system_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES ('商品分类更新', 'product:category:update', 3, 3, @parentId, '', '', '', 0);
INSERT INTO `system_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES ('商品分类删除', 'product:category:delete', 3, 4, @parentId, '', '', '', 0);
INSERT INTO `system_menu`(`name`, `permission`, `menu_type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`) VALUES ('商品分类导出', 'product:category:export', 3, 5, @parentId, '', '', '', 0);
