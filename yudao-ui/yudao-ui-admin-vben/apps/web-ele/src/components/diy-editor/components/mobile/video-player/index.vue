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
      class="w-full"
      :src="property.posterUrl"
      v-if="property.posterUrl"
    />
    <video
      v-else
      class="w-full"
      :src="property.videoUrl"
      :poster="property.posterUrl"
      :autoplay="property.autoplay"
      controls
    ></video>
  </div>
</template>

<style scoped lang="scss">
/* 图片 */
img {
  display: block;
  width: 100%;
  height: 100%;
}
</style>
