package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.condition;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionOperatorEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link DevicePropertyConditionMatcher} 的单元测试
 *
 * @author HUIHUI
 */
public class DevicePropertyConditionMatcherTest extends BaseMockitoUnitTest {

    @InjectMocks
    private DevicePropertyConditionMatcher matcher;

    @Test
    public void testGetSupportedConditionType() {
        // 调用
        IotSceneRuleConditionTypeEnum result = matcher.getSupportedConditionType();

        // 断言
        assertEquals(IotSceneRuleConditionTypeEnum.DEVICE_PROPERTY, result);
    }

    @Test
    public void testGetPriority() {
        // 调用
        int result = matcher.getPriority();

        // 断言
        assertEquals(20, result);
    }

    @Test
    public void testIsEnabled() {
        // 调用
        boolean result = matcher.isEnabled();

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_temperatureEquals_success() {
        // 准备参数
        String propertyName = "temperature";
        Double propertyValue = 25.5;
        Map<String, Object> properties = MapUtil.of(propertyName, propertyValue);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                propertyName,
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                String.valueOf(propertyValue)
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_humidityGreaterThan_success() {
        // 准备参数
        String propertyName = "humidity";
        Integer propertyValue = 75;
        Integer compareValue = 70;
        Map<String, Object> properties = MapUtil.of(propertyName, propertyValue);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                propertyName,
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                String.valueOf(compareValue)
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_pressureLessThan_success() {
        // 准备参数
        String propertyName = "pressure";
        Double propertyValue = 1010.5;
        Integer compareValue = 1020;
        Map<String, Object> properties = MapUtil.of(propertyName, propertyValue);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                propertyName,
                IotSceneRuleConditionOperatorEnum.LESS_THAN.getOperator(),
                String.valueOf(compareValue)
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_statusNotEquals_success() {
        // 准备参数
        String propertyName = "status";
        String propertyValue = "active";
        String compareValue = "inactive";
        Map<String, Object> properties = MapUtil.of(propertyName, propertyValue);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                propertyName,
                IotSceneRuleConditionOperatorEnum.NOT_EQUALS.getOperator(),
                compareValue
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_propertyMismatch_fail() {
        // 准备参数
        String propertyName = "temperature";
        Double propertyValue = 15.0;
        Integer compareValue = 20;
        Map<String, Object> properties = MapUtil.of(propertyName, propertyValue);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                propertyName,
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                String.valueOf(compareValue)
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_propertyNotFound_fail() {
        // 准备参数
        Map<String, Object> properties = MapUtil.of("temperature", 25.5);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                randomString(), // 随机不存在的属性名
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                "50"
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullCondition_fail() {
        // 准备参数
        Map<String, Object> properties = MapUtil.of("temperature", 25.5);
        IotDeviceMessage message = createDeviceMessage(properties);

        // 调用
        boolean result = matcher.matches(message, null);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullConditionType_fail() {
        // 准备参数
        Map<String, Object> properties = MapUtil.of("temperature", 25.5);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(null);

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_missingIdentifier_fail() {
        // 准备参数
        Map<String, Object> properties = MapUtil.of("temperature", 25.5);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(IotSceneRuleConditionTypeEnum.DEVICE_PROPERTY.getType());
        condition.setIdentifier(null); // 缺少标识符
        condition.setOperator(IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator());
        condition.setParam("20");

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_missingOperator_fail() {
        // 准备参数
        Map<String, Object> properties = MapUtil.of("temperature", 25.5);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(IotSceneRuleConditionTypeEnum.DEVICE_PROPERTY.getType());
        condition.setIdentifier("temperature");
        condition.setOperator(null); // 缺少操作符
        condition.setParam("20");

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_missingParam_fail() {
        // 准备参数
        Map<String, Object> properties = MapUtil.of("temperature", 25.5);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(IotSceneRuleConditionTypeEnum.DEVICE_PROPERTY.getType());
        condition.setIdentifier("temperature");
        condition.setOperator(IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator());
        condition.setParam(null); // 缺少参数

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullMessage_fail() {
        // 准备参数
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                "temperature",
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                "20"
        );

        // 调用
        boolean result = matcher.matches(null, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullDeviceProperties_fail() {
        // 准备参数
        IotDeviceMessage message = new IotDeviceMessage();
        message.setParams(null);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                "temperature",
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                "20"
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_voltageGreaterThanOrEquals_success() {
        // 准备参数
        String propertyName = "voltage";
        Double propertyValue = 12.0;
        Map<String, Object> properties = MapUtil.of(propertyName, propertyValue);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                propertyName,
                IotSceneRuleConditionOperatorEnum.GREATER_THAN_OR_EQUALS.getOperator(),
                String.valueOf(propertyValue)
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_currentLessThanOrEquals_success() {
        // 准备参数
        String propertyName = "current";
        Double propertyValue = 2.5;
        Double compareValue = 3.0;
        Map<String, Object> properties = MapUtil.of(propertyName, propertyValue);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                propertyName,
                IotSceneRuleConditionOperatorEnum.LESS_THAN_OR_EQUALS.getOperator(),
                String.valueOf(compareValue)
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_stringProperty_success() {
        // 准备参数
        String propertyName = "mode";
        String propertyValue = "auto";
        Map<String, Object> properties = MapUtil.of(propertyName, propertyValue);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                propertyName,
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                propertyValue
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_booleanProperty_success() {
        // 准备参数
        String propertyName = "enabled";
        Boolean propertyValue = true;
        Map<String, Object> properties = MapUtil.of(propertyName, propertyValue);
        IotDeviceMessage message = createDeviceMessage(properties);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                propertyName,
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                String.valueOf(propertyValue)
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_multipleProperties_success() {
        // 准备参数
        Map<String, Object> properties = MapUtil.builder(new HashMap<String, Object>())
                .put("temperature", 25.5)
                .put("humidity", 60)
                .put("status", "active")
                .put("enabled", true)
                .build();
        IotDeviceMessage message = createDeviceMessage(properties);
        String targetProperty = "humidity";
        Integer targetValue = 60;
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                targetProperty,
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                String.valueOf(targetValue)
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result);
    }

    // ========== 辅助方法 ==========

    /**
     * 创建设备消息
     */
    private IotDeviceMessage createDeviceMessage(Map<String, Object> properties) {
        IotDeviceMessage message = new IotDeviceMessage();
        message.setDeviceId(randomLongId());
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
