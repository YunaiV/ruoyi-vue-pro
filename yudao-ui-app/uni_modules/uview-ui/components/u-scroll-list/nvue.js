// 引入bindingx，此库类似于微信小程序wxs，目的是让js运行在视图层，减少视图层和逻辑层的通信折损
const BindingX = uni.requireNativePlugin('bindingx')

export default {
    methods: {
        // 此处不写注释，请自行体会
        nvueScrollHandler(e) {
            const anchor = this.$refs['u-scroll-list__scroll-view'].ref
            const element = this.$refs['u-scroll-list__indicator__line__bar'].ref
            const scrollLeft = e.contentOffset.x
            const contentSize = e.contentSize.width
            const { scrollWidth } = this
            const barAllMoveWidth = this.indicatorWidth - this.indicatorBarWidth
            // 在安卓和iOS上，需要除的倍数不一样，iOS需要除以2
            const actionNum = uni.$u.os() === 'ios' ? 2 : 1
            const expression = `(x / ${actionNum}) / ${contentSize - scrollWidth} * ${barAllMoveWidth}`
            BindingX.bind({
                anchor,
                eventType: 'scroll',
                props: [{
                    element,
                    property: 'transform.translateX',
                    expression
                }]
            })
        }
    }
}
