<script setup lang="ts">
import type { ContextMenuPortalProps } from 'reka-ui';

import { ContextMenuPortal } from 'reka-ui';

const props = defineProps<ContextMenuPortalProps>();
</script>

<template>
  <ContextMenuPortal v-bind="props">
    <slot></slot>
  </ContextMenuPortal>
</template>
