<script setup lang="ts">
import type { UserCouponProperty } from './config';

import { useVModel } from '@vueuse/core';

import ComponentContainerProperty from '#/components/diy-editor/components/component-container-property.vue';

// 用户卡券属性面板
defineOptions({ name: 'UserCouponProperty' });

const props = defineProps<{ modelValue: UserCouponProperty }>();
const emit = defineEmits(['update:modelValue']);
const formData = useVModel(props, 'modelValue', emit);
</script>

<template>
  <ComponentContainerProperty v-model="formData.style" />
</template>

<style scoped lang="scss"></style>
