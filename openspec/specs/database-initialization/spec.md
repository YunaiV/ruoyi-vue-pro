# database-initialization Specification

## Purpose
TBD - created by archiving change fix-mp-database-scripts. Update Purpose after archive.
## Requirements
### Requirement: PostgreSQL初始化脚本支持
微信公众号模块 MUST 提供PostgreSQL版本的数据库初始化脚本，以支持多数据库部署需求。

#### Scenario: 创建PostgreSQL初始化脚本
**Given** 项目支持PostgreSQL数据库部署
**And** yudao-module-mp模块包含7个数据表定义
**When** 用户在PostgreSQL环境中初始化mp模块数据库
**Then** 系统 SHALL 能够执行mp-postgresql.sql脚本成功创建所有表
**And** 所有表 SHALL 包含序列（sequence）支持主键自增
**And** 所有表 SHALL 包含update_time自动更新触发器
**And** 所有表和字段 SHALL 包含完整的注释说明

#### Scenario: PostgreSQL类型映射正确性
**Given** MySQL脚本中定义了各种字段类型
**When** 转换为PostgreSQL脚本
**Then** bigint SHALL 保持为BIGINT类型
**And** varchar SHALL 保持为VARCHAR类型
**And** datetime SHALL 转换为TIMESTAMP类型
**And** bit(1) SHALL 转换为BOOLEAN类型
**And** tinyint SHALL 转换为SMALLINT类型
**And** decimal SHALL 转换为NUMERIC类型
**And** 字符集排序规则 SHALL 移除（PostgreSQL默认UTF8）

#### Scenario: 序列和自增主键
**Given** PostgreSQL使用序列实现自增主键
**And** 代码实体使用@KeySequence注解
**When** 创建表时
**Then** 系统 SHALL 为每个表创建对应的序列（mp_xxx_seq格式）
**And** 主键字段 SHALL 使用BIGSERIAL或DEFAULT nextval('序列名')
**And** 序列名 SHALL 与@KeySequence注解中的名称一致

#### Scenario: 自动更新触发器
**Given** MySQL使用ON UPDATE CURRENT_TIMESTAMP实现update_time自动更新
**And** PostgreSQL不支持该语法
**When** 创建表时
**Then** 系统 SHALL 创建update_modified_column()触发器函数
**And** 系统 SHALL 为每个表创建BEFORE UPDATE触发器
**And** 触发器 SHALL 自动更新update_time字段为当前时间

### Requirement: MySQL脚本与代码实体一致性
MySQL初始化脚本 MUST 与代码实体定义完全一致，确保字段类型、注释、默认值等完全匹配。

#### Scenario: 字段完整性验证
**Given** 代码实体类中定义了所有业务字段
**When** 对比MySQL脚本
**Then** 脚本中 SHALL 包含实体类中的所有字段
**And** 字段顺序 SHALL 与实体类声明顺序一致
**And** 脚本中 SHALL NOT 存在实体类中未定义的字段（除非有明确说明）

#### Scenario: 字段类型一致性
**Given** 实体类字段使用Java类型定义
**When** 映射到MySQL字段类型
**Then** Long SHALL 映射为bigint
**And** String SHALL 映射为varchar，长度与@TableField或业务需求一致
**And** Integer SHALL 映射为int或tinyint
**And** Boolean SHALL 映射为bit(1)
**And** LocalDateTime SHALL 映射为datetime
**And** Double SHALL 映射为double
**And** BigDecimal或带@TableField(typeHandler)的 SHALL 映射为合适的类型

#### Scenario: 字段注释准确性
**Given** 实体类字段包含JavaDoc注释
**When** 创建MySQL表
**Then** 字段COMMENT SHALL 与JavaDoc注释内容一致
**And** 如果JavaDoc中有枚举引用，COMMENT SHALL 包含枚举说明
**And** 如果JavaDoc中有关联说明，COMMENT SHALL 包含关联信息

#### Scenario: 默认值和约束
**Given** 实体类继承BaseDO或TenantBaseDO
**When** 创建表
**Then** creator字段默认值 SHALL 为空字符串
**And** create_time SHALL 使用CURRENT_TIMESTAMP
**And** updater字段默认值 SHALL 为空字符串
**And** update_time SHALL 使用CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
**And** deleted字段默认值 SHALL 为b'0'（false）
**And** tenant_id字段默认值 SHALL 为0（对于TenantBaseDO）

### Requirement: 特殊字段类型处理
包含TypeHandler的字段 MUST 进行特殊的数据库类型映射。

#### Scenario: JSON数组字段（LongListTypeHandler）
**Given** MpUserDO中tag_ids字段使用LongListTypeHandler
**When** 创建数据库表
**Then** MySQL SHALL 使用varchar(255)类型
**And** PostgreSQL SHALL 使用VARCHAR(255)类型
**And** 字段注释 SHALL 说明这是一个数组（如"标签编号数组"）

#### Scenario: JSON对象字段（JacksonTypeHandler）
**Given** MpMenuDO和MpMessageDO中的articles字段使用JacksonTypeHandler
**And** 该字段存储复杂对象数组
**When** 创建数据库表
**Then** MySQL SHALL 使用varchar(1024)或更大的类型
**And** PostgreSQL SHALL 使用VARCHAR(1024)或TEXT类型
**And** 字段注释 SHALL 说明存储的数据结构

