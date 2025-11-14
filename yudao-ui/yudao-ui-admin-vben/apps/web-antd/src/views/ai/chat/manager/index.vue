<script lang="ts" setup>
import { ref } from 'vue';

import { DocAlert, Page } from '@vben/common-ui';

import { Card, Tabs } from 'ant-design-vue';

import ChatConversationList from './modules/ChatConversationList.vue';
import ChatMessageList from './modules/ChatMessageList.vue';

const activeTabName = ref('conversation');
</script>

<template>
  <Page auto-content-height>
    <template #doc>
      <DocAlert title="AI 对话聊天" url="https://doc.iocoder.cn/ai/chat/" />
    </template>
    <Card>
      <Tabs v-model:active-key="activeTabName">
        <Tabs.TabPane tab="对话列表" key="conversation">
          <ChatConversationList />
        </Tabs.TabPane>
        <Tabs.TabPane tab="消息列表" key="message">
          <ChatMessageList />
        </Tabs.TabPane>
      </Tabs>
    </Card>
  </Page>
</template>
