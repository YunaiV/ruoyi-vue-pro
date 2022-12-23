/**
 * 简单处理对象拷贝
 * @param {Obejct} 被拷贝对象
 * @@return {Object} 拷贝对象
 */
export const deepCopy = (val) => {
	return JSON.parse(JSON.stringify(val))
}
/**
 * 过滤数字类型
 * @param {String} format 数字类型
 * @@return {Boolean} 返回是否为数字类型
 */
export const typeFilter = (format) => {
	return format === 'int' || format === 'double' || format === 'number' || format === 'timestamp';
}

/**
 * 把 value 转换成指定的类型，用于处理初始值，原因是初始值需要入库不能为 undefined
 * @param {String} key 字段名
 * @param {any} value 字段值
 * @param {Object} rules 表单校验规则
 */
export const getValue = (key, value, rules) => {
	const isRuleNumType = rules.find(val => val.format && typeFilter(val.format));
	const isRuleBoolType = rules.find(val => (val.format && val.format === 'boolean') || val.format === 'bool');
	// 输入类型为 number
	if (!!isRuleNumType) {
		if (!value && value !== 0) {
			value = null
		} else {
			value = isNumber(Number(value)) ? Number(value) : value
		}
	}

	// 输入类型为 boolean
	if (!!isRuleBoolType) {
		value = isBoolean(value) ? value : false
	}

	return value;
}

/**
 * 获取表单数据
 * @param {String|Array} name 真实名称，需要使用 realName 获取
 * @param {Object} data 原始数据
 * @param {any} value  需要设置的值
 */
export const setDataValue = (field, formdata, value) => {
	formdata[field] = value
	return value || ''
}

/**
 * 获取表单数据
 * @param {String|Array} field 真实名称，需要使用 realName 获取
 * @param {Object} data 原始数据
 */
export const getDataValue = (field, data) => {
	return objGet(data, field)
}

/**
 * 获取表单类型
 * @param {String|Array} field 真实名称，需要使用 realName 获取
 */
export const getDataValueType = (field, data) => {
	const value = getDataValue(field, data)
	return {
		type: type(value),
		value
	}
}

/**
 * 获取表单可用的真实name
 * @param {String|Array} name 表单name
 * @@return {String} 表单可用的真实name
 */
export const realName = (name, data = {}) => {
	const base_name = _basePath(name)
	if (typeof base_name === 'object' && Array.isArray(base_name) && base_name.length > 1) {
		const realname = base_name.reduce((a, b) => a += `#${b}`, '_formdata_')
		return realname
	}
	return base_name[0] || name
}

/**
 * 判断是否表单可用的真实name
 * @param {String|Array} name 表单name
 * @@return {String} 表单可用的真实name
 */
export const isRealName = (name) => {
	const reg = /^_formdata_#*/
	return reg.test(name)
}

/**
 * 获取表单数据的原始格式
 * @@return {Object|Array} object 需要解析的数据
 */
export const rawData = (object = {}, name) => {
	let newData = JSON.parse(JSON.stringify(object))
	let formData = {}
	for(let i in newData){
		let path = name2arr(i)
		objSet(formData,path,newData[i])
	}
	return formData
}

/**
 * 真实name还原为 array
 * @param {*} name 
 */
export const name2arr = (name) => {
	let field = name.replace('_formdata_#', '')
	field = field.split('#').map(v => (isNumber(v) ? Number(v) : v))
	return field
}

/**
 * 对象中设置值
 * @param {Object|Array} object 源数据
 * @param {String| Array} path 'a.b.c' 或 ['a',0,'b','c']
 * @param {String} value 需要设置的值
 */
export const objSet = (object, path, value) => {
	if (typeof object !== 'object') return object;
	_basePath(path).reduce((o, k, i, _) => {
		if (i === _.length - 1) { 
			// 若遍历结束直接赋值
			o[k] = value
			return null
		} else if (k in o) { 
			// 若存在对应路径，则返回找到的对象，进行下一次遍历
			return o[k]
		} else { 
			// 若不存在对应路径，则创建对应对象，若下一路径是数字，新对象赋值为空数组，否则赋值为空对象
			o[k] = /^[0-9]{1,}$/.test(_[i + 1]) ? [] : {}
			return o[k]
		}
	}, object)
	// 返回object
	return object;
}

// 处理 path， path有三种形式：'a[0].b.c'、'a.0.b.c' 和 ['a','0','b','c']，需要统一处理成数组，便于后续使用
function _basePath(path) {
	// 若是数组，则直接返回
	if (Array.isArray(path)) return path
	// 若有 '[',']'，则替换成将 '[' 替换成 '.',去掉 ']'
	return path.replace(/\[/g, '.').replace(/\]/g, '').split('.')
}

/**
 * 从对象中获取值
 * @param {Object|Array} object 源数据
 * @param {String| Array} path 'a.b.c' 或 ['a',0,'b','c']
 * @param {String} defaultVal 如果无法从调用链中获取值的默认值
 */
export const objGet = (object, path, defaultVal = 'undefined') => {
	// 先将path处理成统一格式
	let newPath = _basePath(path)
	// 递归处理，返回最后结果
	let val = newPath.reduce((o, k) => {
		return (o || {})[k]
	}, object);
	return !val || val !== undefined ? val : defaultVal
}


/**
 * 是否为 number 类型 
 * @param {any} num 需要判断的值
 * @return {Boolean} 是否为 number
 */
export const isNumber = (num) => {
	return !isNaN(Number(num))
}

/**
 * 是否为 boolean 类型 
 * @param {any} bool 需要判断的值
 * @return {Boolean} 是否为 boolean
 */
export const isBoolean = (bool) => {
	return (typeof bool === 'boolean')
}
/**
 * 是否有必填字段
 * @param {Object} rules 规则
 * @return {Boolean} 是否有必填字段
 */
export const isRequiredField = (rules) => {
	let isNoField = false;
	for (let i = 0; i < rules.length; i++) {
		const ruleData = rules[i];
		if (ruleData.required) {
			isNoField = true;
			break;
		}
	}
	return isNoField;
}


/**
 * 获取数据类型
 * @param {Any} obj 需要获取数据类型的值
 */
export const type = (obj) => {
	var class2type = {};

	// 生成class2type映射
	"Boolean Number String Function Array Date RegExp Object Error".split(" ").map(function(item, index) {
		class2type["[object " + item + "]"] = item.toLowerCase();
	})
	if (obj == null) {
		return obj + "";
	}
	return typeof obj === "object" || typeof obj === "function" ?
		class2type[Object.prototype.toString.call(obj)] || "object" :
		typeof obj;
}

/**
 * 判断两个值是否相等
 * @param {any} a 值  
 * @param {any} b 值  
 * @return {Boolean} 是否相等
 */
export const isEqual = (a, b) => {
	//如果a和b本来就全等
	if (a === b) {
		//判断是否为0和-0
		return a !== 0 || 1 / a === 1 / b;
	}
	//判断是否为null和undefined
	if (a == null || b == null) {
		return a === b;
	}
	//接下来判断a和b的数据类型
	var classNameA = toString.call(a),
		classNameB = toString.call(b);
	//如果数据类型不相等，则返回false
	if (classNameA !== classNameB) {
		return false;
	}
	//如果数据类型相等，再根据不同数据类型分别判断
	switch (classNameA) {
		case '[object RegExp]':
		case '[object String]':
			//进行字符串转换比较
			return '' + a === '' + b;
		case '[object Number]':
			//进行数字转换比较,判断是否为NaN
			if (+a !== +a) {
				return +b !== +b;
			}
			//判断是否为0或-0
			return +a === 0 ? 1 / +a === 1 / b : +a === +b;
		case '[object Date]':
		case '[object Boolean]':
			return +a === +b;
	}
	//如果是对象类型
	if (classNameA == '[object Object]') {
		//获取a和b的属性长度
		var propsA = Object.getOwnPropertyNames(a),
			propsB = Object.getOwnPropertyNames(b);
		if (propsA.length != propsB.length) {
			return false;
		}
		for (var i = 0; i < propsA.length; i++) {
			var propName = propsA[i];
			//如果对应属性对应值不相等，则返回false
			if (a[propName] !== b[propName]) {
				return false;
			}
		}
		return true;
	}
	//如果是数组类型
	if (classNameA == '[object Array]') {
		if (a.toString() == b.toString()) {
			return true;
		}
		return false;
	}
}
