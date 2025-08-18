package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.condition;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionOperatorEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link DeviceStateConditionMatcher} 的单元测试类
 *
 * @author HUIHUI
 */
public class DeviceStateConditionMatcherTest extends BaseMockitoUnitTest {

    private DeviceStateConditionMatcher matcher;

    @BeforeEach
    public void setUp() {
        matcher = new DeviceStateConditionMatcher();
    }

    @Test
    public void testGetSupportedConditionType() {
        // when & then
        assertEquals(IotSceneRuleConditionTypeEnum.DEVICE_STATE, matcher.getSupportedConditionType());
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
    public void testIsMatched_Success_OnlineState() {
        // given
        IotDeviceMessage message = createDeviceMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                IotDeviceStateEnum.ONLINE.getState().toString()
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_OfflineState() {
        // given
        IotDeviceMessage message = createDeviceMessage(IotDeviceStateEnum.OFFLINE.getState());
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                IotDeviceStateEnum.OFFLINE.getState().toString()
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_InactiveState() {
        // given
        IotDeviceMessage message = createDeviceMessage(IotDeviceStateEnum.INACTIVE.getState());
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                IotDeviceStateEnum.INACTIVE.getState().toString()
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Failure_StateMismatch() {
        // given
        IotDeviceMessage message = createDeviceMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                IotDeviceStateEnum.OFFLINE.getState().toString()
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Success_NotEqualsOperator() {
        // given
        IotDeviceMessage message = createDeviceMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.NOT_EQUALS.getOperator(),
                IotDeviceStateEnum.OFFLINE.getState().toString()
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_GreaterThanOperator() {
        // given
        IotDeviceMessage message = createDeviceMessage(IotDeviceStateEnum.OFFLINE.getState()); // 2
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                IotDeviceStateEnum.ONLINE.getState().toString() // 1
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_LessThanOperator() {
        // given
        IotDeviceMessage message = createDeviceMessage(IotDeviceStateEnum.INACTIVE.getState()); // 0
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.LESS_THAN.getOperator(),
                IotDeviceStateEnum.ONLINE.getState().toString() // 1
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Failure_NullCondition() {
        // given
        IotDeviceMessage message = createDeviceMessage(IotDeviceStateEnum.ONLINE.getState());

        // when
        boolean result = matcher.isMatched(message, null);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_NullConditionType() {
        // given
        IotDeviceMessage message = createDeviceMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(null);

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_MissingOperator() {
        // given
        IotDeviceMessage message = createDeviceMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(IotSceneRuleConditionTypeEnum.DEVICE_STATE.getType());
        condition.setOperator(null);
        condition.setParam(IotDeviceStateEnum.ONLINE.getState().toString());

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_MissingParam() {
        // given
        IotDeviceMessage message = createDeviceMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(IotSceneRuleConditionTypeEnum.DEVICE_STATE.getType());
        condition.setOperator(IotSceneRuleConditionOperatorEnum.EQUALS.getOperator());
        condition.setParam(null);

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_NullMessage() {
        // given
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                IotDeviceStateEnum.ONLINE.getState().toString()
        );

        // when
        boolean result = matcher.isMatched(null, condition);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_NullDeviceState() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        message.setParams(null);

        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                IotDeviceStateEnum.ONLINE.getState().toString()
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Success_GreaterThanOrEqualsOperator() {
        // given
        IotDeviceMessage message = createDeviceMessage(IotDeviceStateEnum.ONLINE.getState()); // 1
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.GREATER_THAN_OR_EQUALS.getOperator(),
                IotDeviceStateEnum.ONLINE.getState().toString() // 1
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_LessThanOrEqualsOperator() {
        // given
        IotDeviceMessage message = createDeviceMessage(IotDeviceStateEnum.ONLINE.getState()); // 1
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.LESS_THAN_OR_EQUALS.getOperator(),
                IotDeviceStateEnum.OFFLINE.getState().toString() // 2
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Failure_InvalidOperator() {
        // given
        IotDeviceMessage message = createDeviceMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(IotSceneRuleConditionTypeEnum.DEVICE_STATE.getType());
        condition.setOperator("invalid_operator");
        condition.setParam(IotDeviceStateEnum.ONLINE.getState().toString());

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_InvalidParamFormat() {
        // given
        IotDeviceMessage message = createDeviceMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                "invalid_state_value"
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertFalse(result);
    }

    // ========== 辅助方法 ==========

    /**
     * 创建设备消息
     */
    private IotDeviceMessage createDeviceMessage(Integer deviceState) {
        IotDeviceMessage message = new IotDeviceMessage();
        message.setParams(deviceState);
        return message;
    }

    /**
     * 创建有效的条件
     */
    private IotSceneRuleDO.TriggerCondition createValidCondition(String operator, String param) {
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(IotSceneRuleConditionTypeEnum.DEVICE_STATE.getType());
        condition.setOperator(operator);
        condition.setParam(param);
        return condition;
    }
}
