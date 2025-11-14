<script setup lang="ts">
import type { PinInputInputProps } from 'radix-vue';

import { computed } from 'vue';

import { cn } from '@vben-core/shared/utils';

import { PinInputInput, useForwardProps } from 'radix-vue';

const props = defineProps<PinInputInputProps & { class?: any }>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;
  return delegated;
});

const forwardedProps = useForwardProps(delegatedProps);
</script>

<template>
  <PinInputInput
    v-bind="forwardedProps"
    :class="
      cn(
        'border-input bg-background relative flex h-10 w-8 items-center justify-center border-y border-r text-center text-sm transition-all first:rounded-l-md first:border-l last:rounded-r-md focus:relative focus:z-10 focus:outline-none focus:ring-2 md:w-10',
        props.class,
      )
    "
  />
</template>
