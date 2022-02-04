<template>
	<uni-popup ref="popup">
		<view class="pop-content">
			<text class="title">{{ title }}</text>
			<view class="con center">
				<text class="text">{{ text }}</text>
			</view>
			<view class="btn-group row b-t">
				<view class="btn center" @click="close">
					<text>{{ cancelText }}</text>
				</view>
				<view class="btn center b-l" @click="confirm">
					<text>{{ confirmText }}</text>
				</view>
			</view>
		</view>
	</uni-popup>
</template>

<script>
	/**
	 * 确认对话框
	 * @prop title 标题
	 * @prop text 提示内容
	 * @prop cancelText 取消按钮文字
	 * @prop confirmText 确认按钮文字
	 * @event onConfirm 确认按钮点击时触发
	 */
	export default {
		data() {
			return {
				
			};
		},
		props: {
			title: String,
			text: String,
			cancelText: {
				type: String,
				default: '取消'
			},
			confirmText: {
				type: String,
				default: '确定'
			}
		},
		methods: {
			confirm(){
				this.$emit('onConfirm');
				this.close();
			},
			open(){
				this.$refs.popup.open();
			},
			close(){
				this.$refs.popup.close();
			}
		}
	}
</script>

<style scoped lang='scss'>
	.pop-content{
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		width: 540rpx;
		padding-top: 36rpx;
		background-color: #fff;
		border-radius: 24rpx;
		overflow: hidden;
	
		.title{
			font-size: 32rpx;
			color: #333;
			line-height: 48rpx;
			font-weight: 700;
		}
		.con{
			padding: 36rpx 40rpx 54rpx;
		}
		.text{
			width: 460rpx;
			font-size: 26rpx;
			color: #333;
			line-height: 40rpx;
			text-align: center;
		}
		.btn-group{
			width: 100%;
		}
		.btn{
			flex: 1;
			height: 88rpx;
			line-height: 88rpx;
			font-size: 32rpx;
			color: #777;
			
			&:last-child{
				color: #007aff;
			}
		}
	}
</style>
