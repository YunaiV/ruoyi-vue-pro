<script setup lang="ts">
import type { ContextMenuPortalProps } from 'radix-vue';

import { ContextMenuPortal } from 'radix-vue';

const props = defineProps<ContextMenuPortalProps>();
</script>

<template>
  <ContextMenuPortal v-bind="props">
    <slot></slot>
  </ContextMenuPortal>
</template>
