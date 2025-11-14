<!-- 指定满减送的活动列表 -->
<template>
  <s-layout class="activity-wrap" :title="state.activityInfo.title">
    <!-- 活动信息 -->
    <su-sticky bgColor="#fff">
      <view class="ss-flex ss-col-top tip-box">
        <view class="type-text ss-flex ss-row-center">满减：</view>
        <view class="ss-flex-1">
          <view class="tip-content" v-for="item in state.activityInfo.rules" :key="item">
            {{ item.description }}
          </view>
        </view>
        <image class="activity-left-image" src="/static/activity-left.png" />
        <image class="activity-right-image" src="/static/activity-right.png" />
      </view>
    </su-sticky>

    <!-- 商品信息 -->
    <view class="ss-flex ss-flex-wrap ss-p-x-20 ss-m-t-20 ss-col-top">
      <view class="goods-list-box">
        <view class="left-list" v-for="item in state.leftGoodsList" :key="item.id">
          <s-goods-column
            class="goods-md-box"
            size="md"
            :data="item"
            @click="sheep.$router.go('/pages/goods/index', { id: item.id })"
            @getHeight="mountMasonry($event, 'left')"
          >
            <template v-slot:cart>
              <button class="ss-reset-button cart-btn"> </button>
            </template>
          </s-goods-column>
        </view>
      </view>
      <view class="goods-list-box">
        <view class="right-list" v-for="item in state.rightGoodsList" :key="item.id">
          <s-goods-column
            class="goods-md-box"
            size="md"
            :data="item"
            @click="sheep.$router.go('/pages/goods/index', { id: item.id })"
            @getHeight="mountMasonry($event, 'right')"
          >
            <template v-slot:cart>
              <button class="ss-reset-button cart-btn" />
            </template>
          </s-goods-column>
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
  import { onLoad, onReachBottom } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import _ from 'lodash-es';
  import RewardActivityApi from '@/sheep/api/promotion/rewardActivity';
  import SpuApi from '@/sheep/api/product/spu';
  import { appendSettlementProduct } from '@/sheep/hooks/useGoods';
  import OrderApi from '@/sheep/api/trade/order';

  const state = reactive({
    activityId: 0, // 获得编号
    activityInfo: {}, // 获得信息

    pagination: {
      list: [],
      total: 1,
      pageNo: 1,
      pageSize: 8,
    },
    loadStatus: '',
    leftGoodsList: [],
    rightGoodsList: [],
  });

  // 加载瀑布流
  let count = 0;
  let leftHeight = 0;
  let rightHeight = 0;

  function mountMasonry(height = 0, where = 'left') {
    if (!state.pagination.list[count]) return;

    if (where === 'left') {
      leftHeight += height;
    } else {
      rightHeight += height;
    }
    if (leftHeight <= rightHeight) {
      state.leftGoodsList.push(state.pagination.list[count]);
    } else {
      state.rightGoodsList.push(state.pagination.list[count]);
    }
    count++;
  }

  // 加载商品信息
  async function getList() {
    // 处理拓展参数
    const params = {};
    if (state.activityInfo.productScope === 2) {
      params.ids = state.activityInfo.productSpuIds.join(',');
    } else if (state.activityInfo.productScope === 3) {
      params.categoryIds = state.activityInfo.productSpuIds.join(',');
    }
    // 请求数据
    state.loadStatus = 'loading';
    const { code, data } = await SpuApi.getSpuPage({
      pageNo: state.pagination.pageNo,
      pageSize: state.pagination.pageSize,
      ...params,
    });
    if (code !== 0) {
      return;
    }
    // 拼接结算信息（营销）
    await OrderApi.getSettlementProduct(data.list.map((item) => item.id).join(',')).then((res) => {
      if (res.code !== 0) {
        return;
      }
      appendSettlementProduct(data.list, res.data);
    });
    state.pagination.list = _.concat(state.pagination.list, data.list);
    state.pagination.total = data.total;
    state.loadStatus = state.pagination.list.length < state.pagination.total ? 'more' : 'noMore';
    mountMasonry();
  }

  // 加载活动信息
  async function getActivity(id) {
    const { code, data } = await RewardActivityApi.getRewardActivity(id);
    if (code === 0) {
      state.activityInfo = data;
    }
  }

  // 加载更多
  function loadMore() {
    if (state.loadStatus === 'noMore') {
      return;
    }
    state.pagination.pageNo++;
    getList();
  }

  // 上拉加载更多
  onReachBottom(() => {
    loadMore();
  });

  onLoad(async (options) => {
    state.activityId = options.activityId;
    await getActivity(state.activityId);
    await getList(state.activityId);
  });
</script>
<style lang="scss" scoped>
  .goods-list-box {
    width: 50%;
    box-sizing: border-box;
    .left-list {
      margin-right: 10rpx;
      margin-bottom: 20rpx;
    }
    .right-list {
      margin-left: 10rpx;
      margin-bottom: 20rpx;
    }
  }
  .tip-box {
    background: #fff0e7;
    padding: 20rpx;
    width: 100%;
    position: relative;
    box-sizing: border-box;
    .activity-left-image {
      position: absolute;
      bottom: 0;
      left: 0;
      width: 58rpx;
      height: 36rpx;
    }
    .activity-right-image {
      position: absolute;
      top: 0;
      right: 0;
      width: 72rpx;
      height: 50rpx;
    }
    .type-text {
      font-size: 26rpx;
      font-weight: 500;
      color: #ff6000;
      line-height: 42rpx;
    }
    .tip-content {
      font-size: 26rpx;
      font-weight: 500;
      color: #ff6000;
      line-height: 42rpx;
    }
  }
</style>
