# 分布式事务（可选接入）

基于 [Apache Seata](https://seata.apache.org/) 2.6.0 AT 模式的可选接入材料。项目默认**不启用**：本目录不是 Maven 模块，不参与默认构建，主工程的依赖、配置、业务表都不受影响。

接入后，`master`、`slave` 数据源保持不变、不参与全局事务，只有示例的 `seata_order`、`seata_account` 两个数据源由 Dynamic Datasource 包装 Seata 代理。示例 `DemoSeataTradeService#placeOrder` 在同一个进程中，先在订单库创建订单、再在账户库扣减余额，两个库的操作要么都成功，要么都回滚。

Seata Server 的启动、配置、验证及使用限制，请阅读项目文档《分布式事务》，本文只列出材料及分支适用范围：

- 单体项目 ruoyi-vue-pro：<https://doc.iocoder.cn/distributed-transaction/>
- 微服务项目 yudao-cloud：<https://cloud.iocoder.cn/distributed-transaction/>

## 1. 材料清单

| 文件 | 说明 | 目标位置 |
| --- | --- | --- |
| `sql/mysql/demo_seata.sql` | 创建 2 个演示库，各一张示例表 + `undo_log` 表 | 在 MySQL 中执行 |
| `dependencies.xml` | Maven 依赖片段 | `yudao-module-system/pom.xml`（全局事务入口所在的模块，`yudao-server` 通过已有的模块依赖传递获得） |
| `seata.yaml` | `seata_order`、`seata_account` 数据源，以及 Seata 客户端的配置片段 | 合并到 `yudao-server` 的 `application-{profile}.yaml`；`seata: false` 只追加到已经存在的数据源，没有 `slave` 时不要复制 `slave` 段 |
| `src/main/java/.../system/seatademo/` | 订单 DO、Mapper、Service，以及全局事务入口 `DemoSeataTradeService` | `yudao-module-system/src/main/java` 的同名包 |
| `src/main/java/.../infra/seatademo/` | 账户 DO、Mapper、Service | `yudao-module-infra/src/main/java` 的同名包 |

订单放在 `yudao-module-system`、账户放在 `yudao-module-infra`，是因为 `yudao-module-system` 已经依赖 `yudao-module-infra`，可以直接调用账户 Service；与微服务项目 yudao-cloud 中「`system-server` 发起、`infra-server` 参与」的示例保持一致。

## 2. 分支适用范围

目前只有 `master-jdk17` 分支提供本目录的材料；`master`、`master-jdk25` 分支请先从 `master-jdk17` 分支的 `script/seata/` 获取，同步到对应分支后，也可以直接从当前分支获取。

本目录的材料适用于 `master`（JDK 8 + Spring Boot 2.7.18）、`master-jdk17`（Spring Boot 3.5.15）、`master-jdk25`（Spring Boot 4.1.2）三个分支，接入步骤相同，兼容要点如下：

| 要点 | 说明 |
| --- | --- |
| Seata 坐标 | 只使用 `org.apache.seata:seata-spring-boot-starter:2.6.0`，它自带 Dynamic Datasource 4.6.0 需要的 `io.seata` 兼容类，不要再引入 `io.seata:*` 坐标 |
| Druid | 排除 Seata 传递的 druid 1.2.25，继续使用项目 Druid starter 提供的版本 |
| Jackson 2 | Seata 的 undo_log 序列化需要 `jackson-databind`、`jackson-datatype-jsr310`；`master-jdk25` 的 Spring Boot 4 默认使用 Jackson 3，必须添加，其它分支添加后版本不变 |
| 示例代码 | 只使用 Java 8 语法，使用构造方法注入，不引用 `javax`、`jakarta` 包，三个分支无需修改 |

`@GlobalTransactional` 注解的包名是 `org.apache.seata.spring.annotation`。全局事务入口必须是组件扫描的 `@Service` 等 Bean，Seata 2.6 不会增强 `@Bean` 方法定义的 Bean。
