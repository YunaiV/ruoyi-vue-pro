import { defineStore } from 'pinia'
import { store } from '../index'
import { DictDataVO } from '@/api/system/dict/types'
import { useCache } from '@/hooks/web/useCache'
const { wsCache } = useCache('sessionStorage')

export interface DictValueType {
  value: any
  label: string
  clorType?: string
  cssClass?: string
}
export interface DictTypeType {
  dictType: string
  dictValue: DictValueType[]
}
export interface DictState {
  dictMap: Map<string, any>
}

export const useDictStore = defineStore('dict', {
  state: (): DictState => ({
    dictMap: new Map<string, any>()
  }),
  getters: {
    getDictMap(): Recordable {
      const dictMap = wsCache.get('dictCache')
      return dictMap ? dictMap : this.dictMap
    },
    getHasDictData(): boolean {
      if (this.dictMap.size > 0) {
        return true
      } else {
        return false
      }
    }
  },
  actions: {
    setDictMap(dictMap: Recordable) {
      // 设置数据
      const dictDataMap = new Map<string, any>()
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
      this.dictMap = dictDataMap
      wsCache.set('dictCache', dictDataMap, { exp: 60 }) // 60 秒 过期
    }
  }
})

export const useDictStoreWithOut = () => {
  return useDictStore(store)
}
