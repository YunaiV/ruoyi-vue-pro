<template>
	<view
		class="u-avatar"
		:class="[`u-avatar--${shape}`]"
		:style="[{
			backgroundColor: (text || icon) ? (randomBgColor ? colors[colorIndex !== '' ? colorIndex : $u.random(0, 19)] : bgColor) : 'transparent',
			width: $u.addUnit(size),
			height: $u.addUnit(size),
		}, $u.addStyle(customStyle)]"
		@tap="clickHandler"
	>
		<slot>
			<!-- #ifdef MP-WEIXIN || MP-QQ || MP-BAIDU  -->
			<open-data
				v-if="mpAvatar && allowMp"
				type="userAvatarUrl"
				:style="[{
					width: $u.addUnit(size),
					height: $u.addUnit(size)
				}]"
			/>
			<!-- #endif -->
			<!-- #ifndef MP-WEIXIN && MP-QQ && MP-BAIDU  -->
			<template v-if="mpAvatar && allowMp"></template>
			<!-- #endif -->
			<u-icon
				v-else-if="icon"
				:name="icon"
				:size="fontSize"
				:color="color"
			></u-icon>
			<u--text
				v-else-if="text"
				:text="text"
				:size="fontSize"
				:color="color"
				align="center"
				customStyle="justify-content: center"
			></u--text>
			<image
				class="u-avatar__image"
				v-else
				:class="[`u-avatar__image--${shape}`]"
				:src="avatarUrl || defaultUrl"
				:mode="mode"
				@error="errorHandler"
				:style="[{
					width: $u.addUnit(size),
					height: $u.addUnit(size)
				}]"
			></image>
		</slot>
	</view>
</template>

<script>
	import props from './props.js';
	const base64Avatar =
		"data:image/jpg;base64,/9j/4QAYRXhpZgAASUkqAAgAAAAAAAAAAAAAAP/sABFEdWNreQABAAQAAAA8AAD/4QMraHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLwA8P3hwYWNrZXQgYmVnaW49Iu+7vyIgaWQ9Ilc1TTBNcENlaGlIenJlU3pOVGN6a2M5ZCI/PiA8eDp4bXBtZXRhIHhtbG5zOng9ImFkb2JlOm5zOm1ldGEvIiB4OnhtcHRrPSJBZG9iZSBYTVAgQ29yZSA1LjMtYzAxMSA2Ni4xNDU2NjEsIDIwMTIvMDIvMDYtMTQ6NTY6MjcgICAgICAgICI+IDxyZGY6UkRGIHhtbG5zOnJkZj0iaHR0cDovL3d3dy53My5vcmcvMTk5OS8wMi8yMi1yZGYtc3ludGF4LW5zIyI+IDxyZGY6RGVzY3JpcHRpb24gcmRmOmFib3V0PSIiIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bXA6Q3JlYXRvclRvb2w9IkFkb2JlIFBob3Rvc2hvcCBDUzYgKFdpbmRvd3MpIiB4bXBNTTpJbnN0YW5jZUlEPSJ4bXAuaWlkOjREMEQwRkY0RjgwNDExRUE5OTY2RDgxODY3NkJFODMxIiB4bXBNTTpEb2N1bWVudElEPSJ4bXAuZGlkOjREMEQwRkY1RjgwNDExRUE5OTY2RDgxODY3NkJFODMxIj4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6NEQwRDBGRjJGODA0MTFFQTk5NjZEODE4Njc2QkU4MzEiIHN0UmVmOmRvY3VtZW50SUQ9InhtcC5kaWQ6NEQwRDBGRjNGODA0MTFFQTk5NjZEODE4Njc2QkU4MzEiLz4gPC9yZGY6RGVzY3JpcHRpb24+IDwvcmRmOlJERj4gPC94OnhtcG1ldGE+IDw/eHBhY2tldCBlbmQ9InIiPz7/7gAOQWRvYmUAZMAAAAAB/9sAhAAGBAQEBQQGBQUGCQYFBgkLCAYG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";
	/**
	 * Avatar  头像
	 * @description 本组件一般用于展示头像的地方，如个人中心，或者评论列表页的用户头像展示等场所。
	 * @tutorial https://www.uviewui.com/components/avatar.html
	 *
	 * @property {String}			src				头像路径，如加载失败，将会显示默认头像(不能为相对路径)
	 * @property {String}			shape			头像形状  （ circle (默认) | square）
	 * @property {String | Number}	size			头像尺寸，可以为指定字符串(large, default, mini)，或者数值 （默认 40 ）
	 * @property {String}			mode			头像图片的裁剪类型，与uni的image组件的mode参数一致，如效果达不到需求，可尝试传widthFix值 （默认 'scaleToFill' ）
	 * @property {String}			text			用文字替代图片，级别优先于src
	 * @property {String}			bgColor			背景颜色，一般显示文字时用 （默认 '#c0c4cc' ）
	 * @property {String}			color			文字颜色 （默认 '#ffffff' ）
	 * @property {String | Number}	fontSize		文字大小  （默认 18 ）
	 * @property {String}			icon			显示的图标
	 * @property {Boolean}			mpAvatar		显示小程序头像，只对百度，微信，QQ小程序有效  （默认 false ）
	 * @property {Boolean}			randomBgColor	是否使用随机背景色  （默认 false ）
	 * @property {String}			defaultUrl		加载失败的默认头像(组件有内置默认图片)
	 * @property {String | Number}	colorIndex		如果配置了randomBgColor为true，且配置了此值，则从默认的背景色数组中取出对应索引的颜色值，取值0-19之间
	 * @property {String}			name			组件标识符  （默认 'level' ）
	 * @property {Object}			customStyle		定义需要用到的外部样式
	 *
	 * @event    {Function}        click       点击组件时触发   index: 用户传递的标识符
	 * @example  <u-avatar :src="src" mode="square"></u-avatar>
	 */
	export default {
		name: 'u-avatar',
		mixins: [uni.$u.mpMixin, uni.$u.mixin, props],
		data() {
			return {
				// 如果配置randomBgColor参数为true，在图标或者文字的模式下，会随机从中取出一个颜色值当做背景色
				colors: ['#ffb34b', '#f2bba9', '#f7a196', '#f18080', '#88a867', '#bfbf39', '#89c152', '#94d554', '#f19ec2',
					'#afaae4', '#e1b0df', '#c38cc1', '#72dcdc', '#9acdcb', '#77b1cc', '#448aca', '#86cefa', '#98d1ee',
					'#73d1f1',
					'#80a7dc'
				],
				avatarUrl: this.src,
				allowMp: false
			}
		},
		watch: {
			// 监听头像src的变化，赋值给内部的avatarUrl变量，因为图片加载失败时，需要修改图片的src为默认值
			// 而组件内部不能直接修改props的值，所以需要一个中间变量
			src: {
				immediate: true,
				handler(newVal) {
					this.avatarUrl = newVal
					// 如果没有传src，则主动触发error事件，用于显示默认的头像，否则src为''空字符等的时候，会无内容展示
					if(!newVal) {
						this.errorHandler()
					}
				}
			}
		},
		computed: {
			imageStyle() {
				const style = {}
				return style
			}
		},
		created() {
			this.init()
		},
		methods: {
			init() {
				// 目前只有这几个小程序平台具有open-data标签
				// 其他平台可以通过uni.getUserInfo类似接口获取信息，但是需要弹窗授权(首次)，不合符组件逻辑
				// 故目前自动获取小程序头像只支持这几个平台
				// #ifdef MP-WEIXIN || MP-QQ || MP-BAIDU
				this.allowMp = true
				// #endif
			},
			// 判断传入的name属性，是否图片路径，只要带有"/"均认为是图片形式
			isImg() {
				return this.src.indexOf('/') !== -1
			},
			// 图片加载时失败时触发
			errorHandler() {
				this.avatarUrl = this.defaultUrl || base64Avatar
			},
			clickHandler() {
				this.$emit('click', this.name)
			}
		}
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-avatar {
		@include flex;
		align-items: center;
		justify-content: center;

		&--circle {
			border-radius: 100px;
		}

		&--square {
			border-radius: 4px;
		}

		&__image {
			&--circle {
				border-radius: 100px;
			}

			&--square {
				border-radius: 4px;
			}
		}
	}
</style>
