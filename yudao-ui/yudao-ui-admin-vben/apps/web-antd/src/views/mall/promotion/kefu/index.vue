<script lang="ts" setup>
import type { MallKefuConversationApi } from '#/api/mall/promotion/kefu/conversation';

import { onBeforeUnmount, onMounted, ref, watch } from 'vue';

import { Page } from '@vben/common-ui';
import { useAccessStore } from '@vben/stores';

import { useWebSocket } from '@vueuse/core';
import { message } from 'ant-design-vue';

import { useMallKefuStore } from '#/store/mall/kefu';

import ConversationList from './modules/conversation-list.vue';
import MemberInfo from './modules/member/member-info.vue';
import MessageList from './modules/message-list.vue';
import { WebSocketMessageTypeConstants } from './modules/tools/constants';

const accessStore = useAccessStore();
const kefuStore = useMallKefuStore(); // 客服缓存

/** 组件引用 */
const messageListRef = ref<InstanceType<typeof MessageList>>();
const memberInfoRef = ref<InstanceType<typeof MemberInfo>>();
const conversationListRef = ref<InstanceType<typeof ConversationList>>();

// ======================= WebSocket start =======================
const url = `${`${import.meta.env.VITE_BASE_URL}/infra/ws`.replace(
  'http',
  'ws',
)}?token=${accessStore.refreshToken}`; // 使用 refreshToken() :WebSocket 无法方便的刷新访问令牌
const server = ref(url); // WebSocket 服务地址

/** 发起 WebSocket 连接 */
const { data, close, open } = useWebSocket(server.value, {
  autoReconnect: true,
  heartbeat: true,
});

/** 监听 WebSocket 数据 */
watch(
  () => data.value,
  (newData) => {
    if (!newData) return;
    try {
      // 1. 收到心跳
      if (newData === 'pong') return;

      // 2.1 解析 type 消息类型
      const jsonMessage = JSON.parse(newData);
      const type = jsonMessage.type;
      if (!type) {
        message.error(`未知的消息类型：${newData}`);
        return;
      }

      // 2.2 消息类型：KEFU_MESSAGE_TYPE
      if (type === WebSocketMessageTypeConstants.KEFU_MESSAGE_TYPE) {
        const message = JSON.parse(jsonMessage.content);
        // 刷新会话列表
        kefuStore.updateConversation(message.conversationId);
        // 刷新消息列表
        messageListRef.value?.refreshMessageList(message);
        return;
      }

      // 2.3 消息类型：KEFU_MESSAGE_ADMIN_READ
      if (type === WebSocketMessageTypeConstants.KEFU_MESSAGE_ADMIN_READ) {
        // 更新会话已读
        const message = JSON.parse(jsonMessage.content);
        kefuStore.updateConversationStatus(message.conversationId);
      }
    } catch (error) {
      console.error(error);
    }
  },
  {
    immediate: false, // 不立即执行
  },
);
// ======================= WebSocket end =======================

function handleChange(conversation: MallKefuConversationApi.Conversation) {
  messageListRef.value?.getNewMessageList(conversation);
  memberInfoRef.value?.initHistory(conversation);
}

/** 初始化 */
onMounted(() => {
  // 加载会话列表
  kefuStore.setConversationList().then(() => {
    conversationListRef.value?.calculationLastMessageTime();
  });
  // 打开 websocket 连接
  open();
});

/** 销毁 */
onBeforeUnmount(() => {
  // 关闭 websocket 连接
  close();
});
</script>

<template>
  <Page auto-content-height>
    <div class="flex h-full antialiased">
      <div class="flex h-full w-full flex-row overflow-x-hidden">
        <!-- 会话列表 -->
        <ConversationList
          class="w-1/6"
          ref="conversationListRef"
          @change="handleChange"
        />
        <!-- 会话详情（选中会话的消息列表） -->
        <MessageList class="w-4/6" ref="messageListRef" />
        <!-- 会员信息（选中会话的会员信息） -->
        <MemberInfo class="w-[280px]" ref="memberInfoRef" />
      </div>
    </div>
  </Page>
</template>
