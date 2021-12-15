export default {
    props: {
        // 显示的内容，数组
        text: {
            type: [Array, String],
            default: uni.$u.props.noticeBar.text
        },
        // 通告滚动模式，row-横向滚动，column-竖向滚动
        direction: {
            type: String,
            default: uni.$u.props.noticeBar.direction
        },
        // direction = row时，是否使用步进形式滚动
        step: {
            type: Boolean,
            default: uni.$u.props.noticeBar.step
        },
        // 是否显示左侧的音量图标
        icon: {
            type: String,
            default: uni.$u.props.noticeBar.icon
        },
        // 通告模式，link-显示右箭头，closable-显示右侧关闭图标
        mode: {
            type: String,
            default: uni.$u.props.noticeBar.mode
        },
        // 文字颜色，各图标也会使用文字颜色
        color: {
            type: String,
            default: uni.$u.props.noticeBar.color
        },
        // 背景颜色
        bgColor: {
            type: String,
            default: uni.$u.props.noticeBar.bgColor
        },
        // 水平滚动时的滚动速度，即每秒滚动多少px(px)，这有利于控制文字无论多少时，都能有一个恒定的速度
        speed: {
            type: [String, Number],
            default: uni.$u.props.noticeBar.speed
        },
        // 字体大小
        fontSize: {
            type: [String, Number],
            default: uni.$u.props.noticeBar.fontSize
        },
        // 滚动一个周期的时间长，单位ms
        duration: {
            type: [String, Number],
            default: uni.$u.props.noticeBar.duration
        },
        // 是否禁止用手滑动切换
        // 目前HX2.6.11，只支持App 2.5.5+、H5 2.5.5+、支付宝小程序、字节跳动小程序
        disableTouch: {
            type: Boolean,
            default: uni.$u.props.noticeBar.disableTouch
        },
        // 跳转的页面路径
        url: {
            type: String,
            default: uni.$u.props.noticeBar.url
        },
        // 页面跳转的类型
        linkType: {
            type: String,
            default: uni.$u.props.noticeBar.linkType
        }
    }
}
