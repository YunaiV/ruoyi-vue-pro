<!-- 上拉加载区域 -->
<template>
	<view class="mescroll-upwarp" :style="{'background-color':mOption.bgColor,'color':mOption.textColor}">
		<!-- 加载中 (此处不能用v-if,否则android小程序快速上拉可能会不断触发上拉回调) -->
		<view v-show="isUpLoading">
			<view class="upwarp-progress mescroll-rotate" :style="{'border-color':mOption.textColor}"></view>
			<view class="upwarp-tip">{{ mOption.textLoading }}</view>
		</view>
		<!-- 无数据 -->
		<view v-if="isUpNoMore" class="upwarp-nodata">{{ mOption.textNoMore }}</view>
	</view>
</template>

<script>
export default {
	props: {
		option: Object, // up的配置项
		type: Number // 上拉加载的状态：0（loading前），1（loading中），2（没有更多了）
	},
	computed: {
		// 支付宝小程序需写成计算属性,prop定义default仍报错
		mOption() {
			return this.option || {};
		},
		// 加载中
		isUpLoading() {
			return this.type === 1;
		},
		// 没有更多了
		isUpNoMore() {
			return this.type === 2;
		}
	}
};
</script>

<style>
@import './mescroll-up.css';
</style>
