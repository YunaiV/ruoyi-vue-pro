export function bindMethods<T extends object>(instance: T): void {
  const prototype = Object.getPrototypeOf(instance);
  const propertyNames = Object.getOwnPropertyNames(prototype);

  propertyNames.forEach((propertyName) => {
    const descriptor = Object.getOwnPropertyDescriptor(prototype, propertyName);
    const propertyValue = instance[propertyName as keyof T];

    if (
      typeof propertyValue === 'function' &&
      propertyName !== 'constructor' &&
      descriptor &&
      !descriptor.get &&
      !descriptor.set
    ) {
      instance[propertyName as keyof T] = propertyValue.bind(instance);
    }
  });
}

/**
 * 获取嵌套对象的字段值
 * @param obj - 要查找的对象
 * @param path - 用于查找字段的路径，使用小数点分隔
 * @returns 字段值，或者未找到时返回 undefined
 */
export function getNestedValue<T>(obj: T, path: string): any {
  if (typeof path !== 'string' || path.length === 0) {
    throw new Error('Path must be a non-empty string');
  }
  // 把路径字符串按 "." 分割成数组
  const keys = path.split('.') as (number | string)[];

  let current: any = obj;

  for (const key of keys) {
    if (current === null || current === undefined) {
      return undefined;
    }
    current = current[key as keyof typeof current];
  }

  return current;
}

/**
 * 获取链接的参数值（值类型）
 * @param key 参数键名
 * @param urlStr 链接地址，默认为当前浏览器的地址
 */
export function getUrlNumberValue(
  key: string,
  urlStr: string = location.href,
): number {
  return Number(getUrlValue(key, urlStr));
}

/**
 * 获取链接的参数值
 * @param key 参数键名
 * @param urlStr 链接地址，默认为当前浏览器的地址
 */
export function getUrlValue(
  key: string,
  urlStr: string = location.href,
): string {
  if (!urlStr || !key) return '';
  const url = new URL(decodeURIComponent(urlStr));
  return url.searchParams.get(key) ?? '';
}

/**
 * 将值复制到目标对象，且以目标对象属性为准，例：target: {a:1} source:{a:2,b:3} 结果为：{a:2}
 * @param target 目标对象
 * @param source 源对象
 */
export function copyValueToTarget(target: any, source: any) {
  const newObj = Object.assign({}, target, source);
  // 删除多余属性
  Object.keys(newObj).forEach((key) => {
    // 如果不是target中的属性则删除
    if (!Object.keys(target).includes(key)) {
      // eslint-disable-next-line @typescript-eslint/no-dynamic-delete
      delete newObj[key];
    }
  });
  // 更新目标对象值
  Object.assign(target, newObj);
}

/** 实现 groupBy 功能 */
export function groupBy(array: any[], key: string) {
  const result: Record<string, any[]> = {};
  for (const item of array) {
    const groupKey = item[key];
    if (!result[groupKey]) {
      result[groupKey] = [];
    }
    result[groupKey].push(item);
  }
  return result;
}

/**
 * 解析 JSON 字符串
 *
 * @param str
 */
export function jsonParse(str: string) {
  try {
    return JSON.parse(str);
  } catch {
    console.warn(`str[${str}] 不是一个 JSON 字符串`);
    return str;
  }
}
