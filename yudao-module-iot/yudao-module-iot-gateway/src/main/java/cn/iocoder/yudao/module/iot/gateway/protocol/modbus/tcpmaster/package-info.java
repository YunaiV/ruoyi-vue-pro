/**
 * Modbus TCP Master 协议实现包
 * <p>
 * 提供基于 j2mod 的 Modbus TCP 主站（Master）功能，支持：
 * 1. 定时轮询 Modbus 从站设备数据
 * 2. 下发属性设置命令到从站设备
 * 3. 数据格式转换（寄存器值 ↔ 物模型属性值）
 */
package cn.iocoder.yudao.module.iot.gateway.protocol.modbus.tcpmaster;
