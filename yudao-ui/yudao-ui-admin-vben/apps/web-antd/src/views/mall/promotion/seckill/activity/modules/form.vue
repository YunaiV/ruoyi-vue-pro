<script lang="ts" setup>
import type { MallSeckillActivityApi } from '#/api/mall/promotion/seckill/seckillActivity';

import { computed, nextTick, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Button, message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createSeckillActivity,
  getSeckillActivity,
  updateSeckillActivity,
} from '#/api/mall/promotion/seckill/seckillActivity';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MallSeckillActivityApi.SeckillActivity>();

const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['秒杀活动'])
    : $t('ui.actionTitle.create', ['秒杀活动']);
});

// ================= 商品选择相关 =================
const spuId = ref<number>();
const spuName = ref<string>('');
const skuTableData = ref<any[]>([]);

// 选择商品（占位函数，实际需要对接商品选择组件）
const handleSelectProduct = () => {
  message.info('商品选择功能需要对接商品选择组件');
  // TODO: 打开商品选择弹窗
  // 实际使用时需要：
  // 1. 打开商品选择弹窗
  // 2. 选择商品后调用以下逻辑设置数据：
  //    spuId.value = selectedSpu.id;
  //    spuName.value = selectedSpu.name;
  //    skuTableData.value = selectedSkus.map(sku => ({
  //      skuId: sku.id,
  //      skuName: sku.name || '',
  //      picUrl: sku.picUrl || selectedSpu.picUrl || '',
  //      price: sku.price || 0,
  //      stock: 0,
  //      seckillPrice: 0,
  //    }));
};

// ================= end =================

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
  wrapperClass: 'grid-cols-2',
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }

    // 验证商品和 SKU 配置
    if (!spuId.value) {
      message.error('请选择秒杀商品');
      return;
    }

    if (skuTableData.value.length === 0) {
      message.error('请至少配置一个 SKU');
      return;
    }

    // 验证 SKU 配置
    const hasInvalidSku = skuTableData.value.some(
      (sku) => sku.stock < 1 || sku.seckillPrice < 0.01,
    );
    if (hasInvalidSku) {
      message.error('请正确配置 SKU 的秒杀库存（≥1）和秒杀价格（≥0.01）');
      return;
    }

    modalApi.lock();
    try {
      const values = await formApi.getValues();

      // 构建提交数据
      const data: any = {
        ...values,
        spuId: spuId.value,
        products: skuTableData.value.map((sku) => ({
          skuId: sku.skuId,
          stock: sku.stock,
          seckillPrice: Math.round(sku.seckillPrice * 100), // 转换为分
        })),
      };

      await (formData.value?.id
        ? updateSeckillActivity(data)
        : createSeckillActivity(data));

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
      spuId.value = undefined;
      spuName.value = '';
      skuTableData.value = [];
      return;
    }

    const data = modalApi.getData<MallSeckillActivityApi.SeckillActivity>();
    if (!data || !data.id) {
      return;
    }

    modalApi.lock();
    try {
      formData.value = await getSeckillActivity(data.id);
      await nextTick();
      await formApi.setValues(formData.value);

      // TODO: 加载商品和 SKU 信息
      // 需要调用商品 API 获取 SPU 详情
      // spuId.value = formData.value.spuId;
      // await loadProductDetails(formData.value.spuId, formData.value.products);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal class="w-4/5" :title="getTitle">
    <div class="mx-4">
      <Form />

      <!-- 商品选择区域 -->
      <div class="mt-4">
        <div class="mb-2 flex items-center">
          <span class="text-sm font-medium">秒杀活动商品:</span>
          <Button class="ml-2" type="primary" @click="handleSelectProduct">
            选择商品
          </Button>
          <span v-if="spuName" class="ml-4 text-sm text-gray-600">
            已选择: {{ spuName }}
          </span>
        </div>

        <!-- SKU 配置表格 -->
        <div v-if="skuTableData.length > 0" class="mt-4">
          <table class="w-full border-collapse border border-gray-300">
            <thead>
              <tr class="bg-gray-100">
                <th class="border border-gray-300 px-4 py-2">商品图片</th>
                <th class="border border-gray-300 px-4 py-2">SKU 名称</th>
                <th class="border border-gray-300 px-4 py-2">原价(元)</th>
                <th class="border border-gray-300 px-4 py-2">秒杀库存</th>
                <th class="border border-gray-300 px-4 py-2">秒杀价格(元)</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(sku, index) in skuTableData" :key="index">
                <td class="border border-gray-300 px-4 py-2 text-center">
                  <img
                    v-if="sku.picUrl"
                    :src="sku.picUrl"
                    alt="商品图片"
                    class="h-16 w-16 object-cover"
                  />
                </td>
                <td class="border border-gray-300 px-4 py-2">
                  {{ sku.skuName }}
                </td>
                <td class="border border-gray-300 px-4 py-2 text-center">
                  ¥{{ (sku.price / 100).toFixed(2) }}
                </td>
                <td class="border border-gray-300 px-4 py-2">
                  <input
                    v-model.number="sku.stock"
                    type="number"
                    min="0"
                    class="w-full rounded border border-gray-300 px-2 py-1"
                  />
                </td>
                <td class="border border-gray-300 px-4 py-2">
                  <input
                    v-model.number="sku.seckillPrice"
                    type="number"
                    min="0"
                    step="0.01"
                    class="w-full rounded border border-gray-300 px-2 py-1"
                  />
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </Modal>
</template>
