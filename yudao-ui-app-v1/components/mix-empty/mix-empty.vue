<template>
	<view class="mix-empty" :style="{backgroundColor: backgroundColor}">
		<view v-if="type==='cart'" class="cart column center">
			<image class="icon" src="/static/empty/cart.png"></image>
			<text class="title">{{ hasLogin ? '空空如也' : '先去登录嘛' }}</text>
			<text class="text">别忘了买点什么犒赏一下自己哦</text>
			<view class="btn center" @click="onCartBtnClick">
				<text>{{ hasLogin ? '随便逛逛' : '去登录' }}</text>
			</view>
		</view>
		<view v-else-if="type==='address'" class="address column center">
			<image class="icon" src="/static/empty/address.png"></image>
			<text class="text">找不到您的收货地址哦，先去添加一个吧~</text>
			<view class="btn center" @click="navTo('manage')">
				<text class="mix-icon icon-jia2"></text>
			</view>
		</view>
		<view v-else-if="type==='favorite'" class="favorite column center">
			<image class="icon" src="/static/empty/favorite.png"></image>
			<text class="text">收藏夹空空的，先去逛逛吧~</text>
			<view class="btn center" @click="switchTab('/pages/tabbar/home')">
				<text>随便逛逛</text>
			</view>
		</view>
		<view v-else class="default column center">
			<image class="icon" src="/static/empty/default.png"></image>
			<text class="text">{{ text }}</text>
		</view>
	</view>
</template>

<script>
	/**
	 * 缺省显示
	 * @prop text 缺省文字提示
	 * @prop type 缺省类型
	 * @prop backgroundColor 缺省页面背景色
	 */
	export default {
		computed: {
			hasLogin(){
				return !!this.$store.getters.hasLogin;
			}
		},
		props: {
			text: {
				type: String,
				default: '暂时没有数据'
			},
			type: {
				type: String,
				default: ''
			},
			backgroundColor: {
				type: String,
				default: 'rgba(0,0,0,0)'
			}
		},
		methods: {
			onCartBtnClick(){
				if(this.hasLogin){
					uni.switchTab({
						url: '/pages/tabbar/home'
					})
				}else{
					this.navTo('/pages/auth/login');
				}
			},
			switchTab(url){
				uni.switchTab({
					url
				})
			}
		}
	}
</script>

<style scoped lang="scss">
	.mix-empty{
		position: fixed;
		left: 0;
		right: 0;
		top: 0;
		bottom: 0;
		display: flex;
		flex-direction: column;
		align-items: center;
		animation: show .5s 1;
	}
	@keyframes show{
		from {
			opacity: 0;
		}
		to {
			opacity: 1;
		}
	}
	.default{
		padding-top: 26vh;
		/* #ifdef H5 */
		padding-top: 30vh;
		/* #endif */
		
		.icon{
			width: 460rpx;
			height: 342rpx;
		}
		.text{
			margin-top: 10rpx;
			font-size: 28rpx;
			color: #999;
		}
	}
	.cart{
		padding-top: 14vh;
		/* #ifdef H5 */
		padding-top: 18vh;
		/* #endif */
		
		.icon{
			width: 320rpx;
			height: 320rpx;
		}
		.title{
			margin: 50rpx 0 26rpx;
			font-size: 34rpx;
			color: #333;
		}
		.text{
			font-size: 28rpx;
			color: #aaa;
		}
		.btn{
			width: 320rpx;
			height: 80rpx;
			margin-top: 80rpx;
			text-indent: 2rpx;
			letter-spacing: 2rpx;
			font-size: 32rpx;
			color: #fff;
			border-radius: 100rpx;
			background: linear-gradient(to bottom right, #ffb2bf, $base-color);
		}
	}
	.address{
		padding-top: 6vh;
		/* #ifdef H5 */
		padding-top: 10vh;
		/* #endif */
		
		.icon{
			width: 380rpx;
			height: 380rpx;
		}
		.text{
			width: 400rpx;
			margin-top: 40rpx;
			font-size: 30rpx;
			color: #999;
			text-align: center;
			line-height: 1.6;
		}
		.btn{
			position: fixed;
			left: 50%;
			bottom: 120rpx;
			width: 110rpx;
			height: 110rpx;
			background-color: $base-color;
			border-radius: 100rpx;
			transform: translateX(-50%);
			box-shadow: 2rpx 2rpx 10rpx rgba(255, 83, 111, .5);
		}
		.icon-jia2{
			font-size: 50rpx;
			color: #fff;
		}
	}
	.favorite{
		padding-top: 6vh;
		/* #ifdef H5 */
		padding-top: 10vh;
		/* #endif */
		
		.icon{
			width: 360rpx;
			height: 360rpx;
		}
		.text{
			width: 400rpx;
			margin-top: 40rpx;
			font-size: 30rpx;
			color: #999;
			text-align: center;
			line-height: 1.6;
		}
		.btn{
			width: 320rpx;
			height: 80rpx;
			margin-top: 40rpx;
			text-indent: 2rpx;
			letter-spacing: 2rpx;
			font-size: 32rpx;
			color: #fff;
			border-radius: 100rpx;
			background: linear-gradient(to bottom right, #ffb2bf, $base-color);
		}
	}
</style>
