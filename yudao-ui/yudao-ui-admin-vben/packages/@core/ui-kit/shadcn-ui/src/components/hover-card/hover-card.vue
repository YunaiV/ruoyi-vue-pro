<script setup lang="ts">
import type {
  HoverCardContentProps,
  HoverCardRootEmits,
  HoverCardRootProps,
} from 'radix-vue';

import type { ClassType } from '@vben-core/typings';

import { computed } from 'vue';

import { useForwardPropsEmits } from 'radix-vue';

import { HoverCard, HoverCardContent, HoverCardTrigger } from '../../ui';

interface Props extends HoverCardRootProps {
  class?: ClassType;
  contentClass?: ClassType;
  contentProps?: HoverCardContentProps;
}

const props = defineProps<Props>();

const emits = defineEmits<HoverCardRootEmits>();

const delegatedProps = computed(() => {
  const {
    class: _cls,
    contentClass: _,
    contentProps: _cProps,
    ...delegated
  } = props;

  return delegated;
});

const forwarded = useForwardPropsEmits(delegatedProps, emits);
</script>

<template>
  <HoverCard v-bind="forwarded">
    <HoverCardTrigger as-child class="h-full">
      <div class="h-full cursor-pointer">
        <slot name="trigger"></slot>
      </div>
    </HoverCardTrigger>
    <HoverCardContent
      :class="contentClass"
      v-bind="contentProps"
      class="side-content z-popup"
    >
      <slot></slot>
    </HoverCardContent>
  </HoverCard>
</template>
