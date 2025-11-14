<script lang="ts" setup>
import { EllipsisText } from '@vben/common-ui';
</script>
<template>
  <EllipsisText :max-width="240">
    住在我心里孤独的 孤独的海怪 痛苦之王 开始厌倦 深海的光 停滞的海浪
    <template #tooltip>
      <div style="text-align: center">
        《秦皇岛》<br />住在我心里孤独的<br />孤独的海怪 痛苦之王<br />开始厌倦
        深海的光 停滞的海浪
      </div>
    </template>
  </EllipsisText>
</template>
