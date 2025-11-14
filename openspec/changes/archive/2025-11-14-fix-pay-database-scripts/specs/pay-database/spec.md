# 支付模块数据库规范

## ADDED Requirements

### Requirement: 支付模块数据库表结构规范

支付模块 SHALL 提供完整的数据库初始化脚本，支持 MySQL 和 PostgreSQL 两种数据库，表结构必须与代码实体类（DO）完全一致。

#### Scenario: MySQL 数据库初始化

- **WHEN** 使用 MySQL 数据库部署支付模块
- **THEN** 执行 `yudao-module-pay/sql/pay-2025-05-12.sql` 脚本
- **AND** 成功创建14个支付相关数据表
- **AND** 所有表字段、类型、约束与实体类定义一致
- **AND** 包含必要的索引和审计字段

#### Scenario: PostgreSQL 数据库初始化

- **WHEN** 使用 PostgreSQL 数据库部署支付模块
- **THEN** 执行 `sql/postgresql/pay.sql` 脚本（或集成到主脚本）
- **AND** 成功创建14个支付相关数据表
- **AND** 所有表字段、类型、约束与实体类定义一致
- **AND** 为每个表创建对应的序列（Sequence）用于主键自增
- **AND** 包含必要的索引和字段注释

#### Scenario: 数据类型映射正确性

- **WHEN** 从 MySQL 脚本转换为 PostgreSQL 脚本
- **THEN** 数据类型映射如下：
  - `bit(1)` → `int2`
  - `tinyint` → `int2`
  - `bigint` → `int8`
  - `int` → `int4`
  - `datetime` → `timestamp`
  - JSON 字段（如 `config`、`channel_extras`）→ `text` 或 `jsonb`
- **AND** 保持字段长度和精度一致

### Requirement: 支付核心表结构

支付模块 SHALL 包含以下核心表，每个表必须有主键、审计字段和适当的索引。

#### Scenario: 支付应用表（pay_app）

- **WHEN** 创建支付应用表
- **THEN** 包含字段：id, app_key, name, status, remark, order_notify_url, refund_notify_url, transfer_notify_url
- **AND** 包含审计字段：creator, create_time, updater, update_time, deleted, tenant_id
- **AND** 主键为 id（自增）
- **AND** PostgreSQL 创建序列 pay_app_seq

#### Scenario: 支付渠道表（pay_channel）

- **WHEN** 创建支付渠道表
- **THEN** 包含字段：id, code, status, fee_rate, remark, app_id, config
- **AND** config 字段为 JSON 类型（MySQL: text, PostgreSQL: text 或 jsonb）
- **AND** 包含租户字段：tenant_id
- **AND** 设置 autoResultMap = true 用于 MyBatis Plus JSON 处理

#### Scenario: 支付订单表（pay_order）

- **WHEN** 创建支付订单表
- **THEN** 包含核心字段：id, app_id, channel_id, channel_code, user_id, user_type
- **AND** 包含商户字段：merchant_order_id, subject, body, notify_url
- **AND** 包含订单字段：price, channel_fee_rate, channel_fee_price, status, user_ip, expire_time, success_time
- **AND** 包含拓展字段：extension_id, no
- **AND** 包含退款字段：refund_price
- **AND** 包含渠道字段：channel_user_id, channel_order_no

#### Scenario: 支付订单扩展表（pay_order_extension）

- **WHEN** 创建支付订单扩展表
- **THEN** 包含字段：id, no, order_id, channel_id, channel_code, user_ip, status
- **AND** 包含 channel_extras（JSON 类型）
- **AND** 包含渠道响应字段：channel_error_code, channel_error_msg, channel_notify_data

### Requirement: 支付业务表结构

支付模块 SHALL 支持退款、转账、通知等业务功能的数据表。

#### Scenario: 支付退款表（pay_refund）

- **WHEN** 创建支付退款表
- **THEN** 包含字段：id, no, app_id, channel_id, channel_code, order_id, order_no
- **AND** 包含用户字段：user_id, user_type
- **AND** 包含商户字段：merchant_order_id, merchant_refund_id, notify_url
- **AND** 包含退款字段：status, pay_price, refund_price, reason, user_ip
- **AND** 包含渠道字段：channel_order_no, channel_refund_no, success_time
- **AND** 包含错误字段：channel_error_code, channel_error_msg, channel_notify_data

#### Scenario: 转账单表（pay_transfer）

- **WHEN** 创建转账单表
- **THEN** 包含字段：id, no, app_id, channel_id, channel_code, user_id, user_type
- **AND** 包含商户字段：merchant_transfer_id
- **AND** 包含转账字段：subject, price, user_account, user_name, status, success_time
- **AND** 包含其他字段：notify_url, user_ip, channel_extras（JSON）
- **AND** 包含渠道字段：channel_transfer_no, channel_error_code, channel_error_msg, channel_notify_data, channel_package_info

#### Scenario: 支付通知任务表（pay_notify_task）

- **WHEN** 创建支付通知任务表
- **THEN** 包含字段：id, app_id, type, data_id
- **AND** 包含商户字段：merchant_order_id, merchant_refund_id, merchant_transfer_id
- **AND** 包含通知字段：status, next_notify_time, last_execute_time, notify_times, max_notify_times, notify_url
- **AND** 包含租户字段：tenant_id

#### Scenario: 支付通知日志表（pay_notify_log）

- **WHEN** 创建支付通知日志表
- **THEN** 包含字段：id, task_id, notify_times, response, status

### Requirement: 钱包相关表结构

支付模块 SHALL 支持会员钱包功能的数据表。

#### Scenario: 会员钱包表（pay_wallet）

- **WHEN** 创建会员钱包表
- **THEN** 包含字段：id, user_id, user_type
- **AND** 包含余额字段：balance, freeze_price
- **AND** 包含统计字段：total_expense, total_recharge

#### Scenario: 钱包流水表（pay_wallet_transaction）

- **WHEN** 创建钱包流水表
- **THEN** 包含字段：id, no, wallet_id, biz_type, biz_id, title, price, balance

#### Scenario: 钱包充值表（pay_wallet_recharge）

- **WHEN** 创建钱包充值表
- **THEN** 包含字段：id, wallet_id, total_price, pay_price, bonus_price, package_id
- **AND** 包含支付字段：pay_status, pay_order_id, pay_channel_code, pay_time
- **AND** 包含退款字段：pay_refund_id, refund_total_price, refund_pay_price, refund_bonus_price, refund_time, refund_status

#### Scenario: 钱包充值套餐表（pay_wallet_recharge_package）

- **WHEN** 创建钱包充值套餐表
- **THEN** 包含字段：id, name, pay_price, bonus_price, status

### Requirement: 示例表结构

支付模块 SHALL 提供示例表用于演示支付和转账功能的接入。

#### Scenario: 示例订单表（pay_demo_order）

- **WHEN** 创建示例订单表
- **THEN** 包含字段：id, user_id, spu_id, spu_name, price
- **AND** 包含支付字段：pay_status, pay_order_id, pay_time, pay_channel_code
- **AND** 包含退款字段：pay_refund_id, refund_price, refund_time

#### Scenario: 示例提现表（pay_demo_withdraw）

- **WHEN** 创建示例提现表
- **THEN** 包含字段：id, subject, price, user_account, user_name, type, status
- **AND** 包含转账字段：pay_transfer_id, transfer_channel_code, transfer_time, transfer_error_msg

### Requirement: 数据库审计和多租户支持

所有支付模块的数据表 SHALL 包含统一的审计字段和多租户支持（部分表）。

#### Scenario: 审计字段完整性

- **WHEN** 创建任何支付模块数据表
- **THEN** 必须包含以下审计字段：
  - `creator` varchar(64) - 创建者
  - `create_time` datetime/timestamp - 创建时间
  - `updater` varchar(64) - 更新者
  - `update_time` datetime/timestamp - 更新时间
  - `deleted` bit(1)/int2 - 是否删除（逻辑删除标记）

#### Scenario: 多租户字段配置

- **WHEN** 表需要支持多租户（如 pay_app, pay_channel, pay_notify_task）
- **THEN** 必须包含字段：`tenant_id` bigint/int8 NOT NULL DEFAULT 0
- **AND** 在实体类中继承 TenantBaseDO 而非 BaseDO

#### Scenario: PostgreSQL 序列命名规范

- **WHEN** 为 PostgreSQL 创建表的主键序列
- **THEN** 序列名称格式为：`表名_seq`
- **AND** 序列起始值为 1
- **EXAMPLE** pay_app 表的序列为 pay_app_seq
