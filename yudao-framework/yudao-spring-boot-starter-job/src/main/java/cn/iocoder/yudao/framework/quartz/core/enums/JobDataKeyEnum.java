package cn.iocoder.yudao.framework.quartz.core.enums;

/**
 * Quartz Job Data 的 key 枚举
 */
public enum JobDataKeyEnum {

    JOB_ID,
    JOB_HANDLER_NAME,
    JOB_HANDLER_PARAM,
    JOB_RETRY_COUNT, // 最大重试次数
    JOB_RETRY_INTERVAL, // 每次重试间隔

}
