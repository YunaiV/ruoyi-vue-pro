<template>
	<!-- #ifdef H5 -->
	<thead class="uni-table-thead">
		<tr class="uni-table-tr">
			<th :rowspan="rowspan" colspan="1" class="checkbox" :class="{ 'tr-table--border': border }">
				<table-checkbox :indeterminate="indeterminate" :checked="checked" @checkboxSelected="checkboxSelected"></table-checkbox>
			</th>
		</tr>
		<slot></slot>
	</thead>
	<!-- #endif -->
	<!-- #ifndef H5 -->
	<view class="uni-table-thead"><slot></slot></view>
	<!-- #endif -->
</template>

<script>
import tableCheckbox from '../uni-tr/table-checkbox.vue'
export default {
	name: 'uniThead',
	components: {
		tableCheckbox
	},
	options: {
		virtualHost: true
	},
	data() {
		return {
			border: false,
			selection: false,
			rowspan: 1,
			indeterminate: false,
			checked: false
		}
	},
	created() {
		this.root = this.getTable()
		// #ifdef H5
		this.root.theadChildren = this
		// #endif
		this.border = this.root.border
		this.selection = this.root.type
	},
	methods: {
		init(self) {
			this.rowspan++
		},
		checkboxSelected(e) {
			this.indeterminate = false
			const backIndexData = this.root.backIndexData
			const data = this.root.trChildren.filter(v => !v.disabled && v.keyValue)
			if (backIndexData.length === data.length) {
				this.checked = false
				this.root.clearSelection()
			} else {
				this.checked = true
				this.root.selectionAll()
			}
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

.uni-table-thead {
	display: table-header-group;
}

.uni-table-tr {
	/* #ifndef APP-NVUE */
	display: table-row;
	transition: all 0.3s;
	box-sizing: border-box;
	/* #endif */
	border: 1px red solid;
	background-color: #fafafa;
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
