<script lang="ts" setup>
import type { IotDeviceApi } from '#/api/iot/device/device';
import type { IotProductApi } from '#/api/iot/product/product';
import type { ThingModelData } from '#/api/iot/thingmodel';

import { onMounted, ref, unref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { useTabbarStore } from '@vben/stores';

import { message, Tabs } from 'ant-design-vue';

import { getDevice } from '#/api/iot/device/device';
import { DeviceTypeEnum, getProduct } from '#/api/iot/product/product';
import { getThingModelListByProductId } from '#/api/iot/thingmodel';

import DeviceDetailConfig from './DeviceDetailConfig.vue';
import DeviceDetailsHeader from './DeviceDetailsHeader.vue';
import DeviceDetailsInfo from './DeviceDetailsInfo.vue';
import DeviceDetailsMessage from './DeviceDetailsMessage.vue';
import DeviceDetailsSimulator from './DeviceDetailsSimulator.vue';
import DeviceDetailsThingModel from './DeviceDetailsThingModel.vue';

defineOptions({ name: 'IoTDeviceDetail' });

const route = useRoute();
const id = Number(route.params.id); // 将字符串转换为数字
const loading = ref(true); // 加载中
const product = ref<IotProductApi.Product>({} as IotProductApi.Product); // 产品详情
const device = ref<IotDeviceApi.Device>({} as IotDeviceApi.Device); // 设备详情
const activeTab = ref('info'); // 默认激活的标签页
const thingModelList = ref<ThingModelData[]>([]); // 物模型列表数据

/** 获取设备详情 */
async function getDeviceData() {
  loading.value = true;
  try {
    device.value = await getDevice(id);
    await getProductData(device.value.productId);
    await getThingModelList(device.value.productId);
  } finally {
    loading.value = false;
  }
}

/** 获取产品详情 */
async function getProductData(id: number) {
  product.value = await getProduct(id);
}

/** 获取物模型列表 */
async function getThingModelList(productId: number) {
  try {
    const data = await getThingModelListByProductId(productId);
    thingModelList.value = data || [];
  } catch (error) {
    console.error('获取物模型列表失败:', error);
    thingModelList.value = [];
  }
}

/** 初始化 */
const tabbarStore = useTabbarStore(); // 视图操作
const router = useRouter(); // 路由
const { currentRoute } = router;
onMounted(async () => {
  if (!id) {
    message.warning({ content: '参数错误，产品不能为空！' });
    await tabbarStore.closeTab(unref(currentRoute), router);
    return;
  }
  await getDeviceData();
  activeTab.value = (route.query.tab as string) || 'info';
});
</script>
<template>
  <Page>
    <DeviceDetailsHeader
      :loading="loading"
      :product="product"
      :device="device"
      @refresh="getDeviceData"
    />

    <Tabs v-model:active-key="activeTab" class="device-detail-tabs mt-4">
      <Tabs.Pane key="info" tab="设备信息">
        <DeviceDetailsInfo
          v-if="activeTab === 'info'"
          :product="product"
          :device="device"
        />
      </Tabs.Pane>
      <Tabs.Pane key="model" tab="物模型数据">
        <DeviceDetailsThingModel
          v-if="activeTab === 'model' && device.id"
          :device-id="device.id"
          :thing-model-list="thingModelList"
        />
      </Tabs.Pane>
      <Tabs.Pane
        v-if="product.deviceType === DeviceTypeEnum.GATEWAY"
        key="sub-device"
        tab="子设备管理"
      />
      <Tabs.Pane key="log" tab="设备消息">
        <DeviceDetailsMessage
          v-if="activeTab === 'log' && device.id"
          :device-id="device.id"
        />
      </Tabs.Pane>
      <Tabs.Pane key="simulator" tab="模拟设备">
        <DeviceDetailsSimulator
          v-if="activeTab === 'simulator'"
          :product="product"
          :device="device"
          :thing-model-list="thingModelList"
        />
      </Tabs.Pane>
      <Tabs.Pane key="config" tab="设备配置">
        <DeviceDetailConfig
          v-if="activeTab === 'config'"
          :device="device"
          @success="getDeviceData"
        />
      </Tabs.Pane>
    </Tabs>
  </Page>
</template>
