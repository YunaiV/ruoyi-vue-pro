<script setup lang="ts">
import { cn } from '@vben-core/shared/utils';

import { NumberFieldInput } from 'radix-vue';
</script>

<template>
  <NumberFieldInput
    :class="
      cn(
        'border-input placeholder:text-muted-foreground focus-visible:ring-ring flex h-9 w-full rounded-md border bg-transparent py-1 text-center text-sm shadow-sm transition-colors focus-visible:outline-none focus-visible:ring-1 disabled:cursor-not-allowed disabled:opacity-50',
      )
    "
    data-slot="input"
  />
</template>
