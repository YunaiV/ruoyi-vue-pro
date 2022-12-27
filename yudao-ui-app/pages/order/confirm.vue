<template>
  <view class="container">
    <view class="confirm-con">
      <!-- TODO 收货地址 -->
      <navigator url="/pages/address/list?type=1">
        <yd-address-select :address="address"></yd-address-select>
      </navigator>

      <!-- TODO 商品信息 -->
      <view class="goods contain">
        <yd-order-product :productList="productList"></yd-order-product>
          <view class="item row-between">
          <view>买家留言</view>
          <u-input v-model="remark" :clearable="false"
            placeholder="请添加备注（150 字以内）"></u-input>
        </view>
      </view>
    </view>

    <!-- TODO 优惠劵 -->

    <!-- TODO 价格信息 -->

    <!-- TODO 底部：提交订单 -->
  </view>
</template>

<script>
export default {
  data() {
    return {
      address: { // 选中的收货地址
        name: 'test',
        mobile: '15601691300',
        area: '奥特曼奥特曼'
      },
      productList: [{ // 购买的商品列表
        coverUrl: '',
        productTitle: '奥特曼',
        sellPrice: 1024,
        productCount: 2048,
        totalPrice: 1024
      }],
      remark: '', // 备注
      x
    }
  }
}
</script>

<style>
  .confirm-con {
    overflow: hidden;
    padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
  }

  .contain {
    border-radius: 14rpx;
    margin: 20rpx 20rpx 0;
    background-color: #fff;
    overflow: hidden;
  }
</style>
