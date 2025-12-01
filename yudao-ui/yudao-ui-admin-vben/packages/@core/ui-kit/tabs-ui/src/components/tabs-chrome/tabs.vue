<script setup lang="ts">
import type { TabDefinition } from '@vben-core/typings';

import type { TabConfig, TabsProps } from '../../types';

import { computed, ref } from 'vue';

import { Pin, X } from '@vben-core/icons';
import { VbenContextMenu, VbenIcon } from '@vben-core/shadcn-ui';

interface Props extends TabsProps {}

defineOptions({
  name: 'VbenTabsChrome',
  inheritAttrs: false,
});

const props = withDefaults(defineProps<Props>(), {
  contentClass: 'vben-tabs-content',
  contextMenus: () => [],
  gap: 7,
  tabs: () => [],
});

const emit = defineEmits<{
  close: [string];
  unpin: [TabDefinition];
}>();
const active = defineModel<string>('active');

const contentRef = ref();
const tabRef = ref();

const style = computed(() => {
  const { gap } = props;
  return {
    '--gap': `${gap}px`,
  };
});

const tabsView = computed(() => {
  return props.tabs.map((tab) => {
    const { fullPath, meta, name, path, key } = tab || {};
    const { affixTab, icon, newTabTitle, tabClosable, title } = meta || {};
    return {
      affixTab: !!affixTab,
      closable: Reflect.has(meta, 'tabClosable') ? !!tabClosable : true,
      fullPath,
      icon: icon as string,
      key,
      meta,
      name,
      path,
      title: (newTabTitle || title || name) as string,
    } as TabConfig;
  });
});

function onMouseDown(e: MouseEvent, tab: TabConfig) {
  if (
    e.button === 1 &&
    tab.closable &&
    !tab.affixTab &&
    tabsView.value.length > 1 &&
    props.middleClickToClose
  ) {
    e.preventDefault();
    e.stopPropagation();
    emit('close', tab.key);
  }
}
</script>

<template>
  <div
    ref="contentRef"
    :class="contentClass"
    :style="style"
    class="tabs-chrome !flex h-full w-max overflow-y-hidden pr-6"
  >
    <TransitionGroup name="slide-left">
      <div
        v-for="(tab, i) in tabsView"
        :key="tab.key"
        ref="tabRef"
        :class="[
          {
            'is-active': tab.key === active,
            draggable: !tab.affixTab,
            'affix-tab': tab.affixTab,
          },
        ]"
        :data-active-tab="active"
        :data-index="i"
        class="tabs-chrome__item draggable translate-all group relative -mr-3 flex h-full select-none items-center"
        data-tab-item="true"
        @click="active = tab.key"
        @mousedown="onMouseDown($event, tab)"
      >
        <VbenContextMenu
          :handler-data="tab"
          :menus="contextMenus"
          :modal="false"
          item-class="pr-6"
        >
          <div class="relative size-full px-1">
            <!-- divider -->
            <div
              v-if="i !== 0 && tab.key !== active"
              class="tabs-chrome__divider absolute left-[var(--gap)] top-1/2 z-0 h-4 w-[1px] translate-y-[-50%] bg-border transition-all"
            ></div>
            <!-- background -->
            <div
              class="tabs-chrome__background absolute z-[-1] size-full px-[calc(var(--gap)-1px)] py-0 transition-opacity duration-150"
            >
              <div
                class="tabs-chrome__background-content h-full rounded-tl-[var(--gap)] rounded-tr-[var(--gap)] duration-150 group-[.is-active]:bg-primary/15 dark:group-[.is-active]:bg-accent"
              ></div>
              <svg
                class="tabs-chrome__background-before absolute bottom-0 left-[-1px] fill-transparent transition-all duration-150 group-[.is-active]:fill-primary/15 dark:group-[.is-active]:fill-accent"
                height="7"
                width="7"
              >
                <path d="M 0 7 A 7 7 0 0 0 7 0 L 7 7 Z" />
              </svg>
              <svg
                class="tabs-chrome__background-after absolute bottom-0 right-[-1px] fill-transparent transition-all duration-150 group-[.is-active]:fill-primary/15 dark:group-[.is-active]:fill-accent"
                height="7"
                width="7"
              >
                <path d="M 0 0 A 7 7 0 0 0 7 7 L 0 7 Z" />
              </svg>
            </div>

            <!-- extra -->
            <div
              class="tabs-chrome__extra absolute right-[var(--gap)] top-1/2 z-[3] size-4 translate-y-[-50%]"
            >
              <!-- close-icon -->
              <X
                v-show="!tab.affixTab && tabsView.length > 1 && tab.closable"
                class="mt-[2px] size-3 cursor-pointer rounded-full stroke-accent-foreground/80 text-accent-foreground/80 transition-all hover:bg-accent hover:stroke-accent-foreground group-[.is-active]:text-accent-foreground"
                @click.stop="() => emit('close', tab.key)"
              />
              <Pin
                v-show="tab.affixTab && tabsView.length > 1 && tab.closable"
                class="mt-[1px] size-3.5 cursor-pointer rounded-full text-accent-foreground/80 transition-all hover:text-accent-foreground group-[.is-active]:text-accent-foreground"
                @click.stop="() => emit('unpin', tab)"
              />
            </div>

            <!-- tab-item-main -->
            <div
              class="tabs-chrome__item-main z-[2] mx-[calc(var(--gap)*2)] my-0 flex h-full items-center overflow-hidden rounded-tl-[5px] rounded-tr-[5px] pl-2 pr-4 text-accent-foreground duration-150 group-[.is-active]:text-primary dark:group-[.is-active]:text-accent-foreground"
            >
              <VbenIcon
                v-if="showIcon"
                :icon="tab.icon"
                class="mr-1 flex size-4 items-center overflow-hidden"
              />

              <span class="flex-1 overflow-hidden whitespace-nowrap text-sm">
                {{ tab.title }}
              </span>
            </div>
          </div>
        </VbenContextMenu>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.tabs-chrome {
  &__item:not(.dragging) {
    @apply cursor-pointer;

    &:hover:not(.is-active) {
      & + .tabs-chrome__item {
        .tabs-chrome__divider {
          @apply opacity-0;
        }
      }

      .tabs-chrome__divider {
        @apply opacity-0;
      }

      .tabs-chrome__background {
        @apply pb-[2px];

        &-content {
          @apply mx-[2px] rounded-md bg-accent;
        }
      }
    }

    &.is-active {
      @apply z-[2];

      & + .tabs-chrome__item {
        .tabs-chrome__divider {
          @apply opacity-0 !important;
        }
      }
    }
  }
}
</style>
