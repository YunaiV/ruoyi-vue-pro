<script lang="ts" setup>
import { useVModels } from '@vueuse/core';
import { Input } from 'ant-design-vue';

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
  <div class="flex gap-2">
    <Input v-model:value="modelValue" v-bind="$attrs" class="flex-1" />
    <input
      v-model="color"
      type="color"
      class="h-8 w-10 cursor-pointer rounded border border-gray-300"
    />
  </div>
</template>

<style scoped lang="scss">
input[type='color'] {
  &::-webkit-color-swatch-wrapper {
    padding: 2px;
  }

  &::-webkit-color-swatch {
    border: none;
    border-radius: 2px;
  }
}
</style>
