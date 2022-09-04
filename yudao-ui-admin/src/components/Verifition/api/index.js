/**
 * 此处可直接引用自己项目封装好的 axios 配合后端联调
 */

import request from './../utils/axios' // 组件内部封装的axios
// import request from "@/api/axios.js"       //调用项目封装的axios

// 获取验证图片  以及token
export function reqGet(data) {
  return request({
    url: '/captcha/get',
    method: 'post',
    data
  })
}

// 滑动或者点选验证
export function reqCheck(data) {
  return request({
    url: '/captcha/check',
    method: 'post',
    data
  })
}

