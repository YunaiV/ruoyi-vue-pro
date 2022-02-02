<template>
	<view class="app">
		<!-- 个人信息 -->
		<view class="user-wrapper">
			<image class="user-background" src="/static/backgroud/user.jpg"></image>
			<view class="user">
				<!-- 头像 -->
				<image class="avatar" :src="userInfo.avatar || '/static/icon/default-avatar.png'" @click="navTo('/pages/set/userInfo', {login: true})"></image>
				<!-- 已登陆，展示昵称 -->
				<view class="cen column" v-if="hasLogin">
					<text class="username f-m">{{ userInfo.nickname }}</text>
					<text class="group">普通会员</text>
				</view>
				<!-- 未登陆，引导登陆 -->
				<view class="login-box" v-else @click="navTo('/pages/auth/login')">
					<text>点击注册/登录</text>
				</view>
			</view>
			<!-- 下面的圆弧 -->
			<image class="user-background-arc-line" src="/static/icon/arc.png" mode="aspectFill"></image>
		</view>

		<!-- 订单信息 -->
		<view class="order-wrap">
			<view class="order-header row" @click="navTo('/pages/order/list?current=0', {login: true})">
				<text class="title">我的订单</text>
				<text class="more">查看全部</text>
				<text class="mix-icon icon-you"></text>
			</view>
			<view class="order-list">
				<view class="item center" @click="navTo('/pages/order/list?current=1', {login: true})"  hover-class="hover-gray" :hover-stay-time="50">
					<text class="mix-icon icon-daifukuan"></text>
					<text>待付款</text>
					<text v-if="orderCount.c0 > 0" class="number">{{ orderCount.c0 }}</text>
				</view>
				<view class="item center" @click="navTo('/pages/order/list?current=2', {login: true})" hover-class="hover-gray" :hover-stay-time="50">
					<text class="mix-icon icon-daifahuo"></text>
					<text>待发货</text>
					<text v-if="orderCount.c1 > 0" class="number">{{ orderCount.c1 }}</text>
				</view>
				<view class="item center" @click="navTo('/pages/order/list?current=3', {login: true})" hover-class="hover-gray" :hover-stay-time="50">
					<text class="mix-icon icon-yishouhuo"></text>
					<text>待收货</text>
					<text v-if="orderCount.c2 > 0" class="number">{{ orderCount.c2 }}</text>
				</view>
				<view class="item center" @click="navTo('/pages/order/list?current=4', {login: true})" hover-class="hover-gray" :hover-stay-time="50">
					<text class="mix-icon icon-daipingjia"></text>
					<text>待评价</text>
					<text v-if="orderCount.c3 > 0" class="number">{{ orderCount.c3 }}</text>
				</view>
			</view>
		</view>

		<!-- 功能入口 -->
		<u-cell-group class="option1-wrap">
			<u-cell icon="edit-pen" title="个人信息" isLink @click="navTo('/pages/set/userInfo', {login: true})"></u-cell>
			<u-cell icon="setting" title="账号安全" isLink @click="navTo('/pages/set/set', {login: true})"></u-cell>
		</u-cell-group>
	</view>
</template>

<script>
	import { mapState, mapGetters } from 'vuex'
	import { isLogin } from '@/common/js/util.js'
	
	export default {
		data() {
			return {
				orderCount: { // TODO 芋艿：读取
					c0: 1,
					c1: 2,
					c2: 3,
					c3: 4
				}
			};
		},
		computed: {
			...mapState(['userInfo']),
			...mapGetters(['hasLogin']),
		},
		onShow() {
			// 获得用户信息 TODO 芋艿：
			// if (isLogin()) {
			// 	this.$store.dispatch('obtainUserInfo');
			// }
			// TODO 芋艿：获得订单数量
		}
	}
</script>

<style lang="scss">
	.app {
		padding-bottom: 20rpx;
	}
	.user-wrapper {
		position: relative;
		overflow: hidden;
		padding-top: calc(var(--status-bar-height) + 52rpx);
		padding-bottom: 6rpx;
		.user {
			display: flex;
			flex-direction: column;
			flex-direction: row;
			align-items: center;
			position: relative;
			z-index: 5;
			padding: 20rpx 30rpx 60rpx;
			.avatar {
				flex-shrink: 0;
				width: 130rpx;
				height: 130rpx;
				border-radius: 100px;
				margin-right: 24rpx;
				border: 4rpx solid #fff;
				background-color: #fff;
			}
			.username {
				font-size: 34rpx;
				color: #fff;
			}
			.group {
				align-self: flex-start;
				padding: 10rpx 14rpx;
				margin: 16rpx 10rpx; // 10rpx 避免距离昵称太近
				font-size: 20rpx;
				color: #fff;
				background-color: rgba(255, 255, 255,.3);
				border-radius: 100rpx;
			}
			.login-box {
				font-size: 36rpx;
				color: #fff;
			}
		}
		.user-background {
			position: absolute;
			left: 0;
			top: 0;
			width: 100%;
			height: 330rpx;
		}
		.user-background-arc-line {
			position: absolute;
			left: 0;
			bottom: 0;
			z-index: 9;
			width: 100%;
			height: 32rpx;
		}
	}

	.order-wrap {
		width: 700rpx;
		margin: 20rpx auto 0;
		background: #fff;
		border-radius: 10rpx;
		.order-header {
			padding: 28rpx 20rpx 6rpx 26rpx;
			.title {
				flex: 1;
				font-size: 32rpx;
				color: #333;
				font-weight: 700;
			}
			.more {
				font-size: 24rpx;
				color: #999;
			}
			.icon-you {
				margin-left: 4rpx;
				font-size: 20rpx;
				color: #999;
			}
		}
		.order-list {
			display:flex;
			justify-content: space-around;
			padding: 20rpx 0;
			.item{
				flex-direction: column;
				width: 130rpx;
				height: 130rpx;
				border-radius: 8rpx;
				font-size: 24rpx;
				color: #606266;
				position: relative;
				.mix-icon {
					font-size: 50rpx;
					margin-bottom: 20rpx;
					color: #fa436a;
				}
				.icon-shouhoutuikuan {
					font-size: 44rpx;
				}
				.number {
					position: absolute;
					right: 22rpx;
					top: 6rpx;
					min-width: 34rpx;
					height: 34rpx;
					line-height: 30rpx;
					text-align: center;
					padding: 0 8rpx;
					font-size: 18rpx;
					color: #fff;
					border: 2rpx solid #fff;
					background-color: $base-color;
					border-radius: 100rpx;
				}
			}
		}
	}
</style>
