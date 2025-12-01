<script setup lang="ts">
import type { DialogCloseProps } from 'reka-ui';

import { DialogClose } from 'reka-ui';

const props = defineProps<DialogCloseProps>();
</script>

<template>
  <DialogClose v-bind="props">
    <slot></slot>
  </DialogClose>
</template>
