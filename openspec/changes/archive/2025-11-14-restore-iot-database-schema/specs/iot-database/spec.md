# IoT 数据库结构规范

## ADDED Requirements

### Requirement: IoT 产品分类表结构
系统 SHALL 提供 `iot_product_category` 表，用于存储物联网产品分类信息。

#### Scenario: 产品分类基本字段
- **WHEN** 创建产品分类表时
- **THEN** 必须包含以下字段：
  - `id` (BIGINT) - 分类 ID，主键
  - `name` (VARCHAR) - 分类名称
  - `sort` (INT) - 排序号
  - `status` (TINYINT) - 状态（0正常 1停用）
  - `description` (VARCHAR) - 分类描述
  - 标准审计字段：`creator`, `create_time`, `updater`, `update_time`, `deleted`

#### Scenario: PostgreSQL 序列支持
- **WHEN** 在 PostgreSQL 中创建表时
- **THEN** 必须同时创建对应的序列 `iot_product_category_seq`

---

### Requirement: IoT 产品表结构
系统 SHALL 提供 `iot_product` 表，用于存储物联网产品信息。

#### Scenario: 产品基本字段
- **WHEN** 创建产品表时
- **THEN** 必须包含以下字段：
  - `id` (BIGINT) - 产品 ID，主键
  - `name` (VARCHAR) - 产品名称
  - `product_key` (VARCHAR) - 产品标识（唯一）
  - `category_id` (BIGINT) - 产品分类编号
  - `icon` (VARCHAR) - 产品图标 URL
  - `pic_url` (VARCHAR) - 产品图片 URL
  - `description` (VARCHAR) - 产品描述
  - `status` (TINYINT) - 产品状态（0开发中 1已发布）
  - `device_type` (TINYINT) - 设备类型（0直连设备 1网关子设备 2网关设备）
  - `net_type` (TINYINT) - 联网方式
  - `location_type` (TINYINT) - 定位方式
  - `codec_type` (VARCHAR) - 数据格式（编解码器类型）
  - 标准审计字段 + `tenant_id` (BIGINT) - 租户编号

#### Scenario: 产品唯一性约束
- **WHEN** 在 MySQL 中创建表时
- **THEN** `product_key` 字段必须有唯一索引

---

### Requirement: IoT 设备分组表结构
系统 SHALL 提供 `iot_device_group` 表，用于存储设备分组信息。

#### Scenario: 设备分组基本字段
- **WHEN** 创建设备分组表时
- **THEN** 必须包含以下字段：
  - `id` (BIGINT) - 分组 ID，主键
  - `name` (VARCHAR) - 分组名称
  - `status` (TINYINT) - 分组状态
  - `description` (VARCHAR) - 分组描述
  - 标准审计字段

---

### Requirement: IoT 设备表结构
系统 SHALL 提供 `iot_device` 表，用于存储物联网设备信息。

#### Scenario: 设备完整字段
- **WHEN** 创建设备表时
- **THEN** 必须包含以下字段：
  - 基本信息：`id`, `device_name`, `nickname`, `serial_number`, `pic_url`, `group_ids`
  - 产品关联：`product_id`, `product_key`, `device_type`, `gateway_id`
  - 状态管理：`state`, `online_time`, `offline_time`, `active_time`
  - 网络与定位：`ip`, `firmware_id`, `location_type`, `latitude`, `longitude`, `area_id`, `address`
  - 安全认证：`device_secret`, `auth_type`
  - 配置信息：`config` (TEXT/JSON)
  - 标准审计字段 + `tenant_id`

#### Scenario: 设备索引优化
- **WHEN** 在 MySQL 中创建表时
- **THEN** 必须为 `product_id`, `device_name`, `tenant_id` 创建联合索引

---

### Requirement: IoT 物模型表结构
系统 SHALL 提供 `iot_thing_model` 表，用于存储产品的物模型功能定义（属性、事件、服务）。

#### Scenario: 物模型基本字段
- **WHEN** 创建物模型表时
- **THEN** 必须包含以下字段：
  - `id` (BIGINT) - 物模型功能编号，主键
  - `identifier` (VARCHAR) - 功能标识符
  - `name` (VARCHAR) - 功能名称
  - `description` (VARCHAR) - 功能描述
  - `product_id` (BIGINT) - 产品编号
  - `product_key` (VARCHAR) - 产品标识
  - `type` (TINYINT) - 功能类型（1属性 2服务 3事件）
  - `property` (TEXT/JSON) - 属性定义（JSON）
  - `event` (TEXT/JSON) - 事件定义（JSON）
  - `service` (TEXT/JSON) - 服务定义（JSON）
  - 标准审计字段

#### Scenario: PostgreSQL JSON 类型
- **WHEN** 在 PostgreSQL 中创建表时
- **THEN** `property`, `event`, `service` 字段应当使用 `TEXT` 类型存储 JSON

---

### Requirement: IoT 告警配置表结构
系统 SHALL 提供 `iot_alert_config` 表，用于存储告警规则配置。

#### Scenario: 告警配置字段
- **WHEN** 创建告警配置表时
- **THEN** 必须包含以下字段：
  - `id` (BIGINT) - 配置编号，主键
  - `name` (VARCHAR) - 配置名称
  - `description` (VARCHAR) - 配置描述
  - `level` (TINYINT) - 告警级别
  - `status` (TINYINT) - 配置状态
  - `scene_rule_ids` (VARCHAR) - 关联的场景规则编号数组（逗号分隔）
  - `receive_user_ids` (VARCHAR) - 接收用户编号数组（逗号分隔）
  - `receive_types` (VARCHAR) - 接收类型数组（逗号分隔）
  - 标准审计字段

---

### Requirement: IoT 告警记录表结构
系统 SHALL 提供 `iot_alert_record` 表，用于存储告警触发记录。

#### Scenario: 告警记录字段
- **WHEN** 创建告警记录表时
- **THEN** 必须包含以下字段：
  - `id` (BIGINT) - 记录编号，主键
  - `config_id` (BIGINT) - 告警配置编号
  - `config_name` (VARCHAR) - 告警名称（冗余）
  - `config_level` (TINYINT) - 告警级别（冗余）
  - `scene_rule_id` (BIGINT) - 场景规则编号
  - `product_id` (BIGINT) - 产品编号
  - `device_id` (BIGINT) - 设备编号
  - `device_message` (TEXT/JSON) - 触发的设备消息（JSON）
  - `process_status` (BIT/BOOLEAN) - 是否处理
  - `process_remark` (VARCHAR) - 处理结果
  - 标准审计字段

---

### Requirement: IoT 数据流转规则表结构
系统 SHALL 提供 `iot_data_rule` 表，用于存储数据流转规则配置。

#### Scenario: 数据流转规则字段
- **WHEN** 创建数据流转规则表时
- **THEN** 必须包含以下字段：
  - `id` (BIGINT) - 规则编号，主键
  - `name` (VARCHAR) - 规则名称
  - `description` (VARCHAR) - 规则描述
  - `status` (TINYINT) - 规则状态
  - `source_configs` (TEXT/JSON) - 数据源配置数组（JSON）
  - `sink_ids` (VARCHAR) - 数据目的编号数组（逗号分隔）
  - 标准审计字段

---

### Requirement: IoT 数据流转目的表结构
系统 SHALL 提供 `iot_data_sink` 表，用于存储数据流转的目的地配置。

#### Scenario: 数据流转目的字段
- **WHEN** 创建数据流转目的表时
- **THEN** 必须包含以下字段：
  - `id` (BIGINT) - 目的编号，主键
  - `name` (VARCHAR) - 目的名称
  - `description` (VARCHAR) - 目的描述
  - `status` (TINYINT) - 目的状态
  - `type` (TINYINT) - 目的类型
  - `config` (TEXT/JSON) - 目的配置（JSON）
  - 标准审计字段

---

### Requirement: IoT 场景联动规则表结构
系统 SHALL 提供 `iot_scene_rule` 表，用于存储场景联动规则（触发条件和执行动作）。

#### Scenario: 场景联动规则字段
- **WHEN** 创建场景联动规则表时
- **THEN** 必须包含以下字段：
  - `id` (BIGINT) - 场景联动编号，主键
  - `name` (VARCHAR) - 场景名称
  - `description` (VARCHAR) - 场景描述
  - `status` (TINYINT) - 场景状态
  - `triggers` (TEXT/JSON) - 触发条件配置（JSON）
  - `actions` (TEXT/JSON) - 执行动作配置（JSON）
  - 标准审计字段 + `tenant_id`

---

### Requirement: IoT OTA 固件表结构
系统 SHALL 提供 `iot_ota_firmware` 表，用于存储 OTA 固件版本信息。

#### Scenario: OTA 固件字段
- **WHEN** 创建 OTA 固件表时
- **THEN** 必须包含以下字段：
  - `id` (BIGINT) - 固件编号，主键
  - `name` (VARCHAR) - 固件名称
  - `description` (VARCHAR) - 固件描述
  - `version` (VARCHAR) - 版本号
  - `product_id` (BIGINT) - 产品编号
  - `file_url` (VARCHAR) - 固件文件 URL
  - `file_size` (BIGINT) - 固件文件大小（字节）
  - `file_digest_algorithm` (VARCHAR) - 签名算法（如 MD5）
  - `file_digest_value` (VARCHAR) - 签名值
  - 标准审计字段

---

### Requirement: IoT OTA 升级任务表结构
系统 SHALL 提供 `iot_ota_task` 表，用于存储 OTA 升级任务信息。

#### Scenario: OTA 升级任务字段
- **WHEN** 创建 OTA 升级任务表时
- **THEN** 必须包含以下字段：
  - `id` (BIGINT) - 任务编号，主键
  - `name` (VARCHAR) - 任务名称
  - `description` (VARCHAR) - 任务描述
  - `firmware_id` (BIGINT) - 固件编号
  - `status` (TINYINT) - 任务状态
  - `device_scope` (TINYINT) - 设备升级范围
  - `device_total_count` (INT) - 设备总数
  - `device_success_count` (INT) - 成功升级数量
  - 标准审计字段

---

### Requirement: IoT OTA 升级任务记录表结构
系统 SHALL 提供 `iot_ota_task_record` 表，用于存储每个设备的升级记录。

#### Scenario: OTA 升级任务记录字段
- **WHEN** 创建 OTA 升级任务记录表时
- **THEN** 必须包含以下字段：
  - `id` (BIGINT) - 记录编号，主键
  - `firmware_id` (BIGINT) - 固件编号
  - `task_id` (BIGINT) - 任务编号
  - `device_id` (BIGINT) - 设备编号
  - `from_firmware_id` (BIGINT) - 来源固件编号
  - `status` (TINYINT) - 升级状态
  - `progress` (TINYINT) - 升级进度（百分比）
  - `description` (VARCHAR) - 升级进度描述
  - 标准审计字段

---

### Requirement: 数据库跨平台兼容性
系统 SHALL 同时支持 MySQL 和 PostgreSQL 数据库。

#### Scenario: MySQL 建表语法
- **WHEN** 在 MySQL 中执行建表脚本时
- **THEN** 必须使用标准 MySQL 语法：
  - 引擎：`ENGINE=InnoDB`
  - 字符集：`DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci`
  - 主键自增：`AUTO_INCREMENT`
  - 注释：`COMMENT '表注释'`

#### Scenario: PostgreSQL 建表语法
- **WHEN** 在 PostgreSQL 中执行建表脚本时
- **THEN** 必须满足以下要求：
  - 为每张表创建序列：`CREATE SEQUENCE IF NOT EXISTS table_name_seq;`
  - 主键默认值：`DEFAULT nextval('table_name_seq')`
  - 布尔类型：使用 `BOOLEAN` 替代 `BIT(1)`
  - 时间类型：使用 `TIMESTAMP` 替代 `DATETIME`
  - JSON 类型：使用 `TEXT` 存储 JSON 字符串

#### Scenario: 数据类型映射
- **WHEN** 从 MySQL 转换到 PostgreSQL 时
- **THEN** 必须遵循以下类型映射：
  - `BIGINT` → `BIGINT`
  - `INT` → `INTEGER`
  - `TINYINT` → `SMALLINT`
  - `VARCHAR(n)` → `VARCHAR(n)`
  - `DATETIME` → `TIMESTAMP`
  - `BIT(1)` → `BOOLEAN`
  - `TEXT` / `LONGTEXT` → `TEXT`
