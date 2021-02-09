import request from '@/utils/request'

// 获得表定义分页
export function getCodeGenTablePage(query) {
  return request({
    url: '/tool/codegen/page',
    method: 'get',
    params: query
  })
}

// 获得表和字段的明细
export function getCodeGenDetail(tableId) {
  return request({
    url: '/tool/codegen/detail?tableId=' + tableId,
    method: 'get',
  })
}
