<script setup lang="ts">
import type { PinInputRootEmits, PinInputRootProps } from 'radix-vue';

import { computed } from 'vue';

import { cn } from '@vben-core/shared/utils';

import { PinInputRoot, useForwardPropsEmits } from 'radix-vue';

const props = defineProps<PinInputRootProps & { class?: any }>();
const emits = defineEmits<PinInputRootEmits>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;
  return delegated;
});

const forwarded = useForwardPropsEmits(delegatedProps, emits);
</script>

<template>
  <PinInputRoot
    v-bind="forwarded"
    :class="cn('flex items-center gap-2', props.class)"
  >
    <slot></slot>
  </PinInputRoot>
</template>
