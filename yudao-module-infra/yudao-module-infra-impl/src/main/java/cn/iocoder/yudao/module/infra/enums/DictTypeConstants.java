package cn.iocoder.yudao.module.infra.enums;

/**
 * Infra 字典类型的枚举类
 *
 * @author 芋道源码
 */
public interface DictTypeConstants {

    String REDIS_TIMEOUT_TYPE = "inf_redis_timeout_type"; // Redis 超时类型

    String JOB_STATUS = "inf_job_status"; // 定时任务状态的枚举
    String JOB_LOG_STATUS = "inf_job_log_status"; // 定时任务日志状态的枚举

    String API_ERROR_LOG_PROCESS_STATUS = "inf_api_error_log_process_status"; // API 错误日志的处理状态的枚举

    String ERROR_CODE_TYPE = "inf_error_code_type"; // 错误码的类型枚举

    String CONFIG_TYPE = "sys_config_type"; // 参数配置类型
    String BOOLEAN_STRING = "sys_boolean_string"; // Boolean 是否类型

}
