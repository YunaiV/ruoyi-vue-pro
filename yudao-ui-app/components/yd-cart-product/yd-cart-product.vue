<template>
  <view>
    <view class="product-item" v-for="(item, index) in productList" :key="item.productId" @click="handleProductItemClick(item.productId)">
      <view class="product-check" @click.stop="handleCheckProduct(item.productId, item.checked)">
        <u-icon v-if="item.checked" name="checkmark-circle-fill" color="#3c9cff" size="22"></u-icon>
        <view v-else class="un-check-box"></view>
      </view>
      <image class="product-image" :src="item.coverUrl"></image>
      <view class="item-info">
        <view class="info-text">
          <u--text :lines="1" size="15px" color="#333333" :text="item.productTitle"></u--text>
          <u-gap height="2px"></u-gap>
          <u--text class="info-tips" :lines="1" size="12px" color="#939393" :text="item.tips"></u--text>
        </view>
        <view class="price-number-box">
          <view class="price-box">
            <yd-text-price color="red" size="13" intSize="20" :price="item.sellPrice"></yd-text-price>
            <yd-text-price v-if="item.strikePrice" style="margin-left: 5px" decoration="line-through" color="#999" size="12" :price="item.sellPrice"></yd-text-price>
          </view>
          <view class="number-box" @click.stop>
            <u-number-box min="1" max="999" bgColor="#fff" integer :disableMinus="false" :disabledInput="true" :name="item.productId" :value="item.productCount" @change="handleProductCountChange"></u-number-box>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
/**
 * 购物车商品列表
 */
export default {
  name: 'yd-cart-product',
  props: {
    productList: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      //status: 'nomore',
      loadingText: '加载中...',
      loadmoreText: '上拉加载更多',
      nomoreText: '没有更多了'
    }
  },
  methods: {
    handleProductItemClick(productId) {
      uni.$u.route('/pages/product/product', {
        id: productId
      })
    },
    handleItemCartClick(productId) {
      this.$store.dispatch('CartProductCountChange', { productIds: [productId], productCount: 1, addition: true }).then(res => {
        uni.$u.toast('已添加到购物车')
      })
    },
    handleCheckProduct(productId, checked) {
      this.$emit('productCheckedChange', productId, !checked)
    },
    handleProductCountChange({ name, value }) {
      this.$emit('productCartProductCountChange', name, value)
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
    padding: 20rpx 20rpx 0;

    .info-text {
      height: 70rpx;
      padding-bottom: 10rpx;
    }

    .price-number-box {
      @include flex-space-between;

      .price-box {
        @include flex-left();
      }

      .number-box {
        height: 100rpx;
        @include flex-center;
      }
    }
  }
}
</style>
