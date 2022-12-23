<template>
	<view class="u-circle-progress">
		<view class="u-circle-progress__left">
			<view
			    class="u-circle-progress__left__circle"
			    :style="[leftSyle]"
			    ref="left-circle"
			>

			</view>
		</view>
		<view
		    class="u-circle-progress__right"
		>
			<view
			    class="u-circle-progress__right__circle"
			    ref="right-circle"
				:style="[rightSyle]"
			>

			</view>
		</view>
		<view class="u-circle-progress__circle">

		</view>
	</view>
</template>

<script>
	import props from './props.js';
	// #ifdef APP-NVUE
	const animation = uni.requireNativePlugin('animation')
	// #endif
	/**
	 * CircleProgress 圆形进度条 TODO: 待完善 
	 * @description 展示操作或任务的当前进度，比如上传文件，是一个圆形的进度环。
	 * @tutorial https://www.uviewui.com/components/circleProgress.html
	 * @property {String | Number}	percentage	圆环进度百分比值，为数值类型，0-100 (默认 30 )
	 * @example
	 */
	export default {
		name: 'u-circle-progress',
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		data() {
			return {
				leftBorderColor: 'rgb(200, 200, 200)',
				rightBorderColor: 'rgb(200, 200, 200)',
			}
		},
		computed: {
			leftSyle() {
				const style = {}
				style.borderTopColor = this.leftBorderColor
				style.borderRightColor = this.leftBorderColor
				return style
			},
			rightSyle() {
				const style = {}
				style.borderLeftColor = this.rightBorderColor
				style.borderBottomColor = this.rightBorderColor
				return style
			}
		},
		mounted() {
			uni.$u.sleep().then(() => {
				this.rightBorderColor = 'rgb(66, 185, 131)'
				// this.init()
			})
		},
		methods: {
			init() {
				animation.transition(this.$refs['right-circle'].ref, {
					styles: {
						transform: 'rotate(45deg)',
						transformOrigin: 'center center'
					},
				}, () => {
					this.rightBorderColor = 'rgb(66, 185, 131)'
					// animation.transition(this.$refs['right-circle'].ref, {
					// 	styles: {
					// 		transform: 'rotate(225deg)',
					// 		transformOrigin: 'center center'
					// 	},
					// 	duration: 3000,
					// }, () => {
					// 	animation.transition(this.$refs['left-circle'].ref, {
					// 		styles: {
					// 			transform: 'rotate(45deg)',
					// 			transformOrigin: 'center center'
					// 		},
					// 	}, () => {
					// 		this.leftBorderColor = 'rgb(66, 185, 131)'
					// 		animation.transition(this.$refs['left-circle'].ref, {
					// 			styles: {
					// 				transform: 'rotate(225deg)',
					// 				transformOrigin: 'center center'
					// 			},
					// 			duration: 1500,
					// 		}, () => {

					// 		})
					// 	})
					// })
				})

			}
		},
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-circle-progress {
		@include flex(row);
		position: relative;
		border-radius: 100px;
		height: 100px;
		width: 100px;
		// transform: rotate(0deg);
		// background-color: rgb(66, 185, 131);
		background-color: rgb(200, 200, 200);
		overflow: hidden;
		justify-content: space-between;

		&__circle {
			border-radius: 100px;
			height: 90px;
			width: 90px;
			transform: translate(-50%, -50%);
			background-color: rgb(255, 255, 255);
			left: 50px;
			top: 50px;
			position: absolute;
		}

		&__left {
			position: absolute;
			left: 0;
			width: 50px;
			height: 100px;
			overflow: hidden;
			box-sizing: border-box;
			// background-color: rgb(66, 185, 131);
			// background-color: rgb(200, 200, 200);
			// transform-origin: left center;

			&__circle {
				box-sizing: border-box;
				// background-color: red;
				border-left-color: transparent;
				border-bottom-color: transparent;
				border-top-left-radius: 50px;
				border-top-right-radius: 50px;
				border-bottom-right-radius: 50px;
				// border-left-color: rgb(66, 185, 131);
				// border-bottom-color: rgb(66, 185, 131);
				border-top-color: rgb(66, 185, 131);
				border-right-color: rgb(66, 185, 131);
				border-width: 5px;
				width: 100px;
				height: 100px;
				transform: rotate(225deg);
				// border-radius: 100px;
			}
		}

		&__right {
			position: absolute;
			right: 0;
			width: 50px;
			height: 100px;
			overflow: hidden;

			&__circle {
				position: absolute;
				right: 0;
				box-sizing: border-box;
				// background-color: red;
				border-top-color: transparent;
				border-right-color: transparent;
				border-top-left-radius: 50px;
				border-bottom-left-radius: 50px;
				border-bottom-right-radius: 50px;
				// border-left-color: rgb(66, 185, 131);
				// border-bottom-color: rgb(66, 185, 131);
				border-left-color: rgb(200, 200, 200);
				border-bottom-color: rgb(200, 200, 200);
				border-width: 5px;
				width: 100px;
				height: 100px;
				transform: rotate(45deg);
				transform-origin: center center;
				// border-radius: 100px;
			}
		}
	}
</style>
