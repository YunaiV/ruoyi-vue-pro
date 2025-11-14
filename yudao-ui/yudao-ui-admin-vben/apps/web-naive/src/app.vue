<script lang="ts" setup>
import type { GlobalThemeOverrides } from 'naive-ui';

import { computed } from 'vue';

import { useNaiveDesignTokens } from '@vben/hooks';
import { preferences } from '@vben/preferences';

import {
  darkTheme,
  dateEnUS,
  dateZhCN,
  enUS,
  lightTheme,
  NConfigProvider,
  NMessageProvider,
  NNotificationProvider,
  zhCN,
} from 'naive-ui';

defineOptions({ name: 'App' });

const { commonTokens } = useNaiveDesignTokens();

const tokenLocale = computed(() =>
  preferences.app.locale === 'zh-CN' ? zhCN : enUS,
);
const tokenDateLocale = computed(() =>
  preferences.app.locale === 'zh-CN' ? dateZhCN : dateEnUS,
);
const tokenTheme = computed(() =>
  preferences.theme.mode === 'dark' ? darkTheme : lightTheme,
);

const themeOverrides = computed((): GlobalThemeOverrides => {
  return {
    common: commonTokens,
  };
});
</script>

<template>
  <NConfigProvider
    :date-locale="tokenDateLocale"
    :locale="tokenLocale"
    :theme="tokenTheme"
    :theme-overrides="themeOverrides"
    class="h-full"
  >
    <NNotificationProvider>
      <NMessageProvider>
        <RouterView />
      </NMessageProvider>
    </NNotificationProvider>
  </NConfigProvider>
</template>
