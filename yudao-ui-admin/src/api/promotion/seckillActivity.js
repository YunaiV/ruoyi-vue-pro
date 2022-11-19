import request from '@/utils/request'

// 创建秒杀活动
export function createSeckillActivity(data) {
  return request({
    url: '/promotion/seckill-activity/create',
    method: 'post',
    data: data
  })
}

// 更新秒杀活动
export function updateSeckillActivity(data) {
  return request({
    url: '/promotion/seckill-activity/update',
    method: 'put',
    data: data
  })
}

// 删除秒杀活动
export function deleteSeckillActivity(id) {
  return request({
    url: '/promotion/seckill-activity/delete?id=' + id,
    method: 'delete'
  })
}

// 获得秒杀活动
export function getSeckillActivity(id) {
  return request({
    url: '/promotion/seckill-activity/get?id=' + id,
    method: 'get'
  })
}

// 获得秒杀活动分页
export function getSeckillActivityPage(query) {
  return request({
    url: '/promotion/seckill-activity/page',
    method: 'get',
    params: query
  })
}

// 导出秒杀活动 Excel
export function exportSeckillActivityExcel(query) {
  return request({
    url: '/promotion/seckill-activity/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
