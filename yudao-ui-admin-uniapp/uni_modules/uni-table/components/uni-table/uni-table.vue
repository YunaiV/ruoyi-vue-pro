<template>
	<view class="uni-table-scroll" :class="{ 'table--border': border, 'border-none': !noData }">
		<!-- #ifdef H5 -->
		<table class="uni-table" border="0" cellpadding="0" cellspacing="0" :class="{ 'table--stripe': stripe }" :style="{ 'min-width': minWidth + 'px' }">
			<slot></slot>
			<view v-if="noData" class="uni-table-loading">
				<view class="uni-table-text" :class="{ 'empty-border': border }">{{ emptyText }}</view>
			</view>
			<view v-if="loading" class="uni-table-mask" :class="{ 'empty-border': border }"><div class="uni-table--loader"></div></view>
		</table>
		<!-- #endif -->
		<!-- #ifndef H5 -->
		<view class="uni-table" :style="{ 'min-width': minWidth + 'px' }" :class="{ 'table--stripe': stripe }">
			<slot></slot>
			<view v-if="noData" class="uni-table-loading">
				<view class="uni-table-text" :class="{ 'empty-border': border }">{{ emptyText }}</view>
			</view>
			<view v-if="loading" class="uni-table-mask" :class="{ 'empty-border': border }"><div class="uni-table--loader"></div></view>
		</view>
		<!-- #endif -->
	</view>
</template>

<script>
/**
 * Table 表格
 * @description 用于展示多条结构类似的数据
 * @tutorial https://ext.dcloud.net.cn/plugin?id=3270
 * @property {Boolean} 	border 				是否带有纵向边框
 * @property {Boolean} 	stripe 				是否显示斑马线
 * @property {Boolean} 	type 					是否开启多选
 * @property {String} 	emptyText 			空数据时显示的文本内容
 * @property {Boolean} 	loading 			显示加载中
 * @event {Function} 	selection-change 	开启多选时，当选择项发生变化时会触发该事件
 */
export default {
	name: 'uniTable',
	options: {
		virtualHost: true
	},
	emits:['selection-change'],
	props: {
		data: {
			type: Array,
			default() {
				return []
			}
		},
		// 是否有竖线
		border: {
			type: Boolean,
			default: false
		},
		// 是否显示斑马线
		stripe: {
			type: Boolean,
			default: false
		},
		// 多选
		type: {
			type: String,
			default: ''
		},
		// 没有更多数据
		emptyText: {
			type: String,
			default: '没有更多数据'
		},
		loading: {
			type: Boolean,
			default: false
		},
		rowKey: {
			type: String,
			default: ''
		}
	},
	data() {
		return {
			noData: true,
			minWidth: 0,
			multiTableHeads: []
		}
	},
	watch: {
		loading(val) {},
		data(newVal) {
			let theadChildren = this.theadChildren
			let rowspan = 1
			if (this.theadChildren) {
				rowspan = this.theadChildren.rowspan
			}
			
			// this.trChildren.length - rowspan
			this.noData = false
			// this.noData = newVal.length === 0 
		}
	},
	created() {
		// 定义tr的实例数组
		this.trChildren = []
		this.thChildren = []
		this.theadChildren = null
		this.backData = []
		this.backIndexData = []
	},

	methods: {
		isNodata() {
			let theadChildren = this.theadChildren
			let rowspan = 1
			if (this.theadChildren) {
				rowspan = this.theadChildren.rowspan
			}
			this.noData = this.trChildren.length - rowspan <= 0
		},
		/**
		 * 选中所有
		 */
		selectionAll() {
			let startIndex = 1
			let theadChildren = this.theadChildren
			if (!this.theadChildren) {
				theadChildren = this.trChildren[0]
			} else {
				startIndex = theadChildren.rowspan - 1
			}
			let isHaveData = this.data && this.data.length.length > 0
			theadChildren.checked = true
			theadChildren.indeterminate = false
			this.trChildren.forEach((item, index) => {
				if (!item.disabled) {
					item.checked = true
					if (isHaveData && item.keyValue) {
						const row = this.data.find(v => v[this.rowKey] === item.keyValue)
						if (!this.backData.find(v => v[this.rowKey] === row[this.rowKey])) {
							this.backData.push(row)
						}
					}
					if (index > (startIndex - 1) && this.backIndexData.indexOf(index - startIndex) === -1) {
						this.backIndexData.push(index - startIndex)
					}
				}
			})
			// this.backData = JSON.parse(JSON.stringify(this.data))
			this.$emit('selection-change', {
				detail: {
					value: this.backData,
					index: this.backIndexData
				}
			})
		},
		/**
		 * 用于多选表格，切换某一行的选中状态，如果使用了第二个参数，则是设置这一行选中与否（selected 为 true 则选中）
		 */
		toggleRowSelection(row, selected) {
			// if (!this.theadChildren) return
			row = [].concat(row)

			this.trChildren.forEach((item, index) => {
				// if (item.keyValue) {

				const select = row.findIndex(v => {
					//
					if (typeof v === 'number') {
						return v === index - 1
					} else {
						return v[this.rowKey] === item.keyValue
					}
				})
				let ischeck = item.checked
				if (select !== -1) {
					if (typeof selected === 'boolean') {
						item.checked = selected
					} else {
						item.checked = !item.checked
					}
					if (ischeck !== item.checked) {
						this.check(item.rowData||item, item.checked, item.rowData?item.keyValue:null, true)
					}
				}
				// }
			})
			this.$emit('selection-change', {
				detail: {
					value: this.backData,
					index:this.backIndexData
				}
			})
		},

		/**
		 * 用于多选表格，清空用户的选择
		 */
		clearSelection() {
			let theadChildren = this.theadChildren
			if (!this.theadChildren) {
				theadChildren = this.trChildren[0]
			}
			// if (!this.theadChildren) return
			theadChildren.checked = false
			theadChildren.indeterminate = false
			this.trChildren.forEach(item => {
				// if (item.keyValue) {
					item.checked = false
				// }
			})
			this.backData = []
			this.backIndexData = []
			this.$emit('selection-change', {
				detail: {
					value: [],
					index: []
				}
			})
		},
		/**
		 * 用于多选表格，切换所有行的选中状态
		 */
		toggleAllSelection() {
			let list = []
			let startIndex = 1
			let theadChildren = this.theadChildren
			if (!this.theadChildren) {
				theadChildren = this.trChildren[0]
			} else {
				startIndex = theadChildren.rowspan - 1
			}
			this.trChildren.forEach((item, index) => {
				if (!item.disabled) {
					if (index > (startIndex - 1) ) {
						list.push(index-startIndex)
					}
				}
			})
			this.toggleRowSelection(list)
		},

		/**
		 * 选中\取消选中
		 * @param {Object} child
		 * @param {Object} check
		 * @param {Object} rowValue
		 */
		check(child, check, keyValue, emit) {
			let theadChildren = this.theadChildren
			if (!this.theadChildren) {
				theadChildren = this.trChildren[0]
			}
			
			
			
			let childDomIndex = this.trChildren.findIndex((item, index) => child === item)
			if(childDomIndex < 0){
				childDomIndex = this.data.findIndex(v=>v[this.rowKey] === keyValue) + 1
			}
			const dataLen = this.trChildren.filter(v => !v.disabled && v.keyValue).length
			if (childDomIndex === 0) {
				check ? this.selectionAll() : this.clearSelection()
				return
			}

			if (check) {
				if (keyValue) {
					this.backData.push(child)
				}
				this.backIndexData.push(childDomIndex - 1)
			} else {
				const index = this.backData.findIndex(v => v[this.rowKey] === keyValue)
				const idx = this.backIndexData.findIndex(item => item === childDomIndex - 1)
				if (keyValue) {
					this.backData.splice(index, 1)
				}
				this.backIndexData.splice(idx, 1)
			}

			const domCheckAll = this.trChildren.find((item, index) => index > 0 && !item.checked && !item.disabled)
			if (!domCheckAll) {
				theadChildren.indeterminate = false
				theadChildren.checked = true
			} else {
				theadChildren.indeterminate = true
				theadChildren.checked = false
			}

			if (this.backIndexData.length === 0) {
				theadChildren.indeterminate = false
			}

			if (!emit) {
				this.$emit('selection-change', {
					detail: {
						value: this.backData,
						index: this.backIndexData
					}
				})
			}
		}
	}
}
</script>

<style lang="scss">
$border-color: #ebeef5;

.uni-table-scroll {
	width: 100%;
	/* #ifndef APP-NVUE */
	overflow-x: auto;
	/* #endif */
}

.uni-table {
	position: relative;
	width: 100%;
	border-radius: 5px;
	// box-shadow: 0px 0px 3px 1px rgba(0, 0, 0, 0.1);
	background-color: #fff;
	/* #ifndef APP-NVUE */
	box-sizing: border-box;
	display: table;
	overflow-x: auto;
	::v-deep .uni-table-tr:nth-child(n + 2) {
		&:hover {
			background-color: #f5f7fa;
		}
	}
	::v-deep .uni-table-thead {
		.uni-table-tr {
			// background-color: #f5f7fa;
			&:hover {
				background-color:#fafafa;
			}
		}
	}
	/* #endif */
}

.table--border {
	border: 1px $border-color solid;
	border-right: none;
}

.border-none {
	/* #ifndef APP-NVUE */
	border-bottom: none;
	/* #endif */
}

.table--stripe {
	/* #ifndef APP-NVUE */
	::v-deep .uni-table-tr:nth-child(2n + 3) {
		background-color: #fafafa;
	}
	/* #endif */
}

/* 表格加载、无数据样式 */
.uni-table-loading {
	position: relative;
	/* #ifndef APP-NVUE */
	display: table-row;
	/* #endif */
	height: 50px;
	line-height: 50px;
	overflow: hidden;
	box-sizing: border-box;
}
.empty-border {
	border-right: 1px $border-color solid;
}
.uni-table-text {
	position: absolute;
	right: 0;
	left: 0;
	text-align: center;
	font-size: 14px;
	color: #999;
}

.uni-table-mask {
	position: absolute;
	top: 0;
	bottom: 0;
	left: 0;
	right: 0;
	background-color: rgba(255, 255, 255, 0.8);
	z-index: 99;
	/* #ifndef APP-NVUE */
	display: flex;
	margin: auto;
	transition: all 0.5s;
	/* #endif */
	justify-content: center;
	align-items: center;
}

.uni-table--loader {
	width: 30px;
	height: 30px;
	border: 2px solid #aaa;
	// border-bottom-color: transparent;
	border-radius: 50%;
	/* #ifndef APP-NVUE */
	animation: 2s uni-table--loader linear infinite;
	/* #endif */
	position: relative;
}

@keyframes uni-table--loader {
	0% {
		transform: rotate(360deg);
	}

	10% {
		border-left-color: transparent;
	}

	20% {
		border-bottom-color: transparent;
	}

	30% {
		border-right-color: transparent;
	}

	40% {
		border-top-color: transparent;
	}

	50% {
		transform: rotate(0deg);
	}

	60% {
		border-top-color: transparent;
	}

	70% {
		border-left-color: transparent;
	}

	80% {
		border-bottom-color: transparent;
	}

	90% {
		border-right-color: transparent;
	}

	100% {
		transform: rotate(-360deg);
	}
}
</style>
