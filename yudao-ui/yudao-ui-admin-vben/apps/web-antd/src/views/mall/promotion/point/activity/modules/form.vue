<script lang="ts" setup>
import type { MallSpuApi } from '#/api/mall/product/spu';
import type { MallPointActivityApi } from '#/api/mall/promotion/point';
import type {
  RuleConfig,
  SpuProperty,
} from '#/views/mall/product/spu/components';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { cloneDeep, convertToInteger, formatToFraction } from '@vben/utils';

import { Button, InputNumber, message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { VxeColumn } from '#/adapter/vxe-table';
import { getSpu } from '#/api/mall/product/spu';
import {
  createPointActivity,
  getPointActivity,
  updatePointActivity,
} from '#/api/mall/promotion/point';
import { $t } from '#/locales';
import {
  getPropertyList,
  SpuAndSkuList,
  SpuSkuSelect,
} from '#/views/mall/product/spu/components';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MallPointActivityApi.PointActivity>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['积分活动'])
    : $t('ui.actionTitle.create', ['积分活动']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 100,
  },
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
});

// ================= 商品选择相关 =================

const spuSkuSelectRef = ref(); // 商品和属性选择 Ref
const spuAndSkuListRef = ref(); // SPU 和 SKU 列表组件 Ref

const ruleConfig: RuleConfig[] = [
  {
    name: 'productConfig.stock',
    rule: (arg) => arg >= 1,
    message: '商品可兑换库存必须大于等于 1 ！！！',
  },
  {
    name: 'productConfig.point',
    rule: (arg) => arg >= 1,
    message: '商品所需兑换积分必须大于等于 1 ！！！',
  },
  {
    name: 'productConfig.count',
    rule: (arg) => arg >= 1,
    message: '商品可兑换次数必须大于等于 1 ！！！',
  },
]; // SKU 规则配置

const spuList = ref<MallSpuApi.Spu[]>([]); // 选择的 SPU 列表
const spuPropertyList = ref<SpuProperty<MallSpuApi.Spu>[]>([]); // SPU 属性列表

/** 打开商品选择器 */
function openSpuSelect() {
  spuSkuSelectRef.value?.open();
}

/** 选择商品后的回调 */
async function handleSpuSelected(spuId: number, skuIds?: number[]) {
  await formApi.setFieldValue('spuId', spuId);
  await getSpuDetails(spuId, skuIds);
}

/** 获取 SPU 详情 */
async function getSpuDetails(
  spuId: number,
  skuIds?: number[],
  products?: MallPointActivityApi.PointProduct[],
) {
  const res = await getSpu(spuId);
  if (!res) {
    return;
  }

  spuList.value = [];

  // 筛选指定的 SKU
  const selectSkus =
    skuIds === undefined
      ? res.skus
      : res.skus?.filter((sku) => skuIds.includes(sku.id!));
  // 为每个 SKU 配置积分商城相关的配置
  selectSkus?.forEach((sku) => {
    let config: MallPointActivityApi.PointProduct = {
      skuId: sku.id!,
      stock: 0,
      price: 0,
      point: 0,
      count: 0,
    };
    // 如果是编辑模式，回填已有配置
    if (products !== undefined) {
      const product = products.find((item) => item.skuId === sku.id);
      if (product) {
        product.price = formatToFraction(product.price) as unknown as number;
      }
      config = product || config;
    }
    // 动态添加 productConfig 属性到 SKU
    (
      sku as MallSpuApi.Sku & {
        productConfig: MallPointActivityApi.PointProduct;
      }
    ).productConfig = config;
  });
  res.skus = selectSkus;

  const spuProperties: SpuProperty<MallSpuApi.Spu>[] = [
    {
      spuId: res.id!,
      spuDetail: res,
      propertyList: getPropertyList(res),
    },
  ]; // 构建 SPU 属性列表

  // 直接赋值，因为每次只选择一个 SPU
  spuList.value = [res];
  spuPropertyList.value = spuProperties;
}

// ================= end =================

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    try {
      // 获取积分商城商品配置（深拷贝避免直接修改原对象）
      const products: MallPointActivityApi.PointProduct[] = cloneDeep(
        spuAndSkuListRef.value?.getSkuConfigs('productConfig') || [],
      );
      // 价格需要转为分
      products.forEach((item) => {
        item.price = convertToInteger(item.price);
      });
      // 提交表单
      const data =
        (await formApi.getValues()) as MallPointActivityApi.PointActivity;
      data.products = products;
      await (formData.value?.id
        ? updatePointActivity(data)
        : createPointActivity(data));
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
      // 重置表单数据（新增和编辑模式都需要）
      formData.value = undefined;
      spuList.value = [];
      spuPropertyList.value = [];
      return;
    }

    // 加载数据
    const data = modalApi.getData<MallPointActivityApi.PointActivity>();
    if (!data || !data.id) {
      return;
    }
    // 加载数据
    modalApi.lock();
    try {
      formData.value = await getPointActivity(data.id);
      await getSpuDetails(
        formData.value.spuId,
        formData.value.products?.map((sku) => sku.skuId),
        formData.value.products,
      );
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <div>
    <Modal :title="getTitle" class="w-[70%]">
      <Form class="mx-4">
        <!-- 商品选择 -->
        <template #spuId>
          <div class="w-full">
            <Button v-if="!formData?.id" type="primary" @click="openSpuSelect">
              选择商品
            </Button>

            <!-- SPU 和 SKU 列表展示 -->
            <SpuAndSkuList
              ref="spuAndSkuListRef"
              :rule-config="ruleConfig"
              :spu-list="spuList"
              :spu-property-list-p="spuPropertyList"
              class="mt-4"
            >
              <!-- 扩展列：积分商城特有配置 -->
              <template #default>
                <VxeColumn align="center" min-width="168" title="可兑换库存">
                  <template #default="{ row: sku }">
                    <InputNumber
                      v-model:value="sku.productConfig.stock"
                      :max="sku.stock"
                      :min="0"
                      class="w-full"
                    />
                  </template>
                </VxeColumn>
                <VxeColumn align="center" min-width="168" title="可兑换次数">
                  <template #default="{ row: sku }">
                    <InputNumber
                      v-model:value="sku.productConfig.count"
                      :min="0"
                      class="w-full"
                    />
                  </template>
                </VxeColumn>
                <VxeColumn align="center" min-width="168" title="所需积分">
                  <template #default="{ row: sku }">
                    <InputNumber
                      v-model:value="sku.productConfig.point"
                      :min="0"
                      class="w-full"
                    />
                  </template>
                </VxeColumn>
                <VxeColumn align="center" min-width="168" title="所需金额(元)">
                  <template #default="{ row: sku }">
                    <InputNumber
                      v-model:value="sku.productConfig.price"
                      :min="0"
                      :precision="2"
                      :step="0.1"
                      class="w-full"
                    />
                  </template>
                </VxeColumn>
              </template>
            </SpuAndSkuList>
          </div>
        </template>
      </Form>
    </Modal>

    <!-- 商品选择器弹窗 -->
    <SpuSkuSelect
      ref="spuSkuSelectRef"
      :is-select-sku="true"
      @select="handleSpuSelected"
    />
  </div>
</template>
