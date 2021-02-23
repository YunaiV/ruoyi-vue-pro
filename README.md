## 平台简介

**芋道** 是基于 [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue) **重构**，一套**全部开源**的**企业级**的快速开发平台，毫无保留给个人及企业免费使用。

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

### 系统功能

1. 用户管理：用户是系统操作者，该功能主要完成系统用户配置
1. 在线用户：当前系统中活跃用户状态监控
1. 角色管理：角色菜单权限分配、设置角色按机构进行数据范围权限划分
1. 菜单管理：配置系统菜单，操作权限，按钮权限标识等
1. 部门管理：配置系统组织机构（公司、部门、小组），树结构展现支持数据权限
1. 岗位管理：配置系统用户所属担任职务
1. 字典管理：对系统中经常使用的一些较为固定的数据进行维护
1. 通知公告：系统通知公告信息发布维护
1. 操作日志：系统正常操作日志记录和查询；系统异常信息日志记录和查询
1. 登录日志：系统登录日志记录查询包含登录异常

### 基础设施

1. 配置管理：对系统动态配置常用参数
1. 定时任务：在线（添加、修改、删除)任务调度包含执行结果日志
1. MySQL 监控：监视当前系统数据库连接池状态，可进行分析SQL找出系统性能瓶颈
1. Redis 监控：监控 Redis 数据库的使用情况，使用的 Redis Key 管理
1. Java 监控：基于 Spring Boot Admin 实现 Java 应用的监控
1. 链路追踪：基于 SkyWalking 实现性能监控，特别是链路的追踪
1. 分布式锁：基于 Redis 实现分布式锁，满足并发场景
1. 幂等组件：基于 Redis 实现幂等组件，解决重复请求问题

### 研发工具

1. 表单构建：拖动表单元素生成相应的 HTML 代码
1. 代码生成：前后端代码的生成（Java、Vue、SQL），支持 CRUD 下载
1. 系统接口：基于 Swagger 自动生成相关的 RESTful API 接口文档
1. 数据库文档：基于 Screw 自动生成数据库文档

## 在线体验

演示地址：<http://dashboard.yudao.iocoder.cn>
* 账号密码：admin/admin123  

文档地址：<http://www.iocoder.cn/categories/Yudao/>
* [《如何搭建环境》](http://www.iocoder.cn/categories/Yudao/?yudao)

> 未来会补充文档和视频，方便胖友冲冲冲！

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
        <td><img src="https://oscimg.oschina.net/oscnet/up-6d73c2140ce694e3de4c05035fdc1868d4c.png"/></td>
    </tr>
</table>


