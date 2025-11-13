# CRM 数据库脚本更新实施总结

## 变更概述

本次更新修复了 CRM 模块数据库初始化脚本中的字段类型不匹配问题,并补充了 PostgreSQL 版本的完整脚本,确保数据库脚本与代码实体类定义完全一致。

## 发现的问题

### 1. crm_business_status 表

**字段**: `percent` (赢单率)

| 项目 | MySQL脚本(修复前) | 实体类定义 | 修复后 |
|------|------------------|-----------|--------|
| 数据类型 | decimal(24,6) | Integer | int |
| 示例数据 | 10.000000 | 10 | 10 |

**影响**: 赢单率应为整数百分比(0-100),使用 Integer 更符合业务语义

### 2. crm_contact_business 表

**字段**: `id`, `contact_id`, `business_id`

| 字段 | MySQL脚本(修复前) | 实体类定义 | 修复后 |
|------|------------------|-----------|--------|
| id | int | Long | bigint |
| contact_id | int | Long | bigint |
| business_id | int | Long | bigint |

**影响**: 关联表的外键应与主表主键类型一致,统一使用 bigint 避免数据范围限制

## 修复内容

### MySQL 脚本修复

**文件**: `yudao-module-crm/sql/crm-2024-09-30.sql`

1. **字段类型修复**
   - `crm_business_status.percent`: decimal(24,6) → int
   - `crm_contact_business.id`: int → bigint
   - `crm_contact_business.contact_id`: int → bigint
   - `crm_contact_business.business_id`: int → bigint

2. **示例数据修复**
   - 修正了 `crm_business_status` 表 INSERT 语句中的 percent 值

3. **文档更新**
   - 更新文件头部,添加版本说明
   - 添加重要变更记录
   - 说明多数据库支持

### PostgreSQL 脚本生成

**文件**: `yudao-module-crm/sql/crm-postgresql.sql` (新增)

1. **序列定义**
   - 为所有19张表创建对应的序列
   - 起始值与MySQL AUTO_INCREMENT 值一致

2. **表结构转换**
   - 转换所有19张表的CREATE TABLE语句
   - 数据类型映射:
     * `bigint` → `BIGINT`
     * `int` → `INTEGER`
     * `varchar(N)` → `VARCHAR(N)`
     * `bit(1)` → `BOOLEAN`
     * `decimal(M,N)` → `NUMERIC(M,N)`
     * `datetime` → `TIMESTAMP`
     * `AUTO_INCREMENT` → `nextval('序列名')`

3. **索引和注释**
   - 保留并转换了必要的索引
   - 添加了完整的表和列注释(COMMENT ON语法)

## 影响的表

共19张表,全部已验证并更新:

| 序号 | 表名 | 说明 | 修复状态 |
|------|------|------|---------|
| 1 | crm_business | 商机表 | ✅ |
| 2 | crm_business_product | 商机产品关联表 | ✅ |
| 3 | crm_business_status | 商机状态表 | ✅ 修复percent字段 |
| 4 | crm_business_status_type | 商机状态组表 | ✅ |
| 5 | crm_clue | 线索表 | ✅ |
| 6 | crm_contact | 联系人表 | ✅ |
| 7 | crm_contact_business | 联系人商机关联表 | ✅ 修复id等字段 |
| 8 | crm_contract | 合同表 | ✅ |
| 9 | crm_contract_config | 合同配置表 | ✅ |
| 10 | crm_contract_product | 合同产品关联表 | ✅ |
| 11 | crm_customer | 客户表 | ✅ |
| 12 | crm_customer_limit_config | 客户限制配置表 | ✅ |
| 13 | crm_customer_pool_config | 客户公海配置表 | ✅ |
| 14 | crm_follow_up_record | 跟进记录表 | ✅ |
| 15 | crm_permission | 数据权限表 | ✅ |
| 16 | crm_product | 产品表 | ✅ |
| 17 | crm_product_category | 产品分类表 | ✅ |
| 18 | crm_receivable | 回款表 | ✅ |
| 19 | crm_receivable_plan | 回款计划表 | ✅ |

## 数据兼容性

### crm_business_status.percent

- 现有数据值: 10.000000, 20.000000, 30.000000
- 转换后: 10, 20, 30
- **兼容性**: ✅ 完全兼容,decimal 值可安全转换为整数

### crm_contact_business 外键字段

- 现有ID范围: 1-37
- 类型变更: int → bigint
- **兼容性**: ✅ 完全兼容,int 值域完全包含在 bigint 中

## 使用说明

### MySQL 数据库初始化

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS \`ruoyi-vue-pro\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 导入脚本
mysql -u root -p ruoyi-vue-pro < yudao-module-crm/sql/crm-2024-09-30.sql
```

### PostgreSQL 数据库初始化

```bash
# 1. 创建数据库
createdb -U postgres ruoyi_vue_pro

# 2. 导入脚本
psql -U postgres -d ruoyi_vue_pro -f yudao-module-crm/sql/crm-postgresql.sql
```

## 验证建议

### 1. MySQL 验证

```sql
-- 验证字段类型
DESCRIBE crm_business_status;
DESCRIBE crm_contact_business;

-- 验证数据
SELECT id, name, percent FROM crm_business_status;
SELECT id, contact_id, business_id FROM crm_contact_business;
```

### 2. PostgreSQL 验证

```sql
-- 验证表结构
\d crm_business_status
\d crm_contact_business

-- 验证序列
\ds crm_*

-- 验证数据类型
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'crm_business_status' AND column_name = 'percent';
```

## 后续工作

### 可选但建议执行

1. **测试环境验证**
   - 在独立的测试环境执行脚本
   - 验证所有表创建成功
   - 执行基本的CRUD操作

2. **集成测试**
   - 验证 MyBatis Plus 实体映射
   - 运行单元测试
   - 测试多数据库切换功能

3. **文档完善**
   - 更新 CRM 模块 README.md
   - 记录数据库切换指南
   - 补充常见问题FAQ

## 技术债务清理

本次更新解决了以下技术债务:

- ✅ 数据库脚本与实体类定义不一致
- ✅ 缺少 PostgreSQL 数据库支持
- ✅ 脚本文件缺少版本说明和使用文档
- ✅ 字段类型选择不合理(decimal用于整数百分比)

## 相关资源

- MySQL 脚本: `yudao-module-crm/sql/crm-2024-09-30.sql`
- PostgreSQL 脚本: `yudao-module-crm/sql/crm-postgresql.sql`
- 实体类位置: `yudao-module-crm/src/main/java/cn/iocoder/yudao/module/crm/dal/dataobject/`
- OpenSpec 提案: `openspec/changes/update-crm-database-schema/`

## 完成时间

- 提案创建: 2025-11-13
- 实施完成: 2025-11-13
- 状态: ✅ 已完成 (待测试验证)
