import { useCache } from '@/hooks/web/useCache'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'

const { wsCache } = useCache()

export const elLocaleMap = {
  'zh-CN': zhCn,
  en: en
}
export interface LocaleState {
  currentLocale: LocaleDropdownType
  localeMap: LocaleDropdownType[]
}

export const localeModules: LocaleState = {
  currentLocale: {
    lang: wsCache.get('lang') || 'zh-CN',
    elLocale: elLocaleMap[wsCache.get('lang') || 'zh-CN']
  },
  // 多语言
  localeMap: [
    {
      lang: 'zh-CN',
      name: '简体中文'
    },
    {
      lang: 'en',
      name: 'English'
    }
  ]
}
