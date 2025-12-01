<script setup lang="ts">
import type {
  ContextMenuRadioGroupEmits,
  ContextMenuRadioGroupProps,
} from 'reka-ui';

import { ContextMenuRadioGroup, useForwardPropsEmits } from 'reka-ui';

const props = defineProps<ContextMenuRadioGroupProps>();
const emits = defineEmits<ContextMenuRadioGroupEmits>();

const forwarded = useForwardPropsEmits(props, emits);
</script>

<template>
  <ContextMenuRadioGroup v-bind="forwarded">
    <slot></slot>
  </ContextMenuRadioGroup>
</template>
