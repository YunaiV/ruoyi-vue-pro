<!--空布局

可作为独立的组件, 不使用mescroll的页面也能单独引入, 以便APP全局统一管理:
import MescrollEmpty from '@/components/mescroll-uni/components/mescroll-empty.vue';
<mescroll-empty v-if="isShowEmpty" :option="optEmpty" @emptyclick="emptyClick"></mescroll-empty>

-->
<template>
	<view class="mescroll-empty" :class="{ 'empty-fixed': option.fixed }" :style="{ 'z-index': option.zIndex, top: option.top }">
		<view> <image v-if="icon" class="empty-icon" :src="icon" mode="widthFix" /> </view>
		<view v-if="tip" class="empty-tip">{{ tip }}</view>
		<view v-if="option.btnText" class="empty-btn" @click="emptyClick">{{ option.btnText }}</view>
	</view>
</template>

<script>
// 引入全局配置
import GlobalOption from './../mescroll-uni-option.js';
export default {
	props: {
		// empty的配置项: 默认为GlobalOption.up.empty
		option: {
			type: Object,
			default() {
				return {};
			}
		}
	},
	// 使用computed获取配置,用于支持option的动态配置
	computed: {
		// 图标
		icon() {
			return this.option.icon == null ? GlobalOption.up.empty.icon : this.option.icon; // 此处不使用短路求值, 用于支持传空串不显示图标
		},
		// 文本提示
		tip() {
			return this.option.tip == null ? GlobalOption.up.empty.tip : this.option.tip; // 此处不使用短路求值, 用于支持传空串不显示文本提示
		}
	},
	methods: {
		// 点击按钮
		emptyClick() {
			this.$emit('emptyclick');
		}
	}
};
</script>

<style>
/* 无任何数据的空布局 */
.mescroll-empty {
	box-sizing: border-box;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-direction: column;
	width: 100%;
	padding: 30vh 50rpx 100rpx;
	text-align: center;
}

.mescroll-empty.empty-fixed {
	z-index: 99;
	position: absolute; /*transform会使fixed失效,最终会降级为absolute */
	top: 100rpx;
	left: 0;
}

.mescroll-empty .empty-icon {
	width: 170rpx;
	height: 170rpx;
	transform: translateX(16rpx);
}

.mescroll-empty .empty-tip {
	margin-top: 20rpx;
	font-size: 24rpx;
	color: #666;
}

.mescroll-empty .empty-btn {
	display: inline-block;
	margin-top: 40rpx;
	min-width: 200rpx;
	padding: 18rpx;
	font-size: 28rpx;
	border: 1rpx solid #e04b28;
	border-radius: 60rpx;
	color: #e04b28;
}

.mescroll-empty .empty-btn:active {
	opacity: 0.75;
}
</style>
