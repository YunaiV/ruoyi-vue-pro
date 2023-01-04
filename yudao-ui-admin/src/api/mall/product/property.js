import request from '@/utils/request'

// ------------------------ 属性项 -------------------

// 创建属性项
export function createProperty(data) {
  return request({
    url: '/product/property/create',
    method: 'post',
    data: data
  })
}

// 更新属性项
export function updateProperty(data) {
  return request({
    url: '/product/property/update',
    method: 'put',
    data: data
  })
}

// 删除属性项
export function deleteProperty(id) {
  return request({
    url: '/product/property/delete?id=' + id,
    method: 'delete'
  })
}

// 获得属性项
export function getProperty(id) {
  return request({
    url: '/product/property/get?id=' + id,
    method: 'get'
  })
}

// 获得属性项分页
export function getPropertyPage(query) {
  return request({
    url: '/product/property/page',
    method: 'get',
    params: query
  })
}

// 获得属性项列表
export function getPropertyList(query) {
  return request({
    url: '/product/property/list',
    method: 'get',
    params: query
  })
}

// 获得属性项列表
export function getPropertyListAndValue(query) {
  return request({
    url: '/product/property/get-value-list',
    method: 'get',
    params: query
  })
}

// ------------------------ 属性值 -------------------

// 获得属性值分页
export function getPropertyValuePage(query) {
  return request({
    url: '/product/property/value/page',
    method: 'get',
    params: query
  })
}

// 获得属性值
export function getPropertyValue(id) {
  return request({
    url: '/product/property/value/get?id=' + id,
    method: 'get'
  })
}


// 创建属性值
export function createPropertyValue(data) {
  return request({
    url: '/product/property/value/create',
    method: 'post',
    data: data
  })
}

// 更新属性值
export function updatePropertyValue(data) {
  return request({
    url: '/product/property/value/update',
    method: 'put',
    data: data
  })
}

// 删除属性值
export function deletePropertyValue(id) {
  return request({
    url: '/product/property/value/delete?id=' + id,
    method: 'delete'
  })
}

export class exportPropertyExcel {
}
