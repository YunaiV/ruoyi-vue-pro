<!-- 分销首页：明细列表  -->
<template>
  <view class="distribution-log-wrap">
    <view class="header-box">
      <image class="header-bg" :src="sheep.$url.static('/static/img/shop/commission/title2.png')" />
      <view class="ss-flex header-title">
        <view class="title">实时动态</view>
        <text class="cicon-forward" />
      </view>
    </view>
    <scroll-view
      scroll-y="true"
      @scrolltolower="loadmore"
      class="scroll-box log-scroll"
      scroll-with-animation="true"
    >
      <view v-if="state.pagination.list">
        <view
          class="log-item-box ss-flex ss-row-between"
          v-for="item in state.pagination.list"
          :key="item.id"
        >
          <view class="log-item-wrap">
            <view class="log-item ss-flex ss-ellipsis-1 ss-col-center">
              <view class="ss-flex ss-col-center">
                <image
                  class="log-img"
                  :src="sheep.$url.static('/static/img/shop/avatar/notice.png')"
                  mode="aspectFill"
                />
              </view>
              <view class="log-text ss-ellipsis-1">
                {{ item.title }} {{ fen2yuan(item.price) }} 元
              </view>
            </view>
          </view>
          <text class="log-time">{{ dayjs(item.createTime).fromNow() }}</text>
        </view>
      </view>

      <!-- 加载更多 -->
      <uni-load-more
        v-if="state.pagination.total > 0"
        :status="state.loadStatus"
        color="#333333"
        @tap="loadmore"
      />
    </scroll-view>
  </view>
</template>

<script setup>
  import sheep from '@/sheep';
  import { reactive } from 'vue';
  import _ from 'lodash-es';
  import dayjs from 'dayjs';
  import BrokerageApi from '@/sheep/api/trade/brokerage';
  import { fen2yuan } from '../../../sheep/hooks/useGoods';

  const state = reactive({
    loadStatus: '',
    pagination: {
      list: [],
      total: 0,
      pageNo: 1,
      pageSize: 8,
    },
  });

  async function getLog() {
    state.loadStatus = 'loading';
    const { code, data } = await BrokerageApi.getBrokerageRecordPage({
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

  getLog();

  // 加载更多
  function loadmore() {
    if (state.loadStatus === 'noMore') {
      return;
    }
    state.pagination.pageNo++;
    getLog();
  }
</script>

<style lang="scss" scoped>
  .distribution-log-wrap {
    width: 690rpx;
    margin: 0 auto;
    margin-bottom: 20rpx;
    border-radius: 12rpx;
    z-index: 3;
    position: relative;

    .header-box {
      width: 690rpx;
      height: 76rpx;
      position: relative;

      .header-bg {
        width: 690rpx;
        height: 76rpx;
      }

      .header-title {
        position: absolute;
        left: 20rpx;
        top: 24rpx;
      }

      .title {
        font-size: 28rpx;
        font-weight: 500;
        color: #ffffff;
        line-height: 30rpx;
      }

      .cicon-forward {
        font-size: 30rpx;
        font-weight: 400;
        color: #ffffff;
        line-height: 30rpx;
      }
    }

    .log-scroll {
      height: 600rpx;
      background: #fdfae9;
      padding: 10rpx 20rpx 0;
      box-sizing: border-box;
      border-radius: 0 0 12rpx 12rpx;

      .log-item-box {
        margin-bottom: 20rpx;

        .log-time {
          // margin-left: 30rpx;
          text-align: right;
          font-size: 24rpx;
          font-family: OPPOSANS;
          font-weight: 400;
          color: #c4c4c4;
        }
      }

      .loadmore-wrap {
        // line-height: 80rpx;
      }

      .log-item {
        // background: rgba(#ffffff, 0.2);
        border-radius: 24rpx;
        padding: 6rpx 20rpx 6rpx 12rpx;

        .log-img {
          width: 40rpx;
          height: 40rpx;
          border-radius: 50%;
          margin-right: 10rpx;
        }

        .log-text {
          max-width: 480rpx;
          font-size: 24rpx;
          font-weight: 500;
          color: #333333;
        }
      }
    }
  }
</style>
