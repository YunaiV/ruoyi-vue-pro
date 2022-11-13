package cn.iocoder.yudao.framework.common.util.date;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 时间工具类，用于 {@link java.time.LocalDateTime}
 *
 * @author 芋道源码
 */
public class LocalDateTimeUtils {

    public static LocalDateTime addTime(Duration duration) {
        return LocalDateTime.now().plus(duration);
    }

}
