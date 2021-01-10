/**
 * Created by 芋道源码
 *
 * 数据字典工具类
 */
import store from '@/store'

export const DICT_TYPE = {
  SYS_COMMON_STATUS: 'sys_common_status',
  SYS_MENU_TYPE: 'sys_menu_type',
  SYS_ROLE_TYPE: 'sys_role_type',
  SYS_DATA_SCOPE: 'sys_data_scope',
  SYS_USER_SEX: 'sys_user_sex',
}

/**
 * 获取 dictType 对应的数据字典数组
 *
 * @param dictType 数据类型
 * @returns {*|Array} 数据字典数组
 */
export function getDictDatas(dictType) {
  return store.getters.dict_datas[dictType] || []
}

export function getDictDataLabel(dictType, value) {
  // 获取 dictType 对应的数据字典数组
  const dictDatas = getDictDatas(dictType)
  if (!dictDatas || dictDatas.length === 0) {
    return ''
  }
  // 获取 value 对应的展示名
  value = value + '' // 强制转换成字符串，因为 DictData 小类数值，是字符串
  for (const dictData of dictDatas) {
    if (dictData.value === value) {
      return dictData.label
    }
  }
  return ''
}
