<script lang="ts" setup>
import type { PromotionPointProperty } from './config';

import type { MallPointActivityApi } from '#/api/mall/promotion/point';

import { ref, watch } from 'vue';

import { fenToYuan } from '@vben/utils';

import { Image } from 'ant-design-vue';

import { getSpuDetailList } from '#/api/mall/product/spu';
import { getPointActivityListByIds } from '#/api/mall/promotion/point';

/** 积分商城卡片 */
defineOptions({ name: 'PromotionPoint' });

const props = defineProps<{ property: PromotionPointProperty }>();

const spuList = ref<MallPointActivityApi.SpuExtensionWithPoint[]>([]); // 商品列表
const spuIdList = ref<number[]>([]);
const pointActivityList = ref<MallPointActivityApi.PointActivity[]>([]);

watch(
  () => props.property.activityIds,
  async () => {
    try {
      // 新添加的积分商城组件，是没有活动ID的
      const activityIds = props.property.activityIds;
      // 检查活动 ID 的有效性
      if (Array.isArray(activityIds) && activityIds.length > 0) {
        // 获取积分商城活动详情列表
        pointActivityList.value = await getPointActivityListByIds(activityIds);

        // 获取积分商城活动的 SPU 详情列表
        spuList.value = [];
        spuIdList.value = pointActivityList.value.map(
          (activity) => activity.spuId,
        );
        if (spuIdList.value.length > 0) {
          spuList.value = (await getSpuDetailList(
            spuIdList.value,
          )) as MallPointActivityApi.SpuExtensionWithPoint[];
        }

        // 更新 SPU 的最低兑换积分和所需兑换金额
        pointActivityList.value.forEach((activity) => {
          // 匹配spuId
          const spu = spuList.value.find((spu) => spu.id === activity.spuId);
          if (spu) {
            spu.pointStock = activity.stock;
            spu.pointTotalStock = activity.totalStock;
            spu.point = activity.point;
            spu.pointPrice = activity.price;
          }
        });
      }
    } catch (error) {
      console.error('获取积分商城活动细节或 SPU 细节时出错:', error);
    }
  },
  {
    immediate: true,
    deep: true,
  },
);

/** 计算商品的间距 */
function calculateSpace(index: number) {
  const columns = props.property.layoutType === 'twoCol' ? 2 : 1; // 商品的列数
  const marginLeft = index % columns === 0 ? '0' : `${props.property.space}px`; // 第一列没有左边距
  const marginTop = index < columns ? '0' : `${props.property.space}px`; // 第一行没有上边距
  return { marginLeft, marginTop };
}

const containerRef = ref(); // 容器

/** 计算商品的宽度 */
function calculateWidth() {
  let width = '100%';
  if (props.property.layoutType === 'twoCol') {
    // 双列时每列的宽度为：（总宽度 - 间距）/ 2
    width = `${(containerRef.value.offsetWidth - props.property.space) / 2}px`;
  }
  return { width };
}
</script>
<template>
  <div
    ref="containerRef"
    class="box-content flex min-h-[30px] w-full flex-row flex-wrap"
  >
    <div
      v-for="(spu, index) in spuList"
      :key="index"
      :style="{
        ...calculateSpace(index),
        ...calculateWidth(),
        borderTopLeftRadius: `${property.borderRadiusTop}px`,
        borderTopRightRadius: `${property.borderRadiusTop}px`,
        borderBottomLeftRadius: `${property.borderRadiusBottom}px`,
        borderBottomRightRadius: `${property.borderRadiusBottom}px`,
      }"
      class="relative box-content flex flex-row flex-wrap overflow-hidden bg-card"
    >
      <!-- 角标 -->
      <div
        v-if="property.badge.show"
        class="absolute left-0 top-0 z-[1] items-center justify-center"
      >
        <Image
          :src="property.badge.imgUrl"
          class="h-6 w-10 object-cover"
          :preview="false"
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
        <Image
          :src="spu.picUrl"
          class="h-full w-full object-cover"
          :preview="false"
        />
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
          :style="{ color: property.fields.introduction.color }"
          class="truncate text-xs"
        >
          {{ spu.introduction }}
        </div>
        <div>
          <!-- 积分 -->
          <span
            v-if="property.fields.price.show"
            :style="{ color: property.fields.price.color }"
            class="text-base"
          >
            {{ spu.point }}积分
            {{
              !spu.pointPrice || spu.pointPrice === 0
                ? ''
                : `+${fenToYuan(spu.pointPrice)}元`
            }}
          </span>
          <!-- 市场价 -->
          <span
            v-if="property.fields.marketPrice.show && spu.marketPrice"
            :style="{ color: property.fields.marketPrice.color }"
            class="ml-1 text-xs line-through"
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
            已兑{{ (spu.pointTotalStock || 0) - (spu.pointStock || 0) }}件
          </span>
          <!-- 库存 -->
          <span
            v-if="property.fields.stock.show"
            :style="{ color: property.fields.stock.color }"
          >
            库存{{ spu.pointTotalStock || 0 }}
          </span>
        </div>
      </div>
      <!-- 购买按钮 -->
      <div class="absolute bottom-2 right-2">
        <!-- 文字按钮 -->
        <span
          v-if="property.btnBuy.type === 'text'"
          :style="{
            background: `linear-gradient(to right, ${property.btnBuy.bgBeginColor}, ${property.btnBuy.bgEndColor}`,
          }"
          class="rounded-full px-3 py-1 text-sm text-white"
        >
          {{ property.btnBuy.text }}
        </span>
        <!-- 图片按钮 -->
        <Image
          v-else
          :src="property.btnBuy.imgUrl"
          class="size-7 rounded-full object-cover"
          :preview="false"
        />
      </div>
    </div>
  </div>
</template>
