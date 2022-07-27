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
  isSetDict: boolean
  dictMap: Recordable
}

export const useDictStore = defineStore({
  id: 'dict',
  state: (): DictState => ({
    isSetDict: false,
    dictMap: {}
  }),
  persist: {
    enabled: true
  },
  getters: {
    getDictMap(): Recordable {
      return this.dictMap
    },
    getIsSetDict(): boolean {
      return this.isSetDict
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
    },
    setIsSetDict(isSetDict: boolean) {
      this.isSetDict = isSetDict
    }
  }
})

export const useDictStoreWithOut = () => {
  return useDictStore(store)
}
