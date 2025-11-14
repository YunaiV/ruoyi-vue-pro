<script lang="ts" setup>
import { useVbenModal } from '@vben/common-ui';

import { Button, message } from 'ant-design-vue';

const [Modal, modalApi] = useVbenModal({
  draggable: true,
  onCancel() {
    modalApi.close();
  },
  onConfirm() {
    message.info('onConfirm');
    // modalApi.close();
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
      <Button class="mb-3" type="primary" @click="handleUpdateTitle()">
        内部动态修改标题
      </Button>
      <Button class="mb-3" type="primary" @click="handleToggleFullscreen()">
        {{ state.fullscreen ? '退出全屏' : '打开全屏' }}
      </Button>
    </div>
  </Modal>
</template>
