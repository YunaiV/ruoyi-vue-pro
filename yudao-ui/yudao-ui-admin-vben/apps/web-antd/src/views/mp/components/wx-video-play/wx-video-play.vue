<script lang="ts" setup>
import { ref } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { VideoPlayer } from '@videojs-player/vue';
import { Modal } from 'ant-design-vue';

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
  <div class="cursor-pointer" @click="playVideo()">
    <!-- 提示 -->
    <div class="flex items-center">
      <IconifyIcon icon="lucide:circle-play" :size="32" class="mr-2" />
      <p class="text-sm">点击播放视频</p>
    </div>

    <!-- 弹窗播放 -->
    <Modal
      v-model:open="dialogVideo"
      title="视频播放"
      :footer="null"
      :width="850"
      destroy-on-close
    >
      <VideoPlayer
        v-if="dialogVideo"
        class="video-player vjs-big-play-centered"
        :src="props.url"
        poster=""
        crossorigin="anonymous"
        controls
        playsinline
        :volume="0.6"
        :width="800"
        :playback-rates="[0.7, 1.0, 1.5, 2.0]"
      />
    </Modal>
  </div>
</template>
