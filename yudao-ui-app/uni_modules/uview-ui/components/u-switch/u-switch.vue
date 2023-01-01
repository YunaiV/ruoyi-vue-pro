<template>
	<view
	    class="u-switch"
	    :class="[disabled && 'u-switch--disabled']"
	    :style="[switchStyle, $u.addStyle(customStyle)]"
	    @tap="clickHandler"
	>
		<view
		    class="u-switch__bg"
		    :style="[bgStyle]"
		>
		</view>
		<view
		    class="u-switch__node"
		    :class="[value && 'u-switch__node--on']"
		    :style="[nodeStyle]"
		    ref="u-switch__node"
		>
			<u-loading-icon
			    :show="loading"
			    mode="circle"
			    timingFunction='linear'
			    :color="value ? activeColor : '#AAABAD'"
			    :size="size * 0.6"
			/>
		</view>
	</view>
</template>

<script>
	import props from './props.js';
	/**
	 * switch 开关选择器
	 * @description 选择开关一般用于只有两个选择，且只能选其一的场景。
	 * @tutorial https://www.uviewui.com/components/switch.html
	 * @property {Boolean}						loading			是否处于加载中（默认 false ）
	 * @property {Boolean}						disabled		是否禁用（默认 false ）
	 * @property {String | Number}				size			开关尺寸，单位px （默认 25 ）
	 * @property {String}						activeColor		打开时的背景色 （默认 '#2979ff' ）
	 * @property {String} 						inactiveColor	关闭时的背景色 （默认 '#ffffff' ）
	 * @property {Boolean | String | Number}	value			通过v-model双向绑定的值 （默认 false ）
	 * @property {Boolean | String | Number}	activeValue		打开选择器时通过change事件发出的值 （默认 true ）
	 * @property {Boolean | String | Number}	inactiveValue	关闭选择器时通过change事件发出的值 （默认 false ）
	 * @property {Boolean}						asyncChange		是否开启异步变更，开启后需要手动控制输入值 （默认 false ）
	 * @property {String | Number}				space			圆点与外边框的距离 （默认 0 ）
	 * @property {Object}						customStyle		定义需要用到的外部样式
	 *
	 * @event {Function} change 在switch打开或关闭时触发
	 * @example <u-switch v-model="checked" active-color="red" inactive-color="#eee"></u-switch>
	 */
	export default {
		name: "u-switch",
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		watch: {
			value: {
				immediate: true,
				handler(n) {
					if(n !== this.inactiveValue && n !== this.activeValue) {
						uni.$u.error('v-model绑定的值必须为inactiveValue、activeValue二者之一')
					}
				}
			}
		},
		data() {
			return {
				bgColor: '#ffffff'
			}
		},
		computed: {
			isActive(){
				return this.value === this.activeValue;
			},
			switchStyle() {
				let style = {}
				// 这里需要加2，是为了腾出边框的距离，否则圆点node会和外边框紧贴在一起
				style.width = uni.$u.addUnit(this.size * 2 + 2)
				style.height = uni.$u.addUnit(Number(this.size) + 2)
				// style.borderColor = this.value ? 'rgba(0, 0, 0, 0)' : 'rgba(0, 0, 0, 0.12)'
				// 如果自定义了“非激活”演示，name边框颜色设置为透明(跟非激活颜色一致)
				// 这里不能简单的设置为非激活的颜色，否则打开状态时，会有边框，所以需要透明
				if(this.customInactiveColor) {
					style.borderColor = 'rgba(0, 0, 0, 0)'
				}
				style.backgroundColor = this.isActive ? this.activeColor : this.inactiveColor
				return style;
			},
			nodeStyle() {
				let style = {}
				// 如果自定义非激活颜色，将node圆点的尺寸减少两个像素，让其与外边框距离更大一点
				style.width = uni.$u.addUnit(this.size - this.space)
				style.height = uni.$u.addUnit(this.size - this.space)
				const translateX = this.isActive ? uni.$u.addUnit(this.space) : uni.$u.addUnit(this.size);
				style.transform = `translateX(-${translateX})`
				return style
			},
			bgStyle() {
				let style = {}
				// 这里配置一个多余的元素在HTML中，是为了让switch切换时，有更良好的背景色扩充体验(见实际效果)
				style.width = uni.$u.addUnit(Number(this.size) * 2 - this.size / 2)
				style.height = uni.$u.addUnit(this.size)
				style.backgroundColor = this.inactiveColor
				// 打开时，让此元素收缩，否则反之
				style.transform = `scale(${this.isActive ? 0 : 1})`
				return style
			},
			customInactiveColor() {
				// 之所以需要判断是否自定义了“非激活”颜色，是为了让node圆点离外边框更宽一点的距离
				return this.inactiveColor !== '#fff' && this.inactiveColor !== '#ffffff'
			}
		},
		methods: {
			clickHandler() {
				if (!this.disabled && !this.loading) {
					const oldValue = this.isActive ? this.inactiveValue : this.activeValue
					if(!this.asyncChange) {
						this.$emit('input', oldValue)
					}
					// 放到下一个生命周期，因为双向绑定的value修改父组件状态需要时间，且是异步的
					this.$nextTick(() => {
						this.$emit('change', oldValue)
					})
				}
			}
		}
	};
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-switch {
		@include flex(row);
		/* #ifndef APP-NVUE */
		box-sizing: border-box;
		/* #endif */
		position: relative;
		background-color: #fff;
		border-width: 1px;
		border-radius: 100px;
		transition: background-color 0.4s;
		border-color: rgba(0, 0, 0, 0.12);
		border-style: solid;
		justify-content: flex-end;
		align-items: center;
		// 由于weex为阿里逗着玩的KPI项目，导致bug奇多，这必须要写这一行，
		// 否则在iOS上，点击页面任意地方，都会触发switch的点击事件
		overflow: hidden;

		&__node {
			@include flex(row);
			align-items: center;
			justify-content: center;
			border-radius: 100px;
			background-color: #fff;
			border-radius: 100px;
			box-shadow: 1px 1px 1px 0 rgba(0, 0, 0, 0.25);
			transition-property: transform;
			transition-duration: 0.4s;
			transition-timing-function: cubic-bezier(0.3, 1.05, 0.4, 1.05);
		}

		&__bg {
			position: absolute;
			border-radius: 100px;
			background-color: #FFFFFF;
			transition-property: transform;
			transition-duration: 0.4s;
			border-top-left-radius: 0;
			border-bottom-left-radius: 0;
			transition-timing-function: ease;
		}

		&--disabled {
			opacity: 0.6;
		}
	}
</style>
