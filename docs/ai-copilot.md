# AI Copilot MVP — 开发与运营指南

本文档描述 **30 天 AI Copilot MVP** 的四个核心能力的启用方式、配置指南与调试技巧。

---

## 目录
1. [全站 AI Copilot UI（统一入口）](#1-全站-ai-copilot-ui)
2. [上下文注入 v1](#2-上下文注入-v1)
3. [Prompt/Persona 配置（可运营）](#3-promptpersona-配置)
4. [基础配额/限流](#4-基础配额限流)
5. [SSE 调试指南](#5-sse-调试指南)
6. [数据库迁移](#6-数据库迁移)

---

## 1. 全站 AI Copilot UI

### 组件位置
```
yudao-ui/yudao-ui-admin-vue3/src/components/AiChat/AiGlobalCopilot.vue
```

### 在布局中启用

在你的根布局组件（如 `App.vue` 或主框架组件）中加入：

```vue
<template>
  <div id="app">
    <!-- ... 其他布局 ... -->
    <AiGlobalCopilot />
  </div>
</template>

<script setup lang="ts">
import AiGlobalCopilot from '@/components/AiChat/AiGlobalCopilot.vue'
</script>
```

### 效果
- 右下角显示 `✨` 悬浮按钮
- 点击展开右侧抽屉式聊天面板
- 顶部有 7 个模块快切标签（全站/选款/设计/商品/订单/趋势/财务）
- **页面切换后 session 不丢失**（保存在 `localStorage key = ai_copilot_global`）
- 路由变化时自动推断当前模块（如路径含 `/order` 自动切换到"订单"标签）

### Session 持久化
- sessionId 存储在 `localStorage.ai_copilot_global`
- 刷新页面后自动恢复同一 session，AI 记忆不中断
- 点击"🗑️"按钮可清除当前 session 并开启新会话

---

## 2. 上下文注入 v1

### 工作原理
前端每次发消息时，自动附带 `context` JSON，后端将其注入到 AI 的 system prompt 中：

```json
{
  "route": "/order/detail/123",
  "module": "order",
  "entityType": "order",
  "entityId": "123",
  "snapshot": "{\"status\":\"PAID\",\"amount\":99.00,\"currency\":\"EUR\"}"
}
```

### 前端配置

**方式 1：全局 Copilot（自动注入路由，无需配置）**

`AiGlobalCopilot.vue` 内置 `buildContext()` 函数，自动注入当前路由路径和模块。

**方式 2：页面级 Drawer（注入实体上下文）**

在页面中使用 `useAiChat` 时传入 `contextFn`：

```ts
import { useRoute } from 'vue-router'
import { useAiChat } from '@/composables/useAiChat'

const route = useRoute()
const orderId = ref('123')

const chat = useAiChat({
  module: 'order',
  contextFn: () => ({
    route: route.path,
    module: 'order',
    entityType: 'order',
    entityId: orderId.value,
    snapshot: JSON.stringify({
      status: orderDetail.value?.status,
      amount: orderDetail.value?.amount,
    }),
  }),
})
```

### 后端上下文注入逻辑

后端 `AiChatService.buildContextPrefix()` 将 context 转为可读的 prompt 前缀：

```
当前路由：/order/detail/123
实体类型：order
实体ID：123
页面数据快照：{"status":"PAID","amount":99.00}
```

此前缀会拼接在 system prompt 后，让 AI 理解当前页面状态，无需用户重复说明。

### 支持的实体类型（entityType）
| entityType | 说明 |
|---|---|
| `order` | 订单 |
| `product` | 商品 |
| `customer` | 客户 |
| `paymentLink` | 收款链接 |
| `inventory` | 库存 |
| `design` | 设计链 |

---

## 3. Prompt/Persona 配置

### 数据库表
```sql
-- ai_persona 表（详见 sql/mysql/ai-copilot-mvp.sql）
CREATE TABLE ai_persona (
  id            BIGINT AUTO_INCREMENT,
  tenant_id     BIGINT DEFAULT 0,     -- 0 = 全局默认
  module        VARCHAR(64),          -- selection/design/order 等
  role_name     VARCHAR(128),         -- 角色显示名
  system_prompt TEXT,                 -- System Prompt
  enabled       TINYINT DEFAULT 1,
  ...
)
```

### 读取策略（三层降级）
1. **租户级** `tenant_id = 当前租户 AND module = 目标模块`
2. **全局默认** `tenant_id = 0 AND module = 目标模块`
3. **硬编码兜底** `AiPersonaService.FALLBACK_PROMPTS`

### 管理 API

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/deepay/ai/persona` | 获取所有启用的 persona 列表 |
| GET | `/deepay/ai/persona/{id}` | 获取单个 persona |
| POST | `/deepay/ai/persona` | 新增 persona |
| PUT | `/deepay/ai/persona/{id}` | 更新 persona |
| DELETE | `/deepay/ai/persona/{id}` | 删除 persona |

### 示例：修改"订单"模块的 AI 语气

```bash
# 更新 id=7 的 persona
curl -X PUT http://localhost:8080/deepay/ai/persona/7 \
  -H "Content-Type: application/json" \
  -d '{
    "systemPrompt": "你是一位资深的电商订单专家，处理问题极度专业且高效。始终用简洁有力的语气回复，直接给出解决方案，不废话。",
    "roleName": "订单专家"
  }'
```

修改后**立即生效**（无需重启，服务每次调用都从 DB 读取最新配置）。

### 多租户支持（预留）
当系统启用多租户时，只需在调用 `AiPersonaService.getSystemPrompt(tenantId, module)` 时传入实际租户 ID。所有表结构已预留 `tenant_id` 字段。

---

## 4. 基础配额/限流

### 限流规则
| 维度 | 规则 | 实现 |
|---|---|---|
| 用户级（每分钟） | 默认 **3 次/分钟** | `DeepayRateLimitService`（Redis） |
| 用量日志 | 每次调用写入 `ai_usage_log` 表 | `AiUsageLogService`（异步） |

### 修改限流阈值

在 `DeepayRateLimitService.java` 中修改常量：
```java
public static final int MAX_PER_MINUTE = 3;  // 改为你需要的值
```

或者通过配置文件（建议后续改为可配置）。

### 限流响应
触发限流时，SSE 推送 `error` 事件：
```
event: error
data: 请求过于频繁，请稍后再试（每分钟最多 3 次）
```
前端 `useAiChat.ts` 已处理此错误，显示在最后一条 AI 消息气泡中。

### 查询用量
```sql
-- 查询某用户今日调用次数
SELECT COUNT(*) FROM ai_usage_log
WHERE user_id = '123' AND status = 'OK'
  AND created_at >= CURDATE();

-- 查询各模块今日用量分布
SELECT module, status, COUNT(*) as cnt
FROM ai_usage_log
WHERE created_at >= CURDATE()
GROUP BY module, status
ORDER BY cnt DESC;
```

---

## 5. SSE 调试指南

### 命令行调试
```bash
# 基础测试（无上下文）
curl -N -H "Accept: text/event-stream" \
  "http://localhost:8080/deepay/chat/stream?module=selection&userMessage=我想做外套"

# 带上下文注入
curl -N -H "Accept: text/event-stream" \
  'http://localhost:8080/deepay/chat/stream?module=order&userMessage=这个订单什么时候发货&contextJson=%7B%22route%22%3A%22%2Forder%2Fdetail%22%2C%22entityType%22%3A%22order%22%2C%22entityId%22%3A%22123%22%7D'
```

### 浏览器 DevTools 调试
1. 打开 Network 面板
2. 发送一条消息
3. 找到 `stream` 请求（类型 `eventsource`）
4. 点击 "EventStream" 标签，实时查看推送的 token/meta/done 事件

### SSE 事件格式
```
event: token
data: 你好

event: token
data: ！我来

event: meta
data: {"sessionId":"abc123","quickReplies":["外套","裤子"],"done":true}

event: done
data:
```

### 断线重连
- 前端 `EventSource` 在网络断开后会自动重连（浏览器内置行为）
- 后端 `SseEmitter` 超时设置为 **60 秒**，每条回复不会超过此时间
- 如需主动取消，调用 `chat.cancel()` 或点击停止按钮

### 常见问题

| 问题 | 原因 | 解决 |
|---|---|---|
| SSE 无法连接 | 代理配置不支持长连接 | Nginx 添加 `proxy_buffering off; proxy_read_timeout 120s;` |
| 收到 `error` 事件 | 限流/服务异常 | 检查 `DeepayRateLimitService` 配置或后端日志 |
| 打字机效果太慢 | `TOKEN_DELAY_MS` 太大 | 修改 `AiChatStreamService.TOKEN_DELAY_MS`（建议 20~50ms）|
| 切换页面后会话丢失 | persistKey 未配置 | 确保 `AiGlobalCopilot.vue` 已挂载到根组件 |

---

## 6. 数据库迁移

### 执行新建表（MySQL）
```bash
mysql -u root -p your_database < sql/mysql/ai-copilot-mvp.sql
```

### 新增表说明
| 表名 | 说明 |
|---|---|
| `ai_persona` | AI 角色人设配置（支持按租户+模块覆盖 prompt） |
| `ai_usage_log` | AI 调用用量日志（防滥用 + 计费依据） |

### 回滚
```sql
DROP TABLE IF EXISTS ai_persona;
DROP TABLE IF EXISTS ai_usage_log;
```

---

## 开发规范

1. **新增 module** 时需在以下位置同步：
   - `AiPersonaService.FALLBACK_PROMPTS`（硬编码兜底）
   - `sql/mysql/ai-copilot-mvp.sql` INSERT 默认 persona
   - 前端 `AiGlobalCopilot.vue` 的 `MODULE_TABS`

2. **Persona 版本管理**：建议在 `system_prompt` 末尾添加版本注释，如 `// v2 2026-04-25`

3. **租户隔离**：所有新 AI 相关表都已预留 `tenant_id` 字段，当前默认为 `0`
