import { useAxios } from '@/hooks/web/useAxios'
import type { SmsTemplateVO } from './types'

const request = useAxios()

// 查询短信模板列表
export const getSmsTemplatePageApi = (params) => {
  return request.get({ url: '/system/sms-template/page', params })
}

// 查询短信模板详情
export const getSmsTemplateApi = (id: number) => {
  return request.get({ url: '/system/sms-template/get?id=' + id })
}

// 新增短信模板
export const createSmsTemplateApi = (data: SmsTemplateVO) => {
  return request.post({ url: '/system/sms-template/create', data })
}

// 修改短信模板
export const updateSmsTemplateApi = (data: SmsTemplateVO) => {
  return request.put({ url: '/system/sms-template/update', data })
}

// 删除短信模板
export const deleteSmsTemplateApi = (id: number) => {
  return request.delete({ url: '/system/sms-template/delete?id=' + id })
}

// 发送短信
export const sendSmsApi = (data) => {
  return request.post({ url: '/system/sms-template/send-sms', data })
}

// 导出短信模板
export const exportPostApi = (params) => {
  return request.download({
    url: '/system/sms-template/export-excel',
    params
  })
}
