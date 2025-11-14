<script lang="ts" setup>
import { Maximize, Minimize } from '@vben-core/icons';

import { useFullscreen } from '@vueuse/core';

import { VbenIconButton } from '../button';

defineOptions({ name: 'FullScreen' });

const { isFullscreen, toggle } = useFullscreen();

// 重新检查全屏状态
isFullscreen.value = !!(
  document.fullscreenElement ||
  // @ts-ignore
  document.webkitFullscreenElement ||
  // @ts-ignore
  document.mozFullScreenElement ||
  // @ts-ignore
  document.msFullscreenElement
);
</script>
<template>
  <VbenIconButton
    class="hover:animate-[shrink_0.3s_ease-in-out]"
    @click="toggle"
  >
    <Minimize v-if="isFullscreen" class="text-foreground size-4" />
    <Maximize v-else class="text-foreground size-4" />
  </VbenIconButton>
</template>
