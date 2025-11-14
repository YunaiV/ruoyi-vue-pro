<script lang="ts" setup>
import type { MenuRecordRaw } from '@vben/types';

import type { NormalMenuProps } from '@vben-core/menu-ui';

import { onBeforeMount } from 'vue';
import { useRoute } from 'vue-router';

import { findMenuByPath } from '@vben/utils';

import { NormalMenu } from '@vben-core/menu-ui';

interface Props extends NormalMenuProps {}

const props = defineProps<Props>();

const emit = defineEmits<{
  defaultSelect: [MenuRecordRaw, MenuRecordRaw?];
  enter: [MenuRecordRaw];
  select: [MenuRecordRaw];
}>();

const route = useRoute();

onBeforeMount(() => {
  const menu = findMenuByPath(props.menus || [], route.path);
  if (menu) {
    const rootMenu = (props.menus || []).find(
      (item) => item.path === menu.parents?.[0],
    );
    emit('defaultSelect', menu, rootMenu);
  }
});
</script>

<template>
  <NormalMenu
    :active-path="activePath"
    :collapse="collapse"
    :menus="menus"
    :rounded="rounded"
    :theme="theme"
    @enter="(menu) => emit('enter', menu)"
    @select="(menu) => emit('select', menu)"
  />
</template>
