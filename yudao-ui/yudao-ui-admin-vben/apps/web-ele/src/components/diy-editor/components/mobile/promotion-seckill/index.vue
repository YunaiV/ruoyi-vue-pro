<script setup lang="ts">
import type { PromotionSeckillProperty } from './config';

import type { MallSpuApi } from '#/api/mall/product/spu';
import type { MallSeckillActivityApi } from '#/api/mall/promotion/seckill/seckillActivity';

import { ref, watch } from 'vue';

import { fenToYuan } from '@vben/utils';

import { ElImage } from 'element-plus';

import * as ProductSpuApi from '#/api/mall/product/spu';
import * as SeckillActivityApi from '#/api/mall/promotion/seckill/seckillActivity';

/** 秒杀卡片 */
defineOptions({ name: 'PromotionSeckill' });
// 定义属性
const props = defineProps<{ property: PromotionSeckillProperty }>();
// 商品列表
const spuList = ref<MallSpuApi.Spu[]>([]);
const spuIdList = ref<number[]>([]);
const seckillActivityList = ref<MallSeckillActivityApi.SeckillActivity[]>([]);

watch(
  () => props.property.activityIds,
  async () => {
    try {
      // 新添加的秒杀组件，是没有活动ID的
      const activityIds = props.property.activityIds;
      // 检查活动ID的有效性
      if (Array.isArray(activityIds) && activityIds.length > 0) {
        // 获取秒杀活动详情列表
        seckillActivityList.value =
          await SeckillActivityApi.getSeckillActivityListByIds(activityIds);

        // 获取秒杀活动的 SPU 详情列表
        spuList.value = [];
        spuIdList.value = seckillActivityList.value
          .map((activity) => activity.spuId)
          .filter((spuId): spuId is number => typeof spuId === 'number');
        if (spuIdList.value.length > 0) {
          spuList.value = await ProductSpuApi.getSpuDetailList(spuIdList.value);
        }

        // 更新 SPU 的最低价格
        seckillActivityList.value.forEach((activity) => {
          // 匹配spuId
          const spu = spuList.value.find((spu) => spu.id === activity.spuId);
          if (spu) {
            // 赋值活动价格，哪个最便宜就赋值哪个
            spu.price = Math.min(
              activity.seckillPrice || Infinity,
              spu.price || Infinity,
            );
          }
        });
      }
    } catch (error) {
      console.error('获取秒杀活动细节或 SPU 细节时出错:', error);
    }
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
        v-if="property.badge.show"
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
        class="box-border flex flex-col gap-2 p-2"
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
          class="text-sm"
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
          class="truncate text-xs"
          :style="{ color: property.fields.introduction.color }"
        >
          {{ spu.introduction }}
        </div>
        <div>
          <!-- 价格 -->
          <span
            v-if="property.fields.price.show"
            class="text-base"
            :style="{ color: property.fields.price.color }"
          >
            ￥{{ fenToYuan(spu.price || Infinity) }}
          </span>
          <!-- 市场价 -->
          <span
            v-if="property.fields.marketPrice.show && spu.marketPrice"
            class="ml-1 text-[10px] line-through"
            :style="{ color: property.fields.marketPrice.color }"
          >
            ￥{{ fenToYuan(spu.marketPrice) }}
          </span>
        </div>
        <div class="text-xs">
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
      <div class="absolute bottom-2 right-2">
        <!-- 文字按钮 -->
        <span
          v-if="property.btnBuy.type === 'text'"
          class="rounded-full px-3 py-1 text-xs text-white"
          :style="{
            background: `linear-gradient(to right, ${property.btnBuy.bgBeginColor}, ${property.btnBuy.bgEndColor}`,
          }"
        >
          {{ property.btnBuy.text }}
        </span>
        <!-- 图片按钮 -->
        <ElImage
          v-else
          class="h-7 w-7 rounded-full"
          fit="cover"
          :src="property.btnBuy.imgUrl"
        />
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss"></style>
