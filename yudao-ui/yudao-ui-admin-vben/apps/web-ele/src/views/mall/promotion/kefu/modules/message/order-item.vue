<script lang="ts" setup>
import type { MallKefuMessageApi } from '#/api/mall/promotion/kefu/message';

import { computed } from 'vue';
import { useRouter } from 'vue-router';

import { fenToYuan, isObject, jsonParse } from '@vben/utils';

import ProductItem from './product-item.vue';

const props = defineProps<{
  message?: MallKefuMessageApi.Message;
  order?: any;
}>();

const { push } = useRouter();

const getMessageContent = computed(() =>
  props.message === undefined
    ? props.order
    : jsonParse(props!.message!.content),
);

/** 查看订单详情 */
function openDetail(id: number) {
  push({ name: 'TradeOrderDetail', params: { id } });
}

/** 格式化订单状态的颜色 */
function formatOrderColor(order: any) {
  if (order.status === 0) {
    return 'text-gray-500';
  }
  if (
    order.status === 10 ||
    order.status === 20 ||
    (order.status === 30 && !order.commentStatus)
  ) {
    return 'text-orange-500';
  }
  if (order.status === 30 && order.commentStatus) {
    return 'text-green-500';
  }
  return 'text-red-500';
}

/** 格式化订单状态 */
function formatOrderStatus(order: any) {
  if (order.status === 0) {
    return '待付款';
  }
  if (order.status === 10 && order.deliveryType === 1) {
    return '待发货';
  }
  if (order.status === 10 && order.deliveryType === 2) {
    return '待核销';
  }
  if (order.status === 20) {
    return '待收货';
  }
  if (order.status === 30 && !order.commentStatus) {
    return '待评价';
  }
  if (order.status === 30 && order.commentStatus) {
    return '已完成';
  }
  return '已关闭';
}
</script>

<template>
  <div
    v-if="isObject(getMessageContent)"
    :key="getMessageContent.id"
    class="mb-2 rounded-md bg-gray-500/30 p-2"
  >
    <div class="flex h-6 items-center justify-between px-1 font-bold">
      <div class="flex flex-row text-sm">
        <div>订单号：</div>
        <span
          class="cursor-pointer text-primary hover:underline"
          @click="openDetail(getMessageContent.id)"
        >
          {{ getMessageContent.no }}
        </span>
      </div>
      <div
        :class="formatOrderColor(getMessageContent)"
        class="text-sm font-bold"
      >
        {{ formatOrderStatus(getMessageContent) }}
      </div>
    </div>
    <div
      v-for="item in getMessageContent.items"
      :key="item.id"
      class="border-b border-gray-200"
    >
      <ProductItem
        :num="item.count"
        :pic-url="item.picUrl"
        :price="item.price"
        :sku-text="
          item.properties.map((property: any) => property.valueName).join(' ')
        "
        :spu-id="item.spuId"
        :title="item.spuName"
      />
    </div>
    <div class="flex justify-end pr-1.5 pt-2.5 font-bold">
      <div class="flex items-center">
        <div class="text-sm leading-normal">
          共 {{ getMessageContent?.productCount }} 件商品,总金额:
        </div>
        <div class="text-sm font-medium leading-normal">
          ￥{{ fenToYuan(getMessageContent?.payPrice) }}
        </div>
      </div>
    </div>
  </div>
</template>
