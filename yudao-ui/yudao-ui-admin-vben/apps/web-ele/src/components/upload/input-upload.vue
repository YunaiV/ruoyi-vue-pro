<script setup lang="ts">
import type { InputProps } from 'element-plus';

import type { FileUploadProps } from './typing';

import { computed } from 'vue';

import { useVModel } from '@vueuse/core';
import { ElCol, ElInput, ElRow } from 'element-plus';

import FileUpload from './file-upload.vue';

const props = defineProps<{
  defaultValue?: number | string;
  fileUploadProps?: FileUploadProps;
  inputProps?: InputProps;
  inputType?: 'input' | 'textarea';
  modelValue?: number | string;
  textareaProps?: InputProps;
}>();

const emits = defineEmits<{
  (e: 'change', payload: number | string): void;
  (e: 'update:value', payload: number | string): void;
  (e: 'update:modelValue', payload: number | string): void;
}>();

const modelValue = useVModel(props, 'modelValue', emits, {
  defaultValue: props.defaultValue,
  passive: true,
});

function handleReturnText(text: string) {
  modelValue.value = text;
  emits('change', modelValue.value);
  emits('update:value', modelValue.value);
  emits('update:modelValue', modelValue.value);
}

const inputProps = computed(() => {
  return {
    ...props.inputProps,
    value: modelValue.value,
  };
});

const textareaProps = computed(() => {
  return {
    ...props.textareaProps,
    value: modelValue.value,
  };
});

const fileUploadProps = computed(() => {
  return {
    ...props.fileUploadProps,
  };
});
</script>
<template>
  <ElRow>
    <ElCol :span="18">
      <ElInput v-if="inputType === 'input'" v-bind="inputProps" />
      <ElInput v-else :row="4" type="textarea" v-bind="textareaProps" />
    </ElCol>
    <ElCol :span="6">
      <FileUpload
        class="ml-4"
        v-bind="fileUploadProps"
        @return-text="handleReturnText"
      />
    </ElCol>
  </ElRow>
</template>
