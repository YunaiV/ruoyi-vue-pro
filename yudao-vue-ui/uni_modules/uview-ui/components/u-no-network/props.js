export default {
    props: {
        // 页面文字提示
        tips: {
            type: String,
            default: uni.$u.props.noNetwork.tips
        },
        // 一个z-index值，用于设置没有网络这个组件的层次，因为页面可能会有其他定位的元素层级过高，导致此组件被覆盖
        zIndex: {
            type: [String, Number],
            default: uni.$u.props.noNetwork.zIndex
        },
        // image 没有网络的图片提示
        image: {
            type: String,
            default: uni.$u.props.noNetwork.image
        }
    }
}
