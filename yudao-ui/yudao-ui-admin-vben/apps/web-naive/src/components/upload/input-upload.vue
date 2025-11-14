<script setup lang="ts">
import type { InputProps } from 'naive-ui';

import type { FileUploadProps } from './typing';

import { computed } from 'vue';

import { useVModel } from '@vueuse/core';
import { NGrid, NGridItem, NInput } from 'naive-ui';

import FileUpload from './file-upload.vue';

const props = defineProps<{
  defaultValue?: string;
  fileUploadProps?: FileUploadProps;
  inputProps?: InputProps;
  inputType?: 'input' | 'textarea';
  modelValue?: string;
  textareaProps?: InputProps;
}>();

const emits = defineEmits<{
  (e: 'change', payload: string): void;
  (e: 'update:value', payload: string): void;
  (e: 'update:modelValue', payload: string): void;
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

const inputPropsComputed = computed(() => {
  return {
    ...props.inputProps,
    value: modelValue.value as string,
  };
});

const textareaPropsComputed = computed(() => {
  return {
    ...props.textareaProps,
    value: modelValue.value as string,
  };
});

const fileUploadProps = computed(() => {
  return {
    ...props.fileUploadProps,
  };
});
</script>

<template>
  <NGrid :cols="24" :x-gap="12">
    <NGridItem :span="18">
      <NInput
        v-if="inputType === 'input'"
        readonly
        v-bind="inputPropsComputed"
      />
      <NInput
        v-else
        readonly
        type="textarea"
        :rows="4"
        v-bind="textareaPropsComputed"
      />
    </NGridItem>
    <NGridItem :span="6">
      <FileUpload v-bind="fileUploadProps" @return-text="handleReturnText" />
    </NGridItem>
  </NGrid>
</template>
