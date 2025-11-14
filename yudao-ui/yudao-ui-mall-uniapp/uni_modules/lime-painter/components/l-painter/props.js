export default {
	props: {
		board: Object,
		pathType: String, // 'base64'、'url'
		fileType: {
			type: String,
			default: 'png'
		},
		hidden: Boolean,
		quality: {
			type: Number,
			default: 1
		},
		css: [String, Object],
		// styles: [String, Object],
		width: [Number, String],
		height: [Number, String],
		pixelRatio: Number,
		customStyle: String,
		isCanvasToTempFilePath: Boolean,
		// useCanvasToTempFilePath: Boolean,
		sleep: {
			type: Number,
			default: 1000 / 30
		},
		beforeDelay: {
			type: Number,
			default: 100
		},
		afterDelay: {
			type: Number,
			default: 100
		},
		performance: Boolean,
		// #ifdef MP-WEIXIN || MP-TOUTIAO || MP-ALIPAY
		type: {
			type: String,
			default: '2d'
		},
		// #endif
		// #ifdef APP-NVUE
		hybrid: Boolean,
		timeout: {
			type: Number,
			default: 2000
		},
		// #endif
		// #ifdef H5 || APP-PLUS
		useCORS: Boolean,
		hidpi: {
			type: Boolean,
			default: true
		}
		// #endif
	}
}