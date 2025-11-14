# 提案：恢复 IoT 模块数据库初始化脚本

## Why

IoT 模块（yudao-module-iot）的数据库初始化脚本完全丢失，导致：
1. 新部署的项目无法直接初始化 IoT 模块所需的 13 张数据库表
2. MySQL 和 PostgreSQL 数据库都缺少对应的建表脚本
3. 开发者需要手动从代码中推断表结构，增加部署难度和出错风险

## What Changes

基于现有的 Java DO（Data Object）类，恢复完整的数据库初始化脚本：

- **新增** MySQL 数据库初始化脚本（13 张表）
  - 产品管理：`iot_product`、`iot_product_category`
  - 设备管理：`iot_device`、`iot_device_group`
  - 物模型：`iot_thing_model`
  - 告警管理：`iot_alert_config`、`iot_alert_record`
  - 规则引擎：`iot_data_rule`、`iot_data_sink`、`iot_scene_rule`
  - OTA 升级：`iot_ota_firmware`、`iot_ota_task`、`iot_ota_task_record`

- **新增** PostgreSQL 数据库初始化脚本（包含序列定义）
  - 所有表结构与 MySQL 对应
  - 为每张表创建对应的序列（sequence）
  - 处理数据类型差异（如 JSON、BIGINT、DATETIME 等）

- **遵循** 项目现有的数据库设计规范
  - 统一的审计字段：`creator`、`create_time`、`updater`、`update_time`、`deleted`
  - 多租户字段：`tenant_id`（仅部分表需要）
  - 字段注释完整，便于理解业务含义

## Impact

**受影响的能力规范（Specs）：**
- 新增：`iot-database` - IoT 模块数据库结构规范

**受影响的代码/系统：**
- `sql/mysql/ruoyi-vue-pro.sql` - 需追加 IoT 表结构
- `sql/postgresql/ruoyi-vue-pro.sql` - 需追加 IoT 表结构和序列
- `yudao-module-iot/` - 所有 DO 类作为建表依据

**影响范围：**
- ✅ 向后兼容：仅新增表结构，不修改现有数据
- ✅ 无破坏性变更：对已有模块无影响
- ⚠️ 需要协调：与数据库版本管理机制（如 Flyway/Liquibase）对齐（如有）

**预期收益：**
- 开发者可以一键初始化完整的 IoT 模块数据库
- 降低部署门槛，减少人工干预
- 统一多数据库支持（MySQL/PostgreSQL）
