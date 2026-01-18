/**
 * CoAP 协议实现包
 * <p>
 * 提供基于 Eclipse Californium 的 IoT 设备连接和消息处理功能
 * <p>
 * URI 路径：
 * - 认证：POST /auth
 * - 属性上报：POST /topic/sys/{productKey}/{deviceName}/thing/property/post
 * - 事件上报：POST /topic/sys/{productKey}/{deviceName}/thing/event/post
 * <p>
 * Token 通过 CoAP Option 2088 携带
 */
package cn.iocoder.yudao.module.iot.gateway.protocol.coap;
