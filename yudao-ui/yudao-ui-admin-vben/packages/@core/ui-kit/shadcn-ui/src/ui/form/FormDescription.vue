<script lang="ts" setup>
import { cn } from '@vben-core/shared/utils';

import { useFormField } from './useFormField';

const props = defineProps<{
  class?: any;
}>();

const { formDescriptionId } = useFormField();
</script>

<template>
  <p
    :id="formDescriptionId"
    :class="cn('text-sm text-muted-foreground', props.class)"
  >
    <slot></slot>
  </p>
</template>
