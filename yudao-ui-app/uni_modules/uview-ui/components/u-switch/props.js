export default {
    props: {
        // 是否为加载中状态
        loading: {
            type: Boolean,
            default: uni.$u.props.switch.loading
        },
        // 是否为禁用装填
        disabled: {
            type: Boolean,
            default: uni.$u.props.switch.disabled
        },
        // 开关尺寸，单位px
        size: {
            type: [String, Number],
            default: uni.$u.props.switch.size
        },
        // 打开时的背景颜色
        activeColor: {
            type: String,
            default: uni.$u.props.switch.activeColor
        },
        // 关闭时的背景颜色
        inactiveColor: {
            type: String,
            default: uni.$u.props.switch.inactiveColor
        },
        // 通过v-model双向绑定的值
        value: {
            type: [Boolean, String, Number],
            default: uni.$u.props.switch.value
        },
        // switch打开时的值
        activeValue: {
            type: [String, Number, Boolean],
            default: uni.$u.props.switch.activeValue
        },
        // switch关闭时的值
        inactiveValue: {
            type: [String, Number, Boolean],
            default: uni.$u.props.switch.inactiveValue
        },
        // 是否开启异步变更，开启后需要手动控制输入值
        asyncChange: {
            type: Boolean,
            default: uni.$u.props.switch.asyncChange
        },
        // 圆点与外边框的距离
        space: {
            type: [String, Number],
            default: uni.$u.props.switch.space
        }
    }
}
