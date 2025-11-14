<script setup lang="ts">
import type { PrimitiveProps } from 'radix-vue';

import { Dot } from 'lucide-vue-next';
import { Primitive, useForwardProps } from 'radix-vue';

const props = defineProps<PrimitiveProps>();
const forwardedProps = useForwardProps(props);
</script>

<template>
  <Primitive v-bind="forwardedProps">
    <slot>
      <Dot />
    </slot>
  </Primitive>
</template>
