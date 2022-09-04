<template>
	<view class="uni-data-checklist" :style="{'margin-top':isTop+'px'}">
		<template v-if="!isLocal">
			<view class="uni-data-loading">
				<uni-load-more v-if="!mixinDatacomErrorMessage" status="loading" iconType="snow" :iconSize="18" :content-text="contentText"></uni-load-more>
				<text v-else>{{mixinDatacomErrorMessage}}</text>
			</view>
		</template>
		<template v-else>
			<checkbox-group v-if="multiple" class="checklist-group" :class="{'is-list':mode==='list' || wrap}" @change="chagne">
				<label class="checklist-box" :class="['is--'+mode,item.selected?'is-checked':'',(disabled || !!item.disabled)?'is-disable':'',index!==0&&mode==='list'?'is-list-border':'']"
				 :style="item.styleBackgroud" v-for="(item,index) in dataList" :key="index">
					<checkbox class="hidden" hidden :disabled="disabled || !!item.disabled" :value="item[map.value]+''" :checked="item.selected" />
					<view v-if="(mode !=='tag' && mode !== 'list') || ( mode === 'list' && icon === 'left')" class="checkbox__inner"  :style="item.styleIcon">
						<view class="checkbox__inner-icon"></view>
					</view>
					<view class="checklist-content" :class="{'list-content':mode === 'list' && icon ==='left'}">
						<text class="checklist-text" :style="item.styleIconText">{{item[map.text]}}</text>
						<view v-if="mode === 'list' && icon === 'right'" class="checkobx__list" :style="item.styleBackgroud"></view>
					</view>
				</label>
			</checkbox-group>
			<radio-group v-else class="checklist-group" :class="{'is-list':mode==='list','is-wrap':wrap}" @change="chagne">
				<!-- -->
				<label class="checklist-box" :class="['is--'+mode,item.selected?'is-checked':'',(disabled || !!item.disabled)?'is-disable':'',index!==0&&mode==='list'?'is-list-border':'']"
				 :style="item.styleBackgroud" v-for="(item,index) in dataList" :key="index">
					<radio class="hidden" hidden :disabled="disabled || item.disabled" :value="item[map.value]+''" :checked="item.selected" />
					<view v-if="(mode !=='tag' && mode !== 'list') || ( mode === 'list' && icon === 'left')" class="radio__inner"
					 :style="item.styleBackgroud">
						<view class="radio__inner-icon" :style="item.styleIcon"></view>
					</view>
					<view class="checklist-content" :class="{'list-content':mode === 'list' && icon ==='left'}">
						<text class="checklist-text" :style="item.styleIconText">{{item[map.text]}}</text>
						<view v-if="mode === 'list' && icon === 'right'" :style="item.styleRightIcon" class="checkobx__list"></view>
					</view>
				</label>
			</radio-group>
		</template>
	</view>
</template>

<script>
	/**
	 * DataChecklist 数据选择器
	 * @description 通过数据渲染 checkbox 和 radio
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=xxx
	 * @property {String} mode = [default| list | button | tag] 显示模式
	 * @value default  	默认横排模式
	 * @value list		列表模式
	 * @value button	按钮模式
	 * @value tag 		标签模式
	 * @property {Boolean} multiple = [true|false] 是否多选
	 * @property {Array|String|Number} value 默认值
	 * @property {Array} localdata 本地数据 ，格式 [{text:'',value:''}]
	 * @property {Number|String} min 最小选择个数 ，multiple为true时生效
	 * @property {Number|String} max 最大选择个数 ，multiple为true时生效
	 * @property {Boolean} wrap 是否换行显示
	 * @property {String} icon = [left|right]  list 列表模式下icon显示位置
	 * @property {Boolean} selectedColor 选中颜色
	 * @property {Boolean} emptyText 没有数据时显示的文字 ，本地数据无效
	 * @property {Boolean} selectedTextColor 选中文本颜色，如不填写则自动显示
	 * @property {Object} map 字段映射， 默认 map={text:'text',value:'value'}
	 * @value left 左侧显示
	 * @value right 右侧显示
	 * @event {Function} change  选中发生变化触发
	 */

	export default {
		name: 'uniDataChecklist',
		mixins: [uniCloud.mixinDatacom || {}],
		emits:['input','update:modelValue','change'],
		props: {
			mode: {
				type: String,
				default: 'default'
			},

			multiple: {
				type: Boolean,
				default: false
			},
			value: {
				type: [Array, String, Number],
				default () {
					return ''
				}
			},
			// TODO vue3
			modelValue: {
				type: [Array, String, Number],
				default() {
					return '';
				}
			},
			localdata: {
				type: Array,
				default () {
					return []
				}
			},
			min: {
				type: [Number, String],
				default: ''
			},
			max: {
				type: [Number, String],
				default: ''
			},
			wrap: {
				type: Boolean,
				default: false
			},
			icon: {
				type: String,
				default: 'left'
			},
			selectedColor: {
				type: String,
				default: ''
			},
			selectedTextColor: {
				type: String,
				default: ''
			},
			emptyText:{
				type: String,
				default: '暂无数据'
			},
			disabled:{
				type: Boolean,
				default: false
			},
			map:{
				type: Object,
				default(){
					return {
						text:'text',
						value:'value'
					}
				}
			}
		},
		watch: {
			localdata: {
				handler(newVal) {
					this.range = newVal
					this.dataList = this.getDataList(this.getSelectedValue(newVal))
				},
				deep: true
			},
			mixinDatacomResData(newVal) {
				this.range = newVal
				this.dataList = this.getDataList(this.getSelectedValue(newVal))
			},
			value(newVal) {
				this.dataList = this.getDataList(newVal)
				// fix by mehaotian is_reset 在 uni-forms 中定义
				// if(!this.is_reset){
				// 	this.is_reset = false
				// 	this.formItem && this.formItem.setValue(newVal)
				// }
			},
			modelValue(newVal) {
				this.dataList = this.getDataList(newVal);
				// if(!this.is_reset){
				// 	this.is_reset = false
				// 	this.formItem && this.formItem.setValue(newVal)
				// }
			}
		},
		data() {
			return {
				dataList: [],
				range: [],
				contentText: {
					contentdown: '查看更多',
					contentrefresh: '加载中',
					contentnomore: '没有更多'
				},
				isLocal:true,
				styles: {
					selectedColor: '#2979ff',
					selectedTextColor: '#666',
				},
				isTop:0
			};
		},
		computed:{
			dataValue(){
				if(this.value === '')return this.modelValue
				if(this.modelValue === '') return this.value
				return this.value
			}
		},
		created() {
			// this.form = this.getForm('uniForms')
			// this.formItem = this.getForm('uniFormsItem')
			// this.formItem && this.formItem.setValue(this.value)

			// if (this.formItem) {
			// 	this.isTop = 6
			// 	if (this.formItem.name) {
			// 		// 如果存在name添加默认值,否则formData 中不存在这个字段不校验
			// 		if(!this.is_reset){
			// 			this.is_reset = false
			// 			this.formItem.setValue(this.dataValue)
			// 		}
			// 		this.rename = this.formItem.name
			// 		this.form.inputChildrens.push(this)
			// 	}
			// }

			if (this.localdata && this.localdata.length !== 0) {
				this.isLocal = true
				this.range = this.localdata
				this.dataList = this.getDataList(this.getSelectedValue(this.range))
			} else {
				if (this.collection) {
					this.isLocal = false
					this.loadData()
				}
			}
		},
		methods: {
			loadData() {
				this.mixinDatacomGet().then(res=>{
					this.mixinDatacomResData = res.result.data
					if(this.mixinDatacomResData.length === 0){
						this.isLocal = false
						this.mixinDatacomErrorMessage = this.emptyText
					}else{
						this.isLocal = true
					}
				}).catch(err=>{
					this.mixinDatacomErrorMessage = err.message
				})
			},
			/**
			 * 获取父元素实例
			 */
			getForm(name = 'uniForms') {
				let parent = this.$parent;
				let parentName = parent.$options.name;
				while (parentName !== name) {
					parent = parent.$parent;
					if (!parent) return false
					parentName = parent.$options.name;
				}
				return parent;
			},
			chagne(e) {
				const values = e.detail.value

				let detail = {
					value: [],
					data: []
				}

				if (this.multiple) {
					this.range.forEach(item => {

						if (values.includes(item[this.map.value] + '')) {
							detail.value.push(item[this.map.value])
							detail.data.push(item)
						}
					})
				} else {
					const range = this.range.find(item => (item[this.map.value] + '') === values)
					if (range) {
						detail = {
							value: range[this.map.value],
							data: range
						}
					}
				}
				// this.formItem && this.formItem.setValue(detail.value)
				// TODO 兼容 vue2
				this.$emit('input', detail.value);
				// // TOTO 兼容 vue3
				this.$emit('update:modelValue', detail.value);
				this.$emit('change', {
					detail
				})
				if (this.multiple) {
					// 如果 v-model 没有绑定 ，则走内部逻辑
					// if (this.value.length === 0) {
					this.dataList = this.getDataList(detail.value, true)
					// }
				} else {
					this.dataList = this.getDataList(detail.value)
				}
			},

			/**
			 * 获取渲染的新数组
			 * @param {Object} value 选中内容
			 */
			getDataList(value) {
				// 解除引用关系，破坏原引用关系，避免污染源数据
				let dataList = JSON.parse(JSON.stringify(this.range))
				let list = []
				if (this.multiple) {
					if (!Array.isArray(value)) {
						value = []
					}
				}
				dataList.forEach((item, index) => {
					item.disabled = item.disable || item.disabled || false
					if (this.multiple) {
						if (value.length > 0) {
							let have = value.find(val => val === item[this.map.value])
							item.selected = have !== undefined
						} else {
							item.selected = false
						}
					} else {
						item.selected = value === item[this.map.value]
					}

					list.push(item)
				})
				return this.setRange(list)
			},
			/**
			 * 处理最大最小值
			 * @param {Object} list
			 */
			setRange(list) {
				let selectList = list.filter(item => item.selected)
				let min = Number(this.min) || 0
				let max = Number(this.max) || ''
				list.forEach((item, index) => {
					if (this.multiple) {
						if (selectList.length <= min) {
							let have = selectList.find(val => val[this.map.value] === item[this.map.value])
							if (have !== undefined) {
								item.disabled = true
							}
						}

						if (selectList.length >= max && max !== '') {
							let have = selectList.find(val => val[this.map.value] === item[this.map.value])
							if (have === undefined) {
								item.disabled = true
							}
						}
					}
					this.setStyles(item, index)
					list[index] = item
				})
				return list
			},
			/**
			 * 设置 class
			 * @param {Object} item
			 * @param {Object} index
			 */
			setStyles(item, index) {
				//  设置自定义样式
				item.styleBackgroud = this.setStyleBackgroud(item)
				item.styleIcon = this.setStyleIcon(item)
				item.styleIconText = this.setStyleIconText(item)
				item.styleRightIcon = this.setStyleRightIcon(item)
			},

			/**
			 * 获取选中值
			 * @param {Object} range
			 */
			getSelectedValue(range) {
				if (!this.multiple) return this.dataValue
				let selectedArr = []
				range.forEach((item) => {
					if (item.selected) {
						selectedArr.push(item[this.map.value])
					}
				})
				return this.dataValue.length > 0 ? this.dataValue : selectedArr
			},

			/**
			 * 设置背景样式
			 */
			setStyleBackgroud(item) {
				let styles = {}
				let selectedColor = this.selectedColor?this.selectedColor:'#2979ff'
				if (this.mode !== 'list') {
					styles['border-color'] = item.selected?selectedColor:'#DCDFE6'
				}
				if (this.mode === 'tag') {
					styles['background-color'] = item.selected? selectedColor:'#f5f5f5'
				}
				let classles = ''
				for (let i in styles) {
					classles += `${i}:${styles[i]};`
				}
				return classles
			},
			setStyleIcon(item) {
				let styles = {}
				let classles = ''
				let selectedColor = this.selectedColor?this.selectedColor:'#2979ff'
				styles['background-color'] = item.selected?selectedColor:'#fff'
				styles['border-color'] = item.selected?selectedColor:'#DCDFE6'

				if(!item.selected && item.disabled){
					styles['background-color'] = '#F2F6FC'
					styles['border-color'] = item.selected?selectedColor:'#DCDFE6'
				}

				for (let i in styles) {
					classles += `${i}:${styles[i]};`
				}
				return classles
			},
			setStyleIconText(item) {
				let styles = {}
				let classles = ''
				let selectedColor = this.selectedColor?this.selectedColor:'#2979ff'
				if (this.mode === 'tag') {
					styles.color = item.selected?(this.selectedTextColor?this.selectedTextColor:'#fff'):'#666'
				} else {
					styles.color = item.selected?(this.selectedTextColor?this.selectedTextColor:selectedColor):'#666'
				}
				if(!item.selected && item.disabled){
					styles.color = '#999'
				}

				for (let i in styles) {
					classles += `${i}:${styles[i]};`
				}
				return classles
			},
			setStyleRightIcon(item) {
				let styles = {}
				let classles = ''
				if (this.mode === 'list') {
					styles['border-color'] = item.selected?this.styles.selectedColor:'#DCDFE6'
				}
				for (let i in styles) {
					classles += `${i}:${styles[i]};`
				}

				return classles
			}
		}
	}
</script>

<style lang="scss">
	$checked-color: #2979ff;
	$border-color: #DCDFE6;
	$disable:0.4;

	@mixin flex {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
	}

	.uni-data-loading {
		@include flex;
		flex-direction: row;
		justify-content: center;
		align-items: center;
		height: 36px;
		padding-left: 10px;
		color: #999;
	}

	.uni-data-checklist {
		position: relative;
		z-index: 0;
		flex: 1;
		// 多选样式
		.checklist-group {
			@include flex;
			flex-direction: row;
			flex-wrap: wrap;

			&.is-list {
				flex-direction: column;
			}

			.checklist-box {
				@include flex;
				flex-direction: row;
				align-items: center;
				position: relative;
				margin: 5px 0;
				margin-right: 25px;

				.hidden {
					position: absolute;
					opacity: 0;
				}

				// 文字样式
				.checklist-content {
					@include flex;
					flex: 1;
					flex-direction: row;
					align-items: center;
					justify-content: space-between;
					.checklist-text {
						font-size: 14px;
						color: #666;
						margin-left: 5px;
						line-height: 14px;
					}

					.checkobx__list {
						border-right-width: 1px;
						border-right-color: #007aff;
						border-right-style: solid;
						border-bottom-width:1px;
						border-bottom-color: #007aff;
						border-bottom-style: solid;
						height: 12px;
						width: 6px;
						left: -5px;
						transform-origin: center;
						transform: rotate(45deg);
						opacity: 0;
					}
				}

				// 多选样式
				.checkbox__inner {
					/* #ifndef APP-NVUE */
					flex-shrink: 0;
					box-sizing: border-box;
					/* #endif */
					position: relative;
					width: 16px;
					height: 16px;
					border: 1px solid $border-color;
					border-radius: 4px;
					background-color: #fff;
					z-index: 1;
					.checkbox__inner-icon {
						position: absolute;
						/* #ifdef APP-NVUE */
						top: 2px;
						/* #endif */
						/* #ifndef APP-NVUE */
						top: 1px;
						/* #endif */
						left: 5px;
						height: 8px;
						width: 4px;
						border-right-width: 1px;
						border-right-color: #fff;
						border-right-style: solid;
						border-bottom-width:1px ;
						border-bottom-color: #fff;
						border-bottom-style: solid;
						opacity: 0;
						transform-origin: center;
						transform: rotate(40deg);
					}
				}

				// 单选样式
				.radio__inner {
					@include flex;
					/* #ifndef APP-NVUE */
					flex-shrink: 0;
					box-sizing: border-box;
					/* #endif */
					justify-content: center;
					align-items: center;
					position: relative;
					width: 16px;
					height: 16px;
					border: 1px solid $border-color;
					border-radius: 16px;
					background-color: #fff;
					z-index: 1;

					.radio__inner-icon {
						width: 8px;
						height: 8px;
						border-radius: 10px;
						opacity: 0;
					}
				}

				// 默认样式
				&.is--default {

					// 禁用
					&.is-disable {
						/* #ifdef H5 */
						cursor: not-allowed;
						/* #endif */
						.checkbox__inner {
							background-color: #F2F6FC;
							border-color: $border-color;
							/* #ifdef H5 */
							cursor: not-allowed;
							/* #endif */
						}

						.radio__inner {
							background-color: #F2F6FC;
							border-color: $border-color;
						}
						.checklist-text {
							color: #999;
						}
					}

					// 选中
					&.is-checked {
						.checkbox__inner {
							border-color: $checked-color;
							background-color: $checked-color;

							.checkbox__inner-icon {
								opacity: 1;
								transform: rotate(45deg);
							}
						}
						.radio__inner {
							border-color: $checked-color;
							.radio__inner-icon {
								opacity: 1;
								background-color: $checked-color;
							}
						}
						.checklist-text {
							color: $checked-color;
						}
						// 选中禁用
						&.is-disable {
							.checkbox__inner {
								opacity: $disable;
							}

							.checklist-text {
								opacity: $disable;
							}
							.radio__inner {
								opacity: $disable;
							}
						}
					}
				}

				// 按钮样式
				&.is--button {
					margin-right: 10px;
					padding: 5px 10px;
					border: 1px $border-color solid;
					border-radius: 3px;
					transition: border-color 0.2s;

					// 禁用
					&.is-disable {
						/* #ifdef H5 */
						cursor: not-allowed;
						/* #endif */
						border: 1px #eee solid;
						opacity: $disable;
						.checkbox__inner {
							background-color: #F2F6FC;
							border-color: $border-color;
							/* #ifdef H5 */
							cursor: not-allowed;
							/* #endif */
						}
						.radio__inner {
							background-color: #F2F6FC;
							border-color: $border-color;
							/* #ifdef H5 */
							cursor: not-allowed;
							/* #endif */
						}
						.checklist-text {
							color: #999;
						}
					}

					&.is-checked {
						border-color: $checked-color;
						.checkbox__inner {
							border-color: $checked-color;
							background-color: $checked-color;
							.checkbox__inner-icon {
								opacity: 1;
								transform: rotate(45deg);
							}
						}

						.radio__inner {
							border-color: $checked-color;

							.radio__inner-icon {
								opacity: 1;
								background-color: $checked-color;
							}
						}

						.checklist-text {
							color: $checked-color;
						}

						// 选中禁用
						&.is-disable {
							opacity: $disable;
						}
					}
				}

				// 标签样式
				&.is--tag {
					margin-right: 10px;
					padding: 5px 10px;
					border: 1px $border-color solid;
					border-radius: 3px;
					background-color: #f5f5f5;

					.checklist-text {
						margin: 0;
						color: #666;
					}

					// 禁用
					&.is-disable {
						/* #ifdef H5 */
						cursor: not-allowed;
						/* #endif */
						opacity: $disable;
					}

					&.is-checked {
						background-color: $checked-color;
						border-color: $checked-color;

						.checklist-text {
							color: #fff;
						}
					}
				}
				// 列表样式
				&.is--list {
					/* #ifndef APP-NVUE */
					display: flex;
					/* #endif */
					padding: 10px 15px;
					padding-left: 0;
					margin: 0;

					&.is-list-border {
						border-top: 1px #eee solid;
					}

					// 禁用
					&.is-disable {
						/* #ifdef H5 */
						cursor: not-allowed;
						/* #endif */
						.checkbox__inner {
							background-color: #F2F6FC;
							border-color: $border-color;
							/* #ifdef H5 */
							cursor: not-allowed;
							/* #endif */
						}
						.checklist-text {
							color: #999;
						}
					}

					&.is-checked {
						.checkbox__inner {
							border-color: $checked-color;
							background-color: $checked-color;

							.checkbox__inner-icon {
								opacity: 1;
								transform: rotate(45deg);
							}
						}
						.radio__inner {
							.radio__inner-icon {
								opacity: 1;
							}
						}
						.checklist-text {
							color: $checked-color;
						}

						.checklist-content {
							.checkobx__list {
								opacity: 1;
								border-color: $checked-color;
							}
						}

						// 选中禁用
						&.is-disable {
							.checkbox__inner {
								opacity: $disable;
							}

							.checklist-text {
								opacity: $disable;
							}
						}
					}
				}
			}
		}
	}
</style>
