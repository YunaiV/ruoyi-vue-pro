<template>
	<view 
		v-if="isShow" 
		ref="ani" 
		class="uni-transition" 
		:class="[ani.in]" 
		:style="'transform:' +transform+';'+stylesObject"
		@click="change"
	>
		 <slot></slot>
	</view>
</template>

<script>
	// #ifdef APP-NVUE
	const animation = uni.requireNativePlugin('animation');
	// #endif
	/**
	 * Transition 过渡动画
	 * @description 简单过渡动画组件
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=985
	 * @property {Boolean} show = [false|true] 控制组件显示或隐藏
     * @property {Array} modeClass = [fade|slide-top|slide-right|slide-bottom|slide-left|zoom-in|zoom-out] 过渡动画类型
     *  @value fade 渐隐渐出过渡
     *  @value slide-top 由上至下过渡
     *  @value slide-right 由右至左过渡
     *  @value slide-bottom 由下至上过渡
     *  @value slide-left 由左至右过渡
     *  @value zoom-in 由小到大过渡
     *  @value zoom-out 由大到小过渡
	 * @property {Number} duration 过渡动画持续时间
	 * @property {Object} styles 组件样式，同 css 样式，注意带’-‘连接符的属性需要使用小驼峰写法如：`backgroundColor:red`
	 */
	export default {
		name: 'uniTransition',
		props: {
			show: {
				type: Boolean,
				default: false
			},
			modeClass: {
				type: Array,
				default () {
					return []
				}
			},
			duration: {
				type: Number,
				default: 300
			},
			styles: {
				type: Object,
				default () {
					return {}
				}
			},
			maskBackgroundColor: {
				type: String,
				default: 'rgba(0, 0, 0, 0.4)'
			}
		},
		data() {
			return {
				isShow: false,
				transform: '',
				ani: { in: '',
					active: ''
				}
			};
		},
		watch: {
			show: {
				handler(newVal) {
					if (newVal) {
						this.open()
					} else {
						this.close()
					}
				},
				immediate: true
			}
		},
		computed: {
			stylesObject() {
				let styles = {
					...this.styles,
					backgroundColor: this.maskBackgroundColor,
					'transition-duration': this.duration / 1000 + 's'
				}
				let transfrom = ''
				for (let i in styles) {
					let line = this.toLine(i)
					transfrom += line + ':' + styles[i] + ';'
				}
				return transfrom
			}
		},
		created() {
			// this.timer = null
			// this.nextTick = (time = 50) => new Promise(resolve => {
			// 	clearTimeout(this.timer)
			// 	this.timer = setTimeout(resolve, time)
			// 	return this.timer
			// });
		},
		methods: {
			change() {
				this.$emit('click', {
					detail: this.isShow
				})
			},
			open() {
				clearTimeout(this.timer)
				this.isShow = true
				this.transform = ''
				this.ani.in = ''
				for (let i in this.getTranfrom(false)) {
					if (i === 'opacity') {
						this.ani.in = 'fade-in'
					} else {
						this.transform += `${this.getTranfrom(false)[i]} `
					}
				}
				this.$nextTick(() => {
					setTimeout(() => {
						this._animation(true)
					}, 50)
				})

			},
			close(type) {
				clearTimeout(this.timer)
				this._animation(false)
			},
			_animation(type) {
				let styles = this.getTranfrom(type)
				// #ifdef APP-NVUE
				if(!this.$refs['ani']) return
				animation.transition(this.$refs['ani'].ref, {
					styles,
					duration: this.duration, //ms
					timingFunction: 'ease',
					needLayout: false,
					delay: 0 //ms
				}, () => {
					if (!type) {
						this.isShow = false
					}
					this.$emit('change', {
						detail: this.isShow
					})
				})
				// #endif
				// #ifndef APP-NVUE
				this.transform = ''
				for (let i in styles) {
					if (i === 'opacity') {
						this.ani.in = `fade-${type?'out':'in'}`
					} else {
						this.transform += `${styles[i]} `
					}
				}
				this.timer = setTimeout(() => {
					if (!type) {
						this.isShow = false
					}
					this.$emit('change', {
						detail: this.isShow
					})

				}, this.duration)
				// #endif

			},
			getTranfrom(type) {
				let styles = {
					transform: ''
				}
				this.modeClass.forEach((mode) => {
					switch (mode) {
						case 'fade':
							styles.opacity = type ? 1 : 0
							break;
						case 'slide-top':
							styles.transform += `translateY(${type?'0':'-100%'}) `
							break;
						case 'slide-right':
							styles.transform += `translateX(${type?'0':'100%'}) `
							break;
						case 'slide-bottom':
							styles.transform += `translateY(${type?'0':'100%'}) `
							break;
						case 'slide-left':
							styles.transform += `translateX(${type?'0':'-100%'}) `
							break;
						case 'zoom-in':
							styles.transform += `scale(${type?1:0.8}) `
							break;
						case 'zoom-out':
							styles.transform += `scale(${type?1:1.2}) `
							break;
					}
				})
				return styles
			},
			_modeClassArr(type) {
				let mode = this.modeClass
				if (typeof(mode) !== "string") {
					let modestr = ''
					mode.forEach((item) => {
						modestr += (item + '-' + type + ',')
					})
					return modestr.substr(0, modestr.length - 1)
				} else {
					return mode + '-' + type
				}
			},
			// getEl(el) {
			// 	console.log(el || el.ref || null);
			// 	return el || el.ref || null
			// },
			toLine(name) {
				return name.replace(/([A-Z])/g, "-$1").toLowerCase();
			}
		}
	}
</script>

<style>
	.uni-transition {
		transition-timing-function: ease;
		transition-duration: 0.3s;
		transition-property: transform, opacity;
	}

	.fade-in {
		opacity: 0;
	}

	.fade-active {
		opacity: 1;
	}

	.slide-top-in {
		/* transition-property: transform, opacity; */
		transform: translateY(-100%);
	}

	.slide-top-active {
		transform: translateY(0);
		/* opacity: 1; */
	}

	.slide-right-in {
		transform: translateX(100%);
	}

	.slide-right-active {
		transform: translateX(0);
	}

	.slide-bottom-in {
		transform: translateY(100%);
	}

	.slide-bottom-active {
		transform: translateY(0);
	}

	.slide-left-in {
		transform: translateX(-100%);
	}

	.slide-left-active {
		transform: translateX(0);
		opacity: 1;
	}

	.zoom-in-in {
		transform: scale(0.8);
	}

	.zoom-out-active {
		transform: scale(1);
	}

	.zoom-out-in {
		transform: scale(1.2);
	}
</style>
