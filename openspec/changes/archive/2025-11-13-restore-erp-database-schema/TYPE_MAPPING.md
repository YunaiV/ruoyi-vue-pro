# MySQL → PostgreSQL 类型映射规则

**文档版本**: v1.0
**创建时间**: 2025-11-13
**适用范围**: ERP 模块数据库初始化脚本转换

---

## 📖 概述

本文档定义了将 MySQL 数据类型转换为 PostgreSQL 数据类型的详细规则，确保数据库脚本的跨平台兼容性。

---

## 🔄 核心类型映射表

### 1. 数值类型

| MySQL 类型 | PostgreSQL 类型 | 精度范围 | 说明 | 示例字段 |
|-----------|----------------|---------|------|---------|
| `BIGINT AUTO_INCREMENT` | `BIGSERIAL` | 1 ~ 9223372036854775807 | 自增长整数，自动创建序列 | 主键 `id` |
| `BIGINT` | `BIGINT` | -2^63 ~ 2^63-1 | 普通大整数 | 外键、`tenant_id` |
| `INT` | `INTEGER` | -2^31 ~ 2^31-1 | 整数 | `sort`（排序字段） |
| `TINYINT` | `SMALLINT` | -32768 ~ 32767 | 小整数（MySQL: 0~255） | `status`（状态字段） |
| `DECIMAL(p,s)` | `NUMERIC(p,s)` | 可变精度 | 精确数值（金额、百分比） | `total_price`, `tax_percent` |

**注意事项**:
- `TINYINT` 在 MySQL 中范围是 0-255（无符号）或 -128-127（有符号），PostgreSQL 使用 `SMALLINT`（-32768 ~ 32767）可完全覆盖
- `BIGSERIAL` 自动创建名为 `{table_name}_id_seq` 的序列
- `DECIMAL` 和 `NUMERIC` 在 PostgreSQL 中完全等价

---

### 2. 布尔类型

| MySQL 类型 | PostgreSQL 类型 | 存储大小 | 说明 | 示例字段 |
|-----------|----------------|---------|------|---------|
| `BIT(1)` | `BOOLEAN` | 1 字节 | 布尔值（真/假） | `deleted`, `default_status` |

**默认值转换**:
```sql
-- MySQL
deleted BIT(1) NOT NULL DEFAULT b'0'
default_status BIT(1) NULL DEFAULT b'0'

-- PostgreSQL
deleted BOOLEAN NOT NULL DEFAULT FALSE
default_status BOOLEAN DEFAULT FALSE
```

---

### 3. 字符串类型

| MySQL 类型 | PostgreSQL 类型 | 最大长度 | 说明 | 示例字段 |
|-----------|----------------|---------|------|---------|
| `VARCHAR(n)` | `VARCHAR(n)` | 1~65535 字符 | 变长字符串 | `name`, `no`, `mobile` |
| `CHAR(n)` | `CHAR(n)` | 固定长度 | 定长字符串（ERP 未使用） | - |
| `TEXT` | `TEXT` | 无限制 | 长文本 | `remark`（备注字段） |

**编码处理**:
- MySQL: `CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci` → PostgreSQL: 默认 UTF8（无需显式指定）
- PostgreSQL 默认使用 UTF-8 编码，无需额外配置

---

### 4. 日期时间类型

| MySQL 类型 | PostgreSQL 类型 | 精度 | 说明 | 示例字段 |
|-----------|----------------|-----|------|---------|
| `DATETIME` | `TIMESTAMP` | 微秒 | 日期时间（无时区） | `create_time`, `order_time` |
| `TIMESTAMP` | `TIMESTAMP WITH TIME ZONE` | 微秒 | 带时区时间戳（ERP 未使用） | - |
| `DATE` | `DATE` | 日 | 日期（ERP 未使用） | - |

**默认值转换**:
```sql
-- MySQL
create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP

-- PostgreSQL
create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
```

---

## 🎯 默认值映射规则

### 1. 布尔类型默认值

| MySQL 默认值 | PostgreSQL 默认值 | 说明 |
|-------------|------------------|------|
| `DEFAULT b'0'` | `DEFAULT FALSE` | 假值 |
| `DEFAULT b'1'` | `DEFAULT TRUE` | 真值 |
| `NULL` | `NULL` | 空值 |

### 2. 数值类型默认值

| MySQL 默认值 | PostgreSQL 默认值 | 说明 |
|-------------|------------------|------|
| `DEFAULT 0` | `DEFAULT 0` | 零值 |
| `NULL` | `NULL` | 空值 |

### 3. 字符串类型默认值

| MySQL 默认值 | PostgreSQL 默认值 | 说明 |
|-------------|------------------|------|
| `DEFAULT ''` | `DEFAULT ''` | 空字符串 |
| `DEFAULT 'value'` | `DEFAULT 'value'` | 固定值 |
| `NULL` | `NULL` | 空值 |

### 4. 时间类型默认值

| MySQL 默认值 | PostgreSQL 默认值 | 说明 |
|-------------|------------------|------|
| `DEFAULT CURRENT_TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | 当前时间 |
| `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 需使用触发器实现 | 自动更新时间 |
| `NULL` | `NULL` | 空值 |

---

## ⚙️ 特殊字段处理

### 1. `update_time` 字段（自动更新时间）

**MySQL 语法**:
```sql
update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
```

**PostgreSQL 等价实现**:

#### Step 1: 创建全局触发器函数（仅需定义一次）

```sql
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

#### Step 2: 字段定义

```sql
update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
```

#### Step 3: 为每个表创建触发器

```sql
CREATE TRIGGER update_{table_name}_update_time
BEFORE UPDATE ON {table_name}
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();
```

**示例**:
```sql
-- erp_product 表的触发器
CREATE TRIGGER update_erp_product_update_time
BEFORE UPDATE ON erp_product
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();

-- erp_stock 表的触发器
CREATE TRIGGER update_erp_stock_update_time
BEFORE UPDATE ON erp_stock
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();
```

---

### 2. 主键自增字段

**MySQL 语法**:
```sql
id BIGINT NOT NULL AUTO_INCREMENT COMMENT '产品编号',
PRIMARY KEY (`id` DESC) USING BTREE
```

**PostgreSQL 语法（推荐）**:
```sql
id BIGSERIAL PRIMARY KEY
```

**说明**:
- `BIGSERIAL` 是 `BIGINT` + 自动创建序列的简写
- 自动创建名为 `{table_name}_id_seq` 的序列
- `PRIMARY KEY` 后不支持 `DESC`（PostgreSQL 主键不支持排序）

**完整示例**:
```sql
-- MySQL
CREATE TABLE `erp_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '产品编号',
  ...
  PRIMARY KEY (`id` DESC) USING BTREE
);

-- PostgreSQL
CREATE TABLE erp_product (
  id BIGSERIAL PRIMARY KEY,
  ...
);

COMMENT ON COLUMN erp_product.id IS '产品编号';
```

---

## 📝 约束和索引映射

### 1. 主键约束

| MySQL 语法 | PostgreSQL 语法 | 说明 |
|-----------|----------------|------|
| `PRIMARY KEY (\`id\` DESC)` | `PRIMARY KEY (id)` | 移除 `DESC`，PostgreSQL 不支持 |
| `PRIMARY KEY (\`id\`)` | `PRIMARY KEY (id)` | 保持一致 |

### 2. 非空约束

| MySQL 语法 | PostgreSQL 语法 | 说明 |
|-----------|----------------|------|
| `NOT NULL` | `NOT NULL` | 完全一致 |
| `NULL` | 无需显式指定 | 默认允许 NULL |

### 3. 唯一约束

| MySQL 语法 | PostgreSQL 语法 | 说明 |
|-----------|----------------|------|
| `UNIQUE KEY \`uk_name\` (\`name\`)` | `UNIQUE (name)` | PostgreSQL 自动创建索引 |

---

## 💬 注释处理规则

### MySQL 注释语法

```sql
CREATE TABLE `erp_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '产品编号',
  `name` varchar(255) NOT NULL COMMENT '产品名称',
  ...
) ENGINE = InnoDB COMMENT = 'ERP 产品表';
```

### PostgreSQL 注释语法

```sql
CREATE TABLE erp_product (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  ...
);

-- 表注释
COMMENT ON TABLE erp_product IS 'ERP 产品表';

-- 字段注释
COMMENT ON COLUMN erp_product.id IS '产品编号';
COMMENT ON COLUMN erp_product.name IS '产品名称';
```

---

## 🗑️ 需要移除的语法

以下 MySQL 特有语法在 PostgreSQL 中不存在，需要移除：

| MySQL 语法 | 说明 |
|-----------|------|
| `ENGINE = InnoDB` | PostgreSQL 无需指定存储引擎 |
| `AUTO_INCREMENT = 1` | PostgreSQL 使用序列管理自增 |
| `CHARACTER SET utf8mb4` | PostgreSQL 默认 UTF-8，无需指定 |
| `COLLATE utf8mb4_unicode_ci` | PostgreSQL 排序规则配置方式不同 |
| `USING BTREE` | PostgreSQL 默认使用 B-Tree 索引 |
| `DESC` in `PRIMARY KEY` | PostgreSQL 主键不支持排序 |
| 反引号 `` ` `` | PostgreSQL 使用双引号（通常省略） |

---

## 🎨 完整转换示例

### MySQL 表定义

```sql
DROP TABLE IF EXISTS `erp_product`;
CREATE TABLE `erp_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '产品编号',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '产品名称',
  `bar_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '产品条码',
  `status` tinyint NOT NULL COMMENT '产品状态',
  `purchase_price` decimal(24, 6) NULL DEFAULT NULL COMMENT '采购价格，单位：元',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id` DESC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'ERP 产品表';
```

### PostgreSQL 表定义

```sql
DROP TABLE IF EXISTS erp_product CASCADE;
CREATE TABLE erp_product (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  bar_code VARCHAR(255) DEFAULT NULL,
  status SMALLINT NOT NULL,
  purchase_price NUMERIC(24, 6) DEFAULT NULL,
  creator VARCHAR(64) DEFAULT '',
  create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updater VARCHAR(64) DEFAULT '',
  update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted BOOLEAN NOT NULL DEFAULT FALSE,
  tenant_id BIGINT NOT NULL DEFAULT 0
);

-- 表注释
COMMENT ON TABLE erp_product IS 'ERP 产品表';

-- 字段注释
COMMENT ON COLUMN erp_product.id IS '产品编号';
COMMENT ON COLUMN erp_product.name IS '产品名称';
COMMENT ON COLUMN erp_product.bar_code IS '产品条码';
COMMENT ON COLUMN erp_product.status IS '产品状态';
COMMENT ON COLUMN erp_product.purchase_price IS '采购价格，单位：元';
COMMENT ON COLUMN erp_product.creator IS '创建者';
COMMENT ON COLUMN erp_product.create_time IS '创建时间';
COMMENT ON COLUMN erp_product.updater IS '更新者';
COMMENT ON COLUMN erp_product.update_time IS '更新时间';
COMMENT ON COLUMN erp_product.deleted IS '是否删除';
COMMENT ON COLUMN erp_product.tenant_id IS '租户编号';

-- 创建 update_time 触发器
CREATE TRIGGER update_erp_product_update_time
BEFORE UPDATE ON erp_product
FOR EACH ROW
EXECUTE FUNCTION update_modified_column();
```

---

## ✅ 转换检查清单

在生成 PostgreSQL 脚本时，请确保完成以下转换：

- [ ] `BIGINT AUTO_INCREMENT` → `BIGSERIAL`
- [ ] `TINYINT` → `SMALLINT`
- [ ] `BIT(1)` → `BOOLEAN`
- [ ] `DECIMAL` → `NUMERIC`
- [ ] `DATETIME` → `TIMESTAMP`
- [ ] `DEFAULT b'0'` → `DEFAULT FALSE`
- [ ] `DEFAULT b'1'` → `DEFAULT TRUE`
- [ ] 移除 `CHARACTER SET` 和 `COLLATE`
- [ ] 移除 `ENGINE = InnoDB`
- [ ] 移除 `AUTO_INCREMENT = n`
- [ ] 移除 `USING BTREE`
- [ ] 移除主键的 `DESC`
- [ ] 移除反引号 `` ` ``
- [ ] 转换内联注释为 `COMMENT ON` 语句
- [ ] 为所有表创建 `update_time` 触发器

---

## 📚 参考资料

- [PostgreSQL 官方文档 - 数据类型](https://www.postgresql.org/docs/current/datatype.html)
- [PostgreSQL 官方文档 - 序列](https://www.postgresql.org/docs/current/sql-createsequence.html)
- [PostgreSQL 官方文档 - 触发器](https://www.postgresql.org/docs/current/sql-createtrigger.html)
- [MySQL to PostgreSQL 迁移指南](https://wiki.postgresql.org/wiki/Converting_from_other_Databases_to_PostgreSQL#MySQL)

---

**文档维护者**: AI Assistant
**最后审核**: 2025-11-13
**适用版本**: PostgreSQL 12+ (建议使用 14+)
