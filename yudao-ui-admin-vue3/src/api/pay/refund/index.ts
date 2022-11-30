import request from '@/config/axios'

export interface RefundVO {
  id: number
  merchantId: number
  appId: number
  channelId: number
  channelCode: string
  orderId: string
  tradeNo: string
  merchantOrderId: string
  merchantRefundNo: string
  notifyUrl: string
  notifyStatus: number
  status: number
  type: number
  payAmount: number
  refundAmount: number
  reason: string
  userIp: string
  channelOrderNo: string
  channelRefundNo: string
  channelErrorCode: string
  channelErrorMsg: string
  channelExtras: string
  expireTime: Date
  successTime: Date
  notifyTime: Date
  createTime: Date
}

export interface RefundPageReqVO extends PageParam {
  merchantId?: number
  appId?: number
  channelId?: number
  channelCode?: string
  orderId?: string
  tradeNo?: string
  merchantOrderId?: string
  merchantRefundNo?: string
  notifyUrl?: string
  notifyStatus?: number
  status?: number
  type?: number
  payAmount?: number
  refundAmount?: number
  reason?: string
  userIp?: string
  channelOrderNo?: string
  channelRefundNo?: string
  channelErrorCode?: string
  channelErrorMsg?: string
  channelExtras?: string
  expireTime?: Date[]
  successTime?: Date[]
  notifyTime?: Date[]
  createTime?: Date[]
}

export interface PayRefundExportReqVO {
  merchantId?: number
  appId?: number
  channelId?: number
  channelCode?: string
  orderId?: string
  tradeNo?: string
  merchantOrderId?: string
  merchantRefundNo?: string
  notifyUrl?: string
  notifyStatus?: number
  status?: number
  type?: number
  payAmount?: number
  refundAmount?: number
  reason?: string
  userIp?: string
  channelOrderNo?: string
  channelRefundNo?: string
  channelErrorCode?: string
  channelErrorMsg?: string
  channelExtras?: string
  expireTime?: Date[]
  successTime?: Date[]
  notifyTime?: Date[]
  createTime?: Date[]
}

// 查询列表退款订单
export const getRefundPageApi = (params: RefundPageReqVO) => {
  return request.get({ url: '/pay/refund/page', params })
}

// 查询详情退款订单
export const getRefundApi = (id: number) => {
  return request.get({ url: '/pay/refund/get?id=' + id })
}

// 新增退款订单
export const createRefundApi = (data: RefundVO) => {
  return request.post({ url: '/pay/refund/create', data })
}

// 修改退款订单
export const updateRefundApi = (data: RefundVO) => {
  return request.put({ url: '/pay/refund/update', data })
}

// 删除退款订单
export const deleteRefundApi = (id: number) => {
  return request.delete({ url: '/pay/refund/delete?id=' + id })
}

// 导出退款订单
export const exportRefundApi = (params: PayRefundExportReqVO) => {
  return request.download({ url: '/pay/refund/export-excel', params })
}
