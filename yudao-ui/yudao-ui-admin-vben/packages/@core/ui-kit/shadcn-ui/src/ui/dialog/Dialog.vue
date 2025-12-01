<script setup lang="ts">
import type { DialogRootEmits, DialogRootProps } from 'reka-ui';

import { DialogRoot, useForwardPropsEmits } from 'reka-ui';

const props = defineProps<DialogRootProps>();
const emits = defineEmits<DialogRootEmits>();

const forwarded = useForwardPropsEmits(props, emits);
</script>

<template>
  <DialogRoot v-bind="forwarded">
    <slot></slot>
  </DialogRoot>
</template>
