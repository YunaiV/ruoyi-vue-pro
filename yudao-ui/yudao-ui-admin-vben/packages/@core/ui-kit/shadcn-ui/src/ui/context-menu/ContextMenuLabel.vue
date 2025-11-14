<script setup lang="ts">
import type { ContextMenuLabelProps } from 'radix-vue';

import { computed } from 'vue';

import { cn } from '@vben-core/shared/utils';

import { ContextMenuLabel } from 'radix-vue';

const props = defineProps<
  ContextMenuLabelProps & { class?: any; inset?: boolean }
>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});
</script>

<template>
  <ContextMenuLabel
    v-bind="delegatedProps"
    :class="
      cn(
        'text-foreground px-2 py-1.5 text-sm font-semibold',
        inset && 'pl-8',
        props.class,
      )
    "
  >
    <slot></slot>
  </ContextMenuLabel>
</template>
