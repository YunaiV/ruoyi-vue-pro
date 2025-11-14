<script lang="ts" setup>
import type { MallTradeConfigApi } from '#/api/mall/trade/config';

import { onMounted, ref } from 'vue';

import { DocAlert, Page } from '@vben/common-ui';
import { fenToYuan, yuanToFen } from '@vben/utils';

import { Card, message, Tabs } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { getTradeConfig, saveTradeConfig } from '#/api/mall/trade/config';
import { $t } from '#/locales';

import { schema } from './data';

const activeKey = ref('afterSale');
const formData = ref<MallTradeConfigApi.Config & { type?: string }>();

/** 获取配置 */
async function getConfigInfo() {
  const res = await getTradeConfig();
  if (!res) {
    return;
  }
  formData.value = res;
  // 转换金额单位
  formData.value.deliveryExpressFreePrice = Number.parseFloat(
    fenToYuan(formData.value.deliveryExpressFreePrice!),
  );
  formData.value.brokerageWithdrawMinPrice = Number.parseFloat(
    fenToYuan(formData.value.brokerageWithdrawMinPrice!),
  );
  formData.value!.type = activeKey.value;
  formApi.updateSchema(schema);
  // 设置到 values
  await formApi.setValues(formData.value);
}

/** 切换 Tab */
function handleTabChange(key: any) {
  activeKey.value = key;
  formData.value!.type = activeKey.value;
  formApi.setValues(formData.value!);
  formApi.updateSchema(schema);
}

/** 提交表单 */
async function handleSubmit() {
  const { valid } = await formApi.validate();
  if (!valid) {
    return;
  }
  // 提交表单
  const data = (await formApi.getValues()) as MallTradeConfigApi.Config;
  // 转换金额单位
  data.deliveryExpressFreePrice = yuanToFen(data.deliveryExpressFreePrice!);
  data.brokerageWithdrawMinPrice = yuanToFen(data.brokerageWithdrawMinPrice!);
  await saveTradeConfig(data);
  message.success($t('ui.actionMessage.operationSuccess'));
}

const [Form, formApi] = useVbenForm({
  commonConfig: {
    labelWidth: 150,
  },
  layout: 'horizontal',
  handleSubmit,
  schema,
});

/** 初始化 */
onMounted(() => {
  getConfigInfo();
});
</script>

<template>
  <Page>
    <template #doc>
      <DocAlert
        title="【交易】交易订单"
        url="https://doc.iocoder.cn/mall/trade-order/"
      />
      <DocAlert
        title="【交易】购物车"
        url="https://doc.iocoder.cn/mall/trade-cart/"
      />
    </template>
    <Card>
      <Tabs :active-key="activeKey" @change="handleTabChange">
        <Tabs.TabPane tab="售后" key="afterSale" :force-render="true" />
        <Tabs.TabPane tab="配送" key="delivery" :force-render="true" />
        <Tabs.TabPane tab="分销" key="brokerage" :force-render="true" />
      </Tabs>
      <Form class="w-2/5" />
    </Card>
  </Page>
</template>
