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
        'text-muted-foreground flex flex-wrap items-center gap-1.5 break-words text-sm sm:gap-2.5',
        props.class,
      )
    "
  >
    <slot></slot>
  </ol>
</template>
