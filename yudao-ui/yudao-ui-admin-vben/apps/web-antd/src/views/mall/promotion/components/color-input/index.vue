<script setup lang="ts">
import { computed } from 'vue';

import { Input } from 'ant-design-vue';

/** 颜色输入框 */
defineOptions({ name: 'ColorInput' });

const props = defineProps({
  modelValue: {
    type: String,
    default: '',
  },
});

const emit = defineEmits(['update:modelValue']);

const color = computed({
  get: () => {
    return props.modelValue;
  },
  set: (val: string) => {
    emit('update:modelValue', val);
  },
});
</script>

<template>
  <div class="flex gap-2">
    <input
      v-model="color"
      type="color"
      class="h-8 w-12 cursor-pointer rounded border border-gray-300"
    />
    <Input v-model:value="color" class="flex-1" placeholder="请输入颜色值" />
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
