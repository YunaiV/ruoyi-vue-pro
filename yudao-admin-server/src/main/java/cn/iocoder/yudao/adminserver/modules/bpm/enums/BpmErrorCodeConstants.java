package cn.iocoder.yudao.adminserver.modules.bpm.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * 工作流 错误码枚举类
 *
 * 工作流系统，使用 1-009-000-000 段
 */
public interface BpmErrorCodeConstants {

    // ==========  通用流程处理 模块 1-009-000-000 ==========
    ErrorCode PROCESS_INSTANCE_NOT_EXISTS = new ErrorCode(1009000001, "流程实例不存在");
    ErrorCode HIGHLIGHT_IMG_ERROR = new ErrorCode(1009000002, "获取高亮流程图异常");


    // ========== OA 流程模块 1-009-001-000 ==========
    ErrorCode OA_LEAVE_NOT_EXISTS = new ErrorCode(1009001001, "请假申请不存在");
    ErrorCode OA_PM_POST_NOT_EXISTS = new ErrorCode(1009001002, "项目经理岗位未设置");
    ErrorCode OA_DEPART_PM_POST_NOT_EXISTS = new ErrorCode(1009001009, "部门的项目经理不存在");
    ErrorCode OA_BM_POST_NOT_EXISTS = new ErrorCode(1009001004, "部门经理岗位未设置");
    ErrorCode OA_DEPART_BM_POST_NOT_EXISTS = new ErrorCode(1009001005, "部门的部门经理不存在");
    ErrorCode OA_HR_POST_NOT_EXISTS = new ErrorCode(1009001006, "HR岗位未设置");
    ErrorCode OA_DAY_LEAVE_ERROR = new ErrorCode(1009001007, "请假天数必须>=1");

    // ========== OA 工作流模块 1-009-002-000 ==========
    ErrorCode BPM_MODEL_KEY_EXISTS = new ErrorCode(1009002000, "已经存在流程标识为【{}】的流程");
    ErrorCode BPMN_MODEL_NOT_EXISTS = new ErrorCode(1009002001, "流程模型不存在");

    ErrorCode BPMN_MODEL_ERROR = new ErrorCode(1004001002, "工作流模型异常");
    ErrorCode BPMN_MODEL_PROCESS_NOT_EXISTS = new ErrorCode(1004001009, "流程数据为空");
    ErrorCode BPMN_PROCESS_DEFINITION_NOT_EXISTS = new ErrorCode(1004001004, "流程定义不存在");

    // ========== 动态表单模块 1-009-003-000 ==========
    ErrorCode BPM_FORM_NOT_EXISTS = new ErrorCode(1009003000, "动态表单不存在");
    ErrorCode BPM_FORM_FIELD_REPEAT = new ErrorCode(1009003000, "表单项({}) 和 ({}) 使用了相同的字段名({})");

}
