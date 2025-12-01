<script lang="ts" setup>
import type { WxMusicProps } from './types';

import { computed } from 'vue';

import { Typography } from 'ant-design-vue';

/** 微信消息 - 音乐 */
defineOptions({ name: 'WxMusic' });

const props = withDefaults(defineProps<WxMusicProps>(), {
  title: '',
  description: '',
  musicUrl: '',
  hqMusicUrl: '',
  thumbMediaUrl: '',
});

const href = computed(() => props.hqMusicUrl || props.musicUrl);

defineExpose({
  musicUrl: props.musicUrl,
});
</script>

<template>
  <div>
    <Typography.Link
      :href="href"
      target="_blank"
      class="text-success no-underline"
    >
      <div
        class="flex items-center rounded-[5px] border border-[#e8e8e8] bg-white p-2.5 transition hover:border-black/10 hover:shadow-sm"
      >
        <div
          class="mr-3 h-[60px] w-[60px] overflow-hidden rounded-[4px] border border-transparent"
        >
          <img
            :src="thumbMediaUrl"
            alt="音乐封面"
            class="h-full w-full object-cover"
          />
        </div>
        <div class="min-w-0 flex-1">
          <div class="mb-2 truncate text-sm font-medium text-[#333]">
            {{ title }}
          </div>
          <div class="truncate text-xs text-[#666]">
            {{ description }}
          </div>
        </div>
      </div>
    </Typography.Link>
  </div>
</template>
