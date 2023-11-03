<template>
	<view :class="[ 'uni-row', typeClass , justifyClass, alignClass, ]" :style="{
		marginLeft:`${Number(marginValue)}rpx`,
		marginRight:`${Number(marginValue)}rpx`,
	}">
		<slot></slot>
	</view>
</template>

<script>
	const ComponentClass = 'uni-row';
	const modifierSeparator = '--';
	/**
	 * Row	布局-行
	 * @description	流式栅格系统，随着屏幕或视口分为 24 份，可以迅速简便地创建布局。
	 * @tutorial	https://ext.dcloud.net.cn/plugin?id=3958
	 *
	 * @property	{gutter} type = Number 栅格间隔
	 * @property	{justify} type = String flex 布局下的水平排列方式
	 * 						可选	start/end/center/space-around/space-between	start
	 * 						默认值	start
	 * @property	{align} type = String flex 布局下的垂直排列方式
	 * 						可选	top/middle/bottom
	 * 						默认值	top
	 * @property	{width} type = String|Number nvue下需要自行配置宽度用于计算
	 * 						默认值 750
	 */


	export default {
		name: 'uniRow',
		componentName: 'uniRow',
		// #ifdef MP-WEIXIN
		options: {
			virtualHost: true // 在微信小程序中将组件节点渲染为虚拟节点，更加接近Vue组件的表现，可使用flex布局
		},
		// #endif
		props: {
			type: String,
			gutter: Number,
			justify: {
				type: String,
				default: 'start'
			},
			align: {
				type: String,
				default: 'top'
			},
			// nvue如果使用span等属性，需要配置宽度
			width: {
				type: [String, Number],
				default: 750
			}
		},
		created() {
			// #ifdef APP-NVUE
			this.type = 'flex';
			// #endif
		},
		computed: {
			marginValue() {
				// #ifndef APP-NVUE
				if (this.gutter) {
					return -(this.gutter / 2);
				}
				// #endif
				return 0;
			},
			typeClass() {
				return this.type === 'flex' ? `${ComponentClass + modifierSeparator}flex` : '';
			},
			justifyClass() {
				return this.justify !== 'start' ? `${ComponentClass + modifierSeparator}flex-justify-${this.justify}` : ''
			},
			alignClass() {
				return this.align !== 'top' ? `${ComponentClass + modifierSeparator}flex-align-${this.align}` : ''
			}
		}
	};
</script>

<style lang="scss">
	$layout-namespace: ".uni-";
	$row:$layout-namespace+"row";
	$modifier-separator: "--";

	@mixin utils-clearfix {
		$selector: &;

		@at-root {

			/* #ifndef APP-NVUE */
			#{$selector}::before,
			#{$selector}::after {
				display: table;
				content: "";
			}

			#{$selector}::after {
				clear: both;
			}

			/* #endif */
		}

	}

	@mixin utils-flex ($direction: row) {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: $direction;
	}

	@mixin set-flex($state) {
		@at-root &-#{$state} {
			@content
		}
	}

	#{$row} {
		position: relative;
		flex-direction: row;

		/* #ifdef APP-NVUE */
		flex: 1;
		/* #endif */

		/* #ifndef APP-NVUE */
		box-sizing: border-box;
		/* #endif */

		// 非nvue使用float布局
		@include utils-clearfix;

		// 在QQ、字节、百度小程序平台，编译后使用shadow dom，不可使用flex布局，使用float
		@at-root {

			/* #ifndef MP-QQ || MP-TOUTIAO || MP-BAIDU */
			&#{$modifier-separator}flex {
				@include utils-flex;
				flex-wrap: wrap;
				flex: 1;

				&:before,
				&:after {
					/* #ifndef APP-NVUE */
					display: none;
					/* #endif */
				}

				@include set-flex(justify-center) {
					justify-content: center;
				}

				@include set-flex(justify-end) {
					justify-content: flex-end;
				}

				@include set-flex(justify-space-between) {
					justify-content: space-between;
				}

				@include set-flex(justify-space-around) {
					justify-content: space-around;
				}

				@include set-flex(align-middle) {
					align-items: center;
				}

				@include set-flex(align-bottom) {
					align-items: flex-end;
				}
			}

			/* #endif */
		}

	}

	// 字节、QQ配置后不生效
	// 此处用法无法使用
	/* #ifdef MP-WEIXIN || MP-TOUTIAO || MP-QQ */
	:host {
		display: block;
	}

	/* #endif */
</style>
