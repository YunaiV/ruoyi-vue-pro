<script setup lang="ts">
import type { DropdownMenuRootEmits, DropdownMenuRootProps } from 'reka-ui';

import { DropdownMenuRoot, useForwardPropsEmits } from 'reka-ui';

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
