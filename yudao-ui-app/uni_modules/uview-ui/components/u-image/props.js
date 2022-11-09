export default {
    props: {
        // 图片地址
        src: {
            type: String,
            default: uni.$u.props.image.src
        },
        // 裁剪模式
        mode: {
            type: String,
            default: uni.$u.props.image.mode
        },
        // 宽度，单位任意
        width: {
            type: [String, Number],
            default: uni.$u.props.image.width
        },
        // 高度，单位任意
        height: {
            type: [String, Number],
            default: uni.$u.props.image.height
        },
        // 图片形状，circle-圆形，square-方形
        shape: {
            type: String,
            default: uni.$u.props.image.shape
        },
        // 圆角，单位任意
        radius: {
            type: [String, Number],
            default: uni.$u.props.image.radius
        },
        // 是否懒加载，微信小程序、App、百度小程序、字节跳动小程序
        lazyLoad: {
            type: Boolean,
            default: uni.$u.props.image.lazyLoad
        },
        // 开启长按图片显示识别微信小程序码菜单
        showMenuByLongpress: {
            type: Boolean,
            default: uni.$u.props.image.showMenuByLongpress
        },
        // 加载中的图标，或者小图片
        loadingIcon: {
            type: String,
            default: uni.$u.props.image.loadingIcon
        },
        // 加载失败的图标，或者小图片
        errorIcon: {
            type: String,
            default: uni.$u.props.image.errorIcon
        },
        // 是否显示加载中的图标或者自定义的slot
        showLoading: {
            type: Boolean,
            default: uni.$u.props.image.showLoading
        },
        // 是否显示加载错误的图标或者自定义的slot
        showError: {
            type: Boolean,
            default: uni.$u.props.image.showError
        },
        // 是否需要淡入效果
        fade: {
            type: Boolean,
            default: uni.$u.props.image.fade
        },
        // 只支持网络资源，只对微信小程序有效
        webp: {
            type: Boolean,
            default: uni.$u.props.image.webp
        },
        // 过渡时间，单位ms
        duration: {
            type: [String, Number],
            default: uni.$u.props.image.duration
        },
        // 背景颜色，用于深色页面加载图片时，为了和背景色融合
        bgColor: {
            type: String,
            default: uni.$u.props.image.bgColor
        }
    }
}
