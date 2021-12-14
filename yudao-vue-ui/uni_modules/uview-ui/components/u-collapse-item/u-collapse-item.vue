<template>
	<view class="u-collapse-item">
		<u-cell
			:title="title"
			:value="value"
			:label="label"
			:icon="icon"
			:isLink="isLink"
			:clickable="clickable"
			:border="parentData.border && showBorder"
			@click="clickHandler"
			:arrowDirection="expanded ? 'up' : 'down'"
			:disabled="disabled"
		>
			<!-- #ifndef MP-WEIXIN -->
			<!-- 微信小程序不支持，因为微信中不支持 <slot name="title" slot="title" />的写法 -->
			<slot
				name="title"
				slot="title"
			/>
			<slot
				name="icon"
				slot="icon"
			/>
			<slot
				name="value"
				slot="value"
			/>
			<slot
				name="right-icon"
				slot="right-icon"
			/>
			<!-- #endif -->
		</u-cell>
		<view
			class="u-collapse-item__content"
			:animation="animationData"
			ref="animation"
		>
			<view
				class="u-collapse-item__content__text content-class"
				:id="elId"
				:ref="elId"
			><slot /></view>
		</view>
		<u-line v-if="parentData.border"></u-line>
	</view>
</template>

<script>
	import props from './props.js';
	// #ifdef APP-NVUE
	const animation = uni.requireNativePlugin('animation')
	const dom = uni.requireNativePlugin('dom')
	// #endif
	/**
	 * collapseItem 折叠面板Item
	 * @description 通过折叠面板收纳内容区域（搭配u-collapse使用）
	 * @tutorial https://www.uviewui.com/components/collapse.html
	 * @property {String}			title 		标题
	 * @property {String}			value 		标题右侧内容
	 * @property {String}			label 		标题下方的描述信息
	 * @property {Boolean}			disbled 	是否禁用折叠面板 ( 默认 false )
	 * @property {Boolean}			isLink 		是否展示右侧箭头并开启点击反馈 ( 默认 true )
	 * @property {Boolean}			clickable	是否开启点击反馈 ( 默认 true )
	 * @property {Boolean}			border		是否显示内边框 ( 默认 true )
	 * @property {String}			align		标题的对齐方式 ( 默认 'left' )
	 * @property {String | Number}	name		唯一标识符
	 * @property {String}			icon		标题左侧图片，可为绝对路径的图片或内置图标
	 * @event {Function}			change 			某个item被打开或者收起时触发
	 * @example <u-collapse-item :title="item.head" v-for="(item, index) in itemList" :key="index">{{item.body}}</u-collapse-item>
	 */
	export default {
		name: "u-collapse-item",
		mixins: [uni.$u.mpMixin, uni.$u.mixin, props],
		data() {
			return {
				elId: uni.$u.guid(),
				// uni.createAnimation的导出数据
				animationData: {},
				// 是否展开状态
				expanded: false,
				// 根据expanded确定是否显示border，为了控制展开时，cell的下划线更好的显示效果，进行一定时间的延时
				showBorder: false,
				// 是否动画中，如果是则不允许继续触发点击
				animating: false,
				// 父组件u-collapse的参数
				parentData: {
					accordion: false,
					border: false
				}
			};
		},
		watch: {
			expanded(n) {
				clearTimeout(this.timer)
				this.timer = null
				// 这里根据expanded的值来进行一定的延时，是为了cell的下划线更好的显示效果
				this.timer = setTimeout(() => {
					this.showBorder = n
				}, n ? 10 : 290)
			}
		},
		mounted() {
			this.init()
		},
		methods: {
			// 异步获取内容，或者动态修改了内容时，需要重新初始化
			init() {
				// 初始化数据
				this.updateParentData()
				if (!this.parent) {
					return uni.$u.error('u-collapse-item必须要搭配u-collapse组件使用')
				}
				const {
					value,
					accordion,
					children = []
				} = this.parent

				if (accordion) {
					if (uni.$u.test.array(value)) {
						return uni.$u.error('手风琴模式下，u-collapse组件的value参数不能为数组')
					}
					this.expanded = this.name == value
				} else {
					if (!uni.$u.test.array(value) && value !== null) {
						return uni.$u.error('非手风琴模式下，u-collapse组件的value参数必须为数组')
					}
					this.expanded = (value || []).some(item => item == this.name)
				}
				// 设置组件的展开或收起状态
				this.$nextTick(function() {
					this.setContentAnimate()
				})
			},
			updateParentData() {
				// 此方法在mixin中
				this.getParentData('u-collapse')
			},
			async setContentAnimate() {
				// 每次面板打开或者收起时，都查询元素尺寸
				// 好处是，父组件从服务端获取内容后，变更折叠面板后可以获得最新的高度
				const rect = await this.queryRect()
				const height = this.expanded ? rect.height : 0
				this.animating = true
				// #ifdef APP-NVUE
				const ref = this.$refs['animation'].ref
				animation.transition(ref, {
					styles: {
						height: height + 'px'
					},
					duration: this.duration,
					// 必须设置为true，否则会到面板收起或展开时，页面其他元素不会随之调整它们的布局
					needLayout: true,
					timingFunction: 'ease-in-out',
				}, () => {
					this.animating = false
				})
				// #endif

				// #ifndef APP-NVUE
				const animation = uni.createAnimation({
					timingFunction: 'ease-in-out',
				});
				animation
					.height(height)
					.step({
						duration: this.duration,
					})
					.step()
				// 导出动画数据给面板的animationData值
				this.animationData = animation.export()
				// 标识动画结束
				uni.$u.sleep(this.duration).then(() => {
					this.animating = false
				})
				// #endif
			},
			// 点击collapsehead头部
			clickHandler() {
				if (this.disabled && this.animating) return
				// 设置本组件为相反的状态
				this.parent && this.parent.onChange(this)
			},
			// 查询内容高度
			queryRect() {
				// #ifndef APP-NVUE
				// $uGetRect为uView自带的节点查询简化方法，详见文档介绍：https://www.uviewui.com/js/getRect.html
				// 组件内部一般用this.$uGetRect，对外的为this.$u.getRect，二者功能一致，名称不同
				return new Promise(resolve => {
					this.$uGetRect(`#${this.elId}`).then(size => {
						resolve(size)
					})
				})
				// #endif

				// #ifdef APP-NVUE
				// nvue下，使用dom模块查询元素高度
				// 返回一个promise，让调用此方法的主体能使用then回调
				return new Promise(resolve => {
					dom.getComponentRect(this.$refs[this.elId], res => {
						resolve(res.size)
					})
				})
				// #endif
			}
		},
	};
</script>

<style lang="scss" scoped>
	@import "../../libs/css/components.scss";

	.u-collapse-item {

		&__content {
			overflow: hidden;
			height: 0;

			&__text {
				padding: 12px 15px;
				color: $u-content-color;
				font-size: 14px;
				line-height: 18px;
			}
		}
	}
</style>
