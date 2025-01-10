/*
 Navicat Premium Data Transfer

 Source Server         : 151-root
 Source Server Type    : MySQL
 Source Server Version : 80029 (8.0.29)
 Source Host           : 192.168.10.151:3306
 Source Schema         : ruoyi-vue-pro

 Target Server Type    : MySQL
 Target Server Version : 80029 (8.0.29)
 File Encoding         : 65001

 Date: 11/10/2024 15:33:31
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for erp_account
-- ----------------------------
DROP TABLE IF EXISTS `erp_account`;
CREATE TABLE `erp_account` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '结算账户编号',
                               `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账户名称',
                               `no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '账户编码',
                               `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                               `status` tinyint NOT NULL COMMENT '开启状态',
                               `sort` int NOT NULL COMMENT '排序',
                               `default_status` bit(1) DEFAULT b'0' COMMENT '是否默认',
                               `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                               `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                               PRIMARY KEY (`id` DESC) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 结算账户';

-- ----------------------------
-- Table structure for erp_custom_rule
-- ----------------------------
DROP TABLE IF EXISTS `erp_custom_rule`;
CREATE TABLE `erp_custom_rule` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '产品编号',
                                   `country_code` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '国家编码',
                                   `type` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型',
                                   `supplier_product_id` bigint NOT NULL COMMENT '供应商产品编号',
                                   `declared_type_en` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '申报品名（英文）',
                                   `declared_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '申报品名',
                                   `declared_value` float DEFAULT NULL COMMENT '申报金额',
                                   `declared_value_currency_code` varchar(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '申报金额币种',
                                   `tax_rate` decimal(6,0) DEFAULT NULL COMMENT '税率',
                                   `hscode` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'hs编码',
                                   `logistic_attribute` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '物流属性',
                                   `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                   `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                   PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ERP 海关规则表';

-- ----------------------------
-- Table structure for erp_customer
-- ----------------------------
DROP TABLE IF EXISTS `erp_customer`;
CREATE TABLE `erp_customer` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '客户编号',
                                `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '客户名称',
                                `contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系人',
                                `mobile` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号码',
                                `telephone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
                                `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电子邮箱',
                                `fax` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '传真',
                                `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                `status` tinyint NOT NULL COMMENT '开启状态',
                                `sort` int NOT NULL COMMENT '排序',
                                `tax_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '纳税人识别号',
                                `tax_percent` decimal(24,6) DEFAULT NULL COMMENT '税率',
                                `bank_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '开户行',
                                `bank_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '开户账号',
                                `bank_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '开户地址',
                                `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                PRIMARY KEY (`id` DESC) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 客户表';

-- ----------------------------
-- Table structure for erp_finance_payment
-- ----------------------------
DROP TABLE IF EXISTS `erp_finance_payment`;
CREATE TABLE `erp_finance_payment` (
                                       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
                                       `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '付款单号',
                                       `status` tinyint NOT NULL COMMENT '状态',
                                       `payment_time` datetime NOT NULL COMMENT '付款时间',
                                       `finance_user_id` bigint DEFAULT NULL COMMENT '财务人员编号',
                                       `supplier_id` bigint NOT NULL COMMENT '供应商编号',
                                       `account_id` bigint NOT NULL COMMENT '付款账户编号',
                                       `total_price` decimal(24,6) NOT NULL COMMENT '合计价格，单位：元',
                                       `discount_price` decimal(24,6) NOT NULL COMMENT '优惠金额，单位：元',
                                       `payment_price` decimal(24,6) NOT NULL COMMENT '实付金额，单位：分',
                                       `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                       `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                       `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                       `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                       PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 付款单表';

-- ----------------------------
-- Table structure for erp_finance_payment_item
-- ----------------------------
DROP TABLE IF EXISTS `erp_finance_payment_item`;
CREATE TABLE `erp_finance_payment_item` (
                                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
                                            `payment_id` bigint NOT NULL COMMENT '付款单编号',
                                            `biz_type` tinyint NOT NULL COMMENT '业务类型',
                                            `biz_id` bigint NOT NULL COMMENT '业务编号',
                                            `biz_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务单号',
                                            `total_price` decimal(24,6) NOT NULL COMMENT '应付欠款，单位：分',
                                            `paid_price` decimal(24,6) NOT NULL COMMENT '已付欠款，单位：分',
                                            `payment_price` decimal(24,6) NOT NULL COMMENT '本次付款，单位：分',
                                            `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                            `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                            `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                            `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                            PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 付款项表';

-- ----------------------------
-- Table structure for erp_finance_receipt
-- ----------------------------
DROP TABLE IF EXISTS `erp_finance_receipt`;
CREATE TABLE `erp_finance_receipt` (
                                       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
                                       `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收款单号',
                                       `status` tinyint NOT NULL COMMENT '状态',
                                       `receipt_time` datetime NOT NULL COMMENT '收款时间',
                                       `finance_user_id` bigint DEFAULT NULL COMMENT '财务人员编号',
                                       `customer_id` bigint NOT NULL COMMENT '客户编号',
                                       `account_id` bigint NOT NULL COMMENT '收款账户编号',
                                       `total_price` decimal(24,6) NOT NULL COMMENT '合计价格，单位：元',
                                       `discount_price` decimal(24,6) NOT NULL COMMENT '优惠金额，单位：元',
                                       `receipt_price` decimal(24,6) NOT NULL COMMENT '实收金额，单位：分',
                                       `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                       `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                       `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                       `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                       PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 收款单表';

-- ----------------------------
-- Table structure for erp_finance_receipt_item
-- ----------------------------
DROP TABLE IF EXISTS `erp_finance_receipt_item`;
CREATE TABLE `erp_finance_receipt_item` (
                                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
                                            `receipt_id` bigint NOT NULL COMMENT '收款单编号',
                                            `biz_type` tinyint NOT NULL COMMENT '业务类型',
                                            `biz_id` bigint NOT NULL COMMENT '业务编号',
                                            `biz_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务单号',
                                            `total_price` decimal(24,6) NOT NULL COMMENT '应收金额，单位：分',
                                            `receipted_price` decimal(24,6) NOT NULL COMMENT '已收金额，单位：分',
                                            `receipt_price` decimal(24,6) NOT NULL COMMENT '本次收款，单位：分',
                                            `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                            `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                            `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                            `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                            PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 收款项表';

-- ----------------------------
-- Table structure for erp_product
-- ----------------------------
DROP TABLE IF EXISTS `erp_product`;
CREATE TABLE `erp_product` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '产品编号',
                               `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品名称',
                               `bar_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品编码',
                               `category_id` bigint NOT NULL COMMENT '产品分类编号',
                               `unit_id` int NOT NULL COMMENT '单位编号',
                               `status` tinyint NOT NULL COMMENT '产品状态',
                               `standard` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '产品规格',
                               `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '产品备注',
                               `expiry_day` int DEFAULT NULL COMMENT '保质期天数',
                               `weight` decimal(24,6) DEFAULT NULL COMMENT '基础重量（kg）',
                               `purchase_price` decimal(24,6) DEFAULT NULL COMMENT '采购价格，单位：元',
                               `sale_price` decimal(24,6) DEFAULT NULL COMMENT '销售价格，单位：元',
                               `min_price` decimal(24,6) DEFAULT NULL COMMENT '最低价格，单位：元',
                               `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                               `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                               `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                               `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                               `length` decimal(24,6) DEFAULT NULL COMMENT '基础长度（cm）',
                               `width` decimal(24,6) DEFAULT NULL COMMENT '基础宽度（cm）',
                               `height` decimal(24,6) DEFAULT NULL COMMENT '基础高度（cm）',
                               `material` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '材料（中文）',
                               `image_url` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图片URL',
                               PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=15675 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 产品表';

-- ----------------------------
-- Table structure for erp_product_category
-- ----------------------------
DROP TABLE IF EXISTS `erp_product_category`;
CREATE TABLE `erp_product_category` (
                                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类编号',
                                        `parent_id` bigint NOT NULL COMMENT '父分类编号',
                                        `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
                                        `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类编码',
                                        `sort` int DEFAULT '0' COMMENT '分类排序',
                                        `status` tinyint NOT NULL COMMENT '开启状态',
                                        `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                        `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                        `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                        `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                        PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=89 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 产品分类';

-- ----------------------------
-- Table structure for erp_product_unit
-- ----------------------------
DROP TABLE IF EXISTS `erp_product_unit`;
CREATE TABLE `erp_product_unit` (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '单位编号',
                                    `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '单位名字',
                                    `status` tinyint NOT NULL COMMENT '单位状态',
                                    `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                    `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 产品单位表';

-- ----------------------------
-- Table structure for erp_purchase_in
-- ----------------------------
DROP TABLE IF EXISTS `erp_purchase_in`;
CREATE TABLE `erp_purchase_in` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
                                   `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '采购入库编号',
                                   `status` tinyint NOT NULL COMMENT '采购状态',
                                   `supplier_id` bigint NOT NULL COMMENT '供应商编号',
                                   `account_id` bigint NOT NULL COMMENT '结算账户编号',
                                   `in_time` datetime NOT NULL COMMENT '入库时间',
                                   `order_id` bigint NOT NULL COMMENT '采购订单编号',
                                   `order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '采购订单号',
                                   `total_count` decimal(24,6) NOT NULL COMMENT '合计数量',
                                   `total_price` decimal(24,6) NOT NULL COMMENT '合计价格，单位：元',
                                   `payment_price` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '已付款金额，单位：元',
                                   `total_product_price` decimal(24,6) NOT NULL COMMENT '合计产品价格，单位：元',
                                   `total_tax_price` decimal(24,6) NOT NULL COMMENT '合计税额，单位：元',
                                   `discount_percent` decimal(24,6) NOT NULL COMMENT '优惠率，百分比',
                                   `discount_price` decimal(24,6) NOT NULL COMMENT '优惠金额，单位：元',
                                   `other_price` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '其它金额，单位：元',
                                   `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件地址',
                                   `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                   `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                   `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   UNIQUE KEY `no` (`no`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 采购入库表';

-- ----------------------------
-- Table structure for erp_purchase_in_items
-- ----------------------------
DROP TABLE IF EXISTS `erp_purchase_in_items`;
CREATE TABLE `erp_purchase_in_items` (
                                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
                                         `in_id` bigint NOT NULL COMMENT '采购入库编号',
                                         `order_item_id` bigint NOT NULL COMMENT '采购订单项编号',
                                         `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
                                         `product_id` bigint NOT NULL COMMENT '产品编号',
                                         `product_unit_id` bigint NOT NULL COMMENT '产品单位单位',
                                         `product_price` decimal(24,6) NOT NULL COMMENT '产品单价',
                                         `count` decimal(24,6) NOT NULL COMMENT '数量',
                                         `total_price` decimal(24,6) NOT NULL COMMENT '总价',
                                         `tax_percent` decimal(24,6) DEFAULT NULL COMMENT '税率，百分比',
                                         `tax_price` decimal(24,6) DEFAULT NULL COMMENT '税额，单位：元',
                                         `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                         `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                         `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                         `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                         PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 销售入库项表';

-- ----------------------------
-- Table structure for erp_purchase_order
-- ----------------------------
DROP TABLE IF EXISTS `erp_purchase_order`;
CREATE TABLE `erp_purchase_order` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
                                      `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '采购单编号',
                                      `status` tinyint NOT NULL COMMENT '采购状态',
                                      `supplier_id` bigint NOT NULL COMMENT '供应商编号',
                                      `account_id` bigint DEFAULT NULL COMMENT '结算账户编号',
                                      `order_time` datetime NOT NULL COMMENT '采购时间',
                                      `total_count` decimal(24,6) NOT NULL COMMENT '合计数量',
                                      `total_price` decimal(24,6) NOT NULL COMMENT '合计价格，单位：元',
                                      `total_product_price` decimal(24,6) NOT NULL COMMENT '合计产品价格，单位：元',
                                      `total_tax_price` decimal(24,6) NOT NULL COMMENT '合计税额，单位：元',
                                      `discount_percent` decimal(24,6) NOT NULL COMMENT '优惠率，百分比',
                                      `discount_price` decimal(24,6) NOT NULL COMMENT '优惠金额，单位：元',
                                      `deposit_price` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '定金金额，单位：元',
                                      `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件地址',
                                      `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                      `in_count` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '采购入库数量',
                                      `return_count` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '采购退货数量',
                                      `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                      `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                      PRIMARY KEY (`id`) USING BTREE,
                                      UNIQUE KEY `no` (`no`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 采购订单表';

-- ----------------------------
-- Table structure for erp_purchase_order_items
-- ----------------------------
DROP TABLE IF EXISTS `erp_purchase_order_items`;
CREATE TABLE `erp_purchase_order_items` (
                                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
                                            `order_id` bigint NOT NULL COMMENT '采购订单编号',
                                            `product_id` bigint NOT NULL COMMENT '产品编号',
                                            `product_unit_id` bigint NOT NULL COMMENT '产品单位单位',
                                            `product_price` decimal(24,6) NOT NULL COMMENT '产品单价',
                                            `count` decimal(24,6) NOT NULL COMMENT '数量',
                                            `total_price` decimal(24,6) NOT NULL COMMENT '总价',
                                            `tax_percent` decimal(24,6) DEFAULT NULL COMMENT '税率，百分比',
                                            `tax_price` decimal(24,6) DEFAULT NULL COMMENT '税额，单位：元',
                                            `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                            `in_count` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '采购入库数量',
                                            `return_count` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '采购退货数量',
                                            `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                            `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                            `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                            `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                            `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                            PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 采购订单项表';

-- ----------------------------
-- Table structure for erp_purchase_request
-- ----------------------------
DROP TABLE IF EXISTS `erp_purchase_request`;
CREATE TABLE `erp_purchase_request` (
                                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                                        `no` char(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '单据编号',
                                        `applicant` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '申请人',
                                        `application_dept` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '申请部门',
                                        `request_time` datetime NOT NULL COMMENT '单据日期',
                                        `status` tinyint NOT NULL DEFAULT '10' COMMENT '审核状态(0:待审核，1:审核通过，2:审核未通过)',
                                        `off_status` tinyint NOT NULL DEFAULT '0' COMMENT '关闭状态（0已关闭，1已开启）',
                                        `order_status` tinyint NOT NULL DEFAULT '0' COMMENT '订购状态（0部分订购，1全部订购）',
                                        `auditor` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '审核者',
                                        `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
                                        `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                        `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                        `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                        `tenant_id` bigint NOT NULL COMMENT '租户编号',
                                        PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP采购申请单';

-- ----------------------------
-- Table structure for erp_purchase_request_items
-- ----------------------------
DROP TABLE IF EXISTS `erp_purchase_request_items`;
CREATE TABLE `erp_purchase_request_items` (
                                              `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                                              `product_id` bigint NOT NULL COMMENT '商品id',
                                              `request_id` bigint NOT NULL COMMENT '申请单id',
                                              `count` int NOT NULL COMMENT '申请数量',
                                              `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                              `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                              `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                              `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                              `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                              `tenant_id` bigint NOT NULL COMMENT '租户编号',
                                              PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP采购申请单子表';

-- ----------------------------
-- Table structure for erp_purchase_return
-- ----------------------------
DROP TABLE IF EXISTS `erp_purchase_return`;
CREATE TABLE `erp_purchase_return` (
                                       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
                                       `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '采购退货编号',
                                       `status` tinyint NOT NULL COMMENT '退货状态',
                                       `supplier_id` bigint NOT NULL COMMENT '供应商编号',
                                       `account_id` bigint NOT NULL COMMENT '结算账户编号',
                                       `return_time` datetime NOT NULL COMMENT '退货时间',
                                       `order_id` bigint NOT NULL COMMENT '采购订单编号',
                                       `order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '采购订单号',
                                       `total_count` decimal(24,6) NOT NULL COMMENT '合计数量',
                                       `total_price` decimal(24,6) NOT NULL COMMENT '合计价格，单位：元',
                                       `refund_price` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '已退款金额，单位：元',
                                       `total_product_price` decimal(24,6) NOT NULL COMMENT '合计产品价格，单位：元',
                                       `total_tax_price` decimal(24,6) NOT NULL COMMENT '合计税额，单位：元',
                                       `discount_percent` decimal(24,6) NOT NULL COMMENT '优惠率，百分比',
                                       `discount_price` decimal(24,6) NOT NULL COMMENT '优惠金额，单位：元',
                                       `other_price` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '其它金额，单位：元',
                                       `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件地址',
                                       `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                       `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                       `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                       `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                       PRIMARY KEY (`id`) USING BTREE,
                                       UNIQUE KEY `no` (`no`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 采购退货表';

-- ----------------------------
-- Table structure for erp_purchase_return_items
-- ----------------------------
DROP TABLE IF EXISTS `erp_purchase_return_items`;
CREATE TABLE `erp_purchase_return_items` (
                                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
                                             `return_id` bigint NOT NULL COMMENT '采购退货编号',
                                             `order_item_id` bigint NOT NULL COMMENT '采购订单项编号',
                                             `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
                                             `product_id` bigint NOT NULL COMMENT '产品编号',
                                             `product_unit_id` bigint NOT NULL COMMENT '产品单位单位',
                                             `product_price` decimal(24,6) NOT NULL COMMENT '产品单价',
                                             `count` decimal(24,6) NOT NULL COMMENT '数量',
                                             `total_price` decimal(24,6) NOT NULL COMMENT '总价',
                                             `tax_percent` decimal(24,6) DEFAULT NULL COMMENT '税率，百分比',
                                             `tax_price` decimal(24,6) DEFAULT NULL COMMENT '税额，单位：元',
                                             `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                             `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                             `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                             `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                             `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                             PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 采购退货项表';

-- ----------------------------
-- Table structure for erp_sale_order
-- ----------------------------
DROP TABLE IF EXISTS `erp_sale_order`;
CREATE TABLE `erp_sale_order` (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
                                  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '销售单编号',
                                  `status` tinyint NOT NULL COMMENT '销售状态',
                                  `customer_id` bigint NOT NULL COMMENT '客户编号',
                                  `account_id` bigint DEFAULT NULL COMMENT '结算账户编号',
                                  `sale_user_id` bigint DEFAULT NULL COMMENT '销售用户编号',
                                  `order_time` datetime NOT NULL COMMENT '下单时间',
                                  `total_count` decimal(24,6) NOT NULL COMMENT '合计数量',
                                  `total_price` decimal(24,6) NOT NULL COMMENT '合计价格，单位：元',
                                  `total_product_price` decimal(24,6) NOT NULL COMMENT '合计产品价格，单位：元',
                                  `total_tax_price` decimal(24,6) NOT NULL COMMENT '合计税额，单位：元',
                                  `discount_percent` decimal(24,6) NOT NULL COMMENT '优惠率，百分比',
                                  `discount_price` decimal(24,6) NOT NULL COMMENT '优惠金额，单位：元',
                                  `deposit_price` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '定金金额，单位：元',
                                  `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件地址',
                                  `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                  `out_count` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '销售出库数量',
                                  `return_count` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '销售退货数量',
                                  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                  PRIMARY KEY (`id`) USING BTREE,
                                  UNIQUE KEY `no` (`no`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 销售订单表';

-- ----------------------------
-- Table structure for erp_sale_order_items
-- ----------------------------
DROP TABLE IF EXISTS `erp_sale_order_items`;
CREATE TABLE `erp_sale_order_items` (
                                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
                                        `order_id` bigint NOT NULL COMMENT '销售订单编号',
                                        `product_id` bigint NOT NULL COMMENT '产品编号',
                                        `product_unit_id` bigint NOT NULL COMMENT '产品单位单位',
                                        `product_price` decimal(24,6) DEFAULT NULL COMMENT '产品单价',
                                        `count` decimal(24,6) NOT NULL COMMENT '数量',
                                        `total_price` decimal(24,6) DEFAULT NULL COMMENT '总价',
                                        `tax_percent` decimal(24,6) DEFAULT NULL COMMENT '税率，百分比',
                                        `tax_price` decimal(24,6) DEFAULT NULL COMMENT '税额，单位：元',
                                        `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                        `out_count` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '销售出库数量',
                                        `return_count` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '销售退货数量',
                                        `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                        `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                        `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                        `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                        PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 销售订单项表';

-- ----------------------------
-- Table structure for erp_sale_out
-- ----------------------------
DROP TABLE IF EXISTS `erp_sale_out`;
CREATE TABLE `erp_sale_out` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
                                `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '销售出库编号',
                                `status` tinyint NOT NULL COMMENT '出库状态',
                                `customer_id` bigint NOT NULL COMMENT '客户编号',
                                `account_id` bigint NOT NULL COMMENT '结算账户编号',
                                `sale_user_id` bigint DEFAULT NULL COMMENT '销售用户编号',
                                `out_time` datetime NOT NULL COMMENT '出库时间',
                                `order_id` bigint NOT NULL COMMENT '销售订单编号',
                                `order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '销售订单号',
                                `total_count` decimal(24,6) NOT NULL COMMENT '合计数量',
                                `total_price` decimal(24,6) NOT NULL COMMENT '合计价格，单位：元',
                                `receipt_price` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '已收款金额，单位：元',
                                `total_product_price` decimal(24,6) NOT NULL COMMENT '合计产品价格，单位：元',
                                `total_tax_price` decimal(24,6) NOT NULL COMMENT '合计税额，单位：元',
                                `discount_percent` decimal(24,6) NOT NULL COMMENT '优惠率，百分比',
                                `discount_price` decimal(24,6) NOT NULL COMMENT '优惠金额，单位：元',
                                `other_price` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '其它金额，单位：元',
                                `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件地址',
                                `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                PRIMARY KEY (`id`) USING BTREE,
                                UNIQUE KEY `no` (`no`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 销售出库表';

-- ----------------------------
-- Table structure for erp_sale_out_items
-- ----------------------------
DROP TABLE IF EXISTS `erp_sale_out_items`;
CREATE TABLE `erp_sale_out_items` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
                                      `out_id` bigint NOT NULL COMMENT '销售出库编号',
                                      `order_item_id` bigint NOT NULL COMMENT '销售订单项编号',
                                      `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
                                      `product_id` bigint NOT NULL COMMENT '产品编号',
                                      `product_unit_id` bigint NOT NULL COMMENT '产品单位单位',
                                      `product_price` decimal(24,6) NOT NULL COMMENT '产品单价',
                                      `count` decimal(24,6) NOT NULL COMMENT '数量',
                                      `total_price` decimal(24,6) NOT NULL COMMENT '总价',
                                      `tax_percent` decimal(24,6) DEFAULT NULL COMMENT '税率，百分比',
                                      `tax_price` decimal(24,6) DEFAULT NULL COMMENT '税额，单位：元',
                                      `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                      `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                      `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                      PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 销售出库项表';

-- ----------------------------
-- Table structure for erp_sale_return
-- ----------------------------
DROP TABLE IF EXISTS `erp_sale_return`;
CREATE TABLE `erp_sale_return` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
                                   `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '销售退货编号',
                                   `status` tinyint NOT NULL COMMENT '退货状态',
                                   `customer_id` bigint NOT NULL COMMENT '客户编号',
                                   `account_id` bigint NOT NULL COMMENT '结算账户编号',
                                   `sale_user_id` bigint DEFAULT NULL COMMENT '销售用户编号',
                                   `return_time` datetime NOT NULL COMMENT '退货时间',
                                   `order_id` bigint NOT NULL COMMENT '销售订单编号',
                                   `order_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '销售订单号',
                                   `total_count` decimal(24,6) NOT NULL COMMENT '合计数量',
                                   `total_price` decimal(24,6) NOT NULL COMMENT '合计价格，单位：元',
                                   `refund_price` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '已退款金额，单位：元',
                                   `total_product_price` decimal(24,6) NOT NULL COMMENT '合计产品价格，单位：元',
                                   `total_tax_price` decimal(24,6) NOT NULL COMMENT '合计税额，单位：元',
                                   `discount_percent` decimal(24,6) NOT NULL COMMENT '优惠率，百分比',
                                   `discount_price` decimal(24,6) NOT NULL COMMENT '优惠金额，单位：元',
                                   `other_price` decimal(24,6) NOT NULL DEFAULT '0.000000' COMMENT '其它金额，单位：元',
                                   `file_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件地址',
                                   `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                   `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                   `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                   PRIMARY KEY (`id`) USING BTREE,
                                   UNIQUE KEY `no` (`no`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 销售退货表';

-- ----------------------------
-- Table structure for erp_sale_return_items
-- ----------------------------
DROP TABLE IF EXISTS `erp_sale_return_items`;
CREATE TABLE `erp_sale_return_items` (
                                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
                                         `return_id` bigint NOT NULL COMMENT '销售退货编号',
                                         `order_item_id` bigint NOT NULL COMMENT '销售订单项编号',
                                         `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
                                         `product_id` bigint NOT NULL COMMENT '产品编号',
                                         `product_unit_id` bigint NOT NULL COMMENT '产品单位单位',
                                         `product_price` decimal(24,6) NOT NULL COMMENT '产品单价',
                                         `count` decimal(24,6) NOT NULL COMMENT '数量',
                                         `total_price` decimal(24,6) NOT NULL COMMENT '总价',
                                         `tax_percent` decimal(24,6) DEFAULT NULL COMMENT '税率，百分比',
                                         `tax_price` decimal(24,6) DEFAULT NULL COMMENT '税额，单位：元',
                                         `remark` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                         `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                         `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                         `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                         `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                         `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                         `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                         PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 销售退货项表';

-- ----------------------------
-- Table structure for erp_stock
-- ----------------------------
DROP TABLE IF EXISTS `erp_stock`;
CREATE TABLE `erp_stock` (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
                             `product_id` bigint NOT NULL COMMENT '产品编号',
                             `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
                             `count` decimal(24,6) NOT NULL COMMENT '库存数量',
                             `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                             `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                             `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                             PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 产品库存表';

-- ----------------------------
-- Table structure for erp_stock_check
-- ----------------------------
DROP TABLE IF EXISTS `erp_stock_check`;
CREATE TABLE `erp_stock_check` (
                                   `id` bigint NOT NULL AUTO_INCREMENT COMMENT '盘点编号',
                                   `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '盘点单号',
                                   `check_time` datetime NOT NULL COMMENT '盘点时间',
                                   `total_count` decimal(24,6) NOT NULL COMMENT '合计数量',
                                   `total_price` decimal(24,6) NOT NULL COMMENT '合计金额，单位：元',
                                   `status` tinyint NOT NULL COMMENT '状态',
                                   `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                   `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件 URL',
                                   `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                   `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                   PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 库存盘点单表';

-- ----------------------------
-- Table structure for erp_stock_check_item
-- ----------------------------
DROP TABLE IF EXISTS `erp_stock_check_item`;
CREATE TABLE `erp_stock_check_item` (
                                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '盘点项编号',
                                        `check_id` bigint NOT NULL COMMENT '盘点编号',
                                        `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
                                        `product_id` bigint NOT NULL COMMENT '产品编号',
                                        `product_unit_id` bigint NOT NULL COMMENT '产品单位编号',
                                        `product_price` decimal(24,6) DEFAULT NULL COMMENT '产品单价',
                                        `stock_count` decimal(24,6) NOT NULL COMMENT '账面数量（当前库存）',
                                        `actual_count` decimal(24,6) NOT NULL COMMENT '实际数量（实际库存）',
                                        `count` decimal(24,6) NOT NULL COMMENT '盈亏数量',
                                        `total_price` decimal(24,6) DEFAULT NULL COMMENT '合计金额，单位：元',
                                        `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                        `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                        `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                        `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                        `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                        PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 库存盘点项表';

-- ----------------------------
-- Table structure for erp_stock_in
-- ----------------------------
DROP TABLE IF EXISTS `erp_stock_in`;
CREATE TABLE `erp_stock_in` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '入库编号',
                                `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '入库单号',
                                `supplier_id` bigint DEFAULT NULL COMMENT '供应商编号',
                                `in_time` datetime NOT NULL COMMENT '入库时间',
                                `total_count` decimal(24,6) NOT NULL COMMENT '合计数量',
                                `total_price` decimal(24,6) NOT NULL COMMENT '合计金额，单位：元',
                                `status` tinyint NOT NULL COMMENT '状态',
                                `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件 URL',
                                `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 其它入库单表';

-- ----------------------------
-- Table structure for erp_stock_in_item
-- ----------------------------
DROP TABLE IF EXISTS `erp_stock_in_item`;
CREATE TABLE `erp_stock_in_item` (
                                     `id` bigint NOT NULL AUTO_INCREMENT COMMENT '入库项编号',
                                     `in_id` bigint NOT NULL COMMENT '入库编号',
                                     `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
                                     `product_id` bigint NOT NULL COMMENT '产品编号',
                                     `product_unit_id` bigint NOT NULL COMMENT '产品单位编号',
                                     `product_price` decimal(24,6) DEFAULT NULL COMMENT '产品单价',
                                     `count` decimal(24,6) NOT NULL COMMENT '产品数量',
                                     `total_price` decimal(24,6) DEFAULT NULL COMMENT '合计金额，单位：元',
                                     `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                     `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                     `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                     `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                     `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                     `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                     `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                     PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 其它入库单项表';

-- ----------------------------
-- Table structure for erp_stock_move
-- ----------------------------
DROP TABLE IF EXISTS `erp_stock_move`;
CREATE TABLE `erp_stock_move` (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '调拨编号',
                                  `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调拨单号',
                                  `move_time` datetime NOT NULL COMMENT '调拨时间',
                                  `total_count` decimal(24,6) NOT NULL COMMENT '合计数量',
                                  `total_price` decimal(24,6) NOT NULL COMMENT '合计金额，单位：元',
                                  `status` tinyint NOT NULL COMMENT '状态',
                                  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件 URL',
                                  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 库存调拨单表';

-- ----------------------------
-- Table structure for erp_stock_move_item
-- ----------------------------
DROP TABLE IF EXISTS `erp_stock_move_item`;
CREATE TABLE `erp_stock_move_item` (
                                       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '调拨项编号',
                                       `move_id` bigint NOT NULL COMMENT '调拨编号',
                                       `from_warehouse_id` bigint NOT NULL COMMENT '调出仓库编号',
                                       `to_warehouse_id` bigint NOT NULL COMMENT '调入仓库编号',
                                       `product_id` bigint NOT NULL COMMENT '产品编号',
                                       `product_unit_id` bigint NOT NULL COMMENT '产品单位编号',
                                       `product_price` decimal(24,6) DEFAULT NULL COMMENT '产品单价',
                                       `count` decimal(24,6) NOT NULL COMMENT '产品数量',
                                       `total_price` decimal(24,6) DEFAULT NULL COMMENT '合计金额，单位：元',
                                       `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                       `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                       `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                       `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                       `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                       PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 库存调拨项表';

-- ----------------------------
-- Table structure for erp_stock_out
-- ----------------------------
DROP TABLE IF EXISTS `erp_stock_out`;
CREATE TABLE `erp_stock_out` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '出库编号',
                                 `no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '出库单号',
                                 `customer_id` bigint DEFAULT NULL COMMENT '客户编号',
                                 `out_time` datetime NOT NULL COMMENT '出库时间',
                                 `total_count` decimal(24,6) NOT NULL COMMENT '合计数量',
                                 `total_price` decimal(24,6) NOT NULL COMMENT '合计金额，单位：元',
                                 `status` tinyint NOT NULL COMMENT '状态',
                                 `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                 `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '附件 URL',
                                 `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                 `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                 PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 其它入库单表';

-- ----------------------------
-- Table structure for erp_stock_out_item
-- ----------------------------
DROP TABLE IF EXISTS `erp_stock_out_item`;
CREATE TABLE `erp_stock_out_item` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '出库项编号',
                                      `out_id` bigint NOT NULL COMMENT '出库编号',
                                      `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
                                      `product_id` bigint NOT NULL COMMENT '产品编号',
                                      `product_unit_id` bigint NOT NULL COMMENT '产品单位编号',
                                      `product_price` decimal(24,6) DEFAULT NULL COMMENT '产品单价',
                                      `count` decimal(24,6) NOT NULL COMMENT '产品数量',
                                      `total_price` decimal(24,6) DEFAULT NULL COMMENT '合计金额，单位：元',
                                      `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                      `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                      `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                      `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                      `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                      PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 其它出库单项表';

-- ----------------------------
-- Table structure for erp_stock_record
-- ----------------------------
DROP TABLE IF EXISTS `erp_stock_record`;
CREATE TABLE `erp_stock_record` (
                                    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
                                    `product_id` bigint NOT NULL COMMENT '产品编号',
                                    `warehouse_id` bigint NOT NULL COMMENT '仓库编号',
                                    `count` decimal(24,6) NOT NULL COMMENT '出入库数量',
                                    `total_count` decimal(24,6) NOT NULL COMMENT '总库存量',
                                    `biz_type` tinyint NOT NULL COMMENT '业务类型',
                                    `biz_id` bigint NOT NULL COMMENT '业务编号',
                                    `biz_item_id` bigint NOT NULL COMMENT '业务项编号',
                                    `biz_no` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务单号',
                                    `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                    `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                    PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=112 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 产品库存明细表';

-- ----------------------------
-- Table structure for erp_supplier
-- ----------------------------
DROP TABLE IF EXISTS `erp_supplier`;
CREATE TABLE `erp_supplier` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '供应商编号',
                                `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '供应商名称',
                                `contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系人',
                                `mobile` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号码',
                                `telephone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
                                `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电子邮箱',
                                `fax` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '传真',
                                `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                `status` tinyint NOT NULL COMMENT '开启状态',
                                `sort` int NOT NULL COMMENT '排序',
                                `tax_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '纳税人识别号',
                                `tax_percent` decimal(24,6) DEFAULT NULL COMMENT '税率',
                                `bank_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '开户行',
                                `bank_account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '开户账号',
                                `bank_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '开户地址',
                                `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                PRIMARY KEY (`id` DESC) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 供应商表';

-- ----------------------------
-- Table structure for erp_supplier_product
-- ----------------------------
DROP TABLE IF EXISTS `erp_supplier_product`;
CREATE TABLE `erp_supplier_product` (
                                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '供应商产品编号',
                                        `code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '供应商产品编码',
                                        `supplier_id` bigint NOT NULL COMMENT '供应商编号',
                                        `product_id` bigint NOT NULL COMMENT '产品编号',
                                        `package_height` float DEFAULT NULL COMMENT '包装高度',
                                        `package_length` float DEFAULT NULL COMMENT '包装长度',
                                        `package_weight` float DEFAULT NULL COMMENT '包装重量',
                                        `package_width` float DEFAULT NULL COMMENT '包装宽度',
                                        `purchase_price` float DEFAULT NULL COMMENT '采购价格',
                                        `purchase_price_currency_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '采购货币代码',
                                        `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                        `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                        `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                        `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                        `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                        PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='ERP 供应商产品表';

-- ----------------------------
-- Table structure for erp_warehouse
-- ----------------------------
DROP TABLE IF EXISTS `erp_warehouse`;
CREATE TABLE `erp_warehouse` (
                                 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '仓库编号',
                                 `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '仓库名称',
                                 `address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '仓库地址',
                                 `sort` bigint NOT NULL COMMENT '排序',
                                 `remark` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                                 `principal` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '负责人',
                                 `warehouse_price` decimal(24,6) DEFAULT NULL COMMENT '仓储费，单位：元',
                                 `truckage_price` decimal(24,6) DEFAULT NULL COMMENT '搬运费，单位：元',
                                 `status` tinyint NOT NULL COMMENT '开启状态',
                                 `default_status` bit(1) DEFAULT b'0' COMMENT '是否默认',
                                 `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                 `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
                                 PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ERP 仓库表';

SET FOREIGN_KEY_CHECKS = 1;
