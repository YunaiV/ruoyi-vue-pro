# 提案：修复支付模块数据库脚本

## Why

支付模块（`yudao-module-pay`）的数据库初始化脚本可能已过时，需要根据代码实体类（DO）进行同步和修复，并补充 PostgreSQL 版本的数据库脚本。

当前问题：
1. 现有 MySQL 脚本 `yudao-module-pay/sql/pay-2025-05-12.sql` 可能与代码中的实体类定义不一致
2. 缺少 PostgreSQL 版本的支付模块初始化脚本
3. 数据库表结构需要与代码保持一致，确保多数据库支持的完整性

## What Changes

基于支付模块的14个数据对象（DO）修复和补充数据库脚本：

### 核心表（6个）
1. **pay_app** - 支付应用表
2. **pay_channel** - 支付渠道表
3. **pay_order** - 支付订单表
4. **pay_order_extension** - 支付订单扩展表
5. **pay_refund** - 支付退款表
6. **pay_transfer** - 转账单表

### 通知相关表（2个）
7. **pay_notify_task** - 支付通知任务表
8. **pay_notify_log** - 支付通知日志表

### 钱包相关表（4个）
9. **pay_wallet** - 会员钱包表
10. **pay_wallet_transaction** - 钱包流水表
11. **pay_wallet_recharge** - 钱包充值表
12. **pay_wallet_recharge_package** - 钱包充值套餐表

### 示例表（2个）
13. **pay_demo_order** - 示例订单表
14. **pay_demo_withdraw** - 示例提现表

### 变更内容
- 修复现有 MySQL 脚本中与代码不一致的字段定义
- 创建完整的 PostgreSQL 初始化脚本 `sql/postgresql/pay.sql`
- 确保所有表结构、索引、注释与代码实体类保持一致
- 支持序列（Sequence）用于 PostgreSQL 主键自增

## Impact

### 受影响的规范
- **pay-database**: 新增支付模块数据库规范

### 受影响的代码
- `yudao-module-pay/sql/pay-2025-05-12.sql` - MySQL 脚本修复
- `sql/postgresql/pay.sql` - 新增 PostgreSQL 脚本（将集成到主脚本）
- 影响范围：支付模块的所有14个数据表

### 受益方
- 使用 PostgreSQL 数据库的项目用户
- 需要重新初始化支付模块数据库的开发者
- 多数据库支持的统一性和完整性

### 风险
- 低风险：仅涉及数据库初始化脚本，不影响现有数据
- 需要验证：确保新脚本在 MySQL 和 PostgreSQL 中均可正常执行
