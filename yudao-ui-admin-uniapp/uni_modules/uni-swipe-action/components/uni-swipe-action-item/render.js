const MIN_DISTANCE = 10;
export default {
	showWatch(newVal, oldVal, ownerInstance, instance, self) {
		var state = self.state
		var $el = ownerInstance.$el || ownerInstance.$vm && ownerInstance.$vm.$el
		if (!$el) return
		this.getDom(instance, ownerInstance, self)
		if (newVal && newVal !== 'none') {
			this.openState(newVal, instance, ownerInstance, self)
			return
		}

		if (state.left) {
			this.openState('none', instance, ownerInstance, self)
		}
		this.resetTouchStatus(instance, self)
	},

	/**
	 * 开始触摸操作
	 * @param {Object} e
	 * @param {Object} ins
	 */
	touchstart(e, ownerInstance, self) {
		let instance = e.instance;
		let disabled = instance.getDataset().disabled
		let state = self.state;
		this.getDom(instance, ownerInstance, self)
		// fix by mehaotian, TODO 兼容 app-vue 获取dataset为字符串 , h5 获取 为 undefined 的问题,待框架修复
		disabled = this.getDisabledType(disabled)
		if (disabled) return
		// 开始触摸时移除动画类
		instance.requestAnimationFrame(function() {
			instance.removeClass('ani');
			ownerInstance.callMethod('closeSwipe');
		})

		// 记录上次的位置
		state.x = state.left || 0
		// 计算滑动开始位置
		this.stopTouchStart(e, ownerInstance, self)
	},

	/**
	 * 开始滑动操作
	 * @param {Object} e
	 * @param {Object} ownerInstance
	 */
	touchmove(e, ownerInstance, self) {
		let instance = e.instance;
		// 删除之后已经那不到实例了
		if (!instance) return;
		let disabled = instance.getDataset().disabled
		let state = self.state
		// fix by mehaotian, TODO 兼容 app-vue 获取dataset为字符串 , h5 获取 为 undefined 的问题,待框架修复
		disabled = this.getDisabledType(disabled)
		if (disabled) return
		// 是否可以滑动页面
		this.stopTouchMove(e, self);
		if (state.direction !== 'horizontal') {
			return;
		}
		if (e.preventDefault) {
			// 阻止页面滚动
			e.preventDefault()
		}
		let x = state.x + state.deltaX
		this.move(x, instance, ownerInstance, self)
	},

	/**
	 * 结束触摸操作
	 * @param {Object} e
	 * @param {Object} ownerInstance
	 */
	touchend(e, ownerInstance, self) {
		let instance = e.instance;
		let disabled = instance.getDataset().disabled
		let state = self.state
		// fix by mehaotian, TODO 兼容 app-vue 获取dataset为字符串 , h5 获取 为 undefined 的问题,待框架修复
		disabled = this.getDisabledType(disabled)

		if (disabled) return
		// 滑动过程中触摸结束,通过阙值判断是开启还是关闭
		// fixed by mehaotian 定时器解决点击按钮，touchend 触发比 click 事件时机早的问题 ，主要是 ios13
		this.moveDirection(state.left, instance, ownerInstance, self)

	},

	/**
	 * 设置移动距离
	 * @param {Object} value
	 * @param {Object} instance
	 * @param {Object} ownerInstance
	 */
	move(value, instance, ownerInstance, self) {
		value = value || 0
		let state = self.state
		let leftWidth = state.leftWidth
		let rightWidth = state.rightWidth
		// 获取可滑动范围
		state.left = this.range(value, -rightWidth, leftWidth);
		instance.requestAnimationFrame(function() {
			instance.setStyle({
				transform: 'translateX(' + state.left + 'px)',
				'-webkit-transform': 'translateX(' + state.left + 'px)'
			})
		})

	},

	/**
	 * 获取元素信息
	 * @param {Object} instance
	 * @param {Object} ownerInstance
	 */
	getDom(instance, ownerInstance, self) {
		var state = self.state
		var $el = ownerInstance.$el || ownerInstance.$vm && ownerInstance.$vm.$el
		var leftDom = $el.querySelector('.button-group--left')
		var rightDom = $el.querySelector('.button-group--right')

		state.leftWidth = leftDom.offsetWidth || 0
		state.rightWidth = rightDom.offsetWidth || 0
		state.threshold = instance.getDataset().threshold
	},

	getDisabledType(value) {
		return (typeof(value) === 'string' ? JSON.parse(value) : value) || false;
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
	 * @param {Object} ownerInstance
	 * @param {Object} ins
	 */
	moveDirection(left, ins, ownerInstance, self) {
		var state = self.state
		var threshold = state.threshold
		var position = state.position
		var isopen = state.isopen || 'none'
		var leftWidth = state.leftWidth
		var rightWidth = state.rightWidth
		if (state.deltaX === 0) {
			this.openState('none', ins, ownerInstance, self)
			return
		}
		if ((isopen === 'none' && rightWidth > 0 && -left > threshold) || (isopen !== 'none' && rightWidth > 0 &&
				rightWidth +
				left < threshold)) {
			// right
			this.openState('right', ins, ownerInstance, self)
		} else if ((isopen === 'none' && leftWidth > 0 && left > threshold) || (isopen !== 'none' && leftWidth > 0 &&
				leftWidth - left < threshold)) {
			// left
			this.openState('left', ins, ownerInstance, self)
		} else {
			// default
			this.openState('none', ins, ownerInstance, self)
		}
	},


	/**
	 * 开启状态
	 * @param {Boolean} type
	 * @param {Object} ins
	 * @param {Object} ownerInstance
	 */
	openState(type, ins, ownerInstance, self) {
		let state = self.state
		let leftWidth = state.leftWidth
		let rightWidth = state.rightWidth
		let left = ''
		state.isopen = state.isopen ? state.isopen : 'none'
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

		// && !state.throttle

		if (state.isopen !== type) {
			state.throttle = true
			ownerInstance.callMethod('change', {
				open: type
			})

		}

		state.isopen = type
		// 添加动画类
		ins.requestAnimationFrame(() => {
			ins.addClass('ani');
			this.move(left, ins, ownerInstance, self)
		})
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
	resetTouchStatus(instance, self) {
		let state = self.state;
		state.direction = '';
		state.deltaX = 0;
		state.deltaY = 0;
		state.offsetX = 0;
		state.offsetY = 0;
	},

	/**
	 * 设置滑动开始位置
	 * @param {Object} event
	 */
	stopTouchStart(event, ownerInstance, self) {
		let instance = event.instance;
		let state = self.state
		this.resetTouchStatus(instance, self);
		var touch = event.touches[0];
		state.startX = touch.clientX;
		state.startY = touch.clientY;
	},

	/**
	 * 滑动中，是否禁止打开
	 * @param {Object} event
	 */
	stopTouchMove(event, self) {
		let instance = event.instance;
		let state = self.state;
		let touch = event.touches[0];

		state.deltaX = touch.clientX - state.startX;
		state.deltaY = touch.clientY - state.startY;
		state.offsetY = Math.abs(state.deltaY);
		state.offsetX = Math.abs(state.deltaX);
		state.direction = state.direction || this.getDirection(state.offsetX, state.offsetY);
	}
}
