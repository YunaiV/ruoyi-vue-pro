<div align="center">

# 🚀 芋道快速开发平台

<p align="center">
 <img src="https://img.shields.io/badge/Spring%20Boot-3.4.5-blue.svg" alt="Spring Boot">
 <img src="https://img.shields.io/badge/Vue-3.2-blue.svg" alt="Vue">
 <img src="https://img.shields.io/badge/Java-17/21-red.svg" alt="Java">
 <img src="https://img.shields.io/github/license/YunaiV/ruoyi-vue-pro" alt="License" />
 <img src="https://img.shields.io/github/stars/YunaiV/ruoyi-vue-pro?style=social" alt="GitHub stars">
</p>

**严肃声明：现在、未来都不会有商业版本，所有代码全部开源！**

> **「我喜欢写代码，乐此不疲」**  
> **「我喜欢做开源，以此为乐」**

**以开发者为中心，打造中国第一流的快速开发平台，全部开源，个人与企业可 100% 免费使用。**

</div>

---

## 📖 目录

- [🚀 快速开始](#-快速开始)
- [🌟 项目特色](#-项目特色)
- [🏗️ 版本说明](#️-版本说明)
- [🎯 项目关系](#-项目关系)
- [💡 核心功能](#-核心功能)
  - [系统功能](#系统功能)
  - [工作流程](#工作流程)
  - [支付系统](#支付系统)
  - [基础设施](#基础设施)
  - [数据报表](#数据报表)
  - [微信公众号](#微信公众号)
  - [商城系统](#商城系统)
  - [会员中心](#会员中心)
  - [ERP 系统](#erp-系统)
  - [CRM 系统](#crm-系统)
  - [AI 大模型](#ai-大模型)
- [⚡ 技术栈](#-技术栈)
- [📸 演示图](#-演示图)
- [📄 开源协议](#-开源协议)
- [🤝 项目外包](#-项目外包)

---

## 🚀 快速开始

### 在线体验

| 版本 | 演示地址 | 账号密码 |
|------|----------|----------|
| **Vue3 + Element Plus** | [dashboard-vue3.yudao.iocoder.cn](http://dashboard-vue3.yudao.iocoder.cn) | admin / admin123 |
| **Vue3 + Vben(Ant Design)** | [dashboard-vben.yudao.iocoder.cn](http://dashboard-vben.yudao.iocoder.cn) | admin / admin123 |
| **Vue2 + Element UI** | [dashboard.yudao.iocoder.cn](http://dashboard.yudao.iocoder.cn) | admin / admin123 |

### 快速启动

1. **📚 查看文档**：[启动文档](https://doc.iocoder.cn/quick-start/)
2. **🎥 观看视频**：[视频教程](https://doc.iocoder.cn/video/)
3. **💬 加入交流群**：如有问题可在 Issues 中提问

---

## 🌟 项目特色

![架构图](/.image/common/ruoyi-vue-pro-architecture.png)

### 🔥 核心亮点

- **🎨 多前端支持**：Vue3 + Element Plus / Vben，Vue2 + Element UI
- **📱 移动端适配**：uni-app 一份代码，APP/小程序/H5 全端覆盖
- **🏢 企业级架构**：Spring Boot 多模块 + MySQL + Redis + Redisson
- **🔒 安全可靠**：Spring Security + Token + Redis 多端认证
- **🏗️ 多租户支持**：SaaS 多租户，透明化底层封装
- **⚡ 工作流引擎**：Flowable 驱动，支持动态表单和在线设计
- **🚀 高效开发**：代码生成器一键生成前后端代码
- **🌐 实时通信**：WebSocket 集群支持
- **💰 支付集成**：支付宝、微信等主流支付方式
- **☁️ 云服务集成**：阿里云、腾讯云等短信/存储服务

### 🎯 适用场景

- **企业管理系统**：OA、ERP、CRM、HIS 等
- **电商平台**：商城、SCRM、会员系统
- **政务系统**：审批流程、数据报表
- **创业项目**：快速MVP验证

---

## 🏗️ 版本说明

| 版本类型 | 分支 | JDK 版本 | Spring Boot | 功能说明 |
|----------|------|----------|-------------|----------|
| **完整版** | [master](https://gitee.com/zhijiantianya/ruoyi-vue-pro/tree/master/) | JDK 8 | Spring Boot 2.7 | 包含所有业务模块 |
| **完整版** | [master-jdk17](https://gitee.com/zhijiantianya/ruoyi-vue-pro/tree/master-jdk17/) | JDK 17/21 | Spring Boot 3.2 | 包含所有业务模块 |
| **精简版** | [yudao-boot-mini](https://gitee.com/yudaocode/yudao-boot-mini) | JDK 8/17/21 | Spring Boot 2.7/3.2 | 仅核心功能模块 |

> 💡 **迁移提示**：参考 [迁移文档](https://doc.iocoder.cn/migrate-module/)，5-10分钟即可从完整版迁移到精简版

---

## 🎯 项目关系

![架构演进](/.image/common/yudao-roadmap.png)

### 后端项目

| 项目 | Star | 架构特点 |
|------|------|----------|
| [ruoyi-vue-pro](https://gitee.com/zhijiantianya/ruoyi-vue-pro) | [![Gitee star](https://gitee.com/zhijiantianya/ruoyi-vue-pro/badge/star.svg?theme=white)](https://gitee.com/zhijiantianya/ruoyi-vue-pro) | Spring Boot 多模块架构 |
| [yudao-cloud](https://gitee.com/zhijiantianya/yudao-cloud) | [![Gitee star](https://gitee.com/zhijiantianya/yudao-cloud/badge/star.svg?theme=white)](https://gitee.com/zhijiantianya/yudao-cloud) | Spring Cloud 微服务架构 |
| [Spring-Boot-Labs](https://gitee.com/yudaocode/SpringBoot-Labs) | [![Gitee star](https://gitee.com/yudaocode/SpringBoot-Labs/badge/star.svg?theme=white)](https://gitee.com/yudaocode/SpringBoot-Labs) | Spring Boot & Cloud 学习专栏 |

### 前端项目

| 项目 | Star | 技术栈 |
|------|------|--------|
| [yudao-ui-admin-vue3](https://gitee.com/yudaocode/yudao-ui-admin-vue3) | [![Gitee star](https://gitee.com/yudaocode/yudao-ui-admin-vue3/badge/star.svg?theme=white)](https://gitee.com/yudaocode/yudao-ui-admin-vue3) | Vue3 + Element Plus |
| [yudao-ui-admin-vben](https://gitee.com/yudaocode/yudao-ui-admin-vben) | [![Gitee star](https://gitee.com/yudaocode/yudao-ui-admin-vben/badge/star.svg?theme=white)](https://gitee.com/yudaocode/yudao-ui-admin-vben) | Vue3 + Vben(Ant Design) |
| [yudao-mall-uniapp](https://gitee.com/yudaocode/yudao-mall-uniapp) | [![Gitee star](https://gitee.com/yudaocode/yudao-mall-uniapp/badge/star.svg?theme=white)](https://gitee.com/yudaocode/yudao-mall-uniapp) | uni-app 商城小程序 |
| [yudao-ui-go-view](https://gitee.com/yudaocode/yudao-ui-go-view) | [![Gitee star](https://gitee.com/yudaocode/yudao-ui-go-view/badge/star.svg?theme=white)](https://gitee.com/yudaocode/yudao-ui-go-view) | Vue3 + Naive UI 大屏报表 |

---

## 💡 核心功能

![功能分层](/.image/common/ruoyi-vue-pro-biz.png)

### 系统功能

基础的系统管理功能，支持用户、角色、权限、部门等核心管理。

<details>
<summary>📋 查看详细功能列表</summary>

| 功能模块 | 功能描述 | 特色标记 |
|----------|----------|----------|
| 用户管理 | 用户是系统操作者，该功能主要完成系统用户配置 | |
| 在线用户 | 当前系统中活跃用户状态监控，支持手动踢下线 | ⭐️ |
| 角色管理 | 角色菜单权限分配、设置角色按机构进行数据范围权限划分 | |
| 菜单管理 | 配置系统菜单、操作权限、按钮权限标识等 | |
| 部门管理 | 配置系统组织机构（公司、部门、小组），树结构展现 | |
| 岗位管理 | 配置系统用户所属担任职务 | |
| 租户管理 | 配置系统租户，支持 SaaS 场景下的多租户功能 | 🚀 |
| 租户套餐 | 配置租户套餐，自定每个租户的菜单、操作、按钮的权限 | 🚀 |
| 字典管理 | 对系统中经常使用的一些较为固定的数据进行维护 | |
| 短信管理 | 短信渠道、短息模板、短信日志，对接主流短信平台 | 🚀 |
| 邮件管理 | 邮箱账号、邮件模版、邮件发送日志 | 🚀 |
| 站内信 | 系统内的消息通知，提供站内信模版、站内信消息 | 🚀 |
| 操作日志 | 系统正常操作日志记录和查询，集成 Swagger 生成日志内容 | 🚀 |
| 登录日志 | 系统登录日志记录查询，包含登录异常 | ⭐️ |
| 错误码管理 | 系统所有错误码的管理，可在线修改错误提示 | 🚀 |
| 通知公告 | 系统通知公告信息发布维护 | |
| 敏感词 | 配置系统敏感词，支持标签分组 | 🚀 |
| 应用管理 | 管理 SSO 单点登录的应用，支持多种 OAuth2 授权方式 | 🚀 |

</details>

![功能图](/.image/common/system-feature.png)

### 工作流程

基于 Flowable 构建，支持信创数据库，满足中国特色流程操作。

<details>
<summary>🔄 查看工作流功能详情</summary>

**双设计器支持**

| BPMN 设计器 | 钉钉/飞书设计器 |
|-------------|----------------|
| ![BPMN设计器](/.image/工作流设计器-bpmn.jpg) | ![Simple设计器](/.image/工作流设计器-simple.jpg) |

**核心功能一览**

| 功能 | 描述 | 状态 |
|------|------|------|
| **SIMPLE 设计器** | 仿钉钉/飞书设计器，10分钟快速完成审批流程配置 | ✅ |
| **BPMN 设计器** | 基于 BPMN 标准，适配复杂业务场景 | ✅ |
| **会签/或签** | 同一节点多人审批，支持全部同意或任一同意 | ✅ |
| **依次审批** | 顺序会签，按顺序依次审批 | ✅ |
| **驳回/转办/委派** | 灵活的流程控制机制 | ✅ |
| **加签/减签** | 动态增减审批人 | ✅ |
| **表单权限** | 每个节点可配置字段的读写权限 | ✅ |
| **超时审批** | 自动超时处理机制 | ✅ |
| **条件分支** | 根据条件智能路由 | ✅ |
| **并行分支** | 多分支并行执行 | ✅ |
| **子流程** | 支持主子流程嵌套 | ✅ |

</details>

![功能图](/.image/common/bpm-feature.png)

### 支付系统

集成主流支付渠道，提供完整的支付解决方案。

| 功能模块 | 功能描述 |
|----------|----------|
| 应用信息 | 配置商户的应用信息，对接支付宝、微信等多个支付渠道 |
| 支付订单 | 查看用户发起的支付宝、微信等的【支付】订单 |
| 退款订单 | 查看用户发起的支付宝、微信等的【退款】订单 |
| 回调通知 | 查看支付回调业务的【支付】【退款】的通知结果 |

### 基础设施

高效的开发工具集，提升开发效率。

<details>
<summary>🛠️ 查看基础设施功能</summary>

| 功能模块 | 功能描述 |
|----------|----------|
| 代码生成 | 前后端代码的生成（Java、Vue、SQL、单元测试），支持 CRUD 下载 |
| 系统接口 | 基于 Swagger 自动生成相关的 RESTful API 接口文档 |
| 数据库文档 | 基于 Screw 自动生成数据库文档，支持导出 Word、HTML、MD 格式 |
| 表单构建 | 拖动表单元素生成相应的 HTML 代码 |
| 配置管理 | 对系统动态配置常用参数，支持 SpringBoot 加载 |
| 定时任务 | 在线任务调度包含执行结果日志 |
| 文件服务 | 支持 S3、本地、FTP、数据库等多种存储方式 |
| WebSocket | 提供 WebSocket 接入示例，支持一对一、一对多发送 |
| API 日志 | RESTful API 访问日志、异常日志 |
| 监控中心 | MySQL、Redis、Java 应用监控 |
| 消息队列 | 基于 Redis 实现消息队列 |
| 链路追踪 | 接入 SkyWalking 组件 |
| 服务保障 | 分布式锁、幂等、限流功能 |
| 单元测试 | 基于 JUnit + Mockito 实现单元测试 |

</details>

![功能图](/.image/common/infra-feature.png)

### 数据报表

拖拽式报表设计，零代码生成酷炫报表。

| 功能模块 | 功能描述 |
|----------|----------|
| 报表设计器 | 支持数据报表、图形报表、打印设计等 |
| 大屏设计器 | 拖拽生成数据大屏，内置几十种图表组件 |

### 微信公众号

完整的微信公众号管理功能。

<details>
<summary>📱 查看微信公众号功能</summary>

| 功能模块 | 功能描述 |
|----------|----------|
| 账号管理 | 配置接入的微信公众号，可支持多个公众号 |
| 数据统计 | 统计公众号的用户增减、累计用户、消息概况等数据 |
| 粉丝管理 | 查看粉丝列表，可对粉丝进行同步、打标签等操作 |
| 消息管理 | 查看粉丝发送的消息列表，可主动回复粉丝消息 |
| 自动回复 | 自动回复粉丝发送的消息，支持关注回复、消息回复、关键字回复 |
| 标签管理 | 对公众号的标签进行创建、查询、修改、删除等操作 |
| 菜单管理 | 自定义公众号的菜单，也可以从公众号同步菜单 |
| 素材管理 | 管理公众号的图片、语音、视频等素材 |
| 图文草稿箱 | 新增常用的图文素材到草稿箱，可发布到公众号 |

</details>

### 商城系统

功能完整的电商解决方案。

**演示地址**：[商城预览](https://doc.iocoder.cn/mall-preview/)

![功能图](/.image/common/mall-feature.png)
![预览图](/.image/common/mall-preview.png)

### 会员中心

完善的会员管理体系。

| 功能模块 | 功能描述 |
|----------|----------|
| 会员管理 | 会员是 C 端的消费者，该功能用于会员的搜索与管理 |
| 会员标签 | 对会员的标签进行创建、查询、修改、删除等操作 |
| 会员等级 | 对会员的等级、成长值进行管理，可用于订单折扣等会员权益 |
| 会员分组 | 对会员进行分组，用于用户画像、内容推送等运营手段 |
| 积分签到 | 回馈给签到、消费等行为的积分，会员可订单抵现、积分兑换等 |

### ERP 系统

企业资源规划管理系统。

**演示地址**：[ERP 预览](https://doc.iocoder.cn/erp-preview/)

![功能图](/.image/common/erp-feature.png)

### CRM 系统

客户关系管理系统。

**演示地址**：[CRM 预览](https://doc.iocoder.cn/crm-preview/)

![功能图](/.image/common/crm-feature.png)

### AI 大模型

集成主流AI大模型，提供智能化解决方案。

**演示地址**：[AI 预览](https://doc.iocoder.cn/ai-preview/)

![功能图](/.image/common/ai-feature.png)
![演示动图](/.image/common/ai-preview.gif)

---

## ⚡ 技术栈

### 📦 模块架构

| 模块 | 说明 |
|------|------|
| `yudao-dependencies` | Maven 依赖版本管理 |
| `yudao-framework` | Java 框架拓展 |
| `yudao-server` | 管理后台 + 用户 APP 的服务端 |
| `yudao-module-system` | 系统功能的 Module 模块 |
| `yudao-module-infra` | 基础设施的 Module 模块 |
| `yudao-module-bpm` | 工作流程的 Module 模块 |
| `yudao-module-pay` | 支付系统的 Module 模块 |
| `yudao-module-mall` | 商城系统的 Module 模块 |
| `yudao-module-erp` | ERP 系统的 Module 模块 |
| `yudao-module-crm` | CRM 系统的 Module 模块 |
| `yudao-module-ai` | AI 大模型的 Module 模块 |
| `yudao-module-mp` | 微信公众号的 Module 模块 |
| `yudao-module-report` | 大屏报表 Module 模块 |

### 🔧 核心技术

| 技术 | 版本 | 说明 |
|------|------|------|
| **后端框架** |||
| Spring Boot | 3.4.5 | 应用开发框架 |
| Spring Security | 6.3.1 | Spring 安全框架 |
| MyBatis Plus | 3.5.7 | MyBatis 增强工具包 |
| Flowable | 7.0.0 | 工作流引擎 |
| **数据存储** |||
| MySQL | 5.7/8.0+ | 关系型数据库 |
| Redis | 5.0/6.0/7.0 | 缓存数据库 |
| **中间件** |||
| Redisson | 3.32.0 | Redis 客户端 |
| Quartz | 2.3.2 | 任务调度组件 |
| **工具库** |||
| Lombok | 1.18.34 | 简化 Java 代码 |
| MapStruct | 1.6.3 | Bean 转换工具 |
| Jackson | 2.17.1 | JSON 工具库 |
| **监控运维** |||
| SkyWalking | 9.0.0 | 分布式应用追踪系统 |
| Spring Boot Admin | 3.3.2 | Spring Boot 监控平台 |
| **API文档** |||
| Springdoc | 2.3.0 | Swagger 文档 |

> 📚 **学习资源**：每个技术都提供了详细的学习指南，参见 [SpringBoot-Labs](https://github.com/YunaiV/SpringBoot-Labs)

---

## 📸 演示图

### 系统功能展示

<details>
<summary>🖼️ 查看系统功能截图</summary>

| 模块 | 截图1 | 截图2 | 截图3 |
|------|-------|-------|-------|
| 登录 & 首页 | ![登录](/.image/登录.jpg) | ![首页](/.image/首页.jpg) | ![个人中心](/.image/个人中心.jpg) |
| 用户 & 应用 | ![用户管理](/.image/用户管理.jpg) | ![令牌管理](/.image/令牌管理.jpg) | ![应用管理](/.image/应用管理.jpg) |
| 租户 & 套餐 | ![租户管理](/.image/租户管理.jpg) | ![租户套餐](/.image/租户套餐.png) | - |
| 部门 & 岗位 | ![部门管理](/.image/部门管理.jpg) | ![岗位管理](/.image/岗位管理.jpg) | - |

</details>

### 工作流程展示

<details>
<summary>🔄 查看工作流程截图</summary>

| 模块 | 截图1 | 截图2 | 截图3 |
|------|-------|-------|-------|
| 流程模型 | ![流程模型-列表](/.image/流程模型-列表.jpg) | ![流程模型-设计](/.image/流程模型-设计.jpg) | ![流程模型-定义](/.image/流程模型-定义.jpg) |
| 我的流程 | ![我的流程-列表](/.image/我的流程-列表.jpg) | ![我的流程-发起](/.image/我的流程-发起.jpg) | ![我的流程-详情](/.image/我的流程-详情.jpg) |

</details>

### 移动端展示

<details>
<summary>📱 查看移动端截图</summary>

| 管理后台移动端 | | |
|---|---|---|
| ![移动端1](/.image/admin-uniapp/01.png) | ![移动端2](/.image/admin-uniapp/02.png) | ![移动端3](/.image/admin-uniapp/03.png) |
| ![移动端4](/.image/admin-uniapp/04.png) | ![移动端5](/.image/admin-uniapp/05.png) | ![移动端6](/.image/admin-uniapp/06.png) |

</details>

---

## 📄 开源协议

### 🎉 为什么选择我们？

**① 🆓 完全免费**
- 采用 [MIT License](https://gitee.com/zhijiantianya/ruoyi-vue-pro/blob/master/LICENSE) 开源协议
- 个人与企业可 100% 免费使用
- 无需保留作者、Copyright 信息

**② 📖 完全开源**  
- 代码全部开源，不藏不掖
- 完整的架构设计，不像某些项目只开源部分代码

**③ 💯 代码质量**
- 遵循《阿里巴巴 Java 开发手册》规范
- 113,770 行 Java 代码，42,462 行代码注释
- 代码注释详细，架构整洁

![开源项目对比](/.image/common/project-vs.png)

---

## 🤝 项目外包

💼 **专业的外包团队，为您提供一站式解决方案**

**团队优势**：
- 🏆 专业项目经理、架构师、前端/后端工程师、测试工程师、运维工程师
- 🔄 提供全流程外包服务
- 📈 丰富的项目经验

**业务范围**：
- 🛒 **电商系统**：商城、SCRM、会员系统
- 🏢 **企业管理**：OA、ERP、CRM、HIS系统  
- 💰 **金融支付**：支付系统、风控系统
- 💬 **社交通讯**：IM聊天、社交平台
- 📱 **移动应用**：微信公众号、小程序
- 🎯 **其他系统**：物流、CMS等定制化系统

**联系方式**：微信 **Aix9975**

---

<div align="center">

### 🌟 如果这个项目对您有帮助，请给个 Star ⭐

**您的支持是我们持续更新的动力！**

</div>
