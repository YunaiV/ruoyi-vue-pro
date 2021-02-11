package cn.iocoder.dashboard.modules.system.enums.dict;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 字典类型的类型枚举
 */
@Getter
@AllArgsConstructor
public enum SysDictTypeEnum {

    SYS_USER_SEX("sys_user_sex"), // 用户性别
    SYS_COMMON_STATUS("sys_common_status"), // 系统状态
    SYS_OPERATE_TYPE("sys_operate_type"), // 操作类型
    SYS_LOGIN_RESULT("sys_login_result"), // 登陆结果
    SYS_CONFIG_TYPE("sys_config_type"), // 参数配置类型
    SYS_BOOLEAN_STRING("sys_boolean_string"), // Boolean 是否类型

    INF_REDIS_TIMEOUT_TYPE("inf_redis_timeout_type"),  // Redis 超时类型
    ;


    /**
     * 值
     */
    private final String value;

}
