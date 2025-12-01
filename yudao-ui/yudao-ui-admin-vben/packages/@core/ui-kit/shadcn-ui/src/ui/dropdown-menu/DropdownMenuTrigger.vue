<script setup lang="ts">
import type { DropdownMenuTriggerProps } from 'reka-ui';

import { DropdownMenuTrigger, useForwardProps } from 'reka-ui';

const props = defineProps<DropdownMenuTriggerProps>();

const forwardedProps = useForwardProps(props);
</script>

<template>
  <DropdownMenuTrigger class="outline-none" v-bind="forwardedProps">
    <slot></slot>
  </DropdownMenuTrigger>
</template>
