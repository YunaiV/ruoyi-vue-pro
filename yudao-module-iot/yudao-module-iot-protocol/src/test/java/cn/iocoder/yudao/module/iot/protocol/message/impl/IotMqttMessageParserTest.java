package cn.iocoder.yudao.module.iot.protocol.message.impl;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.protocol.message.IotMqttMessage;
import cn.iocoder.yudao.module.iot.protocol.message.IotStandardResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * IoT MQTT 消息解析器测试类
 *
 * @author haohao
 */
class IotMqttMessageParserTest {

    private IotMqttMessageParser parser;

    @BeforeEach
    void setUp() {
        parser = new IotMqttMessageParser();
    }

    @Test
    void testParseValidJsonMessage() {
        // 构建有效的 JSON 消息
        JSONObject message = new JSONObject();
        message.set("id", "123456");
        message.set("version", "1.0");
        message.set("method", "thing.service.property.set");

        Map<String, Object> params = new HashMap<>();
        params.put("temperature", 25.5);
        params.put("humidity", 60.0);
        message.set("params", params);

        String topic = "/sys/productKey/deviceName/thing/service/property/set";
        byte[] payload = message.toString().getBytes(StandardCharsets.UTF_8);

        // 解析消息
        IotMqttMessage result = parser.parse(topic, payload);

        // 验证结果
        assertNotNull(result);
        assertEquals("123456", result.getId());
        assertEquals("1.0", result.getVersion());
        assertEquals("thing.service.property.set", result.getMethod());
        assertNotNull(result.getParams());
        assertEquals(25.5, ((Number) result.getParams().get("temperature")).doubleValue());
        assertEquals(60.0, ((Number) result.getParams().get("humidity")).doubleValue());
    }

    @Test
    void testParseMessageWithoutMethod() {
        // 构建没有 method 字段的消息，应该从 topic 中解析
        JSONObject message = new JSONObject();
        message.set("id", "789012");
        message.set("version", "1.0");

        Map<String, Object> params = new HashMap<>();
        params.put("voltage", 3.3);
        message.set("params", params);

        String topic = "/sys/productKey/deviceName/thing/service/property/set";
        byte[] payload = message.toString().getBytes(StandardCharsets.UTF_8);

        // 解析消息
        IotMqttMessage result = parser.parse(topic, payload);

        // 验证结果
        assertNotNull(result);
        assertEquals("789012", result.getId());
        assertEquals("1.0", result.getVersion());
        assertNotNull(result.getMethod()); // 应该从 topic 中解析出方法
        assertNotNull(result.getParams());
        assertEquals(3.3, ((Number) result.getParams().get("voltage")).doubleValue());
    }

    @Test
    void testParseInvalidJsonMessage() {
        String topic = "/sys/productKey/deviceName/thing/service/property/set";
        byte[] payload = "invalid json".getBytes(StandardCharsets.UTF_8);

        // 解析消息
        IotMqttMessage result = parser.parse(topic, payload);

        // 验证结果
        assertNull(result);
    }

    @Test
    void testParseEmptyPayload() {
        String topic = "/sys/productKey/deviceName/thing/service/property/set";

        // 测试 null payload
        IotMqttMessage result1 = parser.parse(topic, null);
        assertNull(result1);

        // 测试空 payload
        IotMqttMessage result2 = parser.parse(topic, new byte[0]);
        assertNull(result2);
    }

    @Test
    void testFormatResponse() {
        // 创建标准响应
        IotStandardResponse response = IotStandardResponse.success("123456", "property.set", null);

        // 格式化响应
        byte[] result = parser.formatResponse(response);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.length > 0);

        // 验证 JSON 格式
        String jsonString = new String(result, StandardCharsets.UTF_8);
        assertTrue(jsonString.contains("123456"));
        assertTrue(jsonString.contains("property.set"));
    }

    @Test
    void testCanHandle() {
        // 测试支持的主题格式
        assertTrue(parser.canHandle("/sys/productKey/deviceName/thing/service/property/set"));
        assertTrue(parser.canHandle("/mqtt/productKey/deviceName/property/set"));
        assertTrue(parser.canHandle("/device/productKey/deviceName/data"));

        // 测试不支持的主题格式
        assertFalse(parser.canHandle("/http/device/productKey/deviceName/property/set"));
        assertFalse(parser.canHandle("/unknown/topic"));
        assertFalse(parser.canHandle(null));
        assertFalse(parser.canHandle(""));
    }

    @Test
    void testParseMqttTopicFormat() {
        // 测试新的 MQTT 主题格式
        JSONObject message = new JSONObject();
        message.set("id", "mqtt001");
        message.set("version", "1.0");
        message.set("method", "device.property.report");

        Map<String, Object> params = new HashMap<>();
        params.put("signal", 85);
        message.set("params", params);

        String topic = "/mqtt/productKey/deviceName/property/report";
        byte[] payload = message.toString().getBytes(StandardCharsets.UTF_8);

        // 解析消息
        IotMqttMessage result = parser.parse(topic, payload);

        // 验证结果
        assertNotNull(result);
        assertEquals("mqtt001", result.getId());
        assertEquals("device.property.report", result.getMethod());
        assertEquals(85, ((Number) result.getParams().get("signal")).intValue());
    }

    @Test
    void testParseDeviceTopicFormat() {
        // 测试设备主题格式
        JSONObject message = new JSONObject();
        message.set("id", "device001");
        message.set("version", "1.0");
        message.set("method", "sensor.data");

        Map<String, Object> params = new HashMap<>();
        params.put("timestamp", System.currentTimeMillis());
        message.set("params", params);

        String topic = "/device/productKey/deviceName/sensor/data";
        byte[] payload = message.toString().getBytes(StandardCharsets.UTF_8);

        // 解析消息
        IotMqttMessage result = parser.parse(topic, payload);

        // 验证结果
        assertNotNull(result);
        assertEquals("device001", result.getId());
        assertEquals("sensor.data", result.getMethod());
        assertNotNull(result.getParams().get("timestamp"));
    }
} 