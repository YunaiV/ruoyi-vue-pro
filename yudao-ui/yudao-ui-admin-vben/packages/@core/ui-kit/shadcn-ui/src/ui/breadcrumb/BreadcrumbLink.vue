<script lang="ts" setup>
import type { PrimitiveProps } from 'reka-ui';

import { cn } from '@vben-core/shared/utils';

import { Primitive } from 'reka-ui';

const props = withDefaults(defineProps<PrimitiveProps & { class?: any }>(), {
  as: 'a',
});
</script>

<template>
  <Primitive
    :as="as"
    :as-child="asChild"
    :class="cn('transition-colors hover:text-foreground', props.class)"
  >
    <slot></slot>
  </Primitive>
</template>
