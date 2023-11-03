export default {
	data() {
		return {
			x: 0,
			transition: false,
			width: 0,
			viewWidth: 0,
			swipeShow: 0
		}
	},
	watch: {
		show(newVal) {
			if (this.autoClose) return
			if (newVal && newVal !== 'none') {
				this.transition = true
				this.open(newVal)
			} else {
				this.close()
			}
		}
	},
	created() {
		this.swipeaction = this.getSwipeAction()
		if (this.swipeaction.children !== undefined) {
			this.swipeaction.children.push(this)
		}
	},
	mounted() {
		this.isopen = false
		setTimeout(() => {
			this.getQuerySelect()
		}, 50)
	},
	methods: {
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
		/**
		 * 移动触发
		 * @param {Object} e
		 */
		onChange(e) {
			this.moveX = e.detail.x
			this.isclose = false
		},
		touchstart(e) {
			this.transition = false
			this.isclose = true
			this.autoClose && this.swipeaction.closeOther(this)
		},
		touchmove(e) {},
		touchend(e) {
			// 0的位置什么都不执行
			if (this.isclose && this.isopen === 'none') return
			if (this.isclose && this.isopen !== 'none') {
				this.transition = true
				this.close()
			} else {
				this.move(this.moveX + this.leftWidth)
			}
		},

		/**
		 * 移动
		 * @param {Object} moveX
		 */
		move(moveX) {
			// 打开关闭的处理逻辑不太一样
			this.transition = true
			// 未打开状态
			if (!this.isopen || this.isopen === 'none') {
				if (moveX > this.threshold) {
					this.open('left')
				} else if (moveX < -this.threshold) {
					this.open('right')
				} else {
					this.close()
				}
			} else {
				if (moveX < 0 && moveX < this.rightWidth) {
					const rightX = this.rightWidth + moveX
					if (rightX < this.threshold) {
						this.open('right')
					} else {
						this.close()
					}
				} else if (moveX > 0 && moveX < this.leftWidth) {
					const leftX = this.leftWidth - moveX
					if (leftX < this.threshold) {
						this.open('left')
					} else {
						this.close()
					}
				}

			}

		},

		/**
		 * 打开
		 */
		open(type) {
			this.x = this.moveX
			this.animation(type)
		},

		/**
		 * 关闭
		 */
		close() {
			this.x = this.moveX
			// TODO 解决 x 值不更新的问题，所以会多触发一次 nextTick ，待优化
			this.$nextTick(() => {
				this.x = -this.leftWidth
				if (this.isopen !== 'none') {
					this.$emit('change', 'none')
				}
				this.isopen = 'none'
			})
		},

		/**
		 * 执行结束动画
		 * @param {Object} type
		 */
		animation(type) {
			this.$nextTick(() => {
				if (type === 'left') {
					this.x = 0
				} else {
					this.x = -this.rightWidth - this.leftWidth
				}

				if (this.isopen !== type) {
					this.$emit('change', type)
				}
				this.isopen = type
			})

		},
		getSlide(x) {},
		getQuerySelect() {
			const query = uni.createSelectorQuery().in(this);
			query.selectAll('.movable-view--hock').boundingClientRect(data => {
				this.leftWidth = data[1].width
				this.rightWidth = data[2].width
				this.width = data[0].width
				this.viewWidth = this.width + this.rightWidth + this.leftWidth
				if (this.leftWidth === 0) {
					// TODO 疑似bug ,初始化的时候如果x 是0，会导致移动位置错误，所以让元素超出一点
					this.x = -0.1
				} else {
					this.x = -this.leftWidth
				}
				this.moveX = this.x
				this.$nextTick(() => {
					this.swipeShow = 1
				})

				if (!this.buttonWidth) {
					this.disabledView = true
				}

				if (this.autoClose) return
				if (this.show !== 'none') {
					this.transition = true
					this.open(this.shows)
				}
			}).exec();

		}
	}
}
