<template>
	<view class="uni-grid-wrap">
		<view :id="elId" ref="uni-grid" class="uni-grid" :class="{ 'uni-grid--border': showBorder }" :style="{ 'border-left-color':borderColor}">
			<slot />
		</view>
	</view>
</template>

<script>
	// #ifdef APP-NVUE
	const dom = uni.requireNativePlugin('dom');
	// #endif

	/**
	 * Grid 宫格
	 * @description 宫格组件
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=27
	 * @property {Number} column 每列显示个数
	 * @property {String} borderColor 边框颜色
	 * @property {Boolean} showBorder 是否显示边框
	 * @property {Boolean} square 是否方形显示
	 * @property {Boolean} Boolean 点击背景是否高亮
	 * @event {Function} change 点击 grid 触发，e={detail:{index:0}}，index 为当前点击 gird 下标
	 */
	export default {
		name: 'UniGrid',
		emits:['change'],
		props: {
			// 每列显示个数
			column: {
				type: Number,
				default: 3
			},
			// 是否显示边框
			showBorder: {
				type: Boolean,
				default: true
			},
			// 边框颜色
			borderColor: {
				type: String,
				default: '#D2D2D2'
			},
			// 是否正方形显示,默认为 true
			square: {
				type: Boolean,
				default: true
			},
			highlight: {
				type: Boolean,
				default: true
			}
		},
		provide() {
			return {
				grid: this
			}
		},
		data() {
			const elId = `Uni_${Math.ceil(Math.random() * 10e5).toString(36)}`
			return {
				elId,
				width: 0
			}
		},
		created() {
			this.children = []
		},
		mounted() {
			this.$nextTick(()=>{
				this.init()
			})
		},
		methods: {
			init() {
				setTimeout(() => {
					this._getSize((width) => {
						this.children.forEach((item, index) => {
							item.width = width
						})
					})
				}, 50)
			},
			change(e) {
				this.$emit('change', e)
			},
			_getSize(fn) {
				// #ifndef APP-NVUE
				uni.createSelectorQuery()
					.in(this)
					.select(`#${this.elId}`)
					.boundingClientRect()
					.exec(ret => {
						this.width = parseInt((ret[0].width - 1) / this.column) + 'px'
						fn(this.width)
					})
				// #endif
				// #ifdef APP-NVUE
				dom.getComponentRect(this.$refs['uni-grid'], (ret) => {
					this.width = parseInt((ret.size.width - 1) / this.column) + 'px'
					fn(this.width)
				})
				// #endif
			}
		}
	}
</script>

<style lang="scss" scoped>
	.uni-grid-wrap {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex: 1;
		flex-direction: column;
		/* #ifdef H5 */
		width: 100%;
		/* #endif */
	}

	.uni-grid {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		// flex: 1;
		flex-direction: row;
		flex-wrap: wrap;
	}

	.uni-grid--border {
		position: relative;
		/* #ifdef APP-NVUE */
		border-left-color: #D2D2D2;
		border-left-style: solid;
		border-left-width: 0.5px;
		/* #endif */
		/* #ifndef APP-NVUE */
		z-index: 1;
		border-left: 1px #D2D2D2 solid;
		/* #endif */
	}
</style>
