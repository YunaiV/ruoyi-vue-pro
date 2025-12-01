<script lang="ts" setup>
import { cn } from '@vben-core/shared/utils';

const props = defineProps<{
  class?: any;
}>();
</script>

<template>
  <ol
    :class="
      cn(
        'flex flex-wrap items-center gap-1.5 break-words text-sm text-muted-foreground sm:gap-2.5',
        props.class,
      )
    "
  >
    <slot></slot>
  </ol>
</template>
