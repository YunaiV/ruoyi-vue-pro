export default {
    props: {
        // 列表锚点文本内容
        text: {
            type: [String, Number],
            default: uni.$u.props.indexAnchor.text
        },
        // 列表锚点文字颜色
        color: {
            type: String,
            default: uni.$u.props.indexAnchor.color
        },
        // 列表锚点文字大小，单位默认px
        size: {
            type: [String, Number],
            default: uni.$u.props.indexAnchor.size
        },
        // 列表锚点背景颜色
        bgColor: {
            type: String,
            default: uni.$u.props.indexAnchor.bgColor
        },
        // 列表锚点高度，单位默认px
        height: {
            type: [String, Number],
            default: uni.$u.props.indexAnchor.height
        }
    }
}
