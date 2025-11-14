<script setup lang="ts">
import { cn } from '@vben-core/shared/utils';

const props = defineProps<{
  class?: any;
}>();
</script>

<template>
  <span
    :class="
      cn('text-muted-foreground ml-auto text-xs tracking-widest', props.class)
    "
  >
    <slot></slot>
  </span>
</template>
