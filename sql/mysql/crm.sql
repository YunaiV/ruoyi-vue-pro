SET NAMES utf8mb4;

-- ----------------------------
-- 合同表
-- ----------------------------
DROP TABLE IF EXISTS `crm_contract`;
CREATE TABLE `crm_contract`
(
    `id`                  bigint                                                        NOT NULL AUTO_INCREMENT COMMENT '编号，主键自增',
    `name`                varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '合同名称',
    `customer_id`         bigint                                                                 DEFAULT NULL COMMENT '客户编号',
    `business_id`         bigint                                                                 DEFAULT NULL COMMENT '商机编号',
    `process_instance_id` bigint                                                                 DEFAULT NULL COMMENT '工作流编号',
    `order_date`          datetime                                                               DEFAULT NULL COMMENT '下单日期',
    `owner_user_id`       bigint                                                                 DEFAULT NULL COMMENT '负责人的用户编号',
    `no`                  varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci          DEFAULT NULL COMMENT '合同编号',
    `start_time`          datetime                                                               DEFAULT NULL COMMENT '开始时间',
    `end_time`            datetime                                                               DEFAULT NULL COMMENT '结束时间',
    `price`               int                                                                    DEFAULT NULL COMMENT '合同金额',
    `discount_percent`    int                                                                    DEFAULT NULL COMMENT '整单折扣',
    `product_price`       int                                                                    DEFAULT NULL COMMENT '产品总金额',
    `ro_user_ids`         varchar(4096)                                                          DEFAULT NULL COMMENT '只读权限的用户编号数组',
    `rw_user_ids`         varchar(4096)                                                          DEFAULT NULL COMMENT '读写权限的用户编号数组',
    `contact_id`          bigint                                                                 DEFAULT NULL COMMENT '联系人编号',
    `sign_user_id`        bigint                                                                 DEFAULT NULL COMMENT '公司签约人',
    `contact_last_time`   datetime                                                               DEFAULT NULL COMMENT '最后跟进时间',

    -- 通用字段
    `remark`              varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL     DEFAULT NULL COMMENT '备注',
    `creator`             varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL     DEFAULT '' COMMENT '创建者',
    `create_time`         datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater`             varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci  NULL     DEFAULT '' COMMENT '更新者',
    `update_time`         datetime                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`             bit(1)                                                        NOT NULL DEFAULT b'0' COMMENT '是否删除',
    `tenant_id`           bigint                                                        NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT ='合同表';


DROP TABLE IF EXISTS `crm_clue`;
CREATE TABLE `crm_clue`  (
                             `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号，主键自增',
                             `transform_status` tinyint DEFAULT NULL COMMENT '转化状态',
                             `follow_up_status` tinyint DEFAULT NULL COMMENT '跟进状态',
                             `name` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '线索名称',
                             `customer_id` bigint NOT NULL COMMENT '客户id',
                             `contact_next_time` datetime DEFAULT NULL COMMENT '下次联系时间',
                             `telephone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '电话',
                             `mobile` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
                             `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '地址',
                             `owner_user_id` bigint NOT NULL COMMENT '负责人的用户编号',
                             `contact_last_time` datetime DEFAULT NULL COMMENT '最后跟进时间',
                             `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
                             `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                             `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                             `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                             `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                             `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
                             PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB COMMENT = '线索表' ;

-- ----------------------------
-- 商机表
-- ----------------------------

DROP TABLE IF EXISTS `crm_business`;
CREATE TABLE `crm_business` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                `name` varchar(100) NOT NULL COMMENT '商机名称',
                                `status_type_id` bigint DEFAULT NULL COMMENT '商机状态类型编号',
                                `status_id` bigint DEFAULT NULL COMMENT '商机状态编号',
                                `contact_next_time` datetime DEFAULT NULL COMMENT '下次联系时间',
                                `customer_id` bigint NOT NULL COMMENT '客户编号',
                                `deal_time` datetime DEFAULT NULL COMMENT '预计成交日期',
                                `price` decimal(18,2) DEFAULT NULL COMMENT '商机金额',
                                `discount_percent` decimal(10,2) DEFAULT NULL COMMENT '整单折扣',
                                `product_price` decimal(18,2) DEFAULT NULL COMMENT '产品总金额',
                                `remark` varchar(500) DEFAULT NULL COMMENT '备注',
                                `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '创建人',
                                `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '' COMMENT '更新人',
                                `owner_user_id` bigint DEFAULT NULL COMMENT '负责人的用户编号',
                                `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `update_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
                                `ro_user_ids` longtext NOT NULL COMMENT '只读权限的用户编号数组',
                                `rw_user_ids` longtext NOT NULL COMMENT '读写权限的用户编号数组',
                                `end_status` int NOT NULL COMMENT '1赢单2输单3无效',
                                `end_remark` varchar(500) DEFAULT NULL COMMENT '结束时的备注',
                                `deleted` bit(1) DEFAULT b'0' COMMENT '逻辑删除',
                                `contact_last_time` datetime DEFAULT NULL COMMENT '最后跟进时间',
                                `follow_up_status` int DEFAULT NULL COMMENT '跟进状态',
                                `tenant_id` bigint DEFAULT '0' COMMENT '租户ID',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商机表';



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

-- ----------------------------
-- 商机状态表
-- ----------------------------
DROP TABLE IF EXISTS `crm_business_status`;
CREATE TABLE `crm_business_status` (
                                       `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                       `type_id` bigint NOT NULL COMMENT '状态类型编号',
                                       `name` varchar(100) NOT NULL COMMENT '状态名',
                                       `percent` varchar(20) DEFAULT NULL COMMENT '赢单率',
                                       `sort` int DEFAULT NULL COMMENT '排序',
                                       `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商机状态表'

-- ----------------------------
-- 商机状态类型表
-- ----------------------------
DROP TABLE IF EXISTS `crm_business_status_type`;
CREATE TABLE `crm_business_status_type` (
                                            `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
                                            `name` varchar(100) NOT NULL COMMENT '状态类型名',
                                            `dept_ids` varchar(200) NOT NULL COMMENT '使用的部门编号',
                                            `status` bit(1) NOT NULL DEFAULT b'0' COMMENT '开启状态',
                                            `creator` varchar(64) NOT NULL COMMENT '创建人',
                                            `create_time` datetime NOT NULL COMMENT '创建时间',
                                            `update_time` datetime DEFAULT NULL COMMENT '更新时间',
                                            `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
                                            PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商机状态类型表'
