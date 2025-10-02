package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.trigger;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionOperatorEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotBaseConditionMatcherTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotDeviceStateUpdateTriggerMatcher} 的单元测试
 *
 * @author HUIHUI
 */
public class IotDeviceStateUpdateTriggerMatcherTest extends IotBaseConditionMatcherTest {

    private IotDeviceStateUpdateTriggerMatcher matcher;

    @BeforeEach
    public void setUp() {
        matcher = new IotDeviceStateUpdateTriggerMatcher();
    }

    @Test
    public void testGetSupportedTriggerType_success() {
        // 准备参数
        // 无需准备参数

        // 调用
        IotSceneRuleTriggerTypeEnum result = matcher.getSupportedTriggerType();

        // 断言
        assertEquals(IotSceneRuleTriggerTypeEnum.DEVICE_STATE_UPDATE, result);
    }

    @Test
    public void testGetPriority_success() {
        // 准备参数
        // 无需准备参数

        // 调用
        int result = matcher.getPriority();

        // 断言
        assertEquals(10, result);
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
    public void testMatches_onlineStateSuccess() {
        // 准备参数
        IotDeviceMessage message = createStateUpdateMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                IotDeviceStateEnum.ONLINE.getState().toString()
        );

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_offlineStateSuccess() {
        // 准备参数
        IotDeviceMessage message = createStateUpdateMessage(IotDeviceStateEnum.OFFLINE.getState());
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                IotDeviceStateEnum.OFFLINE.getState().toString()
        );

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_stateMismatch() {
        // 准备参数
        IotDeviceMessage message = createStateUpdateMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                IotDeviceStateEnum.OFFLINE.getState().toString()
        );

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullTrigger() {
        // 准备参数
        IotDeviceMessage message = createStateUpdateMessage(IotDeviceStateEnum.ONLINE.getState());

        // 调用
        boolean result = matcher.matches(message, null);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullTriggerType() {
        // 准备参数
        IotDeviceMessage message = createStateUpdateMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(null);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_wrongMessageMethod() {
        // 准备参数
        IotDeviceMessage message = new IotDeviceMessage();
        message.setDeviceId(randomLongId());
        message.setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());
        message.setParams(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                IotDeviceStateEnum.ONLINE.getState().toString()
        );

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullTriggerOperator() {
        // 准备参数
        IotDeviceMessage message = createStateUpdateMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.DEVICE_STATE_UPDATE.getType());
        trigger.setOperator(null);
        trigger.setValue(IotDeviceStateEnum.ONLINE.getState().toString());

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullTriggerValue() {
        // 准备参数
        IotDeviceMessage message = createStateUpdateMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.DEVICE_STATE_UPDATE.getType());
        trigger.setOperator(IotSceneRuleConditionOperatorEnum.EQUALS.getOperator());
        trigger.setValue(null);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullMessageParams() {
        // 准备参数
        IotDeviceMessage message = new IotDeviceMessage();
        message.setDeviceId(randomLongId());
        message.setMethod(IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod());
        message.setParams(null);
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                IotDeviceStateEnum.ONLINE.getState().toString()
        );

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_greaterThanOperatorSuccess() {
        // 准备参数
        IotDeviceMessage message = createStateUpdateMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                IotDeviceStateEnum.INACTIVE.getState().toString()
        );

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_notEqualsOperatorSuccess() {
        // 准备参数
        IotDeviceMessage message = createStateUpdateMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                IotSceneRuleConditionOperatorEnum.NOT_EQUALS.getOperator(),
                IotDeviceStateEnum.OFFLINE.getState().toString()
        );

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
    }

    // ========== 辅助方法 ==========

    /**
     * 创建设备状态更新消息
     */
    private IotDeviceMessage createStateUpdateMessage(Integer state) {
        IotDeviceMessage message = new IotDeviceMessage();
        message.setDeviceId(randomLongId());
        message.setMethod(IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod());
        message.setParams(state);
        return message;
    }

    /**
     * 创建有效的触发器
     */
    private IotSceneRuleDO.Trigger createValidTrigger(String operator, String value) {
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.DEVICE_STATE_UPDATE.getType());
        trigger.setOperator(operator);
        trigger.setValue(value);
        return trigger;
    }

}
