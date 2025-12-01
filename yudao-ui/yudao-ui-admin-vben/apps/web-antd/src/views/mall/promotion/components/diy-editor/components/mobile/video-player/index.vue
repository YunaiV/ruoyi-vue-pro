<script setup lang="ts">
import type { VideoPlayerProperty } from './config';

import { Image } from 'ant-design-vue';

/** 视频播放 */
defineOptions({ name: 'VideoPlayer' });

defineProps<{ property: VideoPlayerProperty }>();
</script>
<template>
  <div class="w-full" :style="{ height: `${property.style.height}px` }">
    <Image
      class="h-full w-full"
      :src="property.posterUrl"
      v-if="property.posterUrl"
      :preview="false"
    />
    <video
      v-else
      class="h-full w-full"
      :src="property.videoUrl"
      :poster="property.posterUrl"
      :autoplay="property.autoplay"
      controls
    ></video>
  </div>
</template>
