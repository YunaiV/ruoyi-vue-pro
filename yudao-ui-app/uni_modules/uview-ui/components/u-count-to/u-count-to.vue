<template>
	<text
		class="u-count-num"
		:style="{
			fontSize: $u.addUnit(fontSize),
			fontWeight: bold ? 'bold' : 'normal',
			color: color
		}"
	>{{ displayValue }}</text>
</template>

<script>
	import props from './props.js';
/**
 * countTo 数字滚动
 * @description 该组件一般用于需要滚动数字到某一个值的场景，目标要求是一个递增的值。
 * @tutorial https://www.uviewui.com/components/countTo.html
 * @property {String | Number}	startVal	开始的数值，默认从0增长到某一个数（默认 0 ）
 * @property {String | Number}	endVal		要滚动的目标数值，必须 （默认 0 ）
 * @property {String | Number}	duration	滚动到目标数值的动画持续时间，单位为毫秒（ms） （默认 2000 ）
 * @property {Boolean}			autoplay	设置数值后是否自动开始滚动 （默认 true ）
 * @property {String | Number}	decimals	要显示的小数位数，见官网说明（默认 0 ）
 * @property {Boolean}			useEasing	滚动结束时，是否缓动结尾，见官网说明（默认 true ）
 * @property {String}			decimal		十进制分割 （ 默认 "." ）
 * @property {String}			color		字体颜色（ 默认 '#606266' )
 * @property {String | Number}	fontSize	字体大小，单位px（ 默认 22 ）
 * @property {Boolean}			bold		字体是否加粗（默认 false ）
 * @property {String}			separator	千位分隔符，见官网说明
 * @event {Function} end 数值滚动到目标值时触发
 * @example <u-count-to ref="uCountTo" :end-val="endVal" :autoplay="autoplay"></u-count-to>
 */
export default {
	name: 'u-count-to',
	data() {
		return {
			localStartVal: this.startVal,
			displayValue: this.formatNumber(this.startVal),
			printVal: null,
			paused: false, // 是否暂停
			localDuration: Number(this.duration),
			startTime: null, // 开始的时间
			timestamp: null, // 时间戳
			remaining: null, // 停留的时间
			rAF: null,
			lastTime: 0 // 上一次的时间
		};
	},
	mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
	computed: {
		countDown() {
			return this.startVal > this.endVal;
		}
	},
	watch: {
		startVal() {
			this.autoplay && this.start();
		},
		endVal() {
			this.autoplay && this.start();
		}
	},
	mounted() {
		this.autoplay && this.start();
	},
	methods: {
		easingFn(t, b, c, d) {
			return (c * (-Math.pow(2, (-10 * t) / d) + 1) * 1024) / 1023 + b;
		},
		requestAnimationFrame(callback) {
			const currTime = new Date().getTime();
			// 为了使setTimteout的尽可能的接近每秒60帧的效果
			const timeToCall = Math.max(0, 16 - (currTime - this.lastTime));
			const id = setTimeout(() => {
				callback(currTime + timeToCall);
			}, timeToCall);
			this.lastTime = currTime + timeToCall;
			return id;
		},
		cancelAnimationFrame(id) {
			clearTimeout(id);
		},
		// 开始滚动数字
		start() {
			this.localStartVal = this.startVal;
			this.startTime = null;
			this.localDuration = this.duration;
			this.paused = false;
			this.rAF = this.requestAnimationFrame(this.count);
		},
		// 暂定状态，重新再开始滚动；或者滚动状态下，暂停
		reStart() {
			if (this.paused) {
				this.resume();
				this.paused = false;
			} else {
				this.stop();
				this.paused = true;
			}
		},
		// 暂停
		stop() {
			this.cancelAnimationFrame(this.rAF);
		},
		// 重新开始(暂停的情况下)
		resume() {
			if (!this.remaining) return
			this.startTime = 0;
			this.localDuration = this.remaining;
			this.localStartVal = this.printVal;
			this.requestAnimationFrame(this.count);
		},
		// 重置
		reset() {
			this.startTime = null;
			this.cancelAnimationFrame(this.rAF);
			this.displayValue = this.formatNumber(this.startVal);
		},
		count(timestamp) {
			if (!this.startTime) this.startTime = timestamp;
			this.timestamp = timestamp;
			const progress = timestamp - this.startTime;
			this.remaining = this.localDuration - progress;
			if (this.useEasing) {
				if (this.countDown) {
					this.printVal = this.localStartVal - this.easingFn(progress, 0, this.localStartVal - this.endVal, this.localDuration);
				} else {
					this.printVal = this.easingFn(progress, this.localStartVal, this.endVal - this.localStartVal, this.localDuration);
				}
			} else {
				if (this.countDown) {
					this.printVal = this.localStartVal - (this.localStartVal - this.endVal) * (progress / this.localDuration);
				} else {
					this.printVal = this.localStartVal + (this.endVal - this.localStartVal) * (progress / this.localDuration);
				}
			}
			if (this.countDown) {
				this.printVal = this.printVal < this.endVal ? this.endVal : this.printVal;
			} else {
				this.printVal = this.printVal > this.endVal ? this.endVal : this.printVal;
			}
			this.displayValue = this.formatNumber(this.printVal) || 0;
			if (progress < this.localDuration) {
				this.rAF = this.requestAnimationFrame(this.count);
			} else {
				this.$emit('end');
			}
		},
		// 判断是否数字
		isNumber(val) {
			return !isNaN(parseFloat(val));
		},
		formatNumber(num) {
			// 将num转为Number类型，因为其值可能为字符串数值，调用toFixed会报错
			num = Number(num);
			num = num.toFixed(Number(this.decimals));
			num += '';
			const x = num.split('.');
			let x1 = x[0];
			const x2 = x.length > 1 ? this.decimal + x[1] : '';
			const rgx = /(\d+)(\d{3})/;
			if (this.separator && !this.isNumber(this.separator)) {
				while (rgx.test(x1)) {
					x1 = x1.replace(rgx, '$1' + this.separator + '$2');
				}
			}
			return x1 + x2;
		},
		destroyed() {
			this.cancelAnimationFrame(this.rAF);
		}
	}
};
</script>

<style lang="scss" scoped>
@import "../../libs/css/components.scss";

.u-count-num {
	/* #ifndef APP-NVUE */
	display: inline-flex;
	/* #endif */
	text-align: center;
}
</style>
