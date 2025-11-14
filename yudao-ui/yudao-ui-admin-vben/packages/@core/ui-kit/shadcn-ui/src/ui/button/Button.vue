<script setup lang="ts">
import type { PrimitiveProps } from 'radix-vue';

import type { ButtonVariants, ButtonVariantSize } from './types';

import { cn } from '@vben-core/shared/utils';

import { Primitive } from 'radix-vue';

import { buttonVariants } from './button';

interface Props extends PrimitiveProps {
  class?: any;
  size?: ButtonVariantSize;
  variant?: ButtonVariants;
}

const props = withDefaults(defineProps<Props>(), {
  as: 'button',
  class: '',
});
</script>

<template>
  <Primitive
    :as="as"
    :as-child="asChild"
    :class="cn(buttonVariants({ variant, size }), props.class)"
  >
    <slot></slot>
  </Primitive>
</template>
