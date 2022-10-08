import { defineStore } from 'pinia'
import { store } from '../index'
import { DictDataVO } from '@/api/system/dict/types'

export interface DictValueType {
  value: string
  label: string
  clorType: string
  cssClass: string
}
export interface DictTypeType {
  dictType: string
  dictValue: DictValueType[]
}
export interface DictState {
  dictMap: Recordable
}

export const useDictStore = defineStore('dict', {
  state: (): DictState => ({
    dictMap: {}
  }),
  getters: {
    getDictMap(): Recordable {
      return this.dictMap
    },
    getHasDictData(): boolean {
      if (this.dictMap.length > 0) {
        return true
      } else {
        return false
      }
    }
  },
  actions: {
    setDictMap(dictMap: Recordable) {
      // 设置数据
      const dictDataMap = {}
      dictMap.forEach((dictData: DictDataVO) => {
        // 获得 dictType 层级
        const enumValueObj = dictDataMap[dictData.dictType]
        if (!enumValueObj) {
          dictDataMap[dictData.dictType] = []
        }
        // 处理 dictValue 层级
        dictDataMap[dictData.dictType].push({
          value: dictData.value,
          label: dictData.label,
          colorType: dictData.colorType,
          cssClass: dictData.cssClass
        })
      })
      this.dictMap = dictMap
    }
  }
})

export const useDictStoreWithOut = () => {
  return useDictStore(store)
}
