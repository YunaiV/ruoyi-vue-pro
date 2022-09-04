/**
 * 通用js方法封装处理
 * Copyright (c) 2019 ruoyi
 */

const baseURL = process.env.VUE_APP_BASE_API

// 日期格式化
export function parseTime(time, pattern) {
  if (arguments.length === 0 || !time) {
    return null
  }
  const format = pattern || '{y}-{m}-{d} {h}:{i}:{s}'
  let date
  if (typeof time === 'object') {
    date = time
  } else {
    if ((typeof time === 'string') && (/^[0-9]+$/.test(time))) {
      time = parseInt(time)
    } else if (typeof time === 'string') {
      time = time.replace(new RegExp(/-/gm), '/').replace('T', ' ').replace(new RegExp(/\.[\d]{3}/gm),'');
    }
    if ((typeof time === 'number') && (time.toString().length === 10)) {
      time = time * 1000
    }
    date = new Date(time)
  }
  const formatObj = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay()
  }
  const time_str = format.replace(/{(y|m|d|h|i|s|a)+}/g, (result, key) => {
    let value = formatObj[key]
    // Note: getDay() returns 0 on Sunday
    if (key === 'a') {
      return ['日', '一', '二', '三', '四', '五', '六'][value]
    }
    if (result.length > 0 && value < 10) {
      value = '0' + value
    }
    return value || 0
  })
  return time_str
}

// 表单重置
export function resetForm(refName) {
  if (this.$refs[refName]) {
    this.$refs[refName].resetFields();
  }
}

// 添加日期范围
export function addDateRange(params, dateRange, propName) {
  const search = params;
  search.params = {};
  if (null != dateRange && '' !== dateRange) {
    if (typeof (propName) === "undefined") {
      search["beginTime"] = dateRange[0];
      search["endTime"] = dateRange[1];
    } else {
      search["begin" + propName] = dateRange[0];
      search["end" + propName] = dateRange[1];
    }
  }
  return search;
}

/**
 * 添加开始和结束时间到 params 参数中
 *
 * @param params 参数
 * @param dateRange 时间范围。
 *                大小为 2 的数组，每个时间为 yyyy-MM-dd 格式
 * @param propName 加入的参数名，可以为空
 */
export function addBeginAndEndTime(params, dateRange, propName) {
  // 必须传入参数
  if (!dateRange) {
    return params;
  }
  // 如果未传递 propName 属性，默认为 time
  if (!propName) {
    propName = 'Time';
  } else {
    propName = propName.charAt(0).toUpperCase() + propName.slice(1);
  }
  // 设置参数
  if (dateRange[0]) {
    params['begin' + propName] = dateRange[0] + ' 00:00:00';
  }
  if (dateRange[1]) {
    params['end' + propName] = dateRange[1] + ' 23:59:59';
  }
  return params;
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

// 转换字符串，undefined,null等转化为""
export function praseStrEmpty(str) {
  if (!str || str == "undefined" || str == "null") {
    return "";
  }
  return str;
}

/**
 * 构造树型结构数据
 * @param {*} data 数据源
 * @param {*} id id字段 默认 'id'
 * @param {*} parentId 父节点字段 默认 'parentId'
 * @param {*} children 孩子节点字段 默认 'children'
 * @param {*} rootId 根Id 默认 0
 */
export function handleTree(data, id, parentId, children, rootId) {
  id = id || 'id'
  parentId = parentId || 'parentId'
  children = children || 'children'
  rootId = rootId || Math.min.apply(Math, data.map(item => {
    return item[parentId]
  })) || 0
  //对源数据深度克隆
  const cloneData = JSON.parse(JSON.stringify(data))
  //循环所有项
  const treeData = cloneData.filter(father => {
    let branchArr = cloneData.filter(child => {
      //返回每一项的子级数组
      return father[id] === child[parentId]
    });
    branchArr.length > 0 ? father.children = branchArr : '';
    //返回第一层
    return father[parentId] === rootId;
  });
  return treeData !== '' ? treeData : data;
}

/**
 * 获取当前时间
 * @param timeStr 时分秒 字符串 格式为 xx:xx:xx
 */
export function getNowDateTime(timeStr) {
  let now = new Date();
  let year = now.getFullYear(); //得到年份
  let month = (now.getMonth() + 1).toString().padStart(2, "0"); //得到月份
  let day = now.getDate().toString().padStart(2, "0"); //得到日期

  if (timeStr != null) {
    return `${year}-${month}-${day} ${timeStr}`;
  }
  let hours = now.getHours().toString().padStart(2, "0") // 得到小时;
  let minutes = now.getMinutes().toString().padStart(2, "0") // 得到分钟;
  let seconds = now.getSeconds().toString().padStart(2, "0") // 得到秒;
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

/**
 * 获得租户功能是否开启
 */
export function getTenantEnable() {
  // console.log("enable: " + process.env.VUE_APP_TENANT_ENABLE)
  if (process.env.VUE_APP_TENANT_ENABLE === "true") {
    return true;
  }
  if (process.env.VUE_APP_TENANT_ENABLE === "false") {
    return false;
  }
  return process.env.VUE_APP_TENANT_ENABLE || true;
}

/**
 * 获得验证码功能是否开启
 */
export function getCaptchaEnable() {
  if (process.env.VUE_APP_CAPTCHA_ENABLE === "true") {
    return true;
  }
  if (process.env.VUE_APP_CAPTCHA_ENABLE === "false") {
    return false;
  }
  return process.env.VUE_APP_CAPTCHA_ENABLE || true;
}

/**
 * 获得文档是否开启
 */
export function getDocEnable() {
  if (process.env.VUE_APP_DOC_ENABLE === "true") {
    return true;
  }
  if (process.env.VUE_APP_DOC_ENABLE === "false") {
    return false;
  }
  return process.env.VUE_APP_DOC_ENABLE || false;
}

/**
 * 获得 Vue 应用的基础路径
 */
export function getBasePath() {
  return process.env.VUE_APP_APP_NAME || '/';
}

/**
 * 获得 Vue 应用的访问路径
 *
 * @param path 路径
 */
export function getPath(path) {
  // 基础路径，必须以 / 结尾
  let basePath = getBasePath();
  if (!basePath.endsWith('/')) {
    return basePath + '/';
  }
  // 访问路径，必须不能以 / 开头
  if (path.startsWith('/')) {
    path = path.substring(1);
  }
  return basePath + path;
}

/**
 * 除法保留两位小数
 *
 * @param {*} divisor 除数
 * @param {*} dividend 被除数
 * @returns
 */
 export function divide(divisor, dividend) {
  if(divisor == null || dividend == null || dividend == 0){
    return null;
  }
  return Math.floor(divisor/dividend*100)/100;
}
