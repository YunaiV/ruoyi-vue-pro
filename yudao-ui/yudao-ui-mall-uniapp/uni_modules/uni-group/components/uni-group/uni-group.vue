<template>
	<view class="uni-group" :class="['uni-group--'+mode ,margin?'group-margin':'']" :style="{marginTop: `${top}px` }">
		<slot name="title">
			<view v-if="title" class="uni-group__title" :style="{'padding-left':border?'30px':'15px'}">
				<text class="uni-group__title-text">{{ title }}</text>
			</view>
		</slot>
		<view class="uni-group__content" :class="{'group-conent-padding':border}">
			<slot />
		</view>
	</view>
</template>

<script>
	/**
	 * Group 分组
	 * @description 表单字段分组
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=3281
	 * @property {String} title 主标题
	 * @property {Number} top 分组间隔
	 * @property {Number} mode 模式
	 */
	export default {
		name: 'uniGroup',
		emits:['click'],
		props: {
			title: {
				type: String,
				default: ''
			},
			top: {
				type: [Number, String],
				default: 10
			},
			mode: {
				type: String,
				default: 'default'
			},
			stat:{
				type: Boolean,
				default: false
			}
		},
		data() {
			return {
				margin: false,
				border: false
			}
		},
		watch: {
			title(newVal) {
				if (uni.report && this.stat && newVal !== '') {
					uni.report('title', newVal)
				}
			}
		},
		created() {
			this.form = this.getForm()
			if (this.form) {
				this.margin = true
				this.border = this.form.border
			}
		},
		methods: {
			/**
			 * 获取父元素实例
			 */
			getForm() {
				let parent = this.$parent;
				let parentName = parent.$options.name;
				while (parentName !== 'uniForms') {
					parent = parent.$parent;
					if (!parent) return false
					parentName = parent.$options.name;
				}
				return parent;
			},
			onClick() {
				this.$emit('click')
			}
		}
	}
</script>
<style lang="scss" >
	.uni-group {
		background: #fff;
		margin-top: 10px;
		// border: 1px red solid;
	}

	.group-margin {
		// margin: 0 -15px;
	}

	.uni-group__title {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row;
		align-items: center;
		padding-left: 15px;
		height: 40px;
		background-color: #eee;
		font-weight: normal;
		color: #666;
	}

	.uni-group__content {
		padding: 15px;
		// padding-bottom: 5px;
		// background-color: #FFF;
	}

	.group-conent-padding {
		padding: 0 15px;
	}

	.uni-group__title-text {
		font-size: 14px;
		color: #666;
	}

	.distraction {
		flex-direction: row;
		align-items: center;
	}

	.uni-group--card {
		margin: 10px;
		border-radius: 5px;
		overflow: hidden;
		box-shadow: 0 0 5px 1px rgba($color: #000000, $alpha: 0.08);
	}
</style>
