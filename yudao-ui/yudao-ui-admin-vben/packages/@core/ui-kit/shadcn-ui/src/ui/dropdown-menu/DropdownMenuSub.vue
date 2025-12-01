<script setup lang="ts">
import type { DropdownMenuSubEmits, DropdownMenuSubProps } from 'reka-ui';

import { DropdownMenuSub, useForwardPropsEmits } from 'reka-ui';

const props = defineProps<DropdownMenuSubProps>();
const emits = defineEmits<DropdownMenuSubEmits>();

const forwarded = useForwardPropsEmits(props, emits);
</script>

<template>
  <DropdownMenuSub v-bind="forwarded">
    <slot></slot>
  </DropdownMenuSub>
</template>
