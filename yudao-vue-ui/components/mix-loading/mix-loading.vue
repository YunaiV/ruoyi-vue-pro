<template>
	<view class="mix-loading center">
		<view v-if="!isTimeout" class="center">
			<view v-if="mask" class="mask" @click.stop.prevent="stopPrevent" @touchmove.stop.prevent="stopPrevent"></view>
			<!-- 黑底菊花 -->
			<view v-if="type === 1" class="chry column center">
				<view class="icon"></view>
				<text class="tit">{{ title }}</text>
			</view>
			<!-- 仓鼠 -->
			<image v-else-if="type === 2" class="hamster" src="/static/loading/hamster.gif"></image>
		</view>
	</view>
</template>

<script>
	/**
	 * loading
	 * @prop type 1 黑底菊花 2 小胖仓鼠
	 * @prop mask 遮罩层
	 * @prop timeout 超时时间（秒） 默认10s
	 */
	export default {
		name: 'MixIconLoading',
		data(){
			return {
				isTimeout: false
			}
		},
		props: {
			type: {
				type: Number,
				default: 1
			},
			mask: {
				type: Boolean,
				default: false
			},
			timeout: {
				type: Number,
				default: 10
			},
			title: {
				type: String,
				default: '请稍候'
			}
		},
		created() {
			this._timer = setTimeout(()=>{
				if(!this.isLoading){
					return;
				}
				this.isTimeout = true;
				uni.showToast({
					title: '加载超时，请重试',
					icon: 'none'
				})
			}, this.timeout * 1000)
		},
		destroyed() {
			this._timer && clearTimeout(this._timer);
		}
	}
</script>

<style scoped lang='scss'>
	.mix-loading{
		position: fixed;
		left: 50vw;
		top: 46vh;
		width: 0;
		height: 0;
		z-index: 999;
	}
	.mask{
		position: fixed;
		left: 0;
		top: 0;
		right: 0;
		bottom: 0;
	}
	.chry{
		min-width: 120rpx;
		min-height: 116rpx;
		border-radius: 10rpx;
		background-color: rgba(17,17,17,.7);
		
		.icon{
			width: 64rpx;
			height: 64rpx;
			background-image: url(data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIxMjAiIGhlaWdodD0iMTIwIiB2aWV3Qm94PSIwIDAgMTAwIDEwMCI+PHBhdGggZmlsbD0ibm9uZSIgZD0iTTAgMGgxMDB2MTAwSDB6Ii8+PHJlY3Qgd2lkdGg9IjciIGhlaWdodD0iMjAiIHg9IjQ2LjUiIHk9IjQwIiBmaWxsPSIjRTlFOUU5IiByeD0iNSIgcnk9IjUiIHRyYW5zZm9ybT0idHJhbnNsYXRlKDAgLTMwKSIvPjxyZWN0IHdpZHRoPSI3IiBoZWlnaHQ9IjIwIiB4PSI0Ni41IiB5PSI0MCIgZmlsbD0iIzk4OTY5NyIgcng9IjUiIHJ5PSI1IiB0cmFuc2Zvcm09InJvdGF0ZSgzMCAxMDUuOTggNjUpIi8+PHJlY3Qgd2lkdGg9IjciIGhlaWdodD0iMjAiIHg9IjQ2LjUiIHk9IjQwIiBmaWxsPSIjOUI5OTlBIiByeD0iNSIgcnk9IjUiIHRyYW5zZm9ybT0icm90YXRlKDYwIDc1Ljk4IDY1KSIvPjxyZWN0IHdpZHRoPSI3IiBoZWlnaHQ9IjIwIiB4PSI0Ni41IiB5PSI0MCIgZmlsbD0iI0EzQTFBMiIgcng9IjUiIHJ5PSI1IiB0cmFuc2Zvcm09InJvdGF0ZSg5MCA2NSA2NSkiLz48cmVjdCB3aWR0aD0iNyIgaGVpZ2h0PSIyMCIgeD0iNDYuNSIgeT0iNDAiIGZpbGw9IiNBQkE5QUEiIHJ4PSI1IiByeT0iNSIgdHJhbnNmb3JtPSJyb3RhdGUoMTIwIDU4LjY2IDY1KSIvPjxyZWN0IHdpZHRoPSI3IiBoZWlnaHQ9IjIwIiB4PSI0Ni41IiB5PSI0MCIgZmlsbD0iI0IyQjJCMiIgcng9IjUiIHJ5PSI1IiB0cmFuc2Zvcm09InJvdGF0ZSgxNTAgNTQuMDIgNjUpIi8+PHJlY3Qgd2lkdGg9IjciIGhlaWdodD0iMjAiIHg9IjQ2LjUiIHk9IjQwIiBmaWxsPSIjQkFCOEI5IiByeD0iNSIgcnk9IjUiIHRyYW5zZm9ybT0icm90YXRlKDE4MCA1MCA2NSkiLz48cmVjdCB3aWR0aD0iNyIgaGVpZ2h0PSIyMCIgeD0iNDYuNSIgeT0iNDAiIGZpbGw9IiNDMkMwQzEiIHJ4PSI1IiByeT0iNSIgdHJhbnNmb3JtPSJyb3RhdGUoLTE1MCA0NS45OCA2NSkiLz48cmVjdCB3aWR0aD0iNyIgaGVpZ2h0PSIyMCIgeD0iNDYuNSIgeT0iNDAiIGZpbGw9IiNDQkNCQ0IiIHJ4PSI1IiByeT0iNSIgdHJhbnNmb3JtPSJyb3RhdGUoLTEyMCA0MS4zNCA2NSkiLz48cmVjdCB3aWR0aD0iNyIgaGVpZ2h0PSIyMCIgeD0iNDYuNSIgeT0iNDAiIGZpbGw9IiNEMkQyRDIiIHJ4PSI1IiByeT0iNSIgdHJhbnNmb3JtPSJyb3RhdGUoLTkwIDM1IDY1KSIvPjxyZWN0IHdpZHRoPSI3IiBoZWlnaHQ9IjIwIiB4PSI0Ni41IiB5PSI0MCIgZmlsbD0iI0RBREFEQSIgcng9IjUiIHJ5PSI1IiB0cmFuc2Zvcm09InJvdGF0ZSgtNjAgMjQuMDIgNjUpIi8+PHJlY3Qgd2lkdGg9IjciIGhlaWdodD0iMjAiIHg9IjQ2LjUiIHk9IjQwIiBmaWxsPSIjRTJFMkUyIiByeD0iNSIgcnk9IjUiIHRyYW5zZm9ybT0icm90YXRlKC0zMCAtNS45OCA2NSkiLz48L3N2Zz4=);
			background-repeat: no-repeat;
			background-size: 100% 100%;
			animation: mix-loading 1s steps(12) infinite;
		}
		.tit{
			margin: 10rpx 0 6rpx;
			font-size: 20rpx;
			color: #e9e9e9;
		}
	}
	@keyframes mix-loading{
		from {
			transform:rotate(0deg)
		}
		to {
			transform: rotate(1turn)
		}
	}
	.hamster{
		width: 106rpx;
		height: 120rpx;
	}
</style>
