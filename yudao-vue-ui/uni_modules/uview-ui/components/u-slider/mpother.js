/**
 * 使用普通的js方案实现slider
 */
export default {
    watch: {
        value(n) {
            // 只有在非滑动状态时，才可以通过value更新滑块值，这里监听，是为了让用户触发
            if (this.status === 'end') {
                this.updateSliderPlacement(n, true)
            }
        }
    },
    mounted() {
        this.init()
    },
    methods: {
        init() {
            this.getSliderRect()
        },
        // 获取slider尺寸
        getSliderRect() {
            // 获取滑块条的尺寸信息
            setTimeout(() => {
                this.$uGetRect('.u-slider').then((rect) => {
                    this.sliderRect = rect
                    this.updateSliderPlacement(this.value, true)
                })
            }, 10)
        },
        // 是否可以操作
        canNotDo() {
            return this.disabled
        },
        // 获取当前手势点的X轴位移值
        getTouchX(e) {
            return e.touches[0].clientX
        },
        formatStep(value) {
            // 移动点占总长度的百分比
            return Math.round(Math.max(this.min, Math.min(value, this.max)) / this.step) * this.step
        },
        // 发出事件
        emitEvent(event, value) {
            this.$emit(event, value || this.value)
        },
        // 标记当前手势的状态
        setTouchStatus(status) {
            this.status = status
        },
        onTouchStart(e) {
            if (this.canNotDo()) {
                return
            }
            // 标示当前的状态为开始触摸滑动
            this.emitEvent('start')
            this.setTouchStatus('start')
        },
        onTouchMove(e) {
            if (this.canNotDo()) {
                return
            }
            // 滑块的左边不一定跟屏幕左边接壤，所以需要减去最外层父元素的左边值
            const x = this.getTouchX(e)
            const { left, width } = this.sliderRect
            const distanceX = x - left
            // 获得移动距离对整个滑块的百分比值，此为带有多位小数的值，不能用此更新视图
            // 否则造成通信阻塞，需要每改变一个step值时修改一次视图
            const percent = (distanceX / width) * 100
            this.setTouchStatus('moving')
            this.updateSliderPlacement(percent, true, 'moving')
        },
        onTouchEnd() {
            if (this.canNotDo()) {
                return
            }
            this.emitEvent('end')
            this.setTouchStatus('end')
        },
        // 设置滑点的位置
        updateSliderPlacement(value, drag, event) {
            // 去掉小数部分，同时也是对step步进的处理
            const { width } = this.sliderRect
            const percent = this.formatStep(value)
            // 设置移动的值
            const barStyle = {
                width: `${percent / 100 * width}px`
            }
            // 移动期间无需过渡动画
            if (drag === true) {
                barStyle.transition = 'none'
            } else {
                // 非移动期间，删掉对过渡为空的声明，让css中的声明起效
                delete barStyle.transition
            }
            // 修改value值
            this.$emit('input', percent)
            // 事件的名称
            if (event) {
                this.emitEvent(event, percent)
            }
            this.barStyle = barStyle
        },
        onClick(e) {
            if (this.canNotDo()) {
                return
            }
            // 直接点击滑块的情况，计算方式与onTouchMove方法相同
            const { left, width } = this.sliderRect
            const value = ((e.detail.x - left) / width) * 100
            this.updateSliderPlacement(value, false, 'click')
        }
    }
}
