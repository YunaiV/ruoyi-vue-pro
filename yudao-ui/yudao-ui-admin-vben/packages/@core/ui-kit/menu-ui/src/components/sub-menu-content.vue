<script lang="ts" setup>
import type { MenuItemProps } from '../types';

import { computed } from 'vue';

import { useNamespace } from '@vben-core/composables';
import { ChevronDown, ChevronRight } from '@vben-core/icons';
import { VbenIcon } from '@vben-core/shadcn-ui';

import { useMenuContext } from '../hooks';

interface Props extends MenuItemProps {
  isMenuMore?: boolean;
  isTopLevelMenuSubmenu: boolean;
  level?: number;
}

defineOptions({ name: 'SubMenuContent' });

const props = withDefaults(defineProps<Props>(), {
  isMenuMore: false,
  level: 0,
});

const rootMenu = useMenuContext();
const { b, e, is } = useNamespace('sub-menu-content');
const nsMenu = useNamespace('menu');

const opened = computed(() => {
  return rootMenu?.openedMenus.includes(props.path);
});

const collapse = computed(() => {
  return rootMenu.props.collapse;
});

const isFirstLevel = computed(() => {
  return props.level === 1;
});

const getCollapseShowTitle = computed(() => {
  return (
    rootMenu.props.collapseShowTitle && isFirstLevel.value && collapse.value
  );
});

const mode = computed(() => {
  return rootMenu?.props.mode;
});

const showArrowIcon = computed(() => {
  return mode.value === 'horizontal' || !(isFirstLevel.value && collapse.value);
});

const hiddenTitle = computed(() => {
  return (
    mode.value === 'vertical' &&
    isFirstLevel.value &&
    collapse.value &&
    !getCollapseShowTitle.value
  );
});

const iconComp = computed(() => {
  return (mode.value === 'horizontal' && !isFirstLevel.value) ||
    (mode.value === 'vertical' && collapse.value)
    ? ChevronRight
    : ChevronDown;
});

const iconArrowStyle = computed(() => {
  return opened.value ? { transform: `rotate(180deg)` } : {};
});
</script>
<template>
  <div
    :class="[
      b(),
      is('collapse-show-title', getCollapseShowTitle),
      is('more', isMenuMore),
    ]"
  >
    <slot></slot>

    <VbenIcon
      v-if="!isMenuMore"
      :class="nsMenu.e('icon')"
      :icon="icon"
      fallback
    />

    <div v-if="!hiddenTitle" :class="[e('title')]">
      <slot name="title"></slot>
    </div>

    <component
      :is="iconComp"
      v-if="!isMenuMore"
      v-show="showArrowIcon"
      :class="[e('icon-arrow')]"
      :style="iconArrowStyle"
      class="size-4"
    />
  </div>
</template>
