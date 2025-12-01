<script lang="ts" setup>
// TODO @jason：你感觉要拆分下，按照表单么？
import type { FormInstance, FormRules } from 'element-plus';

import type { BpmProcessInstanceApi } from '#/api/bpm/processInstance';
import type { SystemUserApi } from '#/api/system/user';

import { computed, nextTick, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';

import { useVbenModal } from '@vben/common-ui';
import {
  BpmCandidateStrategyEnum,
  BpmModelFormType,
  BpmNodeTypeEnum,
  BpmProcessInstanceStatus,
  BpmTaskOperationButtonTypeEnum,
  BpmTaskStatusEnum,
  OPERATION_BUTTON_NAME,
} from '@vben/constants';
import { IconifyIcon } from '@vben/icons';
import { useUserStore } from '@vben/stores';
import { isEmpty } from '@vben/utils';

import FormCreate from '@form-create/element-ui';
import {
  ElAlert,
  ElButton,
  ElCard,
  ElForm,
  ElFormItem,
  ElImage,
  ElInput,
  ElMessage,
  ElOption,
  ElPopover,
  ElSelect,
  ElSpace,
} from 'element-plus';

import {
  cancelProcessInstanceByStartUser,
  getNextApprovalNodes,
} from '#/api/bpm/processInstance';
import {
  approveTask,
  copyTask,
  delegateTask,
  getTaskListByReturn,
  rejectTask,
  returnTask,
  signCreateTask,
  signDeleteTask,
  transferTask,
} from '#/api/bpm/task';
import { setConfAndFields2 } from '#/components/form-create';
import { $t } from '#/locales';

import Signature from './signature.vue';
import ProcessInstanceTimeline from './time-line.vue';

defineOptions({ name: 'ProcessInstanceBtnContainer' });

const props = defineProps<{
  normalForm: any; // 流程表单 formCreate
  normalFormApi: any; // 流程表单 formCreate Api
  processDefinition: any; // 流程定义信息
  processInstance: any; // 流程实例信息
  userOptions: SystemUserApi.User[];
  writableFields: string[]; // 流程表单可以编辑的字段
}>(); // 当前登录的编号
const emit = defineEmits(['success']);

const [SignatureModal, signatureModalApi] = useVbenModal({
  connectedComponent: Signature,
  destroyOnClose: true,
});

const router = useRouter();
const userStore = useUserStore();
const userId = userStore.userInfo?.id;
const formLoading = ref(false); // 表单加载中
const popOverVisible: any = ref({
  approve: false,
  reject: false,
  transfer: false,
  delegate: false,
  addSign: false,
  return: false,
  copy: false,
  cancel: false,
  deleteSign: false,
}); // 气泡卡是否展示
const returnList = ref([] as any); // 退回节点

/** 创建流程表达式 */
function openSignatureModal() {
  signatureModalApi.setData(null).open();
}

// ========== 审批信息 ==========
const runningTask = ref<any>(); // 运行中的任务
const approveForm = ref<any>({}); // 审批通过时，额外的补充信息
const approveFormFApi = ref<any>({}); // approveForms 的 fAPi
const nodeTypeName = ref('审批'); // 节点类型名称

const reasonRequire = ref();
const approveFormRef = ref<FormInstance>(); // 审批通过意见表单
const approveSignFormRef = ref();
const nextAssigneesActivityNode = ref<BpmProcessInstanceApi.ApprovalNodeInfo[]>(
  [],
); // 下一个审批节点信息
const nextAssigneesTimelineRef = ref(); // 下一个节点审批人时间线组件的引用
const approveReasonForm: any = reactive({
  reason: '',
  signPicUrl: '',
  nextAssignees: {},
});
const approveReasonRule: Record<string, any> = computed(() => {
  return {
    reason: [
      {
        required: reasonRequire.value,
        message: `${nodeTypeName.value}意见不能为空`,
        trigger: 'blur',
      },
    ],
    signPicUrl: [
      { required: true, message: '签名不能为空', trigger: 'change' },
    ],
    nextAssignees: [
      { required: true, message: '审批人不能为空', trigger: 'blur' },
    ],
  };
});

const rejectFormRef = ref<FormInstance>();
const rejectReasonForm = reactive({
  reason: '',
}); // 拒绝表单
const rejectReasonRule: any = computed(() => {
  return {
    reason: [
      {
        required: reasonRequire.value,
        message: '审批意见不能为空',
        trigger: 'blur',
      },
    ],
  } as FormRules;
});

const copyFormRef = ref<FormInstance>(); // 抄送表单
const copyForm = reactive({
  copyUserIds: [],
  copyReason: '',
});
const copyFormRule: FormRules = reactive({
  copyUserIds: [
    {
      required: true,
      message: '抄送人不能为空',
      trigger: 'change',
      type: 'array',
    },
  ],
});

const transferFormRef = ref<FormInstance>(); // 转办表单
const transferForm = reactive({
  assigneeUserId: undefined,
  reason: '',
});
const transferFormRule: FormRules = reactive({
  assigneeUserId: [
    { required: true, message: '新审批人不能为空', trigger: 'change' },
  ],
  reason: [{ required: true, message: '审批意见不能为空', trigger: 'blur' }],
});

const delegateFormRef = ref<FormInstance>(); // 委派表单
const delegateForm = reactive({
  delegateUserId: undefined,
  reason: '',
});
const delegateFormRule: FormRules = reactive({
  delegateUserId: [
    { required: true, message: '接收人不能为空', trigger: 'change' },
  ],
  reason: [{ required: true, message: '审批意见不能为空', trigger: 'blur' }],
});

const addSignFormRef = ref<FormInstance>(); // 加签表单
const addSignForm = reactive({
  addSignUserIds: undefined,
  reason: '',
});
const addSignFormRule: FormRules = reactive({
  addSignUserIds: [
    {
      required: true,
      message: '加签处理人不能为空',
      trigger: 'change',
      type: 'array',
    },
  ],
  reason: [{ required: true, message: '审批意见不能为空', trigger: 'blur' }],
});

const deleteSignFormRef = ref<FormInstance>(); // 减签表单
const deleteSignForm = reactive({
  deleteSignTaskId: undefined,
  reason: '',
});
const deleteSignFormRule: FormRules = reactive({
  deleteSignTaskId: [
    { required: true, message: '减签人员不能为空', trigger: 'change' },
  ],
  reason: [{ required: true, message: '审批意见不能为空', trigger: 'blur' }],
});

const returnFormRef = ref<FormInstance>(); // 退回表单
const returnForm = reactive({
  targetTaskDefinitionKey: undefined,
  returnReason: '',
});
const returnFormRule: FormRules = reactive({
  targetTaskDefinitionKey: [
    { required: true, message: '退回节点不能为空', trigger: 'change' },
  ],
  returnReason: [
    { required: true, message: '退回理由不能为空', trigger: 'blur' },
  ],
});

const cancelFormRef = ref<FormInstance>(); // 取消表单
const cancelForm = reactive({
  cancelReason: '',
});
const cancelFormRule: FormRules = reactive({
  cancelReason: [
    { required: true, message: '取消理由不能为空', trigger: 'blur' },
  ],
});

/** 监听 approveFormFApis，实现它对应的 form-create 初始化后，隐藏掉对应的表单提交按钮 */
watch(
  () => approveFormFApi.value,
  (val) => {
    val?.btn?.show(false);
    val?.resetBtn?.show(false);
  },
  {
    deep: true,
  },
);

/** 弹出气泡卡 */
async function openPopover(type: string) {
  if (type === 'approve') {
    // 校验流程表单
    const valid = await validateNormalForm();
    if (!valid) {
      ElMessage.warning('表单校验不通过，请先完善表单!!');
      return;
    }
    await initNextAssigneesFormField();
  }
  if (type === 'return') {
    // 获取退回节点
    returnList.value = await getTaskListByReturn(runningTask.value.id);
    if (returnList.value.length === 0) {
      ElMessage.warning('当前没有可退回的节点');
      return;
    }
  }
  Object.keys(popOverVisible.value).forEach((item) => {
    if (popOverVisible.value[item]) popOverVisible.value[item] = item === type;
  });
}

/** 关闭气泡卡 */
function closePopover(type: string, formRef: any | FormInstance) {
  if (formRef) {
    formRef.resetFields();
  }
  if (popOverVisible.value[type]) popOverVisible.value[type] = false;
  nextAssigneesActivityNode.value = [];
  // 清理 Timeline 组件中的自定义审批人数据
  if (nextAssigneesTimelineRef.value) {
    nextAssigneesTimelineRef.value.batchSetCustomApproveUsers({});
  }
}

/** 流程通过时，根据表单变量查询新的流程节点，判断下一个节点类型是否为自选审批人 */
async function initNextAssigneesFormField() {
  // 获取修改的流程变量, 暂时只支持流程表单
  const variables = getUpdatedProcessInstanceVariables();
  const data = await getNextApprovalNodes({
    processInstanceId: props.processInstance.id,
    taskId: runningTask.value.id,
    processVariablesStr: JSON.stringify(variables),
  });
  if (data && data.length > 0) {
    const customApproveUsersData: Record<string, any[]> = {}; // 用于收集需要设置到 Timeline 组件的自定义审批人数据
    data.forEach((node: BpmProcessInstanceApi.ApprovalNodeInfo) => {
      if (
        // 情况一：当前节点没有审批人，并且是发起人自选
        (isEmpty(node.tasks) &&
          isEmpty(node.candidateUsers) &&
          BpmCandidateStrategyEnum.START_USER_SELECT ===
            node.candidateStrategy) ||
        // 情况二：当前节点是审批人自选
        BpmCandidateStrategyEnum.APPROVE_USER_SELECT === node.candidateStrategy
      ) {
        nextAssigneesActivityNode.value.push(node);
      }

      // 如果节点有 candidateUsers，设置到 customApproveUsers 中
      if (node.candidateUsers && node.candidateUsers.length > 0) {
        customApproveUsersData[node.id] = node.candidateUsers;
      }
    });

    // 将 candidateUsers 设置到 Timeline 组件中
    await nextTick(); // 等待下一个 tick，确保 Timeline 组件已经渲染
    if (
      nextAssigneesTimelineRef.value &&
      Object.keys(customApproveUsersData).length > 0
    ) {
      nextAssigneesTimelineRef.value.batchSetCustomApproveUsers(
        customApproveUsersData,
      );
    }
  }
}

/** 选择下一个节点的审批人 */
function selectNextAssigneesConfirm(id: string, userList: any[]) {
  approveReasonForm.nextAssignees[id] = userList?.map((item: any) => item.id);
}

/** 审批通过时，校验每个自选审批人的节点是否都已配置了审批人 */
function validateNextAssignees() {
  if (Object.keys(nextAssigneesActivityNode.value).length === 0) {
    return true;
  }
  // 如果需要自选审批人，则校验每个节点是否都已配置审批人
  for (const item of nextAssigneesActivityNode.value) {
    if (isEmpty(approveReasonForm.nextAssignees[item.id])) {
      ElMessage.warning('下一个节点的审批人不能为空!');
      return false;
    }
  }
  return true;
}

/** 处理审批通过和不通过的操作 */
async function handleAudit(pass: boolean, formRef: FormInstance | undefined) {
  formLoading.value = true;
  try {
    // 校验表单
    if (!formRef) return;
    await formRef?.validate();
    // 校验流程表单必填字段
    const valid = await validateNormalForm();
    if (!valid) {
      ElMessage.warning('表单校验不通过，请先完善表单!!');
      return;
    }

    if (pass) {
      const nextAssigneesValid = validateNextAssignees();
      if (!nextAssigneesValid) return;
      const variables = getUpdatedProcessInstanceVariables();
      // 审批通过数据
      const data = {
        id: runningTask.value.id,
        reason: approveReasonForm.reason,
        variables, // 审批通过, 把修改的字段值赋于流程实例变量
        nextAssignees: approveReasonForm.nextAssignees, // 下个自选节点选择的审批人信息
      } as any;
      // 签名
      if (runningTask.value.signEnable) {
        data.signPicUrl = approveReasonForm.signPicUrl;
      }
      // 多表单处理，并且有额外的 approveForm 表单，需要校验 + 拼接到 data 表单里提交
      // TODO 芋艿 任务有多表单这里要如何处理，会和可编辑的字段冲突
      const formCreateApi = approveFormFApi.value;
      if (Object.keys(formCreateApi)?.length > 0) {
        await formCreateApi.validate();
        data.variables = approveForm.value.value;
      }
      await approveTask(data);
      popOverVisible.value.approve = false;
      nextAssigneesActivityNode.value = [];
      // 清理 Timeline 组件中的自定义审批人数据
      if (nextAssigneesTimelineRef.value) {
        nextAssigneesTimelineRef.value.batchSetCustomApproveUsers({});
      }
      ElMessage.success('审批通过成功');
    } else {
      // 审批不通过数据
      const data = {
        id: runningTask.value.id,
        reason: rejectReasonForm.reason,
      };
      await rejectTask(data);
      popOverVisible.value.reject = false;
      ElMessage.success('审批不通过成功');
    }
    // 重置表单
    formRef.resetFields();
    // 加载最新数据
    reload();
  } finally {
    formLoading.value = false;
  }
}

/** 处理抄送 */
async function handleCopy() {
  formLoading.value = true;
  try {
    // 1. 校验表单
    if (!copyFormRef.value) return;
    await copyFormRef.value.validate();
    // 2. 提交抄送
    const data = {
      id: runningTask.value.id,
      reason: copyForm.copyReason,
      copyUserIds: copyForm.copyUserIds,
    };
    await copyTask(data);
    copyFormRef.value.resetFields();
    popOverVisible.value.copy = false;
    ElMessage.success($t('ui.actionMessage.operationSuccess'));
  } finally {
    formLoading.value = false;
  }
}

/** 处理转交 */
async function handleTransfer() {
  formLoading.value = true;
  try {
    // 1.1 校验表单
    if (!transferFormRef.value) {
      return;
    }
    await transferFormRef.value.validate();
    // 1.2 提交转交
    const data = {
      id: runningTask.value.id,
      reason: transferForm.reason,
      assigneeUserId: transferForm.assigneeUserId,
    };
    await transferTask(data);
    transferFormRef.value.resetFields();
    popOverVisible.value.transfer = false;
    ElMessage.success($t('ui.actionMessage.operationSuccess'));
    // 2. 加载最新数据
    reload();
  } finally {
    formLoading.value = false;
  }
}

/** 处理委派 */
async function handleDelegate() {
  formLoading.value = true;
  try {
    // 1.1 校验表单
    if (!delegateFormRef.value) return;
    await delegateFormRef.value.validate();
    // 1.2 处理委派
    const data = {
      id: runningTask.value.id,
      reason: delegateForm.reason,
      delegateUserId: delegateForm.delegateUserId,
    };
    await delegateTask(data);
    popOverVisible.value.delegate = false;
    delegateFormRef.value.resetFields();
    ElMessage.success($t('ui.actionMessage.operationSuccess'));
    // 2. 加载最新数据
    reload();
  } finally {
    formLoading.value = false;
  }
}

/** 处理加签 */
async function handlerAddSign(type: string) {
  formLoading.value = true;
  try {
    // 1.1 校验表单
    if (!addSignFormRef.value) return;
    await addSignFormRef.value.validate();
    // 1.2 提交加签
    const data = {
      id: runningTask.value.id,
      type,
      reason: addSignForm.reason,
      userIds: addSignForm.addSignUserIds,
    };
    await signCreateTask(data);
    ElMessage.success($t('ui.actionMessage.operationSuccess'));
    addSignFormRef.value.resetFields();
    popOverVisible.value.addSign = false;
    // 2 加载最新数据
    reload();
  } finally {
    formLoading.value = false;
  }
}

/** 处理退回 */
async function handleReturn() {
  formLoading.value = true;
  try {
    // 1.1 校验表单
    if (!returnFormRef.value) return;
    await returnFormRef.value.validate();
    // 1.2 提交退回
    const data = {
      id: runningTask.value.id,
      reason: returnForm.returnReason,
      targetTaskDefinitionKey: returnForm.targetTaskDefinitionKey,
    };
    await returnTask(data);
    popOverVisible.value.return = false;
    returnFormRef.value.resetFields();
    ElMessage.success($t('ui.actionMessage.operationSuccess'));
    // 2 重新加载数据
    reload();
  } finally {
    formLoading.value = false;
  }
}

/** 处理取消 */
async function handleCancel() {
  formLoading.value = true;
  try {
    // 1.1 校验表单
    if (!cancelFormRef.value) return;
    await cancelFormRef.value.validate();
    // 1.2 提交取消
    await cancelProcessInstanceByStartUser(
      props.processInstance.id,
      cancelForm.cancelReason,
    );
    popOverVisible.value.return = false;
    ElMessage.success($t('ui.actionMessage.operationSuccess'));
    cancelFormRef.value.resetFields();
    // 2 重新加载数据
    reload();
  } finally {
    formLoading.value = false;
  }
}

/** 处理再次提交 */
async function handleReCreate() {
  // 跳转发起流程界面
  await router.push({
    name: 'BpmProcessInstanceCreate',
    query: { processInstanceId: props.processInstance?.id },
  });
}

/** 获取减签人员标签 */
function getDeleteSignUserLabel(task: any): string {
  const deptName = task?.assigneeUser?.deptName || task?.ownerUser?.deptName;
  const nickname = task?.assigneeUser?.nickname || task?.ownerUser?.nickname;
  return `${nickname} ( 所属部门：${deptName} )`;
}

/** 处理减签 */
async function handlerDeleteSign() {
  formLoading.value = true;
  try {
    // 1.1 校验表单
    if (!deleteSignFormRef.value) return;
    await deleteSignFormRef.value?.validate();
    // 1.2 提交减签
    const data = {
      id: deleteSignForm.deleteSignTaskId,
      reason: deleteSignForm.reason,
    };
    await signDeleteTask(data);
    ElMessage.success('减签成功');
    deleteSignFormRef.value.resetFields();
    popOverVisible.value.deleteSign = false;
    // 2 加载最新数据
    reload();
  } finally {
    formLoading.value = false;
  }
}

/** 重新加载数据 */
function reload() {
  emit('success');
}

/** 任务是否为处理中状态 */
function isHandleTaskStatus() {
  let canHandle = false;
  if (BpmTaskStatusEnum.RUNNING === runningTask.value?.status) {
    canHandle = true;
  }
  return canHandle;
}

/** 流程状态是否为结束状态 */
function isEndProcessStatus(status: number) {
  let isEndStatus = false;
  if (
    BpmProcessInstanceStatus.APPROVE === status ||
    BpmProcessInstanceStatus.REJECT === status ||
    BpmProcessInstanceStatus.CANCEL === status
  ) {
    isEndStatus = true;
  }
  return isEndStatus;
}

/** 是否显示按钮 */
function isShowButton(btnType: BpmTaskOperationButtonTypeEnum): boolean {
  let isShow = true;
  if (
    runningTask.value?.buttonsSetting &&
    runningTask.value?.buttonsSetting[btnType]
  ) {
    isShow = runningTask.value.buttonsSetting[btnType].enable;
  }
  return isShow;
}

/** 获取按钮的显示名称 */
function getButtonDisplayName(btnType: BpmTaskOperationButtonTypeEnum) {
  let displayName = OPERATION_BUTTON_NAME.get(btnType);
  if (
    runningTask.value?.buttonsSetting &&
    runningTask.value?.buttonsSetting[btnType]
  ) {
    displayName = runningTask.value.buttonsSetting[btnType].displayName;
  }
  return displayName;
}

/** 加载待办任务 */
function loadTodoTask(task: any) {
  approveForm.value = {};
  runningTask.value = task;
  approveFormFApi.value = {};
  reasonRequire.value = task?.reasonRequire ?? false;
  nodeTypeName.value =
    task?.nodeType === BpmNodeTypeEnum.TRANSACTOR_NODE ? '办理' : '审批';
  // 处理 approve 表单
  if (task && task.formId && task.formConf) {
    const tempApproveForm = {};
    setConfAndFields2(
      tempApproveForm,
      task.formConf,
      task.formFields,
      task.formVariables,
    );
    approveForm.value = tempApproveForm;
  } else {
    approveForm.value = {}; // 占位，避免为空
  }
}

/** 校验流程表单 */
async function validateNormalForm() {
  if (props.processDefinition?.formType === BpmModelFormType.NORMAL) {
    let valid = true;
    try {
      await props.normalFormApi?.validate();
    } catch {
      valid = false;
    }
    return valid;
  } else {
    return true;
  }
}

/** 从可以编辑的流程表单字段，获取需要修改的流程实例的变量 */
function getUpdatedProcessInstanceVariables() {
  const variables: any = {};
  props.writableFields.forEach((field: string) => {
    variables[field] = props.normalFormApi.getValue(field);
  });
  return variables;
}

/** 处理签名完成 */
function handleSignFinish(url: string) {
  approveReasonForm.signPicUrl = url;
  approveFormRef.value?.validateField('signPicUrl');
}

/** 处理弹窗可见性 */
function handlePopoverVisible(visible: boolean) {
  if (!visible) {
    // 拦截关闭事件
    popOverVisible.value.approve = true;
  }
}

defineExpose({ loadTodoTask });
</script>
<template>
  <div class="flex items-center">
    <!-- 【通过】按钮 -->
    <!-- z-index 设置为300 避免覆盖签名弹窗 -->
    <ElSpace size="default">
      <ElPopover
        v-model:visible="popOverVisible.approve"
        placement="top"
        :popper-style="{ minWidth: '400px', zIndex: 300 }"
        trigger="click"
        @open-change="handlePopoverVisible"
        v-if="
          runningTask &&
          isHandleTaskStatus() &&
          isShowButton(BpmTaskOperationButtonTypeEnum.APPROVE)
        "
      >
        <template #reference>
          <ElButton plain type="primary" @click="openPopover('approve')">
            <IconifyIcon icon="lucide:check" />
            <span class="ml-1">{{
              getButtonDisplayName(BpmTaskOperationButtonTypeEnum.APPROVE)
            }}</span>
          </ElButton>
        </template>
        <!-- 办理表单 -->
        <div class="flex flex-1 flex-col px-5 pt-5" v-loading="formLoading">
          <ElForm
            label-position="top"
            class="mb-auto"
            ref="approveFormRef"
            :model="approveReasonForm"
            :rules="approveReasonRule"
            label-width="100px"
          >
            <ElCard v-if="runningTask?.formId > 0" class="!-mt-2.5 mb-3.5">
              <template #title>
                <span class="el-icon-picture-outline">
                  填写表单【{{ runningTask?.formName }}】
                </span>
              </template>
              <FormCreate
                v-model:value="approveForm.value"
                v-model:api="approveFormFApi"
                :option="approveForm.option"
                :rule="approveForm.rule"
              />
            </ElCard>

            <ElFormItem
              label="下一个节点的审批人"
              prop="nextAssignees"
              v-if="nextAssigneesActivityNode.length > 0"
            >
              <div class="-mb-8 -mt-3.5 ml-2.5">
                <ProcessInstanceTimeline
                  ref="nextAssigneesTimelineRef"
                  :activity-nodes="nextAssigneesActivityNode"
                  :show-status-icon="false"
                  :enable-approve-user-select="true"
                  @select-user-confirm="selectNextAssigneesConfirm"
                />
              </div>
            </ElFormItem>
            <ElFormItem
              v-if="runningTask.signEnable"
              label="签名"
              prop="signPicUrl"
              ref="approveSignFormRef"
            >
              <div class="flex items-center gap-2">
                <ElButton @click="openSignatureModal" type="primary">
                  {{ approveReasonForm.signPicUrl ? '重新签名' : '点击签名' }}
                </ElButton>
                <ElImage
                  class="!h-10 !w-40 object-contain"
                  v-if="approveReasonForm.signPicUrl"
                  :src="approveReasonForm.signPicUrl"
                />
              </div>
            </ElFormItem>
            <ElFormItem :label="`${nodeTypeName}意见`" prop="reason">
              <ElInput
                type="textarea"
                v-model="approveReasonForm.reason"
                :placeholder="`请输入${nodeTypeName}意见`"
                :rows="4"
              />
            </ElFormItem>
            <ElFormItem>
              <ElSpace>
                <ElButton
                  :disabled="formLoading"
                  type="primary"
                  @click="handleAudit(true, approveFormRef)"
                >
                  {{
                    getButtonDisplayName(BpmTaskOperationButtonTypeEnum.APPROVE)
                  }}
                </ElButton>
                <ElButton @click="closePopover('approve', approveFormRef)">
                  取消
                </ElButton>
              </ElSpace>
            </ElFormItem>
          </ElForm>
        </div>
      </ElPopover>

      <!-- 【拒绝】按钮 -->
      <ElPopover
        v-model:visible="popOverVisible.reject"
        placement="top"
        :popper-style="{ minWidth: '400px' }"
        trigger="click"
        v-if="
          runningTask &&
          isHandleTaskStatus() &&
          isShowButton(BpmTaskOperationButtonTypeEnum.REJECT)
        "
      >
        <template #reference>
          <ElButton plain danger type="primary" @click="openPopover('reject')">
            <IconifyIcon icon="lucide:x" />
            <span class="ml-1">{{
              getButtonDisplayName(BpmTaskOperationButtonTypeEnum.REJECT)
            }}</span>
          </ElButton>
        </template>
        <!-- 审批表单 -->
        <div class="flex flex-1 flex-col px-5 pt-5" v-loading="formLoading">
          <ElForm
            label-position="top"
            class="mb-auto"
            ref="rejectFormRef"
            :model="rejectReasonForm"
            :rules="rejectReasonRule"
            label-width="100px"
          >
            <ElFormItem label="审批意见" prop="reason">
              <ElInput
                type="textarea"
                v-model="rejectReasonForm.reason"
                placeholder="请输入审批意见"
                :rows="4"
              />
            </ElFormItem>
            <ElFormItem>
              <ElButton
                :disabled="formLoading"
                danger
                type="primary"
                @click="handleAudit(false, rejectFormRef)"
              >
                {{
                  getButtonDisplayName(BpmTaskOperationButtonTypeEnum.REJECT)
                }}
              </ElButton>
              <ElButton
                class="ml-2"
                @click="closePopover('reject', rejectFormRef)"
              >
                取消
              </ElButton>
            </ElFormItem>
          </ElForm>
        </div>
      </ElPopover>

      <!-- 【抄送】按钮 -->
      <ElPopover
        v-model:visible="popOverVisible.copy"
        placement="top"
        :popper-style="{ width: '400px' }"
        trigger="click"
        v-if="
          runningTask &&
          isHandleTaskStatus() &&
          isShowButton(BpmTaskOperationButtonTypeEnum.COPY)
        "
      >
        <template #reference>
          <ElButton plain @click="openPopover('copy')">
            <IconifyIcon icon="lucide:copy" />
            <span class="ml-1">{{
              getButtonDisplayName(BpmTaskOperationButtonTypeEnum.COPY)
            }}</span>
          </ElButton>
        </template>
        <div class="flex flex-1 flex-col px-5 pt-5" v-loading="formLoading">
          <ElForm
            label-position="top"
            class="mb-auto"
            ref="copyFormRef"
            :model="copyForm"
            :rules="copyFormRule"
            label-width="100px"
          >
            <ElFormItem label="抄送人" prop="copyUserIds">
              <ElSelect
                v-model="copyForm.copyUserIds"
                clearable
                multiple
                placeholder="请选择抄送人"
                class="w-full"
              >
                <ElOption
                  v-for="item in userOptions"
                  :key="item.id"
                  :label="item.nickname"
                  :value="item.id!"
                >
                  {{ item.nickname }}
                </ElOption>
              </ElSelect>
            </ElFormItem>
            <ElFormItem label="抄送意见" prop="copyReason">
              <ElInput
                type="textarea"
                v-model="copyForm.copyReason"
                placeholder="请输入抄送意见"
                :rows="3"
              />
            </ElFormItem>
            <ElFormItem>
              <ElSpace>
                <ElButton
                  :disabled="formLoading"
                  type="primary"
                  @click="handleCopy"
                >
                  {{
                    getButtonDisplayName(BpmTaskOperationButtonTypeEnum.COPY)
                  }}
                </ElButton>
                <ElButton @click="closePopover('copy', copyFormRef)">
                  取消
                </ElButton>
              </ElSpace>
            </ElFormItem>
          </ElForm>
        </div>
      </ElPopover>

      <!-- 【转办】按钮 -->
      <ElPopover
        v-model:visible="popOverVisible.transfer"
        placement="top"
        :popper-style="{ width: '400px' }"
        trigger="click"
        v-if="
          runningTask &&
          isHandleTaskStatus() &&
          isShowButton(BpmTaskOperationButtonTypeEnum.TRANSFER)
        "
      >
        <template #reference>
          <ElButton plain @click="openPopover('transfer')">
            <IconifyIcon icon="icon-park-outline:share-two" />
            <span class="ml-1">{{
              getButtonDisplayName(BpmTaskOperationButtonTypeEnum.TRANSFER)
            }}</span>
          </ElButton>
        </template>
        <div class="flex flex-1 flex-col px-5 pt-5" v-loading="formLoading">
          <ElForm
            label-position="top"
            class="mb-auto"
            ref="transferFormRef"
            :model="transferForm"
            :rules="transferFormRule"
            label-width="100px"
          >
            <ElFormItem label="新审批人" prop="assigneeUserId">
              <ElSelect
                v-model="transferForm.assigneeUserId"
                clearable
                style="width: 100%"
              >
                <ElOption
                  v-for="item in userOptions"
                  :key="item.id"
                  :label="item.nickname"
                  :value="item.id!"
                >
                  {{ item.nickname }}
                </ElOption>
              </ElSelect>
            </ElFormItem>
            <ElFormItem label="审批意见" prop="reason">
              <ElInput
                type="textarea"
                v-model="transferForm.reason"
                clearable
                placeholder="请输入审批意见"
                :rows="3"
              />
            </ElFormItem>
            <ElFormItem>
              <ElSpace>
                <ElButton
                  :disabled="formLoading"
                  type="primary"
                  @click="handleTransfer()"
                >
                  {{
                    getButtonDisplayName(
                      BpmTaskOperationButtonTypeEnum.TRANSFER,
                    )
                  }}
                </ElButton>
                <ElButton @click="closePopover('transfer', transferFormRef)">
                  取消
                </ElButton>
              </ElSpace>
            </ElFormItem>
          </ElForm>
        </div>
      </ElPopover>

      <!-- 【委派】按钮 -->
      <ElPopover
        v-model:visible="popOverVisible.delegate"
        placement="top"
        :popper-style="{ width: '400px' }"
        trigger="click"
        v-if="
          runningTask &&
          isHandleTaskStatus() &&
          isShowButton(BpmTaskOperationButtonTypeEnum.DELEGATE)
        "
      >
        <template #reference>
          <ElButton plain @click="openPopover('delegate')">
            <IconifyIcon :size="14" icon="icon-park-outline:user-positioning" />
            <span class="ml-1">{{
              getButtonDisplayName(BpmTaskOperationButtonTypeEnum.DELEGATE)
            }}</span>
          </ElButton>
        </template>
        <div class="flex flex-1 flex-col px-5 pt-5" v-loading="formLoading">
          <ElForm
            label-position="top"
            class="mb-auto"
            ref="delegateFormRef"
            :model="delegateForm"
            :rules="delegateFormRule"
            label-width="100px"
          >
            <ElFormItem label="接收人" prop="delegateUserId">
              <ElSelect
                v-model="delegateForm.delegateUserId"
                clearable
                style="width: 100%"
              >
                <ElOption
                  v-for="item in userOptions"
                  :key="item.id"
                  :label="item.nickname"
                  :value="item.id!"
                >
                  {{ item.nickname }}
                </ElOption>
              </ElSelect>
            </ElFormItem>
            <ElFormItem label="审批意见" prop="reason">
              <ElInput
                type="textarea"
                v-model="delegateForm.reason"
                clearable
                placeholder="请输入审批意见"
                :rows="3"
              />
            </ElFormItem>
            <ElFormItem>
              <ElSpace>
                <ElButton
                  :disabled="formLoading"
                  type="primary"
                  @click="handleDelegate()"
                >
                  {{
                    getButtonDisplayName(
                      BpmTaskOperationButtonTypeEnum.DELEGATE,
                    )
                  }}
                </ElButton>
                <ElButton @click="closePopover('delegate', delegateFormRef)">
                  取消
                </ElButton>
              </ElSpace>
            </ElFormItem>
          </ElForm>
        </div>
      </ElPopover>

      <!-- 【加签】按钮 当前任务审批人为A，向前加签选了一个C，则需要C先审批，然后再是A审批，向后加签B，A审批完，需要B再审批完，才算完成这个任务节点 -->
      <ElPopover
        v-model:visible="popOverVisible.addSign"
        placement="top"
        :popper-style="{ width: '400px' }"
        trigger="click"
        v-if="
          runningTask &&
          isHandleTaskStatus() &&
          isShowButton(BpmTaskOperationButtonTypeEnum.ADD_SIGN)
        "
      >
        <template #reference>
          <ElButton plain @click="openPopover('addSign')">
            <IconifyIcon :size="14" icon="icon-park-outline:plus" />
            <span class="ml-1">{{
              getButtonDisplayName(BpmTaskOperationButtonTypeEnum.ADD_SIGN)
            }}</span>
          </ElButton>
        </template>
        <div class="flex flex-1 flex-col px-5 pt-5" v-loading="formLoading">
          <ElForm
            label-position="top"
            class="mb-auto"
            ref="addSignFormRef"
            :model="addSignForm"
            :rules="addSignFormRule"
            label-width="100px"
          >
            <ElFormItem label="加签处理人" prop="addSignUserIds">
              <ElSelect
                v-model="addSignForm.addSignUserIds"
                clearable
                multiple
                style="width: 100%"
              >
                <ElOption
                  v-for="item in userOptions"
                  :key="item.id"
                  :label="item.nickname"
                  :value="item.id!"
                >
                  {{ item.nickname }}
                </ElOption>
              </ElSelect>
            </ElFormItem>
            <ElFormItem label="审批意见" prop="reason">
              <ElInput
                type="textarea"
                v-model="addSignForm.reason"
                clearable
                placeholder="请输入审批意见"
                :rows="3"
              />
            </ElFormItem>
            <ElFormItem>
              <ElSpace>
                <ElButton
                  :disabled="formLoading"
                  type="primary"
                  @click="handlerAddSign('before')"
                >
                  向前{{
                    getButtonDisplayName(
                      BpmTaskOperationButtonTypeEnum.ADD_SIGN,
                    )
                  }}
                </ElButton>
                <ElButton
                  :disabled="formLoading"
                  type="primary"
                  @click="handlerAddSign('after')"
                >
                  向后{{
                    getButtonDisplayName(
                      BpmTaskOperationButtonTypeEnum.ADD_SIGN,
                    )
                  }}
                </ElButton>
                <ElButton @click="closePopover('addSign', addSignFormRef)">
                  取消
                </ElButton>
              </ElSpace>
            </ElFormItem>
          </ElForm>
        </div>
      </ElPopover>

      <!-- 【减签】按钮 -->
      <ElPopover
        v-model:visible="popOverVisible.deleteSign"
        placement="top"
        :popper-style="{ width: '400px' }"
        trigger="click"
        v-if="runningTask?.children.length > 0"
      >
        <template #reference>
          <ElButton plain @click="openPopover('deleteSign')">
            <IconifyIcon :size="14" icon="icon-park-outline:minus" />
            <span class="ml-1">减签</span>
          </ElButton>
        </template>
        <div class="flex flex-1 flex-col px-5 pt-5" v-loading="formLoading">
          <ElForm
            label-position="top"
            class="mb-auto"
            ref="deleteSignFormRef"
            :model="deleteSignForm"
            :rules="deleteSignFormRule"
            label-width="100px"
          >
            <ElFormItem label="减签人员" prop="deleteSignTaskId">
              <ElSelect
                v-model="deleteSignForm.deleteSignTaskId"
                clearable
                style="width: 100%"
              >
                <ElOption
                  v-for="item in runningTask.children"
                  :key="item.id"
                  :label="getDeleteSignUserLabel(item)"
                  :value="item.id"
                >
                  {{ getDeleteSignUserLabel(item) }}
                </ElOption>
              </ElSelect>
            </ElFormItem>
            <ElFormItem label="审批意见" prop="reason">
              <ElInput
                type="textarea"
                v-model="deleteSignForm.reason"
                clearable
                placeholder="请输入审批意见"
                :rows="3"
              />
            </ElFormItem>
            <ElFormItem>
              <ElSpace>
                <ElButton
                  :disabled="formLoading"
                  type="primary"
                  @click="handlerDeleteSign()"
                >
                  减签
                </ElButton>
                <ElButton
                  @click="closePopover('deleteSign', deleteSignFormRef)"
                >
                  取消
                </ElButton>
              </ElSpace>
            </ElFormItem>
          </ElForm>
        </div>
      </ElPopover>

      <!-- 【退回】按钮 -->
      <ElPopover
        v-model:visible="popOverVisible.return"
        placement="top"
        :popper-style="{ width: '400px' }"
        trigger="click"
        v-if="
          runningTask &&
          isHandleTaskStatus() &&
          isShowButton(BpmTaskOperationButtonTypeEnum.RETURN)
        "
      >
        <template #reference>
          <ElButton plain @click="openPopover('return')">
            <IconifyIcon :size="14" icon="lucide:arrow-left" />
            <span class="ml-1">{{
              getButtonDisplayName(BpmTaskOperationButtonTypeEnum.RETURN)
            }}</span>
          </ElButton>
        </template>
        <div class="flex flex-1 flex-col px-5 pt-5" v-loading="formLoading">
          <ElForm
            label-position="top"
            class="mb-auto"
            ref="returnFormRef"
            :model="returnForm"
            :rules="returnFormRule"
            label-width="100px"
          >
            <ElFormItem label="退回节点" prop="targetTaskDefinitionKey">
              <ElSelect
                v-model="returnForm.targetTaskDefinitionKey"
                clearable
                style="width: 100%"
              >
                <ElOption
                  v-for="item in returnList"
                  :key="item.taskDefinitionKey"
                  :label="item.name"
                  :value="item.taskDefinitionKey"
                >
                  {{ item.name }}
                </ElOption>
              </ElSelect>
            </ElFormItem>
            <ElFormItem label="退回理由" prop="returnReason">
              <ElInput
                type="textarea"
                v-model="returnForm.returnReason"
                clearable
                placeholder="请输入退回理由"
                :rows="3"
              />
            </ElFormItem>
            <ElFormItem>
              <ElSpace>
                <ElButton
                  :disabled="formLoading"
                  type="primary"
                  @click="handleReturn()"
                >
                  {{
                    getButtonDisplayName(BpmTaskOperationButtonTypeEnum.RETURN)
                  }}
                </ElButton>
                <ElButton @click="closePopover('return', returnFormRef)">
                  取消
                </ElButton>
              </ElSpace>
            </ElFormItem>
          </ElForm>
        </div>
      </ElPopover>

      <!--【取消】按钮 这个对应发起人的取消, 只有发起人可以取消 -->
      <ElPopover
        v-model:visible="popOverVisible.cancel"
        placement="top"
        :popper-style="{ width: '460px' }"
        trigger="click"
        v-if="
          userId === processInstance?.startUser?.id &&
          !isEndProcessStatus(processInstance?.status)
        "
      >
        <template #reference>
          <ElButton plain @click="openPopover('cancel')">
            <IconifyIcon :size="14" icon="icon-park-outline:back" />
            <span class="ml-1">取消</span>
          </ElButton>
        </template>
        <div
          class="flex w-full flex-1 flex-col px-5 pt-5"
          v-loading="formLoading"
        >
          <ElForm
            label-position="top"
            class="mb-auto"
            ref="cancelFormRef"
            :model="cancelForm"
            :rules="cancelFormRule"
            label-width="100px"
          >
            <ElFormItem label="取消理由" prop="cancelReason">
              <ElAlert
                class="!mb-2 text-xs"
                type="warning"
                size="small"
                show-icon
                title="友情提醒：取消后，该审批流程将自动结束。"
              />
              <ElInput
                type="textarea"
                v-model="cancelForm.cancelReason"
                clearable
                placeholder="请输入取消理由"
                :rows="3"
              />
            </ElFormItem>
            <ElFormItem>
              <ElSpace>
                <ElButton
                  :disabled="formLoading"
                  type="primary"
                  @click="handleCancel()"
                >
                  确认
                </ElButton>

                <ElButton @click="closePopover('cancel', cancelFormRef)">
                  取消
                </ElButton>
              </ElSpace>
            </ElFormItem>
          </ElForm>
        </div>
      </ElPopover>
      <!-- 【再次提交】 按钮-->
      <ElButton
        plain
        @click="handleReCreate()"
        v-if="
          userId === processInstance?.startUser?.id &&
          isEndProcessStatus(processInstance?.status) &&
          processDefinition?.formType === 10
        "
      >
        <IconifyIcon :size="14" icon="lucide:refresh-cw" />
        <span class="ml-1">再次提交</span>
      </ElButton>
    </ElSpace>
  </div>

  <!-- 签名弹窗 -->
  <SignatureModal @success="handleSignFinish" />
</template>
