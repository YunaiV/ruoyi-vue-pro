<template>
  <view class="container">
    <view class="user-header">
      <view class="user-info" @click="pageRouter('/pages/profile/profile')">
        <u-avatar size="60" shape="square" :src="userInfo.avatar"></u-avatar>
        <view class="info-text">
          <view class="user-nickname">{{ hasLogin ? userInfo.nickname || '会员用户' : '匿名用户' }}</view>
          <view class="user-mobile">{{ hasLogin ? userInfo.mobile || ' ' : '登录/注册' }}</view>
        </view>
      </view>
      <view class="user-setting">
        <u-icon v-if="hasLogin" name="setting" color="#939393" size="22" @click="pageRouter('/pages/setting/setting')"></u-icon>
      </view>
    </view>

    <u-gap height="10" bgColor="#f3f3f3"></u-gap>

    <view>
      <view class="order-header">
        <text class="order-title">我的订单</text>
        <view class="see-all" @click="pageRouter(orderPage, -1)">
          <text>查看全部</text>
          <u-icon name="arrow-right"></u-icon>
        </view>
      </view>

      <view class="order-status-box">
        <u-grid :border="false" :col="orderStatusList.length">
          <u-grid-item v-for="(item, index) in orderStatusList" :key="index" @click="pageRouter(orderPage, item.status)">
            <u-icon :name="item.icon" :size="32"></u-icon>
            <text class="grid-title">{{ item.name }}</text>
          </u-grid-item>
        </u-grid>
      </view>
    </view>

    <u-gap height="10" bgColor="#f3f3f3"></u-gap>

    <view class="stat-box">
      <u-grid :border="false" col="3">
        <u-grid-item v-for="(item, index) in statList" :key="index">
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
      <u-cell class="fun-item" :border="false" icon="map" title="收货地址" @click="pageRouter('/pages/address/list')" isLink></u-cell>
    </u-cell-group>
  </view>
</template>

<script>
import orderStatus from '@/common/orderStatus'

export default {
  data() {
    return {
      orderPage: '/pages/order/list',
      statList: [
        { value: '0', title: '我的收藏' },
        { value: '0', title: '我的消息' },
        { value: '0', title: '我的足迹' }
      ]
    }
  },
  onLoad() {
    if (this.hasLogin) {
      this.$store.dispatch('ObtainUserInfo')
    }
  },
  computed: {
    userInfo() {
      return this.$store.getters.userInfo
    },
    hasLogin() {
      return this.$store.getters.hasLogin
    },
    orderStatusList() {
      let orderStatusList = []
      for (let status in orderStatus) {
        if (status !== '40') {
          orderStatusList.push({ name: orderStatus[status].name, status: status, icon: orderStatus[status].icon })
        }
      }
      return orderStatusList
    }
  },
  methods: {
    pageRouter(pageUrl, param) {
      if (!this.hasLogin) {
        uni.$u.route('/pages/login/social')
      } else if (pageUrl === this.orderPage) {
        uni.$u.route(this.orderPage, {
          status: param
        })
      } else {
        uni.$u.route(pageUrl)
      }
    },
    logout() {
      uni.showModal({
        title: '提示',
        content: '您确定要退出登录吗',
        success: res => {
          if (res.confirm) {
            this.$store.dispatch('Logout')
          } else if (res.cancel) {
            //console.log('用户点击取消')
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.user-header {
  background-color: #fff;
  @include flex-space-between;
  padding: 30rpx;
  height: 200rpx;

  .user-info {
    @include flex-left;
    align-items: center;

    .info-text {
      margin-left: 20rpx;

      .user-nickname {
        font-size: 30rpx;
        font-weight: 700;
        line-height: 50rpx;
      }

      .user-mobile {
        font-size: 24rpx;
        font-weight: 700;
        color: #939393;
        line-height: 50rpx;
      }
    }
  }

  .user-setting {
    margin-right: 5rpx;
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
</style>
