<script lang="ts" setup>
import { cn } from '@vben-core/shared/utils';

const props = defineProps<{
  class?: any;
}>();
</script>

<template>
  <span
    :class="cn('text-foreground font-normal', props.class)"
    aria-current="page"
    aria-disabled="true"
    role="link"
  >
    <slot></slot>
  </span>
</template>
