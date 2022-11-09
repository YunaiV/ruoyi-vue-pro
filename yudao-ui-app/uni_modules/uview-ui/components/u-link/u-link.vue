<template>
	<text
	    class="u-link"
	    @tap.stop="openLink"
	    :style="[linkStyle, $u.addStyle(customStyle)]"
	>{{text}}</text>
</template>

<script>
	import props from './props.js';

	/**
	 * link 超链接
	 * @description 该组件为超链接组件，在不同平台有不同表现形式：在APP平台会通过plus环境打开内置浏览器，在小程序中把链接复制到粘贴板，同时提示信息，在H5中通过window.open打开链接。
	 * @tutorial https://www.uviewui.com/components/link.html
	 * @property {String}			color		文字颜色 （默认 color['u-primary'] ）
	 * @property {String ｜ Number}	fontSize	字体大小，单位px （默认 15 ）
	 * @property {Boolean}			underLine	是否显示下划线 （默认 false ）
	 * @property {String}			href		跳转的链接，要带上http(s)
	 * @property {String}			mpTips		各个小程序平台把链接复制到粘贴板后的提示语（默认“链接已复制，请在浏览器打开”）
	 * @property {String}			lineColor	下划线颜色，默认同color参数颜色 
	 * @property {String}			text		超链接的问题，不使用slot形式传入，是因为nvue下无法修改颜色 
	 * @property {Object}			customStyle	定义需要用到的外部样式
	 * 
	 * @example <u-link href="http://www.uviewui.com">蜀道难，难于上青天</u-link>
	 */
	export default {
		name: "u-link",
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		computed: {
			linkStyle() {
				const style = {
					color: this.color,
					fontSize: uni.$u.addUnit(this.fontSize),
					// line-height设置为比字体大小多2px
					lineHeight: uni.$u.addUnit(uni.$u.getPx(this.fontSize) + 2),
					textDecoration: this.underLine ? 'underline' : 'none'
				}
				// if (this.underLine) {
				// 	style.borderBottomColor = this.lineColor || this.color
				// 	style.borderBottomWidth = '1px'
				// }
				return style
			}
		},
		methods: {
			openLink() {
				// #ifdef APP-PLUS
				plus.runtime.openURL(this.href)
				// #endif
				// #ifdef H5
				window.open(this.href)
				// #endif
				// #ifdef MP
				uni.setClipboardData({
					data: this.href,
					success: () => {
						uni.hideToast();
						this.$nextTick(() => {
							uni.$u.toast(this.mpTips);
						})
					}
				});
				// #endif
				this.$emit('click')
			}
		}
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";
	$u-link-line-height:1 !default;

	.u-link {
		/* #ifndef APP-NVUE */
		line-height: $u-link-line-height;
		/* #endif */
		@include flex;
		flex-wrap: wrap;
		flex: 1;
	}
</style>
