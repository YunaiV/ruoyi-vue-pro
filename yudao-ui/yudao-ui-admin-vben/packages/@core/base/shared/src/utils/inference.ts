// eslint-disable-next-line vue/prefer-import-from-vue
import { isFunction, isObject, isString } from '@vue/shared';

/**
 * 检查传入的值是否为undefined。
 *
 * @param {unknown} value 要检查的值。
 * @returns {boolean} 如果值是undefined，返回true，否则返回false。
 */
function isUndefined(value?: unknown): value is undefined {
  return value === undefined;
}

/**
 * 检查传入的值是否为boolean
 * @param value
 * @returns 如果值是布尔值，返回true，否则返回false。
 */
function isBoolean(value: unknown): value is boolean {
  return typeof value === 'boolean';
}

/**
 * 检查传入的值是否为空。
 *
 * 以下情况将被认为是空：
 * - 值为null。
 * - 值为undefined。
 * - 值为一个空字符串。
 * - 值为一个长度为0的数组。
 * - 值为一个没有元素的Map或Set。
 * - 值为一个没有属性的对象。
 *
 * @param {T} value 要检查的值。
 * @returns {boolean} 如果值为空，返回true，否则返回false。
 */
function isEmpty<T = unknown>(value?: T): value is T {
  if (value === null || value === undefined) {
    return true;
  }

  if (Array.isArray(value) || isString(value)) {
    return value.length === 0;
  }

  if (value instanceof Map || value instanceof Set) {
    return value.size === 0;
  }

  if (isObject(value)) {
    return Object.keys(value).length === 0;
  }

  return false;
}

/**
 * 检查传入的字符串是否为有效的HTTP或HTTPS URL。
 *
 * @param {string} url 要检查的字符串。
 * @return {boolean} 如果字符串是有效的HTTP或HTTPS URL，返回true，否则返回false。
 */
function isHttpUrl(url?: string): boolean {
  if (!url) {
    return false;
  }
  // 使用正则表达式测试URL是否以http:// 或 https:// 开头
  const httpRegex = /^https?:\/\/.*$/;
  return httpRegex.test(url);
}

/**
 * 检查传入的值是否为window对象。
 *
 * @param {any} value 要检查的值。
 * @returns {boolean} 如果值是window对象，返回true，否则返回false。
 */
function isWindow(value: any): value is Window {
  return (
    typeof window !== 'undefined' && value !== null && value === value.window
  );
}

/**
 * 检查当前运行环境是否为Mac OS。
 *
 * 这个函数通过检查navigator.userAgent字符串来判断当前运行环境。
 * 如果userAgent字符串中包含"macintosh"或"mac os x"（不区分大小写），则认为当前环境是Mac OS。
 *
 * @returns {boolean} 如果当前环境是Mac OS，返回true，否则返回false。
 */
function isMacOs(): boolean {
  const macRegex = /macintosh|mac os x/i;
  return macRegex.test(navigator.userAgent);
}

/**
 * 检查当前运行环境是否为Windows OS。
 *
 * 这个函数通过检查navigator.userAgent字符串来判断当前运行环境。
 * 如果userAgent字符串中包含"windows"或"win32"（不区分大小写），则认为当前环境是Windows OS。
 *
 * @returns {boolean} 如果当前环境是Windows OS，返回true，否则返回false。
 */
function isWindowsOs(): boolean {
  const windowsRegex = /windows|win32/i;
  return windowsRegex.test(navigator.userAgent);
}

/**
 * 检查传入的值是否为数字
 * @param value
 */
function isNumber(value: any): value is number {
  return typeof value === 'number' && Number.isFinite(value);
}

/**
 * Returns the first value in the provided list that is neither `null` nor `undefined`.
 *
 * This function iterates over the input values and returns the first one that is
 * not strictly equal to `null` or `undefined`. If all values are either `null` or
 * `undefined`, it returns `undefined`.
 *
 * @template T - The type of the input values.
 * @param {...(T | null | undefined)[]} values - A list of values to evaluate.
 * @returns {T | undefined} - The first value that is not `null` or `undefined`, or `undefined` if none are found.
 *
 * @example
 * // Returns 42 because it is the first non-null, non-undefined value.
 * getFirstNonNullOrUndefined(undefined, null, 42, 'hello'); // 42
 *
 * @example
 * // Returns 'hello' because it is the first non-null, non-undefined value.
 * getFirstNonNullOrUndefined(null, undefined, 'hello', 123); // 'hello'
 *
 * @example
 * // Returns undefined because all values are either null or undefined.
 * getFirstNonNullOrUndefined(undefined, null); // undefined
 */
function getFirstNonNullOrUndefined<T>(
  ...values: (null | T | undefined)[]
): T | undefined {
  for (const value of values) {
    if (value !== undefined && value !== null) {
      return value;
    }
  }
  return undefined;
}

export {
  getFirstNonNullOrUndefined,
  isBoolean,
  isEmpty,
  isFunction,
  isHttpUrl,
  isMacOs,
  isNumber,
  isObject,
  isString,
  isUndefined,
  isWindow,
  isWindowsOs,
};
