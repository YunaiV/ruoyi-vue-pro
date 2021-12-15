/**
 * 多租户，支持如下层面：
 * 1. DB：基于 MyBatis Plus 多租户的功能实现。
 * 2. Redis：通过在 Redis Key 上拼接租户编号的方式，进行隔离。
 * 3. Web：请求 HTTP API 时，解析 Header 的 tenant-id 租户编号，添加到租户上下文。
 * 4. Security：校验当前登陆的用户，是否越权访问其它租户的数据。
 * 5. Job：在 JobHandler 执行任务时，会按照每个租户，都独立并行执行一次。
 * 6. MQ：在 Producer 发送消息时，Header 带上 tenant-id 租户编号；在 Consumer 消费消息时，将 Header 的 tenant-id 租户编号，添加到租户上下文。
 * 7. Async：异步需要保证 ThreadLocal 的传递性，通过使用阿里开源的 TransmittableThreadLocal 实现。相关的改造点，可见：
 *      1）Spring Async：
 *          {@link cn.iocoder.yudao.framework.quartz.config.YudaoAsyncAutoConfiguration#threadPoolTaskExecutorBeanPostProcessor()}
 *      2）Spring Security：
 *          TransmittableThreadLocalSecurityContextHolderStrategy
 *          和 YudaoSecurityAutoConfiguration#securityContextHolderMethodInvokingFactoryBean() 方法
 *
 */
package cn.iocoder.yudao.framework.tenant;
