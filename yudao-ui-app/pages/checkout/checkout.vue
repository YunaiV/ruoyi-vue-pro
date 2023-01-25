<template>
  <view class="container">
    <!-- 收货地址选择 -->
    <yd-address-select :address="address"></yd-address-select>

    <!-- 订单商品信息 -->
    <view class="checkout-goods">
      <yd-order-goods :goods-list="checkoutList"></yd-order-goods>
    </view>

    <view class="freight-coupon-box">
      <vidw class="box-row">
        <view class="title">运费</view>
        <yd-text-price class="freight-fee" size="15" :price="freightAmount"></yd-text-price>
      </vidw>
      <vidw class="box-row">
        <view class="coupon-wrap">
          <view class="coupon-title-tag">
            <view class="title">优惠券</view>
            <scroll-view class="coupon-tag-list" scroll-x="true">
              <view v-for="item in couponList" :key="item.couponId" class="coupon-tag-item">{{ item.couponTag }}</view>
            </scroll-view>
          </view>
          <yd-text-price class="coupon-fee" color="red" size="15" symbol="-￥" :price="couponAmount"></yd-text-price>
        </view>
        <u-icon name="arrow-right"></u-icon>
      </vidw>
    </view>

    <!-- 订单备注信息 -->
    <view class="user-remark-box">
      <view class="title">订单备注</view>
      <u--input maxlength="50" border="none" fontSize="14" v-model="remark" placeholder="如您需要请备注"></u--input>
    </view>

    <view class="cart-btn-container">
      <view class="order-total-wrap">
        <view class="order-total-info">
          <view class="info-text">合计：</view>
          <view>
            <yd-text-price color="red" size="15" intSize="20" :price="totalAmount"></yd-text-price>
          </view>
        </view>

        <view class="cart-btn-group">
          <u-button style="margin-left: 10px" class="main-btn" type="primary" shape="circle" size="small" text="提交订单" @click="handleSubmitOrder"></u-button>
        </view>
      </view>
      <u-safe-bottom customStyle="background: #ffffff"></u-safe-bottom>
    </view>
  </view>
</template>

<script>
import { checkoutCartProduct } from '../../api/order'

export default {
  components: {},
  data() {
    return {
      checkedProduct: [],
      address: {
        name: '客户',
        mobile: '139****6563',
        area: 'XXX市XXX区',
        detail: 'XXX街道XXX小区XXX号楼XX-XXX'
      },
      checkoutList: [],
      couponList: [
        {
          couponId: 3,
          couponTag: '6元运费券'
        }
      ],
      totalAmount: 0,
      freightAmount: 6,
      couponAmount: 6,
      remark: ''
    }
  },
  onLoad(e) {
    const checkedProduct = e.checkedProduct
    if (checkedProduct) {
      this.checkedProduct = JSON.parse(checkedProduct)
      this.loadCheckoutProductData()
    } else {
      uni.$u.toast('请求参数错误')
    }
  },
  methods: {
    loadCheckoutProductData() {
      checkoutCartProduct(this.checkedProduct)
        .then(res => {
          this.checkoutList = res.data.checkoutList || []
          this.totalAmount = res.data.totalAmount || 0
        })
        .catch(err => {
          console.log(err)
        })
    }
  }
}
</script>

<style lang="scss" scoped>
.container {
  background-color: $custom-bg-color;
  height: 100vh;
  overflow-x: scroll;
}

.checkout-goods {
  background-color: #fff;
  margin-top: 20rpx;
  padding: 20rpx;
  border-radius: 20rpx;
}

.freight-coupon-box {
  background-color: #fff;
  margin-top: 20rpx;
  padding: 20rpx 30rpx;
  border-radius: 20rpx;

  .box-row {
    @include flex-space-between;
    padding: 10rpx 0;

    .coupon-wrap {
      @include flex-space-between;
      width: 670rpx;

      .coupon-title-tag {
        @include flex-left;

        .coupon-tag-list {
          @include flex-left;
          overflow-x: scroll;
          width: 360rpx;
          .coupon-tag-item {
            display: inline-block;
            font-size: 22rpx;
            color: red;
            border: 1rpx solid red;
            padding: 1px 6rpx;
            margin-right: 10rpx;
            border-radius: 5rpx;
          }
        }
      }
    }

    .title {
      font-weight: 700;
      font-size: 30rpx;
      color: #333;
      margin-right: 30rpx;
    }

    .freight-fee {
      margin-right: 50rpx;
    }

    .coupon-fee {
      margin-right: 20rpx;
    }
  }
}

.user-remark-box {
  @include flex-space-between;
  background-color: #fff;
  margin-top: 20rpx;
  padding: 30rpx;
  border-radius: 20rpx;

  .title {
    font-weight: 700;
    font-size: 30rpx;
    color: #333;
    margin-right: 30rpx;
  }
}

.cart-btn-container {
  position: fixed;
  bottom: 0;
  left: 0;

  .order-total-wrap {
    background: $custom-bg-color;
    border-top: $custom-border-style;

    width: 750rpx;
    @include flex-space-between();
    height: 100rpx;

    .order-total-info {
      @include flex-left;
      .info-text {
        margin-left: 20rpx;
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
