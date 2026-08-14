package cn.iocoder.yudao.module.fms.util;

import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAccountSetDO;
import lombok.experimental.UtilityClass;

import java.time.YearMonth;

/**
 * FMS 会计期间工具类
 *
 * @author 芋道源码
 */
@UtilityClass
public final class FmsPeriodUtils {

    /**
     * 获得报表和结账内部使用的本年起始期间。
     *
     * 年初取数仍然按自然年计算；账套启用当年从启用期间开始，避免内部查询落到账套启用前的期间。
     *
     * @param accountSet 账套
     * @param endMonth 结束期间
     * @return 有效的本年起始期间
     */
    public static YearMonth getYearStartMonth(FmsAccountSetDO accountSet, YearMonth endMonth) {
        YearMonth yearStartMonth = YearMonth.of(endMonth.getYear(), 1);
        if (accountSet == null || accountSet.getStartTime() == null) {
            return yearStartMonth;
        }
        YearMonth accountStartMonth = YearMonth.from(accountSet.getStartTime());
        return accountStartMonth.isAfter(yearStartMonth) ? accountStartMonth : yearStartMonth;
    }

}
