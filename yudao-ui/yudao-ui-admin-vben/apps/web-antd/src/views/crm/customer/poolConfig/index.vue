<script lang="ts" setup>
import type { CrmCustomerPoolConfigApi } from '#/api/crm/customer/poolConfig';

import { onMounted } from 'vue';

import { Page } from '@vben/common-ui';

import { Card, message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  getCustomerPoolConfig,
  saveCustomerPoolConfig,
} from '#/api/crm/customer/poolConfig';
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
  // 提交表单
  const data =
    (await formApi.getValues()) as CrmCustomerPoolConfigApi.CustomerPoolConfig;
  if (!data.enabled) {
    data.contactExpireDays = undefined;
    data.dealExpireDays = undefined;
    data.notifyEnabled = false;
  }
  if (!data.notifyEnabled) {
    data.notifyDays = undefined;
  }
  await saveCustomerPoolConfig(data);
  // 关闭并提示
  await formApi.setValues(data);
  message.success($t('ui.actionMessage.operationSuccess'));
}

/** 获取配置 */
async function getConfigInfo() {
  const res = await getCustomerPoolConfig();
  await formApi.setValues(res);
}

/** 初始化 */
onMounted(() => {
  getConfigInfo();
});
</script>

<template>
  <Page auto-content-height>
    <Card title="客户公海规则设置">
      <Form class="w-1/4" />
    </Card>
  </Page>
</template>
