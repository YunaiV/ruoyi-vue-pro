<template>
	<view class="uni-filter-dropdown">
		<view class="dropdown-btn" @click="onDropdown">
			<view class="icon-select" :class="{active: canReset}" v-if="isSelect || isRange"></view>
			<view class="icon-search" :class="{active: canReset}" v-if="isSearch">
				<view class="icon-search-0"></view>
				<view class="icon-search-1"></view>
			</view>
			<view class="icon-calendar" :class="{active: canReset}" v-if="isDate">
				<view class="icon-calendar-0"></view>
				<view class="icon-calendar-1"></view>
			</view>
		</view>
		<view class="uni-dropdown-cover" v-if="isOpened" @click="handleClose"></view>
		<view class="dropdown-popup dropdown-popup-right" v-if="isOpened" @click.stop>
			<!-- select-->
			<view v-if="isSelect" class="list">
				<label class="flex-r a-i-c list-item" v-for="(item,index) in dataList" :key="index"
					@click="onItemClick($event, index)">
					<check-box class="check" :checked="item.checked" />
					<view class="checklist-content">
						<text class="checklist-text" :style="item.styleIconText">{{item[map.text]}}</text>
					</view>
				</label>
			</view>
			<view v-if="isSelect" class="flex-r opera-area">
				<view class="flex-f btn btn-default" :class="{disable: !canReset}" @click="handleSelectReset">
					{{resource.reset}}</view>
				<view class="flex-f btn btn-submit" @click="handleSelectSubmit">{{resource.submit}}</view>
			</view>
			<!-- search -->
			<view v-if="isSearch" class="search-area">
				<input class="search-input" v-model="filterValue" />
			</view>
			<view v-if="isSearch" class="flex-r opera-area">
				<view class="flex-f btn btn-submit" @click="handleSearchSubmit">{{resource.search}}</view>
				<view class="flex-f btn btn-default" :class="{disable: !canReset}" @click="handleSearchReset">
					{{resource.reset}}</view>
			</view>
			<!-- range -->
			<view v-if="isRange">
				<view class="input-label">{{resource.gt}}</view>
				<input class="input" v-model="gtValue" />
				<view class="input-label">{{resource.lt}}</view>
				<input class="input" v-model="ltValue" />
			</view>
			<view v-if="isRange" class="flex-r opera-area">
				<view class="flex-f btn btn-default" :class="{disable: !canReset}" @click="handleRangeReset">
					{{resource.reset}}</view>
				<view class="flex-f btn btn-submit" @click="handleRangeSubmit">{{resource.submit}}</view>
			</view>
			<!-- date -->
			<view v-if="isDate">
				<uni-datetime-picker ref="datetimepicker" :value="dateRange" type="datetimerange" return-type="timestamp" @change="datetimechange" @maskClick="timepickerclose">
					<view></view>
				</uni-datetime-picker>
			</view>
		</view>
	</view>
</template>

<script>
	import checkBox from '../uni-tr/table-checkbox.vue'

	const resource = {
		"reset": "重置",
		"search": "搜索",
		"submit": "确定",
		"filter": "筛选",
		"gt": "大于等于",
		"lt": "小于等于",
		"date": "日期范围"
	}

	const DropdownType = {
		Select: "select",
		Search: "search",
		Range: "range",
		Date: "date",
		Timestamp: "timestamp"
	}

	export default {
		name: 'FilterDropdown',
		emits:['change'],
		components: {
			checkBox
		},
		options: {
			virtualHost: true
		},
		props: {
			filterType: {
				type: String,
				default: DropdownType.Select
			},
			filterData: {
				type: Array,
				default () {
					return []
				}
			},
			mode: {
				type: String,
				default: 'default'
			},
			map: {
				type: Object,
				default () {
					return {
						text: 'text',
						value: 'value'
					}
				}
			}
		},
		computed: {
			canReset() {
				if (this.isSearch) {
					return this.filterValue.length > 0
				}
				if (this.isSelect) {
					return this.checkedValues.length > 0
				}
				if (this.isRange) {
					return (this.gtValue.length > 0 && this.ltValue.length > 0)
				}
				if (this.isDate) {
					return this.dateSelect.length > 0
				}
				return false
			},
			isSelect() {
				return this.filterType === DropdownType.Select
			},
			isSearch() {
				return this.filterType === DropdownType.Search
			},
			isRange() {
				return this.filterType === DropdownType.Range
			},
			isDate() {
				return (this.filterType === DropdownType.Date || this.filterType === DropdownType.Timestamp)
			}
		},
		watch: {
			filterData(newVal) {
				this._copyFilters()
			},
			indeterminate(newVal) {
				this.isIndeterminate = newVal
			}
		},
		data() {
			return {
				resource,
				enabled: true,
				isOpened: false,
				dataList: [],
				filterValue: '',
				checkedValues: [],
				gtValue: '',
				ltValue: '',
				dateRange: [],
				dateSelect: []
			};
		},
		created() {
			this._copyFilters()
		},
		methods: {
			_copyFilters() {
				let dl = JSON.parse(JSON.stringify(this.filterData))
				for (let i = 0; i < dl.length; i++) {
					if (dl[i].checked === undefined) {
						dl[i].checked = false
					}
				}
				this.dataList = dl
			},
			openPopup() {
				this.isOpened = true
				if (this.isDate) {
					this.$nextTick(() => {
						if (!this.dateRange.length) {
							this.resetDate()
						}
						this.$refs.datetimepicker.show()
					})
				}
			},
			closePopup() {
				this.isOpened = false
			},
			handleClose(e) {
				this.closePopup()
			},
			resetDate() {
				let date = new Date()
				let dateText = date.toISOString().split('T')[0]
				this.dateRange = [dateText + ' 0:00:00', dateText + ' 23:59:59']
			},
			onDropdown(e) {
				this.openPopup()
			},
			onItemClick(e, index) {
				let items = this.dataList
				let listItem = items[index]
				if (listItem.checked === undefined) {
					items[index].checked = true
				} else {
					items[index].checked = !listItem.checked
				}

				let checkvalues = []
				for (let i = 0; i < items.length; i++) {
					const item = items[i]
					if (item.checked) {
						checkvalues.push(item.value)
					}
				}
				this.checkedValues = checkvalues
			},
			datetimechange(e) {
				this.closePopup()
				this.dateRange = e
				this.dateSelect = e
				this.$emit('change', {
					filterType: this.filterType,
					filter: e
				})
			},
			timepickerclose(e) {
				this.closePopup()
			},
			handleSelectSubmit() {
				this.closePopup()
				this.$emit('change', {
					filterType: this.filterType,
					filter: this.checkedValues
				})
			},
			handleSelectReset() {
				if (!this.canReset) {
					return;
				}
				var items = this.dataList
				for (let i = 0; i < items.length; i++) {
					let item = items[i]
					this.$set(item, 'checked', false)
				}
				this.checkedValues = []
				this.handleSelectSubmit()
			},
			handleSearchSubmit() {
				this.closePopup()
				this.$emit('change', {
					filterType: this.filterType,
					filter: this.filterValue
				})
			},
			handleSearchReset() {
				if (!this.canReset) {
					return;
				}
				this.filterValue = ''
				this.handleSearchSubmit()
			},
			handleRangeSubmit(isReset) {
				this.closePopup()
				this.$emit('change', {
					filterType: this.filterType,
					filter: isReset === true ? [] : [parseInt(this.gtValue), parseInt(this.ltValue)]
				})
			},
			handleRangeReset() {
				if (!this.canReset) {
					return;
				}
				this.gtValue = ''
				this.ltValue = ''
				this.handleRangeSubmit(true)
			}
		}
	}
</script>

<style lang="scss">
	.flex-r {
		display: flex;
		flex-direction: row;
	}

	.flex-f {
		flex: 1;
	}

	.a-i-c {
		align-items: center;
	}

	.j-c-c {
		justify-content: center;
	}

	.icon-select {
		width: 14px;
		height: 16px;
		border: solid 6px transparent;
		border-top: solid 6px #ddd;
		border-bottom: none;
		background-color: #ddd;
		background-clip: content-box;
		box-sizing: border-box;
	}

	.icon-select.active {
		background-color: #1890ff;
		border-top-color: #1890ff;
	}

	.icon-search {
		width: 12px;
		height: 16px;
		position: relative;
	}

	.icon-search-0 {
		border: 2px solid #ddd;
		border-radius: 8px;
		width: 7px;
		height: 7px;
	}

	.icon-search-1 {
		position: absolute;
		top: 8px;
		right: 0;
		width: 1px;
		height: 7px;
		background-color: #ddd;
		transform: rotate(-45deg);
	}

	.icon-search.active .icon-search-0 {
		border-color: #1890ff;
	}

	.icon-search.active .icon-search-1 {
		background-color: #1890ff;
	}

	.icon-calendar {
		color: #ddd;
		width: 14px;
		height: 16px;
	}

	.icon-calendar-0 {
		height: 4px;
		margin-top: 3px;
		margin-bottom: 1px;
		background-color: #ddd;
		border-radius: 2px 2px 1px 1px;
		position: relative;
	}
	.icon-calendar-0:before, .icon-calendar-0:after {
		content: '';
		position: absolute;
		top: -3px;
		width: 4px;
		height: 3px;
		border-radius: 1px;
		background-color: #ddd;
	}
	.icon-calendar-0:before {
		left: 2px;
	}
	.icon-calendar-0:after {
		right: 2px;
	}

	.icon-calendar-1 {
		height: 9px;
		background-color: #ddd;
		border-radius: 1px 1px 2px 2px;
	}

	.icon-calendar.active {
		color: #1890ff;
	}

	.icon-calendar.active .icon-calendar-0,
	.icon-calendar.active .icon-calendar-1,
	.icon-calendar.active .icon-calendar-0:before,
	.icon-calendar.active .icon-calendar-0:after {
		background-color: #1890ff;
	}

	.uni-filter-dropdown {
		position: relative;
		font-weight: normal;
	}

	.dropdown-popup {
		position: absolute;
		top: 100%;
		background-color: #fff;
		box-shadow: 0 3px 6px -4px #0000001f, 0 6px 16px #00000014, 0 9px 28px 8px #0000000d;
		min-width: 150px;
		z-index: 1000;
	}

	.dropdown-popup-left {
		left: 0;
	}

	.dropdown-popup-right {
		right: 0;
	}

	.uni-dropdown-cover {
		position: fixed;
		left: 0;
		top: 0;
		right: 0;
		bottom: 0;
		background-color: transparent;
		z-index: 100;
	}

	.list {
		margin-top: 5px;
		margin-bottom: 5px;
	}

	.list-item {
		padding: 5px 10px;
		text-align: left;
	}

	.list-item:hover {
		background-color: #f0f0f0;
	}

	.check {
		margin-right: 5px;
	}

	.search-area {
		padding: 10px;
	}

	.search-input {
		font-size: 12px;
		border: 1px solid #f0f0f0;
		border-radius: 3px;
		padding: 2px 5px;
		min-width: 150px;
		text-align: left;
	}

	.input-label {
		margin: 10px 10px 5px 10px;
		text-align: left;
	}

	.input {
		font-size: 12px;
		border: 1px solid #f0f0f0;
		border-radius: 3px;
		margin: 10px;
		padding: 2px 5px;
		min-width: 150px;
		text-align: left;
	}

	.opera-area {
		cursor: default;
		border-top: 1px solid #ddd;
		padding: 5px;
	}

	.opera-area .btn {
		font-size: 12px;
		border-radius: 3px;
		margin: 5px;
		padding: 4px 4px;
	}

	.btn-default {
		border: 1px solid #ddd;
	}

	.btn-default.disable {
		border-color: transparent;
	}

	.btn-submit {
		background-color: #1890ff;
		color: #ffffff;
	}
</style>
