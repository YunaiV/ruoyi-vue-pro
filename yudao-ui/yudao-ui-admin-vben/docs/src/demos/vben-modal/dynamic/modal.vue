<script lang="ts" setup>
import { useVbenModal, VbenButton } from '@vben/common-ui';

const [Modal, modalApi] = useVbenModal({
  draggable: true,
  onCancel() {
    modalApi.close();
  },
  onConfirm() {
    console.info('onConfirm');
  },
  title: '动态修改配置示例',
});

const state = modalApi.useStore();

function handleUpdateTitle() {
  modalApi.setState({ title: '内部动态标题' });
}

function handleToggleFullscreen() {
  modalApi.setState((prev) => {
    return { ...prev, fullscreen: !prev.fullscreen };
  });
}
</script>
<template>
  <Modal>
    <div class="flex-col-center">
      <VbenButton class="mb-3" type="primary" @click="handleUpdateTitle()">
        内部动态修改标题
      </VbenButton>
      <VbenButton class="mb-3" @click="handleToggleFullscreen()">
        {{ state.fullscreen ? '退出全屏' : '打开全屏' }}
      </VbenButton>
    </div>
  </Modal>
</template>
