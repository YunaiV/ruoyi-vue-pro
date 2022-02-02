export default {
    props: {
        // 标题
        title: {
            type: String,
            default: uni.$u.props.collapseItem.title
        },
        // 标题右侧内容
        value: {
            type: String,
            default: uni.$u.props.collapseItem.value
        },
        // 标题下方的描述信息
        label: {
            type: String,
            default: uni.$u.props.collapseItem.label
        },
        // 是否禁用折叠面板
        disabled: {
            type: Boolean,
            default: uni.$u.props.collapseItem.disabled
        },
        // 是否展示右侧箭头并开启点击反馈
        isLink: {
            type: Boolean,
            default: uni.$u.props.collapseItem.isLink
        },
        // 是否开启点击反馈
        clickable: {
            type: Boolean,
            default: uni.$u.props.collapseItem.clickable
        },
        // 是否显示内边框
        border: {
            type: Boolean,
            default: uni.$u.props.collapseItem.border
        },
        // 标题的对齐方式
        align: {
            type: String,
            default: uni.$u.props.collapseItem.align
        },
        // 唯一标识符
        name: {
            type: [String, Number],
            default: uni.$u.props.collapseItem.name
        },
        // 标题左侧图片，可为绝对路径的图片或内置图标
        icon: {
            type: String,
            default: uni.$u.props.collapseItem.icon
        },
        // 面板展开收起的过渡时间，单位ms
        duration: {
            type: Number,
            default: uni.$u.props.collapseItem.duration
        }
    }
}
