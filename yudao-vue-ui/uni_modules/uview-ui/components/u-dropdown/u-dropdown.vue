<template>
	<view class="u-drawdown">
		<view
			class="u-dropdown__menu"
			:style="{
				height: $u.addUnit(height)
			}"
			ref="u-dropdown__menu"
		>
			<view
				class="u-dropdown__menu__item"
				v-for="(item, index) in menuList"
				:key="index"
				@tap.stop="clickHandler(item, index)"
			>
				<view class="u-dropdown__menu__item__content">
					<text
						class="u-dropdown__menu__item__content__text"
						:style="[index === current ? activeStyle : inactiveStyle]"
					>{{item.title}}</text>
					<view
						class="u-dropdown__menu__item__content__arrow"
						:class="[index === current && 'u-dropdown__menu__item__content__arrow--rotate']"
					>
						<u-icon
							:name="menuIcon"
							:size="$u.addUnit(menuIconSize)"
						></u-icon>
					</view>
				</view>
			</view>
		</view>
		<view class="u-dropdown__content">
			<slot />
		</view>
	</view>
</template>

<script>
	import props from './props.js';
	/**
	 * Dropdown  
	 * @description 
	 * @tutorial url
	 * @property {String}
	 * @event {Function}
	 * @example
	 */
	export default {
		name: 'u-dropdown',
		mixins: [uni.$u.mixin, props],
		data() {
			return {
				// 菜单数组
				menuList: [],
				current: 0
			}
		},
		computed: {
		
		},
		created() {
			// 引用所有子组件(u-dropdown-item)的this，不能在data中声明变量，否则在微信小程序会造成循环引用而报错
			this.children = [];
		},
		methods: {
			clickHandler(item, index) {
				this.children.map(child => {
					if(child.title === item.title) {
						// this.queryRect('u-dropdown__menu').then(size => {
							child.$emit('click')
							child.setContentAnimate(child.show ? 0 : 300)
							child.show = !child.show
						// })
					} else {
						child.show = false
						child.setContentAnimate(0)
					}
				})
			},
			// 获取标签的尺寸位置
			queryRect(el) {
				// #ifndef APP-NVUE
				// $uGetRect为uView自带的节点查询简化方法，详见文档介绍：https://www.uviewui.com/js/getRect.html
				// 组件内部一般用this.$uGetRect，对外的为this.$u.getRect，二者功能一致，名称不同
				return new Promise(resolve => {
					this.$uGetRect(`.${el}`).then(size => {
						resolve(size)
					})
				})
				// #endif
			
				// #ifdef APP-NVUE 
				// nvue下，使用dom模块查询元素高度
				// 返回一个promise，让调用此方法的主体能使用then回调
				return new Promise(resolve => {
					dom.getComponentRect(this.$refs[el], res => {
						resolve(res.size)
					})
				})
				// #endif
			},
		},
	}
</script>

<style lang="scss">
	@import '../../libs/css/components.scss';

	.u-dropdown {

		&__menu {
			@include flex;

			&__item {
				flex: 1;
				@include flex;
				justify-content: center;

				&__content {
					@include flex;
					align-items: center;
				}
			}
		}
	}
</style>
