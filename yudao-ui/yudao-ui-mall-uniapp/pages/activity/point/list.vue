<!-- 积分商城：商品列表  -->
<template>
  <s-layout title="积分商城" navbar="normal" :leftWidth="0" :rightWidth="0">
    <scroll-view
      class="scroll-box"
      :style="{ height: pageHeight + 'rpx' }"
      scroll-y="true"
      :scroll-with-animation="false"
      :enable-back-to-top="true"
    >
      <s-point-card ref="sPointCardRef" class="ss-p-x-20 ss-m-t-20" />
      <s-empty
        v-if="activityTotal === 0"
        icon="/static/goods-empty.png"
        text="暂无积分商品"
      ></s-empty>
      <uni-load-more
        v-if="activityTotal > 0"
        :status="loadStatus"
        :content-text="{
          contentdown: '上拉加载更多',
        }"
        @tap="loadMore"
      />
    </scroll-view>
  </s-layout>
</template>
<script setup>
  import sheep from '@/sheep';
  import { onLoad, onReachBottom } from '@dcloudio/uni-app';
  import { reactive, ref } from 'vue';
  import PointApi from '@/sheep/api/promotion/point';
  import SLayout from '@/sheep/components/s-layout/s-layout.vue';

  // 计算页面高度
  const { safeAreaInsets, safeArea } = sheep.$platform.device;
  const statusBarHeight = sheep.$platform.device.statusBarHeight * 2;
  const pageHeight =
    (safeArea.height + safeAreaInsets.bottom) * 2 + statusBarHeight - sheep.$platform.navbar - 350;

  const sPointCardRef = ref();
  // 查询活动列表
  const activityPageParams = reactive({
    pageNo: 1, // 页码
    pageSize: 5, // 每页数量
  });

  const activityTotal = ref(0); // 活动总数
  const activityCount = ref(0); // 已加载活动数量
  const loadStatus = ref(''); // 页面加载状态
  async function getActivityList() {
    loadStatus.value = 'loading';
    const { data } = await PointApi.getPointActivityPage(activityPageParams);
    await sPointCardRef.value.concatActivity(data.list);
    activityCount.value = sPointCardRef.value.getActivityCount();
    activityTotal.value = data.total;

    loadStatus.value = activityCount.value < activityTotal.value ? 'more' : 'noMore';
  }

  // 加载更多
  function loadMore() {
    if (loadStatus.value !== 'noMore') {
      activityPageParams.pageNo += 1;
      getActivityList();
    }
  }

  // 上拉加载更多
  onReachBottom(() => {
    loadMore();
  });
  onLoad(() => {
    getActivityList();
  });
</script>
