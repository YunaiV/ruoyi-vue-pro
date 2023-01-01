<template>
	<!-- #ifdef APP-NVUE -->
	<cell>
		<!-- #endif -->
		<view
			class="u-list-item"
			:ref="`u-list-item-${anchor}`"
			:anchor="`u-list-item-${anchor}`"
			:class="[`u-list-item-${anchor}`]"
		>
			<slot />
		</view>
		<!-- #ifdef APP-NVUE -->
	</cell>
	<!-- #endif -->
</template>

<script>
	import props from './props.js';
	// #ifdef APP-NVUE
	const dom = uni.requireNativePlugin('dom')
	// #endif
	/**
	 * List 列表
	 * @description 该组件为高性能列表组件
	 * @tutorial https://www.uviewui.com/components/list.html
	 * @property {String | Number}	anchor	用于滚动到指定item
	 * @example <u-list-ite v-for="(item, index) in indexList" :key="index" ></u-list-item>
	 */
	export default {
		name: 'u-list-item',
		mixins: [uni.$u.mpMixin, uni.$u.mixin,props],
		data() {
			return {
				// 节点信息
				rect: {},
				index: 0,
				show: true,
				sys: uni.$u.sys()
			}
		},
		computed: {

		},
		inject: ['uList'],
		watch: {
			// #ifndef APP-NVUE
			'uList.innerScrollTop'(n) {
				const preLoadScreen = this.uList.preLoadScreen
				const windowHeight = this.sys.windowHeight
				if(n <= windowHeight * preLoadScreen) {
					this.parent.updateOffsetFromChild(0)
				} else if (this.rect.top <= n - windowHeight * preLoadScreen) {
					this.parent.updateOffsetFromChild(this.rect.top)
				}
			}
			// #endif
		},
		created() {
			this.parent = {}
		},
		mounted() {
			this.init()
		},
		methods: {
			init() {
				// 初始化数据
				this.updateParentData()
				this.index = this.parent.children.indexOf(this)
				this.resize()
			},
			updateParentData() {
				// 此方法在mixin中
				this.getParentData('u-list')
			},
			resize() {
				this.queryRect(`u-list-item-${this.anchor}`).then(size => {
					const lastChild = this.parent.children[this.index - 1]
					this.rect = size
					const preLoadScreen = this.uList.preLoadScreen
					const windowHeight = this.sys.windowHeight
					// #ifndef APP-NVUE
					if (lastChild) {
						this.rect.top = lastChild.rect.top + lastChild.rect.height
					}
					if (size.top >= this.uList.innerScrollTop + (1 + preLoadScreen) * windowHeight) this.show =
						false
					// #endif
				})
			},
			// 查询元素尺寸
			queryRect(el) {
				return new Promise(resolve => {
					// #ifndef APP-NVUE
					this.$uGetRect(`.${el}`).then(size => {
						resolve(size)
					})
					// #endif

					// #ifdef APP-NVUE
					const ref = this.$refs[el]
					dom.getComponentRect(ref, res => {
						resolve(res.size)
					})
					// #endif
				})
			}
		}
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-list-item {}
</style>
