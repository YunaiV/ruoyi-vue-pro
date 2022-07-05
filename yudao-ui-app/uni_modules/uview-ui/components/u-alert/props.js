export default {
    props: {
        // 显示文字
        title: {
            type: String,
            default: uni.$u.props.alert.title
        },
        // 主题，success/warning/info/error
        type: {
            type: String,
            default: uni.$u.props.alert.type
        },
        // 辅助性文字
        description: {
            type: String,
            default: uni.$u.props.alert.description
        },
        // 是否可关闭
        closable: {
            type: Boolean,
            default: uni.$u.props.alert.closable
        },
        // 是否显示图标
        showIcon: {
            type: Boolean,
            default: uni.$u.props.alert.showIcon
        },
        // 浅或深色调，light-浅色，dark-深色
        effect: {
            type: String,
            default: uni.$u.props.alert.effect
        },
        // 文字是否居中
        center: {
            type: Boolean,
            default: uni.$u.props.alert.center
        },
        // 字体大小
        fontSize: {
            type: [String, Number],
            default: uni.$u.props.alert.fontSize
        }
    }
}
