<script lang="ts" setup>
import type { BpmOALeaveApi } from '#/api/bpm/oa/leave';
import type { BpmProcessInstanceApi } from '#/api/bpm/processInstance';

import { computed, onMounted, ref, watch } from 'vue';

import { confirm, Page, useVbenForm } from '@vben/common-ui';
import { BpmCandidateStrategyEnum, BpmNodeIdEnum } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';

import { Button, Card, message, Space } from 'ant-design-vue';
import dayjs from 'dayjs';

import { getProcessDefinition } from '#/api/bpm/definition';
import { createLeave, updateLeave } from '#/api/bpm/oa/leave';
import { getApprovalDetail as getApprovalDetailApi } from '#/api/bpm/processInstance';
import { $t } from '#/locales';
import { router } from '#/router';
import ProcessInstanceTimeline from '#/views/bpm/processInstance/detail/modules/time-line.vue';

import { useFormSchema } from './data';

const formLoading = ref(false); // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用

// 审批相关：变量
const processDefineKey = 'oa_leave'; // 流程定义 Key
const startUserSelectTasks = ref<any>([]); // 发起人需要选择审批人的用户任务列表
const startUserSelectAssignees = ref<any>({}); // 发起人选择审批人的数据
const tempStartUserSelectAssignees = ref<any>({}); // 历史发起人选择审批人的数据，用于每次表单变更时，临时保存
const activityNodes = ref<BpmProcessInstanceApi.ApprovalNodeInfo[]>([]); // 审批节点信息
const processDefinitionId = ref('');

const formData = ref<BpmOALeaveApi.Leave>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['请假'])
    : $t('ui.actionTitle.create', ['请假']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 100,
  },
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
});

/** 提交申请 */
async function onSubmit() {
  const { valid } = await formApi.validate();
  if (!valid) {
    return;
  }

  // 1.2 审批相关：校验指定审批人
  if (startUserSelectTasks.value?.length > 0) {
    for (const userTask of startUserSelectTasks.value) {
      if (
        Array.isArray(startUserSelectAssignees.value[userTask.id]) &&
        startUserSelectAssignees.value[userTask.id].length === 0
      ) {
        return message.warning(`请选择${userTask.name}的审批人`);
      }
    }
  }

  // 提交表单
  const data = (await formApi.getValues()) as BpmOALeaveApi.Leave;

  // 审批相关：设置指定审批人
  if (startUserSelectTasks.value?.length > 0) {
    data.startUserSelectAssignees = startUserSelectAssignees.value;
  }

  // 格式化开始时间和结束时间的值
  const submitData: BpmOALeaveApi.Leave = {
    ...data,
    startTime: Number(data.startTime),
    endTime: Number(data.endTime),
  };

  try {
    formLoading.value = true;
    await (formData.value?.id
      ? updateLeave(submitData)
      : createLeave(submitData));
    // 关闭并提示
    message.success({
      content: $t('ui.actionMessage.operationSuccess'),
      key: 'action_process_msg',
    });

    // TODO @ziye、@jason：好像跳转不了？
    await router.push({
      name: 'BpmOALeave',
    });
  } catch (error: any) {
    message.error(error.message);
  } finally {
    formLoading.value = false;
  }
}

/** 返回上一页 */
function onBack() {
  confirm({
    content: '确定要返回上一页吗？请先保存您填写的信息！',
    icon: 'warning',
    beforeClose({ isConfirm }) {
      if (isConfirm) {
        // TODO @ziye、@jason：是不是要关闭当前标签哈。
        router.back();
      }
      return Promise.resolve(true);
    },
  });
}

// ============================== 审核流程相关 ==============================

/** 审批相关：获取审批详情 */
async function getApprovalDetail() {
  try {
    const data = await getApprovalDetailApi({
      processDefinitionId: processDefinitionId.value,
      // TODO 小北：可以支持 processDefinitionKey 查询
      activityId: BpmNodeIdEnum.START_USER_NODE_ID,
      processVariablesStr: JSON.stringify({
        day: dayjs(formData.value?.startTime).diff(
          dayjs(formData.value?.endTime),
          'day',
        ),
      }), // 解决 GET 无法传递对象的问题，后端 String 再转 JSON
    });

    if (!data) {
      message.error('查询不到审批详情信息！');
      return;
    }
    // 获取审批节点，显示 Timeline 的数据
    activityNodes.value = data.activityNodes;

    // 获取发起人自选的任务
    startUserSelectTasks.value = data.activityNodes?.filter(
      (node: BpmProcessInstanceApi.ApprovalNodeInfo) =>
        BpmCandidateStrategyEnum.START_USER_SELECT === node.candidateStrategy,
    );
    // 恢复之前的选择审批人
    if (startUserSelectTasks.value?.length > 0) {
      for (const node of startUserSelectTasks.value) {
        startUserSelectAssignees.value[node.id] =
          tempStartUserSelectAssignees.value[node.id] &&
          tempStartUserSelectAssignees.value[node.id].length > 0
            ? tempStartUserSelectAssignees.value[node.id]
            : [];
      }
    }
  } finally {
    //
  }
}
/** 审批相关：选择发起人 */
function selectUserConfirm(id: string, userList: any[]) {
  startUserSelectAssignees.value[id] = userList?.map((item: any) => item.id);
}

/** 审批相关：预测流程节点会因为输入的参数值而产生新的预测结果值，所以需重新预测一次, formData.value可改成实际业务中的特定字段 */
watch(
  formData.value as object,
  (newValue, oldValue) => {
    if (!oldValue) {
      return;
    }
    if (newValue && Object.keys(newValue).length > 0) {
      // 记录之前的节点审批人
      tempStartUserSelectAssignees.value = startUserSelectAssignees.value;
      startUserSelectAssignees.value = {};
      // 加载最新的审批详情,主要用于节点预测
      getApprovalDetail();
    }
  },
  {
    immediate: true,
  },
);

// ============================== 生命周期 ==============================
onMounted(async () => {
  const processDefinitionDetail: any = await getProcessDefinition(
    undefined,
    processDefineKey,
  );

  if (!processDefinitionDetail) {
    message.error('OA 请假的流程模型未配置，请检查！');
    return;
  }

  processDefinitionId.value = processDefinitionDetail.id;
  startUserSelectTasks.value = processDefinitionDetail.startUserSelectTasks;

  getApprovalDetail();
});
</script>

<template>
  <Page>
    <div class="mx-auto w-[80vw] max-w-[920px]">
      <Card :title="getTitle" class="w-full">
        <template #extra>
          <Button type="default" @click="onBack">
            <IconifyIcon icon="lucide:arrow-left" />
            返回
          </Button>
        </template>

        <Form />
      </Card>

      <Card title="流程" class="mt-2 w-full">
        <ProcessInstanceTimeline
          :activity-nodes="activityNodes"
          :show-status-icon="false"
          @select-user-confirm="selectUserConfirm"
        />

        <template #actions>
          <Space warp :size="12" class="w-full px-6">
            <Button type="primary" @click="onSubmit"> 提交 </Button>
          </Space>
        </template>
      </Card>
    </div>
  </Page>
</template>
