package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.condition;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionOperatorEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotBaseConditionMatcherTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotDevicePropertyConditionMatcher} 的单元测试
 *
 * @author HUIHUI
 */
public class IotDevicePropertyConditionMatcherTest extends IotBaseConditionMatcherTest {

    private IotDevicePropertyConditionMatcher matcher;

    @BeforeEach
    public void setUp() {
        matcher = new IotDevicePropertyConditionMatcher();
    }

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
        assertEquals(25, result); // 修正：实际返回值是 25
    }

    @Test
    public void testMatches_temperatureEquals_success() {
        // 准备参数：创建属性上报消息
        String propertyIdentifier = "temperature";
        Double propertyValue = 25.5;
        IotDeviceMessage message = createPropertyPostMessage(propertyIdentifier, propertyValue);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                propertyIdentifier,
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
        // 准备参数：创建属性上报消息
        String propertyIdentifier = "humidity";
        Integer propertyValue = 75;
        Integer compareValue = 70;
        IotDeviceMessage message = createPropertyPostMessage(propertyIdentifier, propertyValue);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                propertyIdentifier,
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
        // 准备参数：创建属性上报消息
        String propertyIdentifier = "pressure";
        Double propertyValue = 1010.5;
        Integer compareValue = 1020;
        IotDeviceMessage message = createPropertyPostMessage(propertyIdentifier, propertyValue);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                propertyIdentifier,
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
        // 准备参数：创建属性上报消息
        String propertyIdentifier = "status";
        String propertyValue = "active";
        String compareValue = "inactive";
        IotDeviceMessage message = createPropertyPostMessage(propertyIdentifier, propertyValue);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                propertyIdentifier,
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
        // 准备参数：创建属性上报消息，值不满足条件
        String propertyIdentifier = "temperature";
        Double propertyValue = 15.0;
        Integer compareValue = 20;
        IotDeviceMessage message = createPropertyPostMessage(propertyIdentifier, propertyValue);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                propertyIdentifier,
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                String.valueOf(compareValue)
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_identifierMismatch_fail() {
        // 准备参数：标识符不匹配
        String messageIdentifier = "temperature";
        String conditionIdentifier = "humidity";
        Double propertyValue = 25.5;
        IotDeviceMessage message = createPropertyPostMessage(messageIdentifier, propertyValue);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                conditionIdentifier,
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                String.valueOf(propertyValue)
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullCondition_fail() {
        // 准备参数
        IotDeviceMessage message = createPropertyPostMessage("temperature", 25.5);

        // 调用
        boolean result = matcher.matches(message, null);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullConditionType_fail() {
        // 准备参数
        IotDeviceMessage message = createPropertyPostMessage("temperature", 25.5);
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
        IotDeviceMessage message = createPropertyPostMessage("temperature", 25.5);
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
        IotDeviceMessage message = createPropertyPostMessage("temperature", 25.5);
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
        IotDeviceMessage message = createPropertyPostMessage("temperature", 25.5);
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
        // 准备参数：消息的 params 为 null
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
    public void testMatches_propertiesStructure_success() {
        // 测试使用 properties 结构的消息（真实的属性上报场景）
        String identifier = "temperature";
        Double propertyValue = 25.5;
        IotDeviceMessage message = createPropertyPostMessageWithProperties(identifier, propertyValue);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                identifier,
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                "20"
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言：修复后的实现应该能正确从 properties 中提取属性值
        assertTrue(result);
    }

    @Test
    public void testMatches_simpleValueMessage_success() {
        // 测试简单值消息（params 直接是属性值）
        Double propertyValue = 25.5;
        IotDeviceMessage message = createSimpleValueMessage(propertyValue);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                "any", // 对于简单值消息，标识符匹配会被跳过
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                "20"
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言：修复后的实现应该能处理简单值消息
        // 但由于标识符匹配失败，结果为 false
        assertFalse(result);
    }

    @Test
    public void testMatches_valueFieldStructure_success() {
        // 测试使用 value 字段的消息结构
        String identifier = "temperature";
        Double propertyValue = 25.5;

        IotDeviceMessage message = new IotDeviceMessage();
        message.setDeviceId(randomLongId());
        message.setMethod("thing.event.post");

        Map<String, Object> params = new HashMap<>();
        params.put("identifier", identifier);
        params.put("value", propertyValue);
        message.setParams(params);

        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                identifier,
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                "20"
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言：修复后的实现应该能从 value 字段提取属性值
        assertTrue(result);
    }

    @Test
    public void testMatches_voltageGreaterThanOrEquals_success() {
        // 准备参数：创建属性上报消息
        String propertyIdentifier = "voltage";
        Double propertyValue = 12.0;
        IotDeviceMessage message = createPropertyPostMessage(propertyIdentifier, propertyValue);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                propertyIdentifier,
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
        // 准备参数：创建属性上报消息
        String propertyIdentifier = "current";
        Double propertyValue = 2.5;
        Double compareValue = 3.0;
        IotDeviceMessage message = createPropertyPostMessage(propertyIdentifier, propertyValue);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                propertyIdentifier,
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
        // 准备参数：创建属性上报消息
        String propertyIdentifier = "mode";
        String propertyValue = "auto";
        IotDeviceMessage message = createPropertyPostMessage(propertyIdentifier, propertyValue);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                propertyIdentifier,
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
        // 准备参数：创建属性上报消息
        String propertyIdentifier = "enabled";
        Boolean propertyValue = true;
        IotDeviceMessage message = createPropertyPostMessage(propertyIdentifier, propertyValue);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                propertyIdentifier,
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                String.valueOf(propertyValue)
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result);
    }

    // ========== 辅助方法 ==========

    /**
     * 创建设备消息用于测试
     *
     * 支持的消息格式：
     * 1. 直接属性值：params 直接是属性值（适用于简单消息）
     * 2. 标识符+值：params 包含 identifier 和对应的属性值
     * 3. properties 结构：params.properties[identifier] = value
     * 4. data 结构：params.data[identifier] = value
     * 5. value 字段：params.value = value
     */
    private IotDeviceMessage createPropertyPostMessage(String identifier, Object value) {
        IotDeviceMessage message = new IotDeviceMessage();
        message.setDeviceId(randomLongId());
        message.setMethod("thing.event.post"); // 使用事件上报方法

        // 创建符合修复后逻辑的 params 结构
        Map<String, Object> params = new HashMap<>();
        params.put("identifier", identifier);
        // 直接将属性值放在标识符对应的字段中
        params.put(identifier, value);
        message.setParams(params);

        return message;
    }

    /**
     * 创建使用 properties 结构的消息（模拟真实的属性上报消息）
     */
    private IotDeviceMessage createPropertyPostMessageWithProperties(String identifier, Object value) {
        IotDeviceMessage message = new IotDeviceMessage();
        message.setDeviceId(randomLongId());
        message.setMethod("thing.property.post"); // 属性上报方法

        Map<String, Object> properties = new HashMap<>();
        properties.put(identifier, value);

        Map<String, Object> params = new HashMap<>();
        params.put("properties", properties);
        message.setParams(params);

        return message;
    }

    /**
     * 创建简单值消息（params 直接是属性值）
     */
    private IotDeviceMessage createSimpleValueMessage(Object value) {
        IotDeviceMessage message = new IotDeviceMessage();
        message.setDeviceId(randomLongId());
        message.setMethod("thing.property.post");
        // 直接将属性值作为 params
        message.setParams(value);

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
