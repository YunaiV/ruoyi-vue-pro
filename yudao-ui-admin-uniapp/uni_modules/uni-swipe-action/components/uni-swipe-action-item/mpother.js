let otherMixins = {}

// #ifndef APP-PLUS|| MP-WEIXIN  ||  H5
const MIN_DISTANCE = 10;
otherMixins = {
	data() {
		// TODO 随机生生元素ID，解决百度小程序获取同一个元素位置信息的bug
		const elClass = `Uni_${Math.ceil(Math.random() * 10e5).toString(36)}`
		return {
			uniShow: false,
			left: 0,
			buttonShow: 'none',
			ani: false,
			moveLeft: '',
			elClass
		}
	},
	watch: {
		show(newVal) {
			if (this.autoClose) return
			this.openState(newVal)
		},
		left() {
			this.moveLeft = `translateX(${this.left}px)`
		},
		buttonShow(newVal) {
			if (this.autoClose) return
			this.openState(newVal)
		},
		leftOptions() {
			this.init()
		},
		rightOptions() {
			this.init()
		}
	},
	mounted() {
		this.swipeaction = this.getSwipeAction()
		if (this.swipeaction.children !== undefined) {
			this.swipeaction.children.push(this)
		}
		this.init()
	},
	methods: {
		init() {
			clearTimeout(this.timer)
			this.timer = setTimeout(() => {
				this.getSelectorQuery()
			}, 100)
			// 移动距离
			this.left = 0
			this.x = 0
		},

		closeSwipe(e) {
			if (!this.autoClose) return
			this.swipeaction.closeOther(this)
		},
		appTouchStart(e) {
			const {
				clientX
			} = e.changedTouches[0]
			this.clientX = clientX
			this.timestamp = new Date().getTime()
		},
		appTouchEnd(e, index, item, position) {
			const {
				clientX
			} = e.changedTouches[0]
			// fixed by xxxx 模拟点击事件，解决 ios 13 点击区域错位的问题
			let diff = Math.abs(this.clientX - clientX)
			let time = (new Date().getTime()) - this.timestamp
			if (diff < 40 && time < 300) {
				this.$emit('click', {
					content: item,
					index,
					position
				})
			}
		},
		touchstart(e) {
			if (this.disabled) return
			this.ani = false
			this.x = this.left || 0
			this.stopTouchStart(e)
			this.autoClose && this.closeSwipe()
		},
		touchmove(e) {
			if (this.disabled) return
			// 是否可以滑动页面
			this.stopTouchMove(e);
			if (this.direction !== 'horizontal') {
				return;
			}
			this.move(this.x + this.deltaX)
			return false
		},
		touchend() {
			if (this.disabled) return
			this.moveDirection(this.left)
		},
		/**
		 * 设置移动距离
		 * @param {Object} value
		 */
		move(value) {
			value = value || 0
			const leftWidth = this.leftWidth
			const rightWidth = this.rightWidth
			// 获取可滑动范围
			this.left = this.range(value, -rightWidth, leftWidth);
		},

		/**
		 * 获取范围
		 * @param {Object} num
		 * @param {Object} min
		 * @param {Object} max
		 */
		range(num, min, max) {
			return Math.min(Math.max(num, min), max);
		},
		/**
		 * 移动方向判断
		 * @param {Object} left
		 * @param {Object} value
		 */
		moveDirection(left) {
			const threshold = this.threshold
			const isopen = this.isopen || 'none'
			const leftWidth = this.leftWidth
			const rightWidth = this.rightWidth
			if (this.deltaX === 0) {
				this.openState('none')
				return
			}
			if ((isopen === 'none' && rightWidth > 0 && -left > threshold) || (isopen !== 'none' && rightWidth >
					0 && rightWidth +
					left < threshold)) {
				// right
				this.openState('right')
			} else if ((isopen === 'none' && leftWidth > 0 && left > threshold) || (isopen !== 'none' && leftWidth >
					0 &&
					leftWidth - left < threshold)) {
				// left
				this.openState('left')
			} else {
				// default
				this.openState('none')
			}
		},

		/**
		 * 开启状态
		 * @param {Boolean} type
		 */
		openState(type) {
			const leftWidth = this.leftWidth
			const rightWidth = this.rightWidth
			let left = ''
			this.isopen = this.isopen ? this.isopen : 'none'
			switch (type) {
				case "left":
					left = leftWidth
					break
				case "right":
					left = -rightWidth
					break
				default:
					left = 0
			}


			if (this.isopen !== type) {
				this.throttle = true
				this.$emit('change', type)
			}

			this.isopen = type
			// 添加动画类
			this.ani = true
			this.$nextTick(() => {
				this.move(left)
			})
			// 设置最终移动位置,理论上只要进入到这个函数，肯定是要打开的
		},
		close() {
			this.openState('none')
		},
		getDirection(x, y) {
			if (x > y && x > MIN_DISTANCE) {
				return 'horizontal';
			}
			if (y > x && y > MIN_DISTANCE) {
				return 'vertical';
			}
			return '';
		},

		/**
		 * 重置滑动状态
		 * @param {Object} event
		 */
		resetTouchStatus() {
			this.direction = '';
			this.deltaX = 0;
			this.deltaY = 0;
			this.offsetX = 0;
			this.offsetY = 0;
		},

		/**
		 * 设置滑动开始位置
		 * @param {Object} event
		 */
		stopTouchStart(event) {
			this.resetTouchStatus();
			const touch = event.touches[0];
			this.startX = touch.clientX;
			this.startY = touch.clientY;
		},

		/**
		 * 滑动中，是否禁止打开
		 * @param {Object} event
		 */
		stopTouchMove(event) {
			const touch = event.touches[0];
			this.deltaX = touch.clientX - this.startX;
			this.deltaY = touch.clientY - this.startY;
			this.offsetX = Math.abs(this.deltaX);
			this.offsetY = Math.abs(this.deltaY);
			this.direction = this.direction || this.getDirection(this.offsetX, this.offsetY);
		},

		getSelectorQuery() {
			const views = uni.createSelectorQuery().in(this)
			views
				.selectAll('.' + this.elClass)
				.boundingClientRect(data => {
					if (data.length === 0) return
					let show = 'none'
					if (this.autoClose) {
						show = 'none'
					} else {
						show = this.show
					}
					this.leftWidth = data[0].width || 0
					this.rightWidth = data[1].width || 0
					this.buttonShow = show
				})
				.exec()
		}
	}
}

// #endif

export default otherMixins
