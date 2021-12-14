<template>
	<view class="u-swiper-indicator">
		<view
			class="u-swiper-indicator__wrapper"
			v-if="indicatorMode === 'line'"
			:class="[`u-swiper-indicator__wrapper--${indicatorMode}`]"
			:style="{
				width: $u.addUnit(lineWidth * length),
				backgroundColor: indicatorInactiveColor
			}"
		>
			<view
				class="u-swiper-indicator__wrapper--line__bar"
				:style="[lineStyle]"
			></view>
		</view>
		<view
			class="u-swiper-indicator__wrapper"
			v-if="indicatorMode === 'dot'"
		>
			<view
				class="u-swiper-indicator__wrapper__dot"
				v-for="(item, index) in length"
				:key="index"
				:class="[index === current && 'u-swiper-indicator__wrapper__dot--active']"
				:style="[dotStyle(index)]"
			>

			</view>
		</view>
	</view>
</template>

<script>
	import props from './props.js';
	/**
	 * SwiperIndicator 轮播图指示器
	 * @description 该组件一般用于导航轮播，广告展示等场景,可开箱即用，
	 * @tutorial https://www.uviewui.com/components/swiper.html
	 * @property {String | Number}	length					轮播的长度（默认 0 ）
	 * @property {String | Number}	current					当前处于活动状态的轮播的索引（默认 0 ）
	 * @property {String}			indicatorActiveColor	指示器非激活颜色
	 * @property {String}			indicatorInactiveColor	指示器的激活颜色
	 * @property {String}			indicatorMode			指示器模式（默认 'line' ）
	 * @example	<u-swiper :list="list4" indicator keyName="url" :autoplay="false"></u-swiper>
	 */
	export default {
		name: 'u-swiper-indicator',
		mixins: [uni.$u.mpMixin, uni.$u.mixin, props],
		data() {
			return {
				lineWidth: 22
			}
		},
		computed: {
			// 指示器为线型的样式
			lineStyle() {
				let style = {}
				style.width = uni.$u.addUnit(this.lineWidth)
				style.transform = `translateX(${ this.current * this.lineWidth }px)`
				style.backgroundColor = this.indicatorActiveColor
				return style
			},
			// 指示器为点型的样式
			dotStyle() {
				return index => {
					let style = {}
					style.backgroundColor = index === this.current ? this.indicatorActiveColor : this.indicatorInactiveColor
					return style
				}
			}
		},
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-swiper-indicator {

		&__wrapper {
			@include flex;

			&--line {
				border-radius: 100px;
				height: 4px;

				&__bar {
					width: 22px;
					height: 4px;
					border-radius: 100px;
					background-color: #FFFFFF;
					transition: transform 0.3s;
				}
			}

			&__dot {
				width: 5px;
				height: 5px;
				border-radius: 100px;
				margin: 0 4px;

				&--active {
					width: 12px;
				}
			}

		}
	}
</style>
