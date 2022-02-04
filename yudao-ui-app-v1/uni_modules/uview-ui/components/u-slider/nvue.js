/**
 * 使用bindingx方案实现slider
 * 只能使用于nvue下
 */
// 引入bindingx，此库类似于微信小程序wxs，目的是让js运行在视图层，减少视图层和逻辑层的通信折损
const BindingX = uni.requireNativePlugin('bindingx')
// nvue操作dom的库，用于获取dom的尺寸信息
const dom = uni.requireNativePlugin('dom')
// nvue中用于操作元素动画的库，类似于uni.animation，只不过uni.animation不能用于nvue
const animation = uni.requireNativePlugin('animation')

export default {
    data() {
        return {
            // 位移的偏移量
            x: 0,
            // 是否正在触摸过程中，用于标记动画类是否添加或移除
            touching: false,
            changeFromInside: false
        }
    },
    watch: {
        // 监听vlaue的变化，此变化可能是由于内部修改v-model的值，或者外部
        // 从服务端获取一个值后，赋值给slider的v-model而导致的
        value(n) {
            if (!this.changeFromInside) {
                this.initX()
            } else {
                this.changeFromInside = false
            }
        }
    },
    mounted() {
        this.init()
    },
    methods: {
        init() {
            // 更新滑块尺寸信息
            this.getSliderRect().then((size) => {
                this.sliderRect = size
                this.initX()
            })
        },
        // 获取节点信息
        // 获取slider尺寸
        getSliderRect() {
            // 获取滑块条的尺寸信息
            // 通过nvue的dom模块，查询节点信息
            return new Promise((resolve) => {
                this.$nextTick(() => {
                    dom.getComponentRect(this.$refs.slider, (res) => {
                        resolve(res.size)
                    })
                })
            })
        },
        // 初始化按钮位置
        initButtonStyle({
            barStyle,
            buttonWrapperStyle
        }) {
            this.barStyle = barStyle
            this.buttonWrapperStyle = buttonWrapperStyle
        },
        emitEvent(event, value) {
            this.$emit(event, value || this.value)
        },
        // 滑动开始
        async onTouchStart(e) {
            // if (this.disabled) return
            // // 阻止页面滚动，可以保证在滑动过程中，不让页面可以上下滚动，造成不好的体验
            // e.stopPropagation && e.stopPropagation()
            // e.preventDefault && e.preventDefault()
            // // 更新滑块的尺寸信息
            // this.sliderRect = await this.getSliderRect()
            // // 标记滑动过程中触摸点的信息
            // this.touchStart(e)
            // this.startValue = this.format(this.value)
            // this.dragStatus = 'start'

            // 标记滑动过程中触摸点的信息
            // this.touchStart(e)
        },
        // 开始滑动
        onTouchMove(e) {
            // if (this.disabled) return;
            // if (this.dragStatus === 'start') {
            // 	this.$emit('drag-start')
            // }
            // // 标记当前滑动过程中的触点信息，此方法在touch mixin中
            // this.touchMove(e)
            // this.dragStatus = 'draging'
            // const {
            // 	width: sliderWidth
            // } = this.sliderRect
            // const diff = (this.deltaX / sliderWidth) * this.getRange()
            // this.newValue = this.startValue + diff
            // this.updateValue(this.newValue, false, true)
            // 获取元素ref
            // const button = this.$refs['nvue-button'].ref
            // const gap = this.$refs['nvue-gap'].ref

            //          animation.transition(gap, {
            // 	styles: {
            //                  width: `${this.startX + this.deltaX}px`
            // 	}
            // })
            // // console.log(this.startX + this.deltaX);
            // animation.transition(button, {
            // 	styles: {
            //         transform: `translateX(${this.startX + this.deltaX}px)`
            // 	}
            // })
            // this.barStyle = {
            // 	width: `${this.startX + this.deltaX}px`
            // }
            const {
                x
            } = this.getTouchPoint(e)
            this.buttonWrapperStyle = {
                transform: `translateX(${x}px)`
            }
            // this.buttonWrapperStyle = {
            // 	transform: `translateX(${this.format(this.startX + this.deltaX)}px)`
            // }
        },
        // onTouchEnd() {
        // 	if (this.disabled) return;
        // 	if (this.dragStatus === 'draging') {
        // 		this.updateValue(this.newValue, true)
        // 		this.$emit('drag-end');
        // 	}
        // },
        updateValue(value, end, drag) {
            value = this.format(value)
            const {
                width: sliderWidth
            } = this.sliderRect
            const width = `${((value - this.min) * sliderWidth) / this.getRange()}`
            this.value = value
            this.barStyle = {
                width: `${width}px`
            }
            // console.log('width', width);
            if (drag) {
                this.$emit('drag', {
                    value
                })
            }
            if (end) {
                this.$emit('change', value)
            }
            if ((drag || end)) {
                this.changeFromInside = true
                this.$emit('update', value)
            }
        },
        // 从value的变化，倒推得出x的值该为多少
        initX() {
            const {
                left,
                width
            } = this.sliderRect
            // 得出x的初始偏移值，之所以需要这么做，是因为在bindingX中，触摸滑动时，只能的值本次移动的偏移值
            // 而无法的值准确的前后移动的两个点的坐标值，weex纯粹为阿里巴巴的KPI(部门业绩考核)产物，也就这样了
            this.x = this.value / 100 * width
            // 设置移动的值
            const barStyle = {
                width: `${this.x}px`
            }
            // 按钮的初始值
            const buttonWrapperStyle = {
                transform: `translateX(${this.x - this.blockHeight / 2}px)`
            }
            this.initButtonStyle({
                barStyle,
                buttonWrapperStyle
            })
        },
        // 移动点占总长度的百分比，此处需要先除以step，是为了保证step大于1时，比如10，那么在滑动11,12px这样的
        // 距离时，实际上滑块是不会滑动的，到了16,17px，经过四舍五入后，就变成了20px，进行了下一个跳变
        format(value) {
            return Math.round(uni.$u.range(this.min, this.max, value) / this.step) * this.step
        },
        getRange() {
            const {
                max,
                min
            } = this
            return max - min
        }
    }
}
