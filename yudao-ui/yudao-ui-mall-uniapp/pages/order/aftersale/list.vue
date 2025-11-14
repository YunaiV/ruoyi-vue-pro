<!-- 售后列表 -->
<template>
  <s-layout title="售后列表">
    <!-- tab -->
    <su-sticky bgColor="#fff">
      <su-tabs
        :list="tabMaps"
        :scrollable="false"
        @change="onTabsChange"
        :current="state.currentTab"
      />
    </su-sticky>
    <s-empty v-if="state.pagination.total === 0" icon="/static/data-empty.png" text="暂无数据" />
    <!-- 列表 -->
    <view v-if="state.pagination.total > 0">
      <view
        class="list-box ss-m-y-20"
        v-for="order in state.pagination.list"
        :key="order.id"
        @tap="sheep.$router.go('/pages/order/aftersale/detail', { id: order.id })"
      >
        <view class="order-head ss-flex ss-col-center ss-row-between">
          <text class="no">服务单号：{{ order.no }}</text>
          <text class="state">{{ formatAfterSaleStatus(order) }}</text>
        </view>
        <s-goods-item
          :img="order.picUrl"
          :title="order.spuName"
          :skuText="order.properties.map((property) => property.valueName).join(' ')"
          :price="order.refundPrice"
        />
        <view class="apply-box ss-flex ss-col-center ss-row-between border-bottom ss-p-x-20">
          <view class="ss-flex ss-col-center">
            <view class="title ss-m-r-20">{{ order.way === 10 ? '仅退款' : '退款退货' }}</view>
            <view class="value">{{ formatAfterSaleStatusDescription(order) }}</view>
          </view>
          <text class="_icon-forward"></text>
        </view>
        <view class="tool-btn-box ss-flex ss-col-center ss-row-right ss-p-r-20">
          <view>
            <button
              class="ss-reset-button tool-btn"
              @tap.stop="onApply(order.id)"
              v-if="order?.buttons.includes('cancel')"
            >
              取消申请
            </button>
          </view>
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
  import sheep from '@/sheep';
  import { onLoad, onReachBottom } from '@dcloudio/uni-app';
  import { reactive } from 'vue';
  import _ from 'lodash-es';
  import {
    formatAfterSaleStatus,
    formatAfterSaleStatusDescription,
    handleAfterSaleButtons,
  } from '@/sheep/hooks/useGoods';
  import AfterSaleApi from '@/sheep/api/trade/afterSale';
  import { resetPagination } from '@/sheep/helper/utils';

  const state = reactive({
    currentTab: 0,
    showApply: false,
    pagination: {
      list: [],
      total: 0,
      pageNo: 1,
      pageSize: 10,
    },
    loadStatus: '',
  });

  const tabMaps = [
    {
      name: '全部',
      value: [],
    },
    {
      name: '申请中',
      value: [10],
    },
    {
      name: '处理中',
      value: [20, 30, 40],
    },
    {
      name: '已完成',
      value: [50],
    },
    {
      name: '已拒绝',
      value: [61, 62, 63],
    },
  ];

  // 切换选项卡
  function onTabsChange(e) {
    resetPagination(state.pagination);
    state.currentTab = e.index;
    getOrderList();
  }

  // 获取售后列表
  async function getOrderList() {
    state.loadStatus = 'loading';
    let { data, code } = await AfterSaleApi.getAfterSalePage({
      pageNo: state.pagination.pageNo,
      pageSize: state.pagination.pageSize,
      statuses: tabMaps[state.currentTab].value.join(','),
    });
    if (code !== 0) {
      return;
    }
    data.list.forEach((order) => handleAfterSaleButtons(order));
    state.pagination.list = _.concat(state.pagination.list, data.list);
    state.pagination.total = data.total;
    state.loadStatus = state.pagination.list.length < state.pagination.total ? 'more' : 'noMore';
  }

  function onApply(orderId) {
    uni.showModal({
      title: '提示',
      content: '确定要取消此申请吗？',
      success: async function (res) {
        if (!res.confirm) {
          return;
        }
        const { code } = await AfterSaleApi.cancelAfterSale(orderId);
        if (code === 0) {
          resetPagination(state.pagination);
          await getOrderList();
        }
      },
    });
  }

  onLoad(async (options) => {
    if (options.type) {
      state.currentTab = options.type;
    }
    await getOrderList();
  });

  // 加载更多
  function loadMore() {
    if (state.loadStatus === 'noMore') {
      return;
    }
    state.pagination.pageNo++;
    getOrderList();
  }

  // 上拉加载更多
  onReachBottom(() => {
    loadMore();
  });
</script>

<style lang="scss" scoped>
  .list-box {
    background-color: #fff;

    .order-head {
      padding: 0 25rpx;
      height: 77rpx;
    }

    .apply-box {
      height: 82rpx;

      .title {
        font-size: 24rpx;
      }

      .value {
        font-size: 22rpx;
        color: $dark-6;
      }
    }

    .tool-btn-box {
      height: 100rpx;

      .tool-btn {
        width: 160rpx;
        height: 60rpx;
        background: #f6f6f6;
        border-radius: 30rpx;
        font-size: 26rpx;
        font-weight: 400;
      }
    }
  }
</style>
