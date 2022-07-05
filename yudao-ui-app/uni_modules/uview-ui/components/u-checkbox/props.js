export default {
    props: {
        // checkbox的名称
        name: {
            type: [String, Number, Boolean],
            default: uni.$u.props.checkbox.name
        },
        // 形状，square为方形，circle为圆型
        shape: {
            type: String,
            default: uni.$u.props.checkbox.shape
        },
        // 整体的大小
        size: {
            type: [String, Number],
            default: uni.$u.props.checkbox.size
        },
        // 是否默认选中
        checked: {
            type: Boolean,
            default: uni.$u.props.checkbox.checked
        },
        // 是否禁用
        disabled: {
            type: [String, Boolean],
            default: uni.$u.props.checkbox.disabled
        },
        // 选中状态下的颜色，如设置此值，将会覆盖parent的activeColor值
        activeColor: {
            type: String,
            default: uni.$u.props.checkbox.activeColor
        },
        // 未选中的颜色
        inactiveColor: {
            type: String,
            default: uni.$u.props.checkbox.inactiveColor
        },
        // 图标的大小，单位px
        iconSize: {
            type: [String, Number],
            default: uni.$u.props.checkbox.iconSize
        },
        // 图标颜色
        iconColor: {
            type: String,
            default: uni.$u.props.checkbox.iconColor
        },
        // label提示文字，因为nvue下，直接slot进来的文字，由于特殊的结构，无法修改样式
        label: {
            type: [String, Number],
            default: uni.$u.props.checkbox.label
        },
        // label的字体大小，px单位
        labelSize: {
            type: [String, Number],
            default: uni.$u.props.checkbox.labelSize
        },
        // label的颜色
        labelColor: {
            type: String,
            default: uni.$u.props.checkbox.labelColor
        },
        // 是否禁止点击提示语选中复选框
        labelDisabled: {
            type: [String, Boolean],
            default: uni.$u.props.checkbox.labelDisabled
        }
    }
}
