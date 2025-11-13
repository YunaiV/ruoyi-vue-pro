# 实施任务清单

## 1. 准备与分析

- [x] 1.1 读取所有 8 个 BPM 实体类 (DO),提取完整的字段定义、类型、注解信息
- [x] 1.2 读取现有 MySQL 脚本 `bpm-2025-03-30.sql`,分析每个表的当前定义
- [x] 1.3 对比实体类与 MySQL 脚本,列出所有字段差异(缺失字段、类型不匹配、长度不正确等)
- [x] 1.4 参考项目其他模块的 PostgreSQL 脚本格式,确定 BPM 模块 PostgreSQL 脚本规范

## 2. 修复 MySQL 脚本

- [x] 2.1 修复 `bpm_category` 表定义(添加业务索引)
- [x] 2.2 修复 `bpm_form` 表定义,确保 `fields` 字段类型正确 (已确认无需修改)
- [x] 2.3 修复 `bpm_process_definition_info` 表定义,补充所有缺失的 JSON 字段和数组字段 (添加 allow_withdraw_task, print_template_setting)
- [x] 2.4 修复 `bpm_process_expression` 表定义(已确认无需修改)
- [x] 2.5 修复 `bpm_process_listener` 表定义(已确认无需修改)
- [x] 2.6 修复 `bpm_user_group` 表定义,确保 `user_ids` 字段存在且类型正确 (已确认存在)
- [x] 2.7 修复 `bpm_process_instance_copy` 表定义(添加业务索引)
- [x] 2.8 修复 `bpm_oa_leave` 表定义,特别是 `type` 和 `day` 字段类型 (type: tinyint→varchar(50), day: tinyint→bigint)

## 3. 生成 PostgreSQL 脚本

- [x] 3.1 创建 `yudao-module-bpm/sql/postgresql/` 目录
- [x] 3.2 基于修复后的 MySQL 脚本,转换 `bpm_category` 表为 PostgreSQL 语法
- [x] 3.3 转换 `bpm_form` 表,使用 `TEXT` 或 `JSONB` 存储 JSON 字段
- [x] 3.4 转换 `bpm_process_definition_info` 表,处理所有 JSON 和数组字段
- [x] 3.5 转换 `bpm_process_expression` 表
- [x] 3.6 转换 `bpm_process_listener` 表
- [x] 3.7 转换 `bpm_user_group` 表,使用 `TEXT` 存储用户 ID 数组
- [x] 3.8 转换 `bpm_process_instance_copy` 表
- [x] 3.9 转换 `bpm_oa_leave` 表
- [x] 3.10 为所有表添加 PostgreSQL 序列 (SEQUENCE) 定义

## 4. 脚本验证与优化

- [x] 4.1 验证 MySQL 脚本语法正确性(语法符合 MySQL 8.0 规范)
- [x] 4.2 验证 PostgreSQL 脚本语法正确性(语法符合 PostgreSQL 12+ 规范)
- [x] 4.3 检查所有表的索引定义是否完整(已添加关键业务索引)
- [x] 4.4 确保所有字段注释清晰准确(所有字段都有 COMMENT)
- [x] 4.5 统一脚本格式,保持与项目其他脚本风格一致(参考了 AI 模块脚本格式)

## 5. 文档更新

- [x] 5.1 创建 `yudao-module-bpm/sql/README.md` 数据库脚本说明文档
- [x] 5.2 添加 PostgreSQL 数据库支持的说明
- [x] 5.3 更新根目录 `CLAUDE.md` 中 BPM 模块的相关描述(不需要,根目录已有完整索引)

## 验证清单

- [x] 所有 8 个实体类的字段都在数据库脚本中有对应定义
- [x] MySQL 脚本符合 MySQL 5.7 和 8.0+ 语法规范
- [x] PostgreSQL 脚本符合 PostgreSQL 12+ 语法规范
- [x] 脚本定义的表结构与实体类注解完全一致
- [x] 所有字段类型、长度、默认值、注释符合规范
- [x] 幂等性:脚本使用 DROP TABLE IF EXISTS,可重复执行

## 完成总结

### 修复的问题

1. **字段类型修正**:
   - `bpm_oa_leave.type`: `tinyint` → `varchar(50)`
   - `bpm_oa_leave.day`: `tinyint` → `bigint`

2. **缺失字段补充**:
   - `bpm_process_definition_info.allow_withdraw_task`: 新增 bit(1) 字段
   - `bpm_process_definition_info.print_template_setting`: 新增 varchar(512) 字段

3. **索引优化**:
   - `bpm_category`: 添加 code, status, sort 索引
   - `bpm_process_definition_info`: 添加 process_definition_id (UNIQUE), model_id 索引
   - `bpm_process_instance_copy`: 添加 user_id, process_instance_id 索引

4. **PostgreSQL 支持**:
   - 创建完整的 PostgreSQL 初始化脚本
   - 正确转换数据类型 (bit→int2, bigint→int8, datetime→timestamp)
   - 添加 SEQUENCE 定义
   - 使用 COMMENT ON 语法添加注释

### 产出文件

- ✅ `yudao-module-bpm/sql/bpm-2025-03-30.sql` (修复后的 MySQL 脚本)
- ✅ `yudao-module-bpm/sql/postgresql/bpm.sql` (新增 PostgreSQL 脚本)
- ✅ `yudao-module-bpm/sql/README.md` (使用文档)

**所有任务已完成,可以归档!** ✨
