<template>
	<view class="uni-title__box" :style="{'align-items':textAlign}">
		<text class="uni-title__base" :class="['uni-'+type]" :style="{'color':color}">{{title}}</text>
	</view>
</template>

<script>
	/**
	 * Title 标题
	 * @description 标题，通常用于记录页面标题，使用当前组件，uni-app 如果开启统计，将会自动统计页面标题
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=1066
	 * @property {String} type = [h1|h2|h3|h4|h5] 标题类型
	 * 	@value h1 一级标题
	 * 	@value h2 二级标题
	 * 	@value h3 三级标题
	 * 	@value h4 四级标题
	 * 	@value h5 五级标题
	 * @property {String} title 标题内容
	 * @property {String} align = [left|center|right] 对齐方式
	 * 	@value left 做对齐
	 * 	@value center 居中对齐
	 * 	@value right 右对齐
	 * @property {String} color 字体颜色
	 * @property {Boolean} stat = [true|false] 是否开启统计功能呢，如不填写type值，默认为开启，填写 type 属性，默认为关闭
	 */
	export default {
		name:"UniTitle",
		props: {
			type: {
				type: String,
				default: ''
			},
			title: {
				type: String,
				default: ''
			},
			align: {
				type: String,
				default: 'left'
			},
			color: {
				type: String,
				default: '#333333'
			},
			stat: {
				type: [Boolean, String],
				default: ''
			}
		},
		data() {
			return {

			};
		},
		computed: {
			textAlign() {
				let align = 'center';
				switch (this.align) {
					case 'left':
						align = 'flex-start'
						break;
					case 'center':
						align = 'center'
						break;
					case 'right':
						align = 'flex-end'
						break;
				}
				return align
			}
		},
		watch: {
			title(newVal) {
				if (this.isOpenStat()) {
					// 上报数据
					if (uni.report) {
						uni.report('title', this.title)
					}
				}
			}
		},
		mounted() {
			if (this.isOpenStat()) {
				// 上报数据
				if (uni.report) {
					uni.report('title', this.title)
				}
			}
		},
		methods: {
			isOpenStat() {
				if (this.stat === '') {
					this.isStat = false
				}
				let stat_type = (typeof(this.stat) === 'boolean' && this.stat) || (typeof(this.stat) === 'string' && this.stat !==
					'')
				if (this.type === "") {
					this.isStat = true
					if (this.stat.toString() === 'false') {
						this.isStat = false
					}
				}

				if (this.type !== '') {
					this.isStat = true
					if (stat_type) {
						this.isStat = true
					} else {
						this.isStat = false
					}
				}
				return this.isStat
			}
		}
	}
</script>

<style>
	/* .uni-title {

	} */
	.uni-title__box {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: column;
		align-items: flex-start;
		justify-content: center;
		padding: 8px 0;
		flex: 1;
	}

	.uni-title__base {
		font-size: 15px;
		color: #333;
		font-weight: 500;
	}

	.uni-h1 {
		font-size: 20px;
		color: #333;
		font-weight: bold;
	}

	.uni-h2 {
		font-size: 18px;
		color: #333;
		font-weight: bold;
	}

	.uni-h3 {
		font-size: 16px;
		color: #333;
		font-weight: bold;
		/* font-weight: 400; */
	}

	.uni-h4 {
		font-size: 14px;
		color: #333;
		font-weight: bold;
		/* font-weight: 300; */
	}

	.uni-h5 {
		font-size: 12px;
		color: #333;
		font-weight: bold;
		/* font-weight: 200; */
	}
</style>
