package cn.iocoder.yudao.module.iot.core.util;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.event.IotDeviceEventPostReqDTO;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotDeviceMessageUtils} 的单元测试
 *
 * @author HUIHUI
 */
public class IotDeviceMessageUtilsTest {

    @Test
    public void testExtractPropertyValue_directValue() {
        // 测试直接值（非 Map）
        IotDeviceMessage message = new IotDeviceMessage();
        message.setParams(25.5);

        Object result = IotDeviceMessageUtils.extractPropertyValue(message, "temperature");
        assertEquals(25.5, result);
    }

    @Test
    public void testExtractPropertyValue_directIdentifier() {
        // 测试直接通过标识符获取
        IotDeviceMessage message = new IotDeviceMessage();
        Map<String, Object> params = new HashMap<>();
        params.put("temperature", 25.5);
        message.setParams(params);

        Object result = IotDeviceMessageUtils.extractPropertyValue(message, "temperature");
        assertEquals(25.5, result);
    }

    @Test
    public void testExtractPropertyValue_propertiesStructure() {
        // 测试 properties 结构
        IotDeviceMessage message = new IotDeviceMessage();
        Map<String, Object> properties = new HashMap<>();
        properties.put("temperature", 25.5);
        properties.put("humidity", 60);

        Map<String, Object> params = new HashMap<>();
        params.put("properties", properties);
        message.setParams(params);

        Object result = IotDeviceMessageUtils.extractPropertyValue(message, "temperature");
        assertEquals(25.5, result);
    }

    @Test
    public void testExtractPropertyValue_dataStructure() {
        // 测试 data 结构
        IotDeviceMessage message = new IotDeviceMessage();
        Map<String, Object> data = new HashMap<>();
        data.put("temperature", 25.5);

        Map<String, Object> params = new HashMap<>();
        params.put("data", data);
        message.setParams(params);

        Object result = IotDeviceMessageUtils.extractPropertyValue(message, "temperature");
        assertEquals(25.5, result);
    }

    @Test
    public void testExtractPropertyValue_valueField() {
        // 测试 value 字段（策略 5）
        IotDeviceMessage message = new IotDeviceMessage();
        Map<String, Object> params = new HashMap<>();
        params.put("identifier", "temperature");
        params.put("value", 25.5);
        message.setParams(params);

        Object result = IotDeviceMessageUtils.extractPropertyValue(message, "temperature");
        assertEquals(25.5, result);
    }

    @Test
    public void testExtractPropertyValue_singleValueMap() {
        // 测试单值 Map（策略 6：包含 identifier 和一个其他字段）
        IotDeviceMessage message = new IotDeviceMessage();
        Map<String, Object> params = new HashMap<>();
        params.put("identifier", "temperature");
        params.put("actualValue", 25.5);
        message.setParams(params);

        Object result = IotDeviceMessageUtils.extractPropertyValue(message, "temperature");
        assertEquals(25.5, result);
    }

    @Test
    public void testExtractPropertyValue_notFound() {
        // 测试未找到属性值
        IotDeviceMessage message = new IotDeviceMessage();
        Map<String, Object> params = new HashMap<>();
        params.put("humidity", 60);
        message.setParams(params);

        Object result = IotDeviceMessageUtils.extractPropertyValue(message, "temperature");
        assertNull(result);
    }

    @Test
    public void testExtractPropertyValue_nullParams() {
        // 测试 params 为 null
        IotDeviceMessage message = new IotDeviceMessage();
        message.setParams(null);

        Object result = IotDeviceMessageUtils.extractPropertyValue(message, "temperature");
        assertNull(result);
    }

    @Test
    public void testExtractPropertyValue_priorityOrder() {
        // 测试优先级顺序：直接标识符 > properties > data > value
        IotDeviceMessage message = new IotDeviceMessage();

        Map<String, Object> properties = new HashMap<>();
        properties.put("temperature", 20.0);

        Map<String, Object> data = new HashMap<>();
        data.put("temperature", 30.0);

        Map<String, Object> params = new HashMap<>();
        params.put("temperature", 25.5); // 最高优先级
        params.put("properties", properties);
        params.put("data", data);
        params.put("value", 40.0);
        message.setParams(params);

        Object result = IotDeviceMessageUtils.extractPropertyValue(message, "temperature");
        assertEquals(25.5, result); // 应该返回直接标识符的值
    }

    // ========== extractEventValue 测试 ==========

    @Test
    public void testExtractEventValue_scalar() {
        // 标量事件值：{identifier: "gzzt", value: "normal"}
        IotDeviceMessage message = new IotDeviceMessage();
        Map<String, Object> params = new HashMap<>();
        params.put("identifier", "gzzt");
        params.put("value", "normal");
        message.setParams(params);

        Object result = IotDeviceMessageUtils.extractEventValue(message);
        assertEquals("normal", result);
    }

    @Test
    public void testExtractEventValue_struct() {
        // 结构体事件值：{identifier: "alarm", value: {level: "high", message: "..."}}
        IotDeviceMessage message = new IotDeviceMessage();
        Map<String, Object> eventValue = new HashMap<>();
        eventValue.put("level", "high");
        eventValue.put("message", "over temperature");

        Map<String, Object> params = new HashMap<>();
        params.put("identifier", "alarm");
        params.put("value", eventValue);
        message.setParams(params);

        Object result = IotDeviceMessageUtils.extractEventValue(message);
        assertEquals(eventValue, result);
    }

    @Test
    public void testExtractEventValue_nullParams() {
        IotDeviceMessage message = new IotDeviceMessage();
        message.setParams(null);

        Object result = IotDeviceMessageUtils.extractEventValue(message);
        assertNull(result);
    }

    @Test
    public void testExtractEventValue_paramsWithoutValueField() {
        // params 是字符串等非结构化对象，无 value 字段，应返回 null
        IotDeviceMessage message = new IotDeviceMessage();
        message.setParams("not a map");

        Object result = IotDeviceMessageUtils.extractEventValue(message);
        assertNull(result);
    }

    @Test
    public void testExtractEventValue_missingValueField() {
        IotDeviceMessage message = new IotDeviceMessage();
        Map<String, Object> params = new HashMap<>();
        params.put("identifier", "gzzt");
        message.setParams(params);

        Object result = IotDeviceMessageUtils.extractEventValue(message);
        assertNull(result);
    }

    @Test
    public void testExtractEventValue_pojoParams() {
        // 本地总线场景：params 是 IotDeviceEventPostReqDTO POJO（未经 JSON 反序列化），应能反射取到 value
        IotDeviceMessage message = new IotDeviceMessage();
        message.setParams(IotDeviceEventPostReqDTO.of("gzzt", "normal"));

        Object result = IotDeviceMessageUtils.extractEventValue(message);
        assertEquals("normal", result);
    }

    @Test
    public void testGetIdentifier_eventPostPojoParams() {
        // 本地总线场景：EVENT_POST 消息 params 是 DTO POJO，仍应能解析出 identifier
        IotDeviceMessage message = new IotDeviceMessage();
        message.setMethod(IotDeviceMessageMethodEnum.EVENT_POST.getMethod());
        message.setParams(IotDeviceEventPostReqDTO.of("gzzt", "normal"));

        assertEquals("gzzt", IotDeviceMessageUtils.getIdentifier(message));
    }

    // ========== notContainsIdentifier 测试 ==========

    /**
     * 测试 notContainsIdentifier 与 containsIdentifier 的互补性
     * **Property 2: notContainsIdentifier 与 containsIdentifier 互补性**
     * **Validates: Requirements 4.1**
     */
    @Test
    public void testNotContainsIdentifier_complementary_whenContains() {
        // 准备参数：消息包含指定标识符
        IotDeviceMessage message = new IotDeviceMessage();
        message.setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());
        Map<String, Object> params = new HashMap<>();
        params.put("temperature", 25);
        message.setParams(params);
        String identifier = "temperature";

        // 调用 & 断言：验证互补性
        boolean containsResult = IotDeviceMessageUtils.containsIdentifier(message, identifier);
        boolean notContainsResult = IotDeviceMessageUtils.notContainsIdentifier(message, identifier);
        assertTrue(containsResult);
        assertFalse(notContainsResult);
        assertEquals(!containsResult, notContainsResult);
    }

    /**
     * 测试 notContainsIdentifier 与 containsIdentifier 的互补性
     * **Property 2: notContainsIdentifier 与 containsIdentifier 互补性**
     * **Validates: Requirements 4.1**
     */
    @Test
    public void testNotContainsIdentifier_complementary_whenNotContains() {
        // 准备参数：消息不包含指定标识符
        IotDeviceMessage message = new IotDeviceMessage();
        message.setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());
        Map<String, Object> params = new HashMap<>();
        params.put("temperature", 25);
        message.setParams(params);
        String identifier = "humidity";

        // 调用 & 断言：验证互补性
        boolean containsResult = IotDeviceMessageUtils.containsIdentifier(message, identifier);
        boolean notContainsResult = IotDeviceMessageUtils.notContainsIdentifier(message, identifier);
        assertFalse(containsResult);
        assertTrue(notContainsResult);
        assertEquals(!containsResult, notContainsResult);
    }

    /**
     * 测试 notContainsIdentifier 与 containsIdentifier 的互补性 - 空参数场景
     * **Property 2: notContainsIdentifier 与 containsIdentifier 互补性**
     * **Validates: Requirements 4.1**
     */
    @Test
    public void testNotContainsIdentifier_complementary_nullParams() {
        // 准备参数：params 为 null
        IotDeviceMessage message = new IotDeviceMessage();
        message.setParams(null);
        String identifier = "temperature";

        // 调用 & 断言：验证互补性
        boolean containsResult = IotDeviceMessageUtils.containsIdentifier(message, identifier);
        boolean notContainsResult = IotDeviceMessageUtils.notContainsIdentifier(message, identifier);
        assertFalse(containsResult);
        assertTrue(notContainsResult);
        assertEquals(!containsResult, notContainsResult);
    }

    // ========== getPropertyIdentifiers 测试 ==========

    @Test
    public void testGetPropertyIdentifiers_flatStructure() {
        // 扁平结构：顶层 key 即标识符
        IotDeviceMessage message = new IotDeviceMessage();
        Map<String, Object> params = new HashMap<>();
        params.put("temperature", 25.5);
        params.put("humidity", 60);
        message.setParams(params);

        Set<String> identifiers = IotDeviceMessageUtils.getPropertyIdentifiers(message);
        assertEquals(2, identifiers.size());
        assertTrue(identifiers.contains("temperature"));
        assertTrue(identifiers.contains("humidity"));
    }

    @Test
    public void testGetPropertyIdentifiers_nullMessage() {
        // 入参为 null：返回空集合
        Set<String> identifiers = IotDeviceMessageUtils.getPropertyIdentifiers(null);
        assertNotNull(identifiers);
        assertTrue(identifiers.isEmpty());
    }

    @Test
    public void testGetPropertyIdentifiers_nullParams() {
        // params 为 null：返回空集合
        IotDeviceMessage message = new IotDeviceMessage();
        message.setParams(null);

        Set<String> identifiers = IotDeviceMessageUtils.getPropertyIdentifiers(message);
        assertTrue(identifiers.isEmpty());
    }

    @Test
    public void testGetPropertyIdentifiers_emptyParams() {
        // params 为空 Map：返回空集合
        IotDeviceMessage message = new IotDeviceMessage();
        message.setParams(new HashMap<>());

        Set<String> identifiers = IotDeviceMessageUtils.getPropertyIdentifiers(message);
        assertTrue(identifiers.isEmpty());
    }

    @Test
    public void testGetPropertyIdentifiers_jsonStringParams() {
        // params 为 JSON 字符串：parseParamsToMap 解析后正常提取顶层标识符
        IotDeviceMessage message = new IotDeviceMessage();
        message.setParams("{\"temperature\":25.5,\"humidity\":60}");

        Set<String> identifiers = IotDeviceMessageUtils.getPropertyIdentifiers(message);
        assertEquals(2, identifiers.size());
        assertTrue(identifiers.contains("temperature"));
        assertTrue(identifiers.contains("humidity"));
    }

}
