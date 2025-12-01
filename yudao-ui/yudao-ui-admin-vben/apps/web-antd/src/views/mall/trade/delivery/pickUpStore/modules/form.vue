<script lang="ts" setup>
import type { MallDeliveryPickUpStoreApi } from '#/api/mall/trade/delivery/pickUpStore';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Button, message } from 'ant-design-vue';
import dayjs from 'dayjs';

import { useVbenForm } from '#/adapter/form';
import { getTradeConfig } from '#/api/mall/trade/config';
import {
  createDeliveryPickUpStore,
  getDeliveryPickUpStore,
  updateDeliveryPickUpStore,
} from '#/api/mall/trade/delivery/pickUpStore';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MallDeliveryPickUpStoreApi.DeliveryPickUpStore>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['门店'])
    : $t('ui.actionTitle.create', ['门店']);
});

const mapVisible = ref(false); // 是否显示地图弹窗
const tencentLbsUrl = ref(''); // 腾讯位置服务 url

/** 获取经纬度相关方法 */
function selectAddress(loc: any) {
  if (loc.latlng?.lat) {
    formApi.setFieldValue('latitude', loc.latlng.lat);
  }
  if (loc.latlng?.lng) {
    formApi.setFieldValue('longitude', loc.latlng.lng);
  }
  mapVisible.value = false;
}

/** 初始化腾讯地图 */
async function initTencentLbsMap() {
  // 从配置中获取腾讯地图的 key
  const data = await getTradeConfig();
  const key = data.tencentLbsKey;
  if (!key) {
    message.warning('请先配置腾讯位置服务的 key');
    return;
  }

  // 设置 url
  if (!window) {
    return;
  }
  (window as any).selectAddress = selectAddress;
  window.addEventListener(
    'message',
    (event) => {
      // 接收位置信息，用户选择确认位置点后选点组件会触发该事件，回传用户的位置信息
      const loc = event.data;
      if (loc && loc.module === 'locationPicker') {
        // 防止其他应用也会向该页面 post 信息，需判断 module 是否为 'locationPicker'
        (window.parent as any).selectAddress(loc);
      }
    },
    false,
  );
  tencentLbsUrl.value = `https://apis.map.qq.com/tools/locpicker?type=1&key=${key}&referer=myapp`;
}

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    labelWidth: 120,
  },
  fieldMappingTime: [['rangeTime', ['openingTime', 'closingTime'], 'HH:mm']],
  wrapperClass: 'grid-cols-2',
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    // 提交表单
    const data =
      (await formApi.getValues()) as MallDeliveryPickUpStoreApi.DeliveryPickUpStore;
    try {
      await (formData.value?.id
        ? updateDeliveryPickUpStore(data)
        : createDeliveryPickUpStore(data));
      // 关闭并提示
      await modalApi.close();
      emit('success');
      message.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      return;
    }
    // 加载数据
    const data =
      modalApi.getData<MallDeliveryPickUpStoreApi.DeliveryPickUpStore>();
    if (!data || !data.id) {
      // 初始化地图
      await initTencentLbsMap();
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getDeliveryPickUpStore(data.id);
      formData.value.rangeTime = [
        dayjs(formData.value.openingTime, 'HH:mm'),
        dayjs(formData.value.closingTime, 'HH:mm'),
      ];
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
      // 初始化地图
      await initTencentLbsMap();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-2/5">
    <Form class="mx-4" />
    <template #prepend-footer>
      <Button @click="mapVisible = true"> 获取经纬度 </Button>
    </template>
  </Modal>

  <!-- 地图弹窗 -->
  <a-modal
    v-model:open="mapVisible"
    title="获取经纬度"
    :width="800"
    :footer="null"
  >
    <iframe
      v-if="mapVisible && tencentLbsUrl"
      :src="tencentLbsUrl"
      class="h-[600px] w-full border-0"
    ></iframe>
  </a-modal>
</template>
