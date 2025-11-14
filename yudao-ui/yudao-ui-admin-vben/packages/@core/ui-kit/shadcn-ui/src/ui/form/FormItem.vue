<script lang="ts" setup>
import { provide, useId } from 'vue';

import { cn } from '@vben-core/shared/utils';

import { FORM_ITEM_INJECTION_KEY } from './injectionKeys';

const props = defineProps<{
  class?: any;
}>();

const id = useId() as string;
provide(FORM_ITEM_INJECTION_KEY, id);
</script>

<template>
  <div :class="cn(props.class)">
    <slot></slot>
  </div>
</template>
