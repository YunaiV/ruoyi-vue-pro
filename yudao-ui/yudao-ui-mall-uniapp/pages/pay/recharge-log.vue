<!-- 充值记录 -->
<template>
  <s-layout class="widthdraw-log-wrap" title="充值记录">
    <!-- 记录卡片 -->
    <view class="wallet-log-box ss-p-b-30">
      <view class="log-list" v-for="item in state.pagination.list" :key="item">
        <view class="head ss-flex ss-col-center ss-row-between">
          <view class="title">充值金额</view>
          <view class="num" :class="item.refundStatus === 10 ? 'danger-color' : 'success-color'">
            {{ fen2yuan(item.payPrice) }} 元
            <text v-if="item.bonusPrice > 0">（赠送 {{ fen2yuan(item.bonusPrice) }} 元）</text>
          </view>
        </view>
        <view class="status-box item ss-flex ss-col-center ss-row-between">
          <view class="item-title">支付状态</view>
          <view
            class="status-text"
            :class="item.refundStatus === 10 ? 'danger-color' : 'success-color'"
          >
            {{ item.refundStatus === 10 ? '已退款' : '已支付' }}
          </view>
        </view>
        <view class="time-box item ss-flex ss-col-center ss-row-between">
          <text class="item-title">充值渠道</text>
          <view class="time ss-ellipsis-1">{{ item.payChannelName }}</view>
        </view>
        <view class="time-box item ss-flex ss-col-center ss-row-between">
          <text class="item-title">充值单号</text>
          <view class="time"> {{ item.payOrderChannelOrderNo }} </view>
        </view>
        <view class="time-box item ss-flex ss-col-center ss-row-between">
          <text class="item-title">充值时间</text>
          <view class="time">
            {{ sheep.$helper.timeFormat(item.payTime, 'yyyy-mm-dd hh:MM:ss') }}</view
          >
        </view>
      </view>
    </view>
    <s-empty
      v-if="state.pagination.total === 0"
      icon="/static/comment-empty.png"
      text="暂无充值记录"
    />
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
  import _ from 'lodash-es';
  import PayWalletApi from '@/sheep/api/pay/wallet';
  import sheep from '@/sheep';
  import { fen2yuan } from '../../sheep/hooks/useGoods';

  const state = reactive({
    pagination: {
      list: [],
      total: 0,
      pageNo: 1,
      pageSize: 5,
    },
    loadStatus: '',
  });

  async function getLogList(page = 1, list_rows = 5) {
    const { code, data } = await PayWalletApi.getWalletRechargePage({
      pageNo: page,
      pageSize: list_rows,
    });
    if (code !== 0) {
      return;
    }
    state.pagination.list = _.concat(state.pagination.list, data.list);
    state.pagination.total = data.total;
    state.loadStatus = state.pagination.list.length < state.pagination.total ? 'more' : 'noMore';
  }

  // 加载更多
  function loadMore() {
    if (state.loadStatus === 'noMore') {
      return;
    }
    state.pagination.pageNo++;
    getLogList();
  }

  onLoad(() => {
    getLogList();
  });

  onReachBottom(() => {
    loadMore();
  });
</script>

<style lang="scss" scoped>
  // 记录卡片
  .log-list {
    min-height: 213rpx;
    background: $white;
    margin-bottom: 10rpx;
    padding-bottom: 10rpx;

    .head {
      padding: 0 35rpx;
      height: 80rpx;
      border-bottom: 1rpx solid $gray-e;
      margin-bottom: 20rpx;

      .title {
        font-size: 28rpx;
        font-weight: 500;
        color: $dark-3;
      }

      .num {
        font-size: 28rpx;
        font-weight: 500;
      }
    }

    .item {
      padding: 0 30rpx 10rpx;

      .item-icon {
        color: $gray-d;
        font-size: 36rpx;
        margin-right: 8rpx;
      }

      .item-title {
        width: 180rpx;
        font-size: 24rpx;
        font-weight: 400;
        color: #666666;
      }

      .status-text {
        font-size: 24rpx;
        font-weight: 500;
      }

      .time {
        font-size: 24rpx;
        font-weight: 400;
        color: #c0c0c0;
      }
    }
  }
  .warning-color {
    color: #faad14;
  }
  .danger-color {
    color: #ff4d4f;
  }
  .success-color {
    color: #67c23a;
  }
</style>
