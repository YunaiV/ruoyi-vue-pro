<script setup lang="ts">
import type { SelectOption } from '@vben/types';

import { computed } from 'vue';

import { $t } from '@vben/locales';

import SelectItem from '../select-item.vue';
import SwitchItem from '../switch-item.vue';

defineOptions({
  name: 'PreferenceInterfaceControl',
});

const widgetGlobalSearch = defineModel<boolean>('widgetGlobalSearch');
const widgetFullscreen = defineModel<boolean>('widgetFullscreen');
const widgetLanguageToggle = defineModel<boolean>('widgetLanguageToggle');
const widgetNotification = defineModel<boolean>('widgetNotification');
const widgetThemeToggle = defineModel<boolean>('widgetThemeToggle');
const widgetSidebarToggle = defineModel<boolean>('widgetSidebarToggle');
const widgetLockScreen = defineModel<boolean>('widgetLockScreen');
const appPreferencesButtonPosition = defineModel<string>(
  'appPreferencesButtonPosition',
);
const widgetRefresh = defineModel<boolean>('widgetRefresh');

const positionItems = computed((): SelectOption[] => [
  {
    label: $t('preferences.position.auto'),
    value: 'auto',
  },
  {
    label: $t('preferences.position.header'),
    value: 'header',
  },
  {
    label: $t('preferences.position.fixed'),
    value: 'fixed',
  },
]);
</script>

<template>
  <SwitchItem v-model="widgetGlobalSearch">
    {{ $t('preferences.widget.globalSearch') }}
  </SwitchItem>
  <SwitchItem v-model="widgetThemeToggle">
    {{ $t('preferences.widget.themeToggle') }}
  </SwitchItem>
  <SwitchItem v-model="widgetLanguageToggle">
    {{ $t('preferences.widget.languageToggle') }}
  </SwitchItem>
  <SwitchItem v-model="widgetFullscreen">
    {{ $t('preferences.widget.fullscreen') }}
  </SwitchItem>
  <SwitchItem v-model="widgetNotification">
    {{ $t('preferences.widget.notification') }}
  </SwitchItem>
  <SwitchItem v-model="widgetLockScreen">
    {{ $t('preferences.widget.lockScreen') }}
  </SwitchItem>
  <SwitchItem v-model="widgetSidebarToggle">
    {{ $t('preferences.widget.sidebarToggle') }}
  </SwitchItem>
  <SwitchItem v-model="widgetRefresh">
    {{ $t('preferences.widget.refresh') }}
  </SwitchItem>
  <SelectItem v-model="appPreferencesButtonPosition" :items="positionItems">
    {{ $t('preferences.position.title') }}
  </SelectItem>
</template>
