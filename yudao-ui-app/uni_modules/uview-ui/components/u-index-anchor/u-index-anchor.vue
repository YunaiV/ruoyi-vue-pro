<template>
	<!-- #ifdef APP-NVUE -->
	<header>
	<!-- #endif -->
	<view
	    class="u-index-anchor u-border-bottom"
		:ref="`u-index-anchor-${text}`"
	    :style="{
			height: $u.addUnit(height),
			backgroundColor: bgColor
		}"
	>
		<text
		    class="u-index-anchor__text"
		    :style="{
				fontSize: $u.addUnit(size),
				color: color
			}"
		>{{ text }}</text>
	</view>
	<!-- #ifdef APP-NVUE -->
	</header>
	<!-- #endif -->
</template>

<script>
	import props from './props.js';
	// #ifdef APP-NVUE
	const dom = uni.requireNativePlugin('dom')
	// #endif
	/**
	 * IndexAnchor 列表锚点
	 * @description 
	 * @tutorial https://uviewui.com/components/indexList.html
	 * @property {String | Number}	text	列表锚点文本内容
	 * @property {String}			color	列表锚点文字颜色 ( 默认 '#606266' )
	 * @property {String | Number}	size	列表锚点文字大小，单位默认px ( 默认 14 )
	 * @property {String}			bgColor	列表锚点背景颜色 ( 默认 '#dedede' )
	 * @property {String | Number}	height	列表锚点高度，单位默认px ( 默认 32 )
	 * @example <u-index-anchor :text="indexList[index]"></u-index-anchor>
	 */
	export default {
		name: 'u-index-anchor',
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		data() {
			return {
			}
		},
		mounted() {
			this.init()
		},
		methods: {
			init() {
				// 此处会活动父组件实例，并赋值给实例的parent属性
				const indexList = uni.$u.$parent.call(this, 'u-index-list')
				if (!indexList) { 
					return uni.$u.error('u-index-anchor必须要搭配u-index-list组件使用')
				}
				// 将当前实例放入到u-index-list中
				indexList.anchors.push(this)
				const indexListItem = uni.$u.$parent.call(this, 'u-index-item')
				// #ifndef APP-NVUE
				// 只有在非nvue下，u-index-anchor才是嵌套在u-index-item中的
				if (!indexListItem) {
					return uni.$u.error('u-index-anchor必须要搭配u-index-item组件使用')
				}
				// 设置u-index-item的id为anchor的text标识符，因为非nvue下滚动列表需要依赖scroll-view滚动到元素的特性
				indexListItem.id = this.text.charCodeAt(0)
				// #endif
			}
		},
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-index-anchor {
		position: sticky;
		top: 0;
		@include flex;
		align-items: center;
		padding-left: 15px;
		z-index: 1;

		&__text {
			@include flex;
			align-items: center;
		}
	}
</style>
