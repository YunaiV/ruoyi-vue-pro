import request from '@/utils/request'

// 创建文件配置
export function createFileConfig(data) {
  return request({
    url: '/infra/file-config/create',
    method: 'post',
    data: data
  })
}

// 更新文件配置
export function updateFileConfig(data) {
  return request({
    url: '/infra/file-config/update',
    method: 'put',
    data: data
  })
}

// 更新文件配置为主配置
export function updateFileConfigMaster(id) {
  return request({
    url: '/infra/file-config/update-master?id=' + id,
    method: 'put'
  })
}

// 删除文件配置
export function deleteFileConfig(id) {
  return request({
    url: '/infra/file-config/delete?id=' + id,
    method: 'delete'
  })
}

// 获得文件配置
export function getFileConfig(id) {
  return request({
    url: '/infra/file-config/get?id=' + id,
    method: 'get'
  })
}

// 获得文件配置分页
export function getFileConfigPage(query) {
  return request({
    url: '/infra/file-config/page',
    method: 'get',
    params: query
  })
}

export function testFileConfig(id) {
  return request({
    url: '/infra/file-config/test?id=' + id,
    method: 'get'
  })
}
