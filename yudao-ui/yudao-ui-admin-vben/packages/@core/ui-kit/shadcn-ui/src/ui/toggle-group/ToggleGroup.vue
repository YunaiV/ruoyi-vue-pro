<script setup lang="ts">
import type { VariantProps } from 'class-variance-authority';
import type { ToggleGroupRootEmits, ToggleGroupRootProps } from 'reka-ui';

import type { toggleVariants } from '../toggle';

import { computed, provide } from 'vue';

import { cn } from '@vben-core/shared/utils';

import { ToggleGroupRoot, useForwardPropsEmits } from 'reka-ui';

type ToggleGroupVariants = VariantProps<typeof toggleVariants>;

const props = defineProps<
  ToggleGroupRootProps & {
    class?: any;
    size?: ToggleGroupVariants['size'];
    variant?: ToggleGroupVariants['variant'];
  }
>();
const emits = defineEmits<ToggleGroupRootEmits>();

provide('toggleGroup', {
  size: props.size,
  variant: props.variant,
});

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;
  return delegated;
});

const forwarded = useForwardPropsEmits(delegatedProps, emits);
</script>

<template>
  <ToggleGroupRoot
    v-bind="forwarded"
    :class="cn('flex items-center justify-center gap-1', props.class)"
  >
    <slot></slot>
  </ToggleGroupRoot>
</template>
