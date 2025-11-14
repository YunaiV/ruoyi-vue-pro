<script lang="ts" setup>
import type { BacktopProps } from './backtop';

import { computed } from 'vue';

import { ArrowUpToLine } from '@vben-core/icons';

import { VbenButton } from '../button';
import { useBackTop } from './use-backtop';

interface Props extends BacktopProps {}

defineOptions({ name: 'BackTop' });

const props = withDefaults(defineProps<Props>(), {
  bottom: 20,
  isGroup: false,
  right: 24,
  target: '',
  visibilityHeight: 200,
});

const backTopStyle = computed(() => ({
  bottom: `${props.bottom}px`,
  right: `${props.right}px`,
}));

const { handleClick, visible } = useBackTop(props);
</script>
<template>
  <transition name="fade-down">
    <VbenButton
      v-if="visible"
      :style="backTopStyle"
      class="dark:bg-accent dark:hover:bg-heavy bg-background hover:bg-heavy data shadow-float z-popup fixed bottom-10 size-10 rounded-full duration-500"
      size="icon"
      variant="icon"
      @click="handleClick"
    >
      <ArrowUpToLine class="size-4" />
    </VbenButton>
  </transition>
</template>
