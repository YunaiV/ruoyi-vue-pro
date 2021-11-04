package cn.iocoder.yudao.adminserver.modules.activiti.enums.form;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * activiti 系统 错误码枚举类
 *
 * 003 activiti
 * 001 oa
 * activiti 系统，使用 1-003-000-000 段
 */
public interface WfFormErrorCodeConstants {
    ErrorCode FORM_NOT_EXISTS = new ErrorCode(1003001002, "动态表单不存在");
}
