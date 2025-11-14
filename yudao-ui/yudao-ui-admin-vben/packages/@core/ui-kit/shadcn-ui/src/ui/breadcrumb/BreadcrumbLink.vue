<script lang="ts" setup>
import type { PrimitiveProps } from 'radix-vue';

import { cn } from '@vben-core/shared/utils';

import { Primitive } from 'radix-vue';

const props = withDefaults(defineProps<PrimitiveProps & { class?: any }>(), {
  as: 'a',
});
</script>

<template>
  <Primitive
    :as="as"
    :as-child="asChild"
    :class="cn('hover:text-foreground transition-colors', props.class)"
  >
    <slot></slot>
  </Primitive>
</template>
