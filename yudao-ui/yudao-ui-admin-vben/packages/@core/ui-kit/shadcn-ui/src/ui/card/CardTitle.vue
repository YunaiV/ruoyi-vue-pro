<script setup lang="ts">
import { cn } from '@vben-core/shared/utils';

const props = defineProps<{
  class?: any;
}>();
</script>

<template>
  <h3 :class="cn('font-semibold leading-none tracking-tight', props.class)">
    <slot></slot>
  </h3>
</template>
