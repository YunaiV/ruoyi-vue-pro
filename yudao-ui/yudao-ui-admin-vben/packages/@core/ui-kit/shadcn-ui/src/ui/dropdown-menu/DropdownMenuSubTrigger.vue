<script setup lang="ts">
import type { DropdownMenuSubTriggerProps } from 'radix-vue';

import { computed } from 'vue';

import { cn } from '@vben-core/shared/utils';

import { ChevronRight } from 'lucide-vue-next';
import { DropdownMenuSubTrigger, useForwardProps } from 'radix-vue';

const props = defineProps<DropdownMenuSubTriggerProps & { class?: any }>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});

const forwardedProps = useForwardProps(delegatedProps);
</script>

<template>
  <DropdownMenuSubTrigger
    v-bind="forwardedProps"
    :class="
      cn(
        'focus:bg-accent data-[state=open]:bg-accent flex cursor-default select-none items-center rounded-sm px-2 py-1.5 text-sm outline-none',
        props.class,
      )
    "
  >
    <slot></slot>
    <ChevronRight class="ml-auto h-4 w-4" />
  </DropdownMenuSubTrigger>
</template>
