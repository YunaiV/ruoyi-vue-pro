<script setup lang="ts">
import { cn } from '@vben-core/shared/utils';

import { NumberFieldInput } from 'reka-ui';
</script>

<template>
  <NumberFieldInput
    :class="
      cn(
        'flex h-9 w-full rounded-md border border-input bg-transparent py-1 text-center text-sm shadow-sm transition-colors placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50',
      )
    "
    data-slot="input"
  />
</template>
