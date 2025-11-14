<script setup lang="ts">
import { cn } from '@vben-core/shared/utils';

const props = defineProps<{
  class?: any;
}>();
</script>

<template>
  <div :class="cn('p-6 pt-0', props.class)">
    <slot></slot>
  </div>
</template>
