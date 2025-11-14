export function isArray(value) {
  if (typeof Array.isArray === 'function') {
    return Array.isArray(value);
  } else {
    return Object.prototype.toString.call(value) === '[object Array]';
  }
}

export function isObject(value) {
  return Object.prototype.toString.call(value) === '[object Object]';
}

export function isNumber(value) {
  return !isNaN(Number(value));
}

export function isFunction(value) {
  return typeof value == 'function';
}

export function isString(value) {
  return typeof value == 'string';
}

export function isEmpty(value) {
  if (value === '' || value === undefined || value === null) {
    return true;
  }

  if (isArray(value)) {
    return value.length === 0;
  }

  if (isObject(value)) {
    return Object.keys(value).length === 0;
  }

  return false;
}

export function isBoolean(value) {
  return typeof value === 'boolean';
}

export function last(data) {
  if (isArray(data) || isString(data)) {
    return data[data.length - 1];
  }
}

export function cloneDeep(obj) {
  const d = isArray(obj) ? [...obj] : {};

  if (isObject(obj)) {
    for (const key in obj) {
      if (obj[key]) {
        if (obj[key] && typeof obj[key] === 'object') {
          d[key] = cloneDeep(obj[key]);
        } else {
          d[key] = obj[key];
        }
      }
    }
  }

  return d;
}

export function clone(obj) {
  return Object.create(Object.getPrototypeOf(obj), Object.getOwnPropertyDescriptors(obj));
}

export function deepMerge(a, b) {
  let k;
  for (k in b) {
    a[k] = a[k] && a[k].toString() === '[object Object]' ? deepMerge(a[k], b[k]) : (a[k] = b[k]);
  }
  return a;
}

export function contains(parent, node) {
  while (node && (node = node.parentNode)) if (node === parent) return true;
  return false;
}

export function orderBy(list, key) {
  return list.sort((a, b) => a[key] - b[key]);
}

export function deepTree(list) {
  const newList = [];
  const map = {};

  list.forEach((e) => (map[e.id] = e));

  list.forEach((e) => {
    const parent = map[e.parentId];

    if (parent) {
      (parent.children || (parent.children = [])).push(e);
    } else {
      newList.push(e);
    }
  });

  const fn = (list) => {
    list.map((e) => {
      if (e.children instanceof Array) {
        e.children = orderBy(e.children, 'orderNum');

        fn(e.children);
      }
    });
  };

  fn(newList);

  return orderBy(newList, 'orderNum');
}

export function revDeepTree(list = []) {
  const d = [];
  let id = 0;

  const deep = (list, parentId) => {
    list.forEach((e) => {
      if (!e.id) {
        e.id = id++;
      }

      e.parentId = parentId;

      d.push(e);

      if (e.children && isArray(e.children)) {
        deep(e.children, e.id);
      }
    });
  };

  deep(list || [], null);

  return d;
}

export function basename(path) {
  let index = path.lastIndexOf('/');
  index = index > -1 ? index : path.lastIndexOf('\\');
  if (index < 0) {
    return path;
  }
  return path.substring(index + 1);
}

export function isWxBrowser() {
  const ua = navigator.userAgent.toLowerCase();
  if (ua.match(/MicroMessenger/i) == 'micromessenger') {
    return true;
  } else {
    return false;
  }
}

/**
 * @description 如果value小于min，取min；如果value大于max，取max
 * @param {number} min
 * @param {number} max
 * @param {number} value
 */
export function range(min = 0, max = 0, value = 0) {
  return Math.max(min, Math.min(max, Number(value)));
}

import dayjs from 'dayjs';

/**
 * 将一个整数转换为分数保留两位小数
 * @param {number | string | undefined} num 整数
 * @return {number} 分数
 */
export const formatToFraction = (num) => {
  if (typeof num === 'undefined') return 0;
  const parsedNumber = typeof num === 'string' ? parseFloat(num) : num;
  return parseFloat((parsedNumber / 100).toFixed(2));
};

/**
 * 将一个数转换为 1.00 这样
 * 数据呈现的时候使用
 *
 * @param {number | string | undefined} num 整数
 * @return {string} 分数
 */
export const floatToFixed2 = (num) => {
  let str = '0.00';
  if (typeof num === 'undefined') {
    return str;
  }
  const f = formatToFraction(num);
  const decimalPart = f.toString().split('.')[1];
  const len = decimalPart ? decimalPart.length : 0;
  switch (len) {
    case 0:
      str = f.toString() + '.00';
      break;
    case 1:
      str = f.toString() + '.0';
      break;
    case 2:
      str = f.toString();
      break;
  }
  return str;
};

/**
 * 时间日期转换
 * @param {dayjs.ConfigType} date 当前时间，new Date() 格式
 * @param {string} format 需要转换的时间格式字符串
 * @description format 字符串随意，如 `YYYY-mm、YYYY-mm-dd`
 * @description format 季度："YYYY-mm-dd HH:MM:SS QQQQ"
 * @description format 星期："YYYY-mm-dd HH:MM:SS WWW"
 * @description format 几周："YYYY-mm-dd HH:MM:SS ZZZ"
 * @description format 季度 + 星期 + 几周："YYYY-mm-dd HH:MM:SS WWW QQQQ ZZZ"
 * @returns {string} 返回拼接后的时间字符串
 */
export function formatDate(date, format = 'YYYY-MM-DD HH:mm:ss') {
  // 日期不存在，则返回空
  if (!date) {
    return '';
  }
  // 日期存在，则进行格式化
  if (format === undefined) {
    format = 'YYYY-MM-DD HH:mm:ss';
  }
  return dayjs(date).format(format);
}

/**
 * 构造树型结构数据
 *
 * @param {*} data 数据源
 * @param {*} id id字段 默认 'id'
 * @param {*} parentId 父节点字段 默认 'parentId'
 * @param {*} children 孩子节点字段 默认 'children'
 * @param {*} rootId 根Id 默认 0
 */
export function handleTree(
  data,
  id = 'id',
  parentId = 'parentId',
  children = 'children',
  rootId = 0,
) {
  // 对源数据深度克隆
  const cloneData = JSON.parse(JSON.stringify(data));
  // 循环所有项
  const treeData = cloneData.filter((father) => {
    let branchArr = cloneData.filter((child) => {
      //返回每一项的子级数组
      return father[id] === child[parentId];
    });
    branchArr.length > 0 ? (father.children = branchArr) : '';
    //返回第一层
    return father[parentId] === rootId;
  });
  return treeData !== '' ? treeData : data;
}

/**
 * 重置分页对象
 *
 * @param pagination 分页对象
 */
export function resetPagination(pagination) {
  pagination.list = [];
  pagination.total = 0;
  pagination.pageNo = 1;
}

/**
 * 将值复制到目标对象，且以目标对象属性为准，例：target: {a:1} source:{a:2,b:3} 结果为：{a:2}
 * @param target 目标对象
 * @param source 源对象
 */
export const copyValueToTarget = (target, source) => {
  const newObj = Object.assign({}, target, source);
  // 删除多余属性
  Object.keys(newObj).forEach((key) => {
    // 如果不是target中的属性则删除
    if (Object.keys(target).indexOf(key) === -1) {
      delete newObj[key];
    }
  });
  // 更新目标对象值
  Object.assign(target, newObj);
};

/**
 * 解析 JSON 字符串
 *
 * @param str
 */
export function jsonParse(str) {
  try {
    return JSON.parse(str);
  } catch (e) {
    console.warn(`str[${str}] 不是一个 JSON 字符串`);
    return str;
  }
}

/**
 * 获得当前周的开始和结束时间
 */
export function getWeekTimes() {
  const today = new Date();
  const dayOfWeek = today.getDay();
  return [
    new Date(today.getFullYear(), today.getMonth(), today.getDate() - dayOfWeek, 0, 0, 0),
    new Date(today.getFullYear(), today.getMonth(), today.getDate() + (6 - dayOfWeek), 23, 59, 59),
  ];
}

/**
 * 获得当前月的开始和结束时间
 */
export function getMonthTimes() {
  const today = new Date();
  const year = today.getFullYear();
  const month = today.getMonth();
  const startDate = new Date(year, month, 1, 0, 0, 0);
  const nextMonth = new Date(year, month + 1, 1);
  const endDate = new Date(nextMonth.getTime() - 1);
  return [startDate, endDate];
}
