package cn.iocoder.yudao.module.iot.service.rule.scene.matcher.condition;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharPool;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.core.mq.message.IotDeviceMessage;
import cn.iocoder.yudao.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionOperatorEnum;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionTypeEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.IotSceneRuleMatcherHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 当前时间条件匹配器：处理时间相关的子条件匹配逻辑
 *
 * @author HUIHUI
 */
@Component
@Slf4j
public class IotCurrentTimeConditionMatcher implements IotSceneRuleConditionMatcher {

    /**
     * 时间格式化器 - HH:mm:ss
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * 时间格式化器 - HH:mm
     */
    private static final DateTimeFormatter TIME_FORMATTER_SHORT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public IotSceneRuleConditionTypeEnum getSupportedConditionType() {
        return IotSceneRuleConditionTypeEnum.CURRENT_TIME;
    }

    @Override
    public boolean matches(IotDeviceMessage message, IotSceneRuleDO.TriggerCondition condition) {
        // 1.1 基础参数校验
        if (!IotSceneRuleMatcherHelper.isBasicConditionValid(condition)) {
            IotSceneRuleMatcherHelper.logConditionMatchFailure(message, condition, "条件基础参数无效");
            return false;
        }

        // 1.2 检查操作符和参数是否有效
        if (!IotSceneRuleMatcherHelper.isConditionOperatorAndParamValid(condition)) {
            IotSceneRuleMatcherHelper.logConditionMatchFailure(message, condition, "操作符或参数无效");
            return false;
        }

        // 1.3 验证操作符是否为支持的时间操作符
        String operator = condition.getOperator();
        IotSceneRuleConditionOperatorEnum operatorEnum = IotSceneRuleConditionOperatorEnum.operatorOf(operator);
        if (operatorEnum == null) {
            IotSceneRuleMatcherHelper.logConditionMatchFailure(message, condition, "无效的操作符: " + operator);
            return false;
        }

        if (!isTimeOperator(operatorEnum)) {
            IotSceneRuleMatcherHelper.logConditionMatchFailure(message, condition, "不支持的时间操作符: " + operator);
            return false;
        }

        // 2.1 执行时间匹配
        boolean matched = executeTimeMatching(operatorEnum, condition.getParam());

        // 2.2 记录匹配结果
        if (matched) {
            IotSceneRuleMatcherHelper.logConditionMatchSuccess(message, condition);
        } else {
            IotSceneRuleMatcherHelper.logConditionMatchFailure(message, condition, "时间条件不匹配");
        }

        return matched;
    }

    /**
     * 执行时间匹配逻辑
     * 直接实现时间条件匹配，不使用 Spring EL 表达式
     */
    private boolean executeTimeMatching(IotSceneRuleConditionOperatorEnum operatorEnum, String param) {
        try {
            LocalDateTime now = LocalDateTime.now();

            if (isDateTimeOperator(operatorEnum)) {
                // 日期时间匹配（时间戳）
                long currentTimestamp = now.toEpochSecond(java.time.ZoneOffset.of("+8"));
                return matchDateTime(currentTimestamp, operatorEnum, param);
            } else {
                // 当日时间匹配（HH:mm:ss）
                return matchTime(now.toLocalTime(), operatorEnum, param);
            }
        } catch (Exception e) {
            log.error("[executeTimeMatching][operatorEnum({}) param({}) 时间匹配异常]", operatorEnum, param, e);
            return false;
        }
    }

    /**
     * 判断是否为日期时间操作符
     */
    private boolean isDateTimeOperator(IotSceneRuleConditionOperatorEnum operatorEnum) {
        return operatorEnum == IotSceneRuleConditionOperatorEnum.DATE_TIME_GREATER_THAN ||
                operatorEnum == IotSceneRuleConditionOperatorEnum.DATE_TIME_LESS_THAN ||
                operatorEnum == IotSceneRuleConditionOperatorEnum.DATE_TIME_BETWEEN;
    }

    /**
     * 判断是否为时间操作符
     */
    private boolean isTimeOperator(IotSceneRuleConditionOperatorEnum operatorEnum) {
        return operatorEnum == IotSceneRuleConditionOperatorEnum.TIME_GREATER_THAN ||
                operatorEnum == IotSceneRuleConditionOperatorEnum.TIME_LESS_THAN ||
                operatorEnum == IotSceneRuleConditionOperatorEnum.TIME_BETWEEN ||
                isDateTimeOperator(operatorEnum);
    }

    /**
     * 匹配日期时间（时间戳）
     * 直接实现时间戳比较逻辑
     */
    private boolean matchDateTime(long currentTimestamp, IotSceneRuleConditionOperatorEnum operatorEnum, String param) {
        try {
            long targetTimestamp = Long.parseLong(param);
            switch (operatorEnum) {
                case DATE_TIME_GREATER_THAN:
                    return currentTimestamp > targetTimestamp;
                case DATE_TIME_LESS_THAN:
                    return currentTimestamp < targetTimestamp;
                case DATE_TIME_BETWEEN:
                    return matchDateTimeBetween(currentTimestamp, param);
                default:
                    log.warn("[matchDateTime][operatorEnum({}) 不支持的日期时间操作符]", operatorEnum);
                    return false;
            }
        } catch (Exception e) {
            log.error("[matchDateTime][operatorEnum({}) param({}) 日期时间匹配异常]", operatorEnum, param, e);
            return false;
        }
    }

    /**
     * 匹配日期时间区间
     */
    private boolean matchDateTimeBetween(long currentTimestamp, String param) {
        List<String> timestampRange = StrUtil.splitTrim(param, CharPool.COMMA);
        if (timestampRange.size() != 2) {
            log.warn("[matchDateTimeBetween][param({}) 时间戳区间参数格式错误]", param);
            return false;
        }
        long startTimestamp = Long.parseLong(timestampRange.get(0).trim());
        long endTimestamp = Long.parseLong(timestampRange.get(1).trim());
        return currentTimestamp >= startTimestamp && currentTimestamp <= endTimestamp;
    }

    /**
     * 匹配当日时间（HH:mm:ss）
     * 直接实现时间比较逻辑
     */
    private boolean matchTime(LocalTime currentTime, IotSceneRuleConditionOperatorEnum operatorEnum, String param) {
        try {
            LocalTime targetTime = parseTime(param);
            switch (operatorEnum) {
                case TIME_GREATER_THAN:
                    return currentTime.isAfter(targetTime);
                case TIME_LESS_THAN:
                    return currentTime.isBefore(targetTime);
                case TIME_BETWEEN:
                    return matchTimeBetween(currentTime, param);
                default:
                    log.warn("[matchTime][operatorEnum({}) 不支持的时间操作符]", operatorEnum);
                    return false;
            }
        } catch (Exception e) {
            log.error("[matchTime][][operatorEnum({}) param({}) 时间解析异常]", operatorEnum, param, e);
            return false;
        }
    }

    /**
     * 匹配时间区间
     */
    private boolean matchTimeBetween(LocalTime currentTime, String param) {
        List<String> timeRange = StrUtil.splitTrim(param, CharPool.COMMA);
        if (timeRange.size() != 2) {
            log.warn("[matchTimeBetween][param({}) 时间区间参数格式错误]", param);
            return false;
        }
        LocalTime startTime = parseTime(timeRange.get(0).trim());
        LocalTime endTime = parseTime(timeRange.get(1).trim());
        return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
    }

    /**
     * 解析时间字符串
     * 支持 HH:mm 和 HH:mm:ss 两种格式
     */
    private LocalTime parseTime(String timeStr) {
        Assert.isFalse(StrUtil.isBlank(timeStr), "时间字符串不能为空");

        try {
            // 尝试不同的时间格式
            if (timeStr.length() == 5) { // HH:mm
                return LocalTime.parse(timeStr, TIME_FORMATTER_SHORT);
            } else if (timeStr.length() == 8) { // HH:mm:ss
                return LocalTime.parse(timeStr, TIME_FORMATTER);
            } else {
                throw new IllegalArgumentException("时间格式长度不正确，期望 HH:mm 或 HH:mm:ss 格式");
            }
        } catch (Exception e) {
            log.error("[parseTime][timeStr({}) 时间格式解析失败]", timeStr, e);
            throw new IllegalArgumentException("时间格式无效: " + timeStr, e);
        }
    }

    @Override
    public int getPriority() {
        return 40; // 较低优先级
    }

}
