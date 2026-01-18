/**
 * IoT 网关 CoAP 协议
 *
 * 基于 Eclipse Californium 实现，支持设备通过 CoAP 协议进行：
 * 1. 属性上报：POST /sys/{productKey}/{deviceName}/thing/property/post
 * 2. 事件上报：POST /sys/{productKey}/{deviceName}/thing/event/{eventId}/post
 *
 * 认证方式：通过 URI Query 参数 token 进行认证
 * 示例：coap://server:5683/sys/pk/dn/thing/property/post?token=xxx
 *
 * @author 芋道源码
 */
// TODO @AI：参考 /Users/yunai/Java/ruoyi-vue-pro-jdk25/yudao-module-iot/yudao-module-iot-gateway/src/main/java/cn/iocoder/yudao/module/iot/gateway/protocol/mqtt/package-info.java （现在注释应该有点不太对）
package cn.iocoder.yudao.module.iot.gateway.protocol.coap;
