<script setup lang="ts">
import type { ClassType } from '@vben-core/typings';

import { computed, ref } from 'vue';

import { cn } from '@vben-core/shared/utils';

import { ScrollArea, ScrollBar } from '../../ui';

interface Props {
  class?: ClassType;
  horizontal?: boolean;
  scrollBarClass?: ClassType;
  shadow?: boolean;
  shadowBorder?: boolean;
  shadowBottom?: boolean;
  shadowLeft?: boolean;
  shadowRight?: boolean;
  shadowTop?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  class: '',
  horizontal: false,
  shadow: false,
  shadowBorder: false,
  shadowBottom: true,
  shadowLeft: false,
  shadowRight: false,
  shadowTop: true,
});

const emit = defineEmits<{
  scrollAt: [{ bottom: boolean; left: boolean; right: boolean; top: boolean }];
}>();

const isAtTop = ref(true);
const isAtRight = ref(false);
const isAtBottom = ref(false);
const isAtLeft = ref(true);

/**
 * We have to check if the scroll amount is close enough to some threshold in order to
 * more accurately calculate arrivedState. This is because scrollTop/scrollLeft are non-rounded
 * numbers, while scrollHeight/scrollWidth and clientHeight/clientWidth are rounded.
 * https://developer.mozilla.org/en-US/docs/Web/API/Element/scrollHeight#determine_if_an_element_has_been_totally_scrolled
 */
const ARRIVED_STATE_THRESHOLD_PIXELS = 1;

const showShadowTop = computed(() => props.shadow && props.shadowTop);
const showShadowBottom = computed(() => props.shadow && props.shadowBottom);
const showShadowLeft = computed(() => props.shadow && props.shadowLeft);
const showShadowRight = computed(() => props.shadow && props.shadowRight);

const computedShadowClasses = computed(() => {
  return {
    'both-shadow':
      !isAtLeft.value &&
      !isAtRight.value &&
      showShadowLeft.value &&
      showShadowRight.value,
    'left-shadow': !isAtLeft.value && showShadowLeft.value,
    'right-shadow': !isAtRight.value && showShadowRight.value,
  };
});

function handleScroll(event: Event) {
  const target = event.target as HTMLElement;
  const scrollTop = target?.scrollTop ?? 0;
  const scrollLeft = target?.scrollLeft ?? 0;
  const clientHeight = target?.clientHeight ?? 0;
  const clientWidth = target?.clientWidth ?? 0;
  const scrollHeight = target?.scrollHeight ?? 0;
  const scrollWidth = target?.scrollWidth ?? 0;
  isAtTop.value = scrollTop <= 0;
  isAtLeft.value = scrollLeft <= 0;
  isAtBottom.value =
    Math.abs(scrollTop) + clientHeight >=
    scrollHeight - ARRIVED_STATE_THRESHOLD_PIXELS;
  isAtRight.value =
    Math.abs(scrollLeft) + clientWidth >=
    scrollWidth - ARRIVED_STATE_THRESHOLD_PIXELS;

  emit('scrollAt', {
    bottom: isAtBottom.value,
    left: isAtLeft.value,
    right: isAtRight.value,
    top: isAtTop.value,
  });
}
</script>

<template>
  <ScrollArea
    :class="[cn(props.class), computedShadowClasses]"
    :on-scroll="handleScroll"
    class="vben-scrollbar relative"
  >
    <div
      v-if="showShadowTop"
      :class="{
        'opacity-100': !isAtTop,
        'border-border border-t': shadowBorder && !isAtTop,
      }"
      class="scrollbar-top-shadow pointer-events-none absolute top-0 z-10 h-12 w-full opacity-0 transition-opacity duration-300 ease-in-out will-change-[opacity]"
    ></div>
    <slot></slot>
    <div
      v-if="showShadowBottom"
      :class="{
        'opacity-100': !isAtTop && !isAtBottom,
        'border-border border-b': shadowBorder && !isAtTop && !isAtBottom,
      }"
      class="scrollbar-bottom-shadow pointer-events-none absolute bottom-0 z-10 h-12 w-full opacity-0 transition-opacity duration-300 ease-in-out will-change-[opacity]"
    ></div>
    <ScrollBar
      v-if="horizontal"
      :class="scrollBarClass"
      orientation="horizontal"
    />
  </ScrollArea>
</template>

<style scoped>
.vben-scrollbar {
  &:not(.both-shadow).left-shadow {
    mask-image: linear-gradient(90deg, transparent, #000 16px);
  }

  &:not(.both-shadow).right-shadow {
    mask-image: linear-gradient(
      90deg,
      #000 0%,
      #000 calc(100% - 16px),
      transparent
    );
  }

  &.both-shadow {
    mask-image: linear-gradient(
      90deg,
      transparent,
      #000 16px,
      #000 calc(100% - 16px),
      transparent 100%
    );
  }
}

.scrollbar-top-shadow {
  background: linear-gradient(
    to bottom,
    hsl(var(--scroll-shadow, var(--background))),
    transparent
  );
}

.scrollbar-bottom-shadow {
  background: linear-gradient(
    to top,
    hsl(var(--scroll-shadow, var(--background))),
    transparent
  );
}
</style>
