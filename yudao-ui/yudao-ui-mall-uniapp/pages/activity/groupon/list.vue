<!-- 拼团活动列表 -->
<template>
  <s-layout :bgStyle="{ color: '#FE832A' }" navbar="inner">
    <view class="page-bg" :style="[{ marginTop: '-' + Number(statusBarHeight + 88) + 'rpx' }]" />
    <view class="list-content">
      <!-- 参团会员统计 -->
      <view class="content-header ss-flex-col ss-col-center ss-row-center">
        <view class="content-header-title ss-flex ss-row-center">
          <view
            v-for="(item, index) in state.summaryData.avatars"
            :key="index"
            class="picture"
            :style="index === 6 ? 'position: relative' : 'position: static'"
          >
            <span class="avatar" :style="`background-image: url(${item})`" />
            <span v-if="index === 6 && state.summaryData.avatars.length > 3" class="mengceng">
              <i>···</i>
            </span>
          </view>
          <text class="pic_count">{{ state.summaryData.userCount || 0 }}人参与</text>
        </view>
      </view>
      <scroll-view
        class="scroll-box"
        :style="{ height: pageHeight + 'rpx' }"
        scroll-y="true"
        :scroll-with-animation="false"
        :enable-back-to-top="true"
      >
        <view class="goods-box ss-m-b-20" v-for="item in state.pagination.list" :key="item.id">
          <s-goods-column
            class=""
            size="lg"
            :data="item"
            :grouponTag="true"
            @click="sheep.$router.go('/pages/goods/groupon', { id: item.id })"
          >
            <template v-slot:cart>
              <button class="ss-reset-button cart-btn">去拼团</button>
            </template>
          </s-goods-column>
        </view>
        <uni-load-more
          v-if="state.pagination.total > 0"
          :status="state.loadStatus"
          :content-text="{
            contentdown: '上拉加载更多',
          }"
          @tap="loadMore"
        />
      </scroll-view>
    </view>
  </s-layout>
</template>
<script setup>
  import { reactive } from 'vue';
  import { onLoad, onReachBottom } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import CombinationApi from '@/sheep/api/promotion/combination';

  const { safeAreaInsets, safeArea } = sheep.$platform.device;
  const sysNavBar = sheep.$platform.navbar;
  const statusBarHeight = sheep.$platform.device.statusBarHeight * 2;
  const pageHeight =
    (safeArea.height + safeAreaInsets.bottom) * 2 + statusBarHeight - sysNavBar - 350;
  const headerBg = sheep.$url.css('/static/img/shop/goods/groupon-header.png');

  const state = reactive({
    pagination: {
      list: [],
      total: 0,
      pageNo: 1,
      pageSize: 10,
    },
    loadStatus: '',
    summaryData: {},
  });

  // 加载统计数据
  const getSummary = async () => {
    const { data } = await CombinationApi.getCombinationRecordSummary();
    state.summaryData = data;
  };

  // 加载活动列表
  async function getList() {
    state.loadStatus = 'loading';
    const { data } = await CombinationApi.getCombinationActivityPage({
      pageNo: state.pagination.pageNo,
      pageSize: state.pagination.pageSize,
    });
    data.list.forEach((activity) => {
      state.pagination.list.push({ ...activity, price: activity.combinationPrice });
    });
    state.pagination.total = data.total;
    state.loadStatus = state.pagination.list.length < state.pagination.total ? 'more' : 'noMore';
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
  onReachBottom(() => loadMore());

  // 页面初始化
  onLoad(() => {
    getSummary();
    getList();
  });
</script>
<style lang="scss" scoped>
  .page-bg {
    width: 100%;
    height: 458rpx;
    margin-top: -88rpx;
    background: v-bind(headerBg) no-repeat;
    background-size: 100% 100%;
  }
  .list-content {
    position: relative;
    z-index: 3;
    margin: -190rpx 20rpx 0 20rpx;
    background: #fff;
    border-radius: 20rpx 20rpx 0 0;
    .content-header {
      width: 100%;
      border-radius: 20rpx 20rpx 0 0;
      height: 100rpx;
      background: linear-gradient(180deg, #fff4f7, #ffe4d1);
      .content-header-title {
        width: 100%;
        font-size: 30rpx;
        font-weight: 500;
        color: #ff2923;
        line-height: 30rpx;
        position: relative;
        .more {
          position: absolute;
          right: 30rpx;
          top: 0;
          font-size: 24rpx;
          font-weight: 400;
          color: #999999;
          line-height: 30rpx;
        }

        .picture {
          display: inline-table;
        }

        .avatar {
          width: 38rpx;
          height: 38rpx;
          display: inline-table;
          vertical-align: middle;
          -webkit-user-select: none;
          -moz-user-select: none;
          -ms-user-select: none;
          user-select: none;
          border-radius: 50%;
          background-repeat: no-repeat;
          background-size: cover;
          background-position: 0 0;
          margin-right: -10rpx;
          box-shadow: 0 0 0 1px #fe832a;
        }

        .pic_count {
          margin-left: 30rpx;
          font-size: 22rpx;
          font-weight: 500;
          width: auto;
          height: auto;
          background: linear-gradient(90deg, #ff6600 0%, #fe832a 100%);
          color: #ffffff;
          border-radius: 19rpx;
          padding: 4rpx 14rpx;
        }

        .mengceng {
          width: 40rpx;
          height: 40rpx;
          line-height: 36rpx;
          background: rgba(51, 51, 51, 0.6);
          text-align: center;
          border-radius: 50%;
          opacity: 1;
          position: absolute;
          left: -2rpx;
          color: #fff;
          top: 2rpx;
          i {
            font-style: normal;
            font-size: 20rpx;
          }
        }
      }
    }
    .scroll-box {
      height: 900rpx;
      .goods-box {
        position: relative;
        .cart-btn {
          position: absolute;
          bottom: 10rpx;
          right: 20rpx;
          z-index: 11;
          height: 50rpx;
          line-height: 50rpx;
          padding: 0 20rpx;
          border-radius: 25rpx;
          font-size: 24rpx;
          color: #fff;
          background: linear-gradient(90deg, #ff6600 0%, #fe832a 100%);
        }
      }
    }
  }
</style>
