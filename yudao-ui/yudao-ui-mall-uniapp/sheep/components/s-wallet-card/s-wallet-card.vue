<!-- 装修用户组件：用户资产 -->
<template>
	<view class="ss-wallet-menu-wrap ss-flex ss-col-center" :style="[bgStyle, { marginLeft: `${data.space}px` }]">
		<view class="menu-item ss-flex-1 ss-flex-col ss-row-center ss-col-center"
			@tap="sheep.$router.go('/pages/user/wallet/money')">
			<view class="value-box ss-flex ss-col-bottom">
				<view class="value-text ss-line-1">{{ fen2yuan(userWallet.balance) || '0.00' }}</view>
				<view class="unit-text ss-m-l-6">元</view>
			</view>
			<view class="menu-title ss-m-t-28">账户余额</view>
		</view>
		<view class="menu-item ss-flex-1 ss-flex-col ss-row-center ss-col-center"
			@tap="sheep.$router.go('/pages/user/wallet/score')">
			<view class="value-box ss-flex ss-col-bottom">
				<view class="value-text">{{ userInfo.point || 0 }}</view>
				<view class="unit-text ss-m-l-6">个</view>
			</view>
			<view class="menu-title ss-m-t-28">积分</view>
		</view>
		<view class="menu-item ss-flex-1 ss-flex-col ss-row-center ss-col-center" @tap="
        sheep.$router.go('/pages/coupon/list', {
          type: 'geted',
        })
      ">
			<view class="value-box ss-flex ss-col-bottom">
				<view class="value-text">{{ numData.unusedCouponCount }}</view>
				<view class="unit-text ss-m-l-6">张</view>
			</view>
			<view class="menu-title ss-m-t-28">优惠券</view>
		</view>
		<view class="menu-item ss-flex-col ss-row-center ss-col-center menu-wallet"
          @tap="sheep.$router.go('/pages/user/wallet/money')">
			<image class="item-icon" :src="sheep.$url.static('/static/img/shop/user/wallet_icon.png')" mode="aspectFit" />
			<view class="menu-title ss-m-t-30">我的钱包</view>
		</view>
	</view>
</template>

<script setup>
	/**
	 * 装修组件 - 订单菜单组
	 */
	import { computed } from 'vue';
	import sheep from '@/sheep';
	import { fen2yuan } from '../../hooks/useGoods';

	// 接收参数
	const props = defineProps({
		// 装修数据
		data: {
		  type: Object,
		  default: () => ({}),
		},
		// 装修样式
		styles: {
		  type: Object,
		  default: () => ({}),
		},
	});
	// 设置背景样式
	const bgStyle = computed(() => {
	  // 直接从 props.styles 解构
	  const { bgType, bgImg, bgColor } = props.styles; 
	
	  // 根据 bgType 返回相应的样式
	  return {
		background: bgType === 'img'
			? `url(${bgImg}) no-repeat top center / 100% 100%`
			: bgColor
		};
	});
	
	const userWallet = computed(() => sheep.$store('user').userWallet);
	const userInfo = computed(() => sheep.$store('user').userInfo);
	const numData = computed(() => sheep.$store('user').numData);
</script>

<style lang="scss" scoped>
	.ss-wallet-menu-wrap {
		.menu-wallet {
			width: 144rpx;
		}

		.menu-item {
			height: 160rpx;

			.menu-title {
				font-size: 24rpx;
				line-height: 24rpx;
				color: #333333;
			}

			.item-icon {
				width: 44rpx;
				height: 44rpx;
			}

			.value-box {
				height: 50rpx;
				text-align: center;

				.value-text {
					font-size: 28rpx;
					color: #000000;
					line-height: 28rpx;
					vertical-align: text-bottom;
					font-family: OPPOSANS;
				}

				.unit-text {
					font-size: 24rpx;
					color: #343434;
					line-height: 24rpx;
				}
			}
		}
	}
</style>

