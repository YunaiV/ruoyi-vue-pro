package cn.iocoder.yudao.module.iot.gateway.codec.tcp;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TCP JSON 格式数据包单元测试
 * <p>
 * 测试 JSON 格式的 TCP 消息编解码功能
 *
 * @author 芋道源码
 */
@Slf4j
class TcpJsonDataPacketExamplesTest {

    private IotTcpJsonDeviceMessageCodec codec;

    @BeforeEach
    void setUp() {
        codec = new IotTcpJsonDeviceMessageCodec();
    }

    @Test
    void testDataReport() {
        log.info("=== JSON 格式数据上报测试 ===");

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
        String jsonString = new String(packet, StandardCharsets.UTF_8);
        log.info("编码后 JSON: {}", jsonString);
        log.info("数据包长度: {} 字节", packet.length);

        // 解码验证
        IotDeviceMessage decoded = codec.decode(packet);
        log.info("解码后消息 ID: {}", decoded.getId());
        log.info("解码后方法: {}", decoded.getMethod());
        log.info("解码后设备 ID: {}", decoded.getDeviceId());
        log.info("解码后服务 ID: {}", decoded.getServerId());
        log.info("解码后参数: {}", decoded.getParams());

        // 断言验证
        assertNotNull(decoded.getId());
        assertEquals("thing.property.post", decoded.getMethod());
        assertEquals(123456L, decoded.getDeviceId());
        assertNotNull(decoded.getParams());
        assertTrue(decoded.getParams() instanceof Map);
    }

    @Test
    void testHeartbeat() {
        log.info("=== JSON 格式心跳测试 ===");

        // 创建心跳消息
        IotDeviceMessage heartbeat = IotDeviceMessage.requestOf("thing.state.online", null);
        heartbeat.setDeviceId(123456L);

        // 编码
        byte[] packet = codec.encode(heartbeat);
        String jsonString = new String(packet, StandardCharsets.UTF_8);
        log.info("编码后 JSON: {}", jsonString);
        log.info("心跳包长度: {} 字节", packet.length);

        // 解码验证
        IotDeviceMessage decoded = codec.decode(packet);
        log.info("解码后消息 ID: {}", decoded.getId());
        log.info("解码后方法: {}", decoded.getMethod());
        log.info("解码后设备 ID: {}", decoded.getDeviceId());
        log.info("解码后服务 ID: {}", decoded.getServerId());

        // 断言验证
        assertNotNull(decoded.getId());
        assertEquals("thing.state.online", decoded.getMethod());
        assertEquals(123456L, decoded.getDeviceId());
    }

    @Test
    void testEventReport() {
        log.info("=== JSON 格式事件上报测试 ===");

        // 创建事件数据
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventType", "alarm");
        eventData.put("level", "warning");
        eventData.put("description", "温度过高");
        eventData.put("value", 45.8);

        // 创建事件消息
        IotDeviceMessage event = IotDeviceMessage.requestOf("thing.event.post", eventData);
        event.setDeviceId(123456L);

        // 编码
        byte[] packet = codec.encode(event);
        String jsonString = new String(packet, StandardCharsets.UTF_8);
        log.info("编码后 JSON: {}", jsonString);
        log.info("事件包长度: {} 字节", packet.length);

        // 解码验证
        IotDeviceMessage decoded = codec.decode(packet);
        log.info("解码后消息 ID: {}", decoded.getId());
        log.info("解码后方法: {}", decoded.getMethod());
        log.info("解码后设备 ID: {}", decoded.getDeviceId());
        log.info("解码后参数: {}", decoded.getParams());

        // 断言验证
        assertNotNull(decoded.getId());
        assertEquals("thing.event.post", decoded.getMethod());
        assertEquals(123456L, decoded.getDeviceId());
        assertNotNull(decoded.getParams());
    }

    @Test
    void testComplexDataReport() {
        log.info("=== JSON 格式复杂数据上报测试 ===");

        // 创建复杂设备数据（类似 EMQX 格式）
        Map<String, Object> deviceData = new HashMap<>();

        // 环境数据
        Map<String, Object> environment = new HashMap<>();
        environment.put("temperature", 23.8);
        environment.put("humidity", 55.0);
        environment.put("co2", 420);
        environment.put("pm25", 35);
        deviceData.put("environment", environment);

        // GPS 数据
        Map<String, Object> location = new HashMap<>();
        location.put("latitude", 39.9042);
        location.put("longitude", 116.4074);
        location.put("altitude", 43.5);
        location.put("speed", 0.0);
        deviceData.put("location", location);

        // 设备状态
        Map<String, Object> status = new HashMap<>();
        status.put("battery", 78);
        status.put("signal", -65);
        status.put("online", true);
        status.put("version", "1.2.3");
        deviceData.put("status", status);

        // 创建设备消息
        IotDeviceMessage message = IotDeviceMessage.requestOf("thing.property.post", deviceData);
        message.setDeviceId(789012L);

        // 编码
        byte[] packet = codec.encode(message);
        String jsonString = new String(packet, StandardCharsets.UTF_8);
        log.info("编码后 JSON: {}", jsonString);
        log.info("复杂数据包长度: {} 字节", packet.length);

        // 解码验证
        IotDeviceMessage decoded = codec.decode(packet);
        log.info("解码后消息 ID: {}", decoded.getId());
        log.info("解码后方法: {}", decoded.getMethod());
        log.info("解码后设备 ID: {}", decoded.getDeviceId());
        log.info("解码后参数: {}", decoded.getParams());

        // 断言验证
        assertNotNull(decoded.getId());
        assertEquals("thing.property.post", decoded.getMethod());
        assertEquals(789012L, decoded.getDeviceId());
        assertNotNull(decoded.getParams());
    }

}