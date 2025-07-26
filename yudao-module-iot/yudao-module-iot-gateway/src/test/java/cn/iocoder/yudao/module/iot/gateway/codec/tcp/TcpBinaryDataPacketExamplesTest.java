package cn.iocoder.yudao.module.iot.gateway.codec.tcp;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TCP 二进制格式数据包单元测试
 *
 * 测试二进制协议创建和解析 TCP 上报数据包和心跳包
 *
 * 二进制协议格式：
 * 包头(4 字节) | 功能码(2 字节) | 消息序号(2 字节) | 包体数据(变长)
 *
 * @author 芋道源码
 */
@Slf4j
class TcpBinaryDataPacketExamplesTest {

    private IotTcpBinaryDeviceMessageCodec codec;

    @BeforeEach
    void setUp() {
        codec = new IotTcpBinaryDeviceMessageCodec();
    }

    @Test
    void testDataReport() {
        log.info("=== 二进制格式数据上报包测试 ===");

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
        log.info("解码后消息 ID: {}", decoded.getId());
        log.info("解码后请求 ID: {}", decoded.getRequestId());
        log.info("解码后方法: {}", decoded.getMethod());
        log.info("解码后设备 ID: {}", decoded.getDeviceId());
        log.info("解码后服务 ID: {}", decoded.getServerId());
        log.info("解码后参数: {}", decoded.getParams());

        // 断言验证
        assertNotNull(decoded.getId());
        assertEquals("thing.property.post", decoded.getMethod());
        assertNotNull(decoded.getParams());
        assertTrue(decoded.getParams() instanceof Map);
    }

    @Test
    void testHeartbeat() {
        log.info("=== 二进制格式心跳包测试 ===");

        // 创建心跳消息
        IotDeviceMessage heartbeat = IotDeviceMessage.requestOf("thing.state.online", null);
        heartbeat.setDeviceId(123456L);

        // 编码
        byte[] packet = codec.encode(heartbeat);
        log.info("心跳包长度: {} 字节", packet.length);
        log.info("心跳包(HEX): {}", bytesToHex(packet));

        // 解码验证
        IotDeviceMessage decoded = codec.decode(packet);
        log.info("解码后消息 ID: {}", decoded.getId());
        log.info("解码后请求 ID: {}", decoded.getRequestId());
        log.info("解码后方法: {}", decoded.getMethod());
        log.info("解码后设备 ID: {}", decoded.getDeviceId());
        log.info("解码后服务 ID: {}", decoded.getServerId());
        log.info("解码后参数: {}", decoded.getParams());

        // 断言验证
        assertNotNull(decoded.getId());
        assertEquals("thing.state.online", decoded.getMethod());
    }

    @Test
    void testComplexDataReport() {
        log.info("=== 二进制格式复杂数据上报测试 ===");

        // 创建复杂设备数据
        Map<String, Object> deviceData = new HashMap<>();

        // 环境数据
        Map<String, Object> environment = new HashMap<>();
        environment.put("temperature", 23.8);
        environment.put("humidity", 55.0);
        environment.put("co2", 420);
        deviceData.put("environment", environment);

        // GPS 数据
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
        log.info("解码后消息 ID: {}", decoded.getId());
        log.info("解码后请求 ID: {}", decoded.getRequestId());
        log.info("解码后方法: {}", decoded.getMethod());
        log.info("解码后设备 ID: {}", decoded.getDeviceId());
        log.info("解码后服务 ID: {}", decoded.getServerId());
        log.info("解码后参数: {}", decoded.getParams());

        // 断言验证
        assertNotNull(decoded.getId());
        assertEquals("thing.property.post", decoded.getMethod());
        assertNotNull(decoded.getParams());
    }

    @Test
    void testPacketStructureAnalysis() {
        log.info("=== 数据包结构分析测试 ===");

        // 创建测试数据
        Map<String, Object> sensorData = new HashMap<>();
        sensorData.put("temperature", 25.5);
        sensorData.put("humidity", 60.2);

        IotDeviceMessage message = IotDeviceMessage.requestOf("thing.property.post", sensorData);
        message.setDeviceId(123456L);

        // 编码
        byte[] packet = codec.encode(message);

        // 分析数据包结构
        analyzePacketStructure(packet);

        // 断言验证
        assertTrue(packet.length >= 8, "数据包长度应该至少为 8 字节");
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 字节数组转十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
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
     *
     * @param packet 数据包
     */
    private static void analyzePacketStructure(byte[] packet) {
        if (packet.length < 8) {
            log.error("数据包长度不足");
            return;
        }

        int index = 0;

        // 解析包头(4 字节) - 后续数据长度
        int totalLength = ((packet[index] & 0xFF) << 24) |
                ((packet[index + 1] & 0xFF) << 16) |
                ((packet[index + 2] & 0xFF) << 8) |
                (packet[index + 3] & 0xFF);
        index += 4;
        log.info("包头 - 后续数据长度: {} 字节", totalLength);

        // 解析功能码(2 字节)
        int functionCode = ((packet[index] & 0xFF) << 8) | (packet[index + 1] & 0xFF);
        index += 2;
        log.info("功能码: {} ({})", functionCode, getFunctionCodeName(functionCode));

        // 解析消息序号(2 字节)
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
     *
     * @param code 功能码
     * @return 功能码名称
     */
    private static String getFunctionCodeName(int code) {
        return switch (code) {
            case 10 -> "设备注册";
            case 11 -> "注册回复";
            case 20 -> "心跳请求";
            case 21 -> "心跳回复";
            case 30 -> "消息上行";
            case 40 -> "消息下行";
            default -> "未知功能码";
        };
    }
}
