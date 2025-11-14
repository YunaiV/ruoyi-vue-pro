<!-- 我的拼团订单列表 -->
<template>
  <s-layout title="我的拼团">
    <su-sticky bgColor="#fff">
      <su-tabs
        :list="tabMaps"
        :scrollable="false"
        @change="onTabsChange"
        :current="state.currentTab"
      ></su-tabs>
    </su-sticky>
    <s-empty v-if="state.pagination.total === 0" icon="/static/goods-empty.png" />
    <view v-if="state.pagination.total > 0">
      <view
        class="order-list-card-box bg-white ss-r-10 ss-m-t-14 ss-m-20"
        v-for="record in state.pagination.list"
        :key="record.id"
      >
        <view class="order-card-header ss-flex ss-col-center ss-row-between ss-p-x-20">
          <view class="order-no">拼团编号：{{ record.id }}</view>
          <view class="ss-font-26" :class="formatOrderColor(record)">
            {{ tabMaps.find((item) => item.value === record.status).name }}
          </view>
        </view>
        <view class="border-bottom">
          <s-goods-item
            :img="record.picUrl"
            :title="record.spuName"
            :price="record.combinationPrice"
          >
            <template #groupon>
              <view class="ss-flex">
                <view class="sales-title"> {{ record.userSize }} 人团 </view>
              </view>
            </template>
          </s-goods-item>
        </view>
        <view class="order-card-footer ss-flex ss-row-right ss-p-x-20">
          <button
            class="detail-btn ss-reset-button"
            @tap="sheep.$router.go('/pages/order/detail', { id: record.orderId })"
          >
            订单详情
          </button>
          <button
            class="tool-btn ss-reset-button"
            :class="{ 'ui-BG-Main-Gradient': record.status === 0 }"
            @tap="sheep.$router.go('/pages/activity/groupon/detail', { id: record.id })"
          >
            {{ record.status === 0 ? '邀请拼团' : '拼团详情' }}
          </button>
        </view>
      </view>
    </view>
    <uni-load-more
      v-if="state.pagination.total > 0"
      :status="state.loadStatus"
      :content-text="{
        contentdown: '上拉加载更多',
      }"
      @tap="loadMore"
    />
  </s-layout>
</template>

<script setup>
  import { reactive } from 'vue';
  import { onLoad, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import _ from 'lodash-es';
  import { formatOrderColor } from '@/sheep/hooks/useGoods';
  import { resetPagination } from '@/sheep/helper/utils';
  import CombinationApi from '@/sheep/api/promotion/combination';

  // 数据
  const state = reactive({
    currentTab: 0,
    pagination: {
      list: [],
      total: 0,
      pageNo: 1,
      pageSize: 5,
    },
    loadStatus: '',
    deleteOrderId: 0,
  });

  const tabMaps = [
    {
      name: '全部',
    },
    {
      name: '进行中',
      value: 0,
    },
    {
      name: '拼团成功',
      value: 1,
    },
    {
      name: '拼团失败',
      value: 2,
    },
  ];

  // 切换选项卡
  function onTabsChange(e) {
    resetPagination(state.pagination);
    state.currentTab = e.index;
    getGrouponList();
  }

  // 获取订单列表
  async function getGrouponList() {
    state.loadStatus = 'loading';
    const { code, data } = await CombinationApi.getCombinationRecordPage({
      pageNo: state.pagination.pageNo,
      pageSize: state.pagination.pageSize,
      status: tabMaps[state.currentTab].value,
    });
    if (code !== 0) {
      return;
    }
    state.pagination.list = _.concat(state.pagination.list, data.list);
    state.pagination.total = data.total;
    state.loadStatus = state.pagination.list.length < state.pagination.total ? 'more' : 'noMore';
  }

  onLoad((options) => {
    if (options.type) {
      state.currentTab = options.type;
    }
    getGrouponList();
  });

  // 加载更多
  function loadMore() {
    if (state.loadStatus === 'noMore') {
      return;
    }
    state.pagination.pageNo++;
    getGrouponList();
  }

  // 上拉加载更多
  onReachBottom(() => {
    loadMore();
  });

  //下拉刷新
  onPullDownRefresh(() => {
    resetPagination(state.pagination);
    getGrouponList();
    setTimeout(function () {
      uni.stopPullDownRefresh();
    }, 800);
  });
</script>

<style lang="scss" scoped>
  .swiper-box {
    flex: 1;

    .swiper-item {
      height: 100%;
      width: 100%;
    }
  }

  .order-list-card-box {
    .order-card-header {
      height: 80rpx;

      .order-no {
        font-size: 26rpx;
        font-weight: 500;
      }
    }

    .order-card-footer {
      height: 100rpx;

      .detail-btn {
        width: 210rpx;
        height: 66rpx;
        border: 2rpx solid #dfdfdf;
        border-radius: 33rpx;
        font-size: 26rpx;
        font-weight: 400;
        color: #999999;
        margin-right: 20rpx;
      }
      .tool-btn {
        width: 210rpx;
        height: 66rpx;
        border-radius: 33rpx;
        font-size: 26rpx;
        font-weight: 400;
        margin-right: 20rpx;
        background: #f6f6f6;
      }

      .invite-btn {
        width: 210rpx;
        height: 66rpx;
        background: linear-gradient(90deg, #fe832a, #ff6600);
        box-shadow: 0px 8rpx 6rpx 0px rgba(255, 104, 4, 0.22);
        border-radius: 33rpx;
        color: #fff;
        font-size: 26rpx;
        font-weight: 500;
      }
    }
  }

  .sales-title {
    height: 32rpx;
    background: rgba(#ffe0e2, 0.29);
    border-radius: 16rpx;
    font-size: 24rpx;
    font-weight: 400;
    padding: 6rpx 20rpx;
    color: #f7979c;
  }

  .num-title {
    font-size: 24rpx;
    font-weight: 400;
    color: #999999;
  }
  .warning-color {
    color: #faad14;
  }
  .danger-color {
    color: #ff3000;
  }
  .success-color {
    color: #52c41a;
  }
</style>
