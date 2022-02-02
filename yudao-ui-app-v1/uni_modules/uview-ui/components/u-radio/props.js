export default {
    props: {
        // radio的名称
        name: {
            type: [String, Number, Boolean],
            default: uni.$u.props.radio.name
        },
        // 形状，square为方形，circle为圆型
        shape: {
            type: String,
            default: uni.$u.props.radio.shape
        },
        // 是否禁用
        disabled: {
            type: [String, Boolean],
            default: uni.$u.props.radio.disabled
        },
        // 是否禁止点击提示语选中单选框
        labelDisabled: {
            type: [String, Boolean],
            default: uni.$u.props.radio.labelDisabled
        },
        // 选中状态下的颜色，如设置此值，将会覆盖parent的activeColor值
        activeColor: {
            type: String,
            default: uni.$u.props.radio.activeColor
        },
        // 未选中的颜色
        inactiveColor: {
            type: String,
            default: uni.$u.props.radio.inactiveColor
        },
        // 图标的大小，单位px
        iconSize: {
            type: [String, Number],
            default: uni.$u.props.radio.iconSize
        },
        // label的字体大小，px单位
        labelSize: {
            type: [String, Number],
            default: uni.$u.props.radio.labelSize
        },
        // label提示文字，因为nvue下，直接slot进来的文字，由于特殊的结构，无法修改样式
        label: {
            type: [String, Number],
            default: uni.$u.props.radio.label
        },
        // 整体的大小
        size: {
            type: [String, Number],
            default: uni.$u.props.radio.size
        },
        // 图标颜色
        color: {
            type: String,
            default: uni.$u.props.radio.color
        },
        // label的颜色
        labelColor: {
            type: String,
            default: uni.$u.props.radio.labelColor
        }
    }
}
