/**
 * 通用js方法封装处理
 * Copyright (c) 2019 ruoyi
 */

// 日期格式化
export function dateFormat(date, pattern) {
	var d = new Date(date).Format("yyyy-MM-dd hh:mm:ss");
	if (pattern) {
		d = new Date(date).Format(pattern);
	}
	return d.toLocaleString();
}

// 表单重置
export function resetForm(refName) {
	if (this.$refs[refName] !== undefined) {
		this.$refs[refName].resetFields();
	}
}

// 添加日期范围
export function addDateRange(params, dateRange) {
	var search = params;
	if (null != dateRange) {
		search.params = {
			beginTime: this.dateRange[0],
			endTime: this.dateRange[1]
		};
	}
	return search;
}

// 回显数据字典
export function selectDictLabel(datas, value) {
	var actions = [];
	Object.keys(datas).map((key) => {
		if (datas[key].dictValue == ('' + value)) {
			actions.push(datas[key].dictLabel);
			return false;
		}
	})
	return actions.join('');
}

// 字符串格式化(%s )
export function sprintf(str) {
	var args = arguments, flag = true, i = 1;
	str = str.replace(/%s/g, function () {
		var arg = args[i++];
		if (typeof arg === 'undefined') {
			flag = false;
			return '';
		}
		return arg;
	});
	return flag ? str : '';
}

Date.prototype.Format = function (fmt) {
	var o = {
		"M+": this.getMonth() + 1,                   // 月份
		"d+": this.getDate(),                        // 日
		"h+": this.getHours(),                       // 小时
		"m+": this.getMinutes(),                     // 分
		"s+": this.getSeconds(),                     // 秒
		"q+": Math.floor((this.getMonth() + 3) / 3), // 季度
		"S": this.getMilliseconds()                  // 毫秒
	};
	if (/(y+)/.test(fmt))
		fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
	for (var k in o)
		if (new RegExp("(" + k + ")").test(fmt))
			fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
	return fmt;
}  