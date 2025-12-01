<script lang="ts" setup>
import type { Reply } from './types';

import { computed, ref, unref, watch } from 'vue';

import { NewsType, ReplyType } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';

import { Row, Tabs } from 'ant-design-vue';

import TabImage from './tab-image.vue';
import TabMusic from './tab-music.vue';
import TabNews from './tab-news.vue';
import TabText from './tab-text.vue';
import TabVideo from './tab-video.vue';
import TabVoice from './tab-voice.vue';
import { createEmptyReply } from './types';

/** 消息回复选择 */
defineOptions({ name: 'WxReplySelect' });

const props = withDefaults(defineProps<Props>(), {
  newsType: undefined,
});
const emit = defineEmits<{
  (e: 'update:modelValue', v: Reply): void;
}>();

interface Props {
  modelValue: Reply | undefined;
  newsType?: NewsType;
}

const defaultReply: Reply = {
  accountId: -1,
  type: ReplyType.Text,
}; // 提供一个默认的 Reply 对象，避免 undefined 导致的错误

const reply = computed<Reply>({
  get: () => props.modelValue || defaultReply,
  set: (val) => emit('update:modelValue', val),
});

const tabCache = new Map<ReplyType, Reply>(); // 作为多个标签保存各自 Reply 的缓存
const currentTab = ref<ReplyType>(props.modelValue?.type || ReplyType.Text); // 采用独立的 ref 来保存当前 tab，避免在 watch 标签变化，对 reply 进行赋值会产生了循环调用

// 监听 modelValue 变化，同步更新 currentTab 和缓存
watch(
  () => props.modelValue,
  (newValue) => {
    if (newValue?.type) {
      // 如果类型变化，更新 currentTab
      if (newValue.type !== currentTab.value) {
        currentTab.value = newValue.type;
      }
      // 如果 modelValue 有数据，更新对应 tab 的缓存
      if (newValue.type) {
        tabCache.set(newValue.type, { ...newValue });
      }
    }
  },
  { immediate: true, deep: true },
);

watch(
  currentTab,
  (newTab, oldTab) => {
    // 第一次进入：oldTab 为 undefined
    // 判断 newTab 是因为 Reply 为 Partial
    if (oldTab === undefined || newTab === undefined) {
      return;
    }

    // 保存旧tab的数据到缓存
    const oldReply = unref(reply);
    // 只有当旧tab的reply有实际数据时才缓存（避免缓存空数据）
    if (oldReply && oldTab === oldReply.type) {
      tabCache.set(oldTab, oldReply);
    }

    // 从缓存里面取出新tab内容，有则覆盖Reply，没有则创建空Reply
    const temp = tabCache.get(newTab);
    if (temp) {
      reply.value = temp;
    } else {
      // 如果当前reply的类型就是新tab的类型，说明这是从外部传入的数据，应该保留
      const currentReply = unref(reply);
      if (currentReply && currentReply.type === newTab) {
        // 这是从外部传入的数据，直接缓存并使用
        tabCache.set(newTab, currentReply);
        // 不需要修改reply.value，因为它已经是正确的了
      } else {
        // 创建新的空reply
        const newData = createEmptyReply(reply);
        newData.type = newTab;
        reply.value = newData;
      }
    }
  },
  {
    immediate: true,
  },
);

/** 清除除了`type`, `accountId`的字段 */
function clear() {
  reply.value = createEmptyReply(reply);
}

defineExpose({
  clear,
});
</script>

<template>
  <Tabs v-model:active-key="currentTab" type="card">
    <!-- 类型 1：文本 -->
    <Tabs.TabPane :key="ReplyType.Text">
      <template #tab>
        <Row align="middle">
          <IconifyIcon icon="lucide:file-text" class="mr-1" />
          文本
        </Row>
      </template>
      <TabText v-model="reply.content" />
    </Tabs.TabPane>

    <!-- 类型 2：图片 -->
    <Tabs.TabPane :key="ReplyType.Image">
      <template #tab>
        <Row align="middle">
          <IconifyIcon icon="lucide:image" class="mr-1" />
          图片
        </Row>
      </template>
      <TabImage v-model="reply" />
    </Tabs.TabPane>

    <!-- 类型 3：语音 -->
    <Tabs.TabPane :key="ReplyType.Voice">
      <template #tab>
        <Row align="middle">
          <IconifyIcon icon="lucide:mic" class="mr-1" />
          语音
        </Row>
      </template>
      <TabVoice v-model="reply" />
    </Tabs.TabPane>

    <!-- 类型 4：视频 -->
    <Tabs.TabPane :key="ReplyType.Video">
      <template #tab>
        <Row align="middle">
          <IconifyIcon icon="lucide:video" class="mr-1" />
          视频
        </Row>
      </template>
      <TabVideo v-model="reply" />
    </Tabs.TabPane>

    <!-- 类型 5：图文 -->
    <Tabs.TabPane :key="ReplyType.News">
      <template #tab>
        <Row align="middle">
          <IconifyIcon icon="lucide:newspaper" class="mr-1" />
          图文
        </Row>
      </template>
      <TabNews v-model="reply" :news-type="newsType" />
    </Tabs.TabPane>

    <!-- 类型 6：音乐 -->
    <Tabs.TabPane :key="ReplyType.Music">
      <template #tab>
        <Row align="middle">
          <IconifyIcon icon="lucide:music" class="mr-1" />
          音乐
        </Row>
      </template>
      <TabMusic v-model="reply" />
    </Tabs.TabPane>
  </Tabs>
</template>
