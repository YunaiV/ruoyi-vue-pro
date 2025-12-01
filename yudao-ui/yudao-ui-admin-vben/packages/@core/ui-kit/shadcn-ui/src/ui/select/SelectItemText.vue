<script setup lang="ts">
import type { SelectItemTextProps } from 'reka-ui';

import { SelectItemText } from 'reka-ui';

const props = defineProps<SelectItemTextProps>();
</script>

<template>
  <SelectItemText v-bind="props">
    <slot></slot>
  </SelectItemText>
</template>
