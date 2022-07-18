import type { ErrorMessageMode } from 'types/axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from '@/hooks/web/useI18n'
import { useCache } from '@/hooks/web/useCache'

const { wsCache } = useCache()

export function checkStatus(
  status: number,
  msg: string,
  errorMessageMode: ErrorMessageMode = 'message'
): void {
  const { t } = useI18n()
  let errMessage = ''

  switch (status) {
    case 400:
      errMessage = `${msg}`
      break
    // 401: Not logged in
    // 如果未登录，跳转到登录页面，并携带当前页面的路径
    // 成功登录后返回当前页面。此步骤需要在登录页面上操作。
    case 401:
      wsCache.clear()
      errMessage = msg || t('sys.api.errMsg401')
      break
    case 403:
      errMessage = t('sys.api.errMsg403')
      break
    // 404请求不存在
    case 404:
      errMessage = t('sys.api.errMsg404')
      break
    case 405:
      errMessage = t('sys.api.errMsg405')
      break
    case 408:
      errMessage = t('sys.api.errMsg408')
      break
    case 500:
      errMessage = t('sys.api.errMsg500')
      break
    case 501:
      errMessage = t('sys.api.errMsg501')
      break
    case 502:
      errMessage = t('sys.api.errMsg502')
      break
    case 503:
      errMessage = t('sys.api.errMsg503')
      break
    case 504:
      errMessage = t('sys.api.errMsg504')
      break
    case 505:
      errMessage = t('sys.api.errMsg505')
      break
    case 901:
      errMessage = t('sys.api.errMsg505')
      break
    default:
  }

  if (errMessage) {
    if (errorMessageMode === 'modal') {
      ElMessageBox.confirm(errMessage, {
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      })
    } else if (errorMessageMode === 'message') {
      ElMessage.error(errMessage)
    }
  }
}
