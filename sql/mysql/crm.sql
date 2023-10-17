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
