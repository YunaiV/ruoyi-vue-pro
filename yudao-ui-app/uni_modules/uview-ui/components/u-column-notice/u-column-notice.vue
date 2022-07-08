<template>
	<view
		class="u-notice"
		@tap="clickHandler"
	>
		<slot name="icon">
			<view
				class="u-notice__left-icon"
				v-if="icon"
			>
				<u-icon
					:name="icon"
					:color="color"
					size="19"
				></u-icon>
			</view>
		</slot>
		<swiper
			:disable-touch="disableTouch"
			:vertical="step ? false : true"
			circular
			:interval="duration"
			:autoplay="true"
			class="u-notice__swiper"
			@change="noticeChange"
		>
			<swiper-item
				v-for="(item, index) in text"
				:key="index"
				class="u-notice__swiper__item"
			>
				<text
					class="u-notice__swiper__item__text u-line-1"
					:style="[textStyle]"
				>{{ item }}</text>
			</swiper-item>
		</swiper>
		<view
			class="u-notice__right-icon"
			v-if="['link', 'closable'].includes(mode)"
		>
			<u-icon
				v-if="mode === 'link'"
				name="arrow-right"
				:size="17"
				:color="color"
			></u-icon>
			<u-icon
				v-if="mode === 'closable'"
				name="close"
				:size="16"
				:color="color"
				@click="close"
			></u-icon>
		</view>
	</view>
</template>

<script>
	import props from './props.js';
	/**
	 * ColumnNotice 滚动通知中的垂直滚动 内部组件
	 * @description 该组件用于滚动通告场景，是其中的垂直滚动方式
	 * @tutorial https://www.uviewui.com/components/noticeBar.html
	 * @property {Array}			text 			显示的内容，字符串
	 * @property {String}			icon 			是否显示左侧的音量图标 （ 默认 'volume' ）
	 * @property {String}			mode 			通告模式，link-显示右箭头，closable-显示右侧关闭图标
	 * @property {String}			color 			文字颜色，各图标也会使用文字颜色 （ 默认 '#f9ae3d' ）
	 * @property {String}			bgColor 		背景颜色 （ 默认 '#fdf6ec' ）
	 * @property {String | Number}	fontSize		字体大小，单位px  （ 默认 14 ）
	 * @property {String | Number}	speed			水平滚动时的滚动速度，即每秒滚动多少px(rpx)，这有利于控制文字无论多少时，都能有一个恒定的速度 （ 默认 80 ）
	 * @property {Boolean}			step			direction = row时，是否使用步进形式滚动 （ 默认 false ）
	 * @property {String | Number}	duration		滚动一个周期的时间长，单位ms （ 默认 1500 ）
	 * @property {Boolean}			disableTouch	是否禁止用手滑动切换   目前HX2.6.11，只支持App 2.5.5+、H5 2.5.5+、支付宝小程序、字节跳动小程序 （ 默认 true ）
	 * @example 
	 */
	export default {
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		watch: {
			text: {
				immediate: true,
				handler(newValue, oldValue) {
					if(!uni.$u.test.array(newValue)) {
						uni.$u.error('noticebar组件direction为column时，要求text参数为数组形式')
					}
				}
			}
		},
		computed: {
			// 文字内容的样式
			textStyle() {
				let style = {}
				style.color = this.color
				style.fontSize = uni.$u.addUnit(this.fontSize)
				return style
			},
			// 垂直或者水平滚动
			vertical() {
				if (this.mode == 'horizontal') return false
				else return true
			},
		},
		data() {
			return {
				index:0
			}
		},
		methods: {
			noticeChange(e){
				this.index = e.detail.current
			},
			// 点击通告栏
			clickHandler() {
				this.$emit('click', this.index)
			},
			// 点击关闭按钮
			close() {
				this.$emit('close')
			}
		}
	};
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-notice {
		@include flex;
		align-items: center;
		justify-content: space-between;

		&__left-icon {
			align-items: center;
			margin-right: 5px;
		}

		&__right-icon {
			margin-left: 5px;
			align-items: center;
		}

		&__swiper {
			height: 16px;
			@include flex;
			align-items: center;
			flex: 1;

			&__item {
				@include flex;
				align-items: center;
				overflow: hidden;

				&__text {
					font-size: 14px;
					color: $u-warning;
				}
			}
		}
	}
</style>
