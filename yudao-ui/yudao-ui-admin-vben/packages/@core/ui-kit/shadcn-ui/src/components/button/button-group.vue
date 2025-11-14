<script lang="ts" setup>
import { cn } from '@vben-core/shared/utils';

defineOptions({ name: 'VbenButtonGroup' });

withDefaults(
  defineProps<{
    border?: boolean;
    gap?: number;
    size?: 'large' | 'middle' | 'small';
  }>(),
  { border: false, gap: 0, size: 'middle' },
);
</script>
<template>
  <div
    :class="
      cn(
        'vben-button-group rounded-md',
        `size-${size}`,
        gap ? 'with-gap' : 'no-gap',
        $attrs.class as string,
      )
    "
    :style="{ gap: gap ? `${gap}px` : '0px' }"
  >
    <slot></slot>
  </div>
</template>

<style lang="scss" scoped>
.vben-button-group {
  display: inline-flex;

  &.size-large :deep(button) {
    height: 2.25rem;
    padding: 0.5rem 0.75rem;
    font-size: 0.875rem;
    line-height: 1.25rem;

    .icon-wrapper {
      margin-right: 0.4rem;

      svg {
        width: 1rem;
        height: 1rem;
      }
    }
  }

  &.size-middle :deep(button) {
    height: 2rem;
    padding: 0.25rem 0.5rem;
    font-size: 0.75rem;
    line-height: 1rem;

    .icon-wrapper {
      margin-right: 0.2rem;

      svg {
        width: 0.75rem;
        height: 0.75rem;
      }
    }
  }

  &.size-small :deep(button) {
    height: 1.75rem;
    padding: 0.2rem 0.4rem;
    font-size: 0.65rem;
    line-height: 0.75rem;

    .icon-wrapper {
      margin-right: 0.1rem;

      svg {
        width: 0.65rem;
        height: 0.65rem;
      }
    }
  }

  &.no-gap > :deep(button):nth-of-type(1) {
    border-radius: calc(var(--radius) - 2px) 0 0 calc(var(--radius) - 2px);
  }

  &.no-gap > :deep(button):last-of-type {
    border-radius: 0 calc(var(--radius) - 2px) calc(var(--radius) - 2px) 0;
  }

  &.no-gap {
    :deep(button + button) {
      border-left-width: 0;
      border-radius: 0;
    }
  }
}
</style>
