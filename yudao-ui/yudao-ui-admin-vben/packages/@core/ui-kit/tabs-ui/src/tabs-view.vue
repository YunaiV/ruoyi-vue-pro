<script setup lang="ts">
import type { TabsEmits, TabsProps } from './types';

import { useForwardPropsEmits } from '@vben-core/composables';
import { ChevronLeft, ChevronRight } from '@vben-core/icons';
import { VbenScrollbar } from '@vben-core/shadcn-ui';

import { Tabs, TabsChrome } from './components';
import { useTabsDrag } from './use-tabs-drag';
import { useTabsViewScroll } from './use-tabs-view-scroll';

interface Props extends TabsProps {}

defineOptions({
  name: 'TabsView',
});

const props = withDefaults(defineProps<Props>(), {
  contentClass: 'vben-tabs-content',
  draggable: true,
  styleType: 'chrome',
  wheelable: true,
});

const emit = defineEmits<TabsEmits>();

const forward = useForwardPropsEmits(props, emit);

const {
  handleScrollAt,
  handleWheel,
  scrollbarRef,
  scrollDirection,
  scrollIsAtLeft,
  scrollIsAtRight,
  showScrollButton,
} = useTabsViewScroll(props);

function onWheel(e: WheelEvent) {
  if (props.wheelable) {
    handleWheel(e);
    e.stopPropagation();
    e.preventDefault();
  }
}

useTabsDrag(props, emit);
</script>

<template>
  <div class="flex h-full flex-1 overflow-hidden">
    <!-- 左侧滚动按钮 -->
    <span
      v-show="showScrollButton"
      :class="{
        'hover:bg-muted text-muted-foreground cursor-pointer': !scrollIsAtLeft,
        'pointer-events-none opacity-30': scrollIsAtLeft,
      }"
      class="border-r px-2"
      @click="scrollDirection('left')"
    >
      <ChevronLeft class="size-4 h-full" />
    </span>

    <div
      :class="{
        'pt-[3px]': styleType === 'chrome',
      }"
      class="size-full flex-1 overflow-hidden"
    >
      <VbenScrollbar
        ref="scrollbarRef"
        :shadow-bottom="false"
        :shadow-top="false"
        class="h-full"
        horizontal
        scroll-bar-class="z-10 hidden "
        shadow
        shadow-left
        shadow-right
        @scroll-at="handleScrollAt"
        @wheel="onWheel"
      >
        <TabsChrome
          v-if="styleType === 'chrome'"
          v-bind="{ ...forward, ...$attrs, ...$props }"
        />

        <Tabs v-else v-bind="{ ...forward, ...$attrs, ...$props }" />
      </VbenScrollbar>
    </div>

    <!-- 右侧滚动按钮 -->
    <span
      v-show="showScrollButton"
      :class="{
        'hover:bg-muted text-muted-foreground cursor-pointer': !scrollIsAtRight,
        'pointer-events-none opacity-30': scrollIsAtRight,
      }"
      class="hover:bg-muted text-muted-foreground cursor-pointer border-l px-2"
      @click="scrollDirection('right')"
    >
      <ChevronRight class="size-4 h-full" />
    </span>
  </div>
</template>
