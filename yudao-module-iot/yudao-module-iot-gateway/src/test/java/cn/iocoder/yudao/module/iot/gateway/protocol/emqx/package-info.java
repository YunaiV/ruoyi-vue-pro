/**
 * IoT 网关 EMQX 协议集成测试包
 *
 * <p>
 * 测试类直接使用 mqtt 包下的单测即可，因为设备都是通过 MQTT 协议连接 EMQX Broker。
 *
 * @see cn.iocoder.yudao.module.iot.gateway.protocol.mqtt
 *
 * <h2>架构</h2>
 * <pre>
 * +--------+      MQTT       +-------------+     HTTP Hook     +---------+
 * |  设备  | --------------> | EMQX Broker | ----------------> |  网关   |
 * +--------+                 +-------------+                   +---------+
 * </pre>
 *
 * @author 芋道源码
 */
package cn.iocoder.yudao.module.iot.gateway.protocol.emqx;
