<script setup lang="ts">
import type { UserCardProperty } from './config';

import { useVModel } from '@vueuse/core';

import ComponentContainerProperty from '#/components/diy-editor/components/component-container-property.vue';

// 用户卡片属性面板
defineOptions({ name: 'UserCardProperty' });

const props = defineProps<{ modelValue: UserCardProperty }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <ComponentContainerProperty v-model="formData.style" />
</template>

<style scoped lang="scss"></style>
