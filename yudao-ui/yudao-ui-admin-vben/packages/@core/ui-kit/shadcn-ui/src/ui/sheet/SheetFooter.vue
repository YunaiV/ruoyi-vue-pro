<script setup lang="ts">
import { cn } from '@vben-core/shared/utils';

const props = defineProps<{ class?: any }>();
</script>

<template>
  <div
    :class="
      cn('flex flex-row flex-col-reverse justify-end gap-x-2', props.class)
    "
  >
    <slot></slot>
  </div>
</template>
