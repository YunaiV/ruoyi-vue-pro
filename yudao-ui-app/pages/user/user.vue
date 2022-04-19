<template>
  <view class="container">
    <view class="user-header">
      <view class="user-info" @click="loginOrJump('/pages/profile/profile')">
        <u-avatar size="80" :src="avatar"></u-avatar>
        <text class="nick-name">{{ nickName }}</text>
      </view>
    </view>

    <u-gap height="10" bgColor="#f3f3f3"></u-gap>

    <view>
      <view class="order-header">
        <text class="order-title">我的订单</text>
        <view class="see-all">
          <text>查看全部</text>
          <u-icon name="arrow-right"></u-icon>
        </view>
      </view>

      <view class="order-status-box">
        <u-grid :border="false" :col="orderStatusList.length"
          ><u-grid-item v-for="(item, index) in orderStatusList" :key="index">
            <u-icon :name="item.icon" :size="32"></u-icon>
            <text class="grid-title">{{ item.title }}</text>
          </u-grid-item>
        </u-grid>
      </view>
    </view>

    <u-gap height="10" bgColor="#f3f3f3"></u-gap>

    <view class="stat-box">
      <u-grid :border="false" col="3"
        ><u-grid-item v-for="(item, index) in statList" :key="index">
          <text class="grid-value">{{ item.value }}</text>
          <text class="grid-title">{{ item.title }}</text>
        </u-grid-item>
      </u-grid>
    </view>

    <u-gap height="10" bgColor="#f3f3f3"></u-gap>

    <u-cell-group class="fun-list">
      <u-cell class="fun-item" :border="false" icon="gift" title="分销中心" isLink></u-cell>
      <u-cell class="fun-item" :border="false" icon="tags" title="领券中心" isLink></u-cell>
      <u-cell class="fun-item" :border="false" icon="coupon" title="我的优惠券" isLink></u-cell>
      <u-cell class="fun-item" :border="false" icon="map" title="收货地址"  @click="loginOrJump('/pages/address/list')" isLink></u-cell>
    </u-cell-group>

    <view class="logout-btn">
      <u-button type="error" color="#ea322b" text="确定"></u-button>
    </view>

  </view>
</template>

<script>
export default {
  data() {
    return {
      avatar: '',
      nickName: '点击登录',
      orderStatusList: [
        { icon: 'rmb-circle', title: '待支付' },
        { icon: 'car', title: '代发货' },
        { icon: 'order', title: '待收货' },
        { icon: 'integral', title: '已完成' }
      ],
      statList: [
        { value: '0', title: '我的收藏' },
        { value: '0', title: '我的消息' },
        { value: '0', title: '我的足迹' }
      ]
    }
  },
  onLoad() {},
  methods: {
    loginOrJump(pageUrl){
      // TODO 判断是否已经登录逻辑
      if (!uni.getStorageSync('token')) {
        uni.$u.route('/pages/login/login')
      } else {
        uni.$u.route(pageUrl)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.user-header {
  @include flex-center(column);
  height: 280rpx;
  .user-info {
    @include flex-center(column);
    .nick-name {
      margin-top: 20rpx;
      font-size: 32rpx;
      font-weight: 700;
    }
  }
}

.order-header {
  @include flex-space-between;
  padding: 20rpx 30rpx;
  border-bottom: $custom-border-style;

  .order-title {
    color: #333333;
    font-size: 34rpx;
  }
  .see-all {
    height: 40rpx;
    @include flex-right;
    color: #666666;
    font-size: 26rpx;
  }
}

.order-status-box {
  padding: 40rpx 0;
}

.stat-box {
  padding: 20rpx 0;
}

.grid-title {
  line-height: 50rpx;
  font-size: 26rpx;
}

.grid-value {
  line-height: 50rpx;
  font-size: 36rpx;
  font-weight: 700;
  color: #2b85e4;
}

.fun-list {
  .fun-item {
    padding-top: 10rpx;
    padding-bottom: 10rpx;
    border-bottom: $custom-border-style;
  }
}

.logout-btn {
  margin: 60rpx auto 0;
  width: 400rpx;
}
</style>
