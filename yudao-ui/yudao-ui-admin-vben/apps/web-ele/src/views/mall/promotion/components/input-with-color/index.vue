<script lang="ts" setup>
import { PREDEFINE_COLORS } from '@vben/constants';

import { useVModels } from '@vueuse/core';
import { ElColorPicker, ElInput } from 'element-plus';

/** 带颜色选择器输入框 */
defineOptions({ name: 'InputWithColor' });

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
  color: {
    type: String,
    default: '',
  },
});

const emit = defineEmits(['update:modelValue', 'update:color']);

const { modelValue, color } = useVModels(props, emit);
</script>

<template>
  <ElInput v-model="modelValue" v-bind="$attrs">
    <template #append>
      <ElColorPicker v-model="color" :predefine="PREDEFINE_COLORS" />
    </template>
  </ElInput>
</template>
<style scoped lang="scss">
:deep(.el-input-group__append) {
  padding: 0;

  .el-color-picker__trigger {
    padding: 0;
    border-left: none;
    border-radius: 0 var(--el-input-border-radius) var(--el-input-border-radius)
      0;
  }
}
</style>
