<script setup lang="ts">
import type { PaginationNextProps } from 'radix-vue';

import { computed } from 'vue';

import { cn } from '@vben-core/shared/utils';

import { ChevronRight } from 'lucide-vue-next';
import { PaginationNext } from 'radix-vue';

import { Button } from '../button';

const props = withDefaults(
  defineProps<PaginationNextProps & { class?: any }>(),
  {
    asChild: true,
  },
);

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});
</script>

<template>
  <PaginationNext v-bind="delegatedProps">
    <Button :class="cn('size-8 p-0', props.class)" variant="outline">
      <slot>
        <ChevronRight class="size-4" />
      </slot>
    </Button>
  </PaginationNext>
</template>
