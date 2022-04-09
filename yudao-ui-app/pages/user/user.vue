<template>
	<view class="container">
    <view class="user-header">
      <view class="user-info" @click="handleUserInfoClick">
        <u-avatar size="80" :src="avatar"></u-avatar>
        <text class="nick-name">{{nickName}}</text>
      </view>
    </view>

    <u-gap height="10" bgColor="#f6f6f6"></u-gap>

    <view>
      <view class="order-header">
        <text class="order-title">我的订单</text>
        <view class="see-all">
          <text>查看全部</text>
          <u-icon name="arrow-right"></u-icon>
        </view>
      </view>

      <view class="mt-40-r mb-40-r">
        <u-grid :border="false" :col="orderStatusList.length"><u-grid-item v-for="(item,index) in orderStatusList" :key="index">
          <u-icon :name="item.icon" :size="32"></u-icon>
          <text class="grid-title">{{item.title}}</text>
        </u-grid-item>
        </u-grid>
      </view>
    </view>

    <u-gap height="10" bgColor="#f6f6f6"></u-gap>
    <view class="mt-20-r mb-20-r">
      <u-grid :border="false" col="3"><u-grid-item v-for="(item,index) in statisticsList" :key="index">
        <text class="grid-value">{{item.value}}</text>
        <text class="grid-title">{{item.title}}</text>
      </u-grid-item>
      </u-grid>
    </view>

    <u-gap height="10" bgColor="#f6f6f6"></u-gap>

    <u-cell-group class="fun-list">
      <u-cell class="fun-item" :border="false" icon="gift" title="分销中心" isLink></u-cell>
      <u-cell class="fun-item" :border="false" icon="tags" title="领券中心" isLink></u-cell>
      <u-cell class="fun-item" :border="false" icon="coupon" title="我的优惠券" isLink></u-cell>
      <u-cell class="fun-item" :border="false" icon="map" title="收获地址" isLink></u-cell>
    </u-cell-group>

    <u-button class="logout-btn" type="error" color="#ea322b" text="确定"></u-button>

	</view>
</template>

<script>
	export default {
		data() {
			return {
        avatar:'',
        nickName:'点击登录',
        orderStatusList: [{icon: 'rmb-circle', title: '待支付'}, {icon: 'car', title: '代发货'}, {icon: 'order', title: '待收货'}, {icon: 'integral', title: '已完成'}],
        statisticsList: [{value: '2', title: '我的收藏'}, {value: '3', title: '我的消息'}, {value: '3', title: '我的足迹'}]
			}
		},
		onLoad() {

		},
		methods: {
      handleUserInfoClick(){
        if (!uni.getStorageSync('token')) {
          uni.$u.route('/pages/login/login');
        } else {
          uni.$u.route('/pages/profile/profile');
        }
      }
		}
	}
</script>

<style lang="scss" scoped>
.user-header {
  height: 280rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  .user-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    .nick-name {
      margin-top: 20rpx;
      font-size: 32rpx;
      font-weight: 700;
    }
  }
}

.order-header {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #f3f3f3;

  .order-title {
    color: #333333;
    font-size: 34rpx;
  }
  .see-all {
    height: 40rpx;
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: right;
    color: #666666;
    font-size: 26rpx;
  }
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
    border-bottom: 1rpx solid #f3f3f3;
  }
}

.logout-btn {
  margin-top: 60rpx;
  width: 300rpx;
}

</style>
