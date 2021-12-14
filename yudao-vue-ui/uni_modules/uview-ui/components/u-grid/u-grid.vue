<template>
	<view
	    class="u-grid"
		ref='u-grid'
	    :style="[gridStyle]"
	>
		<slot />
	</view>
</template>

<script>
	import props from './props.js';
	/**
	 * grid 宫格布局
	 * @description 宫格组件一般用于同时展示多个同类项目的场景，可以给宫格的项目设置徽标组件(badge)，或者图标等，也可以扩展为左右滑动的轮播形式。
	 * @tutorial https://www.uviewui.com/components/grid.html
	 * @property {String | Number}	col			宫格的列数（默认 3 ）
	 * @property {Boolean}			border		是否显示宫格的边框（默认 false ）
	 * @property {String}			align		宫格对齐方式，表现为数量少的时候，靠左，居中，还是靠右 （默认 'left' ）
	 * @property {Object}			customStyle	定义需要用到的外部样式
	 * @event {Function} click 点击宫格触发
	 * @example <u-grid :col="3" @click="click"></u-grid>
	 */
	export default {
		name: 'u-grid',
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		data() {
			return {
				index: 0,
				width: 0
			}
		},
		watch: {
			// 当父组件需要子组件需要共享的参数发生了变化，手动通知子组件
			parentData() {
				if (this.children.length) {
					this.children.map(child => {
						// 判断子组件(u-radio)如果有updateParentData方法的话，就就执行(执行的结果是子组件重新从父组件拉取了最新的值)
						typeof(child.updateParentData) == 'function' && child.updateParentData();
					})
				}
			},
		},
		created() {
			// 如果将children定义在data中，在微信小程序会造成循环引用而报错
			this.children = []
		},
		computed: {
			// 计算父组件的值是否发生变化
			parentData() {
				return [this.hoverClass, this.col, this.size, this.border];
			},
			// 宫格对齐方式
			gridStyle() {
				let style = {};
				switch (this.align) {
					case 'left':
						style.justifyContent = 'flex-start';
						break;
					case 'center':
						style.justifyContent = 'center';
						break;
					case 'right':
						style.justifyContent = 'flex-end';
						break;
					default:
						style.justifyContent = 'flex-start';
				};
				return uni.$u.deepMerge(style, uni.$u.addStyle(this.customStyle));
			}
		},
		methods: {
			// 此方法由u-grid-item触发，用于在u-grid发出事件
			childClick(name) {
				this.$emit('click', name)
			}
		}
	};
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";
     $u-grid-width:100% !default;
	.u-grid {
		/* #ifdef MP */
		width: $u-grid-width;
		position: relative;
		box-sizing: border-box;
		overflow: hidden;
		display: block;
		/* #endif */
		justify-content: center;
		/* #ifndef MP */
		@include flex;
		flex-wrap: wrap;
		align-items: center;
		/* #endif */
	}
</style>
