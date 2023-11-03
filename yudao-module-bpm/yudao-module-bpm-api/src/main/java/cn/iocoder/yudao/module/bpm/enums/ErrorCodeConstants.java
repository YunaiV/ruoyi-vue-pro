package cn.iocoder.yudao.module.bpm.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Bpm 错误码枚举类
 * <p>
 * bpm 系统，使用 1-009-000-000 段
 */
public interface ErrorCodeConstants {

    // ==========  通用流程处理 模块 1-009-000-000 ==========
    ErrorCode HIGHLIGHT_IMG_ERROR = new ErrorCode(1_009_000_002, "获取高亮流程图异常");

    // ========== OA 流程模块 1-009-001-000 ==========
    ErrorCode OA_LEAVE_NOT_EXISTS = new ErrorCode(1_009_001_001, "请假申请不存在");
    ErrorCode OA_PM_POST_NOT_EXISTS = new ErrorCode(1_009_001_002, "项目经理岗位未设置");
    ErrorCode OA_DEPART_PM_POST_NOT_EXISTS = new ErrorCode(1_009_001_009, "部门的项目经理不存在");
    ErrorCode OA_BM_POST_NOT_EXISTS = new ErrorCode(1_009_001_004, "部门经理岗位未设置");
    ErrorCode OA_DEPART_BM_POST_NOT_EXISTS = new ErrorCode(1_009_001_005, "部门的部门经理不存在");
    ErrorCode OA_HR_POST_NOT_EXISTS = new ErrorCode(1_009_001_006, "HR岗位未设置");
    ErrorCode OA_DAY_LEAVE_ERROR = new ErrorCode(1_009_001_007, "请假天数必须>=1");

    // ========== 流程模型 1-009-002-000 ==========
    ErrorCode MODEL_KEY_EXISTS = new ErrorCode(1_009_002_000, "已经存在流程标识为【{}】的流程");
    ErrorCode MODEL_NOT_EXISTS = new ErrorCode(1_009_002_001, "流程模型不存在");
    ErrorCode MODEL_KEY_VALID = new ErrorCode(1_009_002_002, "流程标识格式不正确，需要以字母或下划线开头，后接任意字母、数字、中划线、下划线、句点！");
    ErrorCode MODEL_DEPLOY_FAIL_FORM_NOT_CONFIG = new ErrorCode(1_009_002_003, "部署流程失败，原因：流程表单未配置，请点击【修改流程】按钮进行配置");
    ErrorCode MODEL_DEPLOY_FAIL_TASK_ASSIGN_RULE_NOT_CONFIG = new ErrorCode(1_009_002_004, "部署流程失败，" +
            "原因：用户任务({})未配置分配规则，请点击【修改流程】按钮进行配置");
    ErrorCode MODEL_DEPLOY_FAIL_TASK_INFO_EQUALS = new ErrorCode(1_009_003_005, "流程定义部署失败，原因：信息未发生变化");

    // ========== 流程定义 1-009-003-000 ==========
    ErrorCode PROCESS_DEFINITION_KEY_NOT_MATCH = new ErrorCode(1_009_003_000, "流程定义的标识期望是({})，当前是({})，请修改 BPMN 流程图");
    ErrorCode PROCESS_DEFINITION_NAME_NOT_MATCH = new ErrorCode(1_009_003_001, "流程定义的名字期望是({})，当前是({})，请修改 BPMN 流程图");
    ErrorCode PROCESS_DEFINITION_NOT_EXISTS = new ErrorCode(1_009_003_002, "流程定义不存在");
    ErrorCode PROCESS_DEFINITION_IS_SUSPENDED = new ErrorCode(1_009_003_003, "流程定义处于挂起状态");
    ErrorCode PROCESS_DEFINITION_BPMN_MODEL_NOT_EXISTS = new ErrorCode(1_009_003_004, "流程定义的模型不存在");

    // ========== 流程实例 1-009-004-000 ==========
    ErrorCode PROCESS_INSTANCE_NOT_EXISTS = new ErrorCode(1_009_004_000, "流程实例不存在");
    ErrorCode PROCESS_INSTANCE_CANCEL_FAIL_NOT_EXISTS = new ErrorCode(1_009_004_001, "流程取消失败，流程不处于运行中");
    ErrorCode PROCESS_INSTANCE_CANCEL_FAIL_NOT_SELF = new ErrorCode(1_009_004_002, "流程取消失败，该流程不是你发起的");

    // ========== 流程任务 1-009-005-000 ==========
    ErrorCode TASK_OPERATE_FAIL_ASSIGN_NOT_SELF = new ErrorCode(1_009_005_001, "操作失败，原因：该任务的审批人不是你");
    ErrorCode TASK_NOT_EXISTS = new ErrorCode(1_009_005_002, "流程任务不存在");
    ErrorCode TASK_IS_PENDING = new ErrorCode(1_009_005_003, "当前任务处于挂起状态，不能操作");
    ErrorCode TASK_TARGET_NODE_NOT_EXISTS = new ErrorCode(1_009_005_004, " 目标节点不存在");
    ErrorCode TASK_RETURN_FAIL_SOURCE_TARGET_ERROR = new ErrorCode(1_009_005_006, "回退任务失败，目标节点是在并行网关上或非同一路线上，不可跳转");
    ErrorCode TASK_DELEGATE_FAIL_USER_REPEAT = new ErrorCode(1_009_005_007, "任务委派失败，委派人和当前审批人为同一人");
    ErrorCode TASK_DELEGATE_FAIL_USER_NOT_EXISTS = new ErrorCode(1_009_005_008, "任务委派失败，被委派人不存在");
    ErrorCode TASK_ADD_SIGN_USER_NOT_EXIST = new ErrorCode(1_009_005_009, "任务加签：选择的用户不存在");
    ErrorCode TASK_ADD_SIGN_TYPE_ERROR = new ErrorCode(1_009_005_010, "任务加签：当前任务已经{}，不能{}");
    ErrorCode TASK_ADD_SIGN_USER_REPEAT = new ErrorCode(1_009_005_011, "任务加签失败，加签人与现有审批人[{}]重复");
    ErrorCode TASK_SUB_SIGN_NO_PARENT = new ErrorCode(1_009_005_011, "任务减签失败，被减签的任务必须是通过加签生成的任务");

    // ========== 流程任务分配规则 1-009-006-000 ==========
    ErrorCode TASK_ASSIGN_RULE_EXISTS = new ErrorCode(1_009_006_000, "流程({}) 的任务({}) 已经存在分配规则");
    ErrorCode TASK_ASSIGN_RULE_NOT_EXISTS = new ErrorCode(1_009_006_001, "流程任务分配规则不存在");
    ErrorCode TASK_UPDATE_FAIL_NOT_MODEL = new ErrorCode(1_009_006_002, "只有流程模型的任务分配规则，才允许被修改");
    ErrorCode TASK_CREATE_FAIL_NO_CANDIDATE_USER = new ErrorCode(1_009_006_003, "操作失败，原因：找不到任务的审批人！");
    ErrorCode TASK_ASSIGN_SCRIPT_NOT_EXISTS = new ErrorCode(1_009_006_004, "操作失败，原因：任务分配脚本({}) 不存在");

    // ========== 动态表单模块 1-009-010-000 ==========
    ErrorCode FORM_NOT_EXISTS = new ErrorCode(1_009_010_000, "动态表单不存在");
    ErrorCode FORM_FIELD_REPEAT = new ErrorCode(1_009_010_001, "表单项({}) 和 ({}) 使用了相同的字段名({})");

    // ========== 用户组模块 1-009-011-000 ==========
    ErrorCode USER_GROUP_NOT_EXISTS = new ErrorCode(1_009_011_000, "用户组不存在");
    ErrorCode USER_GROUP_IS_DISABLE = new ErrorCode(1_009_011_001, "名字为【{}】的用户组已被禁用");

}
