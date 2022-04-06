export default {
    props: {
        // 是否显示组件
        show: {
            type: Boolean,
            default: uni.$u.props.loadingIcon.show
        },
        // 颜色
        color: {
            type: String,
            default: uni.$u.props.loadingIcon.color
        },
        // 提示文字颜色
        textColor: {
            type: String,
            default: uni.$u.props.loadingIcon.textColor
        },
        // 文字和图标是否垂直排列
        vertical: {
            type: Boolean,
            default: uni.$u.props.loadingIcon.vertical
        },
        // 模式选择，circle-圆形，spinner-花朵形，semicircle-半圆形
        mode: {
            type: String,
            default: uni.$u.props.loadingIcon.mode
        },
        // 图标大小，单位默认px
        size: {
            type: [String, Number],
            default: uni.$u.props.loadingIcon.size
        },
        // 文字大小
        textSize: {
            type: [String, Number],
            default: uni.$u.props.loadingIcon.textSize
        },
        // 文字内容
        text: {
            type: [String, Number],
            default: uni.$u.props.loadingIcon.text
        },
        // 动画模式
        timingFunction: {
            type: String,
            default: uni.$u.props.loadingIcon.timingFunction
        },
        // 动画执行周期时间
        duration: {
            type: [String, Number],
            default: uni.$u.props.loadingIcon.duration
        },
        // mode=circle时的暗边颜色
        inactiveColor: {
            type: String,
            default: uni.$u.props.loadingIcon.inactiveColor
        }
    }
}
