<!-- 分销商品列表  -->
<template>
  <s-layout title="推广商品" :onShareAppMessage="state.shareInfo">
    <view class="goods-item ss-m-20" v-for="item in state.pagination.list" :key="item.id">
      <s-goods-item
        size="lg"
        :img="item.picUrl"
        :title="item.name"
        :subTitle="item.introduction"
        :price="item.price"
        :originPrice="item.marketPrice"
        priceColor="#333"
        @tap="sheep.$router.go('/pages/goods/index', { id: item.id })"
      >
        <template #rightBottom>
          <view class="ss-flex ss-row-between">
            <view class="commission-num" v-if="item.brokerageMinPrice === undefined">
              预计佣金：计算中
            </view>
            <view
              class="commission-num"
              v-else-if="item.brokerageMinPrice === item.brokerageMaxPrice"
            >
              预计佣金：{{ fen2yuan(item.brokerageMinPrice) }}
            </view>
            <view class="commission-num" v-else>
              预计佣金：{{ fen2yuan(item.brokerageMinPrice) }} ~
              {{ fen2yuan(item.brokerageMaxPrice) }}
            </view>
            <button
              class="ss-reset-button share-btn ui-BG-Main-Gradient"
              @tap.stop="onShareGoods(item)"
            >
              分享赚
            </button>
          </view>
        </template>
      </s-goods-item>
    </view>
    <s-empty
      v-if="state.pagination.total === 0"
      icon="/static/goods-empty.png"
      text="暂无推广商品"
    />
    <!-- 加载更多 -->
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
  import $share from '@/sheep/platform/share';
  import { onLoad, onReachBottom } from '@dcloudio/uni-app';
  import { reactive } from 'vue';
  import _ from 'lodash-es';
  import { showShareModal } from '@/sheep/hooks/useModal';
  import SpuApi from '@/sheep/api/product/spu';
  import BrokerageApi from '@/sheep/api/trade/brokerage';
  import { fen2yuan } from '@/sheep/hooks/useGoods';

  const state = reactive({
    pagination: {
      list: [],
      total: 0,
      pageNo: 1,
      pageSize: 8,
    },
    loadStatus: '',
    shareInfo: {},
  });

  function onShareGoods(goodsInfo) {
    state.shareInfo = $share.getShareInfo(
      {
        title: goodsInfo.name,
        image: sheep.$url.cdn(goodsInfo.picUrl),
        desc: goodsInfo.introduction,
        params: {
          page: '2',
          query: goodsInfo.id,
        },
      },
      {
        type: 'goods', // 商品海报
        title: goodsInfo.name, // 商品名称
        image: sheep.$url.cdn(goodsInfo.picUrl), // 商品主图
        price: fen2yuan(goodsInfo.price), // 商品价格
        original_price: fen2yuan(goodsInfo.marketPrice), // 商品原价
      },
    );
    showShareModal();
  }

  async function getGoodsList() {
    state.loadStatus = 'loading';
    let { code, data } = await SpuApi.getSpuPage({
      pageSize: state.pagination.pageSize,
      pageNo: state.pagination.pageNo,
    });

    if (code !== 0) {
      state.loadStatus = 'error'; // 处理错误状态
      return;
    }

    // 使用 Promise.all 来等待所有佣金请求完成
    await Promise.all(
      data.list.map(async (item) => {
        try {
          const res = await BrokerageApi.getProductBrokeragePrice(item.id);
          item.brokerageMinPrice = res.data.brokerageMinPrice;
          item.brokerageMaxPrice = res.data.brokerageMaxPrice;
        } catch (error) {
          console.error(`获取商品【${item.name}】的佣金时出错：`, error);
        }
      }),
    );

    // 在所有请求完成后合并列表和更新状态
    state.pagination.list = _.concat(state.pagination.list, data.list);
    state.pagination.total = data.total;
    state.loadStatus = state.pagination.list.length < state.pagination.total ? 'more' : 'noMore';
  }

  onLoad(() => {
    getGoodsList();
  });

  // 加载更多
  function loadMore() {
    if (state.loadStatus === 'noMore') {
      return;
    }
    state.pagination.pageNo++;
    getGoodsList();
  }

  // 上拉加载更多
  onReachBottom(() => {
    loadMore();
  });
</script>

<style lang="scss" scoped>
  .goods-item {
    .commission-num {
      font-size: 24rpx;
      font-weight: 500;
      color: $red;
    }

    .share-btn {
      width: 120rpx;
      height: 50rpx;
      border-radius: 25rpx;
    }
  }
</style>
