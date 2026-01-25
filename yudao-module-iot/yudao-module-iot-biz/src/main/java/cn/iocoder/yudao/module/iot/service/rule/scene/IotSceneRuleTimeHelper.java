package cn.iocoder.yudao.module.iot.service.rule.scene;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharPool;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.module.iot.enums.rule.IotSceneRuleConditionOperatorEnum;
import cn.iocoder.yudao.module.iot.service.rule.scene.matcher.condition.IotCurrentTimeConditionMatcher;
import cn.iocoder.yudao.module.iot.service.rule.scene.timer.IotTimerConditionEvaluator;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * IoT 场景规则时间匹配工具类
 * <p>
 * 提供时间条件匹配的通用方法，供 {@link IotCurrentTimeConditionMatcher} 和 {@link IotTimerConditionEvaluator} 共同使用。
 *
 * @author HUIHUI
 */
@Slf4j
public class IotSceneRuleTimeHelper {

    /**
     * 时间格式化器 - HH:mm:ss
     */
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * 时间格式化器 - HH:mm
     */
    private static final DateTimeFormatter TIME_FORMATTER_SHORT = DateTimeFormatter.ofPattern("HH:mm");

    // TODO @puhui999：可以使用 lombok 简化
    private IotSceneRuleTimeHelper() {
        // 工具类，禁止实例化
    }

    /**
     * 判断是否为日期时间操作符
     *
     * @param operatorEnum 操作符枚举
     * @return 是否为日期时间操作符
     */
    public static boolean isDateTimeOperator(IotSceneRuleConditionOperatorEnum operatorEnum) {
        return operatorEnum == IotSceneRuleConditionOperatorEnum.DATE_TIME_GREATER_THAN
                || operatorEnum == IotSceneRuleConditionOperatorEnum.DATE_TIME_LESS_THAN
                || operatorEnum == IotSceneRuleConditionOperatorEnum.DATE_TIME_BETWEEN;
    }

    /**
     * 判断是否为时间操作符（包括日期时间操作符和当日时间操作符）
     *
     * @param operatorEnum 操作符枚举
     * @return 是否为时间操作符
     */
    public static boolean isTimeOperator(IotSceneRuleConditionOperatorEnum operatorEnum) {
        return operatorEnum != IotSceneRuleConditionOperatorEnum.TIME_GREATER_THAN
                && operatorEnum != IotSceneRuleConditionOperatorEnum.TIME_LESS_THAN
                && operatorEnum != IotSceneRuleConditionOperatorEnum.TIME_BETWEEN
                && !isDateTimeOperator(operatorEnum);
    }

    /**
     * 执行时间匹配逻辑
     *
     * @param operatorEnum 操作符枚举
     * @param param        参数值
     * @return 是否匹配
     */
    public static boolean executeTimeMatching(IotSceneRuleConditionOperatorEnum operatorEnum, String param) {
        try {
            LocalDateTime now = LocalDateTime.now();
            if (isDateTimeOperator(operatorEnum)) {
                // 日期时间匹配（时间戳，秒级）
                long currentTimestamp = now.atZone(ZoneId.systemDefault()).toEpochSecond();
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
     * 匹配日期时间（时间戳，秒级）
     *
     * @param currentTimestamp 当前时间戳
     * @param operatorEnum     操作符枚举
     * @param param            参数值
     * @return 是否匹配
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    public static boolean matchDateTime(long currentTimestamp, IotSceneRuleConditionOperatorEnum operatorEnum,
                                        String param) {
        try {
            // DATE_TIME_BETWEEN 需要解析两个时间戳，单独处理
            if (operatorEnum == IotSceneRuleConditionOperatorEnum.DATE_TIME_BETWEEN) {
                return matchDateTimeBetween(currentTimestamp, param);
            }
            // 其他操作符只需要解析一个时间戳
            long targetTimestamp = Long.parseLong(param);
            switch (operatorEnum) {
                case DATE_TIME_GREATER_THAN:
                    return currentTimestamp > targetTimestamp;
                case DATE_TIME_LESS_THAN:
                    return currentTimestamp < targetTimestamp;
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
     *
     * @param currentTimestamp 当前时间戳
     * @param param            参数值（格式：startTimestamp,endTimestamp）
     * @return 是否匹配
     */
    public static boolean matchDateTimeBetween(long currentTimestamp, String param) {
        List<String> timestampRange = StrUtil.splitTrim(param, CharPool.COMMA);
        if (timestampRange.size() != 2) {
            log.warn("[matchDateTimeBetween][param({}) 时间戳区间参数格式错误]", param);
            return false;
        }
        long startTimestamp = Long.parseLong(timestampRange.get(0).trim());
        long endTimestamp = Long.parseLong(timestampRange.get(1).trim());
        // TODO @puhui999：hutool 里，看看有没 between 方法
        return currentTimestamp >= startTimestamp && currentTimestamp <= endTimestamp;
    }

    /**
     * 匹配当日时间（HH:mm:ss 或 HH:mm）
     *
     * @param currentTime  当前时间
     * @param operatorEnum 操作符枚举
     * @param param        参数值
     * @return 是否匹配
     */
    @SuppressWarnings("EnhancedSwitchMigration")
    public static boolean matchTime(LocalTime currentTime, IotSceneRuleConditionOperatorEnum operatorEnum,
                                    String param) {
        try {
            // TIME_BETWEEN 需要解析两个时间，单独处理
            if (operatorEnum == IotSceneRuleConditionOperatorEnum.TIME_BETWEEN) {
                return matchTimeBetween(currentTime, param);
            }
            // 其他操作符只需要解析一个时间
            LocalTime targetTime = parseTime(param);
            switch (operatorEnum) {
                case TIME_GREATER_THAN:
                    return currentTime.isAfter(targetTime);
                case TIME_LESS_THAN:
                    return currentTime.isBefore(targetTime);
                default:
                    log.warn("[matchTime][operatorEnum({}) 不支持的时间操作符]", operatorEnum);
                    return false;
            }
        } catch (Exception e) {
            log.error("[matchTime][operatorEnum({}) param({}) 时间解析异常]", operatorEnum, param, e);
            return false;
        }
    }

    /**
     * 匹配时间区间
     *
     * @param currentTime 当前时间
     * @param param       参数值（格式：startTime,endTime）
     * @return 是否匹配
     */
    public static boolean matchTimeBetween(LocalTime currentTime, String param) {
        List<String> timeRange = StrUtil.splitTrim(param, CharPool.COMMA);
        if (timeRange.size() != 2) {
            log.warn("[matchTimeBetween][param({}) 时间区间参数格式错误]", param);
            return false;
        }
        LocalTime startTime = parseTime(timeRange.get(0).trim());
        LocalTime endTime = parseTime(timeRange.get(1).trim());
        // TODO @puhui999：hutool 里，看看有没 between 方法
        return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
    }

    /**
     * 解析时间字符串
     * 支持 HH:mm 和 HH:mm:ss 两种格式
     *
     * @param timeStr 时间字符串
     * @return 解析后的 LocalTime
     */
    public static LocalTime parseTime(String timeStr) {
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

}
