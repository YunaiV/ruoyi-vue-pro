package cn.iocoder.dashboard.util.date;

import java.time.Duration;
import java.util.Date;

/**
 * 时间工具类
 */
public class DateUtils {

    public static final String FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND = "yyyy-MM-dd HH:mm:ss";

    public static Date addTime(Duration duration) {
        return new Date(System.currentTimeMillis() + duration.toMillis());
    }

    public static boolean isExpired(Date time) {
        return System.currentTimeMillis() > time.getTime();
    }

    public static Long diff(Date endTime, Date startTime) {
        return endTime.getTime() - startTime.getTime();
    }

}
