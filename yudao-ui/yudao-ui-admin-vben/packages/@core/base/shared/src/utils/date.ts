import dayjs from 'dayjs';

export function formatDate(
  time: Date | number | string | undefined,
  format = 'YYYY-MM-DD',
) {
  if (!time) {
    return time;
  }
  try {
    const date = dayjs(time);
    if (!date.isValid()) {
      throw new Error('Invalid date');
    }
    return date.format(format);
  } catch (error) {
    console.error(`Error formatting date: ${error}`);
    return time;
  }
}

export function formatDateTime(time: Date | number | string | undefined) {
  if (!time) {
    return time;
  }
  return formatDate(time, 'YYYY-MM-DD HH:mm:ss');
}

export function formatDate2(date: Date, format?: string): string {
  // 日期不存在，则返回空
  if (!date) {
    return '';
  }
  // 日期存在，则进行格式化
  return date ? dayjs(date).format(format ?? 'YYYY-MM-DD HH:mm:ss') : '';
}

export function isDate(value: any): value is Date {
  return value instanceof Date;
}

export function isDayjsObject(value: any): value is dayjs.Dayjs {
  return dayjs.isDayjs(value);
}

/**
 * element plus 的时间 Formatter 实现，使用 YYYY-MM-DD HH:mm:ss 格式
 *
 * @param _row
 * @param _column
 * @param cellValue 字段值
 */
export function dateFormatter(_row: any, _column: any, cellValue: any): string {
  return cellValue ? formatDate(cellValue)?.toString() || '' : '';
}
