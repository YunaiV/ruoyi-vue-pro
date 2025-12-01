<script lang="ts" setup>
import type { GlobalConfigProvider } from 'tdesign-vue-next';

import { onMounted } from 'vue';

import { usePreferences } from '@vben/preferences';

import { merge } from 'es-toolkit/compat';
import { ConfigProvider } from 'tdesign-vue-next';
import zhConfig from 'tdesign-vue-next/es/locale/zh_CN';

defineOptions({ name: 'App' });
const { isDark } = usePreferences();

onMounted(() => {
  document.documentElement.setAttribute(
    'theme-mode',
    isDark.value ? 'dark' : '',
  );
});

const customConfig: GlobalConfigProvider = {
  // 可以在此处定义更多自定义配置，具体可配置内容参看 API 文档
  calendar: {},
  table: {},
  pagination: {},
};
const globalConfig = merge(zhConfig, customConfig);
</script>

<template>
  <ConfigProvider :global-config="globalConfig">
    <RouterView />
  </ConfigProvider>
</template>
