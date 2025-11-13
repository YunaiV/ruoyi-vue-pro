# 提案：修复和补充 ERP 模块数据库初始化脚本

## 概述

**变更 ID**: `restore-erp-database-schema`
**类型**: 数据库架构修复
**优先级**: 高
**范围**: `yudao-module-erp` 模块

## 问题描述

当前 ERP 模块的数据库初始化脚本 (`yudao-module-erp/sql/erp-2024-05-03.sql`) 可能已经过时，需要：

1. **验证完整性**：对比 Java 实体类（位于 `yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/dal/dataobject/`），确保所有数据表和字段都已正确定义
2. **修复 MySQL 脚本**：更新现有 MySQL 脚本，修复任何缺失或不一致的表结构
3. **生成 PostgreSQL 脚本**：创建与 MySQL 脚本对应的 PostgreSQL 版本，以支持多数据库部署

## 动机

- **数据完整性**：确保数据库脚本与代码实体类保持同步
- **多数据库支持**：芋道项目承诺支持 MySQL、PostgreSQL、Oracle、SQL Server、达梦等多种数据库
- **开发者体验**：提供可靠的数据库初始化脚本，减少新开发者的部署障碍

## 解决方案

### 阶段 1：架构分析与验证

通过分析所有 Java 实体类（`*DO.java` 文件），提取完整的数据库架构定义，包括：

- 表名（通过 `@TableName` 注解）
- 字段定义（通过 Java 类属性）
- 数据类型映射（Java 类型 → SQL 类型）
- 主键和序列（通过 `@TableId` 和 `@KeySequence` 注解）
- 审计字段（`BaseDO` 继承的通用字段）

### 阶段 2：MySQL 脚本修复

对比现有 MySQL 脚本与实体类定义，修复以下问题：

1. **缺失表**：添加任何缺失的数据表定义
2. **缺失字段**：补充表中缺失的字段
3. **数据类型不一致**：修正字段类型与实体类不匹配的问题
4. **索引和约束**：添加必要的索引和外键约束（如适用）
5. **注释**：确保所有表和字段都有中文注释

### 阶段 3：PostgreSQL 脚本生成

基于修复后的 MySQL 脚本，生成 PostgreSQL 版本的初始化脚本：

1. **语法转换**：
   - `AUTO_INCREMENT` → `SERIAL` 或 `BIGSERIAL`
   - `TINYINT` → `SMALLINT`
   - `BIT(1)` → `BOOLEAN`
   - `DATETIME` → `TIMESTAMP`
   - `DECIMAL(24,6)` → `NUMERIC(24,6)`
   - 反引号 `` ` `` → 双引号 `"` （标识符）

2. **序列定义**：
   - 为每个表创建对应的序列（基于 `@KeySequence` 注解）
   - 例如：`CREATE SEQUENCE erp_product_seq START WITH 1 INCREMENT BY 1;`

3. **默认值处理**：
   - `DEFAULT CURRENT_TIMESTAMP` → `DEFAULT CURRENT_TIMESTAMP`
   - `DEFAULT b'0'` → `DEFAULT FALSE`
   - `DEFAULT ''` → `DEFAULT ''`

## 影响范围

### 受影响的模块

- `yudao-module-erp`：数据库初始化脚本

### 受影响的文件

- **新增**：`yudao-module-erp/sql/erp-postgresql.sql`（PostgreSQL 版本）
- **修改**：`yudao-module-erp/sql/erp-2024-05-03.sql`（可能需要修复）
- **参考**：`yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/dal/dataobject/**/*DO.java`（所有实体类）

## 验证计划

1. **结构验证**：
   - 对比实体类与 SQL 脚本，确保所有表和字段都已定义
   - 使用脚本工具自动对比（可选）

2. **MySQL 测试**：
   - 在干净的 MySQL 8.0 数据库中执行修复后的脚本
   - 验证所有表创建成功，无语法错误
   - 检查表结构是否与实体类一致

3. **PostgreSQL 测试**：
   - 在干净的 PostgreSQL 14+ 数据库中执行新生成的脚本
   - 验证所有表和序列创建成功
   - 检查数据类型映射是否正确

4. **应用集成测试**：
   - 使用修复后的脚本初始化数据库
   - 启动 ERP 模块，运行基本的 CRUD 操作
   - 确保 MyBatis Plus 能够正确映射所有实体类

## 风险评估

| 风险 | 可能性 | 影响 | 缓解措施 |
|------|--------|------|---------|
| MySQL 脚本修复引入语法错误 | 低 | 高 | 在测试数据库中验证，代码审查 |
| PostgreSQL 类型映射不兼容 | 中 | 中 | 参考官方文档，测试覆盖所有数据类型 |
| 遗漏新增的实体类 | 低 | 中 | 遍历所有 `*DO.java` 文件，交叉验证 |
| 现有数据迁移问题 | 低 | 低 | 这是初始化脚本，不涉及数据迁移 |

## 依赖关系

- **前置条件**：无
- **阻塞项**：无
- **后续工作**：可选择性地为其他数据库（Oracle、SQL Server、达梦）生成初始化脚本

## 参考资料

- [芋道项目数据库支持列表](https://doc.iocoder.cn/database/)
- [MyBatis Plus 多数据库支持](https://baomidou.com/pages/56bac0/#多数据库支持)
- [PostgreSQL 数据类型映射](https://www.postgresql.org/docs/current/datatype.html)

## 批准状态

- [ ] 技术审查
- [ ] 产品审查
- [ ] 架构审查

---

**创建时间**: 2025-11-13
**创建者**: AI Assistant
**最后更新**: 2025-11-13
