<template>
	<!-- #ifdef H5 -->
	<tr class="uni-table-tr">
		<th v-if="selection === 'selection' && ishead" class="checkbox" :class="{ 'tr-table--border': border }">
			<table-checkbox :checked="checked" :indeterminate="indeterminate" :disabled="disabled" @checkboxSelected="checkboxSelected"></table-checkbox>
		</th>
		<slot></slot>
		<!-- <uni-th class="th-fixed">123</uni-th> -->
	</tr>
	<!-- #endif -->
	<!-- #ifndef H5 -->
	<view class="uni-table-tr">
		<view v-if="selection === 'selection' " class="checkbox" :class="{ 'tr-table--border': border }">
			<table-checkbox :checked="checked" :indeterminate="indeterminate" :disabled="disabled" @checkboxSelected="checkboxSelected"></table-checkbox>
		</view>
		<slot></slot>
	</view>
	<!-- #endif -->
</template>

<script>
	import tableCheckbox from './table-checkbox.vue'
/**
 * Tr 表格行组件
 * @description 表格行组件 仅包含 th,td 组件
 * @tutorial https://ext.dcloud.net.cn/plugin?id=
 */
export default {
	name: 'uniTr',
	components: { tableCheckbox },
	props: {
		disabled: {
			type: Boolean,
			default: false
		},
		keyValue: {
			type: [String, Number],
			default: ''
		}
	},
	options: {
		virtualHost: true
	},
	data() {
		return {
			value: false,
			border: false,
			selection: false,
			widthThArr: [],
			ishead: true,
			checked: false,
			indeterminate:false
		}
	},
	created() {
		this.root = this.getTable()
		this.head = this.getTable('uniThead')
		if (this.head) {
			this.ishead = false
			this.head.init(this)
		}
		this.border = this.root.border
		this.selection = this.root.type
		this.root.trChildren.push(this)
		const rowData = this.root.data.find(v => v[this.root.rowKey] === this.keyValue)
		if(rowData){
			this.rowData = rowData
		}
		this.root.isNodata()
	},
	mounted() {
		if (this.widthThArr.length > 0) {
			const selectionWidth = this.selection === 'selection' ? 50 : 0
			this.root.minWidth = this.widthThArr.reduce((a, b) => Number(a) + Number(b)) + selectionWidth
		}
	},
	// #ifndef VUE3
	destroyed() {
		const index = this.root.trChildren.findIndex(i => i === this)
		this.root.trChildren.splice(index, 1)
		this.root.isNodata()
	},
	// #endif
	// #ifdef VUE3
	unmounted() {
		const index = this.root.trChildren.findIndex(i => i === this)
		this.root.trChildren.splice(index, 1)
		this.root.isNodata()
	},
	// #endif
	methods: {
		minWidthUpdate(width) {
			this.widthThArr.push(width)
		},
		// 选中
		checkboxSelected(e) {
			let rootData = this.root.data.find(v => v[this.root.rowKey] === this.keyValue)
			this.checked = e.checked
			this.root.check(rootData||this, e.checked,rootData? this.keyValue:null)
		},
		change(e) {
			this.root.trChildren.forEach(item => {
				if (item === this) {
					this.root.check(this, e.detail.value.length > 0 ? true : false)
				}
			})
		},
		/**
		 * 获取父元素实例
		 */
		getTable(name = 'uniTable') {
			let parent = this.$parent
			let parentName = parent.$options.name
			while (parentName !== name) {
				parent = parent.$parent
				if (!parent) return false
				parentName = parent.$options.name
			}
			return parent
		}
	}
}
</script>

<style lang="scss">
$border-color: #ebeef5;

.uni-table-tr {
	/* #ifndef APP-NVUE */
	display: table-row;
	transition: all 0.3s;
	box-sizing: border-box;
	/* #endif */
}

.checkbox {
	padding: 0 8px;
	width: 26px;
	padding-left: 12px;
	/* #ifndef APP-NVUE */
	display: table-cell;
	vertical-align: middle;
	/* #endif */
	color: #333;
	font-weight: 500;
	border-bottom: 1px $border-color solid;
	font-size: 14px;
	// text-align: center;
}

.tr-table--border {
	border-right: 1px $border-color solid;
}

/* #ifndef APP-NVUE */
.uni-table-tr {
	::v-deep .uni-table-th {
		&.table--border:last-child {
			// border-right: none;
		}
	}

	::v-deep .uni-table-td {
		&.table--border:last-child {
			// border-right: none;
		}
	}
}

/* #endif */
</style>
