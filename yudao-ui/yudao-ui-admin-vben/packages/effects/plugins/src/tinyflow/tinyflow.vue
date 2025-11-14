<script lang="ts" setup>
import { ref } from 'vue';

import { Tinyflow } from '@tinyflow-ai/vue';

import '@tinyflow-ai/vue/dist/index.css';

defineProps<{
  data: any;
  provider: any;
}>();

const tinyflowRef = ref<InstanceType<typeof Tinyflow> | null>(null);

defineExpose({
  getData: () => tinyflowRef.value?.getData(),
});
</script>
<template>
  <Tinyflow
    ref="tinyflowRef"
    class-name="custom-class"
    :data="data"
    :provider="provider"
  />
</template>
<style scoped>
:deep(.custom-tinyflow) {
  select {
    appearance: auto !important;
  }

  /* 如果使用checkbox需要添加 */
  input[type='checkbox'] {
    width: 16px;
    height: 16px;
    margin: 0 4px;
    border: 1px solid #ccc;
  }
}

input[type='checkbox'] {
  position: relative;
  width: 18px;
  height: 18px;
  margin: 0 8px 0 0;
  cursor: pointer;
  border: 2px solid #d9d9d9;
  border-radius: 4px;
  transition: all 0.3s;

  &:checked {
    background-color: #1890ff;
    border-color: #1890ff;

    &::after {
      position: absolute;
      top: 50%;
      left: 50%;
      width: 8px;
      height: 12px;
      content: '';
      border: 2px solid #fff;
      border-top: 0;
      border-left: 0;
      transform: translate(-50%, -60%) rotate(45deg);
    }
  }

  &:hover {
    border-color: #40a9ff;
  }
}
</style>
