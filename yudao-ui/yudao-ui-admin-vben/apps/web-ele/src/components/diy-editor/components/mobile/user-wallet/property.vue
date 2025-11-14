<script setup lang="ts">
import type { UserWalletProperty } from './config';

import { useVModel } from '@vueuse/core';

import ComponentContainerProperty from '#/components/diy-editor/components/component-container-property.vue';

// 用户资产属性面板
defineOptions({ name: 'UserWalletProperty' });

const props = defineProps<{ modelValue: UserWalletProperty }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <ComponentContainerProperty v-model="formData.style" />
</template>

<style scoped lang="scss"></style>
