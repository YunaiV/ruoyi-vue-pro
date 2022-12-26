<template>
  <view>
    <view class="prod-block list">
      <view class="bloc-header">
        <text class="bloc-title">更多宝贝</text>
        <text></text>
      </view>

      <view class="prod-list" v-for="(item, index) in productList" :key="item.id">
        <view class="prod-item" @click="handleProdItemClick(item.id)">
          <image class="prod-image" :src="item.image"></image>
          <view class="item-info">
            <view class="info-text">
              <u--text :lines="1" size="14px" color="#333333" :text="item.title"></u--text>
              <u-gap height="2px"></u-gap>
              <u--text class="info-desc" :lines="2" size="12px" color="#939393" :text="item.desc"></u--text>
            </view>
            <view class="price-and-cart">
              <yd-text-price color="red" size="12" intSize="18" :price="item.price"></yd-text-price>
              <u-icon name="shopping-cart" color="#2979ff" size="28"></u-icon>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!--加载更多-->
    <u-loadmore fontSize="28rpx" :line="true" :status="moreStatus" :loading-text="loadingText" :loadmore-text="loadmoreText" :nomore-text="nomoreText" />

  </view>
</template>

<script>
/**
 * 商品列表(加载更多)
 */
export default {
  name: 'yd-product-more',
  components: {},
  props: {
    title: {
      type: String,
      default: '商品推荐'
    },
    productList: {
      type: Array,
      default: () => []
    },
    moreStatus: {
      type: String,
      default: 'nomore'
    }
  },
  data() {
    return {
      //status: 'nomore',
      loadingText: '加载中...',
      loadmoreText: '上拉加载更多',
      nomoreText: '已经到底了'
    }
  },
  methods: {
    handleProdItemClick(productId) {
      uni.$u.route('/pages/product/product', {
        id: productId
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.prod-block {
  margin-top: 0;

  .bloc-header {
    margin-top: 50rpx;
    margin-bottom: 20rpx;

    .bloc-title {
      margin-left: 20rpx;
      color: #333333;
    }
  }

  .prod-list {
    .prod-item {
      background: #ffffff;
      @include flex-space-between;
      border-bottom: $custom-border-style;
      padding: 20rpx;

      .prod-image {
        width: 200rpx;
        height: 200rpx;
        border-radius: 10rpx;
      }

      .item-info {
        flex: 1;
        padding: 20rpx 20rpx 0;

        .info-text {
          height: 100rpx;
          padding-bottom: 10rpx;
        }

        .price-and-cart {
          @include flex-space-between;
        }
      }
    }
  }
}
</style>
