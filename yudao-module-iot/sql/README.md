# IoT 模块数据库初始化脚本

## 概述

本目录包含 IoT 物联网模块的数据库初始化脚本，支持 MySQL 和 PostgreSQL 两种数据库。

## 目录结构

```
sql/
├── mysql/
│   └── iot_schema.sql          # MySQL 数据库建表脚本
├── postgresql/
│   └── iot_schema.sql          # PostgreSQL 数据库建表脚本
└── README.md                   # 本文档
```

## 数据库表说明

### 1. 产品管理（2 张表）
- `iot_product_category` - IoT 产品分类表
- `iot_product` - IoT 产品表

### 2. 设备管理（2 张表）
- `iot_device_group` - IoT 设备分组表
- `iot_device` - IoT 设备表

### 3. 物模型（1 张表）
- `iot_thing_model` - IoT 产品物模型功能表

### 4. 告警管理（2 张表）
- `iot_alert_config` - IoT 告警配置表
- `iot_alert_record` - IoT 告警记录表

### 5. 规则引擎（3 张表）
- `iot_data_rule` - IoT 数据流转规则表
- `iot_data_sink` - IoT 数据流转目的表
- `iot_scene_rule` - IoT 场景联动规则表

### 6. OTA 升级（3 张表）
- `iot_ota_firmware` - IoT OTA 固件表
- `iot_ota_task` - IoT OTA 升级任务表
- `iot_ota_task_record` - IoT OTA 升级任务记录表

**共计：13 张数据库表**

## 使用方法

### MySQL 数据库

#### 方式一：使用 mysql 命令行工具

```bash
# 进入 MySQL 命令行
mysql -u root -p

# 选择数据库
USE your_database_name;

# 执行初始化脚本
SOURCE /path/to/yudao-module-iot/sql/mysql/iot_schema.sql;
```

#### 方式二：直接执行

```bash
mysql -u root -p your_database_name < yudao-module-iot/sql/mysql/iot_schema.sql
```

### PostgreSQL 数据库

#### 方式一：使用 psql 命令行工具

```bash
# 进入 PostgreSQL 命令行
psql -U postgres -d your_database_name

# 执行初始化脚本
\i /path/to/yudao-module-iot/sql/postgresql/iot_schema.sql
```

#### 方式二：直接执行

```bash
psql -U postgres -d your_database_name -f yudao-module-iot/sql/postgresql/iot_schema.sql
```

## 数据类型映射说明

### MySQL → PostgreSQL

| MySQL 类型 | PostgreSQL 类型 | 说明 |
|-----------|----------------|------|
| `BIGINT` | `BIGINT` | 64位整数 |
| `INT` | `INTEGER` | 32位整数 |
| `TINYINT` | `SMALLINT` | 8位整数 → 16位整数 |
| `VARCHAR(n)` | `VARCHAR(n)` | 可变长度字符串 |
| `DATETIME` | `TIMESTAMP` | 日期时间类型 |
| `BIT(1)` | `BOOLEAN` | 布尔类型 |
| `TEXT` | `TEXT` | 文本类型 |
| `DECIMAL(m,n)` | `DECIMAL(m,n)` | 定点数 |

### 主键自增机制

- **MySQL**: 使用 `AUTO_INCREMENT` 关键字
- **PostgreSQL**: 使用 `SEQUENCE` 序列 + `nextval()` 函数

## 字段命名规范

### 标准审计字段（所有表均包含）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `creator` | VARCHAR(64) | 创建者 |
| `create_time` | DATETIME/TIMESTAMP | 创建时间 |
| `updater` | VARCHAR(64) | 更新者 |
| `update_time` | DATETIME/TIMESTAMP | 更新时间 |
| `deleted` | BIT(1)/BOOLEAN | 逻辑删除标志 |

### 多租户字段（部分表包含）

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `tenant_id` | BIGINT | 租户编号 |

包含多租户字段的表：
- `iot_product`
- `iot_device`
- `iot_scene_rule`

## 索引说明

### 唯一索引
- `iot_product.product_key` - 产品标识唯一索引

### 普通索引
- `iot_device.idx_product_device` - 产品设备联合索引 (product_id, device_name, tenant_id)
- `iot_thing_model.idx_product_identifier` - 产品标识符索引 (product_id, identifier)
- `iot_alert_record.idx_config_device` - 配置设备索引 (config_id, device_id)
- `iot_ota_firmware.idx_product_version` - 产品版本索引 (product_id, version)
- `iot_ota_task.idx_firmware` - 固件索引 (firmware_id)
- `iot_ota_task_record.idx_task_device` - 任务设备索引 (task_id, device_id)

## 注意事项

1. **执行顺序**：脚本中的表创建语句已按照依赖关系排序，请按顺序执行。

2. **DROP TABLE**：脚本开头包含 `DROP TABLE IF EXISTS` 语句，执行前会删除已存在的同名表，**请谨慎使用**。

3. **字符集**：
   - MySQL 使用 `utf8mb4` 字符集，排序规则为 `utf8mb4_unicode_ci`
   - PostgreSQL 默认使用 UTF8 字符集

4. **时区**：建议数据库时区设置为 UTC 或与应用服务器时区一致。

5. **权限**：执行脚本的数据库用户需要具备 `CREATE TABLE`、`CREATE SEQUENCE`（PostgreSQL）等权限。

## 版本历史

| 版本 | 日期 | 作者 | 说明 |
|------|------|------|------|
| v1.0.0 | 2025-11-14 | AI Assistant | 初始版本，包含 13 张核心表 |

## 相关文档

- [芋道 RuoYi-Vue-Pro 官方文档](https://doc.iocoder.cn)
- [IoT 模块开发指南](../CLAUDE.md)
- [数据库设计规范](../../CLAUDE.md)

## 常见问题

### Q1: 如何验证脚本是否执行成功？

**MySQL**:
```sql
SHOW TABLES LIKE 'iot_%';
```

**PostgreSQL**:
```sql
SELECT tablename FROM pg_tables WHERE schemaname = 'public' AND tablename LIKE 'iot_%';
```

应该返回 13 张表。

### Q2: 是否需要手动创建数据库？

是的，脚本只包含建表语句，不包含创建数据库语句。请先手动创建数据库：

**MySQL**:
```sql
CREATE DATABASE your_database_name CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**PostgreSQL**:
```sql
CREATE DATABASE your_database_name ENCODING 'UTF8';
```

### Q3: 如何回滚数据库变更？

由于脚本包含 `DROP TABLE` 语句，建议在执行前进行数据库备份：

**MySQL**:
```bash
mysqldump -u root -p your_database_name > backup.sql
```

**PostgreSQL**:
```bash
pg_dump -U postgres -d your_database_name -f backup.sql
```

## 联系方式

如有问题或建议，请提交 Issue 到项目仓库。
