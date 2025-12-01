import { BpmNodeTypeEnum, BpmTaskStatusEnum } from '@vben/constants';

interface DictDataType {
  label: string;
  value: number | string;
}

// 用户任务的审批类型。 【参考飞书】
export enum ApproveType {
  /**
   * 自动通过
   */
  AUTO_APPROVE = 2,
  /**
   * 自动拒绝
   */
  AUTO_REJECT = 3,
  /**
   * 人工审批
   */
  USER = 1,
}

// 多人审批方式类型枚举 （ 用于审批节点 ）
export enum ApproveMethodType {
  /**
   * 多人或签(通过只需一人，拒绝只需一人)
   */
  ANY_APPROVE = 3,

  /**
   * 多人会签(按通过比例)
   */
  APPROVE_BY_RATIO = 2,

  /**
   * 随机挑选一人审批
   */
  RANDOM_SELECT_ONE_APPROVE = 1,
  /**
   * 多人依次审批
   */
  SEQUENTIAL_APPROVE = 4,
}

export enum NodeId {
  /**
   * 发起人节点 Id
   */
  END_EVENT_NODE_ID = 'EndEvent',

  /**
   * 发起人节点 Id
   */
  START_USER_NODE_ID = 'StartUserNode',
}

// 条件配置类型 （ 用于条件节点配置 ）
export enum ConditionType {
  /**
   * 条件表达式
   */
  EXPRESSION = 1,

  /**
   * 条件规则
   */
  RULE = 2,
}

// 操作按钮类型枚举 (用于审批节点)
export enum OperationButtonType {
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

// 审批拒绝类型枚举
export enum RejectHandlerType {
  /**
   * 结束流程
   */
  FINISH_PROCESS = 1,
  /**
   * 驳回到指定节点
   */
  RETURN_USER_TASK = 2,
}

// 用户任务超时处理类型枚举
export enum TimeoutHandlerType {
  /**
   * 自动同意
   */
  APPROVE = 2,
  /**
   * 自动拒绝
   */
  REJECT = 3,
  /**
   * 自动提醒
   */
  REMINDER = 1,
}

// 用户任务的审批人为空时，处理类型枚举
export enum AssignEmptyHandlerType {
  /**
   * 自动通过
   */
  APPROVE = 1,
  /**
   * 转交给流程管理员
   */
  ASSIGN_ADMIN = 4,
  /**
   * 指定人员审批
   */
  ASSIGN_USER = 3,
  /**
   * 自动拒绝
   */
  REJECT = 2,
}

// 用户任务的审批人与发起人相同时，处理类型枚举
export enum AssignStartUserHandlerType {
  /**
   * 转交给部门负责人审批
   */
  ASSIGN_DEPT_LEADER = 3,
  /**
   * 自动跳过【参考飞书】：1）如果当前节点还有其他审批人，则交由其他审批人进行审批；2）如果当前节点没有其他审批人，则该节点自动通过
   */
  SKIP = 2,
  /**
   * 由发起人对自己审批
   */
  START_USER_AUDIT = 1,
}

// 时间单位枚举
export enum TimeUnitType {
  /**
   * 天
   */
  DAY = 3,
  /**
   * 小时
   */
  HOUR = 2,
  /**
   * 分钟
   */
  MINUTE = 1,
}

/**
 * 表单权限的枚举
 */
export enum FieldPermissionType {
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
 * 延迟类型
 */
export enum DelayTypeEnum {
  /**
   * 固定日期时间
   */
  FIXED_DATE_TIME = 2,
  /**
   * 固定时长
   */
  FIXED_TIME_DURATION = 1,
}

/**
 * 触发器类型枚举
 */
export enum TriggerTypeEnum {
  /**
   * 表单数据删除触发器
   */
  FORM_DELETE = 11,
  /**
   * 表单数据更新触发器
   */
  FORM_UPDATE = 10,
  /**
   * 接收 HTTP 回调请求触发器
   */
  HTTP_CALLBACK = 2,
  /**
   * 发送 HTTP 请求触发器
   */
  HTTP_REQUEST = 1,
}

export enum ChildProcessStartUserTypeEnum {
  /**
   * 表单
   */
  FROM_FORM = 2,
  /**
   * 同主流程发起人
   */
  MAIN_PROCESS_START_USER = 1,
}

export enum ChildProcessStartUserEmptyTypeEnum {
  /**
   * 子流程管理员
   */
  CHILD_PROCESS_ADMIN = 2,
  /**
   * 主流程管理员
   */
  MAIN_PROCESS_ADMIN = 3,
  /**
   * 同主流程发起人
   */
  MAIN_PROCESS_START_USER = 1,
}

export enum ChildProcessMultiInstanceSourceTypeEnum {
  /**
   * 固定数量
   */
  FIXED_QUANTITY = 1,
  /**
   * 多选表单
   */
  MULTIPLE_FORM = 3,
  /**
   * 数字表单
   */
  NUMBER_FORM = 2,
}

// 候选人策略枚举 （ 用于审批节点。抄送节点 )
export enum CandidateStrategy {
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

export enum BpmHttpRequestParamTypeEnum {
  /**
   * 固定值
   */
  FIXED_VALUE = 1,
  /**
   * 表单
   */
  FROM_FORM = 2,
}

// 这里定义 HTTP 请求参数类型
export type HttpRequestParam = {
  key: string;
  type: number;
  value: string;
};

// 监听器结构定义
export type ListenerHandler = {
  body?: HttpRequestParam[];
  enable: boolean;
  header?: HttpRequestParam[];
  path?: string;
};

/**
 * 条件规则结构定义
 */
export type ConditionRule = {
  leftSide: string | undefined;
  opCode: string;
  rightSide: string | undefined;
};

/**
 * 条件结构定义
 */
export type Condition = {
  // 条件规则的逻辑关系是否为且
  and: boolean;
  rules: ConditionRule[];
};

/**
 * 条件组结构定义
 */
export type ConditionGroup = {
  // 条件组的逻辑关系是否为且
  and: boolean;
  // 条件数组
  conditions: Condition[];
};

/**
 * 条件节点设置结构定义，用于条件节点
 */
export type ConditionSetting = {
  // 条件表达式
  conditionExpression?: string;
  // 条件组
  conditionGroups?: ConditionGroup;
  // 条件类型
  conditionType?: ConditionType;
  // 是否默认的条件
  defaultFlow?: boolean;
};

/**
 * 审批拒绝结构定义
 */
export type RejectHandler = {
  // 退回节点 Id
  returnNodeId?: string;
  // 审批拒绝类型
  type: RejectHandlerType;
};

/**
 * 审批超时结构定义
 */
export type TimeoutHandler = {
  // 是否开启超时处理
  enable: boolean;
  // 执行动作是自动提醒, 最大提醒次数
  maxRemindCount?: number;
  // 超时时间设置
  timeDuration?: string;
  // 超时执行的动作
  type?: number;
};

/**
 * 审批人为空的结构定义
 */
export type AssignEmptyHandler = {
  // 审批人为空的处理类型
  type: AssignEmptyHandlerType;
  // 指定用户的编号数组
  userIds?: number[];
};

/**
 * 延迟设置
 */
export type DelaySetting = {
  // 延迟时间表达式
  delayTime: string;
  // 延迟类型
  delayType: number;
};

/**
 * 路由分支结构定义
 */
export type RouterSetting = {
  conditionExpression: string;
  conditionGroups: ConditionGroup;
  conditionType: ConditionType;
  nodeId: string | undefined;
};

/**
 * 操作按钮权限结构定义
 */
export type ButtonSetting = {
  displayName: string;
  enable: boolean;
  id: OperationButtonType;
};

/**
 * HTTP 请求触发器结构定义
 */
export type HttpRequestTriggerSetting = {
  // 请求体参数设置
  body?: HttpRequestParam[];
  // 请求头参数设置
  header?: HttpRequestParam[];
  // 请求响应设置
  response?: Record<string, string>[];
  // 请求 URL
  url: string;
};

/**
 * 流程表单触发器配置结构定义
 */
export type FormTriggerSetting = {
  // 条件表达式
  conditionExpression?: string;
  // 条件组
  conditionGroups?: ConditionGroup;
  // 条件类型
  conditionType?: ConditionType;
  // 删除表单字段配置
  deleteFields?: string[];
  // 更新表单字段配置
  updateFormFields?: Record<string, any>;
};

/**
 * 触发器节点结构定义
 */
export type TriggerSetting = {
  formSettings?: FormTriggerSetting[];
  httpRequestSetting?: HttpRequestTriggerSetting;
  type: TriggerTypeEnum;
};

export type IOParameter = {
  source: string;
  target: string;
};

export type StartUserSetting = {
  emptyType?: ChildProcessStartUserEmptyTypeEnum;
  formField?: string;
  type: ChildProcessStartUserTypeEnum;
};

export type TimeoutSetting = {
  enable: boolean;
  timeExpression?: string;
  type?: DelayTypeEnum;
};

export type MultiInstanceSetting = {
  approveRatio?: number;
  enable: boolean;
  sequential?: boolean;
  source?: string;
  sourceType?: ChildProcessMultiInstanceSourceTypeEnum;
};

/**
 * 子流程节点结构定义
 */
export type ChildProcessSetting = {
  async: boolean;
  calledProcessDefinitionKey: string;
  calledProcessDefinitionName: string;
  inVariables?: IOParameter[];
  multiInstanceSetting: MultiInstanceSetting;
  outVariables?: IOParameter[];
  skipStartUserNode: boolean;
  startUserSetting: StartUserSetting;
  timeoutSetting: TimeoutSetting;
};

/**
 *  节点结构定义
 */
export interface SimpleFlowNode {
  id: string;
  type: BpmNodeTypeEnum;
  name: string;
  showText?: string;
  // 孩子节点
  childNode?: SimpleFlowNode;
  // 条件节点
  conditionNodes?: SimpleFlowNode[];
  // 审批类型
  approveType?: ApproveType;
  // 候选人策略
  candidateStrategy?: number;
  // 候选人参数
  candidateParam?: string;
  // 多人审批方式
  approveMethod?: ApproveMethodType;
  // 通过比例
  approveRatio?: number;
  // 审批按钮设置
  buttonsSetting?: any[];
  // 表单权限
  fieldsPermission?: Array<Record<string, any>>;
  // 审批任务超时处理
  timeoutHandler?: TimeoutHandler;
  // 审批任务拒绝处理
  rejectHandler?: RejectHandler;
  // 审批人为空的处理
  assignEmptyHandler?: AssignEmptyHandler;
  // 审批节点的审批人与发起人相同时，对应的处理类型
  assignStartUserHandlerType?: number;
  // 创建任务监听器
  taskCreateListener?: ListenerHandler;
  // 创建任务监听器
  taskAssignListener?: ListenerHandler;
  // 创建任务监听器
  taskCompleteListener?: ListenerHandler;
  // 条件设置
  conditionSetting?: ConditionSetting;
  // 活动的状态，用于前端节点状态展示
  activityStatus?: BpmTaskStatusEnum;
  // 延迟设置
  delaySetting?: DelaySetting;
  // 路由分支
  routerGroups?: RouterSetting[];
  defaultFlowId?: string;
  // 签名
  signEnable?: boolean;
  // 审批意见
  reasonRequire?: boolean;
  // 跳过表达式
  skipExpression?: string;
  // 触发器设置
  triggerSetting?: TriggerSetting;
  // 子流程
  childProcessSetting?: ChildProcessSetting;
}

/**
 * 条件组默认值
 */
export const DEFAULT_CONDITION_GROUP_VALUE = {
  and: true,
  conditions: [
    {
      and: true,
      rules: [
        {
          opCode: '==',
          leftSide: undefined,
          rightSide: '',
        },
      ],
    },
  ],
};

export const NODE_DEFAULT_TEXT = new Map<number, string>();
NODE_DEFAULT_TEXT.set(BpmNodeTypeEnum.USER_TASK_NODE, '请配置审批人');
NODE_DEFAULT_TEXT.set(BpmNodeTypeEnum.COPY_TASK_NODE, '请配置抄送人');
NODE_DEFAULT_TEXT.set(BpmNodeTypeEnum.CONDITION_NODE, '请设置条件');
NODE_DEFAULT_TEXT.set(BpmNodeTypeEnum.START_USER_NODE, '请设置发起人');
NODE_DEFAULT_TEXT.set(BpmNodeTypeEnum.DELAY_TIMER_NODE, '请设置延迟器');
NODE_DEFAULT_TEXT.set(BpmNodeTypeEnum.ROUTER_BRANCH_NODE, '请设置路由节点');
NODE_DEFAULT_TEXT.set(BpmNodeTypeEnum.TRIGGER_NODE, '请设置触发器');
NODE_DEFAULT_TEXT.set(BpmNodeTypeEnum.TRANSACTOR_NODE, '请设置办理人');
NODE_DEFAULT_TEXT.set(BpmNodeTypeEnum.CHILD_PROCESS_NODE, '请设置子流程');

export const NODE_DEFAULT_NAME = new Map<number, string>();
NODE_DEFAULT_NAME.set(BpmNodeTypeEnum.USER_TASK_NODE, '审批人');
NODE_DEFAULT_NAME.set(BpmNodeTypeEnum.COPY_TASK_NODE, '抄送人');
NODE_DEFAULT_NAME.set(BpmNodeTypeEnum.CONDITION_NODE, '条件');
NODE_DEFAULT_NAME.set(BpmNodeTypeEnum.START_USER_NODE, '发起人');
NODE_DEFAULT_NAME.set(BpmNodeTypeEnum.DELAY_TIMER_NODE, '延迟器');
NODE_DEFAULT_NAME.set(BpmNodeTypeEnum.ROUTER_BRANCH_NODE, '路由分支');
NODE_DEFAULT_NAME.set(BpmNodeTypeEnum.TRIGGER_NODE, '触发器');
NODE_DEFAULT_NAME.set(BpmNodeTypeEnum.TRANSACTOR_NODE, '办理人');
NODE_DEFAULT_NAME.set(BpmNodeTypeEnum.CHILD_PROCESS_NODE, '子流程');

// 候选人策略。暂时不从字典中取。 后续可能调整。控制显示顺序
export const CANDIDATE_STRATEGY: DictDataType[] = [
  { label: '指定成员', value: CandidateStrategy.USER as any },
  { label: '指定角色', value: CandidateStrategy.ROLE as any },
  { label: '指定岗位', value: CandidateStrategy.POST as any },
  { label: '部门成员', value: CandidateStrategy.DEPT_MEMBER as any },
  { label: '部门负责人', value: CandidateStrategy.DEPT_LEADER as any },
  {
    label: '连续多级部门负责人',
    value: CandidateStrategy.MULTI_LEVEL_DEPT_LEADER as any,
  },
  { label: '发起人自选', value: CandidateStrategy.START_USER_SELECT as any },
  { label: '审批人自选', value: CandidateStrategy.APPROVE_USER_SELECT as any },
  { label: '发起人本人', value: CandidateStrategy.START_USER as any },
  {
    label: '发起人部门负责人',
    value: CandidateStrategy.START_USER_DEPT_LEADER as any,
  },
  {
    label: '发起人连续部门负责人',
    value: CandidateStrategy.START_USER_MULTI_LEVEL_DEPT_LEADER as any,
  },
  { label: '用户组', value: CandidateStrategy.USER_GROUP as any },
  { label: '表单内用户字段', value: CandidateStrategy.FORM_USER as any },
  {
    label: '表单内部门负责人',
    value: CandidateStrategy.FORM_DEPT_LEADER as any,
  },
  { label: '流程表达式', value: CandidateStrategy.EXPRESSION as any },
];
// 审批节点 的审批类型
export const APPROVE_TYPE: DictDataType[] = [
  { label: '人工审批', value: ApproveType.USER as any },
  { label: '自动通过', value: ApproveType.AUTO_APPROVE as any },
  { label: '自动拒绝', value: ApproveType.AUTO_REJECT as any },
];

export const APPROVE_METHODS: DictDataType[] = [
  {
    label: '按顺序依次审批',
    value: ApproveMethodType.SEQUENTIAL_APPROVE as any,
  },
  {
    label: '会签（可同时审批，至少 % 人必须审批通过）',
    value: ApproveMethodType.APPROVE_BY_RATIO as any,
  },
  {
    label: '或签(可同时审批，有一人通过即可)',
    value: ApproveMethodType.ANY_APPROVE as any,
  },
  {
    label: '随机挑选一人审批',
    value: ApproveMethodType.RANDOM_SELECT_ONE_APPROVE as any,
  },
];

export const CONDITION_CONFIG_TYPES: DictDataType[] = [
  { label: '条件规则', value: ConditionType.RULE as any },
  { label: '条件表达式', value: ConditionType.EXPRESSION as any },
];

// 时间单位类型
export const TIME_UNIT_TYPES: DictDataType[] = [
  { label: '分钟', value: TimeUnitType.MINUTE as any },
  { label: '小时', value: TimeUnitType.HOUR as any },
  { label: '天', value: TimeUnitType.DAY as any },
];
// 超时处理执行动作类型
export const TIMEOUT_HANDLER_TYPES: DictDataType[] = [
  { label: '自动提醒', value: 1 },
  { label: '自动同意', value: 2 },
  { label: '自动拒绝', value: 3 },
];
export const REJECT_HANDLER_TYPES: DictDataType[] = [
  { label: '终止流程', value: RejectHandlerType.FINISH_PROCESS as any },
  { label: '驳回到指定节点', value: RejectHandlerType.RETURN_USER_TASK as any },
  // { label: '结束任务', value: RejectHandlerType.FINISH_TASK }
];
export const ASSIGN_EMPTY_HANDLER_TYPES: DictDataType[] = [
  { label: '自动通过', value: 1 },
  { label: '自动拒绝', value: 2 },
  { label: '指定成员审批', value: 3 },
  { label: '转交给流程管理员', value: 4 },
];
export const ASSIGN_START_USER_HANDLER_TYPES: DictDataType[] = [
  { label: '由发起人对自己审批', value: 1 },
  { label: '自动跳过', value: 2 },
  { label: '转交给部门负责人审批', value: 3 },
];

// 比较运算符
export const COMPARISON_OPERATORS: DictDataType[] = [
  {
    value: '==',
    label: '等于',
  },
  {
    value: '!=',
    label: '不等于',
  },
  {
    value: '>',
    label: '大于',
  },
  {
    value: '>=',
    label: '大于等于',
  },
  {
    value: '<',
    label: '小于',
  },
  {
    value: '<=',
    label: '小于等于',
  },
];
// 审批操作按钮名称
export const OPERATION_BUTTON_NAME = new Map<number, string>();
OPERATION_BUTTON_NAME.set(OperationButtonType.APPROVE, '通过');
OPERATION_BUTTON_NAME.set(OperationButtonType.REJECT, '拒绝');
OPERATION_BUTTON_NAME.set(OperationButtonType.TRANSFER, '转办');
OPERATION_BUTTON_NAME.set(OperationButtonType.DELEGATE, '委派');
OPERATION_BUTTON_NAME.set(OperationButtonType.ADD_SIGN, '加签');
OPERATION_BUTTON_NAME.set(OperationButtonType.RETURN, '退回');
OPERATION_BUTTON_NAME.set(OperationButtonType.COPY, '抄送');

// 默认的按钮权限设置
export const DEFAULT_BUTTON_SETTING: ButtonSetting[] = [
  { id: OperationButtonType.APPROVE, displayName: '通过', enable: true },
  { id: OperationButtonType.REJECT, displayName: '拒绝', enable: true },
  { id: OperationButtonType.TRANSFER, displayName: '转办', enable: true },
  { id: OperationButtonType.DELEGATE, displayName: '委派', enable: true },
  { id: OperationButtonType.ADD_SIGN, displayName: '加签', enable: true },
  { id: OperationButtonType.RETURN, displayName: '退回', enable: true },
];

// 办理人默认的按钮权限设置
export const TRANSACTOR_DEFAULT_BUTTON_SETTING: ButtonSetting[] = [
  { id: OperationButtonType.APPROVE, displayName: '办理', enable: true },
  { id: OperationButtonType.REJECT, displayName: '拒绝', enable: false },
  { id: OperationButtonType.TRANSFER, displayName: '转办', enable: false },
  { id: OperationButtonType.DELEGATE, displayName: '委派', enable: false },
  { id: OperationButtonType.ADD_SIGN, displayName: '加签', enable: false },
  { id: OperationButtonType.RETURN, displayName: '退回', enable: false },
];

// 发起人的按钮权限。暂时定死，不可以编辑
export const START_USER_BUTTON_SETTING: ButtonSetting[] = [
  { id: OperationButtonType.APPROVE, displayName: '提交', enable: true },
  { id: OperationButtonType.REJECT, displayName: '拒绝', enable: false },
  { id: OperationButtonType.TRANSFER, displayName: '转办', enable: false },
  { id: OperationButtonType.DELEGATE, displayName: '委派', enable: false },
  { id: OperationButtonType.ADD_SIGN, displayName: '加签', enable: false },
  { id: OperationButtonType.RETURN, displayName: '退回', enable: false },
];

export const MULTI_LEVEL_DEPT: DictDataType[] = [
  { label: '第 1 级部门', value: 1 },
  { label: '第 2 级部门', value: 2 },
  { label: '第 3 级部门', value: 3 },
  { label: '第 4 级部门', value: 4 },
  { label: '第 5 级部门', value: 5 },
  { label: '第 6 级部门', value: 6 },
  { label: '第 7 级部门', value: 7 },
  { label: '第 8 级部门', value: 8 },
  { label: '第 9 级部门', value: 9 },
  { label: '第 10 级部门', value: 10 },
  { label: '第 11 级部门', value: 11 },
  { label: '第 12 级部门', value: 12 },
  { label: '第 13 级部门', value: 13 },
  { label: '第 14 级部门', value: 14 },
  { label: '第 15 级部门', value: 15 },
];

export const DELAY_TYPE = [
  { label: '固定时长', value: DelayTypeEnum.FIXED_TIME_DURATION },
  { label: '固定日期', value: DelayTypeEnum.FIXED_DATE_TIME },
];

export const BPM_HTTP_REQUEST_PARAM_TYPES = [
  {
    value: 1,
    label: '固定值',
  },
  {
    value: 2,
    label: '表单',
  },
];

export const TRIGGER_TYPES: DictDataType[] = [
  { label: '发送 HTTP 请求', value: TriggerTypeEnum.HTTP_REQUEST as any },
  { label: '接收 HTTP 回调', value: TriggerTypeEnum.HTTP_CALLBACK as any },
  { label: '修改表单数据', value: TriggerTypeEnum.FORM_UPDATE as any },
  { label: '删除表单数据', value: TriggerTypeEnum.FORM_DELETE as any },
];

export const CHILD_PROCESS_START_USER_TYPE = [
  {
    label: '同主流程发起人',
    value: ChildProcessStartUserTypeEnum.MAIN_PROCESS_START_USER,
  },
  { label: '从表单中获取', value: ChildProcessStartUserTypeEnum.FROM_FORM },
];

export const CHILD_PROCESS_START_USER_EMPTY_TYPE = [
  {
    label: '同主流程发起人',
    value: ChildProcessStartUserEmptyTypeEnum.MAIN_PROCESS_START_USER,
  },
  {
    label: '子流程管理员',
    value: ChildProcessStartUserEmptyTypeEnum.CHILD_PROCESS_ADMIN,
  },
  {
    label: '主流程管理员',
    value: ChildProcessStartUserEmptyTypeEnum.MAIN_PROCESS_ADMIN,
  },
];

export const CHILD_PROCESS_MULTI_INSTANCE_SOURCE_TYPE = [
  {
    label: '固定数量',
    value: ChildProcessMultiInstanceSourceTypeEnum.FIXED_QUANTITY,
  },
  {
    label: '数字表单',
    value: ChildProcessMultiInstanceSourceTypeEnum.NUMBER_FORM,
  },
  {
    label: '多选表单',
    value: ChildProcessMultiInstanceSourceTypeEnum.MULTIPLE_FORM,
  },
];
