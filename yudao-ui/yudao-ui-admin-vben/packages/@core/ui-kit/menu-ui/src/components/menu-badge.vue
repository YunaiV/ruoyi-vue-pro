<script setup lang="ts">
import type { MenuRecordBadgeRaw } from '@vben-core/typings';

import { computed } from 'vue';

import { isValidColor } from '@vben-core/shared/color';

import BadgeDot from './menu-badge-dot.vue';

interface Props extends MenuRecordBadgeRaw {
  hasChildren?: boolean;
}

const props = withDefaults(defineProps<Props>(), {});

const variantsMap: Record<string, string> = {
  default: 'bg-green-500',
  destructive: 'bg-destructive',
  primary: 'bg-primary',
  success: 'bg-green-500',
  warning: 'bg-yellow-500',
};

const isDot = computed(() => props.badgeType === 'dot');

const badgeClass = computed(() => {
  const { badgeVariants } = props;

  if (!badgeVariants) {
    return variantsMap.default;
  }

  return variantsMap[badgeVariants] || badgeVariants;
});

const badgeStyle = computed(() => {
  if (badgeClass.value && isValidColor(badgeClass.value)) {
    return {
      backgroundColor: badgeClass.value,
    };
  }
  return {};
});
</script>
<template>
  <span v-if="isDot || badge" :class="$attrs.class" class="absolute">
    <BadgeDot v-if="isDot" :dot-class="badgeClass" :dot-style="badgeStyle" />
    <div
      v-else
      :class="badgeClass"
      :style="badgeStyle"
      class="flex-center rounded-xl px-1.5 py-0.5 text-[10px] text-primary-foreground"
    >
      {{ badge }}
    </div>
  </span>
</template>
