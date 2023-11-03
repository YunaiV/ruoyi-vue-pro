<template>
	<!-- #ifndef APP-NVUE -->
	<view :class="['uni-col', sizeClass, pointClassList]" :style="{
		paddingLeft:`${Number(gutter)}rpx`,
		paddingRight:`${Number(gutter)}rpx`,
	}">
		<slot></slot>
	</view>
	<!-- #endif -->
	<!-- #ifdef APP-NVUE -->
	<!-- 在nvue上，类名样式不生效，换为style -->
	<!-- 设置right正值失效，设置 left 负值 -->
	<view :class="['uni-col']" :style="{
		paddingLeft:`${Number(gutter)}rpx`,
		paddingRight:`${Number(gutter)}rpx`,
		width:`${nvueWidth}rpx`,
		position:'relative',
		marginLeft:`${marginLeft}rpx`,
		left:`${right === 0 ? left : -right}rpx`
	}">
		<slot></slot>
	</view>
	<!-- #endif -->
</template>

<script>
	/**
	 * Col	布局-列
	 * @description	搭配uni-row使用，构建布局。
	 * @tutorial	https://ext.dcloud.net.cn/plugin?id=3958
	 *
	 * @property	{span} type = Number 栅格占据的列数
	 * 						默认 24
	 * @property	{offset} type = Number 栅格左侧的间隔格数
	 * @property	{push} type = Number 栅格向右移动格数
	 * @property	{pull} type = Number 栅格向左移动格数
	 * @property	{xs} type = [Number, Object] <768px 响应式栅格数或者栅格属性对象
	 * 						@description	Number时表示在此屏幕宽度下，栅格占据的列数。Object时可配置多个描述{span: 4, offset: 4}
	 * @property	{sm} type = [Number, Object] ≥768px 响应式栅格数或者栅格属性对象
	 * 						@description	Number时表示在此屏幕宽度下，栅格占据的列数。Object时可配置多个描述{span: 4, offset: 4}
	 * @property	{md} type = [Number, Object] ≥992px 响应式栅格数或者栅格属性对象
	 * 						@description	Number时表示在此屏幕宽度下，栅格占据的列数。Object时可配置多个描述{span: 4, offset: 4}
	 * @property	{lg} type = [Number, Object] ≥1200px 响应式栅格数或者栅格属性对象
	 * 						@description	Number时表示在此屏幕宽度下，栅格占据的列数。Object时可配置多个描述{span: 4, offset: 4}
	 * @property	{xl} type = [Number, Object] ≥1920px 响应式栅格数或者栅格属性对象
	 * 						@description	Number时表示在此屏幕宽度下，栅格占据的列数。Object时可配置多个描述{span: 4, offset: 4}
	 */
	const ComponentClass = 'uni-col';

	// -1 默认值，因为在微信小程序端只给Number会有默认值0
	export default {
		name: 'uniCol',
		// #ifdef MP-WEIXIN
		options: {
			virtualHost: true // 在微信小程序中将组件节点渲染为虚拟节点，更加接近Vue组件的表现
		},
		// #endif
		props: {
			span: {
				type: Number,
				default: 24
			},
			offset: {
				type: Number,
				default: -1
			},
			pull: {
				type: Number,
				default: -1
			},
			push: {
				type: Number,
				default: -1
			},
			xs: [Number, Object],
			sm: [Number, Object],
			md: [Number, Object],
			lg: [Number, Object],
			xl: [Number, Object]
		},
		data() {
			return {
				gutter: 0,
				sizeClass: '',
				parentWidth: 0,
				nvueWidth: 0,
				marginLeft: 0,
				right: 0,
				left: 0
			}
		},
		created() {
			// 字节小程序中，在computed中读取$parent为undefined
			let parent = this.$parent;

			while (parent && parent.$options.componentName !== 'uniRow') {
				parent = parent.$parent;
			}

			this.updateGutter(parent.gutter)
			parent.$watch('gutter', (gutter) => {
				this.updateGutter(gutter)
			})

			// #ifdef APP-NVUE
			this.updateNvueWidth(parent.width)
			parent.$watch('width', (width) => {
				this.updateNvueWidth(width)
			})
			// #endif
		},
		computed: {
			sizeList() {
				let {
					span,
					offset,
					pull,
					push
				} = this;

				return {
					span,
					offset,
					pull,
					push
				}
			},
			// #ifndef APP-NVUE
			pointClassList() {
				let classList = [];

				['xs', 'sm', 'md', 'lg', 'xl'].forEach(point => {
					const props = this[point];
					if (typeof props === 'number') {
						classList.push(`${ComponentClass}-${point}-${props}`)
					} else if (typeof props === 'object' && props) {
						Object.keys(props).forEach(pointProp => {
							classList.push(
								pointProp === 'span' ?
								`${ComponentClass}-${point}-${props[pointProp]}` :
								`${ComponentClass}-${point}-${pointProp}-${props[pointProp]}`
							)
						})
					}
				});

				// 支付宝小程序使用 :class=[ ['a','b'] ]，渲染错误
				return classList.join(' ');
			}
			// #endif
		},
		methods: {
			updateGutter(parentGutter) {
				parentGutter = Number(parentGutter);
				if (!isNaN(parentGutter)) {
					this.gutter = parentGutter / 2
				}
			},
			// #ifdef APP-NVUE
			updateNvueWidth(width) {
				// 用于在nvue端，span，offset，pull，push的计算
				this.parentWidth = width;
				['span', 'offset', 'pull', 'push'].forEach(size => {
					const curSize = this[size];
					if ((curSize || curSize === 0) && curSize !== -1) {
						let RPX = 1 / 24 * curSize * width
						RPX = Number(RPX);
						switch (size) {
							case 'span':
								this.nvueWidth = RPX
								break;
							case 'offset':
								this.marginLeft = RPX
								break;
							case 'pull':
								this.right = RPX
								break;
							case 'push':
								this.left = RPX
								break;
						}
					}
				});
			}
			// #endif
		},
		watch: {
			sizeList: {
				immediate: true,
				handler(newVal) {
					// #ifndef APP-NVUE
					let classList = [];
					for (let size in newVal) {
						const curSize = newVal[size];
						if ((curSize || curSize === 0) && curSize !== -1) {
							classList.push(
								size === 'span' ?
								`${ComponentClass}-${curSize}` :
								`${ComponentClass}-${size}-${curSize}`
							)
						}
					}
					// 支付宝小程序使用 :class=[ ['a','b'] ]，渲染错误
					this.sizeClass = classList.join(' ');
					// #endif
					// #ifdef APP-NVUE
					this.updateNvueWidth(this.parentWidth);
					// #endif
				}
			}
		}
	}
</script>

<style lang='scss' >
	/* breakpoints */
	$--sm: 768px !default;
	$--md: 992px !default;
	$--lg: 1200px !default;
	$--xl: 1920px !default;

	$breakpoints: ('xs' : (max-width: $--sm - 1),
	'sm' : (min-width: $--sm),
	'md' : (min-width: $--md),
	'lg' : (min-width: $--lg),
	'xl' : (min-width: $--xl));

	$layout-namespace: ".uni-";
	$col: $layout-namespace+"col";

	@function getSize($size) {
		/* TODO 1/24 * $size * 100 * 1%; 使用计算后的值，为了解决 vue3 控制台报错 */
		@return 0.04166666666 * $size * 100 * 1%;
	}

	@mixin res($key, $map:$breakpoints) {
		@if map-has-key($map, $key) {
			@media screen and #{inspect(map-get($map,$key))} {
				@content;
			}
		}

		@else {
			@warn "Undeinfed point: `#{$key}`";
		}
	}

	/* #ifndef APP-NVUE */
	#{$col} {
		float: left;
		box-sizing: border-box;
	}

	#{$col}-0 {
		/* #ifdef APP-NVUE */
		width: 0;
		height: 0;
		margin-top: 0;
		margin-right: 0;
		margin-bottom: 0;
		margin-left: 0;
		/* #endif */
		/* #ifndef APP-NVUE */
		display: none;
		/* #endif */
	}

	@for $i from 0 through 24 {
		#{$col}-#{$i} {
			width: getSize($i);
		}

		#{$col}-offset-#{$i} {
			margin-left: getSize($i);
		}

		#{$col}-pull-#{$i} {
			position: relative;
			right: getSize($i);
		}

		#{$col}-push-#{$i} {
			position: relative;
			left: getSize($i);
		}
	}

	@each $point in map-keys($breakpoints) {
		@include res($point) {
			#{$col}-#{$point}-0 {
				display: none;
			}

			@for $i from 0 through 24 {
				#{$col}-#{$point}-#{$i} {
					width: getSize($i);
				}

				#{$col}-#{$point}-offset-#{$i} {
					margin-left: getSize($i);
				}

				#{$col}-#{$point}-pull-#{$i} {
					position: relative;
					right: getSize($i);
				}

				#{$col}-#{$point}-push-#{$i} {
					position: relative;
					left: getSize($i);
				}
			}
		}
	}

	/* #endif */
</style>
