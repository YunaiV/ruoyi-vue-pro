<script lang="ts" setup>
import { ref } from 'vue';

import { DocAlert, Page } from '@vben/common-ui';

import { ElTabs } from 'element-plus';

import ChatConversationList from './modules/conversation-list.vue';
import ChatMessageList from './modules/message-list.vue';

const activeTabName = ref('conversation');
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="AI 对话聊天" url="https://doc.iocoder.cn/ai/chat/" />
    </template>

    <ElTabs v-model:model-value="activeTabName">
      <ElTabs.TabPane label="对话列表" name="conversation">
        <ChatConversationList />
      </ElTabs.TabPane>
      <ElTabs.TabPane label="消息列表" name="message">
        <ChatMessageList />
      </ElTabs.TabPane>
    </ElTabs>
  </Page>
</template>
