# Jeepay 支付网关集成

> Jeepay = 独立的支付网关系统（不是SDK），你系统只通过 HTTP API 调用它。

## 📥 下载 & 启动

```bash
# 后端（支付网关服务）
git clone https://github.com/deepveloce-dot/jeepay.git jeepay/server

# 前端管理台（配置商户/应用）
git clone https://github.com/deepveloce-dot/jeepay-ui.git jeepay/ui

# 启动网关服务（默认端口 9000）
cd jeepay/server
mvn spring-boot:run
```

---

## 🔧 你系统配置（application.yml）

```yaml
deepay:
  payment:
    provider: jeepay          # 激活 JeepayPlugin（改为 mock 用于本地测试）
  jeepay:
    api-url:    http://localhost:9000     # Jeepay 服务地址
    mch-no:     M000001                  # 商户号（Jeepay管理台创建）
    app-id:     YOUR_APP_ID              # 应用ID（Jeepay管理台创建）
    api-key:    YOUR_API_KEY             # 签名密钥
    way-code:   AUTO                     # AUTO=自动选支付方式
    currency:   EUR                      # 欧元（全系统统一）
    notify-url: https://your-domain.com/deepay/callback/payment
```

---

## 🏗️ 系统架构

```
你的系统（Deepay）
   └─ OrderFlowAgent
        └─ PaymentService（统一入口，src/service/PaymentServiceV2.java）
             └─ PaymentPluginManager
                  └─ JeepayPlugin  ← 调用 Jeepay HTTP API
                       └─ Jeepay 支付网关（独立进程，端口 9000）
                            ├─ 微信支付
                            ├─ 支付宝
                            └─ 欧元跨境支付
```

---

## 💳 支付流程

```
1. OrderFlowAgent.run(ctx)
   → paymentService.create(outTradeNo="PAY-ABC123", amount=€19.99, ...)
   → JeepayPlugin.createOrder(req)
   → POST http://jeepay:9000/api/pay/unifiedOrder
   → 返回 payUrl（二维码/H5链接）

2. 用户扫码支付

3. Jeepay 异步回调
   → POST /deepay/callback/payment
   → { outTradeNo:"PAY-ABC123", amount:1999, state:2, sign:"xxxx" }

4. DeepayPaymentCallbackController
   → verifySign（MD5验签）
   → PaymentCallbackService.handle()
      → 幂等检查（order.status == PAID → skip）
      → 金额校验（1999分 = €19.99）
      → 原子更新 order.status=PAID
      → InventoryAgent.onPaid()（扣库存）
      → AIDecisionAgent（BOOST/STOP/REDESIGN）
      → AnalyticsAgent（数据回流）
```

---

## 🔐 Jeepay 签名算法

```java
// ASCII升序排列参数 → key=value& → 末尾加 &key={apiKey} → MD5大写
// 见 JeepayPlugin.sign(Map, String)
```

---

## 💶 货币说明（EUR 欧元）

| 你系统      | Jeepay API   | 示例         |
|------------|--------------|--------------|
| BigDecimal  | Long (分)    | €19.99 → 1999 |
| 19.99      | 1999         |              |

> ⚠️ 禁止使用 `double` / `float`，全部 `BigDecimal`

---

## 📊 Jeepay 回调状态映射

| Jeepay state | 你的系统状态 |
|-------------|------------|
| 1           | WAIT       |
| 2           | PAID ✅    |
| 3           | FAIL       |

---

## 🆔 ID 映射（最关键）

```
你的系统               Jeepay字段
payment_id         →  outTradeNo   ← 全局唯一，绑定你的ID
price (€)          →  amount (分) × 100
```

---

## ⚠️ 已处理的坑

| 坑                   | 处理方式                               |
|---------------------|---------------------------------------|
| 回调会重复发         | 幂等：`markPaid WHERE status='PENDING'` |
| 回调可能延迟         | 不依赖同步，异步回调驱动               |
| 金额不可信           | `PaymentCallbackService` 校验订单金额 |
| 字段可能缺           | 全部 `getOrDefault` 容错              |
| 并发重复支付         | DB 原子 SQL `WHERE status='PENDING'`  |

---

## 🔮 未来扩展

切换支付渠道只需改一行配置：

```yaml
# Stripe（欧美信用卡）
deepay.payment.provider: stripe

# PayPal
deepay.payment.provider: paypal
```

新增 `StripePlugin implements PaymentPlugin`，其他代码不变。
