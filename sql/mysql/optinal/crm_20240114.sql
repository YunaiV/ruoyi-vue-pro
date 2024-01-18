
-- ----------------------------
-- Table structure for crm_business_product
-- ----------------------------
DROP TABLE IF EXISTS `crm_business_product`;
CREATE TABLE `crm_business_product`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `business_id` bigint(0) NOT NULL COMMENT '商机ID',
  `product_id` bigint(0) NOT NULL COMMENT '产品ID',
  `price` decimal(18, 2) NOT NULL COMMENT '产品单价',
  `sales_price` decimal(18, 2) NULL DEFAULT NULL COMMENT '销售价格',
  `num` int(0) NULL DEFAULT NULL COMMENT '数量',
  `discount` decimal(10, 2) NULL DEFAULT NULL COMMENT '折扣',
  `subtotal` decimal(18, 2) NULL DEFAULT NULL COMMENT '小计（折扣后价格）',
  `unit` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '单位',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint(0) NOT NULL DEFAULT 1 COMMENT '租户编号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商机产品关联表' ROW_FORMAT = Dynamic;