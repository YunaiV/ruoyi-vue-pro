<template>
  <view class="container">
    <u-sticky style="top: 0" offset-top="0">
      <u-tabs :list="statusArray" :current="statusIndex" @change="handleStatusChange"></u-tabs>
    </u-sticky>
    <view class="order-list">
      <view v-for="(item, index) in orderList" :key="item.orderNo" class="order-item">
        <view class="item-title">{{ item.orderNo }}</view>
        <view class="item-content">{{ item.orderStatus }}</view>
        <view class="item-btn-group"></view>
      </view>
    </view>
  </view>
</template>

<script>
import { getOrderPageData } from '../../api/order'

export default {
  name: 'order',
  data() {
    return {
      pageNo: 1,
      statusIndex: 0,
      statusArray: [
        {
          name: '全部',
          status: '-1'
        },
        {
          name: '待付款',
          status: '0'
        },
        {
          name: '已付款',
          status: '10'
        },
        {
          name: '待发货',
          status: '20'
        },
        {
          name: '待收货',
          status: '30'
        },
        {
          name: '已完成',
          status: '40'
        },
        {
          name: '已取消',
          status: '50'
        }
      ],
      orderList: []
    }
  },
  onLoad(e) {
    const status = e.status
    if (status !== undefined) {
      this.statusArray.forEach((item, index) => {
        if (item.status === status) {
          this.statusIndex = index
        }
      })
    }
    this.loadOrderPageData()
  },
  methods: {
    handleStatusChange(item) {
      this.statusIndex = item.status
    },
    loadOrderPageData() {
      let params = { pageNo: this.pageNo }
      const status = this.statusArray[this.statusIndex].status
      if (status >= 0) {
        params.orderStatus = status
      }
      getOrderPageData(params)
        .then(res => {
          console.log(res)
        })
        .catch(err => {
          console.log(err)
        })
    }
  }
}
</script>

<style lang="scss" scoped></style>
