<template>
	<uni-popup ref="uniPopup" type="bottom">
		<view class="content">
			<text class="mix-icon icon-guanbi" @click="close"></text>
			<view class="center title">
				<text>输入支付密码</text>
			</view>
			<view class="input center">
				<view class="item center" :class="{has: pwd.length > index}" v-for="(item, index) in 6" :key="index"></view>
			</view>
			<view class="reset-btn center" @click="navTo('/pages/auth/payPassword')">
				<text>重置密码</text>
			</view>
			<number-keyboard ref="keybord" @onChange="onNumberChange"></number-keyboard>
		</view>
	</uni-popup>
</template>

<script>
	/**
	 * 支付密码键盘
	 */
	export default {
		data() {
			return {
				pwd: ''
			};
		},
		watch: {
			pwd(pwd){
				if(pwd.length === 0){
					this.$refs.keybord.val = '';
				}
			}
		},
		methods: {
			open(){
				this.$refs.uniPopup.open();
			},
			close(){
				this.$refs.uniPopup.close();
			},
			onNumberChange(pwd){
				this.pwd = pwd;
				if(pwd.length >= 6){
					this.$emit('onConfirm', pwd.substring(0,6));
				}
			}
		}
	}
</script>

<style scoped lang="scss">
	.content{
		border-radius: 20rpx 20rpx 0 0;
		background-color: #fff;
		position: relative;
	}
	.title{
		height: 110rpx;
		font-size: 32rpx;
		color: #333;
		font-weight: 700;
	}
	.input{
		padding: 30rpx 0 60rpx;
		
		.item{
			width: 88rpx;
			height: 88rpx;
			margin: 0 10rpx;
			border: 1px solid #ddd;
			border-radius: 4rpx;
		}
		.has:after{
			content: '';
			width: 16rpx;
			height: 16rpx;
			border-radius: 100rpx;
			background-color: #333;
		}
	}
	.reset-btn{
		padding-bottom: 20rpx;
		margin-top: -10rpx;
		margin-bottom: 30rpx;
		font-size: 28rpx;
		color: #007aff;
	}
	.icon-guanbi{
		position: absolute;
		left: 10rpx;
		top: 24rpx;
		padding: 20rpx;
		font-size: 28rpx;
	}
</style>
