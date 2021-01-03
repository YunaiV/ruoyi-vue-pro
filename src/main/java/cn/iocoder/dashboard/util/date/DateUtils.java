package cn.iocoder.dashboard.util.date;

import java.time.Duration;
import java.util.Date;

/**
 * 时间工具类
 */
public class DateUtils {

    public static Date addTime(Duration duration) {
        return new Date(System.currentTimeMillis() + duration.toMillis());
    }

    public static boolean isExpired(Date time) {
        return System.currentTimeMillis() > time.getTime();
    }

}
