<template>
  <su-popup :show="show" showClose round="10" backgroundColor="#eee" @close="emits('close')">
    <view class="select-popup">
      <view class="title">
        <span>{{ mode == 'goods' ? '我的浏览' : '我的订单' }}</span>
      </view>
      <scroll-view
        class="scroll-box"
        scroll-y="true"
        :scroll-with-animation="true"
        :show-scrollbar="false"
        @scrolltolower="loadmore"
      >
        <view
          class="item"
          v-for="item in state.pagination.data"
          :key="item.id"
          @tap="emits('select', { type: mode, data: item })"
        >
          <template v-if="mode == 'goods'">
            <GoodsItem :goodsData="item" />
          </template>
          <template v-if="mode == 'order'">
            <OrderItem :orderData="item" />
          </template>
        </view>
        <uni-load-more :status="state.loadStatus" :content-text="{ contentdown: '上拉加载更多' }" />
      </scroll-view>
    </view>
  </su-popup>
</template>

<script setup>
  import { reactive, watch } from 'vue';
  import _ from 'lodash-es';
  import GoodsItem from './goods.vue';
  import OrderItem from './order.vue';
  import OrderApi from '@/sheep/api/trade/order';
  import SpuHistoryApi from '@/sheep/api/product/history';

  const emits = defineEmits(['select', 'close']);
  const props = defineProps({
    mode: {
      type: String,
      default: 'goods',
    },
    show: {
      type: Boolean,
      default: false,
    },
  });

  watch(
    () => props.mode,
    () => {
      state.pagination.data = [];
      if (props.mode) {
        getList(state.pagination.page);
      }
    },
  );

  const state = reactive({
    loadStatus: '',
    pagination: {
      data: [],
      current_page: 1,
      total: 1,
      last_page: 1,
    },
  });

  async function getList(page, list_rows = 5) {
    state.loadStatus = 'loading';
    const res =
      props.mode == 'goods'
        ? await SpuHistoryApi.getBrowseHistoryPage({
            page,
            list_rows,
          })
        : await OrderApi.getOrderPage({
            page,
            list_rows,
          });
    let orderList = _.concat(state.pagination.data, res.data.list);
    state.pagination = {
      ...res.data,
      data: orderList,
    };
    if (state.pagination.current_page < state.pagination.last_page) {
      state.loadStatus = 'more';
    } else {
      state.loadStatus = 'noMore';
    }
  }

  function loadmore() {
    if (state.loadStatus !== 'noMore') {
      getList(state.pagination.current_page + 1);
    }
  }
</script>

<style lang="scss" scoped>
  .select-popup {
    max-height: 600rpx;

    .title {
      height: 100rpx;
      line-height: 100rpx;
      padding: 0 26rpx;
      background: #fff;
      border-radius: 20rpx 20rpx 0 0;

      span {
        font-size: 32rpx;
        position: relative;

        &::after {
          content: '';
          display: block;
          width: 100%;
          height: 2px;
          z-index: 1;
          position: absolute;
          left: 0;
          bottom: -15px;
          background: var(--ui-BG-Main);
          pointer-events: none;
        }
      }
    }

    .scroll-box {
      height: 500rpx;
    }

    .item {
      background: #fff;
      margin: 26rpx 26rpx 0;
      border-radius: 20rpx;

      :deep() {
        .image {
          width: 140rpx;
          height: 140rpx;
        }
      }
    }
  }
</style>
