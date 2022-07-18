// axios配置  可自行根据项目进行更改，只需更改该文件即可，其他文件可以不动
// The axios configuration can be changed according to the project, just change the file, other files can be left unchanged

import type { AxiosResponse } from 'axios'
import type { RequestOptions, RequestResult } from 'types/axios'
import type { AxiosTransform, CreateAxiosOptions } from './axiosTransform'
import { VAxios } from './Axios'
import { checkStatus } from './checkStatus'
import { RequestEnum, ResultEnum, ContentTypeEnum } from '@/enums/http.enum'
import { useCache } from '@/hooks/web/useCache'
import { isString } from '@/utils/is'
import { getAccessToken, setToken } from '@/utils/auth'
import { setObjToUrlParams, deepMerge } from './utils'
import { useI18n } from '@/hooks/web/useI18n'
import { joinTimestamp, formatRequestDate } from './helper'
import { ElMessage, ElMessageBox } from 'element-plus'
import { refreshToken } from '@/api/login'

const { t } = useI18n()
const { wsCache } = useCache()
const tenantEnable = import.meta.env.VITE_APP_TENANT_ENABLE
const BASE_URL = import.meta.env.VITE_BASE_URL
const BASE_API = import.meta.env.VITE_API_URL
const apiUrl = BASE_URL + BASE_API
// 是否显示重新登录
// export let isRelogin = { show: false }
// TODO 请求队列
// let requestList = []
// 是否正在刷新中
let isRefreshToken = false
/**
 * @description: 数据处理，方便区分多种处理方式
 */
const transform: AxiosTransform = {
  /**
   * @description: 处理请求数据。如果数据不是预期格式，可直接抛出错误
   */
  transformRequestHook: async (res: AxiosResponse<RequestResult>, options: RequestOptions) => {
    const { isTransformResponse, isReturnNativeResponse } = options
    // 是否返回原生响应头 比如：需要获取响应头时使用该属性
    if (isReturnNativeResponse) {
      return res
    }
    // 不进行任何处理，直接返回
    // 用于页面代码可能需要直接获取code，data，message这些信息时开启
    if (!isTransformResponse) {
      return res.data
    }
    // 错误的时候返回

    const { data } = res
    if (!data) {
      // 返回“[HTTP]请求没有返回值”;
      throw new Error(t('sys.api.apiRequestFailed'))
    }
    //  这里 code，result，message为 后台统一的字段，需要在 types.ts内修改为项目自己的接口返回格式
    const { code, msg } = data
    const result = data.data
    // TODO 输出res 方便调试，完成后删除
    // console.info('data')
    // console.info(data)
    // console.info('result')
    // console.info(result)
    // TODO 芋艿：文件下载，需要特殊处理
    if (code === undefined) {
      console.log(res)
      return res.data
    }

    // 这里逻辑可以根据项目进行修改
    const hasSuccess = data && Reflect.has(data, 'code') && code === ResultEnum.SUCCESS
    if (hasSuccess) {
      return result
    }

    // 在此处根据自己项目的实际情况对不同的code执行不同的操作
    // 如果不希望中断当前请求，请return数据，否则直接抛出异常即可
    let timeoutMsg = ''
    switch (code) {
      case ResultEnum.TIMEOUT:
        // TODO 未完成
        // 如果未认证，并且未进行刷新令牌，说明可能是访问令牌过期了
        if (!isRefreshToken) {
          isRefreshToken = true
          const refreshTokenRes = await refreshToken()
          // 1. 如果获取不到刷新令牌，则只能执行登出操作
          if (!refreshTokenRes) {
            timeoutMsg = t('sys.api.timeoutMessage')
            wsCache.clear() // 清除浏览器全部临时缓存
            ElMessageBox.confirm(timeoutMsg, {
              confirmButtonText: t('login.relogin'),
              cancelButtonText: t('common.cancel'),
              type: 'warning'
            })
              .then(() => {})
              .catch(() => {})
            break
          } else {
            // 2. 进行刷新访问令牌
            // 2.1 刷新成功，则回放队列的请求 + 当前请求
            setToken(refreshTokenRes.data)
          }
        }
      default:
        if (msg) {
          timeoutMsg = msg
        }
    }

    // errorMessageMode=‘modal’的时候会显示modal错误弹窗，而不是消息提示，用于一些比较重要的错误
    // errorMessageMode='none' 一般是调用时明确表示不希望自动弹出错误提示
    if (options.errorMessageMode === 'modal') {
      await ElMessageBox.confirm(timeoutMsg, {
        type: 'error'
      })
    } else if (options.errorMessageMode === 'message') {
      ElMessage.error(timeoutMsg)
    }

    throw new Error(timeoutMsg || t('sys.api.apiRequestFailed'))
  },

  // 请求之前处理config
  beforeRequestHook: (config, options) => {
    const { apiUrl, joinParamsToUrl, formatDate, joinTime = true } = options

    if (apiUrl && isString(apiUrl)) {
      config.url = `${apiUrl}${config.url}`
    }
    const params = config.params || {}
    const data = config.data || false
    formatDate && data && !isString(data) && formatRequestDate(data)
    if (config.method?.toUpperCase() === RequestEnum.GET) {
      if (!isString(params)) {
        // 给 get 请求加上时间戳参数，避免从缓存中拿数据。
        config.params = Object.assign(params || {}, joinTimestamp(joinTime, false))
      } else {
        // 兼容restful风格
        config.url = config.url + params + `${joinTimestamp(joinTime, true)}`
        config.params = undefined
      }
    } else {
      if (!isString(params)) {
        formatDate && formatRequestDate(params)
        if (Reflect.has(config, 'data') && config.data && Object.keys(config.data).length > 0) {
          config.data = data
          config.params = params
        } else {
          // 非GET请求如果没有提供data，则将params视为data
          config.data = params
          config.params = undefined
        }
        if (joinParamsToUrl) {
          config.url = setObjToUrlParams(
            config.url as string,
            Object.assign({}, config.params, config.data)
          )
        }
      } else {
        // 兼容restful风格
        config.url = config.url + params
        config.params = undefined
      }
    }
    return config
  },

  /**
   * @description: 请求拦截器处理
   */
  requestInterceptors: (config, options) => {
    // 请求之前处理config
    const token = getAccessToken()
    if (token && (config as Recordable)?.requestOptions?.withToken !== false) {
      // jwt token
      ;(config as Recordable).headers.Authorization = options.authenticationScheme
        ? `${options.authenticationScheme} ${token}`
        : token
    }
    // 设置租户
    if (tenantEnable) {
      const tenantId = wsCache.get('tenantId')
      if (tenantId) (config as Recordable).headers.common['tenant-id'] = tenantId
    }
    return config
  },

  /**
   * @description: 响应拦截器处理
   */
  responseInterceptors: (res: AxiosResponse<any>) => {
    return res
  },

  /**
   * @description: 响应错误处理
   */
  responseInterceptorsCatch: (error: any) => {
    const { response, code, message, config } = error || {}
    const errorMessageMode = config?.requestOptions?.errorMessageMode || 'none'
    const msg: string = response?.data?.msg ?? ''
    const err: string = error?.toString?.() ?? ''
    let errMessage = ''

    try {
      if (code === 'ECONNABORTED' && message.indexOf('timeout') !== -1) {
        errMessage = t('sys.api.apiTimeoutMessage')
      }
      if (err?.includes('Network Error')) {
        errMessage = t('sys.api.networkExceptionMsg')
      }

      if (errMessage) {
        if (errorMessageMode === 'modal') {
          ElMessageBox.confirm(errMessage, {
            type: 'error'
          })
        } else if (errorMessageMode === 'message') {
          ElMessage.error(errMessage)
        }
        return Promise.reject(error)
      }
    } catch (error) {
      throw new Error(error as unknown as string)
    }

    checkStatus(error?.response?.status, msg, errorMessageMode)
    return Promise.reject(error)
  }
}

function createAxios(opt?: Partial<CreateAxiosOptions>) {
  return new VAxios(
    deepMerge(
      {
        // See https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication#authentication_schemes
        // authentication schemes，e.g: Bearer
        // authenticationScheme: 'Bearer',
        authenticationScheme: 'Bearer',
        timeout: 10 * 1000,
        // 基础接口地址
        // baseURL: globSetting.apiUrl,

        headers: { 'Content-Type': ContentTypeEnum.JSON },
        // 如果是form-data格式
        // headers: { 'Content-Type': ContentTypeEnum.FORM_URLENCODED },
        // 数据处理方式
        transform,
        // 配置项，下面的选项都可以在独立的接口请求中覆盖
        requestOptions: {
          // 默认将prefix 添加到url
          joinPrefix: true,
          // 是否返回原生响应头 比如：需要获取响应头时使用该属性
          isReturnNativeResponse: false,
          // 需要对返回数据进行处理
          isTransformResponse: true,
          // post请求的时候添加参数到url
          joinParamsToUrl: false,
          // 格式化提交参数时间
          formatDate: true,
          // 消息提示类型
          errorMessageMode: 'message',
          // 接口地址
          apiUrl: apiUrl,
          //  是否加入时间戳
          joinTime: true,
          // 忽略重复请求
          ignoreCancelToken: true,
          // 是否携带token
          withToken: true
        }
      },
      opt || {}
    )
  )
}
export const defHttp = createAxios()

// other api url
// export const otherHttp = createAxios({
//   requestOptions: {
//     apiUrl: 'xxx',
//     urlPrefix: 'xxx',
//   },
// });
