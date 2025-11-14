<script setup lang="ts">
import type { UserOrderProperty } from './config';

import { useVModel } from '@vueuse/core';

import ComponentContainerProperty from '#/components/diy-editor/components/component-container-property.vue';

// 用户订单属性面板
defineOptions({ name: 'UserOrderProperty' });

const props = defineProps<{ modelValue: UserOrderProperty }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <ComponentContainerProperty v-model="formData.style" />
</template>

<style scoped lang="scss"></style>
