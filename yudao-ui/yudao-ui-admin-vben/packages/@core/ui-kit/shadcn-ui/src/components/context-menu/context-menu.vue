<script setup lang="ts">
import type {
  ContextMenuContentProps,
  ContextMenuRootEmits,
  ContextMenuRootProps,
} from 'radix-vue';

import type { ClassType } from '@vben-core/typings';

import type { IContextMenuItem } from './interface';

import { computed } from 'vue';

import { useForwardPropsEmits } from 'radix-vue';

import {
  ContextMenu,
  ContextMenuContent,
  ContextMenuItem,
  ContextMenuSeparator,
  ContextMenuShortcut,
  ContextMenuTrigger,
} from '../../ui/context-menu';

const props = defineProps<
  ContextMenuRootProps & {
    class?: ClassType;
    contentClass?: ClassType;
    contentProps?: ContextMenuContentProps;
    handlerData?: Record<string, any>;
    itemClass?: ClassType;
    menus: (data: any) => IContextMenuItem[];
  }
>();

const emits = defineEmits<ContextMenuRootEmits>();

const delegatedProps = computed(() => {
  const {
    class: _cls,
    contentClass: _,
    contentProps: _cProps,
    itemClass: _iCls,
    ...delegated
  } = props;

  return delegated;
});

const forwarded = useForwardPropsEmits(delegatedProps, emits);

const menusView = computed(() => {
  return props.menus?.(props.handlerData);
});

function handleClick(menu: IContextMenuItem) {
  if (menu.disabled) {
    return;
  }
  menu?.handler?.(props.handlerData);
}
</script>

<template>
  <ContextMenu v-bind="forwarded">
    <ContextMenuTrigger as-child>
      <slot></slot>
    </ContextMenuTrigger>
    <ContextMenuContent
      :class="contentClass"
      v-bind="contentProps"
      class="side-content z-popup"
    >
      <template v-for="menu in menusView" :key="menu.key">
        <ContextMenuItem
          :class="itemClass"
          :disabled="menu.disabled"
          :inset="menu.inset || !menu.icon"
          class="cursor-pointer"
          @click="handleClick(menu)"
        >
          <component
            :is="menu.icon"
            v-if="menu.icon"
            class="mr-2 size-4 text-lg"
          />

          {{ menu.text }}
          <ContextMenuShortcut v-if="menu.shortcut">
            {{ menu.shortcut }}
          </ContextMenuShortcut>
        </ContextMenuItem>
        <ContextMenuSeparator v-if="menu.separator" />
      </template>
    </ContextMenuContent>
  </ContextMenu>
</template>
