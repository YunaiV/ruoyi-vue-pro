<template>
	<view
	    class="u-steps"
	    :class="[`u-steps--${direction}`]"
	>
		<slot></slot>
	</view>
</template>

<script>
	import props from './props.js';
	/**
	 * Steps 步骤条
	 * @description 该组件一般用于完成一个任务要分几个步骤，标识目前处于第几步的场景。
	 * @tutorial https://uviewui.com/components/steps.html
	 * @property {String}			direction		row-横向，column-竖向 (默认 'row' )
	 * @property {String | Number}	current			设置当前处于第几步 (默认 0 )
	 * @property {String}			activeColor		激活状态颜色 (默认 '#3c9cff' )
	 * @property {String}			inactiveColor	未激活状态颜色 (默认 '#969799' )
	 * @property {String}			activeIcon		激活状态的图标
	 * @property {String}			inactiveIcon	未激活状态图标 
	 * @property {Boolean}			dot				是否显示点类型 (默认 false )
	 * @example <u-steps current="0"><u-steps-item title="已出库" desc="10:35" ></u-steps-item></u-steps>
	 */
	export default {
		name: 'u-steps',
		mixins: [uni.$u.mpMixin, uni.$u.mixin, props],
		data() {
			return {
			}
		},
		watch: {
			children() {
				this.updateChildData()
			},
			parentData() {
				this.updateChildData()
			}
		},
		computed: {
			// 监听参数的变化，通过watch中，手动去更新子组件的数据，否则子组件不会自动变化
			parentData() {
				return [this.current, this.direction, this.activeColor, this.inactiveColor, this.activeIcon, this.inactiveIcon, this.dot]
			}
		},
		methods: {
			// 更新子组件的数据
			updateChildData() {
				this.children.map(child => {
					// 先判断子组件是否存在对应的方法
					uni.$u.test.func((child || {}).updateFromParent()) && child.updateFromParent()
				})
			},
			// 接受子组件的通知，去修改其他子组件的数据
			updateFromChild() {
				this.updateChildData()
			}
		},
		created() {
			this.children = []
		}
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-steps {
		@include flex;

		&--column {
			flex-direction: column
		}

		&--row {
			flex-direction: row;
			flex: 1;
		}
	}
</style>
