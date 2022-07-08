<template>
	<view
	    class="u-radio-group"
	    :class="bemClass"
	>
		<slot></slot>
	</view>
</template>

<script>
	import props from './props.js';

	/**
	 * radioRroup 单选框父组件
	 * @description 单选框用于有一个选择，用户只能选择其中一个的场景。搭配u-radio使用
	 * @tutorial https://www.uviewui.com/components/radio.html
	 * @property {String | Number | Boolean}	value 			绑定的值
	 * @property {Boolean}						disabled		是否禁用所有radio（默认 false ）
	 * @property {String}						shape			外观形状，shape-方形，circle-圆形(默认 circle )
	 * @property {String}						activeColor		选中时的颜色，应用到所有子Radio组件（默认 '#2979ff' ）
	 * @property {String}						inactiveColor	未选中的颜色 (默认 '#c8c9cc' )
	 * @property {String}						name			标识符
	 * @property {String | Number}				size			组件整体的大小，单位px（默认 18 ）
	 * @property {String}						placement		布局方式，row-横向，column-纵向 （默认 'row' ）
	 * @property {String}						label			文本
	 * @property {String}						labelColor		label的颜色 （默认 '#303133' ）
	 * @property {String | Number}				labelSize		label的字体大小，px单位 （默认 14 ）
	 * @property {Boolean}						labelDisabled	是否禁止点击文本操作checkbox(默认 false )
	 * @property {String}						iconColor		图标颜色 （默认 '#ffffff' ）
	 * @property {String | Number}				iconSize		图标的大小，单位px （默认 12 ）
	 * @property {Boolean}						borderBottom	placement为row时，是否显示下边框 （默认 false ）
	 * @property {String}						iconPlacement	图标与文字的对齐方式 （默认 'left' ）
     * @property {Object}						customStyle		组件的样式，对象形式
	 * @event {Function} change 任一个radio状态发生变化时触发
	 * @example <u-radio-group v-model="value"></u-radio-group>
	 */
	export default {
		name: 'u-radio-group',
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		computed: {
			// 这里computed的变量，都是子组件u-radio需要用到的，由于头条小程序的兼容性差异，子组件无法实时监听父组件参数的变化
			// 所以需要手动通知子组件，这里返回一个parentData变量，供watch监听，在其中去通知每一个子组件重新从父组件(u-radio-group)
			// 拉取父组件新的变化后的参数
			parentData() {
				return [this.value, this.disabled, this.inactiveColor, this.activeColor, this.size, this.labelDisabled, this.shape,
					this.iconSize, this.borderBottom, this.placement
				]
			},
			bemClass() {
				// this.bem为一个computed变量，在mixin中
				return this.bem('radio-group', ['placement'])
			},
		},
		watch: {
			// 当父组件需要子组件需要共享的参数发生了变化，手动通知子组件
			parentData() {
				if (this.children.length) {
					this.children.map(child => {
						// 判断子组件(u-radio)如果有init方法的话，就就执行(执行的结果是子组件重新从父组件拉取了最新的值)
						typeof(child.init) === 'function' && child.init()
					})
				}
			},
		},
		data() {
			return {

			}
		},
		created() {
			this.children = []
		},
		methods: {
			// 将其他的radio设置为未选中的状态
			unCheckedOther(childInstance) {
				this.children.map(child => {
					// 所有子radio中，被操作组件实例的checked的值无需修改
					if (childInstance !== child) {
						child.checked = false
					}
				})
				const {
					name
				} = childInstance
				// 通过emit事件，设置父组件通过v-model双向绑定的值
				this.$emit('input', name)
				// 发出事件
				this.$emit('change', name)
			},
		}
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-radio-group {
		flex: 1;

		&--row {
			@include flex;
		}

		&--column {
			@include flex(column);
		}
	}
</style>
