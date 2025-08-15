# YuDao Framework 框架封装解析

## 📋 概述

YuDao Framework 是芋道源码团队开发的企业级快速开发平台的核心框架模块，采用模块化设计和 Spring Boot Starter 模式，提供了完整的技术组件封装。

## 🏗️ 整体架构设计

### 模块化设计理念

```xml
<description>
    该包是技术组件，每个子包，代表一个组件。每个组件包括两部分：
        1. core 包：是该组件的核心封装
        2. config 包：是该组件基于 Spring 的配置

    技术组件，也分成两类：
        1. 框架组件：和我们熟悉的 MyBatis、Redis 等等的拓展
        2. 业务组件：和业务相关的组件的封装，例如说数据字典、操作日志等等。
    如果是业务组件，Maven 名字会包含 biz
</description>
```

### Spring Boot Starter 模式

芋道框架采用了 Spring Boot Starter 的设计模式，每个功能模块都是一个独立的 starter，遵循"约定优于配置"的原则。

## 📦 核心模块分类

### 基础框架组件

| 模块名称 | 功能描述 | 核心特性 |
|---------|---------|---------|
| `yudao-common` | 通用工具和基础类 | 统一返回结果、分页、异常处理 |
| `yudao-spring-boot-starter-web` | Web 层封装 | 全局异常处理、CORS、API 前缀 |
| `yudao-spring-boot-starter-mybatis` | 数据访问层封装 | 分页插件、审计字段、多数据源 |
| `yudao-spring-boot-starter-redis` | 缓存层封装 | 序列化配置、缓存注解增强 |
| `yudao-spring-boot-starter-security` | 安全框架封装 | JWT、权限控制、登录认证 |

### 中间件组件

| 模块名称 | 功能描述 | 核心特性 |
|---------|---------|---------|
| `yudao-spring-boot-starter-job` | 定时任务封装 | Quartz 集成、任务管理 |
| `yudao-spring-boot-starter-mq` | 消息队列封装 | RocketMQ、本地事件 |
| `yudao-spring-boot-starter-monitor` | 监控组件封装 | SkyWalking、Admin 监控 |
| `yudao-spring-boot-starter-protection` | 服务保障封装 | 限流、熔断、降级 |

### 业务组件 (带 biz 标识)

| 模块名称 | 功能描述 | 核心特性 |
|---------|---------|---------|
| `yudao-spring-boot-starter-biz-tenant` | 多租户封装 | 租户隔离、上下文管理 |
| `yudao-spring-boot-starter-biz-data-permission` | 数据权限封装 | 部门权限、用户权限 |
| `yudao-spring-boot-starter-biz-ip` | IP 相关业务封装 | IP 地址解析、地理位置 |

### 工具组件

| 模块名称 | 功能描述 | 核心特性 |
|---------|---------|---------|
| `yudao-spring-boot-starter-excel` | Excel 处理封装 | 导入导出、模板处理 |
| `yudao-spring-boot-starter-test` | 测试工具封装 | 单元测试、集成测试 |
| `yudao-spring-boot-starter-websocket` | WebSocket 封装 | 实时通信、消息推送 |

## 🔧 封装设计模式

### 1. 统一返回结果封装

```java
/**
 * 通用返回
 * @param <T> 数据泛型
 */
@Data
public class CommonResult<T> implements Serializable {
    /**
     * 错误码
     */
    private Integer code;
    /**
     * 错误提示，用户可阅读
     */
    private String msg;
    /**
     * 返回数据
     */
    private T data;
    
    // 静态方法提供便捷的创建方式
    public static <T> CommonResult<T> success(T data) {
        // 实现逻辑
    }
    
    public static <T> CommonResult<T> error(Integer code, String message) {
        // 实现逻辑
    }
}
```

### 2. 自动配置模式

```java
@AutoConfiguration
@EnableConfigurationProperties(WebProperties.class)
public class YudaoWebAutoConfiguration implements WebMvcConfigurer {
    
    @Resource
    private WebProperties webProperties;
    
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 配置 API 前缀
        configurePathMatch(configurer, webProperties.getAdminApi());
        configurePathMatch(configurer, webProperties.getAppApi());
    }
    
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
```

### 3. 分页封装

```java
@Data
public class PageParam implements Serializable {
    private static final Integer PAGE_NO = 1;
    private static final Integer PAGE_SIZE = 10;
    
    @Min(value = 1, message = "页码最小值为 1")
    private Integer pageNo = PAGE_NO;
    
    @Min(value = 1, message = "每页条数最小值为 1")
    @Max(value = 100, message = "每页条数最大值为 100")
    private Integer pageSize = PAGE_SIZE;
}

@Data
public class PageResult<T> implements Serializable {
    private List<T> list;
    private Long total;
    
    // 构造方法和工具方法
}
```

## 🎯 核心封装特点

### 1. 分层封装架构

```
yudao-spring-boot-starter-xxx/
├── src/main/java/
│   └── cn/iocoder/yudao/framework/xxx/
│       ├── core/          # 核心业务逻辑封装
│       │   ├── handler/   # 处理器封装
│       │   ├── filter/    # 过滤器封装
│       │   └── util/      # 工具类封装
│       ├── config/        # Spring 自动配置
│       └── package-info.java  # 包说明文档
└── src/main/resources/
    └── META-INF/
        └── spring.factories  # 自动配置声明
```

### 2. 多租户架构支持

- **租户上下文管理**: 自动处理租户ID的传递和隔离
- **数据库层面隔离**: MyBatis 插件自动添加租户条件
- **缓存层面隔离**: Redis key 自动添加租户前缀
- **消息队列隔离**: MQ 消息自动携带租户信息

### 3. 数据权限封装

- **部门数据权限**: 基于部门层级的数据访问控制
- **用户数据权限**: 基于用户维度的数据访问控制
- **自定义权限规则**: 支持灵活的权限规则配置

### 4. 统一异常处理

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(value = ServiceException.class)
    public CommonResult<?> serviceExceptionHandler(ServiceException ex) {
        return CommonResult.error(ex.getCode(), ex.getMessage());
    }
    
    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public CommonResult<?> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException ex) {
        // 处理逻辑
    }
}
```

### 5. 监控和链路追踪

- **SkyWalking 集成**: 自动链路追踪
- **Spring Boot Admin**: 应用监控和管理
- **自定义监控指标**: 业务指标监控

## 🚀 使用优势

### 1. 开箱即用

```xml
<!-- 只需引入对应的 starter 依赖 -->
<dependency>
    <groupId>cn.iocoder.boot</groupId>
    <artifactId>yudao-spring-boot-starter-web</artifactId>
</dependency>
```

- 自动配置，无需复杂的配置文件
- 提供合理的默认配置
- 支持配置文件覆盖默认行为

### 2. 高度可扩展

- 基于 Spring Boot 的条件装配
- 支持自定义配置覆盖默认行为
- 插件化架构，易于扩展新功能

### 3. 企业级特性

- ✅ 多租户支持
- ✅ 数据权限控制
- ✅ 完整的监控体系
- ✅ 高可用和容错机制
- ✅ 安全防护机制

### 4. 开发效率提升

- 减少重复代码编写
- 统一的开发规范和模式
- 丰富的工具类和帮助方法
- 完善的文档和示例

## 📚 最佳实践

### 1. 模块引入原则

```xml
<!-- 按需引入，避免不必要的依赖 -->
<dependencies>
    <!-- 基础 Web 功能 -->
    <dependency>
        <groupId>cn.iocoder.boot</groupId>
        <artifactId>yudao-spring-boot-starter-web</artifactId>
    </dependency>
    
    <!-- 数据库访问 -->
    <dependency>
        <groupId>cn.iocoder.boot</groupId>
        <artifactId>yudao-spring-boot-starter-mybatis</artifactId>
    </dependency>
    
    <!-- 多租户支持（按需） -->
    <dependency>
        <groupId>cn.iocoder.boot</groupId>
        <artifactId>yudao-spring-boot-starter-biz-tenant</artifactId>
    </dependency>
</dependencies>
```

### 2. 配置文件示例

```yaml
yudao:
  web:
    admin-api:
      prefix: /admin-api
    app-api:
      prefix: /app-api
  tenant:
    enable: true
    ignore-urls:
      - /admin-api/system/tenant/get-id-by-name
  security:
    permit-all-urls:
      - /admin-api/system/auth/login
```

## 🔍 总结

YuDao Framework 通过模块化设计和 Spring Boot Starter 模式，提供了一套完整的企业级开发框架封装。其核心优势在于：

1. **标准化**: 统一的开发规范和模式
2. **模块化**: 按需引入，灵活组合
3. **企业级**: 支持多租户、数据权限等企业特性
4. **易扩展**: 基于 Spring Boot 的自动配置机制
5. **高效率**: 减少重复代码，提高开发效率

这种封装设计让开发者可以专注于业务逻辑的实现，而不需要关心底层技术细节的处理，大大提高了开发效率和代码质量。
