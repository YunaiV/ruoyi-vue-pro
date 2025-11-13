# 实施任务清单

## 1. 分析与验证

- [x] 1.1 逐个对比 19 个实体类 DO 与 MySQL 脚本的字段定义
- [x] 1.2 记录所有字段类型、长度、默认值等差异
- [x] 1.3 确认 `@KeySequence` 注解对应的序列名称

## 2. 修复 MySQL 脚本

- [x] 2.1 修正 `crm_business_status.percent` 字段类型:从 `decimal(24,6)` 改为 `int`
- [x] 2.2 检查并修正其他可能存在的字段类型不匹配
  - [x] 修正 `crm_contact_business.id` 从 `int` 改为 `bigint`
  - [x] 修正 `crm_contact_business.contact_id` 从 `int` 改为 `bigint`
  - [x] 修正 `crm_contact_business.business_id` 从 `int` 改为 `bigint`
- [x] 2.3 确保所有表的字符集为 `utf8mb4`
- [x] 2.4 验证索引定义与实际使用场景的一致性

## 3. 生成 PostgreSQL 脚本

- [x] 3.1 创建 `yudao-module-crm/sql/crm-postgresql.sql` 文件
- [x] 3.2 生成所有表的 PostgreSQL CREATE TABLE 语句
  - [x] 3.2.1 转换数据类型:bigint → BIGINT, varchar → VARCHAR, bit(1) → BOOLEAN 等
  - [x] 3.2.2 转换自增主键:AUTO_INCREMENT → nextval('序列名')
  - [x] 3.2.3 转换默认值语法:CURRENT_TIMESTAMP → CURRENT_TIMESTAMP
- [x] 3.3 为所有实体类添加 PostgreSQL 序列定义
  - [x] crm_business_seq
  - [x] crm_business_product_seq
  - [x] crm_business_status_seq
  - [x] crm_business_status_type_seq
  - [x] crm_clue_seq
  - [x] crm_contact_seq
  - [x] crm_contact_business_seq
  - [x] crm_contract_seq
  - [x] crm_contract_config_seq
  - [x] crm_contract_product_seq
  - [x] crm_customer_seq
  - [x] crm_customer_limit_config_seq
  - [x] crm_customer_pool_config_seq
  - [x] crm_follow_up_seq
  - [x] crm_permission_seq
  - [x] crm_product_seq
  - [x] crm_product_category_seq
  - [x] crm_receivable_seq
  - [x] crm_receivable_plan_seq
- [x] 3.4 添加必要的索引定义(如 owner_user_id 等)
- [x] 3.5 添加表和字段的 COMMENT 注释(使用 PostgreSQL COMMENT ON 语法)

## 4. 测试验证 (可选,需要数据库环境)

- [ ] 4.1 在 MySQL 8.0 环境执行修复后的脚本,验证建表成功
- [ ] 4.2 在 PostgreSQL 14+ 环境执行新脚本,验证建表成功
- [ ] 4.3 对比两个数据库的表结构,确保语义一致
- [ ] 4.4 验证实体类 DO 的 MyBatis Plus 映射正常工作

> **说明**: 测试验证任务需要实际的数据库环境,已提供测试命令和验证步骤,可由用户根据需要自行执行。

## 5. 文档更新

- [x] 5.1 在脚本文件头部添加版本说明和使用注意事项
- [x] 5.2 更新 CRM 模块的 README.md(如存在),说明多数据库支持 (CRM模块无README文件)
- [x] 5.3 记录已知的数据库差异和使用限制(如有)

## 实施总结

### 已完成的工作

1. **分析验证** (100%完成)
   - 分析了所有19个实体类与MySQL脚本的对应关系
   - 识别了2处字段类型不匹配问题
   - 确认了所有@KeySequence注解的序列名称

2. **MySQL脚本修复** (100%完成)
   - 修复了 `crm_business_status.percent` 字段类型:decimal(24,6) → int
   - 修复了 `crm_business_status` 表示例数据中的percent值
   - 修复了 `crm_contact_business` 表的3个字段类型:int → bigint
   - 更新了脚本文件头部,添加了版本说明和变更记录

3. **PostgreSQL脚本生成** (100%完成)
   - 创建了完整的PostgreSQL初始化脚本
   - 转换了所有19张表的建表语句
   - 为所有表创建了对应的序列
   - 添加了完整的表和列注释
   - 保留并转换了必要的索引

### 待验证的工作

测试验证部分(第4节)需要在实际的MySQL和PostgreSQL环境中执行,建议在开发或测试环境进行:

- 建议使用Docker快速搭建测试环境
- MySQL测试命令:`mysql -u root -p < crm-2024-09-30.sql`
- PostgreSQL测试命令:`psql -U postgres -d test_db -f crm-postgresql.sql`
