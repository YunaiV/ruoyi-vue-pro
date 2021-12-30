export default {
    props: {
        // 是否展示picker弹窗
        show: {
            type: Boolean,
            default: uni.$u.props.picker.show
        },
        // 是否展示顶部的操作栏
        showToolbar: {
            type: Boolean,
            default: uni.$u.props.picker.showToolbar
        },
        // 顶部标题
        title: {
            type: String,
            default: uni.$u.props.picker.title
        },
        // 对象数组，设置每一列的数据
        columns: {
            type: Array,
            default: uni.$u.props.picker.columns
        },
        // 是否显示加载中状态
        loading: {
            type: Boolean,
            default: uni.$u.props.picker.loading
        },
        // 各列中，单个选项的高度
        itemHeight: {
            type: [String, Number],
            default: uni.$u.props.picker.itemHeight
        },
        // 取消按钮的文字
        cancelText: {
            type: String,
            default: uni.$u.props.picker.cancelText
        },
        // 确认按钮的文字
        confirmText: {
            type: String,
            default: uni.$u.props.picker.confirmText
        },
        // 取消按钮的颜色
        cancelColor: {
            type: String,
            default: uni.$u.props.picker.cancelColor
        },
        // 确认按钮的颜色
        confirmColor: {
            type: String,
            default: uni.$u.props.picker.confirmColor
        },
        // 选择器只有一列时，默认选中项的索引，从0开始
        singleIndex: {
            type: [String, Number],
            default: uni.$u.props.picker.singleIndex
        },
        // 每列中可见选项的数量
        visibleItemCount: {
            type: [String, Number],
            default: uni.$u.props.picker.visibleItemCount
        },
        // 选项对象中，需要展示的属性键名
        keyName: {
            type: String,
            default: uni.$u.props.picker.keyName
        },
        // 是否允许点击遮罩关闭选择器
        closeOnClickOverlay: {
            type: Boolean,
            default: uni.$u.props.picker.closeOnClickOverlay
        },
        // 各列的默认索引
        defaultIndex: {
            type: Array,
            default: uni.$u.props.picker.defaultIndex
        }
    }
}
