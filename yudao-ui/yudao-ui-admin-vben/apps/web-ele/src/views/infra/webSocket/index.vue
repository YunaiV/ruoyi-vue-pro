<script lang="ts" setup>
import type { SystemUserApi } from '#/api/system/user';

import { computed, onMounted, ref, watchEffect } from 'vue';

import { DocAlert, Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';
import { formatDate } from '@vben/utils';

import { useWebSocket } from '@vueuse/core';
import {
  ElAvatar,
  ElBadge,
  ElButton,
  ElCard,
  ElDivider,
  ElEmpty,
  ElInput,
  ElMessage,
  ElOption,
  ElSelect,
  ElTag,
} from 'element-plus';

import { getSimpleUserList } from '#/api/system/user';

const accessStore = useAccessStore();
const refreshToken = accessStore.refreshToken as string;

const server = ref(
  `${`${import.meta.env.VITE_BASE_URL}/infra/ws`.replace(
    'http',
    'ws',
  )}?token=${refreshToken}`, // 使用 refreshToken，而不使用 accessToken 方法的原因：WebSocket 无法方便的刷新访问令牌
); // WebSocket 服务地址
const getIsOpen = computed(() => status.value === 'OPEN'); // WebSocket 连接是否打开
const getTagColor = computed(() => (getIsOpen.value ? 'success' : 'danger')); // WebSocket 连接的展示颜色
const getStatusText = computed(() => (getIsOpen.value ? '已连接' : '未连接')); // 连接状态文本

/** 发起 WebSocket 连接 */
const { status, data, send, close, open } = useWebSocket(server.value, {
  autoReconnect: true,
  heartbeat: true,
});

/** 监听接收到的数据 */
const messageList = ref(
  [] as { text: string; time: number; type?: string; userId?: string }[],
); // 消息列表
const messageReverseList = computed(() => [...messageList.value].toReversed());
watchEffect(() => {
  if (!data.value) {
    return;
  }
  try {
    // 1. 收到心跳
    if (data.value === 'pong') {
      // state.recordList.push({
      //   text: '【心跳】',
      //   time: new Date().getTime()
      // })
      return;
    }

    // 2.1 解析 type 消息类型
    const jsonMessage = JSON.parse(data.value);
    const type = jsonMessage.type;
    const content = JSON.parse(jsonMessage.content);
    if (!type) {
      ElMessage.error(`未知的消息类型：${data.value}`);
      return;
    }
    // 2.2 消息类型：demo-message-receive
    if (type === 'demo-message-receive') {
      const single = content.single;
      messageList.value.push({
        text: content.text,
        time: Date.now(),
        type: single ? 'single' : 'group',
        userId: content.fromUserId,
      });
      return;
    }
    // 2.3 消息类型：notice-push
    if (type === 'notice-push') {
      messageList.value.push({
        text: content.title,
        time: Date.now(),
        type: 'system',
      });
      return;
    }
    ElMessage.error(`未处理消息：${data.value}`);
  } catch (error) {
    ElMessage.error(`处理消息发生异常：${data.value}`);
    console.error(error);
  }
});

/** 发送消息 */
const sendText = ref(''); // 发送内容
const sendUserId = ref('all'); // 发送人
function handlerSend() {
  if (!sendText.value.trim()) {
    ElMessage.warning('消息内容不能为空');
    return;
  }

  // 1.1 先 JSON 化 message 消息内容
  const messageContent = JSON.stringify({
    text: sendText.value,
    toUserId: sendUserId.value === 'all' ? undefined : sendUserId.value,
  });
  // 1.2 再 JSON 化整个消息
  const jsonMessage = JSON.stringify({
    type: 'demo-message-send',
    content: messageContent,
  });
  // 2. 最后发送消息
  send(jsonMessage);
  sendText.value = '';
}

/** 切换 websocket 连接状态 */
function toggleConnectStatus() {
  if (getIsOpen.value) {
    close();
  } else {
    open();
  }
}

/** 获取消息类型的徽标颜色 */
function getMessageBadgeType(type?: string) {
  switch (type) {
    case 'group': {
      return 'success';
    }
    case 'single': {
      return 'primary';
    }
    case 'system': {
      return 'danger';
    }
    default: {
      return 'info';
    }
  }
}

/** 获取消息类型的文本 */
function getMessageTypeText(type?: string) {
  switch (type) {
    case 'group': {
      return '群发';
    }
    case 'single': {
      return '单发';
    }
    case 'system': {
      return '系统';
    }
    default: {
      return '未知';
    }
  }
}

/** 初始化 */
const userList = ref<SystemUserApi.User[]>([]); // 用户列表
onMounted(async () => {
  userList.value = await getSimpleUserList();
});
</script>

<template>
  <Page>
    <template #doc>
      <DocAlert
        title="WebSocket 实时通信"
        url="https://doc.iocoder.cn/websocket/"
      />
    </template>

    <div class="mt-4 flex flex-col gap-4 md:flex-row">
      <!-- 左侧：建立连接、发送消息 -->
      <ElCard shadow="never" class="w-full md:w-1/2">
        <template #header>
          <div class="flex items-center">
            <div v-if="getIsOpen">
              <ElBadge type="success" is-dot />
            </div>
            <div v-else>
              <ElBadge type="danger" is-dot />
            </div>
            <span class="ml-2 text-lg font-medium">连接管理</span>
          </div>
        </template>
        <div class="mb-4 flex items-center rounded-lg p-3">
          <span class="mr-4 font-medium">连接状态:</span>
          <ElTag :type="getTagColor">{{ getStatusText }}</ElTag>
        </div>
        <div class="mb-6 flex space-x-2">
          <ElInput v-model="server" disabled class="rounded-md" size="default">
            <template #prepend>
              <span class="text-gray-600">服务地址</span>
            </template>
          </ElInput>
          <ElButton
            :type="getIsOpen ? 'danger' : 'primary'"
            size="default"
            class="flex-shrink-0"
            @click="toggleConnectStatus"
          >
            {{ getIsOpen ? '关闭连接' : '开启连接' }}
          </ElButton>
        </div>

        <ElDivider>
          <span class="text-gray-500">消息发送</span>
        </ElDivider>

        <ElSelect
          v-model="sendUserId"
          class="mb-3 w-full"
          size="default"
          placeholder="请选择接收人"
          :disabled="!getIsOpen"
        >
          <ElOption key="" value="all" label="所有人">
            <div class="flex items-center">
              <ElAvatar size="small">全</ElAvatar>
              <span class="ml-2">所有人</span>
            </div>
          </ElOption>
          <ElOption
            v-for="user in userList"
            :key="user.id"
            :value="user.id || 0"
            :label="user.nickname"
          >
            <div class="flex items-center">
              <ElAvatar size="small">
                {{ user.nickname.slice(0, 1) }}
              </ElAvatar>
              <span class="ml-2">{{ user.nickname }}</span>
            </div>
          </ElOption>
        </ElSelect>

        <ElInput
          v-model="sendText"
          :rows="3"
          type="textarea"
          :disabled="!getIsOpen"
          class="border-1 rounded-lg"
          clearable
          placeholder="请输入你要发送的消息..."
        />

        <ElButton
          :disabled="!getIsOpen"
          class="mt-4"
          type="primary"
          size="default"
          style="width: 100%"
          @click="handlerSend"
        >
          <template #icon>
            <span class="i-ant-design:send-outlined mr-1"></span>
          </template>
          发送消息
        </ElButton>
      </ElCard>

      <!-- 右侧：消息记录 -->
      <ElCard shadow="never" class="w-full md:w-1/2">
        <template #header>
          <div class="flex items-center">
            <span class="i-ant-design:message-outlined mr-2 text-lg"></span>
            <span class="text-lg font-medium">消息记录</span>
            <ElTag v-if="messageList.length > 0" class="ml-2">
              {{ messageList.length }} 条
            </ElTag>
          </div>
        </template>
        <div class="h-96 overflow-auto rounded-lg p-2">
          <ElEmpty v-if="messageList.length === 0" description="暂无消息记录" />
          <div v-else class="space-y-3">
            <div
              v-for="msg in messageReverseList"
              :key="msg.time"
              class="rounded-lg p-3 shadow-sm"
            >
              <div class="mb-1 flex items-center justify-between">
                <div class="flex items-center">
                  <ElBadge :type="getMessageBadgeType(msg.type)" is-dot />
                  <span class="ml-1 font-medium text-gray-600">
                    {{ getMessageTypeText(msg.type) }}
                  </span>
                  <span v-if="msg.userId" class="ml-2 text-gray-500">
                    用户 ID: {{ msg.userId }}
                  </span>
                </div>
                <span class="text-xs text-gray-400">
                  {{ formatDate(msg.time) }}
                </span>
              </div>
              <div class="mt-2 break-words text-gray-800">
                {{ msg.text }}
              </div>
            </div>
          </div>
        </div>
      </ElCard>
    </div>
  </Page>
</template>
