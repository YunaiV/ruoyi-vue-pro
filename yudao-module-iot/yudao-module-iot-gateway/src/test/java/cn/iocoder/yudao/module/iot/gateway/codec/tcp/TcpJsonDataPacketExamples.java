package cn.iocoder.yudao.module.iot.gateway.codec.tcp;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * TCP JSON格式数据包示例
 * 
 * 演示如何使用新的JSON格式进行TCP消息编解码
 *
 * @author 芋道源码
 */
@Slf4j
public class TcpJsonDataPacketExamples {

    public static void main(String[] args) {
        IotTcpJsonDeviceMessageCodec codec = new IotTcpJsonDeviceMessageCodec();
        
        // 1. 数据上报示例
        demonstrateDataReport(codec);
        
        // 2. 心跳示例
        demonstrateHeartbeat(codec);
        
        // 3. 事件上报示例
        demonstrateEventReport(codec);
        
        // 4. 复杂数据上报示例
        demonstrateComplexDataReport(codec);
        
        // 5. 便捷方法示例
        demonstrateConvenienceMethods();
        
        // 6. EMQX兼容性示例
        demonstrateEmqxCompatibility();
    }

    /**
     * 演示数据上报
     */
    private static void demonstrateDataReport(IotTcpJsonDeviceMessageCodec codec) {
        log.info("=== JSON格式数据上报示例 ===");
        
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
        log.info("编码后JSON: {}", jsonString);
        log.info("数据包长度: {} 字节", packet.length);
        
        // 解码验证
        IotDeviceMessage decoded = codec.decode(packet);
        log.info("解码后消息ID: {}", decoded.getId());
        log.info("解码后方法: {}", decoded.getMethod());
        log.info("解码后设备ID: {}", decoded.getDeviceId());
        log.info("解码后服务ID: {}", decoded.getServerId());
        log.info("解码后参数: {}", decoded.getParams());
        
        System.out.println();
    }

    /**
     * 演示心跳
     */
    private static void demonstrateHeartbeat(IotTcpJsonDeviceMessageCodec codec) {
        log.info("=== JSON格式心跳示例 ===");
        
        // 创建心跳消息
        IotDeviceMessage heartbeat = IotDeviceMessage.requestOf("thing.state.online", null);
        heartbeat.setDeviceId(123456L);
        
        // 编码
        byte[] packet = codec.encode(heartbeat);
        String jsonString = new String(packet, StandardCharsets.UTF_8);
        log.info("编码后JSON: {}", jsonString);
        log.info("心跳包长度: {} 字节", packet.length);
        
        // 解码验证
        IotDeviceMessage decoded = codec.decode(packet);
        log.info("解码后消息ID: {}", decoded.getId());
        log.info("解码后方法: {}", decoded.getMethod());
        log.info("解码后设备ID: {}", decoded.getDeviceId());
        log.info("解码后服务ID: {}", decoded.getServerId());
        
        System.out.println();
    }

    /**
     * 演示事件上报
     */
    private static void demonstrateEventReport(IotTcpJsonDeviceMessageCodec codec) {
        log.info("=== JSON格式事件上报示例 ===");
        
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
        log.info("编码后JSON: {}", jsonString);
        log.info("事件包长度: {} 字节", packet.length);
        
        // 解码验证
        IotDeviceMessage decoded = codec.decode(packet);
        log.info("解码后消息ID: {}", decoded.getId());
        log.info("解码后方法: {}", decoded.getMethod());
        log.info("解码后设备ID: {}", decoded.getDeviceId());
        log.info("解码后参数: {}", decoded.getParams());
        
        System.out.println();
    }

    /**
     * 演示复杂数据上报
     */
    private static void demonstrateComplexDataReport(IotTcpJsonDeviceMessageCodec codec) {
        log.info("=== JSON格式复杂数据上报示例 ===");
        
        // 创建复杂设备数据（类似EMQX格式）
        Map<String, Object> deviceData = new HashMap<>();
        
        // 环境数据
        Map<String, Object> environment = new HashMap<>();
        environment.put("temperature", 23.8);
        environment.put("humidity", 55.0);
        environment.put("co2", 420);
        environment.put("pm25", 35);
        deviceData.put("environment", environment);
        
        // GPS数据
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
        log.info("编码后JSON: {}", jsonString);
        log.info("复杂数据包长度: {} 字节", packet.length);
        
        // 解码验证
        IotDeviceMessage decoded = codec.decode(packet);
        log.info("解码后消息ID: {}", decoded.getId());
        log.info("解码后方法: {}", decoded.getMethod());
        log.info("解码后设备ID: {}", decoded.getDeviceId());
        log.info("解码后参数: {}", decoded.getParams());
        
        System.out.println();
    }

    /**
     * 演示便捷方法
     */
    private static void demonstrateConvenienceMethods() {
        log.info("=== 便捷方法示例 ===");
        
        IotTcpJsonDeviceMessageCodec codec = new IotTcpJsonDeviceMessageCodec();
        
        // 使用便捷方法编码数据上报
        Map<String, Object> sensorData = Map.of(
            "temperature", 26.5,
            "humidity", 58.3
        );
        byte[] dataPacket = codec.encodeDataReport(sensorData, 123456L, "smart_sensor", "device_001");
        log.info("便捷方法编码数据上报: {}", new String(dataPacket, StandardCharsets.UTF_8));
        
        // 使用便捷方法编码心跳
        byte[] heartbeatPacket = codec.encodeHeartbeat(123456L, "smart_sensor", "device_001");
        log.info("便捷方法编码心跳: {}", new String(heartbeatPacket, StandardCharsets.UTF_8));
        
        // 使用便捷方法编码事件
        Map<String, Object> eventData = Map.of(
            "eventType", "maintenance",
            "description", "定期维护提醒"
        );
        byte[] eventPacket = codec.encodeEventReport(eventData, 123456L, "smart_sensor", "device_001");
        log.info("便捷方法编码事件: {}", new String(eventPacket, StandardCharsets.UTF_8));
        
        System.out.println();
    }

    /**
     * 演示与EMQX格式的兼容性
     */
    private static void demonstrateEmqxCompatibility() {
        log.info("=== EMQX格式兼容性示例 ===");
        
        // 模拟EMQX风格的消息格式
        String emqxStyleJson = """
            {
                "id": "msg_001",
                "method": "thing.property.post",
                "deviceId": 123456,
                "params": {
                    "temperature": 25.5,
                    "humidity": 60.2
                },
                "timestamp": 1642781234567
            }
            """;
        
        IotTcpJsonDeviceMessageCodec codec = new IotTcpJsonDeviceMessageCodec();
        
        // 解码EMQX风格的消息
        byte[] emqxBytes = emqxStyleJson.getBytes(StandardCharsets.UTF_8);
        IotDeviceMessage decoded = codec.decode(emqxBytes);
        
        log.info("EMQX风格消息解码成功:");
        log.info("消息ID: {}", decoded.getId());
        log.info("方法: {}", decoded.getMethod());
        log.info("设备ID: {}", decoded.getDeviceId());
        log.info("参数: {}", decoded.getParams());
        
        System.out.println();
    }
}
