package cn.iocoder.yudao.adminserver.modules.bpm.enums.oa;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;
/**
 * activiti 系统 错误码枚举类
 *
 * 003 activiti
 * 001 oa
 * activiti 系统，使用 1-003-000-000 段
 */
public interface OAErrorCodeConstants {
    ErrorCode LEAVE_NOT_EXISTS = new ErrorCode(1003001001, "请假申请不存在");
    ErrorCode PM_POST_NOT_EXISTS = new ErrorCode(1003001002, "项目经理岗位未设置");
    ErrorCode DEPART_PM_POST_NOT_EXISTS = new ErrorCode(1003001003, "部门的项目经理不存在");
    ErrorCode BM_POST_NOT_EXISTS = new ErrorCode(1003001004, "部门经理岗位未设置");
    ErrorCode DEPART_BM_POST_NOT_EXISTS = new ErrorCode(1003001005, "部门的部门经理不存在");
    ErrorCode HR_POST_NOT_EXISTS = new ErrorCode(1003001006, "HR岗位未设置");
    ErrorCode DAY_LEAVE_ERROR = new ErrorCode(1003001007, "请假天数必须大于0");

    ErrorCode PROCESS_INSTANCE_NOT_EXISTS = new ErrorCode(1003001008, "流程实例不存在");
    ErrorCode HIGHLIGHT_IMG_ERROR = new ErrorCode(1003001009, "获取高亮流程图异常");
}
