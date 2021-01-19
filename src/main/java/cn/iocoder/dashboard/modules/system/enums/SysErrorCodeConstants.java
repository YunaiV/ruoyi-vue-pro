package cn.iocoder.dashboard.modules.system.enums;

import cn.iocoder.dashboard.common.exception.ErrorCode;

/**
 * 错误码枚举类
 *
 * system 系统，使用 1-002-000-000 段
 */
public interface SysErrorCodeConstants {

    // ========== AUTH 模块 1002000000 ==========
    ErrorCode AUTH_LOGIN_BAD_CREDENTIALS = new ErrorCode(1002000000, "登录失败，账号密码不正确");
    ErrorCode AUTH_LOGIN_USER_DISABLED = new ErrorCode(1002000001, "登录失败，账号被禁用");
    ErrorCode AUTH_LOGIN_FAIL_UNKNOWN = new ErrorCode(1002000002, "登录失败"); // 登陆失败的兜底，位置原因
    ErrorCode AUTH_LOGIN_CAPTCHA_NOT_FOUND = new ErrorCode(1002000003, "验证码不存在");
    ErrorCode AUTH_LOGIN_CAPTCHA_CODE_ERROR = new ErrorCode(1002000004, "验证码不正确");

    // ========== TOKEN 模块 1002001000 ==========
    ErrorCode TOKEN_EXPIRED = new ErrorCode(1002001000, "Token 已经过期");
    ErrorCode TOKEN_PARSE_FAIL = new ErrorCode(1002001001, "Token 解析失败");

    // ========== 菜单模块 1002002000 ==========
    ErrorCode MENU_NAME_DUPLICATE = new ErrorCode(1002002000, "已经存在该名字的菜单");
    ErrorCode MENU_PARENT_NOT_EXISTS = new ErrorCode(1002002001, "父菜单不存在");
    ErrorCode MENU_PARENT_ERROR = new ErrorCode(1002002002, "不能设置自己为父菜单");
    ErrorCode MENU_NOT_EXISTS = new ErrorCode(1002002003, "菜单不存在");
    ErrorCode MENU_EXISTS_CHILDREN = new ErrorCode(1002002004, "存在子菜单，无法删除");
    ErrorCode MENU_PARENT_NOT_DIR_OR_MENU = new ErrorCode(1002002005, "父菜单的类型必须是目录或者菜单");

    // ========== 角色模块 1002003000 ==========
    ErrorCode ROLE_NOT_EXISTS = new ErrorCode(1002003000, "角色不存在");
    ErrorCode ROLE_NAME_DUPLICATE = new ErrorCode(1002003001, "已经存在名为【{}}】的角色");
    ErrorCode ROLE_CODE_DUPLICATE = new ErrorCode(1002003002, "已经存在编码为【{}}】的角色");
    ErrorCode ROLE_CAN_NOT_UPDATE_SYSTEM_TYPE_ROLE = new ErrorCode(1002003004, "不能操作类型为系统内置的角色");

    // ========== 用户模块 1002004000 ==========
    ErrorCode USER_USERNAME_EXISTS = new ErrorCode(1002004000, "用户账号已经存在");
    ErrorCode USER_MOBILE_EXISTS = new ErrorCode(1002004001, "手机号已经存在");
    ErrorCode USER_EMAIL_EXISTS = new ErrorCode(1002004002, "邮箱已经存在");
    ErrorCode USER_NOT_EXISTS = new ErrorCode(1002004003, "用户不存在");
    ErrorCode USER_IMPORT_LIST_IS_EMPTY = new ErrorCode(1002004004, "导入用户数据不能为空！");

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
    ErrorCode DICT_TYPE_NOT_FOUND = new ErrorCode(1002006001, "当前字典类型不存在");
    ErrorCode DICT_TYPE_NOT_ENABLE = new ErrorCode(1002006002, "字典类型不处于开启状态，不允许选择");
    ErrorCode DICT_TYPE_NAME_DUPLICATE = new ErrorCode(1002006003, "已经存在该名字的字典类型");
    ErrorCode DICT_TYPE_TYPE_DUPLICATE = new ErrorCode(1002006004, "已经存在该类型的字典类型");
    ErrorCode DICT_TYPE_HAS_CHILDREN = new ErrorCode(1002006004, "无法删除，该字典类型还有字典数据");

    // ========== 字典数据 1002007000 ==========
    ErrorCode DICT_DATA_NOT_FOUND = new ErrorCode(1002007001, "当前字典数据不存在");
    ErrorCode DICT_DATA_NOT_ENABLE = new ErrorCode(1002007002, "字典数据不处于开启状态，不允许选择");
    ErrorCode DICT_DATA_VALUE_DUPLICATE = new ErrorCode(1002007003, "已经存在该值的字典数据");

    // ========== 通知公告 1002008000 ==========
    ErrorCode NOTICE_NOT_FOUND = new ErrorCode(1002008001, "当前通知公告不存在");

    // ========== 文件 1002009000 ==========
    ErrorCode FILE_PATH_EXISTS = new ErrorCode(1002009001, "文件路径已经存在");

    // ========== 参数配置 1002010000 ==========
    ErrorCode CONFIG_NOT_FOUND = new ErrorCode(1002010001, "参数配置不存在");
    ErrorCode CONFIG_NAME_DUPLICATE = new ErrorCode(1002010002, "参数配置 key 重复");
    ErrorCode CONFIG_CAN_NOT_DELETE_SYSTEM_TYPE = new ErrorCode(1002010003, "不能删除类型为系统内置的参数配置");
    ErrorCode CONFIG_GET_VALUE_ERROR_IF_SENSITIVE = new ErrorCode(1002010004, "不允许获取敏感配置到前端");
}
