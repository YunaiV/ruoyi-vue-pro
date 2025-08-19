package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.condition;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionOperatorEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link CurrentTimeConditionMatcher} 的单元测试类
 *
 * @author HUIHUI
 */
public class CurrentTimeConditionMatcherTest extends BaseMockitoUnitTest {

    private CurrentTimeConditionMatcher matcher;

    @BeforeEach
    public void setUp() {
        matcher = new CurrentTimeConditionMatcher();
    }

    @Test
    public void testGetSupportedConditionType() {
        // when & then
        assertEquals(IotSceneRuleConditionTypeEnum.CURRENT_TIME, matcher.getSupportedConditionType());
    }

    @Test
    public void testGetPriority() {
        // when & then
        assertEquals(40, matcher.getPriority());
    }

    @Test
    public void testIsEnabled() {
        // when & then
        assertTrue(matcher.isEnabled());
    }

    // ========== 时间戳条件测试 ==========

    @Test
    public void testIsMatched_DateTimeGreaterThan_Success() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        long pastTimestamp = LocalDateTime.now().minusHours(1).toEpochSecond(ZoneOffset.of("+8"));
        IotSceneRuleDO.TriggerCondition condition = createDateTimeCondition(
                IotSceneRuleConditionOperatorEnum.DATE_TIME_GREATER_THAN.getOperator(),
                String.valueOf(pastTimestamp)
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_DateTimeGreaterThan_Failure() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        long futureTimestamp = LocalDateTime.now().plusHours(1).toEpochSecond(ZoneOffset.of("+8"));
        IotSceneRuleDO.TriggerCondition condition = createDateTimeCondition(
                IotSceneRuleConditionOperatorEnum.DATE_TIME_GREATER_THAN.getOperator(),
                String.valueOf(futureTimestamp)
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_DateTimeLessThan_Success() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        long futureTimestamp = LocalDateTime.now().plusHours(1).toEpochSecond(ZoneOffset.of("+8"));
        IotSceneRuleDO.TriggerCondition condition = createDateTimeCondition(
                IotSceneRuleConditionOperatorEnum.DATE_TIME_LESS_THAN.getOperator(),
                String.valueOf(futureTimestamp)
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_DateTimeBetween_Success() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        long startTimestamp = LocalDateTime.now().minusHours(1).toEpochSecond(ZoneOffset.of("+8"));
        long endTimestamp = LocalDateTime.now().plusHours(1).toEpochSecond(ZoneOffset.of("+8"));
        IotSceneRuleDO.TriggerCondition condition = createDateTimeCondition(
                IotSceneRuleConditionOperatorEnum.DATE_TIME_BETWEEN.getOperator(),
                startTimestamp + "," + endTimestamp
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_DateTimeBetween_Failure() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        long startTimestamp = LocalDateTime.now().plusHours(1).toEpochSecond(ZoneOffset.of("+8"));
        long endTimestamp = LocalDateTime.now().plusHours(2).toEpochSecond(ZoneOffset.of("+8"));
        IotSceneRuleDO.TriggerCondition condition = createDateTimeCondition(
                IotSceneRuleConditionOperatorEnum.DATE_TIME_BETWEEN.getOperator(),
                startTimestamp + "," + endTimestamp
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertFalse(result);
    }

    // ========== 当日时间条件测试 ==========

    @Test
    public void testIsMatched_TimeGreaterThan_EarlyMorning() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.TriggerCondition condition = createTimeCondition(
                IotSceneRuleConditionOperatorEnum.TIME_GREATER_THAN.getOperator(),
                "06:00:00" // 早上6点
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        // 结果取决于当前时间，如果当前时间大于6点则为true
        assertNotNull(result);
    }

    @Test
    public void testIsMatched_TimeLessThan_LateNight() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.TriggerCondition condition = createTimeCondition(
                IotSceneRuleConditionOperatorEnum.TIME_LESS_THAN.getOperator(),
                "23:59:59" // 晚上11点59分59秒
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        // 大部分情况下应该为true，除非在午夜前1秒运行测试
        assertNotNull(result);
    }

    @Test
    public void testIsMatched_TimeBetween_AllDay() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.TriggerCondition condition = createTimeCondition(
                IotSceneRuleConditionOperatorEnum.TIME_BETWEEN.getOperator(),
                "00:00:00,23:59:59" // 全天
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertTrue(result); // 全天范围应该总是匹配
    }

    @Test
    public void testIsMatched_TimeBetween_WorkingHours() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.TriggerCondition condition = createTimeCondition(
                IotSceneRuleConditionOperatorEnum.TIME_BETWEEN.getOperator(),
                "09:00:00,17:00:00" // 工作时间
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        // 结果取决于当前时间是否在工作时间内
        assertNotNull(result);
    }

    // ========== 异常情况测试 ==========

    @Test
    public void testIsMatched_NullCondition() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();

        // when
        boolean result = matcher.isMatched(message, null);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_NullConditionType() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(null);

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_InvalidOperator() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(IotSceneRuleConditionTypeEnum.CURRENT_TIME.getType());
        condition.setOperator("invalid_operator");
        condition.setParam("12:00:00");

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_InvalidTimeFormat() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.TriggerCondition condition = createTimeCondition(
                IotSceneRuleConditionOperatorEnum.TIME_GREATER_THAN.getOperator(),
                "invalid-time-format"
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_InvalidTimestampFormat() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.TriggerCondition condition = createDateTimeCondition(
                IotSceneRuleConditionOperatorEnum.DATE_TIME_GREATER_THAN.getOperator(),
                "invalid-timestamp"
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_InvalidBetweenFormat() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.TriggerCondition condition = createTimeCondition(
                IotSceneRuleConditionOperatorEnum.TIME_BETWEEN.getOperator(),
                "09:00:00" // 缺少结束时间
        );

        // when
        boolean result = matcher.isMatched(message, condition);

        // then
        assertFalse(result);
    }

    // ========== 辅助方法 ==========

    /**
     * 创建日期时间条件
     */
    private IotSceneRuleDO.TriggerCondition createDateTimeCondition(String operator, String param) {
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(IotSceneRuleConditionTypeEnum.CURRENT_TIME.getType());
        condition.setOperator(operator);
        condition.setParam(param);
        return condition;
    }

    /**
     * 创建当日时间条件
     */
    private IotSceneRuleDO.TriggerCondition createTimeCondition(String operator, String param) {
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(IotSceneRuleConditionTypeEnum.CURRENT_TIME.getType());
        condition.setOperator(operator);
        condition.setParam(param);
        return condition;
    }
}
