<script lang="ts" setup>
import type { WxMusicProps } from './types';

import { computed } from 'vue';

import { ElLink } from 'element-plus';

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
    <ElLink
      type="success"
      :underline="false"
      target="_blank"
      :href="href"
      class="block"
    >
      <div
        class="flex items-center rounded-sm border border-[#e8e8e8] bg-background p-4 transition hover:border-black/10 hover:shadow-sm"
      >
        <div
          class="mr-3 h-12 w-12 overflow-hidden rounded-full border border-transparent"
        >
          <img :src="thumbMediaUrl" alt="" class="h-full w-full object-cover" />
        </div>
        <div class="flex-1">
          <div class="mb-3 text-base font-medium text-[#000000d9]">
            {{ title }}
          </div>
          <div class="line-clamp-3 h-16 overflow-hidden text-sm text-black/45">
            {{ description }}
          </div>
        </div>
      </div>
    </ElLink>
  </div>
</template>
