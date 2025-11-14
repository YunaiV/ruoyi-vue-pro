<template>
	<view class="uni-badge--x">
		<slot />
		<text v-if="text" :class="classNames" :style="[positionStyle, customStyle, dotStyle]"
			class="uni-badge" @click="onClick()">{{displayValue}}</text>
	</view>
</template>

<script>
	/**
	 * Badge 数字角标
	 * @description 数字角标一般和其它控件（列表、9宫格等）配合使用，用于进行数量提示，默认为实心灰色背景
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=21
	 * @property {String} text 角标内容
	 * @property {String} size = [normal|small] 角标内容
	 * @property {String} type = [info|primary|success|warning|error] 颜色类型
	 * 	@value info 灰色
	 * 	@value primary 蓝色
	 * 	@value success 绿色
	 * 	@value warning 黄色
	 * 	@value error 红色
	 * @property {String} inverted = [true|false] 是否无需背景颜色
	 * @property {Number} maxNum 展示封顶的数字值，超过 99 显示 99+
	 * @property {String} absolute = [rightTop|rightBottom|leftBottom|leftTop] 开启绝对定位, 角标将定位到其包裹的标签的四角上
	 * 	@value rightTop 右上
	 * 	@value rightBottom 右下
	 * 	@value leftTop 左上
	 * 	@value leftBottom 左下
	 * @property {Array[number]} offset	距定位角中心点的偏移量，只有存在 absolute 属性时有效，例如：[-10, -10] 表示向外偏移 10px，[10, 10] 表示向 absolute 指定的内偏移 10px
	 * @property {String} isDot = [true|false] 是否显示为一个小点
	 * @event {Function} click 点击 Badge 触发事件
	 * @example <uni-badge text="1"></uni-badge>
	 */

	export default {
		name: 'UniBadge',
		emits: ['click'],
		props: {
			type: {
				type: String,
				default: 'error'
			},
			inverted: {
				type: Boolean,
				default: false
			},
			isDot: {
				type: Boolean,
				default: false
			},
			maxNum: {
				type: Number,
				default: 99
			},
			absolute: {
				type: String,
				default: ''
			},
			offset: {
				type: Array,
				default () {
					return [0, 0]
				}
			},
			text: {
				type: [String, Number],
				default: ''
			},
			size: {
				type: String,
				default: 'small'
			},
			customStyle: {
				type: Object,
				default () {
					return {}
				}
			}
		},
		data() {
			return {};
		},
		computed: {
			width() {
				return String(this.text).length * 8 + 12
			},
			classNames() {
				const {
					inverted,
					type,
					size,
					absolute
				} = this
				return [
					inverted ? 'uni-badge--' + type + '-inverted' : '',
					'uni-badge--' + type,
					'uni-badge--' + size,
					absolute ? 'uni-badge--absolute' : ''
				].join(' ')
			},
			positionStyle() {
				if (!this.absolute) return {}
				let w = this.width / 2,
					h = 10
				if (this.isDot) {
					w = 5
					h = 5
				}
				const x = `${- w  + this.offset[0]}px`
				const y = `${- h + this.offset[1]}px`

				const whiteList = {
					rightTop: {
						right: x,
						top: y
					},
					rightBottom: {
						right: x,
						bottom: y
					},
					leftBottom: {
						left: x,
						bottom: y
					},
					leftTop: {
						left: x,
						top: y
					}
				}
				const match = whiteList[this.absolute]
				return match ? match : whiteList['rightTop']
			},
			dotStyle() {
				if (!this.isDot) return {}
				return {
					width: '10px',
					minWidth: '0',
					height: '10px',
					padding: '0',
					borderRadius: '10px'
				}
			},
			displayValue() {
				const {
					isDot,
					text,
					maxNum
				} = this
				return isDot ? '' : (Number(text) > maxNum ? `${maxNum}+` : text)
			}
		},
		methods: {
			onClick() {
				this.$emit('click');
			}
		}
	};
</script>

<style lang="scss" >
	$uni-primary: #2979ff !default;
	$uni-success: #4cd964 !default;
	$uni-warning: #f0ad4e !default;
	$uni-error: #dd524d !default;
	$uni-info: #909399 !default;


	$bage-size: 12px;
	$bage-small: scale(0.8);

	.uni-badge--x {
		/* #ifdef APP-NVUE */
		// align-self: flex-start;
		/* #endif */
		/* #ifndef APP-NVUE */
		display: inline-block;
		/* #endif */
		position: relative;
	}

	.uni-badge--absolute {
		position: absolute;
	}

	.uni-badge--small {
		transform: $bage-small;
		transform-origin: center center;
	}

	.uni-badge {
		/* #ifndef APP-NVUE */
		display: flex;
		overflow: hidden;
		box-sizing: border-box;
		font-feature-settings: "tnum";
		min-width: 20px;
		/* #endif */
		justify-content: center;
		flex-direction: row;
		height: 20px;
		padding: 0 4px;
		line-height: 18px;
		color: #fff;
		border-radius: 100px;
		background-color: $uni-info;
		background-color: transparent;
		border: 1px solid #fff;
		text-align: center;
		font-family: 'Helvetica Neue', Helvetica, sans-serif;
		font-size: $bage-size;
		/* #ifdef H5 */
		z-index: 999;
		cursor: pointer;
		/* #endif */

		&--info {
			color: #fff;
			background-color: $uni-info;
		}

		&--primary {
			background-color: $uni-primary;
		}

		&--success {
			background-color: $uni-success;
		}

		&--warning {
			background-color: $uni-warning;
		}

		&--error {
			background-color: $uni-error;
		}

		&--inverted {
			padding: 0 5px 0 0;
			color: $uni-info;
		}

		&--info-inverted {
			color: $uni-info;
			background-color: transparent;
		}

		&--primary-inverted {
			color: $uni-primary;
			background-color: transparent;
		}

		&--success-inverted {
			color: $uni-success;
			background-color: transparent;
		}

		&--warning-inverted {
			color: $uni-warning;
			background-color: transparent;
		}

		&--error-inverted {
			color: $uni-error;
			background-color: transparent;
		}

	}
</style>
