<script lang="ts" setup>
import type { SystemUserApi } from '#/api/system/user';

import { computed, onMounted, ref, watchEffect } from 'vue';

import { DocAlert, Page } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { useAccessStore } from '@vben/stores';
import { formatDate } from '@vben/utils';

import { useWebSocket } from '@vueuse/core';
import {
  Avatar,
  Badge,
  Button,
  Card,
  Divider,
  Empty,
  Input,
  message,
  Select,
  Tag,
} from 'ant-design-vue';

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
const getTagColor = computed(() => (getIsOpen.value ? 'success' : 'red')); // WebSocket 连接的展示颜色
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
      message.error(`未知的消息类型：${data.value}`);
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
    message.error(`未处理消息：${data.value}`);
  } catch (error) {
    message.error(`处理消息发生异常：${data.value}`);
    console.error(error);
  }
});

/** 发送消息 */
const sendText = ref(''); // 发送内容
const sendUserId = ref('all'); // 发送人
function handlerSend() {
  if (!sendText.value.trim()) {
    message.warning('消息内容不能为空');
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
function getMessageBadgeColor(type?: string) {
  switch (type) {
    case 'group': {
      return 'green';
    }
    case 'single': {
      return 'blue';
    }
    case 'system': {
      return 'red';
    }
    default: {
      return 'default';
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
      <Card :bordered="false" class="w-full md:w-1/2">
        <template #title>
          <div class="flex items-center">
            <Badge :status="getIsOpen ? 'success' : 'error'" />
            <span class="ml-2 text-lg font-medium">连接管理</span>
          </div>
        </template>
        <div class="mb-4 flex items-center rounded-lg p-3">
          <span class="mr-4 font-medium">连接状态:</span>
          <Tag :color="getTagColor" class="px-3 py-1">{{ getStatusText }}</Tag>
        </div>
        <div class="mb-6 flex space-x-2">
          <Input
            v-model:value="server"
            disabled
            class="rounded-md"
            size="large"
          >
            <template #addonBefore>
              <span class="text-gray-600">服务地址</span>
            </template>
          </Input>
          <Button
            :type="getIsOpen ? 'default' : 'primary'"
            :danger="getIsOpen"
            size="large"
            class="flex-shrink-0"
            @click="toggleConnectStatus"
          >
            {{ getIsOpen ? '关闭连接' : '开启连接' }}
          </Button>
        </div>

        <Divider>
          <span class="text-gray-500">消息发送</span>
        </Divider>

        <Select
          v-model:value="sendUserId"
          class="mb-3 w-full"
          size="large"
          placeholder="请选择接收人"
          :disabled="!getIsOpen"
        >
          <Select.Option key="" value="" label="所有人">
            <div class="flex items-center">
              <Avatar size="small" class="mr-2">全</Avatar>
              <span>所有人</span>
            </div>
          </Select.Option>
          <Select.Option
            v-for="user in userList"
            :key="user.id"
            :value="user.id"
            :label="user.nickname"
          >
            <div class="flex items-center">
              <Avatar size="small" class="mr-2">
                {{ user.nickname.slice(0, 1) }}
              </Avatar>
              <span>{{ user.nickname }}</span>
            </div>
          </Select.Option>
        </Select>

        <Input.TextArea
          v-model:value="sendText"
          :auto-size="{ minRows: 3, maxRows: 6 }"
          :disabled="!getIsOpen"
          class="border-1 rounded-lg"
          allow-clear
          placeholder="请输入你要发送的消息..."
        />

        <Button
          :disabled="!getIsOpen"
          block
          class="mt-4"
          type="primary"
          size="large"
          @click="handlerSend"
        >
          <template #icon>
            <IconifyIcon icon="lucide:send-horizontal" />
          </template>
          发送消息
        </Button>
      </Card>

      <!-- 右侧：消息记录 -->
      <Card :bordered="false" class="w-full md:w-1/2">
        <template #title>
          <div class="flex items-center">
            <IconifyIcon
              icon="lucide:message-circle-more"
              class="mr-2 text-lg"
            />
            <span class="text-lg font-medium">消息记录</span>
            <Tag v-if="messageList.length > 0" class="ml-2">
              {{ messageList.length }} 条
            </Tag>
          </div>
        </template>
        <div class="h-96 overflow-auto rounded-lg p-2">
          <Empty v-if="messageList.length === 0" description="暂无消息记录" />
          <div v-else class="space-y-3">
            <div
              v-for="msg in messageReverseList"
              :key="msg.time"
              class="rounded-lg p-3 shadow-sm"
            >
              <div class="mb-1 flex items-center justify-between">
                <div class="flex items-center">
                  <Badge :color="getMessageBadgeColor(msg.type)" />
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
      </Card>
    </div>
  </Page>
</template>
