package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.trigger;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionOperatorEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotBaseConditionMatcherTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static cn.hutool.core.util.RandomUtil.randomDouble;
import static cn.hutool.core.util.RandomUtil.randomInt;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotDevicePropertyPostTriggerMatcher} 的单元测试
 *
 * @author HUIHUI
 */
public class IotDevicePropertyPostTriggerMatcherTest extends IotBaseConditionMatcherTest {

    private IotDevicePropertyPostTriggerMatcher matcher;

    @BeforeEach
    public void setUp() {
        matcher = new IotDevicePropertyPostTriggerMatcher();
    }

    @Test
    public void testGetSupportedTriggerType_success() {
        // 准备参数
        // 无需准备参数

        // 调用
        IotSceneRuleTriggerTypeEnum result = matcher.getSupportedTriggerType();

        // 断言
        assertEquals(IotSceneRuleTriggerTypeEnum.DEVICE_PROPERTY_POST, result);
    }

    @Test
    public void testGetPriority_success() {
        // 准备参数
        // 无需准备参数

        // 调用
        int result = matcher.getPriority();

        // 断言
        assertEquals(20, result);
    }

    @Test
    public void testIsEnabled_success() {
        // 准备参数
        // 无需准备参数

        // 调用
        boolean result = matcher.isEnabled();

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_numericPropertyGreaterThanSuccess() {
        // 准备参数
        String propertyName = randomString();
        Double propertyValue = 25.5;
        Integer compareValue = 20;
        Map<String, Object> properties = MapUtil.builder(new HashMap<String, Object>())
                .put(propertyName, propertyValue)
                .build();
        IotDeviceMessage message = createPropertyPostMessage(properties);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                propertyName,
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                String.valueOf(compareValue)
        );

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_integerPropertyEqualsSuccess() {
        // 准备参数
        String propertyName = randomString();
        Integer propertyValue = randomInt();
        Map<String, Object> properties = MapUtil.builder(new HashMap<String, Object>())
                .put(propertyName, propertyValue)
                .build();
        IotDeviceMessage message = createPropertyPostMessage(properties);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                propertyName,
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                String.valueOf(propertyValue)
        );

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_propertyValueNotMeetCondition() {
        // 准备参数
        String propertyName = randomString();
        Double propertyValue = 15.0;
        Integer compareValue = 20;
        Map<String, Object> properties = MapUtil.builder(new HashMap<String, Object>())
                .put(propertyName, propertyValue)
                .build();
        IotDeviceMessage message = createPropertyPostMessage(properties);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                propertyName,
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                String.valueOf(compareValue)
        );

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_propertyNotFound() {
        // 准备参数
        String existingProperty = randomString();
        String missingProperty = randomString();
        Map<String, Object> properties = MapUtil.builder(new HashMap<String, Object>())
                .put(existingProperty, randomDouble())
                .build();
        IotDeviceMessage message = createPropertyPostMessage(properties);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                missingProperty, // 不存在的属性
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                String.valueOf(randomInt())
        );

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_wrongMessageMethod() {
        // 准备参数
        String propertyName = randomString();
        Map<String, Object> properties = MapUtil.builder(new HashMap<String, Object>())
                .put(propertyName, randomDouble())
                .build();
        IotDeviceMessage message = new IotDeviceMessage();
        message.setDeviceId(randomLongId());
        message.setMethod(IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod());
        message.setParams(properties);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                propertyName,
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                String.valueOf(randomInt())
        );

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullTriggerIdentifier() {
        // 准备参数
        String propertyName = randomString();
        Map<String, Object> properties = MapUtil.builder(new HashMap<String, Object>())
                .put(propertyName, randomDouble())
                .build();
        IotDeviceMessage message = createPropertyPostMessage(properties);
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.DEVICE_PROPERTY_POST.getType());
        trigger.setIdentifier(null); // 缺少标识符
        trigger.setOperator(IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator());
        trigger.setValue(String.valueOf(randomInt()));

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullMessageParams() {
        // 准备参数
        String propertyName = randomString();
        IotDeviceMessage message = new IotDeviceMessage();
        message.setDeviceId(randomLongId());
        message.setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());
        message.setParams(null);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                propertyName,
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                String.valueOf(randomInt())
        );

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_invalidMessageParams() {
        // 准备参数
        String propertyName = randomString();
        IotDeviceMessage message = new IotDeviceMessage();
        message.setDeviceId(randomLongId());
        message.setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());
        message.setParams(randomString()); // 不是 Map 类型
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                propertyName,
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                String.valueOf(randomInt())
        );

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_lessThanOperatorSuccess() {
        // 准备参数
        String propertyName = randomString();
        Double propertyValue = 15.0;
        Integer compareValue = 20;
        Map<String, Object> properties = MapUtil.builder(new HashMap<String, Object>())
                .put(propertyName, propertyValue)
                .build();
        IotDeviceMessage message = createPropertyPostMessage(properties);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                propertyName,
                IotSceneRuleConditionOperatorEnum.LESS_THAN.getOperator(),
                String.valueOf(compareValue)
        );

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_notEqualsOperatorSuccess() {
        // 准备参数
        String propertyName = randomString();
        String propertyValue = randomString();
        String compareValue = randomString();
        Map<String, Object> properties = MapUtil.builder(new HashMap<String, Object>())
                .put(propertyName, propertyValue)
                .build();
        IotDeviceMessage message = createPropertyPostMessage(properties);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                propertyName,
                IotSceneRuleConditionOperatorEnum.NOT_EQUALS.getOperator(),
                compareValue
        );

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_multiplePropertiesTargetPropertySuccess() {
        // 准备参数
        String targetProperty = randomString();
        Integer targetValue = randomInt();
        Map<String, Object> properties = MapUtil.builder(new HashMap<String, Object>())
                .put(randomString(), randomDouble())
                .put(targetProperty, targetValue)
                .put(randomString(), randomString())
                .build();
        IotDeviceMessage message = createPropertyPostMessage(properties);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                targetProperty,
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                String.valueOf(targetValue)
        );

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
    }

    // ========== 辅助方法 ==========

    /**
     * 创建属性上报消息
     */
    private IotDeviceMessage createPropertyPostMessage(Map<String, Object> properties) {
        IotDeviceMessage message = new IotDeviceMessage();
        message.setDeviceId(randomLongId());
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
