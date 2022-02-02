export default {
    props: {
        // input的label提示语
        label: {
            type: String,
            default: uni.$u.props.formItem.label
        },
        // 绑定的值
        prop: {
            type: String,
            default: uni.$u.props.formItem.prop
        },
        // 是否显示表单域的下划线边框
        borderBottom: {
            type: [String, Boolean],
            default: uni.$u.props.formItem.borderBottom
        },
        // label的宽度，单位px
        labelWidth: {
            type: [String, Number],
            default: uni.$u.props.formItem.labelWidth
        },
        // 右侧图标
        rightIcon: {
            type: String,
            default: uni.$u.props.formItem.rightIcon
        },
        // 左侧图标
        leftIcon: {
            type: String,
            default: uni.$u.props.formItem.leftIcon
        },
        // 是否显示左边的必填星号，只作显示用，具体校验必填的逻辑，请在rules中配置
        required: {
            type: Boolean,
            default: uni.$u.props.formItem.required
        }
    }
}
