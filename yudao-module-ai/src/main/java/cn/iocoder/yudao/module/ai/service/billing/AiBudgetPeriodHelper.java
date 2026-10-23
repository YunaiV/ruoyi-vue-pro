package cn.iocoder.yudao.module.ai.service.billing;

import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiBudgetConfigDO;
import cn.iocoder.yudao.module.ai.enums.billing.AiBudgetPeriodTypeEnum;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * AI 预算周期计算工具。
 *
 * 规则：
 * - DAILY：滚动 24 小时，按配置创建时间作为锚点；
 * - MONTHLY：按配置创建时刻（日 + 时分秒）锚定自然月；
 * - 无配置：默认按自然月（当月 1 号 00:00）。
 */
public final class AiBudgetPeriodHelper {

    private AiBudgetPeriodHelper() {
    }

    public static LocalDateTime getCurrentPeriodStart(AiBudgetConfigDO config) {
        return getCurrentPeriodStart(config, LocalDateTime.now());
    }

    static LocalDateTime getCurrentPeriodStart(AiBudgetConfigDO config, LocalDateTime now) {
        if (config == null || config.getPeriodType() == null) {
            return startOfMonth(now);
        }
        if (AiBudgetPeriodTypeEnum.DAILY.getType().equals(config.getPeriodType())) {
            return getDailyRollingPeriodStart(config, now);
        }
        return getMonthlyAnchoredPeriodStart(config, now);
    }

    private static LocalDateTime getDailyRollingPeriodStart(AiBudgetConfigDO config, LocalDateTime now) {
        LocalDateTime anchor = config.getCreateTime();
        if (anchor == null) {
            return startOfDay(now);
        }
        if (now.isBefore(anchor)) {
            return anchor;
        }
        long elapsedDays = java.time.Duration.between(anchor, now).toDays();
        return anchor.plusDays(elapsedDays);
    }

    private static LocalDateTime getMonthlyAnchoredPeriodStart(AiBudgetConfigDO config, LocalDateTime now) {
        LocalDateTime anchor = config.getCreateTime();
        if (anchor == null) {
            return startOfMonth(now);
        }
        int anchorDay = anchor.getDayOfMonth();
        int anchorHour = anchor.getHour();
        int anchorMinute = anchor.getMinute();
        int anchorSecond = anchor.getSecond();
        int anchorNano = anchor.getNano();
        LocalDate currentDate = now.toLocalDate();
        LocalDate currentMonthAnchorDate = currentDate.withDayOfMonth(
                Math.min(anchorDay, currentDate.lengthOfMonth()));
        LocalDateTime currentMonthAnchorStart = currentMonthAnchorDate
                .atTime(anchorHour, anchorMinute, anchorSecond, anchorNano);
        if (!now.isBefore(currentMonthAnchorStart)) {
            return currentMonthAnchorStart;
        }
        LocalDate previousMonthDate = currentDate.minusMonths(1);
        LocalDate previousMonthAnchorDate = previousMonthDate.withDayOfMonth(
                Math.min(anchorDay, previousMonthDate.lengthOfMonth()));
        return previousMonthAnchorDate.atTime(anchorHour, anchorMinute, anchorSecond, anchorNano);
    }

    private static LocalDateTime startOfDay(LocalDateTime time) {
        return time.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

    private static LocalDateTime startOfMonth(LocalDateTime time) {
        return time.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

}
