<script setup lang="ts">
import type { IotProductApi } from '#/api/iot/product/product';

// TODO @haohao：detail 挪到 yudao-ui-admin-vben-v5/apps/web-antd/src/views/iot/product/product/detail 下。独立一个，不放在 modules 里。
import { onMounted, provide, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';

import { message, Tabs } from 'ant-design-vue';

import { getDeviceCount } from '#/api/iot/device/device';
import { getProduct } from '#/api/iot/product/product';
import IoTProductThingModel from '#/views/iot/thingmodel/index.vue';

import ProductDetailsHeader from './product-details-header.vue';
import ProductDetailsInfo from './product-details-info.vue';

defineOptions({ name: 'IoTProductDetail' });

const route = useRoute();
const router = useRouter();

const id = Number(route.params.id);
const loading = ref(true);
const product = ref<IotProductApi.Product>({} as IotProductApi.Product);
const activeTab = ref('info');

provide('product', product); // 提供产品信息给子组件

/** 获取产品详情 */
async function getProductData(productId: number) {
  loading.value = true;
  try {
    product.value = await getProduct(productId);
  } catch {
    message.error('获取产品详情失败');
  } finally {
    loading.value = false;
  }
}

/** 查询设备数量 */
async function getDeviceCountData(productId: number) {
  try {
    return await getDeviceCount(productId);
  } catch {
    message.error('获取设备数量失败');
    return 0;
  }
}

/** 初始化 */
onMounted(async () => {
  if (!id) {
    message.warning('参数错误，产品不能为空！');
    router.back();
    return;
  }
  await getProductData(id);

  // 处理 tab 参数
  const { tab } = route.query;
  if (tab) {
    activeTab.value = tab as string;
  }
  // 查询设备数量
  if (product.value.id) {
    product.value.deviceCount = await getDeviceCountData(product.value.id);
  }
});
</script>

<template>
  <Page>
    <ProductDetailsHeader
      :loading="loading"
      :product="product"
      @refresh="() => getProductData(id)"
    />
    <Tabs v-model:active-key="activeTab" class="mt-4">
      <Tabs.TabPane key="info" tab="产品信息">
        <ProductDetailsInfo v-if="activeTab === 'info'" :product="product" />
      </Tabs.TabPane>
      <Tabs.TabPane key="thingModel" tab="物模型（功能定义）">
        <IoTProductThingModel
          v-if="activeTab === 'thingModel'"
          :product-id="id"
        />
      </Tabs.TabPane>
    </Tabs>
  </Page>
</template>
