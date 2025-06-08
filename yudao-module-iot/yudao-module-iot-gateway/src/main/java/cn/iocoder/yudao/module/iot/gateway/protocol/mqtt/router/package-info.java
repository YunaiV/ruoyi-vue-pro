/**
 * MQTT 协议路由器包
 * <p>
 * 包含 MQTT 协议的所有路由处理器和抽象基类：
 * <ul>
 * <li>{@link cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router.IotMqttAbstractHandler}
 * - 抽象路由处理器基类</li>
 * <li>{@link cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router.IotMqttUpstreamRouter}
 * - 上行消息路由器</li>
 * <li>{@link cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router.IotMqttAuthRouter}
 * - 认证路由器</li>
 * <li>{@link cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router.IotMqttPropertyHandler}
 * - 属性处理器</li>
 * <li>{@link cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router.IotMqttEventHandler}
 * - 事件处理器</li>
 * <li>{@link cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router.IotMqttServiceHandler}
 * - 服务处理器</li>
 * </ul>
 *
 * @author 芋道源码
 */
package cn.iocoder.yudao.module.iot.gateway.protocol.mqtt.router;