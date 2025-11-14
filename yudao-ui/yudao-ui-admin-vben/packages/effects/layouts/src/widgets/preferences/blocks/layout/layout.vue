<script setup lang="ts">
import type { Component } from 'vue';

import type { LayoutType } from '@vben/types';

import { computed } from 'vue';

import { CircleHelp } from '@vben/icons';
import { $t } from '@vben/locales';

import { VbenTooltip } from '@vben-core/shadcn-ui';

import {
  FullContent,
  HeaderMixedNav,
  HeaderNav,
  HeaderSidebarNav,
  MixedNav,
  SidebarMixedNav,
  SidebarNav,
} from '../../icons';

interface PresetItem {
  name: string;
  tip: string;
  type: LayoutType;
}

defineOptions({
  name: 'PreferenceLayout',
});

const modelValue = defineModel<LayoutType>({ default: 'sidebar-nav' });

const components: Record<LayoutType, Component> = {
  'full-content': FullContent,
  'header-nav': HeaderNav,
  'mixed-nav': MixedNav,
  'sidebar-mixed-nav': SidebarMixedNav,
  'sidebar-nav': SidebarNav,
  'header-mixed-nav': HeaderMixedNav,
  'header-sidebar-nav': HeaderSidebarNav,
};

const PRESET = computed((): PresetItem[] => [
  {
    name: $t('preferences.vertical'),
    tip: $t('preferences.verticalTip'),
    type: 'sidebar-nav',
  },
  {
    name: $t('preferences.twoColumn'),
    tip: $t('preferences.twoColumnTip'),
    type: 'sidebar-mixed-nav',
  },
  {
    name: $t('preferences.horizontal'),
    tip: $t('preferences.horizontalTip'),
    type: 'header-nav',
  },
  {
    name: $t('preferences.headerSidebarNav'),
    tip: $t('preferences.headerSidebarNavTip'),
    type: 'header-sidebar-nav',
  },
  {
    name: $t('preferences.mixedMenu'),
    tip: $t('preferences.mixedMenuTip'),
    type: 'mixed-nav',
  },
  {
    name: $t('preferences.headerTwoColumn'),
    tip: $t('preferences.headerTwoColumnTip'),
    type: 'header-mixed-nav',
  },
  {
    name: $t('preferences.fullContent'),
    tip: $t('preferences.fullContentTip'),
    type: 'full-content',
  },
]);

function activeClass(theme: string): string[] {
  return theme === modelValue.value ? ['outline-box-active'] : [];
}
</script>

<template>
  <div class="flex w-full flex-wrap gap-5">
    <template v-for="theme in PRESET" :key="theme.name">
      <div
        class="flex w-[100px] cursor-pointer flex-col"
        @click="modelValue = theme.type"
      >
        <div :class="activeClass(theme.type)" class="outline-box flex-center">
          <component :is="components[theme.type]" />
        </div>
        <div
          class="text-muted-foreground flex-center hover:text-foreground mt-2 text-center text-xs"
        >
          {{ theme.name }}
          <VbenTooltip v-if="theme.tip" side="bottom">
            <template #trigger>
              <CircleHelp class="ml-1 size-3 cursor-help" />
            </template>
            {{ theme.tip }}
          </VbenTooltip>
        </div>
      </div>
    </template>
  </div>
</template>
