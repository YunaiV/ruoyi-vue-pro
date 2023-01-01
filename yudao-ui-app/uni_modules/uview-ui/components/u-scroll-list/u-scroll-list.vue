<template>
	<view
		class="u-scroll-list"
		ref="u-scroll-list"
	>
		<!-- #ifdef APP-NVUE -->
		<!-- nvue使用bindingX实现，以得到更好的性能 -->
		<scroller
			class="u-scroll-list__scroll-view"
			ref="u-scroll-list__scroll-view"
			scroll-direction="horizontal"
			:show-scrollbar="false"
			:offset-accuracy="1"
			@scroll="nvueScrollHandler"
		>
			<view class="u-scroll-list__scroll-view__content">
				<slot />
			</view>
		</scroller>
		<!-- #endif -->
		<!-- #ifndef APP-NVUE -->
		<!-- #ifdef MP-WEIXIN || APP-VUE || H5 || MP-QQ -->
		<!-- 以上平台，支持wxs -->
		<scroll-view
			class="u-scroll-list__scroll-view"
			scroll-x
			@scroll="wxs.scroll"
			@scrolltoupper="wxs.scrolltoupper"
			@scrolltolower="wxs.scrolltolower"
			:data-scrollWidth="scrollWidth"
			:data-barWidth="$u.getPx(indicatorBarWidth)"
			:data-indicatorWidth="$u.getPx(indicatorWidth)"
			:show-scrollbar="false"
			:upper-threshold="0"
			:lower-threshold="0"
		>
			<!-- #endif -->
			<!-- #ifndef APP-NVUE || MP-WEIXIN || H5 || APP-VUE || MP-QQ -->
			<!-- 非以上平台，只能使用普通js实现 -->
			<scroll-view
				class="u-scroll-list__scroll-view"
				scroll-x
				@scroll="scrollHandler"
				@scrolltoupper="scrolltoupperHandler"
				@scrolltolower="scrolltolowerHandler"
				:show-scrollbar="false"
				:upper-threshold="0"
				:lower-threshold="0"
			>
				<!-- #endif -->
				<view class="u-scroll-list__scroll-view__content">
					<slot />
				</view>
			</scroll-view>
			<!-- #endif -->
			<view
				class="u-scroll-list__indicator"
				v-if="indicator"
				:style="[$u.addStyle(indicatorStyle)]"
			>
				<view
					class="u-scroll-list__indicator__line"
					:style="[lineStyle]"
				>
					<view
						class="u-scroll-list__indicator__line__bar"
						:style="[barStyle]"
						ref="u-scroll-list__indicator__line__bar"
					></view>
				</view>
			</view>
	</view>
</template>

<script
	src="./scrollWxs.wxs"
	module="wxs"
	lang="wxs"
></script>

<script>
/**
 * scrollList 横向滚动列表
 * @description 该组件一般用于同时展示多个商品、分类的场景，也可以完成左右滑动的列表。
 * @tutorial https://www.uviewui.com/components/scrollList.html
 * @property {String | Number}	indicatorWidth			指示器的整体宽度 (默认 50 )
 * @property {String | Number}	indicatorBarWidth		滑块的宽度 (默认 20 )
 * @property {Boolean}			indicator				是否显示面板指示器 (默认 true )
 * @property {String}			indicatorColor			指示器非激活颜色 (默认 '#f2f2f2' )
 * @property {String}			indicatorActiveColor	指示器的激活颜色 (默认 '#3c9cff' )
 * @property {String | Object}	indicatorStyle			指示器样式，可通过bottom，left，right进行定位
 * @event {Function} left	滑动到左边时触发
 * @event {Function} right	滑动到右边时触发
 * @example
 */
// #ifdef APP-NVUE
const dom = uni.requireNativePlugin('dom')
import nvueMixin from "./nvue.js"
// #endif
import props from './props.js';
export default {
	name: 'u-scroll-list',
	mixins: [uni.$u.mpMixin, uni.$u.mixin, props],
	// #ifdef APP-NVUE
	mixins: [uni.$u.mpMixin, uni.$u.mixin, nvueMixin, props],
	// #endif
	data() {
		return {
			scrollInfo: {
				scrollLeft: 0,
				scrollWidth: 0
			},
			scrollWidth: 0
		}
	},
	computed: {
		// 指示器为线型的样式
		barStyle() {
			const style = {}
			// #ifndef APP-NVUE || MP-WEIXIN || H5 || APP-VUE || MP-QQ
			// 此为普通js方案，只有在非nvue和不支持wxs方案的端才使用、
			// 此处的计算理由为：scroll-view的滚动距离与目标滚动距离(scroll-view的实际宽度减去包裹元素的宽度)之比，等于滑块当前移动距离与总需
			// 滑动距离(指示器的总宽度减去滑块宽度)的比值
			const scrollLeft = this.scrollInfo.scrollLeft,
				scrollWidth = this.scrollInfo.scrollWidth,
				barAllMoveWidth = this.indicatorWidth - this.indicatorBarWidth
			const x = scrollLeft / (scrollWidth - this.scrollWidth) * barAllMoveWidth
			style.transform = `translateX(${ x }px)`
			// #endif
			// 设置滑块的宽度和背景色，是每个平台都需要的
			style.width = uni.$u.addUnit(this.indicatorBarWidth)
			style.backgroundColor = this.indicatorActiveColor
			return style
		},
		lineStyle() {
			const style = {}
			// 指示器整体的样式，需要设置其宽度和背景色
			style.width = uni.$u.addUnit(this.indicatorWidth)
			style.backgroundColor = this.indicatorColor
			return style
		}
	},
	mounted() {
		this.init()
	},
	methods: {
		init() {
			this.getComponentWidth()
		},
		// #ifndef APP-NVUE || MP-WEIXIN || H5 || APP-VUE || MP-QQ
		// scroll-view触发滚动事件
		scrollHandler(e) {
			this.scrollInfo = e.detail
		},
		scrolltoupperHandler() {
			this.scrollEvent('left')
			this.scrollInfo.scrollLeft = 0
		},
		scrolltolowerHandler() {
			this.scrollEvent('right')
			// 在普通js方案中，滚动到右边时，通过设置this.scrollInfo，模拟出滚动到右边的情况
			// 因为上方是用过computed计算的，设置后，会自动调整滑块的位置
			this.scrollInfo.scrollLeft = uni.$u.getPx(this.indicatorWidth) - uni.$u.getPx(this.indicatorBarWidth)
		},
		// #endif
		//
		scrollEvent(status) {
			this.$emit(status)
		},
		// 获取组件的宽度
		async getComponentWidth() {
			// 延时一定时间，以获取dom尺寸
			await uni.$u.sleep(30)
			// #ifndef APP-NVUE
			this.$uGetRect('.u-scroll-list').then(size => {
				this.scrollWidth = size.width
			})
			// #endif

			// #ifdef APP-NVUE
			const ref = this.$refs['u-scroll-list']
			ref && dom.getComponentRect(ref, (res) => {
				this.scrollWidth = res.size.width
			})
			// #endif
		},
	}
}
</script>

<style lang="scss" scoped>
@import "../../libs/css/components.scss";

.u-scroll-list {
	padding-bottom: 10px;

	&__scroll-view {
		@include flex;

		&__content {
			@include flex;
		}
	}

	&__indicator {
		@include flex;
		justify-content: center;
		margin-top: 15px;

		&__line {
			width: 60px;
			height: 4px;
			border-radius: 100px;
			overflow: hidden;

			&__bar {
				width: 20px;
				height: 4px;
				border-radius: 100px;
			}
		}
	}
}
</style>
