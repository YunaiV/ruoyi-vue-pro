<script setup lang="ts">
import type { DropdownMenuRootEmits, DropdownMenuRootProps } from 'radix-vue';

import { DropdownMenuRoot, useForwardPropsEmits } from 'radix-vue';

const props = withDefaults(defineProps<DropdownMenuRootProps>(), {
  modal: false,
});
const emits = defineEmits<DropdownMenuRootEmits>();

const forwarded = useForwardPropsEmits(props, emits);
</script>

<template>
  <DropdownMenuRoot v-bind="forwarded">
    <slot></slot>
  </DropdownMenuRoot>
</template>
