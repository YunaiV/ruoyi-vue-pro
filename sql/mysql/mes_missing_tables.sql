-- Missing MES tables needed by home statistics and other services
CREATE TABLE IF NOT EXISTS `mes_pro_work_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工单ID',
  `code` varchar(64) NOT NULL COMMENT '工单编码',
  `name` varchar(255) NOT NULL COMMENT '工单名称',
  `type` int DEFAULT NULL COMMENT '工单类型',
  `order_source_type` int DEFAULT NULL COMMENT '来源类型',
  `order_source_code` varchar(64) DEFAULT NULL COMMENT '来源单据编号',
  `product_id` bigint DEFAULT NULL COMMENT '物料产品编号',
  `quantity` decimal(19,2) DEFAULT NULL COMMENT '生产数量',
  `quantity_produced` decimal(19,2) DEFAULT NULL COMMENT '已生产数量',
  `quantity_changed` decimal(19,2) DEFAULT NULL COMMENT '调整数量',
  `quantity_scheduled` decimal(19,2) DEFAULT NULL COMMENT '已排产数量',
  `client_id` bigint DEFAULT NULL COMMENT '客户编号',
  `vendor_id` bigint DEFAULT NULL COMMENT '供应商编号',
  `batch_code` varchar(64) DEFAULT NULL COMMENT '批次号',
  `request_date` datetime DEFAULT NULL COMMENT '需求日期',
  `parent_id` bigint DEFAULT '0' COMMENT '父工单编号',
  `finish_date` datetime DEFAULT NULL COMMENT '完成时间',
  `cancel_date` datetime DEFAULT NULL COMMENT '取消时间',
  `status` int DEFAULT NULL COMMENT '工单状态',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MES 生产工单';

CREATE TABLE IF NOT EXISTS `mes_dv_machinery` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `code` varchar(64) NOT NULL COMMENT '设备编码',
  `name` varchar(255) NOT NULL COMMENT '设备名称',
  `brand` varchar(255) DEFAULT NULL COMMENT '品牌',
  `specification` varchar(255) DEFAULT NULL COMMENT '规格型号',
  `machinery_type_id` bigint DEFAULT NULL COMMENT '设备类型编号',
  `workshop_id` bigint DEFAULT NULL COMMENT '车间编号',
  `status` int DEFAULT NULL COMMENT '设备状态',
  `last_mainten_time` datetime DEFAULT NULL COMMENT '最近保养时间',
  `last_check_time` datetime DEFAULT NULL COMMENT '最近点检时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MES 设备台账';

CREATE TABLE IF NOT EXISTS `mes_pro_andon_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '安灯记录ID',
  `config_id` bigint DEFAULT NULL COMMENT '安灯配置编号',
  `workstation_id` bigint DEFAULT NULL COMMENT '工作站编号',
  `user_id` bigint DEFAULT NULL COMMENT '用户编号',
  `work_order_id` bigint DEFAULT NULL COMMENT '工单编号',
  `process_id` bigint DEFAULT NULL COMMENT '工序编号',
  `reason` varchar(500) DEFAULT NULL COMMENT '呼叫原因',
  `level` int DEFAULT NULL COMMENT '级别',
  `status` int DEFAULT NULL COMMENT '处置状态',
  `handle_time` datetime DEFAULT NULL COMMENT '处置时间',
  `handler_user_id` bigint DEFAULT NULL COMMENT '处置人编号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MES 安灯记录';

CREATE TABLE IF NOT EXISTS `mes_dv_repair` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '维修工单ID',
  `code` varchar(64) NOT NULL COMMENT '维修工单编码',
  `name` varchar(255) NOT NULL COMMENT '维修工单名称',
  `machinery_id` bigint DEFAULT NULL COMMENT '设备编号',
  `require_date` datetime DEFAULT NULL COMMENT '报修日期',
  `finish_date` datetime DEFAULT NULL COMMENT '维修完成日期',
  `confirm_date` datetime DEFAULT NULL COMMENT '验收日期',
  `result` int DEFAULT NULL COMMENT '维修结果',
  `accepted_user_id` bigint DEFAULT NULL COMMENT '维修人编号',
  `confirm_user_id` bigint DEFAULT NULL COMMENT '验收人编号',
  `source_doc_type` int DEFAULT NULL COMMENT '来源单据类型',
  `source_doc_id` bigint DEFAULT NULL COMMENT '来源单据编号',
  `source_doc_code` varchar(64) DEFAULT NULL COMMENT '来源单据编码',
  `status` int DEFAULT NULL COMMENT '状态',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MES 维修工单';

CREATE TABLE IF NOT EXISTS `mes_dv_repair_line` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '维修工单行ID',
  `repair_id` bigint NOT NULL COMMENT '维修工单编号',
  `subject_id` bigint DEFAULT NULL COMMENT '点检保养项目编号',
  `malfunction` varchar(500) DEFAULT NULL COMMENT '故障描述',
  `malfunction_url` varchar(500) DEFAULT NULL COMMENT '故障图片URL',
  `description` varchar(500) DEFAULT NULL COMMENT '维修描述',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MES 维修工单行';

CREATE TABLE IF NOT EXISTS `mes_dv_mainten_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '保养记录ID',
  `plan_id` bigint DEFAULT NULL COMMENT '保养方案编号',
  `machinery_id` bigint DEFAULT NULL COMMENT '设备编号',
  `mainten_time` datetime DEFAULT NULL COMMENT '保养时间',
  `user_id` bigint DEFAULT NULL COMMENT '用户编号',
  `status` int DEFAULT NULL COMMENT '状态',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MES 保养记录';

CREATE TABLE IF NOT EXISTS `mes_dv_check_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点检记录ID',
  `plan_id` bigint DEFAULT NULL COMMENT '点检方案编号',
  `machinery_id` bigint DEFAULT NULL COMMENT '设备编号',
  `check_time` datetime DEFAULT NULL COMMENT '点检时间',
  `user_id` bigint DEFAULT NULL COMMENT '用户编号',
  `status` int DEFAULT NULL COMMENT '状态',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MES 点检记录';

CREATE TABLE IF NOT EXISTS `mes_dv_check_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '保养/点检方案ID',
  `code` varchar(64) NOT NULL COMMENT '方案编码',
  `name` varchar(255) NOT NULL COMMENT '方案名称',
  `type` int DEFAULT NULL COMMENT '方案类型',
  `start_date` datetime DEFAULT NULL COMMENT '开始日期',
  `end_date` datetime DEFAULT NULL COMMENT '结束日期',
  `cycle_type` int DEFAULT NULL COMMENT '周期类型',
  `cycle_count` int DEFAULT NULL COMMENT '周期数量',
  `status` int DEFAULT NULL COMMENT '状态',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT NULL COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='MES 保养/点检方案';
