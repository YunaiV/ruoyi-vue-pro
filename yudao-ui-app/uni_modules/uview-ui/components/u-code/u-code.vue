<template>
	<view class="u-code">
		<!-- 此组件功能由js完成，无需写html逻辑 -->
	</view>
</template>

<script>
	import props from './props.js';
	/**
	 * Code 验证码输入框
	 * @description 考虑到用户实际发送验证码的场景，可能是一个按钮，也可能是一段文字，提示语各有不同，所以本组件 不提供界面显示，只提供提示语，由用户将提示语嵌入到具体的场景
	 * @tutorial https://www.uviewui.com/components/code.html
	 * @property {String | Number}	seconds			倒计时所需的秒数（默认 60 ）
	 * @property {String}			startText		开始前的提示语，见官网说明（默认 '获取验证码' ）
	 * @property {String}			changeText		倒计时期间的提示语，必须带有字母"x"，见官网说明（默认 'X秒重新获取' ）
	 * @property {String}			endText			倒计结束的提示语，见官网说明（默认 '重新获取' ）
	 * @property {Boolean}			keepRunning		是否在H5刷新或各端返回再进入时继续倒计时（ 默认false ）
	 * @property {String}			uniqueKey		为了区分多个页面，或者一个页面多个倒计时组件本地存储的继续倒计时变了
	 * 
	 * @event {Function}	change	倒计时期间，每秒触发一次
	 * @event {Function}	start	开始倒计时触发
	 * @event {Function}	end		结束倒计时触发
	 * @example <u-code ref="uCode" @change="codeChange" seconds="20"></u-code> 
	 */
	export default {
		name: "u-code",
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		data() {
			return {
				secNum: this.seconds,
				timer: null,
				canGetCode: true, // 是否可以执行验证码操作
			}
		},
		mounted() {
			this.checkKeepRunning()
		},
		watch: {
			seconds: {
				immediate: true,
				handler(n) {
					this.secNum = n
				}
			}
		},
		methods: {
			checkKeepRunning() {
				// 获取上一次退出页面(H5还包括刷新)时的时间戳，如果没有上次的保存，此值可能为空
				let lastTimestamp = Number(uni.getStorageSync(this.uniqueKey + '_$uCountDownTimestamp'))
				if(!lastTimestamp) return this.changeEvent(this.startText)
				// 当前秒的时间戳
				let nowTimestamp = Math.floor((+ new Date()) / 1000)
				// 判断当前的时间戳，是否小于上一次的本该按设定结束，却提前结束的时间戳
				if(this.keepRunning && lastTimestamp && lastTimestamp > nowTimestamp) {
					// 剩余尚未执行完的倒计秒数
					this.secNum = lastTimestamp - nowTimestamp
					// 清除本地保存的变量
					uni.removeStorageSync(this.uniqueKey + '_$uCountDownTimestamp')
					// 开始倒计时
					this.start()
				} else {
					// 如果不存在需要继续上一次的倒计时，执行正常的逻辑
					this.changeEvent(this.startText)
				}
			},
			// 开始倒计时
			start() {
				// 防止快速点击获取验证码的按钮而导致内部产生多个定时器导致混乱
				if(this.timer) {
					clearInterval(this.timer)
					this.timer = null
				}
				this.$emit('start')
				this.canGetCode = false
				// 这里放这句，是为了一开始时就提示，否则要等setInterval的1秒后才会有提示
				this.changeEvent(this.changeText.replace(/x|X/, this.secNum))
				this.setTimeToStorage()
				this.timer = setInterval(() => {
					if (--this.secNum) {
						// 用当前倒计时的秒数替换提示字符串中的"x"字母
						this.changeEvent(this.changeText.replace(/x|X/, this.secNum))
					} else {
						clearInterval(this.timer)
						this.timer = null
						this.changeEvent(this.endText)
						this.secNum = this.seconds
						this.$emit('end')
						this.canGetCode = true
					}
				}, 1000)
			},
			// 重置，可以让用户再次获取验证码
			reset() {
				this.canGetCode = true
				clearInterval(this.timer)
				this.secNum = this.seconds
				this.changeEvent(this.endText)
			},
			changeEvent(text) {
				this.$emit('change', text)
			},
			// 保存时间戳，为了防止倒计时尚未结束，H5刷新或者各端的右上角返回上一页再进来
			setTimeToStorage() {
				if(!this.keepRunning || !this.timer) return
				// 记录当前的时间戳，为了下次进入页面，如果还在倒计时内的话，继续倒计时
				// 倒计时尚未结束，结果大于0；倒计时已经开始，就会小于初始值，如果等于初始值，说明没有开始倒计时，无需处理
				if(this.secNum > 0 && this.secNum <= this.seconds) {
					// 获取当前时间戳(+ new Date()为特殊写法)，除以1000变成秒，再去除小数部分
					let nowTimestamp = Math.floor((+ new Date()) / 1000)
					// 将本该结束时候的时间戳保存起来 => 当前时间戳 + 剩余的秒数
					uni.setStorage({
						key: this.uniqueKey + '_$uCountDownTimestamp',
						data: nowTimestamp + Number(this.secNum)
					})
				}
			}
		},
		// 组件销毁的时候，清除定时器，否则定时器会继续存在，系统不会自动清除
		beforeDestroy() {
			this.setTimeToStorage()
			clearTimeout(this.timer)
			this.timer = null
		}
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";
</style>
