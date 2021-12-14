<template>
	<view>
		<view v-show="status !== 2" class="mix-load-more center">
			<image class="loading-icon" src="/static/loading/hamster.gif"></image>
			<text class="text">{{ textList[status] }}</text>
		</view>
		<view v-show="status === 2" class="mix-load-more center">
			<image class="logo" src="/static/logo-b-w.png"></image>
			<text class="text">国云网络提供技术支持</text>
		</view>
	</view>
</template>

<script>
	/**
	 * 上划加载更多
	 * @prop {Number} status 0加载前，1加载中，2没有更多
	 * @prop {Array} textList ['加载前提示', '加载中提示', '加载完提示']
	 */
	export default {
		name: "mix-load-more",
		props: {
			status: {
				type: Number,
				default: 0
			},
			textList: {
				type: Array,
				default () {
					return [
						'上拉显示更多',
						'正在加载 ..',
						'我也是有底线的~'
					];
				}
			}
		}
	}
</script>

<style scoped lang="scss">
	.mix-load-more{
		width: 750rpx;
		height: 110rpx;
	}
	.loading-icon{
		width: 64rpx;
		height: 68rpx;
		margin-right: 20rpx;
	}
	.text{
		font-size: 26rpx;
		color: #999;
	}
	.logo{
		width: 34rpx;
		height: 34rpx;
		margin-right: 12rpx;
	}
</style>