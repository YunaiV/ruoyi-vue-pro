<template>
	<view class="mix-icon-loading">
		<view 
			class="loading-icon"
			:style="{
				width: size,
				height: size,
				borderRightColor: color
			}"
		></view>
	</view>
</template>

<script>
	/**
	 * 菊花loading小图标
	 * @prop size 尺寸，单位rpx
	 * @prop color 颜色
	 */
	export default {
		name: 'MixIconLoading',
		data() {
			return {
				
			};
		},
		props: {
			size: {
				type: String,
				default: '26rpx'
			},
			color: {
				type: String,
				default: '#999'
			}
		},
		methods: {
			
		}
	}
</script>

<style scoped lang='scss'>
	.mix-icon-loading{
		display: flex;
		align-items: center;
		justify-content: center;
		width: auto;
		height: auto;
	}
	.loading-icon{
		width: 28rpx;
		height: 28rpx;
		border: 4rpx solid #ddd;
		animation: mix-loading 1.8s steps(12) infinite;
		border-radius: 100rpx;
	}
	@keyframes mix-loading{
		from {
			transform:rotate(0deg)
		}
		to {
			transform: rotate(1turn)
		}
	}
</style>
