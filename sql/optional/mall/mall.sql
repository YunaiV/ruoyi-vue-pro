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

-- ----------------------------
-- Table structure for product_brand
-- ----------------------------

-- ----------------------------
-- Table structure for market_activity
-- ----------------------------
DROP TABLE IF EXISTS `market_activity`;
CREATE TABLE `market_activity`
(
    `id`                    bigint      NOT NULL AUTO_INCREMENT COMMENT '活动编号',
    `title`                 varchar(50) NOT NULL DEFAULT '' COMMENT '活动标题',
    `activity_type`         tinyint(4) NOT NULL COMMENT '活动类型',
    `status`                tinyint(4) NOT NULL DEFAULT '-1' COMMENT '活动状态',
    `start_time`            datetime    NOT NULL COMMENT '开始时间',
    `end_time`              datetime    NOT NULL COMMENT '结束时间',
    `invalid_time`          datetime             DEFAULT NULL COMMENT '失效时间',
    `delete_time`           datetime             DEFAULT NULL COMMENT '删除时间',
    `time_limited_discount` varchar(2000)        DEFAULT NULL COMMENT '限制折扣字符串，使用 JSON 序列化成字符串存储',
    `full_privilege`        varchar(2000)        DEFAULT NULL COMMENT '限制折扣字符串，使用 JSON 序列化成字符串存储',
    `creator`               varchar(64)          DEFAULT '' COMMENT '创建者',
    `create_time`           datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`               varchar(64)          DEFAULT '' COMMENT '更新者',
    `update_time`           datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`               bit(1)      NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`             bigint      NOT NULL DEFAULT '0' COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='促销活动';


-- 规格菜单 SQL
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('规格管理', '', 2, 3, 2001, 'property', '', 'mall/product/property/index', 0);

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('规格查询', 'product:property:query', 3, 1, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('规格创建', 'product:property:create', 3, 2, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('规格更新', 'product:property:update', 3, 3, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('规格删除', 'product:property:delete', 3, 4, @parentId, '', '', '', 0);
INSERT INTO system_menu(name, permission, type, sort, parent_id, path, icon, component, status)
VALUES ('规格导出', 'product:property:export', 3, 5, @parentId, '', '', '', 0);




-- 规格名称表
drop table if exists product_property;
create table product_property
(
    id          bigint NOT NULL AUTO_INCREMENT comment '主键',
    name        varchar(64) comment '规格名称',
    status      tinyint comment '状态： 0 开启 ，1 禁用',
    create_time datetime        default current_timestamp comment '创建时间',
    update_time datetime        default current_timestamp on update current_timestamp comment '更新时间',
    creator     varchar(64) comment '创建人',
    updater     varchar(64) comment '更新人',
    tenant_id   bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
    deleted   bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    primary key (id),
    key         idx_name ( name (32)) comment '规格名称索引'
) comment '规格名称' character set utf8mb4
                 collate utf8mb4_general_ci;

-- 规格值表
drop table if exists product_property_value;
create table product_property_value
(
    id          bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    property_id bigint comment '规格键id',
    name        varchar(128) comment '规格值名字',
    status      tinyint comment '状态： 1 开启 ，2 禁用',
    create_time datetime        default current_timestamp comment '创建时间',
    update_time datetime        default current_timestamp on update current_timestamp comment '更新时间',
    creator     varchar(64) comment '创建人',
    updater     varchar(64) comment '更新人',
    tenant_id   bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
    deleted   bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    primary key (id)
) comment '规格值' character set utf8mb4
                collate utf8mb4_general_ci;



---Market-Banner管理SQL
drop table if exists market_banner;
CREATE TABLE `market_banner` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'Banner编号',
  `title` varchar(64) NOT NULL DEFAULT '' COMMENT 'Banner标题',
  `pic_url` varchar(255) NOT NULL COMMENT '图片URL',
  `status` tinyint(4) NOT NULL DEFAULT '-1' COMMENT '活动状态',
  `url` varchar(255) NOT NULL COMMENT '跳转地址',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',
  `sort` tinyint(4) DEFAULT NULL COMMENT '排序',
  `memo` varchar(255) DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COMMENT='Banner管理';
-- 菜单 SQL
INSERT INTO `system_menu`(`id`,`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`)
VALUES (2002, 'Banner管理', '', 2, 1, 2000, 'brand', '', 'mall/market/banner/index', 0);
-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();
-- 按钮 SQL
INSERT INTO `system_menu`(`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`)
VALUES ('Banner查询', 'market:banner:query', 3, 1, @parentId, '', '', '', 0);
INSERT INTO `system_menu`(`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`)
VALUES ('Banner创建', 'market:banner:create', 3, 2, @parentId, '', '', '', 0);
INSERT INTO `system_menu`(`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`)
VALUES ('Banner更新', 'market:banner:update', 3, 3, @parentId, '', '', '', 0);
INSERT INTO `system_menu`(`name`, `permission`, `type`, `sort`, `parent_id`, `path`, `icon`, `component`, `status`)
VALUES ('Banner删除', 'market:banner:delete', 3, 4, @parentId, '', '', '', 0);
