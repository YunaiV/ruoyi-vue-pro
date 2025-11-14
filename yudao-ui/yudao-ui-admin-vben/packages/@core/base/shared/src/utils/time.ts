import dayjs from 'dayjs';

import { formatDate } from './date';

/**
 * @param {Date | number | string} time 需要转换的时间
 * @param {string} fmt 需要转换的格式 如 yyyy-MM-dd、yyyy-MM-dd HH:mm:ss
 */
export function formatTime(time: Date | number | string, fmt: string) {
  if (time) {
    const date = new Date(time);
    const o = {
      'M+': date.getMonth() + 1,
      'd+': date.getDate(),
      'H+': date.getHours(),
      'm+': date.getMinutes(),
      's+': date.getSeconds(),
      'q+': Math.floor((date.getMonth() + 3) / 3),
      S: date.getMilliseconds(),
    };
    const yearMatch = fmt.match(/y+/);
    if (yearMatch) {
      fmt = fmt.replace(
        yearMatch[0],
        `${date.getFullYear()}`.slice(4 - yearMatch[0].length),
      );
    }
    for (const k in o) {
      const match = fmt.match(new RegExp(`(${k})`));
      if (match) {
        fmt = fmt.replace(
          match[0],
          match[0].length === 1
            ? (o[k as keyof typeof o] as any)
            : `00${o[k as keyof typeof o]}`.slice(
                `${o[k as keyof typeof o]}`.length,
              ),
        );
      }
    }
    return fmt;
  } else {
    return '';
  }
}

/**
 * 获取当前日期是第几周
 * @param dateTime 当前传入的日期值
 * @returns 返回第几周数字值
 */
export function getWeek(dateTime: Date): number {
  const temptTime = new Date(dateTime);
  // 周几
  const weekday = temptTime.getDay() || 7;
  // 周1+5天=周六
  temptTime.setDate(temptTime.getDate() - weekday + 1 + 5);
  let firstDay = new Date(temptTime.getFullYear(), 0, 1);
  const dayOfWeek = firstDay.getDay();
  let spendDay = 1;
  if (dayOfWeek !== 0) spendDay = 7 - dayOfWeek + 1;
  firstDay = new Date(temptTime.getFullYear(), 0, 1 + spendDay);
  const d = Math.ceil((temptTime.valueOf() - firstDay.valueOf()) / 86_400_000);
  return Math.ceil(d / 7);
}

/**
 * 将时间转换为 `几秒前`、`几分钟前`、`几小时前`、`几天前`
 * @param param 当前时间，new Date() 格式或者字符串时间格式
 * @param format 需要转换的时间格式字符串
 * @description param 10秒：  10 * 1000
 * @description param 1分：   60 * 1000
 * @description param 1小时： 60 * 60 * 1000
 * @description param 24小时：60 * 60 * 24 * 1000
 * @description param 3天：   60 * 60* 24 * 1000 * 3
 * @returns 返回拼接后的时间字符串
 */
export function formatPast(
  param: Date | string,
  format = 'YYYY-MM-DD HH:mm:ss',
): string {
  // 传入格式处理、存储转换值
  let s: number, t: any;
  // 获取js 时间戳
  let time: number = Date.now();
  // 是否是对象
  typeof param === 'string' || typeof param === 'object'
    ? (t = new Date(param).getTime())
    : (t = param);
  // 当前时间戳 - 传入时间戳
  time = Number.parseInt(`${time - t}`);
  if (time < 10_000) {
    // 10秒内
    return '刚刚';
  } else if (time < 60_000 && time >= 10_000) {
    // 超过10秒少于1分钟内
    s = Math.floor(time / 1000);
    return `${s}秒前`;
  } else if (time < 3_600_000 && time >= 60_000) {
    // 超过1分钟少于1小时
    s = Math.floor(time / 60_000);
    return `${s}分钟前`;
  } else if (time < 86_400_000 && time >= 3_600_000) {
    // 超过1小时少于24小时
    s = Math.floor(time / 3_600_000);
    return `${s}小时前`;
  } else if (time < 259_200_000 && time >= 86_400_000) {
    // 超过1天少于3天内
    s = Math.floor(time / 86_400_000);
    return `${s}天前`;
  } else {
    // 超过3天
    const date =
      typeof param === 'string' || typeof param === 'object'
        ? new Date(param)
        : param;
    return formatDate(date, format) as string;
  }
}

/**
 * 时间问候语
 * @param param 当前时间，new Date() 格式
 * @description param 调用 `formatAxis(new Date())` 输出 `上午好`
 * @returns 返回拼接后的时间字符串
 */
export function formatAxis(param: Date): string {
  const hour: number = new Date(param).getHours();
  if (hour < 6) return '凌晨好';
  else if (hour < 9) return '早上好';
  else if (hour < 12) return '上午好';
  else if (hour < 14) return '中午好';
  else if (hour < 17) return '下午好';
  else if (hour < 19) return '傍晚好';
  else if (hour < 22) return '晚上好';
  else return '夜里好';
}

/**
 * 将毫秒，转换成时间字符串。例如说，xx 分钟
 *
 * @param ms 毫秒
 * @returns {string} 字符串
 */
export function formatPast2(ms: number): string {
  const day = Math.floor(ms / (24 * 60 * 60 * 1000));
  const hour = Math.floor(ms / (60 * 60 * 1000) - day * 24);
  const minute = Math.floor(ms / (60 * 1000) - day * 24 * 60 - hour * 60);
  const second = Math.floor(
    ms / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - minute * 60,
  );
  if (day > 0) {
    return `${day} 天${hour} 小时 ${minute} 分钟`;
  }
  if (hour > 0) {
    return `${hour} 小时 ${minute} 分钟`;
  }
  if (minute > 0) {
    return `${minute} 分钟`;
  }
  return second > 0 ? `${second} 秒` : `${0} 秒`;
}

/**
 * 设置起始日期，时间为00:00:00
 * @param param 传入日期
 * @returns 带时间00:00:00的日期
 */
export function beginOfDay(param: Date): Date {
  return new Date(
    param.getFullYear(),
    param.getMonth(),
    param.getDate(),
    0,
    0,
    0,
  );
}

/**
 * 设置结束日期，时间为23:59:59
 * @param param 传入日期
 * @returns 带时间23:59:59的日期
 */
export function endOfDay(param: Date): Date {
  return new Date(
    param.getFullYear(),
    param.getMonth(),
    param.getDate(),
    23,
    59,
    59,
  );
}

/**
 * 计算两个日期间隔天数
 * @param param1 日期1
 * @param param2 日期2
 */
export function betweenDay(param1: Date, param2: Date): number {
  param1 = convertDate(param1);
  param2 = convertDate(param2);
  // 计算差值
  return Math.floor((param2.getTime() - param1.getTime()) / (24 * 3600 * 1000));
}

/**
 * 日期计算
 * @param param1 日期
 * @param param2 添加的时间
 */
export function addTime(param1: Date, param2: number): Date {
  param1 = convertDate(param1);
  return new Date(param1.getTime() + param2);
}

/**
 * 日期转换
 * @param param 日期
 */
export function convertDate(param: Date | string): Date {
  if (typeof param === 'string') {
    return new Date(param);
  }
  return param;
}

/**
 * 指定的两个日期, 是否为同一天
 * @param a 日期 A
 * @param b 日期 B
 */
export function isSameDay(a: dayjs.ConfigType, b: dayjs.ConfigType): boolean {
  if (!a || !b) return false;

  const aa = dayjs(a);
  const bb = dayjs(b);
  return (
    aa.year() === bb.year() &&
    aa.month() === bb.month() &&
    aa.day() === bb.day()
  );
}

/**
 * 获取一天的开始时间、截止时间
 * @param date 日期
 * @param days 天数
 */
export function getDayRange(
  date: dayjs.ConfigType,
  days: number,
): [dayjs.ConfigType, dayjs.ConfigType] {
  const day = dayjs(date).add(days, 'd');
  return getDateRange(day, day);
}

/**
 * 获取最近7天的开始时间、截止时间
 */
export function getLast7Days(): [dayjs.ConfigType, dayjs.ConfigType] {
  const lastWeekDay = dayjs().subtract(7, 'd');
  const yesterday = dayjs().subtract(1, 'd');
  return getDateRange(lastWeekDay, yesterday);
}

/**
 * 获取最近30天的开始时间、截止时间
 */
export function getLast30Days(): [dayjs.ConfigType, dayjs.ConfigType] {
  const lastMonthDay = dayjs().subtract(30, 'd');
  const yesterday = dayjs().subtract(1, 'd');
  return getDateRange(lastMonthDay, yesterday);
}

/**
 * 获取最近1年的开始时间、截止时间
 */
export function getLast1Year(): [dayjs.ConfigType, dayjs.ConfigType] {
  const lastYearDay = dayjs().subtract(1, 'y');
  const yesterday = dayjs().subtract(1, 'd');
  return getDateRange(lastYearDay, yesterday);
}

/**
 * 获取指定日期的开始时间、截止时间
 * @param beginDate 开始日期
 * @param endDate 截止日期
 */
export function getDateRange(
  beginDate: dayjs.ConfigType,
  endDate: dayjs.ConfigType,
): [string, string] {
  return [
    dayjs(beginDate).startOf('d').format('YYYY-MM-DD HH:mm:ss'),
    dayjs(endDate).endOf('d').format('YYYY-MM-DD HH:mm:ss'),
  ];
}
