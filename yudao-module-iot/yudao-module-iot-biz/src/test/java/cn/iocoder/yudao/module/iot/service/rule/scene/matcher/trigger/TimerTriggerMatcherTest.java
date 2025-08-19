package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.trigger;

import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link TimerTriggerMatcher} 的单元测试类
 *
 * @author HUIHUI
 */
public class TimerTriggerMatcherTest extends BaseMockitoUnitTest {

    private TimerTriggerMatcher matcher;

    @BeforeEach
    public void setUp() {
        matcher = new TimerTriggerMatcher();
    }

    @Test
    public void testGetSupportedTriggerType() {
        // when & then
        assertEquals(IotSceneRuleTriggerTypeEnum.TIMER, matcher.getSupportedTriggerType());
    }

    @Test
    public void testGetPriority() {
        // when & then
        assertEquals(50, matcher.getPriority());
    }

    @Test
    public void testIsEnabled() {
        // when & then
        assertTrue(matcher.isEnabled());
    }

    @Test
    public void testIsMatched_Success_ValidCronExpression() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.Trigger trigger = createValidTrigger("0 0 12 * * ?"); // 每天中午12点

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_EveryMinuteCron() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.Trigger trigger = createValidTrigger("0 * * * * ?"); // 每分钟

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_WeekdaysCron() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.Trigger trigger = createValidTrigger("0 0 9 ? * MON-FRI"); // 工作日上午9点

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Failure_InvalidCronExpression() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.Trigger trigger = createValidTrigger("invalid-cron-expression");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_EmptyCronExpression() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.Trigger trigger = createValidTrigger("");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_NullCronExpression() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.TIMER.getType());
        trigger.setCronExpression(null);

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_NullTrigger() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();

        // when
        boolean result = matcher.isMatched(message, null);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Failure_NullTriggerType() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(null);
        trigger.setCronExpression("0 0 12 * * ?");

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Success_ComplexCronExpression() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.Trigger trigger = createValidTrigger("0 15 10 ? * 6#3"); // 每月第三个星期五上午10:15

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Failure_IncorrectCronFormat() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.Trigger trigger = createValidTrigger("0 0 12 * *"); // 缺少字段

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Success_SpecificDateCron() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.Trigger trigger = createValidTrigger("0 0 0 1 1 ? 2025"); // 2025年1月1日午夜

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Success_EverySecondCron() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.Trigger trigger = createValidTrigger("* * * * * ?"); // 每秒

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsMatched_Failure_InvalidCharactersCron() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.Trigger trigger = createValidTrigger("0 0 12 * * @ #"); // 包含无效字符

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertFalse(result);
    }

    @Test
    public void testIsMatched_Success_RangeCron() {
        // given
        IotDeviceMessage message = new IotDeviceMessage();
        IotSceneRuleDO.Trigger trigger = createValidTrigger("0 0 9-17 * * MON-FRI"); // 工作日9-17点

        // when
        boolean result = matcher.isMatched(message, trigger);

        // then
        assertTrue(result);
    }

    // ========== 辅助方法 ==========

    /**
     * 创建有效的定时触发器
     */
    private IotSceneRuleDO.Trigger createValidTrigger(String cronExpression) {
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.TIMER.getType());
        trigger.setCronExpression(cronExpression);
        return trigger;
    }
}
