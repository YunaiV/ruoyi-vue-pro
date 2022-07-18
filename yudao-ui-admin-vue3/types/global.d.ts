declare interface Fn<T = any> {
  (...arg: T[]): T
}

declare type Nullable<T> = T | null

declare type ElRef<T extends HTMLElement = HTMLDivElement> = Nullable<T>

declare type ElememtPlusSize = 'default' | 'small' | 'large'

declare type ElementPlusInfoType = 'success' | 'info' | 'warning' | 'danger'

declare type Recordable<T = any, K = string> = Record<K extends null | undefined ? string : K, T>

declare type ComponentRef<T> = InstanceType<T>

declare type LocaleType = 'zh-CN' | 'en'

declare type AxiosHeaders =
  | 'application/json'
  | 'application/x-www-form-urlencoded'
  | 'multipart/form-data'

declare type AxiosMethod = 'get' | 'post' | 'delete' | 'put'

declare type AxiosResponseType = 'arraybuffer' | 'blob' | 'document' | 'json' | 'text' | 'stream'

declare type AxiosConfig = {
  params?: any
  data?: any
  url?: string
  method?: AxiosMethod
  headersType?: string
  responseType?: AxiosResponseType
}

declare type PageResult<T> = {
  list: T[]
  total?: number
}
