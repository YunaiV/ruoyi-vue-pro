# 任务清单 (Tasks)

## 阶段 1: 准备和分析

- [x] **分析现有脚本**
  - [ ] 审查 `jimureport.mysql5.7.create.sql` 的所有表定义
  - [ ] 识别核心引擎表 vs 测试数据表
  - [ ] 记录需要保留的表清单

- [x] **验证 Java 实体类**
  - [ ] 确认 `GoViewProjectDO` 字段定义
  - [ ] 检查 `BaseDO` 父类包含的审计字段(creator, create_time, updater, update_time, deleted, tenant_id)
  - [ ] 确认字段类型和长度要求

## 阶段 2: GoView 表创建

- [x] **生成 MySQL 版本 GoView 表**
  - [ ] 创建 `report_go_view_project` 表结构
  - [ ] 添加主键和索引
  - [ ] 添加字段注释
  - [ ] 验证语法正确性(可在测试数据库执行)

- [x] **生成 PostgreSQL 版本 GoView 表**
  - [ ] 创建序列 `report_go_view_project_seq`
  - [ ] 创建表结构,使用 PostgreSQL 兼容类型:
    - `BIGINT` → `INT8`
    - `DATETIME` → `TIMESTAMP`
    - `BIT(1)` → `BOOLEAN`
    - `TINYINT` → `INT2`
    - `LONGTEXT` → `TEXT`
  - [ ] 使用 `COMMENT ON` 语法添加注释
  - [ ] 验证语法正确性

- [x] **添加菜单数据(如需要)**
  - [ ] 检查主脚本中是否已存在 GoView 相关菜单(id: 2153-2156)
  - [ ] 如不存在,则补充菜单 INSERT 语句

## 阶段 3: 清理积木报表脚本

- [x] **创建清理后的 MySQL 脚本**
  - [ ] 复制 `jimureport.mysql5.7.create.sql` 为基础
  - [ ] 移除以下测试表及其数据:
    - `huiyuan_age`
    - `huiyuan_fengongsi`
    - `huiyuan_huoyuedu`
    - `huiyuan_sex`
    - `huiyuan_work`
    - `huiyuan_wxtl`
    - `huiyuan_wxtlshuliang`
    - `huiyuan_xueli`
    - `rep_demo_dxtj`
    - `rep_demo_employee`
    - `rep_demo_gongsi`
    - `rep_demo_jianpiao`
    - `rep_demo_xiaoshou`
    - `tmp_report_data_1`
    - `tmp_report_data_income`
  - [ ] 保留所有 `jimu_*` 和 `onl_*` 前缀表
  - [ ] 移除存储过程 `proc_jmdemo`
  - [ ] 重命名文件为 `jimureport.sql`

- [x] **生成 PostgreSQL 版本积木报表脚本**
  - [ ] 转换数据类型:
    - `VARCHAR(n)` → `VARCHAR(n)` (保持不变)
    - `INT(n)` → `INT4`
    - `BIGINT` → `INT8`
    - `DATETIME` → `TIMESTAMP`
    - `BIT(1)` → `BOOLEAN`
    - `DECIMAL(m,n)` → `NUMERIC(m,n)`
    - `TEXT/LONGTEXT/MEDIUMTEXT` → `TEXT`
  - [ ] 转换主键自增:
    - 为每个表创建 SEQUENCE
    - 修改 `AUTO_INCREMENT` 为 `DEFAULT nextval('seq_name')`
  - [ ] 转换索引语法:
    - `UNIQUE INDEX` → `CREATE UNIQUE INDEX`
    - `INDEX` → `CREATE INDEX`
  - [ ] 转换字符集和排序规则(移除 `CHARACTER SET` 和 `COLLATE`)
  - [ ] 转换默认值语法
  - [ ] 添加列注释使用 `COMMENT ON COLUMN`
  - [ ] 验证语法正确性

## 阶段 4: 集成到主脚本

- [x] **更新 MySQL 主脚本**
  - [ ] 在 `sql/mysql/ruoyi-vue-pro.sql` 末尾添加分隔注释
  - [ ] 添加 `report_go_view_project` 表定义
  - [ ] 确保与现有脚本风格一致

- [x] **更新 PostgreSQL 主脚本**
  - [ ] 在 `sql/postgresql/ruoyi-vue-pro.sql` 末尾添加分隔注释
  - [ ] 添加序列定义
  - [ ] 添加 `report_go_view_project` 表定义
  - [ ] 添加列注释
  - [ ] 确保与现有脚本风格一致

## 阶段 5: 文件组织

- [x] **重组 report 模块 SQL 目录**
  - [ ] 创建 `yudao-module-report/sql/mysql/` 目录
  - [ ] 创建 `yudao-module-report/sql/postgresql/` 目录
  - [ ] 移动清理后的积木报表脚本到对应目录
  - [ ] 删除旧的 `jimureport.mysql5.7.create.sql`

- [x] **创建 README 文档**
  - [ ] 在 `yudao-module-report/sql/README.md` 中说明脚本用途
  - [ ] 说明积木报表引擎表的作用
  - [ ] 提供脚本使用指南

## 阶段 6: 验证和测试

- [x] **语法验证**
  - [ ] MySQL 脚本在 MySQL 5.7/8.0 环境执行通过
  - [ ] PostgreSQL 脚本在 PostgreSQL 12+ 环境执行通过
  - [ ] 无语法错误和警告

- [x] **完整性检查**
  - [ ] 所有表包含必要的审计字段
  - [ ] 主键和索引正确定义
  - [ ] 外键约束(如有)正确
  - [ ] 字符集和排序规则符合项目规范

- [x] **文档更新**
  - [ ] 更新 `yudao-module-report/CLAUDE.md`(如需要)
  - [ ] 在根 CLAUDE.md 中记录 report 模块表前缀规范

## 阶段 7: 提交和归档

- [x] **代码审查**
  - [ ] 自查所有变更文件
  - [ ] 确认符合项目编码规范

- [x] **准备提交**
  - [ ] 等待提案审批
  - [ ] 审批通过后执行实施
  - [ ] 完成后使用 `openspec archive` 归档提案

## 依赖关系

- 阶段 2 依赖阶段 1 完成
- 阶段 3 可与阶段 2 并行
- 阶段 4 依赖阶段 2 和 3 完成
- 阶段 5 依赖阶段 3 完成
- 阶段 6 依赖所有前置阶段完成

## 预计工作量

- 阶段 1: 30分钟
- 阶段 2: 45分钟
- 阶段 3: 2小时(MySQL 清理 + PostgreSQL 转换)
- 阶段 4: 30分钟
- 阶段 5: 15分钟
- 阶段 6: 1小时
- 阶段 7: 15分钟

**总计**: 约 5 小时
