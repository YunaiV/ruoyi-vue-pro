<script lang="ts" setup>
import { Slot } from 'radix-vue';

import { useFormField } from './useFormField';

const { error, formDescriptionId, formItemId, formMessageId } = useFormField();
</script>

<template>
  <Slot
    :id="formItemId"
    :aria-describedby="
      !error ? `${formDescriptionId}` : `${formDescriptionId} ${formMessageId}`
    "
    :aria-invalid="!!error"
  >
    <slot></slot>
  </Slot>
</template>
