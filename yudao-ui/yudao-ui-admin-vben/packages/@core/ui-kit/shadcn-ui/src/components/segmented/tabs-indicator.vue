<script setup lang="ts">
import type { TabsIndicatorProps } from 'radix-vue';

import { computed } from 'vue';

import { cn } from '@vben-core/shared/utils';

import { TabsIndicator, useForwardProps } from 'radix-vue';

const props = defineProps<TabsIndicatorProps & { class?: any }>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});

const forwardedProps = useForwardProps(delegatedProps);
</script>

<template>
  <TabsIndicator
    v-bind="forwardedProps"
    :class="
      cn(
        'absolute bottom-0 left-0 z-10 h-full w-1/2 translate-x-[--radix-tabs-indicator-position] rounded-full px-0 py-1 pr-0.5 transition-[width,transform] duration-300',
        props.class,
      )
    "
  >
    <div
      class="bg-background text-foreground inline-flex h-full w-full items-center justify-center whitespace-nowrap rounded-md px-3 py-1 text-sm font-medium focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50"
    >
      <slot></slot>
    </div>
  </TabsIndicator>
</template>
