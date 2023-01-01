<template>
	<u-transition
		mode="slide-down"
		:customStyle="containerStyle"
		:show="open"
	>
		<view
			class="u-notify"
			:class="[`u-notify--${tmpConfig.type}`]"
			:style="[backgroundColor, $u.addStyle(customStyle)]"
		>
			<u-status-bar v-if="tmpConfig.safeAreaInsetTop"></u-status-bar>
			<view class="u-notify__warpper">
				<slot name="icon">
					<u-icon
						v-if="['success', 'warning', 'error'].includes(tmpConfig.type)"
						:name="tmpConfig.icon"
						:color="tmpConfig.color"
						:size="1.3 * tmpConfig.fontSize"
						:customStyle="{marginRight: '4px'}"
					></u-icon>
				</slot>
				<text
					class="u-notify__warpper__text"
					:style="{
						fontSize: $u.addUnit(tmpConfig.fontSize),
						color: tmpConfig.color
					}"
				>{{ tmpConfig.message }}</text>
			</view>
		</view>
	</u-transition>
</template>

<script>
	import props from './props.js';
	/**
	 * notify 顶部提示
	 * @description 该组件一般用于页面顶部向下滑出一个提示，尔后自动收起的场景
	 * @tutorial
	 * @property {String | Number}	top					到顶部的距离 ( 默认 0 )
	 * @property {String}			type				主题，primary，success，warning，error ( 默认 'primary' )
	 * @property {String}			color				字体颜色 ( 默认 '#ffffff' )
	 * @property {String}			bgColor				背景颜色
	 * @property {String}			message				展示的文字内容
	 * @property {String | Number}	duration			展示时长，为0时不消失，单位ms ( 默认 3000 )
	 * @property {String | Number}	fontSize			字体大小 ( 默认 15 )
	 * @property {Boolean}			safeAreaInsetTop	是否留出顶部安全距离（状态栏高度） ( 默认 false )
	 * @property {Object}			customStyle			组件的样式，对象形式
	 * @event {Function}	open	开启组件时调用的函数
	 * @event {Function}	close	关闭组件式调用的函数
	 * @example <u-notify message="Hi uView"></u-notify>
	 */
	export default {
		name: 'u-notify',
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		data() {
			return {
				// 是否展示组件
				open: false,
				timer: null,
				config: {
					// 到顶部的距离
					top: uni.$u.props.notify.top,
					// type主题，primary，success，warning，error
					type: uni.$u.props.notify.type,
					// 字体颜色
					color: uni.$u.props.notify.color,
					// 背景颜色
					bgColor: uni.$u.props.notify.bgColor,
					// 展示的文字内容
					message: uni.$u.props.notify.message,
					// 展示时长，为0时不消失，单位ms
					duration: uni.$u.props.notify.duration,
					// 字体大小
					fontSize: uni.$u.props.notify.fontSize,
					// 是否留出顶部安全距离（状态栏高度）
					safeAreaInsetTop: uni.$u.props.notify.safeAreaInsetTop
				},
				// 合并后的配置，避免多次调用组件后，可能会复用之前使用的配置参数
				tmpConfig: {}
			}
		},
		computed: {
			containerStyle() {
				let top = 0
				if (this.tmpConfig.top === 0) {
					// #ifdef H5
					// H5端，导航栏为普通元素，需要将组件移动到导航栏的下边沿
					// H5的导航栏高度为44px
					top = 44
					// #endif
				}
				const style = {
					top: uni.$u.addUnit(this.tmpConfig.top === 0 ? top : this.tmpConfig.top),
					// 因为组件底层为u-transition组件，必须将其设置为fixed定位
					// 让其出现在导航栏底部
					position: 'fixed',
					left: 0,
					right: 0,
					zIndex: 10076
				}
				return style
			},
			// 组件背景颜色
			backgroundColor() {
				const style = {}
				if (this.tmpConfig.bgColor) {
					style.backgroundColor = this.tmpConfig.bgColor
				}
				return style
			},
			// 默认主题下的图标
			icon() {
				let icon
				if (this.tmpConfig.type === 'success') {
					icon = 'checkmark-circle'
				} else if (this.tmpConfig.type === 'error') {
					icon = 'close-circle'
				} else if (this.tmpConfig.type === 'warning') {
					icon = 'error-circle'
				}
				return icon
			}
		},
		created() {
			// 通过主题的形式调用toast，批量生成方法函数
			['primary', 'success', 'error', 'warning'].map(item => {
				this[item] = message => this.show({
					type: item,
					message
				})
			})
		},
		methods: {
			show(options) {
				// 不将结果合并到this.config变量，避免多次调用u-toast，前后的配置造成混乱
				this.tmpConfig = uni.$u.deepMerge(this.config, options)
				// 任何定时器初始化之前，都要执行清除操作，否则可能会造成混乱
				this.clearTimer()
				this.open = true
				if (this.tmpConfig.duration > 0) {
					this.timer = setTimeout(() => {
						this.open = false
						// 倒计时结束，清除定时器，隐藏toast组件
						this.clearTimer()
						// 判断是否存在callback方法，如果存在就执行
						typeof(this.tmpConfig.complete) === 'function' && this.tmpConfig.complete()
					}, this.tmpConfig.duration)
				}
			},
			// 关闭notify
			close() {
				this.clearTimer()
			},
			clearTimer() {
				this.open = false
				// 清除定时器
				clearTimeout(this.timer)
				this.timer = null
			}
		},
		beforeDestroy() {
			this.clearTimer()
		}
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	$u-notify-padding: 8px 10px !default;
	$u-notify-text-font-size: 15px !default;
	$u-notify-primary-bgColor: $u-primary !default;
	$u-notify-success-bgColor: $u-success !default;
	$u-notify-error-bgColor: $u-error !default;
	$u-notify-warning-bgColor: $u-warning !default;


	.u-notify {
		padding: $u-notify-padding;

		&__warpper {
			@include flex;
			align-items: center;
			text-align: center;
			justify-content: center;

			&__text {
				font-size: $u-notify-text-font-size;
				text-align: center;
			}
		}

		&--primary {
			background-color: $u-notify-primary-bgColor;
		}

		&--success {
			background-color: $u-notify-success-bgColor;
		}

		&--error {
			background-color: $u-notify-error-bgColor;
		}

		&--warning {
			background-color: $u-notify-warning-bgColor;
		}
	}
</style>
