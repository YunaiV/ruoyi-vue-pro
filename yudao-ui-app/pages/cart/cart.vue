<template>
  <view class="container">
    <!-- 购物车为空 -->
    <view v-if="!hasLogin || cartList.length === 0">
      <view class="cart-empty">
        <u-empty text="去逛逛添点什么吧" width="500rpx" height="500rpx" icon="/static/images/empty/cart.png"></u-empty>
      </view>
    </view>

    <!-- 购物车列表 -->
    <scroll-view v-if="hasLogin && cartList.length > 0" class="cart-product" scroll-y="true">
      <yd-cart-product :product-list="cartList" @productCheckedChange="handleProductCheckedChange" @productCountChange="handleProductCountChange"></yd-cart-product>
    </scroll-view>

    <!-- 未登录 -->
    <view v-if="!hasLogin" class="login-tips-box">
      <view class="login-tips">
        <navigator url="/pages/login/social" open-type="navigate" hover-class="none">
          <view class="login-link">登录查看</view>
        </navigator>
      </view>
    </view>

    <!-- 底部菜单 -->
    <view class="cart-btn-container">
      <view class="btn-box">
        <view class="product-check-info">
          <view class="check-all-btn" @click.stop="handleCheckAllProduct">
            <u-icon v-if="isCheckAll" name="checkmark-circle-fill" color="#3c9cff" size="22"></u-icon>
            <view v-else class="un-check-box"></view>
          </view>
          <view class="info-text">合计：</view>
          <view>
            <yd-text-price color="red" size="15" intSize="20" :price="totalAmount"></yd-text-price>
          </view>
        </view>

        <view v-if="checkedProduct.length > 0" class="cart-btn-group">
          <u-button type="warning" shape="circle" size="small" text="移除" @click="handleRemoveProduct"></u-button>
          <view class="btn-gap"></view>
          <u-button style="margin-left: 10px" class="main-btn" type="primary" shape="circle" size="small" text="去结算" @click="handleCheckoutProduct"></u-button>
        </view>
        <view v-else class="cart-btn-group">
          <u-button type="warning" shape="circle" size="small" text="移除" disabled></u-button>
          <view class="btn-gap"></view>
          <u-button style="margin-left: 10px" class="main-btn" type="primary" shape="circle" size="small" text="去结算" disabled></u-button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  data() {
    return {
      title: '',
      cartList: [],
      checkedNumber: 0,
      totalAmount: 0
    }
  },
  computed: {
    checkedProduct() {
      return this.cartList.filter(item => {
        return item.checked
      })
    },
    isCheckAll() {
      if (this.cartList.length < 1) {
        return false
      }
      return this.cartList.every(item => {
        return item.checked
      })
    },
    hasLogin() {
      return this.$store.getters.hasLogin
    }
  },
  onShow() {
    if (this.hasLogin) {
      this.loadCartDetailData()
    } else {
      this.cartList =[]
    }
  },
  methods: {
    loadCartDetailData() {
      this.$store.dispatch('CartProductDetail').then(res => {
        this.cartList = res.data || []
      })
    },
    /** 商品全选/取消全选 */
    handleCheckAllProduct() {
      if (this.cartList.length < 1) {
        return
      }
      const productIds = this.cartList.map(item => {
        return item.productId
      })
      this.$store.dispatch('CartProductCheckChange', { productIds, checked: !this.isCheckAll }).then(res => {
        this.cartList = res.data || []
      })
    },
    /** 商品单选/取消单选 */
    handleProductCheckedChange(productId, checked) {
      this.$store.dispatch('CartProductCheckChange', { productIds: [productId], checked: checked }).then(res => {
        this.cartList = res.data || []
      })
    },
    /** 修改购物车商品数量 */
    handleProductCountChange(productId, number) {
      this.$store.dispatch('CartProductCountChange', { productIds: [productId], productCount: number }).then(res => {
        this.cartList = res.data || []
      })
    },
    /** 移除购物车商品 */
    handleRemoveProduct() {
      if (this.checkedProduct < 1) {
        return
      }
      const productIds = this.checkedProduct.map(item => {
        return item.productId
      })

      uni.showModal({
        title: '确定要移除选中的商品？',
        cancelText: '取消',
        confirmText: '移除',
        success: res => {
          if (res.confirm) {
            this.$store.dispatch('CartProductCountChange', { productIds: productIds, productCount: 0 }).then(res => {
              this.cartList = res.data || []
            })
          } else if (res.cancel) {
            //console.log('用户点击取消')
          }
        }
      })
    },
    /** 购物车提交结算 */
    handleCheckoutProduct() {
      if (this.checkedProduct < 1) {
        return
      }
      const checkedProduct = this.checkedProduct.map(item => {
        return { productId: item.productId, productCount: item.productCount, sellPrice: item.sellPrice }
      })
      uni.$u.route('/pages/checkout/checkout', {
        checkedProduct: JSON.stringify(checkedProduct)
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.login-tips-box {
  padding-top: 100rpx;

  .login-tips {
    @include flex-center;
    color: #939393;
    font-size: 24rpx;
    letter-spacing: 5rpx;
  }

  .login-link {
    width: 160rpx;
    height: 50rpx;
    line-height: 50rpx;
    border-radius: 50rpx;
    border: 1px solid #777;
    color: #777;
    text-align: center;
  }
}

.cart-btn-container {
  position: fixed;
  bottom: 0;
  left: 0;

  .btn-box {
    background: $custom-bg-color;
    border-top: $custom-border-style;

    width: 750rpx;
    @include flex-space-between();
    height: 100rpx;

    .product-check-info {
      @include flex-left;

      .check-all-btn {
        padding: 20rpx;

        .un-check-box {
          width: 20px;
          height: 20px;
          border: 1px solid #939393;
          border-radius: 50%;
        }
      }

      .info-text {
        font-size: 26rpx;
        font-weight: bold;
        color: #666666;
      }
    }

    .cart-btn-group {
      @include flex-right();
      width: 360rpx;
      padding-right: 10px;

      .btn-gap {
        width: 20rpx;
      }
    }
  }
}
</style>
