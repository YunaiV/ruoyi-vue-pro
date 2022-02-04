let _debounceTimeout = null,
	_throttleRunning = false
	
/**
 * 防抖
 * 参考文章 https://juejin.cn/post/6844903669389885453
 * 
 * @param {Function} 执行函数
 * @param {Number} delay 延时ms   
 */
export const debounce = (fn, delay=500) => {
	clearTimeout(_debounceTimeout);
	_debounceTimeout = setTimeout(() => {
		fn();
	}, delay);
}

/**
 * 节流
 * 参考文章 https://juejin.cn/post/6844903669389885453
 * 
 * @param {Function} 执行函数
 * @param {Number} delay 延时ms  
 */
export const throttle = (fn, delay=500) => {
	if(_throttleRunning){
		return;
	}
	_throttleRunning = true;
	fn();
	setTimeout(() => {
	    _throttleRunning = false;
	}, delay);
}

/**
 * toast 提示
 * 
 * @param {String} title 标题
 * @param {Object} param 拓展参数
 * @param {Integer} param.duration 持续时间
 * @param {Boolean} param.mask 是否遮罩
 * @param {Boolean} param.icon 图标         
 */
export const msg = (title = '', param={}) => {
	if (!title) {
		return;
	}
	uni.showToast({
		title,
		duration: param.duration || 1500,
		mask: param.mask || false,
		icon: param.icon || 'none' // TODO 芋艿：是否要区分下 error 的提示，或者专门的封装
	});
}

/**
 * 检查登录
 * 
 * @param {Boolean} options.nav 如果未登陆，是否跳转到登陆页。默认为 true
 * @return {Boolean} 是否登陆
 */
export const isLogin = (options = {}) => {
	const token = getAuthToken();
	if (token) {
		return true;
	}
	// 若 nav 不为 false，则进行跳转登陆页
	if (options.nav !== false) {
		uni.navigateTo({
			url: '/pages/auth/login'
		})
	}
	return false;
}

/**
 * 获得认证 Token
 * 
 * @return 认证 Token
 */
export const getAuthToken = () => {
	return uni.getStorageSync('token');
}

/**
 * 校验参数
 *  
 * @param {String} 字符串
 * @param {String}  数据的类型。例如说 mobile 手机号、tel 座机 TODO 芋艿：是否组件里解决
 */
export const checkStr = (str, type) => {
	switch (type) {
		case 'mobile': //手机号码
			return /^1[3|4|5|6|7|8|9][0-9]{9}$/.test(str);
		case 'tel': //座机
			return /^(0\d{2,3}-\d{7,8})(-\d{1,4})?$/.test(str);
		case 'card': //身份证
			return /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/.test(str);
		case 'mobileCode': //6位数字验证码
			return /^[0-9]{6}$/.test(str)
		case 'pwd': //密码以字母开头，长度在6~18之间，只能包含字母、数字和下划线
			return /^([a-zA-Z0-9_]){6,18}$/.test(str)
		case 'payPwd': //支付密码 6位纯数字
			return /^[0-9]{6}$/.test(str)
		case 'postal': //邮政编码
			return /[1-9]\d{5}(?!\d)/.test(str);
		case 'QQ': //QQ号
			return /^[1-9][0-9]{4,9}$/.test(str);
		case 'email': //邮箱
			return /^[\w-]+(\.[\w-]+)*@[\w-]+(\.[\w-]+)+$/.test(str);
		case 'money': //金额(小数点2位)
			return /^\d*(?:\.\d{0,2})?$/.test(str);
		case 'URL': //网址
			return /(http|ftp|https):\/\/[\w\-_]+(\.[\w\-_]+)+([\w\-\.,@?^=%&:/~\+#]*[\w\-\@?^=%&/~\+#])?/.test(str)
		case 'IP': //IP
			return /((?:(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d))/.test(str);
		case 'date': //日期时间
			return /^(\d{4})\-(\d{2})\-(\d{2}) (\d{2})(?:\:\d{2}|:(\d{2}):(\d{2}))$/.test(str) || /^(\d{4})\-(\d{2})\-(\d{2})$/
				.test(str)
		case 'number': //数字
			return /^[0-9]$/.test(str);
		case 'english': //英文
			return /^[a-zA-Z]+$/.test(str);
		case 'chinese': //中文
			return /^[\\u4E00-\\u9FA5]+$/.test(str);
		case 'lower': //小写
			return /^[a-z]+$/.test(str);
		case 'upper': //大写
			return /^[A-Z]+$/.test(str);
		case 'HTML': //HTML标记
			return /<("[^"]*"|'[^']*'|[^'">])*>/.test(str);
		default:
			return true;
	}
}