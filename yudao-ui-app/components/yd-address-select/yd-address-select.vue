<template>
  <view class="address-box">
    <view v-if="!address.area" class="address-empty">
      <view class="address-tips">
        <u--text :lines="1" size="14px" color="#333" bold="true" text="请选择收货地址"></u--text>
        <u-icon class="icon-arrow-right" name="arrow-right"></u-icon>
      </view>
    </view>

    <view v-else class="address-selected">
      <view class="address-area">
        <view class="default-tag">默认</view>
        <u--text :lines="1" size="12px" color="#666" :text="address.area"></u--text>
      </view>
      <view class="address-detail">
        <u--text :lines="2" size="14px" color="#333" :bold="true" :text="address.detail"></u--text>
        <u-icon class="icon-arrow-right" name="arrow-right"></u-icon>
      </view>
      <view class="address-user">
        <view class="user-text">{{ address.name }}</view>
        <view class="user-text">{{ address.mobile }}</view>
      </view>
    </view>
    <image class="address-line" src="/static/images/address-line.png"></image>
  </view>
</template>

<script>
/**
 * 地址选择
 */
export default {
  name: 'yd-address-select',
  props: {
    address: {
      type: Object,
      default: () => []
    }
  },
  data() {
    return {}
  },
  methods: {}
}
</script>

<style lang="scss" scoped>
.address-box {
  padding: 0 10rpx;
  background-color: #fff;
  border-radius: 0 0 30rpx 30rpx;

  .address-empty {
    padding: 20rpx 20rpx 0;
    .address-tips {
      @include flex-space-between;
      padding: 10rpx 0;

      .icon-arrow-right {
        margin-left: 50rpx;
      }
    }
  }

  .address-selected {
    padding: 20rpx 20rpx 0;
    .address-area {
      @include flex-left;

      .default-tag {
        font-size: 22rpx;
        color: $u-primary;
        border: 1rpx solid $u-primary;
        padding: 0 6rpx;
        margin-right: 10rpx;
        border-radius: 5rpx;
      }
    }

    .address-detail {
      @include flex-space-between;
      padding: 10rpx 0;

      .icon-arrow-right {
        margin-left: 50rpx;
      }
    }

    .address-user {
      @include flex-left;

      .user-text {
        color: #666;
        font-size: 24rpx;
        margin-right: 10rpx;
      }
    }
  }

  .address-line {
    width: 100%;
    margin: 0 auto;
    height: 5rpx;
  }
}
</style>
