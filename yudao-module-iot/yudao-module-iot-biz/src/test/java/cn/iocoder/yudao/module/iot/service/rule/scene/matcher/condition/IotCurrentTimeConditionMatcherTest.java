package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.condition;

import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionOperatorEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotBaseConditionMatcherTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

/**
 * {@link IotCurrentTimeConditionMatcher} 的单元测试
 *
 * @author HUIHUI
 */
public class IotCurrentTimeConditionMatcherTest extends IotBaseConditionMatcherTest {

    /**
     * 固定的当前时刻
     */
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 15, 12, 0, 0);
    /**
     * 固定当前时刻对应的秒级时间戳（与生产代码使用同一时区换算）
     */
    private static final long NOW_TIMESTAMP = LocalDateTimeUtils.toEpochSecond(NOW);

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
    public void testMatches_dateTimeGreaterThan_success() {
        assertTrue(matchesAt(NOW, IotSceneRuleConditionOperatorEnum.DATE_TIME_GREATER_THAN,
                String.valueOf(NOW_TIMESTAMP - 3600)));
    }

    @Test
    public void testMatches_dateTimeGreaterThan_fail() {
        assertFalse(matchesAt(NOW, IotSceneRuleConditionOperatorEnum.DATE_TIME_GREATER_THAN,
                String.valueOf(NOW_TIMESTAMP + 3600)));
    }

    @Test
    public void testMatches_dateTimeGreaterThan_boundary() {
        // 等于目标时间戳时，不满足"大于"
        assertFalse(matchesAt(NOW, IotSceneRuleConditionOperatorEnum.DATE_TIME_GREATER_THAN,
                String.valueOf(NOW_TIMESTAMP)));
        assertTrue(matchesAt(NOW, IotSceneRuleConditionOperatorEnum.DATE_TIME_GREATER_THAN,
                String.valueOf(NOW_TIMESTAMP - 1)));
    }

    @Test
    public void testMatches_dateTimeLessThan_success() {
        assertTrue(matchesAt(NOW, IotSceneRuleConditionOperatorEnum.DATE_TIME_LESS_THAN,
                String.valueOf(NOW_TIMESTAMP + 3600)));
    }

    @Test
    public void testMatches_dateTimeLessThan_fail() {
        assertFalse(matchesAt(NOW, IotSceneRuleConditionOperatorEnum.DATE_TIME_LESS_THAN,
                String.valueOf(NOW_TIMESTAMP - 3600)));
    }

    @Test
    public void testMatches_dateTimeLessThan_boundary() {
        // 等于目标时间戳时，不满足"小于"
        assertFalse(matchesAt(NOW, IotSceneRuleConditionOperatorEnum.DATE_TIME_LESS_THAN,
                String.valueOf(NOW_TIMESTAMP)));
        assertTrue(matchesAt(NOW, IotSceneRuleConditionOperatorEnum.DATE_TIME_LESS_THAN,
                String.valueOf(NOW_TIMESTAMP + 1)));
    }

    @Test
    public void testMatches_dateTimeBetween_success() {
        assertTrue(matchesAt(NOW, IotSceneRuleConditionOperatorEnum.DATE_TIME_BETWEEN,
                (NOW_TIMESTAMP - 3600) + "," + (NOW_TIMESTAMP + 3600)));
    }

    @Test
    public void testMatches_dateTimeBetween_fail() {
        assertFalse(matchesAt(NOW, IotSceneRuleConditionOperatorEnum.DATE_TIME_BETWEEN,
                (NOW_TIMESTAMP + 3600) + "," + (NOW_TIMESTAMP + 7200)));
        assertFalse(matchesAt(NOW, IotSceneRuleConditionOperatorEnum.DATE_TIME_BETWEEN,
                (NOW_TIMESTAMP - 7200) + "," + (NOW_TIMESTAMP - 1)));
    }

    @Test
    public void testMatches_dateTimeBetween_boundary() {
        // 区间两端均为闭区间
        assertTrue(matchesAt(NOW, IotSceneRuleConditionOperatorEnum.DATE_TIME_BETWEEN,
                NOW_TIMESTAMP + "," + (NOW_TIMESTAMP + 3600)));
        assertTrue(matchesAt(NOW, IotSceneRuleConditionOperatorEnum.DATE_TIME_BETWEEN,
                (NOW_TIMESTAMP - 3600) + "," + NOW_TIMESTAMP));
        assertFalse(matchesAt(NOW, IotSceneRuleConditionOperatorEnum.DATE_TIME_BETWEEN,
                (NOW_TIMESTAMP + 1) + "," + (NOW_TIMESTAMP + 3600)));
    }

    // ========== 当日时间条件测试 ==========

    @Test
    public void testMatches_timeGreaterThan_earlyMorning() {
        // 早上 6 点之后
        String param = "06:00:00";
        assertTrue(matchesAt(atTime(12, 0, 0), IotSceneRuleConditionOperatorEnum.TIME_GREATER_THAN, param));
        assertTrue(matchesAt(atTime(6, 0, 1), IotSceneRuleConditionOperatorEnum.TIME_GREATER_THAN, param));
        // 边界：等于 6 点不满足"大于"
        assertFalse(matchesAt(atTime(6, 0, 0), IotSceneRuleConditionOperatorEnum.TIME_GREATER_THAN, param));
        assertFalse(matchesAt(atTime(5, 59, 59), IotSceneRuleConditionOperatorEnum.TIME_GREATER_THAN, param));
    }

    @Test
    public void testMatches_timeLessThan_lateNight() {
        // 晚上 11 点 59 分 59 秒之前
        String param = "23:59:59";
        assertTrue(matchesAt(atTime(12, 0, 0), IotSceneRuleConditionOperatorEnum.TIME_LESS_THAN, param));
        assertTrue(matchesAt(atTime(23, 59, 58), IotSceneRuleConditionOperatorEnum.TIME_LESS_THAN, param));
        // 边界：等于目标时间不满足"小于"
        assertFalse(matchesAt(atTime(23, 59, 59), IotSceneRuleConditionOperatorEnum.TIME_LESS_THAN, param));
    }

    @Test
    public void testMatches_timeBetween_allDay() {
        // 全天：闭区间两端都匹配
        String param = "00:00:00,23:59:59";
        assertTrue(matchesAt(atTime(0, 0, 0), IotSceneRuleConditionOperatorEnum.TIME_BETWEEN, param));
        assertTrue(matchesAt(atTime(12, 0, 0), IotSceneRuleConditionOperatorEnum.TIME_BETWEEN, param));
        assertTrue(matchesAt(atTime(23, 59, 59), IotSceneRuleConditionOperatorEnum.TIME_BETWEEN, param));
    }

    @Test
    public void testMatches_timeBetween_workingHours() {
        // 工作时间
        String param = "09:00:00,17:00:00";
        assertTrue(matchesAt(atTime(12, 0, 0), IotSceneRuleConditionOperatorEnum.TIME_BETWEEN, param));
        // 边界：闭区间
        assertTrue(matchesAt(atTime(9, 0, 0), IotSceneRuleConditionOperatorEnum.TIME_BETWEEN, param));
        assertTrue(matchesAt(atTime(17, 0, 0), IotSceneRuleConditionOperatorEnum.TIME_BETWEEN, param));
        // 区间外
        assertFalse(matchesAt(atTime(8, 59, 59), IotSceneRuleConditionOperatorEnum.TIME_BETWEEN, param));
        assertFalse(matchesAt(atTime(17, 0, 1), IotSceneRuleConditionOperatorEnum.TIME_BETWEEN, param));
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
     * 在固定的当前时刻下执行匹配，避免依赖机器时间
     */
    private boolean matchesAt(LocalDateTime now, IotSceneRuleConditionOperatorEnum operator, String param) {
        IotDeviceMessage message = createDeviceMessage();
        IotSceneRuleDO.TriggerCondition condition = createTimeCondition(operator.getOperator(), param);
        try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class, CALLS_REAL_METHODS)) {
            mockedTime.when(LocalDateTime::now).thenReturn(now);
            return matcher.matches(message, condition);
        }
    }

    private static LocalDateTime atTime(int hour, int minute, int second) {
        return NOW.toLocalDate().atTime(hour, minute, second);
    }

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
