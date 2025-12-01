<script setup lang="ts">
import type { ContextMenuGroupProps } from 'reka-ui';

import { ContextMenuGroup } from 'reka-ui';

const props = defineProps<ContextMenuGroupProps>();
</script>

<template>
  <ContextMenuGroup v-bind="props">
    <slot></slot>
  </ContextMenuGroup>
</template>
