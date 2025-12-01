<script lang="ts" setup>
import { inject } from 'vue';

import { ElButton, ElCard, ElImage } from 'element-plus';

defineOptions({ name: 'AiMusicSongInfoIndex' });

const currentSong = inject<any>('currentSong', {});
</script>

<template>
  <ElCard class="!mb-0 w-40 leading-6">
    <ElImage :src="currentSong.imageUrl" class="h-full w-full" />

    <div class="">{{ currentSong.title }}</div>
    <div class="line-clamp-1 text-xs">
      {{ currentSong.desc }}
    </div>
    <div class="text-xs">
      {{ currentSong.date }}
    </div>
    <ElButton size="small" round class="my-2">信息复用</ElButton>
    <div class="text-xs" v-html="currentSong.lyric"></div>
  </ElCard>
</template>
