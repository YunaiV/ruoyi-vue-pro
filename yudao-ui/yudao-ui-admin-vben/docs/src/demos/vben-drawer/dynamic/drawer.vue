<script lang="ts" setup>
import { useVbenDrawer, VbenButton } from '@vben/common-ui';

const [Drawer, drawerApi] = useVbenDrawer({
  onCancel() {
    drawerApi.close();
  },
  onConfirm() {
    console.info('onConfirm');
  },
  title: '动态修改配置示例',
});

function handleUpdateTitle() {
  drawerApi.setState({ title: '内部动态标题' });
}
</script>
<template>
  <Drawer>
    <div class="flex-col-center">
      <VbenButton class="mb-3" type="primary" @click="handleUpdateTitle()">
        内部动态修改标题
      </VbenButton>
    </div>
  </Drawer>
</template>
