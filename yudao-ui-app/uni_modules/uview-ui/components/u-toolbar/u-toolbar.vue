<template>
	<view
		class="u-toolbar"
		@touchmove.stop.prevent="noop"
		v-if="show"
	>
		<view
			class="u-toolbar__cancel__wrapper"
			hover-class="u-hover-class"
		>
			<text
				class="u-toolbar__wrapper__cancel"
				@tap="cancel"
				:style="{
					color: cancelColor
				}"
			>{{ cancelText }}</text>
		</view>
		<text
			class="u-toolbar__title u-line-1"
			v-if="title"
		>{{ title }}</text>
		<view
			class="u-toolbar__confirm__wrapper"
			hover-class="u-hover-class"
		>
			<text
				class="u-toolbar__wrapper__confirm"
				@tap="confirm"
				:style="{
				color: confirmColor
			}"
			>{{ confirmText }}</text>
		</view>
	</view>
</template>

<script>
	import props from './props.js';
	/**
	 * Toolbar 工具条
	 * @description 
	 * @tutorial https://www.uviewui.com/components/toolbar.html
	 * @property {Boolean}	show			是否展示工具条（默认 true ）
	 * @property {String}	cancelText		取消按钮的文字（默认 '取消' ）
	 * @property {String}	confirmText		确认按钮的文字（默认 '确认' ）
	 * @property {String}	cancelColor		取消按钮的颜色（默认 '#909193' ）
	 * @property {String}	confirmColor	确认按钮的颜色（默认 '#3c9cff' ）
	 * @property {String}	title			标题文字
	 * @event {Function} 
	 * @example 
	 */
	export default {
		name: 'u-toolbar',
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		methods: {
			// 点击取消按钮
			cancel() {
				this.$emit('cancel')
			},
			// 点击确定按钮
			confirm() {
				this.$emit('confirm')
			}
		},
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-toolbar {
		height: 42px;
		@include flex;
		justify-content: space-between;
		align-items: center;

		&__wrapper {
			&__cancel {
				color: $u-tips-color;
				font-size: 15px;
				padding: 0 15px;
			}
		}

		&__title {
			color: $u-main-color;
			padding: 0 60rpx;
			font-size: 16px;
			flex: 1;
			text-align: center;
		}

		&__wrapper {
			&__confirm {
				color: $u-primary;
				font-size: 15px;
				padding: 0 15px;
			}
		}
	}
</style>
