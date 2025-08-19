package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.trigger;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceMessageMethodEnum;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionOperatorEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link DeviceStateUpdateTriggerMatcher} 的单元测试类
 *
 * @author HUIHUI
 */
public class DeviceStateUpdateTriggerMatcherTest extends BaseMockitoUnitTest {

    private DeviceStateUpdateTriggerMatcher matcher;

    @BeforeEach
    public void setUp() {
        matcher = new DeviceStateUpdateTriggerMatcher();
    }

    @Test
    public void testGetSupportedTriggerType() {
        // when & then
        assertEquals(IotSceneRuleTriggerTypeEnum.DEVICE_STATE_UPDATE, matcher.getSupportedTriggerType());
    }

    @Test
    public void testGetPriority() {
        // when & then
        assertEquals(10, matcher.getPriority());
    }

    @Test
    public void testIsEnabled() {
        // when & then
        assertTrue(matcher.isEnabled());
    }

    @Test
    public void testIsMatched_Success_OnlineState() {
        // given
        IotDeviceMessage message = createStateUpdateMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                IotDeviceStateEnum.ONLINE.getState().toString()
        );

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_OfflineState() {
        // given
        IotDeviceMessage message = createStateUpdateMessage(IotDeviceStateEnum.OFFLINE.getState());
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                IotDeviceStateEnum.OFFLINE.getState().toString()
        );

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Failure_StateMismatch() {
        // given
        IotDeviceMessage message = createStateUpdateMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                IotDeviceStateEnum.OFFLINE.getState().toString()
        );

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_NullTrigger() {
        // given
        IotDeviceMessage message = createStateUpdateMessage(IotDeviceStateEnum.ONLINE.getState());

        // when
        boolean result = matcher.isMatched(message, null);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_NullTriggerType() {
        // given
        IotDeviceMessage message = createStateUpdateMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(null);

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_WrongMessageMethod() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        message.setMethod(IotDeviceMessageMethodEnum.PROPERTY_POST.getMethod());
        message.setParams(IotDeviceStateEnum.ONLINE.getState());

        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                IotDeviceStateEnum.ONLINE.getState().toString()
        );

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_MissingOperator() {
        // given
        IotDeviceMessage message = createStateUpdateMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.DEVICE_STATE_UPDATE.getType());
        trigger.setOperator(null);
        trigger.setValue(IotDeviceStateEnum.ONLINE.getState().toString());

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_MissingValue() {
        // given
        IotDeviceMessage message = createStateUpdateMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.DEVICE_STATE_UPDATE.getType());
        trigger.setOperator(IotSceneRuleConditionOperatorEnum.EQUALS.getOperator());
        trigger.setValue(null);

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_NullMessageParams() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        message.setMethod(IotDeviceMessageMethodEnum.STATE_UPDATE.getMethod());
        message.setParams(null);

        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                IotDeviceStateEnum.ONLINE.getState().toString()
        );

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Success_GreaterThanOperator() {
        // given
        IotDeviceMessage message = createStateUpdateMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                IotDeviceStateEnum.INACTIVE.getState().toString()
        );

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_NotEqualsOperator() {
        // given
        IotDeviceMessage message = createStateUpdateMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.Trigger trigger = createValidTrigger(
                IotSceneRuleConditionOperatorEnum.NOT_EQUALS.getOperator(),
                IotDeviceStateEnum.OFFLINE.getState().toString()
        );

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    // ========== 辅助方法 ==========

    /**
     * 创建设备状态更新消息
     */
    private IotDeviceMessage createStateUpdateMessage(Integer state) {
        IotDeviceMessage message = new IotDeviceMessage();
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
