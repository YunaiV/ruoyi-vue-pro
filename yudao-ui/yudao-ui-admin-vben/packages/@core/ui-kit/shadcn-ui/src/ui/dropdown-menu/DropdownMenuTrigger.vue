<script setup lang="ts">
import type { DropdownMenuTriggerProps } from 'radix-vue';

import { DropdownMenuTrigger, useForwardProps } from 'radix-vue';

const props = defineProps<DropdownMenuTriggerProps>();

const forwardedProps = useForwardProps(props);
</script>

<template>
  <DropdownMenuTrigger class="outline-none" v-bind="forwardedProps">
    <slot></slot>
  </DropdownMenuTrigger>
</template>
