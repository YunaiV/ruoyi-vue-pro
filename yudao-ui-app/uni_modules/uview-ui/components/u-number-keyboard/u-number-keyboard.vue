<template>
	<view
		class="u-keyboard"
		@touchmove.stop.prevent="noop"
	>
		<view
			class="u-keyboard__button-wrapper"
			v-for="(item, index) in numList"
			:key="index"
		>
			<view
				class="u-keyboard__button-wrapper__button"
				:style="[itemStyle(index)]"
				@tap="keyboardClick(item)"
				hover-class="u-hover-class"
				:hover-stay-time="200"
			>
				<text class="u-keyboard__button-wrapper__button__text">{{ item }}</text>
			</view>
		</view>
		<view
			class="u-keyboard__button-wrapper"
		>
			<view
				class="u-keyboard__button-wrapper__button u-keyboard__button-wrapper__button--gray"
				hover-class="u-hover-class"
				:hover-stay-time="200"
				@touchstart.stop="backspaceClick"
				@touchend="clearTimer"
			>
				<u-icon
					name="backspace"
					color="#303133"
					size="28"
				></u-icon>
			</view>
		</view>
	</view>
</template>

<script>
	import props from './props.js';

	/**
	 * keyboard 键盘组件
	 * @description
	 * @tutorial
	 * @property {String}	mode		键盘的类型，number-数字键盘，card-身份证键盘
	 * @property {Boolean}	dotDisabled	是否显示键盘的"."符号
	 * @property {Boolean}	random		是否打乱键盘按键的顺序
	 * @event {Function} change		点击键盘触发
	 * @event {Function} backspace	点击退格键触发
	 * @example
	 */
	export default {
		mixins: [uni.$u.mpMixin, uni.$u.mixin, props],
		data() {
			return {
				backspace: 'backspace', // 退格键内容
				dot: '.', // 点
				timer: null, // 长按多次删除的事件监听
				cardX: 'X' // 身份证的X符号
			};
		},
		computed: {
			// 键盘需要显示的内容
			numList() {
				let tmp = [];
				if (this.dotDisabled && this.mode == 'number') {
					if (!this.random) {
						return [1, 2, 3, 4, 5, 6, 7, 8, 9, 0];
					} else {
						return uni.$u.randomArray([1, 2, 3, 4, 5, 6, 7, 8, 9, 0]);
					}
				} else if (!this.dotDisabled && this.mode == 'number') {
					if (!this.random) {
						return [1, 2, 3, 4, 5, 6, 7, 8, 9, this.dot, 0];
					} else {
						return uni.$u.randomArray([1, 2, 3, 4, 5, 6, 7, 8, 9, this.dot, 0]);
					}
				} else if (this.mode == 'card') {
					if (!this.random) {
						return [1, 2, 3, 4, 5, 6, 7, 8, 9, this.cardX, 0];
					} else {
						return uni.$u.randomArray([1, 2, 3, 4, 5, 6, 7, 8, 9, this.cardX, 0]);
					}
				}
			},
			// 按键的样式，在非乱序&&数字键盘&&不显示点按钮时，index为9时，按键占位两个空间
			itemStyle() {
				return index => {
					let style = {};
					if (this.mode == 'number' && this.dotDisabled && index == 9) style.width = '464rpx';
					return style;
				};
			},
			// 是否让按键显示灰色，只在非乱序&&数字键盘&&且允许点按键的时候
			btnBgGray() {
				return index => {
					if (!this.random && index == 9 && (this.mode != 'number' || (this.mode == 'number' && !this
							.dotDisabled))) return true;
					else return false;
				};
			},
		},
		created() {

		},
		methods: {
			// 点击退格键
			backspaceClick() {
				this.$emit('backspace');
				clearInterval(this.timer); //再次清空定时器，防止重复注册定时器
				this.timer = null;
				this.timer = setInterval(() => {
					this.$emit('backspace');
				}, 250);
			},
			clearTimer() {
				clearInterval(this.timer);
				this.timer = null;
			},
			// 获取键盘显示的内容
			keyboardClick(val) {
				// 允许键盘显示点模式和触发非点按键时，将内容转为数字类型
				if (!this.dotDisabled && val != this.dot && val != this.cardX) val = Number(val);
				this.$emit('change', val);
			}
		}
	};
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";
	$u-number-keyboard-background-color:rgb(224, 228, 230) !default;
	$u-number-keyboard-padding:8px 10rpx 8px 10rpx !default;
	$u-number-keyboard-button-width:222rpx !default;
	$u-number-keyboard-button-margin:4px 6rpx !default;
	$u-number-keyboard-button-border-top-left-radius:4px !default;
	$u-number-keyboard-button-border-top-right-radius:4px !default;
	$u-number-keyboard-button-border-bottom-left-radius:4px !default;
	$u-number-keyboard-button-border-bottom-right-radius:4px !default;
	$u-number-keyboard-button-height: 90rpx!default;
	$u-number-keyboard-button-background-color:#FFFFFF !default;
	$u-number-keyboard-button-box-shadow:0 2px 0px #BBBCBE !default;
	$u-number-keyboard-text-font-size:20px !default;
	$u-number-keyboard-text-font-weight:500 !default;
	$u-number-keyboard-text-color:$u-main-color !default;
	$u-number-keyboard-gray-background-color:rgb(200, 202, 210) !default;
	$u-number-keyboard-u-hover-class-background-color: #BBBCC6 !default;

	.u-keyboard {
		@include flex;
		flex-direction: row;
		justify-content: space-around;
		background-color: $u-number-keyboard-background-color;
		flex-wrap: wrap;
		padding: $u-number-keyboard-padding;

		&__button-wrapper {
			box-shadow: $u-number-keyboard-button-box-shadow;
			margin: $u-number-keyboard-button-margin;
			border-top-left-radius: $u-number-keyboard-button-border-top-left-radius;
			border-top-right-radius: $u-number-keyboard-button-border-top-right-radius;
			border-bottom-left-radius: $u-number-keyboard-button-border-bottom-left-radius;
			border-bottom-right-radius: $u-number-keyboard-button-border-bottom-right-radius;

			&__button {
				width: $u-number-keyboard-button-width;
				height: $u-number-keyboard-button-height;
				background-color: $u-number-keyboard-button-background-color;
				@include flex;
				justify-content: center;
				align-items: center;
				border-top-left-radius: $u-number-keyboard-button-border-top-left-radius;
				border-top-right-radius: $u-number-keyboard-button-border-top-right-radius;
				border-bottom-left-radius: $u-number-keyboard-button-border-bottom-left-radius;
				border-bottom-right-radius: $u-number-keyboard-button-border-bottom-right-radius;

				&__text {
					font-size: $u-number-keyboard-text-font-size;
					font-weight: $u-number-keyboard-text-font-weight;
					color: $u-number-keyboard-text-color;
				}

				&--gray {
					background-color: $u-number-keyboard-gray-background-color;
				}
			}
		}
	}

	.u-hover-class {
		background-color: $u-number-keyboard-u-hover-class-background-color;
	}
</style>
