import type { Recordable } from '@vben/types';

export * from './rangePickerProps';
export * from './routerHelper';

/**
 * 查找数组对象的某个下标
 * @param {Array} ary 查找的数组
 * @param {Function} fn 判断的方法
 */
type Fn<T = any> = (item: T, index: number, array: Array<T>) => boolean;
export const findIndex = <T = Recordable<any>>(
  ary: Array<T>,
  fn: Fn<T>,
): number => {
  if (ary.findIndex) {
    return ary.findIndex((item, index, array) => fn(item, index, array));
  }
  let index = -1;
  ary.some((item: T, i: number, ary: Array<T>) => {
    const ret: boolean = fn(item, i, ary);
    if (ret) {
      index = i;
      return true;
    }
    return false;
  });
  return index;
};
