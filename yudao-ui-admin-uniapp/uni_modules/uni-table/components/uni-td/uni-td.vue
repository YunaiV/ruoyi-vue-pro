<template>
	<!-- #ifdef H5 -->
	<td class="uni-table-td" :rowspan="rowspan" :colspan="colspan" :class="{'table--border':border}" :style="{width:width + 'px','text-align':align}">
		<slot></slot>
	</td>
	<!-- #endif -->
	<!-- #ifndef H5 -->
	<!-- :class="{'table--border':border}"  -->
	<view class="uni-table-td" :class="{'table--border':border}" :style="{width:width + 'px','text-align':align}">
		<slot></slot>
	</view>
	<!-- #endif -->
	
</template>

<script>
	/**
	 * Td 单元格
	 * @description 表格中的标准单元格组件
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=3270
	 * @property {Number} 	align = [left|center|right]	单元格对齐方式
	 */
	export default {
		name: 'uniTd',
		options: {
			virtualHost: true
		},
		props: {
			width: {
				type: [String, Number],
				default: ''
			},
			align: {
				type: String,
				default: 'left'
			},
			rowspan: {
				type: [Number,String],
				default: 1
			},
			colspan: {
					type: [Number,String],
				default: 1
			}
		},
		data() {
			return {
				border: false
			};
		},
		created() {
			this.root = this.getTable()
			this.border = this.root.border
		},
		methods: {
			/**
			 * 获取父元素实例
			 */
			getTable() {
				let parent = this.$parent;
				let parentName = parent.$options.name;
				while (parentName !== 'uniTable') {
					parent = parent.$parent;
					if (!parent) return false;
					parentName = parent.$options.name;
				}
				return parent;
			},
		}
	}
</script>

<style lang="scss">
	$border-color:#EBEEF5;

	.uni-table-td {
		display: table-cell;
		padding: 8px 10px;
		font-size: 14px;
		border-bottom: 1px $border-color solid;
		font-weight: 400;
		color: #606266;
		line-height: 23px;
		box-sizing: border-box;
	}

	.table--border {
		border-right: 1px $border-color solid;
	}
</style>
