<template>
	<text class="uni-tag" v-if="text" :class="classes" :style="customStyle" @click="onClick">{{text}}</text>
</template>

<script>
	/**
	 * Tag 标签
	 * @description 用于展示1个或多个文字标签，可点击切换选中、不选中的状态
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=35
	 * @property {String} text 标签内容
	 * @property {String} size = [default|small|mini] 大小尺寸
	 * 	@value default 正常
	 * 	@value small 小尺寸
	 * 	@value mini 迷你尺寸
	 * @property {String} type = [default|primary|success｜warning｜error]  颜色类型
	 * 	@value default 灰色
	 * 	@value primary 蓝色
	 * 	@value success 绿色
	 * 	@value warning 黄色
	 * 	@value error 红色
	 * @property {Boolean} disabled = [true|false] 是否为禁用状态
	 * @property {Boolean} inverted = [true|false] 是否无需背景颜色（空心标签）
	 * @property {Boolean} circle = [true|false] 是否为圆角
	 * @event {Function} click 点击 Tag 触发事件
	 */

	export default {
		name: "UniTag",
		emits: ['click'],
		props: {
			type: {
				// 标签类型default、primary、success、warning、error、royal
				type: String,
				default: "default"
			},
			size: {
				// 标签大小 normal, small
				type: String,
				default: "normal"
			},
			// 标签内容
			text: {
				type: String,
				default: ""
			},
			disabled: {
				// 是否为禁用状态
				type: [Boolean, String],
				default: false
			},
			inverted: {
				// 是否为空心
				type: [Boolean, String],
				default: false
			},
			circle: {
				// 是否为圆角样式
				type: [Boolean, String],
				default: false
			},
			mark: {
				// 是否为标记样式
				type: [Boolean, String],
				default: false
			},
			customStyle: {
				type: String,
				default: ''
			}
		},
		computed: {
			classes() {
				const {
					type,
					disabled,
					inverted,
					circle,
					mark,
					size,
					isTrue
				} = this
				const classArr = [
					'uni-tag--' + type,
					'uni-tag--' + size,
					isTrue(disabled) ? 'uni-tag--disabled' : '',
					isTrue(inverted) ? 'uni-tag--' + type + '--inverted' : '',
					isTrue(circle) ? 'uni-tag--circle' : '',
					isTrue(mark) ? 'uni-tag--mark' : '',
					// type === 'default' ? 'uni-tag--default' : 'uni-tag-text',
					isTrue(inverted) ? 'uni-tag--inverted uni-tag-text--' + type : '',
					size === 'small' ? 'uni-tag-text--small' : ''
				]
				// 返回类的字符串，兼容字节小程序
				return classArr.join(' ')
			}
		},
		methods: {
			isTrue(value) {
				return value === true || value === 'true'
			},
			onClick() {
				if (this.isTrue(this.disabled)) return
				this.$emit("click");
			}
		}
	};
</script>

<style lang="scss">
	$uni-primary: #2979ff !default;
	$uni-success: #18bc37 !default;
	$uni-warning: #f3a73f !default;
	$uni-error: #e43d33 !default;
	$uni-info: #8f939c !default;


	$tag-default-pd: 4px 7px;
	$tag-small-pd: 2px 5px;
	$tag-mini-pd: 1px 3px;

	.uni-tag {
		line-height: 14px;
		font-size: 12px;
		font-weight: 200;
		padding: $tag-default-pd;
		color: #fff;
		border-radius: 3px;
		background-color: $uni-info;
		border-width: 1rpx;
		border-style: solid;
		border-color: $uni-info;
		/* #ifdef H5 */
		cursor: pointer;
		/* #endif */

		// size attr
		&--default {
			font-size: 12px;
		}

		&--default--inverted {
			color: $uni-info;
			border-color: $uni-info;
		}

		&--small {
			padding: $tag-small-pd;
			font-size: 12px;
			border-radius: 2px;
		}

		&--mini {
			padding: $tag-mini-pd;
			font-size: 12px;
			border-radius: 2px;
		}

		// type attr
		&--primary {
			background-color: $uni-primary;
			border-color: $uni-primary;
			color: #fff;
		}

		&--success {
			color: #fff;
			background-color: $uni-success;
			border-color: $uni-success;
		}

		&--warning {
			color: #fff;
			background-color: $uni-warning;
			border-color: $uni-warning;
		}

		&--error {
			color: #fff;
			background-color: $uni-error;
			border-color: $uni-error;
		}

		&--primary--inverted {
			color: $uni-primary;
			border-color: $uni-primary;
		}

		&--success--inverted {
			color: $uni-success;
			border-color: $uni-success;
		}

		&--warning--inverted {
			color: $uni-warning;
			border-color: $uni-warning;
		}

		&--error--inverted {
			color: $uni-error;
			border-color: $uni-error;
		}

		&--inverted {
			background-color: #fff;
		}

		// other attr
		&--circle {
			border-radius: 15px !important;
		}

		&--mark {
			border-top-left-radius: 0 !important;
			border-bottom-left-radius: 0 !important;
			border-top-right-radius: 15px !important;
			border-bottom-right-radius: 15px !important;
		}

		&--disabled {
			opacity: 0.5;
			/* #ifdef H5 */
			cursor: not-allowed;
			/* #endif */
		}
	}


	.uni-tag-text {
		color: #fff;
		font-size: 14px;

		&--primary {
			color: $uni-primary;
		}

		&--success {
			color: $uni-success;
		}

		&--warning {
			color: $uni-warning;
		}

		&--error {
			color: $uni-error;
		}

		&--small {
			font-size: 12px;
		}
	}
</style>
