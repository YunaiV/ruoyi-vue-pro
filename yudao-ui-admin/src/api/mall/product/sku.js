import request from '@/utils/request'

// 获得商品 SKU 选项的列表
export function getSkuOptionList() {
  return request({
    url: '/product/sku/get-option-list',
    method: 'get',
  })
}
