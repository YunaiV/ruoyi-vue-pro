<template>
	<text>{{dateShow}}</text>
</template>

<script>
	import {friendlyDate} from './date-format.js'
	/**
	 * Dateformat 日期格式化
	 * @description 日期格式化组件
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=3279
	 * @property {Object|String|Number} date 日期对象/日期字符串/时间戳
	 * @property {String} locale 格式化使用的语言
	 * 	@value zh 中文
	 * 	@value en 英文
	 * @property {Array} threshold 应用不同类型格式化的阈值
	 * @property {String} format 输出日期字符串时的格式
	 */
	export default {
		name: 'uniDateformat',
		props: {
			date: {
				type: [Object, String, Number],
				default () {
					return '-'
				}
			},
			locale: {
				type: String,
				default: 'zh',
			},
			threshold: {
				type: Array,
				default () {
					return [0, 0]
				}
			},
			format: {
				type: String,
				default: 'yyyy/MM/dd hh:mm:ss'
			},
			// refreshRate使用不当可能导致性能问题，谨慎使用
			refreshRate: {
				type: [Number, String],
				default: 0
			}
		},
		data() {
			return {
				refreshMark: 0
			}
		},
		computed: {
			dateShow() {
				this.refreshMark
				return friendlyDate(this.date, {
					locale: this.locale,
					threshold: this.threshold,
					format: this.format
				})
			}
		},
		watch: {
			refreshRate: {
				handler() {
					this.setAutoRefresh()
				},
				immediate: true
			}
		},
		methods: {
			refresh() {
				this.refreshMark++
			},
			setAutoRefresh() {
				clearInterval(this.refreshInterval)
				if (this.refreshRate) {
					this.refreshInterval = setInterval(() => {
						this.refresh()
					}, parseInt(this.refreshRate))
				}
			}
		}
	}
</script>

<style>

</style>
