<template>
	<view class="uni-collapse">
		<slot />
	</view>
</template>
<script>
	/**
	 * Collapse 折叠面板
	 * @description 展示可以折叠 / 展开的内容区域
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=23
	 * @property {String|Array} value 当前激活面板改变时触发(如果是手风琴模式，参数类型为string，否则为array)
	 * @property {Boolean} accordion = [true|false] 是否开启手风琴效果是否开启手风琴效果
	 * @event {Function} change 切换面板时触发，如果是手风琴模式，返回类型为string，否则为array
	 */
	export default {
		name: 'uniCollapse',
		emits:['change','activeItem','input','update:modelValue'],
		props: {
			value: {
				type: [String, Array],
				default: ''
			},
			modelValue: {
				type: [String, Array],
				default: ''
			},
			accordion: {
				// 是否开启手风琴效果
				type: [Boolean, String],
				default: false
			},
		},
		data() {
			return {}
		},
		computed: {
			// TODO 兼容 vue2 和 vue3
			dataValue() {
				let value = (typeof this.value === 'string' && this.value === '') ||
					(Array.isArray(this.value) && this.value.length === 0)
				let modelValue = (typeof this.modelValue === 'string' && this.modelValue === '') ||
					(Array.isArray(this.modelValue) && this.modelValue.length === 0)
				if (value) {
					return this.modelValue
				}
				if (modelValue) {
					return this.value
				}

				return this.value
			}
		},
		watch: {
			dataValue(val) {
				this.setOpen(val)
			}
		},
		created() {
			this.childrens = []
			this.names = []
		},
		mounted() {
			this.$nextTick(()=>{
				this.setOpen(this.dataValue)
			})
		},
		methods: {
			setOpen(val) {
				let str = typeof val === 'string'
				let arr = Array.isArray(val)
				this.childrens.forEach((vm, index) => {
					if (str) {
						if (val === vm.nameSync) {
							if (!this.accordion) {
								console.warn('accordion 属性为 false ,v-model 类型应该为 array')
								return
							}
							vm.isOpen = true
						}
					}
					if (arr) {
						val.forEach(v => {
							if (v === vm.nameSync) {
								if (this.accordion) {
									console.warn('accordion 属性为 true ,v-model 类型应该为 string')
									return
								}
								vm.isOpen = true
							}
						})
					}
				})
				this.emit(val)
			},
			setAccordion(self) {
				if (!this.accordion) return
				this.childrens.forEach((vm, index) => {
					if (self !== vm) {
						vm.isOpen = false
					}
				})
			},
			resize() {
				this.childrens.forEach((vm, index) => {
					// #ifndef APP-NVUE
					vm.getCollapseHeight()
					// #endif
					// #ifdef APP-NVUE
					vm.getNvueHwight()
					// #endif
				})
			},
			onChange(isOpen, self) {
				let activeItem = []

				if (this.accordion) {
					activeItem = isOpen ? self.nameSync : ''
				} else {
					this.childrens.forEach((vm, index) => {
						if (vm.isOpen) {
							activeItem.push(vm.nameSync)
						}
					})
				}
				this.$emit('change', activeItem)
				this.emit(activeItem)
			},
			emit(val){
				this.$emit('input', val)
				this.$emit('update:modelValue', val)
			}
		}
	}
</script>
<style lang="scss" >
	.uni-collapse {
		/* #ifndef APP-NVUE */
		width: 100%;
		display: flex;
		/* #endif */
		/* #ifdef APP-NVUE */
		flex: 1;
		/* #endif */
		flex-direction: column;
		background-color: #fff;
	}
</style>
