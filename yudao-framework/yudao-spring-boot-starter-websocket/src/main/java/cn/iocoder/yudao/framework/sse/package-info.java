/**
 * SSE（Server-Sent Events）框架，支持多节点的广播
 *
 * 与 WebSocket 框架相互独立：消息、发送器、Consumer、配置与连接管理各自一套，只复用 MQ 的连接模板。分包与 WebSocket 对齐：
 * 1. config：配置项与自动配置
 * 2. core.controller：connect、close 接口
 * 3. core.message：SSE 消息
 * 4. core.sender：发送器，local、redis、rocketmq、rabbitmq、kafka 子包为各自的发送器与 Consumer
 * 5. core.session：连接、连接管理、关闭原因
 * 6. core.codec：事件帧编码、响应头
 * 7. core.scheduler：心跳、令牌过期与僵尸连接清理、优雅停机
 */
package cn.iocoder.yudao.framework.sse;
