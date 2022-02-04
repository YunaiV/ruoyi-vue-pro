<template>
	<text
		v-if="show && ((Number(value) === 0 ? showZero : true) || isDot)"
		:class="[isDot ? 'u-badge--dot' : 'u-badge--not-dot', inverted && 'u-badge--inverted', shape === 'horn' && 'u-badge--horn', `u-badge--${type}${inverted ? '--inverted' : ''}`]"
		:style="[$u.addStyle(customStyle), badgeStyle]"
		class="u-badge"
	>{{ isDot ? '' :showValue }}</text>
</template>

<script>
	import props from './props.js';
	/**
	 * badge 徽标数
	 * @description 该组件一般用于图标右上角显示未读的消息数量，提示用户点击，有圆点和圆包含文字两种形式。
	 * @tutorial https://uviewui.com/components/badge.html
	 * 
	 * @property {Boolean} 			isDot 		是否显示圆点 （默认 false ）
	 * @property {String | Number} 	value 		显示的内容
	 * @property {Boolean} 			show 		是否显示 （默认 true ）
	 * @property {String | Number} 	max 		最大值，超过最大值会显示 '{max}+'  （默认999）
	 * @property {String} 			type 		主题类型，error|warning|success|primary （默认 'error' ）
	 * @property {Boolean} 			showZero	当数值为 0 时，是否展示 Badge （默认 false ）
	 * @property {String} 			bgColor 	背景颜色，优先级比type高，如设置，type参数会失效
	 * @property {String} 			color 		字体颜色 （默认 '#ffffff' ）
	 * @property {String} 			shape 		徽标形状，circle-四角均为圆角，horn-左下角为直角 （默认 'circle' ）
	 * @property {String} 			numberType	设置数字的显示方式，overflow|ellipsis|limit  （默认 'overflow' ）
	 * @property {Array}} 			offset		设置badge的位置偏移，格式为 [x, y]，也即设置的为top和right的值，absolute为true时有效
	 * @property {Boolean} 			inverted	是否反转背景和字体颜色（默认 false ）
	 * @property {Boolean} 			absolute	是否绝对定位（默认 false ）
	 * @property {Object}			customStyle	定义需要用到的外部样式
	 * @example <u-badge :type="type" :count="count"></u-badge>
	 */
	export default {
		name: 'u-badge',
		mixins: [uni.$u.mpMixin, props, uni.$u.mixin],
		computed: {
			// 是否将badge中心与父组件右上角重合
			boxStyle() {
				let style = {};
				return style;
			},
			// 整个组件的样式
			badgeStyle() {
				const style = {}
				if(this.color) {
					style.color = this.color
				}
				if (this.bgColor && !this.inverted) {
					style.backgroundColor = this.bgColor
				}
				if (this.absolute) {
					style.position = 'absolute'
					// 如果有设置offset参数
					if(this.offset.length) {
						// top和right分为为offset的第一个和第二个值，如果没有第二个值，则right等于top
						const top = this.offset[0]
						const right = this.offset[1] || top
						style.top = uni.$u.addUnit(top)
						style.right = uni.$u.addUnit(right)
					}
				}
				return style
			},
			showValue() {
				switch (this.numberType) {
					case "overflow":
						return Number(this.value) > Number(this.max) ? this.max + "+" : this.value
						break;
					case "ellipsis":
						return Number(this.value) > Number(this.max) ? "..." : this.value
						break;
					case "limit":
						return Number(this.value) > 999 ? Number(this.value) >= 9999 ?
							Math.floor(this.value / 1e4 * 100) / 100 + "w" : Math.floor(this.value /
								1e3 * 100) / 100 + "k" : this.value
						break;
					default:
						return Number(this.value)
				}
			},
		}
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	$u-badge-primary: $u-primary !default;
	$u-badge-error: $u-error !default;
	$u-badge-success: $u-success !default;
	$u-badge-info: $u-info !default;
	$u-badge-warning: $u-warning !default;
	$u-badge-dot-radius: 100px !default;
	$u-badge-dot-size: 8px !default;
	$u-badge-dot-right: 4px !default;
	$u-badge-dot-top: 0 !default;
	$u-badge-text-font-size: 11px !default;
	$u-badge-text-right: 10px !default;
	$u-badge-text-padding: 2px 5px !default;
	$u-badge-text-align: center !default;
	$u-badge-text-color: #FFFFFF !default;

	.u-badge {
		border-top-right-radius: $u-badge-dot-radius;
		border-top-left-radius: $u-badge-dot-radius;
		border-bottom-left-radius: $u-badge-dot-radius;
		border-bottom-right-radius: $u-badge-dot-radius;
		@include flex;
		line-height: $u-badge-text-font-size;
		text-align: $u-badge-text-align;
		font-size: $u-badge-text-font-size;
		color: $u-badge-text-color;

		&--dot {
			height: $u-badge-dot-size;
			width: $u-badge-dot-size;
		}
		
		&--inverted {
			font-size: 13px;
		}
		
		&--not-dot {
			padding: $u-badge-text-padding;
		}

		&--horn {
			border-bottom-left-radius: 0;
		}

		&--primary {
			background-color: $u-badge-primary;
		}
		
		&--primary--inverted {
			color: $u-badge-primary;
		}

		&--error {
			background-color: $u-badge-error;
		}
		
		&--error--inverted {
			color: $u-badge-error;
		}

		&--success {
			background-color: $u-badge-success;
		}
		
		&--success--inverted {
			color: $u-badge-success;
		}

		&--info {
			background-color: $u-badge-info;
		}
		
		&--info--inverted {
			color: $u-badge-info;
		}

		&--warning {
			background-color: $u-badge-warning;
		}
		
		&--warning--inverted {
			color: $u-badge-warning;
		}
	}
</style>
