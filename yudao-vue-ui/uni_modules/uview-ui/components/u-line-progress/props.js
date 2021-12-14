export default {
    props: {
        // 激活部分的颜色
        activeColor: {
            type: String,
            default: uni.$u.props.lineProgress.activeColor
        },
        inactiveColor: {
            type: String,
            default: uni.$u.props.lineProgress.color
        },
        // 进度百分比，数值
        percentage: {
            type: [String, Number],
            default: uni.$u.props.lineProgress.inactiveColor
        },
        // 是否在进度条内部显示百分比的值
        showText: {
            type: Boolean,
            default: uni.$u.props.lineProgress.showText
        },
        // 进度条的高度，单位px
        height: {
            type: [String, Number],
            default: uni.$u.props.lineProgress.height
        }
    }
}
