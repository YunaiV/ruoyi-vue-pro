
<template>
	<u-popup
	    :show="show"
	    mode="bottom"
	    @close="closeHandler"
	    :safeAreaInsetBottom="safeAreaInsetBottom"
	    :round="round"
	>
		<view class="u-action-sheet">
			<view
			    class="u-action-sheet__header"
			    v-if="title"
			>
				<text class="u-action-sheet__header__title u-line-1">{{title}}</text>
				<view
				    class="u-action-sheet__header__icon-wrap"
				    @tap.stop="close"
				>
					<u-icon
					    name="close"
					    size="17"
					    color="#c8c9cc"
					    bold
					></u-icon>
				</view>
			</view>
			<text
			    class="u-action-sheet__description"
				:style="[{
					marginTop: `${title && description ? 0 : '18px'}`
				}]"
			    v-if="description"
			>{{description}}</text>
			<slot>
				<u-line v-if="description"></u-line>
				<view class="u-action-sheet__item-wrap">
					<template v-for="(item, index) in actions">
						<!-- #ifdef MP -->
						<button
						    :key="index"
						    class="u-reset-button"
						    :openType="item.openType"
						    @getuserinfo="onGetUserInfo"
						    @contact="onContact"
						    @getphonenumber="onGetPhoneNumber"
						    @error="onError"
						    @launchapp="onLaunchApp"
						    @opensetting="onOpenSetting"
						    :lang="lang"
						    :session-from="sessionFrom"
						    :send-message-title="sendMessageTitle"
						    :send-message-path="sendMessagePath"
						    :send-message-img="sendMessageImg"
						    :show-message-card="showMessageCard"
						    :app-parameter="appParameter"
						    @tap="selectHandler(index)"
						    :hover-class="!item.disabled && !item.loading ? 'u-action-sheet--hover' : ''"
						>
							<!-- #endif -->
							<view
							    class="u-action-sheet__item-wrap__item"
							    @tap.stop="selectHandler(index)"
							    :hover-class="!item.disabled && !item.loading ? 'u-action-sheet--hover' : ''"
							    :hover-stay-time="150"
							>
								<template v-if="!item.loading">
									<text
									    class="u-action-sheet__item-wrap__item__name"
									    :style="[itemStyle(index)]"
									>{{ item.name }}</text>
									<text
									    v-if="item.subname"
									    class="u-action-sheet__item-wrap__item__subname"
									>{{ item.subname }}</text>
								</template>
								<u-loading-icon
								    v-else
								    custom-class="van-action-sheet__loading"
								    size="18"
								    mode="circle"
								/>
							</view>
							<!-- #ifdef MP -->
						</button>
						<!-- #endif -->
						<u-line v-if="index !== actions.length - 1"></u-line>
					</template>
				</view>
			</slot>
			<u-gap
			    bgColor="#eaeaec"
			    height="6"
			    v-if="cancelText"
			></u-gap>
			<view hover-class="u-action-sheet--hover">
				<text
				    @touchmove.stop.prevent
				    :hover-stay-time="150"
				    v-if="cancelText"
				    class="u-action-sheet__cancel-text"
				    @tap="cancel"
				>{{cancelText}}</text>
			</view>
		</view>
	</u-popup>
</template>

<script>
	import openType from '../../libs/mixin/openType'
	import button from '../../libs/mixin/button'
	import props from './props.js';
	/**
	 * ActionSheet 操作菜单
	 * @description 本组件用于从底部弹出一个操作菜单，供用户选择并返回结果。本组件功能类似于uni的uni.showActionSheetAPI，配置更加灵活，所有平台都表现一致。
	 * @tutorial https://www.uviewui.com/components/actionSheet.html
	 * 
	 * @property {Boolean}			show				操作菜单是否展示 （默认 false ）
	 * @property {String}			title				操作菜单标题
	 * @property {String}			description			选项上方的描述信息
	 * @property {Array<Object>}	actions				按钮的文字数组，见官方文档示例
	 * @property {String}			cancelText			取消按钮的提示文字,不为空时显示按钮
	 * @property {Boolean}			closeOnClickAction	点击某个菜单项时是否关闭弹窗 （默认 true ）
	 * @property {Boolean}			safeAreaInsetBottom	处理底部安全区 （默认 true ）
	 * @property {String}			openType			小程序的打开方式 (contact | launchApp | getUserInfo | openSetting ｜getPhoneNumber ｜error )
	 * @property {Boolean}			closeOnClickOverlay	点击遮罩是否允许关闭  (默认 true )
	 * @property {Number|String}	round				圆角值，默认无圆角  (默认 0 )
	 * @property {String}			lang				指定返回用户信息的语言，zh_CN 简体中文，zh_TW 繁体中文，en 英文
	 * @property {String}			sessionFrom			会话来源，openType="contact"时有效
	 * @property {String}			sendMessageTitle	会话内消息卡片标题，openType="contact"时有效
	 * @property {String}			sendMessagePath		会话内消息卡片点击跳转小程序路径，openType="contact"时有效
	 * @property {String}			sendMessageImg		会话内消息卡片图片，openType="contact"时有效
	 * @property {Boolean}			showMessageCard		是否显示会话内消息卡片，设置此参数为 true，用户进入客服会话会在右下角显示"可能要发送的小程序"提示，用户点击后可以快速发送小程序消息，openType="contact"时有效 （默认 false ）
	 * @property {String}			appParameter		打开 APP 时，向 APP 传递的参数，openType=launchApp 时有效
	 * 
	 * @event {Function} select			点击ActionSheet列表项时触发 
	 * @event {Function} close			点击取消按钮时触发
	 * @event {Function} getuserinfo	用户点击该按钮时，会返回获取到的用户信息，回调的 detail 数据与 wx.getUserInfo 返回的一致，openType="getUserInfo"时有效
	 * @event {Function} contact		客服消息回调，openType="contact"时有效
	 * @event {Function} getphonenumber	获取用户手机号回调，openType="getPhoneNumber"时有效
	 * @event {Function} error			当使用开放能力时，发生错误的回调，openType="error"时有效
	 * @event {Function} launchapp		打开 APP 成功的回调，openType="launchApp"时有效
	 * @event {Function} opensetting	在打开授权设置页后回调，openType="openSetting"时有效
	 * @example <u-action-sheet :actions="list" :title="title" :show="show"></u-action-sheet>
	 */
	export default {
		name: "u-action-sheet",
		// 一些props参数和methods方法，通过mixin混入，因为其他文件也会用到
		mixins: [openType, button, uni.$u.mixin, props],
		data() {
			return {

			}
		},
		computed: {
			// 操作项目的样式
			itemStyle() {
				return (index) => {
					let style = {};
					if (this.actions[index].color) style.color = this.actions[index].color
					if (this.actions[index].fontSize) style.fontSize = uni.$u.addUnit(this.actions[index].fontSize)
					// 选项被禁用的样式
					if (this.actions[index].disabled) style.color = '#c0c4cc'
					return style;
				}
			},
		},
		methods: {
			closeHandler() {
				// 允许点击遮罩关闭时，才发出close事件
				if(this.closeOnClickOverlay) {
					this.$emit('close')
				}
			},
			// 点击取消按钮
			cancel() {
				this.$emit('close')
			},
			selectHandler(index) {
				const item = this.actions[index]
				if (item && !item.disabled && !item.loading) {
					this.$emit('select', item)
					if (this.closeOnClickAction) {
						this.$emit('close')
					}
				}
			},
		}
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";
	$u-action-sheet-reset-button-width:100% !default;
	$u-action-sheet-title-font-size: 16px !default;
	$u-action-sheet-title-padding: 12px 30px !default;
	$u-action-sheet-title-color: $u-main-color !default;
	$u-action-sheet-header-icon-wrap-right:15px !default;
	$u-action-sheet-header-icon-wrap-top:15px !default;
	$u-action-sheet-description-font-size:13px !default;
	$u-action-sheet-description-color:14px !default;
	$u-action-sheet-description-margin: 18px 15px !default;
	$u-action-sheet-item-wrap-item-padding:15px !default;
	$u-action-sheet-item-wrap-name-font-size:16px !default;
	$u-action-sheet-item-wrap-subname-font-size:13px !default;
	$u-action-sheet-item-wrap-subname-color: #c0c4cc !default;
	$u-action-sheet-item-wrap-subname-margin-top:10px !default;
	$u-action-sheet-cancel-text-font-size:16px !default;
	$u-action-sheet-cancel-text-color:$u-content-color !default;
	$u-action-sheet-cancel-text-font-size:15px !default;
	$u-action-sheet-cancel-text-hover-background-color:rgb(242, 243, 245) !default;

	.u-reset-button {
		width: $u-action-sheet-reset-button-width;
	}

	.u-action-sheet {
		text-align: center;
		&__header {
			position: relative;
			padding: $u-action-sheet-title-padding;
			&__title {
				font-size: $u-action-sheet-title-font-size;
				color: $u-action-sheet-title-color;
				font-weight: bold;
				text-align: center;
			}

			&__icon-wrap {
				position: absolute;
				right: $u-action-sheet-header-icon-wrap-right;
				top: $u-action-sheet-header-icon-wrap-top;
			}
		}

		&__description {
			font-size: $u-action-sheet-description-font-size;
			color: $u-tips-color;
			margin: $u-action-sheet-description-margin;
			text-align: center;
		}

		&__item-wrap {

			&__item {
				padding: $u-action-sheet-item-wrap-item-padding;
				@include flex;
				align-items: center;
				justify-content: center;
				flex-direction: column;

				&__name {
					font-size: $u-action-sheet-item-wrap-name-font-size;
					color: $u-main-color;
					text-align: center;
				}

				&__subname {
					font-size: $u-action-sheet-item-wrap-subname-font-size;
					color: $u-action-sheet-item-wrap-subname-color;
					margin-top: $u-action-sheet-item-wrap-subname-margin-top;
					text-align: center;
				}
			}
		}

		&__cancel-text {
			font-size: $u-action-sheet-cancel-text-font-size;
			color: $u-action-sheet-cancel-text-color;
			text-align: center;
			padding: $u-action-sheet-cancel-text-font-size;
		}

		&--hover {
			background-color: $u-action-sheet-cancel-text-hover-background-color;
		}
	}
</style>
