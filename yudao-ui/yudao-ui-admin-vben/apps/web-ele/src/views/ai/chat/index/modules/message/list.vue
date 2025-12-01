<script setup lang="ts">
import type { PropType } from 'vue';

import type { AiChatConversationApi } from '#/api/ai/chat/conversation';
import type { AiChatMessageApi } from '#/api/ai/chat/message';

import { computed, nextTick, onMounted, ref, toRefs } from 'vue';

import { IconifyIcon, SvgGptIcon } from '@vben/icons';
import { preferences } from '@vben/preferences';
import { useUserStore } from '@vben/stores';
import { formatDateTime } from '@vben/utils';

import { useClipboard } from '@vueuse/core';
import { ElAvatar, ElButton, ElMessage } from 'element-plus';

import { deleteChatMessage } from '#/api/ai/chat/message';
import { MarkdownView } from '#/components/markdown-view';

import MessageFiles from './files.vue';
import MessageKnowledge from './knowledge.vue';
import MessageReasoning from './reasoning.vue';
import MessageWebSearch from './web-search.vue';

const props = defineProps({
  conversation: {
    type: Object as PropType<AiChatConversationApi.ChatConversation>,
    required: true,
  },
  list: {
    type: Array as PropType<AiChatMessageApi.ChatMessage[]>,
    required: true,
  },
});

const emits = defineEmits(['onDeleteSuccess', 'onRefresh', 'onEdit']);
const { copy } = useClipboard(); // 初始化 copy 到粘贴板
const userStore = useUserStore();

// 判断“消息列表”滚动的位置(用于判断是否需要滚动到消息最下方)
const messageContainer: any = ref(null);
const isScrolling = ref(false); // 用于判断用户是否在滚动

const userAvatar = computed(
  () => userStore.userInfo?.avatar || preferences.app.defaultAvatar,
);

const { list } = toRefs(props); // 定义 emits

// ============ 处理对话滚动 ==============

/** 滚动到底部 */
async function scrollToBottom(isIgnore?: boolean) {
  // 注意要使用 nextTick 以免获取不到 dom
  await nextTick();
  if (isIgnore || !isScrolling.value) {
    messageContainer.value.scrollTop =
      messageContainer.value.scrollHeight - messageContainer.value.offsetHeight;
  }
}

function handleScroll() {
  const scrollContainer = messageContainer.value;
  const scrollTop = scrollContainer.scrollTop;
  const scrollHeight = scrollContainer.scrollHeight;
  const offsetHeight = scrollContainer.offsetHeight;
  isScrolling.value = scrollTop + offsetHeight < scrollHeight - 100;
}

/** 回到底部 */
async function handleGoBottom() {
  const scrollContainer = messageContainer.value;
  scrollContainer.scrollTop = scrollContainer.scrollHeight;
}

/** 回到顶部 */
async function handlerGoTop() {
  const scrollContainer = messageContainer.value;
  scrollContainer.scrollTop = 0;
}

defineExpose({ scrollToBottom, handlerGoTop }); // 提供方法给 parent 调用

// ============ 处理消息操作 ==============

/** 复制 */
async function copyContent(content: string) {
  await copy(content);
  ElMessage.success('复制成功！');
}

/** 删除 */
async function handleDelete(id: number) {
  // 删除 message
  await deleteChatMessage(id);
  ElMessage.success('删除成功！');
  // 回调
  emits('onDeleteSuccess');
}

/** 刷新 */
async function handleRefresh(message: AiChatMessageApi.ChatMessage) {
  emits('onRefresh', message);
}

/** 编辑 */
async function handleEdit(message: AiChatMessageApi.ChatMessage) {
  emits('onEdit', message);
}

/** 初始化 */
onMounted(async () => {
  messageContainer.value.addEventListener('scroll', handleScroll);
});
</script>
<template>
  <div ref="messageContainer" class="relative h-full overflow-y-auto">
    <div
      v-for="(item, index) in list"
      :key="index"
      class="mt-12 flex flex-col overflow-y-hidden px-5"
    >
      <!-- 左侧消息：system、assistant -->
      <div v-if="item.type !== 'user'" class="flex flex-row">
        <div class="avatar">
          <ElAvatar
            v-if="conversation.roleAvatar"
            :src="conversation.roleAvatar"
            :size="28"
          />
          <SvgGptIcon v-else class="size-7" />
        </div>
        <div class="mx-4 flex flex-col text-left">
          <div class="text-left leading-10">
            {{ formatDateTime(item.createTime) }}
          </div>
          <div
            class="relative flex flex-col break-words rounded-lg bg-gray-100 p-2.5 pb-1 pt-2.5 shadow-sm"
          >
            <MessageReasoning
              :reasoning-content="item.reasoningContent || ''"
              :content="item.content || ''"
            />
            <MarkdownView
              class="text-sm text-gray-600"
              :content="item.content"
            />
            <MessageFiles :attachment-urls="item.attachmentUrls" />
            <MessageKnowledge v-if="item.segments" :segments="item.segments" />
            <MessageWebSearch
              v-if="item.webSearchPages"
              :web-search-pages="item.webSearchPages"
            />
          </div>
          <div class="mt-2 flex flex-row">
            <ElButton
              class="!ml-1 flex items-center bg-transparent !px-1.5 hover:bg-gray-100"
              text
              @click="copyContent(item.content)"
            >
              <IconifyIcon icon="lucide:copy" />
            </ElButton>
            <ElButton
              v-if="item.id > 0"
              class="!ml-1 flex items-center bg-transparent !px-1.5 hover:bg-gray-100"
              text
              @click="handleDelete(item.id)"
            >
              <IconifyIcon icon="lucide:trash" />
            </ElButton>
          </div>
        </div>
      </div>

      <!-- 右侧消息：user -->
      <div v-else class="flex flex-row-reverse justify-start">
        <div class="avatar">
          <ElAvatar :src="userAvatar" :size="28" />
        </div>
        <div class="mx-4 flex flex-col text-left">
          <div class="text-left leading-8">
            {{ formatDateTime(item.createTime) }}
          </div>
          <div
            v-if="item.attachmentUrls && item.attachmentUrls.length > 0"
            class="mb-2 flex flex-row-reverse"
          >
            <MessageFiles :attachment-urls="item.attachmentUrls" />
          </div>
          <div class="flex flex-row-reverse">
            <div
              v-if="item.content && item.content.trim()"
              class="inline w-auto whitespace-pre-wrap break-words rounded-lg bg-blue-500 p-2.5 text-sm text-white shadow-sm"
            >
              {{ item.content }}
            </div>
          </div>
          <div class="mt-2 flex flex-row-reverse">
            <ElButton
              class="!ml-1 flex items-center bg-transparent !px-1.5 hover:bg-gray-100"
              text
              @click="copyContent(item.content)"
            >
              <IconifyIcon icon="lucide:copy" />
            </ElButton>
            <ElButton
              class="!ml-1 flex items-center bg-transparent !px-1.5 hover:bg-gray-100"
              text
              @click="handleDelete(item.id)"
            >
              <IconifyIcon icon="lucide:trash" />
            </ElButton>
            <ElButton
              class="!ml-1 flex items-center bg-transparent !px-1.5 hover:bg-gray-100"
              text
              @click="handleRefresh(item)"
            >
              <IconifyIcon icon="lucide:refresh-cw" />
            </ElButton>
            <ElButton
              class="!ml-1 flex items-center bg-transparent !px-1.5 hover:bg-gray-100"
              text
              @click="handleEdit(item)"
            >
              <IconifyIcon icon="lucide:edit" />
            </ElButton>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 回到底部按钮 -->
  <div
    v-if="isScrolling"
    class="absolute bottom-0 right-1/2 z-1000"
    @click="handleGoBottom"
  >
    <ElButton circle>
      <IconifyIcon icon="lucide:chevron-down" />
    </ElButton>
  </div>
</template>
