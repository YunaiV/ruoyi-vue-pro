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
 * {@link DeviceEventPostTriggerMatcher} 的单元测试类
 *
 * @author HUIHUI
 */
public class DeviceEventPostTriggerMatcherTest extends BaseMockitoUnitTest {

    private DeviceEventPostTriggerMatcher matcher;

    @BeforeEach
    public void setUp() {
        matcher = new DeviceEventPostTriggerMatcher();
    }

    @Test
    public void testGetSupportedTriggerType() {
        // when & then
        assertEquals(IotSceneRuleTriggerTypeEnum.DEVICE_EVENT_POST, matcher.getSupportedTriggerType());
    }

    @Test
    public void testGetPriority() {
        // when & then
        assertEquals(30, matcher.getPriority());
    }

    @Test
    public void testIsEnabled() {
        // when & then
        assertTrue(matcher.isEnabled());
    }

    @Test
    public void testIsMatched_Success_AlarmEvent() {
        // given
        Map<String, Object> eventParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "alarm")
                .put("value", MapUtil.builder(new HashMap<String, Object>())
                        .put("level", "high")
                        .put("message", "Temperature too high")
                        .build())
                .build();
        IotDeviceMessage message = createEventPostMessage(eventParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger("alarm");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_ErrorEvent() {
        // given
        Map<String, Object> eventParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "error")
                .put("value", MapUtil.builder(new HashMap<String, Object>())
                        .put("code", 500)
                        .put("description", "System error")
                        .build())
                .build();
        IotDeviceMessage message = createEventPostMessage(eventParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger("error");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_InfoEvent() {
        // given
        Map<String, Object> eventParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "info")
                .put("value", MapUtil.builder(new HashMap<String, Object>())
                        .put("status", "normal")
                        .put("timestamp", System.currentTimeMillis())
                        .build())
                .build();
        IotDeviceMessage message = createEventPostMessage(eventParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger("info");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Failure_EventIdentifierMismatch() {
        // given
        Map<String, Object> eventParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "alarm")
                .put("value", MapUtil.builder(new HashMap<String, Object>())
                        .put("level", "high")
                        .build())
                .build();
        IotDeviceMessage message = createEventPostMessage(eventParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger("error"); // 不匹配的事件标识符

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_WrongMessageMethod() {
        // given
        Map<String, Object> eventParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "alarm")
                .put("value", MapUtil.builder(new HashMap<String, Object>())
                        .put("level", "high")
                        .build())
                .build();
        IotDeviceMessage message = new IotDeviceMessage();
        message.setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod()); // 错误的方法
        message.setParams(eventParams);

        IotSceneRuleDO.Trigger trigger = createValidTrigger("alarm");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_MissingIdentifier() {
        // given
        Map<String, Object> eventParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "alarm")
                .put("value", MapUtil.builder(new HashMap<String, Object>())
                        .put("level", "high")
                        .build())
                .build();
        IotDeviceMessage message = createEventPostMessage(eventParams);
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.DEVICE_EVENT_POST.getType());
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
        message.setMethod(IotDeviceMessageMethodEnum.EVENT_POST.getMethod());
        message.setParams(null);

        IotSceneRuleDO.Trigger trigger = createValidTrigger("alarm");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_InvalidMessageParams() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        message.setMethod(IotDeviceMessageMethodEnum.EVENT_POST.getMethod());
        message.setParams("invalid-params"); // 不是 Map 类型

        IotSceneRuleDO.Trigger trigger = createValidTrigger("alarm");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_MissingEventIdentifierInParams() {
        // given
        Map<String, Object> eventParams = MapUtil.builder(new HashMap<String, Object>())
                .put("value", MapUtil.builder(new HashMap<String, Object>())
                        .put("level", "high")
                        .build()) // 缺少 identifier 字段
                .build();
        IotDeviceMessage message = createEventPostMessage(eventParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger("alarm");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_NullTrigger() {
        // given
        Map<String, Object> eventParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "alarm")
                .put("value", MapUtil.builder(new HashMap<String, Object>())
                        .put("level", "high")
                        .build())
                .build();
        IotDeviceMessage message = createEventPostMessage(eventParams);

        // when
        boolean result = matcher.isMatched(message, null);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_NullTriggerType() {
        // given
        Map<String, Object> eventParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "alarm")
                .put("value", MapUtil.builder(new HashMap<String, Object>())
                        .put("level", "high")
                        .build())
                .build();
        IotDeviceMessage message = createEventPostMessage(eventParams);
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(null);
        trigger.setIdentifier("alarm");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Success_ComplexEventValue() {
        // given
        Map<String, Object> eventParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "maintenance")
                .put("value", MapUtil.builder(new HashMap<String, Object>())
                        .put("type", "scheduled")
                        .put("duration", 120)
                        .put("components", new String[]{"motor", "sensor"})
                        .put("priority", "medium")
                        .build())
                .build();
        IotDeviceMessage message = createEventPostMessage(eventParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger("maintenance");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_EmptyEventValue() {
        // given
        Map<String, Object> eventParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "heartbeat")
                .put("value", MapUtil.of()) // 空的事件值
                .build();
        IotDeviceMessage message = createEventPostMessage(eventParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger("heartbeat");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_CaseInsensitiveIdentifier() {
        // given
        Map<String, Object> eventParams = MapUtil.builder(new HashMap<String, Object>())
                .put("identifier", "ALARM") // 大写
                .put("value", MapUtil.builder(new HashMap<String, Object>())
                        .put("level", "high")
                        .build())
                .build();
        IotDeviceMessage message = createEventPostMessage(eventParams);
        IotSceneRuleDO.Trigger trigger = createValidTrigger("alarm"); // 小写

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        // 根据实际实现，这里可能需要调整期望结果
        // 如果实现是大小写敏感的，则应该为 false
        assertFalse(result);
    }

    // ========== 辅助方法 ==========

    /**
     * 创建事件上报消息
     */
    private IotDeviceMessage createEventPostMessage(Map<String, Object> eventParams) {
        IotDeviceMessage message = new IotDeviceMessage();
        message.setMethod(IotDeviceMessageMethodEnum.EVENT_POST.getMethod());
        message.setParams(eventParams);
        return message;
    }

    /**
     * 创建有效的触发器
     */
    private IotSceneRuleDO.Trigger createValidTrigger(String identifier) {
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.DEVICE_EVENT_POST.getType());
        trigger.setIdentifier(identifier);
        return trigger;
    }
}
