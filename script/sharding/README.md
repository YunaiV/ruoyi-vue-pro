# 分库分表（可选接入）

基于 [Apache ShardingSphere-JDBC](https://shardingsphere.apache.org/) 5.5.3 的可选接入材料。项目默认**不启用**：本目录不是 Maven 模块，不参与默认构建，主工程的依赖、配置、业务表都不受影响。

接入后，`master` 主库保持不变，只有 `@DS("sharding")` 的代码，才通过 ShardingSphereDriver 访问 2 库 × 2 表的示例逻辑表 `demo_sharding_order`。

安装、配置、启动验证及使用限制，请阅读项目文档《分库分表》，本文只列出材料及分支适用范围：

- 单体项目 ruoyi-vue-pro：<https://doc.iocoder.cn/sharding/>
- 微服务项目 yudao-cloud：<https://cloud.iocoder.cn/sharding/>

## 1. 材料清单

| 文件 | 说明 | 单体项目的目标位置 | 微服务项目的目标位置 |
| --- | --- | --- | --- |
| `sql/mysql/demo_sharding_order.sql` | 创建 2 个物理库、4 张物理表 | 在 MySQL 中执行 | 在 MySQL 中执行 |
| `dependencies.xml` | Maven 依赖片段 | `yudao-server/pom.xml` | 服务的 `pom.xml`，例如 `yudao-module-infra-server/pom.xml` |
| `dynamic-datasource.yaml` | `sharding` 数据源配置片段 | `yudao-server` 的 `application-{profile}.yaml` | 服务的 `application-{profile}.yaml` 或 Nacos 配置 |
| `src/main/resources/sharding/sharding.yaml` | ShardingSphere 分片规则 | `yudao-module-infra/src/main/resources` | `yudao-module-infra-server/src/main/resources` |
| `src/main/java/.../shardingdemo/` | 示例 DO、Mapper、Service | `yudao-module-infra/src/main/java` | `yudao-module-infra-server/src/main/java` |

## 2. 分支适用范围

本目录维护在 `master-jdk17` 分支，`master`、`master-jdk25` 分支通过正常的分支同步获得同一份材料，三个分支的接入步骤相同。按各分支 Spring Boot BOM 管理的依赖版本，接入时的差异如下：

| 分支 | JDK | Spring Boot | 差异 |
| --- | --- | --- | --- |
| `master` | 8 | 2.7.18 | 添加 `dependencies.xml` 第 2 段的 SnakeYAML 1.33；不能单独升级到 2.x，原因见该文件的注释 |
| `master-jdk17` | 17 | 3.5.15 | 添加 `dependencies.xml` 第 3 段的 SnakeYAML 2.7 |
| `master-jdk25` | 25 | 4.1.2 | 添加 `dependencies.xml` 第 3 段的 SnakeYAML 2.7 |

`dependencies.xml` 的第 2、3 段 SnakeYAML 只能按分支选择其一，不能同时添加。

示例 Java 代码只依赖项目已有的 MyBatis Plus、Dynamic Datasource 和多租户组件，不直接引用 ShardingSphere 的类，三个分支无需修改。
