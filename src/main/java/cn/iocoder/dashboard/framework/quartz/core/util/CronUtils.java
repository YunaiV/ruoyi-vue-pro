package cn.iocoder.dashboard.framework.quartz.core.util;

import org.quartz.CronExpression;

/**
 * Quartz Cron 表达式的工具类
 *
 * @author 芋道源码
 */
public class CronUtils {

    /**
     * 校验 CRON 表达式是否有效
     *
     * @param cronExpression CRON 表达式
     * @return 是否有效
     */
    public static boolean isValid(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

}
