SET NAMES utf8mb4;

DROP TABLE IF EXISTS `crm_customer_limit_config`;
CREATE TABLE `crm_customer_limit_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `type` int NOT NULL COMMENT '规则类型 1: 拥有客户数限制，2:锁定客户数限制',
  `user_ids` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '规则适用人群',
  `dept_ids` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '规则适用部门',
  `max_count` int NOT NULL COMMENT '数量上限',
  `deal_count_enabled` tinyint NULL DEFAULT NULL COMMENT '成交客户是否占有拥有客户数(当 type = 1 时)',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '客户限制配置表' ROW_FORMAT = DYNAMIC;


DROP TABLE IF EXISTS `crm_customer_pool_config`;
CREATE TABLE `crm_customer_pool_config`  (
 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
 `enabled` tinyint(1) NOT NULL COMMENT '是否启用客户公海',
 `contact_expire_days` int NULL DEFAULT NULL COMMENT '未跟进放入公海天数',
 `deal_expire_days` int NULL DEFAULT NULL COMMENT '未成交放入公海天数',
 `notify_enabled` tinyint(1) NULL DEFAULT NULL COMMENT '是否开启提前提醒',
 `notify_days` int NULL DEFAULT NULL COMMENT '提前提醒天数',
 `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
 `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
 `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '客户公海配置表' ROW_FORMAT = DYNAMIC;