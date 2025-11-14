<!-- 商品浏览记录  -->
<template>
  <s-layout :bgStyle="{ color: '#f2f2f2' }" title="我的足迹">
    <view class="cart-box ss-flex ss-flex-col ss-row-between">
      <!-- 头部 -->
      <view class="cart-header ss-flex ss-col-center ss-row-between ss-p-x-30">
        <view class="header-left ss-flex ss-col-center ss-font-26">
          共
          <text class="goods-number ui-TC-Main ss-flex">
            {{ state.pagination.total }}
          </text>
          件商品
        </view>
        <view class="header-right">
          <button
            v-if="state.editMode && state.pagination.total"
            class="ss-reset-button"
            @tap="state.editMode = false"
          >
            取消
          </button>
          <button
            v-if="!state.editMode && state.pagination.total"
            class="ss-reset-button ui-TC-Main"
            @tap="state.editMode = true"
          >
            编辑
          </button>
        </view>
      </view>
      <!-- 内容 -->
      <view class="cart-content">
        <view
          class="goods-box ss-r-10 ss-m-b-14"
          v-for="item in state.pagination.list"
          :key="item.id"
        >
          <view class="ss-flex ss-col-center">
            <label
              class="check-box ss-flex ss-col-center ss-p-l-10"
              v-if="state.editMode"
              @tap="onSelect(item.spuId)"
            >
              <radio
                :checked="state.selectedSpuIdList.includes(item.spuId)"
                color="var(--ui-BG-Main)"
                style="transform: scale(0.8)"
                @tap.stop="onSelect(item.spuId)"
              />
            </label>
            <s-goods-item
              :title="item.spuName"
              :img="item.picUrl"
              :price="item.price"
              :skuText="item.introduction"
              priceColor="#FF3000"
              :titleWidth="400"
              @tap="
                sheep.$router.go('/pages/goods/index', {
                  id: item.spuId,
                })
              "
            >
            </s-goods-item>
          </view>
        </view>
      </view>
      <!-- 底部 -->
      <su-fixed bottom :val="0" placeholder v-show="state.editMode">
        <view class="cart-footer ss-flex ss-col-center ss-row-between ss-p-x-30 border-bottom">
          <view class="footer-left ss-flex ss-col-center">
            <label class="check-box ss-flex ss-col-center ss-p-r-30" @tap="onSelectAll">
              <radio
                :checked="state.selectAll"
                color="var(--ui-BG-Main)"
                style="transform: scale(0.7)"
                @tap.stop="onSelectAll"
              />
              <view>全选</view>
            </label>
          </view>
          <view class="footer-right ss-flex">
            <button
              :class="[
                'ss-reset-button  pay-btn ss-font-28 ',
                {
                  'ui-BG-Main-Gradient': state.selectedSpuIdList.length > 0,
                  'ui-Shadow-Main': state.selectedSpuIdList.length > 0,
                },
              ]"
              @tap="onDelete"
            >
              删除足迹
            </button>
            <button
              class="ss-reset-button ui-BG-Main-Gradient pay-btn ss-font-28 ui-Shadow-Main ml-2"
              @tap="onClean"
            >
              清空
            </button>
          </view>
        </view>
      </su-fixed>
    </view>
    <uni-load-more
      v-if="state.pagination.total > 0"
      :status="state.loadStatus"
      :content-text="{
        contentdown: '上拉加载更多',
      }"
      @tap="loadMore"
    />
    <s-empty
      v-if="state.pagination.total === 0"
      text="暂无浏览记录"
      icon="/static/collect-empty.png"
    />
  </s-layout>
</template>

<script setup>
  import sheep from '@/sheep';
  import { reactive } from 'vue';
  import { onLoad, onReachBottom } from '@dcloudio/uni-app';
  import _ from 'lodash-es';
  import SpuHistoryApi from '@/sheep/api/product/history';
  import { cloneDeep } from '@/sheep/helper/utils';

  const sys_navBar = sheep.$platform.navbar;
  const pagination = {
    list: [],
    pageNo: 1,
    total: 1,
    pageSize: 10,
  };
  const state = reactive({
    pagination: cloneDeep(pagination),
    loadStatus: '',
    editMode: false,
    selectedSpuIdList: [],
    selectAll: false,
  });

  async function getList() {
    state.loadStatus = 'loading';
    const { code, data } = await SpuHistoryApi.getBrowseHistoryPage({
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

  // 单选选中
  const onSelect = (id) => {
    if (!state.selectedSpuIdList.includes(id)) {
      state.selectedSpuIdList.push(id);
    } else {
      state.selectedSpuIdList.splice(state.selectedSpuIdList.indexOf(id), 1);
    }
    state.selectAll = state.selectedSpuIdList.length === state.pagination.list.length;
  };

  // 全选
  const onSelectAll = () => {
    state.selectAll = !state.selectAll;
    if (!state.selectAll) {
      state.selectedSpuIdList = [];
    } else {
      state.pagination.list.forEach((item) => {
        if (state.selectedSpuIdList.includes(item.spuId)) {
          state.selectedSpuIdList.splice(state.selectedSpuIdList.indexOf(item.spuId), 1);
        }
        state.selectedSpuIdList.push(item.spuId);
      });
    }
  };

  // 删除足迹
  async function onDelete() {
    if (state.selectedSpuIdList.length <= 0) {
      return;
    }

    const { code } = await SpuHistoryApi.deleteBrowseHistory(state.selectedSpuIdList);
    if (code === 0) {
      reload();
    }
  }

  // 清空
  async function onClean() {
    const { code } = await SpuHistoryApi.cleanBrowseHistory();
    if (code === 0) {
      reload();
    }
  }

  function reload() {
    state.editMode = false;
    state.selectedSpuIdList = [];
    state.selectAll = false;
    state.pagination = pagination;
    getList();
  }

  // 加载更多
  function loadMore() {
    if (state.loadStatus !== 'noMore') {
      state.pagination.pageNo += 1;
      getList();
    }
  }

  onReachBottom(() => {
    loadMore();
  });

  onLoad(() => {
    getList();
  });
</script>

<style lang="scss" scoped>
  .cart-box {
    .cart-header {
      height: 70rpx;
      background-color: #f6f6f6;
      width: 100%;
      position: fixed;
      left: 0;
      top: v-bind('sys_navBar') rpx;
      z-index: 1000;
      box-sizing: border-box;
    }

    .cart-footer {
      height: 100rpx;
      background-color: #fff;

      .pay-btn {
        height: 80rpx;
        line-height: 80rpx;
        border-radius: 40rpx;
        padding: 0 40rpx;
        min-width: 200rpx;
      }
    }

    .cart-content {
      width: 100%;
      padding: 0 20rpx;
      box-sizing: border-box;
      margin-top: 70rpx;
      .goods-box {
        background-color: #fff;
        &:last-child {
          margin-bottom: 40rpx;
        }
      }
    }
  }

  .title-card {
    padding: 36rpx 0 46rpx 20rpx;

    .img-box {
      width: 164rpx;
      height: 164rpx;

      .order-img {
        width: 164rpx;
        height: 164rpx;
      }
    }

    .check-box {
      height: 100%;
    }

    .title-text {
      font-size: 28rpx;
      font-weight: 500;
      color: #333333;
    }

    .params-box {
      .params-title {
        height: 38rpx;
        background: #f4f4f4;
        border-radius: 2rpx;
        font-size: 24rpx;
        font-weight: 400;
        color: #666666;
      }
    }

    .price-text {
      color: $red;
      font-family: OPPOSANS;
    }
  }
</style>
