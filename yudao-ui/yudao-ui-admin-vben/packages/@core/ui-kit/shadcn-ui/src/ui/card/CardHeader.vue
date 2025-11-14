<script setup lang="ts">
import { cn } from '@vben-core/shared/utils';

const props = defineProps<{
  class?: any;
}>();
</script>

<template>
  <div :class="cn('flex flex-col gap-y-1.5 p-5', props.class)">
    <slot></slot>
  </div>
</template>
