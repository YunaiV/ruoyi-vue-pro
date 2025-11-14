<script lang="ts" setup>
import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Input, message } from 'ant-design-vue';

const [Modal, modalApi] = useVbenModal({
  destroyOnClose: false,
  onCancel() {
    modalApi.close();
  },
  onConfirm() {
    message.info('onConfirm');
    // modalApi.close();
  },
});
const value = ref();
</script>
<template>
  <Modal
    append-to-main
    class="w-[600px]"
    title="基础弹窗示例"
    title-tooltip="标题提示内容"
  >
    此弹窗指定在内容区域打开，并且在关闭之后弹窗内容不会被销毁
    <Input v-model:value="value" placeholder="KeepAlive测试" />
  </Modal>
</template>
