<script lang="ts" setup>
import { useVbenModal, VbenButton } from '@vben/common-ui';

const [Modal, modalApi] = useVbenModal();
</script>
<template>
  <div>
    <VbenButton @click="() => modalApi.open()">Open</VbenButton>
    <Modal class="w-[600px]" title="基础示例"> modal content </Modal>
  </div>
</template>
