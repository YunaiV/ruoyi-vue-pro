<template>
	<view class="u-toast">
		<u-overlay
			:show="isShow"
			:custom-style="overlayStyle"
		>
			<view
				class="u-toast__content"
				:style="[contentStyle]"
				:class="['u-type-' + tmpConfig.type, (tmpConfig.type === 'loading' || tmpConfig.loading) ?  'u-toast__content--loading' : '']"
			>
				<u-loading-icon
					v-if="tmpConfig.type === 'loading'"
					mode="circle"
					color="rgb(255, 255, 255)"
					inactiveColor="rgb(120, 120, 120)"
					size="25"
				></u-loading-icon>
				<u-icon
					v-else-if="tmpConfig.type !== 'defalut' && iconName"
					:name="iconName"
					size="17"
					:color="tmpConfig.type"
					:customStyle="iconStyle"
				></u-icon>
				<u-gap
					v-if="tmpConfig.type === 'loading' || tmpConfig.loading"
					height="12"
					bgColor="transparent"
				></u-gap>
				<text
					class="u-toast__content__text"
					:class="['u-toast__content__text--' + tmpConfig.type]"
					style="max-width: 400rpx;"
				>{{ tmpConfig.message }}</text>
			</view>
		</u-overlay>
	</view>
</template>

<script>
	/**
	 * toast 消息提示
	 * @description 此组件表现形式类似uni的uni.showToastAPI，但也有不同的地方。
	 * @tutorial https://www.uviewui.com/components/toast.html
	 * @property {String | Number}	zIndex		toast展示时的zIndex值 (默认 10090 )
	 * @property {Boolean}			loading		是否加载中 （默认 false ）
	 * @property {String | Number}	message		显示的文字内容
	 * @property {String}			icon		图标，或者绝对路径的图片
	 * @property {String}			type		主题类型 （默认 default）
	 * @property {Boolean}			show		是否显示该组件 （默认 false）
	 * @property {Boolean}			overlay		是否显示透明遮罩，防止点击穿透 （默认 false ）
	 * @property {String}			position	位置 （默认 'center' ）
	 * @property {Object}			params		跳转的参数 
	 * @property {String | Number}  duration	展示时间，单位ms （默认 2000 ）
	 * @property {Boolean}			isTab		是否返回的为tab页面 （默认 false ）
	 * @property {String}			url			toast消失后是否跳转页面，有则跳转，优先级高于back参数 
	 * @property {Function}			complete	执行完后的回调函数 
	 * @property {Boolean}			back		结束toast是否自动返回上一页 （默认 false ）
	 * @property {Object}			customStyle	组件的样式，对象形式
	 * @event {Function} show 显示toast，如需一进入页面就显示toast，请在onReady生命周期调用
	 * @example <u-toast ref="uToast" />
	 */
	export default {
		name: 'u-toast',
		mixins: [uni.$u.mpMixin, uni.$u.mixin],
		data() {
			return {
				isShow: false,
				timer: null, // 定时器
				config: {
					message: '', // 显示文本
					type: '', // 主题类型，primary，success，error，warning，black
					duration: 2000, // 显示的时间，毫秒
					icon: true, // 显示的图标
					position: 'center', // toast出现的位置
					complete: null, // 执行完后的回调函数
					overlay: false, // 是否防止触摸穿透
					loading: false, // 是否加载中状态
				},
				tmpConfig: {}, // 将用户配置和内置配置合并后的临时配置变量
			}
		},
		computed: {
			iconName() {
				// 只有不为none，并且type为error|warning|succes|info时候，才显示图标
				if(!this.tmpConfig.icon || this.tmpConfig.icon == 'none') {
					return '';
				}
				if (['error', 'warning', 'success', 'primary'].includes(this.tmpConfig.type)) {
					return uni.$u.type2icon(this.tmpConfig.type)
				} else {
					return ''
				}
			},
			overlayStyle() {
				const style = {
					justifyContent: 'center',
					alignItems: 'center',
					display: 'flex'
				}
				// 将遮罩设置为100%透明度，避免出现灰色背景
				style.backgroundColor = 'rgba(0, 0, 0, 0)'
				return style
			},
			iconStyle() {
				const style = {}
				// 图标需要一个右边距，以跟右边的文字有隔开的距离
				style.marginRight = '4px'
				// #ifdef APP-NVUE
				// iOSAPP下，图标有1px的向下偏移，这里进行修正
				if (uni.$u.os() === 'ios') {
					style.marginTop = '-1px'
				}
				// #endif
				return style
			},
			loadingIconColor() {
				let color = 'rgb(255, 255, 255)'
				if (['error', 'warning', 'success', 'primary'].includes(this.tmpConfig.type)) {
					// loading-icon组件内部会对color参数进行一个透明度处理，该方法要求传入的颜色值
					// 必须为rgb格式的，所以这里做一个处理
					color = uni.$u.hexToRgb(uni.$u.color[this.tmpConfig.type])
				}
				return color
			},
			// 内容盒子的样式
			contentStyle() {
				const windowHeight = uni.$u.sys().windowHeight, style = {}
				let value = 0
				// 根据top和bottom，对Y轴进行窗体高度的百分比偏移
				if(this.tmpConfig.position === 'top') {
					value = - windowHeight * 0.25
				} else if(this.tmpConfig.position === 'bottom') {
					value = windowHeight * 0.25
				}
				style.transform = `translateY(${value}px)`
				return style
			}
		},
		created() {
			// 通过主题的形式调用toast，批量生成方法函数
			['primary', 'success', 'error', 'warning', 'default', 'loading'].map(item => {
				this[item] = message => this.show({
					type: item,
					message
				})
			})
		},
		methods: {
			// 显示toast组件，由父组件通过this.$refs.xxx.show(options)形式调用
			show(options) {
				// 不将结果合并到this.config变量，避免多次调用u-toast，前后的配置造成混乱
				this.tmpConfig = uni.$u.deepMerge(this.config, options)
				// 清除定时器
				this.clearTimer()
				this.isShow = true
				this.timer = setTimeout(() => {
					// 倒计时结束，清除定时器，隐藏toast组件
					this.clearTimer()
					// 判断是否存在callback方法，如果存在就执行
					typeof(this.tmpConfig.complete) === 'function' && this.tmpConfig.complete()
				}, this.tmpConfig.duration)
			},
			// 隐藏toast组件，由父组件通过this.$refs.xxx.hide()形式调用
			hide() {
				this.clearTimer()
			},
			clearTimer() {
				this.isShow = false
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

	$u-toast-color:#fff !default;
	$u-toast-border-radius:4px !default;
	$u-toast-border-background-color:#585858 !default;
	$u-toast-border-font-size:14px !default;
	$u-toast-border-padding:12px 20px !default;
	$u-toast-loading-border-padding: 20px 20px !default;
	$u-toast-content-text-color:#fff !default;
	$u-toast-content-text-font-size:15px !default;
	$u-toast-u-icon:10rpx !default;
	$u-toast-u-type-primary-color:$u-primary !default;
	$u-toast-u-type-primary-background-color:#ecf5ff !default;
	$u-toast-u-type-primary-border-color:rgb(215, 234, 254) !default;
	$u-toast-u-type-primary-border-width:1px !default;
	$u-toast-u-type-success-color: $u-success !default;
	$u-toast-u-type-success-background-color: #dbf1e1 !default;
	$u-toast-u-type-success-border-color: #BEF5C8 !default;
	$u-toast-u-type-success-border-width: 1px !default;
	$u-toast-u-type-error-color:$u-error !default;
	$u-toast-u-type-error-background-color:#fef0f0 !default;
	$u-toast-u-type-error-border-color:#fde2e2 !default;
	$u-toast-u-type-error-border-width: 1px !default;
	$u-toast-u-type-warning-color:$u-warning !default;
	$u-toast-u-type-warning-background-color:#fdf6ec !default;
	$u-toast-u-type-warning-border-color:#faecd8 !default;
	$u-toast-u-type-warning-border-width: 1px !default;
	$u-toast-u-type-default-color:#fff !default;
	$u-toast-u-type-default-background-color:#585858 !default;

	.u-toast {
		&__content {
			@include flex;
			padding: $u-toast-border-padding;
			border-radius: $u-toast-border-radius;
			background-color: $u-toast-border-background-color;
			color: $u-toast-color;
			align-items: center;
			/* #ifndef APP-NVUE */
			max-width: 600rpx;
			/* #endif */
			position: relative;

			&--loading {
				flex-direction: column;
				padding: $u-toast-loading-border-padding;
			}

			&__text {
				color: $u-toast-content-text-color;
				font-size: $u-toast-content-text-font-size;
				line-height: $u-toast-content-text-font-size;

				&--default {
					color: $u-toast-content-text-color;
				}

				&--error {
					color: $u-error;
				}

				&--primary {
					color: $u-primary;
				}

				&--success {
					color: $u-success;
				}

				&--warning {
					color: $u-warning;
				}
			}
		}
	}

	.u-type-primary {
		color: $u-toast-u-type-primary-color;
		background-color: $u-toast-u-type-primary-background-color;
		border-color: $u-toast-u-type-primary-border-color;
		border-width: $u-toast-u-type-primary-border-width;
	}

	.u-type-success {
		color: $u-toast-u-type-success-color;
		background-color: $u-toast-u-type-success-background-color;
		border-color: $u-toast-u-type-success-border-color;
		border-width: 1px;
	}

	.u-type-error {
		color: $u-toast-u-type-error-color;
		background-color: $u-toast-u-type-error-background-color;
		border-color: $u-toast-u-type-error-border-color;
		border-width: $u-toast-u-type-error-border-width;
	}

	.u-type-warning {
		color: $u-toast-u-type-warning-color;
		background-color: $u-toast-u-type-warning-background-color;
		border-color: $u-toast-u-type-warning-border-color;
		border-width: 1px;
	}

	.u-type-default {
		color: $u-toast-u-type-default-color;
		background-color: $u-toast-u-type-default-background-color;
	}
</style>
