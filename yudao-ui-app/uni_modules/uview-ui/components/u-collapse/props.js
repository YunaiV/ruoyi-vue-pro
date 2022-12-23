export default {
    props: {
        // 当前展开面板的name，非手风琴模式：[<string | number>]，手风琴模式：string | number
        value: {
            type: [String, Number, Array, null],
            default: uni.$u.props.collapse.value
        },
        // 是否手风琴模式
        accordion: {
            type: Boolean,
            default: uni.$u.props.collapse.accordion
        },
        // 是否显示外边框
        border: {
            type: Boolean,
            default: uni.$u.props.collapse.border
        }
    }
}
