# Flowable 达梦补丁回归测试

本目录是独立的 Maven 测试工程，直接编译同级 `flowable-patch-${flowable.version}` 补丁目录的源码，验证补丁本身。它不加入主工程模块，不参与业务默认构建，也不要复制到业务模块。

补丁安装请阅读项目文档《工作流（达梦适配）》，本文只说明如何测试。

## 1. 版本矩阵

| Profile | JDK | Spring Boot | Flowable | 被测补丁 |
| --- | --- | --- | --- | --- |
| `jdk8` | JDK8 | 2.7.18 | 6.8.0 | `flowable-patch-6.8.0` |
| 默认 | JDK17 或 JDK25 | 3.5.15 | 8.0.0 | `flowable-patch-8.0.0` |
| `sb4` | JDK25 | 4.1.1 | 8.0.0 | `flowable-patch-8.0.0` |

各组合的编译输出位于 `target/<Spring Boot 版本>-<Flowable 版本>`，互不覆盖。

## 2. 准备数据库

先创建独立的非生产测试账号，只授予该测试 schema 的建表权限，**不要使用生产库**。6.8.0 与 8.0.0 请使用不同账号/schema 分别测试，不能在同一工作流 schema 混跑两个 Flowable 版本。

通过环境变量指定数据库，密码由本地环境提供，不写入 Git：

```bash
export DM_JDBC_URL='jdbc:dm://127.0.0.1:15236'
export DM_JDBC_USER='FLOWABLE_TEST'
export DM_JDBC_PASSWORD='本地测试密码'
```

## 3. 执行测试

在 `ruoyi-vue-pro` 仓库根目录（包含 `sql/` 目录的一级）执行：

```bash
# 使用 JDK8：Spring Boot 2.7.18 + Flowable 6.8.0
mvn -f sql/dm/flowable-patch-tests/pom.xml -Pjdk8 test

# 使用 JDK17 或 JDK25：Spring Boot 3.5.15 + Flowable 8.0.0
mvn -f sql/dm/flowable-patch-tests/pom.xml test

# 使用 JDK25：Spring Boot 4.1.1 + Flowable 8.0.0
mvn -f sql/dm/flowable-patch-tests/pom.xml -Psb4 test
```

未配置 `DM_JDBC_URL` 时，只运行基于 H2 的非达梦检查（其它数据库不受补丁影响；6.8.0 另检查 SPI 注册顺序），真实达梦用例会在 Maven 输出中显示为 `Skipped`。此时**不能据此认定达梦适配通过**；验收时应确认真实达梦用例没有被跳过。

## 4. 测试覆盖与边界

覆盖 Spring Boot 自动装配（不手工关闭应用级 Liquibase）、首次建表与保持 `flowable.database-schema-update: true` 的重复启动、流程部署/发起/两级审批、退回与拒绝、取消、长文本和布尔变量、历史与分页、事务回滚、并行网关、定时 Job 执行、DMN 建表与决策，以及自动装配后一次部署多个 DMN/事件定义。6.8.0 另覆盖 Oracle/DM 两种 SPI 注册顺序，以及 DM 元数据与布尔类型识别。

不包含完整芋道 Web 服务（微服务的网关、注册中心等）及前端浏览器验收，也不代表已验证所有流程模型、并发性能、跨版本历史库升级，或所有第三方 Flowable 用法。补丁合并到业务模块后，仍需在实际项目中验证启动及流程功能。

## 5. 实现说明

- 两个版本的 `DmFlowableConfiguration` 保持一致。通过 Spring `BeanPostProcessor`，在各个 Flowable 引擎配置 Bean 初始化前，按引擎实际连接的 JDBC 元数据识别达梦，设置工作流 Oracle 方言及普通插入，覆盖主引擎和 Spring 事件/DMN 子引擎。不修改 Flowable 原类，不替换全局 MyBatis `DatabaseIdProvider`，不依赖数据源名称或 `yudao.dameng.enabled` 开关。
- 对达梦关闭 `INSERT ALL` 批量插入，避免部分 DM8 环境下普通用户访问 `SYS.SYSDUAL2` 失败，无需给业务账号授予 DBA。仅关闭主引擎不够，子引擎会将自己的批量实体重新注册到共享 SQL 工厂。代价是批量写入吞吐可能降低，需按实际负载压测。
- 补丁只处理 Spring 管理的 Flowable 配置 Bean。手动 `new` 的非 Bean 配置、独立接入的其它引擎不会被自动覆盖，需要自行设置方言及插入策略。
- 仅 6.8.0 使用 Liquibase SPI 注册 `DmDatabase extends OracleDatabase`，复用 Oracle 的字段、布尔值及 schema 识别逻辑，不覆盖 `BooleanType`。注册短名为独立的 `dm`，避免与原生 Oracle 实现竞争同一个注册键；Flowable 方言仍为 `oracle`，两者是不同机制。Flowable 6.8.0 自带的环境后处理器默认关闭 Spring Boot 应用级 Liquibase，内部事件/DMN 的 Liquibase 建表仍正常执行。
- 8.0.0 的事件/DMN 已改用 SQL 脚本，补丁仅包含 Spring 配置，不引入 Liquibase。若额外加入 Liquibase 依赖，Boot 3 会默认查找应用级 changelog 导致启动失败；不要用 `spring.liquibase.enabled=false` 掩盖该问题。
- DM 的 `COMPATIBLE_MODE` 是数据库配置，`oracle` 是 Flowable 方言，两者不是同一设置。不要在已有库上直接更换方言、重建表或混用两个 Flowable 版本；升级现有工作流数据库须另行备份、验证官方升级脚本，补丁不是升级脚本。
