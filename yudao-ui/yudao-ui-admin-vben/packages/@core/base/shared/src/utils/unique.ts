/**
 * 根据指定字段对对象数组进行去重
 * @param arr 要去重的对象数组
 * @param key 去重依据的字段名
 * @returns 去重后的对象数组
 */
function uniqueByField<T>(arr: T[], key: keyof T): T[] {
  const seen = new Map<any, T>();
  return arr.filter((item) => {
    const value = item[key];
    return seen.has(value) ? false : (seen.set(value, item), true);
  });
}

export { uniqueByField };
