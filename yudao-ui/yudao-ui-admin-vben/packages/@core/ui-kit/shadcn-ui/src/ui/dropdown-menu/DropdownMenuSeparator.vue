<script setup lang="ts">
import type { DropdownMenuSeparatorProps } from 'radix-vue';

import { computed } from 'vue';

import { cn } from '@vben-core/shared/utils';

import { DropdownMenuSeparator } from 'radix-vue';

const props = defineProps<
  DropdownMenuSeparatorProps & {
    class?: any;
  }
>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});
</script>

<template>
  <DropdownMenuSeparator
    v-bind="delegatedProps"
    :class="cn('bg-border -mx-1 my-1 h-px', props.class)"
  />
</template>
