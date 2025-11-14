<!-- 佣金排行榜 -->
<template>
  <s-layout title="佣金排行榜">
    <view class="commission-rank">
      <view class="header">
        <view class="rank" v-if="state.position">
          您目前的排名
          <text class="num">{{ state.position }}</text> 名
        </view>
        <view class="rank" v-else>您目前暂无排名</view>
      </view>
      <view class="wrapper">
        <view class="nav acea-row row-around" style="justify-content: space-around; display: flex">
          <view
            class="item"
            :class="state.currentTab === index ? 'font-color' : ''"
            v-for="(item, index) in tabMaps"
            :key="index"
            @click="switchTap(index)"
          >
            {{ item }}
          </view>
        </view>
        <view class="list">
          <view class="item" v-for="(item, index) in state.pagination.list" :key="index">
            <view class="num" v-if="index <= 2">
              <image :src="'/static/images/medal0' + (index + 1) + '.png'" />
            </view>
            <view class="num" v-else>
              {{ index + 1 }}
            </view>
            <view class="pictrue">
              <image :src="item.avatar" />
            </view>
            <view class="text">{{ item.nickname }}</view>
            <view class="people">￥{{ fen2yuan(item.brokeragePrice) }}</view>
          </view>
        </view>
        <view class="noCommodity" v-if="state.pagination.list.length === 0">
          <emptyPage title="暂无排行～" />
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
  import BrokerageApi from '@/sheep/api/trade/brokerage';
  import { fen2yuan } from '@/sheep/hooks/useGoods';
  import _ from 'lodash-es';
  import { resetPagination, getWeekTimes, getMonthTimes } from '@/sheep/helper/utils';

  const tabMaps = ['周排行', '月排行'];

  const state = reactive({
    currentTab: 0,
    position: 0, // 排名
    pagination: {
      list: [],
      total: 0,
      pageNo: 1,
      pageSize: 10,
    },
    loadStatus: '',
  });

  async function switchTap(index) {
    state.currentTab = index;
    resetPagination(state.pagination);
    calculateTimes();
    getBrokerageRankList();
  }

  async function getBrokerageRankList() {
    const { code, data } = await BrokerageApi.getBrokerageUserChildSummaryPageByPrice({
      pageNo: state.pagination.pageNo,
      pageSize: state.pagination.pageSize,
      'times[0]': state.times[0],
      'times[1]': state.times[1],
    });
    if (code !== 0) {
      return;
    }
    state.pagination.list = _.concat(state.pagination.list, data.list);
    state.pagination.total = data.total;
    state.loadStatus = state.pagination.list.length < state.pagination.total ? 'more' : 'noMore';
    if (state.pagination.pageNo === 1) {
      getBrokerageRankNumber();
    }
  }

  async function getBrokerageRankNumber() {
    const { code, data } = await BrokerageApi.getRankByPrice({
      times: state.times,
    });
    if (code !== 0) {
      return;
    }
    state.position = data;
  }

  function formatDate(date) {
    return sheep.$helper.timeFormat(date, 'yyyy-mm-dd hh:MM:ss');
  }

  function calculateTimes() {
    let times;
    if (state.currentTab === 0) {
      times = getWeekTimes();
    } else {
      times = getMonthTimes();
    }
    state.times = [formatDate(times[0]), formatDate(times[1])];
  }

  onLoad(function () {
    calculateTimes();
    getBrokerageRankList();
  });

  // 加载更多
  function loadMore() {
    if (state.loadStatus === 'noMore') {
      return;
    }
    state.pagination.pageNo++;
    getBrokerageRankList();
  }

  // 上拉加载更多
  onReachBottom(() => loadMore());
</script>
<style lang="scss" scoped>
  .commission-rank {
    .header {
      background: url('data:image/jpeg;base64,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')
        no-repeat;
      width: 100%;
      height: 344rpx;
      background-size: 100% 100%;

      .rank {
        font-size: 33rpx;
        color: #fff;
        position: absolute;
        top: 160rpx;
        left: 48rpx;

        .num {
          font-size: 51rpx;
          font-weight: bold;
          margin: 0 10rpx;
        }
      }
    }

    .wrapper {
      width: 710rpx;
      background-color: #fff;
      border-radius: 14rpx;
      margin: -76rpx auto 0 auto;

      .nav {
        height: 99rpx;
        border-bottom: 2.5rpx solid #f3f3f3;
        font-size: 30rpx;
        font-weight: bold;
        color: #999;
        line-height: 99rpx;

        .item.font-color {
          border-bottom: 4rpx solid #e93323;
          margin-top: -2rpx;
          color: #e93323;
        }
      }

      .list {
        padding: 0 20rpx;

        .item {
          display: flex;
          justify-content: space-around;
          align-items: center;
          border-bottom: 1px solid #f3f3f3;
          height: 101rpx;
          font-size: 28rpx;

          .num {
            color: #666;
            flex: 0.08;
            display: flex;
            justify-content: flex-start;
            align-items: center;

            image {
              width: 50rpx;
              height: 50rpx;
            }
          }

          .pictrue {
            flex: 0.15;

            image {
              width: 68rpx;
              height: 68rpx;
              display: block;
              border-radius: 50%;
              margin: 0 auto;
            }
          }

          .text {
            color: #333;
            flex: 0.5;
          }

          .people {
            text-align: right;
            flex: 0.27;
          }
        }
      }
    }
  }
</style>
