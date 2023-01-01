export default {
    props: {
        // 到顶部的距离
        top: {
            type: [String, Number],
            default: uni.$u.props.notify.top
        },
        // 是否展示组件
        // show: {
        // 	type: Boolean,
        // 	default: uni.$u.props.notify.show
        // },
        // type主题，primary，success，warning，error
        type: {
            type: String,
            default: uni.$u.props.notify.type
        },
        // 字体颜色
        color: {
            type: String,
            default: uni.$u.props.notify.color
        },
        // 背景颜色
        bgColor: {
            type: String,
            default: uni.$u.props.notify.bgColor
        },
        // 展示的文字内容
        message: {
            type: String,
            default: uni.$u.props.notify.message
        },
        // 展示时长，为0时不消失，单位ms
        duration: {
            type: [String, Number],
            default: uni.$u.props.notify.duration
        },
        // 字体大小
        fontSize: {
            type: [String, Number],
            default: uni.$u.props.notify.fontSize
        },
        // 是否留出顶部安全距离（状态栏高度）
        safeAreaInsetTop: {
            type: Boolean,
            default: uni.$u.props.notify.safeAreaInsetTop
        }
    }
}
