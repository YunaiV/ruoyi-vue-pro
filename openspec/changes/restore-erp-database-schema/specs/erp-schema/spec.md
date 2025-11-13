# 规范：ERP 模块数据库架构

**能力 ID**: `erp-schema`
**父规范**: 无
**相关能力**: 无

---

## ADDED Requirements

### Requirement: 数据库脚本完整性

**ID**: `erp-schema-001`
**优先级**: P0
**类别**: 数据完整性

ERP 模块 **MUST** 提供完整的数据库初始化脚本，包含所有 Java 实体类对应的数据表定义。

#### Scenario: 验证所有实体类都有对应的数据表

**Given** ERP 模块定义了 33 个实体类（位于 `yudao-module-erp/src/main/java/cn/iocoder/yudao/module/erp/dal/dataobject/`）

**When** 执行数据库初始化脚本

**Then** 数据库中 **MUST** 包含以下 33 个表：

**产品管理（3 表）**：
- `erp_product`：产品表
- `erp_product_category`：产品分类表
- `erp_product_unit`：产品单位表

**库存管理（10 表）**：
- `erp_warehouse`：仓库表
- `erp_stock`：产品库存表
- `erp_stock_in`：入库单表
- `erp_stock_in_item`：入库单明细表
- `erp_stock_out`：出库单表
- `erp_stock_out_item`：出库单明细表
- `erp_stock_move`：调拨单表
- `erp_stock_move_item`：调拨单明细表
- `erp_stock_check`：盘点单表
- `erp_stock_check_item`：盘点单明细表
- `erp_stock_record`：库存记录表

**采购管理（7 表）**：
- `erp_supplier`：供应商表
- `erp_purchase_order`：采购订单表
- `erp_purchase_order_items`：采购订单明细表
- `erp_purchase_in`：采购入库表
- `erp_purchase_in_items`：采购入库明细表
- `erp_purchase_return`：采购退货表
- `erp_purchase_return_items`：采购退货明细表

**销售管理（7 表）**：
- `erp_customer`：客户表
- `erp_sale_order`：销售订单表
- `erp_sale_order_items`：销售订单明细表
- `erp_sale_out`：销售出库表
- `erp_sale_out_items`：销售出库明细表
- `erp_sale_return`：销售退货表
- `erp_sale_return_items`：销售退货明细表

**财务管理（6 表）**：
- `erp_account`：结算账户表
- `erp_finance_receipt`：收款单表
- `erp_finance_receipt_item`：收款单明细表
- `erp_finance_payment`：付款单表
- `erp_finance_payment_item`：付款单明细表

**And** 每个表的字段 **MUST** 与对应实体类的属性完全一致

---

### Requirement: 数据库字段一致性

**ID**: `erp-schema-002`
**优先级**: P0
**类别**: 数据一致性

数据库表的字段定义 **MUST** 与 Java 实体类的属性定义保持一致，包括字段名、数据类型、约束、注释。

#### Scenario: 验证 erp_product 表结构与 ErpProductDO 一致

**Given** Java 实体类 `ErpProductDO` 定义了以下属性：
```java
@TableName("erp_product")
public class ErpProductDO extends BaseDO {
    @TableId
    private Long id;                    // 产品编号
    private String name;                // 产品名称
    private String barCode;             // 产品条码
    private Long categoryId;            // 产品分类编号
    private Long unitId;                // 单位编号
    private Integer status;             // 产品状态
    private String standard;            // 产品规格
    private String remark;              // 产品备注
    private Integer expiryDay;          // 保质期天数
    private BigDecimal weight;          // 基础重量（kg）
    private BigDecimal purchasePrice;   // 采购价格
    private BigDecimal salePrice;       // 销售价格
    private BigDecimal minPrice;        // 最低价格
}
```

**When** 查询 `erp_product` 表结构

**Then** 该表 **MUST** 包含以下字段（MySQL 语法）：
- `id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '产品编号'`
- `name VARCHAR(255) NOT NULL COMMENT '产品名称'`
- `bar_code VARCHAR(255) COMMENT '产品条码'`
- `category_id BIGINT COMMENT '产品分类编号'`
- `unit_id BIGINT COMMENT '单位编号'`
- `status TINYINT NOT NULL COMMENT '产品状态'`
- `standard VARCHAR(255) COMMENT '产品规格'`
- `remark VARCHAR(255) COMMENT '产品备注'`
- `expiry_day INT COMMENT '保质期天数'`
- `weight DECIMAL(24,6) COMMENT '基础重量（kg）'`
- `purchase_price DECIMAL(24,6) COMMENT '采购价格，单位：元'`
- `sale_price DECIMAL(24,6) COMMENT '销售价格，单位：元'`
- `min_price DECIMAL(24,6) COMMENT '最低价格，单位：元'`

**And** **MUST** 包含审计字段（继承自 `BaseDO`）：
- `creator VARCHAR(64) DEFAULT '' COMMENT '创建者'`
- `create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'`
- `updater VARCHAR(64) DEFAULT '' COMMENT '更新者'`
- `update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'`
- `deleted BIT(1) NOT NULL DEFAULT b'0' COMMENT '是否删除'`
- `tenant_id BIGINT NOT NULL DEFAULT 0 COMMENT '租户编号'`

#### Scenario: 验证字段命名规范

**Given** Java 实体类使用驼峰命名（camelCase）

**When** 生成数据库表字段

**Then** 字段名 **MUST** 转换为下划线命名（snake_case）
- `barCode` → `bar_code`
- `categoryId` → `category_id`
- `expiryDay` → `expiry_day`
- `purchasePrice` → `purchase_price`

---

### Requirement: MySQL 脚本规范

**ID**: `erp-schema-003`
**优先级**: P0
**类别**: 数据库兼容性

MySQL 数据库初始化脚本 **MUST** 符合 MySQL 8.0+ 语法规范，并 **SHALL** 遵循芋道项目的编码约定。

#### Scenario: MySQL 脚本格式规范

**Given** 需要创建 MySQL 数据库表（标准场景）

**When** 编写 `CREATE TABLE` 语句

**Then** **MUST** 包含以下元素：
- 表名和字段名使用反引号包裹：`` `erp_product` ``
- 字符集：`CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci`
- 存储引擎：`ENGINE = InnoDB`
- 表注释：`COMMENT = 'ERP 产品表'`
- 字段注释：`COMMENT '产品编号'`

**And** 脚本头部 **MUST** 包含：
```sql
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
```

**And** 脚本尾部 **MUST** 包含：
```sql
SET FOREIGN_KEY_CHECKS = 1;
```

#### Scenario: MySQL 数据类型规范

**Given** Java 实体类定义了不同类型的属性

**When** 映射为 MySQL 字段类型

**Then** **MUST** 遵循以下规则：

| Java 类型 | MySQL 类型 | 示例字段 |
|-----------|-----------|---------|
| `Long` (主键) | `BIGINT NOT NULL AUTO_INCREMENT` | `id` |
| `Long` (普通) | `BIGINT` | `category_id` |
| `Integer` | `INT` | `expiry_day` |
| `Integer` (状态) | `TINYINT` | `status` |
| `String` | `VARCHAR(n)` | `name`, `bar_code` |
| `BigDecimal` | `DECIMAL(24,6)` | `purchase_price` |
| `LocalDateTime` | `DATETIME` | `create_time` |
| `Boolean` | `BIT(1)` | `deleted` |

---

### Requirement: PostgreSQL 脚本生成

**ID**: `erp-schema-004`
**优先级**: P0
**类别**: 数据库兼容性

ERP 模块 **MUST** 提供 PostgreSQL 版本的数据库初始化脚本，与 MySQL 脚本功能等价。

#### Scenario: PostgreSQL 脚本文件

**Given** 已存在 MySQL 初始化脚本 `erp-2024-05-03.sql`

**When** 生成 PostgreSQL 版本

**Then** **MUST** 创建文件 `yudao-module-erp/sql/erp-postgresql.sql`

**And** 该文件 **MUST** 包含：
- 所有 33 个表的 `CREATE TABLE` 语句
- 所有表和字段的注释（`COMMENT ON TABLE/COLUMN`）
- 全局触发器函数 `update_modified_column()`
- 每个表的 `BEFORE UPDATE` 触发器（用于 `update_time` 自动更新）

#### Scenario: PostgreSQL 数据类型映射

**Given** MySQL 脚本使用特定的数据类型

**When** 转换为 PostgreSQL 语法

**Then** **MUST** 应用以下映射规则：

| MySQL 类型 | PostgreSQL 类型 | 说明 |
|-----------|----------------|------|
| `BIGINT AUTO_INCREMENT` | `BIGSERIAL` | 自增主键 |
| `BIGINT` | `BIGINT` | 普通大整数 |
| `INT` | `INTEGER` | 整数 |
| `TINYINT` | `SMALLINT` | 小整数 |
| `BIT(1)` | `BOOLEAN` | 布尔值 |
| `DECIMAL(24,6)` | `NUMERIC(24,6)` | 精确数值 |
| `VARCHAR(n)` | `VARCHAR(n)` | 变长字符串 |
| `DATETIME` | `TIMESTAMP` | 日期时间 |

**And** 默认值 **MUST** 遵循以下映射：
- `DEFAULT b'0'` → `DEFAULT FALSE`
- `DEFAULT b'1'` → `DEFAULT TRUE`
- `DEFAULT CURRENT_TIMESTAMP` → `DEFAULT CURRENT_TIMESTAMP`

#### Scenario: PostgreSQL 序列定义

**Given** Java 实体类使用 `@KeySequence` 注解

```java
@KeySequence("erp_product_seq")
public class ErpProductDO extends BaseDO { ... }
```

**When** 生成 PostgreSQL 表定义

**Then** **MUST** 使用 `BIGSERIAL` 类型自动创建序列

```sql
CREATE TABLE erp_product (
  id BIGSERIAL PRIMARY KEY,
  ...
);
```

**And** PostgreSQL **SHALL** 自动创建名为 `erp_product_id_seq` 的序列

**Note**: MyBatis Plus 的 `KeySequenceDialect` **SHALL** 自动处理序列名映射。

#### Scenario: PostgreSQL 注释语法

**Given** MySQL 表定义包含注释

```sql
CREATE TABLE `erp_product` (
  `id` BIGINT COMMENT '产品编号',
  ...
) COMMENT = 'ERP 产品表';
```

**When** 转换为 PostgreSQL 语法

**Then** **MUST** 生成独立的注释语句

```sql
CREATE TABLE erp_product (
  id BIGSERIAL PRIMARY KEY,
  ...
);

COMMENT ON TABLE erp_product IS 'ERP 产品表';
COMMENT ON COLUMN erp_product.id IS '产品编号';
```

#### Scenario: PostgreSQL update_time 自动更新

**Given** MySQL 使用 `ON UPDATE CURRENT_TIMESTAMP` 自动更新 `update_time` 字段

**When** 在 PostgreSQL 中实现相同功能

**Then** **MUST** 创建全局触发器函数（仅定义一次）

```sql
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

**And** **MUST** 为每个表创建 `BEFORE UPDATE` 触发器

```sql
CREATE TRIGGER update_erp_product_update_time
BEFORE UPDATE ON erp_product
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();
```

---

### Requirement: 脚本可执行性

**ID**: `erp-schema-005`
**优先级**: P0
**类别**: 质量保证

数据库初始化脚本 **MUST** 能够在干净的数据库环境中无错误执行。

#### Scenario: MySQL 脚本执行验证

**Given** 一个干净的 MySQL 8.0 数据库

**When** 执行 `erp-mysql.sql` 脚本

```bash
mysql -u root -p < yudao-module-erp/sql/erp-mysql.sql
```

**Then** 脚本 **MUST** 成功执行，返回码为 0

**And** 数据库中 **MUST** 包含 33 个表

**And** **MUST** 使用 `SHOW CREATE TABLE` 验证每个表的结构符合预期

#### Scenario: PostgreSQL 脚本执行验证

**Given** 一个干净的 PostgreSQL 14+ 数据库

**When** 执行 `erp-postgresql.sql` 脚本

```bash
psql -U postgres -d ruoyi-vue-pro -f yudao-module-erp/sql/erp-postgresql.sql
```

**Then** 脚本 **MUST** 成功执行，返回码为 0

**And** 数据库中 **MUST** 包含：
- 33 个表
- 1 个触发器函数 `update_modified_column()`
- 33 个触发器（每个表一个）

**And** **MUST** 使用 `\d+ table_name` 验证每个表的结构符合预期

---

### Requirement: MyBatis Plus 兼容性

**ID**: `erp-schema-006`
**优先级**: P0
**类别**: 应用集成

数据库表结构 **MUST** 与 MyBatis Plus ORM 框架完全兼容，**SHALL** 确保应用程序能够正确执行 CRUD 操作。

#### Scenario: 实体类映射验证

**Given** 使用修复后的数据库脚本初始化 MySQL 数据库

**When** 启动 Spring Boot 应用程序（ERP 模块）

**Then** MyBatis Plus **MUST** 成功扫描并映射所有 33 个实体类

**And** 应用日志中 **MUST NOT** 出现表或字段映射错误

#### Scenario: CRUD 操作验证

**Given** 数据库已成功初始化

**When** 执行以下操作（以 `ErpProductDO` 为例）

1. **Create**：插入一条产品记录
2. **Read**：根据 ID 查询产品
3. **Update**：更新产品名称
4. **Delete**：逻辑删除产品（设置 `deleted = 1`）

**Then** 所有操作都 **MUST** 成功执行

**And** `update_time` 字段 **MUST** 在更新操作后自动更新为当前时间

**And** 逻辑删除后，查询 **MUST NOT** 返回该记录（假设启用了逻辑删除插件）

#### Scenario: 多租户支持验证

**Given** 数据库表包含 `tenant_id` 字段

**When** 在多租户模式下执行查询操作

**Then** MyBatis Plus **MUST** 自动添加 `WHERE tenant_id = ?` 条件

**And** 不同租户的数据 **MUST** 完全隔离

---

## MODIFIED Requirements

_（无修改的现有需求）_

---

## REMOVED Requirements

_（无移除的需求）_

---

**创建时间**: 2025-11-13
**最后更新**: 2025-11-13
