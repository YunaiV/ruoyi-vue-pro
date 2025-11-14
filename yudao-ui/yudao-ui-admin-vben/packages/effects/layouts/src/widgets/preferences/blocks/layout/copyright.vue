<script setup lang="ts">
import { computed } from 'vue';

import { $t } from '@vben/locales';

import InputItem from '../input-item.vue';
import SwitchItem from '../switch-item.vue';

const props = defineProps<{ disabled: boolean }>();

const copyrightEnable = defineModel<boolean>('copyrightEnable');
const copyrightDate = defineModel<string>('copyrightDate');
const copyrightIcp = defineModel<string>('copyrightIcp');
const copyrightIcpLink = defineModel<string>('copyrightIcpLink');
const copyrightCompanyName = defineModel<string>('copyrightCompanyName');
const copyrightCompanySiteLink = defineModel<string>(
  'copyrightCompanySiteLink',
);

const itemDisabled = computed(() => props.disabled || !copyrightEnable.value);
</script>

<template>
  <SwitchItem v-model="copyrightEnable" :disabled="disabled">
    {{ $t('preferences.copyright.enable') }}
  </SwitchItem>

  <InputItem v-model="copyrightCompanyName" :disabled="itemDisabled">
    {{ $t('preferences.copyright.companyName') }}
  </InputItem>
  <InputItem v-model="copyrightCompanySiteLink" :disabled="itemDisabled">
    {{ $t('preferences.copyright.companySiteLink') }}
  </InputItem>
  <InputItem v-model="copyrightDate" :disabled="itemDisabled">
    {{ $t('preferences.copyright.date') }}
  </InputItem>

  <InputItem v-model="copyrightIcp" :disabled="itemDisabled">
    {{ $t('preferences.copyright.icp') }}
  </InputItem>
  <InputItem v-model="copyrightIcpLink" :disabled="itemDisabled">
    {{ $t('preferences.copyright.icpLink') }}
  </InputItem>
</template>
