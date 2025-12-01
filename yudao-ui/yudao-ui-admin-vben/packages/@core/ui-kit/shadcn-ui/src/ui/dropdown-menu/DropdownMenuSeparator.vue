<script setup lang="ts">
import type { DropdownMenuSeparatorProps } from 'reka-ui';

import { computed } from 'vue';

import { cn } from '@vben-core/shared/utils';

import { DropdownMenuSeparator } from 'reka-ui';

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
    :class="cn('-mx-1 my-1 h-px bg-border', props.class)"
  />
</template>
