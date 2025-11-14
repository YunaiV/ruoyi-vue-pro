<script setup lang="ts">
import { $t } from '@vben/locales';

import SwitchItem from '../switch-item.vue';

const footerEnable = defineModel<boolean>('footerEnable');
const footerFixed = defineModel<boolean>('footerFixed');
</script>

<template>
  <SwitchItem v-model="footerEnable">
    {{ $t('preferences.footer.visible') }}
  </SwitchItem>
  <SwitchItem v-model="footerFixed" :disabled="!footerEnable">
    {{ $t('preferences.footer.fixed') }}
  </SwitchItem>
</template>
