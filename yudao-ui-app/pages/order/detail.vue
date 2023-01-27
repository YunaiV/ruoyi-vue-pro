<template>
  <view class="container">
    <viwe class="detail-header">
      <view class="order-status">{{ order.status | getStatusName }}</view>
    </viwe>

    <view class="order-product">
      <view class="order-item" v-for="item in order.items" :key="item.id">
        <image class="product-image" :src="item.picUrl"></image>
        <view class="item-info">
          <view class="info-text">
            <u--text :lines="1" size="15px" color="#333333" :text="item.spuName"></u--text>
            <u-gap height="10"></u-gap>
            <yd-text-price class="product-price" size="13" intSize="14" :price="item.originalUnitPrice"></yd-text-price>
          </view>
          <view class="price-number-box">
            <view class="number-box">
              <view class="product-number">共 {{ item.count }} 件</view>
              小计：
            </view>
            <view class="number-box" @click.stop>
              <yd-text-price size="13" intSize="16" :price="item.originalPrice"></yd-text-price>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view v-if="order.no" >
      <view v-if="order.no" class="base-info">
        <view class="info-item">
          <view class="item-name">订单编号：</view>
          <view class="item-value">{{ order.no }}</view>
        </view>
        <view class="info-item">
          <view class="item-name">下单时间：</view>
          <view class="item-value">{{ order.createTime }}</view>
        </view>
        <view v-if="order.payOrderId" class="info-item">
          <view class="item-name">支付方式：</view>
          <view class="item-value">{{ order.payOrderId }}</view>
        </view>
        <view v-if="order.payTime" class="info-item">
          <view class="item-name">支付时间：</view>
          <view class="item-value">{{ order.payTime }}</view>
        </view>
      </view>

      <view class="delivery-info">
        <view class="info-item">
          <view class="item-name">收货地址：</view>
          <view class="item-value">{{ order.receiverAreaName + order.receiverDetailAddress  + order.receiverDetailAddress  + order.receiverDetailAddress }}</view>
        </view>
        <view v-if="order.receiverName" class="info-item">
          <view class="item-name">收货人：</view>
          <view class="item-value">{{ order.receiverName }}</view>
        </view>
        <view v-if="order.receiverMobile" class="info-item">
          <view class="item-name">联系电话：</view>
          <view class="item-value">{{ order.receiverMobile }}</view>
        </view>
      </view>

      <view class="delivery-info">
        <view class="info-item">
          <view class="item-name">商品总额：</view>
          <yd-text-price class="product-price" size="13" intSize="16" :price="order.originalPrice"></yd-text-price>
        </view>
        <view class="info-item">
          <view class="item-name">运费：</view>
          <yd-text-price class="product-price" size="13" intSize="16" :price="order.deliveryPrice"></yd-text-price>
        </view>
        <view class="info-item">
          <view class="item-name">优惠：</view>
          <yd-text-price class="product-price" size="13" intSize="16" symbol="-￥" :price="order.discountPrice"></yd-text-price>
        </view>
        <view class="info-item">
          <view class="item-name">订单金额：</view>
          <yd-text-price class="product-price" size="15" intSize="20" :price="order.orderPrice"></yd-text-price>
        </view>
      </view>
    </view>

  </view>
</template>

<script>
import { getOrderDetail } from '../../api/order'
import orderStatus from '@/common/orderStatus'

export default {
  name: 'orderDetail',
  filters: {
    getStatusName(status) {
      return orderStatus[status + ''].name
    }
  },
  data() {
    return {
      orderId: undefined,
      order: {}
    }
  },
  onLoad(e) {
    this.orderId = e.orderId
    if (!this.orderId) {
      uni.$u.toast('请求参数错误')
    } else {
      this.loadOrderDetailData()
    }
  },
  methods: {
    loadOrderDetailData() {
      getOrderDetail({ id: this.orderId })
        .then(res => {
          this.order = res.data || {}
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
  background-color: #f3f3f3;
  height: 100vh;
}

.detail-header {
  @include flex-center();
  background-color: $custom-bg-color;
  padding: 10rpx 0;
  border-radius: 0 0 20rpx 20rpx;
}

.order-product {
  background-color: $custom-bg-color;
  border-radius: 20rpx;
  margin: 20rpx;
  padding: 10rpx 20rpx;
  .order-item {
    background: #ffffff;
    @include flex-space-between;
    padding: 10rpx 0 10rpx 5rpx;

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
}

.base-info {
  background-color: $custom-bg-color;
  border-radius: 20rpx;
  margin: 20rpx;
  padding: 20rpx;
  .info-item {
    @include flex-left();
    padding: 10rpx 0;
    .item-name {
      color: #999;
      font-size: 26rpx;
    }
    .item-value {
      color: #333;
      font-size: 26rpx;
    }
  }
}

.delivery-info {
  background-color: $custom-bg-color;
  border-radius: 20rpx;
  margin: 20rpx;
  padding: 20rpx;
  .info-item {
    @include flex-left();
    padding: 10rpx 0;
    .item-name {
      color: #999;
      font-size: 26rpx;
      width: 260rpx;
    }
    .item-value {
      color: #333;
      font-size: 26rpx;
    }
  }
}

.delivery-info {
  background-color: $custom-bg-color;
  border-radius: 20rpx;
  margin: 20rpx;
  padding: 20rpx;
  .info-item {
    @include flex-space-between();
    padding: 10rpx 0;
    .item-name {
      color: #999;
      font-size: 26rpx;
    }
  }
}

</style>
