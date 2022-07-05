export default {
    props: {
        color: {
            type: String,
            default: uni.$u.props.line.color
        },
        // 长度，竖向时表现为高度，横向时表现为长度，可以为百分比，带px单位的值等
        length: {
            type: [String, Number],
            default: uni.$u.props.line.length
        },
        // 线条方向，col-竖向，row-横向
        direction: {
            type: String,
            default: uni.$u.props.line.direction
        },
        // 是否显示细边框
        hairline: {
            type: Boolean,
            default: uni.$u.props.line.hairline
        },
        // 线条与上下左右元素的间距，字符串形式，如"30px"、"20px 30px"
        margin: {
            type: [String, Number],
            default: uni.$u.props.line.margin
        },
        // 是否虚线，true-实线，false-虚线
        dashed: {
            type: Boolean,
            default: uni.$u.props.line.dashed
        }
    }
}
