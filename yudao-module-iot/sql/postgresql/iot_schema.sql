-- ----------------------------
-- IoT 模块数据库初始化脚本 - PostgreSQL
-- Author: AI Assistant
-- Date: 2025-11-14
-- Description: IoT 物联网模块的数据库表结构定义
-- ----------------------------

-- ----------------------------
-- 1. 产品管理相关表
-- ----------------------------

-- ----------------------------
-- Sequence for iot_product_category
-- ----------------------------
DROP SEQUENCE IF EXISTS iot_product_category_seq;
CREATE SEQUENCE iot_product_category_seq START 1;

-- ----------------------------
-- Table structure for iot_product_category
-- ----------------------------
DROP TABLE IF EXISTS iot_product_category;
CREATE TABLE iot_product_category (
  id BIGINT NOT NULL DEFAULT nextval('iot_product_category_seq'),
  name VARCHAR(64) NOT NULL,
  sort INTEGER NOT NULL DEFAULT 0,
  status SMALLINT NOT NULL DEFAULT 0,
  description VARCHAR(500),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id)
);

COMMENT ON TABLE iot_product_category IS 'IoT 产品分类表';
COMMENT ON COLUMN iot_product_category.id IS '分类ID';
COMMENT ON COLUMN iot_product_category.name IS '分类名称';
COMMENT ON COLUMN iot_product_category.sort IS '分类排序';
COMMENT ON COLUMN iot_product_category.status IS '分类状态（0正常 1停用）';
COMMENT ON COLUMN iot_product_category.description IS '分类描述';
COMMENT ON COLUMN iot_product_category.creator IS '创建者';
COMMENT ON COLUMN iot_product_category.create_time IS '创建时间';
COMMENT ON COLUMN iot_product_category.updater IS '更新者';
COMMENT ON COLUMN iot_product_category.update_time IS '更新时间';
COMMENT ON COLUMN iot_product_category.deleted IS '是否删除';

-- ----------------------------
-- Sequence for iot_product
-- ----------------------------
DROP SEQUENCE IF EXISTS iot_product_seq;
CREATE SEQUENCE iot_product_seq START 1;

-- ----------------------------
-- Table structure for iot_product
-- ----------------------------
DROP TABLE IF EXISTS iot_product;
CREATE TABLE iot_product (
  id BIGINT NOT NULL DEFAULT nextval('iot_product_seq'),
  name VARCHAR(64) NOT NULL,
  product_key VARCHAR(64) NOT NULL,
  category_id BIGINT,
  icon VARCHAR(255),
  pic_url VARCHAR(255),
  description VARCHAR(500),
  status SMALLINT NOT NULL DEFAULT 0,
  device_type SMALLINT NOT NULL DEFAULT 0,
  net_type SMALLINT,
  location_type SMALLINT,
  codec_type VARCHAR(32),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT uk_product_key UNIQUE (product_key)
);

COMMENT ON TABLE iot_product IS 'IoT 产品表';
COMMENT ON COLUMN iot_product.id IS '产品ID';
COMMENT ON COLUMN iot_product.name IS '产品名称';
COMMENT ON COLUMN iot_product.product_key IS '产品标识';
COMMENT ON COLUMN iot_product.category_id IS '产品分类编号';
COMMENT ON COLUMN iot_product.icon IS '产品图标URL';
COMMENT ON COLUMN iot_product.pic_url IS '产品图片URL';
COMMENT ON COLUMN iot_product.description IS '产品描述';
COMMENT ON COLUMN iot_product.status IS '产品状态（0开发中 1已发布）';
COMMENT ON COLUMN iot_product.device_type IS '设备类型（0直连设备 1网关子设备 2网关设备）';
COMMENT ON COLUMN iot_product.net_type IS '联网方式';
COMMENT ON COLUMN iot_product.location_type IS '定位方式';
COMMENT ON COLUMN iot_product.codec_type IS '数据格式（编解码器类型）';
COMMENT ON COLUMN iot_product.creator IS '创建者';
COMMENT ON COLUMN iot_product.create_time IS '创建时间';
COMMENT ON COLUMN iot_product.updater IS '更新者';
COMMENT ON COLUMN iot_product.update_time IS '更新时间';
COMMENT ON COLUMN iot_product.deleted IS '是否删除';
COMMENT ON COLUMN iot_product.tenant_id IS '租户编号';

-- ----------------------------
-- 2. 设备管理相关表
-- ----------------------------

-- ----------------------------
-- Sequence for iot_device_group
-- ----------------------------
DROP SEQUENCE IF EXISTS iot_device_group_seq;
CREATE SEQUENCE iot_device_group_seq START 1;

-- ----------------------------
-- Table structure for iot_device_group
-- ----------------------------
DROP TABLE IF EXISTS iot_device_group;
CREATE TABLE iot_device_group (
  id BIGINT NOT NULL DEFAULT nextval('iot_device_group_seq'),
  name VARCHAR(64) NOT NULL,
  status SMALLINT NOT NULL DEFAULT 0,
  description VARCHAR(500),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id)
);

COMMENT ON TABLE iot_device_group IS 'IoT 设备分组表';
COMMENT ON COLUMN iot_device_group.id IS '分组ID';
COMMENT ON COLUMN iot_device_group.name IS '分组名称';
COMMENT ON COLUMN iot_device_group.status IS '分组状态（0正常 1停用）';
COMMENT ON COLUMN iot_device_group.description IS '分组描述';
COMMENT ON COLUMN iot_device_group.creator IS '创建者';
COMMENT ON COLUMN iot_device_group.create_time IS '创建时间';
COMMENT ON COLUMN iot_device_group.updater IS '更新者';
COMMENT ON COLUMN iot_device_group.update_time IS '更新时间';
COMMENT ON COLUMN iot_device_group.deleted IS '是否删除';

-- ----------------------------
-- Sequence for iot_device
-- ----------------------------
DROP SEQUENCE IF EXISTS iot_device_seq;
CREATE SEQUENCE iot_device_seq START 1;

-- ----------------------------
-- Table structure for iot_device
-- ----------------------------
DROP TABLE IF EXISTS iot_device;
CREATE TABLE iot_device (
  id BIGINT NOT NULL DEFAULT nextval('iot_device_seq'),
  device_name VARCHAR(64) NOT NULL,
  nickname VARCHAR(64),
  serial_number VARCHAR(64),
  pic_url VARCHAR(255),
  group_ids VARCHAR(500),
  product_id BIGINT NOT NULL,
  product_key VARCHAR(64) NOT NULL,
  device_type SMALLINT NOT NULL DEFAULT 0,
  gateway_id BIGINT,
  state SMALLINT NOT NULL DEFAULT 0,
  online_time TIMESTAMP,
  offline_time TIMESTAMP,
  active_time TIMESTAMP,
  ip VARCHAR(64),
  firmware_id BIGINT,
  device_secret VARCHAR(64),
  auth_type VARCHAR(32),
  location_type SMALLINT,
  latitude DECIMAL(10,6),
  longitude DECIMAL(10,6),
  area_id INTEGER,
  address VARCHAR(255),
  config TEXT,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

CREATE INDEX idx_product_device ON iot_device (product_id, device_name, tenant_id);

COMMENT ON TABLE iot_device IS 'IoT 设备表';
COMMENT ON COLUMN iot_device.id IS '设备ID';
COMMENT ON COLUMN iot_device.device_name IS '设备名称';
COMMENT ON COLUMN iot_device.nickname IS '设备备注名称';
COMMENT ON COLUMN iot_device.serial_number IS '设备序列号';
COMMENT ON COLUMN iot_device.pic_url IS '设备图片URL';
COMMENT ON COLUMN iot_device.group_ids IS '设备分组编号集合（逗号分隔）';
COMMENT ON COLUMN iot_device.product_id IS '产品编号';
COMMENT ON COLUMN iot_device.product_key IS '产品标识';
COMMENT ON COLUMN iot_device.device_type IS '设备类型（0直连设备 1网关子设备 2网关设备）';
COMMENT ON COLUMN iot_device.gateway_id IS '网关设备编号';
COMMENT ON COLUMN iot_device.state IS '设备状态（0未激活 1在线 2离线）';
COMMENT ON COLUMN iot_device.online_time IS '最后上线时间';
COMMENT ON COLUMN iot_device.offline_time IS '最后离线时间';
COMMENT ON COLUMN iot_device.active_time IS '设备激活时间';
COMMENT ON COLUMN iot_device.ip IS '设备IP地址';
COMMENT ON COLUMN iot_device.firmware_id IS '固件编号';
COMMENT ON COLUMN iot_device.device_secret IS '设备密钥';
COMMENT ON COLUMN iot_device.auth_type IS '认证类型';
COMMENT ON COLUMN iot_device.location_type IS '定位方式';
COMMENT ON COLUMN iot_device.latitude IS '设备位置纬度';
COMMENT ON COLUMN iot_device.longitude IS '设备位置经度';
COMMENT ON COLUMN iot_device.area_id IS '地区编码';
COMMENT ON COLUMN iot_device.address IS '设备详细地址';
COMMENT ON COLUMN iot_device.config IS '设备配置（JSON格式）';
COMMENT ON COLUMN iot_device.creator IS '创建者';
COMMENT ON COLUMN iot_device.create_time IS '创建时间';
COMMENT ON COLUMN iot_device.updater IS '更新者';
COMMENT ON COLUMN iot_device.update_time IS '更新时间';
COMMENT ON COLUMN iot_device.deleted IS '是否删除';
COMMENT ON COLUMN iot_device.tenant_id IS '租户编号';

-- ----------------------------
-- 3. 物模型相关表
-- ----------------------------

-- ----------------------------
-- Sequence for iot_thing_model
-- ----------------------------
DROP SEQUENCE IF EXISTS iot_thing_model_seq;
CREATE SEQUENCE iot_thing_model_seq START 1;

-- ----------------------------
-- Table structure for iot_thing_model
-- ----------------------------
DROP TABLE IF EXISTS iot_thing_model;
CREATE TABLE iot_thing_model (
  id BIGINT NOT NULL DEFAULT nextval('iot_thing_model_seq'),
  identifier VARCHAR(64) NOT NULL,
  name VARCHAR(64) NOT NULL,
  description VARCHAR(500),
  product_id BIGINT NOT NULL,
  product_key VARCHAR(64) NOT NULL,
  type SMALLINT NOT NULL,
  property TEXT,
  event TEXT,
  service TEXT,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id)
);

CREATE INDEX idx_product_identifier ON iot_thing_model (product_id, identifier);

COMMENT ON TABLE iot_thing_model IS 'IoT 产品物模型功能表';
COMMENT ON COLUMN iot_thing_model.id IS '物模型功能编号';
COMMENT ON COLUMN iot_thing_model.identifier IS '功能标识';
COMMENT ON COLUMN iot_thing_model.name IS '功能名称';
COMMENT ON COLUMN iot_thing_model.description IS '功能描述';
COMMENT ON COLUMN iot_thing_model.product_id IS '产品编号';
COMMENT ON COLUMN iot_thing_model.product_key IS '产品标识';
COMMENT ON COLUMN iot_thing_model.type IS '功能类型（1属性 2服务 3事件）';
COMMENT ON COLUMN iot_thing_model.property IS '属性定义（JSON格式）';
COMMENT ON COLUMN iot_thing_model.event IS '事件定义（JSON格式）';
COMMENT ON COLUMN iot_thing_model.service IS '服务定义（JSON格式）';
COMMENT ON COLUMN iot_thing_model.creator IS '创建者';
COMMENT ON COLUMN iot_thing_model.create_time IS '创建时间';
COMMENT ON COLUMN iot_thing_model.updater IS '更新者';
COMMENT ON COLUMN iot_thing_model.update_time IS '更新时间';
COMMENT ON COLUMN iot_thing_model.deleted IS '是否删除';

-- ----------------------------
-- 4. 告警管理相关表
-- ----------------------------

-- ----------------------------
-- Sequence for iot_alert_config
-- ----------------------------
DROP SEQUENCE IF EXISTS iot_alert_config_seq;
CREATE SEQUENCE iot_alert_config_seq START 1;

-- ----------------------------
-- Table structure for iot_alert_config
-- ----------------------------
DROP TABLE IF EXISTS iot_alert_config;
CREATE TABLE iot_alert_config (
  id BIGINT NOT NULL DEFAULT nextval('iot_alert_config_seq'),
  name VARCHAR(64) NOT NULL,
  description VARCHAR(500),
  level SMALLINT NOT NULL,
  status SMALLINT NOT NULL DEFAULT 0,
  scene_rule_ids VARCHAR(500),
  receive_user_ids VARCHAR(500),
  receive_types VARCHAR(255),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id)
);

COMMENT ON TABLE iot_alert_config IS 'IoT 告警配置表';
COMMENT ON COLUMN iot_alert_config.id IS '配置编号';
COMMENT ON COLUMN iot_alert_config.name IS '配置名称';
COMMENT ON COLUMN iot_alert_config.description IS '配置描述';
COMMENT ON COLUMN iot_alert_config.level IS '告警级别';
COMMENT ON COLUMN iot_alert_config.status IS '配置状态（0正常 1停用）';
COMMENT ON COLUMN iot_alert_config.scene_rule_ids IS '关联的场景规则编号数组（逗号分隔）';
COMMENT ON COLUMN iot_alert_config.receive_user_ids IS '接收用户编号数组（逗号分隔）';
COMMENT ON COLUMN iot_alert_config.receive_types IS '接收类型数组（逗号分隔）';
COMMENT ON COLUMN iot_alert_config.creator IS '创建者';
COMMENT ON COLUMN iot_alert_config.create_time IS '创建时间';
COMMENT ON COLUMN iot_alert_config.updater IS '更新者';
COMMENT ON COLUMN iot_alert_config.update_time IS '更新时间';
COMMENT ON COLUMN iot_alert_config.deleted IS '是否删除';

-- ----------------------------
-- Sequence for iot_alert_record
-- ----------------------------
DROP SEQUENCE IF EXISTS iot_alert_record_seq;
CREATE SEQUENCE iot_alert_record_seq START 1;

-- ----------------------------
-- Table structure for iot_alert_record
-- ----------------------------
DROP TABLE IF EXISTS iot_alert_record;
CREATE TABLE iot_alert_record (
  id BIGINT NOT NULL DEFAULT nextval('iot_alert_record_seq'),
  config_id BIGINT NOT NULL,
  config_name VARCHAR(64) NOT NULL,
  config_level SMALLINT NOT NULL,
  scene_rule_id BIGINT,
  product_id BIGINT,
  device_id BIGINT,
  device_message TEXT,
  process_status BOOLEAN NOT NULL DEFAULT FALSE,
  process_remark VARCHAR(500),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id)
);

CREATE INDEX idx_config_device ON iot_alert_record (config_id, device_id);

COMMENT ON TABLE iot_alert_record IS 'IoT 告警记录表';
COMMENT ON COLUMN iot_alert_record.id IS '记录编号';
COMMENT ON COLUMN iot_alert_record.config_id IS '告警配置编号';
COMMENT ON COLUMN iot_alert_record.config_name IS '告警名称';
COMMENT ON COLUMN iot_alert_record.config_level IS '告警级别';
COMMENT ON COLUMN iot_alert_record.scene_rule_id IS '场景规则编号';
COMMENT ON COLUMN iot_alert_record.product_id IS '产品编号';
COMMENT ON COLUMN iot_alert_record.device_id IS '设备编号';
COMMENT ON COLUMN iot_alert_record.device_message IS '触发的设备消息（JSON格式）';
COMMENT ON COLUMN iot_alert_record.process_status IS '是否处理';
COMMENT ON COLUMN iot_alert_record.process_remark IS '处理结果';
COMMENT ON COLUMN iot_alert_record.creator IS '创建者';
COMMENT ON COLUMN iot_alert_record.create_time IS '创建时间';
COMMENT ON COLUMN iot_alert_record.updater IS '更新者';
COMMENT ON COLUMN iot_alert_record.update_time IS '更新时间';
COMMENT ON COLUMN iot_alert_record.deleted IS '是否删除';

-- ----------------------------
-- 5. 规则引擎相关表
-- ----------------------------

-- ----------------------------
-- Sequence for iot_data_rule
-- ----------------------------
DROP SEQUENCE IF EXISTS iot_data_rule_seq;
CREATE SEQUENCE iot_data_rule_seq START 1;

-- ----------------------------
-- Table structure for iot_data_rule
-- ----------------------------
DROP TABLE IF EXISTS iot_data_rule;
CREATE TABLE iot_data_rule (
  id BIGINT NOT NULL DEFAULT nextval('iot_data_rule_seq'),
  name VARCHAR(64) NOT NULL,
  description VARCHAR(500),
  status SMALLINT NOT NULL DEFAULT 0,
  source_configs TEXT,
  sink_ids VARCHAR(500),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id)
);

COMMENT ON TABLE iot_data_rule IS 'IoT 数据流转规则表';
COMMENT ON COLUMN iot_data_rule.id IS '规则编号';
COMMENT ON COLUMN iot_data_rule.name IS '规则名称';
COMMENT ON COLUMN iot_data_rule.description IS '规则描述';
COMMENT ON COLUMN iot_data_rule.status IS '规则状态（0正常 1停用）';
COMMENT ON COLUMN iot_data_rule.source_configs IS '数据源配置数组（JSON格式）';
COMMENT ON COLUMN iot_data_rule.sink_ids IS '数据目的编号数组（逗号分隔）';
COMMENT ON COLUMN iot_data_rule.creator IS '创建者';
COMMENT ON COLUMN iot_data_rule.create_time IS '创建时间';
COMMENT ON COLUMN iot_data_rule.updater IS '更新者';
COMMENT ON COLUMN iot_data_rule.update_time IS '更新时间';
COMMENT ON COLUMN iot_data_rule.deleted IS '是否删除';

-- ----------------------------
-- Sequence for iot_data_sink
-- ----------------------------
DROP SEQUENCE IF EXISTS iot_data_sink_seq;
CREATE SEQUENCE iot_data_sink_seq START 1;

-- ----------------------------
-- Table structure for iot_data_sink
-- ----------------------------
DROP TABLE IF EXISTS iot_data_sink;
CREATE TABLE iot_data_sink (
  id BIGINT NOT NULL DEFAULT nextval('iot_data_sink_seq'),
  name VARCHAR(64) NOT NULL,
  description VARCHAR(500),
  status SMALLINT NOT NULL DEFAULT 0,
  type SMALLINT NOT NULL,
  config TEXT,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id)
);

COMMENT ON TABLE iot_data_sink IS 'IoT 数据流转目的表';
COMMENT ON COLUMN iot_data_sink.id IS '目的编号';
COMMENT ON COLUMN iot_data_sink.name IS '目的名称';
COMMENT ON COLUMN iot_data_sink.description IS '目的描述';
COMMENT ON COLUMN iot_data_sink.status IS '目的状态（0正常 1停用）';
COMMENT ON COLUMN iot_data_sink.type IS '目的类型';
COMMENT ON COLUMN iot_data_sink.config IS '目的配置（JSON格式）';
COMMENT ON COLUMN iot_data_sink.creator IS '创建者';
COMMENT ON COLUMN iot_data_sink.create_time IS '创建时间';
COMMENT ON COLUMN iot_data_sink.updater IS '更新者';
COMMENT ON COLUMN iot_data_sink.update_time IS '更新时间';
COMMENT ON COLUMN iot_data_sink.deleted IS '是否删除';

-- ----------------------------
-- Sequence for iot_scene_rule
-- ----------------------------
DROP SEQUENCE IF EXISTS iot_scene_rule_seq;
CREATE SEQUENCE iot_scene_rule_seq START 1;

-- ----------------------------
-- Table structure for iot_scene_rule
-- ----------------------------
DROP TABLE IF EXISTS iot_scene_rule;
CREATE TABLE iot_scene_rule (
  id BIGINT NOT NULL DEFAULT nextval('iot_scene_rule_seq'),
  name VARCHAR(64) NOT NULL,
  description VARCHAR(500),
  status SMALLINT NOT NULL DEFAULT 0,
  triggers TEXT,
  actions TEXT,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (id)
);

COMMENT ON TABLE iot_scene_rule IS 'IoT 场景联动规则表';
COMMENT ON COLUMN iot_scene_rule.id IS '场景联动编号';
COMMENT ON COLUMN iot_scene_rule.name IS '场景名称';
COMMENT ON COLUMN iot_scene_rule.description IS '场景描述';
COMMENT ON COLUMN iot_scene_rule.status IS '场景状态（0正常 1停用）';
COMMENT ON COLUMN iot_scene_rule.triggers IS '触发条件配置（JSON格式）';
COMMENT ON COLUMN iot_scene_rule.actions IS '执行动作配置（JSON格式）';
COMMENT ON COLUMN iot_scene_rule.creator IS '创建者';
COMMENT ON COLUMN iot_scene_rule.create_time IS '创建时间';
COMMENT ON COLUMN iot_scene_rule.updater IS '更新者';
COMMENT ON COLUMN iot_scene_rule.update_time IS '更新时间';
COMMENT ON COLUMN iot_scene_rule.deleted IS '是否删除';
COMMENT ON COLUMN iot_scene_rule.tenant_id IS '租户编号';

-- ----------------------------
-- 6. OTA 升级相关表
-- ----------------------------

-- ----------------------------
-- Sequence for iot_ota_firmware
-- ----------------------------
DROP SEQUENCE IF EXISTS iot_ota_firmware_seq;
CREATE SEQUENCE iot_ota_firmware_seq START 1;

-- ----------------------------
-- Table structure for iot_ota_firmware
-- ----------------------------
DROP TABLE IF EXISTS iot_ota_firmware;
CREATE TABLE iot_ota_firmware (
  id BIGINT NOT NULL DEFAULT nextval('iot_ota_firmware_seq'),
  name VARCHAR(64) NOT NULL,
  description VARCHAR(500),
  version VARCHAR(32) NOT NULL,
  product_id BIGINT NOT NULL,
  file_url VARCHAR(255) NOT NULL,
  file_size BIGINT NOT NULL,
  file_digest_algorithm VARCHAR(32),
  file_digest_value VARCHAR(255),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id)
);

CREATE INDEX idx_product_version ON iot_ota_firmware (product_id, version);

COMMENT ON TABLE iot_ota_firmware IS 'IoT OTA 固件表';
COMMENT ON COLUMN iot_ota_firmware.id IS '固件编号';
COMMENT ON COLUMN iot_ota_firmware.name IS '固件名称';
COMMENT ON COLUMN iot_ota_firmware.description IS '固件描述';
COMMENT ON COLUMN iot_ota_firmware.version IS '版本号';
COMMENT ON COLUMN iot_ota_firmware.product_id IS '产品编号';
COMMENT ON COLUMN iot_ota_firmware.file_url IS '固件文件URL';
COMMENT ON COLUMN iot_ota_firmware.file_size IS '固件文件大小（字节）';
COMMENT ON COLUMN iot_ota_firmware.file_digest_algorithm IS '签名算法（如MD5）';
COMMENT ON COLUMN iot_ota_firmware.file_digest_value IS '签名值';
COMMENT ON COLUMN iot_ota_firmware.creator IS '创建者';
COMMENT ON COLUMN iot_ota_firmware.create_time IS '创建时间';
COMMENT ON COLUMN iot_ota_firmware.updater IS '更新者';
COMMENT ON COLUMN iot_ota_firmware.update_time IS '更新时间';
COMMENT ON COLUMN iot_ota_firmware.deleted IS '是否删除';

-- ----------------------------
-- Sequence for iot_ota_task
-- ----------------------------
DROP SEQUENCE IF EXISTS iot_ota_task_seq;
CREATE SEQUENCE iot_ota_task_seq START 1;

-- ----------------------------
-- Table structure for iot_ota_task
-- ----------------------------
DROP TABLE IF EXISTS iot_ota_task;
CREATE TABLE iot_ota_task (
  id BIGINT NOT NULL DEFAULT nextval('iot_ota_task_seq'),
  name VARCHAR(64) NOT NULL,
  description VARCHAR(500),
  firmware_id BIGINT NOT NULL,
  status SMALLINT NOT NULL DEFAULT 0,
  device_scope SMALLINT NOT NULL,
  device_total_count INTEGER NOT NULL DEFAULT 0,
  device_success_count INTEGER NOT NULL DEFAULT 0,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id)
);

CREATE INDEX idx_firmware ON iot_ota_task (firmware_id);

COMMENT ON TABLE iot_ota_task IS 'IoT OTA 升级任务表';
COMMENT ON COLUMN iot_ota_task.id IS '任务编号';
COMMENT ON COLUMN iot_ota_task.name IS '任务名称';
COMMENT ON COLUMN iot_ota_task.description IS '任务描述';
COMMENT ON COLUMN iot_ota_task.firmware_id IS '固件编号';
COMMENT ON COLUMN iot_ota_task.status IS '任务状态';
COMMENT ON COLUMN iot_ota_task.device_scope IS '设备升级范围';
COMMENT ON COLUMN iot_ota_task.device_total_count IS '设备总数';
COMMENT ON COLUMN iot_ota_task.device_success_count IS '成功升级数量';
COMMENT ON COLUMN iot_ota_task.creator IS '创建者';
COMMENT ON COLUMN iot_ota_task.create_time IS '创建时间';
COMMENT ON COLUMN iot_ota_task.updater IS '更新者';
COMMENT ON COLUMN iot_ota_task.update_time IS '更新时间';
COMMENT ON COLUMN iot_ota_task.deleted IS '是否删除';

-- ----------------------------
-- Sequence for iot_ota_task_record
-- ----------------------------
DROP SEQUENCE IF EXISTS iot_ota_task_record_seq;
CREATE SEQUENCE iot_ota_task_record_seq START 1;

-- ----------------------------
-- Table structure for iot_ota_task_record
-- ----------------------------
DROP TABLE IF EXISTS iot_ota_task_record;
CREATE TABLE iot_ota_task_record (
  id BIGINT NOT NULL DEFAULT nextval('iot_ota_task_record_seq'),
  firmware_id BIGINT NOT NULL,
  task_id BIGINT NOT NULL,
  device_id BIGINT NOT NULL,
  from_firmware_id BIGINT,
  status SMALLINT NOT NULL DEFAULT 0,
  progress SMALLINT NOT NULL DEFAULT 0,
  description VARCHAR(500),
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id)
);

CREATE INDEX idx_task_device ON iot_ota_task_record (task_id, device_id);

COMMENT ON TABLE iot_ota_task_record IS 'IoT OTA 升级任务记录表';
COMMENT ON COLUMN iot_ota_task_record.id IS '记录编号';
COMMENT ON COLUMN iot_ota_task_record.firmware_id IS '固件编号';
COMMENT ON COLUMN iot_ota_task_record.task_id IS '任务编号';
COMMENT ON COLUMN iot_ota_task_record.device_id IS '设备编号';
COMMENT ON COLUMN iot_ota_task_record.from_firmware_id IS '来源固件编号';
COMMENT ON COLUMN iot_ota_task_record.status IS '升级状态';
COMMENT ON COLUMN iot_ota_task_record.progress IS '升级进度（百分比）';
COMMENT ON COLUMN iot_ota_task_record.description IS '升级进度描述';
COMMENT ON COLUMN iot_ota_task_record.creator IS '创建者';
COMMENT ON COLUMN iot_ota_task_record.create_time IS '创建时间';
COMMENT ON COLUMN iot_ota_task_record.updater IS '更新者';
COMMENT ON COLUMN iot_ota_task_record.update_time IS '更新时间';
COMMENT ON COLUMN iot_ota_task_record.deleted IS '是否删除';
