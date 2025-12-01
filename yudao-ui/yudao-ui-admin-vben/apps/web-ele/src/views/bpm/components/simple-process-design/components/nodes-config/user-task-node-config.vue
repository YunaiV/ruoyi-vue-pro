<script setup lang="ts">
import type { FormRules } from 'element-plus';

import type { ComponentPublicInstance, Ref } from 'vue';

import type { ButtonSetting, SimpleFlowNode } from '../../consts';
import type { UserTaskFormType } from '../../helpers';

import { computed, nextTick, onMounted, reactive, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import {
  BpmModelFormType,
  BpmNodeTypeEnum,
  ProcessVariableEnum,
} from '@vben/constants';
import { IconifyIcon } from '@vben/icons';
import { cloneDeep } from '@vben/utils';

import {
  ElButton,
  ElCol,
  ElDivider,
  ElForm,
  ElFormItem,
  ElOption,
  ElRadio,
  ElRadioButton,
  ElRadioGroup,
  ElRow,
  ElSelect,
  ElSwitch,
  ElTabPane,
  ElTabs,
  ElTreeSelect,
} from 'element-plus';

import {
  APPROVE_METHODS,
  APPROVE_TYPE,
  ApproveMethodType,
  ApproveType,
  ASSIGN_EMPTY_HANDLER_TYPES,
  ASSIGN_START_USER_HANDLER_TYPES,
  AssignEmptyHandlerType,
  CANDIDATE_STRATEGY,
  CandidateStrategy,
  DEFAULT_BUTTON_SETTING,
  FieldPermissionType,
  MULTI_LEVEL_DEPT,
  OPERATION_BUTTON_NAME,
  REJECT_HANDLER_TYPES,
  RejectHandlerType,
  TIME_UNIT_TYPES,
  TIMEOUT_HANDLER_TYPES,
  TimeoutHandlerType,
  TimeUnitType,
  TRANSACTOR_DEFAULT_BUTTON_SETTING,
} from '../../consts';
import {
  useFormFieldsPermission,
  useNodeForm,
  useNodeName,
  useWatchNode,
} from '../../helpers';
import UserTaskListener from './modules/user-task-listener.vue';
import { convertTimeUnit, getApproveTypeText } from './utils';

defineOptions({ name: 'UserTaskNodeConfig' });

const props = defineProps({
  flowNode: {
    type: Object as () => SimpleFlowNode,
    required: true,
  },
});

const emits = defineEmits<{
  findReturnTaskNodes: [nodeList: SimpleFlowNode[]];
}>();

const deptLevelLabel = computed(() => {
  let label = '部门负责人来源';
  if (
    configForm.value.candidateStrategy ===
    CandidateStrategy.MULTI_LEVEL_DEPT_LEADER
  ) {
    label = `${label}(指定部门向上)`;
  } else if (
    configForm.value.candidateStrategy === CandidateStrategy.FORM_DEPT_LEADER
  ) {
    label = `${label}(表单内部门向上)`;
  } else {
    label = `${label}(发起人部门向上)`;
  }
  return label;
});

// 监控节点的变化
const currentNode = useWatchNode(props);
// 抽屉配置
const [Drawer, drawerApi] = useVbenDrawer({
  header: true,
  closable: true,
  title: '',
  onConfirm() {
    saveConfig();
  },
});

// 节点名称配置
const { nodeName, showInput, clickIcon, changeNodeName, inputRef } =
  useNodeName(BpmNodeTypeEnum.USER_TASK_NODE);

// 激活的 Tab 标签页
const activeTabName = ref('user');

// 表单字段权限设置
const {
  formType,
  fieldsPermissionConfig,
  formFieldOptions,
  getNodeConfigFormFields,
} = useFormFieldsPermission(FieldPermissionType.READ);

// 表单内用户字段选项, 必须是必填和用户选择器
const userFieldOnFormOptions = computed(() => {
  return formFieldOptions.filter((item) => item.type === 'UserSelect');
});

// 表单内部门字段选项, 必须是必填和部门选择器
const deptFieldOnFormOptions = computed(() => {
  return formFieldOptions.filter((item) => item.type === 'DeptSelect');
});

// 操作按钮设置
const {
  buttonsSetting,
  btnDisplayNameEdit,
  changeBtnDisplayName,
  btnDisplayNameBlurEvent,
  setInputRef,
} = useButtonsSetting();

const approveType = ref(ApproveType.USER);

// 审批人表单设置
const formRef = ref(); // 表单 Ref
// 表单校验规则
const formRules: FormRules = reactive({
  candidateStrategy: [
    { required: true, message: '审批人设置不能为空', trigger: 'change' },
  ],
  userIds: [{ required: true, message: '用户不能为空', trigger: 'change' }],
  roleIds: [{ required: true, message: '角色不能为空', trigger: 'change' }],
  deptIds: [{ required: true, message: '部门不能为空', trigger: 'change' }],
  userGroups: [
    { required: true, message: '用户组不能为空', trigger: 'change' },
  ],
  formUser: [
    { required: true, message: '表单内用户字段不能为空', trigger: 'change' },
  ],
  formDept: [
    { required: true, message: '表单内部门字段不能为空', trigger: 'change' },
  ],
  postIds: [{ required: true, message: '岗位不能为空', trigger: 'change' }],
  expression: [
    { required: true, message: '流程表达式不能为空', trigger: 'blur' },
  ],
  approveMethod: [
    { required: true, message: '多人审批方式不能为空', trigger: 'change' },
  ],
  approveRatio: [
    { required: true, message: '通过比例不能为空', trigger: 'blur' },
  ],
  returnNodeId: [
    { required: true, message: '驳回节点不能为空', trigger: 'change' },
  ],
  timeoutHandlerEnable: [{ required: true }],
  timeoutHandlerType: [{ required: true }],
  timeDuration: [
    { required: true, message: '超时时间不能为空', trigger: 'blur' },
  ],
  maxRemindCount: [
    { required: true, message: '提醒次数不能为空', trigger: 'blur' },
  ],
  assignEmptyHandlerType: [{ required: true }],
  assignEmptyHandlerUserIds: [
    { required: true, message: '用户不能为空', trigger: 'change' },
  ],
  assignStartUserHandlerType: [{ required: true }],
});

const {
  configForm: tempConfigForm,
  roleOptions,
  postOptions,
  userOptions,
  userGroupOptions,
  deptTreeOptions,
  handleCandidateParam,
  parseCandidateParam,
  getShowText,
} = useNodeForm(currentNode.value.type);
const configForm = tempConfigForm as Ref<UserTaskFormType>;

// 改变审批人设置策略
function changeCandidateStrategy() {
  configForm.value.userIds = [];
  configForm.value.deptIds = [];
  configForm.value.roleIds = [];
  configForm.value.postIds = [];
  configForm.value.userGroups = [];
  configForm.value.deptLevel = 1;
  configForm.value.formUser = '';
  configForm.value.formDept = '';
  configForm.value.approveMethod = ApproveMethodType.SEQUENTIAL_APPROVE;
}

/** 审批方式改变 */
function approveMethodChanged() {
  configForm.value.rejectHandlerType = RejectHandlerType.FINISH_PROCESS;
  if (configForm.value.approveMethod === ApproveMethodType.APPROVE_BY_RATIO) {
    configForm.value.approveRatio = 100;
  }
  formRef.value.clearValidate('approveRatio');
}
// 审批拒绝 可退回的节点
const returnTaskList = ref<SimpleFlowNode[]>([]);
// 审批人超时未处理设置
const {
  timeoutHandlerChange,
  cTimeoutType,
  timeoutHandlerTypeChanged,
  timeUnit,
  timeUnitChange,
  isoTimeDuration,
  cTimeoutMaxRemindCount,
} = useTimeoutHandler();

const userTaskListenerRef = ref();

/** 节点类型名称 */
const nodeTypeName = computed(() => {
  return currentNode.value.type === BpmNodeTypeEnum.TRANSACTOR_NODE
    ? '办理'
    : '审批';
});

/** 校验节点配置 */
async function validateConfig() {
  if (!formRef.value) return false;
  if (!userTaskListenerRef.value) return false;

  // 先进行表单验证，记录验证结果
  const userFormValid = await formRef.value.validate().catch(() => false);
  const listenerValid = await userTaskListenerRef.value.validate().catch(() => {
    return false;
  });
  // 如果监听器有错误，切换到监听器Tab
  if (!listenerValid) {
    activeTabName.value = 'listener';
    return false;
  }
  // 如果审批人表单有错误，切换到审批人Tab
  if (!userFormValid) {
    activeTabName.value = 'user';
    return false;
  }

  const showText = getShowText();
  if (!showText) return false;

  return true;
}

/** 保存配置 */
async function saveConfig() {
  // 如果不是人工审批，不执行校验，直接返回
  if (approveType.value !== ApproveType.USER) {
    currentNode.value.name = nodeName.value!;
    currentNode.value.approveType = approveType.value;
    currentNode.value.showText = getApproveTypeText(approveType.value);
    drawerApi.close();
    return true;
  }
  // 执行校验
  if (!(await validateConfig())) {
    return false;
  }
  // 设置审批节点名称
  currentNode.value.name = nodeName.value!;
  // 设置审批类型
  currentNode.value.approveType = approveType.value;
  // 设置审批人设置策略
  currentNode.value.candidateStrategy = configForm.value.candidateStrategy;
  // 处理 candidateParam 参数
  currentNode.value.candidateParam = handleCandidateParam();
  // 设置审批方式
  currentNode.value.approveMethod = configForm.value.approveMethod;
  if (configForm.value.approveMethod === ApproveMethodType.APPROVE_BY_RATIO) {
    currentNode.value.approveRatio = configForm.value.approveRatio;
  }
  // 设置拒绝处理
  currentNode.value.rejectHandler = {
    type: configForm.value.rejectHandlerType!,
    returnNodeId: configForm.value.returnNodeId,
  };
  // 设置超时处理
  currentNode.value.timeoutHandler = {
    enable: configForm.value.timeoutHandlerEnable!,
    type: cTimeoutType.value,
    timeDuration: isoTimeDuration.value,
    maxRemindCount: cTimeoutMaxRemindCount.value,
  };
  // 设置审批人为空时
  currentNode.value.assignEmptyHandler = {
    type: configForm.value.assignEmptyHandlerType!,
    userIds:
      configForm.value.assignEmptyHandlerType ===
      AssignEmptyHandlerType.ASSIGN_USER
        ? configForm.value.assignEmptyHandlerUserIds
        : undefined,
  };
  // 设置审批人与发起人相同时
  currentNode.value.assignStartUserHandlerType =
    configForm.value.assignStartUserHandlerType;
  // 设置表单权限
  currentNode.value.fieldsPermission = fieldsPermissionConfig.value;
  // 设置按钮权限
  currentNode.value.buttonsSetting = buttonsSetting.value;
  // 创建任务监听器
  currentNode.value.taskCreateListener = {
    enable: configForm.value.taskCreateListenerEnable ?? false,
    path: configForm.value.taskCreateListenerPath,
    header: configForm.value.taskCreateListener?.header,
    body: configForm.value.taskCreateListener?.body,
  };
  // 指派任务监听器
  currentNode.value.taskAssignListener = {
    enable: configForm.value.taskAssignListenerEnable ?? false,
    path: configForm.value.taskAssignListenerPath,
    header: configForm.value.taskAssignListener?.header,
    body: configForm.value.taskAssignListener?.body,
  };
  // 完成任务监听器
  currentNode.value.taskCompleteListener = {
    enable: configForm.value.taskCompleteListenerEnable ?? false,
    path: configForm.value.taskCompleteListenerPath,
    header: configForm.value.taskCompleteListener?.header,
    body: configForm.value.taskCompleteListener?.body,
  };
  // 签名
  currentNode.value.signEnable = configForm.value.signEnable;
  // 审批意见
  currentNode.value.reasonRequire = configForm.value.reasonRequire;
  // 跳过表达式
  currentNode.value.skipExpression = configForm.value.skipExpression;
  currentNode.value.showText = getShowText();
  drawerApi.close();
  return true;
}

/** 显示审批节点配置抽屉，由父组件传过来 */
function showUserTaskNodeConfig(node: SimpleFlowNode) {
  nodeName.value = node.name;
  // 1 审批类型
  approveType.value =
    node?.approveType === undefined ? ApproveType.USER : node.approveType;
  // 如果审批类型不是人工审批返回
  if (approveType.value !== ApproveType.USER) {
    drawerApi.open();
    return;
  }

  // 2.1 审批人设置策略
  configForm.value.candidateStrategy = node.candidateStrategy!;
  // 解析候选人参数
  parseCandidateParam(node.candidateStrategy!, node?.candidateParam);
  // 2.2 设置审批方式
  configForm.value.approveMethod = node.approveMethod!;
  if (node.approveMethod === ApproveMethodType.APPROVE_BY_RATIO) {
    configForm.value.approveRatio = node.approveRatio!;
  }
  // 2.3 设置审批拒绝处理
  configForm.value.rejectHandlerType = node.rejectHandler?.type;
  configForm.value.returnNodeId = node.rejectHandler?.returnNodeId;
  const matchNodeList: SimpleFlowNode[] = [];
  emits('findReturnTaskNodes', matchNodeList);
  returnTaskList.value = matchNodeList;
  // 2.4 设置审批超时处理
  configForm.value.timeoutHandlerEnable = node.timeoutHandler?.enable;
  if (node.timeoutHandler?.enable && node.timeoutHandler?.timeDuration) {
    const strTimeDuration = node.timeoutHandler.timeDuration;
    const parseTime = strTimeDuration.slice(2, -1);
    const parseTimeUnit = strTimeDuration.slice(-1);
    configForm.value.timeDuration = Number.parseInt(parseTime);
    timeUnit.value = convertTimeUnit(parseTimeUnit);
  }
  configForm.value.timeoutHandlerType = node.timeoutHandler?.type;
  configForm.value.maxRemindCount = node.timeoutHandler?.maxRemindCount;
  // 2.5 设置审批人为空时
  configForm.value.assignEmptyHandlerType = node.assignEmptyHandler?.type;
  configForm.value.assignEmptyHandlerUserIds = node.assignEmptyHandler?.userIds;
  // 2.6 设置用户任务的审批人与发起人相同处理
  configForm.value.assignStartUserHandlerType = node.assignStartUserHandlerType;
  // 3. 操作按钮设置
  buttonsSetting.value =
    cloneDeep(node.buttonsSetting) ||
    (node.type === BpmNodeTypeEnum.TRANSACTOR_NODE
      ? TRANSACTOR_DEFAULT_BUTTON_SETTING
      : DEFAULT_BUTTON_SETTING);
  // 4. 表单字段权限配置
  getNodeConfigFormFields(node.fieldsPermission);
  // 5. 监听器
  // 5.1 创建任务
  configForm.value.taskCreateListenerEnable = node.taskCreateListener?.enable;
  configForm.value.taskCreateListenerPath = node.taskCreateListener?.path;
  configForm.value.taskCreateListener = {
    header: node.taskCreateListener?.header ?? [],
    body: node.taskCreateListener?.body ?? [],
  };
  // 5.2 指派任务
  configForm.value.taskAssignListenerEnable = node.taskAssignListener?.enable;
  configForm.value.taskAssignListenerPath = node.taskAssignListener?.path;
  configForm.value.taskAssignListener = {
    header: node.taskAssignListener?.header ?? [],
    body: node.taskAssignListener?.body ?? [],
  };
  // 5.3 完成任务
  configForm.value.taskCompleteListenerEnable =
    node.taskCompleteListener?.enable;
  configForm.value.taskCompleteListenerPath = node.taskCompleteListener?.path;
  configForm.value.taskCompleteListener = {
    header: node.taskCompleteListener?.header ?? [],
    body: node.taskCompleteListener?.body ?? [],
  };
  // 6. 签名
  configForm.value.signEnable = node?.signEnable ?? false;
  // 7. 审批意见
  configForm.value.reasonRequire = node?.reasonRequire ?? false;
  // 8. 跳过表达式
  configForm.value.skipExpression = node?.skipExpression ?? '';
  drawerApi.open();
}

defineExpose({ showUserTaskNodeConfig }); // 暴露方法给父组件

/** 操作按钮设置 */
function useButtonsSetting() {
  const buttonsSetting = ref<ButtonSetting[]>();
  // 操作按钮显示名称可编辑
  const btnDisplayNameEdit = ref<boolean[]>([]);
  // 输入框的引用数组 - 内部使用，不暴露出去
  const _btnDisplayNameInputRefs = ref<Array<HTMLInputElement | null>>([]);

  const changeBtnDisplayName = (index: number) => {
    btnDisplayNameEdit.value[index] = true;
    // 输入框自动聚焦
    nextTick(() => {
      if (_btnDisplayNameInputRefs.value[index]) {
        _btnDisplayNameInputRefs.value[index]?.focus();
      }
    });
  };

  const btnDisplayNameBlurEvent = (index: number) => {
    btnDisplayNameEdit.value[index] = false;
    const buttonItem = buttonsSetting.value![index];
    if (buttonItem)
      buttonItem.displayName =
        buttonItem.displayName || OPERATION_BUTTON_NAME.get(buttonItem.id)!;
  };

  // 设置 ref 引用的方法
  const setInputRef = (
    el: ComponentPublicInstance | Element | null,
    index: number,
  ) => {
    _btnDisplayNameInputRefs.value[index] = el as HTMLInputElement;
  };

  return {
    buttonsSetting,
    btnDisplayNameEdit,
    changeBtnDisplayName,
    btnDisplayNameBlurEvent,
    setInputRef,
  };
}

/** 审批人超时未处理配置 */
function useTimeoutHandler() {
  // 时间单位
  const timeUnit = ref(TimeUnitType.HOUR);

  // 超时开关改变
  const timeoutHandlerChange = () => {
    if (configForm.value.timeoutHandlerEnable) {
      timeUnit.value = 2;
      configForm.value.timeDuration = 6;
      configForm.value.timeoutHandlerType = 1;
      configForm.value.maxRemindCount = 1;
    }
  };
  // 超时执行的动作
  const cTimeoutType = computed(() => {
    if (!configForm.value.timeoutHandlerEnable) {
      return undefined;
    }
    return configForm.value.timeoutHandlerType;
  });

  // 超时处理动作改变
  const timeoutHandlerTypeChanged = () => {
    if (configForm.value.timeoutHandlerType === TimeoutHandlerType.REMINDER) {
      configForm.value.maxRemindCount = 1; // 超时提醒次数，默认为1
    }
  };

  // 时间单位改变
  const timeUnitChange = () => {
    // 分钟，默认是 60 分钟
    if (timeUnit.value === TimeUnitType.MINUTE) {
      configForm.value.timeDuration = 60;
    }
    // 小时，默认是 6 个小时
    if (timeUnit.value === TimeUnitType.HOUR) {
      configForm.value.timeDuration = 6;
    }
    // 天， 默认 1 天
    if (timeUnit.value === TimeUnitType.DAY) {
      configForm.value.timeDuration = 1;
    }
  };
  // 超时时间 ISO 表示
  const isoTimeDuration = computed(() => {
    if (!configForm.value.timeoutHandlerEnable) {
      return undefined;
    }
    let strTimeDuration = 'PT';
    if (timeUnit.value === TimeUnitType.MINUTE) {
      strTimeDuration += `${configForm.value.timeDuration}M`;
    }
    if (timeUnit.value === TimeUnitType.HOUR) {
      strTimeDuration += `${configForm.value.timeDuration}H`;
    }
    if (timeUnit.value === TimeUnitType.DAY) {
      strTimeDuration += `${configForm.value.timeDuration}D`;
    }
    return strTimeDuration;
  });

  // 超时最大提醒次数
  const cTimeoutMaxRemindCount = computed(() => {
    if (!configForm.value.timeoutHandlerEnable) {
      return undefined;
    }
    if (configForm.value.timeoutHandlerType !== TimeoutHandlerType.REMINDER) {
      return undefined;
    }
    return configForm.value.maxRemindCount;
  });

  return {
    timeoutHandlerChange,
    cTimeoutType,
    timeoutHandlerTypeChanged,
    timeUnit,
    timeUnitChange,
    isoTimeDuration,
    cTimeoutMaxRemindCount,
  };
}

/** 批量更新权限 */
function updatePermission(type: string) {
  fieldsPermissionConfig.value.forEach((field) => {
    if (type === 'READ') {
      field.permission = FieldPermissionType.READ;
    } else if (type === 'WRITE') {
      field.permission = FieldPermissionType.WRITE;
    } else {
      field.permission = FieldPermissionType.NONE;
    }
  });
}

// 在组件初始化时记录初始值
onMounted(() => {
  // 固定添加发起人ID字段
  formFieldOptions.unshift({
    field: ProcessVariableEnum.START_USER_ID,
    title: '发起人ID',
    type: 'UserSelect',
    required: true,
  });
});
</script>
<template>
  <Drawer class="w-2/5">
    <template #title>
      <div class="config-header">
        <ElInput
          v-if="showInput"
          ref="inputRef"
          type="text"
          class="focus:border-blue-500 focus:shadow-[0_0_0_2px_rgba(24,144,255,0.2)] focus:outline-none"
          @blur="changeNodeName()"
          @keyup.enter="changeNodeName()"
          v-model="nodeName"
          :placeholder="nodeName"
        />
        <div v-else class="node-name">
          {{ nodeName }}
          <IconifyIcon class="ml-1" icon="lucide:edit-3" @click="clickIcon()" />
        </div>
      </div>
    </template>
    <div
      v-if="currentNode.type === BpmNodeTypeEnum.USER_TASK_NODE"
      class="mb-3 flex items-center"
    >
      <span class="mr-3 text-base">审批类型 :</span>
      <ElRadioGroup v-model="approveType">
        <ElRadioButton
          v-for="(item, index) in APPROVE_TYPE"
          :key="index"
          :value="item.value"
        >
          {{ item.label }}
        </ElRadioButton>
      </ElRadioGroup>
    </div>
    <ElTabs v-model="activeTabName" v-if="approveType === ApproveType.USER">
      <ElTabPane :label="`${nodeTypeName}人`" name="user">
        <div>
          <ElForm
            ref="formRef"
            :model="configForm"
            label-position="top"
            :rules="formRules"
          >
            <!-- 审批/办理 人设置 -->
            <ElFormItem
              :label="`${nodeTypeName}人设置`"
              prop="candidateStrategy"
            >
              <ElRadioGroup
                v-model="configForm.candidateStrategy"
                @change="changeCandidateStrategy"
              >
                <ElRow :gutter="8">
                  <ElCol
                    v-for="(dict, index) in CANDIDATE_STRATEGY"
                    :key="index"
                    :span="8"
                  >
                    <ElRadio :value="dict.value">
                      {{ dict.label }}
                    </ElRadio>
                  </ElCol>
                </ElRow>
              </ElRadioGroup>
            </ElFormItem>
            <ElFormItem
              v-if="configForm.candidateStrategy === CandidateStrategy.ROLE"
              label="指定角色"
              name="roleIds"
            >
              <ElSelect v-model="configForm.roleIds" clearable multiple>
                <ElOption
                  v-for="item in roleOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id!"
                />
              </ElSelect>
            </ElFormItem>
            <ElFormItem
              v-if="
                configForm.candidateStrategy ===
                  CandidateStrategy.DEPT_MEMBER ||
                configForm.candidateStrategy ===
                  CandidateStrategy.DEPT_LEADER ||
                configForm.candidateStrategy ===
                  CandidateStrategy.MULTI_LEVEL_DEPT_LEADER
              "
              label="指定部门"
              name="deptIds"
            >
              <ElTreeSelect
                v-model="configForm.deptIds"
                :tree-data="deptTreeOptions"
                :field-names="{
                  label: 'name',
                  value: 'id',
                  children: 'children',
                }"
                empty-text="加载中，请稍等..."
                multiple
                :check-strictly="true"
                clearable
                :show-checkbox="true"
              />
            </ElFormItem>
            <ElFormItem
              v-if="configForm.candidateStrategy === CandidateStrategy.POST"
              label="指定岗位"
              name="postIds"
            >
              <ElSelect v-model="configForm.postIds" clearable multiple>
                <ElOption
                  v-for="item in postOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id!"
                />
              </ElSelect>
            </ElFormItem>
            <ElFormItem
              v-if="configForm.candidateStrategy === CandidateStrategy.USER"
              label="指定用户"
              name="userIds"
            >
              <ElSelect v-model="configForm.userIds" clearable multiple>
                <ElOption
                  v-for="item in userOptions"
                  :key="item.id"
                  :label="item.nickname"
                  :value="item.id!"
                />
              </ElSelect>
            </ElFormItem>
            <ElFormItem
              v-if="
                configForm.candidateStrategy === CandidateStrategy.USER_GROUP
              "
              label="指定用户组"
              name="userGroups"
            >
              <ElSelect v-model="configForm.userGroups" clearable multiple>
                <ElOption
                  v-for="item in userGroupOptions"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </ElSelect>
            </ElFormItem>
            <ElFormItem
              v-if="
                configForm.candidateStrategy === CandidateStrategy.FORM_USER
              "
              label="表单内用户字段"
              name="formUser"
            >
              <ElSelect v-model="configForm.formUser" clearable>
                <ElOption
                  v-for="(item, idx) in userFieldOnFormOptions"
                  :key="idx"
                  :label="item.title"
                  :value="item.field"
                  :disabled="!item.required"
                />
              </ElSelect>
            </ElFormItem>
            <ElFormItem
              v-if="
                configForm.candidateStrategy ===
                CandidateStrategy.FORM_DEPT_LEADER
              "
              label="表单内部门字段"
              name="formDept"
            >
              <ElSelect v-model="configForm.formDept" clearable>
                <ElOption
                  v-for="(item, idx) in deptFieldOnFormOptions"
                  :key="idx"
                  :label="item.title"
                  :value="item.field"
                  :disabled="!item.required"
                />
              </ElSelect>
            </ElFormItem>
            <ElFormItem
              v-if="
                configForm.candidateStrategy ===
                  CandidateStrategy.MULTI_LEVEL_DEPT_LEADER ||
                configForm.candidateStrategy ===
                  CandidateStrategy.START_USER_DEPT_LEADER ||
                configForm.candidateStrategy ===
                  CandidateStrategy.START_USER_MULTI_LEVEL_DEPT_LEADER ||
                configForm.candidateStrategy ===
                  CandidateStrategy.FORM_DEPT_LEADER
              "
              :label="deptLevelLabel!"
              name="deptLevel"
            >
              <ElSelect v-model="configForm.deptLevel" clearable>
                <ElOption
                  v-for="(item, index) in MULTI_LEVEL_DEPT"
                  :key="index"
                  :label="item.label"
                  :value="item.value"
                />
              </ElSelect>
            </ElFormItem>
            <!-- TODO @jason：后续要支持选择已经存好的表达式 -->
            <ElFormItem
              v-if="
                configForm.candidateStrategy === CandidateStrategy.EXPRESSION
              "
              label="流程表达式"
              name="expression"
            >
              <ElInput
                v-model="configForm.expression"
                type="textarea"
                :rows="4"
              />
            </ElFormItem>
            <!-- 多人审批/办理 方式 -->
            <ElFormItem :label="`多人${nodeTypeName}方式`" name="approveMethod">
              <ElRadioGroup
                v-model="configForm.approveMethod"
                @change="approveMethodChanged"
              >
                <ElRow>
                  <ElCol
                    :span="24"
                    v-for="(item, index) in APPROVE_METHODS"
                    :key="index"
                  >
                    <div class="flex items-center">
                      <ElRadio :value="item.value">
                        {{ item.label }}
                      </ElRadio>
                      <ElInputNumber
                        v-if="
                          item.value === ApproveMethodType.APPROVE_BY_RATIO &&
                          configForm.approveMethod ===
                            ApproveMethodType.APPROVE_BY_RATIO
                        "
                        v-model="configForm.approveRatio"
                        :min="10"
                        :max="100"
                        :step="10"
                        size="small"
                      />
                    </div>
                  </ElCol>
                </ElRow>
              </ElRadioGroup>
            </ElFormItem>

            <div v-if="currentNode.type === BpmNodeTypeEnum.USER_TASK_NODE">
              <ElDivider content-position="left">审批人拒绝时</ElDivider>
              <ElFormItem name="rejectHandlerType">
                <ElRadioGroup
                  v-model="configForm.rejectHandlerType"
                  class="w-full"
                >
                  <ElRow :gutter="32">
                    <ElCol
                      :span="8"
                      v-for="(item, index) in REJECT_HANDLER_TYPES"
                      :key="index"
                    >
                      <ElRadio :value="item.value">
                        {{ item.label }}
                      </ElRadio>
                    </ElCol>
                  </ElRow>
                </ElRadioGroup>
              </ElFormItem>

              <ElFormItem
                v-if="
                  configForm.rejectHandlerType ===
                  RejectHandlerType.RETURN_USER_TASK
                "
                label="驳回节点"
                name="returnNodeId"
              >
                <ElSelect v-model="configForm.returnNodeId" clearable>
                  <ElOption
                    v-for="item in returnTaskList"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  />
                </ElSelect>
              </ElFormItem>
            </div>

            <div v-if="currentNode.type === BpmNodeTypeEnum.USER_TASK_NODE">
              <ElDivider content-position="left">审批人超时未处理</ElDivider>
              <ElFormItem label="启用开关" name="timeoutHandlerEnable">
                <ElSwitch
                  v-model="configForm.timeoutHandlerEnable"
                  active-text="开"
                  inactive-text="关"
                  @change="timeoutHandlerChange"
                />
              </ElFormItem>
              <ElFormItem
                label="执行动作"
                name="timeoutHandlerType"
                v-if="configForm.timeoutHandlerEnable"
              >
                <ElRadioGroup
                  v-model="configForm.timeoutHandlerType"
                  @change="timeoutHandlerTypeChanged"
                >
                  <ElRadioButton
                    v-for="item in TIMEOUT_HANDLER_TYPES"
                    :key="item.value"
                    :value="item.value"
                    :label="item.label"
                  >
                    {{ item.label }}
                  </ElRadioButton>
                </ElRadioGroup>
              </ElFormItem>
              <ElFormItem
                label="超时时间设置"
                v-if="configForm.timeoutHandlerEnable"
              >
                <div class="flex items-center gap-2">
                  <span class="text-sm">当超时</span>
                  <ElFormItem name="timeDuration" class="!mb-0">
                    <ElInputNumber
                      v-model="configForm.timeDuration"
                      :min="1"
                      controls-position="right"
                    />
                  </ElFormItem>
                  <ElSelect
                    v-model="timeUnit"
                    class="!w-24"
                    @change="timeUnitChange"
                  >
                    <ElOption
                      v-for="item in TIME_UNIT_TYPES"
                      :key="item.value"
                      :label="item.label"
                      :value="item.value"
                    />
                  </ElSelect>
                  <span class="text-sm">未处理</span>
                </div>
              </ElFormItem>
              <ElFormItem
                label="最大提醒次数"
                name="maxRemindCount"
                v-if="
                  configForm.timeoutHandlerEnable &&
                  configForm.timeoutHandlerType === 1
                "
              >
                <ElInputNumber
                  v-model="configForm.maxRemindCount"
                  :min="1"
                  :max="10"
                />
              </ElFormItem>
            </div>

            <ElDivider content-position="left">
              {{ nodeTypeName }}人为空时
            </ElDivider>
            <ElFormItem name="assignEmptyHandlerType">
              <ElRadioGroup v-model="configForm.assignEmptyHandlerType">
                <ElRow>
                  <ElCol
                    :span="24"
                    v-for="(item, index) in ASSIGN_EMPTY_HANDLER_TYPES"
                    :key="index"
                  >
                    <ElRadio :value="item.value">
                      {{ item.label }}
                    </ElRadio>
                  </ElCol>
                </ElRow>
              </ElRadioGroup>
            </ElFormItem>
            <ElFormItem
              v-if="
                configForm.assignEmptyHandlerType ===
                AssignEmptyHandlerType.ASSIGN_USER
              "
              label="指定用户"
              name="assignEmptyHandlerUserIds"
            >
              <ElSelect
                v-model="configForm.assignEmptyHandlerUserIds"
                clearable
                multiple
              >
                <ElOption
                  v-for="item in userOptions"
                  :key="item.id"
                  :label="item.nickname"
                  :value="item.id!"
                />
              </ElSelect>
            </ElFormItem>

            <div v-if="currentNode.type === BpmNodeTypeEnum.USER_TASK_NODE">
              <ElDivider content-position="left">
                审批人与提交人为同一人时
              </ElDivider>
              <ElFormItem name="assignStartUserHandlerType">
                <ElRadioGroup v-model="configForm.assignStartUserHandlerType">
                  <ElRow :gutter="16">
                    <ElCol
                      :span="24"
                      v-for="(item, index) in ASSIGN_START_USER_HANDLER_TYPES"
                      :key="index"
                    >
                      <ElRadio :value="item.value">
                        {{ item.label }}
                      </ElRadio>
                    </ElCol>
                  </ElRow>
                </ElRadioGroup>
              </ElFormItem>
            </div>

            <div v-if="currentNode.type === BpmNodeTypeEnum.USER_TASK_NODE">
              <ElDivider content-position="left">是否需要签名</ElDivider>
              <ElFormItem name="signEnable">
                <ElSwitch
                  v-model="configForm.signEnable"
                  active-text="是"
                  inactive-text="否"
                />
              </ElFormItem>
            </div>

            <div v-if="currentNode.type === BpmNodeTypeEnum.USER_TASK_NODE">
              <ElDivider content-position="left">审批意见</ElDivider>
              <ElFormItem name="reasonRequire">
                <ElSwitch
                  v-model="configForm.reasonRequire"
                  active-text="必填"
                  inactive-text="非必填"
                />
              </ElFormItem>
            </div>
            <div>
              <ElDivider content-position="left">跳过表达式</ElDivider>
              <ElFormItem prop="skipExpression">
                <ElInput
                  v-model="configForm.skipExpression"
                  type="textarea"
                  :rows="2"
                />
              </ElFormItem>
            </div>
          </ElForm>
        </div>
      </ElTabPane>
      <ElTabPane
        label="操作按钮设置"
        v-if="currentNode.type === BpmNodeTypeEnum.USER_TASK_NODE"
        name="buttons"
      >
        <div class="p-1">
          <div class="mb-4 text-base font-bold">操作按钮</div>

          <!-- 表头 -->
          <ElRow class="border border-gray-200 px-4 py-3">
            <ElCol :span="8" class="font-bold">操作按钮</ElCol>
            <ElCol :span="12" class="font-bold">显示名称</ElCol>
            <ElCol :span="4" class="flex items-center justify-center font-bold">
              启用
            </ElCol>
          </ElRow>

          <!-- 表格内容 -->
          <div v-for="(item, index) in buttonsSetting" :key="index">
            <ElRow class="border border-t-0 border-gray-200 px-4 py-2">
              <ElCol :span="8" class="flex items-center truncate">
                {{ OPERATION_BUTTON_NAME.get(item.id) }}
              </ElCol>
              <ElCol :span="12" class="flex items-center">
                <ElInput
                  v-if="btnDisplayNameEdit[index]"
                  :ref="(el: any) => setInputRef(el, index)"
                  type="text"
                  class="max-w-32 focus:border-blue-500 focus:shadow-[0_0_0_2px_rgba(24,144,255,0.2)] focus:outline-none"
                  @blur="btnDisplayNameBlurEvent(index)"
                  @keyup.enter="btnDisplayNameBlurEvent(index)"
                  v-model="item.displayName"
                  :placeholder="item.displayName"
                />
                <ElButton v-else text @click="changeBtnDisplayName(index)">
                  <div class="flex items-center">
                    {{ item.displayName }}
                    <IconifyIcon icon="lucide:edit" class="ml-2" />
                  </div>
                </ElButton>
              </ElCol>
              <ElCol :span="4" class="flex items-center justify-center">
                <ElSwitch v-model="item.enable" />
              </ElCol>
            </ElRow>
          </div>
        </div>
      </ElTabPane>
      <ElTabPane
        label="表单字段权限"
        name="fields"
        v-if="formType === BpmModelFormType.NORMAL"
      >
        <div class="p-1">
          <div class="mb-4 text-base font-bold">字段权限</div>

          <!-- 表头 -->
          <ElRow class="border border-gray-200 px-4 py-3">
            <ElCol :span="8" class="font-bold">字段名称</ElCol>
            <ElCol :span="16" class="!flex">
              <span
                class="flex-1 cursor-pointer text-center font-bold"
                @click="updatePermission('READ')"
              >
                只读
              </span>
              <span
                class="flex-1 cursor-pointer text-center font-bold"
                @click="updatePermission('WRITE')"
              >
                可编辑
              </span>
              <span
                class="flex-1 cursor-pointer text-center font-bold"
                @click="updatePermission('NONE')"
              >
                隐藏
              </span>
            </ElCol>
          </ElRow>

          <!-- 表格内容 -->
          <div v-for="(item, index) in fieldsPermissionConfig" :key="index">
            <ElRow class="border border-t-0 border-gray-200 px-4 py-2">
              <ElCol :span="8" class="flex items-center truncate">
                {{ item.title }}
              </ElCol>
              <ElCol :span="16">
                <ElRadioGroup v-model="item.permission" class="flex w-full">
                  <div class="flex flex-1 justify-center">
                    <ElRadio :value="FieldPermissionType.READ" />
                  </div>
                  <div class="flex flex-1 justify-center">
                    <ElRadio :value="FieldPermissionType.WRITE" />
                  </div>
                  <div class="flex flex-1 justify-center">
                    <ElRadio :value="FieldPermissionType.NONE" />
                  </div>
                </ElRadioGroup>
              </ElCol>
            </ElRow>
          </div>
        </div>
      </ElTabPane>
      <ElTabPane label="监听器" name="listener">
        <UserTaskListener
          ref="userTaskListenerRef"
          v-model="configForm"
          :form-field-options="formFieldOptions"
        />
      </ElTabPane>
    </ElTabs>
  </Drawer>
</template>
