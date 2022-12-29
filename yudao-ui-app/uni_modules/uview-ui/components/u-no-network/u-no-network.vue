<template>
	<u-overlay
	    :show="!isConnected"
		:zIndex="zIndex"
	    @touchmove.stop.prevent="noop"
		:customStyle="{
			backgroundColor: '#fff',
			display: 'flex',
			justifyContent: 'center',
		}"
	>
		<view
		    class="u-no-network"
		>
			<u-icon
			    :name="image"
			    size="150"
			    imgMode="widthFit"
			    class="u-no-network__error-icon"
			></u-icon>
			<text class="u-no-network__tips">{{tips}}</text>
			<!-- 只有APP平台，才能跳转设置页，因为需要调用plus环境 -->
			<!-- #ifdef APP-PLUS -->
			<view class="u-no-network__app">
				<text class="u-no-network__app__setting">请检查网络，或前往</text>
				<text
				    class="u-no-network__app__to-setting"
				    @tap="openSettings"
				>设置</text>
			</view>
			<!-- #endif -->
			<view class="u-no-network__retry">
				<u-button
				    size="mini"
				    text="重试"
				    type="primary"
					plain
				    @click="retry"
				></u-button>
			</view>
		</view>
	</u-overlay>
</template>

<script>
	import props from './props.js';

	/**
	 * noNetwork 无网络提示
	 * @description 该组件无需任何配置，引入即可，内部自动处理所有功能和事件。
	 * @tutorial https://www.uviewui.com/components/noNetwork.html
	 * @property {String}			tips 	没有网络时的提示语 （默认：'哎呀，网络信号丢失' ）
	 * @property {String | Number}	zIndex	组件的z-index值 
	 * @property {String}			image	无网络的图片提示，可用的src地址或base64图片 
	 * @event {Function}			retry	用户点击页面的"重试"按钮时触发
	 * @example <u-no-network></u-no-network>
	 */
	export default {
		name: "u-no-network",
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		data() {
			return {
				isConnected: true, // 是否有网络连接
				networkType: "none", // 网络类型
			}
		},
		mounted() {
			this.isIOS = (uni.getSystemInfoSync().platform === 'ios')
			uni.onNetworkStatusChange((res) => {
				this.isConnected = res.isConnected
				this.networkType = res.networkType
				this.emitEvent(this.networkType)
			})
			uni.getNetworkType({
				success: (res) => {
					this.networkType = res.networkType
					this.emitEvent(this.networkType)
					if (res.networkType == 'none') {
						this.isConnected = false
					} else {
						this.isConnected = true
					}
				}
			})
		},
		methods: {
			retry() {
				// 重新检查网络
				uni.getNetworkType({
					success: (res) => {
						this.networkType = res.networkType
						this.emitEvent(this.networkType)
						if (res.networkType == 'none') {
							uni.$u.toast('无网络连接')
							this.isConnected = false
						} else {
							uni.$u.toast('网络已连接')
							this.isConnected = true
						}
					}
				})
				this.$emit('retry')
			},
			// 发出事件给父组件
			emitEvent(networkType) {
				this.$emit(networkType === 'none' ? 'disconnected' : 'connected')
			},
			async openSettings() {
				if (this.networkType == "none") {
					this.openSystemSettings()
					return
				}
			},
			openAppSettings() {
				this.gotoAppSetting()
			},
			openSystemSettings() {
				// 以下方法来自5+范畴，如需深究，请自行查阅相关文档
				// https://ask.dcloud.net.cn/docs/
				if (this.isIOS) {
					this.gotoiOSSetting()
				} else {
					this.gotoAndroidSetting()
				}
			},
			network() {
				var result = null
				var cellularData = plus.ios.newObject("CTCellularData")
				var state = cellularData.plusGetAttribute("restrictedState")
				if (state == 0) {
					result = null
				} else if (state == 2) {
					result = 1
				} else if (state == 1) {
					result = 2
				}
				plus.ios.deleteObject(cellularData)
				return result
			},
			gotoAppSetting() {
				if (this.isIOS) {
					var UIApplication = plus.ios.import("UIApplication")
					var application2 = UIApplication.sharedApplication()
					var NSURL2 = plus.ios.import("NSURL")
					var setting2 = NSURL2.URLWithString("app-settings:")
					application2.openURL(setting2)
					plus.ios.deleteObject(setting2)
					plus.ios.deleteObject(NSURL2)
					plus.ios.deleteObject(application2)
				} else {
					var Intent = plus.android.importClass("android.content.Intent")
					var Settings = plus.android.importClass("android.provider.Settings")
					var Uri = plus.android.importClass("android.net.Uri")
					var mainActivity = plus.android.runtimeMainActivity()
					var intent = new Intent()
					intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
					var uri = Uri.fromParts("package", mainActivity.getPackageName(), null)
					intent.setData(uri)
					mainActivity.startActivity(intent)
				}
			},
			gotoiOSSetting() {
				var UIApplication = plus.ios.import("UIApplication")
				var application2 = UIApplication.sharedApplication()
				var NSURL2 = plus.ios.import("NSURL")
				var setting2 = NSURL2.URLWithString("App-prefs:root=General")
				application2.openURL(setting2)
				plus.ios.deleteObject(setting2)
				plus.ios.deleteObject(NSURL2)
				plus.ios.deleteObject(application2)
			},
			gotoAndroidSetting() {
				var Intent = plus.android.importClass("android.content.Intent")
				var Settings = plus.android.importClass("android.provider.Settings")
				var mainActivity = plus.android.runtimeMainActivity()
				var intent = new Intent(Settings.ACTION_SETTINGS)
				mainActivity.startActivity(intent)
			}
		}
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-no-network {
		@include flex(column);
		justify-content: center;
		align-items: center;
		margin-top: -100px;

		&__tips {
			color: $u-tips-color;
			font-size: 14px;
			margin-top: 15px;
		}

		&__app {
			@include flex(row);
			margin-top: 6px;

			&__setting {
				color: $u-light-color;
				font-size: 13px;
			}

			&__to-setting {
				font-size: 13px;
				color: $u-primary;
				margin-left: 3px;
			}
		}

		&__retry {
			@include flex(row);
			justify-content: center;
			margin-top: 15px;
		}
	}
</style>
