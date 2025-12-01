<script setup lang="ts">
import type { DropdownMenuGroupProps } from 'reka-ui';

import { DropdownMenuGroup } from 'reka-ui';

const props = defineProps<DropdownMenuGroupProps>();
</script>

<template>
  <DropdownMenuGroup v-bind="props">
    <slot></slot>
  </DropdownMenuGroup>
</template>
