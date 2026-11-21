/**
 * OPC UA Client 协议：网关作为 OPC UA 客户端连接服务端，采集设备属性，并支持属性设置写入
 * <p>
 * 基于 Eclipse Milo 实现，支持 Read 轮询、MonitoredItem 订阅和标量属性写入（Write Value）；不支持方法调用和透传
 */
package cn.iocoder.yudao.module.iot.gateway.protocol.opcua.client;
