<script lang="ts" setup>
import type { MenuItemProps, MenuItemRegistered } from '../types';

import { computed, onBeforeUnmount, onMounted, reactive, useSlots } from 'vue';

import { useNamespace } from '@vben-core/composables';
import { VbenIcon, VbenTooltip } from '@vben-core/shadcn-ui';

import { MenuBadge } from '../components';
import { useMenu, useMenuContext, useSubMenuContext } from '../hooks';

interface Props extends MenuItemProps {}

defineOptions({ name: 'MenuItem' });

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
});

const emit = defineEmits<{ click: [MenuItemRegistered] }>();

const slots = useSlots();
const { b, e, is } = useNamespace('menu-item');
const nsMenu = useNamespace('menu');
const rootMenu = useMenuContext();
const subMenu = useSubMenuContext();
const { parentMenu, parentPaths } = useMenu();

const active = computed(() => props.path === rootMenu?.activePath);
const menuIcon = computed(() =>
  active.value ? props.activeIcon || props.icon : props.icon,
);

const isTopLevelMenuItem = computed(
  () => parentMenu.value?.type.name === 'Menu',
);

const collapseShowTitle = computed(
  () =>
    rootMenu.props?.collapseShowTitle &&
    isTopLevelMenuItem.value &&
    rootMenu.props.collapse,
);

const showTooltip = computed(
  () =>
    rootMenu.props.mode === 'vertical' &&
    isTopLevelMenuItem.value &&
    rootMenu.props?.collapse &&
    slots.title,
);

const item: MenuItemRegistered = reactive({
  active,
  parentPaths: parentPaths.value,
  path: props.path || '',
});

/**
 * 菜单项点击事件
 */
function handleClick() {
  if (props.disabled) {
    return;
  }
  rootMenu?.handleMenuItemClick?.({
    parentPaths: parentPaths.value,
    path: props.path,
  });
  emit('click', item);
}

onMounted(() => {
  subMenu?.addSubMenu?.(item);
  rootMenu?.addMenuItem?.(item);
});

onBeforeUnmount(() => {
  subMenu?.removeSubMenu?.(item);
  rootMenu?.removeMenuItem?.(item);
});
</script>
<template>
  <li
    :class="[
      rootMenu.theme,
      b(),
      is('active', active),
      is('disabled', disabled),
      is('collapse-show-title', collapseShowTitle),
    ]"
    role="menuitem"
    @click.stop="handleClick"
  >
    <VbenTooltip
      v-if="showTooltip"
      :content-class="[rootMenu.theme]"
      side="right"
    >
      <template #trigger>
        <div :class="[nsMenu.be('tooltip', 'trigger')]">
          <VbenIcon :class="nsMenu.e('icon')" :icon="menuIcon" fallback />
          <slot></slot>
          <span v-if="collapseShowTitle" :class="nsMenu.e('name')">
            <slot name="title"></slot>
          </span>
        </div>
      </template>
      <slot name="title"></slot>
    </VbenTooltip>
    <div v-show="!showTooltip" :class="[e('content')]">
      <MenuBadge
        v-if="rootMenu.props.mode !== 'horizontal'"
        class="right-2"
        v-bind="props"
      />
      <VbenIcon :class="nsMenu.e('icon')" :icon="menuIcon" />
      <slot></slot>
      <slot name="title"></slot>
    </div>
  </li>
</template>
