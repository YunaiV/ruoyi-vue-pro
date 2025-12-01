<!-- emoji 表情选择组件 -->
<script lang="ts" setup>
import type { Emoji } from './emoji';

import { computed } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { List, Popover } from 'ant-design-vue';

import { useEmoji } from './emoji';

/** 选择 emoji 表情 */
const emits = defineEmits<{
  (e: 'selectEmoji', v: Emoji): void;
}>();
const { getEmojiList } = useEmoji();
const emojiList = computed(() => getEmojiList());

function handleSelect(item: Emoji) {
  // 整个 emoji 数据传递出去，方便以后输入框直接显示表情
  emits('selectEmoji', item);
}
</script>

<template>
  <Popover placement="top" trigger="click">
    <template #content>
      <List height="300px" class="w-[500px]">
        <ul class="ml-2 flex flex-wrap px-2">
          <li
            v-for="(item, index) in emojiList"
            :key="index"
            :title="item.name"
            class="w-1/10 m-2 flex cursor-pointer items-center justify-center border border-solid border-primary p-2"
            @click="handleSelect(item)"
          >
            <img :src="item.url" class="size-4" />
          </li>
        </ul>
      </List>
    </template>
    <IconifyIcon
      class="ml-2.5 size-6 cursor-pointer"
      icon="twemoji:grinning-face"
    />
  </Popover>
</template>
