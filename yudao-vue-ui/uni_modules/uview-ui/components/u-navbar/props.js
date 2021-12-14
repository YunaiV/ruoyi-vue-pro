export default {
    props: {
		 // 是否开启顶部安全区适配
		 safeAreaInsetTop: {
			 type: Boolean,
			 default: uni.$u.props.navbar.safeAreaInsetTop
		 },
		 // 固定在顶部时，是否生成一个等高元素，以防止塌陷
		 placeholder: {
			 type: Boolean,
			 default: uni.$u.props.navbar.placeholder
		 },
		 // 是否固定在顶部
		 fixed: {
			 type: Boolean,
			 default: uni.$u.props.navbar.fixed
		 },
		 // 是否显示下边框
		 border: {
			 type: Boolean,
			 default: uni.$u.props.navbar.border
		 },
		 // 左边的图标
		 leftIcon: {
			 type: String,
			 default: uni.$u.props.navbar.leftIcon
		 },
		 // 左边的提示文字
		 leftText: {
			 type: String,
			 default: uni.$u.props.navbar.leftText
		 },
		 // 左右的提示文字
		 rightText: {
			 type: String,
			 default: uni.$u.props.navbar.rightText
		 },
		 // 右边的图标
		 rightIcon: {
			 type: String,
			 default: uni.$u.props.navbar.rightIcon
		 },
		 // 标题
		 title: {
			 type: [String, Number],
			 default: uni.$u.props.navbar.title
		 },
		 // 背景颜色
		 bgColor: {
			 type: String,
			 default: uni.$u.props.navbar.bgColor
		 },
		 // 标题的宽度
		 titleWidth: {
			 type: [String, Number],
			 default: uni.$u.props.navbar.titleWidth
		 },
		 // 导航栏高度
		 height: {
			 type: [String, Number],
			 default: uni.$u.props.navbar.height
		 }
    }
}
