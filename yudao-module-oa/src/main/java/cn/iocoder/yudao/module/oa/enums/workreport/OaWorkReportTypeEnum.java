package cn.iocoder.yudao.module.oa.enums.workreport;

import cn.hutool.core.util.ArrayUtil;
import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * 工作汇报类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum OaWorkReportTypeEnum implements ArrayValuable<Integer> {

    DAILY(1, "日报"),
    WEEKLY(2, "周报"),
    MONTHLY(3, "月报");

    public static final Integer[] ARRAYS = Arrays.stream(values())
            .map(OaWorkReportTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名称
     */
    private final String name;

    /**
     * 获得指定类型对应的枚举
     *
     * @param type 类型
     * @return 对应枚举，未匹配时返回 null
     */
    public static OaWorkReportTypeEnum valueOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    /**
     * 按汇报类型格式化所属周期，仅用于展示与统计，不单独存储。
     *
     * @param startTime 汇报开始时间
     * @return 日期、自然年周次或月份
     */
    public String formatPeriod(LocalDateTime startTime) {
        if (this == DAILY) {
            return startTime.toLocalDate().toString();
        }
        if (this == WEEKLY) {
            LocalDateTime mondayTime = startTime.with(LocalTime.MIN)
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            // 跨年同一周统一归到包含 1 月 1 日的年份，避免一周被统计两次
            int year = mondayTime.plusDays(6).getYear();
            LocalDateTime firstMondayTime = LocalDateTime.of(year, 1, 1, 0, 0)
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            return String.format("%d-%02d", year, ChronoUnit.WEEKS.between(firstMondayTime, mondayTime) + 1);
        }
        return YearMonth.from(startTime).toString();
    }

}
