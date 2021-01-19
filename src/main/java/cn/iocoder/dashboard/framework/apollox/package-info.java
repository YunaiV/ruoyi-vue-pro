/**
 * 配置中心客户端，基于 Apollo Client 实现，所以叫 ApolloX
 *
 * 差别在于，我们使用 {@link cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.config.SysConfigDO} 表作为配置源。
 * 当然，功能肯定也会相对少些，满足最小化诉求。
 *
 * 1. 项目初始化时，可以使用 SysConfigDO 表的配置
 * 2. 使用 Spring @Value 可以注入属性
 * 3. SysConfigDO 表的配置修改时，注入到 @Value 的属性可以刷新
 *
 * 另外，整个包结构会参考 Apollo 为主，方便维护与理解
 *
 * 注意，目前有两个特性是不支持的
 * 1. 自定义配置变化的监听器
 */
package cn.iocoder.dashboard.framework.apollox;
