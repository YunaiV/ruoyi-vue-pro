export default {
	props: {
		// 输入的值
		value: {
			type: [String, Number],
			default: uni.$u.props.input.value
		},
		// 输入框类型
		// number-数字输入键盘，app-vue下可以输入浮点数，app-nvue和小程序平台下只能输入整数
		// idcard-身份证输入键盘，微信、支付宝、百度、QQ小程序
		// digit-带小数点的数字键盘，App的nvue页面、微信、支付宝、百度、头条、QQ小程序
		// text-文本输入键盘
		type: {
			type: String,
			default: uni.$u.props.input.type
		},
		// 如果 textarea 是在一个 position:fixed 的区域，需要显示指定属性 fixed 为 true，
		// 兼容性：微信小程序、百度小程序、字节跳动小程序、QQ小程序
		fixed: {
			type: Boolean,
			default: uni.$u.props.input.fixed
		},
		// 是否禁用输入框
		disabled: {
			type: Boolean,
			default: uni.$u.props.input.disabled
		},
		// 禁用状态时的背景色
		disabledColor: {
			type: String,
			default: uni.$u.props.input.disabledColor
		},
		// 是否显示清除控件
		clearable: {
			type: Boolean,
			default: uni.$u.props.input.clearable
		},
		// 是否密码类型
		password: {
			type: Boolean,
			default: uni.$u.props.input.password
		},
		// 最大输入长度，设置为 -1 的时候不限制最大长度
		maxlength: {
			type: [String, Number],
			default: uni.$u.props.input.maxlength
		},
		// 	输入框为空时的占位符
		placeholder: {
			type: String,
			default: uni.$u.props.input.placeholder
		},
		// 指定placeholder的样式类，注意页面或组件的style中写了scoped时，需要在类名前写/deep/
		placeholderClass: {
			type: String,
			default: uni.$u.props.input.placeholderClass
		},
		// 指定placeholder的样式
		placeholderStyle: {
			type: [String, Object],
			default: uni.$u.props.input.placeholderStyle
		},
		// 是否显示输入字数统计，只在 type ="text"或type ="textarea"时有效
		showWordLimit: {
			type: Boolean,
			default: uni.$u.props.input.showWordLimit
		},
		// 设置右下角按钮的文字，有效值：send|search|next|go|done，兼容性详见uni-app文档
		// https://uniapp.dcloud.io/component/input
		// https://uniapp.dcloud.io/component/textarea
		confirmType: {
			type: String,
			default: uni.$u.props.input.confirmType
		},
		// 点击键盘右下角按钮时是否保持键盘不收起，H5无效
		confirmHold: {
			type: Boolean,
			default: uni.$u.props.input.confirmHold
		},
		// focus时，点击页面的时候不收起键盘，微信小程序有效
		holdKeyboard: {
			type: Boolean,
			default: uni.$u.props.input.holdKeyboard
		},
		// 自动获取焦点
		// 在 H5 平台能否聚焦以及软键盘是否跟随弹出，取决于当前浏览器本身的实现。nvue 页面不支持，需使用组件的 focus()、blur() 方法控制焦点
		focus: {
			type: Boolean,
			default: uni.$u.props.input.focus
		},
		// 键盘收起时，是否自动失去焦点，目前仅App3.0.0+有效
		autoBlur: {
			type: Boolean,
			default: uni.$u.props.input.autoBlur
		},
		// 是否去掉 iOS 下的默认内边距，仅微信小程序，且type=textarea时有效
		disableDefaultPadding: {
			type: Boolean,
			default: uni.$u.props.input.disableDefaultPadding
		},
		// 指定focus时光标的位置
		cursor: {
			type: [String, Number],
			default: uni.$u.props.input.cursor
		},
		// 输入框聚焦时底部与键盘的距离
		cursorSpacing: {
			type: [String, Number],
			default: uni.$u.props.input.cursorSpacing
		},
		// 光标起始位置，自动聚集时有效，需与selection-end搭配使用
		selectionStart: {
			type: [String, Number],
			default: uni.$u.props.input.selectionStart
		},
		// 光标结束位置，自动聚集时有效，需与selection-start搭配使用
		selectionEnd: {
			type: [String, Number],
			default: uni.$u.props.input.selectionEnd
		},
		// 键盘弹起时，是否自动上推页面
		adjustPosition: {
			type: Boolean,
			default: uni.$u.props.input.adjustPosition
		},
		// 输入框内容对齐方式，可选值为：left|center|right
		inputAlign: {
			type: String,
			default: uni.$u.props.input.inputAlign
		},
		// 输入框字体的大小
		fontSize: {
			type: [String, Number],
			default: uni.$u.props.input.fontSize
		},
		// 输入框字体颜色
		color: {
			type: String,
			default: uni.$u.props.input.color
		},
		// 输入框前置图标
		prefixIcon: {
			type: String,
			default: uni.$u.props.input.prefixIcon
		},
		// 前置图标样式，对象或字符串
		prefixIconStyle: {
			type: [String, Object],
			default: uni.$u.props.input.prefixIconStyle
		},
		// 输入框后置图标
		suffixIcon: {
			type: String,
			default: uni.$u.props.input.suffixIcon
		},
		// 后置图标样式，对象或字符串
		suffixIconStyle: {
			type: [String, Object],
			default: uni.$u.props.input.suffixIconStyle
		},
		// 边框类型，surround-四周边框，bottom-底部边框，none-无边框
		border: {
			type: String,
			default: uni.$u.props.input.border
		},
		// 是否只读，与disabled不同之处在于disabled会置灰组件，而readonly则不会
		readonly: {
			type: Boolean,
			default: uni.$u.props.input.readonly
		},
		// 输入框形状，circle-圆形，square-方形
		shape: {
			type: String,
			default: uni.$u.props.input.shape
		},
		// 用于处理或者过滤输入框内容的方法
		formatter: {
			type: [Function, null],
			default: uni.$u.props.input.formatter
		}
	}
}
