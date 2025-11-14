<!-- 优惠券中心  -->
<template>
  <s-layout :bgStyle="{ color: '#f2f2f2' }" title="优惠券">
    <su-sticky bgColor="#fff">
      <su-tabs
        :list="tabMaps"
        :scrollable="false"
        @change="onTabsChange"
        :current="state.currentTab"
      />
    </su-sticky>
    <s-empty
      v-if="state.pagination.total === 0"
      icon="/static/coupon-empty.png"
      text="暂无优惠券"
    />
    <!-- 情况一：领劵中心 -->
    <template v-if="state.currentTab === 0">
      <view v-for="item in state.pagination.list" :key="item.id">
        <s-coupon-list
          :data="item"
          @tap="sheep.$router.go('/pages/coupon/detail', { id: item.id })"
        >
          <template #default>
            <button
              class="ss-reset-button card-btn ss-flex ss-row-center ss-col-center"
              :class="!item.canTake ? 'border-btn' : ''"
              @click.stop="getBuy(item.id)"
              :disabled="!item.canTake"
            >
              {{ item.canTake ? '立即领取' : '已领取' }}
            </button>
          </template>
        </s-coupon-list>
      </view>
    </template>
    <!-- 情况二：我的优惠劵 -->
    <template v-else>
      <view v-for="item in state.pagination.list" :key="item.id">
        <s-coupon-list
          :data="item"
          type="user"
          @tap="sheep.$router.go('/pages/coupon/detail', { couponId: item.id })"
        >
          <template #default>
            <button
              class="ss-reset-button card-btn ss-flex ss-row-center ss-col-center"
              :class="item.status !== 1 ? 'disabled-btn' : ''"
              :disabled="item.status !== 1"
              @click.stop="sheep.$router.go('/pages/coupon/detail', { couponId: item.id })"
            >
              {{ item.status === 1 ? '立即使用' : item.status === 2 ? '已使用' : '已过期' }}
            </button>
          </template>
        </s-coupon-list>
      </view>
    </template>

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
  import { resetPagination } from '@/sheep/helper/utils';
  import CouponApi from '@/sheep/api/promotion/coupon';

  // 数据
  const state = reactive({
    currentTab: 0, // 当前 tab
    type: '1',
    pagination: {
      list: [],
      total: 0,
      pageNo: 1,
      pageSize: 5,
    },
    loadStatus: '',
  });

  const tabMaps = [
    {
      name: '领券中心',
      value: 'all',
    },
    {
      name: '已领取',
      value: '1',
    },
    {
      name: '已使用',
      value: '2',
    },
    {
      name: '已失效',
      value: '3',
    },
  ];

  function onTabsChange(e) {
    state.currentTab = e.index;
    state.type = e.value;
    resetPagination(state.pagination);
    if (state.currentTab === 0) {
      getData();
    } else {
      getCoupon();
    }
  }

  // 获得优惠劵模版列表
  async function getData() {
    state.loadStatus = 'loading';
    const { data, code } = await CouponApi.getCouponTemplatePage({
      pageNo: state.pagination.pageNo,
      pageSize: state.pagination.pageSize,
    });
    if (code !== 0) {
      return;
    }
    state.pagination.list = _.concat(state.pagination.list, data.list);
    state.pagination.total = data.total;
    state.loadStatus = state.pagination.list.length < state.pagination.total ? 'more' : 'noMore';
  }

  // 获得我的优惠劵
  async function getCoupon() {
    state.loadStatus = 'loading';
    const { data, code } = await CouponApi.getCouponPage({
      pageNo: state.pagination.pageNo,
      pageSize: state.pagination.pageSize,
      status: state.type,
    });
    if (code !== 0) {
      return;
    }
    state.pagination.list = _.concat(state.pagination.list, data.list);
    state.pagination.total = data.total;
    state.loadStatus = state.pagination.list.length < state.pagination.total ? 'more' : 'noMore';
  }

  // 领取优惠劵
  async function getBuy(id) {
    const { code } = await CouponApi.takeCoupon(id);
    if (code !== 0) {
      return;
    }
    uni.showToast({
      title: '领取成功',
    });
    setTimeout(() => {
      resetPagination(state.pagination);
      getData();
    }, 1000);
  }

  // 加载更多
  function loadMore() {
    if (state.loadStatus === 'noMore') {
      return;
    }
    state.pagination.pageNo++;
    if (state.currentTab === 0) {
      getData();
    } else {
      getCoupon();
    }
  }

  onLoad((Option) => {
    // 领劵中心
    if (Option.type === 'all' || !Option.type) {
      getData();
      // 我的优惠劵
    } else {
      Option.type === 'geted'
        ? (state.currentTab = 1)
        : Option.type === 'used'
        ? (state.currentTab = 2)
        : (state.currentTab = 3);
      state.type = state.currentTab;
      getCoupon();
    }
  });

  onReachBottom(() => {
    loadMore();
  });
</script>
<style lang="scss" scoped>
  .card-btn {
    // width: 144rpx;
    padding: 0 16rpx;
    height: 50rpx;
    border-radius: 40rpx;
    background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
    color: #ffffff;
    font-size: 24rpx;
    font-weight: 400;
  }

  .border-btn {
    background: linear-gradient(90deg, var(--ui-BG-Main-opacity-4), var(--ui-BG-Main-light));
    color: #fff !important;
  }

  .disabled-btn {
    background: #cccccc;
    background-color: #cccccc !important;
    color: #fff !important;
  }
</style>
