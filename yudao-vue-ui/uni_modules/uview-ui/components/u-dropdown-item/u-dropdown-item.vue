<template>
	<view class="u-drawdown-item">
		<u-overlay
			customStyle="top: 126px"
			:show="show"
			:closeOnClickOverlay="closeOnClickOverlay"
			@click="overlayClick"
		></u-overlay>
		<view
			class="u-drawdown-item__content"
			:style="[style]"
			:animation="animationData"
			ref="animation"
		>
			<slot />
		</view>
	</view>
</template>

<script>
	// #ifdef APP-NVUE
	const animation = uni.requireNativePlugin('animation')
	const dom = uni.requireNativePlugin('dom')
	// #endif
	import props from './props.js';
	/**
	 * Drawdownitem
	 * @description 
	 * @tutorial url
	 * @property {String}
	 * @event {Function}
	 * @example
	 */
	export default {
		name: 'u-drawdown-item',
		mixins: [uni.$u.mpMixin, uni.$u.mixin, props],
		data() {
			return {
				show: false,
				top: '126px',
				// uni.createAnimation的导出数据
				animationData: {},
			}
		},
		mounted() {
			this.init()
		},
		watch: {
			// 发生变化时，需要去更新父组件对应的值
			dataChange(newValue, oldValue) {
				this.updateParentData()
			}
		},
		computed: {
			// 监听对应变量的变化
			dataChange() {
				return [this.title, this.disabled]
			},
			style() {
				const style = {
					zIndex: 10071,
					position: 'fixed',
					display: 'flex',
					left: 0,
					right: 0
				}
				style.top = uni.$u.addUnit(this.top)
				return style
			}
		},
		methods: {
			init() {
				this.updateParentData()
			},
			// 更新父组件所需的数据
			updateParentData() {
				// 获取父组件u-dropdown
				this.getParentData('u-dropdown')
				if (!this.parent) uni.$u.error('u-dropdown-item必须配合u-dropdown使用')
				// 查找父组件menuList数组中对应的标题数据
				const menuIndex = this.parent.menuList.findIndex(item => item.title === this.title)
				const menuContent = {
					title: this.title,
					disabled: this.disabled
				}
				if (menuIndex >= 0) {
					// 如果能找到，则直接修改
					this.parent.menuList[menuIndex] = menuContent;
				} else {
					// 如果无法找到，则为第一次添加，直接push即可
					this.parent.menuList.push(menuContent);
				}
			},
			async setContentAnimate(height) {
				this.animating = true
				// #ifdef APP-NVUE
				const ref = this.$refs['animation'].ref
				animation.transition(ref, {
					styles: {
						height: uni.$u.addUnit(height)
					},
					duration: this.duration,
					timingFunction: 'ease-in-out',
				}, () => {
					this.animating = false
				})
				// #endif
			
				// #ifndef APP-NVUE
				const animation = uni.createAnimation({
					timingFunction: 'ease-in-out',
				});
				animation
					.height(height)
					.step({
						duration: this.duration,
					})
					.step()
				// 导出动画数据给面板的animationData值
				this.animationData = animation.export()
				// 标识动画结束
				uni.$u.sleep(this.duration).then(() => {
					this.animating = false
				})
				// #endif
			},
			overlayClick() {
				this.show = false
				this.setContentAnimate(0)
			}
		},
	}
</script>

<style lang="scss" scoped>
	@import '../../libs/css/components.scss';
	
	.u-drawdown-item {
		
		&__content {
			background-color: #FFFFFF;
			overflow: hidden;
			height: 0;
		}
	}
</style>
