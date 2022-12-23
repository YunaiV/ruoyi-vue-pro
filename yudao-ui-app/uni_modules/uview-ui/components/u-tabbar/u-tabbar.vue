<template>
	<view class="u-tabbar">
		<view
		    class="u-tabbar__content"
		    ref="u-tabbar__content"
		    @touchmove.stop.prevent="noop"
		    :class="[border && 'u-border-top', fixed && 'u-tabbar--fixed']"
		    :style="[tabbarStyle]"
		>
			<view class="u-tabbar__content__item-wrapper">
				<slot />
			</view>
			<u-safe-bottom v-if="safeAreaInsetBottom"></u-safe-bottom>
		</view>
		<view
		    class="u-tabbar__placeholder"
			v-if="placeholder"
		    :style="{
				height: placeholderHeight + 'px',
			}"
		></view>
	</view>
</template>

<script>
	import props from './props.js';
	// #ifdef APP-NVUE
	const dom = uni.requireNativePlugin('dom')
	// #endif
	/**
	 * Tabbar 底部导航栏
	 * @description 此组件提供了自定义tabbar的能力。
	 * @tutorial https://www.uviewui.com/components/tabbar.html
	 * @property {String | Number}	value				当前匹配项的name
	 * @property {Boolean}			safeAreaInsetBottom	是否为iPhoneX留出底部安全距离（默认 true ）
	 * @property {Boolean}			border				是否显示上方边框（默认 true ）
	 * @property {String | Number}	zIndex				元素层级z-index（默认 1 ）
	 * @property {String}			activeColor			选中标签的颜色（默认 '#1989fa' ）
	 * @property {String}			inactiveColor		未选中标签的颜色（默认 '#7d7e80' ）
	 * @property {Boolean}			fixed				是否固定在底部（默认 true ）
	 * @property {Boolean}			placeholder			fixed定位固定在底部时，是否生成一个等高元素防止塌陷（默认 true ）
	 * @property {Object}			customStyle			定义需要用到的外部样式
	 * 
	 * @example <u-tabbar :value="value2" :placeholder="false" @change="name => value2 = name" :fixed="false" :safeAreaInsetBottom="false"><u-tabbar-item text="首页" icon="home" dot ></u-tabbar-item></u-tabbar>
	 */
	export default {
		name: 'u-tabbar',
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		data() {
			return {
				placeholderHeight: 0
			}
		},
		computed: {
			tabbarStyle() {
				const style = {
					zIndex: this.zIndex
				}
				// 合并来自父组件的customStyle样式
				return uni.$u.deepMerge(style, uni.$u.addStyle(this.customStyle))
			},
			// 监听多个参数的变化，通过在computed执行对应的操作
			updateChild() {
				return [this.value, this.activeColor, this.inactiveColor]
			},
			updatePlaceholder() {
				return [this.fixed, this.placeholder]
			}
		},
		watch: {
			updateChild() {
				// 如果updateChildren中的元素发生了变化，则执行子元素初始化操作
				this.updateChildren()
			},
			updatePlaceholder() {
				// 如果fixed，placeholder等参数发生变化，重新计算占位元素的高度
				this.setPlaceholderHeight()
			}
		},
		created() {
			this.children = []
		},
		mounted() {
			this.setPlaceholderHeight()
		},
		methods: {
			updateChildren() {
				// 如果存在子元素，则执行子元素的updateFromParent进行更新数据
				this.children.length && this.children.map(child => child.updateFromParent())
			},
			// 设置用于防止塌陷元素的高度
			async setPlaceholderHeight() {
				if (!this.fixed || !this.placeholder) return
				// 延时一定时间
				await uni.$u.sleep(20)
				// #ifndef APP-NVUE
				this.$uGetRect('.u-tabbar__content').then(({height = 50}) => {
					// 修复IOS safearea bottom 未填充高度
					this.placeholderHeight = height
				})
				// #endif

				// #ifdef APP-NVUE
				dom.getComponentRect(this.$refs['u-tabbar__content'], (res) => {
					const {
						size
					} = res
					this.placeholderHeight = size.height
				})
				// #endif
			}
		}
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-tabbar {
		@include flex(column);
		flex: 1;
		justify-content: center;
		
		&__content {
			@include flex(column);
			background-color: #fff;
			
			&__item-wrapper {
				height: 50px;
				@include flex(row);
			}
		}

		&--fixed {
			position: fixed;
			bottom: 0;
			left: 0;
			right: 0;
		}
	}
</style>
