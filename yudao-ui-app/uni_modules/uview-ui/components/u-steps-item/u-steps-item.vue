<template>
	<view class="u-steps-item" ref="u-steps-item" :class="[`u-steps-item--${parentData.direction}`]">
		<view class="u-steps-item__line" v-if="index + 1 < childLength"
			:class="[`u-steps-item__line--${parentData.direction}`]" :style="[lineStyle]"></view>
		<view class="u-steps-item__wrapper"
			:class="[`u-steps-item__wrapper--${parentData.direction}`, parentData.dot && `u-steps-item__wrapper--${parentData.direction}--dot`]">
			<slot name="icon">
				<view class="u-steps-item__wrapper__dot" v-if="parentData.dot" :style="{
						backgroundColor: statusColor
					}">

				</view>
				<view class="u-steps-item__wrapper__icon" v-else-if="parentData.activeIcon || parentData.inactiveIcon">
					<u-icon :name="index <= parentData.current ? parentData.activeIcon : parentData.inactiveIcon"
						:size="iconSize"
						:color="index <= parentData.current ? parentData.activeColor : parentData.inactiveColor">
					</u-icon>
				</view>
				<view v-else :style="{
						backgroundColor: statusClass === 'process' ? parentData.activeColor : 'transparent',
						borderColor: statusColor
					}" class="u-steps-item__wrapper__circle">
					<text v-if="statusClass === 'process' || statusClass === 'wait'"
						class="u-steps-item__wrapper__circle__text" :style="{
							color: index == parentData.current ? '#ffffff' : parentData.inactiveColor
						}">{{ index + 1}}</text>
					<u-icon v-else :color="statusClass === 'error' ? 'error' : parentData.activeColor" size="12"
						:name="statusClass === 'error' ? 'close' : 'checkmark'"></u-icon>
				</view>
			</slot>
		</view>
		<view class="u-steps-item__content" :class="[`u-steps-item__content--${parentData.direction}`]"
			:style="[contentStyle]">
			<u--text :text="title" :type="parentData.current == index ? 'main' : 'content'" lineHeight="20px"
				:size="parentData.current == index ? 14 : 13"></u--text>
			<slot name="desc">
				<u--text :text="desc" type="tips" size="12"></u--text>
			</slot>
		</view>
		<!-- <view
		    class="u-steps-item__line"
		    v-if="showLine && parentData.direction === 'column'"
			:class="[`u-steps-item__line--${parentData.direction}`]"
		    :style="[lineStyle]"
		></view> -->
	</view>
</template>

<script>
	import props from './props.js';
	// #ifdef APP-NVUE
	const dom = uni.requireNativePlugin('dom')
	// #endif
	/**
	 * StepsItem 步骤条的子组件
	 * @description 本组件需要和u-steps配合使用
	 * @tutorial https://uviewui.com/components/steps.html
	 * @property {String}			title			标题文字
	 * @property {String}			current			描述文本
	 * @property {String | Number}	iconSize		图标大小  (默认 17 )
	 * @property {Boolean}			error			当前步骤是否处于失败状态  (默认 false )
	 * @example <u-steps current="0"><u-steps-item title="已出库" desc="10:35" ></u-steps-item></u-steps>
	 */
	export default {
		name: 'u-steps-item',
		mixins: [uni.$u.mpMixin, uni.$u.mixin, props],
		data() {
			return {
				index: 0,
				childLength: 0,
				showLine: false,
				size: {
					height: 0,
					width: 0
				},
				parentData: {
					direction: 'row',
					current: 0,
					activeColor: '',
					inactiveColor: '',
					activeIcon: '',
					inactiveIcon: '',
					dot: false
				}
			}
		},
		watch: {
			'parentData'(newValue, oldValue) {
			}
		},
		created() {
			this.init()
		},
		computed: {
			lineStyle() {
				const style = {}
				if (this.parentData.direction === 'row') {
					style.width = this.size.width + 'px'
					style.left = this.size.width / 2 + 'px'
				} else {
					style.height = this.size.height + 'px'
					// style.top = this.size.height / 2 + 'px'
				}
				style.backgroundColor = this.parent.children?.[this.index + 1]?.error ? uni.$u.color.error : this.index <
					this
					.parentData
					.current ? this.parentData.activeColor : this.parentData.inactiveColor
				return style
			},
			statusClass() {
				const {
					index,
					error
				} = this
				const {
					current
				} = this.parentData
				if (current == index) {
					return error === true ? 'error' : 'process'
				} else if (error) {
					return 'error'
				} else if (current > index) {
					return 'finish'
				} else {
					return 'wait'
				}
			},
			statusColor() {
				let color = ''
				switch (this.statusClass) {
					case 'finish':
						color = this.parentData.activeColor
						break
					case 'error':
						color = uni.$u.color.error
						break
					case 'process':
						color = this.parentData.dot ? this.parentData.activeColor : 'transparent'
						break
					default:
						color = this.parentData.inactiveColor
						break
				}
				return color
			},
			contentStyle() {
				const style = {}
				if (this.parentData.direction === 'column') {
					style.marginLeft = this.parentData.dot ? '2px' : '6px'
					style.marginTop = this.parentData.dot ? '0px' : '6px'
				} else {
					style.marginTop = this.parentData.dot ? '2px' : '6px'
					style.marginLeft = this.parentData.dot ? '2px' : '6px'
				}

				return style
			}
		},
		mounted() {
			this.parent && this.parent.updateFromChild()
			uni.$u.sleep().then(() => {
				this.getStepsItemRect()
			})
		},
		methods: {
			init() {
				// 初始化数据
				this.updateParentData()
				if (!this.parent) {
					return uni.$u.error('u-steps-item必须要搭配u-steps组件使用')
				}
				this.index = this.parent.children.indexOf(this)
				this.childLength = this.parent.children.length
			},
			updateParentData() {
				// 此方法在mixin中
				this.getParentData('u-steps')
			},
			// 父组件数据发生变化
			updateFromParent() {
				this.init()
			},
			// 获取组件的尺寸，用于设置横线的位置
			getStepsItemRect() {
				// #ifndef APP-NVUE
				this.$uGetRect('.u-steps-item').then(size => {
					this.size = size
				})
				// #endif

				// #ifdef APP-NVUE
				dom.getComponentRect(this.$refs['u-steps-item'], res => {
					const {
						size
					} = res
					this.size = size
				})
				// #endif
			}
		}
	}
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-steps-item {
		flex: 1;
		@include flex;

		&--row {
			flex-direction: column;
			align-items: center;
			position: relative;
		}

		&--column {
			position: relative;
			flex-direction: row;
			justify-content: flex-start;
			padding-bottom: 5px;
		}

		&__wrapper {
			@include flex;
			justify-content: center;
			align-items: center;
			position: relative;
			background-color: #fff;

			&--column {
				width: 20px;
				height: 32px;

				&--dot {
					height: 20px;
					width: 20px;
				}
			}

			&--row {
				width: 32px;
				height: 20px;

				&--dot {
					width: 20px;
					height: 20px;
				}
			}

			&__circle {
				width: 20px;
				height: 20px;
				/* #ifndef APP-NVUE */
				box-sizing: border-box;
				flex-shrink: 0;
				/* #endif */
				border-radius: 100px;
				border-width: 1px;
				border-color: $u-tips-color;
				border-style: solid;
				@include flex(row);
				align-items: center;
				justify-content: center;
				transition: background-color 0.3s;

				&__text {
					color: $u-tips-color;
					font-size: 11px;
					@include flex(row);
					align-items: center;
					justify-content: center;
					text-align: center;
					line-height: 11px;
				}
			}

			&__dot {
				width: 10px;
				height: 10px;
				border-radius: 100px;
				background-color: $u-content-color;
			}
		}

		&__content {
			@include flex;
			flex: 1;

			&--row {
				flex-direction: column;
				align-items: center;
			}

			&--column {
				flex-direction: column;
				margin-left: 6px;
			}
		}

		&__line {
			position: absolute;
			background: $u-tips-color;

			&--row {
				top: 10px;
				height: 1px;
			}

			&--column {
				width: 1px;
				left: 10px;
			}
		}
	}
</style>
