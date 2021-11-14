package cn.iocoder.yudao.adminserver.modules.bpm.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * 工作流 错误码枚举类
 *
 * 工作流系统，使用 1-003-000-000 段
 */
public interface BpmErrorCodeConstants {

    // ==========  通用流程处理 模块 1-003-000-000 ==========
    ErrorCode PROCESS_INSTANCE_NOT_EXISTS = new ErrorCode(1003000001, "流程实例不存在");
    ErrorCode HIGHLIGHT_IMG_ERROR = new ErrorCode(1003000002, "获取高亮流程图异常");


    // ========== OA 流程模块 1-003-001-000 ==========
    // TODO @jason：前缀
    ErrorCode LEAVE_NOT_EXISTS = new ErrorCode(1003001001, "请假申请不存在");
    ErrorCode PM_POST_NOT_EXISTS = new ErrorCode(1003001002, "项目经理岗位未设置");
    ErrorCode DEPART_PM_POST_NOT_EXISTS = new ErrorCode(1003001003, "部门的项目经理不存在");
    ErrorCode BM_POST_NOT_EXISTS = new ErrorCode(1003001004, "部门经理岗位未设置");
    ErrorCode DEPART_BM_POST_NOT_EXISTS = new ErrorCode(1003001005, "部门的部门经理不存在");
    ErrorCode HR_POST_NOT_EXISTS = new ErrorCode(1003001006, "HR岗位未设置");
    ErrorCode DAY_LEAVE_ERROR = new ErrorCode(1003001007, "请假天数必须>=1");

}
