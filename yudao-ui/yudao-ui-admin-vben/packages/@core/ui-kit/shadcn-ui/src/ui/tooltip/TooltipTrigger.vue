<script setup lang="ts">
import type { TooltipTriggerProps } from 'reka-ui';

import { TooltipTrigger } from 'reka-ui';

const props = defineProps<TooltipTriggerProps>();
</script>

<template>
  <TooltipTrigger v-bind="props">
    <slot></slot>
  </TooltipTrigger>
</template>
