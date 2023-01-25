<template>
  <view>
    <view v-if="showType === 'normal'">
      <u-gap height="180" bgColor="#398ade"></u-gap>
      <view class="prod-block">
        <view class="bloc-header">
          <text class="bloc-title">{{title}}</text>
          <text class="see-more">查看更多</text>
        </view>
        <view class="prod-grid">
          <view class="prod-item" v-for="(item, index) in productList" :key="item.id" @click="handleProdItemClick(item.id)">
            <image class="prod-image" :src="item.image"></image>
            <view class="item-info">
              <view class="info-text">
                <u--text :lines="2" size="14px" color="#333333" :text="item.title"></u--text>
              </view>
              <view class="price-and-cart">
                <yd-text-price color="red" size="12" intSize="18" :price="item.price"></yd-text-price>
                <u-icon name="shopping-cart" color="#2979ff" size="28"></u-icon>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view v-if="showType === 'half'">
      <view class="prod-block half">
        <view class="bloc-header">
          <text class="bloc-title">{{title}}</text>
          <text class="more">更多 &gt;</text>
        </view>
        <view class="prod-grid half">
          <view class="prod-item" v-for="(item, index) in productList" :key="item.id" @click="handleProdItemClick(item.id)">
            <image class="prod-image" :src="item.image"></image>
            <view class="item-info">
              <view class="info-text">
                <u--text :lines="1" size="14px" color="#333333" :text="item.title"></u--text>
                <u--text :lines="1" size="12px" color="#939393" :text="item.desc"></u--text>
              </view>
              <view class="price-and-cart">
                <yd-text-price color="red" size="12" intSize="18" :price="item.price"></yd-text-price>
                <u-icon name="shopping-cart" color="#2979ff" size="28"></u-icon>
              </view>
            </view>
          </view>
        </view>
      </view>
    </view>

  </view>
</template>

<script>
/**
 * 商品列表
 */
export default {
  name: 'yd-product-box',
  components: {},
  props: {
    showType: {
      type: String,
      default: 'normal'
    },
    title: {
      type: String,
      default: '商品推荐'
    },
    productList: {
      type: Array,
      default: () => []
    }
  },
  computed: {},
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
  margin-top: -160px;

  .bloc-header {
    @include flex-space-between;
    padding: 10rpx 20rpx;

    .bloc-title {
      color: $custom-bg-color;
      font-size: 34rpx;
    }

    .see-more {
      color: $custom-bg-color;
      background: $u-primary;
      padding: 0 30rpx;
      height: 50rpx;
      line-height: 50rpx;
      border-radius: 50rpx;
      font-size: 24rpx;
    }
  }

  &.half {
    margin-top: 0;

    .bloc-header {
      margin-top: 50rpx;
      margin-bottom: 20rpx;

      .bloc-title {
        color: #333333;
      }

      .more {
        font-size: 24rpx;
      }
    }
  }

  .prod-grid {
    width: 730rpx;
    margin: 0 auto;
    @include flex;
    flex-wrap: wrap;
    justify-content: left;

    &.half {
      .prod-item {
        width: 345rpx;
        margin: 10rpx;

        .prod-image {
          width: 345rpx;
          height: 345rpx;
        }
      }
    }

    .prod-item {
      width: 223rpx;
      margin: 10rpx;
      background: #ffffff;
      border-radius: 10rpx;
      box-shadow: 0rpx 6rpx 8rpx rgba(58, 134, 185, 0.2);

      .prod-image {
        width: 223rpx;
        height: 223rpx;
        border-radius: 10rpx 10rpx 0 0;
      }

      .item-info {
        padding: 15rpx;

        .info-text {
          height: 70rpx;
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
