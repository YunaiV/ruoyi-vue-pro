export default {
    props: {
        // 是否打开组件
        show: {
            type: Boolean,
            default: uni.$u.props.datetimePicker.show
        },
        // 是否展示顶部的操作栏
        showToolbar: {
            type: Boolean,
            default: uni.$u.props.datetimePicker.showToolbar
        },
        // 绑定值
        value: {
            type: [String, Number],
            default: uni.$u.props.datetimePicker.value
        },
        // 顶部标题
        title: {
            type: String,
            default: uni.$u.props.datetimePicker.title
        },
        // 展示格式，mode=date为日期选择，mode=time为时间选择，mode=year-month为年月选择，mode=datetime为日期时间选择
        mode: {
            type: String,
            default: uni.$u.props.datetimePicker.mode
        },
        // 可选的最大时间
        maxDate: {
            type: Number,
            // 最大默认值为后10年
            default: uni.$u.props.datetimePicker.maxDate
        },
        // 可选的最小时间
        minDate: {
            type: Number,
            // 最小默认值为前10年
            default: uni.$u.props.datetimePicker.minDate
        },
        // 可选的最小小时，仅mode=time有效
        minHour: {
            type: Number,
            default: uni.$u.props.datetimePicker.minHour
        },
        // 可选的最大小时，仅mode=time有效
        maxHour: {
            type: Number,
            default: uni.$u.props.datetimePicker.maxHour
        },
        // 可选的最小分钟，仅mode=time有效
        minMinute: {
            type: Number,
            default: uni.$u.props.datetimePicker.minMinute
        },
        // 可选的最大分钟，仅mode=time有效
        maxMinute: {
            type: Number,
            default: uni.$u.props.datetimePicker.maxMinute
        },
        // 选项过滤函数
        filter: {
            type: [Function, null],
            default: uni.$u.props.datetimePicker.filter
        },
        // 选项格式化函数
        formatter: {
            type: [Function, null],
            default: uni.$u.props.datetimePicker.formatter
        },
        // 是否显示加载中状态
        loading: {
            type: Boolean,
            default: uni.$u.props.datetimePicker.loading
        },
        // 各列中，单个选项的高度
        itemHeight: {
            type: [String, Number],
            default: uni.$u.props.datetimePicker.itemHeight
        },
        // 取消按钮的文字
        cancelText: {
            type: String,
            default: uni.$u.props.datetimePicker.cancelText
        },
        // 确认按钮的文字
        confirmText: {
            type: String,
            default: uni.$u.props.datetimePicker.confirmText
        },
        // 取消按钮的颜色
        cancelColor: {
            type: String,
            default: uni.$u.props.datetimePicker.cancelColor
        },
        // 确认按钮的颜色
        confirmColor: {
            type: String,
            default: uni.$u.props.datetimePicker.confirmColor
        },
        // 每列中可见选项的数量
        visibleItemCount: {
            type: [String, Number],
            default: uni.$u.props.datetimePicker.visibleItemCount
        },
        // 是否允许点击遮罩关闭选择器
        closeOnClickOverlay: {
            type: Boolean,
            default: uni.$u.props.datetimePicker.closeOnClickOverlay
        },
        // 各列的默认索引
        defaultIndex: {
            type: Array,
            default: uni.$u.props.datetimePicker.defaultIndex
        }
    }
}
