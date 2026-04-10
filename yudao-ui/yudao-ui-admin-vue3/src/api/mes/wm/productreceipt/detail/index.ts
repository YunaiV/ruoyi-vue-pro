import request from '@/config/axios'

// MES 产品收货单明细 VO
export interface WmProductRecptDetailVO {
  id: number
  lineId: number
  recptId: number
  itemId: number
  itemCode: string
  itemName: string
  specification: string
  unitMeasureName: string
  quantity: number
  batchId: number
  warehouseId: number
  warehouseName: string
  locationId: number
  locationName: string
  areaId: number
  areaName: string
  remark: string
  createTime: string
}

// MES 产品收货单明细 API
export const WmProductRecptDetailApi = {
  // 查询产品收货单明细详情
  getProductRecptDetail: async (id: number) => {
    return await request.get({ url: '/mes/wm/product-recpt-detail/get?id=' + id })
  },

  // 查询产品收货单明细列表（按行编号）
  getProductRecptDetailListByLineId: async (lineId: number) => {
    return await request.get({ url: '/mes/wm/product-recpt-detail/list-by-line?lineId=' + lineId })
  },

  // 新增产品收货单明细
  createProductRecptDetail: async (data: WmProductRecptDetailVO) => {
    return await request.post({ url: '/mes/wm/product-recpt-detail/create', data })
  },

  // 修改产品收货单明细
  updateProductRecptDetail: async (data: WmProductRecptDetailVO) => {
    return await request.put({ url: '/mes/wm/product-recpt-detail/update', data })
  },

  // 删除产品收货单明细
  deleteProductRecptDetail: async (id: number) => {
    return await request.delete({ url: '/mes/wm/product-recpt-detail/delete?id=' + id })
  }
}
