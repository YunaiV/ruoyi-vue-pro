<script lang="ts" setup>
import { computed } from 'vue';

import { ElInput } from 'element-plus';

const props = defineProps<{
  modelValue?: null | string;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', v: null | string): void;
  (e: 'input', v: null | string): void;
}>();

const content = computed({
  get: () => props.modelValue,
  set: (val: null | string) => {
    emit('update:modelValue', val);
    emit('input', val);
  },
});
</script>

<template>
  <ElInput
    type="textarea"
    :rows="5"
    placeholder="请输入内容"
    v-model="content"
    class="w-full"
  />
</template>
