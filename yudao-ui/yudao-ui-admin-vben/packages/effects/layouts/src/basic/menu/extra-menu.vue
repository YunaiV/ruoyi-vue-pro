<script lang="ts" setup>
import type { MenuRecordRaw } from '@vben/types';

import type { MenuProps } from '@vben-core/menu-ui';

import { useRoute } from 'vue-router';

import { Menu } from '@vben-core/menu-ui';

import { useNavigation } from './use-navigation';

interface Props extends MenuProps {
  collapse?: boolean;
  menus?: MenuRecordRaw[];
}

withDefaults(defineProps<Props>(), {
  accordion: true,
  menus: () => [],
});

const route = useRoute();
const { navigation } = useNavigation();

async function handleSelect(key: string) {
  await navigation(key);
}
</script>

<template>
  <Menu
    :accordion="accordion"
    :collapse="collapse"
    :default-active="route.meta?.activePath || route.path"
    :menus="menus"
    :rounded="rounded"
    :theme="theme"
    mode="vertical"
    @select="handleSelect"
  />
</template>
