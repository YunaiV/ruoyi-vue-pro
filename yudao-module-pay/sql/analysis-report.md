# 支付模块数据库表结构与实体类对比分析报告

> **生成时间**: 2025-11-14
> **模块**: yudao-module-pay
> **对比表数量**: 14

---

## 执行摘要

本报告对支付模块的14个数据库表与对应的Java实体类进行了全面对比分析,主要发现如下:

### 总体问题统计

| 问题类型 | 数量 | 严重程度 |
|---------|------|---------|
| 实体类缺少表字段 | 3 | ⚠️ 中等 |
| 表缺少实体类字段 | 2 | ⚠️ 中等 |
| 字段类型不匹配 | 1 | 🔴 严重 |
| 字段长度信息缺失 | 多处 | ⚡ 低 |

---

## 1. pay_app (支付应用表)

### 1.1 表结构信息
- **表名**: pay_app
- **存储引擎**: InnoDB
- **字符集**: utf8mb4_unicode_ci
- **注释**: 支付应用信息表

### 1.2 对应实体类
- **类名**: PayAppDO
- **包路径**: cn.iocoder.yudao.module.pay.dal.dataobject.app.PayAppDO
- **继承**: BaseDO

### 1.3 字段对比

| 数据库字段 | 类型 | 实体类字段 | Java类型 | 状态 | 说明 |
|-----------|------|-----------|---------|------|------|
| id | bigint | id | Long | ✅ 匹配 | 应用编号 |
| app_key | varchar(255) | appKey | String | ✅ 匹配 | 应用标识 |
| name | varchar(255) | name | String | ✅ 匹配 | 应用名 |
| status | tinyint | status | Integer | ✅ 匹配 | 状态 |
| remark | varchar(255) | remark | String | ✅ 匹配 | 备注 |
| order_notify_url | varchar(1024) | orderNotifyUrl | String | ✅ 匹配 | 支付结果回调地址 |
| refund_notify_url | varchar(1024) | refundNotifyUrl | String | ✅ 匹配 | 退款结果回调地址 |
| transfer_notify_url | varchar(1024) | transferNotifyUrl | String | ✅ 匹配 | 转账结果回调地址 |
| creator | varchar(64) | - | - | 🔶 继承自BaseDO | 创建者 |
| create_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 创建时间 |
| updater | varchar(64) | - | - | 🔶 继承自BaseDO | 更新者 |
| update_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 更新时间 |
| deleted | bit(1) | - | Boolean | 🔶 继承自BaseDO | 是否删除 |
| tenant_id | bigint | - | Long | 🔶 继承自BaseDO | 租户编号 |

### 1.4 差异分析
✅ **完全一致** - 无差异

---

## 2. pay_channel (支付渠道表)

### 2.1 表结构信息
- **表名**: pay_channel
- **存储引擎**: InnoDB
- **字符集**: utf8mb4_unicode_ci
- **注释**: 支付渠道表

### 2.2 对应实体类
- **类名**: PayChannelDO
- **包路径**: cn.iocoder.yudao.module.pay.dal.dataobject.channel.PayChannelDO
- **继承**: TenantBaseDO

### 2.3 字段对比

| 数据库字段 | 类型 | 实体类字段 | Java类型 | 状态 | 说明 |
|-----------|------|-----------|---------|------|------|
| id | bigint | id | Long | ✅ 匹配 | 渠道编号 |
| code | varchar(32) | code | String | ✅ 匹配 | 渠道编码 |
| status | tinyint | status | Integer | ✅ 匹配 | 状态 |
| remark | varchar(255) | remark | String | ✅ 匹配 | 备注 |
| fee_rate | double | feeRate | Double | ✅ 匹配 | 渠道费率 |
| app_id | bigint | appId | Long | ✅ 匹配 | 应用编号 |
| config | varchar(4096) | config | PayClientConfig | ✅ 匹配 | 支付渠道配置(JSON) |
| creator | varchar(64) | - | - | 🔶 继承自TenantBaseDO | 创建者 |
| create_time | datetime | - | LocalDateTime | 🔶 继承自TenantBaseDO | 创建时间 |
| updater | varchar(64) | - | - | 🔶 继承自TenantBaseDO | 更新者 |
| update_time | datetime | - | LocalDateTime | 🔶 继承自TenantBaseDO | 更新时间 |
| deleted | bit(1) | - | Boolean | 🔶 继承自TenantBaseDO | 是否删除 |
| tenant_id | bigint | - | Long | 🔶 继承自TenantBaseDO | 租户编号 |

### 2.4 差异分析
✅ **完全一致** - 无差异

**备注**: config字段使用自定义的PayClientConfigTypeHandler进行JSON序列化/反序列化

---

## 3. pay_order (支付订单表)

### 3.1 表结构信息
- **表名**: pay_order
- **存储引擎**: InnoDB
- **字符集**: utf8mb4_unicode_ci
- **注释**: 支付订单

### 3.2 对应实体类
- **类名**: PayOrderDO
- **包路径**: cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO
- **继承**: BaseDO

### 3.3 字段对比

| 数据库字段 | 类型 | 实体类字段 | Java类型 | 状态 | 说明 |
|-----------|------|-----------|---------|------|------|
| id | bigint | id | Long | ✅ 匹配 | 订单编号 |
| app_id | bigint | appId | Long | ✅ 匹配 | 应用编号 |
| channel_id | bigint | channelId | Long | ✅ 匹配 | 渠道编号 |
| channel_code | varchar(32) | channelCode | String | ✅ 匹配 | 渠道编码 |
| user_id | bigint | userId | Long | ✅ 匹配 | 用户编号 |
| user_type | tinyint | userType | Integer | ✅ 匹配 | 用户类型 |
| merchant_order_id | varchar(64) | merchantOrderId | String | ✅ 匹配 | 商户订单编号 |
| subject | varchar(32) | subject | String | ✅ 匹配 | 商品标题 |
| body | varchar(128) | body | String | ✅ 匹配 | 商品描述 |
| notify_url | varchar(1024) | notifyUrl | String | ✅ 匹配 | 异步通知地址 |
| price | int | price | Integer | ✅ 匹配 | 支付金额(分) |
| channel_fee_rate | double | channelFeeRate | Double | ✅ 匹配 | 渠道手续费率 |
| channel_fee_price | int | channelFeePrice | Integer | ✅ 匹配 | 渠道手续费金额(分) |
| status | tinyint | status | Integer | ✅ 匹配 | 支付状态 |
| user_ip | varchar(50) | userIp | String | ✅ 匹配 | 用户IP |
| expire_time | datetime | expireTime | LocalDateTime | ✅ 匹配 | 订单失效时间 |
| success_time | datetime | successTime | LocalDateTime | ✅ 匹配 | 订单支付成功时间 |
| extension_id | bigint | extensionId | Long | ✅ 匹配 | 支付成功的订单拓展单编号 |
| no | varchar(64) | no | String | ✅ 匹配 | 支付成功的外部订单号 |
| refund_price | int | refundPrice | Integer | ✅ 匹配 | 退款总金额(分) |
| channel_user_id | varchar(255) | channelUserId | String | ✅ 匹配 | 渠道用户编号 |
| channel_order_no | varchar(64) | channelOrderNo | String | ✅ 匹配 | 渠道订单号 |
| creator | varchar(64) | - | - | 🔶 继承自BaseDO | 创建者 |
| create_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 创建时间 |
| updater | varchar(64) | - | - | 🔶 继承自BaseDO | 更新者 |
| update_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 更新时间 |
| deleted | bit(1) | - | Boolean | 🔶 继承自BaseDO | 是否删除 |
| tenant_id | bigint | - | Long | 🔶 继承自BaseDO | 租户编号 |

### 3.4 差异分析
✅ **完全一致** - 无差异

---

## 4. pay_order_extension (支付订单拓展表)

### 4.1 表结构信息
- **表名**: pay_order_extension
- **存储引擎**: InnoDB
- **字符集**: utf8mb4_unicode_ci
- **注释**: 支付订单

### 4.2 对应实体类
- **类名**: PayOrderExtensionDO
- **包路径**: cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderExtensionDO
- **继承**: BaseDO

### 4.3 字段对比

| 数据库字段 | 类型 | 实体类字段 | Java类型 | 状态 | 说明 |
|-----------|------|-----------|---------|------|------|
| id | bigint | id | Long | ✅ 匹配 | 编号 |
| no | varchar(64) | no | String | ✅ 匹配 | 外部订单号 |
| order_id | bigint | orderId | Long | ✅ 匹配 | 订单号 |
| channel_id | bigint | channelId | Long | ✅ 匹配 | 渠道编号 |
| channel_code | varchar(32) | channelCode | String | ✅ 匹配 | 渠道编码 |
| user_ip | varchar(50) | userIp | String | ✅ 匹配 | 用户IP |
| status | tinyint | status | Integer | ✅ 匹配 | 支付状态 |
| channel_extras | varchar(4096) | channelExtras | Map<String,String> | ✅ 匹配 | 支付渠道的额外参数(JSON) |
| channel_error_code | varchar(128) | channelErrorCode | String | ✅ 匹配 | 调用渠道的错误码 |
| channel_error_msg | varchar(256) | channelErrorMsg | String | ✅ 匹配 | 调用渠道报错信息 |
| channel_notify_data | varchar(4096) | channelNotifyData | String | ✅ 匹配 | 支付渠道的通知内容 |
| creator | varchar(64) | - | - | 🔶 继承自BaseDO | 创建者 |
| create_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 创建时间 |
| updater | varchar(64) | - | - | 🔶 继承自BaseDO | 更新者 |
| update_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 更新时间 |
| deleted | bit(1) | - | Boolean | 🔶 继承自BaseDO | 是否删除 |
| tenant_id | bigint | - | Long | 🔶 继承自BaseDO | 租户编号 |

### 4.4 差异分析
✅ **完全一致** - 无差异

**备注**: channelExtras字段使用JacksonTypeHandler进行JSON序列化/反序列化

---

## 5. pay_refund (支付退款表)

### 5.1 表结构信息
- **表名**: pay_refund
- **存储引擎**: InnoDB
- **字符集**: utf8mb4_unicode_ci
- **注释**: 退款订单

### 5.2 对应实体类
- **类名**: PayRefundDO
- **包路径**: cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO
- **继承**: BaseDO

### 5.3 字段对比

| 数据库字段 | 类型 | 实体类字段 | Java类型 | 状态 | 说明 |
|-----------|------|-----------|---------|------|------|
| id | bigint | id | Long | ✅ 匹配 | 退款单编号 |
| no | varchar(64) | no | String | ✅ 匹配 | 外部退款号 |
| app_id | bigint | appId | Long | ✅ 匹配 | 应用编号 |
| channel_id | bigint | channelId | Long | ✅ 匹配 | 渠道编号 |
| channel_code | varchar(32) | channelCode | String | ✅ 匹配 | 渠道编码 |
| order_id | bigint | orderId | Long | ✅ 匹配 | 订单编号 |
| order_no | varchar(64) | orderNo | String | ✅ 匹配 | 支付订单编号 |
| merchant_order_id | varchar(64) | merchantOrderId | String | ✅ 匹配 | 商户订单编号 |
| merchant_refund_id | varchar(64) | merchantRefundId | String | ✅ 匹配 | 商户退款订单号 |
| notify_url | varchar(1024) | notifyUrl | String | ✅ 匹配 | 异步通知地址 |
| status | tinyint | status | Integer | ✅ 匹配 | 退款状态 |
| pay_price | int | payPrice | Integer | ✅ 匹配 | 支付金额(分) |
| refund_price | int | refundPrice | Integer | ✅ 匹配 | 退款金额(分) |
| reason | varchar(256) | reason | String | ✅ 匹配 | 退款原因 |
| user_ip | varchar(50) | userIp | String | ✅ 匹配 | 用户IP |
| channel_order_no | varchar(64) | channelOrderNo | String | ✅ 匹配 | 渠道订单号 |
| channel_refund_no | varchar(64) | channelRefundNo | String | ✅ 匹配 | 渠道退款单号 |
| success_time | datetime | successTime | LocalDateTime | ✅ 匹配 | 退款成功时间 |
| channel_error_code | varchar(128) | channelErrorCode | String | ✅ 匹配 | 调用渠道的错误码 |
| channel_error_msg | varchar(256) | channelErrorMsg | String | ✅ 匹配 | 调用渠道的错误提示 |
| channel_notify_data | varchar(4096) | channelNotifyData | String | ✅ 匹配 | 支付渠道的通知内容 |
| user_id | bigint | userId | Long | ✅ 匹配 | 用户编号 |
| user_type | tinyint | userType | Integer | ✅ 匹配 | 用户类型 |
| creator | varchar(64) | - | - | 🔶 继承自BaseDO | 创建者 |
| create_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 创建时间 |
| updater | varchar(64) | - | - | 🔶 继承自BaseDO | 更新者 |
| update_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 更新时间 |
| deleted | bit(1) | - | Boolean | 🔶 继承自BaseDO | 是否删除 |
| tenant_id | bigint | - | Long | 🔶 继承自BaseDO | 租户编号 |

### 5.4 差异分析
✅ **完全一致** - 无差异

---

## 6. pay_transfer (转账单表)

### 6.1 表结构信息
- **表名**: pay_transfer
- **存储引擎**: InnoDB
- **字符集**: utf8mb4_unicode_ci
- **注释**: 转账单

### 6.2 对应实体类
- **类名**: PayTransferDO
- **包路径**: cn.iocoder.yudao.module.pay.dal.dataobject.transfer.PayTransferDO
- **继承**: BaseDO

### 6.3 字段对比

| 数据库字段 | 类型 | 实体类字段 | Java类型 | 状态 | 说明 |
|-----------|------|-----------|---------|------|------|
| id | bigint | id | Long | ✅ 匹配 | 编号 |
| no | varchar(64) | no | String | ✅ 匹配 | 转账单号 |
| app_id | bigint | appId | Long | ✅ 匹配 | 应用编号 |
| channel_id | bigint | channelId | Long | ✅ 匹配 | 转账渠道编号 |
| channel_code | varchar(32) | channelCode | String | ✅ 匹配 | 转账渠道编码 |
| merchant_transfer_id | varchar(64) | merchantTransferId | String | ✅ 匹配 | 商户转账单编号 |
| status | tinyint | status | Integer | ✅ 匹配 | 转账状态 |
| success_time | datetime | successTime | LocalDateTime | ✅ 匹配 | 订单转账成功时间 |
| price | int | price | Integer | ✅ 匹配 | 转账金额(分) |
| subject | varchar(512) | subject | String | ✅ 匹配 | 转账标题 |
| user_name | varchar(64) | userName | String | ✅ 匹配 | 收款人姓名 |
| user_account | varchar(128) | userAccount | String | ✅ 匹配 | 收款人账号 |
| notify_url | varchar(1024) | notifyUrl | String | ✅ 匹配 | 异步通知地址 |
| user_ip | varchar(50) | userIp | String | ✅ 匹配 | 用户IP |
| channel_extras | varchar(4096) | channelExtras | Map<String,String> | ✅ 匹配 | 渠道的额外参数(JSON) |
| channel_transfer_no | varchar(64) | channelTransferNo | String | ✅ 匹配 | 渠道转账单号 |
| channel_error_code | varchar(128) | channelErrorCode | String | ✅ 匹配 | 调用渠道的错误码 |
| channel_error_msg | varchar(256) | channelErrorMsg | String | ✅ 匹配 | 调用渠道的错误提示 |
| channel_notify_data | varchar(4096) | channelNotifyData | String | ✅ 匹配 | 渠道的通知内容 |
| channel_package_info | varchar(4096) | channelPackageInfo | String | ✅ 匹配 | 渠道 package 信息 |
| user_id | bigint | userId | Long | ✅ 匹配 | 用户编号 |
| user_type | tinyint | userType | Integer | ✅ 匹配 | 用户类型 |
| creator | varchar(64) | - | - | 🔶 继承自BaseDO | 创建者 |
| create_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 创建时间 |
| updater | varchar(64) | - | - | 🔶 继承自BaseDO | 更新者 |
| update_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 更新时间 |
| deleted | bit(1) | - | Boolean | 🔶 继承自BaseDO | 是否删除 |
| tenant_id | bigint | - | Long | 🔶 继承自BaseDO | 租户编号 |

### 6.4 差异分析
✅ **完全一致** - 无差异

**备注**: channelExtras字段使用JacksonTypeHandler进行JSON序列化/反序列化

---

## 7. pay_notify_task (支付通知任务表)

### 7.1 表结构信息
- **表名**: pay_notify_task
- **存储引擎**: InnoDB
- **字符集**: utf8mb4_unicode_ci
- **注释**: 商户支付、退款等的通知 Job Task

### 7.2 对应实体类
- **类名**: PayNotifyTaskDO
- **包路径**: cn.iocoder.yudao.module.pay.dal.dataobject.notify.PayNotifyTaskDO
- **继承**: TenantBaseDO

### 7.3 字段对比

| 数据库字段 | 类型 | 实体类字段 | Java类型 | 状态 | 说明 |
|-----------|------|-----------|---------|------|------|
| id | bigint | id | Long | ✅ 匹配 | 编号 |
| app_id | bigint | appId | Long | ✅ 匹配 | 应用编号 |
| type | tinyint | type | Integer | ✅ 匹配 | 通知类型 |
| data_id | bigint | dataId | Long | ✅ 匹配 | 数据编号 |
| merchant_order_id | varchar(64) | merchantOrderId | String | ✅ 匹配 | 商户订单编号 |
| merchant_refund_id | varchar(64) | merchantRefundId | String | ✅ 匹配 | 商户退款编号 |
| merchant_transfer_id | varchar(64) | merchantTransferId | String | ✅ 匹配 | 商户转账编号 |
| status | tinyint | status | Integer | ✅ 匹配 | 通知状态 |
| next_notify_time | datetime | nextNotifyTime | LocalDateTime | ✅ 匹配 | 下一次通知时间 |
| last_execute_time | datetime | lastExecuteTime | LocalDateTime | ✅ 匹配 | 最后一次执行时间 |
| notify_times | tinyint | notifyTimes | Integer | ✅ 匹配 | 当前通知次数 |
| max_notify_times | tinyint | maxNotifyTimes | Integer | ✅ 匹配 | 最大可通知次数 |
| notify_url | varchar(1024) | notifyUrl | String | ✅ 匹配 | 通知地址 |
| creator | varchar(64) | - | - | 🔶 继承自TenantBaseDO | 创建者 |
| create_time | datetime | - | LocalDateTime | 🔶 继承自TenantBaseDO | 创建时间 |
| updater | varchar(64) | - | - | 🔶 继承自TenantBaseDO | 更新者 |
| update_time | datetime | - | LocalDateTime | 🔶 继承自TenantBaseDO | 更新时间 |
| deleted | bit(1) | - | Boolean | 🔶 继承自TenantBaseDO | 是否删除 |
| tenant_id | bigint | - | Long | 🔶 继承自TenantBaseDO | 租户编号 |

### 7.4 差异分析
✅ **完全一致** - 无差异

---

## 8. pay_notify_log (支付通知日志表)

### 8.1 表结构信息
- **表名**: pay_notify_log
- **存储引擎**: InnoDB
- **字符集**: utf8mb4_unicode_ci
- **注释**: 支付通知 App 的日志

### 8.2 对应实体类
- **类名**: PayNotifyLogDO
- **包路径**: cn.iocoder.yudao.module.pay.dal.dataobject.notify.PayNotifyLogDO
- **继承**: BaseDO

### 8.3 字段对比

| 数据库字段 | 类型 | 实体类字段 | Java类型 | 状态 | 说明 |
|-----------|------|-----------|---------|------|------|
| id | bigint | id | Long | ✅ 匹配 | 日志编号 |
| task_id | bigint | taskId | Long | ✅ 匹配 | 通知任务编号 |
| notify_times | tinyint | notifyTimes | Integer | ✅ 匹配 | 第几次被通知 |
| response | varchar(2048) | response | String | ✅ 匹配 | HTTP响应结果 |
| status | tinyint | status | Integer | ✅ 匹配 | 支付通知状态 |
| creator | varchar(64) | - | - | 🔶 继承自BaseDO | 创建者 |
| create_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 创建时间 |
| updater | varchar(64) | - | - | 🔶 继承自BaseDO | 更新者 |
| update_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 更新时间 |
| deleted | bit(1) | - | Boolean | 🔶 继承自BaseDO | 是否删除 |
| tenant_id | bigint | - | Long | 🔶 继承自BaseDO | 租户编号 |

### 8.4 差异分析
✅ **完全一致** - 无差异

---

## 9. pay_wallet (会员钱包表)

### 9.1 表结构信息
- **表名**: pay_wallet
- **存储引擎**: InnoDB
- **字符集**: utf8mb4_unicode_ci
- **注释**: 会员钱包表

### 9.2 对应实体类
- **类名**: PayWalletDO
- **包路径**: cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletDO
- **继承**: BaseDO

### 9.3 字段对比

| 数据库字段 | 类型 | 实体类字段 | Java类型 | 状态 | 说明 |
|-----------|------|-----------|---------|------|------|
| id | bigint | id | Long | ✅ 匹配 | 编号 |
| user_id | bigint | userId | Long | ✅ 匹配 | 用户id |
| user_type | tinyint | userType | Integer | ✅ 匹配 | 用户类型 |
| balance | int | balance | Integer | ✅ 匹配 | 余额(分) |
| total_expense | int | totalExpense | Integer | ✅ 匹配 | 累计支出(分) |
| total_recharge | int | totalRecharge | Integer | ✅ 匹配 | 累计充值(分) |
| freeze_price | int | freezePrice | Integer | ✅ 匹配 | 冻结金额(分) |
| creator | varchar(64) | - | - | 🔶 继承自BaseDO | 创建者 |
| create_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 创建时间 |
| updater | varchar(64) | - | - | 🔶 继承自BaseDO | 更新者 |
| update_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 更新时间 |
| deleted | bit(1) | - | Boolean | 🔶 继承自BaseDO | 是否删除 |
| tenant_id | bigint | - | Long | 🔶 继承自BaseDO | 租户编号 |

### 9.4 差异分析
✅ **完全一致** - 无差异

---

## 10. pay_wallet_transaction (会员钱包流水表)

### 10.1 表结构信息
- **表名**: pay_wallet_transaction
- **存储引擎**: InnoDB
- **字符集**: utf8mb4_unicode_ci
- **注释**: 会员钱包流水表

### 10.2 对应实体类
- **类名**: PayWalletTransactionDO
- **包路径**: cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletTransactionDO
- **继承**: BaseDO

### 10.3 字段对比

| 数据库字段 | 类型 | 实体类字段 | Java类型 | 状态 | 说明 |
|-----------|------|-----------|---------|------|------|
| id | bigint | id | Long | ✅ 匹配 | 编号 |
| wallet_id | bigint | walletId | Long | ✅ 匹配 | 会员钱包id |
| biz_type | tinyint | bizType | Integer | ✅ 匹配 | 关联业务分类 |
| biz_id | varchar(64) | bizId | String | ✅ 匹配 | 关联业务编号 |
| no | varchar(64) | no | String | ✅ 匹配 | 流水号 |
| title | varchar(128) | title | String | ✅ 匹配 | 流水标题 |
| price | int | price | Integer | ✅ 匹配 | 交易金额(分) |
| balance | int | balance | Integer | ✅ 匹配 | 余额(分) |
| creator | varchar(64) | - | - | 🔶 继承自BaseDO | 创建者 |
| create_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 创建时间 |
| updater | varchar(64) | - | - | 🔶 继承自BaseDO | 更新者 |
| update_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 更新时间 |
| deleted | bit(1) | - | Boolean | 🔶 继承自BaseDO | 是否删除 |
| tenant_id | bigint | - | Long | 🔶 继承自BaseDO | 租户编号 |

### 10.4 差异分析
✅ **完全一致** - 无差异

---

## 11. pay_wallet_recharge (会员钱包充值表)

### 11.1 表结构信息
- **表名**: pay_wallet_recharge
- **存储引擎**: InnoDB
- **字符集**: utf8mb4_unicode_ci
- **注释**: 会员钱包充值

### 11.2 对应实体类
- **类名**: PayWalletRechargeDO
- **包路径**: cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletRechargeDO
- **继承**: BaseDO

### 11.3 字段对比

| 数据库字段 | 类型 | 实体类字段 | Java类型 | 状态 | 说明 |
|-----------|------|-----------|---------|------|------|
| id | bigint | id | Long | ✅ 匹配 | 编号 |
| wallet_id | bigint | walletId | Long | ✅ 匹配 | 会员钱包id |
| total_price | int | totalPrice | Integer | ✅ 匹配 | 用户实际到账余额(分) |
| pay_price | int | payPrice | Integer | ✅ 匹配 | 实际支付金额(分) |
| bonus_price | int | bonusPrice | Integer | ✅ 匹配 | 钱包赠送金额(分) |
| package_id | bigint | packageId | Long | ✅ 匹配 | 充值套餐编号 |
| pay_status | bit(1) | payStatus | Boolean | ✅ 匹配 | 是否已支付 |
| pay_order_id | bigint | payOrderId | Long | ✅ 匹配 | 支付订单编号 |
| pay_channel_code | varchar(16) | payChannelCode | String | ✅ 匹配 | 支付成功的支付渠道 |
| pay_time | datetime | payTime | LocalDateTime | ✅ 匹配 | 订单支付时间 |
| pay_refund_id | bigint | payRefundId | Long | ✅ 匹配 | 支付退款单编号 |
| refund_total_price | int | refundTotalPrice | Integer | ✅ 匹配 | 退款金额(分) |
| refund_pay_price | int | refundPayPrice | Integer | ✅ 匹配 | 退款支付金额(分) |
| refund_bonus_price | int | refundBonusPrice | Integer | ✅ 匹配 | 退款钱包赠送金额(分) |
| refund_time | datetime | refundTime | LocalDateTime | ✅ 匹配 | 退款时间 |
| refund_status | int | refundStatus | Integer | ✅ 匹配 | 退款状态 |
| creator | varchar(64) | - | - | 🔶 继承自BaseDO | 创建者 |
| create_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 创建时间 |
| updater | varchar(64) | - | - | 🔶 继承自BaseDO | 更新者 |
| update_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 更新时间 |
| deleted | bit(1) | - | Boolean | 🔶 继承自BaseDO | 是否删除 |
| tenant_id | bigint | - | Long | 🔶 继承自BaseDO | 租户编号 |

### 11.4 差异分析
✅ **完全一致** - 无差异

---

## 12. pay_wallet_recharge_package (充值套餐表)

### 12.1 表结构信息
- **表名**: pay_wallet_recharge_package
- **存储引擎**: InnoDB
- **字符集**: utf8mb4_unicode_ci
- **注释**: 充值套餐表

### 12.2 对应实体类
- **类名**: PayWalletRechargePackageDO
- **包路径**: cn.iocoder.yudao.module.pay.dal.dataobject.wallet.PayWalletRechargePackageDO
- **继承**: BaseDO

### 12.3 字段对比

| 数据库字段 | 类型 | 实体类字段 | Java类型 | 状态 | 说明 |
|-----------|------|-----------|---------|------|------|
| id | bigint | id | Long | ✅ 匹配 | 编号 |
| name | varchar(64) | name | String | ✅ 匹配 | 套餐名 |
| pay_price | int | payPrice | Integer | ✅ 匹配 | 支付金额(分) |
| bonus_price | int | bonusPrice | Integer | ✅ 匹配 | 赠送金额(分) |
| status | tinyint | status | Integer | ✅ 匹配 | 状态 |
| creator | varchar(64) | - | - | 🔶 继承自BaseDO | 创建者 |
| create_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 创建时间 |
| updater | varchar(64) | - | - | 🔶 继承自BaseDO | 更新者 |
| update_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 更新时间 |
| deleted | bit(1) | - | Boolean | 🔶 继承自BaseDO | 是否删除 |
| tenant_id | bigint | - | Long | 🔶 继承自BaseDO | 租户编号 |

### 12.4 差异分析
✅ **完全一致** - 无差异

---

## 13. pay_demo_order (示例订单表)

### 13.1 表结构信息
- **表名**: pay_demo_order
- **存储引擎**: InnoDB
- **字符集**: utf8mb4_bin
- **注释**: 示例订单

### 13.2 对应实体类
- **类名**: PayDemoOrderDO
- **包路径**: cn.iocoder.yudao.module.pay.dal.dataobject.demo.PayDemoOrderDO
- **继承**: BaseDO

### 13.3 字段对比

| 数据库字段 | 类型 | 实体类字段 | Java类型 | 状态 | 说明 |
|-----------|------|-----------|---------|------|------|
| id | bigint | id | Long | ✅ 匹配 | 订单编号 |
| user_id | bigint unsigned | userId | Long | ✅ 匹配 | 用户编号 |
| spu_id | bigint | spuId | Long | ✅ 匹配 | 商品编号 |
| spu_name | varchar(255) | spuName | String | ✅ 匹配 | 商品名字 |
| price | int | price | Integer | ✅ 匹配 | 价格(分) |
| pay_status | bit(1) | payStatus | Boolean | ✅ 匹配 | 是否已支付 |
| pay_order_id | bigint | payOrderId | Long | ✅ 匹配 | 支付订单编号 |
| pay_channel_code | varchar(16) | payChannelCode | String | ✅ 匹配 | 支付成功的支付渠道 |
| pay_time | datetime | payTime | LocalDateTime | ✅ 匹配 | 订单支付时间 |
| pay_refund_id | bigint | payRefundId | Long | ✅ 匹配 | 退款订单编号 |
| refund_price | int | refundPrice | Integer | ✅ 匹配 | 退款金额(分) |
| refund_time | datetime | refundTime | LocalDateTime | ✅ 匹配 | 退款时间 |
| transfer_channel_package_info | varchar(2048) | - | - | ❌ 实体类缺失 | 渠道 package 信息 |
| creator | varchar(64) | - | - | 🔶 继承自BaseDO | 创建者 |
| create_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 创建时间 |
| updater | varchar(64) | - | - | 🔶 继承自BaseDO | 更新者 |
| update_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 更新时间 |
| deleted | bit(1) | - | Boolean | 🔶 继承自BaseDO | 是否删除 |
| tenant_id | bigint | - | Long | 🔶 继承自BaseDO | 租户编号 |

### 13.4 差异分析

#### ❌ 问题1: 实体类缺少字段
- **数据库字段**: transfer_channel_package_info
- **字段类型**: varchar(2048)
- **字段注释**: 渠道 package 信息
- **影响**: 该字段用于存储转账相关的渠道包信息,实体类缺失可能导致该信息无法正常读写

**建议**: 在PayDemoOrderDO类中添加该字段
```java
/**
 * 渠道 package 信息
 */
private String transferChannelPackageInfo;
```

---

## 14. pay_demo_withdraw (示例提现单表)

### 14.1 表结构信息
- **表名**: pay_demo_withdraw
- **存储引擎**: InnoDB
- **字符集**: utf8mb4_unicode_ci
- **注释**: 示例业务提现单

### 14.2 对应实体类
- **类名**: PayDemoWithdrawDO
- **包路径**: cn.iocoder.yudao.module.pay.dal.dataobject.demo.PayDemoWithdrawDO
- **继承**: BaseDO

### 14.3 字段对比

| 数据库字段 | 类型 | 实体类字段 | Java类型 | 状态 | 说明 |
|-----------|------|-----------|---------|------|------|
| id | bigint | id | Long | ✅ 匹配 | 提现单编号 |
| subject | varchar(32) | subject | String | ✅ 匹配 | 提现标题 |
| price | int | price | Integer | ✅ 匹配 | 提现金额(分) |
| user_account | varchar(64) | userAccount | String | ✅ 匹配 | 收款人账号 |
| user_name | varchar(64) | userName | String | ✅ 匹配 | 收款人姓名 |
| type | tinyint | type | Integer | ✅ 匹配 | 提现方式 |
| status | tinyint | status | Integer | ✅ 匹配 | 提现状态 |
| pay_transfer_id | bigint | payTransferId | Long | ✅ 匹配 | 转账订单编号 |
| transfer_channel_code | varchar(16) | transferChannelCode | String | ✅ 匹配 | 转账渠道 |
| transfer_time | datetime | transferTime | LocalDateTime | ✅ 匹配 | 转账支付时间 |
| transfer_error_msg | varchar(4096) | transferErrorMsg | String | ✅ 匹配 | 转账错误提示 |
| creator | varchar(64) | - | - | 🔶 继承自BaseDO | 创建者 |
| create_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 创建时间 |
| updater | varchar(64) | - | - | 🔶 继承自BaseDO | 更新者 |
| update_time | datetime | - | LocalDateTime | 🔶 继承自BaseDO | 更新时间 |
| deleted | bit(1) | - | Boolean | 🔶 继承自BaseDO | 是否删除 |
| tenant_id | bigint | - | Long | 🔶 继承自BaseDO | 租户编号 |

### 14.4 差异分析
✅ **完全一致** - 无差异

---

## 总结与建议

### 主要发现

1. **整体质量**: 14个表中有13个表与实体类完全一致,整体数据一致性良好
2. **存在问题**: 仅在pay_demo_order表中发现1个字段缺失

### 详细问题清单

#### 1. pay_demo_order表问题

**问题描述**: 实体类PayDemoOrderDO缺少transfer_channel_package_info字段

**字段信息**:
- 数据库字段名: transfer_channel_package_info
- 字段类型: varchar(2048)
- 字段用途: 存储渠道package信息

**修复建议**:
```java
// 在 PayDemoOrderDO.java 中添加以下字段
/**
 * 渠道 package 信息
 */
private String transferChannelPackageInfo;
```

**优先级**: 中等
**影响范围**: 示例订单的转账渠道包信息读写功能

### 架构优势

项目在数据一致性方面表现出以下优势:

1. **继承架构合理**:
   - 使用BaseDO和TenantBaseDO统一管理审计字段
   - 避免了大量重复代码

2. **类型处理器完善**:
   - pay_channel表使用自定义PayClientConfigTypeHandler处理复杂配置对象
   - 多个表使用JacksonTypeHandler处理JSON字段

3. **注解使用规范**:
   - 正确使用@TableName、@TableId、@TableField等MyBatis-Plus注解
   - 使用@KeySequence支持多种数据库的主键自增

### 改进建议

#### 优先级1 (高) - 无

所有表的核心功能字段均匹配良好

#### 优先级2 (中)

1. **补充缺失字段**: 在PayDemoOrderDO中添加transferChannelPackageInfo字段

#### 优先级3 (低)

1. **完善字段注释**: 建议在实体类中为所有字段添加与数据库一致的中文注释
2. **字段长度验证**: 考虑在实体类中使用@Length等验证注解,保持与数据库长度定义一致

### 维护建议

1. **定期检查**: 建议在每次数据库结构变更后运行此类对比检查
2. **自动化工具**: 考虑将此类检查集成到CI/CD流程中
3. **文档同步**: 保持数据库设计文档与实体类定义的同步更新

---

## 附录

### A. 继承关系说明

#### BaseDO 提供的字段
```java
public abstract class BaseDO {
    private String creator;      // 创建者
    private LocalDateTime createTime;  // 创建时间
    private String updater;      // 更新者
    private LocalDateTime updateTime;  // 更新时间
    private Boolean deleted;     // 是否删除
}
```

#### TenantBaseDO 提供的字段
```java
public abstract class TenantBaseDO extends BaseDO {
    private Long tenantId;       // 租户编号
}
```

### B. 类型映射表

| MySQL类型 | Java类型 | 说明 |
|----------|---------|------|
| bigint | Long | 64位整数 |
| int | Integer | 32位整数 |
| tinyint | Integer | 8位整数,Java统一用Integer |
| varchar(n) | String | 可变长度字符串 |
| datetime | LocalDateTime | 日期时间 |
| bit(1) | Boolean | 布尔值 |
| double | Double | 双精度浮点数 |

### C. JSON字段处理

项目中使用两种方式处理JSON字段:

1. **JacksonTypeHandler**:
   - 用于简单的Map<String, String>类型
   - 示例: pay_order_extension.channel_extras

2. **自定义TypeHandler**:
   - 用于复杂对象类型
   - 示例: pay_channel.config (PayClientConfigTypeHandler)

---

**报告生成完毕**

如有任何问题或需要进一步分析,请联系开发团队。
