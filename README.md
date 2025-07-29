<div align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-2.7.18-blue.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-3.2-blue.svg" alt="Vue">
  <img src="https://img.shields.io/github/license/YunaiV/ruoyi-vue-pro" alt="License">
  <img src="https://img.shields.io/github/stars/YunaiV/ruoyi-vue-pro?style=social" alt="GitHub Stars">
  <img src="https://gitee.com/zhijiantianya/ruoyi-vue-pro/badge/star.svg?theme=dark" alt="Gitee Stars">
</div>

<h1 align="center">芋道管理系统</h1>

<p align="center">
  <strong>🚀 基于 Spring Boot + Vue 的前后端分离管理系统</strong>
  <br>
  <strong>💡 高效开发 · 功能完整 · 代码规范 · 持续更新</strong>
</p>

<div align="center">
  <h3>🎯 项目愿景</h3>
  <p>
    打造中国第一流的快速开发平台，以开发者为中心<br>
    全部开源，个人与企业可 100% 免费使用
  </p>
</div>

---

## 📋 目录

- [🌟 项目特性](#-项目特性)
- [🎯 快速开始](#-快速开始)
- [🚀 在线演示](#-在线演示)
- [📚 版本说明](#-版本说明)
- [🏗️ 系统架构](#️-系统架构)
- [🔧 技术栈](#-技术栈)
- [📦 功能模块](#-功能模块)
- [🖼️ 系统截图](#️-系统截图)
- [🤝 参与贡献](#-参与贡献)
- [📄 开源协议](#-开源协议)
- [💬 联系我们](#-联系我们)

## 🌟 项目特性

### ✨ 核心亮点

- **🔒 安全可靠**：基于 Spring Security 的权限认证体系，支持多终端、多租户
- **🎨 界面美观**：提供多套精美的前端模板（Vue3 + Element Plus / Ant Design Vue / Vue2）
- **📱 多端适配**：支持 PC、移动端、小程序多终端访问
- **⚡ 高性能**：Redis 缓存、数据库读写分离、接口性能优化
- **🔧 开发友好**：代码生成器、在线文档、规范的代码结构
- **🌐 国际化**：支持多语言切换，满足国际化需求

### 🎯 技术优势

- **模块化设计**：采用 Maven 多模块架构，模块解耦，易于维护和扩展
- **微服务就绪**：可无缝迁移到微服务架构（yudao-cloud）
- **数据库兼容**：支持 MySQL、Oracle、PostgreSQL 等多种数据库
- **云原生支持**：Docker 容器化部署，Kubernetes 编排支持

## 🎯 快速开始

### 📋 环境要求

| 环境 | 版本要求 |
|------|----------|
| JDK | 8+ 或 17/21+ |
| Node.js | 16+ |
| MySQL | 5.7+ / 8.0+ |
| Redis | 5.0+ |
| Maven | 3.6+ |

### ⚡ 一键启动

```bash
# 1. 克隆项目
git clone https://gitee.com/zhijiantianya/ruoyi-vue-pro.git
cd ruoyi-vue-pro

# 2. 导入数据库
# 导入 sql/mysql/ruoyi-vue-pro.sql 到你的 MySQL 数据库

# 3. 修改配置
# 编辑 yudao-server/src/main/resources/application-dev.yaml
# 配置数据库连接和 Redis 连接信息

# 4. 启动后端服务
mvn clean install
cd yudao-server
mvn spring-boot:run

# 5. 启动前端（新开终端）
cd yudao-ui/yudao-ui-admin-vue3
npm install
npm run dev
```

### 📖 详细文档

- **📘 快速入门**：[https://doc.iocoder.cn/quick-start/](https://doc.iocoder.cn/quick-start/)
- **🎥 视频教程**：[https://doc.iocoder.cn/video/](https://doc.iocoder.cn/video/)
- **📖 开发文档**：[https://doc.iocoder.cn/](https://doc.iocoder.cn/)

## 🚀 在线演示

### 💻 管理后台

| 版本 | 演示地址 | 账号密码 |
|------|----------|----------|
| Vue3 + Element Plus | [🔗 点击访问](http://dashboard-vue3.yudao.iocoder.cn) | admin/admin123 |
| Vue3 + Ant Design | [🔗 点击访问](http://dashboard-vben.yudao.iocoder.cn) | admin/admin123 |
| Vue2 + Element UI | [🔗 点击访问](http://dashboard.yudao.iocoder.cn) | admin/admin123 |

### 📱 业务系统

| 系统 | 演示地址 | 描述 |
|------|----------|------|
| 🛒 商城系统 | [🔗 点击访问](https://doc.iocoder.cn/mall-preview/) | 完整的电商解决方案 |
| 📊 ERP 系统 | [🔗 点击访问](https://doc.iocoder.cn/erp-preview/) | 企业资源规划系统 |
| 👥 CRM 系统 | [🔗 点击访问](https://doc.iocoder.cn/crm-preview/) | 客户关系管理系统 |
| 🤖 AI 大模型 | [🔗 点击访问](https://doc.iocoder.cn/ai-preview/) | AI 智能对话系统 |

## 📚 版本说明

### 🎭 版本对比

| 版本类型 | JDK 8 + Spring Boot 2.7 | JDK 17/21 + Spring Boot 3.2 |
|----------|--------------------------|------------------------------|
| **完整版** | [`master`](https://gitee.com/zhijiantianya/ruoyi-vue-pro/tree/master/) | [`master-jdk17`](https://gitee.com/zhijiantianya/ruoyi-vue-pro/tree/master-jdk17/) |
| **精简版** | [`yudao-boot-mini`](https://gitee.com/yudaocode/yudao-boot-mini) | [`yudao-boot-mini-jdk17`](https://gitee.com/yudaocode/yudao-boot-mini/tree/master-jdk17/) |

### 📋 版本说明

- **完整版**：包含所有业务模块（系统管理、工作流、商城、CRM、ERP 等）
- **精简版**：仅包含基础系统功能，适合快速开发定制化项目

> 💡 可参考 [迁移文档](https://doc.iocoder.cn/migrate-module/)，5-10 分钟即可按需迁移功能模块

## 🏗️ 系统架构

<div align="center">
  <img src="/.image/common/ruoyi-vue-pro-architecture.png" alt="系统架构图" width="80%">
</div>

### 🔧 架构特点

- **前后端分离**：Java 后端提供 RESTful API，Vue 前端独立部署
- **多模块设计**：按业务领域划分模块，松耦合高内聚
- **多租户架构**：支持 SaaS 模式，数据隔离安全可靠
- **分布式就绪**：可无缝升级到微服务架构

### 🌐 技术选型

**后端技术栈**
- **核心框架**：Spring Boot 2.7.18 / 3.2+
- **数据访问**：MyBatis Plus + Dynamic Datasource
- **权限认证**：Spring Security + JWT
- **数据缓存**：Redis + Redisson
- **工作流引擎**：Flowable 6.8.0
- **任务调度**：Quartz 2.3.2

**前端技术栈**
- **基础框架**：Vue 3.2 / Vue 2.6
- **UI 组件库**：Element Plus / Ant Design Vue / Element UI
- **状态管理**：Pinia / Vuex
- **构建工具**：Vite / Webpack

## 🔧 技术栈

### 📊 详细技术栈

| 分类 | 技术 | 版本 | 描述 |
|------|------|------|------|
| **后端框架** | Spring Boot | 2.7.18 | 核心应用框架 |
| **安全框架** | Spring Security | 5.7.11 | 认证和授权 |
| **数据访问** | MyBatis Plus | 3.5.7 | ORM 框架 |
| **数据库连接池** | Druid | 1.2.23 | 数据库连接池 |
| **缓存** | Redis | 5.0+ | 内存数据库 |
| **消息队列** | RabbitMQ | 3.8+ | 消息中间件 |
| **搜索引擎** | Elasticsearch | 7.x | 全文搜索 |
| **工作流** | Flowable | 6.8.0 | 工作流引擎 |
| **任务调度** | Quartz | 2.3.2 | 定时任务 |
| **API 文档** | Swagger | 3.0 | 接口文档 |
| **监控** | Spring Boot Admin | 2.7.10 | 应用监控 |
| **链路追踪** | SkyWalking | 8.12.0 | 分布式追踪 |

### 🗄️ 数据库支持

| 数据库 | 版本 | 状态 |
|--------|------|------|
| MySQL | 5.7+ / 8.0+ | ✅ 主要支持 |
| Oracle | 11g+ | ✅ 完全支持 |
| PostgreSQL | 10+ | ✅ 完全支持 |
| SQL Server | 2017+ | ✅ 完全支持 |
| 达梦 DM | 8+ | ✅ 国产化支持 |
| TiDB | 5.0+ | ✅ 分布式支持 |

## 📦 功能模块

### 🏢 系统管理

<div align="center">
  <img src="/.image/common/system-feature.png" alt="系统功能" width="80%">
</div>

| 功能模块 | 功能描述 | 特色 |
|----------|----------|------|
| 👥 **用户管理** | 用户信息维护、角色分配 | 支持批量操作、数据导入导出 |
| 🔐 **角色管理** | 角色权限配置、数据权限控制 | 细粒度权限控制 |
| 📋 **菜单管理** | 动态菜单配置、按钮权限 | 支持多级菜单 |
| 🏛️ **部门管理** | 组织架构管理、树形结构 | 数据权限隔离 |
| 💼 **岗位管理** | 岗位信息维护 | 与用户关联 |
| 🏢 **租户管理** | 多租户数据隔离 | 🚀 SaaS 模式 |
| 📦 **租户套餐** | 租户权限包配置 | 🚀 灵活套餐 |
| 📚 **字典管理** | 数据字典维护 | 支持缓存优化 |
| 📧 **邮件管理** | 邮件发送、模板管理 | 🚀 多平台支持 |
| 📱 **短信管理** | 短信发送、模板管理 | 🚀 多厂商支持 |
| 💬 **站内信** | 系统消息通知 | 🚀 实时推送 |

### 🔄 工作流程

<div align="center">
  <img src="/.image/common/bpm-feature.png" alt="工作流功能" width="80%">
</div>

**🎨 双设计器支持**

| 设计器类型 | 特点 | 适用场景 |
|------------|------|----------|
| **仿钉钉设计器** | 简单易用、拖拽配置 | 📝 简单审批流程 |
| **BPMN 设计器** | 专业标准、功能强大 | 🔧 复杂业务流程 |

**⚡ 核心功能**

- ✅ **多人审批**：会签、或签、依次审批
- ✅ **流程控制**：驳回、转办、委派、加签、减签
- ✅ **表单权限**：字段级别的读写权限控制
- ✅ **超时处理**：自动审批、提醒通知
- ✅ **条件分支**：复杂的流程路由控制

### 💳 支付系统

| 功能 | 描述 |
|------|------|
| 🚀 **应用管理** | 支付应用配置，支持多商户 |
| 🚀 **支付订单** | 统一支付订单管理 |
| 🚀 **退款订单** | 退款流程和记录管理 |
| 🚀 **回调通知** | 支付结果通知处理 |

**支持的支付方式**
- 💰 支付宝：PC、H5、APP、小程序
- 💚 微信支付：JSAPI、H5、APP、小程序、扫码
- 🏦 银联支付：PC、H5、APP

### 🛠️ 基础设施

<div align="center">
  <img src="/.image/common/infra-feature.png" alt="基础设施" width="80%">
</div>

| 功能分类 | 功能列表 |
|----------|----------|
| **开发工具** | 🚀 代码生成器、📖 API 文档、🗃️ 数据库文档、📝 表单构建器 |
| **系统监控** | 📊 Java 监控、🔍 链路追踪、📋 日志中心、📈 性能监控 |
| **文件服务** | 🚀 云存储（阿里云、腾讯云、七牛云）、📁 本地存储、🗂️ FTP 存储 |
| **消息服务** | 🚀 Redis 队列、🐰 RabbitMQ、🚀 RocketMQ、📡 WebSocket |
| **任务调度** | ⏰ 定时任务、📅 任务日志、🔄 任务监控 |

### 📊 业务系统

#### 🛒 商城系统
- **商品管理**：商品分类、属性、规格、库存
- **订单管理**：订单流程、支付、发货、售后
- **营销工具**：优惠券、拼团、秒杀、分销
- **会员体系**：积分、等级、标签、分组

#### 👥 CRM 系统
- **客户管理**：客户信息、跟进记录、客户池
- **销售管理**：商机、合同、回款、业绩
- **营销管理**：活动、线索、转化、分析

#### 📋 ERP 系统
- **采购管理**：供应商、采购订单、入库
- **销售管理**：客户、销售订单、出库
- **库存管理**：仓库、库存、调拨、盘点
- **财务管理**：收款、付款、账目、报表

#### 🤖 AI 大模型
- **对话管理**：智能问答、上下文理解
- **知识库**：文档管理、向量检索
- **模型配置**：多模型支持、参数调优

## 🖼️ 系统截图

### 🖥️ 管理后台

<table>
  <tr>
    <td><img src="/.image/登录.jpg" alt="登录页面"/></td>
    <td><img src="/.image/首页.jpg" alt="系统首页"/></td>
    <td><img src="/.image/用户管理.jpg" alt="用户管理"/></td>
  </tr>
  <tr>
    <td align="center">🔐 登录页面</td>
    <td align="center">🏠 系统首页</td>
    <td align="center">👥 用户管理</td>
  </tr>
</table>

### 📱 移动端

<table>
  <tr>
    <td><img src="/.image/admin-uniapp/01.png" alt="移动端首页" width="200"/></td>
    <td><img src="/.image/admin-uniapp/02.png" alt="个人中心" width="200"/></td>
    <td><img src="/.image/admin-uniapp/03.png" alt="工作台" width="200"/></td>
  </tr>
  <tr>
    <td align="center">📱 移动端首页</td>
    <td align="center">👤 个人中心</td>
    <td align="center">💼 工作台</td>
  </tr>
</table>

### 🔄 工作流程

<table>
  <tr>
    <td><img src="/.image/流程模型-列表.jpg" alt="流程模型"/></td>
    <td><img src="/.image/流程模型-设计.jpg" alt="流程设计"/></td>
  </tr>
  <tr>
    <td align="center">📋 流程模型</td>
    <td align="center">🎨 流程设计</td>
  </tr>
</table>

## 🤝 参与贡献

我们非常欢迎你的贡献，无论是 Bug 报告、功能建议还是代码贡献！

### 🚀 贡献方式

1. **🐛 提交 Issue**：发现 Bug 或有功能建议，请提交 Issue
2. **🔧 代码贡献**：Fork 项目，提交 Pull Request
3. **📖 文档完善**：帮助完善项目文档
4. **🌟 点个 Star**：给项目点个 Star，这是对我们最大的鼓励

### 📋 开发规范

- **代码规范**：遵循阿里巴巴 Java 开发手册
- **提交规范**：使用语义化的 Commit Message
- **测试覆盖**：新功能需要添加对应的单元测试

### 👥 贡献者

感谢所有为这个项目做出贡献的开发者！

<a href="https://github.com/YunaiV/ruoyi-vue-pro/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=YunaiV/ruoyi-vue-pro" />
</a>

## 📄 开源协议

### 🆓 MIT 许可证

本项目基于 **MIT License** 开源协议，这意味着：

- ✅ **商业使用**：可用于商业项目
- ✅ **修改发布**：可修改和重新发布
- ✅ **私有使用**：可在私有项目中使用
- ✅ **无需署名**：不需要保留作者信息

### 🆚 为什么选择我们？

<div align="center">
  <img src="/.image/common/project-vs.png" alt="项目对比" width="80%">
</div>

1. **📜 更宽松的协议**：MIT 协议比 Apache 2.0 更宽松
2. **💯 完全开源**：所有代码完全开源，不像某些项目只开源部分代码
3. **📝 代码质量高**：113,770 行 Java 代码，42,462 行详细注释
4. **🏢 生产验证**：已在多个头部企业生产环境中验证

## 💬 联系我们

### 🌐 官方链接

- **📖 官方文档**：[https://doc.iocoder.cn](https://doc.iocoder.cn)
- **🐛 问题反馈**：[Gitee Issues](https://gitee.com/zhijiantianya/ruoyi-vue-pro/issues)
- **💡 功能建议**：[GitHub Issues](https://github.com/YunaiV/ruoyi-vue-pro/issues)

### 💼 商务合作

**项目外包服务**
- 💬 **微信联系**：Aix9975
- 🏢 **服务范围**：商城、CRM、OA、ERP、支付系统等
- 👥 **专业团队**：项目经理、架构师、前后端工程师、测试工程师

### 🎯 交流群组

加入我们的技术交流群，获取：
- 🆘 技术支持和问题解答
- 📢 版本更新通知
- 💡 最佳实践分享
- 🤝 同行技术交流

---

<div align="center">
  <h3>⭐ 如果这个项目对你有帮助，请给它一个 Star ⭐</h3>
  <p>
    <strong>你的支持是我们持续改进的动力！</strong>
  </p>
  
  <p>
    <img src="https://img.shields.io/github/stars/YunaiV/ruoyi-vue-pro?style=social" alt="GitHub Stars">
    <img src="https://gitee.com/zhijiantianya/ruoyi-vue-pro/badge/star.svg?theme=dark" alt="Gitee Stars">
  </p>
</div>

<div align="center">
  <strong>🌟 现在、未来都不会有商业版本，所有代码全部开源！🌟</strong>
</div>
