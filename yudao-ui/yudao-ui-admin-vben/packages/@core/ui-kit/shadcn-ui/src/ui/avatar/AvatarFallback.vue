<script setup lang="ts">
import type { AvatarFallbackProps } from 'radix-vue';

import { AvatarFallback } from 'radix-vue';

const props = defineProps<AvatarFallbackProps>();
</script>

<template>
  <AvatarFallback v-bind="props">
    <slot></slot>
  </AvatarFallback>
</template>
