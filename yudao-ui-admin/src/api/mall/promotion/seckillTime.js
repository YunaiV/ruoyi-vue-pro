import request from '@/utils/request'

// 创建秒杀时段
export function createSeckillTime(data) {
  return request({
    url: '/promotion/seckill-time/create',
    method: 'post',
    data: data
  })
}

// 更新秒杀时段
export function updateSeckillTime(data) {
  return request({
    url: '/promotion/seckill-time/update',
    method: 'put',
    data: data
  })
}

// 删除秒杀时段
export function deleteSeckillTime(id) {
  return request({
    url: '/promotion/seckill-time/delete?id=' + id,
    method: 'delete'
  })
}

// 获得秒杀时段
export function getSeckillTime(id) {
  return request({
    url: '/promotion/seckill-time/get?id=' + id,
    method: 'get'
  })
}

// 获得秒杀时段分页
export function getSeckillTimePage(query) {
  return request({
    url: '/promotion/seckill-time/page',
    method: 'get',
    params: query
  })
}

// 获取所有的秒杀时段
export function getSeckillTimeList() {
  return request({
    url: '/promotion/seckill-time/list',
    method: 'get'
  })
}

// 导出秒杀时段 Excel
export function exportSeckillTimeExcel(query) {
  return request({
    url: '/promotion/seckill-time/export-excel',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
