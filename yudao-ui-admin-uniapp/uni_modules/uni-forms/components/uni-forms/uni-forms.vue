<template>
	<view class="uni-forms">
		<form>
			<slot></slot>
		</form>
	</view>
</template>

<script>
	import Validator from './validate.js';
	import {
		deepCopy,
		getValue,
		isRequiredField,
		setDataValue,
		getDataValue,
		realName,
		isRealName,
		rawData,
		isEqual
	} from './utils.js'

	// #ifndef VUE3
	// 后续会慢慢废弃这个方法
	import Vue from 'vue';
	Vue.prototype.binddata = function(name, value, formName) {
		if (formName) {
			this.$refs[formName].setValue(name, value);
		} else {
			let formVm;
			for (let i in this.$refs) {
				const vm = this.$refs[i];
				if (vm && vm.$options && vm.$options.name === 'uniForms') {
					formVm = vm;
					break;
				}
			}
			if (!formVm) return console.error('当前 uni-froms 组件缺少 ref 属性');
			formVm.setValue(name, value);
		}
	};
	// #endif
	/**
	 * Forms 表单
	 * @description 由输入框、选择器、单选框、多选框等控件组成，用以收集、校验、提交数据
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=2773
	 * @property {Object} rules	表单校验规则
	 * @property {String} validateTrigger = [bind|submit|blur]	校验触发器方式 默认 submit
	 * @value bind		发生变化时触发
	 * @value submit	提交时触发
	 * @value blur	  失去焦点时触发
	 * @property {String} labelPosition = [top|left]	label 位置 默认 left
	 * @value top		顶部显示 label
	 * @value left	左侧显示 label
	 * @property {String} labelWidth	label 宽度，默认 65px
	 * @property {String} labelAlign = [left|center|right]	label 居中方式  默认 left
	 * @value left		label 左侧显示
	 * @value center	label 居中
	 * @value right		label 右侧对齐
	 * @property {String} errShowType = [undertext|toast|modal]	校验错误信息提示方式
	 * @value undertext	错误信息在底部显示
	 * @value toast			错误信息toast显示
	 * @value modal			错误信息modal显示
	 * @event {Function} submit	提交时触发
	 * @event {Function} validate	校验结果发生变化触发
	 */
	export default {
		name: 'uniForms',
		emits: ['validate', 'submit'],
		options: {
			virtualHost: true
		},
		props: {
			// 即将弃用
			value: {
				type: Object,
				default () {
					return null;
				}
			},
			// vue3 替换 value 属性
			modelValue: {
				type: Object,
				default () {
					return null;
				}
			},
			// 1.4.0 开始将不支持 v-model ，且废弃 value 和 modelValue
			model: {
				type: Object,
				default () {
					return null;
				}
			},
			// 表单校验规则
			rules: {
				type: Object,
				default () {
					return {};
				}
			},
			//校验错误信息提示方式 默认 undertext 取值 [undertext|toast|modal]
			errShowType: {
				type: String,
				default: 'undertext'
			},
			// 校验触发器方式 默认 bind 取值 [bind|submit]
			validateTrigger: {
				type: String,
				default: 'submit'
			},
			// label 位置，默认 left 取值  top/left
			labelPosition: {
				type: String,
				default: 'left'
			},
			// label 宽度
			labelWidth: {
				type: [String, Number],
				default: ''
			},
			// label 居中方式，默认 left 取值 left/center/right
			labelAlign: {
				type: String,
				default: 'left'
			},
			border: {
				type: Boolean,
				default: false
			}
		},
		provide() {
			return {
				uniForm: this
			}
		},
		data() {
			return {
				// 表单本地值的记录，不应该与传如的值进行关联
				formData: {},
				formRules: {}
			};
		},
		computed: {
			// 计算数据源变化的
			localData() {
				const localVal = this.model || this.modelValue || this.value
				if (localVal) {
					return deepCopy(localVal)
				}
				return {}
			}
		},
		watch: {
			// 监听数据变化 ,暂时不使用，需要单独赋值
			// localData: {},
			// 监听规则变化
			rules: {
				handler: function(val, oldVal) {
					this.setRules(val)
				},
				deep: true,
				immediate: true
			}
		},
		created() {
			// #ifdef VUE3
			let getbinddata = getApp().$vm.$.appContext.config.globalProperties.binddata
			if (!getbinddata) {
				getApp().$vm.$.appContext.config.globalProperties.binddata = function(name, value, formName) {
					if (formName) {
						this.$refs[formName].setValue(name, value);
					} else {
						let formVm;
						for (let i in this.$refs) {
							const vm = this.$refs[i];
							if (vm && vm.$options && vm.$options.name === 'uniForms') {
								formVm = vm;
								break;
							}
						}
						if (!formVm) return console.error('当前 uni-froms 组件缺少 ref 属性');
						formVm.setValue(name, value);
					}
				}
			}
			// #endif

			// 子组件实例数组
			this.childrens = []
			// TODO 兼容旧版 uni-data-picker ,新版本中无效，只是避免报错
			this.inputChildrens = []
			this.setRules(this.rules)
		},
		methods: {
			/**
			 * 外部调用方法
			 * 设置规则 ，主要用于小程序自定义检验规则
			 * @param {Array} rules 规则源数据
			 */
			setRules(rules) {
				// TODO 有可能子组件合并规则的时机比这个要早，所以需要合并对象 ，而不是直接赋值，可能会被覆盖
				this.formRules = Object.assign({}, this.formRules, rules)
				// 初始化校验函数
				this.validator = new Validator(rules);
			},

			/**
			 * 外部调用方法
			 * 设置数据，用于设置表单数据，公开给用户使用 ， 不支持在动态表单中使用
			 * @param {Object} key
			 * @param {Object} value
			 */
			setValue(key, value) {
				let example = this.childrens.find(child => child.name === key);
				if (!example) return null;
				this.formData[key] = getValue(key, value, (this.formRules[key] && this.formRules[key].rules) || [])
				return example.onFieldChange(this.formData[key]);
			},

			/**
			 * 外部调用方法
			 * 手动提交校验表单
			 * 对整个表单进行校验的方法，参数为一个回调函数。
			 * @param {Array} keepitem 保留不参与校验的字段
			 * @param {type} callback 方法回调
			 */
			validate(keepitem, callback) {
				return this.checkAll(this.formData, keepitem, callback);
			},

			/**
			 * 外部调用方法
			 * 部分表单校验
			 * @param {Array|String} props 需要校验的字段
			 * @param {Function} 回调函数
			 */
			validateField(props = [], callback) {
				props = [].concat(props);
				let invalidFields = {};
				this.childrens.forEach(item => {
					const name = realName(item.name)
					if (props.indexOf(name) !== -1) {
						invalidFields = Object.assign({}, invalidFields, {
							[name]: this.formData[name]
						});
					}
				});
				return this.checkAll(invalidFields, [], callback);
			},

			/**
			 * 外部调用方法
			 * 移除表单项的校验结果。传入待移除的表单项的 prop 属性或者 prop 组成的数组，如不传则移除整个表单的校验结果
			 * @param {Array|String} props 需要移除校验的字段 ，不填为所有
			 */
			clearValidate(props = []) {
				props = [].concat(props);
				this.childrens.forEach(item => {
					if (props.length === 0) {
						item.errMsg = '';
					} else {
						const name = realName(item.name)
						if (props.indexOf(name) !== -1) {
							item.errMsg = '';
						}
					}
				});
			},

			/**
			 * 外部调用方法 ，即将废弃
			 * 手动提交校验表单
			 * 对整个表单进行校验的方法，参数为一个回调函数。
			 * @param {Array} keepitem 保留不参与校验的字段
			 * @param {type} callback 方法回调
			 */
			submit(keepitem, callback, type) {
				for (let i in this.dataValue) {
					const itemData = this.childrens.find(v => v.name === i);
					if (itemData) {
						if (this.formData[i] === undefined) {
							this.formData[i] = this._getValue(i, this.dataValue[i]);
						}
					}
				}

				if (!type) {
					console.warn('submit 方法即将废弃，请使用validate方法代替！');
				}

				return this.checkAll(this.formData, keepitem, callback, 'submit');
			},

			// 校验所有
			async checkAll(invalidFields, keepitem, callback, type) {
				// 不存在校验规则 ，则停止校验流程
				if (!this.validator) return
				let childrens = []
				// 处理参与校验的item实例
				for (let i in invalidFields) {
					const item = this.childrens.find(v => realName(v.name) === i)
					if (item) {
						childrens.push(item)
					}
				}

				// 如果validate第一个参数是funciont ,那就走回调
				if (!callback && typeof keepitem === 'function') {
					callback = keepitem;
				}

				let promise;
				// 如果不存在回调，那么使用 Promise 方式返回
				if (!callback && typeof callback !== 'function' && Promise) {
					promise = new Promise((resolve, reject) => {
						callback = function(valid, invalidFields) {
							!valid ? resolve(invalidFields) : reject(valid);
						};
					});
				}

				let results = [];
				// 避免引用错乱 ，建议拷贝对象处理
				let tempFormData = JSON.parse(JSON.stringify(invalidFields))
				// 所有子组件参与校验,使用 for 可以使用  awiat
				for (let i in childrens) {
					const child = childrens[i]
					let name = realName(child.name);
					const result = await child.onFieldChange(tempFormData[name]);
					if (result) {
						results.push(result);
						// toast ,modal 只需要执行第一次就可以
						if (this.errShowType === 'toast' || this.errShowType === 'modal') break;
					}
				}


				if (Array.isArray(results)) {
					if (results.length === 0) results = null;
				}
				if (Array.isArray(keepitem)) {
					keepitem.forEach(v => {
						let vName = realName(v);
						let value = getDataValue(v, this.localData)
						if (value !== undefined) {
							tempFormData[vName] = value
						}
					});
				}

				// TODO submit 即将废弃
				if (type === 'submit') {
					this.$emit('submit', {
						detail: {
							value: tempFormData,
							errors: results
						}
					});
				} else {
					this.$emit('validate', results);
				}

				// const resetFormData = rawData(tempFormData, this.localData, this.name)
				let resetFormData = {}
				resetFormData = rawData(tempFormData, this.name)
				callback && typeof callback === 'function' && callback(results, resetFormData);

				if (promise && callback) {
					return promise;
				} else {
					return null;
				}

			},

			/**
			 * 返回validate事件
			 * @param {Object} result
			 */
			validateCheck(result) {
				this.$emit('validate', result);
			},
			_getValue: getValue,
			_isRequiredField: isRequiredField,
			_setDataValue: setDataValue,
			_getDataValue: getDataValue,
			_realName: realName,
			_isRealName: isRealName,
			_isEqual: isEqual
		}
	};
</script>

<style lang="scss">
	.uni-forms {}
</style>
