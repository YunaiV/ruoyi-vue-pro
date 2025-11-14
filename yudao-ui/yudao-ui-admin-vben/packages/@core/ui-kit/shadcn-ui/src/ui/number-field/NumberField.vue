<script setup lang="ts">
import type { NumberFieldRootEmits, NumberFieldRootProps } from 'radix-vue';

import { computed } from 'vue';

import { cn } from '@vben-core/shared/utils';

import { NumberFieldRoot, useForwardPropsEmits } from 'radix-vue';

const props = defineProps<NumberFieldRootProps & { class?: any }>();
const emits = defineEmits<NumberFieldRootEmits>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});

const forwarded = useForwardPropsEmits(delegatedProps, emits);
</script>

<template>
  <NumberFieldRoot v-bind="forwarded" :class="cn('grid gap-1.5', props.class)">
    <slot></slot>
  </NumberFieldRoot>
</template>
