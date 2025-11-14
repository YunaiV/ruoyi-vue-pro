<!-- 我的钱包 -->
<template>
  <s-layout class="wallet-wrap" title="钱包">
    <!-- 钱包卡片 -->
    <view class="header-box ss-flex ss-row-center ss-col-center">
      <view class="card-box ui-BG-Main ui-Shadow-Main">
        <view class="card-head ss-flex ss-col-center">
          <view class="card-title ss-m-r-10">钱包余额（元）</view>
          <view
            @tap="state.showMoney = !state.showMoney"
            class="ss-eye-icon"
            :class="state.showMoney ? 'cicon-eye' : 'cicon-eye-off'"
          />
        </view>
        <view class="ss-flex ss-row-between ss-col-center ss-m-t-64">
          <view class="money-num">{{
            state.showMoney ? fen2yuan(userWallet.balance) : '*****'
          }}</view>
          <button class="ss-reset-button topup-btn" @tap="sheep.$router.go('/pages/pay/recharge')">
            充值
          </button>
        </view>
      </view>
    </view>

    <su-sticky>
      <!-- 统计 -->
      <view class="filter-box ss-p-x-30 ss-flex ss-col-center ss-row-between">
        <uni-datetime-picker
          v-model="state.data"
          type="daterange"
          @change="onChangeTime"
          :end="state.today"
        >
          <button class="ss-reset-button date-btn">
            <text>{{ dateFilterText }}</text>
            <text class="cicon-drop-down ss-seldate-icon"></text>
          </button>
        </uni-datetime-picker>
        <view class="total-box">
          <view class="ss-m-b-10">总收入￥{{ fen2yuan(state.summary.totalIncome) }}</view>
          <view>总支出￥{{ fen2yuan(state.summary.totalExpense) }}</view>
        </view>
      </view>
      <su-tabs
        :list="tabMaps"
        @change="onChange"
        :scrollable="false"
        :current="state.currentTab"
      ></su-tabs>
    </su-sticky>
    <s-empty v-if="state.pagination.total === 0" text="暂无数据" icon="/static/data-empty.png" />

    <!-- 钱包记录 -->
    <view v-if="state.pagination.total > 0">
      <view
        class="wallet-list ss-flex border-bottom"
        v-for="item in state.pagination.list"
        :key="item.id"
      >
        <view class="list-content">
          <view class="title-box ss-flex ss-row-between ss-m-b-20">
            <text class="title ss-line-1">
              {{ item.title }}
            </text>
            <view class="money">
              <text v-if="item.price >= 0" class="add">+{{ fen2yuan(item.price) }}</text>
              <text v-else class="minus">{{ fen2yuan(item.price) }}</text>
            </view>
          </view>
          <text class="time">
            {{ sheep.$helper.timeFormat(state.createTime, 'yyyy-mm-dd hh:MM:ss') }}
          </text>
        </view>
      </view>
    </view>
    <uni-load-more
      v-if="state.pagination.total > 0"
      :status="state.loadStatus"
      :content-text="{
        contentdown: '上拉加载更多',
      }"
    />
  </s-layout>
</template>

<script setup>
  import { computed, reactive } from 'vue';
  import { onLoad, onReachBottom } from '@dcloudio/uni-app';
  import sheep from '@/sheep';
  import dayjs from 'dayjs';
  import _ from 'lodash-es';
  import PayWalletApi from '@/sheep/api/pay/wallet';
  import { fen2yuan } from '@/sheep/hooks/useGoods';
  import { resetPagination } from '@/sheep/helper/utils';

  const headerBg = sheep.$url.css('/static/img/shop/user/wallet_card_bg.png');

  // 数据
  const state = reactive({
    showMoney: false,
    date: [], // 筛选的时间段
    currentTab: 0,
    pagination: {
      list: [],
      total: 0,
      pageNo: 1,
      pageSize: 8,
    },
    summary: {
      totalIncome: 0,
      totalExpense: 0,
    },
    loadStatus: '',
    today: '',
  });

  const tabMaps = [
    {
      name: '全部',
      value: '',
    },
    {
      name: '收入',
      value: '1',
    },
    {
      name: '支出',
      value: '2',
    },
  ];

  const userWallet = computed(() => sheep.$store('user').userWallet);

  // 格式化时间段
  const dateFilterText = computed(() => {
    if (state.date[0] === state.date[1]) {
      return state.date[0];
    } else {
      return state.date.join('~');
    }
  });

  // 获得钱包记录分页
  async function getLogList() {
    state.loadStatus = 'loading';
    const { data, code } = await PayWalletApi.getWalletTransactionPage({
      pageNo: state.pagination.pageNo,
      pageSize: state.pagination.pageSize,
      type: tabMaps[state.currentTab].value,
      'createTime[0]': state.date[0] + ' 00:00:00',
      'createTime[1]': state.date[1] + ' 23:59:59',
    });
    if (code !== 0) {
      return;
    }
    state.pagination.list = _.concat(state.pagination.list, data.list);
    state.pagination.total = data.total;
    state.loadStatus = state.pagination.list.length < state.pagination.total ? 'more' : 'noMore';
  }

  // 获得钱包统计
  async function getSummary() {
    const { data, code } = await PayWalletApi.getWalletTransactionSummary({
      createTime: [state.date[0] + ' 00:00:00', state.date[1] + ' 23:59:59'],
    });
    if (code !== 0) {
      return;
    }
    state.summary = data;
  }

  onLoad(() => {
    state.today = dayjs().format('YYYY-MM-DD');
    state.date = [state.today, state.today];
    getLogList();
    getSummary();
    // 刷新钱包的缓存
    sheep.$store('user').getWallet();
  });

  // 处理 tab 切换
  function onChange(e) {
    state.currentTab = e.index;
    // 重新加载列表
    resetPagination(state.pagination);
    getLogList();
    getSummary();
  }

  // 处理时间筛选
  function onChangeTime(e) {
    state.date[0] = e[0];
    state.date[1] = e[e.length - 1];
    // 重新加载列表
    resetPagination(state.pagination);
    getLogList();
    getSummary();
  }

  onReachBottom(() => {
    if (state.loadStatus === 'noMore') {
      return;
    }
    state.pagination.pageNo++;
    getLogList();
  });
</script>

<style lang="scss" scoped>
  // 钱包
  .header-box {
    background-color: $white;
    padding: 30rpx;

    .card-box {
      width: 100%;
      min-height: 300rpx;
      padding: 40rpx;
      background-size: 100% 100%;
      border-radius: 30rpx;
      overflow: hidden;
      position: relative;
      z-index: 1;
      box-sizing: border-box;

      &::after {
        content: '';
        display: block;
        width: 100%;
        height: 100%;
        z-index: 2;
        position: absolute;
        top: 0;
        left: 0;
        background: v-bind(headerBg) no-repeat;
        pointer-events: none;
      }

      .card-head {
        color: $white;
        font-size: 30rpx;
      }

      .ss-eye-icon {
        font-size: 40rpx;
        color: $white;
      }

      .money-num {
        font-size: 70rpx;
        line-height: 70rpx;
        font-weight: 500;
        color: $white;
        font-family: OPPOSANS;
      }

      .reduce-num {
        font-size: 26rpx;
        font-weight: 400;
        color: $white;
      }

      .topup-btn {
        width: 120rpx;
        height: 60rpx;
        line-height: 60rpx;
        border-radius: 30px;
        font-size: 26rpx;
        font-weight: 500;
        background-color: $white;
        color: var(--ui-BG-Main);
      }
    }
  }

  // 筛选

  .filter-box {
    height: 114rpx;
    background-color: $bg-page;

    .total-box {
      font-size: 24rpx;
      font-weight: 500;
      color: $dark-9;
    }

    .date-btn {
      background-color: $white;
      line-height: 54rpx;
      border-radius: 27rpx;
      padding: 0 20rpx;
      font-size: 24rpx;
      font-weight: 500;
      color: $dark-6;

      .ss-seldate-icon {
        font-size: 50rpx;
        color: $dark-9;
      }
    }
  }

  .tabs-box {
    background: $white;
    border-bottom: 2rpx solid #eeeeee;
  }

  // tab
  .wallet-tab-card {
    .tab-item {
      height: 80rpx;
      position: relative;

      .tab-title {
        font-size: 30rpx;
      }

      .cur-tab-title {
        font-weight: $font-weight-bold;
      }

      .tab-line {
        width: 60rpx;
        height: 6rpx;
        border-radius: 6rpx;
        position: absolute;
        left: 50%;
        transform: translateX(-50%);
        bottom: 2rpx;
        background-color: var(--ui-BG-Main);
      }
    }
  }

  // 钱包记录
  .wallet-list {
    padding: 30rpx;
    background-color: #ffff;

    .head-img {
      width: 70rpx;
      height: 70rpx;
      border-radius: 50%;
      background: $gray-c;
    }

    .list-content {
      justify-content: space-between;
      align-items: flex-start;
      flex: 1;

      .title {
        font-size: 28rpx;
        color: $dark-3;
        width: 400rpx;
      }

      .time {
        color: $gray-c;
        font-size: 22rpx;
      }
    }

    .money {
      font-size: 28rpx;
      font-weight: bold;
      font-family: OPPOSANS;
      .add {
        color: var(--ui-BG-Main);
      }

      .minus {
        color: $dark-3;
      }
    }
  }
</style>
