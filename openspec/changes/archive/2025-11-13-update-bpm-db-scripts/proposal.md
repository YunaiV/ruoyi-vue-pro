# 更新 BPM 模块数据库初始化脚本

## Why

当前 BPM 模块的数据库初始化脚本 (`yudao-module-bpm/sql/bpm-2025-03-30.sql`) 可能已经过时,与实际的 Java 实体类定义不完全一致。同时缺少 PostgreSQL 版本的数据库脚本,这会影响使用 PostgreSQL 数据库的用户。

需要以代码为准,对比实体类 (DO) 的定义,修复 MySQL 脚本中缺失或不一致的字段定义,并生成对应的 PostgreSQL 初始化脚本。

## What Changes

- 对比 BPM 模块所有实体类 (8 个 DO 类) 与现有 MySQL 脚本的字段定义
- 修复 MySQL 脚本中缺失的字段、不正确的字段类型、长度、注释等
- 基于修复后的 MySQL 脚本,生成对应的 PostgreSQL 版本初始化脚本
- 确保脚本包含所有必要的索引、序列(PostgreSQL)、约束等定义
- 保持与项目现有 SQL 脚本的格式和规范一致

涉及的表:
- `bpm_category` - BPM 流程分类
- `bpm_form` - BPM 表单定义
- `bpm_process_definition_info` - BPM 流程定义扩展信息
- `bpm_process_expression` - BPM 流程表达式
- `bpm_process_listener` - BPM 流程监听器
- `bpm_user_group` - BPM 用户组
- `bpm_process_instance_copy` - BPM 流程抄送
- `bpm_oa_leave` - OA 请假申请

## Impact

- 受影响的模块: `yudao-module-bpm`
- 受影响的文件:
  - `yudao-module-bpm/sql/bpm-2025-03-30.sql` (MySQL 脚本,需修复)
  - `yudao-module-bpm/sql/postgresql/` (新增目录及 PostgreSQL 脚本)
- 数据库兼容性: 增强了对 PostgreSQL 数据库的支持
- 向后兼容性: 修复后的脚本应与现有数据库结构兼容,仅补充缺失字段
