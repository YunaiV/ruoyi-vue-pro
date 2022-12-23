export default {
    props: {
        // 给col添加间距，左右边距各占一半
        gutter: {
            type: [String, Number],
            default: uni.$u.props.row.gutter
        },
        // 水平排列方式，可选值为`start`(或`flex-start`)、`end`(或`flex-end`)、`center`、`around`(或`space-around`)、`between`(或`space-between`)
        justify: {
            type: String,
            default: uni.$u.props.row.justify
        },
        // 垂直对齐方式，可选值为top、center、bottom
        align: {
            type: String,
            default: uni.$u.props.row.align
        }
    }
}
