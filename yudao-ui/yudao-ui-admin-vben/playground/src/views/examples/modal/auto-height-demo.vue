<script lang="ts" setup>
import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Button, message } from 'ant-design-vue';

const list = ref<number[]>([]);

const [Modal, modalApi] = useVbenModal({
  onCancel() {
    modalApi.close();
  },
  onConfirm() {
    message.info('onConfirm');
  },
  onOpenChange(isOpen) {
    if (isOpen) {
      handleUpdate();
    }
  },
});

function handleUpdate(len?: number) {
  modalApi.setState({ confirmDisabled: true, loading: true });
  setTimeout(() => {
    list.value = Array.from(
      { length: len ?? Math.floor(Math.random() * 10) + 1 },
      (_v, k) => k + 1,
    );
    modalApi.setState({ confirmDisabled: false, loading: false });
  }, 2000);
}
</script>

<template>
  <Modal title="自动计算高度">
    <div
      v-for="item in list"
      :key="item"
      class="even:bg-heavy bg-muted flex-center h-[220px] w-full"
    >
      {{ item }}
    </div>
    <template #prepend-footer>
      <Button type="link" @click="handleUpdate()">点击更新数据</Button>
    </template>
  </Modal>
</template>
