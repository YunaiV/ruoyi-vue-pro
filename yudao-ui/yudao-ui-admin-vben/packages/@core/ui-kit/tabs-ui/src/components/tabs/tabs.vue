<script lang="ts" setup>
import type { TabDefinition } from '@vben-core/typings';

import type { TabConfig, TabsProps } from '../../types';

import { computed } from 'vue';

import { Pin, X } from '@vben-core/icons';
import { VbenContextMenu, VbenIcon } from '@vben-core/shadcn-ui';

interface Props extends TabsProps {}

defineOptions({
  name: 'VbenTabs',

  inheritAttrs: false,
});
const props = withDefaults(defineProps<Props>(), {
  contentClass: 'vben-tabs-content',
  contextMenus: () => [],
  tabs: () => [],
});

const emit = defineEmits<{
  close: [string];
  unpin: [TabDefinition];
}>();
const active = defineModel<string>('active');

const typeWithClass = computed(() => {
  const typeClasses: Record<string, { content: string }> = {
    brisk: {
      content: `h-full after:content-['']  after:absolute after:bottom-0 after:left-0 after:w-full after:h-[1.5px] after:bg-primary after:scale-x-0 after:transition-[transform] after:ease-out after:duration-300 hover:after:scale-x-100 after:origin-left [&.is-active]:after:scale-x-100 [&:not(:first-child)]:border-l last:border-r last:border-r border-border`,
    },
    card: {
      content:
        'h-[calc(100%-6px)] rounded-md ml-2 border border-border  transition-all',
    },
    plain: {
      content:
        'h-full [&:not(:first-child)]:border-l last:border-r border-border',
    },
  };

  return typeClasses[props.styleType || 'plain'] || { content: '' };
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
    :class="contentClass"
    class="relative !flex h-full w-max items-center overflow-hidden pr-6"
  >
    <TransitionGroup name="slide-left">
      <div
        v-for="(tab, i) in tabsView"
        :key="tab.key"
        :class="[
          {
            'is-active dark:bg-accent bg-primary/15': tab.key === active,
            draggable: !tab.affixTab,
            'affix-tab': tab.affixTab,
          },
          typeWithClass.content,
        ]"
        :data-index="i"
        class="tab-item [&:not(.is-active)]:hover:bg-accent translate-all group relative flex cursor-pointer select-none"
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
          <div class="relative flex size-full items-center">
            <!-- extra -->
            <div
              class="absolute right-1.5 top-1/2 z-[3] translate-y-[-50%] overflow-hidden"
            >
              <!-- close-icon -->
              <X
                v-show="!tab.affixTab && tabsView.length > 1 && tab.closable"
                class="hover:bg-accent stroke-accent-foreground/80 hover:stroke-accent-foreground dark:group-[.is-active]:text-accent-foreground group-[.is-active]:text-primary size-3 cursor-pointer rounded-full transition-all"
                @click.stop="() => emit('close', tab.key)"
              />
              <Pin
                v-show="tab.affixTab && tabsView.length > 1 && tab.closable"
                class="hover:bg-accent hover:stroke-accent-foreground group-[.is-active]:text-primary dark:group-[.is-active]:text-accent-foreground mt-[1px] size-3.5 cursor-pointer rounded-full transition-all"
                @click.stop="() => emit('unpin', tab)"
              />
            </div>

            <!-- tab-item-main -->
            <div
              class="text-accent-foreground group-[.is-active]:text-primary dark:group-[.is-active]:text-accent-foreground mx-3 mr-4 flex h-full items-center overflow-hidden rounded-tl-[5px] rounded-tr-[5px] pr-3 transition-all duration-300"
            >
              <VbenIcon
                v-if="showIcon"
                :icon="tab.icon"
                class="mr-2 flex size-4 items-center overflow-hidden"
                fallback
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
