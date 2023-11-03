let mpMixins = {}
let is_pc = null
// #ifdef H5
import {
	isPC
} from "./isPC"
is_pc = isPC()
// #endif
// #ifdef APP-VUE|| MP-WEIXIN || H5

mpMixins = {
	data() {
		return {
			is_show: 'none'
		}
	},
	watch: {
		show(newVal) {
			this.is_show = this.show
		}
	},
	created() {
		this.swipeaction = this.getSwipeAction()
		if (this.swipeaction.children !== undefined) {
			this.swipeaction.children.push(this)
		}
	},
	mounted() {
		this.is_show = this.show
	},
	methods: {
		// wxs 中调用
		closeSwipe(e) {
			if (!this.autoClose) return
			this.swipeaction.closeOther(this)
		},

		change(e) {
			this.$emit('change', e.open)
			if (this.is_show !== e.open) {
				this.is_show = e.open
			}
		},

		appTouchStart(e) {
			if (is_pc) return
			const {
				clientX
			} = e.changedTouches[0]
			this.clientX = clientX
			this.timestamp = new Date().getTime()
		},
		appTouchEnd(e, index, item, position) {
			if (is_pc) return
			const {
				clientX
			} = e.changedTouches[0]
			// fixed by xxxx 模拟点击事件，解决 ios 13 点击区域错位的问题
			let diff = Math.abs(this.clientX - clientX)
			let time = (new Date().getTime()) - this.timestamp
			if (diff < 40 && time < 300) {
				this.$emit('click', {
					content: item,
					index,
					position
				})
			}
		},
		onClickForPC(index, item, position) {
			if (!is_pc) return
			// #ifdef H5
			this.$emit('click', {
				content: item,
				index,
				position
			})
			// #endif
		}
	}
}

// #endif
export default mpMixins
