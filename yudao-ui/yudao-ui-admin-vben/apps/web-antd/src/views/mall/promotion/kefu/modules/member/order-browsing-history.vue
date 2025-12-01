<script lang="ts" setup>
import type { MallKefuConversationApi } from '#/api/mall/promotion/kefu/conversation';

import { computed, reactive, ref } from 'vue';

import { getOrderPage } from '#/api/mall/trade/order';
import OrderItem from '#/views/mall/promotion/kefu/modules/message/order-item.vue';

const list = ref<any>([]); // 列表
const total = ref(0); // 总数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  userId: 0,
});
const skipGetMessageList = computed(() => {
  // 已加载到最后一页的话则不触发新的消息获取
  return (
    total.value > 0 &&
    Math.ceil(total.value / queryParams.pageSize) === queryParams.pageNo
  );
}); // 跳过消息获取

/** 获得浏览记录 */
async function getHistoryList(val: MallKefuConversationApi.Conversation) {
  queryParams.userId = val.userId;
  const res = await getOrderPage(queryParams);
  total.value = res.total;
  list.value = res.list;
}

/** 加载下一页数据 */
async function loadMore() {
  if (skipGetMessageList.value) {
    return;
  }
  queryParams.pageNo += 1;
  const res = await getOrderPage(queryParams);
  total.value = res.total;
  list.value = [...list.value, ...res.list];
}
defineExpose({ getHistoryList, loadMore });
</script>

<template>
  <OrderItem v-for="item in list" :key="item.id" :order="item" />
</template>
