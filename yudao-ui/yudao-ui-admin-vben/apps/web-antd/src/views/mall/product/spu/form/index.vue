<script lang="ts" setup>
import type { MallSpuApi } from '#/api/mall/product/spu';
import type {
  PropertyAndValues,
  RuleConfig,
} from '#/views/mall/product/spu/components';

import { onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';

import { Page, useVbenModal } from '@vben/common-ui';
import { useTabs } from '@vben/hooks';
import { convertToInteger, formatToFraction } from '@vben/utils';

import { Button, Card, message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { createSpu, getSpu, updateSpu } from '#/api/mall/product/spu';
import { getPropertyList, SkuList } from '#/views/mall/product/spu/components';

import {
  useDeliveryFormSchema,
  useDescriptionFormSchema,
  useInfoFormSchema,
  useOtherFormSchema,
  useSkuFormSchema,
} from './data';
import ProductAttributes from './modules/product-attributes.vue';
import ProductPropertyAddForm from './modules/product-property-add-form.vue';

const spuId = ref<number>();
const { params, name } = useRoute();
const { closeCurrentTab } = useTabs();
const activeTabName = ref('info');
const tabList = ref([
  {
    key: 'info',
    tab: '基础设置',
  },
  {
    key: 'sku',
    tab: '价格库存',
  },
  {
    key: 'delivery',
    tab: '物流设置',
  },
  {
    key: 'description',
    tab: '商品详情',
  },
  {
    key: 'other',
    tab: '其它设置',
  },
]);

const formLoading = ref(false); // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const isDetail = ref(name === 'ProductSpuDetail'); // 是否查看详情
const skuListRef = ref(); // 商品属性列表 Ref

const formData = ref<MallSpuApi.Spu>({
  name: '',
  categoryId: undefined,
  keyword: '',
  picUrl: '',
  sliderPicUrls: [],
  introduction: '',
  deliveryTypes: [],
  deliveryTemplateId: undefined,
  brandId: undefined,
  specType: false,
  subCommissionType: false,
  skus: [
    {
      price: 0,
      marketPrice: 0,
      costPrice: 0,
      barCode: '',
      picUrl: '',
      stock: 0,
      weight: 0,
      volume: 0,
      firstBrokeragePrice: 0,
      secondBrokeragePrice: 0,
    },
  ],
  description: '',
  sort: 0,
  giveIntegral: 0,
  virtualSalesCount: 0,
}); // spu 表单数据
const propertyList = ref<PropertyAndValues[]>([]); // 商品属性列表
const ruleConfig: RuleConfig[] = [
  {
    name: 'stock',
    rule: (arg) => arg >= 0,
    message: '商品库存必须大于等于 1 ！！！',
  },
  {
    name: 'price',
    rule: (arg) => arg >= 0.01,
    message: '商品销售价格必须大于等于 0.01 元！！！',
  },
  {
    name: 'marketPrice',
    rule: (arg) => arg >= 0.01,
    message: '商品市场价格必须大于等于 0.01 元！！！',
  },
  {
    name: 'costPrice',
    rule: (arg) => arg >= 0.01,
    message: '商品成本价格必须大于等于 0.00 元！！！',
  },
]; // sku 相关属性校验规则

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
    labelWidth: 120,
  },
  layout: 'horizontal',
  schema: useSkuFormSchema(propertyList.value, isDetail.value),
  showDefaultActions: false,
  handleValuesChange: (values, fieldsChanged) => {
    if (fieldsChanged.includes('subCommissionType')) {
      formData.value.subCommissionType = values.subCommissionType;
      handleChangeSubCommissionType();
    }
    if (fieldsChanged.includes('specType')) {
      formData.value.specType = values.specType;
      handleChangeSpec();
    }
  },
});

const [ProductPropertyAddFormModal, productPropertyAddFormApi] = useVbenModal({
  connectedComponent: ProductPropertyAddForm,
  destroyOnClose: true,
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

/** tab 切换 */
function handleTabChange(key: string) {
  activeTabName.value = key;
}

/** 提交表单 */
async function handleSubmit() {
  const values: MallSpuApi.Spu = await infoFormApi
    .merge(skuFormApi)
    .merge(deliveryFormApi)
    .merge(descriptionFormApi)
    .merge(otherFormApi)
    .submitAllForm(true);
  values.skus = formData.value.skus;
  if (values.skus) {
    try {
      // 校验 sku
      skuListRef.value.validateSku();
    } catch {
      message.error('【库存价格】不完善，请填写相关信息');
      return;
    }
    values.skus.forEach((item) => {
      // 金额转换：元转分
      item.price = convertToInteger(item.price);
      item.marketPrice = convertToInteger(item.marketPrice);
      item.costPrice = convertToInteger(item.costPrice);
      item.firstBrokeragePrice = convertToInteger(item.firstBrokeragePrice);
      item.secondBrokeragePrice = convertToInteger(item.secondBrokeragePrice);
    });
  }
  // 处理轮播图列表 TODO @puhui999：这个是必须的哇？
  const newSliderPicUrls: any[] = [];
  values.sliderPicUrls!.forEach((item: any) => {
    // 如果是前端选的图
    typeof item === 'object'
      ? newSliderPicUrls.push(item.url)
      : newSliderPicUrls.push(item);
  });
  values.sliderPicUrls = newSliderPicUrls;

  // 提交数据
  await (spuId.value ? updateSpu(values) : createSpu(values));
}

/** 获得详情 */
async function getDetail() {
  if (isDetail.value) {
    isDetail.value = true;
    infoFormApi.setDisabled(true);
    skuFormApi.setDisabled(true);
    deliveryFormApi.setDisabled(true);
    descriptionFormApi.setDisabled(true);
    otherFormApi.setDisabled(true);
  }
  // 将 SKU 的属性，整理成 PropertyAndValues 数组
  propertyList.value = getPropertyList(formData.value);
  formLoading.value = true;
  try {
    const res = await getSpu(spuId.value!);
    // 金额转换：元转分
    res.skus?.forEach((item) => {
      item.price = formatToFraction(item.price);
      item.marketPrice = formatToFraction(item.marketPrice);
      item.costPrice = formatToFraction(item.costPrice);
      item.firstBrokeragePrice = formatToFraction(item.firstBrokeragePrice);
      item.secondBrokeragePrice = formatToFraction(item.secondBrokeragePrice);
    });
    formData.value = res;
    // 初始化各表单值
    infoFormApi.setValues(res).then();
    skuFormApi.setValues(res).then();
    deliveryFormApi.setValues(res).then();
    descriptionFormApi.setValues(res).then();
    otherFormApi.setValues(res).then();
    // 将 SKU 的属性，整理成 PropertyAndValues 数组
    propertyList.value = getPropertyList(formData.value);
  } finally {
    formLoading.value = false;
  }
}

// =========== sku form 逻辑 ===========

/** 打开属性添加表单 */
function openPropertyAddForm() {
  productPropertyAddFormApi.open();
}

/** 调用 SkuList generateTableData 方法*/
function generateSkus(propertyList: PropertyAndValues[]) {
  skuListRef.value.generateTableData(propertyList);
}

/** 分销类型 */
function handleChangeSubCommissionType() {
  // 默认为零，类型切换后也要重置为零
  for (const item of formData.value.skus!) {
    item.firstBrokeragePrice = 0;
    item.secondBrokeragePrice = 0;
  }
}

/** 选择规格 */
function handleChangeSpec() {
  // 重置商品属性列表
  propertyList.value = [];
  // 重置 sku 列表
  formData.value.skus = [
    {
      price: 0,
      marketPrice: 0,
      costPrice: 0,
      barCode: '',
      picUrl: '',
      stock: 0,
      weight: 0,
      volume: 0,
      firstBrokeragePrice: 0,
      secondBrokeragePrice: 0,
    },
  ];
}

/** 监听 sku form schema 变化，更新表单 */
watch(
  propertyList,
  () => {
    skuFormApi.updateSchema(
      useSkuFormSchema(propertyList.value, isDetail.value),
    );
  },
  { deep: true },
);

/** 初始化 */
onMounted(async () => {
  spuId.value = params.id as unknown as number;
  if (!spuId.value) {
    return;
  }
  await getDetail();
});
</script>

<template>
  <div>
    <ProductPropertyAddFormModal :property-list="propertyList" />

    <Page auto-content-height>
      <Card
        class="h-full w-full"
        :loading="formLoading"
        :tab-list="tabList"
        :active-key="activeTabName"
        @tab-change="handleTabChange"
      >
        <template #tabBarExtraContent>
          <Button type="primary" v-if="!isDetail" @click="handleSubmit">
            保存
          </Button>
          <Button type="default" v-else @click="() => closeCurrentTab()">
            返回列表
          </Button>
        </template>

        <InfoForm class="w-3/5" v-show="activeTabName === 'info'" />
        <SkuForm class="w-full" v-show="activeTabName === 'sku'">
          <template #singleSkuList>
            <SkuList
              ref="skuListRef"
              class="w-full"
              :is-detail="isDetail"
              :prop-form-data="formData"
              :property-list="propertyList"
              :rule-config="ruleConfig"
            />
          </template>
          <template #productAttributes>
            <div>
              <Button class="mb-10px mr-15px" @click="openPropertyAddForm">
                添加属性
              </Button>
              <ProductAttributes
                :is-detail="isDetail"
                :property-list="propertyList"
                @success="generateSkus"
              />
            </div>
          </template>
          <template #batchSkuList>
            <SkuList
              :is-batch="true"
              :is-detail="isDetail"
              :prop-form-data="formData"
              :property-list="propertyList"
            />
          </template>
          <template #multiSkuList>
            <SkuList
              ref="skuListRef"
              :is-detail="isDetail"
              :prop-form-data="formData"
              :property-list="propertyList"
              :rule-config="ruleConfig"
            />
          </template>
        </SkuForm>
        <DeliveryForm class="w-3/5" v-show="activeTabName === 'delivery'" />
        <DescriptionForm
          class="w-3/5"
          v-show="activeTabName === 'description'"
        />
        <OtherForm class="w-3/5" v-show="activeTabName === 'other'" />
      </Card>
    </Page>
  </div>
</template>
<style lang="scss" scoped>
// TODO @puhui999：这个样式是必须的哇？
:deep(.ant-tabs-tab-btn) {
  font-size: 14px !important;
}
</style>
