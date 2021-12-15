const MIN_DISTANCE = 10

function getDirection(x, y) {
    if (x > y && x > MIN_DISTANCE) {
        return 'horizontal'
    }
    if (y > x && y > MIN_DISTANCE) {
        return 'vertical'
    }
    return ''
}

export default {
    methods: {
        getTouchPoint(e) {
            if (!e) {
                return {
                    x: 0,
                    y: 0
                }
            } if (e.touches && e.touches[0]) {
                return {
                    x: e.touches[0].pageX,
                    y: e.touches[0].pageY
                }
            } if (e.changedTouches && e.changedTouches[0]) {
                return {
                    x: e.changedTouches[0].pageX,
                    y: e.changedTouches[0].pageY
                }
            }
            return {
                x: e.clientX || 0,
                y: e.clientY || 0
            }
        },
        resetTouchStatus() {
            this.direction = ''
            this.deltaX = 0
            this.deltaY = 0
            this.offsetX = 0
            this.offsetY = 0
        },
        touchStart(event) {
            this.resetTouchStatus()
            const touch = this.getTouchPoint(event)
            this.startX = touch.x
            this.startY = touch.y
        },
        touchMove(event) {
            const touch = this.getTouchPoint(event)
            this.deltaX = touch.x - this.startX
            this.deltaY = touch.y - this.startY
            this.offsetX = Math.abs(this.deltaX)
            this.offsetY = Math.abs(this.deltaY)
            this.direction =				this.direction || getDirection(this.offsetX, this.offsetY)
        }
    }
}
