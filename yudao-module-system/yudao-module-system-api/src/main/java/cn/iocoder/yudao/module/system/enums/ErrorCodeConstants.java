package cn.iocoder.yudao.module.system.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * System 错误码枚举类
 *
 * system 系统，使用 1-002-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== AUTH 模块 1002000000 ==========
    ErrorCode AUTH_LOGIN_BAD_CREDENTIALS = new ErrorCode(1002000000, "登录失败，账号密码不正确");
    ErrorCode AUTH_LOGIN_USER_DISABLED = new ErrorCode(1002000001, "登录失败，账号被禁用");
    ErrorCode AUTH_LOGIN_FAIL_UNKNOWN = new ErrorCode(1002000002, "登录失败"); // 登录失败的兜底，未知原因
    ErrorCode AUTH_LOGIN_CAPTCHA_NOT_FOUND = new ErrorCode(1002000003, "验证码不存在");
    ErrorCode AUTH_LOGIN_CAPTCHA_CODE_ERROR = new ErrorCode(1002000004, "验证码不正确");
    ErrorCode AUTH_THIRD_LOGIN_NOT_BIND = new ErrorCode(1002000005, "未绑定账号，需要进行绑定");
    ErrorCode AUTH_TOKEN_EXPIRED = new ErrorCode(1002000006, "Token 已经过期");

    // ========== 菜单模块 1002002000 ==========
    ErrorCode MENU_NAME_DUPLICATE = new ErrorCode(1002002000, "已经存在该名字的菜单");
    ErrorCode MENU_PARENT_NOT_EXISTS = new ErrorCode(1002002001, "父菜单不存在");
    ErrorCode MENU_PARENT_ERROR = new ErrorCode(1002002002, "不能设置自己为父菜单");
    ErrorCode MENU_NOT_EXISTS = new ErrorCode(1002002003, "菜单不存在");
    ErrorCode MENU_EXISTS_CHILDREN = new ErrorCode(1002002004, "存在子菜单，无法删除");
    ErrorCode MENU_PARENT_NOT_DIR_OR_MENU = new ErrorCode(1002002005, "父菜单的类型必须是目录或者菜单");

    // ========== 角色模块 1002003000 ==========
    ErrorCode ROLE_NOT_EXISTS = new ErrorCode(1002003000, "角色不存在");
    ErrorCode ROLE_NAME_DUPLICATE = new ErrorCode(1002003001, "已经存在名为【{}】的角色");
    ErrorCode ROLE_CODE_DUPLICATE = new ErrorCode(1002003002, "已经存在编码为【{}】的角色");
    ErrorCode ROLE_CAN_NOT_UPDATE_SYSTEM_TYPE_ROLE = new ErrorCode(1002003004, "不能操作类型为系统内置的角色");
    ErrorCode ROLE_IS_DISABLE = new ErrorCode(1002003004, "名字为【{}】的角色已被禁用");

    // ========== 用户模块 1002004000 ==========
    ErrorCode USER_USERNAME_EXISTS = new ErrorCode(1002004000, "用户账号已经存在");
    ErrorCode USER_MOBILE_EXISTS = new ErrorCode(1002004001, "手机号已经存在");
    ErrorCode USER_EMAIL_EXISTS = new ErrorCode(1002004002, "邮箱已经存在");
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1002004003, "用户不存在");
    ErrorCode USER_IMPORT_LIST_IS_EMPTY = new ErrorCode(1002004004, "导入用户数据不能为空！");
    ErrorCode USER_PASSWORD_FAILED = new ErrorCode(1002004005, "用户密码校验失败");
    ErrorCode USER_IS_DISABLE = new ErrorCode(1002003004, "名字为【{}】的用户已被禁用");

    // ========== 部门模块 1002005000 ==========
    ErrorCode DEPT_NAME_DUPLICATE = new ErrorCode(1002004001, "已经存在该名字的部门");
    ErrorCode DEPT_PARENT_NOT_EXITS = new ErrorCode(1002004002,"父级部门不存在");
    ErrorCode DEPT_NOT_FOUND = new ErrorCode(1002004003, "当前部门不存在");
    ErrorCode DEPT_EXITS_CHILDREN = new ErrorCode(1002004004, "存在子部门，无法删除");
    ErrorCode DEPT_PARENT_ERROR = new ErrorCode(1002004005, "不能设置自己为父部门");
    ErrorCode DEPT_EXISTS_USER = new ErrorCode(1002004006, "部门中存在员工，无法删除");
    ErrorCode DEPT_NOT_ENABLE = new ErrorCode(1002004007, "部门不处于开启状态，不允许选择");
    ErrorCode DEPT_PARENT_IS_CHILD = new ErrorCode(1002004008, "不能设置自己的子部门为父部门");

    // ========== 岗位模块 1002005000 ==========
    ErrorCode POST_NOT_FOUND = new ErrorCode(1002005001, "当前岗位不存在");
    ErrorCode POST_NOT_ENABLE = new ErrorCode(1002005002, "岗位({}) 不处于开启状态，不允许选择");
    ErrorCode POST_NAME_DUPLICATE = new ErrorCode(1002005001, "已经存在该名字的岗位");
    ErrorCode POST_CODE_DUPLICATE = new ErrorCode(1002005001, "已经存在该标识的岗位");

    // ========== 字典类型 1002006000 ==========
    ErrorCode DICT_TYPE_NOT_EXISTS = new ErrorCode(1002006001, "当前字典类型不存在");
    ErrorCode DICT_TYPE_NOT_ENABLE = new ErrorCode(1002006002, "字典类型不处于开启状态，不允许选择");
    ErrorCode DICT_TYPE_NAME_DUPLICATE = new ErrorCode(1002006003, "已经存在该名字的字典类型");
    ErrorCode DICT_TYPE_TYPE_DUPLICATE = new ErrorCode(1002006004, "已经存在该类型的字典类型");
    ErrorCode DICT_TYPE_HAS_CHILDREN = new ErrorCode(1002006004, "无法删除，该字典类型还有字典数据");

    // ========== 字典数据 1002007000 ==========
    ErrorCode DICT_DATA_NOT_EXISTS = new ErrorCode(1002007001, "当前字典数据不存在");
    ErrorCode DICT_DATA_NOT_ENABLE = new ErrorCode(1002007002, "字典数据({})不处于开启状态，不允许选择");
    ErrorCode DICT_DATA_VALUE_DUPLICATE = new ErrorCode(1002007003, "已经存在该值的字典数据");

    // ========== 通知公告 1002008000 ==========
    ErrorCode NOTICE_NOT_FOUND = new ErrorCode(1002008001, "当前通知公告不存在");

    // ========== 文件 1002009000 ==========
    ErrorCode FILE_PATH_EXISTS = new ErrorCode(1002009001, "文件路径已经存在");
    ErrorCode FILE_UPLOAD_FAILED = new ErrorCode(1002009002, "文件上传失败");
    ErrorCode FILE_IS_EMPTY= new ErrorCode(1002009003, "文件为空");

    // ========== 短信渠道 1002011000 ==========
    ErrorCode SMS_CHANNEL_NOT_EXISTS = new ErrorCode(1002011000, "短信渠道不存在");
    ErrorCode SMS_CHANNEL_DISABLE = new ErrorCode(1002011001, "短信渠道不处于开启状态，不允许选择");
    ErrorCode SMS_CHANNEL_HAS_CHILDREN = new ErrorCode(1002011002, "无法删除，该短信渠道还有短信模板");

    // ========== 短信模板 1002011000 ==========
    ErrorCode SMS_TEMPLATE_NOT_EXISTS = new ErrorCode(1002011000, "短信模板不存在");
    ErrorCode SMS_TEMPLATE_CODE_DUPLICATE = new ErrorCode(1002011001, "已经存在编码为【{}】的短信模板");

    // ========== 短信发送 1002012000 ==========
    ErrorCode SMS_SEND_MOBILE_NOT_EXISTS = new ErrorCode(1002012000, "手机号不存在");
    ErrorCode SMS_SEND_MOBILE_TEMPLATE_PARAM_MISS = new ErrorCode(1002012001, "模板参数({})缺失");
    ErrorCode SMS_SEND_TEMPLATE_NOT_EXISTS = new ErrorCode(1002012002, "短信模板不存在");

    // ========== 短信验证码 1002013000 ==========
    ErrorCode SMS_CODE_NOT_FOUND = new ErrorCode(1002013000, "验证码不存在");
    ErrorCode SMS_CODE_EXPIRED = new ErrorCode(1002013001, "验证码已过期");
    ErrorCode SMS_CODE_USED = new ErrorCode(1002013002, "验证码已使用");
    ErrorCode SMS_CODE_NOT_CORRECT = new ErrorCode(1002013004, "验证码不正确");
    ErrorCode SMS_CODE_EXCEED_SEND_MAXIMUM_QUANTITY_PER_DAY = new ErrorCode(1002013005, "超过每日短信发送数量");
    ErrorCode SMS_CODE_SEND_TOO_FAST = new ErrorCode(1002013006, "短信发送过于频率");
    ErrorCode SMS_CODE_IS_EXISTS = new ErrorCode(1002013007, "手机号已被使用");
    ErrorCode SMS_CODE_IS_UNUSED = new ErrorCode(1002013008, "验证码未被使用");

    // ========== 租户模块 1002014000 ==========
    ErrorCode TENANT_NOT_EXISTS = new ErrorCode(1002014000, "租户不存在");

    // ========== 错误码模块 1002015000 ==========
    ErrorCode ERROR_CODE_NOT_EXISTS = new ErrorCode(1002015000, "错误码不存在");
    ErrorCode ERROR_CODE_DUPLICATE = new ErrorCode(1002015001, "已经存在编码为【{}】的错误码");

    // ========== 社交用户 1002015000 ==========
    ErrorCode SOCIAL_USER_AUTH_FAILURE = new ErrorCode(1002015000, "社交授权失败，原因是：{}");
    ErrorCode SOCIAL_USER_UNBIND_NOT_SELF = new ErrorCode(1002015001, "社交解绑失败，非当前用户绑定");
    ErrorCode SOCIAL_USER_NOT_FOUND = new ErrorCode(1002015001, "社交授权失败，找不到对应的用户");

}
