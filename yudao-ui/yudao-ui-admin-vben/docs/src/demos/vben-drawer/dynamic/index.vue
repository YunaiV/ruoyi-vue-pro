<script lang="ts" setup>
import { useVbenDrawer, VbenButton } from '@vben/common-ui';

import ExtraDrawer from './drawer.vue';

const [Drawer, drawerApi] = useVbenDrawer({
  // 连接抽离的组件
  connectedComponent: ExtraDrawer,
});

function open() {
  drawerApi.open();
}

function handleUpdateTitle() {
  drawerApi.setState({ title: '外部动态标题' }).open();
}
</script>

<template>
  <div>
    <Drawer />

    <VbenButton @click="open">Open</VbenButton>
    <VbenButton class="ml-2" type="primary" @click="handleUpdateTitle">
      从外部修改标题并打开
    </VbenButton>
  </div>
</template>
