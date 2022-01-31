package cn.iocoder.yudao.adminserver.modules.bpm.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

public interface BpmErrorCodeConstants {
    // ========== 用户组模块 1-009-011-000 ==========
    ErrorCode USER_GROUP_NOT_EXISTS = new ErrorCode(1009011000, "用户组不存在");
    ErrorCode USER_GROUP_IS_DISABLE = new ErrorCode(1009011001, "名字为【{}】的用户组已被禁用");

    // ========== 动态表单模块 1-009-010-000 ==========
    ErrorCode FORM_NOT_EXISTS = new ErrorCode(1009010000, "动态表单不存在");
    ErrorCode FORM_FIELD_REPEAT = new ErrorCode(1009010000, "表单项({}) 和 ({}) 使用了相同的字段名({})");
}
