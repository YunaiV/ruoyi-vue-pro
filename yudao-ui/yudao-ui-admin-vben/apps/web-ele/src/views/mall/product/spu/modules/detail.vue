<script lang="ts" setup>
import type { DictDataType } from '@vben/hooks';

import type { MallSpuApi } from '#/api/mall/product/spu';

import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { IconifyIcon } from '@vben/icons';
import { floatToFixed2 } from '@vben/utils';

import {
  ElButton,
  ElCard,
  ElCarousel,
  ElCarouselItem,
  ElDescriptions,
  ElDescriptionsItem,
  ElEmpty,
  ElImage,
  ElTabPane,
  ElTabs,
  ElTag,
} from 'element-plus';

import * as ProductBrandApi from '#/api/mall/product/brand';
import * as ProductCategoryApi from '#/api/mall/product/category';
import * as ProductSpuApi from '#/api/mall/product/spu';

interface Category {
  id: number;
  name: string;
  children?: Category[];
}

interface Brand {
  id: number;
  name: string;
}

const { push } = useRouter();
const { params } = useRoute();

const formLoading = ref(false); // 表单的加载中：1）修改时的数据加载；2）提交的按钮禁用
const activeTab = ref('basic'); // 当前激活的标签页
const categoryList = ref<Category[]>([]); // 商品分类列表
const brandList = ref<Brand[]>([]); // 商品品牌列表
const deliveryTypeDict = ref<DictDataType[]>([]); // 配送方式字典

// SPU 表单数据
const formData = ref<MallSpuApi.Spu>({
  name: '', // 商品名称
  categoryId: undefined, // 商品分类
  keyword: '', // 关键字
  picUrl: '', // 商品封面图
  sliderPicUrls: [], // 商品轮播图
  introduction: '', // 商品简介
  deliveryTypes: [], // 配送方式数组
  deliveryTemplateId: undefined, // 运费模版
  brandId: undefined, // 商品品牌
  specType: false, // 商品规格
  subCommissionType: false, // 分销类型
  skus: [
    {
      price: 0, // 商品价格
      marketPrice: 0, // 市场价
      costPrice: 0, // 成本价
      barCode: '', // 商品条码
      picUrl: '', // 图片地址
      stock: 0, // 库存
      weight: 0, // 商品重量
      volume: 0, // 商品体积
      firstBrokeragePrice: 0, // 一级分销的佣金
      secondBrokeragePrice: 0, // 二级分销的佣金
    },
  ],
  description: '', // 商品详情
  sort: 0, // 商品排序
  giveIntegral: 0, // 赠送积分
  virtualSalesCount: 0, // 虚拟销量
});

/** 获取配送方式字典 */
const getDeliveryTypeDict = async () => {
  try {
    deliveryTypeDict.value = await getDictOptions(
      DICT_TYPE.TRADE_DELIVERY_TYPE,
      'number',
    );
    deliveryTypeDict.value = await getDictOptions(
      DICT_TYPE.TRADE_DELIVERY_TYPE,
      'number',
    );
  } catch (error) {
    console.error('获取配送方式字典失败', error);
  }
};

/** 获取商品分类列表 */
const getCategoryList = async () => {
  try {
    const data = await ProductCategoryApi.getCategorySimpleList();
    categoryList.value = data as Category[];
  } catch (error) {
    console.error('获取商品分类失败', error);
  }
};

/** 获取商品品牌列表 */
const getBrandList = async () => {
  try {
    const data = await ProductBrandApi.getSimpleBrandList();
    brandList.value = data as Brand[];
  } catch (error) {
    console.error('获取商品品牌失败', error);
  }
};

/** 根据ID获取分类名称 */
const getCategoryNameById = (id: number | undefined) => {
  if (!id || !categoryList.value || categoryList.value.length === 0)
    return '未知分类';
  const category = categoryList.value.find((item) => item.id === id);
  return category ? category.name : '未知分类';
};

/** 根据ID获取品牌名称 */
const getBrandNameById = (id: number | undefined) => {
  if (!id || !brandList.value || brandList.value.length === 0)
    return '未知品牌';
  const brand = brandList.value.find((item) => item.id === id);
  return brand ? brand.name : '未知品牌';
};

/** 根据值获取配送方式名称 */
const getDeliveryTypeName = (value: number) => {
  if (!deliveryTypeDict.value || deliveryTypeDict.value.length === 0)
    return `${value}`;
  const dict = deliveryTypeDict.value.find((item) => item.value === value);
  return dict ? dict.label : `${value}`;
};

/** 获得详情 */
const getDetail = async () => {
  const id = params.id as unknown as number;
  if (id) {
    formLoading.value = true;
    try {
      const res = (await ProductSpuApi.getSpu(id)) as MallSpuApi.Spu;
      res.skus?.forEach((item: MallSpuApi.Sku) => {
        item.price = floatToFixed2(item.price);
        item.marketPrice = floatToFixed2(item.marketPrice);
        item.costPrice = floatToFixed2(item.costPrice);
        item.firstBrokeragePrice = floatToFixed2(item.firstBrokeragePrice);
        item.secondBrokeragePrice = floatToFixed2(item.secondBrokeragePrice);
      });
      formData.value = res;
    } finally {
      formLoading.value = false;
    }
  }
};

/** 返回列表 */
const back = () => {
  push({ name: 'ProductSpu' });
};

/** 编辑商品 */
const editProduct = () => {
  push({ name: 'ProductSpuForm', params: { id: params.id } });
};

/** 初始化 */
onMounted(async () => {
  await Promise.all([getCategoryList(), getBrandList(), getDeliveryTypeDict()]);
  await getDetail();
});
</script>

<template>
  <Page auto-content-height :loading="formLoading">
    <template #title>
      <span class="text-lg font-bold">商品详情</span>
    </template>

    <template #extra>
      <div class="flex gap-2">
        <ElButton type="primary" @click="editProduct">
          <IconifyIcon icon="ep:edit" class="mr-1" />
          编辑商品
        </ElButton>
        <ElButton @click="back">
          <IconifyIcon icon="ep:back" class="mr-1" />
          返回列表
        </ElButton>
      </div>
    </template>

    <ElCard shadow="hover" class="mb-4">
      <div class="mb-4 flex flex-col gap-4 md:flex-row md:items-center">
        <ElImage
          :src="formData.picUrl"
          fit="contain"
          style="width: 120px; height: 120px"
          class="rounded border"
        />
        <div class="flex-grow">
          <h1 class="mb-2 text-xl font-bold">{{ formData.name }}</h1>
          <div class="mb-2 text-gray-500">
            {{ formData.introduction || '暂无简介' }}
          </div>
          <div class="flex flex-wrap gap-2">
            <ElTag v-if="formData.specType" type="success">多规格</ElTag>
            <ElTag v-else type="info">单规格</ElTag>
            <ElTag v-if="formData.subCommissionType" type="warning">分销</ElTag>
            <ElTag type="danger">
              库存:
              {{
                formData.skus?.reduce(
                  (sum, sku) => sum + (sku.stock || 0),
                  0,
                ) || 0
              }}
            </ElTag>
            <ElTag type="info">
              分类: {{ getCategoryNameById(formData.categoryId) }}
            </ElTag>
          </div>
        </div>
      </div>

      <ElTabs v-model="activeTab" type="border-card">
        <ElTabPane name="basic" label="基本信息">
          <div class="grid grid-cols-1 gap-6 lg:grid-cols-2">
            <!-- 基本信息 -->
            <ElCard shadow="never" header="商品信息" class="h-full">
              <ElDescriptions :column="1" border>
                <ElDescriptionsItem label="商品名称">
                  {{ formData.name }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="商品分类">
                  <ElTag type="success">
                    {{ getCategoryNameById(formData.categoryId) }}
                  </ElTag>
                </ElDescriptionsItem>
                <ElDescriptionsItem label="商品品牌">
                  <ElTag type="primary">
                    {{ getBrandNameById(formData.brandId) }}
                  </ElTag>
                </ElDescriptionsItem>
                <ElDescriptionsItem label="关键字">
                  <ElTag type="danger" />{{ formData.keyword || '无' }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="赠送积分">
                  {{ formData.giveIntegral }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="虚拟销量">
                  {{ formData.virtualSalesCount }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="排序">
                  {{ formData.sort }}
                </ElDescriptionsItem>
                <ElDescriptionsItem label="规格类型">
                  <ElTag :type="formData.specType ? 'success' : 'info'">
                    {{ formData.specType ? '多规格' : '单规格' }}
                  </ElTag>
                </ElDescriptionsItem>
                <ElDescriptionsItem label="分销类型">
                  <ElTag
                    :type="formData.subCommissionType ? 'warning' : 'info'"
                  >
                    {{ formData.subCommissionType ? '单独设置' : '默认设置' }}
                  </ElTag>
                </ElDescriptionsItem>
              </ElDescriptions>
            </ElCard>

            <!-- 配送信息 -->
            <ElCard shadow="never" header="配送信息" class="h-full">
              <ElDescriptions :column="1" border>
                <ElDescriptionsItem label="配送方式">
                  <div class="flex flex-wrap gap-2">
                    <ElTag
                      v-for="(type, index) in formData.deliveryTypes"
                      :key="index"
                      :type="
                        (deliveryTypeDict.find((dict) => dict.value === type)
                          ?.colorType as
                          | 'success'
                          | 'warning'
                          | 'info'
                          | 'danger'
                          | 'primary'
                          | undefined) || undefined
                      "
                    >
                      {{ getDeliveryTypeName(type) }}
                    </ElTag>
                    <span
                      v-if="
                        !formData.deliveryTypes ||
                        formData.deliveryTypes.length === 0
                      "
                      class="text-gray-400"
                    >
                      暂无配送方式
                    </span>
                  </div>
                </ElDescriptionsItem>
                <ElDescriptionsItem label="运费模板">
                  {{ formData.deliveryTemplateId || '未设置' }}
                </ElDescriptionsItem>
              </ElDescriptions>
            </ElCard>
          </div>
        </ElTabPane>

        <ElTabPane name="images" label="商品图片">
          <ElCard shadow="never" header="商品轮播图">
            <div
              v-if="formData.sliderPicUrls && formData.sliderPicUrls.length > 0"
            >
              <ElCarousel
                height="400px"
                :interval="4000"
                indicator-position="outside"
                arrow="always"
              >
                <ElCarouselItem
                  v-for="(item, index) in formData.sliderPicUrls"
                  :key="index"
                >
                  <div class="flex h-full items-center justify-center">
                    <ElImage
                      :src="item"
                      fit="contain"
                      class="max-h-full"
                      :preview-src-list="formData.sliderPicUrls"
                      :initial-index="index"
                    />
                  </div>
                </ElCarouselItem>
              </ElCarousel>
              <div class="mt-6 flex flex-wrap justify-center gap-3">
                <div
                  v-for="(item, index) in formData.sliderPicUrls"
                  :key="index"
                  class="cursor-pointer rounded border p-1"
                >
                  <ElImage
                    :src="item"
                    fit="cover"
                    style="width: 80px; height: 80px"
                  />
                </div>
              </div>
            </div>
            <ElEmpty v-else description="暂无轮播图" />
          </ElCard>
        </ElTabPane>

        <ElTabPane name="sku" label="SKU信息">
          <div v-if="formData.skus && formData.skus.length > 0">
            <div
              v-for="(sku, index) in formData.skus"
              :key="index"
              class="mb-6"
            >
              <ElCard
                shadow="hover"
                :header="`规格 ${index + 1}${sku.properties && sku.properties.length > 0 ? ` - ${sku.properties.map((p) => p.valueName).join('/')}` : ''}`"
              >
                <div class="flex flex-col gap-4 md:flex-row">
                  <ElImage
                    :src="sku.picUrl || formData.picUrl"
                    fit="contain"
                    style="width: 120px; height: 120px"
                    class="flex-shrink-0 rounded border"
                  />

                  <div class="grid flex-grow grid-cols-1 gap-6 md:grid-cols-3">
                    <!-- 价格信息 -->
                    <div class="rounded bg-gray-50 p-4">
                      <h3 class="mb-2 border-b pb-2 font-bold text-gray-700">
                        价格信息
                      </h3>
                      <div class="grid grid-cols-2 gap-2">
                        <div class="text-gray-500">销售价:</div>
                        <div class="font-bold text-red-500">
                          ¥{{ sku.price }}
                        </div>
                        <div class="text-gray-500">市场价:</div>
                        <div>¥{{ sku.marketPrice }}</div>
                        <div class="text-gray-500">成本价:</div>
                        <div>¥{{ sku.costPrice }}</div>
                      </div>
                    </div>

                    <!-- 库存信息 -->
                    <div class="rounded bg-gray-50 p-4">
                      <h3 class="mb-2 border-b pb-2 font-bold text-gray-700">
                        库存信息
                      </h3>
                      <div class="grid grid-cols-2 gap-2">
                        <div class="text-gray-500">库存:</div>
                        <div class="font-bold">{{ sku.stock }} 件</div>
                        <div class="text-gray-500">条码:</div>
                        <div>{{ sku.barCode || '未设置' }}</div>
                      </div>
                    </div>

                    <!-- 物流信息 -->
                    <div class="rounded bg-gray-50 p-4">
                      <h3 class="mb-2 border-b pb-2 font-bold text-gray-700">
                        物流信息
                      </h3>
                      <div class="grid grid-cols-2 gap-2">
                        <div class="text-gray-500">重量:</div>
                        <div>{{ sku.weight }} kg</div>
                        <div class="text-gray-500">体积:</div>
                        <div>{{ sku.volume }} m³</div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- 分销佣金 -->
                <div
                  v-if="formData.subCommissionType"
                  class="mt-4 rounded bg-yellow-50 p-4"
                >
                  <h3 class="mb-2 border-b pb-2 font-bold text-gray-700">
                    分销佣金
                  </h3>
                  <div class="grid grid-cols-2 gap-4 md:grid-cols-4">
                    <div class="text-gray-500">一级佣金:</div>
                    <div class="font-bold">¥{{ sku.firstBrokeragePrice }}</div>
                    <div class="text-gray-500">二级佣金:</div>
                    <div class="font-bold">¥{{ sku.secondBrokeragePrice }}</div>
                  </div>
                </div>

                <!-- 规格属性 -->
                <div
                  v-if="sku.properties && sku.properties.length > 0"
                  class="mt-4"
                >
                  <h3 class="mb-2 font-bold text-gray-700">规格属性</h3>
                  <div class="flex flex-wrap gap-2">
                    <ElTag
                      v-for="(prop, propIndex) in sku.properties"
                      :key="propIndex"
                      effect="dark"
                      class="text-sm"
                    >
                      {{ prop.propertyName }}: {{ prop.valueName }}
                    </ElTag>
                  </div>
                </div>
              </ElCard>
            </div>
          </div>
          <ElEmpty v-else description="暂无SKU信息" />
        </ElTabPane>

        <ElTabPane name="detail" label="商品详情">
          <ElCard shadow="never" body-style="padding: 0;">
            <div v-if="formData.description" class="product-description">
              <div v-html="formData.description"></div>
            </div>
            <ElEmpty v-else description="暂无商品详情" />
          </ElCard>
        </ElTabPane>
      </ElTabs>
    </ElCard>
  </Page>
</template>

<style scoped>
.product-description {
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
}

.product-description :deep(img) {
  max-width: 100%;
  height: auto;
}

.product-description :deep(table) {
  width: 100%;
  border-collapse: collapse;
}

.product-description :deep(table td) {
  padding: 8px;
  border: 1px solid #eee;
}
</style>
