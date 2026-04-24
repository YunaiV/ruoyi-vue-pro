# Deepay 一衣一链系统 —— MVP 第一阶段开发任务文档

> **⚡ 进入仓库的开发者请先读这里**
> 本文件是 Deepay 一衣一链系统第一阶段（MVP）的完整任务说明与实现指南。
> 代码位于 `yudao-module-deepay` 模块，接口为 `POST /api/create-product`。

---

## 一、目标（必须理解）

实现核心最小闭环（MVP）：

> **输入一句话 → 自动生成设计 → 自动生成商品 → 自动生成支付 → 返回可售卖链接**

---

## 二、技术基础

- 项目基础：deepay-Vue-Pro（Spring Boot 后端）
- 不新建项目，在现有仓库的 `yudao-module-deepay` 模块内开发
- 所有流程**同步执行**（不引入异步、消息队列等）

---

## 三、核心架构：Agent + Orchestrator（简化版）

### 3.1 统一接口

```java
public interface Agent {
    Context run(Context ctx);
}
```

### 3.2 Context —— 全流程唯一数据载体

```java
public class Context {
    public String prompt;         // 用户输入
    public List<String> images;   // 设计候选图（DesignAgent 填写）
    public String selectedImage;  // 选中图（DecisionAgent 填写）
    public String chainCode;      // 链码（ChainAgent 生成并落库）
    public String iban;           // 收款 IBAN（FinanceAgent 填写）
}
```

### 3.3 Orchestrator —— 流程控制中心

```java
@Service
public class ChainOrchestrator {
    public Context run(String prompt) {
        Context ctx = new Context();
        ctx.prompt = prompt;
        ctx = new DesignAgent().run(ctx);    // 1. 设计生成
        ctx = new DecisionAgent().run(ctx);  // 2. 选图决策
        ctx = chainAgent.run(ctx);           // 3. 链码落库（Spring Bean）
        ctx = new FinanceAgent().run(ctx);   // 4. 生成 IBAN
        return ctx;
    }
}
```

---

## 四、各 Agent 说明

### 1️⃣ DesignAgent —— 设计生成

| 属性 | 说明 |
|------|------|
| 类型 | 普通 Java 类（`new` 创建） |
| 输入 | `ctx.prompt` |
| 输出 | `ctx.images`（3 个 URL） |
| MVP | 返回固定 mock URL（带 prompt 后缀区分） |
| 扩展点 | 替换为真实 AI 出图（FLUX、Stable Diffusion 等） |

### 2️⃣ DecisionAgent —— 选图决策

| 属性 | 说明 |
|------|------|
| 类型 | 普通 Java 类（`new` 创建） |
| 输入 | `ctx.images` |
| 输出 | `ctx.selectedImage` |
| MVP | 直接选 `images.get(0)` |
| 扩展点 | 接入评分模型或人工审核 |

### 3️⃣ ChainAgent —— 链码生成（核心）

| 属性 | 说明 |
|------|------|
| 类型 | Spring `@Component`（需注入 Mapper） |
| 输入 | `ctx.selectedImage` |
| 输出 | `ctx.chainCode`，写库 |
| 规则 | 6 位随机大写字母 + 数字，重复时最多重试 10 次 |
| 扩展点 | 接入区块链铸造、ima 系统等 |

### 4️⃣ FinanceAgent —— 支付 IBAN

| 属性 | 说明 |
|------|------|
| 类型 | 普通 Java 类（`new` 创建） |
| 输入 | `ctx.chainCode` |
| 输出 | `ctx.iban` |
| MVP | 返回 `DEEPAY-DEMO-{chainCode}` |
| 扩展点 | 接入 Swan 支付或真实银行通道 |

---

## 五、数据库

### 表：`deepay_style_chain`

```sql
CREATE TABLE IF NOT EXISTS `deepay_style_chain`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `chain_code` VARCHAR(64)  NOT NULL DEFAULT '' COMMENT '6位随机大写链码，全局唯一',
    `image_url`  VARCHAR(512) NOT NULL DEFAULT '' COMMENT '最终选中的设计图片URL',
    `status`     VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '状态：CREATED',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `uk_chain_code` (`chain_code`) COMMENT '链码唯一索引'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = 'Deepay 样式链码表';
```

初始化脚本位置：`sql/mysql/deepay.sql`

---

## 六、API 接口

### `POST /api/create-product`

**请求**

```json
{
  "prompt": "极简羊绒大衣"
}
```

**响应**

```json
{
  "code": 0,
  "data": {
    "chainCode": "A7K9X2",
    "image": "https://deepay-assets.example.com/designs/...",
    "iban": "DEEPAY-DEMO-A7K9X2",
    "link": "https://deepay.link/A7K9X2"
  },
  "msg": ""
}
```

> 接口**无需登录**，`SecurityConfiguration` 已配置 `permitAll`。

---

## 七、代码位置

```
yudao-module-deepay/
├── pom.xml
└── src/main/java/cn/iocoder/yudao/module/deepay/
    ├── agent/
    │   ├── Agent.java              # 统一接口
    │   ├── Context.java            # 全流程数据载体
    │   ├── DesignAgent.java        # 设计生成（mock）
    │   ├── DecisionAgent.java      # 选图决策
    │   ├── ChainAgent.java         # 链码生成+落库（Spring Bean）
    │   └── FinanceAgent.java       # 支付 IBAN（mock）
    ├── orchestrator/
    │   └── ChainOrchestrator.java  # 流程编排器（Spring Service）
    ├── dal/
    │   ├── dataobject/
    │   │   └── DeepayStyleChainDO.java
    │   └── mysql/
    │       └── DeepayStyleChainMapper.java
    ├── controller/
    │   └── DeepayProductController.java
    └── framework/security/config/
        └── SecurityConfiguration.java  # 放开 /api/create-product
```

---

## 八、快速验证（curl）

启动服务后执行：

```bash
curl -X POST http://localhost:8080/api/create-product \
  -H "Content-Type: application/json" \
  -d '{"prompt":"极简羊绒大衣"}'
```

预期返回（示例）：

```json
{
  "code": 0,
  "data": {
    "chainCode": "A7K9X2",
    "image": "https://deepay-assets.example.com/designs/极简羊绒大衣/v1.jpg",
    "iban": "DEEPAY-DEMO-A7K9X2",
    "link": "https://deepay.link/A7K9X2"
  },
  "msg": ""
}
```

---

## 九、禁止事项

| ❌ 禁止 | ✅ 正确做法 |
|---------|-----------|
| 引入 LangChain 等复杂框架 | 使用普通 Spring Bean |
| 拆微服务 | 同一进程内同步调用 |
| 接真实 AI | mock 数据，URL 占位 |
| 接真实支付 | mock IBAN 字符串 |
| 异步执行 | 全部同步，单线程流水线 |

---

## 十、后续扩展路线（仅参考）

1. **真实 AI 出图**：替换 `DesignAgent`，接入 FLUX / Stable Diffusion API
2. **ima 系统**：在 `ChainAgent` 后追加 ImaAgent
3. **Swan 支付**：替换 `FinanceAgent`，接入真实支付通道
4. **商品页**：在响应的 `link` 后加跳转页面

---

> 文档最后更新：2026-04-21 | 负责人：Deepay 研发团队
