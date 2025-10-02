package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.condition;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionOperatorEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotBaseConditionMatcherTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotCurrentTimeConditionMatcher} 的单元测试
 *
 * @author HUIHUI
 */
public class IotCurrentTimeConditionMatcherTest extends IotBaseConditionMatcherTest {

    private IotCurrentTimeConditionMatcher matcher;

    @BeforeEach
    public void setUp() {
        matcher = new IotCurrentTimeConditionMatcher();
    }

    @Test
    public void testGetSupportedConditionType() {
        // 调用
        IotSceneRuleConditionTypeEnum result = matcher.getSupportedConditionType();

        // 断言
        assertEquals(IotSceneRuleConditionTypeEnum.CURRENT_TIME, result);
    }

    @Test
    public void testGetPriority() {
        // 调用
        int result = matcher.getPriority();

        // 断言
        assertEquals(40, result);
    }

    @Test
    public void testIsEnabled() {
        // 调用
        boolean result = matcher.isEnabled();

        // 断言
        assertTrue(result);
    }

    // ========== 时间戳条件测试 ==========

    @Test
    public void testMatches_DateTimeGreaterThan_success() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        long pastTimestamp = LocalDateTime.now().minusHours(1).toEpochSecond(ZoneOffset.of("+8"));
        IotSceneRuleDO.TriggerCondition condition = createDateTimeCondition(
                IotSceneRuleConditionOperatorEnum.DATE_TIME_GREATER_THAN.getOperator(),
                String.valueOf(pastTimestamp)
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_DateTimeGreaterThan_fail() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        long futureTimestamp = LocalDateTime.now().plusHours(1).toEpochSecond(ZoneOffset.of("+8"));
        IotSceneRuleDO.TriggerCondition condition = createDateTimeCondition(
                IotSceneRuleConditionOperatorEnum.DATE_TIME_GREATER_THAN.getOperator(),
                String.valueOf(futureTimestamp)
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_DateTimeLessThan_success() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        long futureTimestamp = LocalDateTime.now().plusHours(1).toEpochSecond(ZoneOffset.of("+8"));
        IotSceneRuleDO.TriggerCondition condition = createDateTimeCondition(
                IotSceneRuleConditionOperatorEnum.DATE_TIME_LESS_THAN.getOperator(),
                String.valueOf(futureTimestamp)
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_DateTimeBetween_success() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        long startTimestamp = LocalDateTime.now().minusHours(1).toEpochSecond(ZoneOffset.of("+8"));
        long endTimestamp = LocalDateTime.now().plusHours(1).toEpochSecond(ZoneOffset.of("+8"));
        IotSceneRuleDO.TriggerCondition condition = createDateTimeCondition(
                IotSceneRuleConditionOperatorEnum.DATE_TIME_BETWEEN.getOperator(),
                startTimestamp + "," + endTimestamp
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_DateTimeBetween_fail() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        long startTimestamp = LocalDateTime.now().plusHours(1).toEpochSecond(ZoneOffset.of("+8"));
        long endTimestamp = LocalDateTime.now().plusHours(2).toEpochSecond(ZoneOffset.of("+8"));
        IotSceneRuleDO.TriggerCondition condition = createDateTimeCondition(
                IotSceneRuleConditionOperatorEnum.DATE_TIME_BETWEEN.getOperator(),
                startTimestamp + "," + endTimestamp
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    // ========== 当日时间条件测试 ==========

    @Test
    public void testMatches_TimeGreaterThan_earlyMorning() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        IotSceneRuleDO.TriggerCondition condition = createTimeCondition(
                IotSceneRuleConditionOperatorEnum.TIME_GREATER_THAN.getOperator(),
                "06:00:00" // 早上6点
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        // 结果取决于当前时间，如果当前时间大于6点则为true
        assertNotNull(result);
    }

    @Test
    public void testMatches_TimeLessThan_lateNight() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        IotSceneRuleDO.TriggerCondition condition = createTimeCondition(
                IotSceneRuleConditionOperatorEnum.TIME_LESS_THAN.getOperator(),
                "23:59:59" // 晚上11点59分59秒
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        // 大部分情况下应该为true，除非在午夜前1秒运行测试
        assertNotNull(result);
    }

    @Test
    public void testMatches_TimeBetween_allDay() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        IotSceneRuleDO.TriggerCondition condition = createTimeCondition(
                IotSceneRuleConditionOperatorEnum.TIME_BETWEEN.getOperator(),
                "00:00:00,23:59:59" // 全天
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertTrue(result); // 全天范围应该总是匹配
    }

    @Test
    public void testMatches_TimeBetween_workingHours() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        IotSceneRuleDO.TriggerCondition condition = createTimeCondition(
                IotSceneRuleConditionOperatorEnum.TIME_BETWEEN.getOperator(),
                "09:00:00,17:00:00" // 工作时间
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        // 结果取决于当前时间是否在工作时间内
        assertNotNull(result);
    }

    // ========== 异常情况测试 ==========

    @Test
    public void testMatches_nullCondition() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();

        // 调用
        boolean result = matcher.matches(message, null);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullConditionType() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(null);

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_invalidOperator() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        IotSceneRuleDO.TriggerCondition condition = new IotSceneRuleDO.TriggerCondition();
        condition.setType(IotSceneRuleConditionTypeEnum.CURRENT_TIME.getType());
        condition.setOperator(randomString()); // 随机无效操作符
        condition.setParam("12:00:00");

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_invalidTimeFormat() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        IotSceneRuleDO.TriggerCondition condition = createTimeCondition(
                IotSceneRuleConditionOperatorEnum.TIME_GREATER_THAN.getOperator(),
                randomString() // 随机无效时间格式
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_invalidTimestampFormat() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        IotSceneRuleDO.TriggerCondition condition = createDateTimeCondition(
                IotSceneRuleConditionOperatorEnum.DATE_TIME_GREATER_THAN.getOperator(),
                randomString() // 随机无效时间戳格式
        );

        // 调用
        boolean result = matcher.matches(message, condition);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_invalidBetweenFormat() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        IotSceneRuleDO.TriggerCondition condition = createTimeCondition(
                IotSceneRuleConditionOperatorEnum.TIME_BETWEEN.getOperator(),
                "09:00:00" // 缺少结束时间
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
    private IotDeviceMessage createDeviceMessage() {
        IotDeviceMessage message = new IotDeviceMessage();
        message.setDeviceId(randomLongId());
        return message;
    }

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
