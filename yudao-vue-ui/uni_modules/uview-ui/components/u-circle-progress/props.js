export default {
    props: {
        percentage: {
            type: [String, Number],
            default: uni.$u.props.circleProgress.percentage
        }
    }
}
