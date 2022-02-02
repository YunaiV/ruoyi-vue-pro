<template>
	<view class="u-calendar-month-wrapper" ref="u-calendar-month-wrapper">
		<view v-for="(item, index) in months" :key="index" :class="[`u-calendar-month-${index}`]"
			:ref="`u-calendar-month-${index}`" :id="`month-${item.month}`">
			<text v-if="index !== 0" class="u-calendar-month__title">{{ item.year }}年{{ item.month }}月</text>
			<view class="u-calendar-month__days">
				<view v-if="showMark" class="u-calendar-month__days__month-mark-wrapper">
					<text class="u-calendar-month__days__month-mark-wrapper__text">{{ item.month }}</text>
				</view>
				<view class="u-calendar-month__days__day" v-for="(item1, index1) in item.date" :key="index1"
					:style="[dayStyle(index, index1, item1)]" @tap="clickHandler(index, index1, item1)"
					:class="[item1.selected && 'u-calendar-month__days__day__select--selected']">
					<view class="u-calendar-month__days__day__select" :style="[daySelectStyle(index, index1, item1)]">
						<text class="u-calendar-month__days__day__select__info"
							:class="[item1.disabled && 'u-calendar-month__days__day__select__info--disabled']"
							:style="[textStyle(item1)]">{{ item1.day }}</text>
						<text v-if="getBottomInfo(index, index1, item1)"
							class="u-calendar-month__days__day__select__buttom-info"
							:class="[item1.disabled && 'u-calendar-month__days__day__select__buttom-info--disabled']"
							:style="[textStyle(item1)]">{{ getBottomInfo(index, index1, item1) }}</text>
						<text v-if="item1.dot" class="u-calendar-month__days__day__select__dot"></text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	// #ifdef APP-NVUE
	// 由于nvue不支持百分比单位，需要查询宽度来计算每个日期的宽度
	const dom = uni.requireNativePlugin('dom')
	// #endif
	import dayjs from '../../libs/util/dayjs.js';
	export default {
		name: 'u-calendar-month',
		mixins: [uni.$u.mpMixin, uni.$u.mixin],
		props: {
			// 是否显示月份背景色
			showMark: {
				type: Boolean,
				default: true
			},
			// 主题色，对底部按钮和选中日期有效
			color: {
				type: String,
				default: '#3c9cff'
			},
			// 月份数据
			months: {
				type: Array,
				default: () => []
			},
			// 日期选择类型
			mode: {
				type: String,
				default: 'single'
			},
			// 日期行高
			rowHeight: {
				type: [String, Number],
				default: 58
			},
			// mode=multiple时，最多可选多少个日期
			maxCount: {
				type: [String, Number],
				default: Infinity
			},
			// mode=range时，第一个日期底部的提示文字
			startText: {
				type: String,
				default: '开始'
			},
			// mode=range时，最后一个日期底部的提示文字
			endText: {
				type: String,
				default: '结束'
			},
			// 默认选中的日期，mode为multiple或range是必须为数组格式
			defaultDate: {
				type: [Array, String, Date],
				default: null
			},
			// 最小的可选日期
			minDate: {
				type: [String, Number],
				default: 0
			},
			// 最大可选日期
			maxDate: {
				type: [String, Number],
				default: 0
			},
			// 如果没有设置maxDate，则往后推多少个月
			maxMonth: {
				type: [String, Number],
				default: 2
			},
			// 是否为只读状态，只读状态下禁止选择日期
			readonly: {
				type: Boolean,
				default: uni.$u.props.calendar.readonly
			},
			// 日期区间最多可选天数，默认无限制，mode = range时有效
			maxRange: {
				type: [Number, String],
				default: Infinity
			},
			// 范围选择超过最多可选天数时的提示文案，mode = range时有效
			rangePrompt: {
				type: String,
				default: ''
			},
			// 范围选择超过最多可选天数时，是否展示提示文案，mode = range时有效
			showRangePrompt: {
				type: Boolean,
				default: true
			},
			// 是否允许日期范围的起止时间为同一天，mode = range时有效
			allowSameDay: {
				type: Boolean,
				default: false
			}
		},
		data() {
			return {
				// 每个日期的宽度
				width: 0,
				// 当前选中的日期item
				item: {},
				selected: []
			}
		},
		watch: {
			selectedChange: {
				immediate: true,
				handler(n) {
					this.setDefaultDate()
				}
			}
		},
		computed: {
			// 多个条件的变化，会引起选中日期的变化，这里统一管理监听
			selectedChange() {
				return [this.minDate, this.maxDate, this.defaultDate]
			},
			dayStyle(index1, index2, item) {
				return (index1, index2, item) => {
					const style = {}
					let week = item.week
					// 不进行四舍五入的形式保留2位小数
					const dayWidth = Number(parseFloat(this.width / 7).toFixed(3).slice(0, -1))
					// 得出每个日期的宽度
					// #ifdef APP-NVUE
					style.width = uni.$u.addUnit(dayWidth)
					// #endif
					style.height = uni.$u.addUnit(this.rowHeight)
					if (index2 === 0) {
						// 获取当前为星期几，如果为0，则为星期天，减一为每月第一天时，需要向左偏移的item个数
						week = (week === 0 ? 7 : week) - 1
						style.marginLeft = uni.$u.addUnit(week * dayWidth)
					}
					if (this.mode === 'range') {
						// 之所以需要这么写，是因为DCloud公司的iOS客户端的开发者能力有限导致的bug
						style.paddingLeft = 0
						style.paddingRight = 0
						style.paddingBottom = 0
						style.paddingTop = 0
					}
					return style
				}
			},
			daySelectStyle() {
				return (index1, index2, item) => {
					let date = dayjs(item.date).format("YYYY-MM-DD"),
						style = {}
					// 判断date是否在selected数组中，因为月份可能会需要补0，所以使用dateSame判断，而不用数组的includes判断
					if (this.selected.some(item => this.dateSame(item, date))) {
						style.backgroundColor = this.color
					}
					if (this.mode === 'single') {
						if (date === this.selected[0]) {
							// 因为需要对nvue的兼容，只能这么写，无法缩写，也无法通过类名控制等等
							style.borderTopLeftRadius = '3px'
							style.borderBottomLeftRadius = '3px'
							style.borderTopRightRadius = '3px'
							style.borderBottomRightRadius = '3px'
						}
					} else if (this.mode === 'range') {
						if (this.selected.length >= 2) {
							const len = this.selected.length - 1
							// 第一个日期设置左上角和左下角的圆角
							if (this.dateSame(date, this.selected[0])) {
								style.borderTopLeftRadius = '3px'
								style.borderBottomLeftRadius = '3px'
							}
							// 最后一个日期设置右上角和右下角的圆角
							if (this.dateSame(date, this.selected[len])) {
								style.borderTopRightRadius = '3px'
								style.borderBottomRightRadius = '3px'
							}
							// 处于第一和最后一个之间的日期，背景色设置为浅色，通过将对应颜色进行等分，再取其尾部的颜色值
							if (dayjs(date).isAfter(dayjs(this.selected[0])) && dayjs(date).isBefore(dayjs(this
									.selected[len]))) {
								style.backgroundColor = uni.$u.colorGradient(this.color, '#ffffff', 100)[90]
								// 增加一个透明度，让范围区间的背景色也能看到底部的mark水印字符
								style.opacity = 0.7
							}
						} else if (this.selected.length === 1) {
							// 之所以需要这么写，是因为DCloud公司的iOS客户端的开发者能力有限导致的bug
							// 进行还原操作，否则在nvue的iOS，uni-app有bug，会导致诡异的表现
							style.borderTopLeftRadius = '3px'
							style.borderBottomLeftRadius = '3px'
						}
					} else {
						if (this.selected.some(item => this.dateSame(item, date))) {
							style.borderTopLeftRadius = '3px'
							style.borderBottomLeftRadius = '3px'
							style.borderTopRightRadius = '3px'
							style.borderBottomRightRadius = '3px'
						}
					}
					return style
				}
			},
			// 某个日期是否被选中
			textStyle() {
				return (item) => {
					const date = dayjs(item.date).format("YYYY-MM-DD"),
						style = {}
					// 选中的日期，提示文字设置白色
					if (this.selected.some(item => this.dateSame(item, date))) {
						style.color = '#ffffff'
					}
					if (this.mode === 'range') {
						const len = this.selected.length - 1
						// 如果是范围选择模式，第一个和最后一个之间的日期，文字颜色设置为高亮的主题色
						if (dayjs(date).isAfter(dayjs(this.selected[0])) && dayjs(date).isBefore(dayjs(this
								.selected[len]))) {
							style.color = this.color
						}
					}
					return style
				}
			},
			// 获取底部的提示文字
			getBottomInfo() {
				return (index1, index2, item) => {
					const date = dayjs(item.date).format("YYYY-MM-DD")
					const bottomInfo = item.bottomInfo
					// 当为日期范围模式时，且选择的日期个数大于0时
					if (this.mode === 'range' && this.selected.length > 0) {
						if (this.selected.length === 1) {
							// 选择了一个日期时，如果当前日期为数组中的第一个日期，则显示底部文字为“开始”
							if (this.dateSame(date, this.selected[0])) return this.startText
							else return bottomInfo
						} else {
							const len = this.selected.length - 1
							// 如果数组中的日期大于2个时，第一个和最后一个显示为开始和结束日期
							if (this.dateSame(date, this.selected[0]) && this.dateSame(date, this.selected[1]) &&
								len === 1) {
								// 如果长度为2，且第一个等于第二个日期，则提示语放在同一个item中
								return `${this.startText}/${this.endText}`
							} else if (this.dateSame(date, this.selected[0])) {
								return this.startText
							} else if (this.dateSame(date, this.selected[len])) {
								return this.endText
							} else {
								return bottomInfo
							}
						}
					} else {
						return bottomInfo
					}
				}
			}
		},
		mounted() {
			this.init()
		},
		methods: {
			init() {
				this.$nextTick(() => {
					// 这里需要另一个延时，因为获取宽度后，会进行月份数据渲染，只有渲染完成之后，才有真正的高度
					// 因为nvue下，$nextTick并不是100%可靠的
					uni.$u.sleep(10).then(() => {
						this.getWrapperWidth()
						this.getMonthRect()
					})
				})
			},
			// 判断两个日期是否相等
			dateSame(date1, date2) {
				return dayjs(date1).isSame(dayjs(date2))
			},
			// 获取月份数据区域的宽度，因为nvue不支持百分比，所以无法通过css设置每个日期item的宽度
			getWrapperWidth() {
				// #ifdef APP-NVUE
				dom.getComponentRect(this.$refs['u-calendar-month-wrapper'], res => {
					this.width = res.size.width
				})
				// #endif
				// #ifndef APP-NVUE
				this.$uGetRect('.u-calendar-month-wrapper').then(size => {
					this.width = size.width
				})
				// #endif
			},
			getMonthRect() {
				// 获取每个月份数据的尺寸，用于父组件在scroll-view滚动事件中，监听当前滚动到了第几个月份
				const promiseAllArr = this.months.map((item, index) => this.getMonthRectByPromise(
					`u-calendar-month-${index}`))
				// 一次性返回
				Promise.all(promiseAllArr).then(
					sizes => {
						let height = 1
						const topArr = []
						for (let i = 0; i < this.months.length; i++) {
							// 添加到months数组中，供scroll-view滚动事件中，判断当前滚动到哪个月份
							topArr[i] = height
							height += sizes[i].height
						}
						// 由于微信下，无法通过this.months[i].top的形式(引用类型)去修改父组件的month的top值，所以使用事件形式对外发出
						this.$emit('updateMonthTop', topArr)
					})
			},
			// 获取每个月份区域的尺寸
			getMonthRectByPromise(el) {
				// #ifndef APP-NVUE
				// $uGetRect为uView自带的节点查询简化方法，详见文档介绍：https://www.uviewui.com/js/getRect.html
				// 组件内部一般用this.$uGetRect，对外的为this.$u.getRect，二者功能一致，名称不同
				return new Promise(resolve => {
					this.$uGetRect(`.${el}`).then(size => {
						resolve(size)
					})
				})
				// #endif

				// #ifdef APP-NVUE 
				// nvue下，使用dom模块查询元素高度
				// 返回一个promise，让调用此方法的主体能使用then回调
				return new Promise(resolve => {
					dom.getComponentRect(this.$refs[el][0], res => {
						resolve(res.size)
					})
				})
				// #endif
			},
			// 点击某一个日期
			clickHandler(index1, index2, item) {
				if (this.readonly) {
					return;
				}
				this.item = item
				const date = dayjs(item.date).format("YYYY-MM-DD")
				if (item.disabled) return
				// 对上一次选择的日期数组进行深度克隆
				let selected = uni.$u.deepClone(this.selected)
				if (this.mode === 'single') {
					// 单选情况下，让数组中的元素为当前点击的日期
					selected = [date]
				} else if (this.mode === 'multiple') {
					if (selected.some(item => this.dateSame(item, date))) {
						// 如果点击的日期已在数组中，则进行移除操作，也就是达到反选的效果
						const itemIndex = selected.findIndex(item => item === date)
						selected.splice(itemIndex, 1)
					} else {
						// 如果点击的日期不在数组中，且已有的长度小于总可选长度时，则添加到数组中去
						if (selected.length < this.maxCount) selected.push(date)
					}
				} else {
					// 选择区间形式
					if (selected.length === 0 || selected.length >= 2) {
						// 如果原来就为0或者大于2的长度，则当前点击的日期，就是开始日期
						selected = [date]
					} else if (selected.length === 1) {
						// 如果已经选择了开始日期
						const existsDate = selected[0]
						// 如果当前选择的日期小于上一次选择的日期，则当前的日期定为开始日期
						if (dayjs(date).isBefore(existsDate)) {
							selected = [date]
						} else if (dayjs(date).isAfter(existsDate)) {
							// 当前日期减去最大可选的日期天数，如果大于起始时间，则进行提示
							if(dayjs(dayjs(date).subtract(this.maxRange, 'day')).isAfter(dayjs(selected[0])) && this.showRangePrompt) {
								if(this.rangePrompt) {
									uni.$u.toast(this.rangePrompt)
								} else {
									uni.$u.toast(`选择天数不能超过 ${this.maxRange} 天`)
								}
								return 
							}
							// 如果当前日期大于已有日期，将当前的添加到数组尾部
							selected.push(date)
							const startDate = selected[0]
							const endDate = selected[1]
							const arr = []
							let i = 0
							do {
								// 将开始和结束日期之间的日期添加到数组中
								arr.push(dayjs(startDate).add(i, 'day').format("YYYY-MM-DD"))
								i++
								// 累加的日期小于结束日期时，继续下一次的循环
							} while (dayjs(startDate).add(i, 'day').isBefore(dayjs(endDate)))
							// 为了一次性修改数组，避免computed中多次触发，这里才用arr变量一次性赋值的方式，同时将最后一个日期添加近来
							arr.push(endDate)
							selected = arr
						} else {
							// 选择区间时，只有一个日期的情况下，且不允许选择起止为同一天的话，不允许选择自己
							if (selected[0] === date && !this.allowSameDay) return
							selected.push(date)
						}
					}
				}
				this.setSelected(selected)
			},
			// 设置默认日期
			setDefaultDate() {
				if (!this.defaultDate) {
					// 如果没有设置默认日期，则将当天日期设置为默认选中的日期
					const selected = [dayjs().format("YYYY-MM-DD")]
					return this.setSelected(selected, false)
				}
				let defaultDate = []
				const minDate = this.minDate || dayjs().format("YYYY-MM-DD")
				const maxDate = this.maxDate || dayjs(minDate).add(this.maxMonth - 1, 'month').format("YYYY-MM-DD")
				if (this.mode === 'single') {
					// 单选模式，可以是字符串或数组，Date对象等
					if (!uni.$u.test.array(this.defaultDate)) {
						defaultDate = [dayjs(this.defaultDate).format("YYYY-MM-DD")]
					} else {
						defaultDate = [this.defaultDate[0]]
					}
				} else {
					// 如果为非数组，则不执行
					if (!uni.$u.test.array(this.defaultDate)) return
					defaultDate = this.defaultDate
				}
				// 过滤用户传递的默认数组，取出只在可允许最大值与最小值之间的元素
				defaultDate = defaultDate.filter(item => {
					return dayjs(item).isAfter(dayjs(minDate).subtract(1, 'day')) && dayjs(item).isBefore(dayjs(
						maxDate).add(1, 'day'))
				})
				this.setSelected(defaultDate, false)
			},
			setSelected(selected, event = true) {
				this.selected = selected
				event && this.$emit('monthSelected', this.selected)
			}
		}
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-calendar-month-wrapper {
		margin-top: 4px;
	}

	.u-calendar-month {

		&__title {
			font-size: 14px;
			line-height: 42px;
			height: 42px;
			color: $u-main-color;
			text-align: center;
			font-weight: bold;
		}

		&__days {
			position: relative;
			@include flex;
			flex-wrap: wrap;

			&__month-mark-wrapper {
				position: absolute;
				top: 0;
				bottom: 0;
				left: 0;
				right: 0;
				@include flex;
				justify-content: center;
				align-items: center;

				&__text {
					font-size: 155px;
					color: rgba(231, 232, 234, 0.83);
				}
			}

			&__day {
				@include flex;
				padding: 2px;
				/* #ifndef APP-NVUE */
				// vue下使用css进行宽度计算，因为某些安卓机会无法进行js获取父元素宽度进行计算得出，会有偏移
				width: calc(100% / 7);
				box-sizing: border-box;
				/* #endif */

				&__select {
					flex: 1;
					@include flex;
					align-items: center;
					justify-content: center;
					position: relative;

					&__dot {
						width: 7px;
						height: 7px;
						border-radius: 100px;
						background-color: $u-error;
						position: absolute;
						top: 12px;
						right: 7px;
					}

					&__buttom-info {
						color: $u-content-color;
						text-align: center;
						position: absolute;
						bottom: 5px;
						font-size: 10px;
						text-align: center;
						left: 0;
						right: 0;

						&--selected {
							color: #ffffff;
						}

						&--disabled {
							color: #cacbcd;
						}
					}

					&__info {
						text-align: center;
						font-size: 16px;

						&--selected {
							color: #ffffff;
						}

						&--disabled {
							color: #cacbcd;
						}
					}

					&--selected {
						background-color: $u-primary;
						@include flex;
						justify-content: center;
						align-items: center;
						flex: 1;
						border-radius: 3px;
					}

					&--range-selected {
						opacity: 0.3;
						border-radius: 0;
					}

					&--range-start-selected {
						border-top-right-radius: 0;
						border-bottom-right-radius: 0;
					}

					&--range-end-selected {
						border-top-left-radius: 0;
						border-bottom-left-radius: 0;
					}
				}
			}
		}
	}
</style>
