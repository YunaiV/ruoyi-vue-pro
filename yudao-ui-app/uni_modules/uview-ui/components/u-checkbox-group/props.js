export default {
    props: {
        // 标识符
        name: {
            type: String,
            default: uni.$u.props.checkboxGroup.name
        },
        // 绑定的值
        value: {
            type: Array,
            default: uni.$u.props.checkboxGroup.value
        },
        // 形状，circle-圆形，square-方形
        shape: {
            type: String,
            default: uni.$u.props.checkboxGroup.shape
        },
        // 是否禁用全部checkbox
        disabled: {
            type: Boolean,
            default: uni.$u.props.checkboxGroup.disabled
        },

        // 选中状态下的颜色，如设置此值，将会覆盖parent的activeColor值
        activeColor: {
            type: String,
            default: uni.$u.props.checkboxGroup.activeColor
        },
        // 未选中的颜色
        inactiveColor: {
            type: String,
            default: uni.$u.props.checkboxGroup.inactiveColor
        },

        // 整个组件的尺寸，默认px
        size: {
            type: [String, Number],
            default: uni.$u.props.checkboxGroup.size
        },
        // 布局方式，row-横向，column-纵向
        placement: {
            type: String,
            default: uni.$u.props.checkboxGroup.placement
        },
        // label的字体大小，px单位
        labelSize: {
            type: [String, Number],
            default: uni.$u.props.checkboxGroup.labelSize
        },
        // label的字体颜色
        labelColor: {
            type: [String],
            default: uni.$u.props.checkboxGroup.labelColor
        },
        // 是否禁止点击文本操作
        labelDisabled: {
            type: Boolean,
            default: uni.$u.props.checkboxGroup.labelDisabled
        },
        // 图标颜色
        iconColor: {
            type: String,
            default: uni.$u.props.checkboxGroup.iconColor
        },
        // 图标的大小，单位px
        iconSize: {
            type: [String, Number],
            default: uni.$u.props.checkboxGroup.iconSize
        },
        // 勾选图标的对齐方式，left-左边，right-右边
        iconPlacement: {
            type: String,
            default: uni.$u.props.checkboxGroup.iconPlacement
        },
        // 竖向配列时，是否显示下划线
        borderBottom: {
            type: Boolean,
            default: uni.$u.props.checkboxGroup.borderBottom
        }

    }
}
