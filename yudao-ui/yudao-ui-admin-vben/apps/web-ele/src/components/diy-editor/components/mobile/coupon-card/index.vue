<script setup lang="ts">
import type { CouponCardProperty } from './config';

import type { MallCouponTemplateApi } from '#/api/mall/promotion/coupon/couponTemplate';

import { onMounted, ref, watch } from 'vue';

import { ElScrollbar } from 'element-plus';

import * as CouponTemplateApi from '#/api/mall/promotion/coupon/couponTemplate';

import {
  CouponDiscount,
  CouponDiscountDesc,
  CouponValidTerm,
} from './component';

/** 商品卡片 */
defineOptions({ name: 'CouponCard' });
// 定义属性
const props = defineProps<{ property: CouponCardProperty }>();
// 商品列表
const couponList = ref<MallCouponTemplateApi.CouponTemplate[]>([]);
watch(
  () => props.property.couponIds,
  async () => {
    if (props.property.couponIds?.length > 0) {
      couponList.value = await CouponTemplateApi.getCouponTemplateList(
        props.property.couponIds,
      );
    }
  },
  {
    immediate: true,
    deep: true,
  },
);

// 手机宽度
const phoneWidth = ref(375);
// 容器
const containerRef = ref();
// 滚动条宽度
const scrollbarWidth = ref('100%');
// 优惠券的宽度
const couponWidth = ref(375);
// 计算布局参数
watch(
  () => [props.property, phoneWidth, couponList.value.length],
  () => {
    // 每列的宽度为：（总宽度 - 间距 * (列数 - 1)）/ 列数
    couponWidth.value =
      (phoneWidth.value - props.property.space * (props.property.columns - 1)) /
      props.property.columns;
    // 显示滚动条
    scrollbarWidth.value = `${
      couponWidth.value * couponList.value.length +
      props.property.space * (couponList.value.length - 1)
    }px`;
  },
  { immediate: true, deep: true },
);
onMounted(() => {
  // 提取手机宽度
  phoneWidth.value = containerRef.value?.wrapRef?.offsetWidth || 375;
});
</script>
<template>
  <ElScrollbar class="z-10 min-h-[30px]" wrap-class="w-full" ref="containerRef">
    <div
      class="flex flex-row text-xs"
      :style="{
        gap: `${property.space}px`,
        width: scrollbarWidth,
      }"
    >
      <div
        class="box-content"
        :style="{
          background: property.bgImg
            ? `url(${property.bgImg}) 100% center / 100% 100% no-repeat`
            : '#fff',
          width: `${couponWidth}px`,
          color: property.textColor,
        }"
        v-for="(coupon, index) in couponList"
        :key="index"
      >
        <!-- 布局1：1列-->
        <div
          v-if="property.columns === 1"
          class="ml-4 flex flex-row justify-between p-2"
        >
          <div class="flex flex-col justify-evenly gap-1">
            <!-- 优惠值 -->
            <CouponDiscount :coupon="coupon" />
            <!-- 优惠描述 -->
            <CouponDiscountDesc :coupon="coupon" />
            <!-- 有效期 -->
            <CouponValidTerm :coupon="coupon" />
          </div>
          <div class="flex flex-col justify-evenly">
            <div
              class="rounded-full px-2 py-0.5"
              :style="{
                color: property.button.color,
                background: property.button.bgColor,
              }"
            >
              立即领取
            </div>
          </div>
        </div>
        <!-- 布局2：2列-->
        <div
          v-else-if="property.columns === 2"
          class="ml-4 flex flex-row justify-between p-2"
        >
          <div class="flex flex-col justify-evenly gap-1">
            <!-- 优惠值 -->
            <CouponDiscount :coupon="coupon" />
            <!-- 优惠描述 -->
            <CouponDiscountDesc :coupon="coupon" />
            <!-- 领取说明 -->
            <div v-if="coupon.totalCount >= 0">
              仅剩：{{ coupon.totalCount - coupon.takeCount }}张
            </div>
            <div v-else-if="coupon.totalCount === -1">仅剩：不限制</div>
          </div>
          <div class="flex flex-col">
            <div
              class="h-full w-5 rounded-full px-0.5 py-2 text-center"
              :style="{
                color: property.button.color,
                background: property.button.bgColor,
              }"
            >
              立即领取
            </div>
          </div>
        </div>
        <!-- 布局3：3列-->
        <div v-else class="flex flex-col items-center justify-around gap-1 p-1">
          <!-- 优惠值 -->
          <CouponDiscount :coupon="coupon" />
          <!-- 优惠描述 -->
          <CouponDiscountDesc :coupon="coupon" />
          <div
            class="rounded-full px-2 py-0.5"
            :style="{
              color: property.button.color,
              background: property.button.bgColor,
            }"
          >
            立即领取
          </div>
        </div>
      </div>
    </div>
  </ElScrollbar>
</template>
<style scoped lang="scss"></style>
