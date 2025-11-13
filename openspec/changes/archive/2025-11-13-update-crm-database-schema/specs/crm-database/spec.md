# CRM 数据库规范

## ADDED Requirements

### Requirement: CRM 模块数据库脚本规范

CRM 模块 SHALL 提供完整且准确的数据库初始化脚本,确保脚本与代码实体类定义完全一致,并支持多种主流数据库。

#### Scenario: MySQL 脚本字段类型与实体类一致

- **GIVEN** CRM 模块包含 19 个实体类 DO
- **WHEN** 执行 MySQL 初始化脚本 `yudao-module-crm/sql/crm-2024-09-30.sql`
- **THEN** 所有表字段的数据类型 SHALL 与对应实体类的字段类型完全匹配
  - Java `Integer` 对应 MySQL `int`
  - Java `Long` 对应 MySQL `bigint`
  - Java `String` 对应 MySQL `varchar(长度)`
  - Java `Boolean` 对应 MySQL `bit(1)` 或 `tinyint(1)`
  - Java `BigDecimal` 对应 MySQL `decimal(精度,小数位)`
  - Java `LocalDateTime` 对应 MySQL `datetime`
  - Java `List<Long>` (使用 `LongListTypeHandler`) 对应 MySQL `varchar(2048)` 或更大

#### Scenario: PostgreSQL 脚本支持完整

- **GIVEN** CRM 模块包含 19 个实体类 DO,并使用 `@KeySequence` 注解
- **WHEN** 提供 PostgreSQL 数据库初始化脚本 `yudao-module-crm/sql/crm-postgresql.sql`
- **THEN** 脚本 SHALL 包含:
  - 所有 19 张表的 CREATE TABLE 语句,使用 PostgreSQL 语法
  - 每个表对应的序列(SEQUENCE)定义,序列名与 `@KeySequence` 注解一致
  - 表和字段的注释(COMMENT ON TABLE/COLUMN)
  - 必要的索引定义

#### Scenario: 字段类型错误修复

- **GIVEN** `crm_business_status` 实体类中 `percent` 字段定义为 `Integer`
- **WHEN** 查看 MySQL 脚本中 `crm_business_status` 表的 `percent` 字段定义
- **THEN** 该字段类型 SHALL 为 `int`,而非 `decimal(24,6)`

### Requirement: 数据库脚本可执行性

数据库初始化脚本 SHALL 可在干净的数据库环境中直接执行,无错误地创建所有表和索引。

#### Scenario: MySQL 脚本执行成功

- **GIVEN** MySQL 8.0 或更高版本的空数据库
- **WHEN** 执行 `yudao-module-crm/sql/crm-2024-09-30.sql` 脚本
- **THEN** 所有 19 张表 SHALL 成功创建
- **AND** 所有索引 SHALL 成功创建
- **AND** 无 SQL 语法错误或警告

#### Scenario: PostgreSQL 脚本执行成功

- **GIVEN** PostgreSQL 14 或更高版本的空数据库
- **WHEN** 执行 `yudao-module-crm/sql/crm-postgresql.sql` 脚本
- **THEN** 所有 19 张表 SHALL 成功创建
- **AND** 所有序列 SHALL 成功创建
- **AND** 所有索引 SHALL 成功创建
- **AND** 无 SQL 语法错误或警告

### Requirement: 数据库脚本版本管理

数据库脚本 SHALL 包含版本信息和更新说明,便于跟踪脚本变更历史。

#### Scenario: 脚本文件包含版本注释

- **GIVEN** CRM 模块数据库脚本文件
- **WHEN** 查看脚本文件头部注释
- **THEN** 注释 SHALL 包含:
  - 脚本生成日期或版本号
  - 支持的数据库类型和版本
  - 使用说明和注意事项
  - 与代码版本的对应关系

### Requirement: 实体类序列注解规范

所有 CRM 实体类 SHALL 使用 `@KeySequence` 注解声明序列名,确保多数据库主键生成策略的一致性。

#### Scenario: 实体类包含序列注解

- **GIVEN** CRM 模块的任意实体类 DO
- **WHEN** 查看实体类的类级注解
- **THEN** SHALL 包含 `@KeySequence("表名_seq")` 注解
- **AND** 注解中的序列名 SHALL 遵循 `crm_{表名}_seq` 命名规范
- **AND** 注解注释 SHALL 说明"用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增"

## MODIFIED Requirements

无

## REMOVED Requirements

无
