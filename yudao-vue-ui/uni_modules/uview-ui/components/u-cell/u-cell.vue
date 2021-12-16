<template>
	<view class="u-cell" :class="[customClass]" :style="[$u.addStyle(customStyle)]"
		:hover-class="(!disabled && (clickable || isLink)) ? 'u-cell--clickable' : ''" :hover-stay-time="250"
		@tap="clickHandler">
		<view class="u-cell__body" :class="[ center && 'u-cell--center', size === 'large' && 'u-cell__body--large']">
			<view class="u-cell__body__content">
				<view class="u-cell__left-icon-wrap" v-if="$slots.icon || icon">
					<slot name="icon" v-if="$slots.icon">
					</slot>
					<u-icon v-else :name="icon" :custom-style="iconStyle" :size="size === 'large' ? 22 : 18"></u-icon>
				</view>
				<view class="u-cell__title">
					<slot name="title">
						<text v-if="title" class="u-cell__title-text"
							:class="[disabled && 'u-cell--disabled', size === 'large' && 'u-cell__title-text--large']">{{ title }}</text>
					</slot>
					<slot name="label">
						<text class="u-cell__label" v-if="label"
							:class="[disabled && 'u-cell--disabled', size === 'large' && 'u-cell__label--large']">{{ label }}</text>
					</slot>
				</view>
			</view>
			<slot name="value">
				<text class="u-cell__value"
					:class="[disabled && 'u-cell--disabled', size === 'large' && 'u-cell__value--large']"
					v-if="!$u.test.empty(value)">{{ value }}</text>
			</slot>
			<view class="u-cell__right-icon-wrap" v-if="$slots['right-icon'] || isLink"
				:class="[`u-cell__right-icon-wrap--${arrowDirection}`]">
				<slot name="right-icon" v-if="$slots['right-icon']">
				</slot>
				<u-icon v-else :name="rightIcon" :custom-style="rightIconStyle" :color="disabled ? '#c8c9cc' : 'info'"
					:size="size === 'large' ? 18 : 16"></u-icon>
			</view>
		</view>
		<u-line v-if="border"></u-line>
	</view>
</template>

<script>
	import props from './props.js';
	/**
	 * cell  单元格
	 * @description cell单元格一般用于一组列表的情况，比如个人中心页，设置页等。
	 * @tutorial https://uviewui.com/components/cell.html
	 * @property {String | Number}	title			标题
	 * @property {String | Number}	label			标题下方的描述信息
	 * @property {String | Number}	value			右侧的内容
	 * @property {String}			icon			左侧图标名称，或者图片链接(本地文件建议使用绝对地址)
	 * @property {String | Number}	titleWidth		标题的宽度，单位任意，数值默认为px单位
	 * @property {Boolean}			disabled		是否禁用cell	
	 * @property {Boolean}			border			是否显示下边框 (默认 true )
	 * @property {Boolean}			center			内容是否垂直居中(主要是针对右侧的value部分) (默认 false )
	 * @property {String}			url				点击后跳转的URL地址
	 * @property {String}			linkType		链接跳转的方式，内部使用的是uView封装的route方法，可能会进行拦截操作 (默认 'navigateTo' )
	 * @property {Boolean}			clickable		是否开启点击反馈(表现为点击时加上灰色背景) （默认 false ） 
	 * @property {Boolean}			isLink			是否展示右侧箭头并开启点击反馈 （默认 false ）
	 * @property {Boolean}			required		是否显示表单状态下的必填星号(此组件可能会内嵌入input组件) （默认 false ）
	 * @property {String}			rightIcon		右侧的图标箭头 （默认 'arrow-right'）
	 * @property {String}			arrowDirection	右侧箭头的方向，可选值为：left，up，down
	 * @property {Object}			rightIconStyle	右侧箭头图标的样式
	 * @property {Object}			titleStyle		标题的样式
	 * @property {String}			size			单位元的大小，可选值为 large，normal，mini 
	 * @property {Boolean}			stop			点击cell是否阻止事件传播 (默认 true )
	 * @property {Object}			customStyle		定义需要用到的外部样式
	 * 
	 * @event {Function}			click			点击cell列表时触发
	 * @example 该组件需要搭配cell-group组件使用，见官方文档示例
	 */
	export default {
		name: 'u-cell',
		data() {
			return {

			}
		},
		mixins: [uni.$u.mpMixin, uni.$u.mixin, props],
		methods: {
			// 点击cell
			clickHandler(e) {
				if (this.disabled) return
				this.$emit('click', {
					name: this.name
				})
				// 如果配置了url(此props参数通过mixin引入)参数，跳转页面
				this.openPage()
				// 是否阻止事件传播
				this.stop && this.preventEvent(e)
			},
		}
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	$u-cell-padding: 10px 15px !default;
	$u-cell-font-size: 15px !default;
	$u-cell-line-height: 24px !default;
	$u-cell-color: $u-main-color !default;
	$u-cell-icon-size: 16px !default;
	$u-cell-title-font-size: 15px !default;
	$u-cell-title-line-height: 22px !default;
	$u-cell-title-color: $u-main-color !default;
	$u-cell-label-font-size: 12px !default;
	$u-cell-label-color: $u-tips-color !default;
	$u-cell-label-line-height: 18px !default;
	$u-cell-value-font-size: 14px !default;
	$u-cell-value-color: $u-content-color !default;
	$u-cell-clickable-color: $u-bg-color !default;
	$u-cell-disabled-color: #c8c9cc !default;
	$u-cell-padding-top-large: 13px !default;
	$u-cell-padding-bottom-large: 13px !default;
	$u-cell-value-font-size-large: 15px !default;
	$u-cell-label-font-size-large: 14px !default;
	$u-cell-title-font-size-large: 16px !default;
	$u-cell-left-icon-wrap-margin-right: 4px !default;
	$u-cell-right-icon-wrap-margin-left: 4px !default;
	$u-cell-title-flex:1 !default;
	$u-cell-label-margin-top:5px !default;


	.u-cell {
		&__body {
			@include flex();
			/* #ifndef APP-NVUE */
			box-sizing: border-box;
			/* #endif */
			padding: $u-cell-padding;
			font-size: $u-cell-font-size;
			color: $u-cell-color;
			// line-height: $u-cell-line-height;
			align-items: center;

			&__content {
				@include flex(row);
				align-items: center;
				flex: 1;
			}

			&--large {
				padding-top: $u-cell-padding-top-large;
				padding-bottom: $u-cell-padding-bottom-large;
			}
		}

		&__left-icon-wrap,
		&__right-icon-wrap {
			@include flex();
			align-items: center;
			// height: $u-cell-line-height;
			font-size: $u-cell-icon-size;
		}

		&__left-icon-wrap {
			margin-right: $u-cell-left-icon-wrap-margin-right;
		}

		&__right-icon-wrap {
			margin-left: $u-cell-right-icon-wrap-margin-left;
			transition: transform 0.3s;

			&--up {
				transform: rotate(-90deg);
			}

			&--down {
				transform: rotate(90deg);
			}
		}

		&__title {
			flex: $u-cell-title-flex;

			&-text {
				font-size: $u-cell-title-font-size;
				line-height: $u-cell-title-line-height;
				color: $u-cell-title-color;

				&--large {
					font-size: $u-cell-title-font-size-large;
				}
			}

		}

		&__label {
			margin-top: $u-cell-label-margin-top;
			font-size: $u-cell-label-font-size;
			color: $u-cell-label-color;
			line-height: $u-cell-label-line-height;

			&--large {
				font-size: $u-cell-label-font-size-large;
			}
		}

		&__value {
			text-align: right;
			font-size: $u-cell-value-font-size;
			line-height: $u-cell-line-height;
			color: $u-cell-value-color;

			&--large {
				font-size: $u-cell-value-font-size-large;
			}
		}

		&--clickable {
			background-color: $u-cell-clickable-color;
		}

		&--disabled {
			color: $u-cell-disabled-color;
			/* #ifndef APP-NVUE */
			cursor: not-allowed;
			/* #endif */
		}

		&--center {
			align-items: center;
		}
	}
</style>
