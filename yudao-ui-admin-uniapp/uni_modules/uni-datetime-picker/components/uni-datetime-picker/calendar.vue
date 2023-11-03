<template>
	<view class="uni-calendar" @mouseleave="leaveCale">
		<view v-if="!insert&&show" class="uni-calendar__mask" :class="{'uni-calendar--mask-show':aniMaskShow}"
			@click="clean"></view>
		<view v-if="insert || show" class="uni-calendar__content"
			:class="{'uni-calendar--fixed':!insert,'uni-calendar--ani-show':aniMaskShow, 'uni-calendar__content-mobile': aniMaskShow}">
			<view class="uni-calendar__header" :class="{'uni-calendar__header-mobile' :!insert}">
				<view v-if="left" class="uni-calendar__header-btn-box" @click.stop="pre">
					<view class="uni-calendar__header-btn uni-calendar--left"></view>
				</view>
				<picker mode="date" :value="date" fields="month" @change="bindDateChange">
					<text
						class="uni-calendar__header-text">{{ (nowDate.year||'') + yearText + ( nowDate.month||'') + monthText}}</text>
				</picker>
				<view v-if="right" class="uni-calendar__header-btn-box" @click.stop="next">
					<view class="uni-calendar__header-btn uni-calendar--right"></view>
				</view>
				<view v-if="!insert" class="dialog-close" @click="clean">
					<view class="dialog-close-plus" data-id="close"></view>
					<view class="dialog-close-plus dialog-close-rotate" data-id="close"></view>
				</view>

				<!-- <text class="uni-calendar__backtoday" @click="backtoday">回到今天</text> -->
			</view>
			<view class="uni-calendar__box">
				<view v-if="showMonth" class="uni-calendar__box-bg">
					<text class="uni-calendar__box-bg-text">{{nowDate.month}}</text>
				</view>
				<view class="uni-calendar__weeks" style="padding-bottom: 7px;">
					<view class="uni-calendar__weeks-day">
						<text class="uni-calendar__weeks-day-text">{{SUNText}}</text>
					</view>
					<view class="uni-calendar__weeks-day">
						<text class="uni-calendar__weeks-day-text">{{MONText}}</text>
					</view>
					<view class="uni-calendar__weeks-day">
						<text class="uni-calendar__weeks-day-text">{{TUEText}}</text>
					</view>
					<view class="uni-calendar__weeks-day">
						<text class="uni-calendar__weeks-day-text">{{WEDText}}</text>
					</view>
					<view class="uni-calendar__weeks-day">
						<text class="uni-calendar__weeks-day-text">{{THUText}}</text>
					</view>
					<view class="uni-calendar__weeks-day">
						<text class="uni-calendar__weeks-day-text">{{FRIText}}</text>
					</view>
					<view class="uni-calendar__weeks-day">
						<text class="uni-calendar__weeks-day-text">{{SATText}}</text>
					</view>
				</view>
				<view class="uni-calendar__weeks" v-for="(item,weekIndex) in weeks" :key="weekIndex">
					<view class="uni-calendar__weeks-item" v-for="(weeks,weeksIndex) in item" :key="weeksIndex">
						<calendar-item class="uni-calendar-item--hook" :weeks="weeks" :calendar="calendar"
							:selected="selected" :lunar="lunar" :checkHover="range" @change="choiceDate"
							@handleMouse="handleMouse">
						</calendar-item>
					</view>
				</view>
			</view>
			<view v-if="!insert && !range && typeHasTime" class="uni-date-changed uni-calendar--fixed-top"
				style="padding: 0 80px;">
				<view class="uni-date-changed--time-date">{{tempSingleDate ? tempSingleDate : selectDateText}}</view>
				<time-picker type="time" :start="reactStartTime" :end="reactEndTime" v-model="time"
					:disabled="!tempSingleDate" :border="false" :hide-second="hideSecond" class="time-picker-style">
				</time-picker>
			</view>

			<view v-if="!insert && range && typeHasTime" class="uni-date-changed uni-calendar--fixed-top">
				<view class="uni-date-changed--time-start">
					<view class="uni-date-changed--time-date">{{tempRange.before ? tempRange.before : startDateText}}
					</view>
					<time-picker type="time" :start="reactStartTime" v-model="timeRange.startTime" :border="false"
						:hide-second="hideSecond" :disabled="!tempRange.before" class="time-picker-style">
					</time-picker>
				</view>
				<uni-icons type="arrowthinright" color="#999" style="line-height: 50px;"></uni-icons>
				<view class="uni-date-changed--time-end">
					<view class="uni-date-changed--time-date">{{tempRange.after ? tempRange.after : endDateText}}</view>
					<time-picker type="time" :end="reactEndTime" v-model="timeRange.endTime" :border="false"
						:hide-second="hideSecond" :disabled="!tempRange.after" class="time-picker-style">
					</time-picker>
				</view>
			</view>
			<view v-if="!insert" class="uni-date-changed uni-date-btn--ok">
				<!-- <view class="uni-calendar__header-btn-box">
					<text class="uni-calendar__button-text uni-calendar--fixed-width">{{okText}}</text>
				</view> -->
				<view class="uni-datetime-picker--btn" @click="confirm">{{confirmText}}</view>
			</view>
		</view>
	</view>
</template>

<script>
	import Calendar from './util.js';
	import calendarItem from './calendar-item.vue'
	import timePicker from './time-picker.vue'
	import {
		initVueI18n
	} from '@dcloudio/uni-i18n'
	import messages from './i18n/index.js'
	const {
		t
	} = initVueI18n(messages)
	/**
	 * Calendar 日历
	 * @description 日历组件可以查看日期，选择任意范围内的日期，打点操作。常用场景如：酒店日期预订、火车机票选择购买日期、上下班打卡等
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=56
	 * @property {String} date 自定义当前时间，默认为今天
	 * @property {Boolean} lunar 显示农历
	 * @property {String} startDate 日期选择范围-开始日期
	 * @property {String} endDate 日期选择范围-结束日期
	 * @property {Boolean} range 范围选择
	 * @property {Boolean} insert = [true|false] 插入模式,默认为false
	 * 	@value true 弹窗模式
	 * 	@value false 插入模式
	 * @property {Boolean} clearDate = [true|false] 弹窗模式是否清空上次选择内容
	 * @property {Array} selected 打点，期待格式[{date: '2019-06-27', info: '签到', data: { custom: '自定义信息', name: '自定义消息头',xxx:xxx... }}]
	 * @property {Boolean} showMonth 是否选择月份为背景
	 * @event {Function} change 日期改变，`insert :ture` 时生效
	 * @event {Function} confirm 确认选择`insert :false` 时生效
	 * @event {Function} monthSwitch 切换月份时触发
	 * @example <uni-calendar :insert="true":lunar="true" :start-date="'2019-3-2'":end-date="'2019-5-20'"@change="change" />
	 */
	export default {
		components: {
			calendarItem,
			timePicker
		},
		props: {
			date: {
				type: String,
				default: ''
			},
			defTime: {
				type: [String, Object],
				default: ''
			},
			selectableTimes: {
				type: [Object],
				default () {
					return {}
				}
			},
			selected: {
				type: Array,
				default () {
					return []
				}
			},
			lunar: {
				type: Boolean,
				default: false
			},
			startDate: {
				type: String,
				default: ''
			},
			endDate: {
				type: String,
				default: ''
			},
			range: {
				type: Boolean,
				default: false
			},
			typeHasTime: {
				type: Boolean,
				default: false
			},
			insert: {
				type: Boolean,
				default: true
			},
			showMonth: {
				type: Boolean,
				default: true
			},
			clearDate: {
				type: Boolean,
				default: true
			},
			left: {
				type: Boolean,
				default: true
			},
			right: {
				type: Boolean,
				default: true
			},
			checkHover: {
				type: Boolean,
				default: true
			},
			hideSecond: {
				type: [Boolean],
				default: false
			},
			pleStatus: {
				type: Object,
				default () {
					return {
						before: '',
						after: '',
						data: [],
						fulldate: ''
					}
				}
			}
		},
		data() {
			return {
				show: false,
				weeks: [],
				calendar: {},
				nowDate: '',
				aniMaskShow: false,
				firstEnter: true,
				time: '',
				timeRange: {
					startTime: '',
					endTime: ''
				},
				tempSingleDate: '',
				tempRange: {
					before: '',
					after: ''
				}
			}
		},
		watch: {
			date: {
				immediate: true,
				handler(newVal, oldVal) {
					if (!this.range) {
						this.tempSingleDate = newVal
						setTimeout(() => {
							this.init(newVal)
						}, 100)
					}
				}
			},
			defTime: {
				immediate: true,
				handler(newVal, oldVal) {
					if (!this.range) {
						this.time = newVal
					} else {
						// console.log('-----', newVal);
						this.timeRange.startTime = newVal.start
						this.timeRange.endTime = newVal.end
					}
				}
			},
			startDate(val) {
				this.cale.resetSatrtDate(val)
				this.cale.setDate(this.nowDate.fullDate)
				this.weeks = this.cale.weeks
			},
			endDate(val) {
				this.cale.resetEndDate(val)
				this.cale.setDate(this.nowDate.fullDate)
				this.weeks = this.cale.weeks
			},
			selected(newVal) {
				this.cale.setSelectInfo(this.nowDate.fullDate, newVal)
				this.weeks = this.cale.weeks
			},
			pleStatus: {
				immediate: true,
				handler(newVal, oldVal) {
					const {
						before,
						after,
						fulldate,
						which
					} = newVal
					this.tempRange.before = before
					this.tempRange.after = after
					setTimeout(() => {
						if (fulldate) {
							this.cale.setHoverMultiple(fulldate)
							if (before && after) {
								this.cale.lastHover = true
								if (this.rangeWithinMonth(after, before)) return
								this.setDate(before)
							} else {
								this.cale.setMultiple(fulldate)
								this.setDate(this.nowDate.fullDate)
								this.calendar.fullDate = ''
								this.cale.lastHover = false
							}
						} else {
							this.cale.setDefaultMultiple(before, after)
							if (which === 'left') {
								this.setDate(before)
								this.weeks = this.cale.weeks
							} else {
								this.setDate(after)
								this.weeks = this.cale.weeks
							}
							this.cale.lastHover = true
						}
					}, 16)
				}
			}
		},
		computed: {
			reactStartTime() {
				const activeDate = this.range ? this.tempRange.before : this.calendar.fullDate
				const res = activeDate === this.startDate ? this.selectableTimes.start : ''
				return res
			},
			reactEndTime() {
				const activeDate = this.range ? this.tempRange.after : this.calendar.fullDate
				const res = activeDate === this.endDate ? this.selectableTimes.end : ''
				return res
			},
			/**
			 * for i18n
			 */
			selectDateText() {
				return t("uni-datetime-picker.selectDate")
			},
			startDateText() {
				return this.startPlaceholder || t("uni-datetime-picker.startDate")
			},
			endDateText() {
				return this.endPlaceholder || t("uni-datetime-picker.endDate")
			},
			okText() {
				return t("uni-datetime-picker.ok")
			},
			yearText() {
				return t("uni-datetime-picker.year")
			},
			monthText() {
				return t("uni-datetime-picker.month")
			},
			MONText() {
				return t("uni-calender.MON")
			},
			TUEText() {
				return t("uni-calender.TUE")
			},
			WEDText() {
				return t("uni-calender.WED")
			},
			THUText() {
				return t("uni-calender.THU")
			},
			FRIText() {
				return t("uni-calender.FRI")
			},
			SATText() {
				return t("uni-calender.SAT")
			},
			SUNText() {
				return t("uni-calender.SUN")
			},
			confirmText() {
				return t("uni-calender.confirm")
			},
		},
		created() {
			// 获取日历方法实例
			this.cale = new Calendar({
				// date: new Date(),
				selected: this.selected,
				startDate: this.startDate,
				endDate: this.endDate,
				range: this.range,
				// multipleStatus: this.pleStatus
			})
			// 选中某一天
			// this.cale.setDate(this.date)
			this.init(this.date)
			// this.setDay
		},
		methods: {
			leaveCale() {
				this.firstEnter = true
			},
			handleMouse(weeks) {
				if (weeks.disable) return
				if (this.cale.lastHover) return
				let {
					before,
					after
				} = this.cale.multipleStatus
				if (!before) return
				this.calendar = weeks
				// 设置范围选
				this.cale.setHoverMultiple(this.calendar.fullDate)
				this.weeks = this.cale.weeks
				// hover时，进入一个日历，更新另一个
				if (this.firstEnter) {
					this.$emit('firstEnterCale', this.cale.multipleStatus)
					this.firstEnter = false
				}
			},
			rangeWithinMonth(A, B) {
				const [yearA, monthA] = A.split('-')
				const [yearB, monthB] = B.split('-')
				return yearA === yearB && monthA === monthB
			},

			// 取消穿透
			clean() {
				this.close()
			},

			clearCalender() {
				if (this.range) {
					this.timeRange.startTime = ''
					this.timeRange.endTime = ''
					this.tempRange.before = ''
					this.tempRange.after = ''
					this.cale.multipleStatus.before = ''
					this.cale.multipleStatus.after = ''
					this.cale.multipleStatus.data = []
					this.cale.lastHover = false
				} else {
					this.time = ''
					this.tempSingleDate = ''
				}
				this.calendar.fullDate = ''
				this.setDate()
			},

			bindDateChange(e) {
				const value = e.detail.value + '-1'
				this.init(value)
			},
			/**
			 * 初始化日期显示
			 * @param {Object} date
			 */
			init(date) {
				this.cale.setDate(date)
				this.weeks = this.cale.weeks
				this.nowDate = this.calendar = this.cale.getInfo(date)
			},
			// choiceDate(weeks) {
			// 	if (weeks.disable) return
			// 	this.calendar = weeks
			// 	// 设置多选
			// 	this.cale.setMultiple(this.calendar.fullDate, true)
			// 	this.weeks = this.cale.weeks
			// 	this.tempSingleDate = this.calendar.fullDate
			// 	this.tempRange.before = this.cale.multipleStatus.before
			// 	this.tempRange.after = this.cale.multipleStatus.after
			// 	this.change()
			// },
			/**
			 * 打开日历弹窗
			 */
			open() {
				// 弹窗模式并且清理数据
				if (this.clearDate && !this.insert) {
					this.cale.cleanMultipleStatus()
					// this.cale.setDate(this.date)
					this.init(this.date)
				}
				this.show = true
				this.$nextTick(() => {
					setTimeout(() => {
						this.aniMaskShow = true
					}, 50)
				})
			},
			/**
			 * 关闭日历弹窗
			 */
			close() {
				this.aniMaskShow = false
				this.$nextTick(() => {
					setTimeout(() => {
						this.show = false
						this.$emit('close')
					}, 300)
				})
			},
			/**
			 * 确认按钮
			 */
			confirm() {
				this.setEmit('confirm')
				this.close()
			},
			/**
			 * 变化触发
			 */
			change() {
				if (!this.insert) return
				this.setEmit('change')
			},
			/**
			 * 选择月份触发
			 */
			monthSwitch() {
				let {
					year,
					month
				} = this.nowDate
				this.$emit('monthSwitch', {
					year,
					month: Number(month)
				})
			},
			/**
			 * 派发事件
			 * @param {Object} name
			 */
			setEmit(name) {
				let {
					year,
					month,
					date,
					fullDate,
					lunar,
					extraInfo
				} = this.calendar
				this.$emit(name, {
					range: this.cale.multipleStatus,
					year,
					month,
					date,
					time: this.time,
					timeRange: this.timeRange,
					fulldate: fullDate,
					lunar,
					extraInfo: extraInfo || {}
				})
			},
			/**
			 * 选择天触发
			 * @param {Object} weeks
			 */
			choiceDate(weeks) {
				if (weeks.disable) return
				this.calendar = weeks
				this.calendar.userChecked = true
				// 设置多选
				this.cale.setMultiple(this.calendar.fullDate, true)
				this.weeks = this.cale.weeks
				this.tempSingleDate = this.calendar.fullDate
				this.tempRange.before = this.cale.multipleStatus.before
				this.tempRange.after = this.cale.multipleStatus.after
				this.change()
			},
			/**
			 * 回到今天
			 */
			backtoday() {
				let date = this.cale.getDate(new Date()).fullDate
				// this.cale.setDate(date)
				this.init(date)
				this.change()
			},
			/**
			 * 比较时间大小
			 */
			dateCompare(startDate, endDate) {
				// 计算截止时间
				startDate = new Date(startDate.replace('-', '/').replace('-', '/'))
				// 计算详细项的截止时间
				endDate = new Date(endDate.replace('-', '/').replace('-', '/'))
				if (startDate <= endDate) {
					return true
				} else {
					return false
				}
			},
			/**
			 * 上个月
			 */
			pre() {
				const preDate = this.cale.getDate(this.nowDate.fullDate, -1, 'month').fullDate
				this.setDate(preDate)
				this.monthSwitch()

			},
			/**
			 * 下个月
			 */
			next() {
				const nextDate = this.cale.getDate(this.nowDate.fullDate, +1, 'month').fullDate
				this.setDate(nextDate)
				this.monthSwitch()
			},
			/**
			 * 设置日期
			 * @param {Object} date
			 */
			setDate(date) {
				this.cale.setDate(date)
				this.weeks = this.cale.weeks
				this.nowDate = this.cale.getInfo(date)
			}
		}
	}
</script>

<style lang="scss" >
	.uni-calendar {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: column;
	}

	.uni-calendar__mask {
		position: fixed;
		bottom: 0;
		top: 0;
		left: 0;
		right: 0;
		background-color: rgba(0, 0, 0, 0.4);
		transition-property: opacity;
		transition-duration: 0.3s;
		opacity: 0;
		/* #ifndef APP-NVUE */
		z-index: 99;
		/* #endif */
	}

	.uni-calendar--mask-show {
		opacity: 1
	}

	.uni-calendar--fixed {
		position: fixed;
		bottom: calc(var(--window-bottom));
		left: 0;
		right: 0;
		transition-property: transform;
		transition-duration: 0.3s;
		transform: translateY(460px);
		/* #ifndef APP-NVUE */
		z-index: 99;
		/* #endif */
	}

	.uni-calendar--ani-show {
		transform: translateY(0);
	}

	.uni-calendar__content {
		background-color: #fff;
	}

	.uni-calendar__content-mobile {
		border-top-left-radius: 10px;
		border-top-right-radius: 10px;
		box-shadow: 0px 0px 5px 3px rgba(0, 0, 0, 0.1);
	}

	.uni-calendar__header {
		position: relative;
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row;
		justify-content: center;
		align-items: center;
		height: 50px;
	}

	.uni-calendar__header-mobile {
		padding: 10px;
		padding-bottom: 0;
	}

	.uni-calendar--fixed-top {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row;
		justify-content: space-between;
		border-top-color: rgba(0, 0, 0, 0.4);
		border-top-style: solid;
		border-top-width: 1px;
	}

	.uni-calendar--fixed-width {
		width: 50px;
	}

	.uni-calendar__backtoday {
		position: absolute;
		right: 0;
		top: 25rpx;
		padding: 0 5px;
		padding-left: 10px;
		height: 25px;
		line-height: 25px;
		font-size: 12px;
		border-top-left-radius: 25px;
		border-bottom-left-radius: 25px;
		color: #fff;
		background-color: #f1f1f1;
	}

	.uni-calendar__header-text {
		text-align: center;
		width: 100px;
		font-size: 15px;
		color: #666;
	}

	.uni-calendar__button-text {
		text-align: center;
		width: 100px;
		font-size: 14px;
		color: #007aff;
		/* #ifndef APP-NVUE */
		letter-spacing: 3px;
		/* #endif */
	}

	.uni-calendar__header-btn-box {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row;
		align-items: center;
		justify-content: center;
		width: 50px;
		height: 50px;
	}

	.uni-calendar__header-btn {
		width: 9px;
		height: 9px;
		border-left-color: #808080;
		border-left-style: solid;
		border-left-width: 1px;
		border-top-color: #555555;
		border-top-style: solid;
		border-top-width: 1px;
	}

	.uni-calendar--left {
		transform: rotate(-45deg);
	}

	.uni-calendar--right {
		transform: rotate(135deg);
	}


	.uni-calendar__weeks {
		position: relative;
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row;
	}

	.uni-calendar__weeks-item {
		flex: 1;
	}

	.uni-calendar__weeks-day {
		flex: 1;
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: column;
		justify-content: center;
		align-items: center;
		height: 40px;
		border-bottom-color: #F5F5F5;
		border-bottom-style: solid;
		border-bottom-width: 1px;
	}

	.uni-calendar__weeks-day-text {
		font-size: 12px;
		color: #B2B2B2;
	}

	.uni-calendar__box {
		position: relative;
		// padding: 0 10px;
		padding-bottom: 7px;
	}

	.uni-calendar__box-bg {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		justify-content: center;
		align-items: center;
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
	}

	.uni-calendar__box-bg-text {
		font-size: 200px;
		font-weight: bold;
		color: #999;
		opacity: 0.1;
		text-align: center;
		/* #ifndef APP-NVUE */
		line-height: 1;
		/* #endif */
	}

	.uni-date-changed {
		padding: 0 10px;
		// line-height: 50px;
		text-align: center;
		color: #333;
		border-top-color: #DCDCDC;
		;
		border-top-style: solid;
		border-top-width: 1px;
		flex: 1;
	}

	.uni-date-btn--ok {
		padding: 20px 15px;
	}

	.uni-date-changed--time-start {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		align-items: center;
	}

	.uni-date-changed--time-end {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		align-items: center;
	}

	.uni-date-changed--time-date {
		color: #999;
		line-height: 50px;
		margin-right: 5px;
		// opacity: 0.6;
	}

	.time-picker-style {
		// width: 62px;
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		justify-content: center;
		align-items: center
	}

	.mr-10 {
		margin-right: 10px;
	}

	.dialog-close {
		position: absolute;
		top: 0;
		right: 0;
		bottom: 0;
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row;
		align-items: center;
		padding: 0 25px;
		margin-top: 10px;
	}

	.dialog-close-plus {
		width: 16px;
		height: 2px;
		background-color: #737987;
		border-radius: 2px;
		transform: rotate(45deg);
	}

	.dialog-close-rotate {
		position: absolute;
		transform: rotate(-45deg);
	}

	.uni-datetime-picker--btn {
		border-radius: 100px;
		height: 40px;
		line-height: 40px;
		background-color: #007aff;
		color: #fff;
		font-size: 16px;
		letter-spacing: 2px;
	}

	/* #ifndef APP-NVUE */
	.uni-datetime-picker--btn:active {
		opacity: 0.7;
	}
	/* #endif */
</style>
