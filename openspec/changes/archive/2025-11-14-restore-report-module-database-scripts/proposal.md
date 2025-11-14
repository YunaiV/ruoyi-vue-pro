# 修复和补充 report 模块数据库初始化脚本

## Why

当前 yudao-module-report 报表模块的数据库脚本存在严重的不一致性和缺失问题,直接影响系统的部署和使用:

1. **代码与数据库脱节**: Java 代码中已定义 `GoViewProjectDO` 实体类,但所有数据库脚本中均缺失对应的 `report_go_view_project` 表,导致 GoView 功能无法使用
2. **脚本质量不达标**: 现有积木报表脚本包含大量测试数据表和示例数据,不符合生产环境标准,且文件体积过大(3464行)
3. **多数据库支持不完整**: 项目承诺支持 PostgreSQL 等多种数据库,但报表模块仅提供 MySQL 脚本,缺少 PostgreSQL 版本
4. **维护困难**: GoView 表未集成到主数据库脚本中,开发者和运维人员需要手动处理,增加了出错风险

本提案旨在解决这些问题,确保数据库脚本与代码定义一致,符合生产环境标准,并支持项目承诺的所有数据库类型。

## 背景 (Background)

yudao-module-report 报表模块目前存在以下数据库脚本问题:

1. **缺失 GoView 表定义**: 代码中定义了 `GoViewProjectDO` 实体类(映射到 `report_go_view_project` 表),但在所有数据库脚本中均未找到该表的 DDL 语句
2. **积木报表脚本过时**: 现有的 `jimureport.mysql5.7.create.sql` 脚本(3464行)可能已经过时,包含大量测试数据表(如 `huiyuan_*`, `rep_demo_*` 等),不符合生产环境标准
3. **缺少 PostgreSQL 版本**: 仅存在 MySQL 5.7 版本的积木报表脚本,缺少 PostgreSQL 版本

## 目标 (Goals)

1. **以代码为准**: 以 Java 实体类(`*DO.java`)的 `@TableName` 定义为权威标准,还原或修复数据库表结构
2. **补充缺失的 GoView 表**: 为 `report_go_view_project` 表生成 MySQL 和 PostgreSQL 的 DDL 脚本
3. **清理积木报表脚本**: 移除测试数据表,保留核心的积木报表引擎所需表
4. **生成 PostgreSQL 脚本**: 将 MySQL 脚本转换为 PostgreSQL 兼容的版本
5. **集成到主脚本**: 将 report 模块的表定义集成到项目主数据库脚本 `sql/mysql/ruoyi-vue-pro.sql` 和 `sql/postgresql/ruoyi-vue-pro.sql` 中

## 范围 (Scope)

### 包含 (In Scope)

- 基于 `GoViewProjectDO.java` 生成 `report_go_view_project` 表的 DDL
- 审查和清理 `jimureport.mysql5.7.create.sql` 脚本
- 生成 PostgreSQL 版本的积木报表脚本
- 更新主数据库脚本,添加 report 模块表定义
- 添加必要的索引和约束
- 为 Oracle、PostgreSQL、Kingbase 等数据库添加序列(SEQUENCE)支持

### 不包含 (Out of Scope)

- 修改 Java 代码逻辑
- 数据迁移脚本
- 其他业务模块的数据库脚本

## 技术方案 (Technical Approach)

### 1. GoView 表结构设计

基于 `GoViewProjectDO` 类定义,表结构如下:

```sql
-- MySQL 版本
CREATE TABLE report_go_view_project (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '编号',
    name VARCHAR(200) NOT NULL COMMENT '项目名称',
    pic_url VARCHAR(500) COMMENT '预览图片URL',
    content LONGTEXT COMMENT '报表内容(JSON配置)',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '发布状态(0-已发布 1-未发布)',
    remark VARCHAR(500) COMMENT '项目备注',
    creator VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updater VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='GoView 数据可视化项目表';

-- PostgreSQL 版本
CREATE SEQUENCE report_go_view_project_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE report_go_view_project (
    id INT8 NOT NULL DEFAULT nextval('report_go_view_project_seq'),
    name VARCHAR(200) NOT NULL,
    pic_url VARCHAR(500),
    content TEXT,
    status INT2 NOT NULL DEFAULT 0,
    remark VARCHAR(500),
    creator VARCHAR(64) DEFAULT '',
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updater VARCHAR(64) DEFAULT '',
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    tenant_id INT8 NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);

COMMENT ON TABLE report_go_view_project IS 'GoView 数据可视化项目表';
COMMENT ON COLUMN report_go_view_project.id IS '编号';
COMMENT ON COLUMN report_go_view_project.name IS '项目名称';
-- ... 其他字段注释
```

### 2. 积木报表表清理策略

**保留表** (积木报表引擎核心表):
- `jimu_dict` / `jimu_dict_item` - 数据字典
- `jimu_report` - 报表定义
- `jimu_report_data_source` - 数据源
- `jimu_report_db` / `jimu_report_db_field` / `jimu_report_db_param` - 数据库配置
- `jimu_report_link` / `jimu_report_share` - 分享链接
- `jimu_report_export_log` - 导出日志
- `jimu_report_map` - 地图配置
- `onl_drag_*` - 可视化拖拽组件表

**移除表** (测试/示例数据):
- `huiyuan_*` - 会员测试数据
- `rep_demo_*` - 演示数据
- `tmp_report_data_*` - 临时数据

### 3. 文件组织结构

```
sql/
├── mysql/
│   └── ruoyi-vue-pro.sql  (主脚本,添加 report_go_view_project)
├── postgresql/
│   └── ruoyi-vue-pro.sql  (主脚本,添加 report_go_view_project + SEQUENCE)
yudao-module-report/
└── sql/
    ├── mysql/
    │   └── jimureport.sql  (清理后的积木报表脚本)
    └── postgresql/
        └── jimureport.sql  (PostgreSQL版积木报表脚本)
```

## 依赖 (Dependencies)

- 需要访问现有的 Java 实体类代码
- 需要理解项目数据库规范(参考 CLAUDE.md)
- 需要熟悉 PostgreSQL 和 MySQL 的语法差异

## 风险与缓解 (Risks & Mitigation)

| 风险 | 影响 | 缓解措施 |
|------|------|---------|
| 积木报表表删除过多导致功能异常 | 高 | 仅删除明确的测试数据表,保留所有 `jimu_*` 和 `onl_*` 前缀表 |
| PostgreSQL 类型转换错误 | 中 | 参考项目现有 PostgreSQL 脚本的类型映射规范 |
| 主脚本合并冲突 | 低 | 在脚本末尾添加,使用明确的注释分隔 |

## 成功标准 (Success Criteria)

- [x] `report_go_view_project` 表在 MySQL 和 PostgreSQL 脚本中都存在
- [x] 积木报表脚本仅包含核心引擎表,测试数据表已移除
- [x] PostgreSQL 脚本语法正确,包含必要的 SEQUENCE
- [x] 所有脚本遵循项目数据库规范(字段命名、审计字段等)
- [x] 脚本可在干净数据库环境执行成功,无语法错误

## 相关文档 (Related Documentation)

- [项目数据库规范](../../CLAUDE.md#数据库规范)
- [yudao-module-report 模块文档](../../yudao-module-report/CLAUDE.md)
- [GoViewProjectDO 源码](../../yudao-module-report/src/main/java/cn/iocoder/yudao/module/report/dal/dataobject/goview/GoViewProjectDO.java)
