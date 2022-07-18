import { defHttp } from '@/config/axios'
import type { SmsTemplateVO, SmsSendVO } from './types'

// 查询短信模板列表
export const getSmsTemplatePageApi = ({ params }) => {
  return defHttp.get<PageResult<SmsTemplateVO>>({ url: '/system/sms-template/page', params })
}

// 查询短信模板详情
export const getSmsTemplateApi = (id: number) => {
  return defHttp.get<SmsTemplateVO>({ url: '/system/sms-template/get?id=' + id })
}

// 新增短信模板
export const createSmsTemplateApi = (params: SmsTemplateVO) => {
  return defHttp.post({ url: '/system/sms-template/create', params })
}

// 修改短信模板
export const updateSmsTemplateApi = (params: SmsTemplateVO) => {
  return defHttp.put({ url: '/system/sms-template/update', params })
}

// 删除短信模板
export const deleteSmsTemplateApi = (id: number) => {
  return defHttp.delete({ url: '/system/sms-template/delete?id=' + id })
}

// 发送短信
export function sendSms(params: SmsSendVO) {
  return defHttp.post({ url: '/system/sms-template/send-sms', params })
}

// 导出短信模板
export const exportPostApi = (params) => {
  return defHttp.get({ url: '/system/sms-template/export-excel', params, responseType: 'blob' })
}
