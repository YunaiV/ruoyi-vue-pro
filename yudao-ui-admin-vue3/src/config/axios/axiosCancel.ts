import type { AxiosRequestConfig, Canceler } from 'axios'
import axios from 'axios'
import { isFunction } from '@/utils/is'

// 用于存储每个请求的标识和取消功能
let pendingMap = new Map<string, Canceler>()

export const getPendingUrl = (config: AxiosRequestConfig) => [config.method, config.url].join('&')

export class AxiosCanceler {
  /**
   * 添加请求
   * @param {Object} config
   */
  addPending(config: AxiosRequestConfig) {
    this.removePending(config)
    const url = getPendingUrl(config)
    config.cancelToken =
      config.cancelToken ||
      new axios.CancelToken((cancel) => {
        if (!pendingMap.has(url)) {
          // If there is no current request in pending, add it
          pendingMap.set(url, cancel)
        }
      })
  }

  /**
   * @description: 清除所有待处理的
   */
  removeAllPending() {
    pendingMap.forEach((cancel) => {
      cancel && isFunction(cancel) && cancel()
    })
    pendingMap.clear()
  }

  /**
   * 删除请求
   * @param {Object} config
   */
  removePending(config: AxiosRequestConfig) {
    const url = getPendingUrl(config)

    if (pendingMap.has(url)) {
      // 如果挂起中有当前请求标识符，则需要取消并删除当前请求
      const cancel = pendingMap.get(url)
      cancel && cancel(url)
      pendingMap.delete(url)
    }
  }

  /** 重置 */
  reset(): void {
    pendingMap = new Map<string, Canceler>()
  }
}
