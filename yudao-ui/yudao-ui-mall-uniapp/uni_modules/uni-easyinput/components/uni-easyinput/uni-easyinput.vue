<template>
	<view
		class="uni-easyinput"
		:class="{ 'uni-easyinput-error': msg }"
		:style="{ color: inputBorder && msg ? '#e43d33' : styles.color }"
	>
		<view
			class="uni-easyinput__content"
			:class="{
				'is-input-border': inputBorder,
				'is-input-error-border': inputBorder && msg,
				'is-textarea': type === 'textarea',
				'is-disabled': disabled
			}"
			:style="{
				'border-color': inputBorder && msg ? '#dd524d' : styles.borderColor,
				'background-color': disabled ? styles.disableColor : ''
			}"
		>
			<uni-icons
				v-if="prefixIcon"
				class="content-clear-icon"
				:type="prefixIcon"
				color="#c0c4cc"
				@click="onClickIcon('prefix')"
			></uni-icons>
			<textarea
				v-if="type === 'textarea'"
				class="uni-easyinput__content-textarea"
				:class="{ 'input-padding': inputBorder }"
				:name="name"
				:value="val"
				:placeholder="placeholder"
				:placeholderStyle="placeholderStyle"
				:disabled="disabled"
				placeholder-class="uni-easyinput__placeholder-class"
				:maxlength="inputMaxlength"
				:focus="focused"
				:autoHeight="autoHeight"
				@input="onInput"
				@blur="onBlur"
				@focus="onFocus"
				@confirm="onConfirm"
			></textarea>
			<input
				v-else
				:type="type === 'password' ? 'text' : type"
				class="uni-easyinput__content-input"
				:style="{
					'padding-right': type === 'password' || clearable || prefixIcon ? '' : '10px',
					'padding-left': paddingLeft + 'px'

				}"
				:name="name"
				:value="val"
				:password="!showPassword && type === 'password'"
				:placeholder="placeholder"
				:placeholderStyle="placeholderStyle"
				placeholder-class="uni-easyinput__placeholder-class"
				:disabled="disabled"
				:maxlength="inputMaxlength"
				:focus="focused"
				:confirmType="confirmType"
				@focus="onFocus"
				@blur="onBlur"
				@input="onInput"
				@change="onInput"
				@confirm="onConfirm"
				:cursor-spacing="30"
				always-embed
			/>
			<template v-if="type === 'password' && passwordIcon">
				<uni-icons
					v-if="val"
					class="content-clear-icon"
					:class="{ 'is-textarea-icon': type === 'textarea' }"
					:type="showPassword ? 'eye-slash-filled' : 'eye-filled'"
					:size="18"
					color="#c0c4cc"
					@click="onEyes"
				></uni-icons>
			</template>
			<template v-else-if="suffixIcon">
				<uni-icons
					v-if="suffixIcon"
					class="content-clear-icon"
					:type="suffixIcon"
					color="#c0c4cc"
					@click="onClickIcon('suffix')"
				></uni-icons>
			</template>
			<template v-else>
				<uni-icons
					class="content-clear-icon"
					:class="{ 'is-textarea-icon': type === 'textarea' }"
					type="clear"
					:size="clearSize"
					v-if="clearable && val && !disabled"
					color="#c0c4cc"
					@click="onClear"
				></uni-icons>
			</template>
			<slot name="right"></slot>
		</view>
	</view>
</template>

<script>
// import {
// 	debounce,
// 	throttle
// } from './common.js'
/**
 * Easyinput 输入框
 * @description 此组件可以实现表单的输入与校验，包括 "text" 和 "textarea" 类型。
 * @tutorial https://ext.dcloud.net.cn/plugin?id=3455
 * @property {String}	value	输入内容
 * @property {String }	type	输入框的类型（默认text） password/text/textarea/..
 * 	@value text			文本输入键盘
 * 	@value textarea	多行文本输入键盘
 * 	@value password	密码输入键盘
 * 	@value number		数字输入键盘，注意iOS上app-vue弹出的数字键盘并非9宫格方式
 * 	@value idcard		身份证输入键盘，信、支付宝、百度、QQ小程序
 * 	@value digit		带小数点的数字键盘	，App的nvue页面、微信、支付宝、百度、头条、QQ小程序支持
 * @property {Boolean}	clearable	是否显示右侧清空内容的图标控件，点击可清空输入框内容（默认true）
 * @property {Boolean}	autoHeight	是否自动增高输入区域，type为textarea时有效（默认false）
 * @property {String }	placeholder	输入框的提示文字
 * @property {String }	placeholderStyle	placeholder的样式(内联样式，字符串)，如"color: #ddd"
 * @property {Boolean}	focus	是否自动获得焦点（默认false）
 * @property {Boolean}	disabled	是否禁用（默认false）
 * @property {Number }	maxlength	最大输入长度，设置为 -1 的时候不限制最大长度（默认140）
 * @property {String }	confirmType	设置键盘右下角按钮的文字，仅在type="text"时生效（默认done）
 * @property {Number }	clearSize	清除图标的大小，单位px（默认15）
 * @property {String}	prefixIcon	输入框头部图标
 * @property {String}	suffixIcon	输入框尾部图标
 * @property {Boolean}	trim	是否自动去除两端的空格
 * @value both	去除两端空格
 * @value left	去除左侧空格
 * @value right	去除右侧空格
 * @value start	去除左侧空格
 * @value end		去除右侧空格
 * @value all		去除全部空格
 * @value none	不去除空格
 * @property {Boolean}	inputBorder	是否显示input输入框的边框（默认true）
 * @property {Boolean}	passwordIcon	type=password时是否显示小眼睛图标
 * @property {Object}	styles	自定义颜色
 * @event {Function}	input	输入框内容发生变化时触发
 * @event {Function}	focus	输入框获得焦点时触发
 * @event {Function}	blur	输入框失去焦点时触发
 * @event {Function}	confirm	点击完成按钮时触发
 * @event {Function}	iconClick	点击图标时触发
 * @example <uni-easyinput v-model="mobile"></uni-easyinput>
 */

export default {
	name: 'uni-easyinput',
	emits: ['click', 'iconClick', 'update:modelValue', 'input', 'focus', 'blur', 'confirm'],
	model: {
		prop: 'modelValue',
		event: 'update:modelValue'
	},
	props: {
		name: String,
		value: [Number, String],
		modelValue: [Number, String],
		type: {
			type: String,
			default: 'text'
		},
		clearable: {
			type: Boolean,
			default: true
		},
		autoHeight: {
			type: Boolean,
			default: false
		},
		placeholder: String,
		placeholderStyle: String,
		focus: {
			type: Boolean,
			default: false
		},
		disabled: {
			type: Boolean,
			default: false
		},
		maxlength: {
			type: [Number, String],
			default: 140
		},
		confirmType: {
			type: String,
			default: 'done'
		},
		clearSize: {
			type: [Number, String],
			default: 15
		},
		inputBorder: {
			type: Boolean,
			default: true
		},
		prefixIcon: {
			type: String,
			default: ''
		},
		suffixIcon: {
			type: String,
			default: ''
		},
		trim: {
			type: [Boolean, String],
			default: true
		},
		passwordIcon: {
			type: Boolean,
			default: true
		},
		styles: {
			type: Object,
			default() {
				return {
					color: '#333',
					disableColor: '#F7F6F6',
					borderColor: '#e5e5e5'
				};
			}
		},
		errorMessage: {
			type: [String, Boolean],
			default: ''
		},
		paddingLeft:{
			type: [Number, String],
			default: 0
		}
	},
	data() {
		return {
			focused: false,
			errMsg: '',
			val: '',
			showMsg: '',
			border: false,
			isFirstBorder: false,
			showClearIcon: false,
			showPassword: false
		};
	},
	computed: {
		msg() {
			return this.errorMessage || this.errMsg;
		},
		// 因为uniapp的input组件的maxlength组件必须要数值，这里转为数值，用户可以传入字符串数值
		inputMaxlength() {
			return Number(this.maxlength);
		}
	},
	watch: {
		value(newVal) {
			if (this.errMsg) this.errMsg = '';
			this.val = newVal;
			// fix by mehaotian is_reset 在 uni-forms 中定义
			if (this.form && this.formItem && !this.is_reset) {
				this.is_reset = false;
				this.formItem.setValue(newVal);
			}
		},
		modelValue(newVal) {
			if (this.errMsg) this.errMsg = '';
			this.val = newVal;
			if (this.form && this.formItem && !this.is_reset) {
				this.is_reset = false;
				this.formItem.setValue(newVal);
			}
		},
		focus(newVal) {
			this.$nextTick(() => {
				this.focused = this.focus;
			});
		}
	},
	created() {
		if (!this.value && this.value !== 0) {
			this.val = this.modelValue;
		}
		if (!this.modelValue && this.modelValue !== 0) {
			this.val = this.value;
		}
		this.form = this.getForm('uniForms');
		this.formItem = this.getForm('uniFormsItem');
		if (this.form && this.formItem) {
			if (this.formItem.name) {
				if (!this.is_reset) {
					this.is_reset = false;
					this.formItem.setValue(this.val);
				}
				this.rename = this.formItem.name;
				this.form.inputChildrens.push(this);
			}
		}
	},
	mounted() {
		this.$nextTick(() => {
			this.focused = this.focus;
		});
	},
	methods: {
		/**
		 * 初始化变量值
		 */
		init() {},
		onClickIcon(type) {
			this.$emit('iconClick', type);
		},
		/**
		 * 获取父元素实例
		 */
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

		onEyes() {
			this.showPassword = !this.showPassword;
		},
		onInput(event) {
			let value = event.detail.value;
			// 判断是否去除空格
			if (this.trim) {
				if (typeof this.trim === 'boolean' && this.trim) {
					value = this.trimStr(value);
				}
				if (typeof this.trim === 'string') {
					value = this.trimStr(value, this.trim);
				}
			}
			if (this.errMsg) this.errMsg = '';
			this.val = value;
			// TODO 兼容 vue2
			this.$emit('input', value);
			// TODO　兼容　vue3
			this.$emit('update:modelValue', value);
		},
		onFocus(event) {
			this.$emit('focus', event);
		},
		onBlur(event) {
			let value = event.detail.value;
			this.$emit('blur', event);
		},
		onConfirm(e) {
			this.$emit('confirm', e.detail.value);
		},
		onClear(event) {
			this.val = '';
			// TODO 兼容 vue2
			this.$emit('input', '');
			// TODO 兼容 vue2
			// TODO　兼容　vue3
			this.$emit('update:modelValue', '');
		},
		fieldClick() {
			this.$emit('click');
		},
		trimStr(str, pos = 'both') {
			if (pos === 'both') {
				return str.trim();
			} else if (pos === 'left') {
				return str.trimLeft();
			} else if (pos === 'right') {
				return str.trimRight();
			} else if (pos === 'start') {
				return str.trimStart();
			} else if (pos === 'end') {
				return str.trimEnd();
			} else if (pos === 'all') {
				return str.replace(/\s+/g, '');
			} else if (pos === 'none') {
				return str;
			}
			return str;
		}
	}
};
</script>

<style lang="scss">
$uni-error: #e43d33;
$uni-border-1: #dcdfe6 !default;
.uni-easyinput {
	/* #ifndef APP-NVUE */
	width: 100%;
	/* #endif */
	flex: 1;
	position: relative;
	text-align: left;
	color: #333;
	font-size: 14px;
}

.uni-easyinput__content {
	flex: 1;
	/* #ifndef APP-NVUE */
	width: 100%;
	display: flex;
	box-sizing: border-box;
	min-height: 72rpx;
	/* #endif */
	flex-direction: row;
	align-items: center;
}

.uni-easyinput__content-input {
	/* #ifndef APP-NVUE */
	width: auto;
	/* #endif */
	position: relative;
	overflow: hidden;
	flex: 1;
	line-height: 56rpx;
	font-size: 28rpx;
	height: 56rpx;
}
.uni-easyinput__placeholder-class {
	color: #bbbbbb;
	font-size: 28rpx;
	font-weight: 400;
	line-height: normal;
}
.is-textarea {
	align-items: flex-start;
}

.is-textarea-icon {
	margin-top: 5px;
}

.uni-easyinput__content-textarea {
	position: relative;
	overflow: hidden;
	flex: 1;
	line-height: 1.5;
	font-size: 14px;
	padding-top: 6px;
	padding-bottom: 10px;
	height: 80px;
	/* #ifndef APP-NVUE */
	min-height: 80px;
	width: auto;
	/* #endif */
}

.input-padding {
	padding-left: 10px;
}

.content-clear-icon {
	padding: 0 5px;
}

.label-icon {
	margin-right: 5px;
	margin-top: -1px;
}

// 显示边框
.is-input-border {
	/* #ifndef APP-NVUE */
	display: flex;
	box-sizing: border-box;
	/* #endif */
	flex-direction: row;
	align-items: center;
	border: 1px solid $uni-border-1;
	border-radius: 4px;
}

.uni-error-message {
	position: absolute;
	bottom: -17px;
	left: 0;
	line-height: 12px;
	color: $uni-error;
	font-size: 12px;
	text-align: left;
}

.uni-error-msg--boeder {
	position: relative;
	bottom: 0;
	line-height: 22px;
}

.is-input-error-border {
	border-color: $uni-error;
	.uni-easyinput__placeholder-class {
		// color: mix(#fff, $uni-error, 50%);
	}
}

.uni-easyinput--border {
	margin-bottom: 0;
	padding: 10px 15px;
	// padding-bottom: 0;
	border-top: 1px #eee solid;
}

.uni-easyinput-error {
	padding-bottom: 0;
}

.is-first-border {
	/* #ifndef APP-NVUE */
	border: none;
	/* #endif */
	/* #ifdef APP-NVUE */
	border-width: 0;
	/* #endif */
}

.is-disabled {
	border-color: red;
	background-color: #f7f6f6;
	color: #d5d5d5;
	.uni-easyinput__placeholder-class {
		color: #d5d5d5;
		font-size: 12px;
	}
}
</style>
