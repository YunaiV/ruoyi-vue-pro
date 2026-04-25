# Deepay AI Copilot MVP — 使用文档

## 一、功能概览

本次 PR 实现了 **SaaS 全站 AI 交互（30天MVP）**，包含：

| 功能 | 状态 | 说明 |
|------|------|------|
| 全站 AI Copilot UI（全局悬浮入口） | ✅ | `GlobalAiCopilot.vue` 挂载到根布局 |
| 页面切换不丢会话（sessionId 持久） | ✅ | localStorage 持久化，全局 persistKey |
| SSE 流式打字机效果 | ✅ | `EventSource` + 逐字推送 |
| 上下文注入 v1（route/entityType/entityId/snapshot） | ✅ | 前端自动收集，后端注入 prompt |
| Persona 配置迁到 DB（可运营） | ✅ | `deepay_ai_persona` 表 + CRUD 接口 |
| 多租户 Persona 优先级（tenantId+module → 全局 → 代码） | ✅ | `AiPersonaService` |
| 基础配额/限流（每分钟 + 每日上限） | ✅ | Redis 原子计数 + MySQL 落库 |
| 长久记忆（按板块+字段白名单） | ✅ | `deepay_ai_memory_item` 表 |
| 用量记录（DB 持久化） | ✅ | `deepay_ai_usage` 表 |
| MongoDB 高可用方案（rs0 三节点） | ✅ | `docker-compose.mongodb-rs.yml` |
| 合规删除（按 customerId 删除全部记忆） | ✅ | `DELETE /deepay/memory/clear` |
| 单元测试（限流/Persona/记忆服务层） | ✅ | 3个测试类，共35+用例 |

---

## 二、快速开始

### 1. 运行 SQL 迁移

```sql
-- 执行新增表脚本
source yudao-module-deepay/src/main/resources/sql/deepay_ai_copilot_v2.sql
```

创建的表：
- `deepay_ai_persona` — AI 角色人设（可运营 Prompt 配置）
- `deepay_ai_usage`   — 每日用量记录（限流 + 运营分析）
- `deepay_ai_memory_item` — 长久记忆条目（365天TTL）

### 2. 启用全站 AI Copilot（前端）

在根布局文件中加入 `<GlobalAiCopilot />`：

```vue
<!-- 例如在 BasicLayout.vue 或 App.vue 的根节点 -->
<template>
  <div class="app-wrapper">
    <router-view />
    <!-- 全站 AI Copilot（会自动检测当前路由并注入上下文）-->
    <GlobalAiCopilot />
  </div>
</template>

<script setup>
import GlobalAiCopilot from '@/components/AiChat/GlobalAiCopilot.vue'
</script>
```

**效果**：
- 任意页面右下角出现 AI 悬浮按钮
- 点击打开 AI 聊天抽屉
- 切换页面后 sessionId 持续（不重置对话）
- 自动读取当前路由注入 module/entityType/entityId 上下文

---

## 三、Persona 配置（可运营）

### 通过管理接口配置

```bash
# 查看所有 persona
GET /deepay/persona/list

# 预览某板块的 system prompt（含优先级渲染）
GET /deepay/persona/preview?module=sales&tenantId=0

# 新增 persona（租户 123 的 design 专属配置）
POST /deepay/persona/create
{
  "tenantId": 123,
  "module": "design",
  "roleName": "高端设计师",
  "systemPrompt": "你是一位专注于高端定制服装的设计师...",
  "toolWhitelist": "designTool,trendTool",
  "enabled": 1
}

# 更新 persona
PUT /deepay/persona/update
{ "id": 1, "systemPrompt": "更新后的 prompt..." }

# 删除 persona（软删除）
DELETE /deepay/persona/1
```

### 优先级说明

1. `tenantId=当前租户 + module` → 租户自定义
2. `tenantId=0 + module` → 全局默认（DB 配置）
3. 代码硬编码默认值 → 最终兜底

> 修改 Persona 后**无需重启**，下次请求即生效。

---

## 四、SSE 调试

### 使用 curl 测试

```bash
# 基础对话
curl -N "http://localhost:48080/deepay/chat/stream?module=selection&userMessage=我想选外套"

# 带上下文注入
curl -N "http://localhost:48080/deepay/chat/stream?module=order&userMessage=这个订单怎么了&entityType=order&entityId=PAY-ABC123&snapshot={\"status\":\"PENDING\"}"
```

### 前端 EventSource 示例

```js
const url = `/deepay/chat/stream?module=design&userMessage=帮我设计一款外套&entityType=product&entityId=CHAIN-XYZ`
const es = new EventSource(url)

es.addEventListener('token', e => console.log('[token]', e.data))
es.addEventListener('meta',  e => console.log('[meta]', JSON.parse(e.data)))
es.addEventListener('done',  e => es.close())
es.addEventListener('error', e => {
  console.error('[error]', e.data)
  es.close()
})

es.onerror = () => {
  console.warn('SSE 连接断开，准备重连...')
  es.close()
  // 延迟 2 秒重连
  setTimeout(() => reconnect(), 2000)
}
```

### SSE 事件说明

| 事件名 | 描述 | 数据格式 |
|--------|------|----------|
| `token` | 流式 token（打字机效果） | 纯文本 |
| `meta`  | 会话元数据（pendingField/quickReplies/images/done/sessionId） | JSON |
| `done`  | 流结束信号 | 空字符串 |
| `error` | 错误（含限流提示） | 纯文本错误消息 |

---

## 五、限流说明

### 限制规则

| 用户类型 | 每分钟上限 | 每日上限 |
|----------|-----------|---------|
| 登录用户 | 10 次 | 200 次 |
| 匿名用户 | 3 次 | 5 次 |

### 查询剩余配额

```bash
GET /deepay/persona/quota?userId=123&tenantId=0
```

响应：
```json
{
  "dailyLimit":  200,
  "dailyUsed":   15,
  "dailyRemain": 185,
  "minuteLimit": 10
}
```

### 超限响应

超限时接口返回 HTTP 429：
```json
{
  "code": 429,
  "msg": "今日 AI 调用次数已达上限（200 次/天），明日 0 点重置"
}
```

SSE 接口超限时推送 `error` 事件后关闭连接。

---

## 六、长久记忆

### 记忆字段白名单（按板块）

| 板块 | 允许记忆的字段 |
|------|----------------|
| design | preferredStyle, favoriteColor, budgetRange, fabric, dislikeReasons, targetScene, sizePreference |
| sales | motivation, objections, decisionStyle, priceRange, communicationStyle |
| finance | paymentMethod, preferredChain, invoicePreference, riskTolerance |
| order | deliveryPreference, aftersaleHistory, preferredCourier |
| selection | category, crowd, style, market, priceLevel |

### API

```bash
# 查询用户记忆
GET /deepay/memory/list?customerId=cust001&module=design

# 手动写入记忆
POST /deepay/memory/save
{
  "customerId": "cust001",
  "module": "design",
  "memoryType": "profile",
  "memKey": "preferredStyle",
  "memValue": "极简",
  "confidence": 0.9
}

# 合规删除（GDPR）
DELETE /deepay/memory/clear?customerId=cust001

# 查看白名单
GET /deepay/memory/allowed-keys
```

### 记忆注入效果

AI 在回复时会自动感知用户偏好：
> "根据你之前的偏好（极简风格、预算 300-600 元），我为你推荐了以下设计方案..."

---

## 七、MongoDB 高可用（rs0 三节点）

### 启动

```bash
# 1. 生成 keyfile（仅第一次）
openssl rand -base64 756 > script/mongo-keyfile
chmod 400 script/mongo-keyfile

# 2. 启动 3 节点 + 自动初始化
docker-compose -f docker-compose.mongodb-rs.yml up -d

# 3. 验证同步状态（约 30 秒后）
docker exec deepay-mongo1 mongosh -u deepay -p deepay123 --authenticationDatabase admin --eval "rs.status()"
```

### Spring Boot 连接串

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://deepay:deepay123@deepay-mongo1:27017,deepay-mongo2:27017,deepay-mongo3:27017/ruoyi_ai?replicaSet=rs0&authSource=admin
```

---

## 八、上下文注入格式（前端 → 后端）

### SSE 参数

```
GET /deepay/chat/stream
  ?module=order          ← 板块
  &userMessage=...
  &sessionId=abc123      ← 会话 ID（首次不传）
  &customerId=1          ← 用户 ID
  &tenantId=0            ← 租户 ID
  &route=/deepay/order   ← 当前路由
  &entityType=order      ← 实体类型
  &entityId=PAY-XXX      ← 实体 ID
  &snapshot={...}        ← 页面数据快照（JSON）
```

### REST 请求体

```json
{
  "module": "order",
  "sessionId": "abc123",
  "customerId": 1,
  "tenantId": 0,
  "userMessage": "这个订单什么时候发货？",
  "route": "/deepay/order/detail/123",
  "entityType": "order",
  "entityId": "PAY-ABC123",
  "snapshot": "{\"status\":\"PENDING\",\"amount\":299}"
}
```

支持的 `entityType` 值：`order` / `product` / `customer` / `paymentLink` / `design`

---

## 九、模块 Module 对应关系

| Module | 说明 | AI 角色 |
|--------|------|---------|
| selection | 选款/购物 | 购物顾问 🛍️ |
| design | 设计出图 | AI 设计师 🎨 |
| product | 商品管理 | 产品经理 📦 |
| inventory | 库存管理 | 库存专员 🏭 |
| finance | 财务/收款 | 财务总监 💼 |
| trend | 趋势分析 | 趋势分析师 📈 |
| order | 订单/售后 | 客服专员 🎧 |
| sales | 销售成交 | 销售顾问 💪 |
