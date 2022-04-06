<template>
	<view class="u-navbar">
		<view
			class="u-navbar__placeholder"
			v-if="fixed && placeholder"
			:style="{
				height: $u.addUnit($u.getPx(height) + $u.sys().statusBarHeight,'px'),
			}"
		></view>
		<view :class="[fixed && 'u-navbar--fixed']">
			<u-status-bar
				v-if="safeAreaInsetTop"
				:bgColor="bgColor"
			></u-status-bar>
			<view
				class="u-navbar__content"
				:class="[border && 'u-border-bottom']"
				:style="{
					height: $u.addUnit(height),
					backgroundColor: bgColor,
				}"
			>
				<view
					class="u-navbar__content__left"
					hover-class="u-navbar__content__left--hover"
					hover-start-time="150"
					@tap="leftClick"
				>
					<slot name="left">
						<u-icon
							v-if="leftIcon"
							:name="leftIcon"
							:size="leftIconSize"
							:color="leftIconColor"
						></u-icon>
						<text
							v-if="leftText"
							:style="{
								color: leftIconColor
							}"
							class="u-navbar__content__left__text"
						>{{ leftText }}</text>
					</slot>
				</view>
				<slot name="center">
					<text
						class="u-line-1 u-navbar__content__title"
						:style="[{
							width: $u.addUnit(titleWidth),
						}, $u.addStyle(titleStyle)]"
					>{{ title }}</text>
				</slot>
				<view
					class="u-navbar__content__right"
					v-if="$slots.right || rightIcon || rightText"
					@tap="rightClick"
				>
					<slot name="right">
						<u-icon
							v-if="rightIcon"
							:name="rightIcon"
							size="20"
						></u-icon>
						<text
							v-if="rightText"
							class="u-navbar__content__right__text"
						>{{ rightText }}</text>
					</slot>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import props from './props.js';
	/**
	 * Navbar 自定义导航栏
	 * @description 此组件一般用于在特殊情况下，需要自定义导航栏的时候用到，一般建议使用uni-app带的导航栏。
	 * @tutorial https://www.uviewui.com/components/navbar.html
	 * @property {Boolean}			safeAreaInsetTop	是否开启顶部安全区适配  （默认 true ）
	 * @property {Boolean}			placeholder			固定在顶部时，是否生成一个等高元素，以防止塌陷 （默认 false ）
	 * @property {Boolean}			fixed				导航栏是否固定在顶部 （默认 false ）
	 * @property {Boolean}			border				导航栏底部是否显示下边框 （默认 false ）
	 * @property {String}			leftIcon			左边返回图标的名称，只能为uView自带的图标 （默认 'arrow-left' ）
	 * @property {String}			leftText			左边的提示文字
	 * @property {String}			rightText			右边的提示文字
	 * @property {String}			rightIcon			右边返回图标的名称，只能为uView自带的图标
	 * @property {String}			title				导航栏标题，如设置为空字符，将会隐藏标题占位区域
	 * @property {String}			bgColor				导航栏背景设置 （默认 '#ffffff' ）
	 * @property {String | Number}	titleWidth			导航栏标题的最大宽度，内容超出会以省略号隐藏 （默认 '400rpx' ）
	 * @property {String | Number}	height				导航栏高度(不包括状态栏高度在内，内部自动加上)（默认 '44px' ）
	 * @property {String | Number}	leftIconSize		左侧返回图标的大小（默认 20px ）
	 * @property {String | Number}	leftIconColor		左侧返回图标的颜色（默认 #303133 ）
	 * @property {Boolean}	        autoBack			点击左侧区域(返回图标)，是否自动返回上一页（默认 false ）
	 * @property {Object | String}	titleStyle			标题的样式，对象或字符串
	 * @event {Function} leftClick		点击左侧区域
	 * @event {Function} rightClick		点击右侧区域
	 * @example <u-navbar title="剑未配妥，出门已是江湖" left-text="返回" right-text="帮助" @click-left="onClickBack" @click-right="onClickRight"></u-navbar>
	 */
	export default {
		name: 'u-navbar',
		mixins: [uni.$u.mpMixin, uni.$u.mixin, props],
		data() {
			return {

			}
		},
		methods: {
			// 点击左侧区域
			leftClick() {
				// 如果配置了autoBack，自动返回上一页
				this.$emit('leftClick')
				if(this.autoBack) {
					uni.navigateBack()
				}
			},
			// 点击右侧区域
			rightClick() {
				this.$emit('rightClick')
			},
		}
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-navbar {

		&--fixed {
			position: fixed;
			left: 0;
			right: 0;
			top: 0;
			z-index: 11;
		}

		&__content {
			@include flex(row);
			align-items: center;
			height: 44px;
			background-color: #9acafc;
			position: relative;
			justify-content: center;

			&__left,
			&__right {
				padding: 0 13px;
				position: absolute;
				top: 0;
				bottom: 0;
				@include flex(row);
				align-items: center;
			}

			&__left {
				left: 0;
				
				&--hover {
					opacity: 0.7;
				}

				&__text {
					font-size: 15px;
					margin-left: 3px;
				}
			}

			&__title {
				text-align: center;
				font-size: 16px;
				color: $u-main-color;
			}

			&__right {
				right: 0;

				&__text {
					font-size: 15px;
					margin-left: 3px;
				}
			}
		}
	}
</style>
