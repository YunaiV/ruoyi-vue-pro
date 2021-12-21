// #ifdef APP-NVUE
const dom = weex.requireModule('dom');
// #endif
export default {
	data() {
		return {
			uniShow: false,
			left: 0
		}
	},
	computed: {
		moveLeft() {
			return `translateX(${this.left}px)`
		}
	},
	watch: {
		show(newVal) {
			if (!this.position || JSON.stringify(this.position) === '{}') return;
			if (this.autoClose) return
			if (newVal) {
				this.$emit('change', true)
				this.open()
			} else {
				this.$emit('change', false)
				this.close()
			}
		}
	},
	mounted() {
		this.position = {}
		if (this.swipeaction.children !== undefined) {
			this.swipeaction.children.push(this)
		}
		setTimeout(() => {
			this.getSelectorQuery()
		}, 100)
	},
	beforeDestoy() {
		this.swipeaction.children.forEach((item, index) => {
			if (item === this) {
				this.swipeaction.children.splice(index, 1)
			}
		})
	},
	methods: {
		onClick(index, item) {
			this.$emit('click', {
				content: item,
				index
			})
			this.close()
		},
		touchstart(e) {
			const {
				pageX
			} = e.touches[0]
			if (this.disabled) return
			const left = this.position.content.left
			if (this.autoClose) {
				this.swipeaction.closeOther(this)
			}
			this.width = pageX - left
			if (this.isopen) return
			if (this.uniShow) {
				this.uniShow = false
				this.isopen = true
				this.openleft = this.left + this.position.button.width
			}
		},
		touchmove(e, index) {
			if (this.disabled) return
			const {
				pageX
			} = e.touches[0]
			this.setPosition(pageX)
		},
		touchend() {
			if (this.disabled) return
			if (this.isopen) {
				this.move(this.openleft, 0)
				return
			}
			this.move(this.left, -40)
		},
		setPosition(x, y) {
			if (!this.position.button.width) {
				return
			}
			// this.left = x - this.width
			this.setValue(x - this.width)
		},
		setValue(value) {
			// 设置最大最小值
			this.left = Math.max(-this.position.button.width, Math.min(parseInt(value), 0))
			this.position.content.left = this.left
			if (this.isopen) {
				this.openleft = this.left + this.position.button.width
			}
		},
		move(left, value) {
			if (left >= value) {
				this.$emit('change', false)
				this.close()
			} else {
				this.$emit('change', true)
				this.open()
			}
		},
		open() {
			this.uniShow = true
			this.left = -this.position.button.width
			this.setValue(-this.position.button.width)
		},
		close() {
			this.uniShow = true
			this.setValue(0)
			setTimeout(() => {
				this.uniShow = false
				this.isopen = false
			}, 300)
		},
		getSelectorQuery() {
			// #ifndef APP-NVUE
			const views = uni.createSelectorQuery()
				.in(this)
			views
				.selectAll('.selector-query-hock')
				.boundingClientRect(data => {
					this.position.content = data[1]
					this.position.button = data[0]
					if (this.autoClose) return
					if (this.show) {
						this.open()
					} else {
						this.close()
					}
				})
				.exec()
			// #endif
			// #ifdef APP-NVUE
			dom.getComponentRect(this.$refs['selector-content-hock'], (data) => {
				if (this.position.content) return
				this.position.content = data.size
			})
			dom.getComponentRect(this.$refs['selector-button-hock'], (data) => {
				if (this.position.button) return
				this.position.button = data.size
				if (this.autoClose) return
				if (this.show) {
					this.open()
				} else {
					this.close()
				}
			})
			// #endif
		}
	}
}
