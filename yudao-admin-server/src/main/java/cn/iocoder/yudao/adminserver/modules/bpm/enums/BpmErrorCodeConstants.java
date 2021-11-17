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
    ErrorCode OA_LEAVE_NOT_EXISTS = new ErrorCode(1003001001, "请假申请不存在");
    ErrorCode OA_PM_POST_NOT_EXISTS = new ErrorCode(1003001002, "项目经理岗位未设置");
    ErrorCode OA_DEPART_PM_POST_NOT_EXISTS = new ErrorCode(1003001003, "部门的项目经理不存在");
    ErrorCode OA_BM_POST_NOT_EXISTS = new ErrorCode(1003001004, "部门经理岗位未设置");
    ErrorCode OA_DEPART_BM_POST_NOT_EXISTS = new ErrorCode(1003001005, "部门的部门经理不存在");
    ErrorCode OA_HR_POST_NOT_EXISTS = new ErrorCode(1003001006, "HR岗位未设置");
    ErrorCode OA_DAY_LEAVE_ERROR = new ErrorCode(1003001007, "请假天数必须>=1");


    // ========== OA 工作流模块 1-004-001-000 ==========
    ErrorCode BPMN_MODEL_EDITOR_SOURCE_NOT_EXISTS = new ErrorCode(1004001001, "模型数据为空，请先成功设计流程并保存");
    ErrorCode BPMN_MODEL_ERROR = new ErrorCode(1004001002, "工作流模型异常");
    ErrorCode BPMN_MODEL_PROCESS_NOT_EXISTS = new ErrorCode(1004001003, "流程数据为空");

}
