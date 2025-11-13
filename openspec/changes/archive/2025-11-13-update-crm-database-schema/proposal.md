# 更新 CRM 模块数据库脚本

## Why

现有的 CRM 模块数据库初始化脚本(`yudao-module-crm/sql/crm-2024-09-30.sql`)已经过时,与代码实体类定义存在不一致。需要以代码为准,修复 MySQL 脚本的字段类型错误,并补充 PostgreSQL 版本的初始化脚本,确保多数据库支持的一致性。

主要问题:
1. `crm_business_status` 表的 `percent` 字段类型不匹配:SQL 使用 `decimal(24,6)`,实体类使用 `Integer`
2. 部分表字段定义可能与实体类注解不完全对应
3. 缺少 PostgreSQL 数据库脚本,影响多数据库环境部署

## What Changes

- 修复 MySQL 脚本 `crm-2024-09-30.sql` 中的字段类型错误
  - 修正 `crm_business_status.percent` 字段:从 `decimal(24,6)` 改为 `int` (与实体类 `Integer` 对应)
- 基于代码实体类,生成完整的 PostgreSQL 数据库初始化脚本 `crm-postgresql.sql`
  - 包含所有 19 张 CRM 表的建表语句
  - 使用 PostgreSQL 语法(序列、数据类型等)
  - 包含实体类 `@KeySequence` 注解对应的序列定义
- 验证所有表字段与实体类 DO 定义的一致性

## Impact

- 影响的 SQL 脚本:
  - `yudao-module-crm/sql/crm-2024-09-30.sql` (修复)
  - `yudao-module-crm/sql/crm-postgresql.sql` (新增)
- 影响的模块:CRM 客户关系管理模块 (`yudao-module-crm`)
- 影响的表:19 张 CRM 业务表
  - 核心表:crm_customer, crm_clue, crm_business, crm_contract, crm_contact
  - 配置表:crm_customer_limit_config, crm_customer_pool_config, crm_contract_config
  - 关联表:crm_business_product, crm_contract_product, crm_contact_business
  - 其他表:crm_receivable, crm_receivable_plan, crm_follow_up_record, crm_permission, crm_product, crm_product_category, crm_business_status, crm_business_status_type
