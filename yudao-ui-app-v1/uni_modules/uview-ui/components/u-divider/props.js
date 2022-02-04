export default {
    props: {
        // 是否虚线
        dashed: {
            type: Boolean,
            default: uni.$u.props.divider.dashed
        },
        // 是否细线
        hairline: {
            type: Boolean,
            default: uni.$u.props.divider.hairline
        },
        // 是否以点替代文字，优先于text字段起作用
        dot: {
            type: Boolean,
            default: uni.$u.props.divider.dot
        },
        // 内容文本的位置，left-左边，center-中间，right-右边
        textPosition: {
            type: String,
            default: uni.$u.props.divider.textPosition
        },
        // 文本内容
        text: {
            type: [String, Number],
            default: uni.$u.props.divider.text
        },
        // 文本大小
        textSize: {
            type: [String, Number],
            default: uni.$u.props.divider.textSize
        },
        // 文本颜色
        textColor: {
            type: String,
            default: uni.$u.props.divider.textColor
        },
        // 线条颜色
        lineColor: {
            type: String,
            default: uni.$u.props.divider.lineColor
        }
    }
}
