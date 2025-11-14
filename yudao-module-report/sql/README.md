# yudao-module-report SQL 脚本说明

本目录包含 yudao-module-report 报表模块的数据库初始化脚本。

## 目录结构

```
sql/
├── README.md                   # 本文件
├── mysql/                      # MySQL 数据库脚本
│   ├── goview.sql             # GoView 数据可视化大屏项目表
│   └── jimureport.sql         # 积木报表引擎核心表
└── postgresql/                 # PostgreSQL 数据库脚本
    ├── goview.sql             # GoView 数据可视化大屏项目表(含序列)
    └── jimureport.sql         # 积木报表引擎核心表(含序列)
```

## 脚本说明

### 1. GoView 数据可视化大屏

**文件**: `goview.sql` (MySQL / PostgreSQL)

**说明**: GoView 是一个开源的数据可视化大屏设计器,用于创建数据分析看板和大屏展示。

**包含表**:
- `report_go_view_project` - GoView 项目表,存储大屏项目的配置、布局、组件等 JSON 数据

**注意**:
- GoView 表已集成到项目主数据库脚本 `sql/mysql/ruoyi-vue-pro.sql` 和 `sql/postgresql/ruoyi-vue-pro.sql` 中
- 如果已经执行过主脚本,无需再单独执行此脚本
- 此脚本仅供参考或单独使用 GoView 功能时使用

**相关资源**:
- GoView 官网: https://gitee.com/dromara/go-view
- GoView 文档: https://gitee.com/dromara/go-view/wikis

### 2. 积木报表引擎

**文件**: `jimureport.sql` (MySQL / PostgreSQL)

**说明**: 积木报表(JimuReport)是一个开源的企业级低代码报表和可视化设计器,支持报表设计、打印设计、大屏设计等功能。

**包含表** (共 18 个核心表):

**数据字典**:
- `jimu_dict` - 字典主表
- `jimu_dict_item` - 字典项表

**报表管理**:
- `jimu_report` - 报表定义主表
- `jimu_report_data_source` - 报表数据源配置
- `jimu_report_db` - 数据库连接配置
- `jimu_report_db_field` - 数据库字段定义
- `jimu_report_db_param` - 数据库动态参数
- `jimu_report_link` - 报表链接管理
- `jimu_report_share` - 报表分享配置
- `jimu_report_export_log` - 报表导出日志
- `jimu_report_map` - 地图配置

**可视化拖拽组件**:
- `onl_drag_page` - 可视化页面定义
- `onl_drag_page_comp` - 页面组件关联
- `onl_drag_comp` - 组件定义和配置
- `onl_drag_dataset_head` - 数据集定义
- `onl_drag_dataset_item` - 数据集字段
- `onl_drag_dataset_param` - 数据集参数
- `onl_drag_table_relation` - 表关联关系

**注意**:
- 此脚本**不包含**测试数据和示例表
- 此脚本仅包含积木报表引擎的核心表结构
- 积木报表功能需要配合 `yudao-spring-boot-starter-biz-report` 依赖使用

**相关资源**:
- 积木报表官网: http://www.jimureport.com/
- 积木报表文档: http://jimureport.com/doc/start
- 积木报表示例: http://jimureport.com/example

## 使用指南

### 方式一: 使用项目主脚本 (推荐)

执行项目主数据库脚本即可完成所有表的创建,包括 GoView 表:

**MySQL**:
```bash
mysql -u root -p < sql/mysql/ruoyi-vue-pro.sql
```

**PostgreSQL**:
```bash
psql -U postgres -d your_database < sql/postgresql/ruoyi-vue-pro.sql
```

**然后执行积木报表脚本**:

**MySQL**:
```bash
mysql -u root -p < yudao-module-report/sql/mysql/jimureport.sql
```

**PostgreSQL**:
```bash
psql -U postgres -d your_database < yudao-module-report/sql/postgresql/jimureport.sql
```

### 方式二: 单独使用报表模块脚本

如果只需要使用报表功能,可单独执行报表模块脚本:

**MySQL**:
```bash
# 1. 先创建 GoView 表
mysql -u root -p < yudao-module-report/sql/mysql/goview.sql

# 2. 再创建积木报表表
mysql -u root -p < yudao-module-report/sql/mysql/jimureport.sql
```

**PostgreSQL**:
```bash
# 1. 先创建 GoView 表和序列
psql -U postgres -d your_database < yudao-module-report/sql/postgresql/goview.sql

# 2. 再创建积木报表表和序列
psql -U postgres -d your_database < yudao-module-report/sql/postgresql/jimureport.sql
```

## 数据库支持

- ✅ MySQL 5.7 / 8.0+
- ✅ PostgreSQL 12+
- ⚠️ Oracle / SQL Server / 达梦 DM: 项目支持,但报表模块脚本仅提供 MySQL 和 PostgreSQL 版本

## 注意事项

1. **执行顺序**: 先执行主脚本,再执行积木报表脚本
2. **字符集**: MySQL 脚本使用 `utf8mb4` 字符集,确保数据库配置正确
3. **序列**: PostgreSQL 版本为每个自增主键表创建了对应的序列(SEQUENCE)
4. **租户隔离**: 所有表都包含 `tenant_id` 字段,支持多租户环境
5. **逻辑删除**: 所有表都包含 `deleted` 字段,支持逻辑删除
6. **审计字段**: 所有表都包含 `creator`、`create_time`、`updater`、`update_time` 字段

## 脚本维护说明

- **GoView 脚本**: 基于 `GoViewProjectDO.java` 实体类生成,与代码保持一致
- **积木报表脚本**: 清理了测试数据表,仅保留核心引擎表
- **版本**: 最后更新时间 2025-11-14

如有问题或建议,请联系项目维护者或提交 Issue。
