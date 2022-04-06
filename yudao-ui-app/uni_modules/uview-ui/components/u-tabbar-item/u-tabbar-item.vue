<template>
	<view
	    class="u-tabbar-item"
	    :style="[$u.addStyle(customStyle)]"
	    @tap="clickHandler"
	>
		<view class="u-tabbar-item__icon">
			<u-icon
			    v-if="icon"
			    :name="icon"
			    :color="isActive? parentData.activeColor : parentData.inactiveColor"
			    :size="20"
			></u-icon>
			<template v-else>
				<slot
				    v-if="isActive"
				    name="active-icon"
				/>
				<slot
				    v-else
				    name="inactive-icon"
				/>
			</template>
			<u-badge
				absolute
				:offset="[0, dot ? '34rpx' : badge > 9 ? '14rpx' : '20rpx']"
			    :customStyle="badgeStyle"
			    :isDot="dot"
			    :value="badge || (dot ? 1 : null)"
			    :show="dot || badge > 0"
			></u-badge>
		</view>
		
		<slot name="text">
			<text
			    class="u-tabbar-item__text"
			    :style="{
					color: isActive? parentData.activeColor : parentData.inactiveColor
				}"
			>{{ text }}</text>
		</slot>
	</view>
</template>

<script>
	import props from './props.js';
	/**
	 * TabbarItem 底部导航栏子组件
	 * @description 此组件提供了自定义tabbar的能力。
	 * @tutorial https://www.uviewui.com/components/tabbar.html
	 * @property {String | Number}	name		item标签的名称，作为与u-tabbar的value参数匹配的标识符
	 * @property {String}			icon		uView内置图标或者绝对路径的图片
	 * @property {String | Number}	badge		右上角的角标提示信息
	 * @property {Boolean}			dot			是否显示圆点，将会覆盖badge参数（默认 false ）
	 * @property {String}			text		描述文本
	 * @property {Object | String}	badgeStyle	控制徽标的位置，对象或者字符串形式，可以设置top和right属性（默认 'top: 6px;right:2px;' ）
	 * @property {Object}			customStyle	定义需要用到的外部样式
	 * 
	 * @example <u-tabbar :value="value2" :placeholder="false" @change="name => value2 = name" :fixed="false" :safeAreaInsetBottom="false"><u-tabbar-item text="首页" icon="home" dot ></u-tabbar-item></u-tabbar>
	 */
	export default {
		name: 'u-tabbar-item',
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		data() {
			return {
				isActive: false, // 是否处于激活状态
				parentData: {
					value: null,
					activeColor: '',
					inactiveColor: ''
				}
			}
		},
		created() {
			this.init()
		},
		methods: {
			init() {
				// 支付宝小程序不支持provide/inject，所以使用这个方法获取整个父组件，在created定义，避免循环引用
				this.updateParentData()
				if (!this.parent) {
					uni.$u.error('u-tabbar-item必须搭配u-tabbar组件使用')
				}
				// 本子组件在u-tabbar的children数组中的索引
				const index = this.parent.children.indexOf(this)
				// 判断本组件的name(如果没有定义name，就用index索引)是否等于父组件的value参数
				this.isActive = (this.name || index) === this.parentData.value
			},
			updateParentData() {
				// 此方法在mixin中
				this.getParentData('u-tabbar')
			},
			// 此方法将会被父组件u-tabbar调用
			updateFromParent() {
				// 重新初始化
				this.init()
			},
			clickHandler() {
				this.$nextTick(() => {
					const index = this.parent.children.indexOf(this)
					const name = this.name || index
					// 点击的item为非激活的item才发出change事件
					if (name !== this.parent.value) {
						this.parent.$emit('change', name)
					}
					this.$emit('click', name)
				})
			}
		},
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-tabbar-item {
		@include flex(column);
		align-items: center;
		justify-content: center;
		flex: 1;
		
		&__icon {
			@include flex;
			position: relative;
			width: 150rpx;
			justify-content: center;
		}

		&__text {
			margin-top: 2px;
			font-size: 12px;
			color: $u-content-color;
		}
	}

	/* #ifdef MP */
	// 由于小程序都使用shadow DOM形式实现，需要给影子宿主设置flex: 1才能让其撑开
	:host {
		flex: 1
	}
	/* #endif */
</style>
