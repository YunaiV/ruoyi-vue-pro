package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.condition;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionOperatorEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link DevicePropertyConditionMatcher} 的单元测试类
 *
 * @author HUIHUI
 */
public class DevicePropertyConditionMatcherTest extends BaseMockitoUnitTest {

    private DevicePropertyConditionMatcher matcher;

    @BeforeEach
    public void setUp() {
        matcher = new DevicePropertyConditionMatcher();
    }

    @Test
    public void testGetSupportedConditionType() {
        // when & then
        assertEquals(IotSceneRuleConditionTypeEnum.DEVICE_PROPERTY, matcher.getSupportedConditionType());
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
    public void testIsMatched_Success_TemperatureEquals() {
        // given
        Map<String, Object> properties = MapUtil.of("temperature", 25.5);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                "temperature",
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                "25.5"
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_HumidityGreaterThan() {
        // given
        Map<String, Object> properties = MapUtil.of("humidity", 75);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                "humidity",
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                "70"
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_PressureLessThan() {
        // given
        Map<String, Object> properties = MapUtil.of("pressure", 1010.5);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                "pressure",
                IotSceneRuleConditionOperatorEnum.LESS_THAN.getOperator(),
                "1020"
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_StatusNotEquals() {
        // given
        Map<String, Object> properties = MapUtil.of("status", "active");
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                "status",
                IotSceneRuleConditionOperatorEnum.NOT_EQUALS.getOperator(),
                "inactive"
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Failure_PropertyMismatch() {
        // given
        Map<String, Object> properties = MapUtil.of("temperature", 15.0);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                "temperature",
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                "20"
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_PropertyNotFound() {
        // given
        Map<String, Object> properties = MapUtil.of("temperature", 25.5);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                "humidity", // 不存在的属性
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                "50"
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_NullCondition() {
        // given
        Map<String, Object> properties = MapUtil.of("temperature", 25.5);
        IotDeviceMessage message = createDeviceMessage(properties);

        // when
        boolean result = matcher.isMatched(message, null);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_NullConditionType() {
        // given
        Map<String, Object> properties = MapUtil.of("temperature", 25.5);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(null);

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_MissingIdentifier() {
        // given
        Map<String, Object> properties = MapUtil.of("temperature", 25.5);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(IotSceneRuleConditionTypeEnum.DEVICE_PROPERTY.getType());
        condition.setIdentifier(null); // 缺少标识符
        condition.setOperator(IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator());
        condition.setParam("20");

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_MissingOperator() {
        // given
        Map<String, Object> properties = MapUtil.of("temperature", 25.5);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(IotSceneRuleConditionTypeEnum.DEVICE_PROPERTY.getType());
        condition.setIdentifier("temperature");
        condition.setOperator(null); // 缺少操作符
        condition.setParam("20");

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_MissingParam() {
        // given
        Map<String, Object> properties = MapUtil.of("temperature", 25.5);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(IotSceneRuleConditionTypeEnum.DEVICE_PROPERTY.getType());
        condition.setIdentifier("temperature");
        condition.setOperator(IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator());
        condition.setParam(null); // 缺少参数

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_NullMessage() {
        // given
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                "temperature",
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                "20"
        );

        // when
        boolean result = matcher.isMatched(null, condition);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_NullDeviceProperties() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        message.setParams(null);

        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                "temperature",
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                "20"
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Success_GreaterThanOrEquals() {
        // given
        Map<String, Object> properties = MapUtil.of("voltage", 12.0);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                "voltage",
                IotSceneRuleConditionOperatorEnum.GREATER_THAN_OR_EQUALS.getOperator(),
                "12.0"
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_LessThanOrEquals() {
        // given
        Map<String, Object> properties = MapUtil.of("current", 2.5);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                "current",
                IotSceneRuleConditionOperatorEnum.LESS_THAN_OR_EQUALS.getOperator(),
                "3.0"
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_StringProperty() {
        // given
        Map<String, Object> properties = MapUtil.of("mode", "auto");
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                "mode",
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                "auto"
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_BooleanProperty() {
        // given
        Map<String, Object> properties = MapUtil.of("enabled", true);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                "enabled",
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                "true"
        );

        // when
        boolean result = matcher.isMatched(message, condition);

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
                .put("enabled", true)
                .build();
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                "humidity",
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                "60"
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertTrue(result);
    }

    // ========== 辅助方法 ==========

    /**
     * 创建设备消息
     */
    private IotDeviceMessage createDeviceMessage(Map<String, Object> properties) {
        IotDeviceMessage message = new IotDeviceMessage();
        message.setParams(properties);
        return message;
    }

    /**
     * 创建有效的条件
     */
    private IotSceneRuleDO.TriggerCondition createValidCondition(String identifier, String operator, String param) {
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(IotSceneRuleConditionTypeEnum.DEVICE_PROPERTY.getType());
        condition.setIdentifier(identifier);
        condition.setOperator(operator);
        condition.setParam(param);
        return condition;
    }
}
