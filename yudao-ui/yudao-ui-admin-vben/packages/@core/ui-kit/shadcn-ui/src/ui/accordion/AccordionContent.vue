<script setup lang="ts">
import type { AccordionContentProps } from 'radix-vue';

import { computed } from 'vue';

import { cn } from '@vben-core/shared/utils';

import { AccordionContent } from 'radix-vue';

const props = defineProps<AccordionContentProps & { class?: any }>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});
</script>

<template>
  <AccordionContent
    v-bind="delegatedProps"
    class="data-[state=closed]:animate-accordion-up data-[state=open]:animate-accordion-down overflow-hidden text-sm"
  >
    <div :class="cn('pb-4 pt-0', props.class)">
      <slot></slot>
    </div>
  </AccordionContent>
</template>
