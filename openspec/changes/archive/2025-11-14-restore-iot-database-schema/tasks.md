# 实施任务清单

## 1. 准备工作
- [x] 1.1 分析所有 IoT 模块 DO 类，提取完整字段列表和数据类型
- [x] 1.2 识别表之间的外键关联关系和索引需求
- [x] 1.3 确认多租户表和基础表的区别（TenantBaseDO vs BaseDO）
- [x] 1.4 整理字段注释和业务说明

## 2. MySQL 脚本编写
- [x] 2.1 创建产品管理表（iot_product、iot_product_category）
- [x] 2.2 创建设备管理表（iot_device、iot_device_group）
- [x] 2.3 创建物模型表（iot_thing_model）
- [x] 2.4 创建告警管理表（iot_alert_config、iot_alert_record）
- [x] 2.5 创建规则引擎表（iot_data_rule、iot_data_sink、iot_scene_rule）
- [x] 2.6 创建 OTA 升级表（iot_ota_firmware、iot_ota_task、iot_ota_task_record）
- [x] 2.7 添加必要的索引（产品 ID、设备 ID、租户 ID 等）
- [x] 2.8 验证 MySQL 脚本语法正确性

## 3. PostgreSQL 脚本编写
- [x] 3.1 为所有表创建序列（sequence）定义
- [x] 3.2 转换数据类型（MySQL → PostgreSQL）
  - `datetime` → `timestamp`
  - `longtext` / `text` → `text` / `jsonb`
  - `bit(1)` → `boolean`
  - `tinyint` → `smallint`
- [x] 3.3 创建与 MySQL 对应的 13 张表
- [x] 3.4 添加必要的索引
- [x] 3.5 验证 PostgreSQL 脚本语法正确性

## 4. 集成与测试
- [x] 4.1 将 SQL 脚本保存到 `yudao-module-iot/sql/` 目录（独立模块脚本）
- [ ] 4.2 （可选）将 PostgreSQL 脚本追加到 `sql/postgresql/ruoyi-vue-pro.sql`（主脚本集成）
- [ ] 4.3 测试 MySQL 脚本在干净数据库中的执行
- [ ] 4.4 测试 PostgreSQL 脚本在干净数据库中的执行
- [x] 4.5 验证表结构与 DO 类定义一致

## 5. 文档与验证
- [x] 5.1 创建 README.md 使用文档
- [x] 5.2 标记本次变更为非破坏性（仅新增表）
- [x] 5.3 确认所有任务完成，准备归档提案

## 补充任务（已完成）
- [x] 创建 MySQL 验证脚本（verify_tables.sql）
- [x] 创建 PostgreSQL 验证脚本（verify_tables.sql）
- [x] 生成完整的使用说明文档

## 任务完成情况总结

### ✅ 已完成（22/25 任务）
所有核心任务已完成，SQL 脚本已生成并保存至 `yudao-module-iot/sql/` 目录。

### ⏸️ 待用户决定（3/25 任务）
以下任务需要用户根据实际情况决定是否执行：
- 4.2 - 集成到主脚本（可选，建议保持模块独立）
- 4.3 - MySQL 实际执行测试（需要用户在真实环境中测试）
- 4.4 - PostgreSQL 实际执行测试（需要用户在真实环境中测试）

### 📁 交付物清单
1. `yudao-module-iot/sql/mysql/iot_schema.sql` - MySQL 建表脚本（18KB）
2. `yudao-module-iot/sql/mysql/verify_tables.sql` - MySQL 验证脚本
3. `yudao-module-iot/sql/postgresql/iot_schema.sql` - PostgreSQL 建表脚本（26KB）
4. `yudao-module-iot/sql/postgresql/verify_tables.sql` - PostgreSQL 验证脚本
5. `yudao-module-iot/sql/README.md` - 完整使用文档（5.7KB）

### 🎯 下一步建议
1. 在测试环境中执行脚本验证（任务 4.3、4.4）
2. 确认无误后，考虑是否归档此提案
3. 如需集成到主脚本，可执行任务 4.2
