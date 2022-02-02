// nvue操作dom的库，用于获取dom的尺寸信息
const dom = uni.requireNativePlugin('dom');
const bindingX = uni.requireNativePlugin('bindingx');
const animation = uni.requireNativePlugin('animation');

export default {
	data() {
		return {
			// 所有按钮的总宽度
			buttonsWidth: 0,
			// 是否正在移动中
			moving: false
		}
	},
	computed: {
		// 获取过渡时间
		getDuratin() {
			let duration = String(this.duration)
			// 如果ms为单位，返回ms的数值部分
			if (duration.indexOf('ms') >= 0) return parseInt(duration)
			// 如果s为单位，为了得到ms的数值，需要乘以1000
			if (duration.indexOf('s') >= 0) return parseInt(duration) * 1000
			// 如果值传了数值，且小于30，认为是s单位
			duration = Number(duration)
			return duration < 30 ? duration * 1000 : duration
		}
	},
	watch: {
		show(n) {
			if(n) {
				this.moveCellByAnimation('open') 
			} else {
				this.moveCellByAnimation('close') 
			}
		}
	},
	mounted() {
		this.initialize()
	},
	methods: {
		initialize() {
			this.queryRect() 
		},
		// 关闭单元格，用于打开一个，自动关闭其他单元格的场景
		closeHandler() {
			if(this.status === 'open') {
				// 如果在打开状态下，进行点击的话，直接关闭单元格
				return this.moveCellByAnimation('close') && this.unbindBindingX()
			}
		},
		// 点击单元格
		clickHandler() {
			// 如果在移动中被点击，进行忽略
			if(this.moving) return
			// 尝试关闭其他打开的单元格
			this.parent && this.parent.closeOther(this)
			if(this.status === 'open') {
				// 如果在打开状态下，进行点击的话，直接关闭单元格
				return this.moveCellByAnimation('close') && this.unbindBindingX()
			}
		},
		// 滑动单元格
		onTouchstart(e) {
			// 如果当前正在移动中，或者disabled状态，则返回
			if(this.moving || this.disabled) { 
				return this.unbindBindingX()   
			}
			if(this.status === 'open') {
				// 如果在打开状态下，进行点击的话，直接关闭单元格
				return this.moveCellByAnimation('close') && this.unbindBindingX()
			}
			e.stopPropagation && e.stopPropagation() 
			e.preventDefault && e.preventDefault()
			this.moving = true
			// 获取元素ref
			const content = this.getContentRef()
			let expression = `min(max(${-this.buttonsWidth}, x), 0)`
			// 尝试关闭其他打开的单元格
			this.parent && this.parent.closeOther(this)
			
			// 阿里为了KPI而开源的BindingX
			this.panEvent = bindingX.bind({
				anchor: content,
				eventType: 'pan',
				props: [{
					element: content,
					// 绑定width属性，设置其宽度值
					property: 'transform.translateX',
					expression
				}]
			}, (res) => {
				this.moving = false
				if (res.state === 'end' || res.state === 'exit') {
					const deltaX = res.deltaX
					if(deltaX <= -this.buttonsWidth || deltaX >= 0) {
						// 如果触摸滑动的过程中，大于单元格的总宽度，或者大于0，意味着已经动过滑动达到了打开或者关闭的状态
						// 这里直接进行状态的标记
						this.$nextTick(() => {
							this.status = deltaX <= -this.buttonsWidth ? 'open' : 'close'
						})
					} else if(Math.abs(deltaX) > uni.$u.getPx(this.threshold)) {
						// 在移动大于阈值、并且小于总按钮宽度时，进行自动打开或者关闭
						// 移动距离大于0时，意味着需要关闭状态
						if(Math.abs(deltaX) < this.buttonsWidth) {
							this.moveCellByAnimation(deltaX > 0 ? 'close' : 'open')
						}
					} else {
						// 在小于阈值时，进行关闭操作(如果在打开状态下，将不会执行bindingX)
						this.moveCellByAnimation('close')
					}
				}
			})
		},
		// 释放bindingX
		unbindBindingX() {
			// 释放上一次的资源
			if (this?.panEvent?.token != 0) {
				bindingX.unbind({
					token: this.panEvent?.token,
					// pan为手势事件
					eventType: 'pan'
				})
			}
		},
		// 查询按钮节点信息
		queryRect() {
			// 历遍所有按钮数组，通过getRectByDom返回一个promise
			const promiseAll = this.options.map((item, index) => {
				return this.getRectByDom(this.$refs[`u-swipe-action-item__right__button-${index}`][0])
			})
			// 通过promise.all方法，让所有按钮的查询结果返回一个数组的形式
			Promise.all(promiseAll).then(sizes => {
				this.buttons = sizes
				// 计算所有按钮总宽度
				this.buttonsWidth = sizes.reduce((sum, cur) => sum + cur.width, 0)
			})
		},
		// 通过nvue的dom模块，查询节点信息
		getRectByDom(ref) {
			return new Promise(resolve => {
				dom.getComponentRect(ref, res => {
					resolve(res.size)
				})
			}) 
		},
		// 移动单元格到左边或者右边尽头
		moveCellByAnimation(status = 'open') {
			if(this.moving) return
			// 标识当前状态
			this.moveing = true
			const content = this.getContentRef()
			const x = status === 'open' ? -this.buttonsWidth : 0 
			animation.transition(content, {
				styles: {
					transform: `translateX(${x}px)`,
				},
				duration: uni.$u.getDuration(this.duration, false),
				timingFunction: 'ease-in-out'
			}, () => {
				this.moving = false
				this.status = status
				this.unbindBindingX()
			})
		},
		// 获取元素ref
		getContentRef() {
			return this.$refs['u-swipe-action-item__content'].ref
		},
		beforeDestroy() {
			this.unbindBindingX()
		}
	}
}
