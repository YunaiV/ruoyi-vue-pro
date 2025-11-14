<script setup lang="ts">
import type { ProductCardProperty } from './config';

import type { MallSpuApi } from '#/api/mall/product/spu';

import { ref, watch } from 'vue';

import { fenToYuan } from '@vben/utils';

import { ElImage } from 'element-plus';

import * as ProductSpuApi from '#/api/mall/product/spu';

/** 商品卡片 */
defineOptions({ name: 'ProductCard' });
// 定义属性
const props = defineProps<{ property: ProductCardProperty }>();
// 商品列表
const spuList = ref<MallSpuApi.Spu[]>([]);
watch(
  () => props.property.spuIds,
  async () => {
    spuList.value = await ProductSpuApi.getSpuDetailList(props.property.spuIds);
  },
  {
    immediate: true,
    deep: true,
  },
);

/**
 * 计算商品的间距
 * @param index 商品索引
 */
const calculateSpace = (index: number) => {
  // 商品的列数
  const columns = props.property.layoutType === 'twoCol' ? 2 : 1;
  // 第一列没有左边距
  const marginLeft = index % columns === 0 ? '0' : `${props.property.space}px`;
  // 第一行没有上边距
  const marginTop = index < columns ? '0' : `${props.property.space}px`;

  return { marginLeft, marginTop };
};

// 容器
const containerRef = ref();
// 计算商品的宽度
const calculateWidth = () => {
  let width = '100%';
  // 双列时每列的宽度为：（总宽度 - 间距）/ 2
  if (props.property.layoutType === 'twoCol') {
    width = `${(containerRef.value.offsetWidth - props.property.space) / 2}px`;
  }
  return { width };
};
</script>
<template>
  <div
    class="box-content flex min-h-[30px] w-full flex-row flex-wrap"
    ref="containerRef"
  >
    <div
      class="relative box-content flex flex-row flex-wrap overflow-hidden bg-white"
      :style="{
        ...calculateSpace(index),
        ...calculateWidth(),
        borderTopLeftRadius: `${property.borderRadiusTop}px`,
        borderTopRightRadius: `${property.borderRadiusTop}px`,
        borderBottomLeftRadius: `${property.borderRadiusBottom}px`,
        borderBottomRightRadius: `${property.borderRadiusBottom}px`,
      }"
      v-for="(spu, index) in spuList"
      :key="index"
    >
      <!-- 角标 -->
      <div
        v-if="property.badge.show && property.badge.imgUrl"
        class="absolute left-0 top-0 z-[1] items-center justify-center"
      >
        <ElImage
          fit="cover"
          :src="property.badge.imgUrl"
          class="h-[26px] w-[38px]"
        />
      </div>
      <!-- 商品封面图 -->
      <div
        class="h-[140px]"
        :class="[
          {
            'w-full': property.layoutType !== 'oneColSmallImg',
            'w-[140px]': property.layoutType === 'oneColSmallImg',
          },
        ]"
      >
        <ElImage fit="cover" class="h-full w-full" :src="spu.picUrl" />
      </div>
      <div
        class="box-border flex flex-col gap-[8px] p-[8px]"
        :class="[
          {
            'w-full': property.layoutType !== 'oneColSmallImg',
            'w-[calc(100%-140px-16px)]':
              property.layoutType === 'oneColSmallImg',
          },
        ]"
      >
        <!-- 商品名称 -->
        <div
          v-if="property.fields.name.show"
          class="text-[14px]"
          :class="[
            {
              truncate: property.layoutType !== 'oneColSmallImg',
              'line-clamp-2 overflow-ellipsis':
                property.layoutType === 'oneColSmallImg',
            },
          ]"
          :style="{ color: property.fields.name.color }"
        >
          {{ spu.name }}
        </div>
        <!-- 商品简介 -->
        <div
          v-if="property.fields.introduction.show"
          class="truncate text-[12px]"
          :style="{ color: property.fields.introduction.color }"
        >
          {{ spu.introduction }}
        </div>
        <div>
          <!-- 价格 -->
          <span
            v-if="property.fields.price.show"
            class="text-[16px]"
            :style="{ color: property.fields.price.color }"
          >
            ￥{{ fenToYuan(spu.price as any) }}
          </span>
          <!-- 市场价 -->
          <span
            v-if="property.fields.marketPrice.show && spu.marketPrice"
            class="ml-[4px] text-[10px] line-through"
            :style="{ color: property.fields.marketPrice.color }"
            >￥{{ fenToYuan(spu.marketPrice) }}
          </span>
        </div>
        <div class="text-[12px]">
          <!-- 销量 -->
          <span
            v-if="property.fields.salesCount.show"
            :style="{ color: property.fields.salesCount.color }"
          >
            已售{{ (spu.salesCount || 0) + (spu.virtualSalesCount || 0) }}件
          </span>
          <!-- 库存 -->
          <span
            v-if="property.fields.stock.show"
            :style="{ color: property.fields.stock.color }"
          >
            库存{{ spu.stock || 0 }}
          </span>
        </div>
      </div>
      <!-- 购买按钮 -->
      <div class="absolute bottom-[8px] right-[8px]">
        <!-- 文字按钮 -->
        <span
          v-if="property.btnBuy.type === 'text'"
          class="rounded-full px-[12px] py-[4px] text-[12px] text-white"
          :style="{
            background: `linear-gradient(to right, ${property.btnBuy.bgBeginColor}, ${property.btnBuy.bgEndColor}`,
          }"
        >
          {{ property.btnBuy.text }}
        </span>
        <!-- 图片按钮 -->
        <ElImage
          v-else
          class="h-[28px] w-[28px] rounded-full"
          fit="cover"
          :src="property.btnBuy.imgUrl"
        />
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss"></style>
