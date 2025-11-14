<script setup lang="ts">
import type {
  ContextMenuContentEmits,
  ContextMenuContentProps,
} from 'radix-vue';

import { computed } from 'vue';

import { cn } from '@vben-core/shared/utils';

import {
  ContextMenuContent,
  ContextMenuPortal,
  useForwardPropsEmits,
} from 'radix-vue';

const props = defineProps<ContextMenuContentProps & { class?: any }>();
const emits = defineEmits<ContextMenuContentEmits>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});

const forwarded = useForwardPropsEmits(delegatedProps, emits);
</script>

<template>
  <ContextMenuPortal>
    <ContextMenuContent
      v-bind="forwarded"
      :class="
        cn(
          'bg-popover text-popover-foreground data-[state=open]:animate-in data-[state=closed]:animate-out data-[state=closed]:fade-out-0 data-[state=open]:fade-in-0 data-[state=closed]:zoom-out-95 data-[state=open]:zoom-in-95 data-[side=bottom]:slide-in-from-top-2 data-[side=left]:slide-in-from-right-2 data-[side=right]:slide-in-from-left-2 data-[side=top]:slide-in-from-bottom-2 border-border z-popup min-w-32 overflow-hidden rounded-md border p-1 shadow-md',
          props.class,
        )
      "
    >
      <slot></slot>
    </ContextMenuContent>
  </ContextMenuPortal>
</template>
