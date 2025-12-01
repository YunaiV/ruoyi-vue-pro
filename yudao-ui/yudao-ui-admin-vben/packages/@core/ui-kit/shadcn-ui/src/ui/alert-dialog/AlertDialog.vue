<script setup lang="ts">
import type { AlertDialogEmits, AlertDialogProps } from 'reka-ui';

import { AlertDialogRoot, useForwardPropsEmits } from 'reka-ui';

const props = defineProps<AlertDialogProps>();
const emits = defineEmits<AlertDialogEmits>();

const forwarded = useForwardPropsEmits(props, emits);
</script>

<template>
  <AlertDialogRoot v-bind="forwarded">
    <slot></slot>
  </AlertDialogRoot>
</template>
