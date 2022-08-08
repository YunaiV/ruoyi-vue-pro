<template>
	<view class="uni-forms-item"
		:class="['is-direction-' + localLabelPos ,border?'uni-forms-item--border':'' ,border && isFirstBorder?'is-first-border':'']">
		<slot name="label">
			<view class="uni-forms-item__label" :class="{'no-label':!label && !isRequired}"
				:style="{width:localLabelWidth,justifyContent: localLabelAlign}">
				<text v-if="isRequired" class="is-required">*</text>
				<text>{{label}}</text>
			</view>
		</slot>
		<!-- #ifndef APP-NVUE -->
		<view class="uni-forms-item__content">
			<slot></slot>
			<view class="uni-forms-item__error" :class="{'msg--active':msg}">
				<text>{{msg}}</text>
			</view>
		</view>
		<!-- #endif -->
		<!-- #ifdef APP-NVUE -->
		<view class="uni-forms-item__nuve-content">
			<view class="uni-forms-item__content">
				<slot></slot>
			</view>
			<view class="uni-forms-item__error" :class="{'msg--active':msg}">
				<text class="error-text">{{msg}}</text>
			</view>
		</view>
		<!-- #endif -->
	</view>
</template>

<script>
	/**
	 * uni-fomrs-item 表单子组件
	 * @description uni-fomrs-item 表单子组件，提供了基础布局已经校验能力
	 * @tutorial https://ext.dcloud.net.cn/plugin?id=2773
	 * @property {Boolean} required 是否必填，左边显示红色"*"号
	 * @property {String } 	label 				输入框左边的文字提示
	 * @property {Number } 	labelWidth 			label的宽度，单位px（默认65）
	 * @property {String } 	labelAlign = [left|center|right] label的文字对齐方式（默认left）
	 * 	@value left		label 左侧显示
	 * 	@value center	label 居中
	 * 	@value right	label 右侧对齐
	 * @property {String } 	errorMessage 		显示的错误提示内容，如果为空字符串或者false，则不显示错误信息
	 * @property {String } 	name 				表单域的属性名，在使用校验规则时必填
	 * @property {String } 	leftIcon 			【1.4.0废弃】label左边的图标，限 uni-ui 的图标名称
	 * @property {String } 	iconColor 		【1.4.0废弃】左边通过icon配置的图标的颜色（默认#606266）
	 * @property {String} validateTrigger = [bind|submit|blur]	【1.4.0废弃】校验触发器方式 默认 submit
	 * 	@value bind 	发生变化时触发
	 * 	@value submit 提交时触发
	 * 	@value blur 	失去焦点触发
	 * @property {String } 	labelPosition = [top|left] 【1.4.0废弃】label的文字的位置（默认left）
	 * 	@value top	顶部显示 label
	 * 	@value left	左侧显示 label
	 */

	export default {
		name: 'uniFormsItem',
		options: {
			virtualHost: true
		},
		provide() {
			return {
				uniFormItem: this
			}
		},
		inject: {
			form: {
				from: 'uniForm',
				default: null
			},
		},
		props: {
			// 表单校验规则
			rules: {
				type: Array,
				default () {
					return null;
				}
			},
			// 表单域的属性名，在使用校验规则时必填
			name: {
				type: [String, Array],
				default: ''
			},
			required: {
				type: Boolean,
				default: false
			},
			label: {
				type: String,
				default: ''
			},
			// label的宽度 ，默认 80
			labelWidth: {
				type: [String, Number],
				default: ''
			},
			// label 居中方式，默认 left 取值 left/center/right
			labelAlign: {
				type: String,
				default: ''
			},
			// 强制显示错误信息
			errorMessage: {
				type: [String, Boolean],
				default: ''
			},
			// 1.4.0 弃用，统一使用 form 的校验时机
			// validateTrigger: {
			// 	type: String,
			// 	default: ''
			// },
			// 1.4.0 弃用，统一使用 form 的label 位置
			// labelPosition: {
			// 	type: String,
			// 	default: ''
			// },
			// 1.4.0 以下属性已经废弃，请使用  #label 插槽代替
			leftIcon: String,
			iconColor: {
				type: String,
				default: '#606266'
			},
		},
		data() {
			return {
				errMsg: '',
				isRequired: false,
				userRules: null,
				localLabelAlign: 'left',
				localLabelWidth: '65px',
				localLabelPos: 'left',
				border: false,
				isFirstBorder: false,
			};
		},
		computed: {
			// 处理错误信息
			msg() {
				return this.errorMessage || this.errMsg;
			}
		},
		watch: {
			// 规则发生变化通知子组件更新
			'form.formRules'(val) {
				// TODO 处理头条vue3 watch不生效的问题
				// #ifndef MP-TOUTIAO
				this.init()
				// #endif
			},
			'form.labelWidth'(val) {
				// 宽度
				this.localLabelWidth = this._labelWidthUnit(val)

			},
			'form.labelPosition'(val) {
				// 标签位置
				this.localLabelPos = this._labelPosition()
			},
			'form.labelAlign'(val) {

			}
		},
		created() {
			this.init(true)
			if (this.name && this.form) {
				// TODO 处理头条vue3 watch不生效的问题
				// #ifdef MP-TOUTIAO
				this.$watch('form.formRules', () => {
					this.init()
				})
				// #endif

				// 监听变化
				this.$watch(
					() => {
						const val = this.form._getDataValue(this.name, this.form.localData)
						return val
					},
					(value, oldVal) => {
						const isEqual = this.form._isEqual(value, oldVal)
						// 简单判断前后值的变化，只有发生变化才会发生校验
						// TODO  如果 oldVal = undefined ，那么大概率是源数据里没有值导致 ，这个情况不哦校验 ,可能不严谨 ，需要在做观察
						// fix by mehaotian 暂时取消 && oldVal !== undefined ，如果formData 中不存在，可能会不校验
						if (!isEqual) {
							const val = this.itemSetValue(value)
							this.onFieldChange(val, false)
						}
					}, {
						immediate: false
					}
				);
			}

		},
		// #ifndef VUE3
		destroyed() {
			if (this.__isUnmounted) return
			this.unInit()
		},
		// #endif
		// #ifdef VUE3
		unmounted() {
			this.__isUnmounted = true
			this.unInit()
		},
		// #endif
		methods: {
			/**
			 * 外部调用方法
			 * 设置规则 ，主要用于小程序自定义检验规则
			 * @param {Array} rules 规则源数据
			 */
			setRules(rules = null) {
				this.userRules = rules
				this.init(false)
			},
			// 兼容老版本表单组件
			setValue() {
				// console.log('setValue 方法已经弃用，请使用最新版本的 uni-forms 表单组件以及其他关联组件。');
			},
			/**
			 * 外部调用方法
			 * 校验数据
			 * @param {any} value 需要校验的数据
			 * @param {boolean} 是否立即校验
			 * @return {Array|null} 校验内容
			 */
			async onFieldChange(value, formtrigger = true) {
				const {
					formData,
					localData,
					errShowType,
					validateCheck,
					validateTrigger,
					_isRequiredField,
					_realName
				} = this.form
				const name = _realName(this.name)
				if (!value) {
					value = this.form.formData[name]
				}
				// fixd by mehaotian 不在校验前清空信息，解决闪屏的问题
				// this.errMsg = '';

				// fix by mehaotian 解决没有检验规则的情况下，抛出错误的问题
				const ruleLen = this.itemRules.rules && this.itemRules.rules.length
				if (!this.validator || !ruleLen || ruleLen === 0) return;

				// 检验时机
				// let trigger = this.isTrigger(this.itemRules.validateTrigger, this.validateTrigger, validateTrigger);
				const isRequiredField = _isRequiredField(this.itemRules.rules || []);
				let result = null;
				// 只有等于 bind 时 ，才能开启时实校验
				if (validateTrigger === 'bind' || formtrigger) {
					// 校验当前表单项
					result = await this.validator.validateUpdate({
							[name]: value
						},
						formData
					);

					// 判断是否必填,非必填，不填不校验，填写才校验 ,暂时只处理 undefined  和空的情况
					if (!isRequiredField && (value === undefined || value === '')) {
						result = null;
					}

					// 判断错误信息显示类型
					if (result && result.errorMessage) {
						if (errShowType === 'undertext') {
							// 获取错误信息
							this.errMsg = !result ? '' : result.errorMessage;
						}
						if (errShowType === 'toast') {
							uni.showToast({
								title: result.errorMessage || '校验错误',
								icon: 'none'
							});
						}
						if (errShowType === 'modal') {
							uni.showModal({
								title: '提示',
								content: result.errorMessage || '校验错误'
							});
						}
					} else {
						this.errMsg = ''
					}
					// 通知 form 组件更新事件
					validateCheck(result ? result : null)
				} else {
					this.errMsg = ''
				}
				return result ? result : null;
			},
			/**
			 * 初始组件数据
			 */
			init(type = false) {
				const {
					validator,
					formRules,
					childrens,
					formData,
					localData,
					_realName,
					labelWidth,
					_getDataValue,
					_setDataValue
				} = this.form || {}
				// 对齐方式
				this.localLabelAlign = this._justifyContent()
				// 宽度
				this.localLabelWidth = this._labelWidthUnit(labelWidth)
				// 标签位置
				this.localLabelPos = this._labelPosition()
				this.isRequired = this.required
				// 将需要校验的子组件加入form 队列
				this.form && type && childrens.push(this)

				if (!validator || !formRules) return
				// 判断第一个 item
				if (!this.form.isFirstBorder) {
					this.form.isFirstBorder = true;
					this.isFirstBorder = true;
				}

				// 判断 group 里的第一个 item
				if (this.group) {
					if (!this.group.isFirstBorder) {
						this.group.isFirstBorder = true;
						this.isFirstBorder = true;
					}
				}
				this.border = this.form.border;
				// 获取子域的真实名称
				const name = _realName(this.name)
				const itemRule = this.userRules || this.rules
				if (typeof formRules === 'object' && itemRule) {
					// 子规则替换父规则
					formRules[name] = {
						rules: itemRule
					}
					validator.updateSchema(formRules);
				}
				// 注册校验规则
				const itemRules = formRules[name] || {}
				this.itemRules = itemRules
				// 注册校验函数
				this.validator = validator
				// 默认值赋予
				this.itemSetValue(_getDataValue(this.name, localData))
				this.isRequired = this._isRequired()

			},
			unInit() {
				if (this.form) {
					const {
						childrens,
						formData,
						_realName
					} = this.form
					childrens.forEach((item, index) => {
						if (item === this) {
							this.form.childrens.splice(index, 1)
							delete formData[_realName(item.name)]
						}
					})
				}
			},
			// 设置item 的值
			itemSetValue(value) {
				const name = this.form._realName(this.name)
				const rules = this.itemRules.rules || []
				const val = this.form._getValue(name, value, rules)
				this.form._setDataValue(name, this.form.formData, val)
				return val
			},

			/**
			 * 移除该表单项的校验结果
			 */
			clearValidate() {
				this.errMsg = '';
			},

			// 是否显示星号
			_isRequired() {
				if (this.form) {
					return this.required || this.form._isRequiredField(this.itemRules.rules || [])
				}
				return this.required
			},

			// 处理对齐方式
			_justifyContent() {
				if (this.form) {
					const {
						labelAlign
					} = this.form
					let labelAli = this.labelAlign ? this.labelAlign : labelAlign;
					if (labelAli === 'left') return 'flex-start';
					if (labelAli === 'center') return 'center';
					if (labelAli === 'right') return 'flex-end';
				}
				return 'flex-start';
			},
			// 处理 label宽度单位 ,继承父元素的值
			_labelWidthUnit(labelWidth) {

				// if (this.form) {
				// 	const {
				// 		labelWidth
				// 	} = this.form
				return this.num2px(this.labelWidth ? this.labelWidth : (labelWidth || (this.label ? 65 : 'auto')))
				// }
				// return '65px'
			},
			// 处理 label 位置
			_labelPosition() {
				if (this.form) return this.form.labelPosition || 'left'
				return 'left'

			},

			/**
			 * 触发时机
			 * @param {Object} rule 当前规则内时机
			 * @param {Object} itemRlue 当前组件时机
			 * @param {Object} parentRule 父组件时机
			 */
			isTrigger(rule, itemRlue, parentRule) {
				//  bind  submit
				if (rule === 'submit' || !rule) {
					if (rule === undefined) {
						if (itemRlue !== 'bind') {
							if (!itemRlue) {
								return parentRule === '' ? 'bind' : 'submit';
							}
							return 'submit';
						}
						return 'bind';
					}
					return 'submit';
				}
				return 'bind';
			},
			num2px(num) {
				if (typeof num === 'number') {
					return `${num}px`
				}
				return num
			}
		}
	};
</script>

<style lang="scss">
	.uni-forms-item {
		position: relative;
		display: flex;
		/* #ifdef APP-NVUE */
		// 在 nvue 中，使用 margin-bottom error 信息会被隐藏
		padding-bottom: 22px;
		/* #endif */
		/* #ifndef APP-NVUE */
		margin-bottom: 22px;
		/* #endif */
		flex-direction: row;

		&__label {
			display: flex;
			flex-direction: row;
			align-items: center;
			text-align: left;
			font-size: 14px;
			color: #606266;
			height: 36px;
			padding: 0 12px 0 0;
			/* #ifndef APP-NVUE */
			vertical-align: middle;
			flex-shrink: 0;
			/* #endif */

			/* #ifndef APP-NVUE */
			box-sizing: border-box;

			/* #endif */
			&.no-label {
				padding: 0;
			}
		}

		&__content {
			/* #ifndef MP-TOUTIAO */
			// display: flex;
			// align-items: center;
			/* #endif */
			position: relative;
			font-size: 14px;
			flex: 1;
			/* #ifndef APP-NVUE */
			box-sizing: border-box;
			/* #endif */
			flex-direction: row;

			/* #ifndef APP || H5 || MP-WEIXIN || APP-NVUE */
			// TODO 因为小程序平台会多一层标签节点 ，所以需要在多余节点继承当前样式
			&>uni-easyinput,
			&>uni-data-picker {
				width: 100%;
			}

			/* #endif */

		}

		& .uni-forms-item__nuve-content {
			display: flex;
			flex-direction: column;
			flex: 1;
		}

		&__error {
			color: #f56c6c;
			font-size: 12px;
			line-height: 1;
			padding-top: 4px;
			position: absolute;
			/* #ifndef APP-NVUE */
			top: 100%;
			left: 0;
			transition: transform 0.3s;
			transform: translateY(-100%);
			/* #endif */
			/* #ifdef APP-NVUE */
			bottom: 5px;
			/* #endif */

			opacity: 0;

			.error-text {
				// 只有 nvue 下这个样式才生效
				color: #f56c6c;
				font-size: 12px;
			}

			&.msg--active {
				opacity: 1;
				transform: translateY(0%);
			}
		}

		// 位置修饰样式
		&.is-direction-left {
			flex-direction: row;
		}

		&.is-direction-top {
			flex-direction: column;

			.uni-forms-item__label {
				padding: 0 0 8px;
				line-height: 1.5715;
				text-align: left;
				/* #ifndef APP-NVUE */
				white-space: initial;
				/* #endif */
			}
		}

		.is-required {
			// color: $uni-color-error;
			color: #dd524d;
			font-weight: bold;
		}
	}


	.uni-forms-item--border {
		margin-bottom: 0;
		padding: 10px 0;
		// padding-bottom: 0;
		border-top: 1px #eee solid;

		/* #ifndef APP-NVUE */
		.uni-forms-item__content {
			flex-direction: column;
			justify-content: flex-start;
			align-items: flex-start;

			.uni-forms-item__error {
				position: relative;
				top: 5px;
				left: 0;
				padding-top: 0;
			}
		}

		/* #endif */

		/* #ifdef APP-NVUE */
		display: flex;
		flex-direction: column;

		.uni-forms-item__error {
			position: relative;
			top: 0px;
			left: 0;
			padding-top: 0;
			margin-top: 5px;
		}

		/* #endif */

	}

	.is-first-border {
		/* #ifndef APP-NVUE */
		border: none;
		/* #endif */
		/* #ifdef APP-NVUE */
		border-width: 0;
		/* #endif */
	}
</style>
