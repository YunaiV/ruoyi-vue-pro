-- `ruoyi-vue-pro`.crm_contact_business_link definition

CREATE TABLE `crm_contact_business_link` (
                                             `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                             `contact_id` int(11) DEFAULT NULL COMMENT '联系人id',
                                             `business_id` int(11) DEFAULT NULL COMMENT '商机id',
                                             `creator` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
                                             `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             `updater` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
                                             `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                             `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
                                             `tenant_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '租户编号',
                                             PRIMARY KEY (`id`),
                                             UNIQUE KEY `crm_contact_business_link_un` (`contact_id`,`business_id`,`deleted`,`tenant_id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='联系人商机关联表';