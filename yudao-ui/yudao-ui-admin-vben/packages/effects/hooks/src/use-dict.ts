import { useDictStore } from '@vben/stores';
import { isObject } from '@vben/utils';

type ColorType = 'error' | 'info' | 'success' | 'warning';

export interface DictDataType {
  dictType?: string;
  label: string;
  value: boolean | number | string;
  colorType?: ColorType;
  cssClass?: string;
}

export interface NumberDictDataType extends DictDataType {
  value: number;
}

export interface StringDictDataType extends DictDataType {
  value: string;
}

/**
 * 获取字典标签
 *
 * @param dictType 字典类型
 * @param value 字典值
 * @returns 字典标签
 */
export function getDictLabel(dictType: string, value: any) {
  const dictStore = useDictStore();
  const dictObj = dictStore.getDictData(dictType, value);
  return isObject(dictObj) ? dictObj.label : '';
}

/**
 * 获取字典对象
 *
 * @param dictType 字典类型
 * @param value 字典值
 * @returns 字典对象
 */
export function getDictObj(dictType: string, value: any) {
  const dictStore = useDictStore();
  const dictObj = dictStore.getDictData(dictType, value);
  return isObject(dictObj) ? dictObj : null;
}

/**
 * 获取字典数组 用于select radio 等
 *
 * @param dictType 字典类型
 * @param valueType 字典值类型，默认 string 类型
 * @returns 字典数组
 */
export function getDictOptions(
  dictType: string,
  valueType: 'boolean' | 'number' | 'string' = 'string',
): DictDataType[] {
  const dictStore = useDictStore();
  const dictOpts = dictStore.getDictOptions(dictType);
  const dictOptions: DictDataType[] = [];
  if (dictOpts.length > 0) {
    let dictValue: boolean | number | string = '';
    dictOpts.forEach((d) => {
      switch (valueType) {
        case 'boolean': {
          dictValue = `${d.value}` === 'true';
          break;
        }
        case 'number': {
          dictValue = Number.parseInt(`${d.value}`);
          break;
        }
        case 'string': {
          dictValue = `${d.value}`;
          break;
        }
        // No default
      }
      dictOptions.push({
        value: dictValue,
        label: d.label,
      });
    });
  }
  return dictOptions.length > 0 ? dictOptions : [];
}
