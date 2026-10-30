package cn.iocoder.yudao.module.iot.core.util;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotProtocolTypeEnum;
import cn.iocoder.yudao.module.iot.core.enums.device.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.core.topic.event.IotDeviceEventPostReqDTO;
import org.junit.jupiter.api.Test;

import java.util.Collections;
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

    @Test
    public void testEncodeRaw_decodeRaw() {
        // 标准 Base64（带 padding）往返
        byte[] bytes = new byte[]{0x01, 0x02, (byte) 0xFF, 0x00};
        String raw = IotDeviceMessageUtils.encodeRaw(bytes);
        assertEquals("AQL/AA==", raw);
        assertArrayEquals(bytes, IotDeviceMessageUtils.decodeRaw(raw, 4));
        // 上限边界：4096 字节
        byte[] max = new byte[IotDeviceMessageUtils.RAW_PAYLOAD_MAX_BYTES];
        assertArrayEquals(max, IotDeviceMessageUtils.decodeRaw(IotDeviceMessageUtils.encodeRaw(max),
                IotDeviceMessageUtils.RAW_PAYLOAD_MAX_BYTES));
    }

    @Test
    public void testDecodeRaw_invalid() {
        // 为空、不是 Base64
        assertThrows(IllegalArgumentException.class, () -> IotDeviceMessageUtils.decodeRaw(null, 10));
        assertThrows(IllegalArgumentException.class, () -> IotDeviceMessageUtils.decodeRaw("", 10));
        assertThrows(IllegalArgumentException.class, () -> IotDeviceMessageUtils.decodeRaw("!!not-base64!!", 10));
        // 不规范：省略 padding、末尾多余的位不为 0（与 AQI= 解出相同字节）
        assertThrows(IllegalArgumentException.class, () -> IotDeviceMessageUtils.decodeRaw("AQI", 10));
        assertThrows(IllegalArgumentException.class, () -> IotDeviceMessageUtils.decodeRaw("AQJ=", 10));
        // 超过上限：Modbus PDU 253 字节
        String tooLong = IotDeviceMessageUtils.encodeRaw(new byte[IotDeviceMessageUtils.MODBUS_PDU_MAX_BYTES + 1]);
        assertThrows(IllegalArgumentException.class,
                () -> IotDeviceMessageUtils.decodeRaw(tooLong, IotDeviceMessageUtils.MODBUS_PDU_MAX_BYTES));
    }

    @Test
    public void testDecodeRawPayload_withoutPadding() {
        // 管理后台的 payload 允许省略 padding，由调用方规范化
        Map<String, Object> params = new HashMap<>();
        params.put("payload", "AQI");
        assertArrayEquals(new byte[]{0x01, 0x02}, IotDeviceMessageUtils.decodeRawPayload(params, 10));
    }

    @Test
    public void testIsCommandRequest() {
        // 下行指令的请求：property.set / service.invoke
        assertTrue(IotDeviceMessageUtils.isCommandRequest(IotDeviceMessage.requestOf("thing.property.set")));
        assertTrue(IotDeviceMessageUtils.isCommandRequest(IotDeviceMessage.requestOf("thing.service.invoke")));
        // 其它方法、回执不是
        assertFalse(IotDeviceMessageUtils.isCommandRequest(IotDeviceMessage.requestOf("thing.ota.upgrade")));
        assertFalse(IotDeviceMessageUtils.isCommandRequest(
                IotDeviceMessage.replyOf("1", "thing.property.set", null, 0, null)));
    }

    @Test
    public void testIsStateOffline() {
        // 离线的状态消息
        assertTrue(IotDeviceMessageUtils.isStateOffline(IotDeviceMessage.buildStateOffline()));
        // 在线的状态消息、没有状态值的状态消息，不是
        assertFalse(IotDeviceMessageUtils.isStateOffline(IotDeviceMessage.buildStateUpdateOnline()));
        assertFalse(IotDeviceMessageUtils.isStateOffline(
                IotDeviceMessage.requestOf(IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod())));
        // 其它方法的消息，即使带有离线的 state 参数，也不是
        assertFalse(IotDeviceMessageUtils.isStateOffline(IotDeviceMessage.requestOf(
                IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod(),
                Collections.singletonMap("state", IotDeviceStateEnum.OFFLINE.getState()))));
    }

    @Test
    public void testValidateRawPayload() {
        // 通用协议：1~4096 字节
        IotDeviceMessageUtils.validateRawPayload(IotProtocolTypeEnum.TCP, new byte[]{0x01});
        IotDeviceMessageUtils.validateRawPayload(IotProtocolTypeEnum.MQTT,
                new byte[IotDeviceMessageUtils.RAW_PAYLOAD_MAX_BYTES]);
        assertThrows(IllegalArgumentException.class,
                () -> IotDeviceMessageUtils.validateRawPayload(IotProtocolTypeEnum.TCP, new byte[0]));
        assertThrows(IllegalArgumentException.class, () -> IotDeviceMessageUtils.validateRawPayload(
                IotProtocolTypeEnum.TCP, new byte[IotDeviceMessageUtils.RAW_PAYLOAD_MAX_BYTES + 1]));
    }

    @Test
    public void testValidateRawPayload_modbus() {
        // 合法的 03 读保持寄存器：地址 0、数量 10
        IotDeviceMessageUtils.validateRawPayload(IotProtocolTypeEnum.MODBUS_TCP_CLIENT, new byte[]{3, 0, 0, 0, 10});
        // 不合法：不支持的功能码、数量超限、超过 253 字节（通用协议允许的长度，Modbus 也拒绝）
        assertThrows(IllegalArgumentException.class, () -> IotDeviceMessageUtils.validateRawPayload(
                IotProtocolTypeEnum.MODBUS_TCP_CLIENT, new byte[]{8, 0, 0, 0, 1}));
        assertThrows(IllegalArgumentException.class, () -> IotDeviceMessageUtils.validateRawPayload(
                IotProtocolTypeEnum.MODBUS_TCP_SERVER, new byte[]{3, 0, 0, 0, 126}));
        byte[] tooLong = new byte[IotDeviceMessageUtils.MODBUS_PDU_MAX_BYTES + 1];
        tooLong[0] = 0x10;
        assertThrows(IllegalArgumentException.class, () -> IotDeviceMessageUtils.validateRawPayload(
                IotProtocolTypeEnum.MODBUS_TCP_CLIENT, tooLong));
    }

}
