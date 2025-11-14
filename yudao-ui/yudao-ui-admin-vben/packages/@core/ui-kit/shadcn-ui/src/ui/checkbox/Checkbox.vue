<script setup lang="ts">
import type { CheckboxRootEmits, CheckboxRootProps } from 'radix-vue';

import { computed } from 'vue';

import { cn } from '@vben-core/shared/utils';

import { Check, Minus } from 'lucide-vue-next';
import {
  CheckboxIndicator,
  CheckboxRoot,
  useForwardPropsEmits,
} from 'radix-vue';

const props = defineProps<
  CheckboxRootProps & { class?: any; indeterminate?: boolean }
>();
const emits = defineEmits<CheckboxRootEmits>();

const delegatedProps = computed(() => {
  const { class: _, ...delegated } = props;

  return delegated;
});

const forwarded = useForwardPropsEmits(delegatedProps, emits);
</script>

<template>
  <CheckboxRoot
    v-bind="forwarded"
    :class="
      cn(
        'focus-visible:ring-ring data-[state=checked]:bg-primary data-[state=checked]:text-primary-foreground border-border peer h-4 w-4 shrink-0 rounded-sm border transition focus-visible:outline-none focus-visible:ring-1 disabled:cursor-not-allowed disabled:opacity-50',
        props.class,
      )
    "
  >
    <CheckboxIndicator
      class="flex h-full w-full items-center justify-center text-current"
    >
      <slot>
        <component :is="indeterminate ? Minus : Check" class="h-4 w-4" />
      </slot>
    </CheckboxIndicator>
  </CheckboxRoot>
</template>
