<template>
	<view class="u-swipe-action">
		<slot></slot>
	</view>
</template>

<script>
	import props from './props.js';
	/**
	 * SwipeAction 滑动单元格 
	 * @description 该组件一般用于左滑唤出操作菜单的场景，用的最多的是左滑删除操作
	 * @tutorial https://www.uviewui.com/components/swipeAction.html
	 * @property {Boolean}	autoClose	是否自动关闭其他swipe按钮组
	 * @event {Function(index)}	click	点击组件时触发
	 * @example	<u-swipe-action><u-swipe-action-item :rightOptions="options1" ></u-swipe-action-item></u-swipe-action>
	 */
	export default {
		name: 'u-swipe-action',
		mixins: [uni.$u.mpMixin, uni.$u.mixin, props],
		data() {
			return {}
		},
		provide() {
			return {
				swipeAction: this
			}
		},
		computed: {
			// 这里computed的变量，都是子组件u-swipe-action-item需要用到的，由于头条小程序的兼容性差异，子组件无法实时监听父组件参数的变化
			// 所以需要手动通知子组件，这里返回一个parentData变量，供watch监听，在其中去通知每一个子组件重新从父组件(u-swipe-action-item)
			// 拉取父组件新的变化后的参数
			parentData() {
				return [this.autoClose]
			}
		},
		watch: {
			// 当父组件需要子组件需要共享的参数发生了变化，手动通知子组件
			parentData() {
				if (this.children.length) {
					this.children.map(child => {
						// 判断子组件(u-swipe-action-item)如果有updateParentData方法的话，就就执行(执行的结果是子组件重新从父组件拉取了最新的值)
						typeof(child.updateParentData) === 'function' && child.updateParentData()
					})
				}
			},
		},
		created() {
			this.children = []
		},
		methods: {
			closeOther(child) {
				if (this.autoClose) {
					// 历遍所有的单元格，找出非当前操作中的单元格，进行关闭
					this.children.map((item, index) => {
						if (child !== item) {
							item.closeHandler()
						}
					})
				}
			}
		}
	}
</script>

<style lang="scss" scoped>

</style>
