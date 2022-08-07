<template>
	<view class="uni-data-tree">
		<view class="uni-data-tree-input" @click="handleInput">
			<slot :options="options" :data="inputSelected" :error="errorMessage">
				<view class="input-value" :class="{'input-value-border': border}">
					<text v-if="errorMessage" class="selected-area error-text">{{errorMessage}}</text>
					<view v-else-if="loading && !isOpened" class="selected-area">
						<uni-load-more class="load-more" :contentText="loadMore" status="loading"></uni-load-more>
					</view>
					<scroll-view v-else-if="inputSelected.length" class="selected-area" scroll-x="true">
						<view class="selected-list">
							<view class="selected-item" v-for="(item,index) in inputSelected" :key="index">
								<text class="text-color">{{item.text}}</text><text v-if="index<inputSelected.length-1"
									class="input-split-line">{{split}}</text>
							</view>
						</view>
					</scroll-view>
					<text v-else class="selected-area placeholder">{{placeholder}}</text>
					<view v-if="clearIcon && !readonly && inputSelected.length" class="icon-clear"
						@click.stop="clear">
						<uni-icons type="clear" color="#c0c4cc" size="24"></uni-icons>
					</view>
					<view class="arrow-area" v-if="(!clearIcon || !inputSelected.length) && !readonly ">
						<view class="input-arrow"></view>
					</view>
				</view>
			</slot>
		</view>
		<view class="uni-data-tree-cover" v-if="isOpened" @click="handleClose"></view>
		<view class="uni-data-tree-dialog" v-if="isOpened">
			<view class="uni-popper__arrow"></view>
			<view class="dialog-caption">
				<view class="title-area">
					<text class="dialog-title">{{popupTitle}}</text>
				</view>
				<view class="dialog-close" @click="handleClose">
					<view class="dialog-close-plus" data-id="close"></view>
					<view class="dialog-close-plus dialog-close-rotate" data-id="close"></view>
				</view>
			</view>
			<data-picker-view class="picker-view" ref="pickerView" v-model="dataValue" :localdata="localdata"
				:preload="preload" :collection="collection" :field="field" :orderby="orderby" :where="where"
				:step-searh="stepSearh" :self-field="selfField" :parent-field="parentField" :managed-mode="true"
				:map="map" :ellipsis="ellipsis" @change="onchange" @datachange="ondatachange" @nodeclick="onnodeclick">
			</data-picker-view>
		</view>
	</view>
</template>

<script>
	import dataPicker from "../uni-data-pickerview/uni-data-picker.js"
	import DataPickerView from "../uni-data-pickerview/uni-data-pickerview.vue"

	/**
	 * DataPicker 级联选择
	 * @description 支持单列、和多列级联选择。列数没有限制，如果屏幕显示不全，顶部tab区域会左右滚动。
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=3796
	 * @property {String} popup-title 弹出窗口标题
	 * @property {Array} localdata 本地数据，参考
	 * @property {Boolean} border = [true|false] 是否有边框
	 * @property {Boolean} readonly = [true|false] 是否仅读
	 * @property {Boolean} preload = [true|false] 是否预加载数据
	 * @value true 开启预加载数据，点击弹出窗口后显示已加载数据
	 * @value false 关闭预加载数据，点击弹出窗口后开始加载数据
	 * @property {Boolean} step-searh = [true|false] 是否分布查询
	 * @value true 启用分布查询，仅查询当前选中节点
	 * @value false 关闭分布查询，一次查询出所有数据
	 * @property {String|DBFieldString} self-field 分布查询当前字段名称
	 * @property {String|DBFieldString} parent-field 分布查询父字段名称
	 * @property {String|DBCollectionString} collection 表名
	 * @property {String|DBFieldString} field 查询字段，多个字段用 `,` 分割
	 * @property {String} orderby 排序字段及正序倒叙设置
	 * @property {String|JQLString} where 查询条件
	 * @event {Function} popupshow 弹出的选择窗口打开时触发此事件
	 * @event {Function} popuphide 弹出的选择窗口关闭时触发此事件
	 */
	export default {
		name: 'UniDataPicker',
		emits: ['popupopened', 'popupclosed', 'nodeclick', 'input', 'change', 'update:modelValue'],
		mixins: [dataPicker],
		components: {
			DataPickerView
		},
		props: {
			options: {
				type: [Object, Array],
				default () {
					return {}
				}
			},
			popupTitle: {
				type: String,
				default: '请选择'
			},
			placeholder: {
				type: String,
				default: '请选择'
			},
			heightMobile: {
				type: String,
				default: ''
			},
			readonly: {
				type: Boolean,
				default: false
			},
			clearIcon: {
				type: Boolean,
				default: true
			},
			border: {
				type: Boolean,
				default: true
			},
			split: {
				type: String,
				default: '/'
			},
			ellipsis: {
				type: Boolean,
				default: true
			}
		},
		data() {
			return {
				isOpened: false,
				inputSelected: []
			}
		},
		created() {
			this.form = this.getForm('uniForms')
			this.formItem = this.getForm('uniFormsItem')
			if (this.formItem) {
				if (this.formItem.name) {
					this.rename = this.formItem.name
					this.form.inputChildrens.push(this)
				}
			}

			this.$nextTick(() => {
				this.load()
			})
		},
		methods: {
			clear() {
				this.inputSelected.splice(0)
				this._dispatchEvent([])
			},
			onPropsChange() {
				this._treeData = []
				this.selectedIndex = 0
				this.load()
			},
			load() {
				if (this.readonly) {
					this._processReadonly(this.localdata, this.dataValue)
					return
				}

				if (this.isLocaldata) {
					this.loadData()
					this.inputSelected = this.selected.slice(0)
				} else if (!this.parentField && !this.selfField && this.hasValue) {
					this.getNodeData(() => {
						this.inputSelected = this.selected.slice(0)
					})
				} else if (this.hasValue) {
					this.getTreePath(() => {
						this.inputSelected = this.selected.slice(0)
					})
				}
			},
			getForm(name = 'uniForms') {
				let parent = this.$parent;
				let parentName = parent.$options.name;
				while (parentName !== name) {
					parent = parent.$parent;
					if (!parent) return false;
					parentName = parent.$options.name;
				}
				return parent;
			},
			show() {
				this.isOpened = true
				setTimeout(() => {
					this.$refs.pickerView.updateData({
						treeData: this._treeData,
						selected: this.selected,
						selectedIndex: this.selectedIndex
					})
				}, 200)
				this.$emit('popupopened')
			},
			hide() {
				this.isOpened = false
				this.$emit('popupclosed')
			},
			handleInput() {
				if (this.readonly) {
					return
				}
				this.show()
			},
			handleClose(e) {
				this.hide()
			},
			onnodeclick(e) {
				this.$emit('nodeclick', e)
			},
			ondatachange(e) {
				this._treeData = this.$refs.pickerView._treeData
			},
			onchange(e) {
				this.hide()
				this.$nextTick(() => {
					this.inputSelected = e;
				})
				this._dispatchEvent(e)
			},
			_processReadonly(dataList, value) {
				var isTree = dataList.findIndex((item) => {
					return item.children
				})
				if (isTree > -1) {
					let inputValue
					if (Array.isArray(value)) {
						inputValue = value[value.length - 1]
						if (typeof inputValue === 'object' && inputValue.value) {
							inputValue = inputValue.value
						}
					} else {
						inputValue = value
					}
					this.inputSelected = this._findNodePath(inputValue, this.localdata)
					return
				}

				if (!this.hasValue) {
					this.inputSelected = []
					return
				}

				let result = []
				for (let i = 0; i < value.length; i++) {
					var val = value[i]
					var item = dataList.find((v) => {
						return v.value == val
					})
					if (item) {
						result.push(item)
					}
				}
				if (result.length) {
					this.inputSelected = result
				}
			},
			_filterForArray(data, valueArray) {
				var result = []
				for (let i = 0; i < valueArray.length; i++) {
					var value = valueArray[i]
					var found = data.find((item) => {
						return item.value == value
					})
					if (found) {
						result.push(found)
					}
				}
				return result
			},
			_dispatchEvent(selected) {
				let item = {}
				if (selected.length) {
					var value = new Array(selected.length)
					for (var i = 0; i < selected.length; i++) {
						value[i] = selected[i].value
					}
					item = selected[selected.length - 1]
				} else {
					item.value = ''
				}
				if (this.formItem) {
					this.formItem.setValue(item.value)
				}

				this.$emit('input', item.value)
				this.$emit('update:modelValue', item.value)
				this.$emit('change', {
					detail: {
						value: selected
					}
				})
			}
		}
	}
</script>

<style >
	.uni-data-tree {
		flex: 1;
		position: relative;
		font-size: 14px;
	}

	.error-text {
		color: #DD524D;
	}

	.input-value {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row;
		align-items: center;
		flex-wrap: nowrap;
		font-size: 14px;
		/* line-height: 35px; */
		padding: 0 10px;
		padding-right: 5px;
		overflow: hidden;
		height: 35px;
		/* #ifndef APP-NVUE */
		box-sizing: border-box;
		/* #endif */
	}

	.input-value-border {
		border: 1px solid #e5e5e5;
		border-radius: 5px;
	}

	.selected-area {
		flex: 1;
		overflow: hidden;
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row;
	}

	.load-more {
		/* #ifndef APP-NVUE */
		margin-right: auto;
		/* #endif */
		/* #ifdef APP-NVUE */
		width: 40px;
		/* #endif */
	}

	.selected-list {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row;
		flex-wrap: nowrap;
		/* padding: 0 5px; */
	}

	.selected-item {
		flex-direction: row;
		/* padding: 0 1px; */
		/* #ifndef APP-NVUE */
		white-space: nowrap;
		/* #endif */
	}
	
	.text-color {
		color: #333;
	}

	.placeholder {
		color: grey;
		font-size: 12px;
	}

	.input-split-line {
		opacity: .5;
	}

	.arrow-area {
		position: relative;
		width: 20px;
		/* #ifndef APP-NVUE */
		margin-bottom: 5px;
		margin-left: auto;
		display: flex;
		/* #endif */
		justify-content: center;
		transform: rotate(-45deg);
		transform-origin: center;
	}

	.input-arrow {
		width: 7px;
		height: 7px;
		border-left: 1px solid #999;
		border-bottom: 1px solid #999;
	}

	.uni-data-tree-cover {
		position: fixed;
		left: 0;
		top: 0;
		right: 0;
		bottom: 0;
		background-color: rgba(0, 0, 0, .4);
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: column;
		z-index: 100;
	}

	.uni-data-tree-dialog {
		position: fixed;
		left: 0;
		top: 20%;
		right: 0;
		bottom: 0;
		background-color: #FFFFFF;
		border-top-left-radius: 10px;
		border-top-right-radius: 10px;
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: column;
		z-index: 102;
		overflow: hidden;
		/* #ifdef APP-NVUE */
		width: 750rpx;
		/* #endif */
	}

	.dialog-caption {
		position: relative;
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row;
		/* border-bottom: 1px solid #f0f0f0; */
	}

	.title-area {
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		align-items: center;
		/* #ifndef APP-NVUE */
		margin: auto;
		/* #endif */
		padding: 0 10px;
	}

	.dialog-title {
		/* font-weight: bold; */
		line-height: 44px;
	}

	.dialog-close {
		position: absolute;
		top: 0;
		right: 0;
		bottom: 0;
		/* #ifndef APP-NVUE */
		display: flex;
		/* #endif */
		flex-direction: row;
		align-items: center;
		padding: 0 15px;
	}

	.dialog-close-plus {
		width: 16px;
		height: 2px;
		background-color: #666;
		border-radius: 2px;
		transform: rotate(45deg);
	}

	.dialog-close-rotate {
		position: absolute;
		transform: rotate(-45deg);
	}

	.picker-view {
		flex: 1;
		overflow: hidden;
	}
	
	.icon-clear {
		display: flex;
		align-items: center;
	}

	/* #ifdef H5 */
	@media all and (min-width: 768px) {
		.uni-data-tree-cover {
			background-color: transparent;
		}

		.uni-data-tree-dialog {
			position: absolute;
			top: 55px;
			height: auto;
			min-height: 400px;
			max-height: 50vh;
			background-color: #fff;
			border: 1px solid #EBEEF5;
			box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
			border-radius: 4px;
			overflow: unset;
		}

		.dialog-caption {
			display: none;
		}

		.icon-clear {
			/* margin-right: 5px; */
		}
	}

	/* #endif */

	/* picker 弹出层通用的指示小三角, todo：扩展至上下左右方向定位 */
	/* #ifndef APP-NVUE */
	.uni-popper__arrow,
	.uni-popper__arrow::after {
		position: absolute;
		display: block;
		width: 0;
		height: 0;
		border-color: transparent;
		border-style: solid;
		border-width: 6px;
	}

	.uni-popper__arrow {
		filter: drop-shadow(0 2px 12px rgba(0, 0, 0, 0.03));
		top: -6px;
		left: 10%;
		margin-right: 3px;
		border-top-width: 0;
		border-bottom-color: #EBEEF5;
	}

	.uni-popper__arrow::after {
		content: " ";
		top: 1px;
		margin-left: -6px;
		border-top-width: 0;
		border-bottom-color: #fff;
	}
	/* #endif */
	</style>
