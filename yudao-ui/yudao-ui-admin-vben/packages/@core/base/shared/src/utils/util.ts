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
export const getUrlNumberValue = (
  key: string,
  urlStr: string = location.href,
): number => {
  return Number(getUrlValue(key, urlStr));
};

/**
 * 获取链接的参数值
 * @param key 参数键名
 * @param urlStr 链接地址，默认为当前浏览器的地址
 */
export const getUrlValue = (
  key: string,
  urlStr: string = location.href,
): string => {
  if (!urlStr || !key) return '';
  const url = new URL(decodeURIComponent(urlStr));
  return url.searchParams.get(key) ?? '';
};
