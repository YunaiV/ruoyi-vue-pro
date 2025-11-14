<template>
	<a v-if="isShowA" class="uni-link" :href="href"
		:class="{'uni-link--withline':showUnderLine===true||showUnderLine==='true'}"
		:style="{color,fontSize:fontSize+'px'}" :download="download">
		<slot>{{text}}</slot>
	</a>
	<!-- #ifndef APP-NVUE -->
	<text v-else class="uni-link" :class="{'uni-link--withline':showUnderLine===true||showUnderLine==='true'}"
		:style="{color,fontSize:fontSize+'px'}" @click="openURL">
		<slot>{{text}}</slot>
	</text>
	<!-- #endif -->
	<!-- #ifdef APP-NVUE -->
	<text v-else class="uni-link" :class="{'uni-link--withline':showUnderLine===true||showUnderLine==='true'}"
		:style="{color,fontSize:fontSize+'px'}" @click="openURL">
		{{text}}
	</text>
	<!-- #endif -->
</template>

<script>
	/**
	 * Link 外部网页超链接组件
	 * @description uni-link是一个外部网页超链接组件，在小程序内复制url，在app内打开外部浏览器，在h5端打开新网页
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=1182
	 * @property {String} href 点击后打开的外部网页url
	 * @property {String} text 显示的文字
	 * @property {String} downlaod H5平台下载文件名
	 * @property {Boolean} showUnderLine 是否显示下划线
	 * @property {String} copyTips 在小程序端复制链接时显示的提示语
	 * @property {String} color 链接文字颜色
	 * @property {String} fontSize 链接文字大小
	 * @example * <uni-link href="https://ext.dcloud.net.cn" text="https://ext.dcloud.net.cn"></uni-link>
	 */
	export default {
		name: 'uniLink',
		props: {
			href: {
				type: String,
				default: ''
			},
			text: {
				type: String,
				default: ''
			},
			download: {
				type: String,
				default: ''
			},
			showUnderLine: {
				type: [Boolean, String],
				default: true
			},
			copyTips: {
				type: String,
				default: '已自动复制网址，请在手机浏览器里粘贴该网址'
			},
			color: {
				type: String,
				default: '#999999'
			},
			fontSize: {
				type: [Number, String],
				default: 14
			}
		},
		computed: {
			isShowA() {
				// #ifdef H5
				this._isH5 = true;
				// #endif
				if ((this.isMail() || this.isTel()) && this._isH5 === true) {
					return true;
				}
				return false;
			}
		},
		created() {
			this._isH5 = null;
		},
		methods: {
			isMail() {
				return this.href.startsWith('mailto:');
			},
			isTel() {
				return this.href.startsWith('tel:');
			},
			openURL() {
				// #ifdef APP-PLUS
				if (this.isTel()) {
					this.makePhoneCall(this.href.replace('tel:', ''));
				} else {
					plus.runtime.openURL(this.href);
				}
				// #endif
				// #ifdef H5
				window.open(this.href)
				// #endif
				// #ifdef MP
				uni.setClipboardData({
					data: this.href
				});
				uni.showModal({
					content: this.copyTips,
					showCancel: false
				});
				// #endif
			},
			makePhoneCall(phoneNumber) {
				uni.makePhoneCall({
					phoneNumber
				})
			}
		}
	}
</script>

<style>
	/* #ifndef APP-NVUE */
	.uni-link {
		cursor: pointer;
	}

	/* #endif */
	.uni-link--withline {
		text-decoration: underline;
	}
</style>
