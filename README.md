**严肃声明：现在、未来都不会有商业版本，所有功能全部开源！**

**拒绝虚假开源，售卖商业版，程序员不骗程序员！！**

**「我喜欢写代码，乐此不疲」**  
**「我喜欢做开源，以此为乐」**

## 🐯 平台简介

**芋道**，以开发者为中心，打造中国第一流的快速开发平台，全部开源，个人与企业可 100% 免费使用。

> 有任何问题，或者想要的功能，可以在 _Issues_ 中提给艿艿。
>
> 😜 给项目点点 Star 吧，这对我们真的很重要！

* 前端采用 [vue-element-admin](https://github.com/PanJiaChen/vue-element-admin) ，正在支持 Vue 3 + ElementUI Plus 最新方案。
* 后端采用 Spring Boot、MySQL + MyBatis Plus、Redis + Redisson。
* 权限认证使用 Spring Security & Token & Redis，支持多终端、多种用户的认证系统。
* 支持加载动态权限菜单，按钮级别权限控制，本地缓存提升性能。
* 支持 SaaS 多租户系统，可自定义每个租户的权限，提供透明化的多租户底层封装。
* 工作流使用 Activiti + Flowable，支持动态表单、在线设计流程、多种任务分配方式。
* 高效率开发，使用代码生成器可以一键生成前后端代码 + 单元测试 + Swagger 接口文档 + Validator 参数校验。
* 集成微信小程序、微信公众号、企业微信、钉钉等三方登陆，集成支付宝、微信等支付与退款。
* 集成阿里云、腾讯云、云片等短信渠道，集成 MinIO、阿里云、腾讯云、七牛云等云存储服务。

| 项目名                | 说明                     | 传说门                                                                                                                                 |
|--------------------|------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| `ruoyi-vue-pro`    | Spring Boot 多模块        | **[Gitee](https://gitee.com/zhijiantianya/ruoyi-vue-pro)** &nbsp;&nbsp;&nbsp; [Github](https://github.com/YunaiV/ruoyi-vue-pro)     |
| `ruoyi-vue-cloud`  | Spring Cloud 微服务       | **[Gitee](https://gitee.com/zhijiantianya/ruoyi-vue-cloud)** &nbsp;&nbsp;&nbsp; [Github](https://github.com/YunaiV/onemall)         |
| `Spring-Boot-Labs` | Spring Boot & Cloud 入门 | **[Gitee](https://gitee.com/zhijiantianya/SpringBoot-Labs)** &nbsp;&nbsp;&nbsp; [Github](https://github.com/YunaiV/SpringBoot-Labs) |

## 🐶 在线体验

演示地址：<http://dashboard.yudao.iocoder.cn>
* 账号密码：admin/admin123

文档地址：<http://www.iocoder.cn/categories/Yudao/>
* [《如何搭建环境》](http://www.iocoder.cn/categories/Yudao/?yudao)

> 未来会补充文档和视频，方便胖友冲冲冲！

## 🐼 内置功能

分成多种内置功能：
* 系统功能
* 工作流程
* 支付系统
* 商城系统
* 基础设施

> 友情提示：本项目基于 RuoYi-Vue 修改，**重构优化**后端的代码，**美化**前端的界面。
>
> * 额外新增的功能，我们使用 🚀 标记。
> * 重新实现的功能，我们使用 ⭐️ 标记。

🙂 所有功能，都通过 **单元测试** 保证高质量。

### 系统功能

|     | 功能    | 描述                              |
|-----|-------|---------------------------------|
|     | 用户管理  | 用户是系统操作者，该功能主要完成系统用户配置          |
| ⭐️  | 在线用户  | 当前系统中活跃用户状态监控，支持手动踢下线           |
|     | 角色管理  | 角色菜单权限分配、设置角色按机构进行数据范围权限划分      |
|     | 菜单管理  | 配置系统菜单、操作权限、按钮权限标识等，本地缓存提供性能    |
|     | 部门管理  | 配置系统组织机构（公司、部门、小组），树结构展现支持数据权限  |
|     | 岗位管理  | 配置系统用户所属担任职务                    |
| 🚀  | 租户管理  | 配置系统租户，支持 SaaS 场景下的多租户功能        |
| 🚀  | 租户套餐  | 配置租户套餐，自定每个租户的菜单、操作、按钮的权限       |
|     | 字典管理  | 对系统中经常使用的一些较为固定的数据进行维护          |
| 🚀  | 短信管理  | 短信渠道、短息模板、短信日志，对接阿里云、云片等主流短信平台  |
| 🚀  | 操作日志  | 系统正常操作日志记录和查询，集成 Swagger 生成日志内容 |
| ⭐️  | 登录日志  | 系统登录日志记录查询，包含登录异常               |
| 🚀  | 错误码管理 | 系统所有错误码的管理，可在线修改错误提示，无需重启服务     |
|     | 通知公告  | 系统通知公告信息发布维护                    |

### 工作流程

|     | 功能    | 描述                                     |
|-----|-------|----------------------------------------|
| 🚀  | 流程模型  | 配置工作流的流程模型，支持文件导入与在线设计流程图，提供 7 种任务分配规则 |
| 🚀  | 流程表单  | 拖动表单元素生成相应的工作流表单，覆盖 Element UI 所有的表单组件 |
| 🚀  | 用户分组  | 自定义用户分组，可用于工作流的审批分组                    |
| 🚀  | 我的流程  | 查看我发起的工作流程，支持新建、取消流程等操作，高亮流程图、审批时间线    |
| 🚀  | 待办任务  | 查看自己【未】审批的工作任务，支持通过、不通过、转发、委派、退回等操作    |
| 🚀  | 已办任务  | 查看自己【已】审批的工作任务，未来会支持回退操作               |
| 🚀  | OA 请假 | 作为业务自定义接入工作流的使用示例，只需创建请求对应的工作流程，即可进行审批 |

### 支付系统

|     | 功能   | 描述                        |
|-----|------|---------------------------|
| 🚀  | 商户信息 | 管理商户信息，支持 Saas 场景下的多商户功能  |
| 🚀  | 应用信息 | 配置商户的应用信息，对接支付宝、微信等多个支付渠道 |
| 🚀  | 支付订单 | 查看用户发起的支付宝、微信等的【支付】订单     |
| 🚀  | 退款订单 | 查看用户发起的支付宝、微信等的【退款】订单     |

ps：核心功能已经实现，正在对接微信小程序中...

### 商城系统

正在开发中，大体计划如下：
* 2022 Q2 => 完成对 <https://github.com/YunaiV/onemall> 的迁移，作为 onemall 的 Spring Boot 单体版本。
* 2022 Q4 => 完成对 <https://github.com/YunaiV/onemall>> 的重构，作为 onemall 的 Spring Cloud 微服务版本。

### 会员中心

正在开发中，大体计划如下：
* 2021 Q1 =》完成对 <https://github.com/YunaiV/onemall> 的迁移 

### 基础设施

|     | 功能       | 描述                                           |
|-----|----------|----------------------------------------------|
| 🚀  | 代码生成     | 前后端代码的生成（Java、Vue、SQL、单元测试），支持 CRUD 下载       |
| 🚀  | 系统接口     | 基于 Swagger 自动生成相关的 RESTful API 接口文档          |
| 🚀  | 数据库文档    | 基于 Screw 自动生成数据库文档，支持导出 Word、HTML、MD 格式      |
|     | 表单构建     | 拖动表单元素生成相应的 HTML 代码，支持导出 JSON、Vue 文件         |
| 🚀  | 配置管理     | 对系统动态配置常用参数，支持 SpringBoot 加载                 |
| ⭐️  | 定时任务     | 在线（添加、修改、删除)任务调度包含执行结果日志                     |
| 🚀  | 文件服务     | 支持将文件存储到 S3（MinIO、阿里云、腾讯云、七牛云）、本地、FTP、数据库等      | 
| 🚀  | API 日志   | 包括 RESTful API 访问日志、异常日志两部分，方便排查 API 相关的问题   |
|     | MySQL 监控 | 监视当前系统数据库连接池状态，可进行分析SQL找出系统性能瓶颈              |
|     | Redis 监控 | 监控 Redis 数据库的使用情况，使用的 Redis Key 管理           |
| 🚀  | 消息队列     | 基于 Redis 实现消息队列，Stream 提供集群消费，Pub/Sub 提供广播消费 |
| 🚀  | Java 监控  | 基于 Spring Boot Admin 实现 Java 应用的监控           |
| 🚀  | 链路追踪     | 接入 SkyWalking 组件，实现链路追踪                      |
| 🚀  | 日志中心     | 接入 SkyWalking 组件，实现日志中心                      |
| 🚀  | 分布式锁     | 基于 Redis 实现分布式锁，满足并发场景                       |
| 🚀  | 幂等组件     | 基于 Redis 实现幂等组件，解决重复请求问题                     |
| 🚀  | 服务保障     | 基于 Resilience4j 实现服务的稳定性，包括限流、熔断等功能          |
| 🚀  | 日志服务     | 轻量级日志中心，查看远程服务器的日志                           |
| 🚀  | 单元测试     | 基于 JUnit + Mockito 实现单元测试，保证功能的正确性、代码的质量等    |

## 🐨 技术栈

| 项目                    | 说明                 |
|-----------------------|--------------------|
| `yudao-dependencies`  | Maven 依赖版本管理       |
| `yudao-framework`     | Java 框架拓展          |
| `yudao-server`        | 管理后台 + 用户 APP 的服务端 |
| `yudao-admin-ui`      | 管理后台的 UI 界面        |
| `yudao-user-ui`       | 用户 APP 的 UI 界面     |
| `yudao-module-system` | 系统功能的 Module 模块    |
| `yudao-module-member` | 会员中心的 Module 模块    |
| `yudao-module-infra`  | 基础设施的 Module 模块    |
| `yudao-module-tool`   | 研发工具的 Module 模块    |
| `yudao-module-bpm`    | 工作流程的 Module 模块    |
| `yudao-module-pay`    | 支付系统的 Module 模块    |

### 后端

| 框架                                                                                          | 说明               | 版本       | 学习指南                                                           |
|---------------------------------------------------------------------------------------------|------------------|----------|----------------------------------------------------------------|
| [Spring Boot](https://spring.io/projects/spring-boot)                                       | 应用开发框架           | 2.5.12   | [文档](https://github.com/YunaiV/SpringBoot-Labs)                |
| [MySQL](https://www.mysql.com/cn/)                                                          | 数据库服务器           | 5.7      |                                                                |
| [Druid](https://github.com/alibaba/druid)                                                   | JDBC 连接池、监控组件    | 1.2.8    | [文档](http://www.iocoder.cn/Spring-Boot/datasource-pool/?yudao) |
| [MyBatis Plus](https://mp.baomidou.com/)                                                    | MyBatis 增强工具包    | 3.5.1    | [文档](http://www.iocoder.cn/Spring-Boot/MyBatis/?yudao)         |
| [Dynamic Datasource](https://dynamic-datasource.com/)                                       | 动态数据源            | 3.5.0    | [文档](http://www.iocoder.cn/Spring-Boot/datasource-pool/?yudao) |
| [Redis](https://redis.io/)                                                                  | key-value 数据库    | 5.0      |                                                                |
| [Redisson](https://github.com/redisson/redisson)                                            | Redis 客户端        | 3.16.8   | [文档](http://www.iocoder.cn/Spring-Boot/Redis/?yudao)           |
| [Spring MVC](https://github.com/spring-projects/spring-framework/tree/master/spring-webmvc) | MVC 框架           | 5.3.16   | [文档](http://www.iocoder.cn/SpringMVC/MVC/?yudao)               |
| [Spring Security](https://github.com/spring-projects/spring-security)                       | Spring 安全框架      | 5.5.5    | [文档](http://www.iocoder.cn/Spring-Boot/Spring-Security/?yudao) |
| [Hibernate Validator](https://github.com/hibernate/hibernate-validator)                     | 参数校验组件           | 6.2.2    | [文档](http://www.iocoder.cn/Spring-Boot/Validation/?yudao)      |
| [Activiti](https://github.com/Activiti/Activiti)                                            | 工作流引擎            | 7.1.0.M6 | [文档](TODO)                                                     |
| [Quartz](https://github.com/quartz-scheduler)                                               | 任务调度组件           | 2.3.2    | [文档](http://www.iocoder.cn/Spring-Boot/Job/?yudao)             |
| [Knife4j](https://gitee.com/xiaoym/knife4j)                                                 | Swagger 增强 UI 实现 | 3.0.2    | [文档](http://www.iocoder.cn/Spring-Boot/Swagger/?yudao)         |
| [Resilience4j](https://github.com/resilience4j/resilience4j)                                | 服务保障组件           | 1.7.0    | [文档](http://www.iocoder.cn/Spring-Boot/Resilience4j/?yudao)    |
| [SkyWalking](https://skywalking.apache.org/)                                                | 分布式应用追踪系统        | 8.5.0    | [文档](http://www.iocoder.cn/Spring-Boot/SkyWalking/?yudao)      |
| [Spring Boot Admin](https://github.com/codecentric/spring-boot-admin)                       | Spring Boot 监控平台 | 2.4.2    | [文档](http://www.iocoder.cn/Spring-Boot/Admin/?yudao)           |
| [Jackson](https://github.com/FasterXML/jackson)                                             | JSON 工具库         | 2.12.6   |                                                                |
| [MapStruct](https://mapstruct.org/)                                                         | Java Bean 转换     | 1.4.1    | [文档](http://www.iocoder.cn/Spring-Boot/MapStruct/?yudao)       |
| [Lombok](https://projectlombok.org/)                                                        | 消除冗长的 Java 代码    | 1.16.14  | [文档](http://www.iocoder.cn/Spring-Boot/Lombok/?yudao)          |
| [JUnit](https://junit.org/junit5/)                                                          | Java 单元测试框架      | 5.7.2    | -                                                              |
| [Mockito](https://github.com/mockito/mockito)                                               | Java Mock 框架     | 3.9.0    | -                                                              |

### 前端

| 框架                                                                           | 说明            | 版本     |
|------------------------------------------------------------------------------|---------------|--------|
| [Vue](https://cn.vuejs.org/index.html)                                       | JavaScript 框架 | 2.6.12 |
| [Vue Element Admin](https://panjiachen.github.io/vue-element-admin-site/zh/) | 后台前端解决方案      | -      |

## 🐷 演示图

### 系统功能

| 模块       | biu                                                                | biu                                                              | biu                                                              |
|----------|--------------------------------------------------------------------|------------------------------------------------------------------|------------------------------------------------------------------|
| 登录 & 首页  | ![登录](https://static.iocoder.cn/images/ruoyi-vue-pro/登录.jpg)       | ![首页](https://static.iocoder.cn/images/ruoyi-vue-pro/首页.jpg)     | ![个人中心](https://static.iocoder.cn/images/ruoyi-vue-pro/个人中心.jpg) |
| 用户       | ![用户管理](https://static.iocoder.cn/images/ruoyi-vue-pro/用户管理.jpg)   | ![在线用户](https://static.iocoder.cn/images/ruoyi-vue-pro/在线用户.jpg) | -                                                                |
| 租户 & 套餐  | ![租户管理](https://static.iocoder.cn/images/ruoyi-vue-pro/租户管理.jpg)   | ![租户套餐](https://static.iocoder.cn/images/ruoyi-vue-pro/租户套餐.png) | -                                                                |
| 部门 & 岗位  | ![部门管理](https://static.iocoder.cn/images/ruoyi-vue-pro/部门管理.jpg)   | ![岗位管理](https://static.iocoder.cn/images/ruoyi-vue-pro/岗位管理.jpg) | -                                                                |
| 菜单 & 角色  | ![菜单管理](https://static.iocoder.cn/images/ruoyi-vue-pro/菜单管理.jpg)   | ![角色管理](https://static.iocoder.cn/images/ruoyi-vue-pro/角色管理.jpg) | -                                                                |
| 审计日志     | ![操作日志](https://static.iocoder.cn/images/ruoyi-vue-pro/操作日志.jpg)   | ![登录日志](https://static.iocoder.cn/images/ruoyi-vue-pro/登录日志.jpg) | -                                                                |
| 短信       | ![短信渠道](https://static.iocoder.cn/images/ruoyi-vue-pro/短信渠道.jpg)   | ![短信模板](https://static.iocoder.cn/images/ruoyi-vue-pro/短信模板.jpg) | ![短信日志](https://static.iocoder.cn/images/ruoyi-vue-pro/短信日志.jpg) |
| 字典       | ![字典类型](https://static.iocoder.cn/images/ruoyi-vue-pro/字典类型.jpg)   | ![字典数据](https://static.iocoder.cn/images/ruoyi-vue-pro/字典数据.jpg) | -                                                                |
| 错误码 & 通知 | ![错误码管理](https://static.iocoder.cn/images/ruoyi-vue-pro/错误码管理.jpg) | ![通知公告](https://static.iocoder.cn/images/ruoyi-vue-pro/通知公告.jpg) | -                                                                |

### 工作流程

| 模块      | biu                                                                    | biu                                                                    | biu                                                                    |
|---------|------------------------------------------------------------------------|------------------------------------------------------------------------|------------------------------------------------------------------------|
| 流程模型    | ![流程模型-列表](https://static.iocoder.cn/images/ruoyi-vue-pro/流程模型-列表.jpg) | ![流程模型-设计](https://static.iocoder.cn/images/ruoyi-vue-pro/流程模型-设计.jpg) | ![流程模型-定义](https://static.iocoder.cn/images/ruoyi-vue-pro/流程模型-定义.jpg) |
| 表单 & 分组 | ![流程表单](https://static.iocoder.cn/images/ruoyi-vue-pro/流程表单.jpg)       | ![用户分组](https://static.iocoder.cn/images/ruoyi-vue-pro/用户分组.jpg)       | -                                                                      |
| 我的流程    | ![我的流程-列表](https://static.iocoder.cn/images/ruoyi-vue-pro/我的流程-列表.jpg) | ![我的流程-发起](https://static.iocoder.cn/images/ruoyi-vue-pro/我的流程-发起.jpg) | ![我的流程-详情](https://static.iocoder.cn/images/ruoyi-vue-pro/我的流程-详情.jpg) |
| 待办 & 已办 | ![任务列表-审批](https://static.iocoder.cn/images/ruoyi-vue-pro/任务列表-审批.jpg) | ![任务列表-待办](https://static.iocoder.cn/images/ruoyi-vue-pro/任务列表-待办.jpg) | ![任务列表-已办](https://static.iocoder.cn/images/ruoyi-vue-pro/任务列表-已办.jpg) |
| OA 请假   | ![OA请假-列表](https://static.iocoder.cn/images/ruoyi-vue-pro/OA请假-列表.jpg) | ![OA请假-发起](https://static.iocoder.cn/images/ruoyi-vue-pro/OA请假-发起.jpg) | ![OA请假-详情](https://static.iocoder.cn/images/ruoyi-vue-pro/OA请假-详情.jpg) |

### 支付系统

| 模块      | biu                                                              | biu                                                                    | biu                                                                    |
|---------|------------------------------------------------------------------|------------------------------------------------------------------------|------------------------------------------------------------------------|
| 商家 & 应用 | ![商户信息](https://static.iocoder.cn/images/ruoyi-vue-pro/商户信息.jpg) | ![应用信息-列表](https://static.iocoder.cn/images/ruoyi-vue-pro/应用信息-列表.jpg) | ![应用信息-编辑](https://static.iocoder.cn/images/ruoyi-vue-pro/应用信息-编辑.jpg) |
| 支付 & 退款 | ![支付订单](https://static.iocoder.cn/images/ruoyi-vue-pro/支付订单.jpg) | ![退款订单](https://static.iocoder.cn/images/ruoyi-vue-pro/退款订单.jpg)       | ---                                                                    |

### 基础设施

| 模块            | biu                                                                  | biu                                                                | biu                                                              |
|---------------|----------------------------------------------------------------------|--------------------------------------------------------------------|------------------------------------------------------------------|
| 代码生成          | ![代码生成](https://static.iocoder.cn/images/ruoyi-vue-pro/代码生成.jpg)     | ![生成效果](https://static.iocoder.cn/images/ruoyi-vue-pro/生成效果.jpg)   | -                                                                |
| 文档            | ![系统接口](https://static.iocoder.cn/images/ruoyi-vue-pro/系统接口.jpg)     | ![数据库文档](https://static.iocoder.cn/images/ruoyi-vue-pro/数据库文档.jpg) | -                                                                |
| 文件 & 配置       | ![文件配置](https://static.iocoder.cn/images/ruoyi-vue-pro/文件配置.jpg) | ![文件管理](https://static.iocoder.cn/images/ruoyi-vue-pro/文件管理2.jpg)     | ![配置管理](https://static.iocoder.cn/images/ruoyi-vue-pro/配置管理.jpg)   |
| 定时任务          | ![定时任务](https://static.iocoder.cn/images/ruoyi-vue-pro/定时任务.jpg)     | ![任务日志](https://static.iocoder.cn/images/ruoyi-vue-pro/任务日志.jpg)   | -                                                                |
| API 日志        | ![访问日志](https://static.iocoder.cn/images/ruoyi-vue-pro/访问日志.jpg)     | ![错误日志](https://static.iocoder.cn/images/ruoyi-vue-pro/错误日志.jpg)   | -                                                                |
| MySQL & Redis | ![MySQL](https://static.iocoder.cn/images/ruoyi-vue-pro/MySQL.jpg)   | ![Redis](https://static.iocoder.cn/images/ruoyi-vue-pro/Redis.jpg) | -                                                                |
| 监控平台          | ![Java监控](https://static.iocoder.cn/images/ruoyi-vue-pro/Java监控.jpg) | ![链路追踪](https://static.iocoder.cn/images/ruoyi-vue-pro/链路追踪.jpg)   | ![日志中心](https://static.iocoder.cn/images/ruoyi-vue-pro/日志中心.jpg) |

