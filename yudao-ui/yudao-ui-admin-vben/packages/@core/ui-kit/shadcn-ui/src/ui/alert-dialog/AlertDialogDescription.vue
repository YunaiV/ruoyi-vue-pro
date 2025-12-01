<script lang="ts" setup>
import type { AlertDialogDescriptionProps } from 'reka-ui';

import { computed } from 'vue';

import { cn } from '@vben-core/shared/utils';

import { AlertDialogDescription, useForwardProps } from 'reka-ui';

const props = defineProps<AlertDialogDescriptionProps & { class?: any }>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});

const forwardedProps = useForwardProps(delegatedProps);
</script>

<template>
  <AlertDialogDescription
    v-bind="forwardedProps"
    :class="cn('text-sm text-muted-foreground', props.class)"
  >
    <slot></slot>
  </AlertDialogDescription>
</template>
