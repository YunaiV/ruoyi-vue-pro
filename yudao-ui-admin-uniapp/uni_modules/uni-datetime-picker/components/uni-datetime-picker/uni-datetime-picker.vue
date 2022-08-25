<template>
	<view class="uni-date">
		<view class="uni-date-editor" @click="show">
			<slot>
				<view class="uni-date-editor--x" :class="{'uni-date-editor--x__disabled': disabled,
		'uni-date-x--border': border}">
					<view v-if="!isRange" class="uni-date-x uni-date-single">
						<uni-icons type="calendar" color="#c0c4cc" size="22"></uni-icons>
						<input class="uni-date__x-input" type="text" v-model="singleVal"
							:placeholder="singlePlaceholderText" :disabled="true" />
					</view>
					<view v-else class="uni-date-x uni-date-range">
						<uni-icons type="calendar" color="#c0c4cc" size="22"></uni-icons>
						<input class="uni-date__x-input t-c" type="text" v-model="range.startDate"
							:placeholder="startPlaceholderText" :disabled="true" />
						<slot>
							<view class="">{{rangeSeparator}}</view>
						</slot>
						<input class="uni-date__x-input t-c" type="text" v-model="range.endDate"
							:placeholder="endPlaceholderText" :disabled="true" />
					</view>
					<view v-if="showClearIcon" class="uni-date__icon-clear" @click.stop="clear">
						<uni-icons type="clear" color="#c0c4cc" size="24"></uni-icons>
					</view>
				</view>
			</slot>
		</view>

		<view v-show="popup" class="uni-date-mask" @click="close"></view>
		<view v-if="!isPhone" ref="datePicker" v-show="popup" class="uni-date-picker__container">
			<view v-if="!isRange" class="uni-date-single--x" :style="popover">
				<view class="uni-popper__arrow"></view>
				<view v-if="hasTime" class="uni-date-changed popup-x-header">
					<input class="uni-date__input t-c" type="text" v-model="tempSingleDate"
						:placeholder="selectDateText" />
					<time-picker type="time" v-model="time" :border="false" :disabled="!tempSingleDate"
						:start="reactStartTime" :end="reactEndTime" :hideSecond="hideSecond" style="width: 100%;">
						<input class="uni-date__input t-c" type="text" v-model="time" :placeholder="selectTimeText"
							:disabled="!tempSingleDate" />
					</time-picker>
				</view>
				<calendar ref="pcSingle" :showMonth="false" :start-date="caleRange.startDate"
					:end-date="caleRange.endDate" :date="defSingleDate" @change="singleChange"
					style="padding: 0 8px;" />
				<view v-if="hasTime" class="popup-x-footer">
					<!-- <text class="">此刻</text> -->
					<text class="confirm" @click="confirmSingleChange">{{okText}}</text>
				</view>
				<view class="uni-date-popper__arrow"></view>
			</view>

			<view v-else class="uni-date-range--x" :style="popover">
				<view class="uni-popper__arrow"></view>
				<view v-if="hasTime" class="popup-x-header uni-date-changed">
					<view class="popup-x-header--datetime">
						<input class="uni-date__input uni-date-range__input" type="text" v-model="tempRange.startDate"
							:placeholder="startDateText" />
						<time-picker type="time" v-model="tempRange.startTime" :start="reactStartTime" :border="false"
							:disabled="!tempRange.startDate" :hideSecond="hideSecond">
							<input class="uni-date__input uni-date-range__input" type="text"
								v-model="tempRange.startTime" :placeholder="startTimeText"
								:disabled="!tempRange.startDate" />
						</time-picker>
					</view>
					<uni-icons type="arrowthinright" color="#999" style="line-height: 40px;"></uni-icons>
					<view class="popup-x-header--datetime">
						<input class="uni-date__input uni-date-range__input" type="text" v-model="tempRange.endDate"
							:placeholder="endDateText" />
						<time-picker type="time" v-model="tempRange.endTime" :end="reactEndTime" :border="false"
							:disabled="!tempRange.endDate" :hideSecond="hideSecond">
							<input class="uni-date__input uni-date-range__input" type="text" v-model="tempRange.endTime"
								:placeholder="endTimeText" :disabled="!tempRange.endDate" />
						</time-picker>
					</view>
				</view>
				<view class="popup-x-body">
					<calendar ref="left" :showMonth="false" :start-date="caleRange.startDate"
						:end-date="caleRange.endDate" :range="true" @change="leftChange" :pleStatus="endMultipleStatus"
						@firstEnterCale="updateRightCale" @monthSwitch="leftMonthSwitch" style="padding: 0 8px;" />
					<calendar ref="right" :showMonth="false" :start-date="caleRange.startDate"
						:end-date="caleRange.endDate" :range="true" @change="rightChange"
						:pleStatus="startMultipleStatus" @firstEnterCale="updateLeftCale"
						@monthSwitch="rightMonthSwitch" style="padding: 0 8px;border-left: 1px solid #F1F1F1;" />
				</view>
				<view v-if="hasTime" class="popup-x-footer">
					<text class="" @click="clear">{{clearText}}</text>
					<text class="confirm" @click="confirmRangeChange">{{okText}}</text>
				</view>
			</view>
		</view>
		<calendar v-show="isPhone" ref="mobile" :clearDate="false" :date="defSingleDate" :defTime="reactMobDefTime"
			:start-date="caleRange.startDate" :end-date="caleRange.endDate" :selectableTimes="mobSelectableTime"
			:pleStatus="endMultipleStatus" :showMonth="false" :range="isRange" :typeHasTime="hasTime" :insert="false"
			:hideSecond="hideSecond" @confirm="mobileChange" />
	</view>
</template>
<script>
	/**
	 * DatetimePicker 时间选择器
	 * @description 同时支持 PC 和移动端使用日历选择日期和日期范围
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=3962
	 * @property {String} type 选择器类型
	 * @property {String|Number|Array|Date} value 绑定值
	 * @property {String} placeholder 单选择时的占位内容
	 * @property {String} start 起始时间
	 * @property {String} end 终止时间
	 * @property {String} start-placeholder 范围选择时开始日期的占位内容
	 * @property {String} end-placeholder 范围选择时结束日期的占位内容
	 * @property {String} range-separator 选择范围时的分隔符
	 * @property {Boolean} border = [true|false] 是否有边框
	 * @property {Boolean} disabled = [true|false] 是否禁用
	 * @property {Boolean} clearIcon = [true|false] 是否显示清除按钮（仅PC端适用）
	 * @event {Function} change 确定日期时触发的事件
	 * @event {Function} show 打开弹出层
	 * @event {Function} close 关闭弹出层
	 * @event {Function} clear 清除上次选中的状态和值
	 **/
	import calendar from './calendar.vue'
	import timePicker from './time-picker.vue'
	import {
		initVueI18n
	} from '@dcloudio/uni-i18n'
	import messages from './i18n/index.js'
	const {
		t
	} = initVueI18n(messages)

	export default {
		name: 'UniDatetimePicker',
		options: {
			virtualHost: true
		},
		components: {
			calendar,
			timePicker
		},
		inject: {
			form: {
				from: 'uniForm',
				default: null
			},
			formItem: {
				from: 'uniFormItem',
				default: null
			},
		},
		data() {
			return {
				isRange: false,
				hasTime: false,
				mobileRange: false,
				// 单选
				singleVal: '',
				tempSingleDate: '',
				defSingleDate: '',
				time: '',
				// 范围选
				caleRange: {
					startDate: '',
					startTime: '',
					endDate: '',
					endTime: ''
				},
				range: {
					startDate: '',
					// startTime: '',
					endDate: '',
					// endTime: ''
				},
				tempRange: {
					startDate: '',
					startTime: '',
					endDate: '',
					endTime: ''
				},
				// 左右日历同步数据
				startMultipleStatus: {
					before: '',
					after: '',
					data: [],
					fulldate: ''
				},
				endMultipleStatus: {
					before: '',
					after: '',
					data: [],
					fulldate: ''
				},
				visible: false,
				popup: false,
				popover: null,
				isEmitValue: false,
				isPhone: false,
				isFirstShow: true,
			}
		},
		props: {
			type: {
				type: String,
				default: 'datetime'
			},
			value: {
				type: [String, Number, Array, Date],
				default: ''
			},
			modelValue: {
				type: [String, Number, Array, Date],
				default: ''
			},
			start: {
				type: [Number, String],
				default: ''
			},
			end: {
				type: [Number, String],
				default: ''
			},
			returnType: {
				type: String,
				default: 'string'
			},
			placeholder: {
				type: String,
				default: ''
			},
			startPlaceholder: {
				type: String,
				default: ''
			},
			endPlaceholder: {
				type: String,
				default: ''
			},
			rangeSeparator: {
				type: String,
				default: '-'
			},
			border: {
				type: [Boolean],
				default: true
			},
			disabled: {
				type: [Boolean],
				default: false
			},
			clearIcon: {
				type: [Boolean],
				default: true
			},
			hideSecond: {
				type: [Boolean],
				default: false
			}
		},
		watch: {
			type: {
				immediate: true,
				handler(newVal, oldVal) {
					if (newVal.indexOf('time') !== -1) {
						this.hasTime = true
					} else {
						this.hasTime = false
					}
					if (newVal.indexOf('range') !== -1) {
						this.isRange = true
					} else {
						this.isRange = false
					}
				}
			},
			// #ifndef VUE3
			value: {
				immediate: true,
				handler(newVal, oldVal) {
					if (this.isEmitValue) {
						this.isEmitValue = false
						return
					}
					this.initPicker(newVal)
				}
			},
			// #endif
			// #ifdef VUE3
			modelValue: {
				immediate: true,
				handler(newVal, oldVal) {
					if (this.isEmitValue) {
						this.isEmitValue = false
						return
					}
					this.initPicker(newVal)
				}
			},
			// #endif
			start: {
				immediate: true,
				handler(newVal, oldVal) {
					if (!newVal) return
					const {
						defDate,
						defTime
					} = this.parseDate(newVal)
					this.caleRange.startDate = defDate
					if (this.hasTime) {
						this.caleRange.startTime = defTime
					}
				}
			},
			end: {
				immediate: true,
				handler(newVal, oldVal) {
					if (!newVal) return
					const {
						defDate,
						defTime
					} = this.parseDate(newVal)
					this.caleRange.endDate = defDate
					if (this.hasTime) {
						this.caleRange.endTime = defTime
					}
				}
			},
		},
		computed: {
			reactStartTime() {
				const activeDate = this.isRange ? this.tempRange.startDate : this.tempSingleDate
				const res = activeDate === this.caleRange.startDate ? this.caleRange.startTime : ''
				return res
			},
			reactEndTime() {
				const activeDate = this.isRange ? this.tempRange.endDate : this.tempSingleDate
				const res = activeDate === this.caleRange.endDate ? this.caleRange.endTime : ''
				return res
			},
			reactMobDefTime() {
				const times = {
					start: this.tempRange.startTime,
					end: this.tempRange.endTime
				}
				return this.isRange ? times : this.time
			},
			mobSelectableTime() {
				return {
					start: this.caleRange.startTime,
					end: this.caleRange.endTime
				}
			},
			datePopupWidth() {
				// todo
				return this.isRange ? 653 : 301
			},

			/**
			 * for i18n
			 */
			singlePlaceholderText() {
				return this.placeholder || (this.type === 'date' ? this.selectDateText : t(
					"uni-datetime-picker.selectDateTime"))
			},
			startPlaceholderText() {
				return this.startPlaceholder || this.startDateText
			},
			endPlaceholderText() {
				return this.endPlaceholder || this.endDateText
			},
			selectDateText() {
				return t("uni-datetime-picker.selectDate")
			},
			selectTimeText() {
				return t("uni-datetime-picker.selectTime")
			},
			startDateText() {
				return this.startPlaceholder || t("uni-datetime-picker.startDate")
			},
			startTimeText() {
				return t("uni-datetime-picker.startTime")
			},
			endDateText() {
				return this.endPlaceholder || t("uni-datetime-picker.endDate")
			},
			endTimeText() {
				return t("uni-datetime-picker.endTime")
			},
			okText() {
				return t("uni-datetime-picker.ok")
			},
			clearText() {
				return t("uni-datetime-picker.clear")
			},
			showClearIcon() {
				const {
					clearIcon,
					disabled,
					singleVal,
					range
				} = this
				const bool = clearIcon && !disabled && (singleVal || (range.startDate && range.endDate))
				return bool
			}
		},
		created() {
			// if (this.form && this.formItem) {
			// 	this.$watch('formItem.errMsg', (newVal) => {
			// 		this.localMsg = newVal
			// 	})
			// }
		},
		mounted() {
			this.platform()
		},
		methods: {
			initPicker(newVal) {
				if (!newVal || Array.isArray(newVal) && !newVal.length) {
					this.$nextTick(() => {
						this.clear(false)
					})
					return
				}
				if (!Array.isArray(newVal) && !this.isRange) {
					const {
						defDate,
						defTime
					} = this.parseDate(newVal)
					this.singleVal = defDate
					this.tempSingleDate = defDate
					this.defSingleDate = defDate
					if (this.hasTime) {
						this.singleVal = defDate + ' ' + defTime
						this.time = defTime
					}
				} else {
					const [before, after] = newVal
					if (!before && !after) return
					const defBefore = this.parseDate(before)
					const defAfter = this.parseDate(after)
					const startDate = defBefore.defDate
					const endDate = defAfter.defDate
					this.range.startDate = this.tempRange.startDate = startDate
					this.range.endDate = this.tempRange.endDate = endDate

					if (this.hasTime) {
						this.range.startDate = defBefore.defDate + ' ' + defBefore.defTime
						this.range.endDate = defAfter.defDate + ' ' + defAfter.defTime
						this.tempRange.startTime = defBefore.defTime
						this.tempRange.endTime = defAfter.defTime
					}
					const defaultRange = {
						before: defBefore.defDate,
						after: defAfter.defDate
					}
					this.startMultipleStatus = Object.assign({}, this.startMultipleStatus, defaultRange, {
						which: 'right'
					})
					this.endMultipleStatus = Object.assign({}, this.endMultipleStatus, defaultRange, {
						which: 'left'
					})
				}
			},
			updateLeftCale(e) {
				const left = this.$refs.left
				// 设置范围选
				left.cale.setHoverMultiple(e.after)
				left.setDate(this.$refs.left.nowDate.fullDate)
			},
			updateRightCale(e) {
				const right = this.$refs.right
				// 设置范围选
				right.cale.setHoverMultiple(e.after)
				right.setDate(this.$refs.right.nowDate.fullDate)
			},
			platform() {
				const systemInfo = uni.getSystemInfoSync()
				this.isPhone = systemInfo.windowWidth <= 500
				this.windowWidth = systemInfo.windowWidth
			},
			show(event) {
				if (this.disabled) {
					return
				}
				this.platform()
				if (this.isPhone) {
					this.$refs.mobile.open()
					return
				}
				this.popover = {
					top: '10px'
				}
				const dateEditor = uni.createSelectorQuery().in(this).select(".uni-date-editor")
				dateEditor.boundingClientRect(rect => {
					if (this.windowWidth - rect.left < this.datePopupWidth) {
						this.popover.right = 0
					}
				}).exec()
				setTimeout(() => {
					this.popup = !this.popup
					if (!this.isPhone && this.isRange && this.isFirstShow) {
						this.isFirstShow = false
						const {
							startDate,
							endDate
						} = this.range
						if (startDate && endDate) {
							if (this.diffDate(startDate, endDate) < 30) {
								this.$refs.right.next()
							}
						} else {
							this.$refs.right.next()
							this.$refs.right.cale.lastHover = false
						}
					}

				}, 50)
			},

			close() {
				setTimeout(() => {
					this.popup = false
					this.$emit('maskClick', this.value)
				}, 20)
			},
			setEmit(value) {
				if (this.returnType === "timestamp" || this.returnType === "date") {
					if (!Array.isArray(value)) {
						if (!this.hasTime) {
							value = value + ' ' + '00:00:00'
						}
						value = this.createTimestamp(value)
						if (this.returnType === "date") {
							value = new Date(value)
						}
					} else {
						if (!this.hasTime) {
							value[0] = value[0] + ' ' + '00:00:00'
							value[1] = value[1] + ' ' + '00:00:00'
						}
						value[0] = this.createTimestamp(value[0])
						value[1] = this.createTimestamp(value[1])
						if (this.returnType === "date") {
							value[0] = new Date(value[0])
							value[1] = new Date(value[1])
						}
					}
				}
				
				
				this.$emit('change', value)
				this.$emit('input', value)
				this.$emit('update:modelValue', value)
				this.isEmitValue = true
			},
			createTimestamp(date) {
				date = this.fixIosDateFormat(date)
				return Date.parse(new Date(date))
			},
			singleChange(e) {
				this.tempSingleDate = e.fulldate
				if (this.hasTime) return
				this.confirmSingleChange()
			},

			confirmSingleChange() {
				if (!this.tempSingleDate) {
					this.popup = false
					return
				}
				if (this.hasTime) {
					this.singleVal = this.tempSingleDate + ' ' + (this.time ? this.time : '00:00:00')
				} else {
					this.singleVal = this.tempSingleDate
				}
				this.setEmit(this.singleVal)
				this.popup = false
			},

			leftChange(e) {
				const {
					before,
					after
				} = e.range
				this.rangeChange(before, after)
				const obj = {
					before: e.range.before,
					after: e.range.after,
					data: e.range.data,
					fulldate: e.fulldate
				}
				this.startMultipleStatus = Object.assign({}, this.startMultipleStatus, obj)
			},

			rightChange(e) {
				const {
					before,
					after
				} = e.range
				this.rangeChange(before, after)
				const obj = {
					before: e.range.before,
					after: e.range.after,
					data: e.range.data,
					fulldate: e.fulldate
				}
				this.endMultipleStatus = Object.assign({}, this.endMultipleStatus, obj)
			},

			mobileChange(e) {
				if (this.isRange) {
					const {
						before,
						after
					} = e.range
					this.handleStartAndEnd(before, after, true)
					if (this.hasTime) {
						const {
							startTime,
							endTime
						} = e.timeRange
						this.tempRange.startTime = startTime
						this.tempRange.endTime = endTime
					}
					this.confirmRangeChange()

				} else {
					if (this.hasTime) {
						this.singleVal = e.fulldate + ' ' + e.time
					} else {
						this.singleVal = e.fulldate
					}
					this.setEmit(this.singleVal)
				}
				this.$refs.mobile.close()
			},

			rangeChange(before, after) {
				if (!(before && after)) return
				this.handleStartAndEnd(before, after, true)
				if (this.hasTime) return
				this.confirmRangeChange()
			},

			confirmRangeChange() {
				if (!this.tempRange.startDate && !this.tempRange.endDate) {
					this.popup = false
					return
				}
				let start, end
				if (!this.hasTime) {
					start = this.range.startDate = this.tempRange.startDate
					end = this.range.endDate = this.tempRange.endDate
				} else {
					start = this.range.startDate = this.tempRange.startDate + ' ' +
						(this.tempRange.startTime ? this.tempRange.startTime : '00:00:00')
					end = this.range.endDate = this.tempRange.endDate + ' ' +
						(this.tempRange.endTime ? this.tempRange.endTime : '00:00:00')
				}
				const displayRange = [start, end]
				this.setEmit(displayRange)
				this.popup = false
			},

			handleStartAndEnd(before, after, temp = false) {
				if (!(before && after)) return
				const type = temp ? 'tempRange' : 'range'
				if (this.dateCompare(before, after)) {
					this[type].startDate = before
					this[type].endDate = after
				} else {
					this[type].startDate = after
					this[type].endDate = before
				}
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
			 * 比较时间差
			 */
			diffDate(startDate, endDate) {
				// 计算截止时间
				startDate = new Date(startDate.replace('-', '/').replace('-', '/'))
				// 计算详细项的截止时间
				endDate = new Date(endDate.replace('-', '/').replace('-', '/'))
				const diff = (endDate - startDate) / (24 * 60 * 60 * 1000)
				return Math.abs(diff)
			},

			clear(needEmit = true) {
				if (!this.isRange) {
					this.singleVal = ''
					this.tempSingleDate = ''
					this.time = ''
					if (this.isPhone) {
						this.$refs.mobile && this.$refs.mobile.clearCalender()
					} else {
						this.$refs.pcSingle && this.$refs.pcSingle.clearCalender()
					}
					if (needEmit) {
						// 校验规则
						// if(this.form  && this.formItem){
						// 	const {
						// 		validateTrigger
						// 	} = this.form
						// 	if (validateTrigger === 'blur') {
						// 		this.formItem.onFieldChange()
						// 	}
						// }
						this.$emit('change', '')
						this.$emit('input', '')
						this.$emit('update:modelValue', '')
					}
				} else {
					this.range.startDate = ''
					this.range.endDate = ''
					this.tempRange.startDate = ''
					this.tempRange.startTime = ''
					this.tempRange.endDate = ''
					this.tempRange.endTime = ''
					if (this.isPhone) {
						this.$refs.mobile && this.$refs.mobile.clearCalender()
					} else {
						this.$refs.left && this.$refs.left.clearCalender()
						this.$refs.right && this.$refs.right.clearCalender()
						this.$refs.right && this.$refs.right.next()
					}
					if (needEmit) {
						this.$emit('change', [])
						this.$emit('input', [])
						this.$emit('update:modelValue', [])
					}
				}
			},

			parseDate(date) {
				date = this.fixIosDateFormat(date)
				const defVal = new Date(date)
				const year = defVal.getFullYear()
				const month = defVal.getMonth() + 1
				const day = defVal.getDate()
				const hour = defVal.getHours()
				const minute = defVal.getMinutes()
				const second = defVal.getSeconds()
				const defDate = year + '-' + this.lessTen(month) + '-' + this.lessTen(day)
				const defTime = this.lessTen(hour) + ':' + this.lessTen(minute) + (this.hideSecond ? '' : (':' + this
					.lessTen(second)))
				return {
					defDate,
					defTime
				}
			},

			lessTen(item) {
				return item < 10 ? '0' + item : item
			},

			//兼容 iOS、safari 日期格式
			fixIosDateFormat(value) {
				if (typeof value === 'string') {
					value = value.replace(/-/g, '/')
				}
				return value
			},

			leftMonthSwitch(e) {
				// console.log('leftMonthSwitch 返回:', e)
			},
			rightMonthSwitch(e) {
				// console.log('rightMonthSwitch 返回:', e)
			}
		}
	}
</script>

<style>
	.uni-date {
		/* #ifndef APP-NVUE */
		width: 100%;
		/* #endif */
		flex: 1;
	}
	.uni-date-x {
		display: flex;
		flex-direction: row;
		align-items: center;
		justify-content: center;
		padding: 0 10px;
		border-radius: 4px;
		background-color: #fff;
		color: #666;
		font-size: 14px;
		flex: 1;
	}

	.uni-date-x--border {
		box-sizing: border-box;
		border-radius: 4px;
		border: 1px solid #e5e5e5;
	}

	.uni-date-editor--x {
		display: flex;
		align-items: center;
		position: relative;
	}

	.uni-date-editor--x .uni-date__icon-clear {
		padding: 0 5px;
		display: flex;
		align-items: center;
		/* #ifdef H5 */
		cursor: pointer;
		/* #endif */
	}

	.uni-date__x-input {
		padding: 0 8px;
		/* #ifndef APP-NVUE */
		width: auto;
		/* #endif */
		position: relative;
		overflow: hidden;
		flex: 1;
		line-height: 1;
		font-size: 14px;
		height: 35px;
	}

	.t-c {
		text-align: center;
	}

	.uni-date__input {
		height: 40px;
		width: 100%;
		line-height: 40px;
		font-size: 14px;
	}

	.uni-date-range__input {
		text-align: center;
		max-width: 142px;
	}

	.uni-date-picker__container {
		position: relative;
		/* 		position: fixed;
		left: 0;
		right: 0;
		top: 0;
		bottom: 0;
		box-sizing: border-box;
		z-index: 996;
		font-size: 14px; */
	}

	.uni-date-mask {
		position: fixed;
		bottom: 0px;
		top: 0px;
		left: 0px;
		right: 0px;
		background-color: rgba(0, 0, 0, 0);
		transition-duration: 0.3s;
		z-index: 996;
	}

	.uni-date-single--x {
		/* padding: 0 8px; */
		background-color: #fff;
		position: absolute;
		top: 0;
		z-index: 999;
		border: 1px solid #EBEEF5;
		box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
		border-radius: 4px;
	}

	.uni-date-range--x {
		/* padding: 0 8px; */
		background-color: #fff;
		position: absolute;
		top: 0;
		z-index: 999;
		border: 1px solid #EBEEF5;
		box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
		border-radius: 4px;
	}

	.uni-date-editor--x__disabled {
		opacity: 0.4;
		cursor: default;
	}

	.uni-date-editor--logo {
		width: 16px;
		height: 16px;
		vertical-align: middle;
	}

	/* 添加时间 */
	.popup-x-header {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row;
		/* justify-content: space-between; */
	}

	.popup-x-header--datetime {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row;
		flex: 1;
	}

	.popup-x-body {
		display: flex;
	}

	.popup-x-footer {
		padding: 0 15px;
		border-top-color: #F1F1F1;
		border-top-style: solid;
		border-top-width: 1px;
		/* background-color: #fff; */
		line-height: 40px;
		text-align: right;
		color: #666;
	}

	.popup-x-footer text:hover {
		color: #007aff;
		cursor: pointer;
		opacity: 0.8;
	}

	.popup-x-footer .confirm {
		margin-left: 20px;
		color: #007aff;
	}

	.uni-date-changed {
		/* background-color: #fff; */
		text-align: center;
		color: #333;
		border-bottom-color: #F1F1F1;
		border-bottom-style: solid;
		border-bottom-width: 1px;
		/* padding: 0 50px; */
	}

	.uni-date-changed--time text {
		/* padding: 0 20px; */
		height: 50px;
		line-height: 50px;
	}

	.uni-date-changed .uni-date-changed--time {
		/* display: flex; */
		flex: 1;
	}

	.uni-date-changed--time-date {
		color: #333;
		opacity: 0.6;
	}

	.mr-50 {
		margin-right: 50px;
	}

	/* picker 弹出层通用的指示小三角, todo：扩展至上下左右方向定位 */
	.uni-popper__arrow,
	.uni-popper__arrow::after {
		position: absolute;
		display: block;
		width: 0;
		height: 0;
		border-color: transparent;
		border-style: solid;
		border-width: 6px;
	}

	.uni-popper__arrow {
		filter: drop-shadow(0 2px 12px rgba(0, 0, 0, 0.03));
		top: -6px;
		left: 10%;
		margin-right: 3px;
		border-top-width: 0;
		border-bottom-color: #EBEEF5;
	}

	.uni-popper__arrow::after {
		content: " ";
		top: 1px;
		margin-left: -6px;
		border-top-width: 0;
		border-bottom-color: #fff;
	}
</style>
