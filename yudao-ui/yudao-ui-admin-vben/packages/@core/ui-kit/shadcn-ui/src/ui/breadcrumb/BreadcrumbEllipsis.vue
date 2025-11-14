<script lang="ts" setup>
import { cn } from '@vben-core/shared/utils';

import { MoreHorizontal } from 'lucide-vue-next';

const props = defineProps<{
  class?: any;
}>();
</script>

<template>
  <span
    :class="cn('flex h-9 w-9 items-center justify-center', props.class)"
    aria-hidden="true"
    role="presentation"
  >
    <slot>
      <MoreHorizontal class="h-4 w-4" />
    </slot>
    <span class="sr-only">More</span>
  </span>
</template>
