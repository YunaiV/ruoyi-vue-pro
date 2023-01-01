export default {
    methods: {
        // 关闭时执行
        closeHandler() {
            this.status = 'close'
        },
        setState(status) {
            this.status = status
        },
        closeOther() {
            // 尝试关闭其他打开的单元格
            this.parent && this.parent.closeOther(this)
        }
    }
}
