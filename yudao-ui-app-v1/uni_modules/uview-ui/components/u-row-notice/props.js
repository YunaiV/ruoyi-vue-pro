export default {
    props: {
        // 显示的内容，字符串
        text: {
            type: String,
            default: uni.$u.props.rowNotice.text
        },
        // 是否显示左侧的音量图标
        icon: {
            type: String,
            default: uni.$u.props.rowNotice.icon
        },
        // 通告模式，link-显示右箭头，closable-显示右侧关闭图标
        mode: {
            type: String,
            default: uni.$u.props.rowNotice.mode
        },
        // 文字颜色，各图标也会使用文字颜色
        color: {
            type: String,
            default: uni.$u.props.rowNotice.color
        },
        // 背景颜色
        bgColor: {
            type: String,
            default: uni.$u.props.rowNotice.bgColor
        },
        // 字体大小，单位px
        fontSize: {
            type: [String, Number],
            default: uni.$u.props.rowNotice.fontSize
        },
        // 水平滚动时的滚动速度，即每秒滚动多少px(rpx)，这有利于控制文字无论多少时，都能有一个恒定的速度
        speed: {
            type: [String, Number],
            default: uni.$u.props.rowNotice.speed
        }
    }
}
