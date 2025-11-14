<script setup lang="ts">
import type { ContextMenuTriggerProps } from 'radix-vue';

import { ContextMenuTrigger, useForwardProps } from 'radix-vue';

const props = defineProps<ContextMenuTriggerProps>();

const forwardedProps = useForwardProps(props);
</script>

<template>
  <ContextMenuTrigger v-bind="forwardedProps">
    <slot></slot>
  </ContextMenuTrigger>
</template>
