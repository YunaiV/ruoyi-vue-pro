# ERP 数据库结构清单分析报告

> **生成时间**: 2025-11-13
> **源文件**: `yudao-module-erp/sql/erp-2024-05-03.sql`
> **输出文件**: `mysql-script-schema.json`

---

## 📋 总览

- **表数量**: 33 个
- **总字段数**: 574 个
- **字符集**: utf8mb4
- **排序规则**: utf8mb4_unicode_ci
- **存储引擎**: InnoDB

---

## 📊 统计数据

| 指标 | 数量 |
|------|------|
| 总表数 | 33 |
| 总字段数 | 574 |
| 主键字段数 | 33 |
| 自增字段数 | 33 |
| 可空字段数 | 170 |
| 含默认值字段数 | 320 |

---

## 📦 模块划分

ERP 系统按业务模块组织，共包含 9 个子模块：

| 模块 | 表数量 | 说明 |
|------|--------|------|
| **account** | 1 | 结算账户管理 |
| **customer** | 1 | 客户信息管理 |
| **finance** | 4 | 财务管理（收款单、付款单） |
| **product** | 3 | 产品管理（产品、分类、单位） |
| **purchase** | 6 | 采购管理（订单、入库、退货） |
| **sale** | 6 | 销售管理（订单、出库、退货） |
| **stock** | 10 | 库存管理（库存、盘点、调拨、出入库） |
| **supplier** | 1 | 供应商信息管理 |
| **warehouse** | 1 | 仓库信息管理 |

---

## 📑 完整表清单

| 序号 | 表名 | 字段数 | 表注释 |
|------|------|--------|--------|
| 1 | `erp_account` | 13 | ERP 结算账户 |
| 2 | `erp_customer` | 21 | ERP 客户表 |
| 3 | `erp_finance_payment` | 17 | ERP 付款单表 |
| 4 | `erp_finance_payment_item` | 15 | ERP 付款项表 |
| 5 | `erp_finance_receipt` | 17 | ERP 收款单表 |
| 6 | `erp_finance_receipt_item` | 15 | ERP 收款项表 |
| 7 | `erp_product` | 19 | ERP 产品表 |
| 8 | `erp_product_category` | 12 | ERP 产品分类 |
| 9 | `erp_product_unit` | 9 | ERP 产品单位表 |
| 10 | `erp_purchase_in` | 24 | ERP 采购入库表 |
| 11 | `erp_purchase_in_items` | 18 | ERP 销售入库项表 |
| 12 | `erp_purchase_order` | 23 | ERP 采购订单表 |
| 13 | `erp_purchase_order_items` | 18 | ERP 采购订单项表 |
| 14 | `erp_purchase_return` | 24 | ERP 采购退货表 |
| 15 | `erp_purchase_return_items` | 18 | ERP 采购退货项表 |
| 16 | `erp_sale_order` | 24 | ERP 销售订单表 |
| 17 | `erp_sale_order_items` | 18 | ERP 销售订单项表 |
| 18 | `erp_sale_out` | 25 | ERP 销售出库表 |
| 19 | `erp_sale_out_items` | 18 | ERP 销售出库项表 |
| 20 | `erp_sale_return` | 25 | ERP 销售退货表 |
| 21 | `erp_sale_return_items` | 18 | ERP 销售退货项表 |
| 22 | `erp_stock` | 10 | ERP 产品库存表 |
| 23 | `erp_stock_check` | 14 | ERP 库存盘点单表 |
| 24 | `erp_stock_check_item` | 17 | ERP 库存盘点项表 |
| 25 | `erp_stock_in` | 15 | ERP 其它入库单表 |
| 26 | `erp_stock_in_item` | 15 | ERP 其它入库单项表 |
| 27 | `erp_stock_move` | 14 | ERP 库存调拨单表 |
| 28 | `erp_stock_move_item` | 16 | ERP 库存调拨项表 |
| 29 | `erp_stock_out` | 15 | ERP 其它入库单表 |
| 30 | `erp_stock_out_item` | 15 | ERP 其它出库单项表 |
| 31 | `erp_stock_record` | 15 | ERP 产品库存明细表 |
| 32 | `erp_supplier` | 21 | ERP 供应商表 |
| 33 | `erp_warehouse` | 16 | ERP 仓库表 |

---

## 🔤 字段类型分布

| 类型 | 使用次数 | 占比 |
|------|----------|------|
| `VARCHAR` | 155 | 27.0% |
| `BIGINT` | 149 | 26.0% |
| `DECIMAL(24,6)` | 129 | 22.5% |
| `DATETIME` | 78 | 13.6% |
| `BIT(1)` | 35 | 6.1% |
| `TINYINT` | 22 | 3.8% |
| `INT` | 6 | 1.0% |

---

## 🏗️ 设计规范

### 统一字段

所有表均包含以下审计字段：

- `creator` (VARCHAR(64)): 创建者
- `create_time` (DATETIME): 创建时间，默认 `CURRENT_TIMESTAMP`
- `updater` (VARCHAR(64)): 更新者
- `update_time` (DATETIME): 更新时间，自动更新 `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`
- `deleted` (BIT(1)): 逻辑删除标记，默认 `b'0'`
- `tenant_id` (BIGINT): 租户编号，支持多租户隔离，默认 `0`

### 主键设计

- 所有表使用自增 `id` (BIGINT) 作为主键
- 部分表（如订单、入库单等）有唯一索引 `no`（单据编号）

### 金额字段

- 统一使用 `DECIMAL(24,6)` 类型
- 注释中明确标注单位（元）

---

## 📖 核心业务流程

### 采购流程

```
采购订单 (erp_purchase_order)
    ↓
采购入库 (erp_purchase_in + erp_purchase_in_items)
    ↓
付款单 (erp_finance_payment + erp_finance_payment_item)
    ↓
采购退货 (erp_purchase_return + erp_purchase_return_items)
```

### 销售流程

```
销售订单 (erp_sale_order)
    ↓
销售出库 (erp_sale_out + erp_sale_out_items)
    ↓
收款单 (erp_finance_receipt + erp_finance_receipt_item)
    ↓
销售退货 (erp_sale_return + erp_sale_return_items)
```

### 库存管理

```
产品 (erp_product)
    ↓
仓库 (erp_warehouse)
    ↓
库存 (erp_stock)
    ↓
库存明细 (erp_stock_record)
    ↓
库存盘点 (erp_stock_check + erp_stock_check_item)
    ↓
库存调拨 (erp_stock_move + erp_stock_move_item)
    ↓
其它出入库 (erp_stock_in/out + erp_stock_in/out_item)
```

---

## ✅ 验证结果

- ✅ 所有表均有主键
- ✅ 所有表均包含多租户字段 `tenant_id`
- ✅ 所有表均包含完整的审计字段
- ✅ 数据类型使用规范统一
- ✅ JSON 结构完整有效

---

## 📄 相关文件

- **源 SQL 脚本**: `/home/myu/Source/github/ruoyi-vue-pro/yudao-module-erp/sql/erp-2024-05-03.sql`
- **JSON 清单**: `/home/myu/Source/github/ruoyi-vue-pro/openspec/changes/restore-erp-database-schema/mysql-script-schema.json`
- **解析脚本**: `/tmp/parse_erp_sql.py`

---

## 🎯 使用建议

1. **数据库迁移**: 可基于此清单生成其他数据库（PostgreSQL、Oracle 等）的 DDL 脚本
2. **代码生成**: 可根据表结构自动生成实体类、Mapper、Service 等代码
3. **文档生成**: 可生成数据字典、ER 图等文档
4. **对比分析**: 可与其他版本的脚本对比，识别结构变更

---

*报告生成时间: 2025-11-13*
