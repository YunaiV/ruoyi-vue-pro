<script setup lang="ts">
import type { InputProps, TextareaProps } from 'tdesign-vue-next';

import type { FileUploadProps } from './typing';

import { computed } from 'vue';

import { useVModel } from '@vueuse/core';
import { Col, Input, Row, Textarea } from 'tdesign-vue-next';

import FileUpload from './file-upload.vue';

const props = defineProps<{
  defaultValue?: number | string;
  fileUploadProps?: FileUploadProps;
  inputProps?: InputProps;
  inputType?: 'input' | 'textarea';
  modelValue?: number | string;
  textareaProps?: TextareaProps;
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
  <Row>
    <Col :span="18">
      <Input readonly v-if="inputType === 'input'" v-bind="inputProps" />
      <Textarea readonly v-else :row="4" v-bind="textareaProps" />
    </Col>
    <Col :span="6">
      <FileUpload
        class="ml-4"
        v-bind="fileUploadProps"
        @return-text="handleReturnText"
      />
    </Col>
  </Row>
</template>
