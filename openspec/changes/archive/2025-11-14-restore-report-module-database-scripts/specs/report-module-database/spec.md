# Report Module Database Schema

## 概述

yudao-module-report 报表模块的数据库架构规范,包括 GoView 可视化大屏和积木报表引擎所需的所有表定义。

## ADDED Requirements

### Requirement: GoView 可视化项目表支持

系统 SHALL 提供 `report_go_view_project` 表,用于存储 GoView 数据可视化大屏项目的完整配置和元数据。该表 MUST 支持 MySQL、PostgreSQL、Oracle、SQL Server、达梦 DM、人大金仓 Kingbase 等多种数据库。

**优先级**: P0 (关键)

GoView 是一个开源的数据可视化大屏设计器,系统需要存储用户创建的大屏项目配置和元数据。

#### Scenario: 用户创建新的可视化大屏项目

**Given** 用户在 GoView 设计器中设计了一个新的数据可视化大屏
**When** 用户保存该项目
**Then** 系统应在 `report_go_view_project` 表中存储:
- 项目名称
- 预览图片 URL
- 完整的 JSON 配置(包括组件、布局、数据源等)
- 发布状态(已发布/未发布)
- 项目备注
- 标准审计字段(创建者、创建时间、更新者、更新时间)
- 租户隔离字段
- 逻辑删除标记

**And** 系统应支持以下数据库:
- MySQL 5.7/8.0 (使用 AUTO_INCREMENT)
- PostgreSQL 12+ (使用 SEQUENCE)
- Oracle (使用 SEQUENCE)
- SQL Server
- 达梦 DM
- 人大金仓 Kingbase

#### Scenario: 多租户环境下的项目隔离

**Given** 系统启用了多租户模式
**When** 租户 A 的用户查询 GoView 项目列表
**Then** 只应返回 `tenant_id` 等于租户 A ID 的项目
**And** 不应看到其他租户的项目

#### Scenario: 项目的逻辑删除

**Given** 用户删除了一个 GoView 项目
**When** 系统执行删除操作
**Then** 应将该记录的 `deleted` 字段标记为 true
**And** 该项目不再出现在正常查询结果中
**But** 物理数据仍保留在数据库中,可用于审计或恢复

---

### Requirement: 积木报表引擎表支持

系统 SHALL 提供完整的积木报表引擎所需表结构,包括但不限于报表定义(`jimu_report`)、数据源配置(`jimu_report_db` 系列表)、可视化组件(`onl_drag_*` 系列表)、数据字典(`jimu_dict` 系列表)等。所有表 MUST 遵循项目数据库规范,包含审计字段、租户隔离字段和逻辑删除标记。

**优先级**: P0 (关键)

积木报表是一个开源的报表和大屏设计器,需要存储报表定义、数据源配置、可视化组件等信息。

#### Scenario: 用户配置报表数据源

**Given** 用户需要创建一个新的报表
**When** 用户配置数据源连接信息
**Then** 系统应在以下表中存储数据:
- `jimu_report_db`: 数据库连接配置
- `jimu_report_db_field`: 数据库字段定义
- `jimu_report_db_param`: 动态参数配置
- `jimu_report_data_source`: 数据源元信息

#### Scenario: 用户设计报表模板

**Given** 用户在积木报表设计器中设计报表
**When** 用户保存报表设计
**Then** 系统应在 `jimu_report` 表中存储:
- 报表名称和编码
- 报表类型(普通报表/打印报表)
- 报表模板 JSON 配置
- 数据源关联
- 发布状态

**And** 如果包含自定义字典,应在 `jimu_dict` 和 `jimu_dict_item` 中存储

#### Scenario: 用户分享报表

**Given** 用户设计完成一个报表并发布
**When** 用户创建分享链接
**Then** 系统应在 `jimu_report_share` 表中生成分享记录
**And** 分享链接可在 `jimu_report_link` 表中记录访问统计

#### Scenario: 用户导出报表

**Given** 用户查看一个已发布的报表
**When** 用户导出为 Excel/PDF
**Then** 系统应在 `jimu_report_export_log` 表中记录导出日志
**And** 记录导出时间、用户、文件格式等信息

#### Scenario: 用户使用可视化拖拽组件

**Given** 用户使用在线表单设计器或大屏设计器
**When** 用户拖拽组件到画布
**Then** 系统应在以下表中存储组件配置:
- `onl_drag_page`: 页面定义
- `onl_drag_page_comp`: 页面组件关联
- `onl_drag_comp`: 组件定义和配置
- `onl_drag_dataset_head`: 数据集定义
- `onl_drag_dataset_item`: 数据集字段
- `onl_drag_dataset_param`: 数据集参数
- `onl_drag_table_relation`: 表关联关系

---

### Requirement: 数据库脚本清理和标准化

生产环境的数据库脚本 SHALL 仅包含核心引擎表,MUST NOT 包含任何测试数据表(如 `huiyuan_*`、`rep_demo_*`、`tmp_report_data_*` 等)和示例存储过程。测试和示例数据 SHALL 与核心脚本分离,作为可选的额外脚本提供。

**优先级**: P1 (重要)

现有积木报表脚本包含大量测试和示例数据,不适合生产环境使用,需要清理。

#### Scenario: 生产环境初始化数据库

**Given** 运维人员在生产环境初始化数据库
**When** 执行报表模块的 SQL 脚本
**Then** 应只创建核心引擎表,不包含任何测试数据表
**And** 不应包含示例会员数据(`huiyuan_*`)
**And** 不应包含演示数据(`rep_demo_*`)
**And** 不应包含临时数据表(`tmp_report_data_*`)

**验证标准**:
- 移除表: `huiyuan_age`, `huiyuan_fengongsi`, `huiyuan_huoyuedu`, `huiyuan_sex`, `huiyuan_work`, `huiyuan_wxtl`, `huiyuan_wxtlshuliang`, `huiyuan_xueli`
- 移除表: `rep_demo_dxtj`, `rep_demo_employee`, `rep_demo_gongsi`, `rep_demo_jianpiao`, `rep_demo_xiaoshou`
- 移除表: `tmp_report_data_1`, `tmp_report_data_income`
- 移除存储过程: `proc_jmdemo`

#### Scenario: 开发环境使用示例数据

**Given** 开发人员需要学习积木报表功能
**When** 需要查看示例报表
**Then** 可选择性地执行包含示例数据的额外脚本
**But** 示例数据脚本应与核心脚本分离

---

### Requirement: PostgreSQL 数据库支持

所有报表模块表 SHALL 提供 PostgreSQL 兼容的 DDL 脚本。自增主键 MUST 使用 SEQUENCE 实现,数据类型 MUST 正确映射(如 BIGINT→INT8, DATETIME→TIMESTAMP, BIT(1)→BOOLEAN),表和列注释 MUST 使用 `COMMENT ON` 语法。

**优先级**: P0 (关键)

项目需要支持 PostgreSQL 数据库,所有报表模块表都需要提供 PostgreSQL 兼容的 DDL 脚本。

#### Scenario: PostgreSQL 环境下创建 GoView 表

**Given** 系统使用 PostgreSQL 数据库
**When** 执行 GoView 表创建脚本
**Then** 应首先创建自增序列 `report_go_view_project_seq`
**And** 表的主键应使用 `DEFAULT nextval('report_go_view_project_seq')`
**And** 字段类型应正确映射:
- `BIGINT` → `INT8`
- `DATETIME` → `TIMESTAMP`
- `BIT(1)` → `BOOLEAN`
- `TINYINT` → `INT2`
- `LONGTEXT` → `TEXT`

**And** 表和列注释应使用 `COMMENT ON` 语法

#### Scenario: PostgreSQL 环境下创建积木报表表

**Given** 系统使用 PostgreSQL 数据库
**When** 执行积木报表脚本
**Then** 所有表都应遵循相同的类型映射规则
**And** 每个自增主键表都应有对应的 SEQUENCE
**And** 索引定义应使用 `CREATE INDEX` 语法而非内联定义

---

### Requirement: 数据库脚本集成

`report_go_view_project` 表定义 SHALL 集成到项目主数据库脚本(`sql/mysql/ruoyi-vue-pro.sql` 和 `sql/postgresql/ruoyi-vue-pro.sql`)中,MUST 包含表结构和相关的系统菜单数据。执行主脚本 MUST 能够完成 GoView 功能所需的数据库初始化。

**优先级**: P1 (重要)

GoView 表定义应集成到项目主数据库脚本中,方便统一初始化。

#### Scenario: 全新项目初始化

**Given** 新项目需要初始化数据库
**When** 执行 `sql/mysql/ruoyi-vue-pro.sql`
**Then** 应自动创建 `report_go_view_project` 表
**And** 应包含 GoView 相关的系统菜单数据

#### Scenario: PostgreSQL 项目初始化

**Given** 新项目使用 PostgreSQL 数据库
**When** 执行 `sql/postgresql/ruoyi-vue-pro.sql`
**Then** 应自动创建 `report_go_view_project_seq` 序列
**And** 应自动创建 `report_go_view_project` 表
**And** 应包含 GoView 相关的系统菜单数据

---

## 表结构规范

### report_go_view_project 表字段定义

| 字段名 | MySQL 类型 | PostgreSQL 类型 | 必填 | 默认值 | 说明 |
|--------|-----------|----------------|------|--------|------|
| id | BIGINT | INT8 | 是 | AUTO_INCREMENT / nextval(seq) | 主键 |
| name | VARCHAR(200) | VARCHAR(200) | 是 | - | 项目名称 |
| pic_url | VARCHAR(500) | VARCHAR(500) | 否 | NULL | 预览图片URL |
| content | LONGTEXT | TEXT | 否 | NULL | 报表内容(JSON) |
| status | TINYINT | INT2 | 是 | 0 | 发布状态 |
| remark | VARCHAR(500) | VARCHAR(500) | 否 | NULL | 备注 |
| creator | VARCHAR(64) | VARCHAR(64) | 否 | '' | 创建者 |
| create_time | DATETIME | TIMESTAMP | 是 | CURRENT_TIMESTAMP | 创建时间 |
| updater | VARCHAR(64) | VARCHAR(64) | 否 | '' | 更新者 |
| update_time | DATETIME | TIMESTAMP | 是 | CURRENT_TIMESTAMP | 更新时间 |
| deleted | BIT(1) | BOOLEAN | 是 | 0/FALSE | 逻辑删除 |
| tenant_id | BIGINT | INT8 | 是 | 0 | 租户ID |

### 索引定义

- **主键索引**: `PRIMARY KEY (id)`
- **租户索引** (可选): `INDEX idx_tenant_id (tenant_id)` - 优化多租户查询
- **创建时间索引** (可选): `INDEX idx_create_time (create_time)` - 优化时间范围查询

### 字符集和排序规则

- **MySQL**: `ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci`
- **PostgreSQL**: 使用数据库默认字符集(UTF-8)

---

## 脚本文件组织

```
sql/
├── mysql/
│   └── ruoyi-vue-pro.sql          # 主脚本,包含 report_go_view_project
├── postgresql/
│   └── ruoyi-vue-pro.sql          # 主脚本,包含 report_go_view_project + 序列
yudao-module-report/
└── sql/
    ├── mysql/
    │   ├── jimureport.sql         # 清理后的积木报表核心表
    │   └── jimureport-demo.sql    # 可选: 示例数据(未来扩展)
    ├── postgresql/
    │   └── jimureport.sql         # PostgreSQL 版积木报表脚本
    └── README.md                  # 脚本使用说明
```

---

## 验证标准

### 功能验证

- [ ] MySQL 5.7 环境执行所有脚本成功
- [ ] MySQL 8.0 环境执行所有脚本成功
- [ ] PostgreSQL 12+ 环境执行所有脚本成功
- [ ] GoView 项目可正常创建、查询、更新、删除
- [ ] 积木报表功能正常(需集成测试)

### 数据完整性验证

- [ ] 所有表都包含审计字段
- [ ] 所有表都包含租户隔离字段
- [ ] 所有表都包含逻辑删除标记
- [ ] 主键和索引正确定义

### 规范验证

- [ ] 表名遵循 `report_` 前缀规范(GoView 表)
- [ ] 字段名使用下划线分隔
- [ ] 字段注释完整且准确
- [ ] 符合项目数据库规范(参考 CLAUDE.md)

---

## 相关资源

- [积木报表官方文档](http://www.jimureport.com/)
- [GoView 开源项目](https://github.com/dromara/go-view)
- [项目数据库规范](../../../CLAUDE.md#数据库规范)
- [yudao-module-report 模块文档](../../../yudao-module-report/CLAUDE.md)
