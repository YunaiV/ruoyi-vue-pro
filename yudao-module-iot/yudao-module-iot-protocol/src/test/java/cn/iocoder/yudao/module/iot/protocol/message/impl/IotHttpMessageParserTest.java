package cn.iocoder.yudao.module.iot.protocol.message.impl;

import cn.hutool.json.JSONObject;
import cn.iocoder.yudao.module.iot.protocol.message.IotAlinkMessage;
import cn.iocoder.yudao.module.iot.protocol.message.IotStandardResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotHttpMessageParser} 单元测试
 * <p>
 * 测试阿里云IoT平台HTTPS协议标准的消息解析功能
 *
 * @author haohao
 */
class IotHttpMessageParserTest {

    private IotHttpMessageParser parser;

    @BeforeEach
    void setUp() {
        parser = new IotHttpMessageParser();
    }

    @Test
    void testCanHandle() {
        // 测试能处理的路径
        assertTrue(parser.canHandle("/auth"));
        assertTrue(parser.canHandle("/topic/sys/test/device1/thing/service/property/set"));
        assertTrue(parser.canHandle("/topic/test/device1/user/get"));

        // 测试不能处理的路径
        assertFalse(parser.canHandle("/sys/test/device1/thing/service/property/set"));
        assertFalse(parser.canHandle("/unknown/path"));
        assertFalse(parser.canHandle(null));
        assertFalse(parser.canHandle(""));
    }

    @Test
    void testParseAuthMessage() {
        // 构建认证消息
        JSONObject authMessage = new JSONObject();
        authMessage.set("productKey", "a1GFjLP****");
        authMessage.set("deviceName", "device123");
        authMessage.set("clientId", "device123_001");
        authMessage.set("timestamp", "1501668289957");
        authMessage.set("sign", "4870141D4067227128CBB4377906C3731CAC221C");
        authMessage.set("signmethod", "hmacsha1");
        authMessage.set("version", "default");

        String topic = "/auth";
        byte[] payload = authMessage.toString().getBytes(StandardCharsets.UTF_8);

        // 解析消息
        IotAlinkMessage result = parser.parse(topic, payload);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("device.auth", result.getMethod());
        assertEquals("default", result.getVersion());
        assertNotNull(result.getParams());

        Map<String, Object> params = result.getParams();
        assertEquals("a1GFjLP****", params.get("productKey"));
        assertEquals("device123", params.get("deviceName"));
        assertEquals("device123_001", params.get("clientId"));
        assertEquals("1501668289957", params.get("timestamp"));
        assertEquals("4870141D4067227128CBB4377906C3731CAC221C", params.get("sign"));
        assertEquals("hmacsha1", params.get("signmethod"));
    }

    @Test
    void testParseAuthMessageWithMissingFields() {
        // 构建缺少必需字段的认证消息
        JSONObject authMessage = new JSONObject();
        authMessage.set("productKey", "a1GFjLP****");
        authMessage.set("deviceName", "device123");
        // 缺少 clientId 和 sign

        String topic = "/auth";
        byte[] payload = authMessage.toString().getBytes(StandardCharsets.UTF_8);

        // 解析消息
        IotAlinkMessage result = parser.parse(topic, payload);

        // 验证结果
        assertNull(result);
    }

    @Test
    void testParseJsonDataMessage() {
        // 构建JSON格式的数据消息
        JSONObject dataMessage = new JSONObject();
        dataMessage.set("id", "123456");
        dataMessage.set("version", "1.0");
        dataMessage.set("method", "thing.event.property.post");

        JSONObject params = new JSONObject();
        JSONObject properties = new JSONObject();
        properties.set("temperature", 25.6);
        properties.set("humidity", 60.3);
        params.set("properties", properties);
        dataMessage.set("params", params);

        String topic = "/topic/sys/a1GFjLP****/device123/thing/event/property/post";
        byte[] payload = dataMessage.toString().getBytes(StandardCharsets.UTF_8);

        // 解析消息
        IotAlinkMessage result = parser.parse(topic, payload);

        // 验证结果
        assertNotNull(result);
        assertEquals("123456", result.getId());
        assertEquals("thing.event.property.post", result.getMethod());
        assertEquals("1.0", result.getVersion());
        assertNotNull(result.getParams());
        assertNotNull(result.getParams().get("properties"));
    }

    @Test
    void testParseRawDataMessage() {
        // 原始数据消息
        String rawData = "temperature:25.6,humidity:60.3";
        String topic = "/topic/sys/a1GFjLP****/device123/thing/event/property/post";
        byte[] payload = rawData.getBytes(StandardCharsets.UTF_8);

        // 解析消息
        IotAlinkMessage result = parser.parse(topic, payload);

        // 验证结果
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("thing.event.property.post", result.getMethod());
        assertEquals("1.0", result.getVersion());
        assertNotNull(result.getParams());
        assertEquals(rawData, result.getParams().get("data"));
    }

    @Test
    void testInferMethodFromTopic() {
        // 测试系统主题方法推断
        testInferMethod("/sys/test/device/thing/service/property/set", "thing.service.property.set");
        testInferMethod("/sys/test/device/thing/service/property/get", "thing.service.property.get");
        testInferMethod("/sys/test/device/thing/event/property/post", "thing.event.property.post");
        testInferMethod("/sys/test/device/thing/event/alarm/post", "thing.event.alarm.post");
        testInferMethod("/sys/test/device/thing/service/reboot", "thing.service.reboot");

        // 测试自定义主题
        testInferMethod("/test/device/user/get", "custom.message");
    }

    private void testInferMethod(String actualTopic, String expectedMethod) {
        String topic = "/topic" + actualTopic;
        String rawData = "test data";
        byte[] payload = rawData.getBytes(StandardCharsets.UTF_8);

        IotAlinkMessage result = parser.parse(topic, payload);
        assertNotNull(result);
        assertEquals(expectedMethod, result.getMethod());
    }

    @Test
    void testFormatAuthResponse() {
        // 创建认证成功响应
        Map<String, Object> data = new HashMap<>();
        data.put("token", "6944e5bfb92e4d4ea3918d1eda39****");

        IotStandardResponse response = IotStandardResponse.success("auth123", "device.auth", data);

        // 格式化响应
        byte[] result = parser.formatResponse(response);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.length > 0);

        String responseStr = new String(result, StandardCharsets.UTF_8);
        JSONObject responseJson = new JSONObject(responseStr);

        assertEquals(200, responseJson.getInt("code"));
        assertEquals("success", responseJson.getStr("message"));
        assertNotNull(responseJson.get("info"));

        JSONObject info = responseJson.getJSONObject("info");
        assertEquals("6944e5bfb92e4d4ea3918d1eda39****", info.getStr("token"));
    }

    @Test
    void testFormatDataResponse() {
        // 创建数据上报响应
        IotStandardResponse response = IotStandardResponse.success("123456", "thing.event.property.post", null);

        // 格式化响应
        byte[] result = parser.formatResponse(response);

        // 验证结果
        assertNotNull(result);
        assertTrue(result.length > 0);

        String responseStr = new String(result, StandardCharsets.UTF_8);
        JSONObject responseJson = new JSONObject(responseStr);

        assertEquals(200, responseJson.getInt("code"));
        assertEquals("success", responseJson.getStr("message"));
        assertNotNull(responseJson.get("info"));

        JSONObject info = responseJson.getJSONObject("info");
        assertEquals("123456", info.getStr("messageId"));
    }

    @Test
    void testParseInvalidMessage() {
        String topic = "/topic/sys/test/device/thing/service/property/set";

        // 测试空消息
        assertNull(parser.parse(topic, null));
        assertNull(parser.parse(topic, new byte[0]));

        // 测试不支持的路径
        byte[] validPayload = "test data".getBytes(StandardCharsets.UTF_8);
        assertNull(parser.parse("/unknown/path", validPayload));
    }

    @Test
    void testParseDeviceKey() {
        // 测试有效的设备标识
        String[] result1 = IotHttpMessageParser.parseDeviceKey("productKey/deviceName");
        assertNotNull(result1);
        assertEquals(2, result1.length);
        assertEquals("productKey", result1[0]);
        assertEquals("deviceName", result1[1]);

        // 测试无效的设备标识
        assertNull(IotHttpMessageParser.parseDeviceKey(null));
        assertNull(IotHttpMessageParser.parseDeviceKey(""));
        assertNull(IotHttpMessageParser.parseDeviceKey("invalid"));
        assertNull(IotHttpMessageParser.parseDeviceKey("product/device/extra"));
    }

    @Test
    void testBuildDeviceKey() {
        // 测试构建设备标识
        assertEquals("productKey/deviceName",
                IotHttpMessageParser.buildDeviceKey("productKey", "deviceName"));

        // 测试无效参数
        assertNull(IotHttpMessageParser.buildDeviceKey(null, "deviceName"));
        assertNull(IotHttpMessageParser.buildDeviceKey("productKey", null));
        assertNull(IotHttpMessageParser.buildDeviceKey("", "deviceName"));
        assertNull(IotHttpMessageParser.buildDeviceKey("productKey", ""));
    }
}