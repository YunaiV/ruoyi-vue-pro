/**
 * @desc 函数防抖
 * @param func 目标函数
 * @param wait 延迟执行毫秒数
 * @param immediate true - 立即执行， false - 延迟执行
 */
export const debounce = function(func, wait = 1000, immediate = true) {
	let timer;
	console.log(1);
	return function() {
		console.log(123);
		let context = this,
			args = arguments;
		if (timer) clearTimeout(timer);
		if (immediate) {
			let callNow = !timer;
			timer = setTimeout(() => {
				timer = null;
			}, wait);
			if (callNow) func.apply(context, args);
		} else {
			timer = setTimeout(() => {
				func.apply(context, args);
			}, wait)
		}
	}
}
/**
 * @desc 函数节流
 * @param func 函数
 * @param wait 延迟执行毫秒数
 * @param type 1 使用表时间戳，在时间段开始的时候触发 2 使用表定时器，在时间段结束的时候触发
 */
export const throttle = (func, wait = 1000, type = 1) => {
	let previous = 0;
	let timeout;
	return function() {
		let context = this;
		let args = arguments;
		if (type === 1) {
			let now = Date.now();

			if (now - previous > wait) {
				func.apply(context, args);
				previous = now;
			}
		} else if (type === 2) {
			if (!timeout) {
				timeout = setTimeout(() => {
					timeout = null;
					func.apply(context, args)
				}, wait)
			}
		}
	}
}
