<script setup lang="ts">
import type { TooltipProviderProps } from 'reka-ui';

import { TooltipProvider } from 'reka-ui';

const props = defineProps<TooltipProviderProps>();
</script>

<template>
  <TooltipProvider v-bind="props">
    <slot></slot>
  </TooltipProvider>
</template>
