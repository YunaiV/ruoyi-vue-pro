<template>
  <view class="container">
    <u-sticky style="top: 0" offset-top="0">
      <u-tabs :list="tabArray" :current="tabIndex" itemStyle="padding-left: 18px; padding-right: 18px; height: 36px;" @change="handleStatusChange"></u-tabs>
    </u-sticky>
    <view class="order-list">
      <view v-for="order in orderList" :key="order.no" class="order-item">
        <view class="order-header">
          <view class="order-no">订单编号：{{ order.no }}</view>
          <view class="order-status">{{ order.status | getStatusName }}</view>
        </view>

        <view v-if="order.items.length === 1" class="order-single-item" @click="handleOrderClick(order.id)">
          <view class="item-wrap" v-for="item in order.items" :key="item.id">
            <view class="item-info">
              <image class="item-cover" :src="item.picUrl"></image>
              <u--text :lines="2" size="15px" color="#333333" :text="item.spuName"></u--text>
            </view>
            <view class="item-count">共{{ item.count }}件</view>
          </view>
        </view>
        <view v-else class="order-multi-item" @click="handleOrderClick(order.id)">
          <u-scroll-list :indicator="false">
            <view class="item-wrap" v-for="item in order.items" :key="item.id">
              <image class="item-image" :src="item.picUrl"></image>
            </view>
          </u-scroll-list>
          <view class="product-count">共{{ order.productCount }}件</view>
        </view>

        <view class="order-btn-group">
          <view class="order-btn">再次购买</view>
          <view class="order-btn">其他操作</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { getOrderPage } from '../../api/order'
import orderStatus from '@/common/orderStatus'

export default {
  name: 'orderList',
  filters: {
    getStatusName(status) {
      return orderStatus[status + ''].name
    }
  },
  data() {
    return {
      pageNo: 1,
      tabIndex: 0,
      orderList: []
    }
  },
  computed: {
    tabArray() {
      let tabArray = [{ name: '全部', status: 'all' }]
      for (let status in orderStatus) {
        if (status !== '40') {
          tabArray.push({ name: orderStatus[status].name, status: status })
        }
      }
      return tabArray
    }
  },
  onLoad(e) {
    const status = e.status
    if (status !== undefined) {
      this.tabArray.forEach((item, index) => {
        if (item.status === status) {
          this.tabIndex = index
        }
      })
    }
    this.loadOrderPageData()
  },
  methods: {
    handleStatusChange({ index }) {
      this.tabIndex = index
      this.loadOrderPageData()
    },
    loadOrderPageData() {
      let params = { pageNo: this.pageNo }
      const status = this.tabArray[this.tabIndex].status
      if (status !== 'all') {
        params.orderStatus = status
      }
      getOrderPage(params)
        .then(res => {
          this.orderList = res.data.list || []
        })
        .catch(err => {
          console.log(err)
        })
    },
    handleOrderClick(orderId) {
      uni.$u.route('/pages/order/detail', {
        orderId: orderId
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.order-list {
  background-color: #f3f3f3;

  .order-item {
    padding: 20rpx;
    background-color: #ffffff;
    border-bottom: $custom-border-style;

    .order-header {
      @include flex-space-between;
      height: 80rpx;

      .order-no {
        font-size: 28rpx;
        color: #333;
      }

      .order-status {
        font-size: 24rpx;
        color: #999;
      }
    }

    .order-single-item {
      .item-wrap {
        @include flex-space-between();

        .item-info {
          @include flex-left();

          .item-cover {
            width: 100rpx;
            height: 100rpx;
            border-radius: 10rpx;
            margin-right: 15rpx;
          }
        }

        .item-count {
          color: #999;
          font-size: 24rpx;
          width: 120rpx;
          text-align: right;
        }
      }
    }

    .order-multi-item {
      @include flex-space-between();

      .item-wrap {
        margin-right: 20rpx;

        .item-image {
          width: 100rpx;
          height: 100rpx;
          border-radius: 10rpx;
        }
      }

      .product-count {
        color: #999;
        font-size: 24rpx;
        width: 120rpx;
        text-align: right;
      }
    }

    .order-btn-group {
      margin-top: 10rpx;
      @include flex-right();

      .order-btn {
        width: 120rpx;
        height: 36rpx;
        line-height: 36rpx;
        border-radius: 36rpx;
        border: 1px solid #777;
        color: #777;
        font-size: 22rpx;
        text-align: center;
        margin-left: 15rpx;
      }
    }
  }
}
</style>
