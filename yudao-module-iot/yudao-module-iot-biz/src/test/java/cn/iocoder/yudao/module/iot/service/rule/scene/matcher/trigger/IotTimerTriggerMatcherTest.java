package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.trigger;

import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleTriggerTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotBaseConditionMatcherTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IotTimerTriggerMatcher} 的单元测试
 *
 * @author HUIHUI
 */
public class IotTimerTriggerMatcherTest extends IotBaseConditionMatcherTest {

    private IotTimerTriggerMatcher matcher;

    @BeforeEach
    public void setUp() {
        matcher = new IotTimerTriggerMatcher();
    }

    @Test
    public void testGetSupportedTriggerType_success() {
        // 准备参数
        // 无需准备参数

        // 调用
        IotSceneRuleTriggerTypeEnum result = matcher.getSupportedTriggerType();

        // 断言
        assertEquals(IotSceneRuleTriggerTypeEnum.TIMER, result);
    }

    @Test
    public void testGetPriority_success() {
        // 准备参数
        // 无需准备参数

        // 调用
        int result = matcher.getPriority();

        // 断言
        assertEquals(50, result);
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
    public void testMatches_validCronExpressionSuccess() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        String cronExpression = "0 0 12 * * ?"; // 每天中午12点
        IotSceneRuleDO.Trigger trigger = createValidTrigger(cronExpression);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_everyMinuteCronSuccess() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        String cronExpression = "0 * * * * ?"; // 每分钟
        IotSceneRuleDO.Trigger trigger = createValidTrigger(cronExpression);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_weekdaysCronSuccess() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        String cronExpression = "0 0 9 ? * MON-FRI"; // 工作日上午9点
        IotSceneRuleDO.Trigger trigger = createValidTrigger(cronExpression);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_invalidCronExpression() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        String cronExpression = randomString(); // 随机无效的 cron 表达式
        IotSceneRuleDO.Trigger trigger = createValidTrigger(cronExpression);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_emptyCronExpression() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        String cronExpression = ""; // 空的 cron 表达式
        IotSceneRuleDO.Trigger trigger = createValidTrigger(cronExpression);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullCronExpression() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.TIMER.getType());
        trigger.setCronExpression(null);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullTrigger() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();

        // 调用
        boolean result = matcher.matches(message, null);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_nullTriggerType() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(null);
        trigger.setCronExpression("0 0 12 * * ?");

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_complexCronExpressionSuccess() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        String cronExpression = "0 15 10 ? * 6#3"; // 每月第三个星期五上午10:15
        IotSceneRuleDO.Trigger trigger = createValidTrigger(cronExpression);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_incorrectCronFormat() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        String cronExpression = "0 0 12 * *"; // 缺少字段的 cron 表达式
        IotSceneRuleDO.Trigger trigger = createValidTrigger(cronExpression);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_specificDateCronSuccess() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        String cronExpression = "0 0 0 1 1 ? 2025"; // 2025年1月1日午夜
        IotSceneRuleDO.Trigger trigger = createValidTrigger(cronExpression);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_everySecondCronSuccess() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        String cronExpression = "* * * * * ?"; // 每秒执行
        IotSceneRuleDO.Trigger trigger = createValidTrigger(cronExpression);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
    }

    @Test
    public void testMatches_invalidCharactersCron() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        String cronExpression = "0 0 12 * * @ #"; // 包含无效字符的 cron 表达式
        IotSceneRuleDO.Trigger trigger = createValidTrigger(cronExpression);

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertFalse(result);
    }

    @Test
    public void testMatches_rangeCronSuccess() {
        // 准备参数
        IotDeviceMessage message = createDeviceMessage();
        IotSceneRuleDO.Trigger trigger = createValidTrigger("0 0 9-17 * * MON-FRI"); // 工作日9-17点

        // 调用
        boolean result = matcher.matches(message, trigger);

        // 断言
        assertTrue(result);
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
     * 创建有效的定时触发器
     */
    private IotSceneRuleDO.Trigger createValidTrigger(String cronExpression) {
        IotSceneRuleDO.Trigger trigger = new IotSceneRuleDO.Trigger();
        trigger.setType(IotSceneRuleTriggerTypeEnum.TIMER.getType());
        trigger.setCronExpression(cronExpression);
        return trigger;
    }

}
