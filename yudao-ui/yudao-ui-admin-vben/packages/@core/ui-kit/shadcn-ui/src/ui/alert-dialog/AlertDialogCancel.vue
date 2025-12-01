<script setup lang="ts">
import type { AlertDialogCancelProps } from 'reka-ui';

import { AlertDialogCancel } from 'reka-ui';

const props = defineProps<AlertDialogCancelProps>();
</script>

<template>
  <AlertDialogCancel v-bind="props">
    <slot></slot>
  </AlertDialogCancel>
</template>
