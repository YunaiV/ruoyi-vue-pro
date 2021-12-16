export default {
    props: {
        // 是否展示modal
        show: {
            type: Boolean,
            default: uni.$u.props.modal.show
        },
        // 标题
        title: {
            type: [String],
            default: uni.$u.props.modal.title
        },
        // 弹窗内容
        content: {
            type: String,
            default: uni.$u.props.modal.content
        },
        // 确认文案
        confirmText: {
            type: String,
            default: uni.$u.props.modal.confirmText
        },
        // 取消文案
        cancelText: {
            type: String,
            default: uni.$u.props.modal.cancelText
        },
        // 是否显示确认按钮
        showConfirmButton: {
            type: Boolean,
            default: uni.$u.props.modal.showConfirmButton
        },
        // 是否显示取消按钮
        showCancelButton: {
            type: Boolean,
            default: uni.$u.props.modal.showCancelButton
        },
        // 确认按钮颜色
        confirmColor: {
            type: String,
            default: uni.$u.props.modal.confirmColor
        },
        // 取消文字颜色
        cancelColor: {
            type: String,
            default: uni.$u.props.modal.cancelColor
        },
        // 对调确认和取消的位置
        buttonReverse: {
            type: Boolean,
            default: uni.$u.props.modal.buttonReverse
        },
        // 是否开启缩放效果
        zoom: {
            type: Boolean,
            default: uni.$u.props.modal.zoom
        },
        // 是否异步关闭，只对确定按钮有效
        asyncClose: {
            type: Boolean,
            default: uni.$u.props.modal.asyncClose
        },
        // 是否允许点击遮罩关闭modal
        closeOnClickOverlay: {
            type: Boolean,
            default: uni.$u.props.modal.closeOnClickOverlay
        },
        // 给一个负的margin-top，往上偏移，避免和键盘重合的情况
        negativeTop: {
            type: [String, Number],
            default: uni.$u.props.modal.negativeTop
        },
        // modal宽度，不支持百分比，可以数值，px，rpx单位
        width: {
            type: [String, Number],
            default: uni.$u.props.modal.width
        },
        // 确认按钮的样式，circle-圆形，square-方形，如设置，将不会显示取消按钮
        confirmButtonShape: {
            type: String,
            default: uni.$u.props.modal.confirmButtonShape
        }
    }
}
