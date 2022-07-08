<template>
	<view
	    class="u-radio"
		@tap.stop="wrapperClickHandler"
	    :style="[radioStyle]"
	    :class="[`u-radio-label--${parentData.iconPlacement}`, parentData.borderBottom && parentData.placement === 'column' && 'u-border-bottom']"
	>
		<view
		    class="u-radio__icon-wrap"
		    @tap.stop="iconClickHandler"
		    :class="iconClasses"
		    :style="[iconWrapStyle]"
		>
			<slot name="icon">
				<u-icon
				    class="u-radio__icon-wrap__icon"
				    name="checkbox-mark"
				    :size="elIconSize"
				    :color="elIconColor"
				/>
			</slot>
		</view>
		<text
			class="u-radio__text"
		    @tap.stop="labelClickHandler"
		    :style="{
				color: elDisabled ? elInactiveColor : elLabelColor,
				fontSize: elLabelSize,
				lineHeight: elLabelSize
			}"
		>{{label}}</text>
	</view>
</template>

<script>
	import props from './props.js';
	/**
	 * radio 单选框
	 * @description 单选框用于有一个选择，用户只能选择其中一个的场景。搭配u-radio-group使用
	 * @tutorial https://www.uviewui.com/components/radio.html
	 * @property {String | Number}	name			radio的名称
	 * @property {String}			shape			形状，square为方形，circle为圆型
	 * @property {Boolean}			disabled		是否禁用
	 * @property {String | Boolean}	labelDisabled	是否禁止点击提示语选中单选框
	 * @property {String}			activeColor		选中时的颜色，如设置parent的active-color将失效
	 * @property {String}			inactiveColor	未选中的颜色
	 * @property {String | Number}	iconSize		图标大小，单位px
	 * @property {String | Number}	labelSize		label字体大小，单位px
	 * @property {String | Number}	label			label提示文字，因为nvue下，直接slot进来的文字，由于特殊的结构，无法修改样式
	 * @property {String | Number}	size			整体的大小
	 * @property {String}			iconColor		图标颜色
	 * @property {String}			labelColor		label的颜色
	 * @property {Object}			customStyle		组件的样式，对象形式
	 * 
	 * @event {Function} change 某个radio状态发生变化时触发(选中状态)
	 * @example <u-radio :labelDisabled="false">门掩黄昏，无计留春住</u-radio>
	 */
	export default {
		name: "u-radio",
		
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		data() {
			return {
				checked: false,
				// 当你看到这段代码的时候，
				// 父组件的默认值，因为头条小程序不支持在computed中使用this.parent.shape的形式
				// 故只能使用如此方法
				parentData: {
					iconSize: 12,
					labelDisabled: null,
					disabled: null,
					shape: null,
					activeColor: null,
					inactiveColor: null,
					size: 18,
					value: null,
					iconColor: null,
					placement: 'row',
					borderBottom: false,
					iconPlacement: 'left'
				}
			}
		},
		computed: {
			// 是否禁用，如果父组件u-raios-group禁用的话，将会忽略子组件的配置
			elDisabled() {
				return this.disabled !== '' ? this.disabled : this.parentData.disabled !== null ? this.parentData.disabled : false;
			},
			// 是否禁用label点击
			elLabelDisabled() {
				return this.labelDisabled !== '' ? this.labelDisabled : this.parentData.labelDisabled !== null ? this.parentData.labelDisabled :
					false;
			},
			// 组件尺寸，对应size的值，默认值为21px
			elSize() {
				return this.size ? this.size : (this.parentData.size ? this.parentData.size : 21);
			},
			// 组件的勾选图标的尺寸，默认12px
			elIconSize() {
				return this.iconSize ? this.iconSize : (this.parentData.iconSize ? this.parentData.iconSize : 12);
			},
			// 组件选中激活时的颜色
			elActiveColor() {
				return this.activeColor ? this.activeColor : (this.parentData.activeColor ? this.parentData.activeColor : '#2979ff');
			},
			// 组件选未中激活时的颜色
			elInactiveColor() {
				return this.inactiveColor ? this.inactiveColor : (this.parentData.inactiveColor ? this.parentData.inactiveColor :
					'#c8c9cc');
			},
			// label的颜色
			elLabelColor() {
				return this.labelColor ? this.labelColor : (this.parentData.labelColor ? this.parentData.labelColor : '#606266')
			},
			// 组件的形状
			elShape() {
				return this.shape ? this.shape : (this.parentData.shape ? this.parentData.shape : 'circle');
			},
			// label大小
			elLabelSize() {
				return uni.$u.addUnit(this.labelSize ? this.labelSize : (this.parentData.labelSize ? this.parentData.labelSize :
					'15'))
			},
			elIconColor() {
				const iconColor = this.iconColor ? this.iconColor : (this.parentData.iconColor ? this.parentData.iconColor :
					'#ffffff');
				// 图标的颜色
				if (this.elDisabled) {
					// disabled状态下，已勾选的radio图标改为elInactiveColor
					return this.checked ? this.elInactiveColor : 'transparent'
				} else {
					return this.checked ? iconColor : 'transparent'
				}
			},
			iconClasses() {
				let classes = []
				// 组件的形状
				classes.push('u-radio__icon-wrap--' + this.elShape)
				if (this.elDisabled) {
					classes.push('u-radio__icon-wrap--disabled')
				}
				if (this.checked && this.elDisabled) {
					classes.push('u-radio__icon-wrap--disabled--checked')
				}
				// 支付宝，头条小程序无法动态绑定一个数组类名，否则解析出来的结果会带有","，而导致失效
				// #ifdef MP-ALIPAY || MP-TOUTIAO
				classes = classes.join(' ')
				// #endif
				return classes
			},
			iconWrapStyle() {
				// radio的整体样式
				const style = {}
				style.backgroundColor = this.checked && !this.elDisabled ? this.elActiveColor : '#ffffff'
				style.borderColor = this.checked && !this.elDisabled ? this.elActiveColor : this.elInactiveColor
				style.width = uni.$u.addUnit(this.elSize)
				style.height = uni.$u.addUnit(this.elSize)
				// 如果是图标在右边的话，移除它的右边距
				if (this.parentData.iconPlacement === 'right') {
					style.marginRight = 0
				}
				return style
			},
			radioStyle() {
				const style = {}
				if(this.parentData.borderBottom && this.parentData.placement === 'row') {
					uni.$u.error('检测到您将borderBottom设置为true，需要同时将u-radio-group的placement设置为column才有效')
				}
				// 当父组件设置了显示下边框并且排列形式为纵向时，给内容和边框之间加上一定间隔
				if(this.parentData.borderBottom && this.parentData.placement === 'column') {
					// ios像素密度高，需要多一点的距离
					style.paddingBottom = uni.$u.os() === 'ios' ? '12px' : '8px'
				}
				return uni.$u.deepMerge(style, uni.$u.addStyle(this.customStyle))
			}
		},
		mounted() {
			this.init()
		},
		methods: {
			init() {
				// 支付宝小程序不支持provide/inject，所以使用这个方法获取整个父组件，在created定义，避免循环引用
				this.updateParentData()
				if (!this.parent) {
					uni.$u.error('u-radio必须搭配u-radio-group组件使用')
				}
				// 设置初始化时，是否默认选中的状态
				this.checked = this.name === this.parentData.value
			},
			updateParentData() {
				this.getParentData('u-radio-group')
			},
			// 点击图标
			iconClickHandler(e) {
				this.preventEvent(e)
				// 如果整体被禁用，不允许被点击
				if (!this.elDisabled) {
					this.setRadioCheckedStatus()
				}
			},
			// 横向两端排列时，点击组件即可触发选中事件
			wrapperClickHandler(e) {
				this.parentData.iconPlacement === 'right' && this.iconClickHandler(e)
			},
			// 点击label
			labelClickHandler(e) {
				this.preventEvent(e)
				// 如果按钮整体被禁用或者label被禁用，则不允许点击文字修改状态
				if (!this.elLabelDisabled && !this.elDisabled) {
					this.setRadioCheckedStatus()
				}
			},
			emitEvent() {
				// u-radio的checked不为true时(意味着未选中)，才发出事件，避免多次点击触发事件
				if (!this.checked) {
					this.$emit('change', this.name)
					// 尝试调用u-form的验证方法，进行一定延迟，否则微信小程序更新可能会不及时
					this.$nextTick(() => {
						uni.$u.formValidate(this, 'change')
					})
				}
			},
			// 改变组件选中状态
			// 这里的改变的依据是，更改本组件的checked值为true，同时通过父组件遍历所有u-radio实例
			// 将本组件外的其他u-radio的checked都设置为false(都被取消选中状态)，因而只剩下一个为选中状态
			setRadioCheckedStatus() {
				this.emitEvent()
				// 将本组件标记为选中状态
				this.checked = true
				typeof this.parent.unCheckedOther === 'function' && this.parent.unCheckedOther(this)
			}
		}
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";
	$u-radio-wrap-margin-right:6px !default;
	$u-radio-wrap-font-size:20px !default;
	$u-radio-wrap-border-width:1px !default;
	$u-radio-wrap-border-color: #c8c9cc !default;
	$u-radio-line-height:0 !default;
	$u-radio-circle-border-radius:100% !default;
	$u-radio-square-border-radius:3px !default;
	$u-radio-checked-color:#fff !default;
	$u-radio-checked-background-color:red !default;
	$u-radio-checked-border-color: #2979ff !default;
	$u-radio-disabled-background-color:#ebedf0 !default;
	$u-radio-disabled--checked-color:#c8c9cc !default;
	$u-radio-label-margin-left: 5px !default;
	$u-radio-label-margin-right:12px !default;
	$u-radio-label-color:$u-content-color !default;
	$u-radio-label-font-size:15px !default;
	$u-radio-label-disabled-color:#c8c9cc !default;
	
	.u-radio {
		/* #ifndef APP-NVUE */
		@include flex(row);
		/* #endif */
		overflow: hidden;
		flex-direction: row;
		align-items: center;

		&-label--left {
			flex-direction: row
		}

		&-label--right {
			flex-direction: row-reverse;
			justify-content: space-between
		}

		&__icon-wrap {
			/* #ifndef APP-NVUE */
			box-sizing: border-box;
			// nvue下，border-color过渡有问题
			transition-property: border-color, background-color, color;
			transition-duration: 0.2s;
			/* #endif */
			color: $u-content-color;
			@include flex;
			align-items: center;
			justify-content: center;
			color: transparent;
			text-align: center;
			margin-right: $u-radio-wrap-margin-right;
			font-size: $u-radio-wrap-font-size;
			border-width: $u-radio-wrap-border-width;
			border-color: $u-radio-wrap-border-color;
			border-style: solid;

			/* #ifdef MP-TOUTIAO */
			// 头条小程序兼容性问题，需要设置行高为0，否则图标偏下
			&__icon {
				line-height: $u-radio-line-height;
			}

			/* #endif */

			&--circle {
				border-radius: $u-radio-circle-border-radius;
			}

			&--square {
				border-radius: $u-radio-square-border-radius;
			}

			&--checked {
				color: $u-radio-checked-color;
				background-color: $u-radio-checked-background-color;
				border-color: $u-radio-checked-border-color;
			}

			&--disabled {
				background-color: $u-radio-disabled-background-color !important;
			}

			&--disabled--checked {
				color: $u-radio-disabled--checked-color !important;
			}
		}

		&__label {
			/* #ifndef APP-NVUE */
			word-wrap: break-word;
			/* #endif */
			margin-left: $u-radio-label-margin-left;
			margin-right: $u-radio-label-margin-right;
			color: $u-radio-label-color;
			font-size: $u-radio-label-font-size;

			&--disabled {
				color: $u-radio-label-disabled-color;
			}
		}
	}
</style>
