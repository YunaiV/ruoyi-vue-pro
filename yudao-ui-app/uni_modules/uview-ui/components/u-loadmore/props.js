export default {
    props: {
        // 组件状态，loadmore-加载前的状态，loading-加载中的状态，nomore-没有更多的状态
        status: {
            type: String,
            default: uni.$u.props.loadmore.status
        },
        // 组件背景色
        bgColor: {
            type: String,
            default: uni.$u.props.loadmore.bgColor
        },
        // 是否显示加载中的图标
        icon: {
            type: Boolean,
            default: uni.$u.props.loadmore.icon
        },
        // 字体大小
        fontSize: {
            type: [String, Number],
            default: uni.$u.props.loadmore.fontSize
        },
		    // 图标大小
        iconSize: {
            type: [String, Number],
            default: uni.$u.props.loadmore.iconSize
        },
        // 字体颜色
        color: {
            type: String,
            default: uni.$u.props.loadmore.color
        },
        // 加载中状态的图标，spinner-花朵状图标，circle-圆圈状，semicircle-半圆
        loadingIcon: {
            type: String,
            default: uni.$u.props.loadmore.loadingIcon
        },
        // 加载前的提示语
        loadmoreText: {
            type: String,
            default: uni.$u.props.loadmore.loadmoreText
        },
        // 加载中提示语
        loadingText: {
            type: String,
            default: uni.$u.props.loadmore.loadingText
        },
        // 没有更多的提示语
        nomoreText: {
            type: String,
            default: uni.$u.props.loadmore.nomoreText
        },
        // 在“没有更多”状态下，是否显示粗点
        isDot: {
            type: Boolean,
            default: uni.$u.props.loadmore.isDot
        },
        // 加载中图标的颜色
        iconColor: {
            type: String,
            default: uni.$u.props.loadmore.iconColor
        },
        // 上边距
        marginTop: {
            type: [String, Number],
            default: uni.$u.props.loadmore.marginTop
        },
        // 下边距
        marginBottom: {
            type: [String, Number],
            default: uni.$u.props.loadmore.marginBottom
        },
        // 高度，单位px
        height: {
            type: [String, Number],
            default: uni.$u.props.loadmore.height
        },
        // 是否显示左边分割线
        line: {
            type: Boolean,
            default: uni.$u.props.loadmore.line
        },
        // 线条颜色
        lineColor: {
            type: String,
            default: uni.$u.props.loadmore.lineColor
        },
        // 是否虚线，true-虚线，false-实线
        dashed: {
            type: Boolean,
            default: uni.$u.props.loadmore.dashed
        }
    }
}
