# BPM 模块数据库初始化脚本

## 概述

本目录包含 BPM (Business Process Management) 工作流模块的数据库初始化脚本,支持 MySQL 和 PostgreSQL 数据库。

## 脚本清单

### MySQL 脚本

- **bpm-2025-03-30.sql** - MySQL 数据库初始化脚本
  - 支持 MySQL 5.7 / 8.0+
  - 包含所有 BPM 模块表结构定义
  - 已根据实体类定义进行修正和优化

### PostgreSQL 脚本

- **postgresql/bpm.sql** - PostgreSQL 数据库初始化脚本
  - 支持 PostgreSQL 12+
  - 包含所有表结构、序列 (SEQUENCE) 定义
  - 数据类型已转换为 PostgreSQL 兼容类型

## 数据库表说明

BPM 模块包含以下 8 个核心数据表:

| 表名 | 说明 | 主要字段 |
|------|------|---------|
| `bpm_category` | 流程分类 | id, name, code, status, sort |
| `bpm_form` | 表单定义 | id, name, conf, fields |
| `bpm_process_definition_info` | 流程定义扩展信息 | id, process_definition_id, model_id, form_type |
| `bpm_process_expression` | 流程表达式 | id, name, expression, status |
| `bpm_process_listener` | 流程监听器 | id, name, type, event |
| `bpm_user_group` | 用户组 | id, name, user_ids, status |
| `bpm_process_instance_copy` | 流程抄送 | id, user_id, process_instance_id |
| `bpm_oa_leave` | OA 请假申请示例 | id, user_id, type, day, reason |

## 使用说明

### MySQL 数据库

```bash
# 登录 MySQL
mysql -u root -p

# 选择数据库
use ruoyi-vue-pro;

# 执行初始化脚本
source /path/to/yudao-module-bpm/sql/bpm-2025-03-30.sql
```

### PostgreSQL 数据库

```bash
# 登录 PostgreSQL
psql -U postgres -d ruoyi-vue-pro

# 执行初始化脚本
\i /path/to/yudao-module-bpm/sql/postgresql/bpm.sql
```

## 重要修正说明 (2025-11-13)

本次更新对数据库脚本进行了以下修正,确保与实体类定义完全一致:

### 1. 字段类型修正

- **bpm_oa_leave** 表:
  - `type` 字段: `tinyint` → `varchar(50)` (对应 Java String 类型)
  - `day` 字段: `tinyint` → `bigint` (对应 Java Long 类型)

### 2. 缺失字段补充

- **bpm_process_definition_info** 表:
  - 新增 `allow_withdraw_task` 字段: 是否允许审批人撤回任务
  - 新增 `print_template_setting` 字段: 自定义打印模板设置

### 3. 索引优化

为提升查询性能,为以下表添加了业务索引:

- **bpm_category**:
  - `idx_code`: 分类标志索引
  - `idx_status`: 状态索引
  - `idx_sort`: 排序索引

- **bpm_process_definition_info**:
  - `idx_process_definition_id`: 流程定义编号唯一索引 (UNIQUE)
  - `idx_model_id`: 流程模型编号索引

- **bpm_process_instance_copy**:
  - `idx_user_id`: 用户编号索引
  - `idx_process_instance_id`: 流程实例编号索引

### 4. PostgreSQL 脚本生成

- 基于修正后的 MySQL 脚本生成完整的 PostgreSQL 版本
- 数据类型转换:
  - `bit(1)` → `int2` (smallint)
  - `bigint` → `int8` (bigint)
  - `tinyint` → `int2` (smallint)
  - `datetime` → `timestamp`
  - `text` → `text`
- 自增主键使用 `SEQUENCE` 实现
- 所有表和字段都包含完整注释

## 注意事项

1. **幂等性**: 所有脚本都支持重复执行,使用 `DROP TABLE IF EXISTS` 确保幂等性
2. **字符集**: MySQL 脚本使用 `utf8mb4` 字符集,PostgreSQL 默认使用 UTF-8
3. **租户隔离**: 所有表都包含 `tenant_id` 字段,支持多租户
4. **软删除**: 所有表都包含 `deleted` 字段,实现逻辑删除
5. **审计字段**: 所有表都包含 `creator`, `create_time`, `updater`, `update_time` 审计字段

## 版本历史

| 日期 | 版本 | 说明 |
|------|------|------|
| 2025-11-13 | v2 | 修正字段类型、补充缺失字段、优化索引、生成 PostgreSQL 脚本 |
| 2025-03-30 | v1 | 初始版本 |

## 相关实体类

数据库表定义基于以下 Java 实体类 (DO):

- `cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmCategoryDO`
- `cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmFormDO`
- `cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessDefinitionInfoDO`
- `cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessExpressionDO`
- `cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmProcessListenerDO`
- `cn.iocoder.yudao.module.bpm.dal.dataobject.definition.BpmUserGroupDO`
- `cn.iocoder.yudao.module.bpm.dal.dataobject.task.BpmProcessInstanceCopyDO`
- `cn.iocoder.yudao.module.bpm.dal.dataobject.oa.BpmOALeaveDO`

如有字段或类型不一致的情况,请以实体类定义为准。
