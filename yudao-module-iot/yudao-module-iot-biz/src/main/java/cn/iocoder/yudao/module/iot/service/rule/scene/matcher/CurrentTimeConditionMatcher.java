package cn.iocoder.yudao.module.iot.service.rule.scene.matcher;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionLevelEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 当前时间条件匹配器
 * <p>
 * 处理时间相关的子条件匹配逻辑
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class CurrentTimeConditionMatcher extends AbstractIotSceneRuleMatcher {

    /**
     * 时间格式化器 - HH:mm:ss
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * 时间格式化器 - HH:mm
     */
    private static final DateTimeFormatter TIME_FORMATTER_SHORT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public MatcherType getMatcherType() {
        return MatcherType.CONDITION;
    }

    @Override
    public IotSceneRuleConditionTypeEnum getSupportedConditionType() {
        return IotSceneRuleConditionTypeEnum.CURRENT_TIME;
    }

    @Override
    public IotSceneRuleConditionLevelEnum getSupportedConditionLevel() {
        return IotSceneRuleConditionLevelEnum.SECONDARY;
    }

    @Override
    public boolean isMatched(IotDeviceMessage message, IotSceneRuleDO.TriggerCondition condition) {
        // 1. 基础参数校验
        if (!isBasicConditionValid(condition)) {
            logConditionMatchFailure(message, condition, "条件基础参数无效");
            return false;
        }

        // 2. 检查操作符和参数是否有效
        if (!isConditionOperatorAndParamValid(condition)) {
            logConditionMatchFailure(message, condition, "操作符或参数无效");
            return false;
        }

        // 3. 获取当前时间
        LocalDateTime now = LocalDateTime.now();

        // 4. 根据操作符类型进行不同的时间匹配
        String operator = condition.getOperator();
        String param = condition.getParam();

        boolean matched = false;

        try {
            if (operator.startsWith("date_time_")) {
                // 日期时间匹配（时间戳）
                matched = matchDateTime(now, operator, param);
            } else if (operator.startsWith("time_")) {
                // 当日时间匹配（HH:mm:ss）
                matched = matchTime(now.toLocalTime(), operator, param);
            } else {
                // 其他操作符，使用通用条件评估器
                matched = evaluateCondition(now.toEpochSecond(java.time.ZoneOffset.of("+8")), operator, param);
            }

            if (matched) {
                logConditionMatchSuccess(message, condition);
            } else {
                logConditionMatchFailure(message, condition, "时间条件不匹配");
            }

        } catch (Exception e) {
            log.error("[CurrentTimeConditionMatcher][时间条件匹配异常] operator: {}, param: {}", operator, param, e);
            logConditionMatchFailure(message, condition, "时间条件匹配异常: " + e.getMessage());
            matched = false;
        }

        return matched;
    }

    /**
     * 匹配日期时间（时间戳）
     */
    private boolean matchDateTime(LocalDateTime now, String operator, String param) {
        long currentTimestamp = now.toEpochSecond(java.time.ZoneOffset.of("+8"));
        return evaluateCondition(currentTimestamp, operator.substring("date_time_".length()), param);
    }

    /**
     * 匹配当日时间（HH:mm:ss）
     */
    private boolean matchTime(LocalTime currentTime, String operator, String param) {
        try {
            String actualOperator = operator.substring("time_".length());

            if ("between".equals(actualOperator)) {
                // 时间区间匹配
                String[] timeRange = param.split(",");
                if (timeRange.length != 2) {
                    return false;
                }

                LocalTime startTime = parseTime(timeRange[0].trim());
                LocalTime endTime = parseTime(timeRange[1].trim());

                return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
            } else {
                // 单个时间比较
                LocalTime targetTime = parseTime(param);

                switch (actualOperator) {
                    case ">":
                        return currentTime.isAfter(targetTime);
                    case "<":
                        return currentTime.isBefore(targetTime);
                    case ">=":
                        return !currentTime.isBefore(targetTime);
                    case "<=":
                        return !currentTime.isAfter(targetTime);
                    case "=":
                        return currentTime.equals(targetTime);
                    default:
                        return false;
                }
            }
        } catch (Exception e) {
            log.error("[CurrentTimeConditionMatcher][时间解析异常] param: {}", param, e);
            return false;
        }
    }

    /**
     * 解析时间字符串
     */
    private LocalTime parseTime(String timeStr) {
        if (StrUtil.isBlank(timeStr)) {
            throw new IllegalArgumentException("时间字符串不能为空");
        }

        // 尝试不同的时间格式
        try {
            if (timeStr.length() == 5) { // HH:mm
                return LocalTime.parse(timeStr, TIME_FORMATTER_SHORT);
            } else { // HH:mm:ss
                return LocalTime.parse(timeStr, TIME_FORMATTER);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("时间格式无效: " + timeStr, e);
        }
    }

    @Override
    public int getPriority() {
        return 40; // 较低优先级
    }

}
