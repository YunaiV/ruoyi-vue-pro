# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作时提供指导。

## 快速参考

- **构建**: `mvn clean install -DskipTests`（全模块）；建议默认跳过测试，因为完整测试量大
- **运行测试**: `mvn test -pl yudao-module-system`（单个模块）；`mvn test -Dtest=DeptServiceImplTest -pl yudao-module-system`（单个测试类）
- **启动应用**: 运行 `yudao-server` 中的 `cn.iocoder.yudao.server.YudaoServerApplication`，激活 profile 为 `local`（默认）；确保本地 MySQL 数据库 `ruoyi-vue-pro-jdk8` 和 Redis 已启动
- **API 文档**: 启动后访问 Swagger UI（Knife4j）：`/swagger-ui`；默认端口 `48080`

## 项目架构

这是**芋道（Yudao）**——基于 **RuoYi-Vue** 构建的快速开发平台，采用 **Spring Boot 2.7.18 + JDK 8** 多模块 Maven 架构。Maven groupId 为 `cn.iocoder.boot`，Java 基础包名为 `cn.iocoder.yudao`。

### 模块分层

```
yudao-dependencies/        → BOM：统一管理所有 Maven 依赖版本
yudao-framework/           → 框架扩展（14 个 Spring Boot Starter）
  ├── yudao-common/                     → 基础 POJO、枚举、工具类（所有模块共享）
  ├── yudao-spring-boot-starter-mybatis → MyBatis Plus + 多数据源配置
  ├── yudao-spring-boot-starter-redis   → Redis + Redisson 缓存与分布式锁
  ├── yudao-spring-boot-starter-security → Spring Security + Token 认证 + 权限框架
  ├── yudao-spring-boot-starter-web     → Spring MVC + Jackson + XSS 防护 + API 加密
  ├── yudao-spring-boot-starter-websocket → WebSocket 支持（含集群方案）
  ├── yudao-spring-boot-starter-mq      → 消息队列抽象层（Redis Stream / RabbitMQ / Kafka / RocketMQ）
  ├── yudao-spring-boot-starter-job     → Quartz 定时任务调度
  ├── yudao-spring-boot-starter-protection → 限流 + 幂等 + 分布式锁
  ├── yudao-spring-boot-starter-monitor → SkyWalking 链路追踪 + Spring Boot Admin
  ├── yudao-spring-boot-starter-excel   → Excel 导入导出
  ├── yudao-spring-boot-starter-test    → 测试基类 + 随机 POJO 工具 + 断言辅助类
  ├── yudao-spring-boot-starter-biz-tenant      → 多租户支持
  ├── yudao-spring-boot-starter-biz-data-permission → 数据权限过滤
  └── yudao-spring-boot-starter-biz-ip          → IP/地区工具

yudao-server/              → 薄壳容器：Spring Boot 入口，通过 Maven 依赖组装各模块
yudao-module-system/       → **始终启用**：认证、用户、角色、菜单、部门、租户、OAuth2、短信、邮件、通知
yudao-module-infra/        → **始终启用**：代码生成、文件服务、定时任务管理、API 日志、配置管理
yudao-module-{bpm|pay|mall|member|crm|erp|mes|wms|im|ai|iot|mp|report}/ → 可选业务模块
```

可选模块在根 `pom.xml`（`<modules>` 中）和 `yudao-server/pom.xml`（依赖中）均被注释，以保证默认编译速度。要启用某个模块，需要同时取消这两处的注释。

### 模块内部结构

每个 `yudao-module-*` 在 `cn.iocoder.yudao.module.{name}` 下遵循统一的包结构：

| 包 | 用途 |
| --- | --- |
| `controller/admin/` | 管理后台的 REST 控制器；请求/响应 VO 类放在 `vo/` 子包中 |
| `controller/app/` | 面向移动端/用户的 REST 控制器 |
| `service/` | Service 接口及 `*ServiceImpl` 实现类；每个业务领域一个子包 |
| `dal/dataobject/` | 数据库实体类（后缀 `DO`），使用 MyBatis-Plus + Swagger 注解 |
| `dal/mysql/` | MyBatis-Plus Mapper 接口（继承 `BaseMapperX`） |
| `dal/redis/` | 缓存数据的 Redis DAO |
| `convert/` | MapStruct 转换器接口，用于 DO ↔ VO ↔ DTO |
| `enums/` | 模块特有的枚举类 |
| `api/` | **模块间 Feign 风格的 API 接口**：定义 `Api` 接口，其他模块通过 `@Resource` 注入使用。这是模块间通信的方式，避免直接依赖 |
| `framework/` | 模块内部框架代码（如 `system` 模块中的自定义短信客户端实现） |
| `job/` | Quartz 定时任务类 |
| `mq/` | 消息队列生产者和消费者 |

### 请求/响应 VO 命名规范

- `*ReqVO` — 请求体（查询/列表）
- `*RespVO` — 响应体（详情/列表）
- `*PageReqVO` — 分页查询请求
- `*SaveReqVO` — 新增或修改请求（新增时 id 为 null，修改时 id 非空）
- `*SimpleRespVO` — 精简响应（仅含 id + name，用于下拉框等场景）
- `*ExcelVO` — Excel 导入导出 DTO

### Framework Starter 架构

每个 `yudao-spring-boot-starter-*` 包含两层：
- `core/` — 框架无关的核心逻辑（纯 Java，不依赖 Spring 注解）
- `config/` — Spring 自动配置，将 `core/` 注入 Spring 上下文

后缀为 `-biz-*` 的 Starter 是业务层组件（租户、数据权限、IP）；其余为基础设施层组件（mybatis、redis、security 等）。

### 认证与授权流程

1. `TokenAuthenticationFilter`（位于 `yudao-spring-boot-starter-security`）拦截所有请求
2. 从 `Authorization` 请求头提取 Token，通过 `SecurityFrameworkService` 从 Redis 中查找 `LoginUser`
3. 将 `LoginUser` 存入 `TransmittableThreadLocal` 上下文（支持异步传播）
4. 通过 `@PreAuthorize` + `SecurityFrameworkService.hasPermission()` 实现按钮级权限校验
5. 权限数据缓存在 Redis 中；菜单/权限数据从数据库动态加载

### 多租户与数据权限

- **多租户**: `yudao-spring-boot-starter-biz-tenant` 通过 MyBatis-Plus 拦截器自动按租户 ID 过滤 SQL 查询。忽略的 URL/表/缓存通过 `application.yaml` 中 `yudao.tenant` 配置项管理。
- **数据权限**: `yudao-spring-boot-starter-biz-data-permission` 基于用户部门/角色的数据权限范围，实现行级数据过滤。

### 数据库

- 默认本地数据库：`ruoyi-vue-pro-jdk8`，MySQL `127.0.0.1:3306`（用户名：`root`，密码：`123456`）
- 通过 `dynamic-datasource-spring-boot-starter` 支持多数据源；主数据源为 `master`，预配置了一个懒加载的 `slave` 从库
- MyBatis-Plus 逻辑删除（`deleted=1`）；ID 类型为 `NONE`（根据数据库类型自动适配）
- SQL 初始化脚本位于 `sql/mysql/ruoyi-vue-pro.sql` 和 `sql/mysql/quartz.sql`
- 支持 9 种数据库：MySQL、Oracle、PostgreSQL、SQL Server、MariaDB、DM（达梦）、Kingbase（人大金仓）、OpenGauss、HighGo（瀚高）

## 测试

### 测试基类（来自 `yudao-spring-boot-starter-test`）

| 基类 | 适用场景 |
| --- | --- |
| `BaseMockitoUnitTest` | 纯 Mockito — 模拟所有依赖的 Service 层测试 |
| `BaseDbUnitTest` | 需要 H2 内存数据库的测试（启动 MyBatis-Plus + Druid）；每个模块在 `src/test/resources/sql/` 下有对应的 `create_tables.sql` 和 `clean.sql` |
| `BaseDbAndRedisUnitTest` | 需要数据库 + Redis 的测试（启动内嵌 Redis） |
| `BaseRedisUnitTest` | 仅需要 Redis 的测试 |

### 测试工具类（来自 `yudao-spring-boot-starter-test` 的静态导入）

- `RandomUtils.randomPojo(Class, Consumer)` — 生成带合理默认值的随机 POJO，可通过 Consumer 自定义字段值
- `RandomUtils.randomString()`、`randomLongId()`、`randomCommonStatus()` — 常用随机值生成
- `AssertUtils.assertPojoEquals(expected, actual, ...ignoredFields)` — 反射逐字段断言
- `AssertUtils.assertServiceException(ErrorCode, Runnable)` — 断言抛出指定错误码的 `ServiceException`

### 测试示例

```java
@Import(SomeServiceImpl.class)  // 仅导入被测类
public class SomeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private SomeServiceImpl someService;     // 被测类
    @Resource
    private SomeMapper someMapper;           // 真实 Mapper（H2 数据库支持）

    @Test
    public void testCreate() {
        // 1. 构造随机请求 VO
        SomeSaveReqVO reqVO = randomPojo(SomeSaveReqVO.class, o -> o.setId(null));
        // 2. 调用 Service 方法
        Long id = someService.createSome(reqVO);
        // 3. 验证数据库状态
        SomeDO dbRecord = someMapper.selectById(id);
        assertPojoEquals(reqVO, dbRecord, "id");
    }
}
```

### 关键测试约定

- `XxxServiceImpl` 的测试类始终命名为 `XxxServiceImplTest`（而非 `XxxServiceTest`）
- 在类级别使用 `@Import(XxxServiceImpl.class)`，而非 `@SpringBootTest` 全量扫描
- 每个模块的测试 SQL 文件位于：`src/test/resources/sql/create_tables.sql` 和 `clean.sql`
- `BaseDbUnitTest` 自动设置激活 profile 为 `unit-test`
- 测试文件路径结构与主源码完全一致（如 `service/dept/DeptServiceImplTest.java` 测试 `service/dept/DeptServiceImpl.java`）

## 配置

- `application.yaml`（位于 `yudao-server`）— 所有 profile 共享的配置
- `application-local.yaml` — 本地开发环境（默认激活）：`spring.profiles.active=local`，DB/Redis/MQ 指向 localhost，验证码关闭，开启 mock 安全模式
- `application-dev.yaml` — 开发/测试环境
- `application-unit-test` — 由 `BaseDbUnitTest` 通过 `@ActiveProfiles("unit-test")` 自动加载

需要了解的 `yudao.*` 自定义配置项：
- `yudao.info.base-package=cn.iocoder.yudao` — 控制 `@SpringBootApplication` 的扫描范围
- `yudao.tenant.enable=true` — 多租户开关
- `yudao.security.mock-enable=true` — 本地环境绕过真实认证
- `yudao.captcha.enable=false` — 本地环境关闭验证码校验

## 模块间通信

模块通过各自的 `api/` 包进行通信。每个模块在 `api/` 下暴露 `*Api` 接口，其他模块通过 `@Resource` 注入使用。例如：`yudao-module-system/api/permission/PermissionApi.java` 在 `yudao-module-system` 中作为 `@Service` 实现，可以被任何需要权限校验的其他模块注入使用。

## 包可见性规则

- `controller` → 调用 → `service`（接口位于 `service/`，实现类为包级可见、后缀为 `*Impl`）
- `service` → 使用 → `dal/mysql/`（Mapper）、`dal/redis/`（Redis DAO）、`convert/`（MapStruct 转换器）
- 模块间**不应**直接引用另一个模块的 `service/` 或 `dal/` —— 始终通过 `api/` 接口调用
- `yudao-common` 由**所有**模块共享（包含 `PageParam`、`PageResult`、`CommonResult` 等基础 POJO，以及各种工具类）