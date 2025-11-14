<script setup lang="ts">
import type {
  ContextMenuRadioGroupEmits,
  ContextMenuRadioGroupProps,
} from 'radix-vue';

import { ContextMenuRadioGroup, useForwardPropsEmits } from 'radix-vue';

const props = defineProps<ContextMenuRadioGroupProps>();
const emits = defineEmits<ContextMenuRadioGroupEmits>();

const forwarded = useForwardPropsEmits(props, emits);
</script>

<template>
  <ContextMenuRadioGroup v-bind="forwarded">
    <slot></slot>
  </ContextMenuRadioGroup>
</template>
