/*
 Navicat Premium Data Transfer


 Source Server         : 127.0.0.1 MySQL
 Source Server Type    : MySQL
 Source Server Version : 80030
 Source Host           : localhost:3306

 Target Server Type    : MySQL
 Target Server Version : 80030
 File Encoding         : 65001

 Date: 29/06/2023 21:05:23
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for trade_after_sale_log
-- ----------------------------
DROP TABLE IF EXISTS `trade_after_sale_log`;
CREATE TABLE `trade_after_sale_log`
(
    `id`            bigint                             NOT NULL AUTO_INCREMENT COMMENT '编号',
    `after_sale_id` bigint                             NOT NULL COMMENT '售后服务单号',
    `content`       varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '售后服务信息',
    `operate_type`  varchar(255)                       NOT NULL COMMENT '操作类型',
    `user_id`       bigint                             NOT NULL COMMENT '创建者ID',
    `user_type`     int                                NOT NULL COMMENT '创建者类型',
    `creator`       varchar(255) CHARACTER SET utf8mb4          DEFAULT '' COMMENT '创建者',
    `create_time`   datetime                           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`       varchar(64) CHARACTER SET utf8mb4           DEFAULT '' COMMENT '更新者',
    `update_time`   datetime                           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       bit(1)                             NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`     bigint                             NOT NULL DEFAULT '0' COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT='售后日志';

-- ----------------------------
-- Table structure for trade_order_log
-- ----------------------------
DROP TABLE IF EXISTS `trade_order_log`;
CREATE TABLE `trade_order_log`
(
    `id`           bigint                             NOT NULL AUTO_INCREMENT COMMENT '编号',
    `order_id`     bigint                             NOT NULL COMMENT '订单号',
    `content`      varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '订单日志信息',
    `operate_type` varchar(255) CHARACTER SET utf8mb4 NOT NULL COMMENT '操作类型',
    `user_id`      bigint                             NOT NULL COMMENT '创建者ID',
    `user_type`    int                                NOT NULL COMMENT '创建者类型',
    `creator`      varchar(255) CHARACTER SET utf8mb4          DEFAULT '' COMMENT '创建者',
    `create_time`  datetime                           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`      varchar(64) CHARACTER SET utf8mb4           DEFAULT '' COMMENT '更新者',
    `update_time`  datetime                           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      bit(1)                             NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`    bigint                             NOT NULL DEFAULT '0' COMMENT '租户编号',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4  COMMENT='订单日志';

SET FOREIGN_KEY_CHECKS = 1;
