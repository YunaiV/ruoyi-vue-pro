<script setup lang="ts">
import type { AvatarVariants } from './avatar';

import { cn } from '@vben-core/shared/utils';

import { AvatarRoot } from 'radix-vue';

import { avatarVariant } from './avatar';

const props = withDefaults(
  defineProps<{
    class?: any;
    shape?: AvatarVariants['shape'];
    size?: AvatarVariants['size'];
  }>(),
  {
    shape: 'circle',
    size: 'sm',
  },
);
</script>

<template>
  <AvatarRoot :class="cn(avatarVariant({ size, shape }), props.class)">
    <slot></slot>
  </AvatarRoot>
</template>
