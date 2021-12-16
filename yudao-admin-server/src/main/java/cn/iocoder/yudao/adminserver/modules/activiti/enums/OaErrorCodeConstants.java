package cn.iocoder.yudao.adminserver.modules.activiti.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
/**
 * activiti 系统 错误码枚举类
 *
 * 003 activiti
 * 001 oa
 * activiti 系统，使用 1-003-000-000 段
 */
public interface OaErrorCodeConstants {
    ErrorCode LEAVE_NOT_EXISTS = new ErrorCode(1003001001, "请假申请不存在");
}
