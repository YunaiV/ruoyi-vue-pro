<template>
	<view
	    class="u-line-progress"
	    :style="[$u.addStyle(customStyle)]"
	>
		<view
		    class="u-line-progress__background"
		    ref="u-line-progress__background"
		    :style="[{
				backgroundColor: inactiveColor,
				height: $u.addUnit(height),
			}]"
		>
		</view>
		<view
		    class="u-line-progress__line"
		    :style="[progressStyle]"
		> 
			<slot>
				<text v-if="showText && percentage >= 10" class="u-line-progress__text">{{innserPercentage + '%'}}</text>
			</slot> 
		</view>
	</view>
</template>

<script>
	import props from './props.js';
	// #ifdef APP-NVUE
	const dom = uni.requireNativePlugin('dom')
	// #endif
	/**
	 * lineProgress 线型进度条
	 * @description 展示操作或任务的当前进度，比如上传文件，是一个线形的进度条。
	 * @tutorial https://www.uviewui.com/components/lineProgress.html
	 * @property {String}			activeColor		激活部分的颜色 ( 默认 '#19be6b' )
	 * @property {String}			inactiveColor	背景色 ( 默认 '#ececec' )
	 * @property {String | Number}	percentage		进度百分比，数值 ( 默认 0 )
	 * @property {Boolean}			showText		是否在进度条内部显示百分比的值 ( 默认 true )
	 * @property {String | Number}	height			进度条的高度，单位px ( 默认 12 )
	 * 
	 * @example <u-line-progress :percent="70" :show-percent="true"></u-line-progress>
	 */
	export default {
		name: "u-line-progress",
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		data() {
			return {
				lineWidth: 0,
			}
		},
		watch: {
			percentage(n) {
				this.resizeProgressWidth()
			}
		},
		computed: {
			progressStyle() { 
				let style = {}
				style.width = this.lineWidth
				style.backgroundColor = this.activeColor
				style.height = uni.$u.addUnit(this.height)
				return style
			},
			innserPercentage() {
				// 控制范围在0-100之间
				return uni.$u.range(0, 100, this.percentage)
			}
		},
		mounted() {
			this.init()
		},
		methods: {
			init() {
				uni.$u.sleep(20).then(() => {
					this.resizeProgressWidth()
				})
			},
			getProgressWidth() {
				// #ifndef APP-NVUE
				return this.$uGetRect('.u-line-progress__background')
				// #endif

				// #ifdef APP-NVUE
				// 返回一个promise
				return new Promise(resolve => {
					dom.getComponentRect(this.$refs['u-line-progress__background'], (res) => {
						resolve(res.size)
					})
				})
				// #endif
			},
			resizeProgressWidth() {
				this.getProgressWidth().then(size => {
					const {
						width
					} = size
					// 通过设置的percentage值，计算其所占总长度的百分比
					this.lineWidth = width * this.innserPercentage / 100 + 'px'
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-line-progress {
		align-items: stretch;
		position: relative;
		@include flex(row);
		flex: 1;
		overflow: hidden;
		border-radius: 100px;

		&__background {
			background-color: #ececec;
			border-radius: 100px;
			flex: 1;
		}

		&__line {
			position: absolute;
			top: 0;
			left: 0;
			bottom: 0;
			align-items: center;
			@include flex(row);
			color: #ffffff;
			border-radius: 100px;
			transition: width 0.5s ease;
			justify-content: flex-end;
		}

		&__text {
			font-size: 10px;
			align-items: center;
			text-align: right;
			color: #FFFFFF;
			margin-right: 5px;
			transform: scale(0.9);
		}
	}
</style>
