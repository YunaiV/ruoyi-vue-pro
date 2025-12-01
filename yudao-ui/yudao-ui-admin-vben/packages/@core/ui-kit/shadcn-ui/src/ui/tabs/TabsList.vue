<script setup lang="ts">
import type { TabsListProps } from 'reka-ui';

import { computed } from 'vue';

import { cn } from '@vben-core/shared/utils';

import { TabsList } from 'reka-ui';

const props = defineProps<TabsListProps & { class?: any }>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});
</script>

<template>
  <TabsList
    v-bind="delegatedProps"
    :class="
      cn(
        'inline-flex h-9 items-center justify-center rounded-md bg-muted p-1 text-muted-foreground',
        props.class,
      )
    "
  >
    <slot></slot>
  </TabsList>
</template>
