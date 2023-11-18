SET NAMES utf8mb4;

CREATE TABLE `crm_contact` (
                               `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
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
                               `owner_user_id` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '负责人用户编号',
                               `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
                               `update_time` timestamp NULL DEFAULT NULL COMMENT '更新时间',
                               `last_time` timestamp NULL DEFAULT NULL COMMENT '最后跟进时间',
                               `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '更新人',
                               `deleted` bit(1) NOT NULL DEFAULT b'0',
                               `tenant_id` bigint(20) DEFAULT NULL,
                               `parent_id` bigint(20) DEFAULT NULL COMMENT '直系上属',
                               `qq` int(11) DEFAULT NULL,
                               `webchat` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                               `sex` int(1) DEFAULT NULL COMMENT '性别',
                               `policy_makers` bit(1) DEFAULT NULL COMMENT '是否关键决策人',
                               PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUAUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='crm联系人';