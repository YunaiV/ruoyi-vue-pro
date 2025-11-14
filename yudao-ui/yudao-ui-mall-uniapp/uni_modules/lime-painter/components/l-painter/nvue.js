// #ifdef APP-NVUE
import {
	sleep,
	getImageInfo,
	isBase64,
	networkReg
} from './utils';
const dom = weex.requireModule('dom')
import {
	version
} from '../../package.json'

export default {
	data() {
		return {
			tempFilePath: [],
			isInitFile: false,
			osName: uni.getSystemInfoSync().osName
		}
	},
	methods: {
		getParentWeith() {
			return new Promise(resolve => {
				dom.getComponentRect(this.$refs.limepainter, (res) => {
					this.parentWidth = Math.ceil(res.size.width)
					this.canvasWidth = this.canvasWidth || this.parentWidth || 300
					this.canvasHeight = res.size.height || this.canvasHeight || 150
					resolve(res.size)
				})
			})
		},
		onPageFinish() {
			this.webview = this.$refs.webview
			this.webview.evalJS(`init(${this.dpr})`)
		},
		onMessage(e) {
			const res = e.detail.data[0] || null;
			if (res.event) {
				if (res.event == 'inited') {
					this.inited = true
				}
				if (res.event == 'fail') {
					this.$emit('fail', res)
				}
				if (res.event == 'layoutChange') {
					const data = typeof res.data == 'string' ? JSON.parse(res.data) : res.data
					this.canvasWidth = Math.ceil(data.width);
					this.canvasHeight = Math.ceil(data.height);
				}
				if (res.event == 'progressChange') {
					this.progress = res.data * 1
				}
				if (res.event == 'file') {
					this.tempFilePath.push(res.data)
					if (this.tempFilePath.length > 7) {
						this.tempFilePath.shift()
					}
					return
				}
				if (res.event == 'success') {
					if (res.data) {
						this.tempFilePath.push(res.data)
						if (this.tempFilePath.length > 8) {
							this.tempFilePath.shift()
						}
						if (this.isCanvasToTempFilePath) {
							this.setFilePath(this.tempFilePath.join(''), {
								isEmit: true
							})
						}
					} else {
						this.$emit('fail', 'canvas no data')
					}
					return
				}
				this.$emit(res.event, JSON.parse(res.data));
			} else if (res.file) {
				this.file = res.data;
			} else {
				console.info(res[0])
			}
		},
		getWebViewInited() {
			if (this.inited) return Promise.resolve(this.inited);
			return new Promise((resolve) => {
				this.$watch(
					'inited',
					async val => {
						if (val) {
							resolve(val)
						}
					}, {
						immediate: true
					}
				);
			})
		},
		getTempFilePath() {
			if (this.tempFilePath.length == 8) return Promise.resolve(this.tempFilePath)
			return new Promise((resolve) => {
				this.$watch(
					'tempFilePath',
					async val => {
						if (val.length == 8) {
							resolve(val.join(''))
						}
					}, {
						deep: true
					}
				);
			})
		},
		getWebViewDone() {
			if (this.progress == 1) return Promise.resolve(this.progress);
			return new Promise((resolve) => {
				this.$watch(
					'progress',
					async val => {
						if (val == 1) {
							this.$emit('done')
							this.done = true
							this.runTask()
							resolve(val)
						}
					}, {
						immediate: true
					}
				);
			})
		},
		async render(args) {
			try {
				await this.getSize(args)
				const {
					width
				} = args.css || args
				if (!width && this.parentWidth) {
					Object.assign(args, {
						width: this.parentWidth
					})
				}
				const newNode = await this.calcImage(args);
				await this.getWebViewInited()
				this.webview.evalJS(`source(${JSON.stringify(newNode)})`)
				await this.getWebViewDone()
				await sleep(this.afterDelay)
				if (this.isCanvasToTempFilePath) {
					const params = {
						fileType: this.fileType,
						quality: this.quality
					}
					this.webview.evalJS(`save(${JSON.stringify(params)})`)
				}
				return Promise.resolve()
			} catch (e) {
				this.$emit('fail', e)
			}
		},
		async calcImage(args) {
			let node = JSON.parse(JSON.stringify(args))
			const urlReg = /url\((.+)\)/
			const {
				backgroundImage
			} = node.css || {}
			const isBG = backgroundImage && urlReg.exec(backgroundImage)[1]
			const url = node.url || node.src || isBG
			if (['text', 'qrcode'].includes(node.type)) {
				return node
			}
			if ((node.type === "image" || isBG) && url && !isBase64(url) && (this.osName == 'ios' || !networkReg
					.test(url))) {
				let {
					path
				} = await getImageInfo(url, true)
				if (isBG) {
					node.css.backgroundImage = `url(${path})`
				} else {
					node.src = path
				}
			} else if (node.views && node.views.length) {
				for (let i = 0; i < node.views.length; i++) {
					node.views[i] = await this.calcImage(node.views[i])
				}
			}
			return node
		},
		async canvasToTempFilePath(args = {}) {
			if (!this.inited) {
				return this.$emit('fail', 'no init')
			}
			this.tempFilePath = []
			if (args.fileType == 'jpg') {
				args.fileType = 'jpeg'
			}

			this.webview.evalJS(`save(${JSON.stringify(args)})`)
			try {
				let tempFilePath = await this.getTempFilePath()

				tempFilePath = await this.setFilePath(tempFilePath, args)
				args.success({
					errMsg: "canvasToTempFilePath:ok",
					tempFilePath
				})
			} catch (e) {
				console.log('e', e)
				args.fail({
					error: e
				})
			}
		}
	}
}
// #endif