// const defaultOption = {
// 	duration: 300,
// 	timingFunction: 'linear',
// 	delay: 0,
// 	transformOrigin: '50% 50% 0'
// }
// #ifdef APP-NVUE
const nvueAnimation = uni.requireNativePlugin('animation')
// #endif
class MPAnimation {
	constructor(options, _this) {
		this.options = options
		this.animation = uni.createAnimation(options)
		this.currentStepAnimates = {}
		this.next = 0
		this.$ = _this

	}

	_nvuePushAnimates(type, args) {
		let aniObj = this.currentStepAnimates[this.next]
		let styles = {}
		if (!aniObj) {
			styles = {
				styles: {},
				config: {}
			}
		} else {
			styles = aniObj
		}
		if (animateTypes1.includes(type)) {
			if (!styles.styles.transform) {
				styles.styles.transform = ''
			}
			let unit = ''
			if(type === 'rotate'){
				unit = 'deg'
			}
			styles.styles.transform += `${type}(${args+unit}) `
		} else {
			styles.styles[type] = `${args}`
		}
		this.currentStepAnimates[this.next] = styles
	}
	_animateRun(styles = {}, config = {}) {
		let ref = this.$.$refs['ani'].ref
		if (!ref) return
		return new Promise((resolve, reject) => {
			nvueAnimation.transition(ref, {
				styles,
				...config
			}, res => {
				resolve()
			})
		})
	}

	_nvueNextAnimate(animates, step = 0, fn) {
		let obj = animates[step]
		if (obj) {
			let {
				styles,
				config
			} = obj
			this._animateRun(styles, config).then(() => {
				step += 1
				this._nvueNextAnimate(animates, step, fn)
			})
		} else {
			this.currentStepAnimates = {}
			typeof fn === 'function' && fn()
			this.isEnd = true
		}
	}

	step(config = {}) {
		// #ifndef APP-NVUE
		this.animation.step(config)
		// #endif
		// #ifdef APP-NVUE
		this.currentStepAnimates[this.next].config = Object.assign({}, this.options, config)
		this.currentStepAnimates[this.next].styles.transformOrigin = this.currentStepAnimates[this.next].config.transformOrigin
		this.next++
		// #endif
		return this
	}

	run(fn) {
		// #ifndef APP-NVUE
		this.$.animationData = this.animation.export()
		this.$.timer = setTimeout(() => {
			typeof fn === 'function' && fn()
		}, this.$.durationTime)
		// #endif
		// #ifdef APP-NVUE
		this.isEnd = false
		let ref = this.$.$refs['ani'] && this.$.$refs['ani'].ref
		if(!ref) return
		this._nvueNextAnimate(this.currentStepAnimates, 0, fn)
		this.next = 0
		// #endif
	}
}


const animateTypes1 = ['matrix', 'matrix3d', 'rotate', 'rotate3d', 'rotateX', 'rotateY', 'rotateZ', 'scale', 'scale3d',
	'scaleX', 'scaleY', 'scaleZ', 'skew', 'skewX', 'skewY', 'translate', 'translate3d', 'translateX', 'translateY',
	'translateZ'
]
const animateTypes2 = ['opacity', 'backgroundColor']
const animateTypes3 = ['width', 'height', 'left', 'right', 'top', 'bottom']
animateTypes1.concat(animateTypes2, animateTypes3).forEach(type => {
	MPAnimation.prototype[type] = function(...args) {
		// #ifndef APP-NVUE
		this.animation[type](...args)
		// #endif
		// #ifdef APP-NVUE
		this._nvuePushAnimates(type, args)
		// #endif
		return this
	}
})

export function createAnimation(option, _this) {
	if(!_this) return
	clearTimeout(_this.timer)
	return new MPAnimation(option, _this)
}
