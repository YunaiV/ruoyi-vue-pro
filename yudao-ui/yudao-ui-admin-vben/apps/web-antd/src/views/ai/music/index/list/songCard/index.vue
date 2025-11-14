<script lang="ts" setup>
import { inject } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { Image } from 'ant-design-vue';

defineOptions({ name: 'AiMusicSongCardIndex' });

defineProps({
  songInfo: {
    type: Object,
    default: () => ({}),
  },
});

const emits = defineEmits(['play']);

const currentSong = inject<any>('currentSong', {});

function playSong() {
  emits('play');
}
</script>

<template>
  <div class="mb-3 flex rounded p-3">
    <div class="relative" @click="playSong">
      <Image :src="songInfo.imageUrl" class="w-20 flex-none" />
      <div
        class="absolute left-0 top-0 flex h-full w-full cursor-pointer items-center justify-center bg-black bg-opacity-40"
      >
        <IconifyIcon
          :icon="
            currentSong.id === songInfo.id
              ? 'solar:pause-circle-bold'
              : 'mdi:arrow-right-drop-circle'
          "
          :size="30"
        />
      </div>
    </div>
    <div class="ml-2">
      <div>{{ songInfo.title }}</div>
      <div class="mt-2 line-clamp-2 text-xs">
        {{ songInfo.desc }}
      </div>
    </div>
  </div>
</template>
