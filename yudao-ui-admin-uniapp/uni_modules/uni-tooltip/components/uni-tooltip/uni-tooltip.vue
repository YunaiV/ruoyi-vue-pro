<template>
	<view class="uni-tooltip">
		<slot></slot>
		<view v-if="content || $slots.content" class="uni-tooltip-popup">
			<slot name="content">
				{{content}}
			</slot>
		</view>
	</view>
</template>


<script>
	/**
	 * Tooltip 提示文字
	 * @description 常用于展示鼠标 hover 时的提示信息。
	 * @tutorial https://uniapp.dcloud.io/component/uniui/uni-tooltip
	 * @property {String} content   弹出层显示的内容
	 * @property {String}  placement出现位置, 目前只支持 left
	 */


	export default {
		name: "uni-tooltip",
		data() {
			return {

			};
		},
		props: {
			content: {
				type: String,
				default: ''
			},

			placement: {
				type: String,
				default: 'bottom'
			},
		}
	}
</script>

<style>
	.uni-tooltip {
		position: relative;
		cursor: pointer;
	}

	.uni-tooltip-popup {
		z-index: 1;
		display: none;
		position: absolute;
		left: 0;
		background-color: #333;
		border-radius: 8px;
		color: #fff;
		font-size: 12px;
		text-align: left;
		line-height: 16px;
		padding: 12px;
	}


	.uni-tooltip:hover .uni-tooltip-popup {
		display: block;
	}
</style>
