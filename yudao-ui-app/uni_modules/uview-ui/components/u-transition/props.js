export default {
    props: {
        // 是否展示组件
        show: {
            type: Boolean,
            default: uni.$u.props.transition.show
        },
        // 使用的动画模式
        mode: {
            type: String,
            default: uni.$u.props.transition.mode
        },
        // 动画的执行时间，单位ms
        duration: {
            type: [String, Number],
            default: uni.$u.props.transition.duration
        },
        // 使用的动画过渡函数
        timingFunction: {
            type: String,
            default: uni.$u.props.transition.timingFunction
        }
    }
}
