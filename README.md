**严肃声明：现在、未来都不会有商业版本，所有功能全部开源！**

**拒绝虚假开源，售卖商业版，程序员不骗程序员！！**

**「我喜欢写代码，乐此不疲」**  
**「我喜欢做开源，以此为乐」**

我 🐶 在上海艰苦奋斗，早中晚在 top3 大厂认真搬砖，夜里为开源做贡献。

如果这个项目让你有所收获，记得 Star 关注哦，这对我是非常不错的鼓励与支持。

## 🐶 新手必读

* 演示地址：<http://dashboard.yudao.iocoder.cn>
* 启动文档：<https://doc.iocoder.cn/quick-start/>
* 视频教程：<https://doc.iocoder.cn/video/>

## 🐯 平台简介

**芋道**，以开发者为中心，打造中国第一流的快速开发平台，全部开源，个人与企业可 100% 免费使用。

> 有任何问题，或者想要的功能，可以在 _Issues_ 中提给艿艿。
>
> 😜 给项目点点 Star 吧，这对我们真的很重要！

* 前端 Vue2 版本采用 [vue-element-admin](https://github.com/PanJiaChen/vue-element-admin)
* 前端 Vue3 版本采用 [vue-element-plus-admin](https://gitee.com/kailong110120130/vue-element-plus-admin)
* 后端采用 Spring Boot、MySQL + MyBatis Plus、Redis + Redisson
* 数据库可使用 MySQL、Oracle、PostgreSQL、SQL Server、MariaDB、国产达梦 DM、TiDB 等
* 权限认证使用 Spring Security & Token & Redis，支持多终端、多种用户的认证系统，支持 SSO 单点登录
* 支持加载动态权限菜单，按钮级别权限控制，本地缓存提升性能
* 支持 SaaS 多租户系统，可自定义每个租户的权限，提供透明化的多租户底层封装
* 工作流使用 Flowable，支持动态表单、在线设计流程、会签 / 或签、多种任务分配方式
* 高效率开发，使用代码生成器可以一键生成前后端代码 + 单元测试 + Swagger 接口文档 + Validator 参数校验
* 集成微信小程序、微信公众号、企业微信、钉钉等三方登陆，集成支付宝、微信等支付与退款
* 集成阿里云、腾讯云、云片等短信渠道，集成 MinIO、阿里云、腾讯云、七牛云等云存储服务
* 集成报表设计器，支持数据报表、图形报表、打印设计等

| 项目名                | 说明                     | 传说门                                                                                                                                 |
|--------------------|------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
| `ruoyi-vue-pro`    | Spring Boot 多模块        | **[Gitee](https://gitee.com/zhijiantianya/ruoyi-vue-pro)** &nbsp;&nbsp;&nbsp; [Github](https://github.com/YunaiV/ruoyi-vue-pro)     |
| `yudao-cloud`  | Spring Cloud 微服务       | **[Gitee](https://gitee.com/zhijiantianya/yudao-cloud)** &nbsp;&nbsp;&nbsp; [Github](https://github.com/YunaiV/yudao-cloud)         |
| `Spring-Boot-Labs` | Spring Boot & Cloud 入门 | **[Gitee](https://gitee.com/zhijiantianya/SpringBoot-Labs)** &nbsp;&nbsp;&nbsp; [Github](https://github.com/YunaiV/SpringBoot-Labs) |


## 🐼 内置功能

分成多种内置功能：
* 系统功能
* 基础设施
* 工作流程
* 支付系统
* 商城系统
* 数据报表

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
| 🚀  | 敏感词  | 配置系统敏感词，支持标签分组                    |
| 🚀  | 应用管理  | 管理 SSO 单点登录的应用，支持多种 OAuth2 授权方式 |

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

### 数据报表

|     | 功能       | 描述                                           |
|-----|----------|----------------------------------------------|
| 🚀  | 报表设计器     | 支持数据报表、图形报表、打印设计等       |
| 🚀  | 大屏设计器     | 建设中... 拖拽式实现可视化数据大屏          |

### 商城系统

建设中...

![功能图](http://static.iocoder.cn/mall%20%E5%8A%9F%E8%83%BD%E5%9B%BE-min.png)

![GIF 图-耐心等待](https://raw.githubusercontent.com/YunaiV/Blog/master/Mall/onemall-admin-min.gif)

![GIF 图-耐心等待](https://raw.githubusercontent.com/YunaiV/Blog/master/Mall/onemall-h5-min.gif)

### 会员中心

和「商城系统」一起开发

## 🐨 技术栈

| 项目                    | 说明                 |
|-----------------------|--------------------|
| `yudao-dependencies`  | Maven 依赖版本管理       |
| `yudao-framework`     | Java 框架拓展          |
| `yudao-server`        | 管理后台 + 用户 APP 的服务端 |
| `yudao-ui-admin`      | Vue2 管理后台的 UI 界面        |
| `yudao-ui-admin-vue3`      | Vue3 管理后台的 UI 界面        |
| `yudao-ui-app`       | 用户 APP 的 UI 界面     |
| `yudao-module-system` | 系统功能的 Module 模块    |
| `yudao-module-member` | 会员中心的 Module 模块    |
| `yudao-module-infra`  | 基础设施的 Module 模块    |
| `yudao-module-tool`   | 研发工具的 Module 模块    |
| `yudao-module-bpm`    | 工作流程的 Module 模块    |
| `yudao-module-pay`    | 支付系统的 Module 模块    |

### 后端

| 框架                                                                                         | 说明                   | 版本      | 学习指南                                                           |
|---------------------------------------------------------------------------------------------|-----------------------|-----------|----------------------------------------------------------------|
| [Spring Boot](https://spring.io/projects/spring-boot)                                       | 应用开发框架             | 2.6.10    | [文档](https://github.com/YunaiV/SpringBoot-Labs)                |
| [MySQL](https://www.mysql.com/cn/)                                                          | 数据库服务器             | 5.7      |                                                                |
| [Druid](https://github.com/alibaba/druid)                                                   | JDBC 连接池、监控组件     | 1.2.11    | [文档](http://www.iocoder.cn/Spring-Boot/datasource-pool/?yudao) |
| [MyBatis Plus](https://mp.baomidou.com/)                                                    | MyBatis 增强工具包       | 3.5.2    | [文档](http://www.iocoder.cn/Spring-Boot/MyBatis/?yudao)         |
| [Dynamic Datasource](https://dynamic-datasource.com/)                                       | 动态数据源               | 3.5.0    | [文档](http://www.iocoder.cn/Spring-Boot/datasource-pool/?yudao) |
| [Redis](https://redis.io/)                                                                  | key-value 数据库        | 5.0      |                                                                |
| [Redisson](https://github.com/redisson/redisson)                                            | Redis 客户端            | 3.17.4   | [文档](http://www.iocoder.cn/Spring-Boot/Redis/?yudao)           |
| [Spring MVC](https://github.com/spring-projects/spring-framework/tree/master/spring-webmvc) | MVC 框架               | 5.3.20    | [文档](http://www.iocoder.cn/SpringMVC/MVC/?yudao)               |
| [Spring Security](https://github.com/spring-projects/spring-security)                       | Spring 安全框架         | 5.6.5    | [文档](http://www.iocoder.cn/Spring-Boot/Spring-Security/?yudao) |
| [Hibernate Validator](https://github.com/hibernate/hibernate-validator)                     | 参数校验组件             | 6.2.3    | [文档](http://www.iocoder.cn/Spring-Boot/Validation/?yudao)      |
| [Flowable](https://github.com/flowable/flowable-engine)                                     | 工作流引擎               | 6.7.0    | [文档](https://doc.iocoder.cn/bpm/)                                                     |
| [Quartz](https://github.com/quartz-scheduler)                                               | 任务调度组件             | 2.3.2    | [文档](http://www.iocoder.cn/Spring-Boot/Job/?yudao)             |
| [Knife4j](https://gitee.com/xiaoym/knife4j)                                                 | Swagger 增强 UI 实现    | 3.0.3    | [文档](http://www.iocoder.cn/Spring-Boot/Swagger/?yudao)         |
| [Resilience4j](https://github.com/resilience4j/resilience4j)                                | 服务保障组件             | 1.7.1    | [文档](http://www.iocoder.cn/Spring-Boot/Resilience4j/?yudao)    |
| [SkyWalking](https://skywalking.apache.org/)                                                | 分布式应用追踪系统        | 8.5.0    | [文档](http://www.iocoder.cn/Spring-Boot/SkyWalking/?yudao)      |
| [Spring Boot Admin](https://github.com/codecentric/spring-boot-admin)                       | Spring Boot 监控平台    | 2.6.7    | [文档](http://www.iocoder.cn/Spring-Boot/Admin/?yudao)           |
| [Jackson](https://github.com/FasterXML/jackson)                                             | JSON 工具库             | 2.13.3   |                                                                |
| [MapStruct](https://mapstruct.org/)                                                         | Java Bean 转换         | 1.4.1    | [文档](http://www.iocoder.cn/Spring-Boot/MapStruct/?yudao)       |
| [Lombok](https://projectlombok.org/)                                                        | 消除冗长的 Java 代码     | 1.16.14  | [文档](http://www.iocoder.cn/Spring-Boot/Lombok/?yudao)          |
| [JUnit](https://junit.org/junit5/)                                                          | Java 单元测试框架        | 5.8.2    | -                                                              |
| [Mockito](https://github.com/mockito/mockito)                                               | Java Mock 框架         | 4.0.0    | -                                                              |

### Vue2 前端

| 框架                                                                           | 说明            | 版本     |
|------------------------------------------------------------------------------|---------------|--------|
| [Vue](https://cn.vuejs.org/index.html)                                       | JavaScript 框架 | 2.6.12 |
| [Vue Element Admin](https://panjiachen.github.io/vue-element-admin-site/zh/) | 后台前端解决方案      | -      |

### Vue3 前端

| 框架                                                                  | 说明               | 版本     |
|----------------------------------------------------------------------|------------------|--------|
| [Vue](https://staging-cn.vuejs.org/)                                 | Vue 框架           | 3.2.37 |
| [Vite](https://cn.vitejs.dev//)                                      | 开发与构建工具          | 3.0.3  |
| [Element Plus](https://element-plus.org/zh-CN/)                      | Element Plus     | 2.2.11 |
| [TypeScript](https://www.typescriptlang.org/docs/)                   | TypeScript       | 4.7.4  |
| [pinia](https://pinia.vuejs.org/)                                    | Vue 存储库 替代 vuex5 | 2.0.17 |
| [vue-i18n](https://kazupon.github.io/vue-i18n/zh/introduction.html/) | 国际化              | 9.1.10 |
| [windicss](https://cn.windicss.org/)                                 | 下一代工具优先的 CSS 框架  | 3.5.6  |
| [iconify](https://icon-sets.iconify.design/)                         | 在线图标库            | 2.2.1  |

## 🐷 演示图

### 系统功能

| 模块       | biu                                                                | biu                                                              | biu                                                              |
|----------|--------------------------------------------------------------------|------------------------------------------------------------------|------------------------------------------------------------------|
| 登录 & 首页  | ![登录](https://static.iocoder.cn/images/ruoyi-vue-pro/登录.jpg?imageView2/2/format/webp/w/1280)       | ![首页](https://static.iocoder.cn/images/ruoyi-vue-pro/首页.jpg?imageView2/2/format/webp/w/1280)     | ![个人中心](https://static.iocoder.cn/images/ruoyi-vue-pro/个人中心.jpg?imageView2/2/format/webp/w/1280) |
| 用户 & 应用      | ![用户管理](https://static.iocoder.cn/images/ruoyi-vue-pro/用户管理.jpg?imageView2/2/format/webp/w/1280)   | ![令牌管理](https://static.iocoder.cn/images/ruoyi-vue-pro/令牌管理.jpg?imageView2/2/format/webp/w/1280) | ![应用管理](https://static.iocoder.cn/images/ruoyi-vue-pro/应用管理.jpg?imageView2/2/format/webp/w/1280)                                                                |
| 租户 & 套餐  | ![租户管理](https://static.iocoder.cn/images/ruoyi-vue-pro/租户管理.jpg?imageView2/2/format/webp/w/1280)   | ![租户套餐](https://static.iocoder.cn/images/ruoyi-vue-pro/租户套餐.png) | -                                                                |
| 部门 & 岗位  | ![部门管理](https://static.iocoder.cn/images/ruoyi-vue-pro/部门管理.jpg?imageView2/2/format/webp/w/1280)   | ![岗位管理](https://static.iocoder.cn/images/ruoyi-vue-pro/岗位管理.jpg?imageView2/2/format/webp/w/1280) | -                                                                |
| 菜单 & 角色  | ![菜单管理](https://static.iocoder.cn/images/ruoyi-vue-pro/菜单管理.jpg?imageView2/2/format/webp/w/1280)   | ![角色管理](https://static.iocoder.cn/images/ruoyi-vue-pro/角色管理.jpg?imageView2/2/format/webp/w/1280) | -                                                                |
| 审计日志     | ![操作日志](https://static.iocoder.cn/images/ruoyi-vue-pro/操作日志.jpg?imageView2/2/format/webp/w/1280)   | ![登录日志](https://static.iocoder.cn/images/ruoyi-vue-pro/登录日志.jpg?imageView2/2/format/webp/w/1280) | -                                                                |
| 短信       | ![短信渠道](https://static.iocoder.cn/images/ruoyi-vue-pro/短信渠道.jpg?imageView2/2/format/webp/w/1280)   | ![短信模板](https://static.iocoder.cn/images/ruoyi-vue-pro/短信模板.jpg?imageView2/2/format/webp/w/1280) | ![短信日志](https://static.iocoder.cn/images/ruoyi-vue-pro/短信日志.jpg?imageView2/2/format/webp/w/1280) |
| 字典 & 敏感词      | ![字典类型](https://static.iocoder.cn/images/ruoyi-vue-pro/字典类型.jpg?imageView2/2/format/webp/w/1280)   | ![字典数据](https://static.iocoder.cn/images/ruoyi-vue-pro/字典数据.jpg?imageView2/2/format/webp/w/1280) | ![敏感词](https://static.iocoder.cn/images/ruoyi-vue-pro/敏感词.jpg?imageView2/2/format/webp/w/1280)                                                                |
| 错误码 & 通知 | ![错误码管理](https://static.iocoder.cn/images/ruoyi-vue-pro/错误码管理.jpg?imageView2/2/format/webp/w/1280) | ![通知公告](https://static.iocoder.cn/images/ruoyi-vue-pro/通知公告.jpg?imageView2/2/format/webp/w/1280) | -                                                                |

### 工作流程

| 模块      | biu                                                                    | biu                                                                    | biu                                                                    |
|---------|------------------------------------------------------------------------|------------------------------------------------------------------------|------------------------------------------------------------------------|
| 流程模型    | ![流程模型-列表](https://static.iocoder.cn/images/ruoyi-vue-pro/流程模型-列表.jpg?imageView2/2/format/webp/w/1280) | ![流程模型-设计](https://static.iocoder.cn/images/ruoyi-vue-pro/流程模型-设计.jpg?imageView2/2/format/webp/w/1280) | ![流程模型-定义](https://static.iocoder.cn/images/ruoyi-vue-pro/流程模型-定义.jpg?imageView2/2/format/webp/w/1280) |
| 表单 & 分组 | ![流程表单](https://static.iocoder.cn/images/ruoyi-vue-pro/流程表单.jpg?imageView2/2/format/webp/w/1280)       | ![用户分组](https://static.iocoder.cn/images/ruoyi-vue-pro/用户分组.jpg?imageView2/2/format/webp/w/1280)       | -                                                                      |
| 我的流程    | ![我的流程-列表](https://static.iocoder.cn/images/ruoyi-vue-pro/我的流程-列表.jpg?imageView2/2/format/webp/w/1280) | ![我的流程-发起](https://static.iocoder.cn/images/ruoyi-vue-pro/我的流程-发起.jpg?imageView2/2/format/webp/w/1280) | ![我的流程-详情](https://static.iocoder.cn/images/ruoyi-vue-pro/我的流程-详情.jpg?imageView2/2/format/webp/w/1280) |
| 待办 & 已办 | ![任务列表-审批](https://static.iocoder.cn/images/ruoyi-vue-pro/任务列表-审批.jpg?imageView2/2/format/webp/w/1280) | ![任务列表-待办](https://static.iocoder.cn/images/ruoyi-vue-pro/任务列表-待办.jpg?imageView2/2/format/webp/w/1280) | ![任务列表-已办](https://static.iocoder.cn/images/ruoyi-vue-pro/任务列表-已办.jpg?imageView2/2/format/webp/w/1280) |
| OA 请假   | ![OA请假-列表](https://static.iocoder.cn/images/ruoyi-vue-pro/OA请假-列表.jpg?imageView2/2/format/webp/w/1280) | ![OA请假-发起](https://static.iocoder.cn/images/ruoyi-vue-pro/OA请假-发起.jpg?imageView2/2/format/webp/w/1280) | ![OA请假-详情](https://static.iocoder.cn/images/ruoyi-vue-pro/OA请假-详情.jpg?imageView2/2/format/webp/w/1280) |

### 基础设施

| 模块            | biu                                                                  | biu                                                                | biu                                                              |
|---------------|----------------------------------------------------------------------|--------------------------------------------------------------------|------------------------------------------------------------------|
| 代码生成          | ![代码生成](https://static.iocoder.cn/images/ruoyi-vue-pro/代码生成.jpg?imageView2/2/format/webp/w/1280)     | ![生成效果](https://static.iocoder.cn/images/ruoyi-vue-pro/生成效果.jpg?imageView2/2/format/webp/w/1280)   | -                                                                |
| 文档            | ![系统接口](https://static.iocoder.cn/images/ruoyi-vue-pro/系统接口.jpg?imageView2/2/format/webp/w/1280)     | ![数据库文档](https://static.iocoder.cn/images/ruoyi-vue-pro/数据库文档.jpg?imageView2/2/format/webp/w/1280) | -                                                                |
| 文件 & 配置       | ![文件配置](https://static.iocoder.cn/images/ruoyi-vue-pro/文件配置.jpg?imageView2/2/format/webp/w/1280) | ![文件管理](https://static.iocoder.cn/images/ruoyi-vue-pro/文件管理2.jpg?imageView2/2/format/webp/w/1280)     | ![配置管理](https://static.iocoder.cn/images/ruoyi-vue-pro/配置管理.jpg?imageView2/2/format/webp/w/1280)   |
| 定时任务          | ![定时任务](https://static.iocoder.cn/images/ruoyi-vue-pro/定时任务.jpg?imageView2/2/format/webp/w/1280)     | ![任务日志](https://static.iocoder.cn/images/ruoyi-vue-pro/任务日志.jpg?imageView2/2/format/webp/w/1280)   | -                                                                |
| API 日志        | ![访问日志](https://static.iocoder.cn/images/ruoyi-vue-pro/访问日志.jpg?imageView2/2/format/webp/w/1280)     | ![错误日志](https://static.iocoder.cn/images/ruoyi-vue-pro/错误日志.jpg?imageView2/2/format/webp/w/1280)   | -                                                                |
| MySQL & Redis | ![MySQL](https://static.iocoder.cn/images/ruoyi-vue-pro/MySQL.jpg?imageView2/2/format/webp/w/1280)   | ![Redis](https://static.iocoder.cn/images/ruoyi-vue-pro/Redis.jpg?imageView2/2/format/webp/w/1280) | -                                                                |
| 监控平台          | ![Java监控](https://static.iocoder.cn/images/ruoyi-vue-pro/Java监控.jpg?imageView2/2/format/webp/w/1280) | ![链路追踪](https://static.iocoder.cn/images/ruoyi-vue-pro/链路追踪.jpg?imageView2/2/format/webp/w/1280)   | ![日志中心](https://static.iocoder.cn/images/ruoyi-vue-pro/日志中心.jpg?imageView2/2/format/webp/w/1280) |

### 支付系统

| 模块      | biu                                                              | biu                                                                    | biu                                                                    |
|---------|------------------------------------------------------------------|------------------------------------------------------------------------|------------------------------------------------------------------------|
| 商家 & 应用 | ![商户信息](https://static.iocoder.cn/images/ruoyi-vue-pro/商户信息.jpg?imageView2/2/format/webp/w/1280) | ![应用信息-列表](https://static.iocoder.cn/images/ruoyi-vue-pro/应用信息-列表.jpg?imageView2/2/format/webp/w/1280) | ![应用信息-编辑](https://static.iocoder.cn/images/ruoyi-vue-pro/应用信息-编辑.jpg?imageView2/2/format/webp/w/1280) |
| 支付 & 退款 | ![支付订单](https://static.iocoder.cn/images/ruoyi-vue-pro/支付订单.jpg?imageView2/2/format/webp/w/1280) | ![退款订单](https://static.iocoder.cn/images/ruoyi-vue-pro/退款订单.jpg?imageView2/2/format/webp/w/1280)       | ---                                                                    |

### 数据报表

| 模块      | biu                                                              | biu                                                                    | biu                                                                    |
|---------|------------------------------------------------------------------|------------------------------------------------------------------------|------------------------------------------------------------------------|
| 报表设计器 | ![数据报表](https://static.iocoder.cn/images/ruoyi-vue-pro/报表设计器-数据报表.jpg?imageView2/2/format/webp/w/1280) | ![图形报表](https://static.iocoder.cn/images/ruoyi-vue-pro/报表设计器-图形报表.jpg?imageView2/2/format/webp/w/1280) | ![报表设计器-打印设计](https://static.iocoder.cn/images/ruoyi-vue-pro/报表设计器-打印设计.jpg?imageView2/2/format/webp/w/1280) |
