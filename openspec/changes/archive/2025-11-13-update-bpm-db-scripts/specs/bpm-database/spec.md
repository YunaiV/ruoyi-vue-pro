# BPM 数据库脚本规范

## ADDED Requirements

### Requirement: BPM 模块数据库脚本完整性

BPM 模块的数据库初始化脚本 SHALL 与 Java 实体类定义保持一致,包含所有必要的表、字段、索引和约束定义。

#### Scenario: MySQL 脚本与实体类字段一致性

- **WHEN** 开发者查看 `yudao-module-bpm/sql/bpm-2025-03-30.sql` 中的表定义
- **THEN** 脚本中的字段定义应与对应的 DO 类中的字段完全匹配
- **AND** 字段类型、长度、默认值、注释应符合实体类的注解定义
- **AND** 所有 `@TableField` 注解的字段都应在表定义中存在

#### Scenario: PostgreSQL 脚本支持

- **WHEN** 开发者需要在 PostgreSQL 数据库上初始化 BPM 模块
- **THEN** 应存在 `yudao-module-bpm/sql/postgresql/bpm.sql` 脚本
- **AND** PostgreSQL 脚本应包含所有与 MySQL 脚本相同的表和字段
- **AND** 数据类型应转换为 PostgreSQL 兼容的类型
- **AND** 应包含必要的序列(SEQUENCE)定义用于主键自增

#### Scenario: 基础审计字段标准化

- **WHEN** 创建任何 BPM 相关的表
- **THEN** 必须包含以下标准审计字段:
  - `creator` VARCHAR(64) - 创建者
  - `create_time` TIMESTAMP - 创建时间
  - `updater` VARCHAR(64) - 更新者
  - `update_time` TIMESTAMP - 更新时间
  - `deleted` BOOLEAN - 逻辑删除标识
  - `tenant_id` BIGINT - 租户编号
- **AND** 字段定义应与 `BaseDO` 基类保持一致

### Requirement: BPM 流程定义扩展信息表完整性

`bpm_process_definition_info` 表 SHALL 包含流程定义的所有扩展字段,支持流程元数据、表单配置、权限控制等功能。

#### Scenario: JSON 类型字段支持

- **WHEN** 实体类中使用 `JacksonTypeHandler` 处理 JSON 字段
- **THEN** MySQL 脚本应使用 `VARCHAR` 或 `TEXT` 类型存储 JSON 数据
- **AND** PostgreSQL 脚本应优先使用 `JSONB` 类型
- **AND** 字段注释应标明该字段存储 JSON 格式数据

#### Scenario: 数组类型字段支持

- **WHEN** 实体类中使用 `LongListTypeHandler` 处理 ID 数组字段
- **THEN** MySQL 脚本应使用 `VARCHAR` 类型存储逗号分隔的 ID
- **AND** PostgreSQL 脚本可使用 `BIGINT[]` 数组类型
- **AND** 应添加注释说明该字段存储数组数据

### Requirement: BPM 用户组成员字段支持

`bpm_user_group` 表 SHALL 支持存储用户编号数组,实现用户组成员管理。

#### Scenario: 用户编号集合存储

- **WHEN** `BpmUserGroupDO` 实体类定义 `userIds` 字段为 `Set<Long>` 类型
- **THEN** MySQL 脚本应定义 `user_ids` 字段为 `VARCHAR` 或 `TEXT` 类型
- **AND** PostgreSQL 脚本应定义为 `BIGINT[]` 数组类型或 `TEXT` 类型
- **AND** 字段注释应说明存储格式(如 JSON 数组)

### Requirement: OA 请假申请表字段类型修正

`bpm_oa_leave` 表的字段类型 SHALL 与实体类定义严格一致。

#### Scenario: 请假类型字段定义

- **WHEN** `BpmOALeaveDO` 实体类中 `type` 字段定义为 `String` 类型
- **THEN** 数据库脚本中 `type` 字段应使用 `VARCHAR` 类型,而非 `TINYINT`
- **AND** 字段长度应足够存储请假类型字符串值

#### Scenario: 请假天数字段定义

- **WHEN** `BpmOALeaveDO` 实体类中 `day` 字段定义为 `Long` 类型
- **THEN** 数据库脚本中 `day` 字段应使用 `BIGINT` 类型,而非 `TINYINT`
- **AND** 应支持存储较大的天数值

### Requirement: 数据库脚本格式规范

所有数据库脚本 SHALL 遵循项目统一的格式规范,保持可读性和可维护性。

#### Scenario: 脚本文件头注释

- **WHEN** 创建或更新数据库脚本文件
- **THEN** 文件头部应包含注释说明脚本用途、目标数据库类型和版本
- **AND** 应包含字符集设置(MySQL: `SET NAMES utf8mb4`)
- **AND** 应包含外键检查设置(MySQL: `SET FOREIGN_KEY_CHECKS = 0`)

#### Scenario: 表定义格式

- **WHEN** 定义数据库表结构
- **THEN** 应使用 `DROP TABLE IF EXISTS` 确保幂等性
- **AND** 字段定义应按逻辑分组并添加空行分隔(业务字段、审计字段等)
- **AND** 每个字段应包含 `COMMENT` 注释
- **AND** 表定义应包含 `COMMENT` 说明表用途

#### Scenario: PostgreSQL 特定语法

- **WHEN** 编写 PostgreSQL 数据库脚本
- **THEN** 主键自增应使用 `SERIAL` 或 `BIGSERIAL` 类型,或创建对应的 `SEQUENCE`
- **AND** 布尔类型应使用 `BOOLEAN`,而非 `BIT(1)`
- **AND** 时间戳类型应使用 `TIMESTAMP`,并设置默认值 `CURRENT_TIMESTAMP`
- **AND** 字符串类型应使用 `VARCHAR` 或 `TEXT`
