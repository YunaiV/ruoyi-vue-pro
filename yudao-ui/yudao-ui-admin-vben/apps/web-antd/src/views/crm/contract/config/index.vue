<script lang="ts" setup>
import type { CrmContractConfigApi } from '#/api/crm/contract/config';

import { onMounted } from 'vue';

import { Page } from '@vben/common-ui';

import { Card, message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  getContractConfig,
  saveContractConfig,
} from '#/api/crm/contract/config';
import { $t } from '#/locales';

import { schema } from './data';

const [Form, formApi] = useVbenForm({
  commonConfig: {
    labelClass: 'w-100',
  },
  layout: 'horizontal',
  schema,
  handleSubmit,
});

/** 提交表单 */
async function handleSubmit() {
  const { valid } = await formApi.validate();
  if (!valid) {
    return;
  }
  const data = (await formApi.getValues()) as CrmContractConfigApi.Config;
  if (!data.notifyEnabled) {
    data.notifyDays = undefined;
  }
  await saveContractConfig(data);
  await formApi.setValues(data);
  message.success($t('ui.actionMessage.operationSuccess'));
}

/** 获取配置 */
async function getConfigInfo() {
  const res = await getContractConfig();
  await formApi.setValues(res);
}

/** 初始化 */
onMounted(() => {
  getConfigInfo();
});
</script>

<template>
  <Page auto-content-height>
    <Card title="合同配置设置">
      <Form class="w-1/4" />
    </Card>
  </Page>
</template>
