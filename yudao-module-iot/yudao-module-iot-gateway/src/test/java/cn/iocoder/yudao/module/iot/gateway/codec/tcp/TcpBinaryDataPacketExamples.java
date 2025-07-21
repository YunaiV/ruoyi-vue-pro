package cn.iocoder.yudao.module.iot.gateway.codec.tcp;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * TCP二进制格式数据包示例
 *
 * 演示如何使用二进制协议创建和解析TCP上报数据包和心跳包
 *
 * 二进制协议格式：
 * 包头(4字节) | 地址长度(2字节) | 设备地址(变长) | 功能码(2字节) | 消息序号(2字节) | 包体数据(变长)
 *
 * @author 芋道源码
 */
@Slf4j
public class TcpBinaryDataPacketExamples {

    public static void main(String[] args) {
        IotTcpBinaryDeviceMessageCodec codec = new IotTcpBinaryDeviceMessageCodec();
        
        // 1. 数据上报包示例
        demonstrateDataReport(codec);
        
        // 2. 心跳包示例
        demonstrateHeartbeat(codec);
        
        // 3. 复杂数据上报示例
        demonstrateComplexDataReport(codec);
    }

    /**
     * 演示二进制格式数据上报包
     */
    private static void demonstrateDataReport(IotTcpBinaryDeviceMessageCodec codec) {
        log.info("=== 二进制格式数据上报包示例 ===");
        
        // 创建传感器数据
        Map<String, Object> sensorData = new HashMap<>();
        sensorData.put("temperature", 25.5);
        sensorData.put("humidity", 60.2);
        sensorData.put("pressure", 1013.25);
        sensorData.put("battery", 85);
        
        // 创建设备消息
        IotDeviceMessage message = IotDeviceMessage.requestOf("thing.property.post", sensorData);
        message.setDeviceId(123456L);
        
        // 编码
        byte[] packet = codec.encode(message);
        log.info("编码后数据包长度: {} 字节", packet.length);
        log.info("编码后数据包(HEX): {}", bytesToHex(packet));
        
        // 解码验证
        IotDeviceMessage decoded = codec.decode(packet);
        log.info("解码后消息ID: {}", decoded.getId());
        log.info("解码后请求ID: {}", decoded.getRequestId());
        log.info("解码后方法: {}", decoded.getMethod());
        log.info("解码后设备ID: {}", decoded.getDeviceId());
        log.info("解码后服务ID: {}", decoded.getServerId());
        log.info("解码后上报时间: {}", decoded.getReportTime());
        log.info("解码后参数: {}", decoded.getParams());
        
        System.out.println();
    }

    /**
     * 演示二进制格式心跳包
     */
    private static void demonstrateHeartbeat(IotTcpBinaryDeviceMessageCodec codec) {
        log.info("=== 二进制格式心跳包示例 ===");
        
        // 创建心跳消息
        IotDeviceMessage heartbeat = IotDeviceMessage.requestOf("thing.state.online", null);
        heartbeat.setDeviceId(123456L);
        
        // 编码
        byte[] packet = codec.encode(heartbeat);
        log.info("心跳包长度: {} 字节", packet.length);
        log.info("心跳包(HEX): {}", bytesToHex(packet));
        
        // 解码验证
        IotDeviceMessage decoded = codec.decode(packet);
        log.info("解码后消息ID: {}", decoded.getId());
        log.info("解码后请求ID: {}", decoded.getRequestId());
        log.info("解码后方法: {}", decoded.getMethod());
        log.info("解码后设备ID: {}", decoded.getDeviceId());
        log.info("解码后服务ID: {}", decoded.getServerId());
        log.info("解码后参数: {}", decoded.getParams());
        
        System.out.println();
    }

    /**
     * 演示二进制格式复杂数据上报
     */
    private static void demonstrateComplexDataReport(IotTcpBinaryDeviceMessageCodec codec) {
        log.info("=== 二进制格式复杂数据上报示例 ===");
        
        // 创建复杂设备数据
        Map<String, Object> deviceData = new HashMap<>();
        
        // 环境数据
        Map<String, Object> environment = new HashMap<>();
        environment.put("temperature", 23.8);
        environment.put("humidity", 55.0);
        environment.put("co2", 420);
        deviceData.put("environment", environment);
        
        // GPS数据
        Map<String, Object> location = new HashMap<>();
        location.put("latitude", 39.9042);
        location.put("longitude", 116.4074);
        location.put("altitude", 43.5);
        deviceData.put("location", location);
        
        // 设备状态
        Map<String, Object> status = new HashMap<>();
        status.put("battery", 78);
        status.put("signal", -65);
        status.put("online", true);
        deviceData.put("status", status);
        
        // 创建设备消息
        IotDeviceMessage message = IotDeviceMessage.requestOf("thing.property.post", deviceData);
        message.setDeviceId(789012L);
        
        // 编码
        byte[] packet = codec.encode(message);
        log.info("复杂数据包长度: {} 字节", packet.length);
        log.info("复杂数据包(HEX): {}", bytesToHex(packet));
        
        // 解码验证
        IotDeviceMessage decoded = codec.decode(packet);
        log.info("解码后消息ID: {}", decoded.getId());
        log.info("解码后请求ID: {}", decoded.getRequestId());
        log.info("解码后方法: {}", decoded.getMethod());
        log.info("解码后设备ID: {}", decoded.getDeviceId());
        log.info("解码后服务ID: {}", decoded.getServerId());
        log.info("解码后参数: {}", decoded.getParams());
        
        System.out.println();
    }

    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X ", b));
        }
        return result.toString().trim();
    }

    /**
     * 演示数据包结构分析
     */
    public static void analyzePacketStructure(byte[] packet) {
        if (packet.length < 8) {
            log.error("数据包长度不足");
            return;
        }
        
        int index = 0;
        
        // 解析包头(4字节) - 后续数据长度
        int totalLength = ((packet[index] & 0xFF) << 24) |
                         ((packet[index + 1] & 0xFF) << 16) |
                         ((packet[index + 2] & 0xFF) << 8) |
                         (packet[index + 3] & 0xFF);
        index += 4;
        log.info("包头 - 后续数据长度: {} 字节", totalLength);
        
        // 解析设备地址长度(2字节)
        int addrLength = ((packet[index] & 0xFF) << 8) | (packet[index + 1] & 0xFF);
        index += 2;
        log.info("设备地址长度: {} 字节", addrLength);
        
        // 解析设备地址
        String deviceAddr = new String(packet, index, addrLength);
        index += addrLength;
        log.info("设备地址: {}", deviceAddr);
        
        // 解析功能码(2字节)
        int functionCode = ((packet[index] & 0xFF) << 8) | (packet[index + 1] & 0xFF);
        index += 2;
        log.info("功能码: {} ({})", functionCode, getFunctionCodeName(functionCode));
        
        // 解析消息序号(2字节)
        int messageId = ((packet[index] & 0xFF) << 8) | (packet[index + 1] & 0xFF);
        index += 2;
        log.info("消息序号: {}", messageId);
        
        // 解析包体数据
        if (index < packet.length) {
            String payload = new String(packet, index, packet.length - index);
            log.info("包体数据: {}", payload);
        }
    }

    /**
     * 获取功能码名称
     */
    private static String getFunctionCodeName(int code) {
        switch (code) {
            case 10: return "设备注册";
            case 11: return "注册回复";
            case 20: return "心跳请求";
            case 21: return "心跳回复";
            case 30: return "消息上行";
            case 40: return "消息下行";
            default: return "未知功能码";
        }
    }
}
