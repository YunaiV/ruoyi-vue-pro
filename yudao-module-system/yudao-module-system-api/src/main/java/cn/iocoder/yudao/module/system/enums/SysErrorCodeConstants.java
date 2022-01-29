package cn.iocoder.yudao.module.system.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * System 错误码枚举类
 *
 * system 系统，使用 1-006-000-000 段
 */
public interface SysErrorCodeConstants {

    // ========== 短信发送 1006000000 ==========
    ErrorCode SMS_SEND_MOBILE_NOT_EXISTS = new ErrorCode(1006000000, "手机号不存在");
    ErrorCode SMS_SEND_MOBILE_TEMPLATE_PARAM_MISS = new ErrorCode(1006000001, "模板参数({})缺失");
    ErrorCode SMS_SEND_TEMPLATE_NOT_EXISTS = new ErrorCode(1006000000, "短信模板不存在");

    // ========== 社交模块 1006002000 ==========
    ErrorCode SOCIAL_AUTH_FAILURE = new ErrorCode(1006002000, "社交授权失败，原因是：{}");
    ErrorCode SOCIAL_UNBIND_NOT_SELF = new ErrorCode(1006002001, "社交解绑失败，非当前用户绑定");

    // ========== 用户模块 1006003000 ==========
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1006003000, "用户不存在");
    ErrorCode USER_IS_DISABLE = new ErrorCode(1006003001, "名字为【{}】的用户已被禁用");

    // ========== 部门模块 1006004000 ==========
    ErrorCode DEPT_NOT_FOUND = new ErrorCode(1006004000, "当前部门不存在");
    ErrorCode DEPT_NOT_ENABLE = new ErrorCode(1006004001, "部门不处于开启状态，不允许选择");

    // ========== 角色模块 1006005000 ==========
    ErrorCode ROLE_NOT_EXISTS = new ErrorCode(1006005000, "角色不存在");
    ErrorCode ROLE_IS_DISABLE = new ErrorCode(1006005001, "名字为【{}】的角色已被禁用");

    // ========== 岗位模块 1006007000 ==========
    ErrorCode POST_NOT_FOUND = new ErrorCode(1006007000, "当前岗位不存在");
    ErrorCode POST_NOT_ENABLE = new ErrorCode(1006007001, "岗位({}) 不处于开启状态，不允许选择");

}
