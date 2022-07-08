<template>
	<u-transition
	    :show="show"
	    custom-class="u-overlay"
	    :duration="duration"
	    :custom-style="overlayStyle"
	    @click="clickHandler"
	>
		<slot />
	</u-transition>
</template>

<script>
	import props from './props.js';

	/**
	 * overlay 遮罩
	 * @description 创建一个遮罩层，用于强调特定的页面元素，并阻止用户对遮罩下层的内容进行操作，一般用于弹窗场景
	 * @tutorial https://www.uviewui.com/components/overlay.html
	 * @property {Boolean}			show		是否显示遮罩（默认 false ）
	 * @property {String | Number}	zIndex		zIndex 层级（默认 10070 ）
	 * @property {String | Number}	duration	动画时长，单位毫秒（默认 300 ）
	 * @property {String | Number}	opacity		不透明度值，当做rgba的第四个参数 （默认 0.5 ）
	 * @property {Object}			customStyle	定义需要用到的外部样式
	 * @event {Function} click 点击遮罩发送事件
	 * @example <u-overlay :show="show" @click="show = false"></u-overlay>
	 */
	export default {
		name: "u-overlay",
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		computed: {
			overlayStyle() {
				const style = {
					position: 'fixed',
					top: 0,
					left: 0,
					right: 0,
					zIndex: this.zIndex,
					bottom: 0,
					'background-color': `rgba(0, 0, 0, ${this.opacity})`
				}
				return uni.$u.deepMerge(style, uni.$u.addStyle(this.customStyle))
			}
		},
		methods: {
			clickHandler() {
				this.$emit('click')
			}
		}
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";
     $u-overlay-top:0 !default;
     $u-overlay-left:0 !default;
     $u-overlay-width:100% !default;
     $u-overlay-height:100% !default;
     $u-overlay-background-color:rgba(0, 0, 0, .7) !default;
	.u-overlay {
		position: fixed;
		top:$u-overlay-top;
		left:$u-overlay-left;
		width: $u-overlay-width;
		height:$u-overlay-height;
		background-color:$u-overlay-background-color;
	}
</style>
