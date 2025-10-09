/**
 * IoT 网关 MQTT WebSocket 协议实现
 * <p>
 * 基于 Vert.x 实现 MQTT over WebSocket 服务端，支持：
 * - 标准 MQTT 3.1.1 协议
 * - WebSocket 协议升级
 * - SSL/TLS 加密（wss://）
 * - 设备认证与连接管理
 * - QoS 0/1/2 消息质量保证
 * - 双向消息通信（上行/下行）
 *
 * @author 芋道源码
 */
package cn.iocoder.yudao.module.iot.gateway.protocol.mqttws;

