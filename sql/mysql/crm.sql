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
    `index_no` bigint(20) NULL DEFAULT NULL COMMENT '期数',
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






CREATE TABLE `crm_contact` (
                               `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                               `name` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系人名称',
                               `next_time` datetime DEFAULT NULL COMMENT '下次联系时间',
                               `mobile` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
                               `telephone` varchar(16) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电话',
                               `email` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电子邮箱',
                               `post` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '职务',
                               `customer_id` bigint(20) DEFAULT NULL COMMENT '客户编号',
                               `address` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '地址',
                               `remark` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                               `creator` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '创建人',
                               `owner_user_id` bigint DEFAULT NULL COMMENT '负责人用户编号',
                               `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
                               `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
                               `last_time` timestamp NULL DEFAULT NULL COMMENT '最后跟进时间',
                               `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
                               `deleted` bit(1) NOT NULL DEFAULT b'0',
                               `tenant_id` bigint DEFAULT NULL,
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='crm联系人';

