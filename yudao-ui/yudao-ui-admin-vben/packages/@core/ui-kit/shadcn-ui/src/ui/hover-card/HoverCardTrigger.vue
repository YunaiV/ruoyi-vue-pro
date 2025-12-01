<script setup lang="ts">
import type { HoverCardTriggerProps } from 'reka-ui';

import { HoverCardTrigger } from 'reka-ui';

const props = defineProps<HoverCardTriggerProps>();
</script>

<template>
  <HoverCardTrigger v-bind="props">
    <slot></slot>
  </HoverCardTrigger>
</template>
