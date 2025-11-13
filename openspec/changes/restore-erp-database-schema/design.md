# 设计文档：ERP 模块数据库初始化脚本修复

## 设计目标

为芋道项目的 ERP 模块提供完整、准确、多数据库支持的初始化脚本，确保与 Java 实体类的完全一致性。

## 架构决策

### 1. 脚本组织结构

```
yudao-module-erp/
└── sql/
    ├── erp-2024-05-03.sql          # 现有 MySQL 脚本（可能需要修复）
    ├── erp-mysql.sql               # 修复后的 MySQL 脚本（可选重命名）
    └── erp-postgresql.sql          # 新生成的 PostgreSQL 脚本
```

**决策理由**：
- 保留原始文件，避免破坏现有部署
- 明确区分数据库类型，便于维护
- 未来可扩展支持其他数据库（Oracle、SQL Server、达梦等）

---

### 2. 数据库表结构设计

#### 2.1 表分类

ERP 模块包含 **33 个数据表**，按业务领域划分为 5 类：

| 业务域 | 表前缀 | 表数量 | 说明 |
|--------|--------|--------|------|
| **产品管理** | `erp_product*` | 3 | 产品、产品分类、产品单位 |
| **库存管理** | `erp_stock*`, `erp_warehouse` | 10 | 库存、入库、出库、盘点、调拨、仓库 |
| **采购管理** | `erp_purchase*`, `erp_supplier` | 7 | 采购订单、采购入库、采购退货、供应商 |
| **销售管理** | `erp_sale*`, `erp_customer` | 7 | 销售订单、销售出库、销售退货、客户 |
| **财务管理** | `erp_finance*`, `erp_account` | 6 | 收款、付款、结算账户 |

#### 2.2 标准字段

所有表都继承 `BaseDO`，包含以下审计字段：

| 字段名 | 类型 (MySQL) | 类型 (PostgreSQL) | 说明 |
|--------|-------------|------------------|------|
| `creator` | `VARCHAR(64)` | `VARCHAR(64)` | 创建者 |
| `create_time` | `DATETIME DEFAULT CURRENT_TIMESTAMP` | `TIMESTAMP DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `updater` | `VARCHAR(64)` | `VARCHAR(64)` | 更新者 |
| `update_time` | `DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | `TIMESTAMP DEFAULT CURRENT_TIMESTAMP` | 更新时间（PostgreSQL 需要触发器） |
| `deleted` | `BIT(1) DEFAULT b'0'` | `BOOLEAN DEFAULT FALSE` | 逻辑删除标记 |
| `tenant_id` | `BIGINT DEFAULT 0` | `BIGINT DEFAULT 0` | 租户 ID（多租户支持） |

---

### 3. MySQL → PostgreSQL 类型映射规则

#### 3.1 核心类型映射

| MySQL 类型 | PostgreSQL 类型 | 示例 | 说明 |
|-----------|----------------|------|------|
| `BIGINT AUTO_INCREMENT` | `BIGSERIAL` | 主键 `id` | 自增长整数 |
| `BIGINT` | `BIGINT` | 外键、租户 ID | 普通大整数 |
| `INT` | `INTEGER` | 排序字段 `sort` | 整数 |
| `TINYINT` | `SMALLINT` | 状态字段 `status` | 小整数（0-255 → -32768~32767） |
| `BIT(1)` | `BOOLEAN` | 逻辑删除 `deleted` | 布尔值 |
| `DECIMAL(24,6)` | `NUMERIC(24,6)` | 金额、数量字段 | 精确数值 |
| `VARCHAR(n)` | `VARCHAR(n)` | 文本字段 | 变长字符串 |
| `DATETIME` | `TIMESTAMP` | 时间字段 | 日期时间 |
| `TEXT` | `TEXT` | 备注字段 | 长文本 |

#### 3.2 默认值映射

| MySQL 默认值 | PostgreSQL 默认值 | 字段类型 |
|-------------|------------------|---------|
| `DEFAULT b'0'` | `DEFAULT FALSE` | `BIT(1)` → `BOOLEAN` |
| `DEFAULT b'1'` | `DEFAULT TRUE` | `BIT(1)` → `BOOLEAN` |
| `DEFAULT CURRENT_TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | `DATETIME` → `TIMESTAMP` |
| `DEFAULT ''` | `DEFAULT ''` | `VARCHAR` |
| `DEFAULT 0` | `DEFAULT 0` | 数值类型 |

#### 3.3 约束映射

| MySQL 语法 | PostgreSQL 语法 | 说明 |
|-----------|----------------|------|
| `PRIMARY KEY (\`id\` DESC)` | `PRIMARY KEY (id)` | PostgreSQL 主键不支持排序 |
| `NOT NULL` | `NOT NULL` | 非空约束（一致） |
| `UNIQUE` | `UNIQUE` | 唯一约束（一致） |

---

### 4. PostgreSQL 序列设计

#### 4.1 序列命名规范

每个表对应一个序列，命名规则：`{table_name}_seq`

**示例**：
- `erp_product` → `erp_product_seq`
- `erp_stock` → `erp_stock_seq`

#### 4.2 序列定义模板

```sql
CREATE SEQUENCE erp_product_seq START WITH 1 INCREMENT BY 1;
```

#### 4.3 主键默认值

两种方案：

**方案 A：使用 BIGSERIAL（推荐）**
```sql
CREATE TABLE erp_product (
  id BIGSERIAL PRIMARY KEY,  -- 自动创建序列
  ...
);
```

**方案 B：显式使用序列**
```sql
CREATE SEQUENCE erp_product_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE erp_product (
  id BIGINT PRIMARY KEY DEFAULT nextval('erp_product_seq'),
  ...
);
```

**决策**：采用 **方案 A**（BIGSERIAL），原因：
- 简洁，减少代码量
- PostgreSQL 自动管理序列
- 与实体类的 `@KeySequence` 注解一致（MyBatis Plus 自动处理）

---

### 5. 注释处理

#### 5.1 MySQL 注释语法

```sql
CREATE TABLE erp_product (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '产品编号',
  name VARCHAR(255) NOT NULL COMMENT '产品名称',
  ...
) ENGINE = InnoDB COMMENT = 'ERP 产品表';
```

#### 5.2 PostgreSQL 注释语法

```sql
CREATE TABLE erp_product (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  ...
);

COMMENT ON TABLE erp_product IS 'ERP 产品表';
COMMENT ON COLUMN erp_product.id IS '产品编号';
COMMENT ON COLUMN erp_product.name IS '产品名称';
```

#### 5.3 注释生成策略

1. **提取 MySQL 注释**：从 `CREATE TABLE` 语句中提取 `COMMENT` 子句
2. **转换为 PostgreSQL**：生成对应的 `COMMENT ON TABLE/COLUMN` 语句
3. **保持一致性**：确保两种脚本的注释内容完全一致

---

### 6. 特殊字段处理

#### 6.1 `update_time` 字段

**MySQL 语法**：
```sql
update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
```

**PostgreSQL 等价实现**：
```sql
-- 1. 字段定义
update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP

-- 2. 创建触发器函数（全局，只需定义一次）
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 3. 为每个表创建触发器
CREATE TRIGGER update_erp_product_update_time
BEFORE UPDATE ON erp_product
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();
```

**决策**：
- **MySQL**：保留 `ON UPDATE CURRENT_TIMESTAMP`
- **PostgreSQL**：
  1. 创建全局触发器函数 `update_modified_column()`
  2. 为所有 33 个表创建 `BEFORE UPDATE` 触发器

---

### 7. 脚本结构设计

#### 7.1 MySQL 脚本结构

```sql
-- 文件头注释
/*
 ERP 模块数据库初始化脚本 (MySQL)
 Author: 芋道源码
 Date: 2025-11-13
 Version: v1.0
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ========== 产品管理 ==========
DROP TABLE IF EXISTS `erp_product`;
CREATE TABLE `erp_product` (...);

DROP TABLE IF EXISTS `erp_product_category`;
CREATE TABLE `erp_product_category` (...);

DROP TABLE IF EXISTS `erp_product_unit`;
CREATE TABLE `erp_product_unit` (...);

-- ========== 库存管理 ==========
...

-- ========== 采购管理 ==========
...

-- ========== 销售管理 ==========
...

-- ========== 财务管理 ==========
...

SET FOREIGN_KEY_CHECKS = 1;
```

#### 7.2 PostgreSQL 脚本结构

```sql
-- 文件头注释
/*
 ERP 模块数据库初始化脚本 (PostgreSQL)
 Author: 芋道源码
 Date: 2025-11-13
 Version: v1.0
*/

-- ========== 创建触发器函数（用于 update_time 自动更新）==========
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ========== 产品管理 ==========
DROP TABLE IF EXISTS erp_product CASCADE;
CREATE TABLE erp_product (...);

COMMENT ON TABLE erp_product IS 'ERP 产品表';
COMMENT ON COLUMN erp_product.id IS '产品编号';
...

CREATE TRIGGER update_erp_product_update_time
BEFORE UPDATE ON erp_product
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- ========== 库存管理 ==========
...

-- ========== 采购管理 ==========
...

-- ========== 销售管理 ==========
...

-- ========== 财务管理 ==========
...
```

---

### 8. 索引设计

#### 8.1 主键索引

- **MySQL**：`PRIMARY KEY (\`id\` DESC)` → 移除 `DESC`（不影响性能）
- **PostgreSQL**：`PRIMARY KEY (id)`

#### 8.2 外键索引（可选）

当前脚本未显式定义外键约束，依赖应用层逻辑。

**未来改进建议**：
- 添加外键约束以增强数据完整性
- 例如：`erp_product.category_id` → `erp_product_category.id`

#### 8.3 业务索引（可选）

根据查询需求，可考虑添加以下索引：
- `erp_product.bar_code`（产品条码查询）
- `erp_stock.product_id, warehouse_id`（库存查询）
- `erp_sale_order.customer_id`（客户订单查询）

**决策**：暂不添加额外索引，保持与现有脚本一致。

---

### 9. 验证策略

#### 9.1 结构验证

**工具**：自定义 Python/Shell 脚本

**步骤**：
1. 解析所有 `*DO.java` 文件，提取表结构
2. 解析 SQL 脚本，提取表结构
3. 对比字段名、类型、注释，生成差异报告

#### 9.2 功能验证

**工具**：MySQL CLI、PostgreSQL psql

**步骤**：
1. 在干净数据库中执行脚本
2. 使用 `SHOW CREATE TABLE`（MySQL）或 `\d+ table_name`（PostgreSQL）验证表结构
3. 插入测试数据，验证约束和触发器

#### 9.3 集成验证

**工具**：Spring Boot Test + JUnit

**步骤**：
1. 配置测试数据源（MySQL 和 PostgreSQL）
2. 启动 ERP 模块，运行单元测试和集成测试
3. 验证 MyBatis Plus 能正确映射所有实体类

---

## 技术决策总结

| 决策点 | 选择 | 理由 |
|--------|------|------|
| **脚本文件命名** | `erp-mysql.sql`, `erp-postgresql.sql` | 明确区分数据库类型 |
| **主键自增** | MySQL: `AUTO_INCREMENT`<br>PostgreSQL: `BIGSERIAL` | 符合各数据库最佳实践 |
| **逻辑删除** | MySQL: `BIT(1)`<br>PostgreSQL: `BOOLEAN` | 语义更清晰 |
| **`update_time` 更新** | MySQL: `ON UPDATE CURRENT_TIMESTAMP`<br>PostgreSQL: 触发器 | PostgreSQL 无内置支持，需触发器 |
| **注释** | MySQL: 内联<br>PostgreSQL: 单独语句 | 符合各数据库语法 |
| **外键约束** | 暂不添加 | 保持与现有架构一致 |
| **序列命名** | `{table_name}_seq` | 符合 MyBatis Plus `@KeySequence` |

---

## 风险缓解措施

| 风险 | 缓解措施 |
|------|---------|
| **类型映射错误** | 参考 PostgreSQL 官方文档，逐一验证每种类型 |
| **触发器逻辑错误** | 在测试数据库中充分测试 `update_time` 更新 |
| **注释丢失** | 使用自动化脚本提取和转换注释 |
| **脚本执行失败** | 在干净数据库中多次测试，记录错误日志 |

---

**创建时间**: 2025-11-13
**最后更新**: 2025-11-13
