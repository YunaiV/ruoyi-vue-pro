## 平台简介

**芋道**，一套**全部开源**的**企业级**的快速开发平台，毫无保留给个人及企业免费使用。

> 有任何问题，或者想要的功能，可以在 _Issues_ 中提给艿艿。

* 前端采用 [vue-element-admin](https://github.com/PanJiaChen/vue-element-admin)。
* 后端采用 Spring Boot、MySQL、Redis。
* 权限认证使用 Spring Security & Token，支持多终端认证系统。
* 支持加载动态权限菜单，多方式轻松权限控制。
* 高效率开发，使用代码生成器可以一键生成前后端代码。

## 内置功能

分成三种内置功能：
* 系统功能
* 基础设施
* 研发工具

> 友情提示：本项目基于 RuoYi-Vue 修改，**重构优化**后端的代码，**美化**前端的界面。
> 
> 额外新增的功能，我们使用 🚀 标记。

🙂 所有功能，都通过 **单元测试** 保证高质量。

### 系统功能


|  | 功能 | 描述 |
| --- | --- | --- |
|  | 用户管理 | 用户是系统操作者，该功能主要完成系统用户配置 |
|  | 在线用户 | 当前系统中活跃用户状态监控，支持手动踢下线 |
|  | 角色管理 | 角色菜单权限分配、设置角色按机构进行数据范围权限划分 |
|  | 菜单管理 | 配置系统菜单，操作权限，按钮权限标识等 |
|  | 部门管理 | 配置系统组织机构（公司、部门、小组），树结构展现支持数据权限 |
|  | 岗位管理 | 配置系统用户所属担任职务 |
|  | 字典管理 | 对系统中经常使用的一些较为固定的数据进行维护 |
| 🚀 | 短信管理 | 短信渠道、短息模板、短信日志，对接阿里云、云片等主流短信平台 |
| 🚀 | 操作日志 | 系统正常操作日志记录和查询，集成 Swagger 生成日志内容 |
|  | 登录日志 | 系统登录日志记录查询，包含登录异常 |
| 🚀 | 错误码管理 | 系统所有错误码的管理，可在线修改错误提示，无需重启服务 |
|  | 通知公告 | 系统通知公告信息发布维护 |

计划新增功能：
* 邮件
* 钉钉、飞书等通知

### 基础设施

|  | 功能 | 描述 |
| --- | --- | --- |
| 🚀 | 配置管理 | 对系统动态配置常用参数，支持 SpringBoot 加载 |
| | 定时任务 | 在线（添加、修改、删除)任务调度包含执行结果日志 |
| 🚀 | 文件服务 | 支持本地文件存储，同时支持兼容 Amazon S3 协议的云服务、开源组件 | 
| 🚀 | API 日志 | 包括 RESTful API 访问日志、异常日志两部分，方便排查 API 相关的问题 |
|  | MySQL 监控 | 监视当前系统数据库连接池状态，可进行分析SQL找出系统性能瓶颈 |
|  | Redis 监控 |监控 Redis 数据库的使用情况，使用的 Redis Key 管理 |
| 🚀 |Java 监控 | 基于 Spring Boot Admin 实现 Java 应用的监控 |
| 🚀 | 链路追踪 | 基于 SkyWalking 实现性能监控，特别是链路的追踪 |
| 🚀 | 分布式锁 | 基于 Redis 实现分布式锁，满足并发场景 |
| 🚀 | 幂等组件 | 基于 Redis 实现幂等组件，解决重复请求问题 |
| 🚀 | 服务保障 | 基于 Resilience4j 实现服务的稳定性，包括限流、熔断等功能 |
| 🚀 | 日志服务 | 轻量级日志中心，查看远程服务器的日志 |
| 🚀 | 单元测试 |基于 JUnit + Mockito 实现单元测试，保证功能的正确性、代码的质量等 |

计划新增：
* 工作流

### 研发工具

|  | 功能 | 描述 |
| --- | --- | --- |
| 🚀 | 代码生成 |前后端代码的生成（Java、Vue、SQL、单元测试），支持 CRUD 下载 |
| 🚀 | 系统接口 | 基于 Swagger 自动生成相关的 RESTful API 接口文档 |
| 🚀 | 数据库文档 | 基于 Screw 自动生成数据库文档，支持导出 Word、HTML、MD 格式 |
| | 表单构建 | 拖动表单元素生成相应的 HTML 代码 |

## 在线体验

演示地址：<http://dashboard.yudao.iocoder.cn>
* 账号密码：admin/admin123  

文档地址：<http://www.iocoder.cn/categories/Yudao/>
* [《如何搭建环境》](http://www.iocoder.cn/categories/Yudao/?yudao)

> 未来会补充文档和视频，方便胖友冲冲冲！

## 技术栈

### 后端

| 框架 | 说明 |  版本 | 学习指南 |
| --- | --- | --- | --- |
| [Spring Boot](https://spring.io/projects/spring-boot) | 应用开发框架 | 2.4.2 | [文档](https://github.com/YunaiV/SpringBoot-Labs) |
| [MySQL](https://www.mysql.com/cn/) | 数据库服务器 | 5.7 |  |
| [Druid](https://github.com/alibaba/druid) | JDBC 连接池、监控组件 | 1.2.4 | [文档](http://www.iocoder.cn/Spring-Boot/datasource-pool/?yudao) |
| [MyBatis Plus](https://mp.baomidou.com/) | MyBatis 增强工具包 | 3.4.1 | [文档](http://www.iocoder.cn/Spring-Boot/MyBatis/?yudao) |
| [Dynamic Datasource](https://dynamic-datasource.com/) | 动态数据源 | 3.3.2 | [文档](http://www.iocoder.cn/Spring-Boot/datasource-pool/?yudao) |
| [Redis](https://redis.io/) | key-value 数据库 | 5.0 |  |
| [Redisson](https://github.com/redisson/redisson) | Redis 客户端 | 3.1.46 | [文档](http://www.iocoder.cn/Spring-Boot/Redis/?yudao) |
| [Spring MVC](https://github.com/spring-projects/spring-framework/tree/master/spring-webmvc) | MVC 框架  | 5.4.2 | [文档](http://www.iocoder.cn/SpringMVC/MVC/?yudao) |
| [Spring Security](https://github.com/spring-projects/spring-security) | Spring 安全框架 | 5.4.2 | [文档](http://www.iocoder.cn/Spring-Boot/Spring-Security/?yudao) |
| [Hibernate Validator](https://github.com/hibernate/hibernate-validator) | 参数校验组件 | 6.1.7 | [文档](http://www.iocoder.cn/Spring-Boot/Validation/?yudao) |
| [Quartz](https://github.com/quartz-scheduler) | 任务调度组件 | 2.3.2 | [文档](http://www.iocoder.cn/Spring-Boot/Job/?yudao) |
| [Knife4j](https://gitee.com/xiaoym/knife4j) | Swagger 增强 UI 实现 | 3.0.2 | [文档](http://www.iocoder.cn/Spring-Boot/Swagger/?yudao) |
| [Resilience4j](https://github.com/resilience4j/resilience4j) | 服务保障组件 | 1.7.0 | [文档](http://www.iocoder.cn/Spring-Boot/Resilience4j/?yudao) |
| [SkyWalking](https://skywalking.apache.org/) | 分布式应用追踪系统 | 8.6.0 | [文档](http://www.iocoder.cn/Spring-Boot/SkyWalking/?yudao) |
| [Spring Boot Admin](https://github.com/codecentric/spring-boot-admin) | Spring Boot 监控平台 | 8.6.0 | [文档](http://www.iocoder.cn/Spring-Boot/Admin/?yudao) |
| [Jackson](https://github.com/FasterXML/jackson) | JSON 工具库 | 2.11.4 |  |
| [MapStruct](https://mapstruct.org/) | Java Bean 转换 | 1.4.1 | [文档](http://www.iocoder.cn/Spring-Boot/MapStruct/?yudao) |
| [Lombok](https://projectlombok.org/) | 消除冗长的 Java 代码 | 1.16.14 | [文档](http://www.iocoder.cn/Spring-Boot/Lombok/?yudao) |
| [JUnit](https://junit.org/junit5/) | Java 单元测试框架 | 5.7.0 | - |
| [Mockito](https://github.com/mockito/mockito) | Java Mock 框架 | 3.6.28 | - |

### 前端

| 框架 | 说明 |  版本 |
| --- | --- | --- |
| [Vue](https://cn.vuejs.org/index.html) | JavaScript 框架 | 2.6.12 |
| [Vue Element Admin](https://ant.design/docs/react/introduce-cn) | 后台前端解决方案 | - |

## 演示图

<table>
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/cd1f90be5f2684f4560c9519c0f2a232ee8.jpg"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/1cbcf0e6f257c7d3a063c0e3f2ff989e4b3.jpg"/></td>
    </tr>
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-8074972883b5ba0622e13246738ebba237a.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-9f88719cdfca9af2e58b352a20e23d43b12.png"/></td>
    </tr>
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-39bf2584ec3a529b0d5a3b70d15c9b37646.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-936ec82d1f4872e1bc980927654b6007307.png"/></td>
    </tr>
	<tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-b2d62ceb95d2dd9b3fbe157bb70d26001e9.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-d67451d308b7a79ad6819723396f7c3d77a.png"/></td>
    </tr>	 
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/5e8c387724954459291aafd5eb52b456f53.jpg"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/644e78da53c2e92a95dfda4f76e6d117c4b.jpg"/></td>
    </tr>
	<tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-8370a0d02977eebf6dbf854c8450293c937.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-49003ed83f60f633e7153609a53a2b644f7.png"/></td>
    </tr>
	<tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-d4fe726319ece268d4746602c39cffc0621.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-c195234bbcd30be6927f037a6755e6ab69c.png"/></td>
    </tr>
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/b6115bc8c31de52951982e509930b20684a.jpg"/></td>
        <td> - </td>
    </tr>
</table>


