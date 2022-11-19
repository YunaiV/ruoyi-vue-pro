export default {
    props: {
        // 提示内容
        loadingText: {
            type: [String, Number],
            default: uni.$u.props.loadingPage.loadingText
        },
        // 文字上方用于替换loading动画的图片
        image: {
            type: String,
            default: uni.$u.props.loadingPage.image
        },
        // 加载动画的模式，circle-圆形，spinner-花朵形，semicircle-半圆形
        loadingMode: {
            type: String,
            default: uni.$u.props.loadingPage.loadingMode
        },
        // 是否加载中
        loading: {
            type: Boolean,
            default: uni.$u.props.loadingPage.loading
        },
        // 背景色
        bgColor: {
            type: String,
            default: uni.$u.props.loadingPage.bgColor
        },
        // 文字颜色
        color: {
            type: String,
            default: uni.$u.props.loadingPage.color
        },
        // 文字大小
        fontSize: {
            type: [String, Number],
            default: uni.$u.props.loadingPage.fontSize
        },
		// 图标大小
		iconSize: {
		    type: [String, Number],
		    default: uni.$u.props.loadingPage.fontSize
		},
        // 加载中图标的颜色，只能rgb或者十六进制颜色值
        loadingColor: {
            type: String,
            default: uni.$u.props.loadingPage.loadingColor
        }
    }
}
