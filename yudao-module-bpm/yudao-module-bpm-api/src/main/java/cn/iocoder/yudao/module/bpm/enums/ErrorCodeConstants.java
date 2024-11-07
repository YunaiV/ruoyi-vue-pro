package cn.iocoder.yudao.module.bpm.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Bpm 错误码枚举类
 * <p>
 * bpm 系统，使用 1-009-000-000 段
 */
public interface ErrorCodeConstants {

    // ==========  通用流程处理 模块 1-009-000-000 ==========

    // ========== OA 流程模块 1-009-001-000 ==========
    ErrorCode OA_LEAVE_NOT_EXISTS = new ErrorCode(1_009_001_001, "请假申请不存在");

    // ========== 流程模型 1-009-002-000 ==========
    ErrorCode MODEL_KEY_EXISTS = new ErrorCode(1_009_002_000, "已经存在流程标识为【{}】的流程");
    ErrorCode MODEL_NOT_EXISTS = new ErrorCode(1_009_002_001, "流程模型不存在");
    ErrorCode MODEL_KEY_VALID = new ErrorCode(1_009_002_002, "流程标识格式不正确，需要以字母或下划线开头，后接任意字母、数字、中划线、下划线、句点！");
    ErrorCode MODEL_DEPLOY_FAIL_FORM_NOT_CONFIG = new ErrorCode(1_009_002_003, "部署流程失败，原因：流程表单未配置，请点击【修改流程】按钮进行配置");
    ErrorCode MODEL_DEPLOY_FAIL_TASK_CANDIDATE_NOT_CONFIG = new ErrorCode(1_009_002_004, "部署流程失败，" +
            "原因：用户任务({})未配置审批人，请点击【流程设计】按钮，选择该它的【任务（审批人）】进行配置");
    ErrorCode MODEL_DEPLOY_FAIL_BPMN_START_EVENT_NOT_EXISTS = new ErrorCode(1_009_002_005, "部署流程失败，原因：BPMN 流程图中，没有开始事件");
    ErrorCode MODEL_DEPLOY_FAIL_BPMN_USER_TASK_NAME_NOT_EXISTS = new ErrorCode(1_009_002_006, "部署流程失败，原因：BPMN 流程图中，用户任务({})的名字不存在");
    ErrorCode MODEL_UPDATE_FAIL_NOT_MANAGER = new ErrorCode(1_009_002_007, "操作流程失败，原因：你不是该流程的管理员");

    // ========== 流程定义 1-009-003-000 ==========
    ErrorCode PROCESS_DEFINITION_KEY_NOT_MATCH = new ErrorCode(1_009_003_000, "流程定义的标识期望是({})，当前是({})，请修改 BPMN 流程图");
    ErrorCode PROCESS_DEFINITION_NAME_NOT_MATCH = new ErrorCode(1_009_003_001, "流程定义的名字期望是({})，当前是({})，请修改 BPMN 流程图");
    ErrorCode PROCESS_DEFINITION_NOT_EXISTS = new ErrorCode(1_009_003_002, "流程定义不存在");
    ErrorCode PROCESS_DEFINITION_IS_SUSPENDED = new ErrorCode(1_009_003_003, "流程定义处于挂起状态");

    // ========== 流程实例 1-009-004-000 ==========
    ErrorCode PROCESS_INSTANCE_NOT_EXISTS = new ErrorCode(1_009_004_000, "流程实例不存在");
    ErrorCode PROCESS_INSTANCE_CANCEL_FAIL_NOT_EXISTS = new ErrorCode(1_009_004_001, "流程取消失败，流程不处于运行中");
    ErrorCode PROCESS_INSTANCE_CANCEL_FAIL_NOT_SELF = new ErrorCode(1_009_004_002, "流程取消失败，该流程不是你发起的");
    ErrorCode PROCESS_INSTANCE_START_USER_SELECT_ASSIGNEES_NOT_CONFIG = new ErrorCode(1_009_004_003, "审批任务({})的审批人未配置");
    ErrorCode PROCESS_INSTANCE_START_USER_SELECT_ASSIGNEES_NOT_EXISTS = new ErrorCode(1_009_004_004, "审批任务({})的审批人({})不存在");
    ErrorCode PROCESS_INSTANCE_START_USER_CAN_START = new ErrorCode(1_009_004_005, "发起流程失败，你没有权限发起该流程");

    // ========== 流程任务 1-009-005-000 ==========
    ErrorCode TASK_OPERATE_FAIL_ASSIGN_NOT_SELF = new ErrorCode(1_009_005_001, "操作失败，原因：该任务的审批人不是你");
    ErrorCode TASK_NOT_EXISTS = new ErrorCode(1_009_005_002, "流程任务不存在");
    ErrorCode TASK_IS_PENDING = new ErrorCode(1_009_005_003, "当前任务处于挂起状态，不能操作");
    ErrorCode TASK_TARGET_NODE_NOT_EXISTS = new ErrorCode(1_009_005_004, " 目标节点不存在");
    ErrorCode TASK_RETURN_FAIL_SOURCE_TARGET_ERROR = new ErrorCode(1_009_005_006, "回退任务失败，目标节点是在并行网关上或非同一路线上，不可跳转");
    ErrorCode TASK_DELEGATE_FAIL_USER_REPEAT = new ErrorCode(1_009_005_007, "任务委派失败，委派人和当前审批人为同一人");
    ErrorCode TASK_DELEGATE_FAIL_USER_NOT_EXISTS = new ErrorCode(1_009_005_008, "任务委派失败，被委派人不存在");
    ErrorCode TASK_SIGN_CREATE_USER_NOT_EXIST = new ErrorCode(1_009_005_009, "任务加签：选择的用户不存在");
    ErrorCode TASK_SIGN_CREATE_TYPE_ERROR = new ErrorCode(1_009_005_010, "任务加签：当前任务已经{}，不能{}");
    ErrorCode TASK_SIGN_CREATE_USER_REPEAT = new ErrorCode(1_009_005_011, "任务加签失败，加签人与现有审批人[{}]重复");
    ErrorCode TASK_SIGN_DELETE_NO_PARENT = new ErrorCode(1_009_005_012, "任务减签失败，被减签的任务必须是通过加签生成的任务");
    ErrorCode TASK_TRANSFER_FAIL_USER_REPEAT = new ErrorCode(1_009_005_013, "任务转办失败，转办人和当前审批人为同一人");
    ErrorCode TASK_TRANSFER_FAIL_USER_NOT_EXISTS = new ErrorCode(1_009_005_014, "任务转办失败，转办人不存在");
    ErrorCode TASK_CREATE_FAIL_NO_CANDIDATE_USER = new ErrorCode(1_009_006_003, "操作失败，原因：找不到任务的审批人！");

    // ========== 动态表单模块 1-009-010-000 ==========
    ErrorCode FORM_NOT_EXISTS = new ErrorCode(1_009_010_000, "动态表单不存在");
    ErrorCode FORM_FIELD_REPEAT = new ErrorCode(1_009_010_001, "表单项({}) 和 ({}) 使用了相同的字段名({})");

    // ========== 用户组模块 1-009-011-000 ==========
    ErrorCode USER_GROUP_NOT_EXISTS = new ErrorCode(1_009_011_000, "用户分组不存在");
    ErrorCode USER_GROUP_IS_DISABLE = new ErrorCode(1_009_011_001, "名字为【{}】的用户分组已被禁用");

    // ========== 用户组模块 1-009-012-000 ==========
    ErrorCode CATEGORY_NOT_EXISTS = new ErrorCode(1_009_012_000, "流程分类不存在");
    ErrorCode CATEGORY_NAME_DUPLICATE = new ErrorCode(1_009_012_001, "流程分类名字【{}】重复");
    ErrorCode CATEGORY_CODE_DUPLICATE = new ErrorCode(1_009_012_002, "流程分类编码【{}】重复");

    // ========== BPM 流程监听器 1-009-013-000 ==========
    ErrorCode PROCESS_LISTENER_NOT_EXISTS = new ErrorCode(1_009_013_000, "流程监听器不存在");
    ErrorCode PROCESS_LISTENER_CLASS_NOT_FOUND = new ErrorCode(1_009_013_001, "流程监听器类({})不存在");
    ErrorCode PROCESS_LISTENER_CLASS_IMPLEMENTS_ERROR = new ErrorCode(1_009_013_002, "流程监听器类({})没有实现接口({})");
    ErrorCode PROCESS_LISTENER_EXPRESSION_INVALID = new ErrorCode(1_009_013_003, "流程监听器表达式({})不合法");

    // ========== BPM 流程表达式 1-009-014-000 ==========
    ErrorCode PROCESS_EXPRESSION_NOT_EXISTS = new ErrorCode(1_009_014_000, "流程表达式不存在");

}
