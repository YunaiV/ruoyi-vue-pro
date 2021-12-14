export default {
	data() {
		return {
			isshow: false,
			viewWidth: 0,
			buttonWidth: 0,
			disabledView: false,
			x: 0,
			transition: false
		}
	},
	watch: {
		show(newVal) {
			if (this.autoClose) return
			if (newVal) {
				this.open()
			} else {
				this.close()
			}
		},
	},
	created() {
		if (this.swipeaction.children !== undefined) {
			this.swipeaction.children.push(this)
		}
	},
	beforeDestroy() {
		this.swipeaction.children.forEach((item, index) => {
			if (item === this) {
				this.swipeaction.children.splice(index, 1)
			}
		})
	},
	mounted() {
		this.isopen = false
		this.transition = true
		setTimeout(() => {
			this.getQuerySelect()
		}, 50)

	},
	methods: {
		onClick(index, item) {
			this.$emit('click', {
				content: item,
				index
			})
		},
		touchstart(e) {
			let {
				pageX,
				pageY
			} = e.changedTouches[0]
			this.transition = false
			this.startX = pageX
			if (this.autoClose) {
				this.swipeaction.closeOther(this)
			}
		},
		touchmove(e) {
			let {
				pageX,
			} = e.changedTouches[0]
			this.slide = this.getSlide(pageX)
			if (this.slide === 0) {
				this.disabledView = false
			}

		},
		touchend(e) {
			this.stop = false
			this.transition = true
			if (this.isopen) {
				if (this.moveX === -this.buttonWidth) {
					this.close()
					return
				}
				this.move()
			} else {
				if (this.moveX === 0) {
					this.close()
					return
				}
				this.move()
			}
		},
		open() {
			this.x = this.moveX
			this.$nextTick(() => {
				this.x = -this.buttonWidth
				this.moveX = this.x
				
				if(!this.isopen){
					this.isopen = true
					this.$emit('change', true)
				}
			})
		},
		close() {
			this.x = this.moveX
			this.$nextTick(() => {
				this.x = 0
				this.moveX = this.x
				if(this.isopen){
					this.isopen = false
					this.$emit('change', false)
				}
			})
		},
		move() {
			if (this.slide === 0) {
				this.open()
			} else {
				this.close()
			}
		},
		onChange(e) {
			let x = e.detail.x
			this.moveX = x
			if (x >= this.buttonWidth) {
				this.disabledView = true
				this.$nextTick(() => {
					this.x = this.buttonWidth
				})
			}
		},
		getSlide(x) {
			if (x >= this.startX) {
				this.startX = x
				return 1
			} else {
				this.startX = x
				return 0
			}

		},
		getQuerySelect() {
			const query = uni.createSelectorQuery().in(this);
			query.selectAll('.viewWidth-hook').boundingClientRect(data => {

				this.viewWidth = data[0].width
				this.buttonWidth = data[1].width
				this.transition = false
				this.$nextTick(() => {
					this.transition = true
				})

				if (!this.buttonWidth) {
					this.disabledView = true
				}

				if (this.autoClose) return
				if (this.show) {
					this.open()
				}
			}).exec();

		}
	}
}
