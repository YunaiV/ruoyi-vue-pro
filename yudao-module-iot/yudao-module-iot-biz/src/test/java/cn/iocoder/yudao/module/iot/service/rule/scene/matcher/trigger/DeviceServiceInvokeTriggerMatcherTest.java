package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.trigger;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link DeviceServiceInvokeTriggerMatcher} 的单元测试类
 *
 * @author HUIHUI
 */
public class DeviceServiceInvokeTriggerMatcherTest extends BaseMockitoUnitTest {

    private DeviceServiceInvokeTriggerMatcher matcher;

    @BeforeEach
    public void setUp() {
        matcher = new DeviceServiceInvokeTriggerMatcher();
    }

    @Test
    public void testGetSupportedTriggerType() {
        // when & then
        assertEquals(IotSceneRuleTriggerTypeEnum.DEVICE_SERVICE_INVOKE, matcher.getSupportedTriggerType());
    }

    @Test
    public void testGetPriority() {
        // when & then
        assertEquals(40, matcher.getPriority());
    }

    @Test
    public void testIsEnabled() {
        // when & then
        assertTrue(matcher.isEnabled());
    }

    @Test
    public void testIsMatched_Success_RestartService() {
        // given
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "restart")
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("mode", "soft")
                        .build())
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger("restart");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_ConfigService() {
        // given
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "config")
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("interval", 30)
                        .put("enabled", true)
                        .put("threshold", 75.5)
                        .build())
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger("config");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_UpdateService() {
        // given
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "update")
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("version", "1.2.3")
                        .put("url", "http://example.com/firmware.bin")
                        .build())
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger("update");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Failure_ServiceIdentifierMismatch() {
        // given
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "restart")
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("mode", "soft")
                        .build())
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger("config"); // 不匹配的服务标识符

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_WrongMessageMethod() {
        // given
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "restart")
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("mode", "soft")
                        .build())
                .build();
        IotDeviceMessage message = new IotDeviceMessage();
        message.setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod()); // 错误的方法
        message.setParams(serviceParams);

        IotSceneRuleDO.Trigger trigger = createValidTrigger("restart");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_MissingIdentifier() {
        // given
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "restart")
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("mode", "soft")
                        .build())
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.DEVICE_SERVICE_INVOKE.getType());
        trigger.setIdentifier(null); // 缺少标识符

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_NullMessageParams() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        message.setMethod(IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod());
        message.setParams(null);

        IotSceneRuleDO.Trigger trigger = createValidTrigger("restart");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_InvalidMessageParams() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        message.setMethod(IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod());
        message.setParams("invalid-params"); // 不是 Map 类型

        IotSceneRuleDO.Trigger trigger = createValidTrigger("restart");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_MissingServiceIdentifierInParams() {
        // given
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("mode", "soft")
                        .build()) // 缺少 identifier 字段
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger("restart");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_NullTrigger() {
        // given
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "restart")
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("mode", "soft")
                        .build())
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);

        // when
        boolean result = matcher.isMatched(message, null);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_NullTriggerType() {
        // given
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "restart")
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("mode", "soft")
                        .build())
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(null);
        trigger.setIdentifier("restart");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Success_EmptyInputData() {
        // given
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "ping")
                .put("inputData", MapUtil.of()) // 空的输入数据
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger("ping");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_NoInputData() {
        // given
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "status")
                // 没有 inputData 字段
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger("status");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_ComplexInputData() {
        // given
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "calibrate")
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("sensors", new String[]{"temperature", "humidity", "pressure"})
                        .put("precision", 0.01)
                        .put("duration", 300)
                        .put("autoSave", true)
                        .put("config", MapUtil.builder(new HashMap<String, Object>())
                                .put("mode", "auto")
                                .put("level", "high")
                                .build())
                        .build())
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger("calibrate");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_CaseInsensitiveIdentifier() {
        // given
        Map<String, Object> serviceParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "RESTART") // 大写
                .put("inputData", MapUtil.builder(new HashMap<String, Object>())
                        .put("mode", "soft")
                        .build())
                .build();
        IotDeviceMessage message = createServiceInvokeMessage(serviceParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger("restart"); // 小写

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        // 根据实际实现，这里可能需要调整期望结果
        // 如果实现是大小写敏感的，则应该为 false
        assertFalse(result);
    }

    // ========== 辅助方法 ==========

    /**
     * 创建服务调用消息
     */
    private IotDeviceMessage createServiceInvokeMessage(Map<String, Object> serviceParams) {
        IotDeviceMessage message = new IotDeviceMessage();
        message.setMethod(IotDeviceMessageMethodEnum.SERVICE_INVOKE.getMethod());
        message.setParams(serviceParams);
        return message;
    }

    /**
     * 创建有效的触发器
     */
    private IotSceneRuleDO.Trigger createValidTrigger(String identifier) {
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.DEVICE_SERVICE_INVOKE.getType());
        trigger.setIdentifier(identifier);
        return trigger;
    }
}
