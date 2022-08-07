<template>
	<view
	    class="u-divider"
	    :style="[$u.addStyle(customStyle)]"
		@tap="click"
	>
		<u-line
		    :color="lineColor"
		    :customStyle="leftLineStyle"
		    :hairline="hairline"
			:dashed="dashed"
		></u-line>
		<text
		    v-if="dot"
		    class="u-divider__dot"
		>●</text>
		<text
		    v-else-if="text"
		    class="u-divider__text"
		    :style="[textStyle]"
		>{{text}}</text>
		<u-line
		    :color="lineColor"
		    :customStyle="rightLineStyle"
		    :hairline="hairline"
			:dashed="dashed"
		></u-line>
	</view>
</template>

<script>
	import props from './props.js';
	/**
	 * divider 分割线
	 * @description 区隔内容的分割线，一般用于页面底部"没有更多"的提示。
	 * @tutorial https://www.uviewui.com/components/divider.html
	 * @property {Boolean}			dashed			是否虚线 （默认 false ）
	 * @property {Boolean}			hairline		是否细线 （默认  true ）
	 * @property {Boolean}			dot				是否以点替代文字，优先于text字段起作用 （默认 false ）
	 * @property {String}			textPosition	内容文本的位置，left-左边，center-中间，right-右边 （默认 'center' ）
	 * @property {String | Number}	text			文本内容
	 * @property {String | Number}	textSize		文本大小 （默认 14）
	 * @property {String}			textColor		文本颜色 （默认 '#909399' ）
	 * @property {String}			lineColor		线条颜色 （默认 '#dcdfe6' ）
	 * @property {Object}			customStyle		定义需要用到的外部样式
	 *
	 * @event {Function}	click	divider组件被点击时触发
	 * @example <u-divider :color="color">锦瑟无端五十弦</u-divider>
	 */
	export default {
		name:'u-divider',
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		computed: {
			textStyle() {
				const style = {}
				style.fontSize = uni.$u.addUnit(this.textSize)
				style.color = this.textColor
				return style
			},
			// 左边线条的的样式
			leftLineStyle() {
				const style = {}
				// 如果是在左边，设置左边的宽度为固定值
				if (this.textPosition === 'left') {
					style.width = '80rpx'
				} else {
					style.flex = 1
				}
				return style
			},
			// 右边线条的的样式
			rightLineStyle() {
				const style = {}
				// 如果是在右边，设置右边的宽度为固定值
				if (this.textPosition === 'right') {
					style.width = '80rpx'
				} else {
					style.flex = 1
				}
				return style
			}
		},
		methods: {
			// divider组件被点击时触发
			click() {
				this.$emit('click');
			}
		}
	}
</script>

<style lang="scss" scoped>
	@import '../../libs/css/components.scss';
	$u-divider-margin:15px 0 !default;
	$u-divider-text-margin:0 15px !default;
	$u-divider-dot-font-size:12px !default;
	$u-divider-dot-margin:0 12px !default;
	$u-divider-dot-color: #c0c4cc !default;

	.u-divider {
		@include flex;
		flex-direction: row;
		align-items: center;
		margin: $u-divider-margin;

		&__text {
			margin: $u-divider-text-margin;
		}

		&__dot {
			font-size: $u-divider-dot-font-size;
			margin: $u-divider-dot-margin;
			color: $u-divider-dot-color;
		}
	}
</style>
