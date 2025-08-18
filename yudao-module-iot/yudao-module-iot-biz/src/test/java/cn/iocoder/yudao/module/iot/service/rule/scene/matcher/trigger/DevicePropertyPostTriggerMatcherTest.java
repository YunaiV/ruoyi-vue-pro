package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.trigger;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionOperatorEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link DevicePropertyPostTriggerMatcher} 的单元测试类
 *
 * @author HUIHUI
 */
public class DevicePropertyPostTriggerMatcherTest extends BaseMockitoUnitTest {

    private DevicePropertyPostTriggerMatcher matcher;

    @BeforeEach
    public void setUp() {
        matcher = new DevicePropertyPostTriggerMatcher();
    }

    @Test
    public void testGetSupportedTriggerType() {
        // when & then
        assertEquals(IotSceneRuleTriggerTypeEnum.DEVICE_PROPERTY_POST, matcher.getSupportedTriggerType());
    }

    @Test
    public void testGetPriority() {
        // when & then
        assertEquals(20, matcher.getPriority());
    }

    @Test
    public void testIsEnabled() {
        // when & then
        assertTrue(matcher.isEnabled());
    }

    @Test
    public void testIsMatched_Success_TemperatureProperty() {
        // given
        Map<String, Object> properties = MapUtil.builder(new HashMap<String, Object>())
                .put("temperature", 25.5)
                .build();
        IotDeviceMessage message = createPropertyPostMessage(properties);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                "temperature",
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                "20"
        );

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_HumidityProperty() {
        // given
        Map<String, Object> properties = MapUtil.builder(new HashMap<String, Object>())
                .put("humidity", 60)
                .build();
        IotDeviceMessage message = createPropertyPostMessage(properties);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                "humidity",
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                "60"
        );

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Failure_PropertyMismatch() {
        // given
        Map<String, Object> properties = MapUtil.builder(new HashMap<String, Object>())
                .put("temperature", 15.0)
                .build();
        IotDeviceMessage message = createPropertyPostMessage(properties);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                "temperature",
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                "20"
        );

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_PropertyNotFound() {
        // given
        Map<String, Object> properties = MapUtil.builder(new HashMap<String, Object>())
                .put("temperature", 25.5)
                .build();
        IotDeviceMessage message = createPropertyPostMessage(properties);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                "humidity", // 不存在的属性
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                "50"
        );

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_WrongMessageMethod() {
        // given
        Map<String, Object> properties = MapUtil.builder(new HashMap<String, Object>())
                .put("temperature", 25.5)
                .build();
        IotDeviceMessage message = new IotDeviceMessage();
        message.setMethod(IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod());
        message.setParams(properties);

        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                "temperature",
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                "20"
        );

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_MissingIdentifier() {
        // given
        Map<String, Object> properties = MapUtil.builder(new HashMap<String, Object>())
                .put("temperature", 25.5)
                .build();
        IotDeviceMessage message = createPropertyPostMessage(properties);
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.DEVICE_PROPERTY_POST.getType());
        trigger.setIdentifier(null); // 缺少标识符
        trigger.setOperator(IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator());
        trigger.setValue("20");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_NullMessageParams() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        message.setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());
        message.setParams(null);

        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                "temperature",
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                "20"
        );

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_InvalidMessageParams() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        message.setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());
        message.setParams("invalid-params"); // 不是 Map 类型

        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                "temperature",
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                "20"
        );

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Success_LessThanOperator() {
        // given
        Map<String, Object> properties = MapUtil.builder(new HashMap<String, Object>())
                .put("temperature", 15.0)
                .build();
        IotDeviceMessage message = createPropertyPostMessage(properties);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                "temperature",
                IotSceneRuleConditionOperatorEnum.LESS_THAN.getOperator(),
                "20"
        );

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_NotEqualsOperator() {
        // given
        Map<String, Object> properties = MapUtil.builder(new HashMap<String, Object>())
                .put("status", "active")
                .build();
        IotDeviceMessage message = createPropertyPostMessage(properties);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                "status",
                IotSceneRuleConditionOperatorEnum.NOT_EQUALS.getOperator(),
                "inactive"
        );

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_MultipleProperties() {
        // given
        Map<String, Object> properties = MapUtil.builder(new HashMap<String, Object>())
                .put("temperature", 25.5)
                .put("humidity", 60)
                .put("status", "active")
                .build();
        IotDeviceMessage message = createPropertyPostMessage(properties);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                "humidity",
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                "60"
        );

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    // ========== 辅助方法 ==========

    /**
     * 创建属性上报消息
     */
    private IotDeviceMessage createPropertyPostMessage(Map<String, Object> properties) {
        IotDeviceMessage message = new IotDeviceMessage();
        message.setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());
        message.setParams(properties);
        return message;
    }

    /**
     * 创建有效的触发器
     */
    private IotSceneRuleDO.Trigger createValidTrigger(String identifier, String operator, String value) {
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.DEVICE_PROPERTY_POST.getType());
        trigger.setIdentifier(identifier);
        trigger.setOperator(operator);
        trigger.setValue(value);
        return trigger;
    }
}
