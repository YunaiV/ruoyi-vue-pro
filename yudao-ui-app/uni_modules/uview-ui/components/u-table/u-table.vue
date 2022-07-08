<template>
	<view class="u-table">
		
	</view>
</template>

<script>
	import props from './props.js';
	/**
	 * Table 表格 
	 * @description 表格组件一般用于展示大量结构化数据的场景 本组件标签类似HTML的table表格，由table、tr、th、td四个组件组成
	 * @tutorial https://www.uviewui.com/components/table.html
	 * @example <u-table><u-tr><u-th>学校</u-th </u-tr> <u-tr><u-td>浙江大学</u-td> </u-tr> <u-tr><u-td>清华大学</u-td> </u-tr></u-table>
	 */
	export default {
		name: 'u-table',
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		data() {
			return {
				
			}
		}
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";
	
</style>
