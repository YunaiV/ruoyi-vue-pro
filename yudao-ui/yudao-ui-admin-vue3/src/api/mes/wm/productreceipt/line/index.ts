import request from '@/config/axios'

// MES 产品收货单行 VO
export interface WmProductRecptLineVO {
  id: number
  recptId: number
  itemId: number
  itemCode: string
  itemName: string
  specification: string
  unitMeasureName: string
  receivedQuantity: number
  batchId: number
  batchCode: string
  remark: string
  createTime: string
}

// MES 产品收货单行 API
export const WmProductRecptLineApi = {
  // 查询产品收货单行分页
  getProductRecptLinePage: async (params: any) => {
    return await request.get({ url: '/mes/wm/product-recpt-line/page', params })
  },

  // 查询产品收货单行详情
  getProductRecptLine: async (id: number) => {
    return await request.get({ url: '/mes/wm/product-recpt-line/get?id=' + id })
  },

  // 新增产品收货单行
  createProductRecptLine: async (data: WmProductRecptLineVO) => {
    return await request.post({ url: '/mes/wm/product-recpt-line/create', data })
  },

  // 修改产品收货单行
  updateProductRecptLine: async (data: WmProductRecptLineVO) => {
    return await request.put({ url: '/mes/wm/product-recpt-line/update', data })
  },

  // 删除产品收货单行
  deleteProductRecptLine: async (id: number) => {
    return await request.delete({ url: '/mes/wm/product-recpt-line/delete?id=' + id })
  }
}
