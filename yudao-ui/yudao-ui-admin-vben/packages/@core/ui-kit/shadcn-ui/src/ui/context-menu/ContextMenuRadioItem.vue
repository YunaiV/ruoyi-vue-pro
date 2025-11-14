<script setup lang="ts">
import type {
  ContextMenuRadioItemEmits,
  ContextMenuRadioItemProps,
} from 'radix-vue';

import { computed } from 'vue';

import { cn } from '@vben-core/shared/utils';

import { Circle } from 'lucide-vue-next';
import {
  ContextMenuItemIndicator,
  ContextMenuRadioItem,
  useForwardPropsEmits,
} from 'radix-vue';

const props = defineProps<ContextMenuRadioItemProps & { class?: any }>();
const emits = defineEmits<ContextMenuRadioItemEmits>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});

const forwarded = useForwardPropsEmits(delegatedProps, emits);
</script>

<template>
  <ContextMenuRadioItem
    v-bind="forwarded"
    :class="
      cn(
        'focus:bg-accent focus:text-accent-foreground relative flex cursor-default select-none items-center rounded-sm py-1.5 pl-8 pr-2 text-sm outline-none data-[disabled]:pointer-events-none data-[disabled]:opacity-50',
        props.class,
      )
    "
  >
    <span class="absolute left-2 flex h-3.5 w-3.5 items-center justify-center">
      <ContextMenuItemIndicator>
        <Circle class="h-2 w-2 fill-current" />
      </ContextMenuItemIndicator>
    </span>
    <slot></slot>
  </ContextMenuRadioItem>
</template>
