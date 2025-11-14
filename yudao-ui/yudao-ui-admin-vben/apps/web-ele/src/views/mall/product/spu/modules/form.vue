<script lang="ts" setup>
import type { MallSpuApi } from '#/api/mall/product/spu';

import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';

import { ContentWrap, Page } from '@vben/common-ui';
import { convertToInteger, formatToFraction } from '@vben/utils';

import { ElButton, ElTabs } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { createSpu, getSpu, updateSpu } from '#/api/mall/product/spu';

import {
  useDeliveryFormSchema,
  useDescriptionFormSchema,
  useInfoFormSchema,
  useOtherFormSchema,
  useSkuFormSchema,
} from './form-data';

const spuId = ref<number>();
const { params } = useRoute();

const activeTab = ref('info');

const [InfoForm, infoFormApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 120,
  },
  layout: 'horizontal',
  schema: useInfoFormSchema(),
  showDefaultActions: false,
});

const [SkuForm, skuFormApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 120,
  },
  layout: 'horizontal',
  schema: useSkuFormSchema(),
  showDefaultActions: false,
});

const [DeliveryForm, deliveryFormApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 120,
  },
  layout: 'horizontal',
  schema: useDeliveryFormSchema(),
  showDefaultActions: false,
});

const [DescriptionForm, descriptionFormApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 120,
  },
  layout: 'vertical',
  schema: useDescriptionFormSchema(),
  showDefaultActions: false,
});

const [OtherForm, otherFormApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 120,
  },
  layout: 'horizontal',
  schema: useOtherFormSchema(),
  showDefaultActions: false,
});

async function onSubmit() {
  const values: MallSpuApi.Spu = await infoFormApi
    .merge(skuFormApi)
    .merge(deliveryFormApi)
    .merge(descriptionFormApi)
    .merge(otherFormApi)
    .submitAllForm(true);

  if (values.skus) {
    values.skus.forEach((item) => {
      // sku相关价格元转分
      item.price = convertToInteger(item.price);
      item.marketPrice = convertToInteger(item.marketPrice);
      item.costPrice = convertToInteger(item.costPrice);
      item.firstBrokeragePrice = convertToInteger(item.firstBrokeragePrice);
      item.secondBrokeragePrice = convertToInteger(item.secondBrokeragePrice);
    });
  }
  // 处理轮播图列表
  const newSliderPicUrls: any[] = [];
  values.sliderPicUrls!.forEach((item: any) => {
    // 如果是前端选的图
    typeof item === 'object'
      ? newSliderPicUrls.push(item.url)
      : newSliderPicUrls.push(item);
  });
  values.sliderPicUrls = newSliderPicUrls;

  await (spuId.value ? updateSpu(values) : createSpu(values));
}

async function initDate() {
  spuId.value = params.id as unknown as number;
  if (!spuId.value) {
    return;
  }
  const res = await getSpu(spuId.value);
  if (res.skus) {
    res.skus.forEach((item) => {
      // 回显价格分转元
      item.price = formatToFraction(item.price);
      item.marketPrice = formatToFraction(item.marketPrice);
      item.costPrice = formatToFraction(item.costPrice);
      item.firstBrokeragePrice = formatToFraction(item.firstBrokeragePrice);
      item.secondBrokeragePrice = formatToFraction(item.secondBrokeragePrice);
    });
  }
  infoFormApi.setValues(res);
  skuFormApi.setValues(res);
  deliveryFormApi.setValues(res);
  descriptionFormApi.setValues(res);
  otherFormApi.setValues(res);
}

onMounted(async () => {
  await initDate();
});
</script>

<template>
  <Page auto-content-height>
    <ContentWrap class="h-full w-full pb-8">
      <template #extra>
        <ElButton type="primary" @click="onSubmit"> 保存 </ElButton>
      </template>
      <ElTabs v-model="activeTab">
        <ElTabs.TabPane label="基础设置" name="info">
          <InfoForm class="w-3/5" />
        </ElTabs.TabPane>
        <ElTabs.TabPane label="价格库存" name="sku">
          <SkuForm class="w-3/5" />
        </ElTabs.TabPane>
        <ElTabs.TabPane label="物流设置" name="delivery">
          <DeliveryForm class="w-3/5" />
        </ElTabs.TabPane>
        <ElTabs.TabPane label="商品详情" name="description">
          <DescriptionForm class="w-3/5" />
        </ElTabs.TabPane>
        <ElTabs.TabPane label="其它设置" name="other">
          <OtherForm class="w-3/5" />
        </ElTabs.TabPane>
      </ElTabs>
    </ContentWrap>
  </Page>
</template>
