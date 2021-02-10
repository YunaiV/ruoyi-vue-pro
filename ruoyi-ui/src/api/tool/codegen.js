import request from '@/utils/request'

// 获得表定义分页
export function getCodeGenTablePage(query) {
  return request({
    url: '/tool/codegen/table/page',
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

// 修改代码生成信息
export function updateCodegen(data) {
  return request({
    url: '/tool/codegen/update',
    method: 'put',
    data: data
  })
}
