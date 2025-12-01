<script setup lang="ts">
import type { VideoPlayerProperty } from './config';

import { ElImage } from 'element-plus';

/** 视频播放 */
defineOptions({ name: 'VideoPlayer' });

defineProps<{ property: VideoPlayerProperty }>();
</script>
<template>
  <div class="w-full" :style="{ height: `${property.style.height}px` }">
    <ElImage
      class="block h-full w-full"
      :src="property.posterUrl"
      v-if="property.posterUrl"
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
