# 提案：修复和补充 ERP 模块数据库初始化脚本

## 概述

**变更 ID**: `restore-erp-database-schema`
**类型**: 数据库架构修复
**优先级**: 高
**范围**: `yudao-module-erp` 模块

## Why

### 问题背景

当前 ERP 模块的数据库初始化脚本存在以下问题：

1. **字段类型错误**：`erp_product.unit_id` 字段使用 `INT` 类型，但 Java 实体类定义为 `Long`，应该使用 `BIGINT`
2. **缺少 PostgreSQL 支持**：芋道项目承诺支持多种数据库，但 ERP 模块只有 MySQL 脚本
3. **同步风险**：数据库脚本与代码实体类可能不一致，导致部署或运行时错误

### 影响

- **分布式 ID 溢出**：使用雪花算法等分布式 ID 时，`INT` 类型会导致数据溢出
- **多数据库部署受限**：无法在 PostgreSQL 环境中部署 ERP 模块
- **维护困难**：手动对比实体类与 SQL 脚本容易出错

### 动机

- **数据完整性**：确保数据库脚本与代码实体类保持 100% 同步
- **多数据库支持**：实现芋道项目对 PostgreSQL 的承诺
- **开发者体验**：提供可靠、完整的数据库初始化脚本

## What Changes

### 核心变更

1. **修复 MySQL 脚本字段类型错误**
   - 修复 `erp_product.unit_id`：`INT` → `BIGINT`
   - 验证所有 33 个表的字段类型与实体类一致

2. **生成 PostgreSQL 初始化脚本**
   - 新建 `yudao-module-erp/sql/erp-postgresql.sql`（1,644 行）
   - 实现 MySQL → PostgreSQL 的完整类型映射
   - 创建 33 个表的 `BEFORE UPDATE` 触发器用于自动更新 `update_time`
   - 添加完整的表和字段注释

3. **创建完整的验证文档**
   - 实体类结构分析（JSON 格式）
   - MySQL 脚本结构分析（JSON 格式）
   - 差异对比报告
   - PostgreSQL 类型映射规则文档

### 技术细节

**MySQL 脚本修复**：
- 1 个字段类型修正

**PostgreSQL 脚本特性**：
- 使用 `BIGSERIAL` 实现主键自增
- `BIT(1)` → `BOOLEAN` 类型转换
- `DATETIME` → `TIMESTAMP` 类型转换
- `DECIMAL(24,6)` → `NUMERIC(24,6)` 精确数值
- 全局触发器函数 `update_modified_column()` 用于自动更新时间戳

### 不改变的内容

- ❌ 不修改 Java 实体类
- ❌ 不修改现有表结构逻辑（只修正类型错误）
- ❌ 不涉及数据迁移（仅初始化脚本）

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
