/**  数据处理类，可根据项目配置 */
import type { AxiosRequestConfig, AxiosResponse } from 'axios'
import type { RequestOptions, RequestResult } from 'types/axios'

export interface CreateAxiosOptions extends AxiosRequestConfig {
  authenticationScheme?: string
  transform?: AxiosTransform
  requestOptions?: RequestOptions
}

export abstract class AxiosTransform {
  /** 请求前的流程配置 */
  beforeRequestHook?: (config: AxiosRequestConfig, options: RequestOptions) => AxiosRequestConfig

  /** 请求成功处理 */
  transformRequestHook?: (res: AxiosResponse<RequestResult>, options: RequestOptions) => any

  /** 请求失败处理 */
  requestCatchHook?: (e: Error, options: RequestOptions) => Promise<any>

  /** 请求之前的拦截器 */
  requestInterceptors?: (
    config: AxiosRequestConfig,
    options: CreateAxiosOptions
  ) => AxiosRequestConfig

  /** 请求之后的拦截器 */
  responseInterceptors?: (res: AxiosResponse<any>) => AxiosResponse<any>

  /** 请求之前的拦截器错误处理 */
  requestInterceptorsCatch?: (error: Error) => void

  /** 请求之后的拦截器错误处理 */
  responseInterceptorsCatch?: (error: Error) => void
}
