package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.condition;

import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionOperatorEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotBaseConditionMatcherTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotDeviceStateConditionMatcher} 的单元测试
 *
 * @author HUIHUI
 */
public class IotDeviceStateConditionMatcherTest extends IotBaseConditionMatcherTest {

    private IotDeviceStateConditionMatcher matcher;

    @BeforeEach
    public void setUp() {
        matcher = new IotDeviceStateConditionMatcher();
    }

    @Test
    public void testGetSupportedConditionType() {
        // 调用
        IotSceneRuleConditionTypeEnum result = matcher.getSupportedConditionType();

        // 断言
        assertEquals(IotSceneRuleConditionTypeEnum.DEVICE_STATE, result);
    }

    @Test
    public void testGetPriority() {
        // 调用
        int result = matcher.getPriority();

        // 断言
        assertEquals(30, result);
    }

    @Test
    public void testIsEnabled() {
        // 调用
        boolean result = matcher.isEnabled();

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_onlineState_success() {
        // 准备参数
        IotDeviceStateEnum deviceState = IotDeviceStateEnum.ONLINE;
        IotDeviceMessage message = createDeviceMessage(deviceState.getState());
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                deviceState.getState().toString()
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_offlineState_success() {
        // 准备参数
        IotDeviceStateEnum deviceState = IotDeviceStateEnum.OFFLINE;
        IotDeviceMessage message = createDeviceMessage(deviceState.getState());
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                deviceState.getState().toString()
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_inactiveState_success() {
        // 准备参数
        IotDeviceStateEnum deviceState = IotDeviceStateEnum.INACTIVE;
        IotDeviceMessage message = createDeviceMessage(deviceState.getState());
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                deviceState.getState().toString()
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_stateMismatch_fail() {
        // 准备参数
        IotDeviceStateEnum actualState = IotDeviceStateEnum.ONLINE;
        IotDeviceStateEnum expectedState = IotDeviceStateEnum.OFFLINE;
        IotDeviceMessage message = createDeviceMessage(actualState.getState());
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                expectedState.getState().toString()
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_notEqualsOperator_success() {
        // 准备参数
        IotDeviceStateEnum actualState = IotDeviceStateEnum.ONLINE;
        IotDeviceStateEnum compareState = IotDeviceStateEnum.OFFLINE;
        IotDeviceMessage message = createDeviceMessage(actualState.getState());
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.NOT_EQUALS.getOperator(),
                compareState.getState().toString()
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_greaterThanOperator_success() {
        // 准备参数
        IotDeviceStateEnum actualState = IotDeviceStateEnum.OFFLINE; // 状态值为 2
        IotDeviceStateEnum compareState = IotDeviceStateEnum.ONLINE; // 状态值为 1
        IotDeviceMessage message = createDeviceMessage(actualState.getState());
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.GREATER_THAN.getOperator(),
                compareState.getState().toString()
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_lessThanOperator_success() {
        // 准备参数
        IotDeviceStateEnum actualState = IotDeviceStateEnum.INACTIVE; // 状态值为 0
        IotDeviceStateEnum compareState = IotDeviceStateEnum.ONLINE; // 状态值为 1
        IotDeviceMessage message = createDeviceMessage(actualState.getState());
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.LESS_THAN.getOperator(),
                compareState.getState().toString()
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_nullCondition_fail() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage(IotDeviceStateEnum.ONLINE.getState());

        // 调用
        boolean result = matcher.matches(message, null);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullConditionType_fail() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(null);

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_missingOperator_fail() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(IotSceneRuleConditionTypeEnum.DEVICE_STATE.getType());
        condition.setOperator(null);
        condition.setParam(IotDeviceStateEnum.ONLINE.getState().toString());

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_missingParam_fail() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(IotSceneRuleConditionTypeEnum.DEVICE_STATE.getType());
        condition.setOperator(IotSceneRuleConditionOperatorEnum.EQUALS.getOperator());
        condition.setParam(null);

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullMessage_fail() {
        // 准备参数
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                IotDeviceStateEnum.ONLINE.getState().toString()
        );

        // 调用
        boolean result = matcher.matches(null, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullDeviceState_fail() {
        // 准备参数
        IotDeviceMessage message = new IotDeviceMessage();
        message.setParams(null);
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                IotDeviceStateEnum.ONLINE.getState().toString()
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_greaterThanOrEqualsOperator_success() {
        // 准备参数
        IotDeviceStateEnum deviceState = IotDeviceStateEnum.ONLINE; // 状态值为 1
        IotDeviceMessage message = createDeviceMessage(deviceState.getState());
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.GREATER_THAN_OR_EQUALS.getOperator(),
                deviceState.getState().toString() // 比较值也为 1
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_lessThanOrEqualsOperator_success() {
        // 准备参数
        IotDeviceStateEnum actualState = IotDeviceStateEnum.ONLINE; // 状态值为 1
        IotDeviceStateEnum compareState = IotDeviceStateEnum.OFFLINE; // 状态值为 2
        IotDeviceMessage message = createDeviceMessage(actualState.getState());
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.LESS_THAN_OR_EQUALS.getOperator(),
                compareState.getState().toString()
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_invalidOperator_fail() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(IotSceneRuleConditionTypeEnum.DEVICE_STATE.getType());
        condition.setOperator(randomString()); // 随机无效操作符
        condition.setParam(IotDeviceStateEnum.ONLINE.getState().toString());

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_invalidParamFormat_fail() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage(IotDeviceStateEnum.ONLINE.getState());
        IotSceneRuleDO.TriggerCondition condition = createValidCondition(
                IotSceneRuleConditionOperatorEnum.EQUALS.getOperator(),
                randomString() // 随机无效状态值
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    // ========== 辅助方法 ==========

    /**
     * 创建设备消息
     */
    private IotDeviceMessage createDeviceMessage(Integer deviceState) {
        IotDeviceMessage message = new IotDeviceMessage();
        message.setDeviceId(randomLongId());
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
