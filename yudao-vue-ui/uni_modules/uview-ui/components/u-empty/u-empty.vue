<template>
	<view
	    class="u-empty"
	    :style="[emptyStyle]"
	    v-if="show"
	>
		<u-icon
		    v-if="!isSrc"
		    :name="mode === 'message' ? 'chat' : `empty-${mode}`"
		    :size="iconSize"
		    :color="iconColor"
		    margin-top="14"
		></u-icon>
		<image
		    v-else
		    :style="{
				width: $u.addUnit(width),
				height: $u.addUnit(height),
			}"
		    :src="icon"
		    mode="widthFix"
		></image>
		<text
		    class="u-empty__text"
		    :style="[textStyle]"
		>{{text ? text : icons[mode]}}</text>
		<view class="u-empty__wrap" v-if="$slots.default || $slots.$default">
			<slot />
		</view>
	</view>
</template>

<script>
	import props from './props.js';

	/**
	 * empty 内容为空
	 * @description 该组件用于需要加载内容，但是加载的第一页数据就为空，提示一个"没有内容"的场景， 我们精心挑选了十几个场景的图标，方便您使用。
	 * @tutorial https://www.uviewui.com/components/empty.html
	 * @property {String}			icon		内置图标名称，或图片路径，建议绝对路径
	 * @property {String}			text		提示文字
	 * @property {String}			textColor	文字颜色 (默认 '#c0c4cc' )
	 * @property {String | Number}	textSize	文字大小 （默认 14 ）
	 * @property {String}			iconColor	图标的颜色 （默认 '#c0c4cc' ）
	 * @property {String | Number}	iconSize	图标的大小 （默认 90 ）
	 * @property {String}			mode		选择预置的图标类型 （默认 'data' ）
	 * @property {String | Number}	width		图标宽度，单位px （默认 160 ）
	 * @property {String | Number}	height		图标高度，单位px （默认 160 ）
	 * @property {Boolean}			show		是否显示组件 （默认 true ）
	 * @property {String | Number}	marginTop	组件距离上一个元素之间的距离，默认px单位 （默认 0 ）
	 * @property {Object}			customStyle	定义需要用到的外部样式
	 * 
	 * @event {Function} click 点击组件时触发
	 * @event {Function} close 点击关闭按钮时触发
	 * @example <u-empty text="所谓伊人，在水一方" mode="list"></u-empty>
	 */
	export default {
		name: "u-empty",
		mixins: [uni.$u.mpMixin, uni.$u.mixin, props],
		data() {
			return {
				icons: {
					car: '购物车为空',
					page: '页面不存在',
					search: '没有搜索结果',
					address: '没有收货地址',
					wifi: '没有WiFi',
					order: '订单为空',
					coupon: '没有优惠券',
					favor: '暂无收藏',
					permission: '无权限',
					history: '无历史记录',
					news: '无新闻列表',
					message: '消息列表为空',
					list: '列表为空',
					data: '数据为空',
					comment: '暂无评论',
				}
			}
		},
		computed: {
			// 组件样式
			emptyStyle() {
				const style = {}
				style.marginTop = uni.$u.addUnit(this.marginTop)
				// 合并customStyle样式，此参数通过mixin中的props传递
				return uni.$u.deepMerge(uni.$u.addStyle(this.customStyle), style)
			},
			// 文本样式
			textStyle() {
				const style = {}
				style.color = this.textColor
				style.fontSize = uni.$u.addUnit(this.textSize)
				return style
			},
			// 判断icon是否图片路径
			isSrc() {
				return this.icon.indexOf('/') >= 0
			}
		}
	}
</script>

<style lang="scss" scoped>
	@import '../../libs/css/components.scss';
	$u-empty-text-margin-top:20rpx !default;
	$u-empty-slot-margin-top:20rpx !default;

	.u-empty {
		@include flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;

		&__text {
			@include flex;
			justify-content: center;
			align-items: center;
			margin-top: $u-empty-text-margin-top;
		}
	}
		.u-slot-wrap {
			@include flex;
			justify-content: center;
			align-items: center;
			margin-top:$u-empty-slot-margin-top;
		}
</style>
