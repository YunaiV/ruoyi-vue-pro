# Deepay AI 智能层 — 三大能力补充

本文档说明以下三大 AI 能力的实现方案：

---

## PR1：聊天记忆（Chat Memory）

### 功能
- **会话持久化**：每次对话的会话元信息存入 MongoDB `ai_chat_session`
- **消息落库**：用户发出消息立即落库，AI 完成后合并落 1 条最终 assistant message（SSE 流式过程中不逐 token 落库）
- **自动摘要**：每 6 轮对话异步更新 `ai_chat_session.summary`（可配置）
- **结构化长久记忆**：抽取允许字段按 module 写入 `ai_memory_item`
- **365 天 TTL**：`ai_chat_session` 与 `ai_chat_message` 均有 TTL index（365 天）
- **合规删除**：按 `customerId` 全量删除所有聊天与记忆

### 数据结构

| 集合 | 主要字段 | TTL |
|------|---------|-----|
| `ai_chat_session` | tenantId, customerId, module, summary, messageCount, memoryEnabled, lastActiveAt | 365 天（lastActiveAt） |
| `ai_chat_message` | sessionId, tenantId, customerId, module, role, content, createdAt | 365 天（createdAt） |
| `ai_memory_item` | tenantId, customerId, module, facts（Map），createdAt, updatedAt | 无（用户可删除） |

### 记忆字段规则（按 module）

| module | 允许字段 |
|--------|---------|
| design | stylePreference, sizePreference, taboos, colorPreference, fabricPreference |
| sales | buyingMotivation, objections, communicationStyle, decisionSpeed, keyInfluencers |
| finance | paymentPreference, budgetRange, invoiceRequired, preferredCurrency |
| order | shippingPreference, deliveryNotes, returnPolicy, packagingPreference |

### 接口

```
GET  /deepay/memory/sessions          # 查询会话列表
DEL  /deepay/memory/sessions/{id}     # 删除会话
PUT  /deepay/memory/sessions/{id}/memory-switch  # 开关记忆
GET  /deepay/memory/messages          # 查询消息
GET  /deepay/memory/items             # 查询记忆
DEL  /deepay/memory/items             # 删除某 module 记忆
POST /deepay/memory/items/upsert      # 更新记忆
DEL  /deepay/memory/compliance/delete-all  # 合规删除全部
```

---

## PR2：Tool Calling / 动作执行平台

### 架构
- `AiTool` 接口：name / description / paramsSchema / riskLevel / execute()
- `AiToolRegistry`：所有实现 `AiTool` 的 Spring Bean 自动注册
- `AiToolExecutor`：统一调用入口，HIGH 风险先创建 `PendingAction` 等待确认

### 风险等级

| 等级 | 行为 |
|-----|-----|
| LOW | 只读，直接执行 |
| MEDIUM | 写入，直接执行 + 记录审计 |
| HIGH | 先创建 `ai_pending_action`，前端确认后再执行 |

### 首批 10 个工具

| 工具 | 描述 | 风险 |
|-----|-----|-----|
| searchProducts | 搜索商品列表 | LOW |
| getProductDetail | 获取商品详情 | LOW |
| createOrderDraft | 创建订单草稿 | MEDIUM |
| updateOrderField | 更新订单字段 | HIGH |
| createPaymentLink | 生成收款链接草稿 | MEDIUM |
| queryPaymentStatus | 查询支付状态 | LOW |
| createCustomer | 创建客户档案 | MEDIUM |
| addCustomerNote | 添加客户备注 | LOW |
| generateQuoteDraft | 生成报价单草稿 | MEDIUM |
| sendNotificationDraft | 发送通知草稿 | MEDIUM |

### 接口

```
GET  /deepay/tools/list               # 工具列表
POST /deepay/tools/call               # 调用工具
POST /deepay/tools/confirm/{actionId} # 确认高风险动作
POST /deepay/tools/cancel/{actionId}  # 取消动作
GET  /deepay/tools/audit              # 审计日志
```

### 审计日志（`ai_tool_audit_log`）

记录：tenantId / customerId / module / sessionId / operator / toolName / riskLevel / params / result / error / status / latencyMs

---

## PR3：Model Gateway（多模型网关）

### 功能
- **统一入口**：`ModelGatewayService.chat(GatewayRequest)` 统一调用
- **多 Provider**：支持 deepseek / moonshot / openai / zhipu（OpenAI 兼容格式）
- **自动 Fallback**：主模型失败（含重试）自动切换备用模型
- **用量统计**：每次调用记录 tokensIn/tokensOut/latencyMs/成本估算（USD）到 `ai_model_usage`

### 成本估算（参考价，每 1M token）

| 模型 | 输入 | 输出 |
|-----|------|-----|
| deepseek-chat | $0.14 | $0.28 |
| moonshot-v1-8k | $1.60 | $1.60 |
| gpt-4o-mini | $0.15 | $0.60 |
| gpt-4o | $5.00 | $15.00 |

### 配置（application.yml）

```yaml
deepay:
  gateway:
    primary-model:   deepseek-chat
    primary-api-url: https://api.deepseek.com/v1/chat/completions
    primary-api-key: ""      # 填入真实 API Key（生产环境通过环境变量注入）
    fallback-model:   moonshot-v1-8k
    fallback-api-url: https://api.moonshot.cn/v1/chat/completions
    fallback-api-key: ""     # 填入真实 API Key（生产环境通过环境变量注入）
    timeout-seconds: 30
    max-retries:     2
```

### MongoDB 配置

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/deepay
      auto-index-creation: true  # 自动创建 TTL 索引
```

### 接口

```
POST /deepay/gateway/chat             # 统一 chat 入口
GET  /deepay/gateway/usage            # 租户用量统计
GET  /deepay/gateway/usage/customer   # 客户用量统计
```

---

## 安装 / 配置步骤

1. 安装 MongoDB 5.0+（宝塔面板：软件商店 → MongoDB）
2. 创建数据库：`deepay`
3. 在 `application-prod.yml` 填入 MongoDB URI 和模型 API Key
4. 启动服务后，Spring Data MongoDB 会自动创建集合与 TTL 索引

## tenantId / customerId / module 全链路贯穿

所有 Mongo 文档均包含：
- `tenantId`：租户隔离
- `customerId`：客户隔离
- `module`：板块隔离（selection/design/product/inventory/finance/trend/order）

索引均以 `tenantId` 打头，确保多租户查询性能。
