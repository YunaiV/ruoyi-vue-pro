export default {
    props: {
        // 滑块的移动过渡时间，单位ms
        duration: {
            type: Number,
            default: uni.$u.props.tabs.duration
        },
        // tabs标签数组
        list: {
            type: Array,
            default: uni.$u.props.tabs.list
        },
        // 滑块颜色
        lineColor: {
            type: String,
            default: uni.$u.props.tabs.lineColor
        },
        // 菜单选择中时的样式
        activeStyle: {
            type: [String, Object],
            default: uni.$u.props.tabs.activeStyle
        },
        // 菜单非选中时的样式
        inactiveStyle: {
            type: [String, Object],
            default: uni.$u.props.tabs.inactiveStyle
        },
        // 滑块长度
        lineWidth: {
            type: [String, Number],
            default: uni.$u.props.tabs.lineWidth
        },
        // 滑块高度
        lineHeight: {
            type: [String, Number],
            default: uni.$u.props.tabs.lineHeight
        },
        // 菜单item的样式
        itemStyle: {
            type: [String, Object],
            default: uni.$u.props.tabs.itemStyle
        },
        // 菜单是否可滚动
        scrollable: {
            type: Boolean,
            default: uni.$u.props.tabs.scrollable
        }
    }
}
