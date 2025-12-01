// 候选人策略枚举 （ 用于审批节点。抄送节点 )
export enum BpmCandidateStrategyEnum {
  /**
   * 审批人自选
   */
  APPROVE_USER_SELECT = 34,
  /**
   * 部门的负责人
   */
  DEPT_LEADER = 21,
  /**
   * 部门成员
   */
  DEPT_MEMBER = 20,
  /**
   * 流程表达式
   */
  EXPRESSION = 60,
  /**
   * 表单内部门负责人
   */
  FORM_DEPT_LEADER = 51,
  /**
   * 表单内用户字段
   */
  FORM_USER = 50,
  /**
   * 连续多级部门的负责人
   */
  MULTI_LEVEL_DEPT_LEADER = 23,
  /**
   * 指定岗位
   */
  POST = 22,
  /**
   * 指定角色
   */
  ROLE = 10,
  /**
   * 发起人自己
   */
  START_USER = 36,
  /**
   * 发起人部门负责人
   */
  START_USER_DEPT_LEADER = 37,
  /**
   * 发起人连续多级部门的负责人
   */
  START_USER_MULTI_LEVEL_DEPT_LEADER = 38,
  /**
   * 发起人自选
   */
  START_USER_SELECT = 35,
  /**
   * 指定用户
   */
  USER = 30,
  /**
   * 指定用户组
   */
  USER_GROUP = 40,
}

/**
 * 节点类型
 */
export enum BpmNodeTypeEnum {
  /**
   * 子流程节点
   */
  CHILD_PROCESS_NODE = 20,
  /**
   * 条件分支节点 (对应排他网关)
   */
  CONDITION_BRANCH_NODE = 51,
  /**
   * 条件节点
   */
  CONDITION_NODE = 50,

  /**
   * 抄送人节点
   */
  COPY_TASK_NODE = 12,

  /**
   * 延迟器节点
   */
  DELAY_TIMER_NODE = 14,

  /**
   * 结束节点
   */
  END_EVENT_NODE = 1,

  /**
   * 包容分支节点 (对应包容网关)
   */
  INCLUSIVE_BRANCH_NODE = 53,

  /**
   * 并行分支节点 (对应并行网关)
   */
  PARALLEL_BRANCH_NODE = 52,

  /**
   * 路由分支节点
   */
  ROUTER_BRANCH_NODE = 54,
  /**
   * 发起人节点
   */
  START_USER_NODE = 10,
  /**
   * 办理人节点
   */
  TRANSACTOR_NODE = 13,

  /**
   * 触发器节点
   */
  TRIGGER_NODE = 15,
  /**
   * 审批人节点
   */
  USER_TASK_NODE = 11,
}

/**
 *  流程任务操作按钮
 */
export enum BpmTaskOperationButtonTypeEnum {
  /**
   * 加签
   */
  ADD_SIGN = 5,
  /**
   * 通过
   */
  APPROVE = 1,
  /**
   * 抄送
   */
  COPY = 7,
  /**
   * 委派
   */
  DELEGATE = 4,
  /**
   * 拒绝
   */
  REJECT = 2,
  /**
   * 退回
   */
  RETURN = 6,
  /**
   * 转办
   */
  TRANSFER = 3,
}

/**
 * 任务状态枚举
 */
export enum BpmTaskStatusEnum {
  /**
   * 审批通过
   */
  APPROVE = 2,
  /**
   * 审批通过中
   */
  APPROVING = 7,

  /**
   * 已取消
   */
  CANCEL = 4,
  /**
   * 未开始
   */
  NOT_START = -1,
  /**
   * 审批不通过
   */
  REJECT = 3,

  /**
   * 已退回
   */
  RETURN = 5,

  /**
   * 审批中
   */
  RUNNING = 1,
  /**
   * 跳过
   */
  SKIP = -2,
  /**
   * 待审批
   */
  WAIT = 0,
}

/**
 * 节点 Id 枚举
 */
export enum BpmNodeIdEnum {
  /**
   * 发起人节点 Id
   */
  END_EVENT_NODE_ID = 'EndEvent',

  /**
   * 发起人节点 Id
   */
  START_USER_NODE_ID = 'StartUserNode',
}

/**
 * 表单权限的枚举
 */
export enum BpmFieldPermissionType {
  /**
   * 隐藏
   */
  NONE = '3',
  /**
   * 只读
   */
  READ = '1',
  /**
   * 编辑
   */
  WRITE = '2',
}

/**
 * 流程模型类型
 */
export const BpmModelType = {
  BPMN: 10, // BPMN 设计器
  SIMPLE: 20, // 简易设计器
};

/**
 * 流程模型表单类型
 */
export const BpmModelFormType = {
  NORMAL: 10, // 流程表单
  CUSTOM: 20, // 业务表单
};

/**
 * 流程实例状态
 */
export const BpmProcessInstanceStatus = {
  NOT_START: -1, // 未开始
  RUNNING: 1, // 审批中
  APPROVE: 2, // 审批通过
  REJECT: 3, // 审批不通过
  CANCEL: 4, // 已取消
};

/**
 * 自动审批类型
 */
export const BpmAutoApproveType = {
  NONE: 0, // 不自动通过
  APPROVE_ALL: 1, // 仅审批一次，后续重复的审批节点均自动通过
  APPROVE_SEQUENT: 2, // 仅针对连续审批的节点自动通过
};

/**
 * 审批操作按钮名称
 */
export const OPERATION_BUTTON_NAME = new Map<number, string>();
OPERATION_BUTTON_NAME.set(BpmTaskOperationButtonTypeEnum.APPROVE, '通过');
OPERATION_BUTTON_NAME.set(BpmTaskOperationButtonTypeEnum.REJECT, '拒绝');
OPERATION_BUTTON_NAME.set(BpmTaskOperationButtonTypeEnum.TRANSFER, '转办');
OPERATION_BUTTON_NAME.set(BpmTaskOperationButtonTypeEnum.DELEGATE, '委派');
OPERATION_BUTTON_NAME.set(BpmTaskOperationButtonTypeEnum.ADD_SIGN, '加签');
OPERATION_BUTTON_NAME.set(BpmTaskOperationButtonTypeEnum.RETURN, '退回');
OPERATION_BUTTON_NAME.set(BpmTaskOperationButtonTypeEnum.COPY, '抄送');

/**
 * 流程实例的变量枚举
 */
export enum ProcessVariableEnum {
  /**
   * 流程定义名称
   */
  PROCESS_DEFINITION_NAME = 'PROCESS_DEFINITION_NAME',
  /**
   * 发起时间
   */
  START_TIME = 'PROCESS_START_TIME',
  /**
   * 发起用户 ID
   */
  START_USER_ID = 'PROCESS_START_USER_ID',
}
