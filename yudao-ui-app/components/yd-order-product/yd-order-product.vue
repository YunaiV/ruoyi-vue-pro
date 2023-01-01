<template>
  <view>
    <view class="product-item" v-for="(item, index) in productList" :key="item.productId">
      <image class="product-image" :src="item.coverUrl"></image>
      <view class="item-info">
        <view class="info-text">
          <u--text :lines="1" size="15px" color="#333333" :text="item.productTitle"></u--text>
          <u-gap height="10"></u-gap>
          <yd-text-price class="product-price" size="13" intSize="16" :price="item.sellPrice"></yd-text-price>
        </view>
        <view class="price-number-box">
          <view class="number-box">
            <view class="product-number">共 {{ item.productCount }} 件</view> 小计：
          </view>
          <view class="number-box" @click.stop>
            <yd-text-price size="13" intSize="18" :price="item.totalPrice"></yd-text-price>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
/**
 * 订单商品列表
 */
export default {
  name: 'yd-order-product',
  props: {
    productList: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {}
  },
  methods: {
    handleProductItemClick(productId) {
      uni.$u.route('/pages/product/product', {
        id: productId
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.product-item {
  background: #ffffff;
  @include flex-space-between;
  border-bottom: $custom-border-style;
  padding: 10rpx 0 0 5rpx;

  .product-check {
    padding: 20rpx;

    .un-check-box {
      width: 20px;
      height: 20px;
      border: 1px solid #939393;
      border-radius: 50%;
    }
  }

  .product-image {
    width: 180rpx;
    height: 180rpx;
    border-radius: 10rpx;
  }

  .item-info {
    flex: 1;
    padding: 0 20rpx;

    .info-text {
      padding-bottom: 10rpx;
      .product-price {
        margin-top: 15rpx;
      }
    }

    .price-number-box {
      @include flex-space-between;

      .number-box {
        font-size: 24rpx;
        .product-number {
          width: 200rpx;
        }
      }

      .number-box {
        height: 60rpx;
        @include flex-center;
      }
    }
  }
}
</style>
