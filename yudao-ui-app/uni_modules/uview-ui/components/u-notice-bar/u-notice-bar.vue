<template>
	<view
		class="u-notice-bar"
		v-if="show"
		:style="[{
			backgroundColor: bgColor
		}, $u.addStyle(customStyle)]"
	>
		<template v-if="direction === 'column' || (direction === 'row' && step)">
			<u-column-notice
				:color="color"
				:bgColor="bgColor"
				:text="text"
				:mode="mode"
				:step="step"
				:icon="icon"
				:disable-touch="disableTouch"
				:fontSize="fontSize"
				:duration="duration"
				@close="close"
				@click="click"
			></u-column-notice>
		</template>
		<template v-else>
			<u-row-notice
				:color="color"
				:bgColor="bgColor"
				:text="text"
				:mode="mode"
				:fontSize="fontSize"
				:speed="speed"
				:url="url"
				:linkType="linkType"
				:icon="icon"
				@close="close"
				@click="click"
			></u-row-notice>
		</template>
	</view>
</template>
<script>
	import props from './props.js';

	/**
	 * noticeBar 滚动通知
	 * @description 该组件用于滚动通告场景，有多种模式可供选择
	 * @tutorial https://www.uviewui.com/components/noticeBar.html
	 * @property {Array | String}	text			显示的内容，数组
	 * @property {String}			direction		通告滚动模式，row-横向滚动，column-竖向滚动 ( 默认 'row' )
	 * @property {Boolean}			step			direction = row时，是否使用步进形式滚动  ( 默认 false )
	 * @property {String}			icon			是否显示左侧的音量图标 ( 默认 'volume' )
	 * @property {String}			mode			通告模式，link-显示右箭头，closable-显示右侧关闭图标
	 * @property {String}			color			文字颜色，各图标也会使用文字颜色 ( 默认 '#f9ae3d' )
	 * @property {String}			bgColor			背景颜色 ( 默认 '#fdf6ec' )
	 * @property {String | Number}	speed			水平滚动时的滚动速度，即每秒滚动多少px(px)，这有利于控制文字无论多少时，都能有一个恒定的速度 ( 默认 80 )
	 * @property {String | Number}	fontSize		字体大小 ( 默认 14 )
	 * @property {String | Number}	duration		滚动一个周期的时间长，单位ms ( 默认 2000 )
	 * @property {Boolean}			disableTouch	是否禁止用手滑动切换 目前HX2.6.11，只支持App 2.5.5+、H5 2.5.5+、支付宝小程序、字节跳动小程序（默认34） ( 默认 true )
	 * @property {String}			url				跳转的页面路径
	 * @property {String}			linkType		页面跳转的类型 ( 默认 navigateTo )
	 * @property {Object}			customStyle		定义需要用到的外部样式
	 * 
	 * @event {Function}			click			点击通告文字触发
	 * @event {Function}			close			点击右侧关闭图标触发
	 * @example <u-notice-bar :more-icon="true" :list="list"></u-notice-bar>
	 */
	export default {
		name: "u-notice-bar",
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		data() {
			return {
				show: true
			}
		},
		methods: {
			// 点击通告栏
			click(index) {
				this.$emit('click', index)
				if (this.url && this.linkType) {
					// 此方法写在mixin中，另外跳转的url和linkType参数也在mixin的props中
					this.openPage()
				}
			},
			// 点击关闭按钮
			close() {
				this.show = false
				this.$emit('close')
			}
		}
	};
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-notice-bar {
		overflow: hidden;
		padding: 9px 12px;
		flex: 1;
	}
</style>
