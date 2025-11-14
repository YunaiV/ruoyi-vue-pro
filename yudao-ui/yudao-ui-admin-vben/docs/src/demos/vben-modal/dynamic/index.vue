<script lang="ts" setup>
import { useVbenModal, VbenButton } from '@vben/common-ui';

import ExtraModal from './modal.vue';

const [Modal, modalApi] = useVbenModal({
  // 连接抽离的组件
  connectedComponent: ExtraModal,
});

function openModal() {
  modalApi.open();
}

function handleUpdateTitle() {
  modalApi.setState({ title: '外部动态标题' }).open();
}
</script>

<template>
  <div>
    <Modal />

    <VbenButton @click="openModal">Open</VbenButton>
    <VbenButton class="ml-2" type="primary" @click="handleUpdateTitle">
      从外部修改标题并打开
    </VbenButton>
  </div>
</template>
