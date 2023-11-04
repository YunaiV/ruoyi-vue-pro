SET NAMES utf8mb4;




-- ----------------------------
-- 回款表
-- ----------------------------
DROP TABLE IF EXISTS `crm_receivable`;
CREATE TABLE `crm_receivable`  (
   `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
   `no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '回款编号',
   `plan_id` bigint(20) NULL DEFAULT NULL COMMENT '回款计划ID',
   `customer_id` bigint(20) NULL DEFAULT NULL COMMENT '客户ID',
   `contract_id` bigint(20) NULL DEFAULT NULL COMMENT '合同ID',
   `check_status` tinyint(4) NULL DEFAULT NULL COMMENT '审批状态',
   `process_instance_id` bigint(20) NULL DEFAULT NULL COMMENT '工作流编号',
   `return_time` datetime NULL DEFAULT NULL COMMENT '回款日期',
   `return_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '回款方式',
   `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '回款金额',
   `owner_user_id` bigint(20) NULL DEFAULT NULL COMMENT '负责人的用户编号',
   `batch_id` bigint(20) NULL DEFAULT NULL COMMENT '批次',
   `sort` int(11) NULL DEFAULT NULL COMMENT '显示顺序',
   `data_scope` tinyint(4) NULL DEFAULT 1 COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
   `data_scope_dept_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '数据范围(指定部门数组)',
   `status` tinyint(4) NOT NULL COMMENT '状态（0正常 1停用）',
   `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
   `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
   `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
   `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
   `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
   `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
   PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '回款管理' ROW_FORMAT = DYNAMIC;


-- ----------------------------
-- 回款计划表
-- ----------------------------
DROP TABLE IF EXISTS `crm_receivable_plan`;
CREATE TABLE `crm_receivable_plan`  (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `period` tinyint(4) DEFAULT NULL COMMENT '期数',
    `receivable_id` bigint(20) NULL DEFAULT NULL COMMENT '回款ID',
    `status` tinyint(4) NOT NULL COMMENT '完成状态',
    `check_status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '审批状态',
    `process_instance_id` bigint(20) NULL DEFAULT NULL COMMENT '工作流编号',
    `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '计划回款金额',
    `return_time` datetime NULL DEFAULT NULL COMMENT '计划回款日期',
    `remind_days` bigint(20) NULL DEFAULT NULL COMMENT '提前几天提醒',
    `remind_time` datetime NULL DEFAULT NULL COMMENT '提醒日期',
    `customer_id` bigint(20) NULL DEFAULT NULL COMMENT '客户ID',
    `contract_id` bigint(20) NULL DEFAULT NULL COMMENT '合同ID',
    `owner_user_id` bigint(20) NULL DEFAULT NULL COMMENT '负责人',
    `sort` int(11) NULL DEFAULT NULL COMMENT '显示顺序',
    `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
    `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '回款计划' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 产品表
-- ----------------------------
DROP TABLE IF EXISTS `crm_product`;
CREATE TABLE `crm_product`
(
    `id`            bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `name`          varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品名称',
    `no`            varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NOT NULL COMMENT '产品编码',
    `unit`          varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '单位',
    `price`         bigint(20) NULL DEFAULT 0 COMMENT '价格',
    `status`        tinyint(2) NOT NULL DEFAULT 1 COMMENT '状态 1-上架 0-下架',
    `category_id`   bigint(20) NOT NULL COMMENT '产品分类ID',
    `description`   varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品描述',
    `owner_user_id` bigint(20) NOT NULL COMMENT '负责人的用户编号',
    `creator`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
    `create_time`   datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`       varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
    `update_time`   datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `tenant_id`     bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
    `deleted`       bit(1)                                                        NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '产品表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 产品分类表
-- ----------------------------
DROP TABLE IF EXISTS `crm_product_category`;
CREATE TABLE `crm_product_category`
(
    `id`          bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `name`        varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '名称',
    `parent_id`   bigint(20) NOT NULL COMMENT '父级id',
    `creator`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
    `create_time` datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
    `update_time` datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `tenant_id`   bigint(20) NOT NULL DEFAULT 0 COMMENT '租户编号',
    `deleted`     bit(1)                                                        NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '产品分类表' ROW_FORMAT = Dynamic;