<script setup lang="ts">
import { cn } from '@vben-core/shared/utils';

import { useVModel } from '@vueuse/core';

const props = defineProps<{
  class?: any;
  defaultValue?: number | string;
  modelValue?: number | string;
}>();

const emits = defineEmits<{
  (e: 'update:modelValue', payload: number | string): void;
}>();

const modelValue = useVModel(props, 'modelValue', emits, {
  defaultValue: props.defaultValue,
  passive: true,
});
</script>

<template>
  <input
    v-model="modelValue"
    :class="
      cn(
        'border-input bg-background ring-offset-background placeholder:text-muted-foreground focus-visible:ring-ring flex h-10 w-full rounded-md border px-3 py-2 text-sm file:border-0 file:bg-transparent file:text-sm file:font-medium focus-visible:outline-none focus-visible:ring-1 disabled:cursor-not-allowed disabled:opacity-50',
        props.class,
      )
    "
  />
</template>
<style lang="scss" scoped>
input {
  --ring: var(--primary);
}
</style>
