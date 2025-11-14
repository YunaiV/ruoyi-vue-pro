<script setup lang="ts">
import type { TooltipRootEmits, TooltipRootProps } from 'radix-vue';

import { TooltipRoot, useForwardPropsEmits } from 'radix-vue';

const props = defineProps<TooltipRootProps>();
const emits = defineEmits<TooltipRootEmits>();

const forwarded = useForwardPropsEmits(props, emits);
</script>

<template>
  <TooltipRoot v-bind="forwarded">
    <slot></slot>
  </TooltipRoot>
</template>
