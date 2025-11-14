<script setup lang="ts">
import type { DropdownMenuSubEmits, DropdownMenuSubProps } from 'radix-vue';

import { DropdownMenuSub, useForwardPropsEmits } from 'radix-vue';

const props = defineProps<DropdownMenuSubProps>();
const emits = defineEmits<DropdownMenuSubEmits>();

const forwarded = useForwardPropsEmits(props, emits);
</script>

<template>
  <DropdownMenuSub v-bind="forwarded">
    <slot></slot>
  </DropdownMenuSub>
</template>
