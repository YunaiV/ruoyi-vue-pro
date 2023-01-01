export default {
    props: {
        // 指示器的整体宽度
        indicatorWidth: {
            type: [String, Number],
            default: uni.$u.props.scrollList.indicatorWidth
        },
        // 滑块的宽度
        indicatorBarWidth: {
            type: [String, Number],
            default: uni.$u.props.scrollList.indicatorBarWidth
        },
        // 是否显示面板指示器
        indicator: {
            type: Boolean,
            default: uni.$u.props.scrollList.indicator
        },
        // 指示器非激活颜色
        indicatorColor: {
            type: String,
            default: uni.$u.props.scrollList.indicatorColor
        },
        // 指示器的激活颜色
        indicatorActiveColor: {
            type: String,
            default: uni.$u.props.scrollList.indicatorActiveColor
        },
        // 指示器样式，可通过bottom，left，right进行定位
        indicatorStyle: {
            type: [String, Object],
            default: uni.$u.props.scrollList.indicatorStyle
        }
    }
}
