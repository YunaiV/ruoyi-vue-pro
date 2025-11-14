<script lang="ts" setup>
import { useVbenDrawer, VbenButton } from '@vben/common-ui';

const [Drawer, drawerApi] = useVbenDrawer();
</script>
<template>
  <div>
    <VbenButton @click="() => drawerApi.open()">Open</VbenButton>
    <Drawer class="w-[600px]" title="基础示例"> drawer content </Drawer>
  </div>
</template>
