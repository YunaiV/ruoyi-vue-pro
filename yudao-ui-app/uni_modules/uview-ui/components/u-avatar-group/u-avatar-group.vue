<template>
	<view class="u-avatar-group">
		<view
		    class="u-avatar-group__item"
		    v-for="(item, index) in showUrl"
		    :key="index"
		    :style="{
				marginLeft: index === 0 ? 0 : $u.addUnit(-size * gap)
			}"
		>
			<u-avatar
			    :size="size"
			    :shape="shape"
			    :mode="mode"
			    :src="$u.test.object(item) ? keyName && item[keyName] || item.url : item"
			></u-avatar>
			<view
			    class="u-avatar-group__item__show-more"
			    v-if="showMore && index === showUrl.length - 1 && (urls.length > maxCount || extraValue > 0)"
				@tap="clickHandler"
			>
				<u--text
				    color="#ffffff"
				    :size="size * 0.4"
				    :text="`+${extraValue || urls.length - showUrl.length}`"
					align="center"
					customStyle="justify-content: center"
				></u--text>
			</view>
		</view>
	</view>
</template>

<script>
	import props from './props.js';
	/**
	 * AvatarGroup  头像组
	 * @description 本组件一般用于展示头像的地方，如个人中心，或者评论列表页的用户头像展示等场所。
	 * @tutorial https://www.uviewui.com/components/avatar.html
	 * 
	 * @property {Array}           urls     头像图片组 （默认 [] ）
	 * @property {String | Number} maxCount 最多展示的头像数量 （ 默认 5 ）
	 * @property {String}          shape    头像形状（ 'circle' (默认) | 'square' ）
	 * @property {String}          mode     图片裁剪模式（默认 'scaleToFill' ）
	 * @property {Boolean}         showMore 超出maxCount时是否显示查看更多的提示 （默认 true ）
	 * @property {String | Number} size      头像大小 （默认 40 ）
	 * @property {String}          keyName  指定从数组的对象元素中读取哪个属性作为图片地址 
	 * @property {String | Number} gap      头像之间的遮挡比例（0.4代表遮挡40%）  （默认 0.5 ）
	 * @property {String | Number} extraValue  需额外显示的值
	 * @event    {Function}        showMore 头像组更多点击
	 * @example  <u-avatar-group:urls="urls" size="35" gap="0.4" ></u-avatar-group:urls=>
	 */
	export default {
		name: 'u-avatar-group',
		mixins: [uni.$u.mpMixin, uni.$u.mixin, props],
		data() {
			return {

			}
		},
		computed: {
			showUrl() {
				return this.urls.slice(0, this.maxCount)
			}
		},
		methods: {
			clickHandler() {
				this.$emit('showMore')
			}
		},
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-avatar-group {
		@include flex;

		&__item {
			margin-left: -10px;
			position: relative;

			&--no-indent {
				// 如果你想质疑作者不会使用:first-child，说明你太年轻，因为nvue不支持
				margin-left: 0;
			}

			&__show-more {
				position: absolute;
				top: 0;
				bottom: 0;
				left: 0;
				right: 0;
				background-color: rgba(0, 0, 0, 0.3);
				@include flex;
				align-items: center;
				justify-content: center;
				border-radius: 100px;
			}
		}
	}
</style>
