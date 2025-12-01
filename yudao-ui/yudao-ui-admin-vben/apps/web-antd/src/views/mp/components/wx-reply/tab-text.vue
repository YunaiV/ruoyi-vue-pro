<script lang="ts" setup>
import { computed } from 'vue';

import { Textarea } from 'ant-design-vue';

const props = defineProps<{
  modelValue?: null | string;
}>();

const emit = defineEmits<{
  (e: 'input', v: null | string): void;
  (e: 'update:modelValue', v: null | string): void;
}>();

const content = computed({
  get: () => props.modelValue ?? '',
  set: (val: string) => {
    emit('update:modelValue', val || null);
    emit('input', val || null);
  },
});
</script>

<template>
  <Textarea v-model:value="content" :rows="5" placeholder="请输入内容" />
</template>
