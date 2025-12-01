<script setup lang="ts">
import type { PopoverTriggerProps } from 'reka-ui';

import { PopoverTrigger } from 'reka-ui';

const props = defineProps<PopoverTriggerProps>();
</script>

<template>
  <PopoverTrigger v-bind="props">
    <slot></slot>
  </PopoverTrigger>
</template>
