# 限流熔断（可选接入）

基于 [Sentinel](https://sentinelguard.io/zh-cn/docs/introduction.html) 1.8.10 的可选接入材料，演示接口（资源）级别的限流、熔断。项目默认**不启用**：本目录不是 Maven 模块，不参与默认构建，主工程的依赖、配置、业务代码都不受影响。

项目的 `yudao-spring-boot-starter-protection` 组件本身**不包含** Sentinel，它提供的是基于 Redis（Redisson）的 `@RateLimiter` 请求限流、`@Idempotent` 幂等、Lock4j 分布式锁，接入本材料后保持不变，可以和 Sentinel 同时使用。

接入后，只有添加了 `@SentinelResource` 注解、并且配置了规则的资源，才会被限流、熔断，其它接口不受影响。示例的规则从 classpath 中的 JSON 文件加载，只保存在 JVM 内存中；单机阈值，多个实例不共享计数。

接入步骤、验证方式及使用限制，请阅读项目文档《限流熔断》，本文只列出材料及分支适用范围：

- 单体项目 ruoyi-vue-pro：<https://doc.iocoder.cn/server-protection/>
- 微服务项目 yudao-cloud：<https://cloud.iocoder.cn/server-protection/>

## 1. 材料清单

| 文件 | 说明 | 目标位置 |
| --- | --- | --- |
| `dependencies.xml` | Maven 依赖片段：第 1 段 Sentinel 核心及注解切面（必须），第 2 段与 Dashboard 通信的模块（可选） | `yudao-module-infra/pom.xml` |
| `src/main/resources/sentinel/demo-flow-rules.json` | 限流规则：资源 `demo-sentinel-flow`，QPS 2 | `yudao-module-infra/src/main/resources/sentinel/` |
| `src/main/resources/sentinel/demo-degrade-rules.json` | 熔断规则：资源 `demo-sentinel-degrade`，慢调用比例 | `yudao-module-infra/src/main/resources/sentinel/` |
| `src/main/java/.../infra/sentineldemo/DemoSentinelConfiguration.java` | 注册 `SentinelResourceAspect` 切面，启动时加载上面两个规则文件 | `yudao-module-infra/src/main/java` 的同名包 |
| `src/main/java/.../infra/sentineldemo/controller/admin/DemoSentinelController.java` | 示例接口 `/admin-api/infra/sentinel-demo/flow`、`/admin-api/infra/sentinel-demo/degrade`，及被阻断时的 `blockHandler` | `yudao-module-infra/src/main/java` 的同名包 |

示例的包名在 `yudao.info.base-package`（`cn.iocoder.yudao`）下，才能被 Spring 扫描到；Controller 放在 `controller.admin` 子包下，才会添加 `/admin-api` 前缀。规则 JSON 是严格的 JSON，不能包含注释，字段说明见项目文档。

## 2. 分支适用范围

目前只有 `master-jdk17` 分支提供本目录的材料；`master`、`master-jdk25` 分支请先从 `master-jdk17` 分支的 `script/sentinel/` 获取，同步到对应分支后，也可以直接从当前分支获取。

本目录的材料按 `master`（JDK 8 + Spring Boot 2.7.18）、`master-jdk17`（Spring Boot 3.5.15）、`master-jdk25`（Spring Boot 4.1.2）三个分支的 API 编写，接入步骤相同，兼容要点如下：

| 要点 | 说明 |
| --- | --- |
| Sentinel 坐标 | 显式指定 `com.alibaba.csp:*:1.8.10`，三个依赖的版本保持一致。单体项目没有 Spring Cloud Alibaba 的 BOM，不要为了 Sentinel 引入 Spring Cloud 相关的 BOM 或 starter |
| JDK 8 | Sentinel 1.8.10 的常规类按 Java 8 字节码发布（仅 `module-info.class` 面向 Java 9+），`master` 分支可以使用 |
| AOP | `@SentinelResource` 依赖 Spring AOP，项目的 Security 组件已经引入（`master`、`master-jdk17` 是 `spring-boot-starter-aop`，`master-jdk25` 是 `spring-boot-starter-aspectj`），不需要额外添加 |
| 示例代码 | 只使用 Java 8 语法，不引用 `javax`、`jakarta` 包；规则通过项目的 `JsonUtils` 解析，不区分 Jackson 2、Jackson 3，三个分支无需修改 |
| Web 适配模块 | 示例没有使用自动统计所有 URL 的 Web 适配模块：它在 `master` 分支是 `sentinel-spring-webmvc-adapter`（`javax`），`master-jdk17` 分支是 `sentinel-spring-webmvc-v6x-adapter`（`jakarta`），两者的包名、`BlockExceptionHandler` 方法签名都不同，需要按分支自行接入 |
| `master-jdk25` | Sentinel 官方没有声明对 Spring Boot 4、Spring Framework 7 的适配，接入后以实际的启动、请求验证为准 |
