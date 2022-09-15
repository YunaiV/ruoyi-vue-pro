let bindIngXMixins = {}

// #ifdef APP-NVUE
const BindingX = uni.requireNativePlugin('bindingx');
const dom = uni.requireNativePlugin('dom');
const animation = uni.requireNativePlugin('animation');

bindIngXMixins = {
	data() {
		return {}
	},

	watch: {
		show(newVal) {
			if (this.autoClose) return
			if (this.stop) return
			this.stop = true
			if (newVal) {
				this.open(newVal)
			} else {
				this.close()
			}
		},
		leftOptions() {
			this.getSelectorQuery()
			this.init()
		},
		rightOptions(newVal) {
			this.init()
		}
	},
	created() {
		this.swipeaction = this.getSwipeAction()
		if (this.swipeaction.children !== undefined) {
			this.swipeaction.children.push(this)
		}
	},
	mounted() {
		this.box = this.getEl(this.$refs['selector-box--hock'])
		this.selector = this.getEl(this.$refs['selector-content--hock']);
		this.leftButton = this.getEl(this.$refs['selector-left-button--hock']);
		this.rightButton = this.getEl(this.$refs['selector-right-button--hock']);
		this.init()
	},
	// beforeDestroy() {
	// 	this.swipeaction.children.forEach((item, index) => {
	// 		if (item === this) {
	// 			this.swipeaction.children.splice(index, 1)
	// 		}
	// 	})
	// },
	methods: {
		init() {
			this.$nextTick(() => {
				this.x = 0
				this.button = {
					show: false
				}
				setTimeout(() => {
					this.getSelectorQuery()
				}, 200)
			})
		},
		onClick(index, item, position) {
			this.$emit('click', {
				content: item,
				index,
				position
			})
		},
		touchstart(e) {
			// fix by mehaotian 禁止滑动
			if (this.disabled) return
			// 每次只触发一次，避免多次监听造成闪烁
			if (this.stop) return
			this.stop = true
			if (this.autoClose) {
				this.swipeaction.closeOther(this)
			}

			const leftWidth = this.button.left.width
			const rightWidth = this.button.right.width
			let expression = this.range(this.x, -rightWidth, leftWidth)
			let leftExpression = this.range(this.x - leftWidth, -leftWidth, 0)
			let rightExpression = this.range(this.x + rightWidth, 0, rightWidth)

			this.eventpan = BindingX.bind({
				anchor: this.box,
				eventType: 'pan',
				props: [{
					element: this.selector,
					property: 'transform.translateX',
					expression
				}, {
					element: this.leftButton,
					property: 'transform.translateX',
					expression: leftExpression
				}, {
					element: this.rightButton,
					property: 'transform.translateX',
					expression: rightExpression
				}, ]
			}, (e) => {
				// nope
				if (e.state === 'end') {
					this.x = e.deltaX + this.x;
					this.isclick = true
					this.bindTiming(e.deltaX)
				}
			});
		},
		touchend(e) {
			if (this.isopen !== 'none' && !this.isclick) {
				this.open('none')
			}
		},
		bindTiming(x) {
			const left = this.x
			const leftWidth = this.button.left.width
			const rightWidth = this.button.right.width
			const threshold = this.threshold
			if (!this.isopen || this.isopen === 'none') {
				if (left > threshold) {
					this.open('left')
				} else if (left < -threshold) {
					this.open('right')
				} else {
					this.open('none')
				}
			} else {
				if ((x > -leftWidth && x < 0) || x > rightWidth) {
					if ((x > -threshold && x < 0) || (x - rightWidth > threshold)) {
						this.open('left')
					} else {
						this.open('none')
					}
				} else {
					if ((x < threshold && x > 0) || (x + leftWidth < -threshold)) {
						this.open('right')
					} else {
						this.open('none')
					}
				}
			}
		},

		/**
		 * 移动范围
		 * @param {Object} num
		 * @param {Object} mix
		 * @param {Object} max
		 */
		range(num, mix, max) {
			return `min(max(x+${num}, ${mix}), ${max})`
		},

		/**
		 * 开启swipe
		 */
		open(type) {
			this.animation(type)
		},

		/**
		 * 关闭swipe
		 */
		close() {
			this.animation('none')
		},

		/**
		 * 开启关闭动画
		 * @param {Object} type
		 */
		animation(type) {
			const time = 300
			const leftWidth = this.button.left.width
			const rightWidth = this.button.right.width
			if (this.eventpan && this.eventpan.token) {
				BindingX.unbind({
					token: this.eventpan.token,
					eventType: 'pan'
				})
			}

			switch (type) {
				case 'left':
					Promise.all([
						this.move(this.selector, leftWidth),
						this.move(this.leftButton, 0),
						this.move(this.rightButton, rightWidth * 2)
					]).then(() => {
						this.setEmit(leftWidth, type)
					})
					break
				case 'right':
					Promise.all([
						this.move(this.selector, -rightWidth),
						this.move(this.leftButton, -leftWidth * 2),
						this.move(this.rightButton, 0)
					]).then(() => {
						this.setEmit(-rightWidth, type)
					})
					break
				default:
					Promise.all([
						this.move(this.selector, 0),
						this.move(this.leftButton, -leftWidth),
						this.move(this.rightButton, rightWidth)
					]).then(() => {
						this.setEmit(0, type)
					})

			}
		},
		setEmit(x, type) {
			const leftWidth = this.button.left.width
			const rightWidth = this.button.right.width
			this.isopen = this.isopen || 'none'
			this.stop = false
			this.isclick = false
			// 只有状态不一致才会返回结果
			if (this.isopen !== type && this.x !== x) {
				if (type === 'left' && leftWidth > 0) {
					this.$emit('change', 'left')
				}
				if (type === 'right' && rightWidth > 0) {
					this.$emit('change', 'right')
				}
				if (type === 'none') {
					this.$emit('change', 'none')
				}
			}
			this.x = x
			this.isopen = type
		},
		move(ref, value) {
			return new Promise((resolve, reject) => {
				animation.transition(ref, {
					styles: {
						transform: `translateX(${value})`,
					},
					duration: 150, //ms
					timingFunction: 'linear',
					needLayout: false,
					delay: 0 //ms
				}, function(res) {
					resolve(res)
				})
			})

		},

		/**
		 * 获取ref
		 * @param {Object} el
		 */
		getEl(el) {
			return el.ref
		},
		/**
		 * 获取节点信息
		 */
		getSelectorQuery() {
			Promise.all([
				this.getDom('left'),
				this.getDom('right'),
			]).then((data) => {
				let show = 'none'
				if (this.autoClose) {
					show = 'none'
				} else {
					show = this.show
				}

				if (show === 'none') {
					// this.close()
				} else {
					this.open(show)
				}

			})

		},
		getDom(str) {
			return new Promise((resolve, reject) => {
				dom.getComponentRect(this.$refs[`selector-${str}-button--hock`], (data) => {
					if (data) {
						this.button[str] = data.size
						resolve(data)
					} else {
						reject()
					}
				})
			})
		}
	}
}

// #endif

export default bindIngXMixins
