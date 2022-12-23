<template>
	<view class="u-gap" :style="[gapStyle]"></view>
</template>

<script>
	import props from './props.js';
	/**
	 * gap 间隔槽
	 * @description 该组件一般用于内容块之间的用一个灰色块隔开的场景，方便用户风格统一，减少工作量
	 * @tutorial https://www.uviewui.com/components/gap.html
	 * @property {String}			bgColor			背景颜色 （默认 'transparent' ）
	 * @property {String | Number}	height			分割槽高度，单位px （默认 20 ）
	 * @property {String | Number}	marginTop		与前一个组件的距离，单位px（ 默认 0 ）
	 * @property {String | Number}	marginBottom	与后一个组件的距离，单位px （默认 0 ）
	 * @property {Object}			customStyle		定义需要用到的外部样式
	 * 
	 * @example <u-gap height="80" bg-color="#bbb"></u-gap>
	 */
	export default {
		name: "u-gap",
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		computed: {
			gapStyle() {
				const style = {
					backgroundColor: this.bgColor,
					height: uni.$u.addUnit(this.height),
					marginTop: uni.$u.addUnit(this.marginTop),
					marginBottom: uni.$u.addUnit(this.marginBottom),
				}
				return uni.$u.deepMerge(style, uni.$u.addStyle(this.customStyle))
			}
		}
	};
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";
</style>
