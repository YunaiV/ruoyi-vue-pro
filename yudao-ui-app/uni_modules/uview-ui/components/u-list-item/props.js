export default {
    props: {
        // 用于滚动到指定item
        anchor: {
            type: [String, Number],
            default: uni.$u.props.listItem.anchor
        }
    }
}
