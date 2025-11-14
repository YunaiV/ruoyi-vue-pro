<!-- 审批详情的右侧：审批流 -->
<script lang="ts" setup>
import type { BpmProcessInstanceApi } from '#/api/bpm/processInstance';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { useVbenModal } from '@vben/common-ui';
import {
  BpmCandidateStrategyEnum,
  BpmNodeTypeEnum,
  BpmTaskStatusEnum,
} from '@vben/constants';
import { IconifyIcon } from '@vben/icons';
import { formatDateTime, isEmpty } from '@vben/utils';

import { Avatar, Button, Image, Timeline, Tooltip } from 'ant-design-vue';

import { UserSelectModal } from '#/components/select-modal';

defineOptions({ name: 'BpmProcessInstanceTimeline' });

const props = withDefaults(
  defineProps<{
    activityNodes: BpmProcessInstanceApi.ApprovalNodeInfo[]; // 审批节点信息
    enableApproveUserSelect?: boolean; // 是否开启审批人自选功能
    showStatusIcon?: boolean; // 是否显示头像右下角状态图标
  }>(),
  {
    showStatusIcon: true, // 默认值为 true
    enableApproveUserSelect: false, // 默认值为 false
  },
);

const emit = defineEmits<{
  selectUserConfirm: [activityId: string, userList: any[]];
}>();

const { push } = useRouter(); // 路由

// 状态图标映射
const statusIconMap: Record<
  string,
  { animation?: string; color: string; icon: string }
> = {
  // 跳过
  '-2': { color: '#909398', icon: 'mdi:skip-forward-outline' },
  // 审批未开始
  '-1': { color: '#909398', icon: 'mdi:clock-outline' },
  // 待审批
  '0': { color: '#ff943e', icon: 'mdi:loading', animation: 'animate-spin' },
  // 审批中
  '1': { color: '#448ef7', icon: 'mdi:loading', animation: 'animate-spin' },
  // 审批通过
  '2': { color: '#00b32a', icon: 'mdi:check' },
  // 审批不通过
  '3': { color: '#f46b6c', icon: 'mdi:close' },
  // 已取消
  '4': { color: '#cccccc', icon: 'mdi:trash-can-outline' },
  // 退回
  '5': { color: '#f46b6c', icon: 'mdi:arrow-left' },
  // 委派中
  '6': { color: '#448ef7', icon: 'mdi:clock-outline' },
  // 审批通过中
  '7': { color: '#00b32a', icon: 'mdi:check' },
};

// 节点类型图标映射
const nodeTypeSvgMap = {
  // 结束节点
  [BpmNodeTypeEnum.END_EVENT_NODE]: {
    color: '#909398',
    icon: 'mdi:power',
  },
  // 开始节点
  [BpmNodeTypeEnum.START_USER_NODE]: {
    color: '#909398',
    icon: 'mdi:account-outline',
  },
  // 用户任务节点
  [BpmNodeTypeEnum.USER_TASK_NODE]: {
    color: '#ff943e',
    icon: 'tdesign:seal',
  },
  // 事务节点
  [BpmNodeTypeEnum.TRANSACTOR_NODE]: {
    color: '#ff943e',
    icon: 'mdi:file-edit-outline',
  },
  // 复制任务节点
  [BpmNodeTypeEnum.COPY_TASK_NODE]: {
    color: '#3296fb',
    icon: 'mdi:content-copy',
  },
  // 条件分支节点
  [BpmNodeTypeEnum.CONDITION_NODE]: {
    color: '#14bb83',
    icon: 'carbon:flow',
  },
  // 并行分支节点
  [BpmNodeTypeEnum.PARALLEL_BRANCH_NODE]: {
    color: '#14bb83',
    icon: 'si:flow-parallel-line',
  },
  // 子流程节点
  [BpmNodeTypeEnum.CHILD_PROCESS_NODE]: {
    color: '#14bb83',
    icon: 'icon-park-outline:tree-diagram',
  },
} as Record<BpmNodeTypeEnum, { color: string; icon: string }>;

// 只有状态是 -1、0、1 才展示头像右小角状态小icon
const onlyStatusIconShow = [-1, 0, 1];

// 获取审批节点类型图标
function getApprovalNodeTypeIcon(nodeType: BpmNodeTypeEnum) {
  return nodeTypeSvgMap[nodeType]?.icon;
}

// 获取审批节点图标
function getApprovalNodeIcon(taskStatus: number, nodeType: BpmNodeTypeEnum) {
  if (taskStatus === BpmTaskStatusEnum.NOT_START) {
    return statusIconMap[taskStatus]?.icon || 'mdi:clock-outline';
  }

  if (
    nodeType === BpmNodeTypeEnum.START_USER_NODE ||
    nodeType === BpmNodeTypeEnum.USER_TASK_NODE ||
    nodeType === BpmNodeTypeEnum.TRANSACTOR_NODE ||
    nodeType === BpmNodeTypeEnum.CHILD_PROCESS_NODE ||
    nodeType === BpmNodeTypeEnum.END_EVENT_NODE
  ) {
    return statusIconMap[taskStatus]?.icon || 'mdi:clock-outline';
  }
  return 'mdi:clock-outline';
}

// 获取审批节点颜色
function getApprovalNodeColor(taskStatus: number) {
  return statusIconMap[taskStatus]?.color;
}

// 获取审批节点时间
function getApprovalNodeTime(node: BpmProcessInstanceApi.ApprovalNodeInfo) {
  if (node.nodeType === BpmNodeTypeEnum.START_USER_NODE && node.startTime) {
    return formatDateTime(node.startTime);
  }
  if (node.endTime) {
    return formatDateTime(node.endTime);
  }
  if (node.startTime) {
    return formatDateTime(node.startTime);
  }
  return '';
}

// 选择自定义审批人
const [UserSelectModalComp, userSelectModalApi] = useVbenModal({
  connectedComponent: UserSelectModal,
  destroyOnClose: true,
});
const selectedActivityNodeId = ref<string>();
const customApproveUsers = ref<Record<string, any[]>>({}); // key：activityId，value：用户列表

// 打开选择用户弹窗
const handleSelectUser = (activityId: string, selectedList: any[]) => {
  selectedActivityNodeId.value = activityId;
  userSelectModalApi
    .setData({ userIds: selectedList.map((item) => item.id) })
    .open();
};

// 选择用户完成
const selectedUsers = ref<number[]>([]);
function handleUserSelectConfirm(userList: any[]) {
  if (!selectedActivityNodeId.value) {
    return;
  }
  customApproveUsers.value[selectedActivityNodeId.value] = userList || [];

  emit('selectUserConfirm', selectedActivityNodeId.value, userList);
}

/** 跳转子流程 */
function handleChildProcess(activity: any) {
  if (!activity.processInstanceId) {
    return;
  }
  push({
    name: 'BpmProcessInstanceDetail',
    query: {
      id: activity.processInstanceId,
    },
  });
}

// 判断是否需要显示自定义选择审批人
function shouldShowCustomUserSelect(
  activity: BpmProcessInstanceApi.ApprovalNodeInfo,
) {
  return (
    isEmpty(activity.tasks) &&
    ((BpmCandidateStrategyEnum.START_USER_SELECT ===
      activity.candidateStrategy &&
      isEmpty(activity.candidateUsers)) ||
      (props.enableApproveUserSelect &&
        BpmCandidateStrategyEnum.APPROVE_USER_SELECT ===
          activity.candidateStrategy))
  );
}

// 判断是否需要显示审批意见
function shouldShowApprovalReason(task: any, nodeType: BpmNodeTypeEnum) {
  return (
    task.reason &&
    [BpmNodeTypeEnum.END_EVENT_NODE, BpmNodeTypeEnum.USER_TASK_NODE].includes(
      nodeType,
    )
  );
}

// 用户选择弹窗关闭
function handleUserSelectClosed() {
  selectedUsers.value = [];
}

// 用户选择弹窗取消
function handleUserSelectCancel() {
  selectedUsers.value = [];
}

/** 设置自定义审批人 */
const setCustomApproveUsers = (activityId: string, users: any[]) => {
  customApproveUsers.value[activityId] = users || [];
};

/** 批量设置多个节点的自定义审批人 */
const batchSetCustomApproveUsers = (data: Record<string, any[]>) => {
  Object.keys(data).forEach((activityId) => {
    customApproveUsers.value[activityId] = data[activityId] || [];
  });
};

// 暴露方法给父组件
defineExpose({ setCustomApproveUsers, batchSetCustomApproveUsers });
</script>

<template>
  <div>
    <Timeline class="pt-5">
      <!-- 遍历每个审批节点 -->
      <Timeline.Item
        v-for="(activity, index) in activityNodes"
        :key="index"
        :color="getApprovalNodeColor(activity.status)"
      >
        <template #dot>
          <div class="relative">
            <div
              class="position-absolute left--2.5 top--1.5 flex h-8 w-8 items-center justify-center rounded-full border border-solid border-gray-200 bg-blue-500 p-1.5"
            >
              <IconifyIcon
                :icon="getApprovalNodeTypeIcon(activity.nodeType)"
                class="size-6 text-white"
              />
            </div>

            <div
              v-if="showStatusIcon"
              class="absolute right--2.5 top-4 flex size-5 items-center rounded-full border-2 border-solid border-white p-0.5"
              :style="{
                backgroundColor: getApprovalNodeColor(activity.status),
              }"
            >
              <IconifyIcon
                :icon="getApprovalNodeIcon(activity.status, activity.nodeType)"
                class="text-white"
                :class="[statusIconMap[activity.status]?.animation]"
              />
            </div>
          </div>
        </template>

        <div
          class="ml-2 flex flex-col items-start gap-2"
          :id="`activity-task-${activity.id}-${index}`"
        >
          <!-- 第一行：节点名称、时间 -->
          <div class="flex w-full">
            <div class="font-bold">
              {{ activity.name }}
              <span v-if="activity.status === BpmTaskStatusEnum.SKIP">
                【跳过】
              </span>
            </div>
            <!-- 信息：时间 -->
            <div
              v-if="activity.status !== BpmTaskStatusEnum.NOT_START"
              class="ml-auto mt-1 text-sm text-gray-500"
            >
              {{ getApprovalNodeTime(activity) }}
            </div>
          </div>

          <!-- 子流程节点 -->
          <div v-if="activity.nodeType === BpmNodeTypeEnum.CHILD_PROCESS_NODE">
            <Button
              type="primary"
              ghost
              size="small"
              @click="handleChildProcess(activity)"
              :disabled="!activity.processInstanceId"
            >
              查看子流程
            </Button>
          </div>

          <!-- 需要自定义选择审批人 -->
          <div
            v-if="shouldShowCustomUserSelect(activity)"
            class="flex flex-wrap items-center gap-2"
          >
            <Tooltip title="添加用户" placement="left">
              <Button
                type="primary"
                size="middle"
                ghost
                class="flex items-center justify-center"
                @click="
                  handleSelectUser(
                    activity.id,
                    customApproveUsers[activity.id] ?? [],
                  )
                "
              >
                <template #icon>
                  <IconifyIcon icon="lucide:user-plus" class="size-4" />
                </template>
              </Button>
            </Tooltip>

            <div
              v-for="(user, userIndex) in customApproveUsers[activity.id]"
              :key="user.id || userIndex"
              class="relative flex h-9 items-center gap-2 rounded-3xl bg-gray-100 pr-2 dark:bg-gray-600"
            >
              <Avatar
                class="!m-1"
                :size="28"
                v-if="user.avatar"
                :src="user.avatar"
              />

              <Avatar class="!m-1" :size="28" v-else>
                <span>{{ user.nickname.substring(0, 1) }}</span>
              </Avatar>
              <span class="text-sm">{{ user.nickname }}</span>
            </div>
          </div>

          <div v-else class="mt-1 flex flex-wrap items-center gap-2">
            <!-- 情况一：遍历每个审批节点下的【进行中】task 任务 -->
            <div
              v-for="(task, idx) in activity.tasks"
              :key="idx"
              class="flex flex-col gap-2 pr-2"
            >
              <div
                class="relative flex flex-wrap gap-2"
                v-if="task.assigneeUser || task.ownerUser"
              >
                <!-- 信息：头像昵称 -->
                <div class="relative flex h-8 items-center rounded-3xl pr-2">
                  <template
                    v-if="
                      task.assigneeUser?.avatar || task.assigneeUser?.nickname
                    "
                  >
                    <Avatar
                      class="!m-1"
                      :size="28"
                      v-if="task.assigneeUser?.avatar"
                      :src="task.assigneeUser?.avatar"
                    />
                    <Avatar class="!m-1" :size="28" v-else>
                      {{ task.assigneeUser?.nickname.substring(0, 1) }}
                    </Avatar>
                    {{ task.assigneeUser?.nickname }}
                  </template>
                  <template
                    v-else-if="
                      task.ownerUser?.avatar || task.ownerUser?.nickname
                    "
                  >
                    <Avatar
                      class="!m-1"
                      :size="28"
                      v-if="task.ownerUser?.avatar"
                      :src="task.ownerUser?.avatar"
                    />
                    <Avatar class="!m-1" :size="28" v-else>
                      {{ task.ownerUser?.nickname.substring(0, 1) }}
                    </Avatar>
                    {{ task.ownerUser?.nickname }}
                  </template>

                  <!-- 信息：任务状态图标 -->
                  <div
                    v-if="
                      showStatusIcon && onlyStatusIconShow.includes(task.status)
                    "
                    class="absolute left-6 top-5 flex items-center rounded-full border-2 border-solid border-white p-1"
                    :style="{
                      backgroundColor: statusIconMap[task.status]?.color,
                    }"
                  >
                    <IconifyIcon
                      :icon="statusIconMap[task.status]?.icon || 'lucide:clock'"
                      class="size-2 text-white"
                      :class="[statusIconMap[task.status]?.animation]"
                    />
                  </div>
                </div>
              </div>

              <!-- 审批意见和签名 -->
              <teleport defer :to="`#activity-task-${activity.id}-${index}`">
                <div
                  v-if="shouldShowApprovalReason(task, activity.nodeType)"
                  class="mt-1 w-full rounded-md bg-gray-100 p-2 text-sm text-gray-500"
                >
                  审批意见：{{ task.reason }}
                </div>
                <div
                  v-if="
                    task.signPicUrl &&
                    activity.nodeType === BpmNodeTypeEnum.USER_TASK_NODE
                  "
                  class="mt-1 w-full rounded-md bg-gray-100 p-2 text-sm text-gray-500"
                >
                  签名：
                  <Image
                    class="ml-1 h-10 w-24"
                    :src="task.signPicUrl"
                    :preview="{ src: task.signPicUrl }"
                  />
                </div>
              </teleport>
            </div>

            <!-- 情况二：遍历每个审批节点下的【候选的】task 任务 -->
            <div
              v-for="(user, userIndex) in activity.candidateUsers"
              :key="userIndex"
              class="relative flex h-8 items-center rounded-3xl pr-2"
            >
              <Avatar
                class="!m-1"
                :size="28"
                v-if="user.avatar"
                :src="user.avatar"
              />
              <Avatar class="!m-1" :size="28" v-else>
                {{ user.nickname.substring(0, 1) }}
              </Avatar>
              <span class="text-sm">
                {{ user.nickname }}
              </span>

              <!-- 候选任务状态图标 -->
              <div
                v-if="showStatusIcon"
                class="absolute left-6 top-5 flex items-center rounded-full border-2 border-solid border-white p-1"
                :style="{ backgroundColor: statusIconMap['-1']?.color }"
              >
                <IconifyIcon
                  class="text-xs text-white"
                  :icon="statusIconMap['-1']?.icon || 'lucide:clock'"
                />
              </div>
            </div>
          </div>
        </div>
      </Timeline.Item>
    </Timeline>

    <!-- 用户选择弹窗 -->
    <UserSelectModalComp
      class="w-3/5"
      v-model:value="selectedUsers"
      :multiple="true"
      title="选择用户"
      @confirm="handleUserSelectConfirm"
      @closed="handleUserSelectClosed"
      @cancel="handleUserSelectCancel"
    />
  </div>
</template>
