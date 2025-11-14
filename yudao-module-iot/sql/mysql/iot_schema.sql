-- ----------------------------
-- IoT 模块数据库初始化脚本 - MySQL
-- Author: AI Assistant
-- Date: 2025-11-14
-- Description: IoT 物联网模块的数据库表结构定义
-- ----------------------------

-- ----------------------------
-- 1. 产品管理相关表
-- ----------------------------

-- ----------------------------
-- Table structure for iot_product_category
-- ----------------------------
DROP TABLE IF EXISTS `iot_product_category`;
CREATE TABLE `iot_product_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(64) NOT NULL COMMENT '分类名称',
  `sort` int NOT NULL DEFAULT 0 COMMENT '分类排序',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '分类状态（0正常 1停用）',
  `description` varchar(500) DEFAULT NULL COMMENT '分类描述',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 产品分类表';

-- ----------------------------
-- Table structure for iot_product
-- ----------------------------
DROP TABLE IF EXISTS `iot_product`;
CREATE TABLE `iot_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '产品ID',
  `name` varchar(64) NOT NULL COMMENT '产品名称',
  `product_key` varchar(64) NOT NULL COMMENT '产品标识',
  `category_id` bigint DEFAULT NULL COMMENT '产品分类编号',
  `icon` varchar(255) DEFAULT NULL COMMENT '产品图标URL',
  `pic_url` varchar(255) DEFAULT NULL COMMENT '产品图片URL',
  `description` varchar(500) DEFAULT NULL COMMENT '产品描述',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '产品状态（0开发中 1已发布）',
  `device_type` tinyint NOT NULL DEFAULT 0 COMMENT '设备类型（0直连设备 1网关子设备 2网关设备）',
  `net_type` tinyint DEFAULT NULL COMMENT '联网方式',
  `location_type` tinyint DEFAULT NULL COMMENT '定位方式',
  `codec_type` varchar(32) DEFAULT NULL COMMENT '数据格式（编解码器类型）',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_key` (`product_key`) USING BTREE COMMENT '产品标识唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 产品表';

-- ----------------------------
-- 2. 设备管理相关表
-- ----------------------------

-- ----------------------------
-- Table structure for iot_device_group
-- ----------------------------
DROP TABLE IF EXISTS `iot_device_group`;
CREATE TABLE `iot_device_group` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分组ID',
  `name` varchar(64) NOT NULL COMMENT '分组名称',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '分组状态（0正常 1停用）',
  `description` varchar(500) DEFAULT NULL COMMENT '分组描述',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 设备分组表';

-- ----------------------------
-- Table structure for iot_device
-- ----------------------------
DROP TABLE IF EXISTS `iot_device`;
CREATE TABLE `iot_device` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设备ID',
  `device_name` varchar(64) NOT NULL COMMENT '设备名称',
  `nickname` varchar(64) DEFAULT NULL COMMENT '设备备注名称',
  `serial_number` varchar(64) DEFAULT NULL COMMENT '设备序列号',
  `pic_url` varchar(255) DEFAULT NULL COMMENT '设备图片URL',
  `group_ids` varchar(500) DEFAULT NULL COMMENT '设备分组编号集合（逗号分隔）',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  `product_key` varchar(64) NOT NULL COMMENT '产品标识',
  `device_type` tinyint NOT NULL DEFAULT 0 COMMENT '设备类型（0直连设备 1网关子设备 2网关设备）',
  `gateway_id` bigint DEFAULT NULL COMMENT '网关设备编号',
  `state` tinyint NOT NULL DEFAULT 0 COMMENT '设备状态（0未激活 1在线 2离线）',
  `online_time` datetime DEFAULT NULL COMMENT '最后上线时间',
  `offline_time` datetime DEFAULT NULL COMMENT '最后离线时间',
  `active_time` datetime DEFAULT NULL COMMENT '设备激活时间',
  `ip` varchar(64) DEFAULT NULL COMMENT '设备IP地址',
  `firmware_id` bigint DEFAULT NULL COMMENT '固件编号',
  `device_secret` varchar(64) DEFAULT NULL COMMENT '设备密钥',
  `auth_type` varchar(32) DEFAULT NULL COMMENT '认证类型',
  `location_type` tinyint DEFAULT NULL COMMENT '定位方式',
  `latitude` decimal(10,6) DEFAULT NULL COMMENT '设备位置纬度',
  `longitude` decimal(10,6) DEFAULT NULL COMMENT '设备位置经度',
  `area_id` int DEFAULT NULL COMMENT '地区编码',
  `address` varchar(255) DEFAULT NULL COMMENT '设备详细地址',
  `config` text DEFAULT NULL COMMENT '设备配置（JSON格式）',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_product_device` (`product_id`, `device_name`, `tenant_id`) USING BTREE COMMENT '产品设备联合索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 设备表';

-- ----------------------------
-- 3. 物模型相关表
-- ----------------------------

-- ----------------------------
-- Table structure for iot_thing_model
-- ----------------------------
DROP TABLE IF EXISTS `iot_thing_model`;
CREATE TABLE `iot_thing_model` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '物模型功能编号',
  `identifier` varchar(64) NOT NULL COMMENT '功能标识',
  `name` varchar(64) NOT NULL COMMENT '功能名称',
  `description` varchar(500) DEFAULT NULL COMMENT '功能描述',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  `product_key` varchar(64) NOT NULL COMMENT '产品标识',
  `type` tinyint NOT NULL COMMENT '功能类型（1属性 2服务 3事件）',
  `property` text DEFAULT NULL COMMENT '属性定义（JSON格式）',
  `event` text DEFAULT NULL COMMENT '事件定义（JSON格式）',
  `service` text DEFAULT NULL COMMENT '服务定义（JSON格式）',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_product_identifier` (`product_id`, `identifier`) USING BTREE COMMENT '产品标识符索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 产品物模型功能表';

-- ----------------------------
-- 4. 告警管理相关表
-- ----------------------------

-- ----------------------------
-- Table structure for iot_alert_config
-- ----------------------------
DROP TABLE IF EXISTS `iot_alert_config`;
CREATE TABLE `iot_alert_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置编号',
  `name` varchar(64) NOT NULL COMMENT '配置名称',
  `description` varchar(500) DEFAULT NULL COMMENT '配置描述',
  `level` tinyint NOT NULL COMMENT '告警级别',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '配置状态（0正常 1停用）',
  `scene_rule_ids` varchar(500) DEFAULT NULL COMMENT '关联的场景规则编号数组（逗号分隔）',
  `receive_user_ids` varchar(500) DEFAULT NULL COMMENT '接收用户编号数组（逗号分隔）',
  `receive_types` varchar(255) DEFAULT NULL COMMENT '接收类型数组（逗号分隔）',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 告警配置表';

-- ----------------------------
-- Table structure for iot_alert_record
-- ----------------------------
DROP TABLE IF EXISTS `iot_alert_record`;
CREATE TABLE `iot_alert_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录编号',
  `config_id` bigint NOT NULL COMMENT '告警配置编号',
  `config_name` varchar(64) NOT NULL COMMENT '告警名称',
  `config_level` tinyint NOT NULL COMMENT '告警级别',
  `scene_rule_id` bigint DEFAULT NULL COMMENT '场景规则编号',
  `product_id` bigint DEFAULT NULL COMMENT '产品编号',
  `device_id` bigint DEFAULT NULL COMMENT '设备编号',
  `device_message` text DEFAULT NULL COMMENT '触发的设备消息（JSON格式）',
  `process_status` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否处理',
  `process_remark` varchar(500) DEFAULT NULL COMMENT '处理结果',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_config_device` (`config_id`, `device_id`) USING BTREE COMMENT '配置设备索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 告警记录表';

-- ----------------------------
-- 5. 规则引擎相关表
-- ----------------------------

-- ----------------------------
-- Table structure for iot_data_rule
-- ----------------------------
DROP TABLE IF EXISTS `iot_data_rule`;
CREATE TABLE `iot_data_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '规则编号',
  `name` varchar(64) NOT NULL COMMENT '规则名称',
  `description` varchar(500) DEFAULT NULL COMMENT '规则描述',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '规则状态（0正常 1停用）',
  `source_configs` text DEFAULT NULL COMMENT '数据源配置数组（JSON格式）',
  `sink_ids` varchar(500) DEFAULT NULL COMMENT '数据目的编号数组（逗号分隔）',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 数据流转规则表';

-- ----------------------------
-- Table structure for iot_data_sink
-- ----------------------------
DROP TABLE IF EXISTS `iot_data_sink`;
CREATE TABLE `iot_data_sink` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '目的编号',
  `name` varchar(64) NOT NULL COMMENT '目的名称',
  `description` varchar(500) DEFAULT NULL COMMENT '目的描述',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '目的状态（0正常 1停用）',
  `type` tinyint NOT NULL COMMENT '目的类型',
  `config` text DEFAULT NULL COMMENT '目的配置（JSON格式）',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 数据流转目的表';

-- ----------------------------
-- Table structure for iot_scene_rule
-- ----------------------------
DROP TABLE IF EXISTS `iot_scene_rule`;
CREATE TABLE `iot_scene_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '场景联动编号',
  `name` varchar(64) NOT NULL COMMENT '场景名称',
  `description` varchar(500) DEFAULT NULL COMMENT '场景描述',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '场景状态（0正常 1停用）',
  `triggers` text DEFAULT NULL COMMENT '触发条件配置（JSON格式）',
  `actions` text DEFAULT NULL COMMENT '执行动作配置（JSON格式）',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT 场景联动规则表';

-- ----------------------------
-- 6. OTA 升级相关表
-- ----------------------------

-- ----------------------------
-- Table structure for iot_ota_firmware
-- ----------------------------
DROP TABLE IF EXISTS `iot_ota_firmware`;
CREATE TABLE `iot_ota_firmware` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '固件编号',
  `name` varchar(64) NOT NULL COMMENT '固件名称',
  `description` varchar(500) DEFAULT NULL COMMENT '固件描述',
  `version` varchar(32) NOT NULL COMMENT '版本号',
  `product_id` bigint NOT NULL COMMENT '产品编号',
  `file_url` varchar(255) NOT NULL COMMENT '固件文件URL',
  `file_size` bigint NOT NULL COMMENT '固件文件大小（字节）',
  `file_digest_algorithm` varchar(32) DEFAULT NULL COMMENT '签名算法（如MD5）',
  `file_digest_value` varchar(255) DEFAULT NULL COMMENT '签名值',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_product_version` (`product_id`, `version`) USING BTREE COMMENT '产品版本索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT OTA 固件表';

-- ----------------------------
-- Table structure for iot_ota_task
-- ----------------------------
DROP TABLE IF EXISTS `iot_ota_task`;
CREATE TABLE `iot_ota_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务编号',
  `name` varchar(64) NOT NULL COMMENT '任务名称',
  `description` varchar(500) DEFAULT NULL COMMENT '任务描述',
  `firmware_id` bigint NOT NULL COMMENT '固件编号',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '任务状态',
  `device_scope` tinyint NOT NULL COMMENT '设备升级范围',
  `device_total_count` int NOT NULL DEFAULT 0 COMMENT '设备总数',
  `device_success_count` int NOT NULL DEFAULT 0 COMMENT '成功升级数量',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_firmware` (`firmware_id`) USING BTREE COMMENT '固件索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT OTA 升级任务表';

-- ----------------------------
-- Table structure for iot_ota_task_record
-- ----------------------------
DROP TABLE IF EXISTS `iot_ota_task_record`;
CREATE TABLE `iot_ota_task_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录编号',
  `firmware_id` bigint NOT NULL COMMENT '固件编号',
  `task_id` bigint NOT NULL COMMENT '任务编号',
  `device_id` bigint NOT NULL COMMENT '设备编号',
  `from_firmware_id` bigint DEFAULT NULL COMMENT '来源固件编号',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '升级状态',
  `progress` tinyint NOT NULL DEFAULT 0 COMMENT '升级进度（百分比）',
  `description` varchar(500) DEFAULT NULL COMMENT '升级进度描述',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_task_device` (`task_id`, `device_id`) USING BTREE COMMENT '任务设备索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='IoT OTA 升级任务记录表';
