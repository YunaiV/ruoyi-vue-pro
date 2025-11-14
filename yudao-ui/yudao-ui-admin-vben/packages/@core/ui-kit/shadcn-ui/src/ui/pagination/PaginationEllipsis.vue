<script setup lang="ts">
import type { PaginationEllipsisProps } from 'radix-vue';

import { computed } from 'vue';

import { cn } from '@vben-core/shared/utils';

import { MoreHorizontal } from 'lucide-vue-next';
import { PaginationEllipsis } from 'radix-vue';

const props = defineProps<PaginationEllipsisProps & { class?: any }>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});
</script>

<template>
  <PaginationEllipsis
    v-bind="delegatedProps"
    :class="cn('flex size-8 items-center justify-center', props.class)"
  >
    <slot>
      <MoreHorizontal class="size-4" />
    </slot>
  </PaginationEllipsis>
</template>
