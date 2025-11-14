<!-- 搜索界面 -->
<template>
  <s-layout :bgStyle="{ color: '#FFF' }" class="set-wrap" title="搜索">
    <view class="ss-p-x-24">
      <view class="ss-flex ss-col-center">
        <uni-search-bar
          class="ss-flex-1"
          radius="33"
          placeholder="请输入关键字"
          cancelButton="none"
          :focus="true"
          @confirm="onSearch($event.value)"
        />
      </view>
      <view class="ss-flex ss-row-between ss-col-center">
        <view class="serach-history">搜索历史</view>
        <button class="clean-history ss-reset-button" @tap="onDelete"> 清除搜索历史 </button>
      </view>
      <view class="ss-flex ss-col-center ss-row-left ss-flex-wrap">
        <button
          class="history-btn ss-reset-button"
          @tap="onSearch(item)"
          v-for="(item, index) in state.historyList"
          :key="index"
        >
          {{ item }}
        </button>
      </view>
    </view>
  </s-layout>
</template>

<script setup>
  import { reactive } from 'vue';
  import sheep from '@/sheep';
  import { onLoad } from '@dcloudio/uni-app';

  const state = reactive({
    historyList: [],
  });

  // 搜索
  function onSearch(keyword) {
    if (!keyword) {
      return;
    }
    saveSearchHistory(keyword);
    // 前往商品列表（带搜索条件）
    sheep.$router.go('/pages/goods/list', { keyword });
  }

  // 保存搜索历史
  function saveSearchHistory(keyword) {
    // 如果关键词在搜索历史中，则把此关键词先移除
    if (state.historyList.includes(keyword)) {
      state.historyList.splice(state.historyList.indexOf(keyword), 1);
    }
    // 置顶关键词
    state.historyList.unshift(keyword);

    // 最多保留 10 条记录
    if (state.historyList.length >= 10) {
      state.historyList.length = 10;
    }
    uni.setStorageSync('searchHistory', state.historyList);
  }

  function onDelete() {
    uni.showModal({
      title: '提示',
      content: '确认清除搜索历史吗？',
      success: function (res) {
        if (res.confirm) {
          state.historyTag = [];
          uni.removeStorageSync('searchHistory');
        }
      },
    });
  }

  onLoad(() => {
    state.historyList = uni.getStorageSync('searchHistory') || [];
  });
</script>

<style lang="scss" scoped>
  .serach-title {
    font-size: 30rpx;
    font-weight: 500;
    color: #333333;
  }

  .uni-searchbar {
    padding-left: 0;
  }

  .serach-history {
    font-weight: bold;
    color: #333333;
    font-size: 30rpx;
  }

  .clean-history {
    font-weight: 500;
    color: #999999;
    font-size: 28rpx;
  }

  .history-btn {
    padding: 0 38rpx;
    height: 60rpx;
    background: #f5f6f8;
    border-radius: 30rpx;
    font-size: 28rpx;
    color: #333333;
    max-width: 690rpx;
    margin: 0 20rpx 20rpx 0;
  }
</style>
