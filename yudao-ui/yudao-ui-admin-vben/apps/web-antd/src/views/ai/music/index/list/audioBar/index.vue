<script lang="ts" setup>
import { inject, reactive, ref } from 'vue';

import { IconifyIcon } from '@vben/icons';
import { formatPast } from '@vben/utils';

import { Image, Slider } from 'ant-design-vue';

defineOptions({ name: 'AiMusicAudioBarIndex' });

const currentSong = inject<any>('currentSong', {});

const audioRef = ref<HTMLAudioElement | null>(null);
// 音频相关属性https://www.runoob.com/tags/ref-av-dom.html
const audioProps = reactive<any>({
  autoplay: true,
  paused: false,
  currentTime: '00:00',
  duration: '00:00',
  muted: false,
  volume: 50,
});

function toggleStatus(type: string) {
  audioProps[type] = !audioProps[type];
  if (type === 'paused' && audioRef.value) {
    if (audioProps[type]) {
      audioRef.value.pause();
    } else {
      audioRef.value.play();
    }
  }
}

// 更新播放位置
function audioTimeUpdate(args: any) {
  audioProps.currentTime = formatPast(new Date(args.timeStamp), 'mm:ss');
}
</script>

<template>
  <div
    class="b-1 b-l-none h-18 bg-card flex items-center justify-between border border-solid border-rose-100 px-2"
  >
    <!-- 歌曲信息 -->
    <div class="flex gap-2.5">
      <Image
        src="https://zos.alipayobjects.com/rmsportal/jkjgkEfvpUPVyRjUImniVslZfWPnJuuZ.png"
        :width="45"
      />
      <div>
        <div>{{ currentSong.name }}</div>
        <div class="text-xs text-gray-400">{{ currentSong.singer }}</div>
      </div>
    </div>
    <!-- 音频controls -->
    <div class="flex items-center gap-3">
      <IconifyIcon
        icon="majesticons:back-circle"
        class="size-5 cursor-pointer text-gray-300"
      />
      <IconifyIcon
        :icon="
          audioProps.paused
            ? 'mdi:arrow-right-drop-circle'
            : 'solar:pause-circle-bold'
        "
        class="size-7 cursor-pointer"
        @click="toggleStatus('paused')"
      />
      <IconifyIcon
        icon="majesticons:next-circle"
        class="size-5 cursor-pointer text-gray-300"
      />
      <div class="flex items-center gap-4">
        <span>{{ audioProps.currentTime }}</span>
        <Slider
          v-model:value="audioProps.duration"
          color="#409eff"
          class="!w-40"
        />
        <span>{{ audioProps.duration }}</span>
      </div>
      <!-- 音频 -->
      <audio
        v-bind="audioProps"
        ref="audioRef"
        controls
        v-show="!audioProps"
        @timeupdate="audioTimeUpdate"
      >
        <!-- <source :src="audioUrl" /> -->
      </audio>
    </div>
    <div class="flex items-center gap-4">
      <IconifyIcon
        :icon="audioProps.muted ? 'tabler:volume-off' : 'tabler:volume'"
        class="size-5 cursor-pointer"
        @click="toggleStatus('muted')"
      />
      <Slider v-model:value="audioProps.volume" color="#409eff" class="!w-40" />
    </div>
  </div>
</template>
