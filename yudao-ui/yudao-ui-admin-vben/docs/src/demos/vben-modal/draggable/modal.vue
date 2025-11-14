<script lang="ts" setup>
import { useVbenModal } from '@vben/common-ui';

const [Modal] = useVbenModal({
  draggable: true,
});
</script>
<template>
  <Modal title="拖拽示例"> modal content </Modal>
</template>
