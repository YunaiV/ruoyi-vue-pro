<template>
	<view :class="[circle === true || circle === 'true' ? 'uni-fav--circle' : '']" :style="[{ backgroundColor: checked ? bgColorChecked : bgColor }]"
	 @click="onClick" class="uni-fav">
		<!-- #ifdef MP-ALIPAY -->
		<view class="uni-fav-star" v-if="!checked && (star === true || star === 'true')">
			<uni-icons :color="fgColor" :style="{color: checked ? fgColorChecked : fgColor}" size="14" type="star-filled" />
		</view>
		<!-- #endif -->
		<!-- #ifndef MP-ALIPAY -->
		<uni-icons :color="fgColor" :style="{color: checked ? fgColorChecked : fgColor}" class="uni-fav-star" size="14" type="star-filled"
		 v-if="!checked && (star === true || star === 'true')" />
		<!-- #endif -->
		<text :style="{color: checked ? fgColorChecked : fgColor}" class="uni-fav-text">{{ checked ? contentFav : contentDefault }}</text>
	</view>
</template>

<script>

	/**
	 * Fav 收藏按钮
	 * @description 用于收藏功能，可点击切换选中、不选中的状态
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=864
	 * @property {Boolean} star = [true|false] 按钮是否带星星
	 * @property {String} bgColor 未收藏时的背景色
	 * @property {String} bgColorChecked 已收藏时的背景色
	 * @property {String} fgColor 未收藏时的文字颜色
	 * @property {String} fgColorChecked 已收藏时的文字颜色
	 * @property {Boolean} circle = [true|false] 是否为圆角
	 * @property {Boolean} checked = [true|false] 是否为已收藏
	 * @property {Object} contentText = [true|false] 收藏按钮文字
	 * @property {Boolean} stat 是否开启统计功能
	 * @event {Function} click 点击 fav按钮触发事件
	 * @example <uni-fav :checked="true"/>
	 */

	import {
		initVueI18n
	} from '@dcloudio/uni-i18n'
	import messages from './i18n/index.js'
	const {	t	} = initVueI18n(messages)

	export default {
		name: "UniFav",
		// TODO 兼容 vue3，需要注册事件
		emits: ['click'],
		props: {
			star: {
				type: [Boolean, String],
				default: true
			},
			bgColor: {
				type: String,
				default: "#eeeeee"
			},
			fgColor: {
				type: String,
				default: "#666666"
			},
			bgColorChecked: {
				type: String,
				default: "#007aff"
			},
			fgColorChecked: {
				type: String,
				default: "#FFFFFF"
			},
			circle: {
				type: [Boolean, String],
				default: false
			},
			checked: {
				type: Boolean,
				default: false
			},
			contentText: {
				type: Object,
				default () {
					return {
						contentDefault: "",
						contentFav: ""
					};
				}
			},
			stat:{
				type: Boolean,
				default: false
			}
		},
		computed: {
			contentDefault() {
				return this.contentText.contentDefault || t("uni-fav.collect")
			},
			contentFav() {
				return this.contentText.contentFav || t("uni-fav.collected")
			},
		},
		watch: {
			checked() {
				if (uni.report && this.stat) {
					if (this.checked) {
						uni.report("收藏", "收藏");
					} else {
						uni.report("取消收藏", "取消收藏");
					}
				}
			}
		},
		methods: {
			onClick() {
				this.$emit("click");
			}
		}
	};
</script>

<style lang="scss" >
	$fav-height: 25px;

	.uni-fav {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row;
		align-items: center;
		justify-content: center;
		width: 60px;
		height: $fav-height;
		line-height: $fav-height;
		text-align: center;
		border-radius: 3px;
		/* #ifdef H5 */
		cursor: pointer;
		/* #endif */
	}

	.uni-fav--circle {
		border-radius: 30px;
	}

	.uni-fav-star {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		height: $fav-height;
		line-height: 24px;
		margin-right: 3px;
		align-items: center;
		justify-content: center;
	}

	.uni-fav-text {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		height: $fav-height;
		line-height: $fav-height;
		align-items: center;
		justify-content: center;
		font-size: 12px;
	}
</style>
