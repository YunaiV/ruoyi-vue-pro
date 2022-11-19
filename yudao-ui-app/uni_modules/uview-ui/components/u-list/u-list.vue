<template>
	<!-- #ifdef APP-NVUE -->
	<list
		class="u-list"
		:enableBackToTop="enableBackToTop"
		:loadmoreoffset="lowerThreshold"
		:showScrollbar="showScrollbar"
		:style="[listStyle]"
		:offset-accuracy="Number(offsetAccuracy)"
		@scroll="onScroll"
		@loadmore="scrolltolower"
	>
		<slot />
	</list>
	<!-- #endif -->
	<!-- #ifndef APP-NVUE -->
	<scroll-view
		class="u-list"
		:scroll-into-view="scrollIntoView"
		:style="[listStyle]"
		scroll-y
		:scroll-top="Number(scrollTop)"
		:lower-threshold="Number(lowerThreshold)"
		:upper-threshold="Number(upperThreshold)"
		:show-scrollbar="showScrollbar"
		:enable-back-to-top="enableBackToTop"
		:scroll-with-animation="scrollWithAnimation"
		@scroll="onScroll"
		@scrolltolower="scrolltolower"
		@scrolltoupper="scrolltoupper"
	>
		<view>
			<slot />
		</view>
	</scroll-view>
	<!-- #endif -->
</template>

<script>
	import props from './props.js';
	// #ifdef APP-NVUE
	const dom = uni.requireNativePlugin('dom')
	// #endif
	/**
	 * List 列表
	 * @description 该组件为高性能列表组件
	 * @tutorial https://www.uviewui.com/components/list.html
	 * @property {Boolean}			showScrollbar		控制是否出现滚动条，仅nvue有效 （默认 false ）
	 * @property {String ｜ Number}	lowerThreshold		距底部多少时触发scrolltolower事件 （默认 50 ）
	 * @property {String ｜ Number}	upperThreshold		距顶部多少时触发scrolltoupper事件，非nvue有效 （默认 0 ）
	 * @property {String ｜ Number}	scrollTop			设置竖向滚动条位置（默认 0 ）
	 * @property {String ｜ Number}	offsetAccuracy		控制 onscroll 事件触发的频率，仅nvue有效（默认 10 ）
	 * @property {Boolean}			enableFlex			启用 flexbox 布局。开启后，当前节点声明了display: flex就会成为flex container，并作用于其孩子节点，仅微信小程序有效（默认 false ）
	 * @property {Boolean}			pagingEnabled		是否按分页模式显示List，（默认 false ）
	 * @property {Boolean}			scrollable			是否允许List滚动（默认 true ）
	 * @property {String}			scrollIntoView		值应为某子元素id（id不能以数字开头）
	 * @property {Boolean}			scrollWithAnimation	在设置滚动条位置时使用动画过渡 （默认 false ）
	 * @property {Boolean}			enableBackToTop		iOS点击顶部状态栏、安卓双击标题栏时，滚动条返回顶部，只对微信小程序有效 （默认 false ）
	 * @property {String ｜ Number}	height				列表的高度 （默认 0 ）
	 * @property {String ｜ Number}	width				列表宽度 （默认 0 ）
	 * @property {String ｜ Number}	preLoadScreen		列表前后预渲染的屏数，1代表一个屏幕的高度，1.5代表1个半屏幕高度  （默认 1 ）
	 * @property {Object}			customStyle			定义需要用到的外部样式
	 *
	 * @example <u-list @scrolltolower="scrolltolower"></u-list>
	 */
	export default {
		name: 'u-list',
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		watch: {
			scrollIntoView(n) {
				this.scrollIntoViewById(n)
			}
		},
		data() {
			return {
				// 记录内部滚动的距离
				innerScrollTop: 0,
				// vue下，scroll-view在上拉加载时的偏移值
				offset: 0,
				sys: uni.$u.sys()
			}
		},
		computed: {
			listStyle() {
				const style = {},
					addUnit = uni.$u.addUnit
				if (this.width != 0) style.width = addUnit(this.width)
				if (this.height != 0) style.height = addUnit(this.height)
				// 如果没有定义列表高度，则默认使用屏幕高度
				if (!style.height) style.height = addUnit(this.sys.windowHeight, 'px')
				return uni.$u.deepMerge(style, uni.$u.addStyle(this.customStyle))
			}
		},
		provide() {
			return {
				uList: this
			}
		},
		created() {
			this.refs = []
			this.children = []
			this.anchors = []
		},
		mounted() {},
		methods: {
			updateOffsetFromChild(top) {
				this.offset = top
			},
			onScroll(e) {
				let scrollTop = 0
				// #ifdef APP-NVUE
				scrollTop = e.contentOffset.y
				// #endif
				// #ifndef APP-NVUE
				scrollTop = e.detail.scrollTop
				// #endif
				this.innerScrollTop = scrollTop
				this.$emit('scroll', Math.abs(scrollTop))
			},
			scrollIntoViewById(id) {
				// #ifdef APP-NVUE
				// 根据id参数，找到所有u-list-item中匹配的节点，再通过dom模块滚动到对应的位置
				const item = this.refs.find(item => item.$refs[id] ? true : false)
				dom.scrollToElement(item.$refs[id], {
					// 是否需要滚动动画
					animated: this.scrollWithAnimation
				})
				// #endif
			},
			// 滚动到底部触发事件
			scrolltolower(e) {
				uni.$u.sleep(30).then(() => {
					this.$emit('scrolltolower')
				})
			},
			// #ifndef APP-NVUE
			// 滚动到底部时触发，非nvue有效
			scrolltoupper(e) {
				uni.$u.sleep(30).then(() => {
					this.$emit('scrolltoupper')
					// 这一句很重要，能绝对保证在性功能障碍的webview，滚动条到顶时，取消偏移值，让页面置顶
					this.offset = 0
				})
			}
			// #endif
		},
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-list {
		@include flex(column);

	}
</style>
