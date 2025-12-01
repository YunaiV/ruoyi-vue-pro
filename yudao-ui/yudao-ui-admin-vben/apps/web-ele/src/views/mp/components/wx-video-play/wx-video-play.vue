<script lang="ts" setup>
import { ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { VideoPlayer } from '@videojs-player/vue';
import { ElDialog } from 'element-plus';

import 'video.js/dist/video-js.css';

/** 微信消息 - 视频 */
defineOptions({ name: 'WxVideoPlayer' });

const props = defineProps<{
  url: string;
}>();

const dialogVideo = ref(false);

function playVideo() {
  dialogVideo.value = true;
}
</script>

<template>
  <div @click="playVideo()">
    <!-- 提示 -->
    <div class="flex cursor-pointer flex-col items-center">
      <IconifyIcon icon="lucide:circle-play" :size="32" class="mr-5px" />
      <p class="text-sm">点击播放视频</p>
    </div>

    <!-- 弹窗播放 -->
    <ElDialog v-model="dialogVideo" title="视频播放" append-to-body>
      <VideoPlayer
        v-if="dialogVideo"
        class="video-player vjs-big-play-centered"
        :src="props.url"
        poster=""
        controls
        playsinline
        :volume="0.6"
        :width="800"
        :playback-rates="[0.7, 1.0, 1.5, 2.0]"
      />
    </ElDialog>
  </div>
</template>
