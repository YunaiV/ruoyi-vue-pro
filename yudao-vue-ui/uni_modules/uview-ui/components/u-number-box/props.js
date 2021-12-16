export default {
    props: {
        // 步进器标识符，在change回调返回
        name: {
            type: [String, Number],
            default: uni.$u.props.numberBox.name
        },
        // 用于双向绑定的值，初始化时设置设为默认min值(最小值)
        value: {
            type: [String, Number],
            default: uni.$u.props.numberBox.value
        },
        // 最小值
        min: {
            type: [String, Number],
            default: uni.$u.props.numberBox.min
        },
        // 最大值
        max: {
            type: [String, Number],
            default: uni.$u.props.numberBox.max
        },
        // 加减的步长，可为小数
        step: {
            type: [String, Number],
            default: uni.$u.props.numberBox.step
        },
        // 是否只允许输入整数
        integer: {
            type: Boolean,
            default: uni.$u.props.numberBox.integer
        },
        // 是否禁用，包括输入框，加减按钮
        disabled: {
            type: Boolean,
            default: uni.$u.props.numberBox.disabled
        },
        // 是否禁用输入框
        disabledInput: {
            type: Boolean,
            default: uni.$u.props.numberBox.disabledInput
        },
        // 是否开启异步变更，开启后需要手动控制输入值
        asyncChange: {
            type: Boolean,
            default: uni.$u.props.numberBox.asyncChange
        },
        // 输入框宽度，单位为px
        inputWidth: {
            type: [String, Number],
            default: uni.$u.props.numberBox.inputWidth
        },
        // 是否显示减少按钮
        showMinus: {
            type: Boolean,
            default: uni.$u.props.numberBox.showMinus
        },
        // 是否显示增加按钮
        showPlus: {
            type: Boolean,
            default: uni.$u.props.numberBox.showPlus
        },
        // 显示的小数位数
        decimalLength: {
            type: [String, Number, null],
            default: uni.$u.props.numberBox.decimalLength
        },
        // 是否开启长按加减手势
        longPress: {
            type: Boolean,
            default: uni.$u.props.numberBox.longPress
        },
        // 输入框文字和加减按钮图标的颜色
        color: {
            type: String,
            default: uni.$u.props.numberBox.color
        },
        // 按钮大小，宽高等于此值，单位px，输入框高度和此值保持一致
        buttonSize: {
            type: [String, Number],
            default: uni.$u.props.numberBox.buttonSize
        },
        // 输入框和按钮的背景颜色
        bgColor: {
            type: String,
            default: uni.$u.props.numberBox.bgColor
        },
        // 指定光标于键盘的距离，避免键盘遮挡输入框，单位px
        cursorSpacing: {
            type: [String, Number],
            default: uni.$u.props.numberBox.cursorSpacing
        },
        // 是否禁用增加按钮
        disablePlus: {
            type: Boolean,
            default: uni.$u.props.numberBox.disablePlus
        },
        // 是否禁用减少按钮
        disableMinus: {
            type: Boolean,
            default: uni.$u.props.numberBox.disableMinus
        },
        // 加减按钮图标的样式
        iconStyle: {
            type: [Object, String],
            default: uni.$u.props.numberBox.iconStyle
        }
    }
}
