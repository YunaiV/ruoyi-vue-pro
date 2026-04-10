/**
 * Modbus TCP Client（主站）协议：网关主动连接并轮询 Modbus 从站设备
 * <p>
 * 基于 j2mod 实现，支持 FC01-04 读、FC05/06/15/16 写，定时轮询 + 下发属性设置
 */
package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpclient;
