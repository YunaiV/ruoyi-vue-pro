import request from '@/utils/request'

// 创建满减送活动
export function createRewardActivity(data) {
  return request({
    url: '/promotion/reward-activity/create',
    method: 'post',
    data: data
  })
}

// 更新满减送活动
export function updateRewardActivity(data) {
  return request({
    url: '/promotion/reward-activity/update',
    method: 'put',
    data: data
  })
}

// 关闭满减送活动
export function closeRewardActivity(id) {
  return request({
    url: '/promotion/reward-activity/close?id=' + id,
    method: 'put'
  })
}

// 删除满减送活动
export function deleteRewardActivity(id) {
  return request({
    url: '/promotion/reward-activity/delete?id=' + id,
    method: 'delete'
  })
}

// 获得满减送活动
export function getRewardActivity(id) {
  return request({
    url: '/promotion/reward-activity/get?id=' + id,
    method: 'get'
  })
}

// 获得满减送活动分页
export function getRewardActivityPage(query) {
  return request({
    url: '/promotion/reward-activity/page',
    method: 'get',
    params: query
  })
}
