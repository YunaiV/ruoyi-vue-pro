import type { Ref } from 'vue';

import type {
  ConditionGroup,
  HttpRequestParam,
  SimpleFlowNode,
} from './consts';

import type { BpmUserGroupApi } from '#/api/bpm/userGroup';
import type { SystemDeptApi } from '#/api/system/dept';
import type { SystemPostApi } from '#/api/system/post';
import type { SystemRoleApi } from '#/api/system/role';
import type { SystemUserApi } from '#/api/system/user';

import { inject, nextTick, ref, toRaw, unref, watch } from 'vue';

import {
  BpmNodeTypeEnum,
  BpmTaskStatusEnum,
  ProcessVariableEnum,
} from '@vben/constants';

import {
  ApproveMethodType,
  AssignEmptyHandlerType,
  AssignStartUserHandlerType,
  CandidateStrategy,
  COMPARISON_OPERATORS,
  ConditionType,
  FieldPermissionType,
  NODE_DEFAULT_NAME,
  RejectHandlerType,
} from './consts';

export function useWatchNode(props: {
  flowNode: SimpleFlowNode;
}): Ref<SimpleFlowNode> {
  const node = ref<SimpleFlowNode>(props.flowNode);
  watch(
    () => props.flowNode,
    (newValue) => {
      node.value = newValue;
    },
  );
  return node;
}

// 解析 formCreate 所有表单字段, 并返回
function parseFormCreateFields(formFields?: string[]) {
  const result: Array<Record<string, any>> = [];
  if (formFields) {
    formFields.forEach((fieldStr: string) => {
      parseFormFields(JSON.parse(fieldStr), result);
    });
  }
  return result;
}

/**
 * 解析表单组件的  field, title 等字段（递归，如果组件包含子组件）
 *
 * @param rule  组件的生成规则 https://www.form-create.com/v3/guide/rule
 * @param fields 解析后表单组件字段
 * @param parentTitle  如果是子表单，子表单的标题，默认为空
 */
export const parseFormFields = (
  rule: Record<string, any>,
  fields: Array<Record<string, any>> = [],
  parentTitle: string = '',
) => {
  const { type, field, $required, title: tempTitle, children } = rule;
  if (field && tempTitle) {
    let title = tempTitle;
    if (parentTitle) {
      title = `${parentTitle}.${tempTitle}`;
    }
    let required = false;
    if ($required) {
      required = true;
    }
    fields.push({
      field,
      title,
      type,
      required,
    });
    // TODO 子表单 需要处理子表单字段
    // if (type === 'group' && rule.props?.rule && Array.isArray(rule.props.rule)) {
    //   // 解析子表单的字段
    //   rule.props.rule.forEach((item) => {
    //     parseFields(item, fieldsPermission, title)
    //   })
    // }
  }
  if (children && Array.isArray(children)) {
    children.forEach((rule) => {
      parseFormFields(rule, fields);
    });
  }
};

/**
 * @description 表单数据权限配置，用于发起人节点 、审批节点、抄送节点
 */
export function useFormFieldsPermission(
  defaultPermission: FieldPermissionType,
) {
  // 字段权限配置. 需要有 field, title,  permissioin 属性
  const fieldsPermissionConfig = ref<Array<Record<string, any>>>([]);

  const formType = inject<Ref<number | undefined>>('formType', ref()); // 表单类型

  const formFields = inject<Ref<string[]>>('formFields', ref([])); // 流程表单字段

  function getNodeConfigFormFields(
    nodeFormFields?: Array<Record<string, string>>,
  ) {
    nodeFormFields = toRaw(nodeFormFields);
    fieldsPermissionConfig.value =
      !nodeFormFields || nodeFormFields.length === 0
        ? getDefaultFieldsPermission(unref(formFields))
        : mergeFieldsPermission(nodeFormFields, unref(formFields));
  }
  // 合并已经设置的表单字段权限，当前流程表单字段 (可能新增，或删除了字段)
  function mergeFieldsPermission(
    formFieldsPermisson: Array<Record<string, string>>,
    formFields?: string[],
  ) {
    let mergedFieldsPermission: Array<Record<string, any>> = [];
    if (formFields) {
      mergedFieldsPermission = parseFormCreateFields(formFields).map((item) => {
        const found = formFieldsPermisson.find(
          (fieldPermission) => fieldPermission.field === item.field,
        );
        return {
          field: item.field,
          title: item.title,
          permission: found ? found.permission : defaultPermission,
        };
      });
    }
    return mergedFieldsPermission;
  }

  // 默认的表单权限： 获取表单的所有字段，设置字段默认权限为只读
  function getDefaultFieldsPermission(formFields?: string[]) {
    let defaultFieldsPermission: Array<Record<string, any>> = [];
    if (formFields) {
      defaultFieldsPermission = parseFormCreateFields(formFields).map(
        (item) => {
          return {
            field: item.field,
            title: item.title,
            permission: defaultPermission,
          };
        },
      );
    }
    return defaultFieldsPermission;
  }

  // 获取表单的所有字段，作为下拉框选项
  const formFieldOptions = parseFormCreateFields(unref(formFields));

  return {
    formType,
    fieldsPermissionConfig,
    formFieldOptions,
    getNodeConfigFormFields,
  };
}

/**
 * @description 获取流程表单的字段
 */
export function useFormFields() {
  const formFields = inject<Ref<string[]>>('formFields', ref([])); // 流程表单字段
  return parseFormCreateFields(unref(formFields));
}

// TODO @芋艿：后续需要把各种类似 useFormFieldsPermission 的逻辑，抽成一个通用方法。
/**
 * @description 获取流程表单的字段和发起人字段
 */
export function useFormFieldsAndStartUser() {
  const injectFormFields = inject<Ref<string[]>>('formFields', ref([])); // 流程表单字段
  const formFields = parseFormCreateFields(unref(injectFormFields));
  // 添加发起人
  formFields.unshift({
    field: ProcessVariableEnum.START_USER_ID,
    title: '发起人',
    required: true,
  });
  return formFields;
}

export type UserTaskFormType = {
  approveMethod: ApproveMethodType;
  approveRatio?: number;
  assignEmptyHandlerType?: AssignEmptyHandlerType;
  assignEmptyHandlerUserIds?: number[];
  assignStartUserHandlerType?: AssignStartUserHandlerType;
  buttonsSetting: any[];
  candidateStrategy: CandidateStrategy;
  deptIds?: number[]; // 部门
  deptLevel?: number; // 部门层级
  expression?: string; // 流程表达式
  formDept?: string; // 表单内部门字段
  formUser?: string; // 表单内用户字段
  maxRemindCount?: number;
  postIds?: number[]; // 岗位
  reasonRequire: boolean;
  rejectHandlerType?: RejectHandlerType;
  returnNodeId?: string;
  roleIds?: number[]; // 角色
  signEnable: boolean;
  skipExpression?: string; // 跳过表达式
  taskAssignListener?: {
    body: HttpRequestParam[];
    header: HttpRequestParam[];
  };
  taskAssignListenerEnable?: boolean;
  taskAssignListenerPath?: string;
  taskCompleteListener?: {
    body: HttpRequestParam[];
    header: HttpRequestParam[];
  };
  taskCompleteListenerEnable?: boolean;
  taskCompleteListenerPath?: string;
  taskCreateListener?: {
    body: HttpRequestParam[];
    header: HttpRequestParam[];
  };
  taskCreateListenerEnable?: boolean;
  taskCreateListenerPath?: string;
  timeDuration?: number;
  timeoutHandlerEnable?: boolean;
  timeoutHandlerType?: number;
  userGroups?: number[]; // 用户组
  userIds?: number[]; // 用户
};

export type CopyTaskFormType = {
  candidateStrategy: CandidateStrategy;
  deptIds?: number[]; // 部门
  deptLevel?: number; // 部门层级
  expression?: string; // 流程表达式
  formDept?: string; // 表单内部门字段
  formUser?: string; // 表单内用户字段
  postIds?: number[]; // 岗位
  roleIds?: number[]; // 角色
  userGroups?: number[]; // 用户组
  userIds?: number[]; // 用户
};

/**
 * @description 节点表单数据。 用于审批节点、抄送节点
 */
export function useNodeForm(nodeType: BpmNodeTypeEnum) {
  const roleOptions = inject<Ref<SystemRoleApi.Role[]>>('roleList', ref([])); // 角色列表
  const postOptions = inject<Ref<SystemPostApi.Post[]>>('postList', ref([])); // 岗位列表
  const userOptions = inject<Ref<SystemUserApi.User[]>>('userList', ref([])); // 用户列表
  const deptOptions = inject<Ref<SystemDeptApi.Dept[]>>('deptList', ref([])); // 部门列表
  const userGroupOptions = inject<Ref<BpmUserGroupApi.UserGroup[]>>(
    'userGroupList',
    ref([]),
  ); // 用户组列表
  const deptTreeOptions = inject<Ref<SystemDeptApi.Dept[]>>(
    'deptTree',
    ref([]),
  ); // 部门树
  const formFields = inject<Ref<string[]>>('formFields', ref([])); // 流程表单字段
  const configForm = ref<any | CopyTaskFormType | UserTaskFormType>();

  if (
    nodeType === BpmNodeTypeEnum.USER_TASK_NODE ||
    nodeType === BpmNodeTypeEnum.TRANSACTOR_NODE
  ) {
    configForm.value = {
      candidateStrategy: CandidateStrategy.USER,
      approveMethod: ApproveMethodType.SEQUENTIAL_APPROVE,
      approveRatio: 100,
      rejectHandlerType: RejectHandlerType.FINISH_PROCESS,
      assignStartUserHandlerType: AssignStartUserHandlerType.START_USER_AUDIT,
      returnNodeId: '',
      timeoutHandlerEnable: false,
      timeoutHandlerType: 1,
      timeDuration: 6, // 默认 6小时
      maxRemindCount: 1, // 默认 提醒 1次
      buttonsSetting: [],
    };
  }
  configForm.value = {
    candidateStrategy: CandidateStrategy.USER,
  };

  function getShowText(): string {
    let showText = '';
    // 指定成员
    if (
      configForm.value?.candidateStrategy === CandidateStrategy.USER &&
      configForm.value?.userIds?.length > 0
    ) {
      const candidateNames: string[] = [];
      userOptions?.value.forEach((item: any) => {
        if (configForm.value?.userIds?.includes(item.id)) {
          candidateNames.push(item.nickname);
        }
      });
      showText = `指定成员：${candidateNames.join(',')}`;
    }
    // 指定角色
    if (
      configForm.value?.candidateStrategy === CandidateStrategy.ROLE &&
      configForm.value.roleIds?.length > 0
    ) {
      const candidateNames: string[] = [];
      roleOptions?.value.forEach((item: any) => {
        if (configForm.value?.roleIds?.includes(item.id)) {
          candidateNames.push(item.name);
        }
      });
      showText = `指定角色：${candidateNames.join(',')}`;
    }
    // 指定部门
    if (
      (configForm.value?.candidateStrategy === CandidateStrategy.DEPT_MEMBER ||
        configForm.value?.candidateStrategy === CandidateStrategy.DEPT_LEADER ||
        configForm.value?.candidateStrategy ===
          CandidateStrategy.MULTI_LEVEL_DEPT_LEADER) &&
      configForm.value?.deptIds?.length > 0
    ) {
      const candidateNames: string[] = [];
      deptOptions?.value.forEach((item) => {
        if (configForm.value?.deptIds?.includes(item.id)) {
          candidateNames.push(item.name);
        }
      });
      if (
        configForm.value.candidateStrategy === CandidateStrategy.DEPT_MEMBER
      ) {
        showText = `部门成员：${candidateNames.join(',')}`;
      } else if (
        configForm.value.candidateStrategy === CandidateStrategy.DEPT_LEADER
      ) {
        showText = `部门的负责人：${candidateNames.join(',')}`;
      } else {
        showText = `多级部门的负责人：${candidateNames.join(',')}`;
      }
    }

    // 指定岗位
    if (
      configForm.value?.candidateStrategy === CandidateStrategy.POST &&
      configForm.value.postIds?.length > 0
    ) {
      const candidateNames: string[] = [];
      postOptions?.value.forEach((item) => {
        if (configForm.value?.postIds?.includes(item.id)) {
          candidateNames.push(item.name);
        }
      });
      showText = `指定岗位: ${candidateNames.join(',')}`;
    }
    // 指定用户组
    if (
      configForm.value?.candidateStrategy === CandidateStrategy.USER_GROUP &&
      configForm.value?.userGroups?.length > 0
    ) {
      const candidateNames: string[] = [];
      userGroupOptions?.value.forEach((item) => {
        if (configForm.value?.userGroups?.includes(item.id)) {
          candidateNames.push(item.name);
        }
      });
      showText = `指定用户组: ${candidateNames.join(',')}`;
    }

    // 表单内用户字段
    if (configForm.value?.candidateStrategy === CandidateStrategy.FORM_USER) {
      const formFieldOptions = parseFormCreateFields(unref(formFields));
      const item = formFieldOptions.find(
        (item) => item.field === configForm.value?.formUser,
      );
      showText = `表单用户：${item?.title}`;
    }

    // 表单内部门负责人
    if (
      configForm.value?.candidateStrategy === CandidateStrategy.FORM_DEPT_LEADER
    ) {
      showText = `表单内部门负责人`;
    }

    // 审批人自选
    if (
      configForm.value?.candidateStrategy ===
      CandidateStrategy.APPROVE_USER_SELECT
    ) {
      showText = `审批人自选`;
    }

    // 发起人自选
    if (
      configForm.value?.candidateStrategy ===
      CandidateStrategy.START_USER_SELECT
    ) {
      showText = `发起人自选`;
    }
    // 发起人自己
    if (configForm.value?.candidateStrategy === CandidateStrategy.START_USER) {
      showText = `发起人自己`;
    }
    // 发起人的部门负责人
    if (
      configForm.value?.candidateStrategy ===
      CandidateStrategy.START_USER_DEPT_LEADER
    ) {
      showText = `发起人的部门负责人`;
    }
    // 发起人的部门负责人
    if (
      configForm.value?.candidateStrategy ===
      CandidateStrategy.START_USER_MULTI_LEVEL_DEPT_LEADER
    ) {
      showText = `发起人连续部门负责人`;
    }
    // 流程表达式
    if (configForm.value?.candidateStrategy === CandidateStrategy.EXPRESSION) {
      showText = `流程表达式：${configForm.value.expression}`;
    }
    return showText;
  }

  /**
   *  处理候选人参数的赋值
   */
  function handleCandidateParam() {
    let candidateParam: string | undefined;
    if (!configForm.value) {
      return candidateParam;
    }
    switch (configForm.value.candidateStrategy) {
      case CandidateStrategy.DEPT_LEADER:
      case CandidateStrategy.DEPT_MEMBER: {
        candidateParam = configForm.value.deptIds?.join(',');
        break;
      }
      case CandidateStrategy.EXPRESSION: {
        candidateParam = configForm.value.expression;
        break;
      }
      // 表单内部门的负责人
      case CandidateStrategy.FORM_DEPT_LEADER: {
        // 候选人参数格式: | 分隔 。左边为表单内部门字段。 右边为部门层级
        const deptFieldOnForm = configForm.value.formDept;
        candidateParam = deptFieldOnForm?.concat(
          `|${configForm.value.deptLevel}`,
        );
        break;
      }
      case CandidateStrategy.FORM_USER: {
        candidateParam = configForm.value?.formUser;
        break;
      }
      // 指定连续多级部门的负责人
      case CandidateStrategy.MULTI_LEVEL_DEPT_LEADER: {
        // 候选人参数格式: | 分隔 。左边为部门（多个部门用 , 分隔）。 右边为部门层级
        const deptIds = configForm.value.deptIds?.join(',');
        candidateParam = deptIds?.concat(`|${configForm.value.deptLevel}`);
        break;
      }
      case CandidateStrategy.POST: {
        candidateParam = configForm.value.postIds?.join(',');
        break;
      }
      case CandidateStrategy.ROLE: {
        candidateParam = configForm.value.roleIds?.join(',');
        break;
      }
      // 发起人部门负责人
      case CandidateStrategy.START_USER_DEPT_LEADER:
      case CandidateStrategy.START_USER_MULTI_LEVEL_DEPT_LEADER: {
        candidateParam = `${configForm.value.deptLevel}`;
        break;
      }
      case CandidateStrategy.USER: {
        candidateParam = configForm.value.userIds?.join(',');
        break;
      }
      case CandidateStrategy.USER_GROUP: {
        candidateParam = configForm.value.userGroups?.join(',');
        break;
      }
      default: {
        break;
      }
    }
    return candidateParam;
  }
  /**
   *  解析候选人参数
   */
  function parseCandidateParam(
    candidateStrategy: CandidateStrategy,
    candidateParam: string | undefined,
  ) {
    if (!configForm.value || !candidateParam) {
      return;
    }
    switch (candidateStrategy) {
      case CandidateStrategy.DEPT_LEADER:
      case CandidateStrategy.DEPT_MEMBER: {
        configForm.value.deptIds = candidateParam
          .split(',')
          .map((item) => +item);
        break;
      }
      case CandidateStrategy.EXPRESSION: {
        configForm.value.expression = candidateParam;
        break;
      }
      // 表单内的部门负责人
      case CandidateStrategy.FORM_DEPT_LEADER: {
        // 候选人参数格式: | 分隔 。左边为表单内的部门字段。 右边为部门层级
        const paramArray = candidateParam.split('|');
        if (paramArray.length > 1) {
          configForm.value.formDept = paramArray[0];
          if (paramArray[1]) configForm.value.deptLevel = +paramArray[1];
        }
        break;
      }
      case CandidateStrategy.FORM_USER: {
        configForm.value.formUser = candidateParam;
        break;
      }
      // 指定连续多级部门的负责人
      case CandidateStrategy.MULTI_LEVEL_DEPT_LEADER: {
        // 候选人参数格式: | 分隔 。左边为部门（多个部门用 , 分隔）。 右边为部门层级
        const paramArray = candidateParam.split('|') as string[];
        if (paramArray.length > 1) {
          configForm.value.deptIds = paramArray[0]
            ?.split(',')
            .map((item) => +item);
          if (paramArray[1]) configForm.value.deptLevel = +paramArray[1];
        }
        break;
      }
      case CandidateStrategy.POST: {
        configForm.value.postIds = candidateParam
          .split(',')
          .map((item) => +item);
        break;
      }
      case CandidateStrategy.ROLE: {
        configForm.value.roleIds = candidateParam
          .split(',')
          .map((item) => +item);
        break;
      }
      // 发起人部门负责人
      case CandidateStrategy.START_USER_DEPT_LEADER:
      case CandidateStrategy.START_USER_MULTI_LEVEL_DEPT_LEADER: {
        configForm.value.deptLevel = +candidateParam;
        break;
      }
      case CandidateStrategy.USER: {
        configForm.value.userIds = candidateParam
          .split(',')
          .map((item) => +item);
        break;
      }
      case CandidateStrategy.USER_GROUP: {
        configForm.value.userGroups = candidateParam
          .split(',')
          .map((item) => +item);
        break;
      }
      default: {
        break;
      }
    }
  }
  return {
    configForm,
    roleOptions,
    postOptions,
    userOptions,
    userGroupOptions,
    deptTreeOptions,
    handleCandidateParam,
    parseCandidateParam,
    getShowText,
  };
}

/**
 * @description 抽屉配置
 */
export function useDrawer() {
  // 抽屉配置是否可见
  const settingVisible = ref(false);
  // 关闭配置抽屉
  function closeDrawer() {
    settingVisible.value = false;
  }
  // 打开配置抽屉
  function openDrawer() {
    settingVisible.value = true;
  }
  return {
    settingVisible,
    closeDrawer,
    openDrawer,
  };
}

/**
 * @description 节点名称配置
 */
export function useNodeName(nodeType: BpmNodeTypeEnum) {
  // 节点名称
  const nodeName = ref<string>();
  // 节点名称输入框
  const showInput = ref(false);
  // 输入框的引用
  const inputRef = ref<HTMLInputElement | null>(null);
  // 点击节点名称编辑图标
  function clickIcon() {
    showInput.value = true;
  }
  // 修改节点名称
  function changeNodeName() {
    showInput.value = false;
    nodeName.value =
      nodeName.value || (NODE_DEFAULT_NAME.get(nodeType) as string);
  }
  // 监听 showInput 的变化，当变为 true 时自动聚焦
  watch(showInput, (value) => {
    if (value) {
      nextTick(() => {
        inputRef.value?.focus();
      });
    }
  });

  return {
    nodeName,
    showInput,
    inputRef,
    clickIcon,
    changeNodeName,
  };
}

export function useNodeName2(
  node: Ref<SimpleFlowNode>,
  nodeType: BpmNodeTypeEnum,
) {
  // 显示节点名称输入框
  const showInput = ref(false);
  // 输入框的引用
  const inputRef = ref<HTMLInputElement | null>(null);

  // 监听 showInput 的变化，当变为 true 时自动聚焦
  watch(showInput, (value) => {
    if (value) {
      nextTick(() => {
        inputRef.value?.focus();
      });
    }
  });

  // 修改节点名称
  function changeNodeName() {
    showInput.value = false;
    node.value.name =
      node.value.name || (NODE_DEFAULT_NAME.get(nodeType) as string);
    console.warn('node.value.name===>', node.value.name);
  }
  // 点击节点标题进行输入
  function clickTitle() {
    showInput.value = true;
  }
  return {
    showInput,
    inputRef,
    clickTitle,
    changeNodeName,
  };
}

/**
 * @description 根据节点任务状态，获取节点任务状态样式
 */
export function useTaskStatusClass(
  taskStatus: BpmTaskStatusEnum | undefined,
): string {
  if (!taskStatus) {
    return '';
  }
  if (taskStatus === BpmTaskStatusEnum.APPROVE) {
    return 'status-pass';
  }
  if (taskStatus === BpmTaskStatusEnum.RUNNING) {
    return 'status-running';
  }
  if (taskStatus === BpmTaskStatusEnum.REJECT) {
    return 'status-reject';
  }
  if (taskStatus === BpmTaskStatusEnum.CANCEL) {
    return 'status-cancel';
  }
  return '';
}

/** 条件组件文字展示 */
export function getConditionShowText(
  conditionType: ConditionType | undefined,
  conditionExpression: string | undefined,
  conditionGroups: ConditionGroup | undefined,
  fieldOptions: Array<Record<string, any>>,
) {
  let showText: string | undefined;
  if (conditionType === ConditionType.EXPRESSION && conditionExpression) {
    showText = `表达式：${conditionExpression}`;
  }
  if (conditionType === ConditionType.RULE) {
    // 条件组是否为与关系
    const groupAnd = conditionGroups?.and;
    let warningMessage: string | undefined;
    const conditionGroup = conditionGroups?.conditions.map((item) => {
      return `(${item.rules
        .map((rule) => {
          if (rule.leftSide && rule.rightSide) {
            return `${getFormFieldTitle(
              fieldOptions,
              rule.leftSide,
            )} ${getOpName(rule.opCode)} ${rule.rightSide}`;
          } else {
            // 有一条规则不完善。提示错误
            warningMessage = '请完善条件规则';
            return '';
          }
        })
        .join(item.and ? ' 且 ' : ' 或 ')} ) `;
    });
    showText = warningMessage
      ? ''
      : conditionGroup?.join(groupAnd ? ' 且 ' : ' 或 ');
  }
  return showText;
}

/** 获取表单字段名称*/
function getFormFieldTitle(
  fieldOptions: Array<Record<string, any>>,
  field: string,
) {
  const item = fieldOptions.find((item) => item.field === field);
  return item?.title;
}

/** 获取操作符名称 */
function getOpName(opCode: string): string | undefined {
  const opName = COMPARISON_OPERATORS.find(
    (item: any) => item.value === opCode,
  );
  return opName?.label;
}

/** 获取条件节点默认的名称 */
export function getDefaultConditionNodeName(
  index: number,
  defaultFlow: boolean | undefined,
): string {
  if (defaultFlow) {
    return '其它情况';
  }
  return `条件${index + 1}`;
}

/** 获取包容分支条件节点默认的名称 */
export function getDefaultInclusiveConditionNodeName(
  index: number,
  defaultFlow: boolean | undefined,
): string {
  if (defaultFlow) {
    return '其它情况';
  }
  return `包容条件${index + 1}`;
}
